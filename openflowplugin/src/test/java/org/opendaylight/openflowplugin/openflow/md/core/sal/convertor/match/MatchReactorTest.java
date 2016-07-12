/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

/**
 * match conversion and injection test
 */
public class MatchReactorTest {
    private MatchBuilder matchBuilder;
    private ConvertorManager convertorManager;

    /**
     * prepare input match
     */
    @Before
    public void setUp() {
        matchBuilder = new MatchBuilder();
        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        EthernetTypeBuilder ethernetTypeBuilder = new EthernetTypeBuilder();
        ethernetTypeBuilder.setType(new EtherType(42L));
        ethernetMatchBuilder.setEthernetType(ethernetTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    /**
     * convert for OF-1.3, inject into {@link FlowModInputBuilder}
     */
    @Test
    public void testMatchConvertorV13_flow() {
        FlowModInputBuilder target = new FlowModInputBuilder();
        MatchReactor.getInstance().convert(matchBuilder.build(),
                OFConstants.OFP_VERSION_1_3, target, convertorManager);
        Assert.assertNotNull(target.getMatch());
    }

    /**
     * convert for OF-1.0, inject into {@link FlowModInputBuilder}
     */
    @Test
    public void testMatchConvertorV10_flow() {
        FlowModInputBuilder target = new FlowModInputBuilder();
        MatchReactor.getInstance().convert(matchBuilder.build(),
                OFConstants.OFP_VERSION_1_0, target, convertorManager);
        Assert.assertNotNull(target.getMatchV10());
    }


    /**
     * convert for OF-1.3, inject into {@link MultipartRequestFlowBuilder}
     */
    @Test
    public void testMatchConvertorV13_mpRequestFlow() {
        MultipartRequestFlowBuilder target = new MultipartRequestFlowBuilder();
        MatchReactor.getInstance().convert(matchBuilder.build(),
                OFConstants.OFP_VERSION_1_3, target, convertorManager);
        Assert.assertNotNull(target.getMatch());
    }

    /**
     * convert for OF-1.0, inject into {@link MultipartRequestFlowBuilder}
     */
    @Test
    public void testMatchConvertorV10_mpRequestFlow() {
        MultipartRequestFlowBuilder target = new MultipartRequestFlowBuilder();
        MatchReactor.getInstance().convert(matchBuilder.build(),
                OFConstants.OFP_VERSION_1_0, target, convertorManager);
        Assert.assertNotNull(target.getMatchV10());
    }

    @Test
    public void testMatchConvertorV10_null() {
        MultipartRequestAggregateBuilder target = new MultipartRequestAggregateBuilder();
        MatchReactor.getInstance().convert(null,
                OFConstants.OFP_VERSION_1_0, target, convertorManager);
        Assert.assertNotNull(target.getMatchV10());
    }

    /**
     * convert for OF-1.3, inject into {@link MultipartRequestAggregateBuilder}
     */
    @Test
    public void testMatchConvertorV13_mpRequestAggregate() {
        MultipartRequestAggregateBuilder target = new MultipartRequestAggregateBuilder();
        MatchReactor.getInstance().convert(matchBuilder.build(),
                OFConstants.OFP_VERSION_1_3, target, convertorManager);
        Assert.assertNotNull(target.getMatch());
    }

    @Test
    public void testMatchConvertorV13_null() {
        MultipartRequestAggregateBuilder target = new MultipartRequestAggregateBuilder();
        MatchReactor.getInstance().convert(null,
                OFConstants.OFP_VERSION_1_3, target, convertorManager);
        Assert.assertNotNull(target.getMatch());
        Assert.assertEquals(0, target.getMatch().getMatchEntry().size());
    }

    /**
     * convert for OF-1.0, inject into {@link MultipartRequestAggregateBuilder}
     */
    @Test
    public void testMatchConvertorV10_mpRequestAggregate() {
        MultipartRequestAggregateBuilder target = new MultipartRequestAggregateBuilder();
        MatchReactor.getInstance().convert(matchBuilder.build(),
                OFConstants.OFP_VERSION_1_0, target, convertorManager);
        Assert.assertNotNull(target.getMatchV10());
    }


}