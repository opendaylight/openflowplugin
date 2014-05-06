/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;

import com.google.common.util.concurrent.SettableFuture;

/**
 * @param <T> input type
 * @param <K> future output type
 */
public abstract class OFRpcTask<T, K> implements Runnable {
    
    private SwitchConnectionDistinguisher cookie;
    private IMessageDispatchService messageService;
    private SessionContext session;
    private T input;
    private SettableFuture<K> result;
    private NotificationProviderService rpcNotificationProviderService;
    
    /**
     * @return the result
     */
    public SettableFuture<K> getResult() {
        return result;
    }
    
    /**
     * @param result the result to set
     */
    public void setResult(SettableFuture<K> result) {
        this.result = result;
    }

    /**
     * @return the cookie
     */
    public SwitchConnectionDistinguisher getCookie() {
        return cookie;
    }

    /**
     * @return the messageService
     */
    public IMessageDispatchService getMessageService() {
        return messageService;
    }

    /**
     * @return the session
     */
    public SessionContext getSession() {
        return session;
    }
    
    /**
     * @return protocol version
     */
    public Short getVersion() {
        return session.getFeatures().getVersion();
    }

    /**
     * @param cookie the cookie to set
     */
    public void setCookie(SwitchConnectionDistinguisher cookie) {
        this.cookie = cookie;
    }

    /**
     * @param messageService the messageService to set
     */
    public void setMessageService(IMessageDispatchService messageService) {
        this.messageService = messageService;
    }

    /**
     * @param session the session to set
     */
    public void setSession(SessionContext session) {
        this.session = session;
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
     * @param rpcNotificationProviderService
     */
    public void setRpcNotificationProviderService(
            NotificationProviderService rpcNotificationProviderService) {
                this.rpcNotificationProviderService = rpcNotificationProviderService;
    }
    
    /**
     * @return the rpcNotificationProviderService
     */
    public NotificationProviderService getRpcNotificationProviderService() {
        return rpcNotificationProviderService;
    }
}
