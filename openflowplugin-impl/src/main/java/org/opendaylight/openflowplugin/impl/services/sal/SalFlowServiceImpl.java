/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerFlowService;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerFlowService;
import org.opendaylight.openflowplugin.impl.util.ErrorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalFlowServiceImpl implements SalFlowService {
    private static final Logger LOG = LoggerFactory.getLogger(SalFlowServiceImpl.class);
    private final MultiLayerFlowService<UpdateFlowOutput> flowUpdate;
    private final MultiLayerFlowService<AddFlowOutput> flowAdd;
    private final MultiLayerFlowService<RemoveFlowOutput> flowRemove;
    private final SingleLayerFlowService<AddFlowOutput> flowAddMessage;
    private final SingleLayerFlowService<UpdateFlowOutput> flowUpdateMessage;
    private final SingleLayerFlowService<RemoveFlowOutput> flowRemoveMessage;
    private final DeviceContext deviceContext;

    public SalFlowServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor) {
        this.deviceContext = deviceContext;
        flowRemove = new MultiLayerFlowService<>(requestContextStack, deviceContext, RemoveFlowOutput.class, convertorExecutor);
        flowAdd = new MultiLayerFlowService<>(requestContextStack, deviceContext, AddFlowOutput.class, convertorExecutor);
        flowUpdate = new MultiLayerFlowService<>(requestContextStack, deviceContext, UpdateFlowOutput.class, convertorExecutor);
        flowAddMessage = new SingleLayerFlowService<>(requestContextStack, deviceContext, AddFlowOutput.class);
        flowUpdateMessage = new SingleLayerFlowService<>(requestContextStack, deviceContext, UpdateFlowOutput.class);
        flowRemoveMessage= new SingleLayerFlowService<>(requestContextStack, deviceContext, RemoveFlowOutput.class);
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(deviceContext.getDeviceInfo().getVersion(), input);
        final ListenableFuture<RpcResult<AddFlowOutput>> future;

        if (flowAddMessage.canUseSingleLayerSerialization()) {
            future = flowAddMessage.handleServiceCall(input);
            Futures.addCallback(future, new AddFlowCallback(input, flowRegistryKey));
        } else {
            future = flowAdd.processFlowModInputBuilders(flowAdd.toFlowModInputs(input));
            Futures.addCallback(future, new AddFlowCallback(input, flowRegistryKey));

        }
        return future;
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        final ListenableFuture<RpcResult<RemoveFlowOutput>> future;

        if (flowRemoveMessage.canUseSingleLayerSerialization()) {
            future = flowRemoveMessage.handleServiceCall(input);
            Futures.addCallback(future, new RemoveFlowCallback(input));

        } else {
            future = flowRemove.processFlowModInputBuilders(flowRemove.toFlowModInputs(input));
            Futures.addCallback(future, new RemoveFlowCallback(input));
        }

        return future;
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
        final UpdatedFlow updated = input.getUpdatedFlow();
        final OriginalFlow original = input.getOriginalFlow();

        final List<FlowModInputBuilder> allFlowMods = new ArrayList<>();
        final List<FlowModInputBuilder> ofFlowModInputs;

        ListenableFuture<RpcResult<UpdateFlowOutput>> future;
        if (flowUpdateMessage.canUseSingleLayerSerialization()) {

            if (!FlowCreatorUtil.canModifyFlow(original, updated, flowUpdateMessage.getVersion())) {
                final SettableFuture<RpcResult<UpdateFlowOutput>> objectSettableFuture = SettableFuture.create();

                final ListenableFuture<List<RpcResult<UpdateFlowOutput>>> listListenableFuture = Futures.successfulAsList(
                        flowUpdateMessage.handleServiceCall(input.getOriginalFlow()),
                        flowUpdateMessage.handleServiceCall(input.getUpdatedFlow()));

                Futures.addCallback(listListenableFuture, new FutureCallback<List<RpcResult<UpdateFlowOutput>>>() {
                    @Override
                    public void onSuccess(final List<RpcResult<UpdateFlowOutput>> results) {
                        final ArrayList<RpcError> errors = new ArrayList();
                        for (RpcResult<UpdateFlowOutput> flowModResult : results) {
                            if (flowModResult == null) {
                                errors.add(RpcResultBuilder.newError(
                                        RpcError.ErrorType.PROTOCOL, OFConstants.APPLICATION_TAG,
                                        "unexpected flowMod result (null) occurred"));
                            } else if (!flowModResult.isSuccessful()) {
                                errors.addAll(flowModResult.getErrors());
                            }
                        }

                        final RpcResultBuilder<UpdateFlowOutput> rpcResultBuilder;
                        if (errors.isEmpty()) {
                            rpcResultBuilder = RpcResultBuilder.success();
                        } else {
                            rpcResultBuilder = RpcResultBuilder.<UpdateFlowOutput>failed().withRpcErrors(errors);
                        }

                        objectSettableFuture.set(rpcResultBuilder.build());
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        RpcResultBuilder<UpdateFlowOutput> rpcResultBuilder = RpcResultBuilder.failed();
                        objectSettableFuture.set(rpcResultBuilder.build());
                    }
                });

                future = objectSettableFuture;
            } else {
                future = flowUpdateMessage.handleServiceCall(input.getUpdatedFlow());
            }
        } else {
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

            future = flowUpdate.processFlowModInputBuilders(allFlowMods);
        }

        Futures.addCallback(future, new UpdateFlowCallback(input));
        return future;
    }

    @VisibleForTesting
    private static KeyedInstanceIdentifier<Flow, FlowKey> createFlowPath(FlowDescriptor flowDescriptor,
                                                                         KeyedInstanceIdentifier<Node, NodeKey> nodePath) {
        return nodePath.augmentation(FlowCapableNode.class)
                .child(Table.class, flowDescriptor.getTableKey())
                .child(Flow.class, new FlowKey(flowDescriptor.getFlowId()));
    }

    private class AddFlowCallback implements FutureCallback<RpcResult<AddFlowOutput>> {
        private final AddFlowInput input;
        private final FlowRegistryKey flowRegistryKey;

        private AddFlowCallback(final AddFlowInput input,
                                final FlowRegistryKey flowRegistryKey) {
            this.input = input;
            this.flowRegistryKey = flowRegistryKey;
        }

        @Override
        public void onSuccess(final RpcResult<AddFlowOutput> rpcResult) {
            if (rpcResult.isSuccessful()) {
                final FlowDescriptor flowDescriptor;

                if (Objects.nonNull(input.getFlowRef())) {
                    final FlowId flowId = input.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId();
                    flowDescriptor = FlowDescriptorFactory.create(input.getTableId(), flowId);
                    deviceContext.getDeviceFlowRegistry().storeDescriptor(flowRegistryKey, flowDescriptor);
                } else {
                    deviceContext.getDeviceFlowRegistry().store(flowRegistryKey);
                    flowDescriptor = deviceContext.getDeviceFlowRegistry().retrieveDescriptor(flowRegistryKey);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow add with id={} finished without error", flowDescriptor.getFlowId().getValue());
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow add failed for flow={}, errors={}", input,
                            ErrorUtil.errorsToString(rpcResult.getErrors()));
                }
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.warn("Service call for adding flow={} failed, reason: {}", input, throwable);
        }
    }




    private class RemoveFlowCallback implements FutureCallback<RpcResult<RemoveFlowOutput>> {
        private final RemoveFlowInput input;

        private RemoveFlowCallback(final RemoveFlowInput input) {
            this.input = input;
        }

        @Override
        public void onSuccess(final RpcResult<RemoveFlowOutput> result) {
            if (result.isSuccessful()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow remove finished without error for flow={}", input);
                }
                FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(deviceContext.getDeviceInfo().getVersion(), input);
                deviceContext.getDeviceFlowRegistry().addMark(flowRegistryKey);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow remove failed for flow={}, errors={}", input,
                            ErrorUtil.errorsToString(result.getErrors()));
                }
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.warn("Service call for removing flow={} failed, reason: {}", input, throwable);
        }
    }

    private class UpdateFlowCallback implements FutureCallback<RpcResult<UpdateFlowOutput>> {
        private final UpdateFlowInput input;

        private UpdateFlowCallback(UpdateFlowInput input) {
            this.input = input;
        }

        @Override
        public void onSuccess(final RpcResult<UpdateFlowOutput> result) {
            final DeviceFlowRegistry deviceFlowRegistry = deviceContext.getDeviceFlowRegistry();

            final UpdatedFlow updated = input.getUpdatedFlow();
            final OriginalFlow original = input.getOriginalFlow();
            final FlowRegistryKey origFlowRegistryKey = FlowRegistryKeyFactory.create(deviceContext.getDeviceInfo().getVersion(), original);
            final FlowRegistryKey updatedFlowRegistryKey = FlowRegistryKeyFactory.create(deviceContext.getDeviceInfo().getVersion(), updated);
            final FlowDescriptor origFlowDescriptor = deviceFlowRegistry.retrieveDescriptor(origFlowRegistryKey);

            final boolean isUpdate = Objects.nonNull(origFlowDescriptor);
            final FlowDescriptor updatedFlowDescriptor;

            if (Objects.nonNull(input.getFlowRef())) {
                updatedFlowDescriptor = FlowDescriptorFactory.create(updated.getTableId(), input.getFlowRef().getValue().firstKeyOf(Flow.class).getId());
            } else {
                if (isUpdate) {
                    updatedFlowDescriptor = origFlowDescriptor;
                } else {
                    deviceFlowRegistry.store(updatedFlowRegistryKey);
                    updatedFlowDescriptor = deviceFlowRegistry.retrieveDescriptor(updatedFlowRegistryKey);
                }
            }

            if (isUpdate) {
                deviceFlowRegistry.addMark(origFlowRegistryKey);
                deviceFlowRegistry.storeDescriptor(updatedFlowRegistryKey, updatedFlowDescriptor);
            }
            if (result.isSuccessful()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow update finished without error for flow={}", input);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow update failed for flow={}, errors={}", input,
                            ErrorUtil.errorsToString(result.getErrors()));
                }
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.warn("Service call for updating flow={} failed, reason: {}", input, throwable);
        }
    }
}
