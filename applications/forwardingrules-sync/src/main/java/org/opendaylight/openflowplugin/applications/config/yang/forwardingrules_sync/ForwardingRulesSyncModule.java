/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.config.yang.forwardingrules_sync;

import org.opendaylight.openflowplugin.applications.frsync.impl.ForwardingRulesSyncProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardingRulesSyncModule extends org.opendaylight.openflowplugin.applications.config.yang.forwardingrules_sync.AbstractForwardingRulesSyncModule {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesSyncModule.class);

    public ForwardingRulesSyncModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public ForwardingRulesSyncModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.openflowplugin.applications.config.yang.forwardingrules_sync.ForwardingRulesSyncModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("FRSync module initialization.");

        final ForwardingRulesSyncProvider forwardingRulesSyncProvider =
                new ForwardingRulesSyncProvider(getBrokerDependency(), getDataBrokerDependency(), getRpcRegistryDependency());

        return forwardingRulesSyncProvider;
    }
}