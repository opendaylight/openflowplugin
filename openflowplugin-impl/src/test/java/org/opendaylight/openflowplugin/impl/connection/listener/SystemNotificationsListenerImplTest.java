/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.listener;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEventBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEventBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Testing basic bahavior of {@link SystemNotificationsListenerImpl}
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemNotificationsListenerImplTest {

    private static final int SAFE_TIMEOUT = 1000;
    private final static int ECHO_REPLY_TIMEOUT = 2000;

    @Mock
    private ConnectionAdapter connectionAdapter;
    @Mock
    private FeaturesReply features;

    private ConnectionContext connectionContext;
    private ConnectionContextImpl connectionContextGolem;
    private SystemNotificationsListenerImpl systemNotificationsListener;

    private static final NodeId nodeId =
            new NodeId("OFP:TEST");

    private final ThreadPoolLoggingExecutor threadPool =
            new ThreadPoolLoggingExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), "opfpool");

    @Before
    public void setUp() {
        connectionContextGolem = new ConnectionContextImpl(connectionAdapter);
        connectionContextGolem.changeStateToWorking();
        connectionContextGolem.setNodeId(nodeId);
        connectionContext = Mockito.spy(connectionContextGolem);

        Mockito.when(connectionAdapter.getRemoteAddress()).thenReturn(
                InetSocketAddress.createUnresolved("unit-odl.example.org", 4242));

        Mockito.when(features.getAuxiliaryId()).thenReturn((short) 0);

        Mockito.when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionContext.getFeatures()).thenReturn(features);

        systemNotificationsListener =
                new SystemNotificationsListenerImpl(connectionContext, ECHO_REPLY_TIMEOUT, threadPool);

    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(connectionContext);
    }

    /**
     * successful scenario - connection is on and closes without errors
     *
     * @throws Exception
     */
    @Test
    public void testOnDisconnectEvent1() throws Exception {
        Mockito.when(connectionAdapter.isAlive()).thenReturn(true);
        Mockito.when(connectionAdapter.disconnect()).thenReturn(Futures.immediateFuture(Boolean.TRUE));

        DisconnectEvent disconnectNotification = new DisconnectEventBuilder().setInfo("testing disconnect").build();
        systemNotificationsListener.onDisconnectEvent(disconnectNotification);

        verifyCommonInvocationsSubSet();
        Mockito.verify(connectionContext).onConnectionClosed();
        Mockito.verify(connectionContext).getConnectionAdapter();
        Mockito.verify(connectionContext, Mockito.atLeastOnce()).getSafeNodeIdForLOG();
    }

    /**
     * broken scenario - connection is on but fails to close
     *
     * @throws Exception
     */
    @Test
    public void testOnDisconnectEvent2() throws Exception {
        Mockito.when(connectionAdapter.isAlive()).thenReturn(true);
        Mockito.when(connectionAdapter.disconnect()).thenReturn(Futures.immediateFuture(Boolean.FALSE));

        DisconnectEvent disconnectNotification = new DisconnectEventBuilder().setInfo("testing disconnect").build();
        systemNotificationsListener.onDisconnectEvent(disconnectNotification);

        verifyCommonInvocationsSubSet();
        Mockito.verify(connectionContext).onConnectionClosed();
        Mockito.verify(connectionContext).getConnectionAdapter();
        Mockito.verify(connectionContext, Mockito.atLeastOnce()).getSafeNodeIdForLOG();
    }

    /**
     * successful scenario - connection is already down
     *
     * @throws Exception
     */
    @Test
    public void testOnDisconnectEvent3() throws Exception {
        connectionContextGolem.changeStateToTimeouting();

        Mockito.when(connectionAdapter.isAlive()).thenReturn(true);
        Mockito.when(connectionAdapter.disconnect()).thenReturn(Futures.<Boolean>immediateFailedFuture(new Exception("unit exception")));

        DisconnectEvent disconnectNotification = new DisconnectEventBuilder().setInfo("testing disconnect").build();
        systemNotificationsListener.onDisconnectEvent(disconnectNotification);

        verifyCommonInvocationsSubSet();
        Mockito.verify(connectionContext).onConnectionClosed();
        Mockito.verify(connectionContext).getConnectionAdapter();
        Mockito.verify(connectionContext, Mockito.atLeastOnce()).getSafeNodeIdForLOG();
    }

    /**
     * broken scenario - connection is on but throws error on close
     *
     * @throws Exception
     */
    @Test
    public void testOnDisconnectEvent4() throws Exception {
        Mockito.when(connectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.RIP);
        Mockito.when(connectionAdapter.isAlive()).thenReturn(false);

        DisconnectEvent disconnectNotification = new DisconnectEventBuilder().setInfo("testing disconnect").build();
        systemNotificationsListener.onDisconnectEvent(disconnectNotification);

        verifyCommonInvocationsSubSet();
        Mockito.verify(connectionContext).onConnectionClosed();
        Mockito.verify(connectionContext).getConnectionAdapter();
        Mockito.verify(connectionContext, Mockito.atLeastOnce()).getSafeNodeIdForLOG();
    }

    /**
     * first encounter of idle event, echo received successfully
     *
     * @throws Exception
     */
    @Test
    public void testOnSwitchIdleEvent1() throws Exception {
        final Future<RpcResult<EchoOutput>> echoReply = Futures.immediateFuture(RpcResultBuilder.success(new EchoOutputBuilder().setXid(0L).build()).build());

        Mockito.when(connectionAdapter.echo(Matchers.any(EchoInput.class))).thenReturn(echoReply);

        SwitchIdleEvent notification = new SwitchIdleEventBuilder().setInfo("wake up, device sleeps").build();
        systemNotificationsListener.onSwitchIdleEvent(notification);

        // make sure that the idle notification processing thread started
        Thread.sleep(SAFE_TIMEOUT);

        verifyCommonInvocations();
        Mockito.verify(connectionAdapter, Mockito.timeout(SAFE_TIMEOUT)).echo(Matchers.any(EchoInput.class));
        Mockito.verify(connectionAdapter, Mockito.never()).disconnect();
        Mockito.verify(connectionContext).changeStateToTimeouting();
        Mockito.verify(connectionContext).changeStateToWorking();
    }

    /**
     * first encounter of idle event, echo not receive
     *
     * @throws Exception
     */
    @Test
    public void testOnSwitchIdleEvent2() throws Exception {
        final SettableFuture<RpcResult<EchoOutput>> echoReply = SettableFuture.create();
        Mockito.when(connectionAdapter.echo(Matchers.any(EchoInput.class))).thenReturn(echoReply);
        Mockito.when(connectionAdapter.isAlive()).thenReturn(true);
        Mockito.when(connectionAdapter.disconnect()).thenReturn(Futures.<Boolean>immediateFailedFuture(new Exception("unit exception")));

        SwitchIdleEvent notification = new SwitchIdleEventBuilder().setInfo("wake up, device sleeps").build();
        systemNotificationsListener.onSwitchIdleEvent(notification);

        Thread.sleep(SystemNotificationsListenerImpl.MAX_ECHO_REPLY_TIMEOUT + SAFE_TIMEOUT);

        verifyCommonInvocations();
        Mockito.verify(connectionAdapter, Mockito.timeout(SAFE_TIMEOUT)).echo(Matchers.any(EchoInput.class));
        Mockito.verify(connectionAdapter).disconnect();
        Mockito.verify(connectionContext).changeStateToTimeouting();
        Mockito.verify(connectionContext).closeConnection(true);
        Mockito.verify(connectionContext, Mockito.atLeastOnce()).getSafeNodeIdForLOG();

    }

    private void verifyCommonInvocations() {
        verifyCommonInvocationsSubSet();
        Mockito.verify(connectionContext, Mockito.timeout(SAFE_TIMEOUT).atLeastOnce()).getConnectionAdapter();
    }

    private void verifyCommonInvocationsSubSet() {
        Mockito.verify(connectionContext, Mockito.timeout(SAFE_TIMEOUT).atLeastOnce()).getConnectionState();
        Mockito.verify(connectionContext, Mockito.timeout(SAFE_TIMEOUT).atLeastOnce()).getFeatures();
    }
}