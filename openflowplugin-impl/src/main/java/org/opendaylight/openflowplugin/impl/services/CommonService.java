/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.NotificationComposer;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionAware;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;

public class CommonService {
    // protected OFRpcTaskContext rpcTaskContext;
    protected short version;
    protected BigInteger datapathId;
    protected RpcContext rpcContext;
    protected SwitchConnectionDistinguisher cookie;
    // TODO should come from deviceContext
    protected IMessageDispatchService messageService;
    protected Xid xid;
    protected Boolean isBarrier;

    protected NotificationProviderService notificationProviderService;

    protected final static Future<RpcResult<Void>> errorRpcResult = Futures.immediateFuture(RpcResultBuilder
            .<Void> failed().withError(ErrorType.APPLICATION, "", "Request quota exceeded.").build());

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CommonService.class);

    public CommonService() {

    }

    /**
     * @param xid
     * 
     */
    public CommonService(final RpcContext rpcContext, final short version, final BigInteger datapathId,
            final IMessageDispatchService service, final Xid xid, final SwitchConnectionDistinguisher cookie) {
        this.rpcContext = rpcContext;
        this.version = version;
        this.datapathId = datapathId;
        this.messageService = service;
        this.xid = xid;
        this.cookie = cookie;
    }

    /**
     * @param task
     *            of rpc
     * @param originalResult
     * @param notificationProviderService
     * @param notificationComposer
     *            lazy notification composer
     */
    protected <R extends RpcResult<? extends TransactionAware>, N extends Notification, I extends DataContainer> void hookFutureNotification(
            final ListenableFuture<R> originalResult, final NotificationProviderService notificationProviderService,
            final NotificationComposer<N> notificationComposer) {

        class FutureCallbackImpl implements FutureCallback<R> {
            @Override
            public void onSuccess(final R result) {
                if (null == notificationProviderService) {
                    LOG.warn("onSuccess(): notificationServiceProvider is null, could not publish result {}", result);
                } else if (notificationComposer == null) {
                    LOG.warn("onSuccess(): notificationComposer is null, could not publish result {}", result);
                } else if (result == null) {
                    LOG.warn("onSuccess(): result is null, could not publish result {}", result);
                } else if (result.getResult() == null) {
                    LOG.warn("onSuccess(): result.getResult() is null, could not publish result {}", result);
                } else if (result.getResult().getTransactionId() == null) {
                    LOG.warn("onSuccess(): result.getResult().getTransactionId() is null, could not publish result {}",
                            result);
                } else {
                    notificationProviderService.publish(notificationComposer.compose(result.getResult()
                            .getTransactionId()));
                    // TODO: solve without task
                    // task.getTaskContext().getMessageSpy().spyMessage(
                    // task.getInput(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMITTED_SUCCESS);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                // TODO: good place to notify MD-SAL about errors
                // TODO: solve without task
                // task.getTaskContext().getMessageSpy().spyMessage(
                // task.getInput(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMITTED_FAILURE);
            }
        }

        Futures.addCallback(originalResult, new FutureCallbackImpl());
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

    /**
     * @param originalInput
     * @return
     */
    protected static <T extends TransactionAware> Function<RpcResult<BarrierOutput>, RpcResult<T>> transformBarrierToTransactionAware(
            final RpcResult<T> originalInput, final BarrierInput barrierInput) {

        class FunctionImpl implements Function<RpcResult<BarrierOutput>, RpcResult<T>> {

            @Override
            public RpcResult<T> apply(final RpcResult<BarrierOutput> barrierResult) {
                RpcResultBuilder<T> rpcBuilder = null;
                if (barrierResult.isSuccessful()) {
                    rpcBuilder = RpcResultBuilder.<T> success();
                } else {
                    rpcBuilder = RpcResultBuilder.<T> failed();
                    final RpcError rpcError = RpcResultBuilder
                            .newWarning(
                                    ErrorType.RPC,
                                    OFConstants.ERROR_TAG_TIMEOUT,
                                    "barrier sending failed",
                                    OFConstants.APPLICATION_TAG,
                                    "switch failed to respond on barrier request, barrier.xid = "
                                            + barrierInput.getXid(), null);
                    final List<RpcError> chainedErrors = new ArrayList<>();
                    chainedErrors.add(rpcError);
                    chainedErrors.addAll(barrierResult.getErrors());
                    rpcBuilder.withRpcErrors(chainedErrors);
                }

                rpcBuilder.withResult(originalInput.getResult());

                return rpcBuilder.build();
            }
        }

        return new FunctionImpl();
    }

}
