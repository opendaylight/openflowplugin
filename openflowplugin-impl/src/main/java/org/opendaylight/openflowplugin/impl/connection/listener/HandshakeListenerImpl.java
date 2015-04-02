/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection.listener;

import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;

import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
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
        connectionContext.setConnectionState(ConnectionContext.CONNECTION_STATE.WORKING);
        connectionContext.setFeatures(featureOutput);
        connectionContext.setNodeId(InventoryDataServiceUtil.nodeIdFromDatapathId(featureOutput.getDatapathId()));
        deviceConnectedHandler.deviceConnected(connectionContext);
    }

    @Override
    public void onHandshakeFailure() {
        LOG.info("handshake failed: {}", connectionContext.getConnectionAdapter().getRemoteAddress());
        connectionContext.setConnectionState(ConnectionContext.CONNECTION_STATE.RIP);
        // TODO ensure that connection is closed
    }
}
