/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProviderList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OpenflowPluginDiagStatusProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginDiagStatusProvider.class);
    private static final String OPENFLOW_SERVICE_NAME = "OPENFLOW";

    private final DiagStatusService diagStatusService;

    @Inject
    public OpenflowPluginDiagStatusProvider(final @Reference DiagStatusService diagStatusService,
                                            final SwitchConnectionProviderList switchConnectionProviders) {
        this.diagStatusService = diagStatusService;
        diagStatusService.register(OPENFLOW_SERVICE_NAME);
    }

    public void reportStatus(ServiceState serviceState) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_NAME, serviceState));
    }

    public void reportStatus(ServiceState serviceState, Throwable throwable) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_NAME, throwable));
    }

    public void reportStatus(ServiceState serviceState, String description) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_NAME, serviceState, description));
    }
}
