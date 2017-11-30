/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.diagstatus;

import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowPluginDiagStatusProvider implements ServiceStatusProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginDiagStatusProvider.class);
    private static final String OPENFLOW_SERVICE_NAME = "OPENFLOW";

    private final DiagStatusService diagStatusService;
    private volatile ServiceDescriptor serviceDescriptor;

    public OpenflowPluginDiagStatusProvider(final DiagStatusService diagStatusService) {
        this.diagStatusService = diagStatusService;
        diagStatusService.register(OPENFLOW_SERVICE_NAME);
    }

    public void reportStatus(ServiceState serviceState, String description) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        serviceDescriptor = new ServiceDescriptor(OPENFLOW_SERVICE_NAME, serviceState, description);
        diagStatusService.report(serviceDescriptor);
    }

    @Override
    public ServiceDescriptor getServiceDescriptor() {
        // TODO Check 6653/6633 port status to report dynamic status
        return serviceDescriptor;
    }
}