/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection.listener;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SystemNotificationsListenerImpl implements SystemNotificationsListener {

    private static final Logger LOG = LoggerFactory.getLogger(SystemNotificationsListenerImpl.class);
    private static final long ECHO_XID = 0L;

    private final ConnectionContext connectionContext;
    @VisibleForTesting
    static final long MAX_ECHO_REPLY_TIMEOUT = 2000;
    private final long echoReplyTimeout;
    private final ThreadPoolExecutor threadPool;

    public SystemNotificationsListenerImpl(@Nonnull final ConnectionContext connectionContext,
                                           long echoReplyTimeout,
                                           @Nonnull final ThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
        this.connectionContext = Preconditions.checkNotNull(connectionContext);
        this.echoReplyTimeout = echoReplyTimeout;
    }

    @Override
    public void onDisconnectEvent(final DisconnectEvent notification) {
        LOG.info("ConnectionEvent: Connection closed by device, Device:{}, NodeId:{}",
                connectionContext.getConnectionAdapter().getRemoteAddress(), connectionContext.getSafeNodeIdForLOG());
        connectionContext.onConnectionClosed();
    }

    @Override
    public void onSwitchIdleEvent(final SwitchIdleEvent notification) {
        threadPool.execute(this::executeOnSwitchIdleEvent);
    }

    private void executeOnSwitchIdleEvent() {
        boolean shouldBeDisconnected = true;

        final InetSocketAddress remoteAddress = connectionContext.getConnectionAdapter().getRemoteAddress();

        if (ConnectionContext.CONNECTION_STATE.WORKING.equals(connectionContext.getConnectionState())) {
            FeaturesReply features = connectionContext.getFeatures();
            LOG.info("Switch Idle state occurred, node={}|auxId={}", remoteAddress, features.getAuxiliaryId());
            connectionContext.changeStateToTimeouting();
            EchoInputBuilder builder = new EchoInputBuilder();
            builder.setVersion(features.getVersion());
            Xid xid = new Xid(ECHO_XID);
            builder.setXid(xid.getValue());

            Future<RpcResult<EchoOutput>> echoReplyFuture = connectionContext.getConnectionAdapter().echo(builder.build());

            try {
                RpcResult<EchoOutput> echoReplyValue = echoReplyFuture.get(echoReplyTimeout, TimeUnit.MILLISECONDS);
                if (echoReplyValue.isSuccessful() && echoReplyValue.getResult().getXid() == ECHO_XID) {
                    connectionContext.changeStateToWorking();
                    shouldBeDisconnected = false;
                } else {
                    logErrors(remoteAddress, echoReplyValue);
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Exception while  waiting for echoReply from [{}] in TIMEOUTING state: {}", remoteAddress, e.getMessage());
                }

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Exception while  waiting for echoReply from [{}] in TIMEOUTING state: {}", remoteAddress, e);
                }

            }
        }
        if (shouldBeDisconnected) {
            if (LOG.isInfoEnabled()) {
                LOG.info("ConnectionEvent:Closing connection as device is idle. Echo sent at {}. Device:{}, NodeId:{}",
                        new Date(System.currentTimeMillis() - echoReplyTimeout), remoteAddress, connectionContext.getSafeNodeIdForLOG());
            }

            connectionContext.closeConnection(true);
        }
    }

    private void logErrors(InetSocketAddress remoteAddress, RpcResult<EchoOutput> echoReplyValue) {
        for (RpcError replyError : echoReplyValue.getErrors()) {
            Throwable cause = replyError.getCause();
            if (LOG.isWarnEnabled()) {
                LOG.warn("Received EchoReply from [{}] in TIMEOUTING state, Error:{}", remoteAddress, cause.getMessage());
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("Received EchoReply from [{}] in TIMEOUTING state, Error:{}", remoteAddress, cause);
            }

        }
    }
}
