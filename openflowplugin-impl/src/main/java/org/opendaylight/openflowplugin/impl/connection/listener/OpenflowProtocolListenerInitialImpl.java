/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter.MessageListener;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.impl.connection.HandshakeStepWrapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowProtocolListenerInitialImpl implements MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowProtocolListenerInitialImpl.class);

    private final ConnectionContext connectionContext;
    private final HandshakeContext handshakeContext;

    /**
     * Constructor.
     *
     * @param connectionContext - connection context
     * @param handshakeContext - handshake context
     */
    public OpenflowProtocolListenerInitialImpl(final ConnectionContext connectionContext,
            final HandshakeContext handshakeContext) {
        this.connectionContext = connectionContext;
        this.handshakeContext = handshakeContext;
    }

    @Override
    public void onEchoRequest(final EchoRequestMessage echoRequestMessage) {
        final var xid = echoRequestMessage.getXid();
        LOG.debug("echo request received: {}", xid);
        Futures.addCallback(connectionContext.getConnectionAdapter().echoReply(
            new EchoReplyInputBuilder().setXid(xid).setData(echoRequestMessage.getData()).build()),
            new FutureCallback<>() {
                @Override
                public void onSuccess(final RpcResult<EchoReplyOutput> result) {
                    LOG.debug("echo reply sent: {}", xid);
                }

                @Override
                public void onFailure(final Throwable cause) {
                    LOG.debug("echo reply failed: {}", xid, cause);
                }
            }, MoreExecutors.directExecutor());
    }

    @Override
    public void onError(final ErrorMessage notification) {
        LOG.debug("NOOP: Error message received during handshake phase: {}", notification);
    }

    @Override
    public void onExperimenter(final ExperimenterMessage notification) {
        LOG.debug("NOOP: Experimenter message during handshake phase not supported: {}", notification);
    }

    @Override
    public void onFlowRemoved(final FlowRemovedMessage notification) {
        LOG.debug("NOOP: Flow-removed message during handshake phase not supported: {}", notification);
    }

    @Override
    public void onHello(final HelloMessage hello) {
        LOG.debug("processing HELLO.xid: {} from device {}", hello.getXid(),
                connectionContext.getConnectionAdapter().getRemoteAddress());
        final ConnectionContext.CONNECTION_STATE connectionState = connectionContext.getConnectionState();
        if (connectionState == null
                || ConnectionContext.CONNECTION_STATE.HANDSHAKING.equals(connectionState)) {
            synchronized (connectionContext) {
                if (connectionContext.getConnectionState() == null) {
                    // got here before connection ready notification
                    connectionContext.changeStateToHandshaking();
                }

                if (checkState(ConnectionContext.CONNECTION_STATE.HANDSHAKING)) {
                    final var handshakeStepWrapper = new HandshakeStepWrapper(hello,
                        handshakeContext.getHandshakeManager(), connectionContext.getConnectionAdapter());
                    // use up netty thread
                    handshakeStepWrapper.run();
                } else {
                    LOG.debug("already out of handshake phase but still received hello message from device {}",
                            connectionContext.getConnectionAdapter().getRemoteAddress());
                }
            }
        } else {
            //TODO: consider disconnecting of bad behaving device
            LOG.warn("Hello message received outside handshake phase:{} ", hello);
            LOG.debug("already touched by onConnectionReady event from device {} (or finished handshake)",
                    connectionContext.getConnectionAdapter().getRemoteAddress());
        }
    }

    @Override
    public void onMultipartReply(final MultipartReplyMessage notification) {
        LOG.debug("NOOP: Multipart-reply message during handshake phase not supported: {}", notification);
    }

    @Override
    public void onPacketIn(final PacketInMessage notification) {
        LOG.debug("NOOP: Packet-in message during handshake phase not supported: {}", notification);
    }

    @Override
    public void onPortStatus(final PortStatusMessage notification) {
        connectionContext.handlePortStatusMessage(notification);
    }

    /**
     * Check state of the connection context.
     *
     * @param expectedState - the expected state
     */
    protected boolean checkState(final ConnectionContext.CONNECTION_STATE expectedState) {
        boolean verdict = true;
        if (!Objects.equals(connectionContext.getConnectionState(), expectedState)) {
            verdict = false;
            LOG.info("Expected state: {}, actual state: {}", expectedState,
                    connectionContext.getConnectionState());
        }

        return verdict;
    }
}
