/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.mp.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.mp.MultipartType.METER_CONFIG;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.of.lib.msg.MeterBandFactory.createBand;
import static org.opendaylight.of.lib.msg.MeterFlag.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the OfmMultipartRequest and OfmMultipartReply messages of
 * type MultipartType.METER_CONFIG.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfmMultipartMeterConfigTest extends OfmMultipartTest {

    // test files
    private static final String TF_REQ_13 = "v13/mpRequestMeterConfig";
    private static final String TF_REPLY_13 = "v13/mpReplyMeterConfig";
    private static final String TF_REPLY_13_TWICE = "v13/mpReplyMeterConfigTwice";

    // expected values
    private static final MeterId EXP_METER_ID = MeterId.CONTROLLER;
    private static final MeterId EXP_REPLY_METER_ID_1 = MeterId.valueOf(23l);
    private static final MeterFlag[] EXP_FLAGS_1 = {PKTPS, STATS};
    private static final Set<MeterFlag> EXP_FLAG_SET_1 =
            new TreeSet<MeterFlag>(Arrays.asList(EXP_FLAGS_1));
    private static final int EXP_BAND_SIZE_1 = 1;

    private static final MeterId EXP_REPLY_METER_ID_2 = MeterId.valueOf(51l);
    private static final MeterFlag[] EXP_FLAGS_2 = {BURST};
    private static final Set<MeterFlag> EXP_FLAG_SET_2 =
            new TreeSet<MeterFlag>(Arrays.asList(EXP_FLAGS_2));
    private static final int EXP_BAND_SIZE_2 = 2;

    // meter band expected data
    private static final long DROP_PKT_RATE = 4000l;
    private static final long BRST_SIZE_2048 = 2048l;
    private static final long RMK_PKT_RATE_2048 = 2048l;
    private static final long RMK_PKT_RATE_1024 = 1024l;

    private static final int PREC_LVL_3 = 3;
    private static final int PREC_LVL_2 = 2;

    // provide asserts for a given meter body config entry
    private void checkMeterConfig(MBodyMeterConfig cfg, MeterId expMeterId,
                                  Set<MeterFlag> expFlags, int expBandSize) {
        assertEquals(AM_NEQ, expMeterId, cfg.getMeterId());
        assertEquals(AM_NEQ, expFlags, cfg.getFlags());
        assertEquals(AM_NEQ, expBandSize, cfg.getBands().size());
    }

    // provide asserts for meter bands of type drop
    private void checkDropMeterBand(MeterBand src, long expDropRate,
                                    long expBurst) {
        assertEquals(AM_NEQ, MeterBandType.DROP, src.getType());
        assertEquals(AM_NEQ, expDropRate, src.getRate());
        assertEquals(AM_NEQ, expBurst, src.getBurstSize());
    }

    // provide asserts for meter bands of type dscp
    private void checkDscpMeterBand(MeterBand src, long expRemarkRate,
                                    long expBurst, int expPrec) {
        assertEquals(AM_NEQ, MeterBandType.DSCP_REMARK, src.getType());
        assertEquals(AM_NEQ, expRemarkRate, src.getRate());
        assertEquals(AM_NEQ, expBurst, src.getBurstSize());
        assertEquals(AM_NEQ, expPrec, ((MeterBandDscpRemark)src).getPrecLevel());
    }

    //===================================================== PARSING ==========

    @Test
    public void mpRequestMeterConfig13() {
        print(EOL + "mpRequestMeterConfig13()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_13, V_1_3, MULTIPART_REQUEST, 24);
        MBodyMeterConfigRequest body = (MBodyMeterConfigRequest)
                verifyMpHeader(msg, METER_CONFIG);
        assertEquals(AM_NEQ, EXP_METER_ID, body.getMeterId());
    }

    @Test
    public void mpReplyMeterConfig13() throws MessageParseException {
        print(EOL + "mpReplyMeterConfig13()");
        OfPacketReader pkt = getOfmTestReader(TF_REPLY_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyMeterConfig13(m);
    }


    @Test
    public void mpReplyMeterConfig13Twice() throws MessageParseException {
        print(EOL + "mpReplyMeterConfig13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_REPLY_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyMeterConfig13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyMeterConfig13(m);
    }

    private void validateReplyMeterConfig13(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 80, m.length());

        OfmMultipartReply msg = (OfmMultipartReply) m;

        MBodyMeterConfig.Array body =
                (MBodyMeterConfig.Array) verifyMpHeader(msg, METER_CONFIG);

        List<MBodyMeterConfig> cfgs = body.getList();
        assertEquals(AM_UXS, 2, cfgs.size());

        Iterator<MBodyMeterConfig> iterator = cfgs.iterator();

        MBodyMeterConfig meterConfig = iterator.next();
        checkMeterConfig(meterConfig, EXP_REPLY_METER_ID_1, EXP_FLAG_SET_1,
                EXP_BAND_SIZE_1);
        checkDropMeterBand(meterConfig.getBands().get(0), DROP_PKT_RATE,
                BRST_SIZE_2048);

        meterConfig = iterator.next();
        checkMeterConfig(meterConfig, EXP_REPLY_METER_ID_2, EXP_FLAG_SET_2,
                EXP_BAND_SIZE_2);
        checkDscpMeterBand(meterConfig.getBands().get(0), RMK_PKT_RATE_2048,
                BRST_SIZE_2048, PREC_LVL_3) ;
        checkDscpMeterBand(meterConfig.getBands().get(1), RMK_PKT_RATE_1024,
                BRST_SIZE_2048, PREC_LVL_2);
    }

    // NOTE: Meter Config not supported in 1.0, 1.1, 1.2

    //======================================== Creating / Encoding  ==========

    @Test
    public void encodeMpRequestConfig13() {
        print(EOL + "encodeMpRequestConfig13()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.clearXid();

        MBodyMutableMeterConfigRequest body = (MBodyMutableMeterConfigRequest)
                MpBodyFactory.createRequestBody(V_1_3, METER_CONFIG);
        body.meterId(EXP_METER_ID);
        req.body((MultipartBody) body.toImmutable());

        // encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_13);
    }

    @Test
    public void encodeMpRequestConfig13withMpType() {
        print(EOL + "encodeMpRequestConfig13withMpType()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, METER_CONFIG);
        req.clearXid();

        MBodyMutableMeterConfigRequest body =
                (MBodyMutableMeterConfigRequest) req.getBody();
        body.meterId(EXP_METER_ID);

        req.body((MultipartBody) body.toImmutable());
        // encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_13);
    }

    @Test
    public void encodeMpReplyMeterConfig13()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyMeterConfig13()");
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY);
        reply.clearXid();

        MBodyMeterConfig.MutableArray array = (MBodyMeterConfig.MutableArray)
                MpBodyFactory.createReplyBody(V_1_3, METER_CONFIG);
        fillMeterConfigArray(array);
        reply.body((MultipartBody) array.toImmutable());
        encodeAndVerifyMessage(reply.toImmutable(), TF_REPLY_13);
    }

    @Test
    public void encodeMpReplyMeterConfig13withMpType()
            throws IncompleteStructureException {
        print(EOL + "encodeMpReplyMeterConfig13withMpType()");

        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, METER_CONFIG);
        rep.clearXid();

        MBodyMeterConfig.MutableArray array =
                (MBodyMeterConfig.MutableArray) rep.getBody();
        fillMeterConfigArray(array);
        encodeAndVerifyMessage(rep.toImmutable(), TF_REPLY_13);
    }

    // NOTE: Meter Config not supported in 1.0, 1.1, 1.2

    @Test(expected = VersionMismatchException.class)
    public void mismatchVersion10() {
        MessageFactory.create(V_1_0, MULTIPART_REPLY, METER_CONFIG);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void mismatchVersion11() {
        MessageFactory.create(V_1_1, MULTIPART_REPLY, METER_CONFIG);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void mismatchVersion12() {
        MessageFactory.create(V_1_2, MULTIPART_REPLY, METER_CONFIG);
    }

    // populates the contents of the Meter Config message
    private void fillMeterConfigArray(MBodyMeterConfig.MutableArray array)
            throws IncompleteStructureException {

        MBodyMutableMeterConfig meterCfg = (MBodyMutableMeterConfig)
                MpBodyFactory.createReplyBodyElement(V_1_3, METER_CONFIG);

        meterCfg.meterId(EXP_REPLY_METER_ID_1).meterFlags(EXP_FLAG_SET_1)
                .addBand(createBand(V_1_3, MeterBandType.DROP, DROP_PKT_RATE,
                        BRST_SIZE_2048));
        array.addMeterConfigs((MBodyMeterConfig) meterCfg.toImmutable());

        meterCfg = (MBodyMutableMeterConfig)
                MpBodyFactory.createReplyBodyElement(V_1_3, METER_CONFIG);

        meterCfg.meterId(EXP_REPLY_METER_ID_2).meterFlags(EXP_FLAG_SET_2)
                .addBand(createBand(V_1_3, MeterBandType.DSCP_REMARK,
                        RMK_PKT_RATE_2048, BRST_SIZE_2048, PREC_LVL_3))
                .addBand(createBand(V_1_3, MeterBandType.DSCP_REMARK,
                        RMK_PKT_RATE_1024, BRST_SIZE_2048, PREC_LVL_2));
        array.addMeterConfigs((MBodyMeterConfig) meterCfg.toImmutable());
    }
}
