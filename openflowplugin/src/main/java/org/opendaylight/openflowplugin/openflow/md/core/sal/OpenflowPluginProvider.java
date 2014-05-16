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

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.openflow.md.core.MDController;
import org.opendaylight.openflowplugin.openflow.md.core.cmd.MessageCountCommandProvider;
import org.opendaylight.openflowplugin.openflow.md.queue.MessageObservatory;
import org.opendaylight.openflowplugin.openflow.md.queue.MessageSpyCounterImpl;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OFPlugin provider implementation
 */
public class OpenflowPluginProvider implements BindingAwareProvider, AutoCloseable {
    
    private static Logger LOG = LoggerFactory.getLogger(OpenflowPluginProvider.class);
    
    private BindingAwareBroker broker;

    private BundleContext context;

    private Collection<SwitchConnectionProvider> switchConnectionProviders;

    private MDController mdController;
    
    private MessageCountCommandProvider messageCountCommandProvider;

    private MessageObservatory<DataContainer> messageCountProvider;
    
    private SalRegistrationManager registrationManager;
    
    /**
     * @param switchConnectionProvider
     */
    public void setSwitchConnectionProviders(Collection<SwitchConnectionProvider> switchConnectionProvider) {
        this.switchConnectionProviders = switchConnectionProvider;
    }

    /**
     * @return osgi context
     */
    public BundleContext getContext() {
        return context;
    }

    /**
     * dependencymanager requirement 
     * @param context
     * 
     * @deprecated we should stop relying on osgi to provide cli interface for messageCounter 
     */
    @Deprecated
    public void setContext(BundleContext context) {
        this.context = context;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.debug("onSessionInitiated");
        messageCountProvider = new MessageSpyCounterImpl();
        registrationManager = new SalRegistrationManager();
        registrationManager.onSessionInitiated(session);
        mdController = new MDController();
        mdController.setSwitchConnectionProviders(switchConnectionProviders);
        mdController.setMessageSpyCounter(messageCountProvider);
        mdController.init();
        mdController.start();
        messageCountCommandProvider = new MessageCountCommandProvider(context, messageCountProvider);
        messageCountCommandProvider.onSessionInitiated(session);
    }
    
    @Override
    public void close() {
        LOG.debug("close");
        mdController.stop();
        mdController = null;
        registrationManager.close();
        registrationManager = null;
        messageCountCommandProvider.close();
        messageCountCommandProvider = null;
    }

    @Override
    public void onSessionInitialized(ConsumerContext session) {
        // NOOP
    }

    @Override
    public Collection<? extends ProviderFunctionality> getFunctionality() {
        return Collections.emptySet();
    }

    @Override
    public java.util.Collection<? extends RpcService> getImplementations() {
        return Collections.emptySet();
    }

    /**
     * @return BA default broker
     */
    public BindingAwareBroker getBroker() {
        return broker;
    }

    /**
     * dependencymanager requirement 
     * @param broker
     */
    public void setBroker(BindingAwareBroker broker) {
        this.broker = broker;
    }

    /**
     * dependencymanager requirement 
     * @param brokerArg
     */
    public void unsetBroker(BindingAwareBroker brokerArg) {
        this.broker = null;
    }

    private boolean hasAllDependencies(){
        if(this.broker != null && this.switchConnectionProviders != null) {
            return true;
        }
        return false;
    }
    
    /**
     * register providers for md-sal
     */
    public void registerProvider() {
        //FIXME: is it needed
        if(hasAllDependencies()) {
            this.broker.registerProvider(this,context);
        }
    }
 }
