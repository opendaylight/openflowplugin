/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractCodeBasedEnumTest;
import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.TableFeaturePropType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for TableFeaturePropType.
 *
 * @author Simon Hunt
 */
public class TableFeaturePropTypeTest extends AbstractCodeBasedEnumTest<TableFeaturePropType> {
    @Override
    protected TableFeaturePropType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return TableFeaturePropType.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (TableFeaturePropType t: TableFeaturePropType.values())
            print(t);
        assertEquals(AM_UXCC, 16, TableFeaturePropType.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        notSup(V_1_0);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        notSup(V_1_1);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        notSup(V_1_2);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, INSTRUCTIONS);
        check(V_1_3, 1, INSTRUCTIONS_MISS);
        check(V_1_3, 2, NEXT_TABLES);
        check(V_1_3, 3, NEXT_TABLES_MISS);
        check(V_1_3, 4, WRITE_ACTIONS);
        check(V_1_3, 5, WRITE_ACTIONS_MISS);
        check(V_1_3, 6, APPLY_ACTIONS);
        check(V_1_3, 7, APPLY_ACTIONS_MISS);
        check(V_1_3, 8, MATCH);
        check(V_1_3, 10, WILDCARDS);
        check(V_1_3, 12, WRITE_SETFIELD);
        check(V_1_3, 13, WRITE_SETFIELD_MISS);
        check(V_1_3, 14, APPLY_SETFIELD);
        check(V_1_3, 15, APPLY_SETFIELD_MISS);
        check(V_1_3, 16, null);
        check(V_1_3, 0xfffe, EXPERIMENTER);
        check(V_1_3, 0xffff, EXPERIMENTER_MISS);
    }

    @Test
    public void isMiss() {
        print(EOL + "isMiss()");
        verifyIsMiss(INSTRUCTIONS, false);
        verifyIsMiss(INSTRUCTIONS_MISS, true);
        verifyIsMiss(NEXT_TABLES, false);
        verifyIsMiss(NEXT_TABLES_MISS, true);
        verifyIsMiss(WRITE_ACTIONS, false);
        verifyIsMiss(WRITE_ACTIONS_MISS, true);
        verifyIsMiss(APPLY_ACTIONS, false);
        verifyIsMiss(APPLY_ACTIONS_MISS, true);
        verifyIsMiss(MATCH, false);
        verifyIsMiss(WILDCARDS, false);
        verifyIsMiss(WRITE_SETFIELD, false);
        verifyIsMiss(WRITE_SETFIELD_MISS, true);
        verifyIsMiss(APPLY_SETFIELD, false);
        verifyIsMiss(APPLY_SETFIELD_MISS, true);
        verifyIsMiss(EXPERIMENTER, false);
        verifyIsMiss(EXPERIMENTER_MISS, true);
    }

    private void verifyIsMiss(TableFeaturePropType type, boolean b) {
        assertEquals(AM_NEQ, b, type.isMiss());
    }

    @Test
    public void regular() {
        print(EOL + "regular()");
        verifyRegular(INSTRUCTIONS, INSTRUCTIONS_MISS);
        verifyRegular(NEXT_TABLES, NEXT_TABLES_MISS);
        verifyRegular(WRITE_ACTIONS, WRITE_ACTIONS_MISS);
        verifyRegular(APPLY_ACTIONS, APPLY_ACTIONS_MISS);
        verifyRegular(MATCH, null);
        verifyRegular(WILDCARDS, null);
        verifyRegular(WRITE_SETFIELD, WRITE_SETFIELD_MISS);
        verifyRegular(APPLY_SETFIELD, APPLY_SETFIELD_MISS);
        verifyRegular(EXPERIMENTER, EXPERIMENTER_MISS);
    }

    private void verifyRegular(TableFeaturePropType reg,
                               TableFeaturePropType miss) {
        assertEquals(AM_NEQ, reg, reg.regular());
        if (miss != null)
            assertEquals(AM_NEQ, reg, miss.regular());
    }
}
