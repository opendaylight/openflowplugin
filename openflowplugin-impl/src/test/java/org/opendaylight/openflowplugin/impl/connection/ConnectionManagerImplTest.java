/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import static org.mockito.ArgumentMatchers.any;

import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter.MessageListener;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.impl.util.ThreadPoolLoggingExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint32Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfigBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test of {@link ConnectionManagerImpl} - lightweight version, using basic ways (TDD).
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionManagerImplTest {

    // timeout of final step [ms]
    private static final int FINAL_STEP_TIMEOUT = 500;
    private ConnectionManagerImpl connectionManagerImpl;
    @Mock
    private ConnectionAdapter connection;
    @Mock
    private DeviceConnectedHandler deviceConnectedHandler;
    @Mock
    private NotificationPublishService notificationPublishService;
    @Captor
    private ArgumentCaptor<ConnectionReadyListener> connectionReadyListenerAC;
    @Captor
    private ArgumentCaptor<MessageListener> ofpListenerAC;
    @Mock
    DataBroker dataBroker;

    private static final Uint32 ECHO_REPLY_TIMEOUT = Uint32.valueOf(500);
    private static final Uint16 DEVICE_CONNECTION_RATE_LIMIT_PER_MIN = Uint16.ZERO;
    private static final Uint16 DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS = Uint16.valueOf(60);

    @Before
    public void setUp() {
        final ThreadPoolLoggingExecutor threadPool = new ThreadPoolLoggingExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(), "ofppool");

        connectionManagerImpl = new ConnectionManagerImpl(new OpenflowProviderConfigBuilder()
                .setEchoReplyTimeout(new NonZeroUint32Type(ECHO_REPLY_TIMEOUT))
                .setDeviceConnectionRateLimitPerMin(DEVICE_CONNECTION_RATE_LIMIT_PER_MIN)
                .setDeviceConnectionHoldTimeInSeconds(DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS)
                .build(), threadPool, dataBroker, notificationPublishService);

        connectionManagerImpl.setDeviceConnectedHandler(deviceConnectedHandler);
        final InetSocketAddress deviceAddress = InetSocketAddress.createUnresolved("yahoo", 42);
        Mockito.when(connection.getRemoteAddress()).thenReturn(deviceAddress);
        Mockito.when(connection.isAlive()).thenReturn(true);
        Mockito.when(connection.barrier(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new BarrierOutputBuilder().build()).buildFuture());
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(200L);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl#onSwitchConnected(
     * org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)}.
     * invoking onConnectionReady first, scenario:
     * <ol>
     * <li>send hello to device (rpc with void output)</li>
     * <li>receive hello from device (notification)</li>
     * <li>send getFeature to device (rpc with getFeatureOutput)</li>
     * <li>wait for rpc to finish with getFeatureOutput</li>
     * </ol>
     *
     * @throws InterruptedException - interrupted exception
     */
    @Test
    public void testOnSwitchConnected1() throws Exception {
        connectionManagerImpl.onSwitchConnected(connection);
        Mockito.verify(connection).setConnectionReadyListener(connectionReadyListenerAC.capture());
        Mockito.verify(connection).setMessageListener(ofpListenerAC.capture());

        // prepare void reply (hello rpc output)
        final SettableFuture<RpcResult<HelloOutput>> voidResponseFx = SettableFuture.create();
        Mockito.when(connection.hello(any(HelloInput.class))).thenReturn(voidResponseFx);
        // prepare getFeature reply (getFeture rpc output)
        final SettableFuture<RpcResult<GetFeaturesOutput>> featureResponseFx =
                SettableFuture.create();
        Mockito.when(connection.getFeatures(any(GetFeaturesInput.class))).thenReturn(featureResponseFx);


        // fire handshake
        connectionReadyListenerAC.getValue().onConnectionReady();

        // deliver hello send output (void)
        Thread.sleep(100L);
        final RpcResult<HelloOutput> helloResponse = RpcResultBuilder.success((HelloOutput) null).build();
        voidResponseFx.set(helloResponse);

        //set dpn last connected time to be before dpn hold time seconds from now
        connectionManagerImpl.getDeviceConnectionStatusProvider().addDeviceLastConnectionTime(BigInteger.TEN,
                LocalDateTime.now().minusSeconds(DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS.toJava()));

        // send hello reply
        final HelloMessage hello = new HelloMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(Uint32.ONE)
                .build();
        ofpListenerAC.getValue().onHello(hello);

        // deliver getFeature output
        Thread.sleep(100L);
        final GetFeaturesOutput getFeatureOutput = new GetFeaturesOutputBuilder()
                .setDatapathId(Uint64.TEN)
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(Uint32.TWO)
                .setTables(Uint8.valueOf(15))
                .build();
        final RpcResult<GetFeaturesOutput> rpcFeaturesOutput = RpcResultBuilder.success(getFeatureOutput).build();
        featureResponseFx.set(rpcFeaturesOutput);

        Mockito.verify(deviceConnectedHandler,
                Mockito.timeout(500)).deviceConnected(any(ConnectionContext.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl#onSwitchConnected(
     * org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)}.
     * invoking onHelloMessage, scenario:
     * <ol>
     * <li>receive hello from device (notification)</li>
     * <li>send hello to device (rpc with void output)</li>
     * <li>send getFeature to device (rpc with getFeatureOutput)</li>
     * <li>wait for rpc to finish with getFeatureOutput</li>
     * </ol>
     *
     * @throws InterruptedException - interrupted exception
     */
    @Test
    public void testOnSwitchConnected2() throws Exception {
        connectionManagerImpl.onSwitchConnected(connection);
        Mockito.verify(connection).setConnectionReadyListener(connectionReadyListenerAC.capture());
        Mockito.verify(connection).setMessageListener(ofpListenerAC.capture());

        // prepare void reply (hello rpc output)
        final SettableFuture<RpcResult<HelloOutput>> voidResponseFx = SettableFuture.create();
        Mockito.when(connection.hello(any(HelloInput.class))).thenReturn(voidResponseFx);
        // prepare getFeature reply (getFeture rpc output)
        final SettableFuture<RpcResult<GetFeaturesOutput>> featureResponseFx =
                SettableFuture.create();
        Mockito.when(connection.getFeatures(any(GetFeaturesInput.class))).thenReturn(featureResponseFx);

        //set dpn last connected time to be before dpn hold time seconds from now
        connectionManagerImpl.getDeviceConnectionStatusProvider().addDeviceLastConnectionTime(BigInteger.TEN,
                LocalDateTime.now().minusSeconds(DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS.toJava()));

        // fire handshake - send hello reply
        final HelloMessage hello = new HelloMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(Uint32.ONE)
                .build();
        ofpListenerAC.getValue().onHello(hello);

        // notify about connection ready
        connectionReadyListenerAC.getValue().onConnectionReady();

        // deliver hello send output (void)
        Thread.sleep(100L);
        final RpcResult<HelloOutput> helloResponse = RpcResultBuilder.success((HelloOutput) null).build();
        voidResponseFx.set(helloResponse);

        // deliver getFeature output
        Thread.sleep(100L);
        final GetFeaturesOutput getFeatureOutput = new GetFeaturesOutputBuilder()
                .setDatapathId(Uint64.TEN)
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(Uint32.TWO)
                .setTables(Uint8.valueOf(15))
                .build();
        final RpcResult<GetFeaturesOutput> rpcFeaturesOutput = RpcResultBuilder.success(getFeatureOutput).build();
        featureResponseFx.set(rpcFeaturesOutput);

        Mockito.verify(deviceConnectedHandler,
                Mockito.timeout(FINAL_STEP_TIMEOUT)).deviceConnected(any(ConnectionContext.class));
    }
}
