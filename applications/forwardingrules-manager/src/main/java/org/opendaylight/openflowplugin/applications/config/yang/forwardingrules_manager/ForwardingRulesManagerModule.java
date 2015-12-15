/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.config.yang.forwardingrules_manager;

import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardingRulesManagerModule extends org.opendaylight.openflowplugin.applications.config.yang.forwardingrules_manager.AbstractForwardingRulesManagerModule {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesManagerModule.class);

    public ForwardingRulesManagerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public ForwardingRulesManagerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.openflowplugin.applications.config.yang.forwardingrules_manager.ForwardingRulesManagerModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("FRM module initialization.");
        final ForwardingRulesManagerImpl forwardingrulessManagerProvider =
                new ForwardingRulesManagerImpl(getDataBrokerDependency(), getRpcRegistryDependency());
        forwardingrulessManagerProvider.start();
        LOG.info("FRM module started successfully.");
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                try {
                    forwardingrulessManagerProvider.close();
                } catch (final Exception e) {
                    LOG.warn("Unexpected error by stopping FRM", e);
                }
                LOG.info("FRM module stopped.");
            }
        };
    }

}
