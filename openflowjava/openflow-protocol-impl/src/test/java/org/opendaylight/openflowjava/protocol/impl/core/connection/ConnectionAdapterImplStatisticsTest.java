/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.ClassToInstanceMap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.statistics.CounterEventTypes;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Barrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Echo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableMod;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test counters in ConnectionAdapter (at least DS_ENTERED_OFJAVA, DS_FLOW_MODS_ENTERED and US_MESSAGE_PASS counters
 * have to be enabled).
 *
 * @author madamjak
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionAdapterImplStatisticsTest {

    private static final int RPC_RESPONSE_EXPIRATION = 1;
    private static final int CHANNEL_OUTBOUND_QUEUE_SIZE = 1024;
    private static final RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>> REMOVAL_LISTENER =
        notification -> notification.getValue().discard();

    @Mock SystemNotificationsListener systemListener;
    @Mock ConnectionReadyListener readyListener;
    @Mock ChannelFuture channelFuture;
    @Mock OpenflowProtocolListener messageListener;
    @Mock SocketChannel channel;
    @Mock ChannelPipeline pipeline;
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
    private StatisticsCounters statCounters;

    /**
     * Initialize mocks.
     * Start counting and reset counters before each test.
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

        statCounters = StatisticsCounters.getInstance();
        statCounters.startCounting(false, 0);
    }

    private static void mockXid(final OfHeader message) {
        doReturn(Uint32.ZERO).when(message).getXid();
    }

    /**
     * Disconnect adapter.
     * Stop counting after each test.
     */
    @After
    public void tearDown() {
        if (adapter != null && adapter.isAlive()) {
            adapter.disconnect();
        }
        statCounters.stopCounting();
    }

    /**
     * Test statistic counter for all rpc calls (counters DS_ENTERED_OFJAVA and DS_FLOW_MODS_ENTERED have to be
     * enabled).
     */
    @Test
    public void testEnterOFJavaCounter() {
        if (!statCounters.isCounterEnabled(CounterEventTypes.DS_ENTERED_OFJAVA)) {
            Assert.fail("Counter " + CounterEventTypes.DS_ENTERED_OFJAVA + " is not enabled");
        }
        if (!statCounters.isCounterEnabled(CounterEventTypes.DS_FLOW_MODS_ENTERED)) {
            Assert.fail("Counter " + CounterEventTypes.DS_FLOW_MODS_ENTERED + " is not enabled");
        }
        final EmbeddedChannel embChannel = new EmbeddedChannel(new EmbededChannelHandler());
        adapter = new ConnectionAdapterImpl(embChannel, InetSocketAddress.createUnresolved("localhost", 9876), true,
                CHANNEL_OUTBOUND_QUEUE_SIZE);
        rpcMap = adapter.getRpcClassToInstanceMap();
        cache = CacheBuilder.newBuilder().concurrencyLevel(1)
                .expireAfterWrite(RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES).removalListener(REMOVAL_LISTENER).build();
        adapter.setResponseCache(cache);
        rpcMap.getInstance(Barrier.class).invoke(barrierInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(Echo.class).invoke(echoInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(EchoReply.class).invoke(echoReplyInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(Experimenter.class).invoke(experimenterInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(FlowMod.class).invoke(flowModInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(GetConfig.class).invoke(getConfigInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(GetFeatures.class).invoke(getFeaturesInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(GetQueueConfig.class).invoke(getQueueConfigInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(GroupMod.class).invoke(groupModInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(Hello.class).invoke(helloInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(MeterMod.class).invoke(meterModInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(PacketOut.class).invoke(packetOutInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(MultipartRequest.class).invoke(multipartRequestInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(PortMod.class).invoke(portModInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(RoleRequest.class).invoke(roleRequestInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(SetConfig.class).invoke(setConfigInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(TableMod.class).invoke(tableModInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(GetAsync.class).invoke(getAsyncInput);
        embChannel.runPendingTasks();
        rpcMap.getInstance(SetAsync.class).invoke(setAsyncInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - bad counter value for ConnectionAdapterImpl rpc methods", 19,
                statCounters.getCounter(CounterEventTypes.DS_ENTERED_OFJAVA).getCounterValue());
        Assert.assertEquals("Wrong - bad counter value for ConnectionAdapterImpl flow-mod entered", 1,
                statCounters.getCounter(CounterEventTypes.DS_FLOW_MODS_ENTERED).getCounterValue());
        adapter.disconnect();
    }

    /**
     * Test counter for pass messages to consumer (counter US_MESSAGE_PASS has to be enabled).
     */
    @Test
    public void testMessagePassCounter() {
        if (!statCounters.isCounterEnabled(CounterEventTypes.US_MESSAGE_PASS)) {
            Assert.fail("Counter " + CounterEventTypes.US_MESSAGE_PASS + " is not enabled");
        }
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
        DataObject message = new EchoRequestMessageBuilder().build();
        adapter.consume(message);
        message = new ErrorMessageBuilder().build();
        adapter.consume(message);
        message = new ExperimenterMessageBuilder().build();
        adapter.consume(message);
        message = new FlowRemovedMessageBuilder().build();
        adapter.consume(message);
        message = new HelloMessageBuilder().build();
        adapter.consume(message);
        message = new MultipartReplyMessageBuilder().build();
        adapter.consume(message);
        message = new PacketInMessageBuilder().build();
        adapter.consume(message);
        message = new PortStatusMessageBuilder().build();
        adapter.consume(message);
        message = new EchoRequestMessageBuilder().build();
        adapter.consume(message);
        Assert.assertEquals("Wrong - bad counter value for ConnectionAdapterImpl consume method", 9,
                statCounters.getCounter(CounterEventTypes.US_MESSAGE_PASS).getCounterValue());
        adapter.disconnect();
    }

    /**
     * Empty channel Handler for testing.
     * @author madamjak
     */
    private static final class EmbededChannelHandler extends ChannelOutboundHandlerAdapter {
        // no operation need to test
    }
}
