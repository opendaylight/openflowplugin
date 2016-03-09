/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import org.junit.After;
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
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * test of {@link ConnectionManagerImpl} - lightweight version, using basic ways (TDD)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionManagerImplTest {

    /** timeout of final step [ms] */
    private static final int FINAL_STEP_TIMEOUT = 500;
    private ConnectionManagerImpl connectionManagerImpl;
    @Mock
    private ConnectionAdapter connection;
    @Mock
    private DeviceConnectedHandler deviceConnectedHandler;
    @Captor
    private ArgumentCaptor<ConnectionReadyListener> connectionReadyListenerAC;
    @Captor
    private ArgumentCaptor<OpenflowProtocolListener> ofpListenerAC;

    private final static int ECHO_REPLY_TIMEOUT = 500;

    /**
     * before each test method
     */
    @Before
    public void setUp() {
        connectionManagerImpl = new ConnectionManagerImpl(ECHO_REPLY_TIMEOUT);
        connectionManagerImpl.setDeviceConnectedHandler(deviceConnectedHandler);
        final InetSocketAddress deviceAddress = InetSocketAddress.createUnresolved("yahoo", 42);
        Mockito.when(connection.getRemoteAddress()).thenReturn(deviceAddress);
        Mockito.when(connection.isAlive()).thenReturn(true);
        Mockito.when(connection.barrier(Matchers.<BarrierInput>any()))
                .thenReturn(RpcResultBuilder.success(new BarrierOutputBuilder().build()).buildFuture());
    }

    /**
     * after each test method
     * @throws InterruptedException
     */
    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(200L);
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl#onSwitchConnected(org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)}.
     * invoking onConnectionReady first, scenario:
     * <ol>
     *  <li>send hello to device (rpc with void output)</li>
     *  <li>receive hello from device (notification)</li>
     *  <li>send getFeature to device (rpc with getFeatureOutput)</li>
     *  <li>wait for rpc to finish with getFeatureOutput</li>
     * </ol>
     * @throws InterruptedException
     */
    @Test
    public void testOnSwitchConnected1() throws Exception {
        connectionManagerImpl.onSwitchConnected(connection);
        Mockito.verify(connection).setConnectionReadyListener(connectionReadyListenerAC.capture());
        Mockito.verify(connection).setMessageListener(ofpListenerAC.capture());

        // prepare void reply (hello rpc output)
        final SettableFuture<RpcResult<Void>> voidResponseFx = SettableFuture.<RpcResult<Void>>create();
        Mockito.when(connection.hello(Matchers.any(HelloInput.class))).thenReturn(voidResponseFx);
        // prepare getFeature reply (getFeture rpc output)
        final SettableFuture<RpcResult<GetFeaturesOutput>> featureResponseFx = SettableFuture.<RpcResult<GetFeaturesOutput>>create();
        Mockito.when(connection.getFeatures(Matchers.any(GetFeaturesInput.class))).thenReturn(featureResponseFx);


        // fire handshake
        connectionReadyListenerAC.getValue().onConnectionReady();

        // deliver hello send output (void)
        Thread.sleep(100L);
        final RpcResult<Void> helloResponse = RpcResultBuilder.success((Void) null).build();
        voidResponseFx.set(helloResponse);

        // send hello reply
        final HelloMessage hello = new HelloMessageBuilder().setVersion(OFConstants.OFP_VERSION_1_3).setXid(1L).build();
        ofpListenerAC.getValue().onHelloMessage(hello);

        // deliver getFeature output
        Thread.sleep(100L);
        final GetFeaturesOutput getFeatureOutput = new GetFeaturesOutputBuilder()
        .setDatapathId(BigInteger.TEN)
        .setVersion(OFConstants.OFP_VERSION_1_3)
        .setXid(2L)
        .setTables((short) 15)
        .build();
        final RpcResult<GetFeaturesOutput> rpcFeaturesOutput = RpcResultBuilder.success(getFeatureOutput).build();
        featureResponseFx.set(rpcFeaturesOutput);

        Mockito.verify(deviceConnectedHandler, Mockito.timeout(500)).deviceConnected(Matchers.any(ConnectionContext.class));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl#onSwitchConnected(org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter)}.
     * invoking onHelloMessage, scenario:
     * <ol>
     *  <li>receive hello from device (notification)</li>
     *  <li>send hello to device (rpc with void output)</li>
     *  <li>send getFeature to device (rpc with getFeatureOutput)</li>
     *  <li>wait for rpc to finish with getFeatureOutput</li>
     * </ol>
     * @throws InterruptedException
     */
    @Test
    public void testOnSwitchConnected2() throws Exception {
        connectionManagerImpl.onSwitchConnected(connection);
        Mockito.verify(connection).setConnectionReadyListener(connectionReadyListenerAC.capture());
        Mockito.verify(connection).setMessageListener(ofpListenerAC.capture());

        // prepare void reply (hello rpc output)
        final SettableFuture<RpcResult<Void>> voidResponseFx = SettableFuture.<RpcResult<Void>>create();
        Mockito.when(connection.hello(Matchers.any(HelloInput.class))).thenReturn(voidResponseFx);
        // prepare getFeature reply (getFeture rpc output)
        final SettableFuture<RpcResult<GetFeaturesOutput>> featureResponseFx = SettableFuture.<RpcResult<GetFeaturesOutput>>create();
        Mockito.when(connection.getFeatures(Matchers.any(GetFeaturesInput.class))).thenReturn(featureResponseFx);


        // fire handshake - send hello reply
        final HelloMessage hello = new HelloMessageBuilder().setVersion(OFConstants.OFP_VERSION_1_3).setXid(1L).build();
        ofpListenerAC.getValue().onHelloMessage(hello);

        // notify about connection ready
        connectionReadyListenerAC.getValue().onConnectionReady();

        // deliver hello send output (void)
        Thread.sleep(100L);
        final RpcResult<Void> helloResponse = RpcResultBuilder.success((Void) null).build();
        voidResponseFx.set(helloResponse);

        // deliver getFeature output
        Thread.sleep(100L);
        final GetFeaturesOutput getFeatureOutput = new GetFeaturesOutputBuilder()
        .setDatapathId(BigInteger.TEN)
        .setVersion(OFConstants.OFP_VERSION_1_3)
        .setXid(2L)
        .setTables((short) 15)
        .build();
        final RpcResult<GetFeaturesOutput> rpcFeaturesOutput = RpcResultBuilder.success(getFeatureOutput).build();
        featureResponseFx.set(rpcFeaturesOutput);

        Mockito.verify(deviceConnectedHandler, Mockito.timeout(FINAL_STEP_TIMEOUT)).deviceConnected(Matchers.any(ConnectionContext.class));
    }
}
