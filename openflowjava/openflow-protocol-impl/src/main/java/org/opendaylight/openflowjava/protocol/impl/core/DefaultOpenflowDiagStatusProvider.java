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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceRegistration;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.openflowjava.protocol.api.connection.OpenflowDiagStatusProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component
public final class DefaultOpenflowDiagStatusProvider implements OpenflowDiagStatusProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOpenflowDiagStatusProvider.class);
    private static final String OPENFLOW_SERVICE = "OPENFLOW";
    private static final String OPENFLOW_SERVER_6633 = "OPENFLOW_SERVER_6633";
    private static final String OPENFLOW_SERVER_6653 = "OPENFLOW_SERVER_6653";
    private static final String OPENFLOW_SERVICE_AGGREGATE = OPENFLOW_SERVICE;

    private final ConcurrentMap<String, ServiceState> statusMap = new ConcurrentHashMap<>(Map.of(
        OPENFLOW_SERVICE, STARTING,
        OPENFLOW_SERVER_6633, STARTING,
        OPENFLOW_SERVER_6653, STARTING));
    private final DiagStatusService diagStatusService;

    private ServiceRegistration reg;

    @Inject
    @Activate
    public DefaultOpenflowDiagStatusProvider(@Reference final DiagStatusService diagStatusService) {
        this.diagStatusService = diagStatusService;
        reg = diagStatusService.register(OPENFLOW_SERVICE_AGGREGATE);
        LOG.debug("OpenFlow diagnostic status provider activated");
    }

    @PreDestroy
    @Deactivate
    public void close() {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }
        LOG.debug("OpenFlow diagnostic status provider deactivated");
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
        if (statusMap.values().stream().allMatch(OPERATIONAL::equals)) {
            reportStatus(OPERATIONAL);
        }
    }
}