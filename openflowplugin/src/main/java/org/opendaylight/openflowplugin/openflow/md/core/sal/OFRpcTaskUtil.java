/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.common.util.RpcErrors;
import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.MessageFactory;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * 
 */
public abstract class OFRpcTaskUtil {

    /**
     * @param taskContext 
     * @param isBarrier 
     * @param cookie 
     * @return rpcResult of given type, containing wrapped errors of barrier sending (if any) or success
     */
    public static Collection<RpcError> manageBarrier(OFRpcTaskContext taskContext, Boolean isBarrier, 
            SwitchConnectionDistinguisher cookie) {
        Collection<RpcError> errors = null;
        if (Objects.firstNonNull(isBarrier, Boolean.FALSE)) {
            Future<RpcResult<BarrierOutput>> barrierFuture = sendBarrier(taskContext.getSession(), cookie, taskContext.getMessageService());
            try {
                RpcResult<BarrierOutput> barrierResult = barrierFuture.get(
                        taskContext.getMaxTimeout(), taskContext.getMaxTimeoutUnit());
                if (!barrierResult.isSuccessful()) {
                    errors = barrierResult.getErrors();
                }
            } catch (Exception e) {
                RpcError rpcError = RpcErrors.getRpcError(
                        OFConstants.APPLICATION_TAG, OFConstants.ERROR_TAG_TIMEOUT, 
                        "barrier sending failed", ErrorSeverity.WARNING, 
                        "switch failed to respond on barrier request - message ordering is not preserved", ErrorType.RPC, e);
                errors = Lists.newArrayList(rpcError);
            }
        } 
        
        if (errors == null) {
            errors = Collections.emptyList();
        }
        
        return errors;
    }

    /**
     * @param session
     * @param cookie
     * @param messageService
     * @return barrier response
     */
    private static Future<RpcResult<BarrierOutput>> sendBarrier(SessionContext session, 
            SwitchConnectionDistinguisher cookie, IMessageDispatchService messageService) {
        BarrierInput barrierInput = MessageFactory.createBarrier(
                session.getFeatures().getVersion(), session.getNextXid());
        return messageService.barrier(barrierInput, cookie);
    }

    /**
     * @param result rpcResult with success = false, errors = given collection
     * @param barrierErrors
     */
    public static <T> void wrapBarrierErrors(SettableFuture<RpcResult<T>> result,
            Collection<RpcError> barrierErrors) {
        result.set(Rpcs.<T>getRpcResult(false, barrierErrors));
    }
    
    /**
     * @param originalResult
     * @param notificationProviderService
     * @param notificationComposer lazy notification composer
     */
    public static <R, N extends Notification> void hookFutureNotification(ListenableFuture<R> originalResult, 
            final NotificationProviderService notificationProviderService, 
            final NotificationComposer<N> notificationComposer) {
        Futures.addCallback(originalResult, new FutureCallback<R>() {
            @Override
            public void onSuccess(R result) {
                if (null != notificationProviderService) {
                    notificationProviderService.publish(notificationComposer.compose());
                }
            }
            
            @Override
            public void onFailure(Throwable t) {
                //NOOP
            }
        });
        
    }

}
