/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;

@ExtendWith(MockitoExtension.class)
class TcpServerFacadeTest {
    private final InetAddress serverAddress = InetAddress.getLoopbackAddress();

    @Mock
    private ConnectionConfiguration connConfig;
    @Mock
    private ChannelHandlerContext mockChHndlrCtx;
    @Mock
    private TcpChannelInitializer mockChannelInitializer;
    @Mock
    private SwitchConnectionHandler mockSwitchConnHndler;
    @Mock
    private SerializationFactory mockSerializationFactory;
    @Mock
    private DeserializationFactory mockDeserializationFactory;

    private TcpServerFacade tcpHandler;

    @AfterEach
    void afterEach() throws Exception {
        if (tcpHandler != null) {
            tcpHandler.shutdown().get(10, TimeUnit.SECONDS);
        }
    }

    /**
     * Test run with null address set.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testRunWithNullAddress(final boolean epollEnabled) {
        tcpHandler = assertFacade(null, 0, epollEnabled);
        assertTrue(clientConnection(tcpHandler.localAddress().getPort())) ;
    }

    /**
     * Test run with address set.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testRunWithAddress(final boolean epollEnabled) {
        tcpHandler = assertFacade(serverAddress, 0, epollEnabled);
        assertTrue(clientConnection(tcpHandler.localAddress().getPort())) ;
    }

    /**
     * Test run with encryption.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void testRunWithEncryption(final boolean epollEnabled) {
        final int serverPort = 28001;
        tcpHandler = assertFacade(serverAddress, serverPort, epollEnabled);
        assertEquals(0, tcpHandler.getNumberOfConnections());
        assertEquals(serverPort, tcpHandler.localAddress().getPort());
        assertEquals(serverAddress.getHostAddress(), tcpHandler.localAddress().getHostString());

        assertTrue(clientConnection(serverPort));
    }

    /**
     * Test run on already used port.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testSocketAlreadyInUse(final boolean epollEnabled) throws Exception {
        final int serverPort = 28001;

        try (var firstBinder = new Socket()) {
            firstBinder.bind(new InetSocketAddress(serverAddress, serverPort));

            doReturn(serverAddress).when(connConfig).getAddress();
            doReturn(serverPort).when(connConfig).getPort();

            final var future = TcpServerFacade.start(connConfig, epollEnabled, mockChannelInitializer);
            try {
                future.get(1500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new AssertionError(e);
            }
        }
    }

    private TcpServerFacade assertFacade(final InetAddress address, final int port, final boolean epollEnabled) {
        doReturn(address).when(connConfig).getAddress();
        doReturn(port).when(connConfig).getPort();

        final var future = TcpServerFacade.start(connConfig, epollEnabled, mockChannelInitializer);
        try {
            return future.get(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new AssertionError(e);
        }
    }

    private static boolean clientConnection(final int port) {
        // Connect, and disconnect
        try (var socket = new Socket(InetAddress.getLoopbackAddress(), port)) {
            return socket.isConnected();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
