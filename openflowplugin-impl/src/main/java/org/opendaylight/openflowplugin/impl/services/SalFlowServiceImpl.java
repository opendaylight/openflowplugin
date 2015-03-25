/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.NotificationComposer;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcFutureResultTransformFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class SalFlowServiceImpl extends CommonService implements SalFlowService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SalFlowServiceImpl.class);

    public SalFlowServiceImpl(final RpcContext rpcContext, final short version, final BigInteger datapathId,
            final IMessageDispatchService service, final Xid xid, final SwitchConnectionDistinguisher cookie) {
        // TODO set cookie
        super(rpcContext, version, datapathId, service, xid, cookie);
    }

    public SalFlowServiceImpl(final RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService#addFlow(org.opendaylight.
     * yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput)
     */
    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(final AddFlowInput input) {
        LOG.debug("Calling the FlowMod RPC method on MessageDispatchService");
        // use primary connection
        final SwitchConnectionDistinguisher cookie = null;

        final RequestContext requestContext = rpcContext.createRequestContext();
        ListenableFuture<RpcResult<UpdateFlowOutput>> result = rpcContext.storeOrFail(requestContext);

        if (!result.isDone()) {

            // Convert the AddFlowInput to FlowModInput
            final List<FlowModInputBuilder> ofFlowModInputs = FlowConvertor.toFlowModInputs(input, version, datapathId);
            LOG.debug("Number of flows to push to switch: {}", ofFlowModInputs.size());
            result = chainFlowMods(ofFlowModInputs, 0, xid, cookie);
            result = chainFutureBarrier(result);
            hookFutureNotification(result, notificationProviderService, createFlowAddedNotification(input));
        } else {
            requestContext.close();
        }
        return Futures.transform(result, OFRpcFutureResultTransformFactory.createForAddFlowOutput());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService#removeFlow(org.opendaylight
     * .yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput)
     */
    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(final RemoveFlowInput input) {
        final RequestContext requestContext = rpcContext.createRequestContext();
        final SettableFuture<RpcResult<RemoveFlowOutput>> result = rpcContext.storeOrFail(requestContext);

        if (!result.isDone()) {
            try {
                // Convert the AddFlowInput to FlowModInput
                final FlowModInputBuilder ofFlowModInput = FlowConvertor.toFlowModInput(input, version, datapathId);
                ofFlowModInput.setXid(deviceContext.getNextXid().getValue());
                final Future<RpcResult<Void>> resultFromOFLib = provideConnectionAdapter(input.getCookie().getValue())
                        .flowMod(ofFlowModInput.build());
                final RpcResult<Void> rpcResult = resultFromOFLib.get(getWaitTime(), TimeUnit.MILLISECONDS);
                if (!rpcResult.isSuccessful()) {
                    result.set(RpcResultBuilder.<RemoveFlowOutput> failed().withRpcErrors(rpcResult.getErrors())
                            .build());
                    requestContext.close();
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                result.set(RpcResultBuilder
                        .<RemoveFlowOutput> failed()
                        .withError(RpcError.ErrorType.APPLICATION, "",
                                "Flow modification on device wasn't successfull.").build());
                requestContext.close();
            } catch (final Exception e) {
                result.set(RpcResultBuilder.<RemoveFlowOutput> failed()
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
        ListenableFuture<RpcResult<UpdateFlowOutput>> result = null;

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
        LOG.debug("Number of flows to push to switch: {}", allFlowMods.size());
        result = chainFlowMods(allFlowMods, 0, xid, cookie);

        result = chainFutureBarrier(result);
        hookFutureNotification(result, notificationProviderService, createFlowUpdatedNotification(in));
        return result;
    }

    /**
     * @param input
     * @return
     */
    protected NotificationComposer<FlowAdded> createFlowAddedNotification(final AddFlowInput input) {
        return new NotificationComposer<FlowAdded>() {
            @Override
            public FlowAdded compose(final TransactionId tXid) {
                final FlowAddedBuilder newFlow = new FlowAddedBuilder((Flow) input);
                newFlow.setTransactionId(tXid);
                newFlow.setFlowRef(input.getFlowRef());
                return newFlow.build();
            }
        };
    }

    protected NotificationComposer<FlowUpdated> createFlowUpdatedNotification(final UpdateFlowInput input) {
        return new NotificationComposer<FlowUpdated>() {
            @Override
            public FlowUpdated compose(final TransactionId tXid) {
                final FlowUpdatedBuilder updFlow = new FlowUpdatedBuilder(input.getUpdatedFlow());
                updFlow.setTransactionId(tXid);
                updFlow.setFlowRef(input.getFlowRef());
                return updFlow.build();
            }
        };
    }

    protected static NotificationComposer<FlowRemoved> createFlowRemovedNotification(final RemoveFlowInput input) {
        return new NotificationComposer<FlowRemoved>() {
            @Override
            public FlowRemoved compose(final TransactionId tXid) {
                final FlowRemovedBuilder removedFlow = new FlowRemovedBuilder((Flow) input);
                removedFlow.setTransactionId(tXid);
                removedFlow.setFlowRef(input.getFlowRef());
                return removedFlow.build();
            }
        };
    }
}
