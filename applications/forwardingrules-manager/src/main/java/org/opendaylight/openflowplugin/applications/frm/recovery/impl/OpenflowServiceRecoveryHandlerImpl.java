/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.recovery.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.openflowplugin.applications.frm.recovery.OpenflowServiceRecoveryHandler;
import org.opendaylight.serviceutils.srm.RecoverableListener;
import org.opendaylight.serviceutils.srm.ServiceRecoveryInterface;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.Ofplugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OpenflowServiceRecoveryHandlerImpl implements ServiceRecoveryInterface,
        OpenflowServiceRecoveryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowServiceRecoveryHandlerImpl.class);

    private final ServiceRecoveryRegistry serviceRecoveryRegistry;

    @Inject
    public OpenflowServiceRecoveryHandlerImpl(final ServiceRecoveryRegistry serviceRecoveryRegistry) {
        LOG.info("Registering openflowplugin service recovery handlers");
        this.serviceRecoveryRegistry = serviceRecoveryRegistry;
        serviceRecoveryRegistry.registerServiceRecoveryRegistry(buildServiceRegistryKey(), this);
    }

    private void deregisterListeners() {
        serviceRecoveryRegistry.getRecoverableListeners(buildServiceRegistryKey())
                .forEach(RecoverableListener::deregisterListener);
    }

    private void registerListeners() {
        serviceRecoveryRegistry.getRecoverableListeners(buildServiceRegistryKey())
                .forEach(RecoverableListener::registerListener);
    }

    @Override
    public void recoverService(final String entityId) {
        LOG.info("Recover Openflowplugin service by deregistering and registering all relevant listeners");
        deregisterListeners();
        //FIXME: device group registry cache to be cleared before starting the listeners
        registerListeners();
    }

    @Override
    public String buildServiceRegistryKey() {
        return Ofplugin.class.toString();
    }
}
