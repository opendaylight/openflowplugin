/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.diagstatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowPluginDiagStatusProvider implements ServiceStatusProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginDiagStatusProvider.class);
    private static final String OPENFLOW_SERVICE_NAME = "OPENFLOW";
    private static final int OF_PORT_11 = 6633;
    private static final int OF_PORT_13 = 6653;

    private final DiagStatusService diagStatusService;
    private InetAddress defaultInetAddres;
    private InetAddress legacyInetAddress;

    public OpenflowPluginDiagStatusProvider(final DiagStatusService diagStatusService,
                                            final List<SwitchConnectionProvider> switchConnectionProviders) {
        this.diagStatusService = diagStatusService;
        setSwitchConnectionInetAddress(switchConnectionProviders);
        diagStatusService.register(OPENFLOW_SERVICE_NAME);
    }

    private void setSwitchConnectionInetAddress(final List<SwitchConnectionProvider> switchConnectionProviders) {
        switchConnectionProviders.forEach(switchConnectionProvider -> {
            if (switchConnectionProvider.getConfiguration().getPort() == OF_PORT_11) {
                legacyInetAddress = switchConnectionProvider.getConfiguration().getAddress();
            } else if (switchConnectionProvider.getConfiguration().getPort() == OF_PORT_13) {
                defaultInetAddres = switchConnectionProvider.getConfiguration().getAddress();
            }
        });
    }

    public void reportStatus(ServiceState serviceState, Throwable throwable) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        serviceDescriptor = new ServiceDescriptor(OPENFLOW_SERVICE_NAME, throwable);
        diagStatusService.report(serviceDescriptor);
    }

    public void reportStatus(ServiceState serviceState, String description) {
        LOG.debug("reporting status as {} for {}", serviceState, OPENFLOW_SERVICE_NAME);
        diagStatusService.report(new ServiceDescriptor(OPENFLOW_SERVICE_NAME, serviceState, description));
    }

    @Override
    public ServiceDescriptor getServiceDescriptor() {
        if (getApplicationNetworkState(OF_PORT_13, defaultInetAddres)
                && getApplicationNetworkState(OF_PORT_11, legacyInetAddress)) {
            return new ServiceDescriptor(OPENFLOW_SERVICE_NAME, ServiceState.OPERATIONAL,
                    "OF::PORTS:: 6653 and 6633 are up.");
        } else {
            return new ServiceDescriptor(OPENFLOW_SERVICE_NAME, ServiceState.ERROR,
                    "OF::PORTS:: 6653 and 6633 are not up yet");
        }
    }

    private boolean getApplicationNetworkState(int port, InetAddress inetAddress) {
        Socket socket = null;
        try {
            if (inetAddress == null) {
                socket = new Socket("localhost", port);
            } else {
                socket = new Socket(inetAddress, port);
            }
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