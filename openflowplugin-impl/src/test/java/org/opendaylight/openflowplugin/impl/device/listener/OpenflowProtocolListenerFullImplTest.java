/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.listener;

import java.net.InetSocketAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
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

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.connection.listener
 * <p/>
 * test of {@link OpenflowProtocolListenerFullImpl} - lightweight version, using basic ways (TDD)
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         <p/>
 *         Created: Mar 26, 2015
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenflowProtocolListenerFullImplTest {

    private OpenflowProtocolListenerFullImpl ofProtocolListener;

    @Mock
    private DeviceReplyProcessor deviceReplyProcessor;
    @Mock
    private ConnectionAdapter connectionAdapter;

    private final long xid = 42L;

    @Before
    public void setUp() {
        // place for mocking method's general behavior for HandshakeContext and ConnectionContext
        ofProtocolListener = new OpenflowProtocolListenerFullImpl(connectionAdapter, deviceReplyProcessor);
        connectionAdapter.setMessageListener(ofProtocolListener);
        Mockito.when(connectionAdapter.getRemoteAddress()).thenReturn(InetSocketAddress.createUnresolved("ofp-junit.example.org", 6663));
        Mockito.verify(connectionAdapter).setMessageListener(Matchers.any(OpenflowProtocolListener.class));
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(connectionAdapter, deviceReplyProcessor);
    }

    /**
     * Test method for {@link OpenflowProtocolListenerFullImpl#onEchoRequestMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage)}.
     */
    @Test
    public void testOnEchoRequestMessage() {
        EchoRequestMessage echoRequestMessage = new EchoRequestMessageBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onEchoRequestMessage(echoRequestMessage);

        Mockito.verify(connectionAdapter).echoReply(Matchers.any(EchoReplyInput.class));
    }

    /**
     * Test method for {@link OpenflowProtocolListenerFullImpl#onErrorMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage)}.
     */
    @Test
    public void testOnErrorMessage() {
        ErrorMessage errorMessage = new ErrorMessageBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onErrorMessage(errorMessage);

        Mockito.verify(deviceReplyProcessor).processReply(Matchers.any(ErrorMessage.class));
    }

    /**
     * Test method for {@link OpenflowProtocolListenerFullImpl#onExperimenterMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage)}.
     */
    @Test
    public void testOnExperimenterMessage() {
        ExperimenterMessage experimenterMessage = new ExperimenterMessageBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onExperimenterMessage(experimenterMessage);

        Mockito.verify(deviceReplyProcessor).processExperimenterMessage(Matchers.<ExperimenterMessage>any());
    }

    /**
     * Test method for {@link OpenflowProtocolListenerFullImpl#onFlowRemovedMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage)}.
     */
    @Test
    public void testOnFlowRemovedMessage() {
        FlowRemovedMessage flowRemovedMessage = new FlowRemovedMessageBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onFlowRemovedMessage(flowRemovedMessage);

        Mockito.verify(deviceReplyProcessor).processFlowRemovedMessage(Matchers.any(FlowRemovedMessage.class));
    }

    /**
     * Test method for {@link OpenflowProtocolListenerFullImpl#onHelloMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage)}.
     */
    @Test
    public void testOnHelloMessage() {
        HelloMessage helloMessage = new HelloMessageBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onHelloMessage(helloMessage);

        Mockito.verify(connectionAdapter).getRemoteAddress();
        Mockito.verify(connectionAdapter).disconnect();
    }

    /**
     * Test method for {@link OpenflowProtocolListenerFullImpl#onPacketInMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage)}.
     */
    @Test
    public void testOnPacketInMessage() {
        PacketInMessage packetInMessage = new PacketInMessageBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onPacketInMessage(packetInMessage);

        Mockito.verify(deviceReplyProcessor).processPacketInMessage(Matchers.any(PacketInMessage.class));
    }

    /**
     * Test method for {@link OpenflowProtocolListenerFullImpl#onPortStatusMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage)}.
     */
    @Test
    public void testOnPortStatusMessage() {
        PortStatusMessage portStatusMessage = new PortStatusMessageBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_3).setXid(xid).build();
        ofProtocolListener.onPortStatusMessage(portStatusMessage);

        Mockito.verify(deviceReplyProcessor).processPortStatusMessage(Matchers.any(PortStatusMessage.class));
    }

}
