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
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionStatus;
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("handshake succeeded: {}", connectionContext.getConnectionAdapter().getRemoteAddress());
        }
        this.handshakeContext.close();
        connectionContext.changeStateToWorking();
        connectionContext.setFeatures(featureOutput);
        connectionContext.setNodeId(InventoryDataServiceUtil.nodeIdFromDatapathId(featureOutput.getDatapathId()));
        connectionContext.handshakeSuccessful();

        // fire barrier in order to sweep all handshake and posthandshake messages before continue
        final ListenableFuture<RpcResult<BarrierOutput>> barrier = fireBarrier(version, 0L);
        Futures.addCallback(barrier, addBarrierCallback());
    }

    private FutureCallback<RpcResult<BarrierOutput>> addBarrierCallback() {
        return new FutureCallback<RpcResult<BarrierOutput>>() {
            @Override
            public void onSuccess(@Nullable final RpcResult<BarrierOutput> result) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("succeeded by getting sweep barrier after post-handshake for device {}", connectionContext.getDeviceInfo());
                }
                try {
                    ConnectionStatus connectionStatusResult = deviceConnectedHandler.deviceConnected(connectionContext);
                    if (connectionStatusResult != ConnectionStatus.MAY_CONTINUE) {
                        connectionContext.closeConnection(false);
                    }
                    SessionStatistics.countEvent(connectionContext.getDeviceInfo().toString(),
                            SessionStatistics.ConnectionStatus.CONNECTION_CREATED);
                } catch (final Exception e) {
                    LOG.warn("initial processing failed for device {}", connectionContext.getDeviceInfo(), e);
                    SessionStatistics.countEvent(connectionContext.getDeviceInfo().toString(),
                            SessionStatistics.ConnectionStatus.CONNECTION_DISCONNECTED_BY_OFP);
                    connectionContext.closeConnection(true);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("failed to get sweep barrier after post-handshake for device {}", connectionContext.getDeviceInfo(), t);
                connectionContext.closeConnection(false);
            }
        };
    }

    private ListenableFuture<RpcResult<BarrierOutput>> fireBarrier(final Short version, final long xid) {
        final BarrierInput barrierInput = new BarrierInputBuilder()
                .setXid(xid)
                .setVersion(version)
                .build();
        return JdkFutureAdapters.listenInPoolThread(
                this.connectionContext.getConnectionAdapter().barrier(barrierInput));
    }

    @Override
    public void onHandshakeFailure() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("handshake failed: {}", this.connectionContext.getConnectionAdapter().getRemoteAddress());
        }
        this.handshakeContext.close();
        this.connectionContext.closeConnection(false);
    }

    @Override
    public void setHandshakeContext(final HandshakeContext handshakeContext) {
        this.handshakeContext = handshakeContext;
    }
}
