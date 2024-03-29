/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.listener;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.DeviceConnectionStatusProvider;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link HandshakeListenerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class HandshakeListenerImplTest {

    @Mock
    private DeviceConnectedHandler deviceConnectedHandler;
    @Mock
    private GetFeaturesOutput features;
    @Mock
    private ConnectionAdapter connectionAdapter;
    @Mock
    private HandshakeContext handshakeContext;
    @Mock
    private DeviceConnectionStatusProvider deviceConnectionStatusProvider;
    @Captor
    private ArgumentCaptor<NodeId> nodeIdCaptor;

    private ConnectionContext connectionContextSpy;
    private HandshakeListenerImpl handshakeListener;

    @Before
    public void setUp() {
        when(connectionAdapter.barrier(any()))
                .thenReturn(RpcResultBuilder.success(new BarrierOutputBuilder().build()).buildFuture());
        connectionContextSpy = spy(new ConnectionContextImpl(connectionAdapter, deviceConnectionStatusProvider));
        when(connectionContextSpy.getConnectionAdapter()).thenReturn(connectionAdapter);
        when(features.getDatapathId()).thenReturn(Uint64.TEN);
        when(features.getVersion()).thenReturn(Uint8.ONE);
        handshakeListener = new HandshakeListenerImpl(connectionContextSpy, deviceConnectedHandler);
        handshakeListener.setHandshakeContext(handshakeContext);
    }

    @After
    public void tearDown() {
        verify(handshakeContext).close();
    }

    @Test
    public void testOnHandshakeSuccessfull() {
        handshakeListener.onHandshakeSuccessful(features, OFConstants.OFP_VERSION_1_3);
        verify(connectionContextSpy).changeStateToWorking();
        verify(connectionContextSpy).setFeatures(any(FeaturesReply.class));
        verify(connectionContextSpy).setNodeId(nodeIdCaptor.capture());
        verify(connectionContextSpy).handshakeSuccessful();
        verify(deviceConnectedHandler).deviceConnected(connectionContextSpy);
        verify(handshakeContext).close();

        assertEquals("openflow:10", nodeIdCaptor.getValue().getValue());
    }

    @Test
    public void testOnHandshakeFailure1() {
        connectionContextSpy.setNodeId(new NodeId("ut-device:10"));
        handshakeListener.onHandshakeFailure();
        verify(handshakeContext).close();
        verify(connectionContextSpy).closeConnection(false);
    }

    @Test
    public void testOnHandshakeFailure2() {
        connectionContextSpy.setNodeId(new NodeId("openflow:1"));
        handshakeListener.onHandshakeFailure();
        verify(handshakeContext).close();
        verify(connectionContextSpy).closeConnection(false);
    }
}
