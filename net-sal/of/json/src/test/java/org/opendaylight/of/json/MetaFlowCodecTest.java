/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.json;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.controller.MetaFlow;
import org.opendaylight.of.controller.MetaFlowData;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.Instruction;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.mp.MBodyFlowStats;
import org.opendaylight.of.lib.mp.MBodyMutableFlowStats;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.FlowModFlag;
import org.opendaylight.util.json.JSON;
import org.opendaylight.util.json.JsonFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.InstructionFactory.createInstruction;
import static org.opendaylight.of.lib.instr.InstructionType.CLEAR_ACTIONS;
import static org.opendaylight.of.lib.instr.InstructionType.GOTO_TABLE;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.METADATA;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.TUNNEL_ID;
import static org.opendaylight.of.lib.mp.MpBodyFactory.createReplyBodyElement;
import static org.opendaylight.of.lib.msg.FlowModFlag.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MetaFlowCodec}.
 *
 * @author Simon Hunt
 */
public class MetaFlowCodecTest extends AbstractCodecTest {

    private static final String METAFLOW = "metaflow";

    @BeforeClass
    public static void classSetUp() {
        JsonFactory factory = new OfJsonFactory();
        JSON.registerFactory(factory);
    }

    @Test
    public void metaFlow() {
        String expected = getJsonContents(METAFLOW);
        String actual = JSON.toJson(createMetaFlow(), true);
        print(actual);
        assertEquals(AM_NEQ, normalizeEOL(expected), normalizeEOL(actual));
    }

    private MetaFlow createMetaFlow() {
        return new MetaFlowData(createFlowStats());
    }

    private static final Set<FlowModFlag> FM_FLAGS_1 =
            EnumSet.of(SEND_FLOW_REM, NO_PACKET_COUNTS, NO_BYTE_COUNTS);


    private List<MBodyFlowStats> createFlowStats() {
        List<MBodyFlowStats> stats = new ArrayList<>();
        stats.add(createStats(0, 300, 4000, 4, 60, 100, 0x1234, 2341, 134232425,
                createMatchOne(), FM_FLAGS_1, createInstrOne()));
        return stats;
    }

    private static final long MASK = 0xf00ff00ff00ff00L;
    private static final long META_VAL = 0x1f2f3f4f5f6f7f8L;
    private static final long TUNN_VAL = 0x1f2f3f4f5f6f723L;

    private Match createMatchOne() {
        MutableMatch m = MatchFactory.createMatch(V_1_3);
        m.addField(createBasicField(V_1_3, METADATA, META_VAL, MASK));
        m.addField(createBasicField(V_1_3, TUNNEL_ID, TUNN_VAL, MASK));
        return (Match) m.toImmutable();
    }

    private List<Instruction> createInstrOne() {
        List<Instruction> instrs = new ArrayList<>();
        instrs.add(createInstruction(V_1_3, CLEAR_ACTIONS));
        instrs.add(createInstruction(V_1_3, GOTO_TABLE, TableId.valueOf(3)));
        return instrs;
    }

    private MBodyFlowStats createStats(int tid, int dsec, int dnsec, int pri,
                                       int idle, int hard, int cookie,
                                       long pkt, long byt, Match match,
                                       Set<FlowModFlag> flags,
                                       List<Instruction> ins) {
        MBodyMutableFlowStats fs = (MBodyMutableFlowStats)
                createReplyBodyElement(V_1_3, MultipartType.FLOW);
        fs.tableId(TableId.valueOf(tid)).duration(dsec, dnsec).priority(pri)
                .idleTimeout(idle).hardTimeout(hard).cookie(cookie)
                .packetCount(pkt).byteCount(byt).match(match).flags(flags)
                .instructions(ins);
        return (MBodyFlowStats) fs.toImmutable();
    }

}
