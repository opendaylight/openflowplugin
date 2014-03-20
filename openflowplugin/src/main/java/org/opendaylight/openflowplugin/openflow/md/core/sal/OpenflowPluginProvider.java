/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.openflow.md.core.MDController;
import org.opendaylight.openflowplugin.openflow.md.core.cmd.MessageCountCommandProvider;
import org.opendaylight.openflowplugin.openflow.md.queue.MessageObservatory;
import org.opendaylight.openflowplugin.openflow.md.queue.MessageSpyCounterImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.osgi.framework.BundleContext;

import java.util.Collection;
import java.util.Collections;

/**
 * OFPlugin provider implementation
 */
public class OpenflowPluginProvider implements BindingAwareProvider, AutoCloseable {

    private BindingAwareBroker broker;

    private BundleContext context;

    private SwitchConnectionProvider switchConnectionProvider;

    private MDController mdController;
    
    private MessageCountCommandProvider messageCountCommandProvider;

    private MessageObservatory<OfHeader, DataObject> messageCountProvider;
    
    public void unsetSwitchConnectionProvider() {
        switchConnectionProvider = null;
    }

    public void setSwitchConnectionProvider(
            SwitchConnectionProvider switchConnectionProvider) {
        this.switchConnectionProvider = switchConnectionProvider;
        registerProvider();
    }

    public BundleContext getContext() {
        return context;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    SalRegistrationManager registrationManager = new SalRegistrationManager();


    @Override
    public void onSessionInitiated(ProviderContext session) {
        messageCountProvider = new MessageSpyCounterImpl();
        registrationManager.onSessionInitiated(session);
        mdController = new MDController();
        mdController.setSwitchConnectionProvider(switchConnectionProvider);
        mdController.setMessageSpyCounter(messageCountProvider);
        mdController.init();
        mdController.startController();
        messageCountCommandProvider = new MessageCountCommandProvider(context, messageCountProvider);
        messageCountCommandProvider.onSessionInitiated(session);
    }
    
    @Override
    public void close() {
        mdController.stop();
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

    public BindingAwareBroker getBroker() {
        return broker;
    }

    public void setBroker(BindingAwareBroker broker) {
        this.broker = broker;
        registerProvider();
    }

    public void unsetBroker(BindingAwareBroker broker) {
        this.broker = null;
    }

    private boolean hasAllDependencies(){
        if(this.broker != null && this.switchConnectionProvider != null) {
            return true;
        }
        return false;
    }
    private void registerProvider() {
        if(hasAllDependencies()) {
            this.broker.registerProvider(this,context);
        }
    }
 }
