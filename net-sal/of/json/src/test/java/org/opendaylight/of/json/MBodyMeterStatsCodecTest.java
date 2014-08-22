/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.mp.MBodyMeterStats;
import org.opendaylight.of.lib.mp.MBodyMeterStats.MeterBandStats;
import org.opendaylight.of.lib.mp.MBodyMutableMeterStats;
import org.opendaylight.of.lib.mp.MpBodyFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.json.MBodyMeterStatsCodec.ROOTS;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.dt.MeterId.SLOWPATH;
import static org.opendaylight.of.lib.mp.MultipartType.METER;
import static org.opendaylight.util.StringUtils.getFileContents;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;

/**
 * Unit tests for {@link MBodyMeterStatsCodec}.
 *
 * @author Shaila Shree
 */
public class MBodyMeterStatsCodecTest {

    private static final MBodyMeterStatsCodec codec = (MBodyMeterStatsCodec)
            OfJsonFactory.instance().codec(MBodyMeterStats.class);

    private static final String EXP_JSON_PATH = "org/opendaylight/of/json/v13/";

    private static final String MBODY_METER_STAT_JSON = "mbodyMeterStat.json";
    private static final String MBODY_METER_STATS_JSON = "mbodyMeterStats.json";

    // expected values
    private static final MeterId EXP_METER_ID = SLOWPATH;

    // meter stat 0
    private static final MeterId EXP_MI = EXP_METER_ID;
    private static final long EXP_FC = 1000l;
    private static final long EXP_PIC = 100l;
    private static final long EXP_BIC = 10000l;
    private static final long EXP_DUR = 100000l;
    private static final long EXP_DUR_NS = 1000000l;

    // meter stat 1
    private static final MeterId EXP_MI_1 = MeterId.valueOf(42l);
    private static final long EXP_FC_1 = 200l;
    private static final long EXP_PIC_1 = 2000l;
    private static final long EXP_BIC_1 = 20000l;
    private static final long EXP_DUR_1 = 200000l;
    private static final long EXP_DUR_NS_1 = 2000000l;

    // meter band stats
    private static final long EXP_MBS_A_PKT = 42l;
    private static final long EXP_MBS_A_BYTE = 7l;

    private static final long EXP_MBS_B_PKT = 9876543210l;
    private static final long EXP_MBS_B_BYTE = 1234567890l;

    private static final long EXP_MBS_C_PKT = 2420242l;
    private static final long EXP_MBS_C_BYTE = 4420442l;

    private static final long EXP_MBS_D_PKT = 112233445566778899l;
    private static final long EXP_MBS_D_BYTE = 998877665544332211l;

    private static final long EXP_MBS_E_PKT = 111122223333l;
    private static final long EXP_MBS_E_BYTE = 444455556666l;

    private final ClassLoader cl = getClass().getClassLoader();

    private String getJson(String name) throws IOException {
        return getFileContents(EXP_JSON_PATH + name, cl);
    }

    @Test
    public void encode() throws IOException {
        String exp = getJson(MBODY_METER_STAT_JSON);
        String actual = codec.encode(createMBodyMeterStats(V_1_3), true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decode() throws IOException {
        String actual = getJson(MBODY_METER_STAT_JSON);
        verifyMBodyMeterStats(codec.decode(actual));
        //Schema does not support singular form of port_stats
        //validate(actual, ROOT);
    }

    @Test
    public void encodeList() throws IOException {
        String exp = getJson(MBODY_METER_STATS_JSON);
        List<MBodyMeterStats> ms = new ArrayList<MBodyMeterStats>();

        ms.add(createMBodyMeterStats(V_1_3));
        ms.add(createMBodyMeterStats1(V_1_3));

        String actual = codec.encodeList(ms, true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decodeList() throws IOException {
        String actual = getJson(MBODY_METER_STATS_JSON);
        List<MBodyMeterStats> ms = codec.decodeList(actual);

        assertEquals(AM_NEQ, 2, ms.size());

        verifyMBodyMeterStats(ms.get(0));
        verifyMBodyMeterStats1(ms.get(1));

        validate(actual, ROOTS);
    }

    private MBodyMeterStats createMBodyMeterStats(ProtocolVersion version) {
        MBodyMutableMeterStats meterStats = (MBodyMutableMeterStats)
                MpBodyFactory.createReplyBodyElement(version, METER);

        meterStats.meterId(EXP_MI)
                  .flowCount(EXP_FC)
                  .packetInCount(EXP_PIC)
                  .byteInCount(EXP_BIC)
                  .duration(EXP_DUR, EXP_DUR_NS);

        meterStats.addMeterBandStat(new MeterBandStats(EXP_MBS_A_PKT,
                EXP_MBS_A_BYTE));
        meterStats.addMeterBandStat(new MeterBandStats(EXP_MBS_B_PKT,
                EXP_MBS_B_BYTE));

        return (MBodyMeterStats)meterStats.toImmutable();
    }

    private void verifyMBodyMeterStats(MBodyMeterStats mbs) {
        assertEquals(AM_NEQ, EXP_MI, mbs.getMeterId());
        assertEquals(AM_NEQ, EXP_FC, mbs.getFlowCount());
        assertEquals(AM_NEQ, EXP_PIC, mbs.getPktInCount());
        assertEquals(AM_NEQ, EXP_BIC, mbs.getByteInCount());
        assertEquals(AM_NEQ, EXP_DUR, mbs.getDurationSec());
        assertEquals(AM_NEQ, EXP_DUR_NS, mbs.getDurationNSec());

        assertEquals(AM_NEQ, 2, mbs.getBandStats().size());

        MeterBandStats mb = mbs.getBandStats().get(0);
        assertEquals(AM_NEQ, EXP_MBS_A_PKT, mb.getPacketBandCount());
        assertEquals(AM_NEQ, EXP_MBS_A_BYTE, mb.getByteBandCount());

        mb = mbs.getBandStats().get(1);
        assertEquals(AM_NEQ, EXP_MBS_B_PKT, mb.getPacketBandCount());
        assertEquals(AM_NEQ, EXP_MBS_B_BYTE, mb.getByteBandCount());
    }

    private MBodyMeterStats createMBodyMeterStats1(ProtocolVersion version) {
        MBodyMutableMeterStats  ms = (MBodyMutableMeterStats)
                MpBodyFactory.createReplyBodyElement(version, METER);

        ms.meterId(EXP_MI_1)
          .flowCount(EXP_FC_1)
          .packetInCount(EXP_PIC_1).byteInCount(EXP_BIC_1)
          .duration(EXP_DUR_1, EXP_DUR_NS_1);

        ms.addMeterBandStat(new MeterBandStats(EXP_MBS_C_PKT, EXP_MBS_C_BYTE));
        ms.addMeterBandStat(new MeterBandStats(EXP_MBS_D_PKT, EXP_MBS_D_BYTE));
        ms.addMeterBandStat(new MeterBandStats(EXP_MBS_E_PKT, EXP_MBS_E_BYTE));

        return (MBodyMeterStats)ms.toImmutable();
    }

    private void verifyMBodyMeterStats1(MBodyMeterStats mbs) {
        assertEquals(AM_NEQ, EXP_MI_1, mbs.getMeterId());
        assertEquals(AM_NEQ, EXP_FC_1, mbs.getFlowCount());
        assertEquals(AM_NEQ, EXP_PIC_1, mbs.getPktInCount());
        assertEquals(AM_NEQ, EXP_BIC_1, mbs.getByteInCount());
        assertEquals(AM_NEQ, EXP_DUR_1, mbs.getDurationSec());
        assertEquals(AM_NEQ, EXP_DUR_NS_1, mbs.getDurationNSec());

        assertEquals(AM_NEQ, 3, mbs.getBandStats().size());

        MeterBandStats mb = mbs.getBandStats().get(0);
        assertEquals(AM_NEQ, EXP_MBS_C_PKT, mb.getPacketBandCount());
        assertEquals(AM_NEQ, EXP_MBS_C_BYTE, mb.getByteBandCount());

        mb = mbs.getBandStats().get(1);
        assertEquals(AM_NEQ, EXP_MBS_D_PKT, mb.getPacketBandCount());
        assertEquals(AM_NEQ, EXP_MBS_D_BYTE, mb.getByteBandCount());

        mb = mbs.getBandStats().get(2);
        assertEquals(AM_NEQ, EXP_MBS_E_PKT, mb.getPacketBandCount());
        assertEquals(AM_NEQ, EXP_MBS_E_BYTE, mb.getByteBandCount());
    }
}

