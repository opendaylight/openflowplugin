/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.listener;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collection;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OpenflowProtocolListenerFullImpl implements OpenflowProtocolListener, MultiMsgCollector {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowProtocolListenerFullImpl.class);

    private final ConnectionAdapter connectionAdapter;
    private final DeviceReplyProcessor deviceReplyProcessor;
    private final MultiMsgCollectorImpl multiMsgCollector;

    /**
     * @param connectionAdapter
     * @param deviceReplyProcessor
     */
    public OpenflowProtocolListenerFullImpl(final ConnectionAdapter connectionAdapter, final DeviceReplyProcessor deviceReplyProcessor) {
        this.connectionAdapter = connectionAdapter;
        this.deviceReplyProcessor = deviceReplyProcessor;
        multiMsgCollector = new MultiMsgCollectorImpl();
        multiMsgCollector.setDeviceReplyProcessor(deviceReplyProcessor);
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
        // TODO Auto-generated method stub

    }

    @Override
    public void onFlowRemovedMessage(final FlowRemovedMessage notification) {
        deviceReplyProcessor.processFlowRemovedMessage(notification);
    }

    @Override
    public void onHelloMessage(final HelloMessage hello) {
        // FIXME: invalid state - must disconnect and close all contexts
    }

    @Override
    public void onMultipartReplyMessage(final MultipartReplyMessage notification) {
        LOG.trace("Multipart Reply with XID: {}", notification.getXid());
        multiMsgCollector.addMultipartMsg(notification);
    }

    @Override
    public void onPacketInMessage(final PacketInMessage notification) {
        deviceReplyProcessor.processPacketInMessage(notification);
    }

    @Override
    public void onPortStatusMessage(final PortStatusMessage notification) {
        deviceReplyProcessor.processPortStatusMessage(notification);
    }

    @Override
    public ListenableFuture<Collection<MultipartReply>> registerMultipartMsg(final long xid) {
        return multiMsgCollector.registerMultipartMsg(xid);
    }

    @Override
    public void registerMultipartFutureMsg(final long xid, @CheckForNull final SettableFuture<Collection<MultipartReply>> future) {
        multiMsgCollector.registerMultipartFutureMsg(xid, future);
    }

    @Override
    public void addMultipartMsg(@Nonnull final MultipartReply reply) {
        multiMsgCollector.addMultipartMsg(reply);
    }
}
