/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.topology.lldp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.NonZeroUint32Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.TopologyLldpDiscoveryConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.TopologyLldpDiscoveryConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link LLDPLinkAger}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPLinkAgerTest {

    private static final Logger LOG = LoggerFactory.getLogger(LLDPLinkAgerTest.class);

    private static final long LLDP_INTERVAL = 5L;
    private static final long LINK_EXPIRATION_TIME = 10L;

    /**
     * We need to wait while other tasks are finished before we can check anything in LLDPAgingTask.
     */
    private static final int SLEEP = 100;

    private LLDPLinkAger lldpLinkAger;

    @Mock
    private LinkDiscovered link;
    @Mock
    private NotificationProviderService notificationService;

    @Before
    public void setUp() throws Exception {
        lldpLinkAger = new LLDPLinkAger(getConfig(), notificationService, getConfigurationService());
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
        verify(notificationService).publish(Matchers.any(LinkRemoved.class));
    }

    private TopologyLldpDiscoveryConfig getConfig() {
        TopologyLldpDiscoveryConfigBuilder cfgBuilder = new TopologyLldpDiscoveryConfigBuilder();
        cfgBuilder.setTopologyLldpInterval(new NonZeroUint32Type(LLDP_INTERVAL));
        cfgBuilder.setTopologyLldpExpirationInterval(new NonZeroUint32Type(LINK_EXPIRATION_TIME));
        return cfgBuilder.build();
    }

    private ConfigurationService getConfigurationService() {
        final ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        final TopologyLldpDiscoveryConfig config = getConfig();

        Mockito.when(configurationService.registerListener(Mockito.any())).thenReturn(() -> {
        });

        Mockito.when(configurationService.getProperty(Mockito.eq("topology-lldp-interval"), Mockito.any()))
                .thenReturn(config.getTopologyLldpInterval());

        Mockito.when(configurationService.getProperty(Mockito.eq("topology-lldp-expiration-interval"), Mockito.any()))
                .thenReturn(config.getTopologyLldpExpirationInterval());

        return configurationService;
    }
}
