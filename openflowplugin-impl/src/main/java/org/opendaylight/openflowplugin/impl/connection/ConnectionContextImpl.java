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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;

/**
 * 
 */
public class ConnectionContextImpl implements ConnectionContext {

    private ConnectionAdapter connectionAdapter;
    private CONNECTION_STATE connectionState;
    private FeaturesReply featuresReply;

    /**
     * @param connectionAdapter
     */
    public ConnectionContextImpl(ConnectionAdapter connectionAdapter) {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setConnectionState(CONNECTION_STATE connectionState) {
        this.connectionState = connectionState;
    }

    @Override
    public FeaturesReply getFeatures() {
        return featuresReply;
    }

    @Override
    public void setFeatures(FeaturesReply featuresReply) {
        this.featuresReply = featuresReply;
        
    }
}
