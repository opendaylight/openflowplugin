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

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link LLDPLinkAger}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPLinkAgerTest {

    private static final Logger LOG = LoggerFactory.getLogger(LLDPLinkAgerTest.class);

    private LLDPLinkAger lldpLinkAger;
    private final long LLDP_INTERVAL = 5L;
    private final long LINK_EXPIRATION_TIME = 10L;
    /**
     * We need to w8 while other tasks are finished before we can check anything
     * in LLDPAgingTask
     */
    private final int SLEEP = 35;


    @Mock
    private LinkDiscovered link;
    @Mock
    private Map<LinkDiscovered, Date> linkToDate;
    @Mock
    private Timer timer;
    @Mock
    private NotificationProviderService notificationService;
    @Mock
    private LinkRemoved linkRemoved;

    @Before
    public void setUp() throws Exception {
        lldpLinkAger = new LLDPLinkAger(LLDP_INTERVAL, LINK_EXPIRATION_TIME, notificationService);
    }

    @Test
    public void testPut() {
        assertTrue(lldpLinkAger.isLinkToDateEmpty());
        lldpLinkAger.put(link);
        assertFalse(lldpLinkAger.isLinkToDateEmpty());
    }

    @Test
    public void testClose() {
        lldpLinkAger.close();
        assertTrue(lldpLinkAger.isLinkToDateEmpty());
    }

    /**
     * Inner class LLDPAgingTask removes all expired records from linkToDate if any (in constructor of LLDPLinkAger)
     */
    @Test
    public void testLLDPAgingTask() throws InterruptedException {
        lldpLinkAger.put(link);
        Thread.sleep(SLEEP);
        verify(notificationService).publish(Matchers.any(LinkRemoved.class));
    }
}