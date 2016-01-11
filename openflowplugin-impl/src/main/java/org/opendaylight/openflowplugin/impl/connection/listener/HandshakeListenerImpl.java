/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.listener;

import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HandshakeListenerImpl implements HandshakeListener {

    private static final Logger LOG = LoggerFactory.getLogger(HandshakeListenerImpl.class);

    private ConnectionContext connectionContext;
    private DeviceConnectedHandler deviceConnectedHandler;
    private HandshakeContext handshakeContext;

    /**
     * @param connectionContext
     * @param deviceConnectedHandler
     */
    public HandshakeListenerImpl(ConnectionContext connectionContext, DeviceConnectedHandler deviceConnectedHandler) {
        this.connectionContext = connectionContext;
        this.deviceConnectedHandler = deviceConnectedHandler;
    }

    @Override
    public void onHandshakeSuccessfull(GetFeaturesOutput featureOutput, Short version) {
        LOG.debug("handshake succeeded: {}", connectionContext.getConnectionAdapter().getRemoteAddress());
        closeHandshakeContext();
        connectionContext.changeStateToWorking();
        connectionContext.setFeatures(featureOutput);
        connectionContext.setNodeId(InventoryDataServiceUtil.nodeIdFromDatapathId(featureOutput.getDatapathId()));
        deviceConnectedHandler.initializeDeviceContext(connectionContext);
        SessionStatistics.countEvent(connectionContext.getNodeId().toString(), SessionStatistics.ConnectionStatus.CONNECTION_CREATED);
    }

    @Override
    public void onHandshakeFailure() {
        LOG.debug("handshake failed: {}", connectionContext.getConnectionAdapter().getRemoteAddress());
        closeHandshakeContext();
        connectionContext.closeConnection(false);
    }

    private void closeHandshakeContext() {
        try {
            handshakeContext.close();
        } catch (Exception e) {
            LOG.warn("Closing handshake context failed: {}", e.getMessage());
            LOG.debug("Detail in hanshake context close:", e);
        }
    }

    @Override
    public void setHandshakeContext(HandshakeContext handshakeContext) {
        this.handshakeContext = handshakeContext;
    }
}
