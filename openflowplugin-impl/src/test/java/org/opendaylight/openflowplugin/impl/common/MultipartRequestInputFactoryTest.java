/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.common
 *
 * Test class for testing {@link org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory}
 * static methods and validations for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType}.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Mar 28, 2015
 */
public class MultipartRequestInputFactoryTest {

    private short ofVersion;

    @Before
    public void initialization() {
        ofVersion = 13;
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * Validation for null input type expect IllegalArgumentException
     */
    @Test(expected=IllegalArgumentException.class)
    public void testMakeMultipartRequestInputLongShortMultipartTypeNullType(){
        final long xid = 1l;
        MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, null);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPDESC}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeDesc(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPDESC;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestDescCase defaultBodyForComparison = new MultipartRequestDescCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPFLOW}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeFlow(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPFLOW;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestFlowCase defaultBodyForComparison = new MultipartRequestFlowCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPAGGREGATE}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeAggr(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPAGGREGATE;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestAggregateCase defaultBodyForComparison = new MultipartRequestAggregateCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPTABLE}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeTable(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPTABLE;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestTableCase defaultBodyForComparison = new MultipartRequestTableCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPPORTSTATS}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypePortStat(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPPORTSTATS;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestPortStatsCase defaultBodyForComparison = new MultipartRequestPortStatsCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPQUEUE}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeQueue(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPQUEUE;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestQueueCase defaultBodyForComparison = new MultipartRequestQueueCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPGROUP}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeGroup(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPGROUP;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestGroupCase defaultBodyForComparison = new MultipartRequestGroupCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPGROUPDESC}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeGrupDesc(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPGROUPDESC;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestGroupDescCase defaultBodyForComparison = new MultipartRequestGroupDescCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPGROUPFEATURES}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeGroupFeaturs(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPGROUPFEATURES;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestGroupFeaturesCase defaultBodyForComparison = new MultipartRequestGroupFeaturesCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPMETER}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMeter(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPMETER;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestMeterCase defaultBodyForComparison = new MultipartRequestMeterCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPMETERCONFIG}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMeterConf(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPMETERCONFIG;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestMeterConfigCase defaultBodyForComparison = new MultipartRequestMeterConfigCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPMETERFEATURES}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMeterFeatures(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPMETERFEATURES;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestMeterFeaturesCase defaultBodyForComparison = new MultipartRequestMeterFeaturesCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPMETERFEATURES}
     */
    @Test
    @Ignore // table features are not working correctly in OVS so we have workaround
    public void testMakeMultipartRequestInputLongShortMultipartTypeTableFeatures(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPTABLEFEATURES;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestTableFeaturesCase defaultBodyForComparison = new MultipartRequestTableFeaturesCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPPORTDESC}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypePortDesc(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPPORTDESC;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestPortDescCase defaultBodyForComparison = new MultipartRequestPortDescCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType)}.
     * {@link MultipartType.OFPMPEXPERIMENTER}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeExperimenter(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPEXPERIMENTER;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type);
        final MultipartRequestExperimenterCase defaultBodyForComparison = new MultipartRequestExperimenterCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPDESC}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyDesc(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPDESC;
        final MultipartRequestDescCase body = new MultipartRequestDescCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPFLOW}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyFlow(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPFLOW;
        final MultipartRequestFlowCase body = new MultipartRequestFlowCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPAGGREGATE}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyAggr(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPAGGREGATE;
        final MultipartRequestAggregateCase body = new MultipartRequestAggregateCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPTABLE}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyTable(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPTABLE;
        final MultipartRequestTableCase body = new MultipartRequestTableCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPPORTSTATS}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyPortStat(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPPORTSTATS;
        final MultipartRequestPortStatsCase body = new MultipartRequestPortStatsCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPQUEUE}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyQueue(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPQUEUE;
        final MultipartRequestQueueCase body = new MultipartRequestQueueCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPGROUP}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyGroup(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPGROUP;
        final MultipartRequestGroupCase body = new MultipartRequestGroupCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPGROUPDESC}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyGrupDesc(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPGROUPDESC;
        final MultipartRequestGroupDescCase body = new MultipartRequestGroupDescCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPGROUPFEATURES}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyGroupFeaturs(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPGROUPFEATURES;
        final MultipartRequestGroupFeaturesCase body = new MultipartRequestGroupFeaturesCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPMETER}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyMeter(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPMETER;
        final MultipartRequestMeterCase body = new MultipartRequestMeterCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPMETERCONFIG}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyMeterConf(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPMETERCONFIG;
        final MultipartRequestMeterConfigCase body = new MultipartRequestMeterConfigCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPMETERFEATURES}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyMeterFeatures(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPMETERFEATURES;
        final MultipartRequestMeterFeaturesCase body = new MultipartRequestMeterFeaturesCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPMETERFEATURES}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyTableFeatures(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPTABLEFEATURES;
        final MultipartRequestTableFeaturesCase body = new MultipartRequestTableFeaturesCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPPORTDESC}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyPortDesc(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPPORTDESC;
        final MultipartRequestPortDescCase body = new MultipartRequestPortDescCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPEXPERIMENTER}
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyExperimenter(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPEXPERIMENTER;
        final MultipartRequestExperimenterCase body = new MultipartRequestExperimenterCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
        validation(multipartReqInput, xid, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * Validation for null input type expect IllegalArgumentException
     */
    @Test(expected=IllegalArgumentException.class)
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyNullType(){
        final long xid = 1l;
        final MultipartRequestDescCase body = new MultipartRequestDescCaseBuilder().build();
        MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, null, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * Validation for null input body expect IllegalArgumentException
     */
    @Test(expected=IllegalArgumentException.class)
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyNullBody(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPDESC;
        MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, null);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, MultipartRequestBody)}.
     * Validation for bad input body Class expect IllegalArgumentException
     */
    @Test(expected=IllegalArgumentException.class)
    public void testMakeMultipartRequestInputLongShortMultipartTypeMultipartRequestBodyBadType(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPEXPERIMENTER;
        final MultipartRequestDescCase body = new MultipartRequestDescCaseBuilder().build();
        MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, body);
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short, MultipartType, boolean)}.
     * {@link MultipartType.OFPMPDESC}
     * note: we are able to add next test suite for all MultipartType but I guess it is same as suite before
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeBoolean(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPDESC;
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, true);
        final MultipartRequestDescCase defaultBodyForComparison = new MultipartRequestDescCaseBuilder().build();
        validation(multipartReqInput, xid, type, defaultBodyForComparison);
        Assert.assertTrue(multipartReqInput.getFlags().isOFPMPFREQMORE());
    }

    /**
     * Test method for {@link MultipartRequestInputFactory#makeMultipartRequestInput(long, short,MultipartType, boolean, MultipartRequestBody)}.
     * {@link MultipartType.OFPMPDESC}
     * note: we are able to add next test suite for all MultipartType but I guess it is same as suite before
     */
    @Test
    public void testMakeMultipartRequestInputLongShortMultipartTypeBooleanMultipartRequestBody(){
        final long xid = 1l;
        final MultipartType type = MultipartType.OFPMPDESC;
        final MultipartRequestDescCase body = new MultipartRequestDescCaseBuilder().build();
        final MultipartRequestInput multipartReqInput = MultipartRequestInputFactory.makeMultipartRequestInput(xid, ofVersion, type, true, body);
        validation(multipartReqInput, xid, type, body);
        Assert.assertTrue(multipartReqInput.getFlags().isOFPMPFREQMORE());
    }

    private void validation(final MultipartRequestInput multipartReqInput, final long xid, final MultipartType type, final MultipartRequestBody body) {
        Assert.assertNotNull(multipartReqInput);
        Assert.assertEquals(xid, multipartReqInput.getXid().longValue());
        Assert.assertEquals(ofVersion, multipartReqInput.getVersion().shortValue());
        Assert.assertNotNull(multipartReqInput.getType());
        Assert.assertEquals(type, multipartReqInput.getType());
        Assert.assertNotNull(multipartReqInput.getMultipartRequestBody());
        Assert.assertEquals(body.getClass(), multipartReqInput.getMultipartRequestBody().getClass());
        Assert.assertEquals(body, multipartReqInput.getMultipartRequestBody());
    }
}
