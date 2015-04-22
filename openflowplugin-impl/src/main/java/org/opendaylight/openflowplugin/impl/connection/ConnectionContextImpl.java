/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ConnectionContextImpl implements ConnectionContext {

    private final ConnectionAdapter connectionAdapter;
    private CONNECTION_STATE connectionState;
    private FeaturesReply featuresReply;
    private NodeId nodeId;
    private DeviceDisconnectedHandler deviceDisconnectedHandler;
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionContextImpl.class);

    /**
     * @param connectionAdapter
     */
    public ConnectionContextImpl(final ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = connectionAdapter;
    }

    @Override
    public ConnectionAdapter getConnectionAdapter() {
        return connectionAdapter;
    }

    @Override
    public CONNECTION_STATE getConnectionState() {
        return connectionState;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public void setNodeId(final NodeId nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public void setConnectionState(final CONNECTION_STATE connectionState) {
        this.connectionState = connectionState;
    }

    @Override
    public FeaturesReply getFeatures() {
        return featuresReply;
    }

    @Override
    public void setDeviceDisconnectedHandler(final DeviceDisconnectedHandler deviceDisconnectedHandler) {
        this.deviceDisconnectedHandler = deviceDisconnectedHandler;
    }

    @Override
    public void propagateClosingConnection() {
        if (null != deviceDisconnectedHandler) {
            LOG.trace("Populating connection closed event.");
            this.deviceDisconnectedHandler.onDeviceDisconnected(this);
        }
    }

    @Override
    public void setFeatures(final FeaturesReply featuresReply) {
        this.featuresReply = featuresReply;
    }
}
