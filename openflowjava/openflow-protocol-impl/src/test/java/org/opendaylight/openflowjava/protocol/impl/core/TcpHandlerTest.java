/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.util.concurrent.ListenableFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.unix.Errors;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;

/**
 * Unit tests for TcpHandler.
 *
 * @author jameshall
 */
@RunWith(MockitoJUnitRunner.class)
public class TcpHandlerTest {

    private final InetAddress serverAddress = InetAddress.getLoopbackAddress() ;
    @Mock ChannelHandlerContext mockChHndlrCtx ;
    @Mock TcpChannelInitializer mockChannelInitializer;
    @Mock SwitchConnectionHandler mockSwitchConnHndler ;
    @Mock SerializationFactory mockSerializationFactory ;
    @Mock DeserializationFactory mockDeserializationFactory ;

    TcpHandler tcpHandler ;

    /**
     * Test run with null address set.
     */
    @Test
    public void testRunWithNullAddress() throws IOException, InterruptedException, ExecutionException  {
        tcpHandler = new TcpHandler(null, 0, () -> { });
        tcpHandler.setChannelInitializer(mockChannelInitializer);

        assertEquals("failed to start server", true, startupServer(false)) ;
        assertEquals("failed to connect client", true, clientConnection(tcpHandler.getPort())) ;
        shutdownServer();
    }

    /**
     * Test run with null address set on Epoll native transport.
     */
    @Test
    public void testRunWithNullAddressOnEpoll() throws IOException, InterruptedException, ExecutionException  {
        tcpHandler = new TcpHandler(null, 0, () -> { });
        tcpHandler.setChannelInitializer(mockChannelInitializer);

        //Use Epoll native transport
        assertEquals("failed to start server", true, startupServer(true)) ;
        assertEquals("failed to connect client", true, clientConnection(tcpHandler.getPort())) ;
        shutdownServer();
    }

    /**
     * Test run with address set.
     */
    @Test
    public void testRunWithAddress() throws IOException, InterruptedException, ExecutionException  {
        tcpHandler = new TcpHandler(serverAddress, 0, () -> { });
        tcpHandler.setChannelInitializer(mockChannelInitializer);

        assertEquals("failed to start server", true, startupServer(false)) ;
        assertEquals("failed to connect client", true, clientConnection(tcpHandler.getPort())) ;
        shutdownServer();
    }

    /**
     * Test run with address set on Epoll native transport.
     */
    @Test
    public void testRunWithAddressOnEpoll() throws IOException, InterruptedException, ExecutionException  {
        tcpHandler = new TcpHandler(serverAddress, 0, () -> { });
        tcpHandler.setChannelInitializer(mockChannelInitializer);

        //Use Epoll native transport
        assertEquals("failed to start server", true, startupServer(true));
        assertEquals("failed to connect client", true, clientConnection(tcpHandler.getPort()));
        shutdownServer();
    }

    /**
     * Test run with encryption.
     */
    @Test
    public void testRunWithEncryption() throws InterruptedException, IOException, ExecutionException {
        int serverPort = 28001;
        tcpHandler = new TcpHandler(serverAddress, serverPort, () -> { });
        tcpHandler.setChannelInitializer(mockChannelInitializer);

        assertEquals("failed to start server", true, startupServer(false));
        assertEquals("wrong connection count", 0, tcpHandler.getNumberOfConnections());
        assertEquals("wrong port", serverPort, tcpHandler.getPort());
        assertEquals("wrong address", serverAddress.getHostAddress(), tcpHandler.getAddress());

        assertEquals("failed to connect client", true, clientConnection(tcpHandler.getPort()));

        shutdownServer();
    }

    /**
     * Test run with encryption on Epoll native transport.
     */
    @Test
    public void testRunWithEncryptionOnEpoll() throws InterruptedException, IOException, ExecutionException {
        int serverPort = 28001;
        tcpHandler = new TcpHandler(serverAddress, serverPort, () -> { });
        tcpHandler.setChannelInitializer(mockChannelInitializer);

        //Use Epoll native transport
        assertEquals("failed to start server", true, startupServer(true));
        assertEquals("wrong connection count", 0, tcpHandler.getNumberOfConnections());
        assertEquals("wrong port", serverPort, tcpHandler.getPort());
        assertEquals("wrong address", serverAddress.getHostAddress(), tcpHandler.getAddress());

        assertEquals("failed to connect client", true, clientConnection(tcpHandler.getPort()));

        shutdownServer();
    }

    /**
     * Test run on already used port.
     */
    @Test(expected = BindException.class)
    public void testSocketAlreadyInUse() throws IOException {
        int serverPort = 28001;
        Socket firstBinder = new Socket();

        try (firstBinder) {
            firstBinder.bind(new InetSocketAddress(serverAddress, serverPort));
            tcpHandler = new TcpHandler(serverAddress, serverPort, () -> { });
            tcpHandler.setChannelInitializer(mockChannelInitializer);
            tcpHandler.initiateEventLoopGroups(null, false);
            tcpHandler.run();
        }
    }

    /**
     * Test run on already used port.
     */
    @Test
    public void testSocketAlreadyInUseOnEpoll() throws IOException {
        int serverPort = 28001;
        Socket firstBinder = new Socket();

        try (firstBinder) {
            firstBinder.bind(new InetSocketAddress(serverAddress, serverPort));

            tcpHandler = new TcpHandler(serverAddress, serverPort, () -> { });
            tcpHandler.setChannelInitializer(mockChannelInitializer);
            //Use Epoll native transport
            tcpHandler.initiateEventLoopGroups(null, true);
            tcpHandler.run();
            fail("Expected BindException or Errors.NativeIoException");
        } catch (BindException | Errors.NativeIoException e) {
            // expected
        }
    }

    /**
     * Trigger the server shutdown and wait 2 seconds for completion.
     */
    private void shutdownServer() throws InterruptedException, ExecutionException {
        ListenableFuture<Boolean> shutdownRet = tcpHandler.shutdown() ;
        while (shutdownRet.isDone() != true) {
            Thread.sleep(100) ;
        }
        assertEquals("shutdown failed", true, shutdownRet.get());
    }

    private Boolean startupServer(final boolean isEpollEnabled) throws InterruptedException {
        final var online = tcpHandler.getIsOnlineFuture();
        /**
         * Test EPoll based native transport if isEpollEnabled is true.
         * Else use Nio based transport.
         */
        tcpHandler.initiateEventLoopGroups(null, isEpollEnabled);
        new Thread(tcpHandler).start();
        int retry = 0;
        while (online.isDone() != true && retry++ < 20) {
            Thread.sleep(100);
        }
        return online.isDone();
    }

    private static Boolean clientConnection(final int port) throws IOException {
        // Connect, and disconnect
        Socket socket = new Socket(InetAddress.getLoopbackAddress(), port);
        Boolean result = socket.isConnected();
        socket.close() ;
        return result ;
    }
}
