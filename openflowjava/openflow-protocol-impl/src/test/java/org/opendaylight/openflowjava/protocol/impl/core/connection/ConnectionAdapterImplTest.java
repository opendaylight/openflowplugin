/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutputBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEventBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEventBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit test for ConnectionAdapterImpl.
 *
 * @author michal.polkorab
 * @author madamjak
 */
public class ConnectionAdapterImplTest {

    private static final int RPC_RESPONSE_EXPIRATION = 1;
    private static final int CHANNEL_OUTBOUND_QUEUE_SIZE = 1024;
    private static final RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>> REMOVAL_LISTENER =
        notification -> notification.getValue().discard();

    @Mock SocketChannel channel;
    @Mock ChannelPipeline pipeline;
    @Mock OpenflowProtocolListener messageListener;
    @Mock SystemNotificationsListener systemListener;
    @Mock ConnectionReadyListener readyListener;
    @Mock Cache<RpcResponseKey, ResponseExpectedRpcListener<?>> mockCache;
    @Mock ChannelFuture channelFuture;

    private ConnectionAdapterImpl adapter;
    private Cache<RpcResponseKey, ResponseExpectedRpcListener<?>> cache;

    /**
     * Initializes ConnectionAdapter.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(channel.pipeline()).thenReturn(pipeline);
        adapter = new ConnectionAdapterImpl(channel, InetSocketAddress.createUnresolved("10.0.0.1", 6653), true,
                CHANNEL_OUTBOUND_QUEUE_SIZE);
        adapter.setMessageListener(messageListener);
        adapter.setSystemListener(systemListener);
        adapter.setConnectionReadyListener(readyListener);
        cache = CacheBuilder.newBuilder().concurrencyLevel(1)
                .expireAfterWrite(RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES).removalListener(REMOVAL_LISTENER).build();
        adapter.setResponseCache(cache);
        when(channel.disconnect()).thenReturn(channelFuture);
    }

    /**
     * Tests {@link ConnectionAdapterImpl#consume(DataObject)} with notifications.
     */
    @Test
    public void testConsume() {
        DataObject message = new EchoRequestMessageBuilder().build();
        adapter.consume(message);
        verify(messageListener, times(1)).onEchoRequestMessage((EchoRequestMessage) message);
        message = new ErrorMessageBuilder().build();
        adapter.consume(message);
        verify(messageListener, times(1)).onErrorMessage((ErrorMessage) message);
        message = new ExperimenterMessageBuilder().build();
        adapter.consume(message);
        verify(messageListener, times(1)).onExperimenterMessage((ExperimenterMessage) message);
        message = new FlowRemovedMessageBuilder().build();
        adapter.consume(message);
        verify(messageListener, times(1)).onFlowRemovedMessage((FlowRemovedMessage) message);
        message = new HelloMessageBuilder().build();
        adapter.consume(message);
        verify(messageListener, times(1)).onHelloMessage((HelloMessage) message);
        message = new MultipartReplyMessageBuilder().build();
        adapter.consume(message);
        verify(messageListener, times(1)).onMultipartReplyMessage((MultipartReplyMessage) message);
        message = new PacketInMessageBuilder().build();
        adapter.consume(message);
        verify(messageListener, times(1)).onPacketInMessage((PacketInMessage) message);
        message = new PortStatusMessageBuilder().build();
        adapter.consume(message);
        verify(messageListener, times(1)).onPortStatusMessage((PortStatusMessage) message);
        message = new SwitchIdleEventBuilder().build();
        adapter.consume(message);
        verify(systemListener, times(1)).onSwitchIdleEvent((SwitchIdleEvent) message);
        message = new DisconnectEventBuilder().build();
        adapter.consume(message);
        verify(systemListener, times(1)).onDisconnectEvent((DisconnectEvent) message);
        message = new EchoRequestMessageBuilder().build();
        adapter.consume(message);
        verify(messageListener, times(1)).onEchoRequestMessage((EchoRequestMessage) message);
    }

    /**
     * Tests {@link ConnectionAdapterImpl#consume(DataObject)} with unexpected rpc.
     */
    @Test
    public void testConsume2() {
        adapter.setResponseCache(mockCache);
        final BarrierOutputBuilder barrierBuilder = new BarrierOutputBuilder();
        barrierBuilder.setXid(Uint32.valueOf(42));
        final BarrierOutput barrier = barrierBuilder.build();
        adapter.consume(barrier);
        verify(mockCache, times(1)).getIfPresent(any(RpcResponseKey.class));
    }

    /**
     * Tests {@link ConnectionAdapterImpl#consume(DataObject)} with expected rpc.
     */
    @Test
    public void testConsume3() {
        final BarrierInputBuilder inputBuilder = new BarrierInputBuilder();
        inputBuilder.setVersion(EncodeConstants.OF_VERSION_1_3);
        inputBuilder.setXid(Uint32.valueOf(42));
        final BarrierInput barrierInput = inputBuilder.build();
        final RpcResponseKey key = new RpcResponseKey(42L,
                "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput");
        final ResponseExpectedRpcListener<OfHeader> listener = new ResponseExpectedRpcListener<>(barrierInput,
                "failure", mockCache, key);
        cache.put(key, listener);
        final BarrierOutputBuilder barrierBuilder = new BarrierOutputBuilder();
        barrierBuilder.setXid(Uint32.valueOf(42));
        final BarrierOutput barrierOutput = barrierBuilder.build();
        adapter.consume(barrierOutput);
        final ResponseExpectedRpcListener<?> ifPresent = cache.getIfPresent(key);
        Assert.assertNull("Listener was not discarded", ifPresent);
    }

    /**
     * Test IsAlive method.
     */
    @Test
    public void testIsAlive() {
        final int port = 9876;
        final String host = "localhost";
        final InetSocketAddress inetSockAddr = InetSocketAddress.createUnresolved(host, port);
        final ConnectionAdapterImpl connAddapter = new ConnectionAdapterImpl(channel, inetSockAddr, true,
                CHANNEL_OUTBOUND_QUEUE_SIZE);
        Assert.assertEquals("Wrong - diffrence between channel.isOpen() and ConnectionAdapterImpl.isAlive()",
                channel.isOpen(), connAddapter.isAlive());
        connAddapter.disconnect();
        Assert.assertFalse("Wrong - ConnectionAdapterImpl can not be alive after disconnet.", connAddapter.isAlive());
    }

    /**
     * Test throw exception if no listeners are present.
     */
    @Test(expected = java.lang.IllegalStateException.class)
    public void testMissingListeners() {
        final int port = 9876;
        final String host = "localhost";
        final InetSocketAddress inetSockAddr = InetSocketAddress.createUnresolved(host, port);
        final ConnectionAdapterImpl connAddapter = new ConnectionAdapterImpl(channel, inetSockAddr, true,
                CHANNEL_OUTBOUND_QUEUE_SIZE);
        connAddapter.setSystemListener(null);
        connAddapter.setMessageListener(null);
        connAddapter.setConnectionReadyListener(null);
        connAddapter.checkListeners();
    }
}
