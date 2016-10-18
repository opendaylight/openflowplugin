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
import java.util.List;
import java.util.Objects;
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
import org.opendaylight.openflowplugin.impl.util.ErrorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
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

    public SalFlowServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor) {
        this.deviceContext = deviceContext;
        flowRemove = new FlowService<>(requestContextStack, deviceContext, RemoveFlowOutput.class, convertorExecutor);
        flowAdd = new FlowService<>(requestContextStack, deviceContext, AddFlowOutput.class, convertorExecutor);
        flowUpdate = new FlowService<>(requestContextStack, deviceContext, UpdateFlowOutput.class, convertorExecutor);
    }

    @Override
    public void setItemLifecycleListener(@Nullable ItemLifecycleListener itemLifecycleListener) {
        this.itemLifecycleListener = itemLifecycleListener;
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(input);
        final ListenableFuture<RpcResult<AddFlowOutput>> future =
                flowAdd.processFlowModInputBuilders(flowAdd.toFlowModInputs(input));
        Futures.addCallback(future, new AddFlowCallback(input, flowRegistryKey));
        return future;
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        final ListenableFuture<RpcResult<RemoveFlowOutput>> future =
                flowRemove.processFlowModInputBuilders(flowRemove.toFlowModInputs(input));
        Futures.addCallback(future, new RemoveFlowCallback(input));
        return future;
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
        final UpdatedFlow updated = input.getUpdatedFlow();
        final OriginalFlow original = input.getOriginalFlow();

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
                    deviceContext.getDeviceFlowRegistry().store(flowRegistryKey, flowDescriptor);
                } else {
                    final FlowId flowId = deviceContext.getDeviceFlowRegistry().storeIfNecessary(flowRegistryKey);
                    flowDescriptor = FlowDescriptorFactory.create(input.getTableId(), flowId);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow add with id={} finished without error", flowDescriptor.getFlowId().getValue());
                }

                if (itemLifecycleListener != null) {
                    KeyedInstanceIdentifier<Flow, FlowKey> flowPath = createFlowPath(flowDescriptor,
                            deviceContext.getDeviceInfo().getNodeInstanceIdentifier());
                    final FlowBuilder flowBuilder = new FlowBuilder(input).setId(flowDescriptor.getFlowId());
                    itemLifecycleListener.onAdded(flowPath, flowBuilder.build());
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
                FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(input);
                deviceContext.getDeviceFlowRegistry().removeDescriptor(flowRegistryKey);

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
        public void onSuccess(final RpcResult<UpdateFlowOutput> o) {
            final DeviceFlowRegistry deviceFlowRegistry = deviceContext.getDeviceFlowRegistry();

            final UpdatedFlow updated = input.getUpdatedFlow();
            final OriginalFlow original = input.getOriginalFlow();
            final FlowRegistryKey origFlowRegistryKey = FlowRegistryKeyFactory.create(original);
            final FlowRegistryKey updatedFlowRegistryKey = FlowRegistryKeyFactory.create(updated);
            final FlowDescriptor origFlowDescriptor = deviceFlowRegistry.retrieveIdForFlow(origFlowRegistryKey);

            final boolean isUpdate = Objects.nonNull(origFlowDescriptor);
            final FlowId fLowId = Objects.nonNull(input.getFlowRef())
                    ? input.getFlowRef().getValue().firstKeyOf(Flow.class).getId()
                    : isUpdate ? origFlowDescriptor.getFlowId() : deviceFlowRegistry.storeIfNecessary(updatedFlowRegistryKey);
            final FlowDescriptor updatedFlowDescriptor = FlowDescriptorFactory.create(updated.getTableId(), fLowId);
            if (isUpdate) {
                deviceFlowRegistry.removeDescriptor(origFlowRegistryKey);
                deviceFlowRegistry.store(updatedFlowRegistryKey, updatedFlowDescriptor);
            }

            if (itemLifecycleListener != null) {
                final KeyedInstanceIdentifier<Flow, FlowKey> flowPath =
                        createFlowPath(
                                updatedFlowDescriptor,
                                deviceContext.getDeviceInfo().getNodeInstanceIdentifier());

                final Flow flow = new FlowBuilder(updated)
                        .setId(updatedFlowDescriptor.getFlowId())
                        .build();

                if (Objects.nonNull(origFlowDescriptor)) {
                    itemLifecycleListener.onUpdated(flowPath, flow);
                } else {
                    itemLifecycleListener.onAdded(flowPath, flow);
                }
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.warn("Service call for updating flow={} failed, reason: {}", input, throwable);
        }
    }
}