/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalFlowServiceImpl implements SalFlowService, ItemLifeCycleSource {
    private static final Logger LOG = LoggerFactory.getLogger(SalFlowServiceImpl.class);
    private final FlowService<UpdateFlowOutput> flowUpdate;
    private final FlowService<AddFlowOutput> flowAdd;
    private final FlowService<RemoveFlowOutput> flowRemove;
    private final DeviceContext deviceContext;
    private ItemLifecycleListener itemLifecycleListener;

    public SalFlowServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final ConvertorManager convertorManager) {
        this.deviceContext = deviceContext;
        flowRemove = new FlowService<>(requestContextStack, deviceContext, RemoveFlowOutput.class, convertorManager);
        flowAdd = new FlowService<>(requestContextStack, deviceContext, AddFlowOutput.class, convertorManager);
        flowUpdate = new FlowService<>(requestContextStack, deviceContext, UpdateFlowOutput.class, convertorManager);
    }

    @Override
    public void setItemLifecycleListener(@Nullable ItemLifecycleListener itemLifecycleListener) {
        this.itemLifecycleListener = itemLifecycleListener;
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        final FlowId flowId;
        if (null != input.getFlowRef()) {
            flowId = input.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId();
        } else {
            flowId = FlowUtil.createAlienFlowId(input.getTableId());
        }
        LOG.trace("Calling add flow for flow with ID ={}.", flowId);
        final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(input);
        final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(input.getTableId(), flowId);
        deviceContext.getDeviceFlowRegistry().store(flowRegistryKey, flowDescriptor);
        final ListenableFuture<RpcResult<AddFlowOutput>> future =
                flowAdd.processFlowModInputBuilders(flowAdd.toFlowModInputs(input));
        Futures.addCallback(future, new FutureCallback<RpcResult<AddFlowOutput>>() {
            @Override
            public void onSuccess(final RpcResult<AddFlowOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("flow add with id={},finished without error,", flowId.getValue());
                    }
                    if (itemLifecycleListener != null) {
                        KeyedInstanceIdentifier<Flow, FlowKey> flowPath = createFlowPath(flowDescriptor,
                                deviceContext.getDeviceInfo().getNodeInstanceIdentifier());
                        final FlowBuilder flowBuilder = new FlowBuilder(input).setId(flowDescriptor.getFlowId());
                        itemLifecycleListener.onAdded(flowPath, flowBuilder.build());
                    }
                } else {
                LOG.error("flow add failed for id={}, errors={}", flowId.getValue(), errorsToString(rpcResult.getErrors()));
            }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                deviceContext.getDeviceFlowRegistry().markToBeremoved(flowRegistryKey);
                LOG.error("Service call for adding flow with  id={} failed, reason {} .", flowId.getValue(), throwable);
            }
        });

        return future;
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        LOG.trace("Calling remove flow for flow with ID ={}.", input.getFlowRef());

        final ListenableFuture<RpcResult<RemoveFlowOutput>> future =
                flowRemove.processFlowModInputBuilders(flowRemove.toFlowModInputs(input));
        Futures.addCallback(future, new FutureCallback<RpcResult<RemoveFlowOutput>>() {
            @Override
            public void onSuccess(final RpcResult<RemoveFlowOutput> result) {
                if (result.isSuccessful()) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("flow removed finished without error,");
                    }
                    FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(input);
                    deviceContext.getDeviceFlowRegistry().markToBeremoved(flowRegistryKey);
                    if (itemLifecycleListener != null) {
                        final FlowDescriptor flowDescriptor =
                                deviceContext.getDeviceFlowRegistry().retrieveIdForFlow(flowRegistryKey);
                        if (flowDescriptor != null) {
                            KeyedInstanceIdentifier<Flow, FlowKey> flowPath = createFlowPath(flowDescriptor,
                                    deviceContext.getDeviceInfo().getNodeInstanceIdentifier());
                            itemLifecycleListener.onRemoved(flowPath);
                        }
                    }
                } else {
                    LOG.error("Flow remove failed with errors : {}",errorsToString(result.getErrors()));
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Service call for removing flow with id {} failed ,reason {}",input.getFlowRef().getValue(), throwable);
            }
        });

        return future;
    }

    private final String errorsToString(final Collection<RpcError> rpcErrors) {
        final StringBuilder errors = new StringBuilder();
        if ((null != rpcErrors) && (rpcErrors.size() > 0)) {
            for (final RpcError rpcError : rpcErrors) {
                errors.append(rpcError.getMessage());
            }
        }
        return errors.toString();
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
                final FlowRef flowRef = input.getFlowRef();
                final DeviceFlowRegistry deviceFlowRegistry = deviceContext.getDeviceFlowRegistry();

                if (flowRef == null) { //then this is equivalent to a delete
                    deviceFlowRegistry.markToBeremoved(flowRegistryKey);

                    if (itemLifecycleListener != null) {
                        final FlowDescriptor flowDescriptor =
                                deviceContext.getDeviceFlowRegistry().retrieveIdForFlow( flowRegistryKey);
                        KeyedInstanceIdentifier<Flow, FlowKey> flowPath = createFlowPath(flowDescriptor,
                                deviceContext.getDeviceInfo().getNodeInstanceIdentifier());
                        itemLifecycleListener.onRemoved(flowPath);
                    }
                } else { //this is either an add or an update
                    final FlowId flowId = flowRef.getValue().firstKeyOf(Flow.class, FlowKey.class).getId();
                    final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(updated.getTableId(), flowId);
                    deviceFlowRegistry.store(updatedflowRegistryKey, flowDescriptor);

                    if (itemLifecycleListener != null) {
                        KeyedInstanceIdentifier<Flow, FlowKey> flowPath = createFlowPath(flowDescriptor,
                                deviceContext.getDeviceInfo().getNodeInstanceIdentifier());
                        final FlowBuilder flowBuilder = new FlowBuilder(
                                                    input.getUpdatedFlow()).setId(flowDescriptor.getFlowId());

                        boolean isUpdate = null !=
                                            deviceFlowRegistry.retrieveIdForFlow(flowRegistryKey);
                        if (isUpdate) {
                            itemLifecycleListener.onUpdated(flowPath, flowBuilder.build());
                        } else {
                            itemLifecycleListener.onAdded(flowPath, flowBuilder.build());
                        }
                    }


                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Service call for updating flow failed, reason{}", throwable);
            }
        });
        return future;
    }

    @VisibleForTesting
    static KeyedInstanceIdentifier<Flow, FlowKey> createFlowPath(FlowDescriptor flowDescriptor,
                                                                 KeyedInstanceIdentifier<Node, NodeKey> nodePath) {
        return nodePath.augmentation(FlowCapableNode.class)
                .child(Table.class, flowDescriptor.getTableKey())
                .child(Flow.class, new FlowKey(flowDescriptor.getFlowId()));
    }
}
