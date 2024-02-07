/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection.listener;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter.SystemListener;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.ssl.connection.error.service.rev190723.SslErrorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.ssl.connection.error.service.rev190723.SslErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.ssl.connection.error.service.rev190723.ssl.error.SwitchCertificateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SslConnectionError;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemNotificationsListenerImpl implements SystemListener {
    private static final Logger LOG = LoggerFactory.getLogger(SystemNotificationsListenerImpl.class);
    private static final Logger OF_EVENT_LOG = LoggerFactory.getLogger("OfEventLog");
    private static final Xid ECHO_XID = new Xid(Uint32.ZERO);

    private final ConnectionContext connectionContext;
    @VisibleForTesting
    static final long MAX_ECHO_REPLY_TIMEOUT = 2000;
    private final long echoReplyTimeout;
    private final Executor executor;
    private final NotificationPublishService notificationPublishService;

    public SystemNotificationsListenerImpl(@NonNull final ConnectionContext connectionContext,
            final long echoReplyTimeout, final @NonNull Executor executor,
            @NonNull final NotificationPublishService notificationPublishService) {
        this.connectionContext = requireNonNull(connectionContext);
        this.echoReplyTimeout = echoReplyTimeout;
        this.executor = requireNonNull(executor);
        this.notificationPublishService = requireNonNull(notificationPublishService);
    }

    @Override
    public void onSslConnectionError(final SslConnectionError sslConnectionError) {
        final var switchCert = sslConnectionError.getSwitchCertificate();
        notificationPublishService.offerNotification(new SslErrorBuilder()
            .setType(SslErrorType.SslConFailed)
            .setCode(Uint16.valueOf(SslErrorType.SslConFailed.getIntValue()))
            .setNodeIpAddress(remoteAddress())
            .setData(sslConnectionError.getInfo())
            .setSwitchCertificate(switchCert == null ? null : new SwitchCertificateBuilder(switchCert).build())
            .build());
    }

    @Override
    public void onSwitchIdle(final SwitchIdleEvent switchIdle) {
        executor.execute(this::executeOnSwitchIdleEvent);
    }

    @Override
    public void onDisconnect(final DisconnectEvent notification) {
        OF_EVENT_LOG.debug("Disconnect, Node: {}", connectionContext.getSafeNodeIdForLOG());
        LOG.info("ConnectionEvent: Connection closed by device, Device:{}, NodeId:{}",
                connectionContext.getConnectionAdapter().getRemoteAddress(), connectionContext.getSafeNodeIdForLOG());
        connectionContext.onConnectionClosed();
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void executeOnSwitchIdleEvent() {
        boolean shouldBeDisconnected = true;

        final InetSocketAddress remoteAddress = connectionContext.getConnectionAdapter().getRemoteAddress();

        if (ConnectionContext.CONNECTION_STATE.WORKING.equals(connectionContext.getConnectionState())) {
            FeaturesReply features = connectionContext.getFeatures();
            LOG.debug("Switch Idle state occurred, node={}|auxId={}", remoteAddress, features.getAuxiliaryId());
            OF_EVENT_LOG.debug("Switch idle state, Node: {}", features.getDatapathId());
            connectionContext.changeStateToTimeouting();
            EchoInputBuilder builder = new EchoInputBuilder();
            builder.setVersion(features.getVersion());
            builder.setXid(ECHO_XID.getValue());

            Future<RpcResult<EchoOutput>> echoReplyFuture =
                    connectionContext.getConnectionAdapter().echo(builder.build());

            try {
                RpcResult<EchoOutput> echoReplyValue = echoReplyFuture.get(echoReplyTimeout, TimeUnit.MILLISECONDS);
                if (echoReplyValue.isSuccessful()
                        && Objects.equals(echoReplyValue.getResult().getXid(), ECHO_XID.getValue())) {
                    connectionContext.changeStateToWorking();
                    shouldBeDisconnected = false;
                } else {
                    logErrors(remoteAddress, echoReplyValue);
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Exception while  waiting for echoReply from [{}] in TIMEOUTING state: {}",
                            remoteAddress, e.getMessage());
                }

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Exception while  waiting for echoReply from [{}] in TIMEOUTING state",
                            remoteAddress, e);
                }

            }
        }
        if (shouldBeDisconnected) {
            if (LOG.isInfoEnabled()) {
                LOG.info("ConnectionEvent:Closing connection as device is idle. Echo sent at {}. Device:{}, NodeId:{}",
                        new Date(System.currentTimeMillis() - echoReplyTimeout),
                        remoteAddress, connectionContext.getSafeNodeIdForLOG());
            }

            connectionContext.closeConnection(true);
        }
    }

    private static void logErrors(final InetSocketAddress remoteAddress, final RpcResult<EchoOutput> echoReplyValue) {
        for (RpcError replyError : echoReplyValue.getErrors()) {
            Throwable cause = replyError.getCause();
            if (LOG.isWarnEnabled()) {
                LOG.warn("Received EchoReply from [{}] in TIMEOUTING state, Error:{}",
                        remoteAddress, cause.getMessage());
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("Received EchoReply from [{}] in TIMEOUTING state", remoteAddress, cause);
            }
        }
    }

    private @Nullable IpAddress remoteAddress() {
        final var connectionAdapter = connectionContext.getConnectionAdapter();
        if (connectionAdapter != null) {
            final var remoteAddress = connectionAdapter.getRemoteAddress();
            if (remoteAddress != null) {
                final var inetAddress = remoteAddress.getAddress();
                if (inetAddress != null) {
                    return IetfInetUtil.ipAddressFor(inetAddress.getHostAddress());
                }
            }
        }
        return null;
    }
}
