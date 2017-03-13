/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeatures;

/**
 * Test for {@link MultipartRequestInputFactory}.
 */
public class MultipartRequestInputFactoryTest {

    private long xid = 42L;
    private short ofVersion;

    @Before

    public void setUp() throws Exception {
        ofVersion = OFConstants.OFP_VERSION_1_3;
    }

    @Test
    public void testMakeMultipartRequestInput_DESC() throws Exception {
        MultipartType mpType = MultipartType.OFPMPDESC;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        checkEmptyBody(mpRqInput.getMultipartRequestBody(), MultipartRequestDescCase.class);
    }

    @Test
    public void testMakeMultipartRequestInput_FLOW_13() throws Exception {
        final MultipartType mpType = MultipartType.OFPMPFLOW;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        final MultipartRequestBody mpRqBody = mpRqInput.getMultipartRequestBody();
        Assert.assertTrue(mpRqBody instanceof MultipartRequestFlowCase);

        MultipartRequestFlow mpRq = ((MultipartRequestFlowCase) mpRqBody).getMultipartRequestFlow();
        Assert.assertEquals(OFConstants.OFPTT_ALL, mpRq.getTableId());
        Assert.assertEquals(OFConstants.OFPP_ANY, mpRq.getOutPort());
        Assert.assertEquals(OFConstants.OFPG_ANY, mpRq.getOutGroup());
        Assert.assertEquals(0, mpRq.getCookie().intValue());
        Assert.assertEquals(0, mpRq.getCookieMask().intValue());
        Assert.assertNotNull(mpRq.getMatch());
        Assert.assertNull(mpRq.getMatchV10());

        final String EMPTY_MATCH_DUMP =
                "Match [_type=class org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType, " +
                        "augmentation=[]]";
        Assert.assertEquals(EMPTY_MATCH_DUMP, String.valueOf(mpRq.getMatch()));
    }

    @Test
    public void testMakeMultipartRequestInput_FLOW_10() throws Exception {
        final MultipartType mpType = MultipartType.OFPMPFLOW;
        ofVersion = OFConstants.OFP_VERSION_1_0;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        final MultipartRequestBody mpRqBody = mpRqInput.getMultipartRequestBody();
        Assert.assertTrue(mpRqBody instanceof MultipartRequestFlowCase);

        MultipartRequestFlow mpRq = ((MultipartRequestFlowCase) mpRqBody).getMultipartRequestFlow();
        Assert.assertEquals(OFConstants.OFPTT_ALL, mpRq.getTableId());
        Assert.assertEquals(OFConstants.OFPP_ANY, mpRq.getOutPort());
        Assert.assertEquals(OFConstants.OFPG_ANY, mpRq.getOutGroup());
        Assert.assertEquals(0, mpRq.getCookie().intValue());
        Assert.assertEquals(0, mpRq.getCookieMask().intValue());
        Assert.assertNull(mpRq.getMatch());
        Assert.assertNotNull(mpRq.getMatchV10());
    }

    @Test
    public void testMakeMultipartRequestInput_AGGREGATE() throws Exception {
        MultipartType mpType = MultipartType.OFPMPAGGREGATE;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        checkEmptyBody(mpRqInput.getMultipartRequestBody(), MultipartRequestAggregateCase.class);
    }

    @Test
    public void testMakeMultipartRequestInput_TABLE() throws Exception {
        MultipartType mpType = MultipartType.OFPMPTABLE;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        checkEmptyBody(mpRqInput.getMultipartRequestBody(), MultipartRequestTableCase.class);
    }

    @Test
    public void testMakeMultipartRequestInput_PORTSTATS() throws Exception {
        final MultipartType mpType = MultipartType.OFPMPPORTSTATS;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        final MultipartRequestBody mpRqBody = mpRqInput.getMultipartRequestBody();
        Assert.assertTrue(mpRqBody instanceof MultipartRequestPortStatsCase);

        MultipartRequestPortStats mpRq = ((MultipartRequestPortStatsCase) mpRqBody).getMultipartRequestPortStats();
        Assert.assertEquals(OFConstants.OFPP_ANY, mpRq.getPortNo());
    }

    @Test
    public void testMakeMultipartRequestInput_QUEUE() throws Exception {
        final MultipartType mpType = MultipartType.OFPMPQUEUE;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        final MultipartRequestBody mpRqBody = mpRqInput.getMultipartRequestBody();
        Assert.assertTrue(mpRqBody instanceof MultipartRequestQueueCase);

        MultipartRequestQueue mpRq = ((MultipartRequestQueueCase) mpRqBody).getMultipartRequestQueue();
        Assert.assertEquals(OFConstants.OFPP_ANY, mpRq.getPortNo());
        Assert.assertEquals(OFConstants.OFPQ_ALL, mpRq.getQueueId());
    }

    @Test
    public void testMakeMultipartRequestInput_GROUP() throws Exception {
        final MultipartType mpType = MultipartType.OFPMPGROUP;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        final MultipartRequestBody mpRqBody = mpRqInput.getMultipartRequestBody();
        Assert.assertTrue(mpRqBody instanceof MultipartRequestGroupCase);

        MultipartRequestGroup mpRq = ((MultipartRequestGroupCase) mpRqBody).getMultipartRequestGroup();
        Assert.assertEquals(OFConstants.OFPG_ALL, mpRq.getGroupId().getValue());
    }

    @Test
    public void testMakeMultipartRequestInput_GROUPDESC() throws Exception {
        MultipartType mpType = MultipartType.OFPMPGROUPDESC;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        checkEmptyBody(mpRqInput.getMultipartRequestBody(), MultipartRequestGroupDescCase.class);
    }

    @Test
    public void testMakeMultipartRequestInput_GROUPFEATURES() throws Exception {
        MultipartType mpType = MultipartType.OFPMPGROUPFEATURES;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        checkEmptyBody(mpRqInput.getMultipartRequestBody(), MultipartRequestGroupFeaturesCase.class);
    }

    @Test
    public void testMakeMultipartRequestInput_METER() throws Exception {
        final MultipartType mpType = MultipartType.OFPMPMETER;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        final MultipartRequestBody mpRqBody = mpRqInput.getMultipartRequestBody();
        Assert.assertTrue(mpRqBody instanceof MultipartRequestMeterCase);

        MultipartRequestMeter mpRq = ((MultipartRequestMeterCase) mpRqBody).getMultipartRequestMeter();
        Assert.assertEquals(OFConstants.OFPM_ALL, mpRq.getMeterId().getValue());
    }

    @Test
    public void testMakeMultipartRequestInput_METERCONFIG() throws Exception {
        final MultipartType mpType = MultipartType.OFPMPMETERCONFIG;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        final MultipartRequestBody mpRqBody = mpRqInput.getMultipartRequestBody();
        Assert.assertTrue(mpRqBody instanceof MultipartRequestMeterConfigCase);

        MultipartRequestMeterConfig mpRq = ((MultipartRequestMeterConfigCase) mpRqBody).getMultipartRequestMeterConfig();
        Assert.assertEquals(OFConstants.OFPM_ALL, mpRq.getMeterId().getValue());
    }

    @Test
    public void testMakeMultipartRequestInput_METERFEATURES() throws Exception {
        MultipartType mpType = MultipartType.OFPMPMETERFEATURES;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        checkEmptyBody(mpRqInput.getMultipartRequestBody(), MultipartRequestMeterFeaturesCase.class);
    }

    @Test
    public void testMakeMultipartRequestInput_TABLEFEATURES() throws Exception {
        final MultipartType mpType = MultipartType.OFPMPTABLEFEATURES;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        final MultipartRequestBody mpRqBody = mpRqInput.getMultipartRequestBody();
        Assert.assertTrue(mpRqBody instanceof MultipartRequestTableFeaturesCase);

        MultipartRequestTableFeatures mpRq = ((MultipartRequestTableFeaturesCase) mpRqBody).getMultipartRequestTableFeatures();
        Assert.assertNotNull(mpRq);
    }

    @Test
    public void testMakeMultipartRequestInput_PORTDESC() throws Exception {
        MultipartType mpType = MultipartType.OFPMPPORTDESC;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        checkEmptyBody(mpRqInput.getMultipartRequestBody(), MultipartRequestPortDescCase.class);
    }

    @Test
    public void testMakeMultipartRequestInput_EXPERIMENTER() throws Exception {
        MultipartType mpType = MultipartType.OFPMPEXPERIMENTER;
        final MultipartRequestInput mpRqInput =
            (MultipartRequestInput) MultipartRequestInputFactory.makeMultipartRequest(xid, ofVersion, mpType, false);
        checkHeader(mpRqInput, mpType, ofVersion);
        checkEmptyBody(mpRqInput.getMultipartRequestBody(), MultipartRequestExperimenterCase.class);
    }

    private void checkHeader(MultipartRequestInput mpRqInput, MultipartType mpType, short ofVersion) {
        Assert.assertFalse(mpRqInput.getFlags().isOFPMPFREQMORE());
        Assert.assertEquals(ofVersion, mpRqInput.getVersion().shortValue());
        Assert.assertEquals(mpType, mpRqInput.getType());
        Assert.assertEquals(xid, mpRqInput.getXid().longValue());
    }

    private void checkEmptyBody(MultipartRequestBody mpRqBody, Class<? extends MultipartRequestBody> expectedMpRqBodyClass) throws Exception {
        Assert.assertTrue(expectedMpRqBodyClass.isAssignableFrom(mpRqBody.getImplementedInterface()));
        final String expectedDump = expectedMpRqBodyClass.getSimpleName() + " [augmentation=[]]";
        Assert.assertEquals(expectedDump, String.valueOf(mpRqBody));
    }
}
