/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.net.InetAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueProcessorLightImpl;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * basic interconnecting piece between plugin and library 
 */
public class SwitchConnectionHandlerImpl implements SwitchConnectionHandler {
    
    private ScheduledThreadPoolExecutor spyPool; 

    private QueueProcessorLightImpl queueProcessor;
    private ErrorHandler errorHandler;
    private MessageSpy<DataContainer> messageSpy;
    private int spyRate = 10;

    /**
     *
     */
    public SwitchConnectionHandlerImpl() {
        queueProcessor = new QueueProcessorLightImpl();
        
        //TODO: implement shutdown invocation upon service stop event
        spyPool = new ScheduledThreadPoolExecutor(1);
    }

    /**
     * wire all up
     */
    public void init() {
        queueProcessor.setTranslatorMapping(OFSessionUtil.getTranslatorMap());
        queueProcessor.setPopListenersMapping(OFSessionUtil.getPopListenerMapping());
        queueProcessor.setMessageSpy(messageSpy);
        
        queueProcessor.init();
        
        spyPool.scheduleAtFixedRate(messageSpy, spyRate, spyRate, TimeUnit.SECONDS);
    }

    @Override
    public boolean accept(InetAddress address) {
        // TODO:: add policy derived rules
        return true;
    }

    @Override
    public void onSwitchConnected(ConnectionAdapter connectionAdapter) {
        ConnectionConductor conductor = ConnectionConductorFactory.createConductor(
                connectionAdapter, queueProcessor);
        conductor.setErrorHandler(errorHandler);
    }
    
    /**
     * @param messageSpy the messageSpy to set
     */
    public void setMessageSpy(MessageSpy<DataContainer> messageSpy) {
        this.messageSpy = messageSpy;
    }
    
    /**
     * @param errorHandler the errorHandler to set
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

}
