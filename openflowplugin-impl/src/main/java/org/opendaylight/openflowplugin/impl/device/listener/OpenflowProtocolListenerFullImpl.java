/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.listener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.extensibility.AlienMessageListener;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.listener.OpenflowMessageListenerFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowProtocolListenerFullImpl implements AlienMessageListener, OpenflowMessageListenerFacade {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowProtocolListenerFullImpl.class);

    private final ConnectionAdapter connectionAdapter;
    private final DeviceReplyProcessor deviceReplyProcessor;

    /**
     * Constructor.
     *
     * @param connectionAdapter - connection adapter
     * @param deviceReplyProcessor - device replay processor
     */
    public OpenflowProtocolListenerFullImpl(final ConnectionAdapter connectionAdapter,
                                            final DeviceReplyProcessor deviceReplyProcessor) {
        this.connectionAdapter = connectionAdapter;
        this.deviceReplyProcessor = deviceReplyProcessor;
    }

    @Override
    public void onEchoRequest(final EchoRequestMessage echoRequestMessage) {
        final var xid = echoRequestMessage.getXid();
        LOG.debug("echo request received: {}", xid);
        Futures.addCallback(connectionAdapter.echoReply(
            new EchoReplyInputBuilder()
                .setVersion(echoRequestMessage.getVersion())
                .setXid(xid)
                .setData(echoRequestMessage.getData())
                .build()),
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
        deviceReplyProcessor.processReply(notification);
    }

    @Override
    public void onExperimenter(final ExperimenterMessage notification) {
        LOG.trace("Received experiementer message: {}", notification.getClass());
        deviceReplyProcessor.processExperimenterMessage(notification);
    }

    @Override
    public void onFlowRemoved(final FlowRemovedMessage notification) {
        deviceReplyProcessor.processFlowRemovedMessage(notification);
    }

    @Override
    public void onHello(final HelloMessage hello) {
        LOG.warn("hello message received outside handshake phase -> dropping connection {}",
                connectionAdapter.getRemoteAddress());
        connectionAdapter.disconnect();
    }

    @Override
    public void onMultipartReply(final MultipartReplyMessage notification) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Multipart Reply with XID: {}", notification.getXid());
        }
        // multiMsgCollector.addMultipartMsg(notification);
    }

    @Override
    public void onPacketIn(final PacketInMessage notification) {
        deviceReplyProcessor.processPacketInMessage(notification);
    }

    @Override
    public void onPortStatus(final PortStatusMessage notification) {
        deviceReplyProcessor.processPortStatusMessage(notification);
    }

    @Override
    public boolean onAlienMessage(final OfHeader message) {
        return deviceReplyProcessor.processAlienMessage(message);
    }
}
