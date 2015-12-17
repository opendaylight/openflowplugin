/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection.listener;

import com.google.common.base.Objects;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.openflow.md.core.HandshakeStepWrapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OpenflowProtocolListenerInitialImpl implements OpenflowProtocolListener {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowProtocolListenerInitialImpl.class);

    private final ConnectionContext connectionContext;
    private final HandshakeContext handshakeContext;

    /**
     * @param connectionContext
     * @param handshakeContext
     */
    public OpenflowProtocolListenerInitialImpl(final ConnectionContext connectionContext,
                                               final HandshakeContext handshakeContext) {
        this.connectionContext = connectionContext;
        this.handshakeContext = handshakeContext;
    }

    @Override
    public void onEchoRequestMessage(final EchoRequestMessage echoRequestMessage) {
        LOG.debug("echo request received: {}", echoRequestMessage.getXid());
        EchoReplyInputBuilder builder = new EchoReplyInputBuilder();
        builder.setVersion(echoRequestMessage.getVersion());
        builder.setXid(echoRequestMessage.getXid());
        builder.setData(echoRequestMessage.getData());

        connectionContext.getConnectionAdapter().echoReply(builder.build());
    }

    @Override
    public void onErrorMessage(final ErrorMessage notification) {
        LOG.warn("NOOP: Error message received during handshake phase: {}", notification);
    }

    @Override
    public void onExperimenterMessage(final ExperimenterMessage notification) {
        LOG.info("NOOP: Experimenter message during handshake phase not supported: {}", notification);
    }

    @Override
    public void onFlowRemovedMessage(final FlowRemovedMessage notification) {
        LOG.info("NOOP: Flow-removed message during handshake phase not supported: {}", notification);
    }

    @Override
    public void onHelloMessage(final HelloMessage hello) {
        LOG.debug("processing HELLO.xid: {} from device {}", hello.getXid(), connectionContext.getConnectionAdapter().getRemoteAddress());
        final ConnectionContext.CONNECTION_STATE connectionState = connectionContext.getConnectionState();
        if (connectionState == null
                || ConnectionContext.CONNECTION_STATE.HANDSHAKING.equals(connectionState)) {
            synchronized (connectionContext) {
                if (connectionContext.getConnectionState() == null) {
                    // got here before connection ready notification
                    connectionContext.changeStateToHandshaking();
                }

                if (checkState(ConnectionContext.CONNECTION_STATE.HANDSHAKING)) {
                    final HandshakeStepWrapper handshakeStepWrapper = new HandshakeStepWrapper(
                            hello, handshakeContext.getHandshakeManager(), connectionContext.getConnectionAdapter());
                    // use up netty thread
                    handshakeStepWrapper.run();
                } else {
                    LOG.debug("already out of handshake phase but still received hello message from device {}", connectionContext.getConnectionAdapter().getRemoteAddress());
                }
            }
        } else {
            LOG.debug("already touched by onConnectionReady event from device {} (or finished handshake)", connectionContext.getConnectionAdapter().getRemoteAddress());
        }
    }

    @Override
    public void onMultipartReplyMessage(final MultipartReplyMessage notification) {
        LOG.info("NOOP: Multipart-reply message during handshake phase not supported: {}", notification);
    }

    @Override
    public void onPacketInMessage(final PacketInMessage notification) {
        LOG.info("NOOP: Packet-in message during handshake phase not supported: {}", notification);
    }

    @Override
    public void onPortStatusMessage(final PortStatusMessage notification) {
        LOG.info("NOOP: Port-status message during handshake phase not supported: {}", notification);
    }

    /**
     * @param expectedState
     */
    protected boolean checkState(final ConnectionContext.CONNECTION_STATE expectedState) {
        boolean verdict = true;
        if (! Objects.equal(connectionContext.getConnectionState(), expectedState)) {
            verdict = false;
            LOG.info("Expected state: {}, actual state: {}", expectedState,
                    connectionContext.getConnectionState());
        }

        return verdict;
    }
}
