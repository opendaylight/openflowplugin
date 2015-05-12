/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.listener;

import static org.junit.Assert.*;

import org.opendaylight.openflowplugin.impl.connection.HandshakeContextImpl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import java.net.InetSocketAddress;
import java.util.List;
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
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.impl.connection.ConnectionContextImpl;
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

    public static final int SAFE_TIMEOUT = 1000;
    @Mock
    private org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter connectionAdapter;
    @Mock
    private FeaturesReply features;
    private ConnectionContext connectionContext;
    @Captor
    private ArgumentCaptor<ConnectionContext.CONNECTION_STATE> connectionStateArgumentCaptor;

    private SystemNotificationsListenerImpl systemNotificationsListener;
    private ConnectionContextImpl connectionContextGolem;

    @Before
    public void setUp() {
        connectionContextGolem = new ConnectionContextImpl(connectionAdapter);
        connectionContextGolem.setConnectionState(ConnectionContext.CONNECTION_STATE.WORKING);

        Mockito.when(connectionAdapter.getRemoteAddress()).thenReturn(
                InetSocketAddress.createUnresolved("unit-odl.example.org", 4242));
        connectionContext = Mockito.spy(connectionContextGolem);
        Mockito.when(features.getAuxiliaryId()).thenReturn((short) 0);

        Mockito.when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionContext.getFeatures()).thenReturn(features);

        ThreadPoolLoggingExecutor threadPoolLoggingExecutor = new ThreadPoolLoggingExecutor(2000, 2000, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(20), "OFHandshake-test identifier");

        systemNotificationsListener = new SystemNotificationsListenerImpl(connectionContext,
               new HandshakeContextImpl(threadPoolLoggingExecutor, null));
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

        verifyCommonInvocations();
        Mockito.verify(connectionAdapter).disconnect();
        Mockito.verify(connectionContext).setConnectionState(ConnectionContext.CONNECTION_STATE.RIP);
        Mockito.verify(connectionContext).propagateClosingConnection();
        assertTrue(systemNotificationsListener.handshakeContext.getHandshakePool().isTerminated());
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

        verifyCommonInvocations();
        Mockito.verify(connectionAdapter).disconnect();
        Mockito.verify(connectionContext).setConnectionState(ConnectionContext.CONNECTION_STATE.RIP);
        Mockito.verify(connectionContext).propagateClosingConnection();
        assertTrue(systemNotificationsListener.handshakeContext.getHandshakePool().isTerminated());
    }

    /**
     * successful scenario - connection is already down
     *
     * @throws Exception
     */
    @Test
    public void testOnDisconnectEvent3() throws Exception {
        connectionContextGolem.setConnectionState(ConnectionContext.CONNECTION_STATE.TIMEOUTING);

        Mockito.when(connectionAdapter.isAlive()).thenReturn(true);
        Mockito.when(connectionAdapter.disconnect()).thenReturn(Futures.<Boolean>immediateFailedFuture(new Exception("unit exception")));

        DisconnectEvent disconnectNotification = new DisconnectEventBuilder().setInfo("testing disconnect").build();
        systemNotificationsListener.onDisconnectEvent(disconnectNotification);

        verifyCommonInvocations();
        Mockito.verify(connectionAdapter).disconnect();
        Mockito.verify(connectionContext).setConnectionState(ConnectionContext.CONNECTION_STATE.RIP);
        Mockito.verify(connectionContext).propagateClosingConnection();
        assertTrue(systemNotificationsListener.handshakeContext.getHandshakePool().isTerminated());
    }

    /**
     * broken scenario - connection is on but throws error on close
     *
     * @throws Exception
     */
    @Test
    public void testOnDisconnectEvent4() throws Exception {
        connectionContextGolem.setConnectionState(ConnectionContext.CONNECTION_STATE.RIP);

        Mockito.when(connectionAdapter.isAlive()).thenReturn(false);

        DisconnectEvent disconnectNotification = new DisconnectEventBuilder().setInfo("testing disconnect").build();
        systemNotificationsListener.onDisconnectEvent(disconnectNotification);

        verifyCommonInvocations();
        Mockito.verify(connectionAdapter, Mockito.never()).disconnect();
        Mockito.verify(connectionContext).setConnectionState(ConnectionContext.CONNECTION_STATE.RIP);
        Mockito.verify(connectionContext).propagateClosingConnection();
        assertTrue(systemNotificationsListener.handshakeContext.getHandshakePool().isTerminated());
    }

    /**
     * first encounter of idle event, echo received successfully
     *
     * @throws Exception
     */
    @Test
    public void testOnSwitchIdleEvent1() throws Exception {
        final SettableFuture<RpcResult<EchoOutput>> echoReply = SettableFuture.create();
        Mockito.when(connectionAdapter.echo(Matchers.any(EchoInput.class))).thenReturn(echoReply);

        SwitchIdleEvent notification = new SwitchIdleEventBuilder().setInfo("wake up, device sleeps").build();
        systemNotificationsListener.onSwitchIdleEvent(notification);

        // make sure that the idle notification processing thread started
        Thread.sleep(SAFE_TIMEOUT);
        EchoOutput echoReplyVal = new EchoOutputBuilder().build();
        echoReply.set(RpcResultBuilder.success(echoReplyVal).build());

        Mockito.verify(connectionContext, Mockito.timeout(SAFE_TIMEOUT).times(2))
                .setConnectionState(connectionStateArgumentCaptor.capture());
        List<ConnectionContext.CONNECTION_STATE> allStates = connectionStateArgumentCaptor.getAllValues();
        Assert.assertEquals(2, allStates.size());
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.TIMEOUTING, allStates.get(0));
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.WORKING, allStates.get(1));

        verifyCommonInvocations();
        Mockito.verify(connectionAdapter, Mockito.timeout(SAFE_TIMEOUT)).echo(Matchers.any(EchoInput.class));
        Mockito.verify(connectionContext, Mockito.timeout(SAFE_TIMEOUT)).setConnectionState(ConnectionContext.CONNECTION_STATE.WORKING);
        Mockito.verify(connectionAdapter, Mockito.never()).disconnect();
        assertFalse(systemNotificationsListener.handshakeContext.getHandshakePool().isTerminated());
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
        Mockito.verify(connectionContext, Mockito.timeout(SAFE_TIMEOUT).times(2))
                .setConnectionState(connectionStateArgumentCaptor.capture());
        List<ConnectionContext.CONNECTION_STATE> allStates = connectionStateArgumentCaptor.getAllValues();
        Assert.assertEquals(2, allStates.size());
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.TIMEOUTING, allStates.get(0));
        Assert.assertEquals(ConnectionContext.CONNECTION_STATE.RIP, allStates.get(1));

        Mockito.verify(connectionAdapter).disconnect();
        Mockito.verify(connectionContext).propagateClosingConnection();
        assertTrue(systemNotificationsListener.handshakeContext.getHandshakePool().isTerminated());
    }

    private void verifyCommonInvocations() {
        Mockito.verify(connectionContext, Mockito.timeout(SAFE_TIMEOUT).atLeastOnce()).getConnectionState();
        Mockito.verify(connectionContext, Mockito.timeout(SAFE_TIMEOUT).atLeastOnce()).getFeatures();
        Mockito.verify(connectionContext, Mockito.timeout(SAFE_TIMEOUT).atLeastOnce()).getConnectionAdapter();
    }
}