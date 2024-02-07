/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection.listener;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
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
        when(connectionAdapter.isAlive()).thenReturn(true);
        when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        when(connectionContext.getConnectionState())
                .thenReturn(null, ConnectionContext.CONNECTION_STATE.HANDSHAKING);
        when(handshakeContext.getHandshakeManager()).thenReturn(handshakeManager);

        openflowProtocolListenerInitial = new OpenflowProtocolListenerInitialImpl(connectionContext, handshakeContext);
    }

    @Test
    public void testOnEchoRequestMessage() {
        when(connectionAdapter.echoReply(any())).thenReturn(Futures.immediateFuture(null));

        openflowProtocolListenerInitial.onEchoRequestMessage(
            new EchoRequestMessageBuilder()
                .setXid(Uint32.valueOf(42))
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .build());

        verify(connectionAdapter).echoReply(any());
    }

    @Test
    public void testOnHelloMessage() {
        HelloMessageBuilder helloMessageBld = new HelloMessageBuilder()
                .setXid(Uint32.valueOf(42))
                .setVersion(EncodeConstants.OF_VERSION_1_3);
        openflowProtocolListenerInitial.onHelloMessage(helloMessageBld.build());

        verify(handshakeManager).shake(any());
    }

    @Test
    public void testCheckState() {
        assertFalse(openflowProtocolListenerInitial.checkState(ConnectionContext.CONNECTION_STATE.HANDSHAKING));
        assertTrue(openflowProtocolListenerInitial.checkState(ConnectionContext.CONNECTION_STATE.HANDSHAKING));
    }
}