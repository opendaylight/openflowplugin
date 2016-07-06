/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import java.math.BigInteger;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;

/**
 * flow building helper
 */
public class TestFlowHelper {
    /**
     * @param i data seed
     * @return flow stats builder with dummy content
     */
    protected static FlowAndStatisticsMapListBuilder createFlowAndStatisticsMapListBuilder(int i) {
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
        return flowAndStatisticsMapListBuilder;
    }
}
