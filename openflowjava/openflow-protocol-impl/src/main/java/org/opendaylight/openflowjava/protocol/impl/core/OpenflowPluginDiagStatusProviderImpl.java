/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import org.apache.aries.blueprint.annotation.service.Reference;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.openflowjava.protocol.spi.connection.OpenflowPluginDiagStatusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Service(classes = OpenflowPluginDiagStatusProvider.class)
public class OpenflowPluginDiagStatusProviderImpl implements OpenflowPluginDiagStatusProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginDiagStatusProviderImpl.class);
    private static final String OPENFLOW_SERVICE_NAME = "OPENFLOW";

    private final DiagStatusService diagStatusService;

    @Inject
    public OpenflowPluginDiagStatusProviderImpl(final @Reference DiagStatusService diagStatusService) {
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
