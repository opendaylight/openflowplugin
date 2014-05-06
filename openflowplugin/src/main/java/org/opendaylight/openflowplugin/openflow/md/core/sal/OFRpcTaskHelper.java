/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.SettableFuture;

/**
 * 
 */
public class OFRpcTaskHelper {

    private IMessageDispatchService messageService;
    private SessionContext session;
    private NotificationProviderService rpcNotificationProviderService;
    /**
     * @param cookie
     * @param messageService
     * @param session
     * @param rpcNotificationProviderService 
     */
    public OFRpcTaskHelper(IMessageDispatchService messageService, SessionContext session, 
            NotificationProviderService rpcNotificationProviderService) {
        this.messageService = messageService;
        this.session = session;
        this.rpcNotificationProviderService = rpcNotificationProviderService;
    }
    
    
    /**
     * @param task
     * @param input 
     * @param cookie 
     * @return inited task
     */
    public <T, K> OFRpcTask<T, K> initTask(OFRpcTask<T, K> task, T input, SwitchConnectionDistinguisher cookie) {
        task.setMessageService(messageService);
        task.setSession(session);
        task.setRpcNotificationProviderService(rpcNotificationProviderService);
        task.setResult(SettableFuture.<K>create());
        task.setCookie(cookie);
        task.setInput(input);
        return task;
    }
    
    /**
     * @param intern 
     * @param wrapper 
     */
    public static <K> void chainFutures(final Future<K> intern, final SettableFuture<K> wrapper) {
        Futures.addCallback(
                JdkFutureAdapters.listenInPoolThread(intern),
                new FutureCallback<K>() {

                    @Override
                    public void onSuccess(
                            K result) {
                        wrapper.set(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        wrapper.setException(t);
                    }

                });
    }
    
    /**
     * @param maxTimeout
     * @param maxTimeoutUnit
     * @param isBarrier 
     * @param cookie 
     * @param result 
     */
    public <T> void rawBarrierSend(final long maxTimeout, final TimeUnit maxTimeoutUnit, 
            Boolean isBarrier, SwitchConnectionDistinguisher cookie, SettableFuture<RpcResult<T>> result) {
        if (Objects.firstNonNull(isBarrier, Boolean.FALSE)) {
            Future<RpcResult<BarrierOutput>> barrierFuture = ModelDrivenSwitchImpl.sendBarrier(cookie, session, messageService);
            try {
                RpcResult<BarrierOutput> barrierResult = barrierFuture.get(maxTimeout, maxTimeoutUnit);
                if (!barrierResult.isSuccessful()) {
                    result.set(Rpcs.<T>getRpcResult(false, barrierResult.getErrors()));
                }
            } catch (Exception e) {
                result.setException(e);
            }
        }
    }
}
