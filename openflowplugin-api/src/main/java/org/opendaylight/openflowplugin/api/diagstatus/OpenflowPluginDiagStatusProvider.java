/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.diagstatus;

import java.io.IOException;
import java.net.Socket;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowPluginDiagStatusProvider implements ServiceStatusProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginDiagStatusProvider.class);
    private static final String OPENFLOW_SERVICE_NAME = "OPENFLOW";
    private static final int OF_PORT_11 = 6633;
    private static final int OF_PORT_13 = 6653;

    private final DiagStatusService diagStatusService;
    private volatile ServiceDescriptor serviceDescriptor;

    public OpenflowPluginDiagStatusProvider(final DiagStatusService diagStatusService) {
        this.diagStatusService = diagStatusService;
        diagStatusService.register(OPENFLOW_SERVICE_NAME);
    }

    public void reportStatus(ServiceState serviceState, String description) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        if (serviceState == ServiceState.ERROR) {
            serviceDescriptor = new ServiceDescriptor(OPENFLOW_SERVICE_NAME, new IOException(description));
        } else {
            serviceDescriptor = new ServiceDescriptor(OPENFLOW_SERVICE_NAME, serviceState, description);
        }
        diagStatusService.report(serviceDescriptor);
    }

    @Override
    public ServiceDescriptor getServiceDescriptor() {

        if (serviceDescriptor.getServiceState().equals(ServiceState.OPERATIONAL)) {
            if (getApplicationNetworkState(OF_PORT_13) && getApplicationNetworkState(OF_PORT_11)) {
                return serviceDescriptor;
            } else {
                serviceDescriptor = new ServiceDescriptor(OPENFLOW_SERVICE_NAME,
                        new IOException("OF::PORTS:: 6653 and 6633 are not up yet"));
                return serviceDescriptor;
            }
        }
        return serviceDescriptor;
    }

    private boolean getApplicationNetworkState(int port) {
        Socket socket = null;
        try {
            socket = new Socket("localhost", port);
            LOG.debug("Socket connection established");
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                LOG.error("Failed to close socket : {}", socket, ex);
            }
        }
    }
}