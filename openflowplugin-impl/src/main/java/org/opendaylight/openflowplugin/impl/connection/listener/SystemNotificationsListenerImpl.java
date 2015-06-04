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
import java.util.concurrent.Future;
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

    private final ConnectionContext connectionContext;
    private static final Logger LOG = LoggerFactory.getLogger(SystemNotificationsListenerImpl.class);
    @VisibleForTesting
    static final long MAX_ECHO_REPLY_TIMEOUT = 2000;

    public SystemNotificationsListenerImpl(@Nonnull final ConnectionContext connectionContext) {
        this.connectionContext = Preconditions.checkNotNull(connectionContext);
    }

    @Override
    public void onDisconnectEvent(final DisconnectEvent notification) {
        connectionContext.onConnectionClosed();
    }

    @Override
    public void onSwitchIdleEvent(final SwitchIdleEvent notification) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean shouldBeDisconnected = true;

                final InetSocketAddress remoteAddress = connectionContext.getConnectionAdapter().getRemoteAddress();

                if (ConnectionContext.CONNECTION_STATE.WORKING.equals(connectionContext.getConnectionState())) {
                    FeaturesReply features = connectionContext.getFeatures();
                    LOG.debug(
                            "first idle state occured, node={}|auxId={}",
                            remoteAddress, features.getAuxiliaryId());
                    connectionContext.changeStateToTimeouting();
                    EchoInputBuilder builder = new EchoInputBuilder();
                    builder.setVersion(features.getVersion());
                    Xid xid = new Xid(0L);
                    builder.setXid(xid.getValue());

                    Future<RpcResult<EchoOutput>> echoReplyFuture = connectionContext.getConnectionAdapter()
                            .echo(builder.build());

                    try {
                        RpcResult<EchoOutput> echoReplyValue = echoReplyFuture.get(MAX_ECHO_REPLY_TIMEOUT, TimeUnit.MILLISECONDS);
                        if (echoReplyValue.isSuccessful()) {
                            connectionContext.changeStateToWorking();
                            shouldBeDisconnected = false;
                        } else {
                            for (RpcError replyError : echoReplyValue
                                    .getErrors()) {
                                Throwable cause = replyError.getCause();
                                LOG.warn("while receiving echoReply [{}] in TIMEOUTING state {} ",
                                        remoteAddress,
                                        cause.getMessage());
                                LOG.trace("while receiving echoReply [{}] in TIMEOUTING state ..", remoteAddress, cause);
                            }
                        }
                    } catch (Exception e) {
                        LOG.warn("while waiting for echoReply in TIMEOUTING state: {}", e.getMessage());
                        LOG.trace("while waiting for echoReply in TIMEOUTING state ..", remoteAddress, e);
                    }
                }
                if (shouldBeDisconnected) {
                    connectionContext.closeConnection(true);
                }
            }
        }).start();
    }
}
