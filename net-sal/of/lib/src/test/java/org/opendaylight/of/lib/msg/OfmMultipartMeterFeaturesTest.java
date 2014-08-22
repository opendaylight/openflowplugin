/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.VersionNotSupportedException;
import org.opendaylight.of.lib.mp.MBodyMeterFeatures;
import org.opendaylight.of.lib.mp.MBodyMutableMeterFeatures;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.mp.MultipartType.METER_FEATURES;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the OfmMultipartRequest and OfmMultipartReply message of
 * type METER_FEATURES, and the constituent parts.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfmMultipartMeterFeaturesTest extends OfmMultipartTest {

    // Test files
    private static final String TF_REQ_MF_13 = "v13/mpRequestMeterFeatures";
    private static final String TF_REPLY_MF_13 = "v13/mpReplyMeterFeatures";

    // ====== Expected values
    private static final long EXP_MAX_METERS = 17l;

    private static final Set<MeterFlag> EXP_FLAGS =
            new TreeSet<MeterFlag>(Arrays.asList(MeterFlag.KBPS,
                    MeterFlag.PKTPS, MeterFlag.BURST, MeterFlag.STATS));

    private static final Set<MeterBandType> EXP_METER_BAND_SET =
            new TreeSet<MeterBandType>(Arrays.asList(MeterBandType.DROP,
                    MeterBandType.DSCP_REMARK));

    private static final int EXP_MAX_BANDS = 21;
    private static final int EXP_MAX_COLORS = 12;

    private static final long U32_PLUS_ONE = 0xffffffffL + 1;
    private static final int U8_PLUS_ONE = 0xff + 1;

    // ========================================================= PARSING ====

    @Test
    public void mpRequestMeterFeatures13() {
        print(EOL + "mpRequestMeterFeatures13()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_MF_13, V_1_3, MULTIPART_REQUEST, 16);
        verifyMpHeader(msg, METER_FEATURES);
    }

    @Test
    public void mpReplyMeterFeatures13() {
        print(EOL + "mpReplyMeterFeatures13()");
        OfmMultipartReply rep = (OfmMultipartReply)
                verifyMsgHeader(TF_REPLY_MF_13, V_1_3, MULTIPART_REPLY, 32);
        MBodyMeterFeatures body =
                (MBodyMeterFeatures) verifyMpHeader(rep, METER_FEATURES);
        print(body);
        assertEquals(AM_NEQ, EXP_MAX_METERS, body.getMaxMeters());
        assertEquals(AM_NEQ, EXP_METER_BAND_SET, body.getBandTypes());
        assertEquals(AM_NEQ, EXP_FLAGS, body.getCapabilities());
        assertEquals(AM_NEQ, EXP_MAX_BANDS, body.getMaxBands());
        assertEquals(AM_NEQ, EXP_MAX_COLORS, body.getMaxColor());
    }


    // NOTE: Multipart METER_FEATURES not defined in 1.0, 1,1, or 1.2

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMpRequestMeterFeatures13WithMpType() {
        print(EOL + "encodeMpRequestMeterFeatures13WithMpType()");

        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, METER_FEATURES);
        req.clearXid();

        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_MF_13);
    }

    @Test
    public void encodeMpReplyMeterFeatures13WithMpType() {
        print(EOL + "encodeMpReplyMeterFeatures13WithMpType()");
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, METER_FEATURES);
        reply.clearXid();

        MBodyMutableMeterFeatures features =
                (MBodyMutableMeterFeatures) reply.getBody();
        features.maxMeters(EXP_MAX_METERS).bandTypes(EXP_METER_BAND_SET)
                .capabilities(EXP_FLAGS).maxBands(EXP_MAX_BANDS)
                .maxColor(EXP_MAX_COLORS);

        reply.body((MultipartBody) features.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(reply.toImmutable(), TF_REPLY_MF_13);
    }

    @Test
    public void encodeMpReplyMeterFeatures13() {
        print(EOL + "encodeMpReplyMeterFeatures13()");
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY);
        reply.clearXid();

        MBodyMutableMeterFeatures body = (MBodyMutableMeterFeatures)
                MpBodyFactory.createReplyBody(V_1_3, METER_FEATURES);

        body.maxMeters(EXP_MAX_METERS).bandTypes(EXP_METER_BAND_SET)
                .capabilities(EXP_FLAGS).maxBands(EXP_MAX_BANDS)
                .maxColor(EXP_MAX_COLORS);

        reply.body((MultipartBody) body.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(reply.toImmutable(), TF_REPLY_MF_13);
    }

    // NOTE: Multipart METER_FEATURES not defined in 1.0, 1,1, or 1.2

    @Test(expected = VersionNotSupportedException.class)
    public void encodeMpRequestMeterFeatures12() {
        MessageFactory.create(V_1_2, MULTIPART_REQUEST, METER_FEATURES);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeMpRequestMeterFeatures11() {
        MessageFactory.create(V_1_1, MULTIPART_REQUEST, METER_FEATURES);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeMpRequestMeterFeatures10() {
        MessageFactory.create(V_1_0, MULTIPART_REQUEST, METER_FEATURES);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeMpReplyMeterFeatures12() {
        MessageFactory.create(V_1_2, MULTIPART_REPLY, METER_FEATURES);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeMpReplyMeterFeatures11() {
        MessageFactory.create(V_1_1, MULTIPART_REPLY, METER_FEATURES);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeMpReplyMeterFeatures10() {
        MessageFactory.create(V_1_0, MULTIPART_REPLY, METER_FEATURES);
    }

    // ================================================= BOUNDS CHECKING ====

    @Test(expected = IllegalArgumentException.class)
    public void maxMetersNegative() {
        new MBodyMutableMeterFeatures(V_1_3).maxMeters(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void maxMetersNotU32() {
        new MBodyMutableMeterFeatures(V_1_3).maxMeters(U32_PLUS_ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void maxBandsNegative() {
        new MBodyMutableMeterFeatures(V_1_3).maxBands(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void maxBandsNotU8() {
        new MBodyMutableMeterFeatures(V_1_3).maxBands(U8_PLUS_ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void maxColorNegative() {
        new MBodyMutableMeterFeatures(V_1_3).maxColor(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void maxColorNotU8() {
        new MBodyMutableMeterFeatures(V_1_3).maxColor(U8_PLUS_ONE);
    }

}
