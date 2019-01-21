/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.aries.blueprint.annotation.service.Reference;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.openflowjava.protocol.spi.connection.OpenflowPluginDiagStatusProviders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Service(classes = OpenflowPluginDiagStatusProviders.class)
public class OpenflowPluginDiagStatusProviderImpl implements OpenflowPluginDiagStatusProviders {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginDiagStatusProviderImpl.class);
    private static final String OPENFLOW_SERVICE_NAME = "OPENFLOW";
    private static final String OPENFLOW_SERVER_6633 = "OPENFLOW_SERVER_6633";
    private static final String OPENFLOW_SERVER_6653 = "OPENFLOW_SERVER_6653";

    private final DiagStatusService diagStatusService;

    @Inject
    public OpenflowPluginDiagStatusProviderImpl(final @Reference DiagStatusService diagStatusService) {
        this.diagStatusService = diagStatusService;
        LOG.error("PRINTED");
    }

    public void reportStatus(ServiceState serviceState) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_NAME, serviceState));
    }

    public void reportStatus(ServiceState serviceState, Throwable throwable) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_NAME, throwable));
    }

    public void reportStatus(String diagStatusIdentifier) {
        if (diagStatusIdentifier.equals(OPENFLOW_SERVER_6653)) {
            LOG.debug("reporting status as {} for {}",ServiceState.OPERATIONAL, diagStatusIdentifier);
            //diagStatusService.report(new ServiceDescriptor(diagStatusIdentifier, ServiceState.OPERATIONAL));
        }
        else if (diagStatusIdentifier.equals(OPENFLOW_SERVER_6633)) {
            LOG.debug("reporting status as {} for {}",ServiceState.OPERATIONAL, diagStatusIdentifier);
            //diagStatusService.report(new ServiceDescriptor(diagStatusIdentifier, ServiceState.OPERATIONAL));
        }
        else {
            LOG.error("The diagStatusIdentifier is {} ",diagStatusIdentifier);
        }
    }

    @SuppressFBWarnings("BX_UNBOXING_IMMEDIATELY_REBOXED")
    public void reportStatus(String diagStatusIdentifier, List<Boolean> result) {
        Boolean openflow6633 = result.get(0).booleanValue();
        Boolean openflow6653 = result.get(1).booleanValue();
        if ((result.size() == 2) && (openflow6633 == true)  && (openflow6653 == true)
                && diagStatusIdentifier.equals(OPENFLOW_SERVICE_NAME)) {
            diagStatusService.register(OPENFLOW_SERVICE_NAME);
            reportStatus(ServiceState.OPERATIONAL);
        }
        else if (openflow6633 == false) {
            reportStatus(ServiceState.ERROR,OPENFLOW_SERVER_6633 + " is not operational");
        }
        else if (openflow6653 == false) {
            reportStatus(ServiceState.ERROR,OPENFLOW_SERVER_6653 + " is not operational");
        }
        else if (result.size() != 2) {
            reportStatus(ServiceState.ERROR,"The result size is not equal to 2");
        }
    }

    public void reportStatus(String diagStatusIdentifier, Throwable throwable) {
        LOG.debug("reporting status as {} for {}",  diagStatusIdentifier, throwable.toString());
        diagStatusService.report(new ServiceDescriptor(diagStatusIdentifier, throwable));
    }

    public void reportStatus(String diagStatusIdentifier, String threadName) {
        LOG.debug("reporting status as {} for {} and {} thread is terminated", ServiceState.ERROR,
                diagStatusIdentifier, threadName);
        //diagStatusService.register(diagStatusIdentifier);
        diagStatusService.report(new ServiceDescriptor(
                OPENFLOW_SERVICE_NAME, ServiceState.ERROR, threadName + " terminated"));
    }

    public void reportStatus(ServiceState serviceState, String description) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_NAME, serviceState, description));
    }
}
