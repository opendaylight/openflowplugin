/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

public class SalFlowServiceImpl extends CommonService implements SalFlowService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalFlowServiceImpl.class);

    public SalFlowServiceImpl(final RpcContext rpcContext) {
        super(rpcContext);
    }

    <T extends DataObject, F> ListenableFuture<RpcResult<T>> handleServiceCall(final BigInteger connectionID,
            final FlowModInputBuilder flowModInputBuilder, final Function<DataCrate<T>, Future<RpcResult<F>>> function) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");

        final RequestContext<T> requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<T>> result = rpcContext.storeOrFail(requestContext);
        final DataCrate<T> dataCrate = DataCrateBuilder.<T> builder().setiDConnection(connectionID)
                .setRequestContext(requestContext).setFlowModInputBuilder(flowModInputBuilder).build();

        if (!result.isDone()) {
            final Future<RpcResult<F>> resultFromOFLib = function.apply(dataCrate);

            final RpcResultConvertor<T> rpcResultConvertor = new RpcResultConvertor<>(requestContext, deviceContext);
            rpcResultConvertor.processResultFromOfJava(resultFromOFLib);

        } else {
            RequestContextUtil.closeRequstContext(requestContext);
        }
        return result;
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        final List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(input, version, datapathId);
        return processFlowModInputBuilders(ofFlowModInputs);
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {

        return this.<RemoveFlowOutput, Void> handleServiceCall(PRIMARY_CONNECTION,
                new Function<DataCrate<RemoveFlowOutput>, Future<RpcResult<Void>>>() {
                    @Override
                    public Future<RpcResult<Void>> apply(final DataCrate<RemoveFlowOutput> data) {
                        final FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(input, version,
                                datapathId);
                        return createResultForFlowMod(data, ofFlowModInput);
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
        return processFlowModInputBuilders(allFlowMods);
    }

    private <T extends DataObject> Future<RpcResult<T>> processFlowModInputBuilders(
            final List<FlowModInputBuilder> ofFlowModInputs) {
        final List<ListenableFuture<RpcResult<T>>> partialFutures = new ArrayList<>();
        for (FlowModInputBuilder flowModInputBuilder : ofFlowModInputs) {
            ListenableFuture<RpcResult<T>> partialFuture = handleServiceCall(PRIMARY_CONNECTION, flowModInputBuilder,
                    new Function<DataCrate<T>, Future<RpcResult<Void>>>() {
                        @Override
                        public ListenableFuture<RpcResult<Void>> apply(final DataCrate<T> data) {
                            return createResultForFlowMod(data);
                        }
                    });
            partialFutures.add(partialFuture);
        }

        ListenableFuture<List<RpcResult<T>>> allFutures = Futures.allAsList(partialFutures);
        final SettableFuture<RpcResult<T>> finalFuture = SettableFuture.create();
        Futures.addCallback(allFutures, new FutureCallback<List<RpcResult<T>>>() {
            @Override
            public void onSuccess(List<RpcResult<T>> result) {
                for (RpcResult<T> rpcResult : result) {
                    if (rpcResult.isSuccessful()) {
                        // TODO: AddFlowOutput has getTransactionId() - shouldn't it have some value?
                        finalFuture.set(RpcResultBuilder.<T> success().build());
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                finalFuture.set(RpcResultBuilder.<T> failed().withError(ErrorType.APPLICATION, "", t.getMessage())
                        .build());
            }
        });

        return finalFuture;
    }

    protected <T extends DataObject> ListenableFuture<RpcResult<Void>> createResultForFlowMod(final DataCrate<T> data) {
        return createResultForFlowMod(data, data.getFlowModInputBuilder()) ;
    }

    protected <T extends DataObject> ListenableFuture<RpcResult<Void>> createResultForFlowMod(final DataCrate<T> data, final FlowModInputBuilder flowModInput) {
        final Xid xId = deviceContext.getNextXid();
        flowModInput.setXid(xId.getValue());
        data.getRequestContext().setXid(xId);
        Future<RpcResult<Void>> flowModResult = provideConnectionAdapter(data.getiDConnection()).flowMod(
                flowModInput.build());
        return JdkFutureAdapters.listenInPoolThread(flowModResult);
    }

}
