/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.impl.core.UdpChannelInitializer;
import org.opendaylight.openflowjava.protocol.impl.core.UdpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for UdpHandler.
 *
 * @author madamjak
 */
@RunWith(MockitoJUnitRunner.class)
public class UdpHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(UdpHandlerTest.class);

    @Mock
    private UdpChannelInitializer udpChannelInitializerMock;
    private UdpHandler udpHandler;

    /**
     * Test to create UdpHandler with empty address and zero port.
     */
    @Test
    public void testWithEmptyAddress() throws Exception {
        udpHandler = new UdpHandler(null, 0, () -> { });
        udpHandler.setChannelInitializer(udpChannelInitializerMock);
        assertTrue("Wrong - start server", startupServer(false));
        udpHandler.getIsOnlineFuture().get(1500, TimeUnit.MILLISECONDS);
        assertFalse("Wrong - port has been set to zero", udpHandler.getPort() == 0);
        shutdownServer();
    }

    /**
     * Test to create UdpHandler with empty address and zero port on Epoll native transport.
     */
    @Test
    public void testWithEmptyAddressOnEpoll() throws Exception {
        udpHandler = new UdpHandler(null, 0, () -> { });
        udpHandler.setChannelInitializer(udpChannelInitializerMock);
        assertTrue("Wrong - start server", startupServer(true));
        udpHandler.getIsOnlineFuture().get(1500,TimeUnit.MILLISECONDS);
        assertFalse("Wrong - port has been set to zero", udpHandler.getPort() == 0);
        shutdownServer();
    }

    /**
     * Test to create UdpHandler with fill address and given port.
     */
    @Test
    public void testWithAddressAndPort() throws Exception {
        int port = 9874;
        udpHandler = new UdpHandler(InetAddress.getLocalHost(), port, () -> { });
        udpHandler.setChannelInitializer(udpChannelInitializerMock);
        assertTrue("Wrong - start server", startupServer(false));
        udpHandler.getIsOnlineFuture().get(1500,TimeUnit.MILLISECONDS);
        assertEquals("Wrong - bad port number has been set", port, udpHandler.getPort());
        shutdownServer();
    }

    /**
     * Test to create UdpHandler with fill address and given port on Epoll native transport.
     */
    @Test
    public void testWithAddressAndPortOnEpoll() throws Exception {
        int port = 9874;
        udpHandler = new UdpHandler(InetAddress.getLocalHost(), port, () -> { });
        udpHandler.setChannelInitializer(udpChannelInitializerMock);
        assertTrue("Wrong - start server", startupServer(true));
        udpHandler.getIsOnlineFuture().get(1500,TimeUnit.MILLISECONDS);
        assertEquals("Wrong - bad port number has been set", port, udpHandler.getPort());
        shutdownServer();
    }

    private Boolean startupServer(final boolean isEpollEnabled)
            throws InterruptedException, ExecutionException {
        final var online = udpHandler.getIsOnlineFuture();
        /**
         * Test EPoll based native transport if isEpollEnabled is true.
         * Else use Nio based transport.
         */
        udpHandler.initiateEventLoopGroups(null, isEpollEnabled);
        new Thread(udpHandler).start();

        try {
            online.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            LOG.warn("Timeout while waiting for UDP handler to start", e);
        }

        return online.isDone();
    }

    private void shutdownServer() throws InterruptedException, ExecutionException, TimeoutException {
        final var shutdownRet = udpHandler.shutdown() ;
        assertTrue("Wrong - shutdown failed", shutdownRet.get(10, TimeUnit.SECONDS));
    }
}
