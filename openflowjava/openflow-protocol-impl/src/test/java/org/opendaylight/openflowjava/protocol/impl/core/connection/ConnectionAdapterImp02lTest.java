/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.mockito.Mockito.doReturn;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.ClassToInstanceMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Barrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Echo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Hello;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for ConnectionAdapterImp02l.
 *
 * @author madamjak
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionAdapterImp02lTest {
    private static final int RPC_RESPONSE_EXPIRATION = 1;
    private static final int CHANNEL_OUTBOUND_QUEUE_SIZE = 1024;
    private static final RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>> REMOVAL_LISTENER =
        notification -> notification.getValue().discard();

    @Mock EchoInput echoInput;
    @Mock BarrierInput barrierInput;
    @Mock EchoReplyInput echoReplyInput;
    @Mock ExperimenterInput experimenterInput;
    @Mock FlowModInput flowModInput;
    @Mock GetConfigInput getConfigInput;
    @Mock GetFeaturesInput getFeaturesInput;
    @Mock GetQueueConfigInput getQueueConfigInput;
    @Mock GroupModInput groupModInput;
    @Mock HelloInput helloInput;
    @Mock MeterModInput meterModInput;
    @Mock PacketOutInput packetOutInput;
    @Mock MultipartRequestInput multipartRequestInput;
    @Mock PortModInput portModInput;
    @Mock RoleRequestInput roleRequestInput;
    @Mock SetConfigInput setConfigInput;
    @Mock TableModInput tableModInput;
    @Mock GetAsyncInput getAsyncInput;
    @Mock SetAsyncInput setAsyncInput;
    private ConnectionAdapterImpl adapter;
    private ClassToInstanceMap<Rpc<?, ?>> rpcMap;
    private Cache<RpcResponseKey, ResponseExpectedRpcListener<?>> cache;
    private OfHeader responseOfCall;

    /**
     * Initialize mocks.
     */
    @Before
    public void setUp() {
        mockXid(barrierInput);
        mockXid(echoInput);
        mockXid(echoReplyInput);
        mockXid(getConfigInput);
        mockXid(getFeaturesInput);
        mockXid(getQueueConfigInput);
        mockXid(groupModInput);
        mockXid(roleRequestInput);
        mockXid(setConfigInput);
        mockXid(tableModInput);
        mockXid(getAsyncInput);
        mockXid(setAsyncInput);
    }

    private static void mockXid(final OfHeader message) {
        doReturn(Uint32.ZERO).when(message).getXid();
    }

    /**
     * Disconnect adapter.
     */
    @After
    public void tearDown() {
        if (adapter != null && adapter.isAlive()) {
            adapter.disconnect();
        }
    }

    /**
     * Test Rpc Calls.
     */
    @Test
    public void testRcp() {
        final EmbeddedChannel embChannel = new EmbeddedChannel(new EmbededChannelHandler());
        adapter = new ConnectionAdapterImpl(embChannel, InetSocketAddress.createUnresolved("localhost", 9876), true,
                CHANNEL_OUTBOUND_QUEUE_SIZE);
        rpcMap = adapter.getRpcClassToInstanceMap();
        cache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(
                RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES).removalListener(REMOVAL_LISTENER).build();
        adapter.setResponseCache(cache);
        // -- barrier
        rpcMap.getInstance(Barrier.class).invoke(barrierInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - barrier", barrierInput, responseOfCall);
        // -- echo
        rpcMap.getInstance(Echo.class).invoke(echoInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - echo", echoInput, responseOfCall);
        // -- echoReply
        rpcMap.getInstance(EchoReply.class).invoke(echoReplyInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - echoReply",echoReplyInput, responseOfCall);
        // -- experimenter
        rpcMap.getInstance(Experimenter.class).invoke(experimenterInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - experimenter",experimenterInput, responseOfCall);
        // -- flowMod
        rpcMap.getInstance(FlowMod.class).invoke(flowModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - flowMod", flowModInput, responseOfCall);
        // -- getConfig
        rpcMap.getInstance(GetConfig.class).invoke(getConfigInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - getConfig", getConfigInput, responseOfCall);
        // -- getFeatures
        rpcMap.getInstance(GetFeatures.class).invoke(getFeaturesInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - getFeatures",getFeaturesInput, responseOfCall);
        // -- getQueueConfig
        rpcMap.getInstance(GetQueueConfig.class).invoke(getQueueConfigInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - getQueueConfig",getQueueConfigInput, responseOfCall);
        // -- groupMod
        rpcMap.getInstance(GroupMod.class).invoke(groupModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - groupMod", groupModInput, responseOfCall);
        // -- hello
        rpcMap.getInstance(Hello.class).invoke(helloInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - helloInput",helloInput, responseOfCall);
        // -- meterMod
        rpcMap.getInstance(MeterMod.class).invoke(meterModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - meterMod",meterModInput, responseOfCall);
        // -- packetOut
        rpcMap.getInstance(PacketOut.class).invoke(packetOutInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - packetOut",packetOutInput, responseOfCall);
        // -- multipartRequest
        rpcMap.getInstance(MultipartRequest.class).invoke(multipartRequestInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - multipartRequest", multipartRequestInput, responseOfCall);
        // -- portMod
        rpcMap.getInstance(PortMod.class).invoke(portModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - portMod", portModInput, responseOfCall);
        // -- roleRequest
        rpcMap.getInstance(RoleRequest.class).invoke(roleRequestInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - roleRequest", roleRequestInput, responseOfCall);
        // -- setConfig
        rpcMap.getInstance(SetConfig.class).invoke(setConfigInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - setConfig",setConfigInput, responseOfCall);
        // -- tableMod
        rpcMap.getInstance(TableMod.class).invoke(tableModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - tableMod", tableModInput, responseOfCall);
        // -- getAsync
        rpcMap.getInstance(GetAsync.class).invoke(getAsyncInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - getAsync", getAsyncInput, responseOfCall);
        // -- setAsync
        rpcMap.getInstance(SetAsync.class).invoke(setAsyncInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - setAsync", setAsyncInput, responseOfCall);
        adapter.disconnect();
    }

    /**
     * Channel Handler for testing.
     * @author madamjak
     */
    private final class EmbededChannelHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(final ChannelHandlerContext ctx, final Object msg,
                final ChannelPromise promise) {
            responseOfCall = null;
            if (msg instanceof MessageListenerWrapper listener) {
                responseOfCall = listener.getMsg();
            }
        }
    }
}
