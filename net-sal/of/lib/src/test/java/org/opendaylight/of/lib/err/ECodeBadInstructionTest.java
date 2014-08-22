/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.err;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractCodeBasedEnumTest;
import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.err.ECodeBadInstruction.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for @{link ECodeBadInstruction}.
 *
 * @author Pramod Shanbhag
 */
public class ECodeBadInstructionTest extends AbstractCodeBasedEnumTest<ECodeBadInstruction> {

    @Override
    protected ECodeBadInstruction decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeBadInstruction.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeBadInstruction ec: ECodeBadInstruction.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.BAD_INSTRUCTION, parent);
        }
        assertEquals(AM_UXCC, 9, ECodeBadInstruction.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        notSup(V_1_0);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, UNKNOWN_INST);
        check(V_1_1, 1, UNSUP_INST);
        check(V_1_1, 2, BAD_TABLE_ID);
        check(V_1_1, 3, UNSUP_METADATA);
        check(V_1_1, 4, UNSUP_METADATA_MASK);
        check(V_1_1, 5, BAD_EXP_TYPE);
        check(V_1_1, 6, null);
        check(V_1_1, 7, null);
        check(V_1_1, 8, null);
        check(V_1_1, 9, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, UNKNOWN_INST);
        check(V_1_2, 1, UNSUP_INST);
        check(V_1_2, 2, BAD_TABLE_ID);
        check(V_1_2, 3, UNSUP_METADATA);
        check(V_1_2, 4, UNSUP_METADATA_MASK);
        check(V_1_2, 5, BAD_EXPERIMENTER);
        check(V_1_2, 6, BAD_EXP_TYPE);
        check(V_1_2, 7, BAD_LEN);
        check(V_1_2, 8, EPERM);
        check(V_1_2, 9, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, UNKNOWN_INST);
        check(V_1_3, 1, UNSUP_INST);
        check(V_1_3, 2, BAD_TABLE_ID);
        check(V_1_3, 3, UNSUP_METADATA);
        check(V_1_3, 4, UNSUP_METADATA_MASK);
        check(V_1_3, 5, BAD_EXPERIMENTER);
        check(V_1_3, 6, BAD_EXP_TYPE);
        check(V_1_3, 7, BAD_LEN);
        check(V_1_3, 8, EPERM);
        check(V_1_3, 9, null);
    }
}
