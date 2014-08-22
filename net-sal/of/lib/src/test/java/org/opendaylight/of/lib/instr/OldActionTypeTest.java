/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
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
import static org.opendaylight.of.lib.instr.OldActionType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OldActionType.
 *
 * @author Simon Hunt
 */
public class OldActionTypeTest extends AbstractCodeBasedEnumTest<OldActionType> {

    @Override
    protected OldActionType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return OldActionType.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (OldActionType type: OldActionType.values())
            print(type);
        assertEquals(AM_UXCC, 27, OldActionType.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, OUTPUT);
        check(V_1_0, 1, SET_VLAN_VID);
        check(V_1_0, 2, SET_VLAN_PCP);
        check(V_1_0, 3, STRIP_VLAN);
        check(V_1_0, 4, SET_DL_SRC);
        check(V_1_0, 5, SET_DL_DST);
        check(V_1_0, 6, SET_NW_SRC);
        check(V_1_0, 7, SET_NW_DST);
        check(V_1_0, 8, SET_NW_TOS);
        check(V_1_0, 9, SET_TP_SRC);
        check(V_1_0, 10, SET_TP_DST);
        check(V_1_0, 11, SET_QUEUE); // named ENQUEUE in 1.0 spec
        check(V_1_0, 12, null);
        check(V_1_0, 13, null);
        check(V_1_0, 14, null);
        check(V_1_0, 15, null);
        check(V_1_0, 16, null);
        check(V_1_0, 17, null);
        check(V_1_0, 18, null);
        check(V_1_0, 19, null);
        check(V_1_0, 20, null);
        check(V_1_0, 21, null);
        check(V_1_0, 22, null);
        check(V_1_0, 23, null);
        check(V_1_0, 24, null);
        check(V_1_0, 25, null);
        check(V_1_0, 0xffff, EXPERIMENTER);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, OUTPUT);
        check(V_1_1, 1, SET_VLAN_VID);
        check(V_1_1, 2, SET_VLAN_PCP);
        check(V_1_1, 3, SET_DL_SRC);
        check(V_1_1, 4, SET_DL_DST);
        check(V_1_1, 5, SET_NW_SRC);
        check(V_1_1, 6, SET_NW_DST);
        check(V_1_1, 7, SET_NW_TOS);
        check(V_1_1, 8, SET_NW_ECN);
        check(V_1_1, 9, SET_TP_SRC);
        check(V_1_1, 10, SET_TP_DST);
        check(V_1_1, 11, COPY_TTL_OUT);
        check(V_1_1, 12, COPY_TTL_IN);
        check(V_1_1, 13, SET_MPLS_LABEL);
        check(V_1_1, 14, SET_MPLS_TC);
        check(V_1_1, 15, SET_MPLS_TTL);
        check(V_1_1, 16, DEC_MPLS_TTL);
        check(V_1_1, 17, PUSH_VLAN);
        check(V_1_1, 18, POP_VLAN);
        check(V_1_1, 19, PUSH_MPLS);
        check(V_1_1, 20, POP_MPLS);
        check(V_1_1, 21, SET_QUEUE);
        check(V_1_1, 22, GROUP);
        check(V_1_1, 23, SET_NW_TTL);
        check(V_1_1, 24, DEC_NW_TTL);
        check(V_1_1, 25, null);
        check(V_1_1, 0xffff, EXPERIMENTER);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        notSup(V_1_2);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        notSup(V_1_3);
    }
}
