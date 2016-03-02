/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HandshakeListenerImpl implements HandshakeListener {

    private static final Logger LOG = LoggerFactory.getLogger(HandshakeListenerImpl.class);

    private final ConnectionContext connectionContext;
    private final DeviceConnectedHandler deviceConnectedHandler;
    private HandshakeContext handshakeContext;

    /**
     * @param connectionContext
     * @param deviceConnectedHandler
     */
    public HandshakeListenerImpl(final ConnectionContext connectionContext, final DeviceConnectedHandler deviceConnectedHandler) {
        this.connectionContext = connectionContext;
        this.deviceConnectedHandler = deviceConnectedHandler;
    }

    @Override
    public void onHandshakeSuccessful(final GetFeaturesOutput featureOutput, final Short version) {
        LOG.debug("handshake succeeded: {}", connectionContext.getConnectionAdapter().getRemoteAddress());
        closeHandshakeContext();
        connectionContext.changeStateToWorking();
        connectionContext.setFeatures(featureOutput);
        connectionContext.setNodeId(InventoryDataServiceUtil.nodeIdFromDatapathId(featureOutput.getDatapathId()));

        // fire barrier in order to sweep all handshake and posthandshake messages before continue
        final ListenableFuture<RpcResult<BarrierOutput>> barrier = fireBarrier(version, 0L);
        Futures.addCallback(barrier, new FutureCallback<RpcResult<BarrierOutput>>() {
            @Override
            public void onSuccess(@Nullable final RpcResult<BarrierOutput> result) {
                LOG.debug("succeeded by getting sweep barrier after posthandshake for device {}", connectionContext.getNodeId());
                try {
                    deviceConnectedHandler.deviceConnected(connectionContext);
                    SessionStatistics.countEvent(connectionContext.getNodeId().toString(),
                            SessionStatistics.ConnectionStatus.CONNECTION_CREATED);
                } catch (final Exception e) {
                    LOG.info("ConnectionContext initial processing failed: {}", e.getMessage());
                    SessionStatistics.countEvent(connectionContext.getNodeId().toString(),
                            SessionStatistics.ConnectionStatus.CONNECTION_DISCONNECTED_BY_OFP);
                    connectionContext.closeConnection(false);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.info("failed to get sweep barrier after posthandshake for device {}", connectionContext.getNodeId());
                connectionContext.closeConnection(false);
            }
        });
    }

    protected ListenableFuture<RpcResult<BarrierOutput>> fireBarrier(final Short version, final long xid) {
        final BarrierInput barrierInput = new BarrierInputBuilder()
                .setXid(xid)
                .setVersion(version)
                .build();
        return JdkFutureAdapters.listenInPoolThread(
                connectionContext.getConnectionAdapter().barrier(barrierInput));
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
        } catch (final Exception e) {
            LOG.warn("Closing handshake context failed: {}", e.getMessage());
            LOG.debug("Detail in handshake context close:", e);
        }
    }

    @Override
    public void setHandshakeContext(final HandshakeContext handshakeContext) {
        this.handshakeContext = handshakeContext;
    }
}
