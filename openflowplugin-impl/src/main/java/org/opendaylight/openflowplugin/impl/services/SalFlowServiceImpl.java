/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class SalFlowServiceImpl extends CommonService implements SalFlowService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalFlowServiceImpl.class);

    private interface Function {
        Future<RpcResult<Void>> apply(List<FlowModInputBuilder> flowModInputBuilders, BigInteger cookie);
    }

    public SalFlowServiceImpl(final RpcContext rpcContext) {
        super(rpcContext);
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        return processFlow(input, new Function() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final List<FlowModInputBuilder> flowModInputBuilders,
                    final BigInteger cookie) {
                return chainFlowMods(flowModInputBuilders, 0, input.getCookie().getValue());
            }
        });
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        return processFlow(input, new Function() {
            @Override
            public Future<RpcResult<Void>> apply(final List<FlowModInputBuilder> flowModInputBuilders,
                    final BigInteger cookie) {
                return provideConnectionAdapter(input.getCookie().getValue()).flowMod(
                        flowModInputBuilders.get(0).build());
            }
        });
    }

    // @Override
    // public Future<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
    // return processFlow(input, new Function() {
    // @Override
    // public Future<RpcResult<Void>> apply(final List<FlowModInputBuilder> flowModInputBuilders,
    // final BigInteger cookie) {
    // final UpdateFlowInput in = input;
    // final UpdatedFlow updated = in.getUpdatedFlow();
    // final OriginalFlow original = in.getOriginalFlow();
    //
    // final List<FlowModInputBuilder> allFlowMods = new ArrayList<>();
    // List<FlowModInputBuilder> ofFlowModInputs;
    //
    // if (!FlowCreatorUtil.canModifyFlow(original, updated, version)) {
    // // We would need to remove original and add updated.
    //
    // // remove flow
    // final RemoveFlowInputBuilder removeflow = new RemoveFlowInputBuilder(original);
    // final List<FlowModInputBuilder> ofFlowRemoveInput = FlowConvertor.toFlowModInputs(
    // removeflow.build(), version, datapathId);
    // // remove flow should be the first
    // allFlowMods.addAll(ofFlowRemoveInput);
    // final AddFlowInputBuilder addFlowInputBuilder = new AddFlowInputBuilder(updated);
    // ofFlowModInputs = FlowConvertor.toFlowModInputs(addFlowInputBuilder.build(), version, datapathId);
    // } else {
    // ofFlowModInputs = FlowConvertor.toFlowModInputs(updated, version, datapathId);
    // }
    //
    // allFlowMods.addAll(ofFlowModInputs);
    // LOG.debug("Number of flows to push to switch: {}", allFlowMods.size());
    // return chainFlowMods(allFlowMods, 0, cookie);
    // }
    // });
    // }

    private <T extends DataObject> Future<RpcResult<T>> processFlow(final NodeFlow input, final Function function) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");
        // use primary connection

        final RequestContext requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<T>> result = rpcContext.storeOrFail(requestContext);

        if (!result.isDone()) {
            try {
                // Convert the AddFlowInput to FlowModInput
                final List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(input, version,
                        datapathId);
                LOG.debug("Number of flows to push to switch: {}", ofFlowModInputs.size());
                final Future<RpcResult<Void>> resultFromOFLib = function.apply(ofFlowModInputs, input.getCookie()
                        .getValue());
                final RpcResult<Void> rpcResult = resultFromOFLib.get(getWaitTime(), TimeUnit.MILLISECONDS);
                if (!rpcResult.isSuccessful()) {
                    result.set(RpcResultBuilder.<T> failed().withRpcErrors(rpcResult.getErrors()).build());
                    requestContext.close();
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                result.set(RpcResultBuilder
                        .<T> failed()
                        .withError(RpcError.ErrorType.APPLICATION, "",
                                "Flow modification on device wasn't successfull.").build());
                requestContext.close();
            } catch (final Exception e) {
                result.set(RpcResultBuilder.<T> failed()
                        .withError(RpcError.ErrorType.APPLICATION, "", "Flow translation to OF JAVA failed.").build());
                requestContext.close();
            }

        } else {
            requestContext.close();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService#updateFlow(org.opendaylight
     * .yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput)
     */
    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(final UpdateFlowInput input) {
        // TODO Auto-generated method stub
        return null;
    }

}
