/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProviderList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Service(classes = ServiceStatusProvider.class)
public class OpenflowPluginDiagStatusProvider implements ServiceStatusProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowPluginDiagStatusProvider.class);
    private static final String OPENFLOW_SERVICE_NAME = "OPENFLOW";
    private static final int OF_PORT_11 = 6633;
    private static final int OF_PORT_13 = 6653;

    private final DiagStatusService diagStatusService;
    private InetAddress defaultInetAddres;
    private InetAddress legacyInetAddress;

    @Inject
    public OpenflowPluginDiagStatusProvider(final @Reference DiagStatusService diagStatusService,
                                            final SwitchConnectionProviderList switchConnectionProviders) {
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
