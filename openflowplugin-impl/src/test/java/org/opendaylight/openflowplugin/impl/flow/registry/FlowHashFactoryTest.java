/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.flow.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.flow.registry.FlowHash;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowHashFactoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowHashFactoryTest.class);


    private static final FlowsStatisticsUpdateBuilder FLOWS_STATISTICS_UPDATE_BUILDER = new FlowsStatisticsUpdateBuilder();


    @Before
    public void setup() {
        List<FlowAndStatisticsMapList> flowAndStatisticsMapListList = new ArrayList();
        for (int i = 1; i < 4; i++) {
            FlowAndStatisticsMapListBuilder flowAndStatisticsMapListBuilder = new FlowAndStatisticsMapListBuilder();
            flowAndStatisticsMapListBuilder.setPriority(i);
            flowAndStatisticsMapListBuilder.setTableId((short) i);
            flowAndStatisticsMapListBuilder.setCookie(new FlowCookie(BigInteger.TEN));

            MatchBuilder matchBuilder = new MatchBuilder();

            EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();

            EthernetSourceBuilder ethernetSourceBuilder = new EthernetSourceBuilder();
            MacAddress macAddress = new MacAddress("00:00:00:00:00:0" + i);
            ethernetSourceBuilder.setAddress(macAddress);
            ethernetMatchBuilder.setEthernetSource(ethernetSourceBuilder.build());

            EthernetDestinationBuilder ethernetDestinationBuilder = new EthernetDestinationBuilder();
            ethernetDestinationBuilder.setAddress(new MacAddress("00:00:00:0" + i + ":00:00"));
            ethernetMatchBuilder.setEthernetDestination(ethernetDestinationBuilder.build());

            matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());

            flowAndStatisticsMapListBuilder.setMatch(matchBuilder.build());
            flowAndStatisticsMapListList.add(flowAndStatisticsMapListBuilder.build());
        }
        FLOWS_STATISTICS_UPDATE_BUILDER.setFlowAndStatisticsMapList(flowAndStatisticsMapListList);
    }

    @Test
    public void testEquals() throws Exception {
        FlowsStatisticsUpdate flowStats = FLOWS_STATISTICS_UPDATE_BUILDER.build();

        HashSet<FlowHash> flowHashs = new HashSet();
        for (FlowAndStatisticsMapList item : flowStats.getFlowAndStatisticsMapList()) {
            FlowHash flowHash = FlowHashFactory.create(item);
            flowHashs.add(flowHash);
            flowHashs.add(flowHash);
        }
        assertEquals(3, flowHashs.size());
    }

    @Test
    public void testGetHash() throws Exception {
        FlowsStatisticsUpdate flowStats = FLOWS_STATISTICS_UPDATE_BUILDER.build();

        for (FlowAndStatisticsMapList item : flowStats.getFlowAndStatisticsMapList()) {
            FlowHash flowHash = FlowHashFactory.create(item);
            FlowHash lastHash = null;
            if (null != lastHash) {
                assertNotEquals(lastHash, flowHash);
            } else {
                lastHash = flowHash;
            }
        }
    }
}