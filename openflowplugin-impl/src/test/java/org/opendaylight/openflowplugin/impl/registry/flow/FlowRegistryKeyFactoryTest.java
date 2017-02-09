/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class FlowRegistryKeyFactoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowRegistryKeyFactoryTest.class);


    private static final FlowsStatisticsUpdateBuilder FLOWS_STATISTICS_UPDATE_BUILDER = new FlowsStatisticsUpdateBuilder();
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private DeviceState deviceState;
    @Mock
    private DeviceInfo deviceInfo;


    @Before
    public void setup() {
        List<FlowAndStatisticsMapList> flowAndStatisticsMapListList = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            flowAndStatisticsMapListList.add(TestFlowHelper.createFlowAndStatisticsMapListBuilder(i).build());
        }
        FLOWS_STATISTICS_UPDATE_BUILDER.setFlowAndStatisticsMapList(flowAndStatisticsMapListList);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
    }

    @Test
    public void testEquals() throws Exception {
        FlowsStatisticsUpdate flowStats = FLOWS_STATISTICS_UPDATE_BUILDER.build();

        HashSet<FlowRegistryKey> flowRegistryKeys = new HashSet<>();
        for (FlowAndStatisticsMapList item : flowStats.getFlowAndStatisticsMapList()) {
            final FlowRegistryKey key1 = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), item);
            final FlowRegistryKey key2 = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), item);
            flowRegistryKeys.add(key1);
            flowRegistryKeys.add(key1);
            flowRegistryKeys.add(key2);
        }
        assertEquals(3, flowRegistryKeys.size());
    }

    @Test
    public void testEqualsNegative() throws Exception {
        final FlowAndStatisticsMapList flowStatisticsMapList1 = TestFlowHelper.createFlowAndStatisticsMapListBuilder(1).build();
        final FlowRegistryKey key1 = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), flowStatisticsMapList1);

        FlowRegistryKey key2;
        FlowAndStatisticsMapListBuilder flowStatisticsMapListBld2;

        // different priority
        flowStatisticsMapListBld2 = new FlowAndStatisticsMapListBuilder(flowStatisticsMapList1);
        flowStatisticsMapListBld2.setPriority(flowStatisticsMapListBld2.getPriority() + 1);
        key2 = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), flowStatisticsMapListBld2.build());
        Assert.assertFalse(key1.equals(key2));

        // different match
        flowStatisticsMapListBld2 = new FlowAndStatisticsMapListBuilder(flowStatisticsMapList1);
        flowStatisticsMapListBld2.setMatch(new MatchBuilder().build());
        key2 = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), flowStatisticsMapListBld2.build());
        Assert.assertFalse(key1.equals(key2));

        // different tableId
        flowStatisticsMapListBld2 = new FlowAndStatisticsMapListBuilder(flowStatisticsMapList1);
        flowStatisticsMapListBld2.setTableId((short) (flowStatisticsMapListBld2.getTableId() + 1));
        key2 = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), flowStatisticsMapListBld2.build());
        Assert.assertFalse(key1.equals(key2));

        Assert.assertFalse(key1.equals(null));
    }

    @Test
    public void testGetHash2() throws Exception {
        MatchBuilder match1Builder = new MatchBuilder().setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Destination(new Ipv4Prefix("10.0.1.157/32")).build());
        FlowBuilder flow1Builder = new FlowBuilder()
                .setCookie(new FlowCookie(BigInteger.valueOf(483)))
                .setMatch(match1Builder.build())
                .setPriority(2)
                .setTableId((short) 0);

        FlowRegistryKey flow1Hash = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), flow1Builder.build());
        LOG.info("flowHash1: {}", flow1Hash.hashCode());


        MatchBuilder match2Builder = new MatchBuilder().setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Destination(new Ipv4Prefix("10.0.0.242/32")).build());
        FlowBuilder flow2Builder = new FlowBuilder(flow1Builder.build())
                .setCookie(new FlowCookie(BigInteger.valueOf(148)))
                .setMatch(match2Builder.build());

        FlowRegistryKey flow2Hash = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), flow2Builder.build());
        LOG.info("flowHash2: {}", flow2Hash.hashCode());

        Assert.assertNotSame(flow1Hash, flow2Hash);
    }

    @Test
    public void testGetHashNPE() throws Exception {
        MatchBuilder match1Builder = new MatchBuilder().setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Destination(new Ipv4Prefix("10.0.1.157/32")).build());
        FlowBuilder flow1Builder = new FlowBuilder()
                .setCookie(new FlowCookie(BigInteger.valueOf(483)))
                .setMatch(match1Builder.build())
                .setPriority(2)
                .setTableId((short) 0);

        FlowBuilder fb1 = new FlowBuilder(flow1Builder.build());
        fb1.setTableId(null);
        try {
            FlowRegistryKeyFactory.create(deviceInfo.getVersion(), fb1.build());
            Assert.fail("hash creation should have failed because of NPE");
        } catch (Exception e) {
            // expected
            Assert.assertEquals("flow tableId must not be null", e.getMessage());
        }

        FlowBuilder fb2 = new FlowBuilder(flow1Builder.build());
        fb2.setPriority(null);
        try {
            FlowRegistryKeyFactory.create(deviceInfo.getVersion(), fb2.build());
        } catch (Exception e) {
            // not expected
            Assert.fail("no exception was expected while hash was creating.");
        }

        FlowBuilder fb3 = new FlowBuilder(flow1Builder.build());
        fb3.setCookie(null);
        FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), fb3.build());
        Assert.assertNotNull(flowRegistryKey.getCookie());
        Assert.assertEquals(OFConstants.DEFAULT_COOKIE, flowRegistryKey.getCookie());
    }

    @Test
    public void testGetHash() throws Exception {
        FlowsStatisticsUpdate flowStats = FLOWS_STATISTICS_UPDATE_BUILDER.build();

        for (FlowAndStatisticsMapList item : flowStats.getFlowAndStatisticsMapList()) {
            FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(deviceInfo.getVersion(), item);
            FlowRegistryKey lastHash = null;
            if (null != lastHash) {
                assertNotEquals(lastHash, flowRegistryKey);
            } else {
                lastHash = flowRegistryKey;
            }
        }
    }
}
