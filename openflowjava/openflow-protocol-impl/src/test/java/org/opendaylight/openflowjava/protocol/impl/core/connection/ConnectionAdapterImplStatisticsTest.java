/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.statistics.CounterEventTypes;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Test counters in ConnectionAdapter (at least DS_ENTERED_OFJAVA, DS_FLOW_MODS_ENTERED and US_MESSAGE_PASS counters have to be enabled)
 * @author madamjak
 *
 */
public class ConnectionAdapterImplStatisticsTest {

    private static final int RPC_RESPONSE_EXPIRATION = 1;
    private static final RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>> REMOVAL_LISTENER =
            new RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>>() {
        @Override
        public void onRemoval(
                final RemovalNotification<RpcResponseKey, ResponseExpectedRpcListener<?>> notification) {
            notification.getValue().discard();
        }
    };

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
    private Cache<RpcResponseKey, ResponseExpectedRpcListener<?>> cache;
    private StatisticsCounters statCounters;

    /**
     * Initialize mocks
     * Start counting and reset counters before each test
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        statCounters = StatisticsCounters.getInstance();
        statCounters.startCounting(false, 0);
    }

    /**
     * Disconnect adapter
     * Stop counting after each test
     */
    @After
    public void tierDown(){
        if (adapter != null && adapter.isAlive()) {
            adapter.disconnect();
        }
        statCounters.stopCounting();
    }

    /**
     * Test statistic counter for all rpc calls (counters DS_ENTERED_OFJAVA and DS_FLOW_MODS_ENTERED have to be enabled)
     */
    @Test
    public void testEnterOFJavaCounter() {
        if(!statCounters.isCounterEnabled(CounterEventTypes.DS_ENTERED_OFJAVA)){
            Assert.fail("Counter " + CounterEventTypes.DS_ENTERED_OFJAVA + " is not enabled");
        }
        if(!statCounters.isCounterEnabled(CounterEventTypes.DS_FLOW_MODS_ENTERED)){
            Assert.fail("Counter " + CounterEventTypes.DS_FLOW_MODS_ENTERED + " is not enabled");
        }
        final EmbeddedChannel embChannel = new EmbeddedChannel(new EmbededChannelHandler());
        adapter = new ConnectionAdapterImpl(embChannel, InetSocketAddress.createUnresolved("localhost", 9876), true);
        cache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES)
                .removalListener(REMOVAL_LISTENER).build();
        adapter.setResponseCache(cache);
        adapter.barrier(barrierInput);
        embChannel.runPendingTasks();
        adapter.echo(echoInput);
        embChannel.runPendingTasks();
        adapter.echoReply(echoReplyInput);
        embChannel.runPendingTasks();
        adapter.experimenter(experimenterInput);
        embChannel.runPendingTasks();
        adapter.flowMod(flowModInput);
        embChannel.runPendingTasks();
        adapter.getConfig(getConfigInput);
        embChannel.runPendingTasks();
        adapter.getFeatures(getFeaturesInput);
        embChannel.runPendingTasks();
        adapter.getQueueConfig(getQueueConfigInput);
        embChannel.runPendingTasks();
        adapter.groupMod(groupModInput);
        embChannel.runPendingTasks();
        adapter.hello(helloInput);
        embChannel.runPendingTasks();
        adapter.meterMod(meterModInput);
        embChannel.runPendingTasks();
        adapter.packetOut(packetOutInput);
        embChannel.runPendingTasks();
        adapter.multipartRequest(multipartRequestInput);
        embChannel.runPendingTasks();
        adapter.portMod(portModInput);
        embChannel.runPendingTasks();
        adapter.roleRequest(roleRequestInput);
        embChannel.runPendingTasks();
        adapter.setConfig(setConfigInput);
        embChannel.runPendingTasks();
        adapter.tableMod(tableModInput);
        embChannel.runPendingTasks();
        adapter.getAsync(getAsyncInput);
        embChannel.runPendingTasks();
        adapter.setAsync(setAsyncInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - bad counter value for ConnectionAdapterImpl rpc methods", 19, statCounters.getCounter(CounterEventTypes.DS_ENTERED_OFJAVA).getCounterValue());
        Assert.assertEquals("Wrong - bad counter value for ConnectionAdapterImpl flow-mod entered", 1, statCounters.getCounter(CounterEventTypes.DS_FLOW_MODS_ENTERED).getCounterValue());
        adapter.disconnect();
    }

    /**
     * Test counter for pass messages to consumer (counter US_MESSAGE_PASS has to be enabled)
     */
    @Test
    public void testMessagePassCounter() {
        if(!statCounters.isCounterEnabled(CounterEventTypes.US_MESSAGE_PASS)){
            Assert.fail("Counter " + CounterEventTypes.US_MESSAGE_PASS + " is not enabled");
        }
        when(channel.pipeline()).thenReturn(pipeline);
        adapter = new ConnectionAdapterImpl(channel, InetSocketAddress.createUnresolved("10.0.0.1", 6653), true);
        adapter.setMessageListener(messageListener);
        adapter.setSystemListener(systemListener);
        adapter.setConnectionReadyListener(readyListener);
        cache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES)
                .removalListener(REMOVAL_LISTENER).build();
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
        Assert.assertEquals("Wrong - bad counter value for ConnectionAdapterImpl consume method", 9, statCounters.getCounter(CounterEventTypes.US_MESSAGE_PASS).getCounterValue());
        adapter.disconnect();
    }

    /**
     * Empty channel Handler for testing
     * @author madamjak
     *
     */
    private class EmbededChannelHandler extends ChannelOutboundHandlerAdapter {
        // no operation need to test
    }
}
