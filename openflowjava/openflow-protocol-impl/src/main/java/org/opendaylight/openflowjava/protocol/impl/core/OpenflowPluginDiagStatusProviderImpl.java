/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.aries.blueprint.annotation.service.Reference;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.openflowjava.protocol.api.connection.OpenflowPluginDiagStatusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Service(classes = OpenflowPluginDiagStatusProvider.class)
public class OpenflowPluginDiagStatusProviderImpl implements OpenflowPluginDiagStatusProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginDiagStatusProviderImpl.class);
    private static final String OPENFLOW_SERVICE = "OPENFLOW";
    private static final String OPENFLOW_SERVER_6633 = "OPENFLOW_SERVER_6633";
    private static final String OPENFLOW_SERVER_6653 = "OPENFLOW_SERVER_6653";
    private static final String OPENFLOW_SERVICE_AGGREGATE = "OPENFLOW_AGGREGATE";

    private final DiagStatusService diagStatusService;
    private volatile Map<String,Boolean> statusMap = new HashMap<>();

    @Inject
    public OpenflowPluginDiagStatusProviderImpl(final @Reference DiagStatusService diagStatusService) {
        this.diagStatusService = diagStatusService;
        diagStatusService.register(OPENFLOW_SERVICE_AGGREGATE);
        LOG.error("PRINTED");
    }

    public void reportStatus(ServiceState serviceState) {
        LOG.debug("reporting status as {} for {}", serviceState,OPENFLOW_SERVICE_AGGREGATE);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_AGGREGATE, serviceState));
    }

    public void reportStatus(ServiceState serviceState, Throwable throwable) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_AGGREGATE, throwable));
    }

    public void reportStatus(ServiceState serviceState, String description) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_AGGREGATE, serviceState, description));
    }

    public void reportStatus(String diagStatusIdentifier,ServiceState serviceState) {
        LOG.info("the statusmap has ");
        if ((diagStatusIdentifier.equals(OPENFLOW_SERVER_6653)) || (diagStatusIdentifier.equals(OPENFLOW_SERVER_6633))
                || diagStatusIdentifier.equals(OPENFLOW_SERVICE)) {
            statusMap.put(diagStatusIdentifier, true);
            LOG.info("the status map has {}",statusMap.size());
            LOG.info("reporting status as {} for {}",serviceState, diagStatusIdentifier);
            if (statusMap.size() == 4) {
                reportStatus(OPENFLOW_SERVICE_AGGREGATE);
            }
            else {
                reportStatus(ServiceState.ERROR,"The no of services operational are not 3");
                LOG.debug("reporting status as {} for {} as no of services are not 3",
                        ServiceState.ERROR, diagStatusIdentifier);
            }
        }
        else {
            reportStatus(ServiceState.ERROR,"the service is " + diagStatusIdentifier);
            LOG.info("reporting status as {} for {}",ServiceState.ERROR, diagStatusIdentifier);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public void reportStatus(String diagStatusIdentifier) {
        LOG.info("the statusmap definitely has ");
        try {
            if (statusMap.get(OPENFLOW_SERVER_6633) && statusMap.get(OPENFLOW_SERVER_6653)
                    && statusMap.get(OPENFLOW_SERVICE)) {
                reportStatus(ServiceState.OPERATIONAL);
            } else if ((!statusMap.containsKey(OPENFLOW_SERVER_6653)) || (!statusMap.get(OPENFLOW_SERVER_6653))) {
                reportStatus(ServiceState.ERROR, OPENFLOW_SERVER_6653 + " is not operational");
                LOG.debug("reporting status as {} for {}",ServiceState.ERROR, diagStatusIdentifier);
            } else if ((!statusMap.containsKey(OPENFLOW_SERVER_6633)) || (!statusMap.get(OPENFLOW_SERVER_6633))) {
                reportStatus(ServiceState.ERROR, OPENFLOW_SERVER_6633 + " is not operational");
                LOG.debug("reporting status as {} for {}",ServiceState.ERROR, diagStatusIdentifier);
            } else if ((!statusMap.containsKey(OPENFLOW_SERVICE)) || (!statusMap.get(OPENFLOW_SERVICE))) {
                reportStatus(ServiceState.ERROR, OPENFLOW_SERVICE + " is not operational");
                LOG.debug("reporting status as {} for {}",ServiceState.ERROR, diagStatusIdentifier);
            }
        } catch (Throwable throwable) {
            LOG.error("The error is {} ", throwable.toString());
            reportStatus(ServiceState.ERROR, throwable);
        }
    }

    public void reportStatus(String diagStatusIdentifier, Throwable throwable) {
        LOG.debug("the error for {} is {}",  diagStatusIdentifier, throwable.toString());
        diagStatusService.report(new ServiceDescriptor(diagStatusIdentifier, throwable));
    }

    public void reportStatus(String diagStatusIdentifier, ServiceState serviceState, String threadName) {
        LOG.debug("the reporting status {} for {} and {} thread is terminated", serviceState,
                diagStatusIdentifier, threadName);
        diagStatusService.report(new ServiceDescriptor(
                OPENFLOW_SERVICE_AGGREGATE, serviceState, threadName + " terminated"));
    }
}