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
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowHash;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RunWith(MockitoJUnitRunner.class)
public class FlowHashFactoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowHashFactoryTest.class);


    private static final FlowsStatisticsUpdateBuilder FLOWS_STATISTICS_UPDATE_BUILDER = new FlowsStatisticsUpdateBuilder();
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private DeviceState deviceState;


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
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
    }

    @Test
    public void testEquals() throws Exception {
        FlowsStatisticsUpdate flowStats = FLOWS_STATISTICS_UPDATE_BUILDER.build();

        HashSet<FlowHash> flowHashs = new HashSet();
        for (FlowAndStatisticsMapList item : flowStats.getFlowAndStatisticsMapList()) {
            FlowHash flowHash = FlowHashFactory.create(item, deviceContext);
            flowHashs.add(flowHash);
            flowHashs.add(flowHash);
        }
        assertEquals(3, flowHashs.size());
    }

    @Test
    @Ignore
    public void testGetHash2() throws Exception {
        MatchBuilder match1Builder = new MatchBuilder().setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Destination(new Ipv4Prefix("10.0.1.157/32")).build());
        FlowBuilder flow1Builder = new FlowBuilder()
                .setCookie(new FlowCookie(BigInteger.valueOf(483)))
                .setMatch(match1Builder.build())
                .setPriority(2)
                .setTableId((short) 0);

        FlowHash flow1Hash = FlowHashFactory.create(flow1Builder.build(), deviceContext);
        LOG.info("flowHash1: {}", flow1Hash.hashCode());


        MatchBuilder match2Builder = new MatchBuilder().setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Destination(new Ipv4Prefix("10.0.0.242/32")).build());
        FlowBuilder flow2Builder = new FlowBuilder(flow1Builder.build())
                .setCookie(new FlowCookie(BigInteger.valueOf(148)))
                .setMatch(match2Builder.build());

        FlowHash flow2Hash = FlowHashFactory.create(flow2Builder.build(), deviceContext);
        LOG.info("flowHash2: {}", flow2Hash.hashCode());

        Assert.assertNotSame(flow1Hash, flow2Hash);
    }

    @Test
    public void testGetHash() throws Exception {
        FlowsStatisticsUpdate flowStats = FLOWS_STATISTICS_UPDATE_BUILDER.build();

        for (FlowAndStatisticsMapList item : flowStats.getFlowAndStatisticsMapList()) {
            FlowHash flowHash = FlowHashFactory.create(item, deviceContext);
            FlowHash lastHash = null;
            if (null != lastHash) {
                assertNotEquals(lastHash, flowHash);
            } else {
                lastHash = flowHash;
            }
        }
    }
}