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
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowHash;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
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

    <T, F> ListenableFuture<RpcResult<T>> handleServiceCall(final BigInteger connectionID,
                                                            final FlowModInputBuilder flowModInputBuilder, final Function<DataCrate<T>, ListenableFuture<RpcResult<F>>> function) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");

        final RequestContext<T> requestContext = requestContextStack.createRequestContext();
        final SettableFuture<RpcResult<T>> result = requestContextStack.storeOrFail(requestContext);
        final DataCrate<T> dataCrate = DataCrateBuilder.<T>builder().setiDConnection(connectionID)
                .setRequestContext(requestContext).setFlowModInputBuilder(flowModInputBuilder).build();

        if (!result.isDone()) {
            final ListenableFuture<RpcResult<F>> resultFromOFLib = function.apply(dataCrate);

            final OFJResult2RequestCtxFuture<T> OFJResult2RequestCtxFuture = new OFJResult2RequestCtxFuture<>(requestContext, deviceContext);
            OFJResult2RequestCtxFuture.processResultFromOfJava(resultFromOFLib);

        } else {
            RequestContextUtil.closeRequstContext(requestContext);
        }
        return result;
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {

        final FlowId flowId = input.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId();

        final FlowHash flowHash = FlowHashFactory.create(input);
        final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(input.getTableId(), flowId);
        deviceContext.getDeviceFlowRegistry().store(flowHash, flowDescriptor);

        final List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(input, version, datapathId);
        final ListenableFuture future = processFlowModInputBuilders(ofFlowModInputs);

        Futures.addCallback(future, new FutureCallback() {
            @Override
            public void onSuccess(final Object o) {
                LOG.debug("flow add finished without error, id={}", flowId.getValue());
            }

            @Override
            public void onFailure(final Throwable throwable) {
                deviceContext.getDeviceFlowRegistry().markToBeremoved(flowHash);
                LOG.trace("Service call for adding flows failed, id={}.", flowId.getValue(), throwable);
            }
        });

        return future;
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        LOG.trace("Calling remove flow for flow with ID ={}.", input.getFlowRef());
        return this.<RemoveFlowOutput, Void>handleServiceCall(PRIMARY_CONNECTION,
                new Function<DataCrate<RemoveFlowOutput>, ListenableFuture<RpcResult<Void>>>() {
                    @Override
                    public ListenableFuture<RpcResult<Void>> apply(final DataCrate<RemoveFlowOutput> data) {
                        final FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(input, version,
                                datapathId);
                        final ListenableFuture<RpcResult<Void>> future = createResultForFlowMod(data, ofFlowModInput);
                        Futures.addCallback(future, new FutureCallback() {
                            @Override
                            public void onSuccess(final Object o) {
                                FlowHash flowHash = FlowHashFactory.create(input);
                                deviceContext.getDeviceFlowRegistry().markToBeremoved(flowHash);
                            }

                            @Override
                            public void onFailure(final Throwable throwable) {
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
                                }
                                LOG.trace("Flow modification failed. Errors : {}", errors.toString());
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

        if (!FlowCreatorUtil.canModifyFlow(original, updated, version)) {
            // We would need to remove original and add updated.

            // remove flow
            final RemoveFlowInputBuilder removeflow = new RemoveFlowInputBuilder(original);
            final List<FlowModInputBuilder> ofFlowRemoveInput = FlowConvertor.toFlowModInputs(removeflow.build(),
                    version, datapathId);
            // remove flow should be the first
            allFlowMods.addAll(ofFlowRemoveInput);
            final AddFlowInputBuilder addFlowInputBuilder = new AddFlowInputBuilder(updated);
            ofFlowModInputs = FlowConvertor.toFlowModInputs(addFlowInputBuilder.build(), version, datapathId);
        } else {
            ofFlowModInputs = FlowConvertor.toFlowModInputs(updated, version, datapathId);
        }

        allFlowMods.addAll(ofFlowModInputs);
        ListenableFuture future = processFlowModInputBuilders(allFlowMods);
        Futures.addCallback(future, new FutureCallback() {
            @Override
            public void onSuccess(final Object o) {
                FlowHash flowHash = FlowHashFactory.create(original);
                deviceContext.getDeviceFlowRegistry().markToBeremoved(flowHash);

                flowHash = FlowHashFactory.create(updated);
                FlowId flowId = input.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId();
                FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(updated.getTableId(), flowId);
                deviceContext.getDeviceFlowRegistry().store(flowHash, flowDescriptor);

            }

            @Override
            public void onFailure(final Throwable throwable) {

            }
        });
        return future;
    }

    private <T> ListenableFuture<RpcResult<T>> processFlowModInputBuilders(
            final List<FlowModInputBuilder> ofFlowModInputs) {
        final List<ListenableFuture<RpcResult<T>>> partialFutures = new ArrayList<>();
        for (FlowModInputBuilder flowModInputBuilder : ofFlowModInputs) {
            ListenableFuture<RpcResult<T>> partialFuture = handleServiceCall(PRIMARY_CONNECTION, flowModInputBuilder,
                    new Function<DataCrate<T>, ListenableFuture<RpcResult<Void>>>() {
                        @Override
                        public ListenableFuture<RpcResult<Void>> apply(final DataCrate<T> data) {
                            return createResultForFlowMod(data);
                        }
                    });
            partialFutures.add(partialFuture);
        }

        final ListenableFuture<List<RpcResult<T>>> allFutures = Futures.allAsList(partialFutures);
        final SettableFuture<RpcResult<T>> finalFuture = SettableFuture.create();
        Futures.addCallback(allFutures, new FutureCallback<List<RpcResult<T>>>() {
            @Override
            public void onSuccess(List<RpcResult<T>> results) {
                Iterator<FlowModInputBuilder> flowModInputBldIterator = ofFlowModInputs.iterator();
                List<RpcError> rpcErrorLot = new ArrayList<>();
                for (RpcResult<T> rpcResult : results) {
                    FlowModInputBuilder flowModInputBld = flowModInputBldIterator.next();
                    if (rpcResult.isSuccessful()) {
                        Long xid = flowModInputBld.getXid();
                        LOG.warn("Positive confirmation of flow push is not supported by OF-spec");
                        LOG.warn("flow future result was successful [{}] = this should have never happen",
                                xid);
                        rpcErrorLot.add(RpcResultBuilder.newError(ErrorType.APPLICATION, "",
                                "flow future result was successful ["+xid+"] = this should have never happen"));
                    } else {
                        rpcErrorLot.addAll(rpcResult.getErrors());
                    }
                }
                finalFuture.set(RpcResultBuilder.<T>failed().withRpcErrors(rpcErrorLot).build());
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.trace("Flow mods chained future failed.");
                RpcResultBuilder<T> resultBuilder;
                if (allFutures.isCancelled()) {
                    if (LOG.isTraceEnabled()) {
                        for (FlowModInputBuilder ofFlowModInput : ofFlowModInputs) {
                            LOG.trace("flow future result was cancelled [{}] = barrier passed it without error",
                                    ofFlowModInput.getXid());
                        }
                    }
                    resultBuilder = RpcResultBuilder.<T>success();
                } else {
                    resultBuilder = RpcResultBuilder.<T>failed().withError(ErrorType.APPLICATION, "", t.getMessage());
                }
                finalFuture.set(resultBuilder.build());
            }
        });

        return finalFuture;
    }

    protected <T> ListenableFuture<RpcResult<Void>> createResultForFlowMod(final DataCrate<T> data) {
        return createResultForFlowMod(data, data.getFlowModInputBuilder());
    }

    protected <T> ListenableFuture<RpcResult<Void>> createResultForFlowMod(final DataCrate<T> data, final FlowModInputBuilder flowModInput) {
        final Xid xId = deviceContext.getNextXid();
        flowModInput.setXid(xId.getValue());
        data.getRequestContext().setXid(xId);
        Future<RpcResult<Void>> flowModResult = provideConnectionAdapter(data.getiDConnection()).flowMod(
                flowModInput.build());
        return JdkFutureAdapters.listenInPoolThread(flowModResult);
    }

}
