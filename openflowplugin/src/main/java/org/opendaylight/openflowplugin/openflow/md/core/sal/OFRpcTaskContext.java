/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.yangtools.yang.binding.DataContainer;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * 
 */
public class OFRpcTaskContext {

    private IMessageDispatchService messageService;
    private SessionContext session;
    private NotificationProviderService rpcNotificationProviderService;
    private long maxTimeout;
    private TimeUnit maxTimeoutUnit;
    private ListeningExecutorService rpcPool;
    private MessageSpy<DataContainer> messageSpy;
    
    /**
     * @return the messageService
     */
    public IMessageDispatchService getMessageService() {
        return messageService;
    }
    /**
     * @param messageService the messageService to set
     */
    public void setMessageService(IMessageDispatchService messageService) {
        this.messageService = messageService;
    }
    /**
     * @return the session
     */
    public SessionContext getSession() {
        return session;
    }
    /**
     * @param session the session to set
     */
    public void setSession(SessionContext session) {
        this.session = session;
    }
    /**
     * @return the rpcNotificationProviderService
     */
    public NotificationProviderService getRpcNotificationProviderService() {
        return rpcNotificationProviderService;
    }
    /**
     * @param rpcNotificationProviderService the rpcNotificationProviderService to set
     */
    public void setRpcNotificationProviderService(
            NotificationProviderService rpcNotificationProviderService) {
        this.rpcNotificationProviderService = rpcNotificationProviderService;
    }
    /**
     * @return the maxTimeout
     */
    public long getMaxTimeout() {
        return maxTimeout;
    }
    /**
     * @param maxTimeout the maxTimeout to set
     */
    public void setMaxTimeout(long maxTimeout) {
        this.maxTimeout = maxTimeout;
    }
    /**
     * @return the maxTimeoutUnit
     */
    public TimeUnit getMaxTimeoutUnit() {
        return maxTimeoutUnit;
    }
    /**
     * @param maxTimeoutUnit the maxTimeoutUnit to set
     */
    public void setMaxTimeoutUnit(TimeUnit maxTimeoutUnit) {
        this.maxTimeoutUnit = maxTimeoutUnit;
    }
    /**
     * @param rpcPool executor service pool for rpc
     */
    public void setRpcPool(ListeningExecutorService rpcPool) {
        this.rpcPool = rpcPool;
    }
    
    /**
     * @return the rpcPool
     */
    public ListeningExecutorService getRpcPool() {
        return rpcPool;
    }
    
    /**
     * @param messageSpy the message spy
     */
    public void setMessageSpy(MessageSpy<DataContainer> messageSpy) {
        this.messageSpy = messageSpy;
    }
    /**
     * @return the messageSpy
     */
    public MessageSpy<DataContainer> getMessageSpy() {
        return messageSpy;
    }
}
