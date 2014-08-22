/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.mp.MBodyFlowStats;
import org.opendaylight.of.lib.mp.MBodyMutableFlowStats;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.msg.FlowModFlag;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.json.ActionCodecTestUtils.*;
import static org.opendaylight.of.json.InstructionCodecTestUtils.*;
import static org.opendaylight.of.json.MatchFieldCodecTestUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.FLOW;
import static org.opendaylight.of.lib.msg.FlowModFlag.*;
import static org.opendaylight.util.StringUtils.getFileContents;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MBodyFlowStatsCodec}.
 *
 * @author Shaila Shree
 * @author Simon Hunt
 */
public class MBodyFlowStatsCodecTest extends AbstractCodecTest {
    private static final MBodyFlowStatsCodec codec = (MBodyFlowStatsCodec)
            OfJsonFactory.instance().codec(MBodyFlowStats.class);

    private static final String EXP_JSON_PATH = "org/opendaylight/of/json/";
    private static final String V10 = "v10";
    private static final String V13 = "v13";
    private static final String MOBODY_FLOWSTAT = "mbodyFlowStat.json";
    private static final String MOBODY_FLOWSTAT_NO_COUNTERS =
            "mbodyFlowStatNoCounters.json";
    private static final String MOBODY_FLOWSTATS = "mbodyFlowStats.json";

    private static final TableId EXP_TABLE_ID = tid(3);
    private static final int EXP_DURATION = 300;
    private static final int EXP_NANO_DURATION = 4000;
    private static final int EXP_PRIORITY = 4;
    private static final int EXP_IDLE_TIMEOUT = 60;
    private static final int EXP_HARD_TIMEOUT = 100;
    private static final long EXP_COOKIE = 0x1234;
    private static final long EXP_PACKET_COUNT = 2341;
    private static final long EXP_BYTE_COUNT = 134232425;

    private static final long NO_COUNTER = -1;

    private static final Set<FlowModFlag> EXP_FLAGS =
            new HashSet<FlowModFlag>(Arrays.asList(
                    SEND_FLOW_REM, NO_PACKET_COUNTS, NO_BYTE_COUNTS
            ));

    private static final TableId EXP_TABLE_ID_1 = tid(0);
    private static final int EXP_DURATION_1 = 200;
    private static final int EXP_NANO_DURATION_1 = 5000;
    private static final int EXP_PRIORITY_1 = 7;
    private static final int EXP_IDLE_TIMEOUT_1 = 20;
    private static final int EXP_HARD_TIMEOUT_1 = 200;
    private static final long EXP_COOKIE_1 = 0x1434;
    private static final long EXP_PACKET_COUNT_1 = 231;
    private static final long EXP_BYTE_COUNT_1 = 132311425;

    private static final Set<FlowModFlag> EXP_FLAGS_1 =
            new HashSet<FlowModFlag>(Arrays.asList(
                    SEND_FLOW_REM, NO_PACKET_COUNTS
            ));

    private final ClassLoader cl = getClass().getClassLoader();

    private String getJson(String dir, String name) throws IOException {
        return getFileContents(EXP_JSON_PATH + dir + "/" + name, cl);
    }

    @Test
    public void encode() throws IOException {
        String exp = getJson(V13, MOBODY_FLOWSTAT);
        String actual = codec.encode(createMBodyFlowStats(V_1_3), true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void encodeWithNaCounters() throws IOException {
        print(EOL + "encodeWithNaCounters()");
        String exp = getJson(V13, MOBODY_FLOWSTAT_NO_COUNTERS);
        String actual = codec.encode(createMBodyFlowStats(V_1_3, true), true);
        print(actual);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decode() throws IOException {
        String actual = getJson(V13, MOBODY_FLOWSTAT);
        MBodyFlowStats mbfs = codec.decode(actual);
        verifyMBodyFlowStats(V_1_3, mbfs);
    }

    @Test
    public void encodeListV10() throws IOException {
        List<MBodyFlowStats> flowStatsList = new ArrayList<MBodyFlowStats>();
        flowStatsList.add(createMBodyFlowStats(V_1_0));
        flowStatsList.add(createMBodyFlowStats1(V_1_0));

        String exp = getJson(V10, MOBODY_FLOWSTATS);
        String actual = codec.encodeList(flowStatsList, true);

        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decodeListV10() throws IOException {
        String actual = getJson(V10, MOBODY_FLOWSTATS);
        List<MBodyFlowStats> mBodyFlowStatsList = codec.decodeList(actual);

        assertEquals(AM_NEQ, 2, mBodyFlowStatsList.size());
        verifyMBodyFlowStats(V_1_0, mBodyFlowStatsList.get(0));
        verifyMBodyFlowStats1(V_1_0, mBodyFlowStatsList.get(1));
    }

    @Test
    public void encodeListV13() throws IOException {
        List<MBodyFlowStats> flowStatsList = new ArrayList<MBodyFlowStats>();
        flowStatsList.add(createMBodyFlowStats(V_1_3));
        flowStatsList.add(createMBodyFlowStats1(V_1_3));

        String exp = getJson(V13, MOBODY_FLOWSTATS);
        String actual = codec.encodeList(flowStatsList, true);

        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decodeListV13() throws IOException {
        String actual = getJson(V13, MOBODY_FLOWSTATS);
        List<MBodyFlowStats> mBodyFlowStatsList = codec.decodeList(actual);

        assertEquals(AM_NEQ, 2, mBodyFlowStatsList.size());
        verifyMBodyFlowStats(V_1_3, mBodyFlowStatsList.get(0));
        verifyMBodyFlowStats1(V_1_3, mBodyFlowStatsList.get(1));
    }

    private MBodyFlowStats createMBodyFlowStats(ProtocolVersion version) {
        return createMBodyFlowStats(version, false);
    }

    private MBodyFlowStats createMBodyFlowStats(ProtocolVersion version,
                                                boolean noCounters) {
        MBodyMutableFlowStats mbfs = (MBodyMutableFlowStats)
                MpBodyFactory.createReplyBodyElement(version, FLOW);

        mbfs.tableId(EXP_TABLE_ID)
                .duration(EXP_DURATION, EXP_NANO_DURATION)
                .priority(EXP_PRIORITY)
                .idleTimeout(EXP_IDLE_TIMEOUT)
                .hardTimeout(EXP_HARD_TIMEOUT)
                .cookie(EXP_COOKIE)
                .packetCount(noCounters ? NO_COUNTER : EXP_PACKET_COUNT)
                .byteCount(noCounters ? NO_COUNTER : EXP_BYTE_COUNT)
                .match(createSampleMatchA(version));

        if (version.equals(V_1_3)) {
            mbfs.flags(EXP_FLAGS);
            mbfs.instructions(createRandomInstructions1());
        } else {
            mbfs.actions(createRandomActions1(version));
        }

        return (MBodyFlowStats) mbfs.toImmutable();
    }

    private void verifyMBodyFlowStats(ProtocolVersion version,
                                      MBodyFlowStats mbfs) {
        if (version.equals(V_1_3)) {
            assertEquals(AM_NEQ, EXP_TABLE_ID, mbfs.getTableId());
        }
        assertEquals(AM_NEQ, EXP_DURATION, mbfs.getDurationSec());
        assertEquals(AM_NEQ, EXP_PRIORITY, mbfs.getPriority());
        assertEquals(AM_NEQ, EXP_NANO_DURATION, mbfs.getDurationNsec());
        assertEquals(AM_NEQ, EXP_IDLE_TIMEOUT, mbfs.getIdleTimeout());
        assertEquals(AM_NEQ, EXP_HARD_TIMEOUT, mbfs.getHardTimeout());
        assertEquals(AM_NEQ, EXP_COOKIE, mbfs.getCookie());
        assertEquals(AM_NEQ, EXP_PACKET_COUNT, mbfs.getPacketCount());
        assertEquals(AM_NEQ, EXP_BYTE_COUNT, mbfs.getByteCount());
        verifySampleMatchA(version, mbfs.getMatch());

        if (version.equals(V_1_3)) {
            assertEquals(AM_NEQ, EXP_FLAGS, mbfs.getFlags());
            assertEquals(AM_NEQ, null, mbfs.getActions());
            verifyRandomInstructions1(mbfs.getInstructions());
        }
        else {
            assertEquals(AM_NEQ, null, mbfs.getFlags());
            assertEquals(AM_NEQ, null, mbfs.getInstructions());
            verifyRandomActions1(version, mbfs.getActions());
        }
    }

    private MBodyFlowStats createMBodyFlowStats1(ProtocolVersion version) {
        MBodyMutableFlowStats mbfs = (MBodyMutableFlowStats)
                MpBodyFactory.createReplyBodyElement(version, FLOW);

        mbfs.tableId(EXP_TABLE_ID_1)
            .duration(EXP_DURATION_1, EXP_NANO_DURATION_1)
            .priority(EXP_PRIORITY_1)
            .idleTimeout(EXP_IDLE_TIMEOUT_1)
            .hardTimeout(EXP_HARD_TIMEOUT_1)
            .cookie(EXP_COOKIE_1)
            .packetCount(EXP_PACKET_COUNT_1)
            .byteCount(EXP_BYTE_COUNT_1)
            .match(createSampleMatchB(version));

        if (version.equals(V_1_3)) {
            mbfs.flags(EXP_FLAGS_1);
            mbfs.instructions(createRandomInstructions2());
        } else{
            mbfs.actions(createRandomActions2(version));
        }

        return (MBodyFlowStats) mbfs.toImmutable();
    }


    private void verifyMBodyFlowStats1(ProtocolVersion version,
                                       MBodyFlowStats mbfs) {
        assertEquals(AM_NEQ, EXP_DURATION_1, mbfs.getDurationSec());
        assertEquals(AM_NEQ, EXP_PRIORITY_1, mbfs.getPriority());
        assertEquals(AM_NEQ, EXP_NANO_DURATION_1, mbfs.getDurationNsec());
        assertEquals(AM_NEQ, EXP_IDLE_TIMEOUT_1, mbfs.getIdleTimeout());
        assertEquals(AM_NEQ, EXP_HARD_TIMEOUT_1, mbfs.getHardTimeout());
        assertEquals(AM_NEQ, EXP_NANO_DURATION_1, mbfs.getDurationNsec());
        assertEquals(AM_NEQ, EXP_COOKIE_1, mbfs.getCookie());
        assertEquals(AM_NEQ, EXP_PACKET_COUNT_1, mbfs.getPacketCount());
        assertEquals(AM_NEQ, EXP_BYTE_COUNT_1, mbfs.getByteCount());
        verifySampleMatchB(version, mbfs.getMatch());

        if (version.equals(V_1_3)) {
            assertEquals(AM_NEQ, EXP_FLAGS_1, mbfs.getFlags());
            assertEquals(AM_NEQ, null, mbfs.getActions());
            verifyRandomInstructions2(mbfs.getInstructions());
            assertEquals(AM_NEQ, EXP_TABLE_ID_1, mbfs.getTableId());
        }
        else {
            assertEquals(AM_NEQ, null, mbfs.getFlags());
            assertEquals(AM_NEQ, null, mbfs.getInstructions());
            verifyRandomActions2(version, mbfs.getActions());
            assertEquals(AM_NEQ, null, mbfs.getTableId());
        }
    }
}


