/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.config.yang.forwardingrules_manager;

import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerConfig;
import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardingRulesManagerModule extends org.opendaylight.openflowplugin.applications.config.yang.forwardingrules_manager.AbstractForwardingRulesManagerModule {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesManagerModule.class);
    private static final boolean ENABLE_FGM_STALE_MARKING = false;

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
        final ForwardingRulesManagerConfig config = readConfig();
        final ForwardingRulesManagerImpl forwardingrulessManagerProvider =
                new ForwardingRulesManagerImpl(getDataBrokerDependency(), getRpcRegistryDependency(), config);
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

    private ForwardingRulesManagerConfig readConfig(){

        final ForwardingRulesManagerConfig.ForwardingRulesManagerConfigBuilder fwdRulesMgrCfgBuilder = ForwardingRulesManagerConfig.builder();
        if (getForwardingManagerSettings() != null && getForwardingManagerSettings().getStaleMarkingEnabled() != null){
            fwdRulesMgrCfgBuilder.setStaleMarkingEnabled(getForwardingManagerSettings().getStaleMarkingEnabled());
        }
        else{
            LOG.warn("Could not load XML configuration file via ConfigSubsystem! Fallback to default config value(s)");
            fwdRulesMgrCfgBuilder.setStaleMarkingEnabled(ENABLE_FGM_STALE_MARKING);
        }

        return fwdRulesMgrCfgBuilder.build();

    }

}
