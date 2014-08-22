/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.Instruction;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.json.JsonFactory;
import org.opendaylight.util.net.BigPortNumber;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.json.ActionCodecTestUtils.*;
import static org.opendaylight.of.json.InstructionCodecTestUtils.*;
import static org.opendaylight.of.json.MatchFieldCodecTestUtils.*;
import static org.opendaylight.of.json.OfmFlowModCodec.ROOT;
import static org.opendaylight.of.json.OfmFlowModCodec.ROOTS;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.FlowModCommand.ADD;
import static org.opendaylight.of.lib.msg.FlowModCommand.DELETE_STRICT;
import static org.opendaylight.of.lib.msg.FlowModFlag.*;
import static org.opendaylight.util.StringUtils.getFileContents;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;


/**
 * Unit tests for {@link OfmFlowModCodec}.
 * @author Shaila Shree
 *
 */
public class OfmFlowModCodecTest extends AbstractCodecTest {
    private static final JsonFactory factory = OfJsonFactory.instance();
    private static final OfmFlowModCodec codec = (OfmFlowModCodec)
            factory.codec(OfmFlowMod.class);

    private static final String EXP_JSON_PATH = "org/opendaylight/of/json/";
    private static final String V10 = "v10";
    private static final String V13 = "v13";
    private static final String FLOW_MOD = "ofmFlowMod.json";
    private static final String FLOW_MODS = "ofmFlowMods.json";

    private static final TableId EXP_TABLE_ID = tid(3);
    private static final int EXP_PRIORITY = 4;
    private static final int EXP_IDLE_TIMEOUT = 60;
    private static final int EXP_HARD_TIMEOUT = 100;
    private static final long EXP_COOKIE = 0x1234;
    private static final FlowModCommand EXP_FLOW_CMD = ADD;
    private static final long EXP_COOKIE_MASK = 0xffff;
    private static final BufferId EXP_BUFFER_ID = bid(7);
    private static final BigPortNumber EXP_OUT_PORT = bpn(25);
    private static final GroupId EXP_OUT_GROUP = gid(8);
    private static final Set<FlowModFlag> EXP_FLAGS =
            new HashSet<FlowModFlag>(Arrays.asList(
                    SEND_FLOW_REM, NO_PACKET_COUNTS, NO_BYTE_COUNTS
            ));

    private static final TableId EXP_TABLE_ID_1 = tid(0);
    private static final int EXP_PRIORITY_1 = 2;
    private static final int EXP_IDLE_TIMEOUT_1 = 40;
    private static final int EXP_HARD_TIMEOUT_1 = 300;
    private static final long EXP_COOKIE_1 = 0x1254;
    private static final FlowModCommand EXP_FLOW_CMD_1 = DELETE_STRICT;
    private static final long EXP_COOKIE_MASK_1 = 0xfff0;
    private static final BufferId EXP_BUFFER_ID_1 = bid(5);
    private static final BigPortNumber EXP_OUT_PORT_1 =
            BigPortNumber.valueOf(50);
    private static final GroupId EXP_OUT_GROUP_1 = gid(10);
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
        String exp = getJson(V13, FLOW_MOD);
        String actual = codec.encode(createOfmFlowMod(V_1_3), true);

        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, ROOT);
    }

    @Test
    public void decode() throws IOException {
        String actual = getJson(V13, FLOW_MOD);
        verifyOfmFlowMod(V_1_3, codec.decode(actual));
    }

    @Test
    public void encodeListV10() throws IOException {
        List<OfmFlowMod> flowMods = new ArrayList<OfmFlowMod>();
        flowMods.add(createOfmFlowMod(V_1_0));
        flowMods.add(createOfmFlowMod1(V_1_0));

        String exp = getJson(V10, FLOW_MODS);
        String actual = codec.encodeList(flowMods, true);

        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, ROOTS);
    }

    @Test
    public void decodeListV10() throws IOException {
        String actual = getJson(V10, FLOW_MODS);
        List<OfmFlowMod> flowMods = codec.decodeList(actual);

        verifyOfmFlowMod(V_1_0, flowMods.get(0));
        verifyOfmFlowMod1(V_1_0, flowMods.get(1));
    }

    @Test
    public void encodeListV13() throws IOException {
        List<OfmFlowMod> flowMods = new ArrayList<OfmFlowMod>();
        flowMods.add(createOfmFlowMod(V_1_3));
        flowMods.add(createOfmFlowMod1(V_1_3));

        String exp = getJson(V13, FLOW_MODS);
        String actual = codec.encodeList(flowMods, true);

        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
        validate(actual, ROOTS);
    }

    @Test
    public void decodeListV13() throws IOException {
        String actual = getJson(V13, FLOW_MODS);
        List<OfmFlowMod> flowMods = codec.decodeList(actual);

        verifyOfmFlowMod(V_1_3, flowMods.get(0));
        verifyOfmFlowMod1(V_1_3, flowMods.get(1));
    }

    private OfmFlowMod createOfmFlowMod(ProtocolVersion version) {
        OfmMutableFlowMod flowMod = (OfmMutableFlowMod)
                MessageFactory.create(version, MessageType.FLOW_MOD);

        flowMod.cookie(EXP_COOKIE)
               .idleTimeout(EXP_IDLE_TIMEOUT)
               .hardTimeout(EXP_HARD_TIMEOUT)
               .priority(EXP_PRIORITY)
               .bufferId(EXP_BUFFER_ID)
               .outPort(EXP_OUT_PORT)
               .flowModFlags(EXP_FLAGS)
               .command(EXP_FLOW_CMD)
               .match(MatchFieldCodecTestUtils.createSampleMatchA(version));

        if (version.equals(V_1_3)) {
            flowMod.cookieMask(EXP_COOKIE_MASK);
            flowMod.outGroup(EXP_OUT_GROUP);
            flowMod.tableId(EXP_TABLE_ID);

            List<Instruction> instructions =
                    InstructionCodecTestUtils.createRandomInstructions1();

            for (Instruction instruction: instructions)
                flowMod.addInstruction(instruction);
        } else {
            List<Action> actions =
                    ActionCodecTestUtils.createRandomActions1(V_1_0);

            for (Action action: actions)
                flowMod.addAction(action);
        }

        return (OfmFlowMod) flowMod.toImmutable();
    }

    private void verifyOfmFlowMod(ProtocolVersion version, OfmFlowMod flowMod) {
        assertEquals(AM_NEQ, EXP_COOKIE, flowMod.getCookie());
        assertEquals(AM_NEQ, EXP_IDLE_TIMEOUT, flowMod.getIdleTimeout());
        assertEquals(AM_NEQ, EXP_HARD_TIMEOUT, flowMod.getHardTimeout());
        assertEquals(AM_NEQ, EXP_PRIORITY, flowMod.getPriority());
        assertEquals(AM_NEQ, EXP_BUFFER_ID, flowMod.getBufferId());
        assertEquals(AM_NEQ, EXP_OUT_PORT, flowMod.getOutPort());
        assertEquals(AM_NEQ, EXP_FLAGS, flowMod.getFlags());
        assertEquals(AM_NEQ, EXP_FLOW_CMD, flowMod.getCommand());
        verifySampleMatchA(version, flowMod.getMatch());

        if (version.equals(V_1_3)) {
            assertEquals(AM_NEQ, EXP_COOKIE_MASK, flowMod.getCookieMask());
            assertEquals(AM_NEQ, EXP_TABLE_ID, flowMod.getTableId());
            assertEquals(AM_NEQ, EXP_OUT_GROUP, flowMod.getOutGroup());

            verifyRandomInstructions1(flowMod.getInstructions());
        } else {
            assertEquals(AM_NEQ, 0, flowMod.getCookieMask());
            assertEquals(AM_NEQ, null, flowMod.getTableId());
            assertEquals(AM_NEQ, GroupId.ANY, flowMod.getOutGroup());
            verifyRandomActions1(version, flowMod.getActions());
        }
    }

    private OfmFlowMod createOfmFlowMod1(ProtocolVersion version) {
        OfmMutableFlowMod flowMod = (OfmMutableFlowMod)
                MessageFactory.create(version, MessageType.FLOW_MOD);

        flowMod.cookie(EXP_COOKIE_1)
                .idleTimeout(EXP_IDLE_TIMEOUT_1)
                .hardTimeout(EXP_HARD_TIMEOUT_1)
                .priority(EXP_PRIORITY_1)
                .bufferId(EXP_BUFFER_ID_1)
                .outPort(EXP_OUT_PORT_1)
                .flowModFlags(EXP_FLAGS_1)
                .command(EXP_FLOW_CMD_1)
                .match(createSampleMatchB(version));

        if (version.equals(V_1_3)) {
            flowMod.cookieMask(EXP_COOKIE_MASK_1);
            flowMod.outGroup(EXP_OUT_GROUP_1);
            flowMod.tableId(EXP_TABLE_ID_1);

            List<Instruction> insList = createRandomInstructions2();

            for (Instruction instruction: insList)
                flowMod.addInstruction(instruction);
        } else {
            List<Action> actions = createRandomActions2(V_1_0);

            for (Action action: actions)
                flowMod.addAction(action);
        }

        return (OfmFlowMod) flowMod.toImmutable();
    }

    private void verifyOfmFlowMod1(ProtocolVersion version,
                                   OfmFlowMod flowMod) {
        assertEquals(AM_NEQ, EXP_COOKIE_1, flowMod.getCookie());
        assertEquals(AM_NEQ, EXP_IDLE_TIMEOUT_1, flowMod.getIdleTimeout());
        assertEquals(AM_NEQ, EXP_HARD_TIMEOUT_1, flowMod.getHardTimeout());
        assertEquals(AM_NEQ, EXP_PRIORITY_1, flowMod.getPriority());
        assertEquals(AM_NEQ, EXP_BUFFER_ID_1, flowMod.getBufferId());
        assertEquals(AM_NEQ, EXP_OUT_PORT_1, flowMod.getOutPort());
        assertEquals(AM_NEQ, EXP_FLAGS_1, flowMod.getFlags());
        assertEquals(AM_NEQ, EXP_FLOW_CMD_1, flowMod.getCommand());
        verifySampleMatchB(version, flowMod.getMatch());

        if (version.equals(V_1_3)) {
            assertEquals(AM_NEQ, EXP_COOKIE_MASK_1, flowMod.getCookieMask());
            assertEquals(AM_NEQ, EXP_TABLE_ID_1, flowMod.getTableId());
            assertEquals(AM_NEQ, EXP_OUT_GROUP_1, flowMod.getOutGroup());

            verifyRandomInstructions2(flowMod.getInstructions());
        } else {
            assertEquals(AM_NEQ, 0, flowMod.getCookieMask());
            assertEquals(AM_NEQ, null, flowMod.getTableId());
            assertEquals(AM_NEQ, GroupId.ANY, flowMod.getOutGroup());
            verifyRandomActions2(version, flowMod.getActions());
        }
    }
}


