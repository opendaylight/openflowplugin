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
import static org.opendaylight.of.lib.err.ErrorType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for ErrorType.
 *
 * @author Simon Hunt
 */
public class ErrorTypeTest extends AbstractCodeBasedEnumTest<ErrorType> {

    @Override
    protected ErrorType decode(int code, ProtocolVersion pv) throws DecodeException {
        return ErrorType.decode(code, pv);
    }


    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ErrorType t: ErrorType.values())
            print(t);
        assertEquals(AM_UXCC, 15, ErrorType.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, HELLO_FAILED);
        check(V_1_0, 1, BAD_REQUEST);
        check(V_1_0, 2, BAD_ACTION);
        check(V_1_0, 3, FLOW_MOD_FAILED);
        check(V_1_0, 4, PORT_MOD_FAILED);
        check(V_1_0, 5, QUEUE_OP_FAILED);
        // NOTE: Version mismatch is not thrown for codes that match later
        //        versions, because of the way the enum is coded, to allow
        //        for the re-assigned code numbers from v1.0.
        //  Not perfect, but we'll live with it.
        check(V_1_0, 6, null);
        check(V_1_0, 7, null);
        check(V_1_0, 8, null);
        check(V_1_0, 9, null);
        check(V_1_0, 10, null);
        check(V_1_0, 11, null);
        check(V_1_0, 12, null);
        check(V_1_0, 13, null);
        check(V_1_0, 14, null);
        check(V_1_0, 0xffff, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, HELLO_FAILED);
        check(V_1_1, 1, BAD_REQUEST);
        check(V_1_1, 2, BAD_ACTION);
        check(V_1_1, 3, BAD_INSTRUCTION);
        check(V_1_1, 4, BAD_MATCH);
        check(V_1_1, 5, FLOW_MOD_FAILED);
        check(V_1_1, 6, GROUP_MOD_FAILED);
        check(V_1_1, 7, PORT_MOD_FAILED);
        check(V_1_1, 8, TABLE_MOD_FAILED);
        check(V_1_1, 9, QUEUE_OP_FAILED);
        check(V_1_1, 10, SWITCH_CONFIG_FAILED);
        // NOTE: Version mismatch *is* thrown for V1.1 and later
        check(V_1_1, 11, null, true);
        check(V_1_1, 12, null, true);
        check(V_1_1, 13, null, true);
        check(V_1_1, 14, null);
        check(V_1_1, 0xffff, null, true);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, HELLO_FAILED);
        check(V_1_2, 1, BAD_REQUEST);
        check(V_1_2, 2, BAD_ACTION);
        check(V_1_2, 3, BAD_INSTRUCTION);
        check(V_1_2, 4, BAD_MATCH);
        check(V_1_2, 5, FLOW_MOD_FAILED);
        check(V_1_2, 6, GROUP_MOD_FAILED);
        check(V_1_2, 7, PORT_MOD_FAILED);
        check(V_1_2, 8, TABLE_MOD_FAILED);
        check(V_1_2, 9, QUEUE_OP_FAILED);
        check(V_1_2, 10, SWITCH_CONFIG_FAILED);
        check(V_1_2, 11, ROLE_REQUEST_FAILED);
        // NOTE: Version mismatch *is* thrown for V1.1 and later
        check(V_1_2, 12, null, true);
        check(V_1_2, 13, null, true);
        check(V_1_2, 14, null);
        check(V_1_2, 0xffff, EXPERIMENTER);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, HELLO_FAILED);
        check(V_1_3, 1, BAD_REQUEST);
        check(V_1_3, 2, BAD_ACTION);
        check(V_1_3, 3, BAD_INSTRUCTION);
        check(V_1_3, 4, BAD_MATCH);
        check(V_1_3, 5, FLOW_MOD_FAILED);
        check(V_1_3, 6, GROUP_MOD_FAILED);
        check(V_1_3, 7, PORT_MOD_FAILED);
        check(V_1_3, 8, TABLE_MOD_FAILED);
        check(V_1_3, 9, QUEUE_OP_FAILED);
        check(V_1_3, 10, SWITCH_CONFIG_FAILED);
        check(V_1_3, 11, ROLE_REQUEST_FAILED);
        check(V_1_3, 12, METER_MOD_FAILED);
        check(V_1_3, 13, TABLE_FEATURES_FAILED);
        check(V_1_3, 14, null);
        check(V_1_3, 0xffff, EXPERIMENTER);
    }

}
