/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * <p/>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowHash;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowHashFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class SalFlowServiceImpl extends CommonService implements SalFlowService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalFlowServiceImpl.class);

    public SalFlowServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        getMessageSpy().spyMessage(input.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_ENTERED);

        final List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(input, getVersion(), getDatapathId());
        final ListenableFuture<RpcResult<AddFlowOutput>> future = processFlowModInputBuilders(ofFlowModInputs);

        Futures.addCallback(future, new FutureCallback<RpcResult<AddFlowOutput>>() {

            final DeviceContext deviceContext = getDeviceContext();
            final FlowHash flowHash = FlowHashFactory.create(input, deviceContext.getPrimaryConnectionContext().getFeatures().getVersion());
            FlowId flowId = null;
            @Override
            public void onSuccess(final RpcResult<AddFlowOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    getMessageSpy().spyMessage(FlowModInput.class, MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);
                    LOG.debug("flow add finished without error, id={}", flowId.getValue());
                } else {
                    getMessageSpy().spyMessage(FlowModInput.class, MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
                    LOG.debug("flow add failed with error, id={}", flowId.getValue());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                deviceContext.getDeviceFlowRegistry().markToBeremoved(flowHash);
                getMessageSpy().spyMessage(FlowModInput.class, MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
                LOG.trace("Service call for adding flows failed, id={}.", flowId.getValue(), throwable);
            }
        });

        return future;
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        LOG.trace("Calling remove flow for flow with ID ={}.", input.getFlowRef());
        return this.<RemoveFlowOutput, Void>handleServiceCall(new Function<DataCrate<RemoveFlowOutput>, ListenableFuture<RpcResult<Void>>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final DataCrate<RemoveFlowOutput> data) {
                final FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(input, getVersion(),
                        getDatapathId());
                final ListenableFuture<RpcResult<Void>> future = createResultForFlowMod(data, ofFlowModInput);
                Futures.addCallback(future, new FutureCallback() {
                    @Override
                    public void onSuccess(final Object o) {
                        final DeviceContext deviceContext = getDeviceContext();
                        getMessageSpy().spyMessage(input.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);
                        FlowHash flowHash = FlowHashFactory.create(input, deviceContext.getPrimaryConnectionContext().getFeatures().getVersion());
                        deviceContext.getDeviceFlowRegistry().markToBeremoved(flowHash);
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        getMessageSpy().spyMessage(input.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
                        LOG.trace("Flow modification failed..", throwable);
                        StringBuffer errors = new StringBuffer();
                        try {
                            RpcResult<Void> result = future.get();
                            Collection<RpcError> rpcErrors = result.getErrors();
                            if (null != rpcErrors && rpcErrors.size() > 0) {
                                for (RpcError rpcError : rpcErrors) {
                                    errors.append(rpcError.getMessage());
                                }
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            LOG.trace("Flow modification failed. Can't read errors from RpcResult.");
                        } finally {
                            LOG.trace("Flow modification failed. Errors : {}", errors.toString());
                        }
                    }
                });
                return future;
            }
        });
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
        final UpdateFlowInput in = input;
        final UpdatedFlow updated = in.getUpdatedFlow();
        final OriginalFlow original = in.getOriginalFlow();

        final List<FlowModInputBuilder> allFlowMods = new ArrayList<>();
        List<FlowModInputBuilder> ofFlowModInputs;

        if (!FlowCreatorUtil.canModifyFlow(original, updated, getVersion())) {
            // We would need to remove original and add updated.

            // remove flow
            final RemoveFlowInputBuilder removeflow = new RemoveFlowInputBuilder(original);
            final List<FlowModInputBuilder> ofFlowRemoveInput = FlowConvertor.toFlowModInputs(removeflow.build(),
                    getVersion(), getDatapathId());
            // remove flow should be the first
            allFlowMods.addAll(ofFlowRemoveInput);
            final AddFlowInputBuilder addFlowInputBuilder = new AddFlowInputBuilder(updated);
            ofFlowModInputs = FlowConvertor.toFlowModInputs(addFlowInputBuilder.build(), getVersion(), getDatapathId());
        } else {
            ofFlowModInputs = FlowConvertor.toFlowModInputs(updated, getVersion(), getDatapathId());
        }

        allFlowMods.addAll(ofFlowModInputs);
        ListenableFuture future = processFlowModInputBuilders(allFlowMods);
        Futures.addCallback(future, new FutureCallback() {
            @Override
            public void onSuccess(final Object o) {
                final DeviceContext deviceContext = getDeviceContext();
                getMessageSpy().spyMessage(input.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);
                final short version = deviceContext.getPrimaryConnectionContext().getFeatures().getVersion();
                FlowHash flowHash = FlowHashFactory.create(original, version);

                FlowHash updatedflowHash = FlowHashFactory.create(updated, version);
                FlowId flowId = input.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId();
                FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(updated.getTableId(), flowId);
                final DeviceFlowRegistry deviceFlowRegistry = deviceContext.getDeviceFlowRegistry();
                deviceFlowRegistry.markToBeremoved(flowHash);
                deviceFlowRegistry.store(updatedflowHash, flowDescriptor);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                getMessageSpy().spyMessage(input.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
            }
        });
        return future;
    }

    private <T> ListenableFuture<RpcResult<T>> processFlowModInputBuilders(final List<FlowModInputBuilder> ofFlowModInputs) {

        final List<ListenableFuture<RpcResult<T>>> partialFutures = new ArrayList<>();

        for (FlowModInputBuilder flowModInputBuilder : ofFlowModInputs) {
            DataCrateBuilder<T> dataCrateBuilder = DataCrateBuilder.<T>builder().setFlowModInputBuilder(flowModInputBuilder);
            ListenableFuture<RpcResult<T>> partialFuture = handleServiceCall(
                    new Function<DataCrate<T>, ListenableFuture<RpcResult<Void>>>() {
                        @Override
                        public ListenableFuture<RpcResult<Void>> apply(final DataCrate<T> data) {
                            return createResultForFlowMod(data);
                        }
                    },
                    dataCrateBuilder
            );
            partialFutures.add(partialFuture);
        }

        final ListenableFuture<List<RpcResult<T>>> allFutures = Futures.successfulAsList(partialFutures);
        final SettableFuture<RpcResult<T>> finalFuture = SettableFuture.create();
        Futures.addCallback(allFutures, new FutureCallback<List<RpcResult<T>>>() {
            @Override
            public void onSuccess(List<RpcResult<T>> results) {
                RpcResultBuilder rpcResultBuilder = RpcResultBuilder.success();
                finalFuture.set(rpcResultBuilder.build());
            }

            @Override
            public void onFailure(Throwable t) {
                RpcResultBuilder rpcResultBuilder = RpcResultBuilder.failed();
                finalFuture.set(rpcResultBuilder.build());
            }
        });

        return finalFuture;
    }

    protected <T> ListenableFuture<RpcResult<Void>> createResultForFlowMod(final DataCrate<T> data) {
        return createResultForFlowMod(data, data.getFlowModInputBuilder());
    }

    protected <T> ListenableFuture<RpcResult<Void>> createResultForFlowMod(final DataCrate<T> data, final FlowModInputBuilder flowModInputBuilder) {
        final OutboundQueue outboundQueue = getDeviceContext().getPrimaryConnectionContext().getOutboundQueueProvider().getOutboundQueue();
        final long xid = data.getRequestContext().getXid().getValue();
        flowModInputBuilder.setXid(xid);
        final FlowModInput flowModInput = flowModInputBuilder.build();

        final SettableFuture<RpcResult<Void>> settableFuture = SettableFuture.create();
        outboundQueue.commitEntry(xid, flowModInput, new FutureCallback<OfHeader>() {
            @Override
            public void onSuccess(final OfHeader ofHeader) {
                RequestContextUtil.closeRequstContext(data.getRequestContext());
                getDeviceContext().unhookRequestCtx(data.getRequestContext().getXid());
                getMessageSpy().spyMessage(FlowModInput.class, MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);

                settableFuture.set(RpcResultBuilder.<Void>success().build());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                RpcResultBuilder rpcResultBuilder = RpcResultBuilder.<Void>failed().withError(ErrorType.APPLICATION, throwable.getMessage(), throwable);
                RequestContextUtil.closeRequstContext(data.getRequestContext());
                getDeviceContext().unhookRequestCtx(data.getRequestContext().getXid());
                settableFuture.set(rpcResultBuilder.build());
            }
        });
        return settableFuture;
    }

}
