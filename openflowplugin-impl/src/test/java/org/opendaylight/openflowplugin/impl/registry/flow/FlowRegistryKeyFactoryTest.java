/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class FlowRegistryKeyFactoryTest {
    private static final Logger LOG = LoggerFactory.getLogger(FlowRegistryKeyFactoryTest.class);
    private static final FlowsStatisticsUpdateBuilder FLOWS_STATISTICS_UPDATE_BUILDER =
            new FlowsStatisticsUpdateBuilder();
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private DeviceState deviceState;
    @Mock
    private DeviceInfo deviceInfo;

    @Before
    public void setup() {
        var flowAndStatisticsMapListList = new ArrayList<FlowAndStatisticsMapList>();
        for (int i = 1; i < 4; i++) {
            flowAndStatisticsMapListList.add(TestFlowHelper.createFlowAndStatisticsMapListBuilder(i).build());
        }
        FLOWS_STATISTICS_UPDATE_BUILDER.setFlowAndStatisticsMapList(flowAndStatisticsMapListList);
    }

    @Test
    public void testEquals() {
        FlowsStatisticsUpdate flowStats = FLOWS_STATISTICS_UPDATE_BUILDER.build();

        HashSet<FlowRegistryKey> flowRegistryKeys = new HashSet<>();
        for (FlowAndStatisticsMapList item : flowStats.nonnullFlowAndStatisticsMapList()) {
            final FlowRegistryKey key1 = FlowRegistryKeyFactory.VERSION_1_3.create(item);
            final FlowRegistryKey key2 = FlowRegistryKeyFactory.VERSION_1_3.create(item);
            flowRegistryKeys.add(key1);
            flowRegistryKeys.add(key1);
            flowRegistryKeys.add(key2);
        }
        assertEquals(3, flowRegistryKeys.size());
    }

    @Test
    public void testEqualsNegative() {
        final var keyFactory = FlowRegistryKeyFactory.VERSION_1_3;

        final FlowAndStatisticsMapList flowStatisticsMapList1 =
                TestFlowHelper.createFlowAndStatisticsMapListBuilder(1).build();
        final FlowRegistryKey key1 = keyFactory.create(flowStatisticsMapList1);

        FlowRegistryKey key2;
        FlowAndStatisticsMapListBuilder flowStatisticsMapListBld2;

        // different priority
        flowStatisticsMapListBld2 = new FlowAndStatisticsMapListBuilder(flowStatisticsMapList1);
        flowStatisticsMapListBld2.setPriority(Uint16.valueOf(flowStatisticsMapListBld2.getPriority().toJava() + 1));
        key2 = keyFactory.create(flowStatisticsMapListBld2.build());
        assertFalse(key1.equals(key2));

        // different match
        flowStatisticsMapListBld2 = new FlowAndStatisticsMapListBuilder(flowStatisticsMapList1);
        flowStatisticsMapListBld2.setMatch(new MatchBuilder().build());
        key2 = keyFactory.create(flowStatisticsMapListBld2.build());
        assertFalse(key1.equals(key2));

        // different tableId
        flowStatisticsMapListBld2 = new FlowAndStatisticsMapListBuilder(flowStatisticsMapList1);
        flowStatisticsMapListBld2.setTableId(Uint8.valueOf(flowStatisticsMapListBld2.getTableId().toJava() + 1));
        key2 = keyFactory.create(flowStatisticsMapListBld2.build());
        assertFalse(key1.equals(key2));

        assertFalse(key1.equals(null));
    }

    @Test
    public void testGetHash2() {
        MatchBuilder match1Builder = new MatchBuilder().setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Destination(new Ipv4Prefix("10.0.1.157/32")).build());
        FlowBuilder flow1Builder = new FlowBuilder()
                .setId(new FlowId("foo"))
                .setCookie(new FlowCookie(Uint64.valueOf(483)))
                .setMatch(match1Builder.build())
                .setPriority(Uint16.TWO)
                .setTableId(Uint8.ZERO);

        FlowRegistryKey flow1Hash = FlowRegistryKeyFactory.VERSION_1_3.create(flow1Builder.build());
        LOG.info("flowHash1: {}", flow1Hash.hashCode());


        MatchBuilder match2Builder = new MatchBuilder().setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Destination(new Ipv4Prefix("10.0.0.242/32")).build());
        FlowBuilder flow2Builder = new FlowBuilder(flow1Builder.build())
                .setCookie(new FlowCookie(Uint64.valueOf(148)))
                .setMatch(match2Builder.build());

        FlowRegistryKey flow2Hash = FlowRegistryKeyFactory.VERSION_1_3.create(flow2Builder.build());
        LOG.info("flowHash2: {}", flow2Hash.hashCode());

        assertNotSame(flow1Hash, flow2Hash);
    }

    @Test
    public void testGetHashNPE() {
        MatchBuilder match1Builder = new MatchBuilder().setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Destination(new Ipv4Prefix("10.0.1.157/32")).build());
        FlowBuilder flow1Builder = new FlowBuilder()
                .setId(new FlowId("foo"))
                .setCookie(new FlowCookie(Uint64.valueOf(483)))
                .setMatch(match1Builder.build())
                .setPriority(Uint16.TWO)
                .setTableId(Uint8.ZERO);

        FlowBuilder fb1 = new FlowBuilder(flow1Builder.build());
        fb1.setTableId((Uint8) null);

        var ex = assertThrows(NoSuchElementException.class,
            () -> FlowRegistryKeyFactory.VERSION_1_3.create(fb1.build()));
        assertEquals("Value of tableid is not present", ex.getMessage());

        FlowBuilder fb2 = new FlowBuilder(flow1Builder.build());
        fb2.setPriority((Uint16) null);

        assertNotNull(FlowRegistryKeyFactory.VERSION_1_3.create(fb2.build()));

        FlowBuilder fb3 = new FlowBuilder(flow1Builder.build());
        fb3.setCookie(null);
        FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.VERSION_1_3.create(fb3.build());
        assertNotNull(flowRegistryKey.getCookie());
        assertEquals(OFConstants.DEFAULT_COOKIE, flowRegistryKey.getCookie());
    }

    @Test
    public void testGetHash() {
        FlowsStatisticsUpdate flowStats = FLOWS_STATISTICS_UPDATE_BUILDER.build();

        for (FlowAndStatisticsMapList item : flowStats.nonnullFlowAndStatisticsMapList()) {
            FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.VERSION_1_3.create(item);
            FlowRegistryKey lastHash = null;
            if (null != lastHash) {
                assertNotEquals(lastHash, flowRegistryKey);
            } else {
                lastHash = flowRegistryKey;
            }
        }
    }
}
