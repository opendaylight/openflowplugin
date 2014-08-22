/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.instr.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.opendaylight.of.json.CodecUtils.*;
import static org.opendaylight.of.lib.CommonUtils.hex;
import static org.opendaylight.of.lib.CommonUtils.parseHexLong;
import static org.opendaylight.of.lib.instr.InstructionFactory.createInstruction;
import static org.opendaylight.of.lib.instr.InstructionFactory.createMutableInstruction;
import static org.opendaylight.util.ByteUtils.hexWithPrefix;
import static org.opendaylight.util.ByteUtils.parseHexWithPrefix;
import static org.opendaylight.util.JSONUtils.fromKey;
import static org.opendaylight.util.JSONUtils.toKey;

/**
 * A JSON codec capable of encoding and decoding {@link Instruction} objects.
 *
 * @author Shaila Shree
 *
 */
public class InstructionCodec extends OfJsonCodec<Instruction> {
    static final String ROOT = "flow_instruction";
    static final String ROOTS = "flow_instructions";

    private static final String MASK = "mask";

    private volatile ActionCodec ac;

    /**
     * Constructs an Instruction Codec.
     */
    protected InstructionCodec() {
        super(ROOT, ROOTS);
    }

    @Override
    public ObjectNode encode(Instruction ins) {
        ObjectNode node = null;
        InstructionType type = ins.getInstructionType();
        String name = toKey(type);

        switch (type) {
            case CLEAR_ACTIONS:
                node = encodeType(name);
                break;
            case APPLY_ACTIONS:
                node = encodeActions(name,
                        ((InstrApplyActions) ins).getActionList());
                break;
            case WRITE_ACTIONS:
                List<Action> actions =
                        new ArrayList<Action>(((InstrWriteActions) ins).getActionSet());
                node = encodeActions(name, actions);
                break;
            case WRITE_METADATA:
                node = encodeMetaData(name, ins);
                break;
            case GOTO_TABLE:
                node = encodeTable(name, ins);
                break;
            case METER:
                node = encodeMeter(name, ins);
                break;
            case EXPERIMENTER:
                node = encodeExp(name, ins);
                break;
            default:
                break;
        }
        return node;
    }

    private ObjectNode encodeType(String name) {
        return objectNode().put(name, true);
    }

    private ObjectNode encodeActions(String name, List<Action> actions) {
        ObjectNode node = objectNode();
        node.put(name, getActionCodec().encodeList(actions));
        return node;
    }

    private ObjectNode encodeMetaData(String name, Instruction ins) {
        InstrWriteMetadata iwmd = (InstrWriteMetadata) ins;

        ObjectNode node = objectNode().put(name, hex(iwmd.getMetadata()));

        long mask = iwmd.getMask();
        if (mask != 0)
            node.put(MASK, hex(mask));

        return node;
    }

    private ObjectNode encodeTable(String name, Instruction ins) {
        InstrGotoTable igt = (InstrGotoTable) ins;
        return objectNode().put(name, encodeTableId(igt.getTableId()));
    }

    private ObjectNode encodeMeter(String name, Instruction ins) {
        InstrMeter im = (InstrMeter) ins;
        return objectNode().put(name, encodeMeterId(im.getMeterId()));
    }

    private ObjectNode encodeExp(String name, Instruction ins) {
        InstrExperimenter exp = (InstrExperimenter) ins;
        return objectNode().put(name, hexWithPrefix(exp.getData()));
    }

    @Override
    public Instruction decode(ObjectNode node) {
        String field = null;
        JsonNode pv = null;
        JsonNode value = null;
        JsonNode mask = null;

        Iterator<Map.Entry<String, JsonNode>> nodes = node.fields();

        while (nodes.hasNext()) {
            Map.Entry<String, JsonNode> entry = nodes.next();
            if (entry.getKey().equals(VERSION)) {
                pv = entry.getValue();
            } else if (entry.getKey().equals(MASK)) {
                mask = entry.getValue();
            } else {
                field = entry.getKey();
                value = entry.getValue();
            }
        }

        ProtocolVersion version  = decodeProtocolVersion(pv);
        InstructionType type = fromKey(InstructionType.class, field);
        Instruction ins = null;

        switch (type) {
            case CLEAR_ACTIONS:
                ins = decodeType(version, type);
                break;
            case APPLY_ACTIONS:
                ins = decodeActions(version, type, value);
                break;
            case WRITE_ACTIONS:
                ins = decodeActions(version, type, value);
                break;
            case WRITE_METADATA:
                ins = decodeMetaData(version, type, value, mask);
                break;
            case GOTO_TABLE:
                ins = decodeTable(version, type, value);
                break;
            case METER:
                ins = decodeMeter(version, type, value);
                break;
            case EXPERIMENTER:
                ins = decodeExp(version, type, value);
                break;
            default:
                break;
        }

        return ins;
    }

    private Instruction decodeType(ProtocolVersion version,
                                   InstructionType type) {
        return createInstruction(version, type);
    }

    private Instruction decodeActions (ProtocolVersion version,
                                       InstructionType type,
                                       JsonNode value)  {

        InstrMutableAction mutIns = createMutableInstruction(version, type);

        for (JsonNode node: value) {
            ObjectNode action = (ObjectNode) node;
            action.put(VERSION, encodeProtocolVersion(version));
            mutIns.addAction(getActionCodec().decode(action));
        }

        return (Instruction) mutIns.toImmutable();
    }

    private Instruction decodeMetaData(ProtocolVersion version,
                                       InstructionType type,
                                       JsonNode value,
                                       JsonNode mask)  {
        long metaMask = 0;
        if (mask != null)
            metaMask = parseHexLong(mask.asText());

        return createInstruction(version, type,
                                 parseHexLong(value.asText()), metaMask);
    }

    private Instruction decodeTable(ProtocolVersion version,
                                    InstructionType type,
                                    JsonNode value) {
        return createInstruction(version, type, decodeTableId(value));
    }

    private Instruction decodeMeter(ProtocolVersion version,
                                    InstructionType type,
                                    JsonNode value) {
        return createInstruction(version, type, decodeMeterId(value));
    }

    private Instruction decodeExp(ProtocolVersion version,
                                  InstructionType type,
                                  JsonNode value) {
        return createInstruction(version, type,
                1234, // FIXME - need to parse and pass in an Experimenter ID
                                 parseHexWithPrefix(value.asText()));
    }

    private ActionCodec getActionCodec() {
        if (ac == null)
            ac = (ActionCodec) OfJsonFactory.instance().codec(Action.class);

        return ac;
    }
}
