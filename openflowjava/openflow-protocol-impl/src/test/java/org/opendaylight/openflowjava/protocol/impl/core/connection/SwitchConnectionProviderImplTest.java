/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceRegistration;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfigurationImpl;
import org.opendaylight.openflowjava.protocol.impl.core.SwitchConnectionProviderImpl;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;

/**
 * Unit tests for SwitchConnectionProviderImpl.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class SwitchConnectionProviderImplTest {

    @Mock SwitchConnectionHandler handler;
    @Mock DiagStatusService diagStatus;

    private static final int SWITCH_IDLE_TIMEOUT = 2000;
    private static final int WAIT_TIMEOUT = 2000;
    private static final int CHANNEL_OUTBOUND_QUEUE_SIZE = 1024;
    private TlsConfiguration tlsConfiguration;
    private SwitchConnectionProviderImpl provider;
    private ConnectionConfigurationImpl config;

    @Before
    public void before() {
        doReturn(mock(ServiceRegistration.class)).when(diagStatus).register(any());
    }

    /**
     * Creates new {@link SwitchConnectionProvider} instance for each test.
     * @param protocol communication protocol
     */
    public void startUp(final TransportProtocol protocol) throws UnknownHostException {
        config = null;
        if (protocol != null) {
            createConfig(protocol);
        }
        provider = new SwitchConnectionProviderImpl(diagStatus, config);
    }

    private void createConfig(final TransportProtocol protocol) throws UnknownHostException {
        InetAddress startupAddress = InetAddress.getLocalHost();

        tlsConfiguration = null;
        if (protocol.equals(TransportProtocol.TLS)) {
            tlsConfiguration = new TlsConfigurationImpl(KeystoreType.JKS,
                    "/selfSignedSwitch", PathType.CLASSPATH, KeystoreType.JKS,
                    "/selfSignedController", PathType.CLASSPATH,
                    List.of("TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA256"));
        }
        config = new ConnectionConfigurationImpl(startupAddress, 0, tlsConfiguration, SWITCH_IDLE_TIMEOUT, true,
                false, CHANNEL_OUTBOUND_QUEUE_SIZE);
        config.setTransferProtocol(protocol);
    }

    /**
     * Tests provider startup - without configuration and {@link SwitchConnectionHandler}.
     */
    @Test
    public void testStartup1() throws UnknownHostException {
        startUp(null);
        final var future = provider.startup();

        final var cause = assertThrows(ExecutionException.class, () -> future.get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS))
            .getCause();
        assertThat(cause, instanceOf(IllegalStateException.class));
        assertEquals("Connection not configured", cause.getMessage());
    }

    /**
     * Tests provider startup - without configuration.
     */
    @Test
    public void testStartup2() throws UnknownHostException {
        startUp(null);
        provider.setSwitchConnectionHandler(handler);
        final var future = provider.startup();

        final var cause = assertThrows(ExecutionException.class, () -> future.get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS))
            .getCause();
        assertThat(cause, instanceOf(IllegalStateException.class));
        assertEquals("Connection not configured", cause.getMessage());
    }

    /**
     * Tests provider startup - without {@link SwitchConnectionHandler}.
     */
    @Test
    public void testStartup3() throws UnknownHostException {
        startUp(TransportProtocol.TCP);
        final var future = provider.startup();

        final var cause = assertThrows(ExecutionException.class, () -> future.get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS))
            .getCause();
        assertThat(cause, instanceOf(IllegalStateException.class));
        assertEquals("SwitchConnectionHandler is not set", cause.getMessage());
    }

    /**
     * Tests correct provider startup - over TCP.
     */
    @Test
    public void testStartup4() throws Exception {
        startUp(TransportProtocol.TCP);
        provider.setSwitchConnectionHandler(handler);

        provider.startup().get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * Tests correct provider startup - over TLS.
     */
    @Test
    public void testStartup5() throws Exception {
        startUp(TransportProtocol.TLS);
        provider.setSwitchConnectionHandler(handler);

        provider.startup().get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * Tests correct provider startup - over UDP.
     */
    @Test
    public void testStartup6() throws Exception {
        startUp(TransportProtocol.UDP);
        provider.setSwitchConnectionHandler(handler);

        provider.startup().get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * Tests correct provider shutdown.
     */
    @Test
    public void testShutdown() throws Exception {
        startUp(TransportProtocol.TCP);
        provider.setSwitchConnectionHandler(handler);

        provider.startup().get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        assertTrue("Failed to stop", provider.shutdown().get(5 * WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
    }
}
