/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import java.net.InetSocketAddress;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Test for {@link ConnectionContextImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionContextImplTest {

    @Mock
    private ConnectionAdapter connetionAdapter;
    @Mock
    private HandshakeContext handshakeContext;
    @Mock
    private OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueRegistration;
    @Mock
    private DeviceDisconnectedHandler deviceDisconnectedHandler;

    private ConnectionContextImpl connectionContext;

    @Before
    public void setUp() throws Exception {
        Mockito.when(connetionAdapter.getRemoteAddress()).thenReturn(InetSocketAddress.createUnresolved("ofp-ut.example.org", 4242));
        Mockito.when(connetionAdapter.isAlive()).thenReturn(true);

        connectionContext = new ConnectionContextImpl(connetionAdapter);
        connectionContext.setHandshakeContext(handshakeContext);
        connectionContext.setNodeId(new NodeId("ut-node:123"));
        connectionContext.setOutboundQueueHandleRegistration(outboundQueueRegistration);
        connectionContext.setDeviceDisconnectedHandler(deviceDisconnectedHandler);

        Assert.assertNull(connectionContext.getConnectionState());
    }

    @Test
    public void testCloseConnection1() throws Exception {
        connectionContext.closeConnection(true);
        Mockito.verify(outboundQueueRegistration).close();
        Mockito.verify(handshakeContext).close();
        Mockito.verify(connetionAdapter).disconnect();
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.RIP, connectionContext.getConnectionState());

        Mockito.verify(deviceDisconnectedHandler).onDeviceDisconnected(connectionContext);
    }

    @Test
    public void testCloseConnection2() throws Exception {
        connectionContext.closeConnection(false);
        Mockito.verify(outboundQueueRegistration).close();
        Mockito.verify(handshakeContext).close();
        Mockito.verify(connetionAdapter).disconnect();
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.RIP, connectionContext.getConnectionState());

        Mockito.verify(deviceDisconnectedHandler, Mockito.never()).onDeviceDisconnected(connectionContext);
    }

    @Test
    public void testOnConnectionClosed() throws Exception {
        connectionContext.onConnectionClosed();
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.RIP, connectionContext.getConnectionState());
        Mockito.verify(outboundQueueRegistration).close();
        Mockito.verify(handshakeContext).close();
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.RIP, connectionContext.getConnectionState());
        Mockito.verify(deviceDisconnectedHandler).onDeviceDisconnected(connectionContext);
    }

    @Test
    public void testChangeStateToHandshaking() throws Exception {
        connectionContext.changeStateToHandshaking();
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.HANDSHAKING, connectionContext.getConnectionState());
    }

    @Test
    public void testChangeStateToTimeouting() throws Exception {
        connectionContext.changeStateToTimeouting();
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.TIMEOUTING, connectionContext.getConnectionState());
    }

    @Test
    public void testChangeStateToWorking() throws Exception {
        connectionContext.changeStateToWorking();
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.WORKING, connectionContext.getConnectionState());
    }
}