/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalFlowServiceImpl implements SalFlowService {
    private static final Logger LOG = LoggerFactory.getLogger(SalFlowServiceImpl.class);
    private final FlowService<UpdateFlowOutput> flowUpdate;
    private final FlowService<AddFlowOutput> flowAdd;
    private final FlowRemoveService flowRemove;

    public SalFlowServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        flowRemove = new FlowRemoveService(requestContextStack, deviceContext);
        flowAdd = new FlowService<>(requestContextStack, deviceContext, AddFlowOutput.class);
        flowUpdate = new FlowService<>(requestContextStack, deviceContext, UpdateFlowOutput.class);
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        final ListenableFuture<RpcResult<AddFlowOutput>> future = flowAdd.processFlowModInputBuilders(flowAdd.toFlowModInputs(input));
        final FlowId flowId;
        if (null != input.getFlowRef()) {
            flowId = input.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId();
        } else {
            flowId = FlowUtil.createAlienFlowId(input.getTableId());
        }

        final DeviceContext deviceContext = flowAdd.getDeviceContext();
        final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(input);
        final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(input.getTableId(), flowId);
        deviceContext.getDeviceFlowRegistry().store(flowRegistryKey, flowDescriptor);
        Futures.addCallback(future, new FutureCallback<RpcResult<AddFlowOutput>>() {
            @Override
            public void onSuccess(final RpcResult<AddFlowOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    LOG.debug("flow add finished without error, id={}", flowId.getValue());
                } else {
                    LOG.debug("flow add failed with error, id={}", flowId.getValue());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                deviceContext.getDeviceFlowRegistry().markToBeremoved(flowRegistryKey);
                LOG.trace("Service call for adding flows failed, id={}.", flowId.getValue(), throwable);
            }
        });

        return future;
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        LOG.trace("Calling remove flow for flow with ID ={}.", input.getFlowRef());

        final ListenableFuture<RpcResult<RemoveFlowOutput>> future = flowRemove.handleServiceCall(input);
        Futures.addCallback(future, new FutureCallback<RpcResult<RemoveFlowOutput>>() {
            @Override
            public void onSuccess(final RpcResult<RemoveFlowOutput> result) {
                if (result.isSuccessful()) {
                    FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(input);
                    flowRemove.getDeviceContext().getDeviceFlowRegistry().markToBeremoved(flowRegistryKey);
                } else {
                    if (LOG.isTraceEnabled()) {
                        StringBuilder errors = new StringBuilder();
                        Collection<RpcError> rpcErrors = result.getErrors();
                        if (null != rpcErrors && rpcErrors.size() > 0) {
                            for (RpcError rpcError : rpcErrors) {
                                errors.append(rpcError.getMessage());
                            }
                        }
                        LOG.trace("Flow modification failed. Errors : {}", errors.toString());
                    }
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.trace("Flow modification failed..", throwable);
            }
        });

        return future;
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
        final UpdateFlowInput in = input;
        final UpdatedFlow updated = in.getUpdatedFlow();
        final OriginalFlow original = in.getOriginalFlow();

        final List<FlowModInputBuilder> allFlowMods = new ArrayList<>();
        final List<FlowModInputBuilder> ofFlowModInputs;

        if (!FlowCreatorUtil.canModifyFlow(original, updated, flowUpdate.getVersion())) {
            // We would need to remove original and add updated.

            // remove flow
            final RemoveFlowInputBuilder removeflow = new RemoveFlowInputBuilder(original);
            final List<FlowModInputBuilder> ofFlowRemoveInput = flowUpdate.toFlowModInputs(removeflow.build());
            // remove flow should be the first
            allFlowMods.addAll(ofFlowRemoveInput);
            final AddFlowInputBuilder addFlowInputBuilder = new AddFlowInputBuilder(updated);
            ofFlowModInputs = flowUpdate.toFlowModInputs(addFlowInputBuilder.build());
        } else {
            ofFlowModInputs = flowUpdate.toFlowModInputs(updated);
        }

        allFlowMods.addAll(ofFlowModInputs);
        ListenableFuture<RpcResult<UpdateFlowOutput>> future = flowUpdate.processFlowModInputBuilders(allFlowMods);
        Futures.addCallback(future, new FutureCallback<RpcResult<UpdateFlowOutput>>() {
            @Override
            public void onSuccess(final RpcResult<UpdateFlowOutput> o) {
                FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(original);

                FlowRegistryKey updatedflowRegistryKey = FlowRegistryKeyFactory.create(updated);
                FlowId flowId = input.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId();
                FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(updated.getTableId(), flowId);
                final DeviceFlowRegistry deviceFlowRegistry = flowUpdate.getDeviceContext().getDeviceFlowRegistry();
                deviceFlowRegistry.markToBeremoved(flowRegistryKey);
                deviceFlowRegistry.store(updatedflowRegistryKey, flowDescriptor);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.debug("Flow update failed", throwable);
            }
        });
        return future;
    }
}
