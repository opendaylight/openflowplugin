/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.opendaylight.infrautils.diagstatus.ServiceState.ERROR;
import static org.opendaylight.infrautils.diagstatus.ServiceState.OPERATIONAL;
import static org.opendaylight.infrautils.diagstatus.ServiceState.STARTING;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.openflowjava.protocol.api.connection.OpenflowDiagStatusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Service(classes = OpenflowDiagStatusProvider.class)
public class OpenflowDiagStatusProviderImpl implements OpenflowDiagStatusProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowDiagStatusProviderImpl.class);
    private static final String OPENFLOW_SERVICE = "OPENFLOW";
    private static final String OPENFLOW_SERVER_6633 = "OPENFLOW_SERVER_6633";
    private static final String OPENFLOW_SERVER_6653 = "OPENFLOW_SERVER_6653";
    private static final String OPENFLOW_SERVICE_AGGREGATE = OPENFLOW_SERVICE;

    private final DiagStatusService diagStatusService;
    private volatile Map<String, ServiceState> statusMap = new HashMap<>(Map.of(
        OPENFLOW_SERVICE, STARTING,
        OPENFLOW_SERVER_6633, STARTING,
        OPENFLOW_SERVER_6653, STARTING));

    @Inject
    public OpenflowDiagStatusProviderImpl(final @Reference DiagStatusService diagStatusService) {
        this.diagStatusService = diagStatusService;
        diagStatusService.register(OPENFLOW_SERVICE_AGGREGATE);
    }

    @Override
    public void reportStatus(final ServiceState serviceState) {
        LOG.debug("reporting status as {} for {}", serviceState,OPENFLOW_SERVICE_AGGREGATE);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_AGGREGATE, serviceState));
    }

    @Override
    public void reportStatus(final String diagStatusIdentifier, final Throwable throwable) {
        LOG.debug("Reporting error for {} as {}",  diagStatusIdentifier, throwable.toString());
        statusMap.replace(diagStatusIdentifier, ERROR);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_AGGREGATE, throwable));
    }

    @Override
    public void reportStatus(final String diagStatusIdentifier, final ServiceState serviceState,
                             final String description) {
        LOG.debug("Reporting status {} for {} and desc {}", serviceState, diagStatusIdentifier, description);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_AGGREGATE, serviceState, description));
    }

    @Override
    public void reportStatus(final String diagStatusIdentifier, final ServiceState serviceState) {
        statusMap.replace(diagStatusIdentifier, serviceState);
        LOG.info("The report status is {} for {}", serviceState, diagStatusIdentifier);
        reportStatus();
    }

    public void reportStatus() {
        boolean state = statusMap.values().stream().allMatch(serviceState -> serviceState.equals(OPERATIONAL));
        if (state) {
            reportStatus(OPERATIONAL);
        }
    }
}