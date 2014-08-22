/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.util.SafeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.of.lib.instr.InstructionType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Utility test class for verifying the contents of instructions.
 *
 * @author Simon Hunt
 */
public class InstrLookup {

    /** Verify a GOTO_TABLE instruction.
     *
     * @param ins the instruction
     * @param expTableId the expected table id
     */
    public static void verifyInstrGoTab(Instruction ins, int expTableId) {
        assertEquals(AM_NEQ, GOTO_TABLE, ins.getInstructionType());
        assertTrue(AM_WRCL, InstrGotoTable.class.isInstance(ins));
        InstrGotoTable igt = (InstrGotoTable) ins;
        assertEquals(AM_NEQ, TableId.valueOf(expTableId), igt.getTableId());
    }

    /** Verify a WRITE_METADATA instruction.
     *
     * @param ins the instruction
     * @param expMeta the expected metadata
     * @param expMask the expected metadata mask
     */
    public static void verifyInstrWrMeta(Instruction ins, long expMeta,
                                         long expMask) {
        assertEquals(AM_NEQ, WRITE_METADATA, ins.getInstructionType());
        assertTrue(AM_WRCL, InstrWriteMetadata.class.isInstance(ins));
        InstrWriteMetadata iwm = (InstrWriteMetadata) ins;
        assertEquals(AM_NEQ, expMeta, iwm.getMetadata());
        assertEquals(AM_NEQ, expMask, iwm.getMask());
    }

    private static final SafeMap<InstructionType,
            Class<? extends Instruction>> I_CLS =
            new SafeMap.Builder<InstructionType,
                    Class<? extends Instruction>>(Instruction.class)
                    .add(WRITE_ACTIONS, InstrWriteActions.class)
                    .add(APPLY_ACTIONS, InstrApplyActions.class)
                    .add(CLEAR_ACTIONS, InstrClearActions.class)
                    .build();


    /** Verify one of the "Action" Instructions: WriteActions, ApplyActions
     * or ClearActions. Specify the number of actions expected in the
     * instruction. This must be 0 for ClearActions.
     * Note that this method does not verify the action contents; you must
     * do that elsewhere.
     *
     * @param ins the instruction
     * @param expType the expected type
     * @param expActionCount the expected action count
     */
    public static void verifyInstrActions(Instruction ins,
                                          InstructionType expType,
                                          int expActionCount) {
        assertEquals(AM_NEQ, expType, ins.getInstructionType());
        assertTrue(AM_WRCL, I_CLS.get(expType).isInstance(ins));
        InstrAction ia = (InstrAction) ins;
        assertEquals(AM_UXS, expActionCount, ia.actions.size());
    }

    /** Verifies a METER instruction.
     *
     * @param ins the instruction
     * @param expMeterId the expected meter id
     */
    public static void verifyInstrMeter(Instruction ins, long expMeterId) {
        assertEquals(AM_NEQ, METER, ins.getInstructionType());
        assertTrue(AM_WRCL, InstrMeter.class.isInstance(ins));
        InstrMeter im = (InstrMeter) ins;
        assertEquals(AM_NEQ, MeterId.valueOf(expMeterId), im.getMeterId());
    }
}
