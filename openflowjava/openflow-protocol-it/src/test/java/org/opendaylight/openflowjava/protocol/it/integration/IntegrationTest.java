/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.it.integration;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfigurationImpl;
import org.opendaylight.openflowjava.protocol.impl.clients.ClientEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.ListeningSimpleClient;
import org.opendaylight.openflowjava.protocol.impl.clients.OFClient;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioFactory;
import org.opendaylight.openflowjava.protocol.impl.clients.ScenarioHandler;
import org.opendaylight.openflowjava.protocol.impl.clients.SendEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.SimpleClient;
import org.opendaylight.openflowjava.protocol.impl.clients.SleepEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.UdpSimpleClient;
import org.opendaylight.openflowjava.protocol.impl.clients.WaitForMessageEvent;
import org.opendaylight.openflowjava.protocol.impl.core.SwitchConnectionProviderImpl;
import org.opendaylight.openflowjava.protocol.impl.core.TcpHandler;
import org.opendaylight.openflowjava.protocol.impl.core.UdpHandler;
import org.opendaylight.openflowjava.protocol.impl.core.connection.ConnectionConfigurationImpl;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class IntegrationTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IntegrationTest.class);

    private static int port;
    private TlsConfiguration tlsConfiguration;
    private static final int SWITCH_IDLE_TIMEOUT = 2000;
    private static final long CONNECTION_TIMEOUT = 2000;
    private InetAddress startupAddress;
    private MockPlugin mockPlugin;
    private SwitchConnectionProviderImpl switchConnectionProvider;
    private ConnectionConfigurationImpl connConfig;

    private Thread t;

    private enum ClientType {SIMPLE, LISTENING}
    /**
     * @param protocol communication protocol to be used during test
     * @throws Exception
     */
    public void setUp(final TransportProtocol protocol) throws Exception {
        LOGGER.debug("\n starting test -------------------------------");

        final String currentDir = System.getProperty("user.dir");
        LOGGER.debug("Current dir using System: {}", currentDir);
        startupAddress = InetAddress.getLocalHost();
        tlsConfiguration = null;
        if (protocol.equals(TransportProtocol.TLS)) {
            tlsConfiguration = new TlsConfigurationImpl(KeystoreType.JKS,
                    "/selfSignedSwitch", PathType.CLASSPATH, KeystoreType.JKS,
                    "/selfSignedController", PathType.CLASSPATH,
                    new ArrayList<String>());
        }
        connConfig = new ConnectionConfigurationImpl(startupAddress, 0, tlsConfiguration, SWITCH_IDLE_TIMEOUT, true);
        connConfig.setTransferProtocol(protocol);
        mockPlugin = new MockPlugin();

        switchConnectionProvider = new SwitchConnectionProviderImpl();
        switchConnectionProvider.setSwitchConnectionHandler(mockPlugin);
        switchConnectionProvider.setConfiguration(connConfig);
        switchConnectionProvider.startup().get(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        if (protocol.equals(TransportProtocol.TCP) || protocol.equals(TransportProtocol.TLS)) {
            final TcpHandler tcpHandler = (TcpHandler) switchConnectionProvider.getServerFacade();
            port = tcpHandler.getPort();
        } else {
            final UdpHandler udpHandler = (UdpHandler) switchConnectionProvider.getServerFacade();
            port = udpHandler.getPort();
        }
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        switchConnectionProvider.close();
        LOGGER.debug("\n ending test -------------------------------");
    }

    /**
     * Library integration and communication test with handshake
     * @throws Exception
     */
    @Test
    public void testHandshake() throws Exception {
        setUp(TransportProtocol.TCP);
        final int amountOfCLients = 1;
        final Deque<ClientEvent> scenario = ScenarioFactory.createHandshakeScenario();
        final ScenarioHandler handler = new ScenarioHandler(scenario);
        final List<OFClient> clients = createAndStartClient(amountOfCLients, handler, TransportProtocol.TCP, ClientType.SIMPLE);
        final OFClient firstClient = clients.get(0);
        firstClient.getScenarioDone().get();
        Thread.sleep(1000);

        LOGGER.debug("testHandshake() Finished") ;
    }

    /**
     * Library integration and secured communication test with handshake
     * @throws Exception
     */
    @Test
    public void testTlsHandshake() throws Exception {
        setUp(TransportProtocol.TLS);
        final int amountOfCLients = 1;
        final Deque<ClientEvent> scenario = ScenarioFactory.createHandshakeScenario();
        final ScenarioHandler handler = new ScenarioHandler(scenario);
        final List<OFClient> clients = createAndStartClient(amountOfCLients, handler, TransportProtocol.TLS, ClientType.SIMPLE);
        final OFClient firstClient = clients.get(0);
        firstClient.getScenarioDone().get();
        Thread.sleep(1000);

        LOGGER.debug("testTlsHandshake() Finished") ;
    }

    /**
     * Library integration and communication test with handshake + echo exchange
     * @throws Exception
     */
    @Test
    public void testHandshakeAndEcho() throws Exception {
        setUp(TransportProtocol.TCP);
        final int amountOfCLients = 1;
        final Deque<ClientEvent> scenario = ScenarioFactory.createHandshakeScenario();
        scenario.addFirst(new SleepEvent(1000));
        scenario.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 02 00 08 00 00 00 04")));
        scenario.addFirst(new SleepEvent(1000));
        scenario.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 03 00 08 00 00 00 04")));
        final ScenarioHandler handler = new ScenarioHandler(scenario);
        final List<OFClient> clients = createAndStartClient(amountOfCLients, handler, TransportProtocol.TCP, ClientType.SIMPLE);
        final OFClient firstClient = clients.get(0);
        firstClient.getScenarioDone().get();

        LOGGER.debug("testHandshakeAndEcho() Finished") ;
    }

    /**
     * Library integration and secured communication test with handshake + echo exchange
     * @throws Exception
     */
    @Test
    public void testTlsHandshakeAndEcho() throws Exception {
        setUp(TransportProtocol.TLS);
        final int amountOfCLients = 1;
        final Deque<ClientEvent> scenario = ScenarioFactory.createHandshakeScenario();
        scenario.addFirst(new SleepEvent(1000));
        scenario.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 02 00 08 00 00 00 04")));
        scenario.addFirst(new SleepEvent(1000));
        scenario.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 03 00 08 00 00 00 04")));
        final ScenarioHandler handler = new ScenarioHandler(scenario);
        final List<OFClient> clients = createAndStartClient(amountOfCLients, handler, TransportProtocol.TLS, ClientType.SIMPLE);
        final OFClient firstClient = clients.get(0);
        firstClient.getScenarioDone().get();

        LOGGER.debug("testTlsHandshakeAndEcho() Finished") ;
    }

    /**
     * Library udp integration and communication test with handshake + echo exchange
     * @throws Exception
     */
    @Test
    public void testUdpHandshakeAndEcho() throws Exception {
        setUp(TransportProtocol.UDP);
        final int amountOfCLients = 1;
        final Deque<ClientEvent> scenario = ScenarioFactory.createHandshakeScenario();
        scenario.addFirst(new SleepEvent(1000));
        scenario.addFirst(new SendEvent(ByteBufUtils.hexStringToBytes("04 02 00 08 00 00 00 04")));
        scenario.addFirst(new SleepEvent(1000));
        scenario.addFirst(new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 03 00 08 00 00 00 04")));
        final ScenarioHandler handler = new ScenarioHandler(scenario);
        final List<OFClient> clients = createAndStartClient(amountOfCLients, handler, TransportProtocol.UDP, ClientType.SIMPLE);
        final OFClient firstClient = clients.get(0);
        firstClient.getScenarioDone().get();

        LOGGER.debug("testUdpHandshakeAndEcho() Finished") ;
    }

    /**
     * Library integration and communication test (with virtual machine)
     * @throws Exception
     */
    //@Test
    public void testCommunicationWithVM() throws Exception {
        mockPlugin.getFinishedFuture().get();
    }

    /**
     * @param amountOfCLients
     * @param protocol true if encrypted connection should be used
     * @return new clients up and running
     * @throws ExecutionException if some client could not start
     */
    private List<OFClient> createAndStartClient(final int amountOfCLients, final ScenarioHandler scenarioHandler,
            final TransportProtocol protocol, final ClientType clientType) throws ExecutionException {
        final List<OFClient> clientsHorde = new ArrayList<>();
        for (int i = 0; i < amountOfCLients; i++) {
            LOGGER.debug("startup address in createclient: {}", startupAddress.getHostAddress());
            OFClient sc = null;
            if (clientType == ClientType.SIMPLE) {
                if (protocol.equals(TransportProtocol.TCP)) {
                    sc = new SimpleClient(startupAddress.getHostAddress(), port);
                    sc.setSecuredClient(false);
                } else if (protocol.equals(TransportProtocol.TLS)) {
                    sc = new SimpleClient(startupAddress.getHostAddress(), port);
                    sc.setSecuredClient(true);
                } else {
                    sc = new UdpSimpleClient(startupAddress.getHostAddress(), port);
                }
            } else if (clientType == ClientType.LISTENING) {
                sc = new ListeningSimpleClient(0);
                sc.setScenarioHandler(scenarioHandler);
                sc.setSecuredClient(false);
            } else {
                LOGGER.error("Unknown type of client.");
                throw new IllegalStateException("Unknown type of client.");
            }

            sc.setScenarioHandler(scenarioHandler);
            clientsHorde.add(sc);
            //sc.run();
            t = new Thread(sc);
            t.start();
        }
        for (final OFClient sc : clientsHorde) {
            try {
                sc.getIsOnlineFuture().get(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (final Exception e) {
                LOGGER.error("createAndStartClient: Something borked ... ", e.getMessage(), e);
                throw new ExecutionException(e);
            }
        }
        return clientsHorde;
    }

    /**
     * @throws Exception
     */
    @Test
    public void testInitiateConnection() throws Exception {
        setUp(TransportProtocol.TCP);

        final Deque<ClientEvent> scenario = ScenarioFactory.createHandshakeScenario();
        final ScenarioHandler handler = new ScenarioHandler(scenario);
        final List<OFClient> clients = createAndStartClient(1, handler, TransportProtocol.TCP, ClientType.LISTENING);
        final OFClient ofClient = clients.get(0);
        ofClient.getIsOnlineFuture().get(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        final int listeningClientPort = ((ListeningSimpleClient) ofClient).getPort();
        mockPlugin.initiateConnection(switchConnectionProvider, "localhost", listeningClientPort);
        ofClient.getScenarioDone().get();
        LOGGER.debug("testInitiateConnection() Finished") ;
    }
}
