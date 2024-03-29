/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.recovery.impl;

import static java.util.Objects.requireNonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.openflowplugin.applications.frm.recovery.OpenflowServiceRecoveryHandler;
import org.opendaylight.serviceutils.srm.RecoverableListener;
import org.opendaylight.serviceutils.srm.ServiceRecoveryInterface;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.Ofplugin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = OpenflowServiceRecoveryHandler.class, immediate = true)
public final class OpenflowServiceRecoveryHandlerImpl
        implements ServiceRecoveryInterface, OpenflowServiceRecoveryHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowServiceRecoveryHandlerImpl.class);

    private final ServiceRecoveryRegistry serviceRecoveryRegistry;

    @Inject
    @Activate
    public OpenflowServiceRecoveryHandlerImpl(@Reference final ServiceRecoveryRegistry serviceRecoveryRegistry) {
        this.serviceRecoveryRegistry = requireNonNull(serviceRecoveryRegistry);
        // FIXME: how can we undo this registration?
        LOG.info("Registering openflowplugin service recovery handlers");
        serviceRecoveryRegistry.registerServiceRecoveryRegistry(buildServiceRegistryKey(), this);
    }

    @Override
    public void recoverService(final String entityId) {
        LOG.info("Recover Openflowplugin service by deregistering and registering all relevant listeners");
        serviceRecoveryRegistry.getRecoverableListeners(buildServiceRegistryKey())
            .forEach(RecoverableListener::deregisterListener);
        // FIXME: device group registry cache to be cleared before starting the listeners
        serviceRecoveryRegistry.getRecoverableListeners(buildServiceRegistryKey())
            .forEach(RecoverableListener::registerListener);
    }

    @Override
    public String buildServiceRegistryKey() {
        return Ofplugin.class.toString();
    }
}
