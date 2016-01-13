/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.listener;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link HandshakeListenerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class HandshakeListenerImplTest {

    private final Short version = OFConstants.OFP_VERSION_1_3;

    @Mock
    private DeviceConnectedHandler deviceConnectedHandler;
    @Mock
    private GetFeaturesOutput features;
    @Mock
    private ConnectionAdapter connectionAdapter;
    @Mock
    private HandshakeContext handshakeContext;
    @Captor
    private ArgumentCaptor<NodeId> nodeIdCaptor;

    private ConnectionContext connectionContextSpy;
    private HandshakeListenerImpl handshakeListener;

    @Before
    public void setUp() throws Exception {
        Mockito.when(connectionAdapter.barrier(Matchers.<BarrierInput>any()))
                .thenReturn(RpcResultBuilder.success(new BarrierOutputBuilder().build()).buildFuture());
        connectionContextSpy = Mockito.spy(new ConnectionContextImpl(connectionAdapter));
        Mockito.when(connectionContextSpy.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(features.getDatapathId()).thenReturn(BigInteger.TEN);
        handshakeListener = new HandshakeListenerImpl(connectionContextSpy, deviceConnectedHandler);
        handshakeListener.setHandshakeContext(handshakeContext);
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verify(handshakeContext).close();
    }

    @Test
    public void testOnHandshakeSuccessfull() throws Exception {
        handshakeListener.onHandshakeSuccessfull(features, version);
        Mockito.verify(connectionContextSpy).changeStateToWorking();
        Mockito.verify(connectionContextSpy).setFeatures(Matchers.any(FeaturesReply.class));
        Mockito.verify(connectionContextSpy).setNodeId(nodeIdCaptor.capture());
        Mockito.verify(deviceConnectedHandler).deviceConnected(connectionContextSpy);
        Mockito.verify(handshakeContext).close();

        Assert.assertEquals("openflow:10", nodeIdCaptor.getValue().getValue());
    }

    @Test
    public void testOnHandshakeFailure1() throws Exception {
        connectionContextSpy.setNodeId(new NodeId("ut-device:10"));
        handshakeListener.onHandshakeFailure();
        Mockito.verify(handshakeContext).close();
        Mockito.verify(connectionContextSpy).closeConnection(false);
    }

    @Test
    public void testOnHandshakeFailure2() throws Exception {
        Mockito.when(connectionAdapter.getRemoteAddress()).thenReturn(InetSocketAddress.createUnresolved("ut-ofp.example.org", 4242));
        handshakeListener.onHandshakeFailure();
        Mockito.verify(handshakeContext).close();
        Mockito.verify(connectionContextSpy).closeConnection(false);
    }
}