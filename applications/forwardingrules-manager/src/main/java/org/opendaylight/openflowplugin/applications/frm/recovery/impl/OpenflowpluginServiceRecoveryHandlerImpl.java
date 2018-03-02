/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.recovery.impl;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opendaylight.genius.srm.RecoverableListener;
import org.opendaylight.genius.srm.ServiceRecoveryInterface;
import org.opendaylight.genius.srm.ServiceRecoveryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.Ofplugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OpenflowpluginServiceRecoveryHandlerImpl implements ServiceRecoveryInterface,
        OpenflowpluginServiceRecoveryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginServiceRecoveryHandlerImpl.class);

    private final ServiceRecoveryRegistry serviceRecoveryRegistry;

    @Inject
    public OpenflowpluginServiceRecoveryHandlerImpl(final ServiceRecoveryRegistry serviceRecoveryRegistry) {
        LOG.info("registering openflowplugin service recovery handlers");
        this.serviceRecoveryRegistry = serviceRecoveryRegistry;
        serviceRecoveryRegistry.registerServiceRecoveryRegistry(buildServiceRegistryKey(), this);
    }

    private void deregisterListeners() {
        List<RecoverableListener> recoverableListeners =
                serviceRecoveryRegistry.getRecoverableListeners(buildServiceRegistryKey());
        recoverableListeners.forEach((recoverableListener -> recoverableListener.deregisterListener()));
    }

    private void registerListeners() {
        List<RecoverableListener> recoverableListeners =
                serviceRecoveryRegistry.getRecoverableListeners(buildServiceRegistryKey());
        recoverableListeners.forEach((recoverableListener -> recoverableListener.registerListener()));
    }

    @Override
    public void recoverService(final String entityId) {
        LOG.info("recover Openflowplugin service by deregistering and registering all relevant listeners");
        deregisterListeners();
        registerListeners();
    }

    @Override
    public String buildServiceRegistryKey() {
        return Ofplugin.class.toString();
    }
}
