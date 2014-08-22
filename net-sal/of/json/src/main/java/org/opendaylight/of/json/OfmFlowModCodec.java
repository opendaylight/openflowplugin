/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
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
import org.opendaylight.of.lib.msg.FlowModFlag;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.of.lib.msg.OfmMutableFlowMod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.json.CodecUtils.*;
import static org.opendaylight.of.lib.CommonUtils.hex;
import static org.opendaylight.of.lib.CommonUtils.parseHexLong;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.msg.MessageType.FLOW_MOD;

/**
 * A JSON codec capable of encoding and decoding {@link OfmFlowMod} objects.
 *
 * @author Shaila Shree
 * @author Simon Hunt
 */
public class OfmFlowModCodec extends OfJsonCodec<OfmFlowMod> {

    // unit test access
    static final String ROOT = "flow";
    static final String ROOTS = "flows";

    private static final String TABLE_ID = "table_id";
    private static final String IDLE_TIMEOUT = "idle_timeout";
    private static final String HARD_TIMEOUT = "hard_timeout";
    private static final String MATCH = "match";
    private static final String COOKIE = "cookie";
    private static final String FLOW_MOD_FLAGS = "flow_mod_flags";
    private static final String ACTIONS = "actions";
    private static final String INSTRUCTIONS = "instructions";
    private static final String COOKIE_MASK = "cookie_mask";
    private static final String BUFFER_ID = "buffer_id";
    private static final String OUT_PORT = "out_port";
    private static final String OUT_GROUP = "out_group";

    // the following keys are used externally for injecting values dynamically
    public static final String PRIORITY = "priority";
    public static final String FLOW_MOD_CMD = "flow_mod_cmd";

    private volatile MatchFieldCodec mfc;
    private volatile InstructionCodec ic;
    private volatile ActionCodec ac;

    /**
     * Constructors an OfmFlowMod Codec.
     */
    protected OfmFlowModCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(OfmFlowMod fm) {
        ObjectNode node = objectNode();

        if (fm.getVersion().gt(V_1_0))
            node.put(TABLE_ID, fm.getTableId().toInt());

        node.put(PRIORITY, fm.getPriority());
        node.put(IDLE_TIMEOUT, fm.getIdleTimeout());
        node.put(HARD_TIMEOUT, fm.getHardTimeout());
        node.put(FLOW_MOD_CMD, encodeFlowModCmd(fm.getCommand()));
        node.put(COOKIE, hex(fm.getCookie()));

        if (fm.getVersion().gt(V_1_0))
            node.put(COOKIE_MASK, hex(fm.getCookieMask()));

        node.put(BUFFER_ID, encodeBufferId(fm.getBufferId()));
        encodeBigPort(node, OUT_PORT, fm.getOutPort(), fm.getVersion());

        if (fm.getVersion().gt(V_1_0))
            node.put(OUT_GROUP, encodeGroupId(fm.getOutGroup()));

        node.put(FLOW_MOD_FLAGS, encodeFlowModFlags(fm));
        node.put(MATCH, encodeMatch(fm));

        if (fm.getVersion().gt(V_1_0))
            node.put(INSTRUCTIONS, encodeInstructions(fm));
        else
            node.put(ACTIONS, encodeActions(fm));

        return node;
    }

    private ArrayNode encodeMatch(OfmFlowMod fm) {
        return getMatchFieldCodec().encodeList(fm.getMatch().getMatchFields());
    }

    private ArrayNode encodeInstructions(OfmFlowMod fm) {
        return getInstructionCodec().encodeList(fm.getInstructions());
    }

    private ArrayNode encodeActions(OfmFlowMod fm) {
        return getActionCodec().encodeList(fm.getActions());
    }

    private ArrayNode encodeFlowModFlags(OfmFlowMod fm) {
        ArrayNode node = arrayNode();
        for (FlowModFlag flag : fm.getFlags())
            node.add(encodeFlowModFlag(flag));
        return node;
    }

    @Override
    public OfmFlowMod decode(ObjectNode node) {
        ProtocolVersion pv = decodeProtocolVersion(node.get(VERSION));
        OfmMutableFlowMod fm =
                (OfmMutableFlowMod) MessageFactory.create(pv, FLOW_MOD);

        // ======================= Mandatory fields =========================

        fm.priority(node.get(PRIORITY).asInt());
        fm.bufferId(decodeBufferId(node.get(BUFFER_ID)));
        fm.match(decodeMatch(pv, node.get(MATCH)));

        // ======================== Optional fields =========================

        if (node.get(COOKIE) != null)
            fm.cookie(parseHexLong(node.get(COOKIE).asText()));
        if (node.get(COOKIE_MASK) != null)
            fm.cookieMask(parseHexLong(node.get(COOKIE_MASK).asText()));
        if (node.get(TABLE_ID) != null && pv.gt(ProtocolVersion.V_1_0))
            fm.tableId(TableId.valueOf(node.get(TABLE_ID).intValue()));
        if (node.get(IDLE_TIMEOUT) != null)
            fm.idleTimeout(node.get(IDLE_TIMEOUT).intValue());
        if (node.get(HARD_TIMEOUT) != null)
            fm.hardTimeout(node.get(HARD_TIMEOUT).intValue());
        if (node.get(OUT_PORT) != null)
            fm.outPort(decodeBigPort(node.get(OUT_PORT)));
        if (node.get(OUT_GROUP) != null)
            fm.outGroup(decodeGroupId(node.get(OUT_GROUP)));
        if (node.get(FLOW_MOD_FLAGS) != null)
            fm.flowModFlags(decodeFlowModFlags(node.get(FLOW_MOD_FLAGS)));
        if (node.get(FLOW_MOD_CMD) != null)
            fm.command(decodeFlowModCmd(node.get(FLOW_MOD_CMD)));
        if (node.get(INSTRUCTIONS) != null) {
            List<Instruction> ins = decodeInstructions(pv, node.get(INSTRUCTIONS));
            for (Instruction in : ins)
                fm.addInstruction(in);
        }
        if (node.get(ACTIONS) != null) {
            List<Action> actions = decodeActions(pv, node.get(ACTIONS));
            for (Action action : actions)
                fm.addAction(action);
        }

        return (OfmFlowMod)fm.toImmutable();
    }

    private Match decodeMatch(ProtocolVersion pv, JsonNode node) {
        MutableMatch match = MatchFactory.createMatch(pv);
        for (JsonNode mNode : node) {
            ObjectNode matchNode = (ObjectNode) mNode;
            matchNode.put(VERSION, encodeProtocolVersion(pv));
            match.addField(getMatchFieldCodec().decode(matchNode));
        }
        return (Match) match.toImmutable();
    }

    private Set<FlowModFlag> decodeFlowModFlags(JsonNode node) {
        Set<FlowModFlag> flags = new HashSet<>();
        for (JsonNode flagNode : node)
            flags.add(decodeFlowModFlag(flagNode));
        return flags;
    }

    private List<Instruction> decodeInstructions(ProtocolVersion pv,
                                                 JsonNode node) {
        List<Instruction> insList = new ArrayList<>();
        for (JsonNode insNode : node) {
            ObjectNode instr = (ObjectNode) insNode;
            instr.put(VERSION, encodeProtocolVersion(pv));
            insList.add(getInstructionCodec().decode(instr));
        }
        return insList;
    }

    private List<Action> decodeActions(ProtocolVersion pv, JsonNode node) {
        List<Action> actList = new ArrayList<>();
        for (JsonNode actNode : node) {
            ObjectNode action = (ObjectNode) actNode;
            action.put(VERSION, encodeProtocolVersion(pv));
            actList.add(getActionCodec().decode(action));
        }
        return actList;
    }

    private MatchFieldCodec getMatchFieldCodec() {
        if (mfc == null)
            mfc = (MatchFieldCodec)
                    OfJsonFactory.instance().codec(MatchField.class);
        return mfc;
    }

    private ActionCodec getActionCodec() {
        if (ac == null)
            ac = (ActionCodec) OfJsonFactory.instance().codec(Action.class);
        return ac;
    }

    private InstructionCodec getInstructionCodec() {
        if (ic == null)
            ic = (InstructionCodec)
                    OfJsonFactory.instance().codec(Instruction.class);
        return ic;
    }
}
