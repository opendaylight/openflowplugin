/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractCodeBasedEnumTest;
import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.instr.InstructionType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for InstrType.
 *
 * @author Simon Hunt
 */
public class InstructionTypeTest extends AbstractCodeBasedEnumTest<InstructionType> {

    @Override
    protected InstructionType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return InstructionType.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (InstructionType t: InstructionType.values())
            print(t);
        assertEquals(AM_UXCC, 7, InstructionType.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        notSup(V_1_0);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, 0, null);
        check(V_1_1, 1, GOTO_TABLE);
        check(V_1_1, 2, WRITE_METADATA);
        check(V_1_1, 3, WRITE_ACTIONS);
        check(V_1_1, 4, APPLY_ACTIONS);
        check(V_1_1, 5, CLEAR_ACTIONS);
        check(V_1_1, 6, null, true);
        check(V_1_1, 7, null);
        check(V_1_1, 0xffff, EXPERIMENTER);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, 0, null);
        check(V_1_2, 1, GOTO_TABLE);
        check(V_1_2, 2, WRITE_METADATA);
        check(V_1_2, 3, WRITE_ACTIONS);
        check(V_1_2, 4, APPLY_ACTIONS);
        check(V_1_2, 5, CLEAR_ACTIONS);
        check(V_1_2, 6, null, true);
        check(V_1_2, 7, null);
        check(V_1_2, 0xffff, EXPERIMENTER);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, 0, null);
        check(V_1_3, 1, GOTO_TABLE);
        check(V_1_3, 2, WRITE_METADATA);
        check(V_1_3, 3, WRITE_ACTIONS);
        check(V_1_3, 4, APPLY_ACTIONS);
        check(V_1_3, 5, CLEAR_ACTIONS);
        check(V_1_3, 6, METER);
        check(V_1_3, 7, null);
        check(V_1_3, 0xffff, EXPERIMENTER);
    }

}
