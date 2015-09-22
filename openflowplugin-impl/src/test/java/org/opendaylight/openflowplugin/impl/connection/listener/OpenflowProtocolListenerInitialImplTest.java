/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.listener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;

/**
 * Test for {@link OpenflowProtocolListenerInitialImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenflowProtocolListenerInitialImplTest {

    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private HandshakeContext handshakeContext;
    @Mock
    private ConnectionAdapter connectionAdapter;
    @Mock
    private HandshakeManager handshakeManager;

    private OpenflowProtocolListenerInitialImpl openflowProtocolListenerInitial;

    @Before
    public void setUp() throws Exception {
        Mockito.when(connectionAdapter.isAlive()).thenReturn(true);
        Mockito.when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionContext.getConnectionState()).thenReturn(null, ConnectionContext.CONNECTION_STATE.HANDSHAKING);
        Mockito.when(handshakeContext.getHandshakeManager()).thenReturn(handshakeManager);

        openflowProtocolListenerInitial = new OpenflowProtocolListenerInitialImpl(connectionContext, handshakeContext);
    }

    @Test
    public void testOnEchoRequestMessage() throws Exception {
        EchoRequestMessageBuilder echoRequestMessageBld = new EchoRequestMessageBuilder()
                .setXid(42L)
                .setVersion(OFConstants.OFP_VERSION_1_3);
        openflowProtocolListenerInitial.onEchoRequestMessage(echoRequestMessageBld.build());

        Mockito.verify(connectionAdapter).echoReply(Matchers.<EchoReplyInput>any());
    }

    @Test
    public void testOnHelloMessage() throws Exception {
        HelloMessageBuilder helloMessageBld = new HelloMessageBuilder()
                .setXid(42L)
                .setVersion(OFConstants.OFP_VERSION_1_3);
        openflowProtocolListenerInitial.onHelloMessage(helloMessageBld.build());

        Mockito.verify(handshakeManager).shake(Matchers.<HelloMessage>any());
    }

    @Test
    public void testCheckState() throws Exception {
        Assert.assertFalse(openflowProtocolListenerInitial.checkState(ConnectionContext.CONNECTION_STATE.HANDSHAKING));
        Assert.assertTrue(openflowProtocolListenerInitial.checkState(ConnectionContext.CONNECTION_STATE.HANDSHAKING));
    }
}