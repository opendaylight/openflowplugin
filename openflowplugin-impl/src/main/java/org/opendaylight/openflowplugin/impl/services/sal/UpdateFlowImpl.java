/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupStatus;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.util.FlowCreatorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UpdateFlowImpl extends AbstractFlowRpc<UpdateFlowOutput> implements UpdateFlow {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateFlowImpl.class);

    public UpdateFlowImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor, UpdateFlowOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<UpdateFlowOutput>> invoke(final UpdateFlowInput input) {
        final var updated = input.getUpdatedFlow();
        final var original = input.getOriginalFlow();
        final var allFlowMods = new ArrayList<FlowModInputBuilder>();
        final List<FlowModInputBuilder> ofFlowModInputs;

        ListenableFuture<RpcResult<UpdateFlowOutput>> future;
        if (single.canUseSingleLayerSerialization()) {

            if (!FlowCreatorUtil.canModifyFlow(original, updated, single.getVersion())) {
                final var objectSettableFuture = SettableFuture.<RpcResult<UpdateFlowOutput>>create();
                final var listListenableFuture = Futures.successfulAsList(
                    single.handleServiceCall(input.getOriginalFlow()),
                    single.handleServiceCall(input.getUpdatedFlow()));

                Futures.addCallback(listListenableFuture, new FutureCallback<>() {
                    @Override
                    public void onSuccess(final List<RpcResult<UpdateFlowOutput>> results) {
                        final var errors = new ArrayList<RpcError>();
                        for (var flowModResult : results) {
                            if (flowModResult == null) {
                                errors.add(RpcResultBuilder.newError(
                                        ErrorType.PROTOCOL, OFConstants.APPLICATION_TAG,
                                        "unexpected flowMod result (null) occurred"));
                            } else if (!flowModResult.isSuccessful()) {
                                errors.addAll(flowModResult.getErrors());
                            }
                        }

                        final var rpcResultBuilder = errors.isEmpty() ? RpcResultBuilder.<UpdateFlowOutput>success()
                            : RpcResultBuilder.<UpdateFlowOutput>failed().withRpcErrors(errors);

                        objectSettableFuture.set(rpcResultBuilder.build());
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        objectSettableFuture.set(RpcResultBuilder.<UpdateFlowOutput>failed().build());
                    }
                }, MoreExecutors.directExecutor());

                future = objectSettableFuture;
            } else {
                future = single.handleServiceCall(input.getUpdatedFlow());
            }
        } else {
            if (!FlowCreatorUtil.canModifyFlow(original, updated, multi.getVersion())) {
                // We would need to remove original and add updated.

                // remove flow
                final RemoveFlowInputBuilder removeflow = new RemoveFlowInputBuilder(original);
                final List<FlowModInputBuilder> ofFlowRemoveInput = multi.toFlowModInputs(removeflow.build());
                // remove flow should be the first
                allFlowMods.addAll(ofFlowRemoveInput);
                final AddFlowInputBuilder addFlowInputBuilder = new AddFlowInputBuilder(updated);
                ofFlowModInputs = multi.toFlowModInputs(addFlowInputBuilder.build());
            } else {
                ofFlowModInputs = multi.toFlowModInputs(updated);
            }

            allFlowMods.addAll(ofFlowModInputs);

            future = multi.processFlowModInputBuilders(allFlowMods);
        }

        Futures.addCallback(future, new UpdateFlowCallback(input), MoreExecutors.directExecutor());
        return future;
    }


    private final class UpdateFlowCallback implements FutureCallback<RpcResult<UpdateFlowOutput>> {
        private final UpdateFlowInput input;

        private UpdateFlowCallback(final UpdateFlowInput input) {
            this.input = input;
        }

        @Override
        public void onSuccess(final RpcResult<UpdateFlowOutput> updateFlowOutputRpcResult) {
            final DeviceFlowRegistry deviceFlowRegistry = deviceContext.getDeviceFlowRegistry();
            final UpdatedFlow updated = input.getUpdatedFlow();
            final OriginalFlow original = input.getOriginalFlow();
            final FlowRegistryKey origFlowRegistryKey =
                    FlowRegistryKeyFactory.create(deviceContext.getDeviceInfo().getVersion(), original);
            final FlowRegistryKey updatedFlowRegistryKey =
                    FlowRegistryKeyFactory.create(deviceContext.getDeviceInfo().getVersion(), updated);
            final FlowDescriptor origFlowDescriptor = deviceFlowRegistry.retrieveDescriptor(origFlowRegistryKey);

            final boolean isUpdate = origFlowDescriptor != null;
            final FlowDescriptor updatedFlowDescriptor;
            final FlowRef flowRef = input.getFlowRef();
            if (flowRef != null) {
                final Uint8 tableId = updated.getTableId();
                final FlowId flowId = flowRef.getValue().firstKeyOf(Flow.class).getId();
                // FIXME: this does not look right, we probably want better integration
                deviceFlowRegistry.appendHistoryFlow(flowId, tableId, FlowGroupStatus.MODIFIED);

                updatedFlowDescriptor = FlowDescriptorFactory.create(tableId, flowId);
            } else if (isUpdate) {
                updatedFlowDescriptor = origFlowDescriptor;
            } else {
                deviceFlowRegistry.store(updatedFlowRegistryKey);
                updatedFlowDescriptor = deviceFlowRegistry.retrieveDescriptor(updatedFlowRegistryKey);
            }

            if (isUpdate) {
                deviceFlowRegistry.addMark(origFlowRegistryKey);
                deviceFlowRegistry.storeDescriptor(updatedFlowRegistryKey, updatedFlowDescriptor);
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.warn("Service call for updating flow={} failed", input, throwable);
        }
    }
}
