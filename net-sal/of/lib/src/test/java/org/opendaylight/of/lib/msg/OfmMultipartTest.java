/*
 * (c) Copyright 2012-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.mp.*;
import org.opendaylight.util.SafeMap;

import static org.junit.Assert.*;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_WRCL;

/**
 * Base class for testing openflow multipart message classes.
 *
 * @author Simon Hunt
 */
public abstract class OfmMultipartTest extends OfmTest {

    protected static final String FMT_START_TARGET = "" +
            "** PARSING Buffer Start={}, Target={} **";

    // mapping of multipart type to concrete request class
    private static final SafeMap<MultipartType,
            Class<? extends MultipartBody>> REQUEST_CLS =
            new SafeMap.Builder<MultipartType,
                    Class<? extends MultipartBody>>(MultipartBody.class)
                    // DESC request has no body
                    .add(MultipartType.FLOW, MBodyFlowStatsRequest.class)
                    // TODO: AGGREGATE ?
                    // TABLE request has no body
                    .add(MultipartType.PORT_STATS, MBodyPortStatsRequest.class)
                    .add(MultipartType.QUEUE, MBodyQueueStatsRequest.class)
                    .add(MultipartType.GROUP, MBodyGroupStatsRequest.class)
                    // GROUP_DESC request has no body
                    // GROUP_FEATURES request has no body
                    .add(MultipartType.METER, MBodyMeterStatsRequest.class)
                    .add(MultipartType.METER_CONFIG,
                            MBodyMeterConfigRequest.class)
                    // METER_FEATURES request has no body
                    .add(MultipartType.TABLE_FEATURES,
                            MBodyTableFeatures.Array.class)
                    // PORT_DESC request has no body
                    .add(MultipartType.EXPERIMENTER, MBodyExperimenter.class)
                    .build();

    // mapping of multipart type to concrete reply class
    private static final SafeMap<MultipartType,
            Class<? extends MultipartBody>> REPLY_CLS =
            new SafeMap.Builder<MultipartType,
                    Class<? extends MultipartBody>>(MultipartBody.class)
                    .add(MultipartType.DESC, MBodyDesc.class)
                    .add(MultipartType.FLOW, MBodyFlowStats.Array.class)
                    // TODO: AGGRGATE ?
                    .add(MultipartType.TABLE, MBodyTableStats.Array.class)
                    .add(MultipartType.PORT_STATS, MBodyPortStats.Array.class)
                    .add(MultipartType.QUEUE, MBodyQueueStats.Array.class)
                    .add(MultipartType.GROUP, MBodyGroupStats.Array.class)
                    .add(MultipartType.GROUP_DESC,
                         MBodyGroupDescStats.Array.class)
                    .add(MultipartType.GROUP_FEATURES, MBodyGroupFeatures.class)
                    .add(MultipartType.METER, MBodyMeterStats.Array.class)
                    .add(MultipartType.METER_CONFIG, MBodyMeterConfig.Array.class)
                    .add(MultipartType.METER_FEATURES, MBodyMeterFeatures.class)
                    .add(MultipartType.TABLE_FEATURES,
                            MBodyTableFeatures.Array.class)
                    .add(MultipartType.PORT_DESC, MBodyPortDesc.Array.class)
                    .add(MultipartType.EXPERIMENTER, MBodyExperimenter.class)
                    .build();


    /** Verifies the multipart (request) header data.
     *
     * @param msg the multipart request message to test
     * @param expType the expected multipart type
     * @param expFlags the expected multipart request flags
     * @return the body of the multipart message
     */
    MultipartBody verifyMpHeader(OfmMultipartRequest msg, MultipartType expType,
                                MultipartRequestFlag... expFlags) {
        assertEquals(AM_NEQ, expType, msg.getMultipartType());
        verifyFlags(msg.getFlags(), expFlags);

        Class<? extends MultipartBody> bodyLookup = REQUEST_CLS.get(expType);
        Class<? extends MultipartBody> expReqBodyCls =
                bodyLookup == MultipartBody.class ? null : bodyLookup;

        MultipartBody body = msg.getBody();
        if (expReqBodyCls == null)
            assertNull("Unexpected body", body);
        else
            assertTrue(AM_WRCL, expReqBodyCls.isInstance(body));
        return body;
    }

    /** Verifies the multipart (reply) header data.
     *
     * @param msg the multipart reply message to test
     * @param expType the expected multipart type
     * @param expFlags the expected multipart reply flags
     * @return body of the multipart message
     */
    MultipartBody verifyMpHeader(OfmMultipartReply msg, MultipartType expType,
                                MultipartReplyFlag... expFlags) {
        assertEquals(AM_NEQ, expType, msg.getMultipartType());
        verifyFlags(msg.getFlags(), expFlags);

        Class<? extends MultipartBody> expRepBodyCls = REPLY_CLS.get(expType);

        if (expRepBodyCls ==  MultipartBody.class)
            fail("Need to update the body class map");

        MultipartBody body = msg.getBody();
        assertTrue(AM_WRCL, expRepBodyCls.isInstance(body));
        return body;
    }

}
