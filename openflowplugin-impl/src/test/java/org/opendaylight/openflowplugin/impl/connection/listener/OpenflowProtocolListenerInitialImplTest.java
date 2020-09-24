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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

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
    public void setUp() {
        Mockito.when(connectionAdapter.isAlive()).thenReturn(true);
        Mockito.when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionContext.getConnectionState())
                .thenReturn(null, ConnectionContext.CONNECTION_STATE.HANDSHAKING);
        Mockito.when(handshakeContext.getHandshakeManager()).thenReturn(handshakeManager);

        openflowProtocolListenerInitial = new OpenflowProtocolListenerInitialImpl(connectionContext, handshakeContext);
    }

    @Test
    public void testOnEchoRequestMessage() {
        EchoRequestMessageBuilder echoRequestMessageBld = new EchoRequestMessageBuilder()
                .setXid(Uint32.valueOf(42))
                .setVersion(OFConstants.OFP_VERSION_1_3);
        openflowProtocolListenerInitial.onEchoRequestMessage(echoRequestMessageBld.build());

        Mockito.verify(connectionAdapter).echoReply(ArgumentMatchers.any());
    }

    @Test
    public void testOnHelloMessage() {
        HelloMessageBuilder helloMessageBld = new HelloMessageBuilder()
                .setXid(Uint32.valueOf(42))
                .setVersion(OFConstants.OFP_VERSION_1_3);
        openflowProtocolListenerInitial.onHelloMessage(helloMessageBld.build());

        Mockito.verify(handshakeManager).shake(ArgumentMatchers.any());
    }

    @Test
    public void testCheckState() {
        Assert.assertFalse(openflowProtocolListenerInitial.checkState(ConnectionContext.CONNECTION_STATE.HANDSHAKING));
        Assert.assertTrue(openflowProtocolListenerInitial.checkState(ConnectionContext.CONNECTION_STATE.HANDSHAKING));
    }
}