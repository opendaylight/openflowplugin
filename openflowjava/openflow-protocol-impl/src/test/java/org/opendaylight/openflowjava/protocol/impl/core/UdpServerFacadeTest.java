/*
 * Copyright (c) 2014 Pantheon Technologies, s.r.o. and others. All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doReturn;

import java.net.InetAddress;
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

@ExtendWith(MockitoExtension.class)
class UdpServerFacadeTest {
    @Mock
    private ConnectionConfiguration connConfig;
    @Mock
    private UdpChannelInitializer udpChannelInitializerMock;
    private UdpServerFacade udpHandler;

    @AfterEach
    void afterEach() throws Exception {
        if (udpHandler != null) {
            udpHandler.shutdown().get(10, TimeUnit.SECONDS);
        }
    }

    /**
     * Test to create UdpHandler with empty address and zero port.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testWithEmptyAddress(final boolean epollEnabled) {
        udpHandler = assertFacade(null, 0, epollEnabled);
        assertNotEquals(0, udpHandler.localAddress().getPort());
    }


    /**
     * Test to create UdpHandler with fill address and given port.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testWithAddressAndPort(final boolean epollEnabled) throws Exception {
        final int port = 9874;
        udpHandler = assertFacade(InetAddress.getLocalHost(), port, epollEnabled);
        assertEquals(port, udpHandler.localAddress().getPort());
    }

    private UdpServerFacade assertFacade(final InetAddress address, final int port, final boolean epollEnabled) {
        doReturn(address).when(connConfig).getAddress();
        doReturn(port).when(connConfig).getPort();

        final var future = UdpServerFacade.start(connConfig, epollEnabled, udpChannelInitializerMock);
        try {
            return future.get(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new AssertionError(e);
        }
    }
}
