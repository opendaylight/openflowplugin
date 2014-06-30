/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

public class FlowCreatorUtilTest {

    private static final MacAddress macAddress = new MacAddress("00:00:00:00:00:00");
    private static final Ipv4Address ipv4Address = new Ipv4Address("0.0.0.0");

    /**
     * Test method for {@link FlowCreatorUtil#setWildcardedFlowMatch(short version, MultipartRequestFlowBuilder flowBuilder)}.
     */
    @Test
    public void testSetWildcardedFlowMatch_1_0() {
        MultipartRequestFlowBuilder multipartRequestFlowBuilder = new MultipartRequestFlowBuilder();
        FlowCreatorUtil.setWildcardedFlowMatch(OFConstants.OFP_VERSION_1_0, multipartRequestFlowBuilder);
        MultipartRequestFlow multipartRequestFlow = multipartRequestFlowBuilder.build();
        assertMatch(multipartRequestFlow.getMatchV10());

        multipartRequestFlowBuilder = new MultipartRequestFlowBuilder();
        FlowCreatorUtil.setWildcardedFlowMatch(OFConstants.OFP_VERSION_1_3, multipartRequestFlowBuilder);
        multipartRequestFlow = multipartRequestFlowBuilder.build();
        assertMatch(multipartRequestFlow.getMatch());
    }

    /**
     * Test method for {@link FlowCreatorUtil#setWildcardedFlowMatch(short version, MultipartRequestAggregateBuilder aggregateBuilder)}.
     */
    @Test
    public void testSetWildcardedFlowMatch_() {
        MultipartRequestAggregateBuilder multipartRequestAggregateBuilder = new MultipartRequestAggregateBuilder();
        FlowCreatorUtil.setWildcardedFlowMatch(OFConstants.OFP_VERSION_1_0, multipartRequestAggregateBuilder);
        MultipartRequestAggregate multipartRequestAggregate = multipartRequestAggregateBuilder.build();
        assertMatch(multipartRequestAggregate.getMatchV10());

        multipartRequestAggregateBuilder = new MultipartRequestAggregateBuilder();
        FlowCreatorUtil.setWildcardedFlowMatch(OFConstants.OFP_VERSION_1_3, multipartRequestAggregateBuilder);
        multipartRequestAggregate = multipartRequestAggregateBuilder.build();
        assertMatch(multipartRequestAggregate.getMatch());


    }

    private void assertMatch(Match match) {
        assertTrue(match.getType().getClass().isInstance(OxmMatchType.class));
    }

    private void assertMatch(MatchV10 matchV10) {
        assertEquals(matchV10.getDlDst(), macAddress);
        assertEquals(matchV10.getDlSrc(), macAddress);

        assertTrue(matchV10.getNwSrcMask().shortValue() == 0);
        assertTrue(matchV10.getNwDstMask().shortValue() == 0);

        assertTrue(matchV10.getInPort().intValue() == 0);
        assertTrue(matchV10.getDlVlan().intValue() == 0);
        assertTrue(matchV10.getDlVlanPcp().shortValue() == 0);
        assertTrue(matchV10.getDlType().intValue() == 0);

        assertTrue(matchV10.getNwTos().shortValue() == 0);
        assertTrue(matchV10.getNwProto().shortValue() == 0);

        assertEquals(matchV10.getNwSrc(), ipv4Address);
        assertEquals(matchV10.getNwDst(), ipv4Address);

        assertTrue(matchV10.getTpSrc().intValue() == 0);
        assertTrue(matchV10.getTpDst().intValue() == 0);
    }

}
