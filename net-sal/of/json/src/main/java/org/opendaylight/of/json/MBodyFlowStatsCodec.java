/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.Instruction;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.mp.MBodyFlowStats;
import org.opendaylight.of.lib.mp.MBodyMutableFlowStats;
import org.opendaylight.of.lib.msg.FlowModFlag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.json.CodecUtils.*;
import static org.opendaylight.of.lib.CommonUtils.hex;
import static org.opendaylight.of.lib.CommonUtils.parseHexLong;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;

/**
 * A JSON codec capable of encoding and decoding {@link MBodyFlowStats} objects.
 *
 * @author Shaila Shree
 * @author Simon Hunt
 */
public class MBodyFlowStatsCodec extends OfJsonCodec<MBodyFlowStats> {
    private static final String ROOT = "flow";
    private static final String ROOTS = "flows";

    private static final String TABLE_ID = "table_id";
    private static final String PRIORITY = "priority";
    private static final String IDLE_TIMEOUT = "idle_timeout";
    private static final String HARD_TIMEOUT = "hard_timeout";
    private static final String MATCH = "match";
    private static final String COOKIE = "cookie";
    private static final String DURATION_SEC = "duration_sec";
    private static final String DURATION_NSEC = "duration_nsec";
    private static final String PACKET_COUNT = "packet_count";
    private static final String BYTE_COUNT = "byte_count";
    private static final String FLOW_MOD_FLAGS = "flow_mod_flags";
    private static final String ACTIONS = "actions";
    private static final String INSTRUCTIONS = "instructions";

    private static final String PARSE_ERROR = "parse_error";

    // as per the openflow spec
    private static final long NO_COUNTER = -1;
    private static final String NA_VAL = CommonValues.lookup("na");

    private volatile MatchFieldCodec mfc;
    private volatile ActionCodec ac;
    private volatile InstructionCodec ic;

    /**
     * Constructs MBodyFlowStats Codec.
     */
    protected MBodyFlowStatsCodec() {
        super(ROOT, ROOTS);
    }


    private static String counterValue(long v) {
        return (v == NO_COUNTER) ? NA_VAL : Long.toString(v);
    }

    @Override
    public ObjectNode encode(MBodyFlowStats mbfs) {
        ObjectNode node = objectNode();
        ProtocolVersion pv = mbfs.getVersion();

        // if this flow stats represents a parse error, encode the
        // cause exception message and do a quick exit...
        if (mbfs.incomplete()) {
            node.put(PARSE_ERROR, mbfs.parseErrorCause().getMessage());
            return node;
        }

        if (pv.gt(V_1_0))
            node.put(TABLE_ID, mbfs.getTableId().toInt());
        else
            node.put(TABLE_ID, NA_VAL);

        node.put(DURATION_SEC, mbfs.getDurationSec());
        node.put(DURATION_NSEC, counterValue(mbfs.getDurationNsec()));
        node.put(PRIORITY, mbfs.getPriority());
        node.put(IDLE_TIMEOUT, mbfs.getIdleTimeout());
        node.put(HARD_TIMEOUT, mbfs.getHardTimeout());
        node.put(COOKIE, hex(mbfs.getCookie()));
        node.put(PACKET_COUNT, counterValue(mbfs.getPacketCount()));
        node.put(BYTE_COUNT, counterValue(mbfs.getByteCount()));
        node.put(MATCH, encodeMatch(mbfs));

        if (pv.gt(V_1_0)) {
            node.put(FLOW_MOD_FLAGS, encodeFlowModFlags(mbfs));
            node.put(INSTRUCTIONS, encodeInstructions(mbfs));
        } else {
            node.put(ACTIONS, encodeActions(mbfs));
        }

        return node;
    }

    private ArrayNode encodeMatch(MBodyFlowStats mbfs) {
        return getMatchFieldCodec().encodeList(mbfs.getMatch().getMatchFields());
    }

    private ArrayNode encodeInstructions(MBodyFlowStats mbfs) {
        return getInstructionCodec().encodeList(mbfs.getInstructions());
    }

    private ArrayNode encodeActions(MBodyFlowStats mbfs) {
        return getActionCodec().encodeList(mbfs.getActions());
    }

    private ArrayNode encodeFlowModFlags(MBodyFlowStats mbfs) {
        ArrayNode node = arrayNode();
        for (FlowModFlag flag : mbfs.getFlags())
            node.add(encodeFlowModFlag(flag));

        return node;
    }

    private int decodeUnsignedIntString(ObjectNode node, String key) {
        JsonNode n = node.get(key);
        return n == null ? -1 : n.asInt(-1);
    }

    private long decodeUnsignedLongString(ObjectNode node, String key) {
        JsonNode n = node.get(key);
        return n == null ? -1 : n.asLong(-1);
    }

    @Override
    public MBodyFlowStats decode(ObjectNode node) {
        ProtocolVersion ver = decodeProtocolVersion(node.get(VERSION));

        MBodyMutableFlowStats mbfs = new MBodyMutableFlowStats(ver);

        int tid = decodeUnsignedIntString(node, TABLE_ID);
        if (tid > -1)
            mbfs.tableId(TableId.valueOf(tid));

        mbfs.duration(node.get(DURATION_SEC).asLong(),
                node.get(DURATION_NSEC).asLong());
        mbfs.priority(node.get(PRIORITY).asInt());
        mbfs.idleTimeout(node.get(IDLE_TIMEOUT).asInt());
        mbfs.hardTimeout(node.get(HARD_TIMEOUT).asInt());
        mbfs.cookie(parseHexLong(node.get(COOKIE).asText()));
        mbfs.packetCount(decodeUnsignedLongString(node, PACKET_COUNT));
        mbfs.byteCount(decodeUnsignedLongString(node, BYTE_COUNT));
        mbfs.match(decodeMatch(ver, node.get(MATCH)));

        if (ver.gt(V_1_0)) {
            mbfs.flags(decodeFlowModFlags(node.get(FLOW_MOD_FLAGS)));
            mbfs.instructions(decodeInstructions(ver, node.get(INSTRUCTIONS)));
        } else {
            mbfs.actions(decodeActions(ver, node.get(ACTIONS)));
        }

        return (MBodyFlowStats) mbfs.toImmutable();
    }

    private Match decodeMatch(ProtocolVersion version, JsonNode node) {
        MutableMatch match = MatchFactory.createMatch(version);
        for (JsonNode mNode: node) {
            ObjectNode matchNode = (ObjectNode) mNode;
            matchNode.put(VERSION, encodeProtocolVersion(version));
            match.addField(getMatchFieldCodec().decode(matchNode));
        }

        return (Match)match.toImmutable();
    }

    private List<Instruction> decodeInstructions(ProtocolVersion version,
                                                 JsonNode node) {
        List<Instruction> insList = new ArrayList<Instruction>();

        for (JsonNode insNode: node) {
            ObjectNode instruction = (ObjectNode) insNode;
            instruction.put(VERSION, encodeProtocolVersion(version));
            insList.add(getInstructionCodec().decode(instruction));
        }

        return insList;
    }

    private List<Action> decodeActions(ProtocolVersion version, JsonNode node) {
        List<Action> actions = new ArrayList<Action>();

        for (JsonNode actNode: node) {
            ObjectNode action = (ObjectNode) actNode;
            action.put(VERSION, encodeProtocolVersion(version));
            actions.add(getActionCodec().decode(action));
        }

        return actions;
    }

    private Set<FlowModFlag> decodeFlowModFlags(JsonNode node) {
        Set<FlowModFlag> flags = new HashSet<FlowModFlag>();

        for (JsonNode flagNode: node)
            flags.add(decodeFlowModFlag(flagNode));

        return flags;
    }

    private ActionCodec getActionCodec() {
        if (ac == null)
            ac = (ActionCodec) OfJsonFactory.instance().codec(Action.class);

        return ac;
    }

    private MatchFieldCodec getMatchFieldCodec() {
        if (mfc == null)
            mfc = (MatchFieldCodec)
                    OfJsonFactory.instance().codec(MatchField.class);

        return mfc;
    }

    private InstructionCodec getInstructionCodec() {
        if (ic == null)
            ic = (InstructionCodec)
                    OfJsonFactory.instance().codec(Instruction.class);

        return ic;
    }
}
