/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * @param <T> input type
 * @param <K> future output type
 */
public abstract class OFRpcTask<T, K> implements Callable<ListenableFuture<K>> {
    
    private OFRpcTaskContext taskContext;
    private T input;
    private SwitchConnectionDistinguisher cookie;
    
    /**
     * @param taskContext rpc task context
     * @param input  task input
     * @param cookie switch connection distinguisher cookie value
     */
    public OFRpcTask(OFRpcTaskContext taskContext, SwitchConnectionDistinguisher cookie, T input) {
        this.taskContext = taskContext;
        this.cookie = cookie;
        this.input = input;
    }

    /**
     * @return the cookie
     */
    public SwitchConnectionDistinguisher getCookie() {
        return cookie;
    }

    /**
     * @param cookie the cookie to set
     */
    public void setCookie(SwitchConnectionDistinguisher cookie) {
        this.cookie = cookie;
    }

    /**
     * @return the input
     */
    public T getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(T input) {
        this.input = input;
    }

    /**
     * @return the rpcNotificationProviderService
     */
    public NotificationProviderService getRpcNotificationProviderService() {
        return taskContext.getRpcNotificationProviderService();
    }

    /**
     * @return message service
     * @see org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTaskContext#getMessageService()
     */
    public IMessageDispatchService getMessageService() {
        return taskContext.getMessageService();
    }

    /**
     * @return session
     * @see org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTaskContext#getSession()
     */
    public SessionContext getSession() {
        return taskContext.getSession();
    }

    /**
     * @return max timeout
     * @see org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTaskContext#getMaxTimeout()
     */
    public long getMaxTimeout() {
        return taskContext.getMaxTimeout();
    }

    /**
     * @return time unit for max timeout
     * @see org.opendaylight.openflowplugin.openflow.md.core.sal.OFRpcTaskContext#getMaxTimeoutUnit()
     */
    public TimeUnit getMaxTimeoutUnit() {
        return taskContext.getMaxTimeoutUnit();
    }
    
    /**
     * @return protocol version
     */
    public Short getVersion() {
        return taskContext.getSession().getFeatures().getVersion();
        
    }
    
    /**
     * @return the taskContext
     */
    public OFRpcTaskContext getTaskContext() {
        return taskContext;
    }
    
    /**
     * submit task into rpc worker pool
     * @return future result of task 
     */
    public ListenableFuture<K> submit() {
        ListenableFuture<ListenableFuture<K>> compoundResult = getTaskContext().getRpcPool().submit(this);
        return Futures.dereference(compoundResult);
    }
    
    /**
     * @return required barrier value
     */
    public Boolean isBarrier() {
        return null;
    }
}
