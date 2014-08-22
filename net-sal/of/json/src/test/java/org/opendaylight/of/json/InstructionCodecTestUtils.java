/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.json.ActionCodecTestUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.InstructionFactory.createMutableInstruction;
import static org.opendaylight.of.lib.instr.InstructionType.*;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_WRCL;

/**
 * Utility methods for {@link InstructionCodec}.
 *
 * @author Shaila Shree
 */
public class InstructionCodecTestUtils extends AbstractCodecTest {

    public static final String AM_DUP = "Duplicate Instruction : ";
    public static final String AM_UNKNOWN_INSTR =
                        "Instruction of unknown type : ";

    private static final long EXP_META_DATA = 0x1234L;
    private static final long EXP_META_MASK = 0xffffL;

    private static final MeterId EXP_METER_ID = mid(192);

    private static final ExperimenterId EID = ExperimenterId.HP;

    private static final byte[] EXP_EXP_DATA = {1, 2, 3, 4, 5, 6, 7, 8};

    private static final TableId EXP_TABLE_ID = tid(3);

    public static Instruction createInstruction() {
        InstrMutableAction ins = createMutableInstruction(V_1_3, WRITE_ACTIONS);
        List<Action> actions = createAllActions(V_1_3);

        for (Action action: actions)
            ins.addAction(action);

        return (Instruction)ins.toImmutable();
    }

    public static void verifyInstruction(InstrWriteActions instruction) {
        assertEquals(AM_NEQ, V_1_3, instruction.getVersion());
        assertEquals(AM_NEQ, WRITE_ACTIONS, instruction.getInstructionType());

        verifyAllActions(V_1_3,
                new ArrayList<Action>(instruction.getActionSet()));
    }

    public static List<Instruction> createRandomInstructions1() {
        List<Instruction> insList = new ArrayList<Instruction>();

        insList.add(InstructionFactory.createInstruction(V_1_3, CLEAR_ACTIONS));
        InstrMutableAction ins = createMutableInstruction(V_1_3, WRITE_ACTIONS);

        List<Action> actions = createRandomActions1(V_1_3);

        for (Action action: actions)
            ins.addAction(action);

        insList.add((Instruction) ins.toImmutable());

        insList.add(InstructionFactory.createInstruction(
                V_1_3, WRITE_METADATA, EXP_META_DATA, EXP_META_MASK));

        return insList;
    }

    public static void verifyRandomInstructions1(List<Instruction> insList) {
        assertEquals(AM_NEQ, 3, insList.size());

        Set<InstructionType> hit = new HashSet<InstructionType>();
        for (Instruction instruction: insList) {
            InstructionType type = instruction.getInstructionType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, instruction.getVersion());

            switch (type) {
                case CLEAR_ACTIONS:
                    assertTrue(AM_WRCL, instruction instanceof
                            InstrClearActions);
                    break;
                case WRITE_ACTIONS:
                    List<Action> actions = new ArrayList<Action>(
                            ((InstrWriteActions)instruction).getActionSet());
                    verifyRandomActions1(V_1_3, actions);
                    break;
                case WRITE_METADATA:
                    validateMetaData(instruction);
                    break;
                default:
                    fail(AM_UNKNOWN_INSTR + type.name());
                    break;
            }
        }
    }

    public static List<Instruction> createRandomInstructions2() {
        List<Instruction> instructions = new ArrayList<Instruction>();

        InstrMutableAction mutIns =
                createMutableInstruction(V_1_3, APPLY_ACTIONS);
        List<Action> actions = createRandomActions2(V_1_3);

        for (Action action: actions)
            mutIns.addAction(action);

        instructions.add((Instruction) mutIns.toImmutable());

        instructions.add(InstructionFactory.createInstruction(
                V_1_3, GOTO_TABLE, EXP_TABLE_ID));
        instructions.add(InstructionFactory.createInstruction(
                V_1_3, METER, EXP_METER_ID));
        instructions.add(InstructionFactory.createInstruction(
                V_1_3, EXPERIMENTER, EID, EXP_EXP_DATA));

        return instructions;
    }

    public static void verifyRandomInstructions2(List<Instruction> insList) {
        assertEquals(AM_NEQ, 4, insList.size());

        Set<InstructionType> hit = new HashSet<InstructionType>();
        for (Instruction instruction: insList) {
            InstructionType type = instruction.getInstructionType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, instruction.getVersion());

            switch (type) {
                case APPLY_ACTIONS:
                    verifyRandomActions2(V_1_3,
                            ((InstrApplyActions) instruction).getActionList());
                    break;
                case GOTO_TABLE:
                    validateTableId(instruction);
                    break;
                case METER:
                    validateMeterId(instruction);
                    break;
                case EXPERIMENTER:
                    validateExp(instruction);
                    break;
                default:
                    fail(AM_UNKNOWN_INSTR + type.name());
                    break;
            }
        }
    }

    public static List<Instruction> createAllInstructions() {
        List<Instruction> instructions = new ArrayList<Instruction>();

        instructions.add(InstructionFactory.createInstruction(V_1_3,
                CLEAR_ACTIONS));
        InstrMutableAction mutIns = createMutableInstruction(V_1_3,
                WRITE_ACTIONS);

        List<Action> actions = createAllActions(V_1_3);

        for (Action action: actions)
            mutIns.addAction(action);

        instructions.add((Instruction) mutIns.toImmutable());

        mutIns = createMutableInstruction(V_1_3, APPLY_ACTIONS);
        actions = createAllActions(V_1_3);

        for (Action action: actions)
            mutIns.addAction(action);

        instructions.add((Instruction) mutIns.toImmutable());

        instructions.add(InstructionFactory.createInstruction(
                V_1_3, WRITE_METADATA, EXP_META_DATA, EXP_META_MASK));
        instructions.add(InstructionFactory.createInstruction(
                V_1_3, GOTO_TABLE, EXP_TABLE_ID));
        instructions.add(InstructionFactory.createInstruction(
                V_1_3, METER, EXP_METER_ID));
        instructions.add(InstructionFactory.createInstruction(
                V_1_3, EXPERIMENTER, EID, EXP_EXP_DATA));

        return instructions;
    }

    public static void verifyAllInstructions(List<Instruction> instructions) {
        assertEquals(AM_NEQ, 7, instructions.size());

        Set<InstructionType> hit = new HashSet<InstructionType>();
        for (Instruction instruction: instructions) {
            InstructionType type = instruction.getInstructionType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, instruction.getVersion());

            switch (type) {
                case CLEAR_ACTIONS:
                    assertTrue(AM_WRCL, instruction instanceof
                            InstrClearActions);
                    break;
                case APPLY_ACTIONS:
                    verifyAllActions(V_1_3,
                            ((InstrApplyActions) instruction).getActionList());
                    break;
                case WRITE_ACTIONS:
                    List<Action> actions =
                            new ArrayList<Action>(
                              ((InstrWriteActions)instruction).getActionSet());

                    verifyAllActions(V_1_3, actions);
                    break;
                case WRITE_METADATA:
                    validateMetaData(instruction);
                    break;
                case GOTO_TABLE:
                    validateTableId(instruction);
                    break;
                case METER:
                    validateMeterId(instruction);
                    break;
                case EXPERIMENTER:
                    validateExp(instruction);
                    break;
                default:
                    fail(AM_UNKNOWN_INSTR + type.name());
                    break;
            }
        }
    }

    private static void validateMetaData(Instruction ins) {
        InstrWriteMetadata insWMd = (InstrWriteMetadata) ins;
        assertEquals(AM_NEQ, EXP_META_DATA, insWMd.getMetadata());
        assertEquals(AM_NEQ, EXP_META_MASK, insWMd.getMask());
    }

    private static void validateTableId(Instruction ins) {
        assertEquals(AM_NEQ, EXP_TABLE_ID, ((InstrGotoTable) ins).getTableId());
    }

    private static void validateMeterId(Instruction ins) {
        assertEquals(AM_NEQ, EXP_METER_ID, ((InstrMeter) ins).getMeterId());
    }

    private static void validateExp(Instruction ins) {
        InstrExperimenter insExp = (InstrExperimenter) ins;
        assertArrayEquals(AM_NEQ, EXP_EXP_DATA, insExp.getData());
    }
}

