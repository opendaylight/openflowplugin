/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import java.net.InetSocketAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.connection.listener
 * test of {@link OpenflowProtocolListenerFullImpl} - lightweight version, using basic ways (TDD).
 *
 * @author Vaclav Demcak
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenflowProtocolListenerFullImplTest {
    @Mock
    private DeviceReplyProcessor deviceReplyProcessor;
    @Mock
    private ConnectionAdapter connectionAdapter;

    private final Uint32 xid = Uint32.valueOf(42);

    private OpenflowProtocolListenerFullImpl ofProtocolListener;

    @Before
    public void setUp() {
        // place for mocking method's general behavior for HandshakeContext and ConnectionContext
        ofProtocolListener = new OpenflowProtocolListenerFullImpl(connectionAdapter, deviceReplyProcessor);
        connectionAdapter.setMessageListener(ofProtocolListener);
        when(connectionAdapter.getRemoteAddress())
                .thenReturn(InetSocketAddress.createUnresolved("ofp-junit.example.org", 6663));
        verify(connectionAdapter).setMessageListener(any());
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(connectionAdapter, deviceReplyProcessor);
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onEchoRequest(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage)}.
     */
    @Test
    public void testOnEchoRequestMessage() {
        when(connectionAdapter.echoReply(any())).thenReturn(Futures.immediateFuture(null));
        ofProtocolListener.onEchoRequest(
            new EchoRequestMessageBuilder().setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build());

        verify(connectionAdapter).echoReply(any(EchoReplyInput.class));
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onError(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage)}.
     */
    @Test
    public void testOnErrorMessage() {
        ofProtocolListener.onError(
            new ErrorMessageBuilder().setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build());

        verify(deviceReplyProcessor).processReply(any(ErrorMessage.class));
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onExperimenter(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage)}.
     */
    @Test
    public void testOnExperimenterMessage() {
        ofProtocolListener.onExperimenter(
            new ExperimenterMessageBuilder().setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build());

        verify(deviceReplyProcessor).processExperimenterMessage(any());
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onFlowRemoved(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage)}.
     */
    @Test
    public void testOnFlowRemovedMessage() {
        ofProtocolListener.onFlowRemoved(
            new FlowRemovedMessageBuilder().setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build());

        verify(deviceReplyProcessor).processFlowRemovedMessage(any(FlowRemovedMessage.class));
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onHello(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage)}.
     */
    @Test
    public void testOnHelloMessage() {
        ofProtocolListener.onHello(
            new HelloMessageBuilder().setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build());

        verify(connectionAdapter).getRemoteAddress();
        verify(connectionAdapter).disconnect();
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onPacketIn(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage)}.
     */
    @Test
    public void testOnPacketInMessage() {
        ofProtocolListener.onPacketIn(
            new PacketInMessageBuilder().setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build());

        verify(deviceReplyProcessor).processPacketInMessage(any(PacketInMessage.class));
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onPortStatus(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage)}.
     */
    @Test
    public void testOnPortStatusMessage() {
        ofProtocolListener.onPortStatus(
            new PortStatusMessageBuilder().setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build());

        verify(deviceReplyProcessor).processPortStatusMessage(any(PortStatusMessage.class));
    }
}
