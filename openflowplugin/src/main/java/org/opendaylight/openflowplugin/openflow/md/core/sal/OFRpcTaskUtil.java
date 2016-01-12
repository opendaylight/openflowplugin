/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.NotificationComposer;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.util.RpcInputOutputTuple;
import org.opendaylight.openflowplugin.openflow.md.util.TaskUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionAware;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class OFRpcTaskUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(OFRpcTaskUtil.class);
    /**
     * @param taskContext
     * @param isBarrier
     * @param cookie
     * @return rpcResult of given type, containing wrapped errors of barrier sending (if any) or success
     */
    private OFRpcTaskUtil() {
        //hiding implicit constructor
    }

    public static Collection<RpcError> manageBarrier(final OFRpcTaskContext taskContext, final Boolean isBarrier,
            final SwitchConnectionDistinguisher cookie) {
        Collection<RpcError> errors = null;
        if (MoreObjects.firstNonNull(isBarrier, Boolean.FALSE)) {
            RpcInputOutputTuple<BarrierInput, ListenableFuture<RpcResult<BarrierOutput>>> sendBarrierRpc =
                    TaskUtil.sendBarrier(taskContext.getSession(), cookie, taskContext.getMessageService());
            Future<RpcResult<BarrierOutput>> barrierFuture = sendBarrierRpc.getOutput();
            try {
                RpcResult<BarrierOutput> barrierResult = barrierFuture.get(
                        taskContext.getMaxTimeout(), taskContext.getMaxTimeoutUnit());
                if (!barrierResult.isSuccessful()) {
                    errors = barrierResult.getErrors();
                }
            } catch (Exception e) {
                RpcError rpcError = RpcResultBuilder.newWarning(
                        ErrorType.RPC,
                        OFConstants.ERROR_TAG_TIMEOUT,
                        "barrier sending failed",
                        OFConstants.APPLICATION_TAG,
                        "switch failed to respond on barrier request - message ordering is not preserved",
                        e);
                errors = Lists.newArrayList(rpcError);
            }
        }

        if (errors == null) {
            errors = Collections.emptyList();
        }

        return errors;
    }

    /**
     * @param task task
     * @param originalResult original result
     * @param notificationProviderService notification provider service
     * @param notificationComposer lazy notification composer
     * @param <I> data container
     * @param <N> notification
     * @param <R> R
     */
    public static <R extends RpcResult<? extends TransactionAware>, N extends Notification, I extends DataContainer>
    void hookFutureNotification(
            final OFRpcTask<I, R> task,
            final ListenableFuture<R> originalResult,
            final NotificationProviderService notificationProviderService,
            final NotificationComposer<N> notificationComposer) {

        class FutureCallbackImpl implements FutureCallback<R> {
            @Override
            public void onSuccess(final R result) {
                if(null == notificationProviderService) {
                    LOG.warn("onSuccess(): notificationServiceProvider is null, could not publish result {}",result);
                } else if (notificationComposer == null) {
                    LOG.warn("onSuccess(): notificationComposer is null, could not publish result {}",result);
                } else if(result == null) {
                    LOG.warn("onSuccess(): result is null, could not publish result {}",result);
                } else if (result.getResult() == null) {
                    LOG.warn("onSuccess(): result.getResult() is null, could not publish result {}",result);
                } else if (result.getResult().getTransactionId() == null) {
                    LOG.warn("onSuccess(): result.getResult().getTransactionId() is null, could not publish result {}",result);
                } else {
                    notificationProviderService.publish(notificationComposer.compose(result.getResult().getTransactionId()));
                    task.getTaskContext().getMessageSpy().spyMessage(
                            task.getInput(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMITTED_SUCCESS);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                //TODO: good place to notify MD-SAL about errors
                task.getTaskContext().getMessageSpy().spyMessage(
                        task.getInput(), MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMITTED_FAILURE);
            }
        }

        Futures.addCallback(originalResult, new FutureCallbackImpl());
    }

    /**
     * @param task of rpc
     * @param originalResult original result
     * @param <T> R
     * @param <I> I
     * @return chained result with barrier
     */
    public static <T extends TransactionAware, I extends DataContainer>
    ListenableFuture<RpcResult<T>> chainFutureBarrier(
            final OFRpcTask<I, RpcResult<T>> task,
            final ListenableFuture<RpcResult<T>> originalResult) {

        ListenableFuture<RpcResult<T>> chainResult = originalResult;
        if (MoreObjects.firstNonNull(task.isBarrier(), Boolean.FALSE)) {

            chainResult = Futures.transform(originalResult, new AsyncFunction<RpcResult<T>, RpcResult<T>>() {

                @Override
                public ListenableFuture<RpcResult<T>> apply(final RpcResult<T> input) throws Exception {
                    if (input.isSuccessful()) {
                        RpcInputOutputTuple<BarrierInput, ListenableFuture<RpcResult<BarrierOutput>>> sendBarrierRpc = TaskUtil.sendBarrier(
                                task.getSession(), task.getCookie(), task.getMessageService());
                        ListenableFuture<RpcResult<T>> barrierTxResult = Futures.transform(
                                sendBarrierRpc.getOutput(),
                                transformBarrierToTransactionAware(input, sendBarrierRpc.getInput()));
                        return barrierTxResult;
                    } else {
                        return Futures.immediateFuture(input);
                    }
                }

            });
        }

        return chainResult;
    }

    /**
     * @param originalInput original input
     * @param barrierInput barrier input
     * @param <T> T
     * @return result
     */
    protected static <T extends TransactionAware> Function<RpcResult<BarrierOutput>, RpcResult<T>> transformBarrierToTransactionAware(
            final RpcResult<T> originalInput, final BarrierInput barrierInput) {

        class FunctionImpl implements Function<RpcResult<BarrierOutput>, RpcResult<T>> {

            @Override
            public RpcResult<T> apply(final RpcResult<BarrierOutput> barrierResult) {
                RpcResultBuilder<T> rpcBuilder = null;
                if (barrierResult.isSuccessful()) {
                    rpcBuilder = RpcResultBuilder.<T>success();
                } else {
                    rpcBuilder = RpcResultBuilder.<T>failed();
                    RpcError rpcError = RpcResultBuilder.newWarning(
                            ErrorType.RPC,
                            OFConstants.ERROR_TAG_TIMEOUT,
                            "barrier sending failed",
                            OFConstants.APPLICATION_TAG,
                            "switch failed to respond on barrier request, barrier.xid = "+barrierInput.getXid(),
                            null);
                    List<RpcError> chainedErrors = new ArrayList<>();
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
