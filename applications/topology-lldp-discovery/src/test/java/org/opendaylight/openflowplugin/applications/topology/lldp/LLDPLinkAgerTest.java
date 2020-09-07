/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.NonZeroUint32Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.TopologyLldpDiscoveryConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.TopologyLldpDiscoveryConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link LLDPLinkAger}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPLinkAgerTest {

    private static final Logger LOG = LoggerFactory.getLogger(LLDPLinkAgerTest.class);

    private static final Uint32 LLDP_INTERVAL = Uint32.valueOf(5);
    private static final Uint32 LINK_EXPIRATION_TIME = Uint32.TEN;

    /**
     * We need to wait while other tasks are finished before we can check anything in LLDPAgingTask.
     */
    private static final int SLEEP = 100;

    private LLDPLinkAger lldpLinkAger;

    @Mock
    private LinkDiscovered link;
    @Mock
    private NotificationPublishService notificationService;
    @Mock
    private EntityOwnershipService eos;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private LinkRemoved linkRemoved;


    @Before
    public void setUp() {
        lldpLinkAger = new LLDPLinkAger(getConfig(), notificationService, getConfigurationService(), eos, dataBroker);
        Mockito.when(link.getDestination()).thenReturn(new NodeConnectorRef(
                InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId("openflow:1")))));
        Mockito.when(eos.getOwnershipState(any(Entity.class))).thenReturn(
                Optional.of(EntityOwnershipState.IS_OWNER));
    }

    @Test
    public void testPut() {
        assertTrue(lldpLinkAger.isLinkToDateEmpty());
        lldpLinkAger.put(link);
        assertFalse(lldpLinkAger.isLinkToDateEmpty());
    }

    @Test
    public void testClose() throws Exception {
        lldpLinkAger.close();
        assertTrue(lldpLinkAger.isLinkToDateEmpty());
    }

    /**
     * Inner class LLDPAgingTask removes all expired records from linkToDate if any (in constructor of LLDPLinkAger).
     */
    @Test
    public void testLLDPAgingTask() throws InterruptedException {
        lldpLinkAger.put(link);
        Thread.sleep(SLEEP);
        verify(notificationService).putNotification(any(LinkRemoved.class));
    }

    private static TopologyLldpDiscoveryConfig getConfig() {
        TopologyLldpDiscoveryConfigBuilder cfgBuilder = new TopologyLldpDiscoveryConfigBuilder();
        cfgBuilder.setTopologyLldpInterval(new NonZeroUint32Type(LLDP_INTERVAL));
        cfgBuilder.setTopologyLldpExpirationInterval(new NonZeroUint32Type(LINK_EXPIRATION_TIME));
        return cfgBuilder.build();
    }

    private static ConfigurationService getConfigurationService() {
        final ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        final TopologyLldpDiscoveryConfig config = getConfig();

        Mockito.when(configurationService.registerListener(any())).thenReturn(() -> {
        });

        Mockito.lenient().when(configurationService.getProperty(Mockito.eq("topology-lldp-interval"), any()))
                .thenReturn(config.getTopologyLldpInterval());

        Mockito.lenient().when(configurationService.getProperty(Mockito.eq("topology-lldp-expiration-interval"), any()))
                .thenReturn(config.getTopologyLldpExpirationInterval());

        return configurationService;
    }
}
