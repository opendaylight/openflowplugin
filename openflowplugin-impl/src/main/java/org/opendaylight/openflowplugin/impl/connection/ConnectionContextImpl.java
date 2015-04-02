/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collection;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.MultiMsgCollector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;

/**
 *
 */
public class ConnectionContextImpl implements ConnectionContext {

    private final ConnectionAdapter connectionAdapter;
    private CONNECTION_STATE connectionState;
    private FeaturesReply featuresReply;
    private final MultiMsgCollector multipartCollector;
    private NodeId nodeId;

    /**
     * @param connectionAdapter
     */
    public ConnectionContextImpl(final ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = connectionAdapter;
        multipartCollector = new MultiMsgCollectorImpl();
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
    public void setNodeId(NodeId nodeId) {
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
    public void setFeatures(final FeaturesReply featuresReply) {
        this.featuresReply = featuresReply;

    }

    @Override
    public ListenableFuture<Collection<MultipartReply>> registerMultipartMsg(final long xid) {
        return multipartCollector.registerMultipartMsg(xid);
    }

    @Override
    public void addMultipartMsg(final MultipartReply reply) {
        multipartCollector.addMultipartMsg(reply);
    }

    @Override
    public void registerMultipartFutureMsg(final long xid, final SettableFuture<Collection<MultipartReply>> future) {
        multipartCollector.registerMultipartFutureMsg(xid, future);
    }
}
