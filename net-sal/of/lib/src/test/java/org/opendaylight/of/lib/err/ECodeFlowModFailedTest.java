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
import static org.opendaylight.of.lib.err.ECodeFlowModFailed.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ECodeFlowModFailed}.
 *
 * @author Pramod Shanbhag
 */
public class ECodeFlowModFailedTest extends AbstractCodeBasedEnumTest<ECodeFlowModFailed> {

    @Override
    protected ECodeFlowModFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeFlowModFailed.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeFlowModFailed ec: ECodeFlowModFailed.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.FLOW_MOD_FAILED, parent);
        }
        assertEquals(AM_UXCC, 9, ECodeFlowModFailed.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, TABLE_FULL);
        check(V_1_0, 1, OVERLAP);
        check(V_1_0, 2, EPERM);
        check(V_1_0, 3, BAD_TIMEOUT);
        check(V_1_0, 4, BAD_COMMAND);
        check(V_1_0, 5, UNSUPPORTED_ACTION_LIST);
        // NOTE: Version mismatch is not thrown for codes that match later
        //        versions, because of the way the enum is coded, to allow
        //        for the re-assigned code numbers from v1.0.
        //  Not perfect, but we'll live with it.
        check(V_1_0, 6, null);
        check(V_1_0, 7, null);
        check(V_1_0, 8, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, UNKNOWN);
        check(V_1_1, 1, TABLE_FULL);
        check(V_1_1, 2, BAD_TABLE_ID);
        check(V_1_1, 3, OVERLAP);
        check(V_1_1, 4, EPERM);
        check(V_1_1, 5, BAD_TIMEOUT);
        check(V_1_1, 6, BAD_COMMAND);
        check(V_1_1, 7, null, true);
        check(V_1_1, 8, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, UNKNOWN);
        check(V_1_2, 1, TABLE_FULL);
        check(V_1_2, 2, BAD_TABLE_ID);
        check(V_1_2, 3, OVERLAP);
        check(V_1_2, 4, EPERM);
        check(V_1_2, 5, BAD_TIMEOUT);
        check(V_1_2, 6, BAD_COMMAND);
        check(V_1_2, 7, BAD_FLAGS);
        check(V_1_2, 8, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, UNKNOWN);
        check(V_1_3, 1, TABLE_FULL);
        check(V_1_3, 2, BAD_TABLE_ID);
        check(V_1_3, 3, OVERLAP);
        check(V_1_3, 4, EPERM);
        check(V_1_3, 5, BAD_TIMEOUT);
        check(V_1_3, 6, BAD_COMMAND);
        check(V_1_3, 7, BAD_FLAGS);
        check(V_1_3, 8, null);
    }
}
