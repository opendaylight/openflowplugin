/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.impl.core.UdpChannelInitializer;
import org.opendaylight.openflowjava.protocol.impl.core.UdpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author madamjak
 *
 */
public class UdpHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(UdpHandlerTest.class);

    @Mock
    private UdpChannelInitializer udpChannelInitializerMock;
    private UdpHandler udpHandler;
    /**
     * Mock init
     */
    @Before
    public void startUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test to create UdpHandler with empty address and zero port
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Test
    public void testWithEmptyAddress() throws Exception {
        udpHandler = new UdpHandler(null, 0);
        udpHandler.setChannelInitializer(udpChannelInitializerMock);
        Assert.assertTrue("Wrong - start server", startupServer(false));
        try {
            Assert.assertTrue(udpHandler.getIsOnlineFuture().get(1500, TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
            Assert.fail("Wrong - getIsOnlineFuture timed out");
        }
        Assert.assertFalse("Wrong - port has been set to zero", udpHandler.getPort() == 0);
        shutdownServer();
    }

    /**
     * Test to create UdpHandler with empty address and zero port on Epoll native transport
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Test
    public void testWithEmptyAddressOnEpoll() throws Exception {
        udpHandler = new UdpHandler(null, 0);
        udpHandler.setChannelInitializer(udpChannelInitializerMock);
        Assert.assertTrue("Wrong - start server", startupServer(true));
        try {
            Assert.assertTrue(udpHandler.getIsOnlineFuture().get(1500,TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
            Assert.fail("Wrong - getIsOnlineFuture timed out");
        }
        Assert.assertFalse("Wrong - port has been set to zero", udpHandler.getPort() == 0);
        shutdownServer();
    }

    /**
     * Test to create UdpHandler with fill address and given port
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Test
    public void testWithAddressAndPort() throws Exception{
        int port = 9874;
        udpHandler = new UdpHandler(InetAddress.getLocalHost(), port);
        udpHandler.setChannelInitializer(udpChannelInitializerMock);
        Assert.assertTrue("Wrong - start server", startupServer(false));
        try {
            Assert.assertTrue(udpHandler.getIsOnlineFuture().get(1500,TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
            Assert.fail("Wrong - getIsOnlineFuture timed out");
        }
        Assert.assertEquals("Wrong - bad port number has been set", port, udpHandler.getPort());
        shutdownServer();
    }

    /**
     * Test to create UdpHandler with fill address and given port on Epoll native transport
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Test
    public void testWithAddressAndPortOnEpoll() throws Exception {
        int port = 9874;
        udpHandler = new UdpHandler(InetAddress.getLocalHost(), port);
        udpHandler.setChannelInitializer(udpChannelInitializerMock);
        Assert.assertTrue("Wrong - start server", startupServer(true));
        try {
            Assert.assertTrue(udpHandler.getIsOnlineFuture().get(1500,TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
            Assert.fail("Wrong - getIsOnlineFuture timed out");
        }
        Assert.assertEquals("Wrong - bad port number has been set", port, udpHandler.getPort());
        shutdownServer();
    }

    private Boolean startupServer(final boolean isEpollEnabled) throws InterruptedException, IOException, ExecutionException {
        ListenableFuture<Boolean> online = udpHandler.getIsOnlineFuture();
        /**
         * Test EPoll based native transport if isEpollEnabled is true.
         * Else use Nio based transport.
         */
        udpHandler.initiateEventLoopGroups(null, isEpollEnabled);
        (new Thread(udpHandler)).start();

        boolean startedSuccessfully = false;
        try {
            startedSuccessfully = online.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            LOG.warn("Timeout while waiting for UDP handler to start", e);
        }

        return online.isDone();
    }

    private void shutdownServer() throws InterruptedException, ExecutionException, TimeoutException {
        ListenableFuture<Boolean> shutdownRet = udpHandler.shutdown() ;
        final Boolean shutdownSucceeded = shutdownRet.get(10, TimeUnit.SECONDS);
        Assert.assertTrue("Wrong - shutdown failed", shutdownSucceeded);
    }
}
