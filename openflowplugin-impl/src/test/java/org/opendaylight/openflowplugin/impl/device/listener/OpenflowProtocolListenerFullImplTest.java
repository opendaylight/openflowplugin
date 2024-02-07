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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
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
        verify(connectionAdapter).setMessageListener(any(OpenflowProtocolListener.class));
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(connectionAdapter, deviceReplyProcessor);
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onEchoRequestMessage(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage)}.
     */
    @Test
    public void testOnEchoRequestMessage() {
        when(connectionAdapter.echoReply(any())).thenReturn(Futures.immediateFuture(null));
        ofProtocolListener.onEchoRequestMessage(
            new EchoRequestMessageBuilder().setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build());

        verify(connectionAdapter).echoReply(any(EchoReplyInput.class));
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onErrorMessage(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage)}.
     */
    @Test
    public void testOnErrorMessage() {
        ErrorMessage errorMessage = new ErrorMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onErrorMessage(errorMessage);

        verify(deviceReplyProcessor).processReply(any(ErrorMessage.class));
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onExperimenterMessage(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage)}.
     */
    @Test
    public void testOnExperimenterMessage() {
        ExperimenterMessage experimenterMessage = new ExperimenterMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onExperimenterMessage(experimenterMessage);

        verify(deviceReplyProcessor).processExperimenterMessage(any());
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onFlowRemovedMessage(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage)}.
     */
    @Test
    public void testOnFlowRemovedMessage() {
        FlowRemovedMessage flowRemovedMessage = new FlowRemovedMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onFlowRemovedMessage(flowRemovedMessage);

        verify(deviceReplyProcessor).processFlowRemovedMessage(any(FlowRemovedMessage.class));
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onHelloMessage(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage)}.
     */
    @Test
    public void testOnHelloMessage() {
        HelloMessage helloMessage = new HelloMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onHelloMessage(helloMessage);

        verify(connectionAdapter).getRemoteAddress();
        verify(connectionAdapter).disconnect();
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onPacketInMessage(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage)}.
     */
    @Test
    public void testOnPacketInMessage() {
        PacketInMessage packetInMessage = new PacketInMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onPacketInMessage(packetInMessage);

        verify(deviceReplyProcessor).processPacketInMessage(any(PacketInMessage.class));
    }

    /**
     * Test method for
     * {@link OpenflowProtocolListenerFullImpl#onPortStatusMessage(
     * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage)}.
     */
    @Test
    public void testOnPortStatusMessage() {
        PortStatusMessage portStatusMessage = new PortStatusMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onPortStatusMessage(portStatusMessage);

        verify(deviceReplyProcessor).processPortStatusMessage(any(PortStatusMessage.class));
    }
}
