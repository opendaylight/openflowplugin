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
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OFPlugin provider implementation
 */
public class OpenflowPluginProvider implements BindingAwareProvider {
    private final static Logger LOG = LoggerFactory.getLogger(OpenflowPluginProvider.class);
    private BindingAwareBroker broker;

    private BundleContext context;
    private DataProviderService dataService;
    private InventoryDataServiceUtil inventoryUtil;

    public BundleContext getContext() {
        return context;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    SalRegistrationManager registrationManager = new SalRegistrationManager();

    @Override
    public void onSessionInitiated(ProviderContext session) {
        registrationManager.onSessionInitiated(session);
        dataService = session.getSALService(DataProviderService.class);
        LOG.error("About to instantiate InventoryUtil: " + inventoryUtil);
        inventoryUtil = new InventoryDataServiceUtil(dataService);
        LOG.error("OpenflowPlugin Provider Initialized. InventoryUtil: " + inventoryUtil);

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
        broker.registerProvider(this, context);
    };

    public void unsetBroker(BindingAwareBroker broker) {
        this.broker = null;
    };
}
