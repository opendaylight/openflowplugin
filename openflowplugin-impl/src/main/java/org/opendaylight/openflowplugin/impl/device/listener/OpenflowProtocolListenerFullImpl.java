/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.listener;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.listener.OpenflowMessageListenerFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OpenflowProtocolListenerFullImpl implements OpenflowMessageListenerFacade {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowProtocolListenerFullImpl.class);

    private final ConnectionAdapter connectionAdapter;
    private final DeviceReplyProcessor deviceReplyProcessor;

    /**
     * @param connectionAdapter
     * @param deviceReplyProcessor
     */
    public OpenflowProtocolListenerFullImpl(final ConnectionAdapter connectionAdapter, final DeviceReplyProcessor deviceReplyProcessor) {
        this.connectionAdapter = connectionAdapter;
        this.deviceReplyProcessor = deviceReplyProcessor;
    }

    @Override
    public void onEchoRequestMessage(final EchoRequestMessage echoRequestMessage) {
        LOG.debug("echo request received: {}", echoRequestMessage.getXid());
        final EchoReplyInputBuilder builder = new EchoReplyInputBuilder();
        builder.setVersion(echoRequestMessage.getVersion());
        builder.setXid(echoRequestMessage.getXid());
        builder.setData(echoRequestMessage.getData());

        connectionAdapter.echoReply(builder.build());
    }

    @Override
    public void onErrorMessage(final ErrorMessage notification) {
        deviceReplyProcessor.processReply(notification);
    }

    @Override
    public void onExperimenterMessage(final ExperimenterMessage notification) {
        LOG.trace("Received experiementer message: {}", notification.getClass());
        deviceReplyProcessor.processExperimenterMessage(notification);
    }

    @Override
    public void onFlowRemovedMessage(final FlowRemovedMessage notification) {
        deviceReplyProcessor.processFlowRemovedMessage(notification);
    }

    @Override
    public void onHelloMessage(final HelloMessage hello) {
        LOG.warn("hello message received outside handshake phase -> dropping connection {}", connectionAdapter.getRemoteAddress());
        connectionAdapter.disconnect();
    }

    @Override
    public void onMultipartReplyMessage(final MultipartReplyMessage notification) {
        LOG.trace("Multipart Reply with XID: {}", notification.getXid());
//        multiMsgCollector.addMultipartMsg(notification);
    }

    @Override
    public void onPacketInMessage(final PacketInMessage notification) {
        deviceReplyProcessor.processPacketInMessage(notification);
    }

    @Override
    public void onPortStatusMessage(final PortStatusMessage notification) {
        deviceReplyProcessor.processPortStatusMessage(notification);
    }

}
