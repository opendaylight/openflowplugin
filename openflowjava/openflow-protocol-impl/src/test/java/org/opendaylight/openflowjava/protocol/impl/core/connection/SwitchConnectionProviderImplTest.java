/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfigurationImpl;
import org.opendaylight.openflowjava.protocol.impl.core.SwitchConnectionProviderImpl;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;

/**
 * @author michal.polkorab
 *
 */
public class SwitchConnectionProviderImplTest {

    @Mock SwitchConnectionHandler handler;

    private static final int SWITCH_IDLE_TIMEOUT = 2000;
    private static final int WAIT_TIMEOUT = 2000;
    private InetAddress startupAddress;
    private TlsConfiguration tlsConfiguration;
    private SwitchConnectionProviderImpl provider;
    private ConnectionConfigurationImpl config;

    /**
     * Creates new {@link SwitchConnectionProvider} instance for each test
     * @param protocol communication protocol
     */
    public void startUp(final TransportProtocol protocol) {
        MockitoAnnotations.initMocks(this);
        config = null;
        if (protocol != null) {
            createConfig(protocol);
        }
        provider = new SwitchConnectionProviderImpl();
    }

    private void createConfig(final TransportProtocol protocol) {
        try {
            startupAddress = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        }
        tlsConfiguration = null;
        if (protocol.equals(TransportProtocol.TLS)) {
            tlsConfiguration = new TlsConfigurationImpl(KeystoreType.JKS,
                    "/selfSignedSwitch", PathType.CLASSPATH, KeystoreType.JKS,
                    "/selfSignedController", PathType.CLASSPATH,
                    Lists.newArrayList("TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA256")) ;
        }
        config = new ConnectionConfigurationImpl(startupAddress, 0, tlsConfiguration, SWITCH_IDLE_TIMEOUT, true);
        config.setTransferProtocol(protocol);
    }

    /**
     * Tests provider startup - without configuration and {@link SwitchConnectionHandler}
     */
    @Test
    public void testStartup1() {
        provider = new SwitchConnectionProviderImpl();
        final ListenableFuture<Boolean> future = provider.startup();
        try {
            future.get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertEquals("Wrong state", "java.lang.NullPointerException", e.getMessage());
        }
    }

    /**
     * Tests provider startup - without configuration
     */
    @Test
    public void testStartup2() {
        startUp(null);
        provider.setSwitchConnectionHandler(handler);
        final ListenableFuture<Boolean> future = provider.startup();
        try {
            future.get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertEquals("Wrong state", "java.lang.NullPointerException", e.getMessage());
        }
    }

    /**
     * Tests provider startup - without {@link SwitchConnectionHandler}
     */
    @Test
    public void testStartup3() {
        startUp(TransportProtocol.TCP);
        provider.setConfiguration(config);
        final ListenableFuture<Boolean> future = provider.startup();
        try {
            future.get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertEquals("Wrong state", "java.lang.IllegalStateException:"
                    + " SwitchConnectionHandler is not set", e.getMessage());
        }
    }

    /**
     * Tests correct provider startup - over TCP
     */
    @Test
    public void testStartup4() {
        startUp(TransportProtocol.TCP);
        provider.setConfiguration(config);
        provider.setSwitchConnectionHandler(handler);
        try {
            Assert.assertTrue("Failed to start", provider.startup().get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.fail();
        }
    }

    /**
     * Tests correct provider startup - over TLS
     */
    @Test
    public void testStartup5() {
        startUp(TransportProtocol.TLS);
        provider.setConfiguration(config);
        provider.setSwitchConnectionHandler(handler);
        try {
            Assert.assertTrue("Failed to start", provider.startup().get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.fail();
        }
    }

    /**
     * Tests correct provider startup - over UDP
     */
    @Test
    public void testStartup6() {
        startUp(TransportProtocol.UDP);
        provider.setConfiguration(config);
        provider.setSwitchConnectionHandler(handler);
        try {
            Assert.assertTrue("Failed to start", provider.startup().get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.fail();
        }
        }

    /**
     * Tests correct provider shutdown
     */
    @Test
    public void testShutdown() {
        startUp(TransportProtocol.TCP);
        provider.setConfiguration(config);
        provider.setSwitchConnectionHandler(handler);
        try {
            Assert.assertTrue("Failed to start", provider.startup().get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
            Assert.assertTrue("Failed to stop", provider.shutdown().get(5 * WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

}