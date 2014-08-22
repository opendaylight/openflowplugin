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
import static org.opendaylight.of.lib.err.ECodeBadRequest.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for ECodeBadRequest.
 *
 * @author Simon Hunt
 */
public class ECodeBadRequestTest
        extends AbstractCodeBasedEnumTest<ECodeBadRequest> {

    @Override
    protected ECodeBadRequest decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeBadRequest.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeBadRequest ec: ECodeBadRequest.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.BAD_REQUEST, parent);
        }
        assertEquals(AM_UXCC, 14, ECodeBadRequest.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, BAD_VERSION);
        check(V_1_0, 1, BAD_TYPE);
        check(V_1_0, 2, BAD_STAT);
        check(V_1_0, 3, BAD_EXPERIMENTER);
        check(V_1_0, 4, BAD_EXP_TYPE);
        check(V_1_0, 5, EPERM);
        check(V_1_0, 6, BAD_LEN);
        check(V_1_0, 7, BUFFER_EMPTY);
        check(V_1_0, 8, BUFFER_UNKNOWN);
        check(V_1_0, 9, null, true);
        check(V_1_0, 10, null, true);
        check(V_1_0, 11, null, true);
        check(V_1_0, 12, null, true);
        check(V_1_0, 13, null, true);
        check(V_1_0, 14, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, BAD_VERSION);
        check(V_1_1, 1, BAD_TYPE);
        check(V_1_1, 2, BAD_STAT);
        check(V_1_1, 3, BAD_EXPERIMENTER);
        check(V_1_1, 4, BAD_EXP_TYPE);
        check(V_1_1, 5, EPERM);
        check(V_1_1, 6, BAD_LEN);
        check(V_1_1, 7, BUFFER_EMPTY);
        check(V_1_1, 8, BUFFER_UNKNOWN);
        check(V_1_1, 9, BAD_TABLE_ID);
        check(V_1_1, 10, null, true);
        check(V_1_1, 11, null, true);
        check(V_1_1, 12, null, true);
        check(V_1_1, 13, null, true);
        check(V_1_1, 14, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, BAD_VERSION);
        check(V_1_2, 1, BAD_TYPE);
        check(V_1_2, 2, BAD_STAT);
        check(V_1_2, 3, BAD_EXPERIMENTER);
        check(V_1_2, 4, BAD_EXP_TYPE);
        check(V_1_2, 5, EPERM);
        check(V_1_2, 6, BAD_LEN);
        check(V_1_2, 7, BUFFER_EMPTY);
        check(V_1_2, 8, BUFFER_UNKNOWN);
        check(V_1_2, 9, BAD_TABLE_ID);
        check(V_1_2, 10, IS_SLAVE);
        check(V_1_2, 11, BAD_PORT);
        check(V_1_2, 12, BAD_PACKET);
        check(V_1_2, 13, null, true);
        check(V_1_2, 14, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, BAD_VERSION);
        check(V_1_3, 1, BAD_TYPE);
        check(V_1_3, 2, BAD_STAT);
        check(V_1_3, 3, BAD_EXPERIMENTER);
        check(V_1_3, 4, BAD_EXP_TYPE);
        check(V_1_3, 5, EPERM);
        check(V_1_3, 6, BAD_LEN);
        check(V_1_3, 7, BUFFER_EMPTY);
        check(V_1_3, 8, BUFFER_UNKNOWN);
        check(V_1_3, 9, BAD_TABLE_ID);
        check(V_1_3, 10, IS_SLAVE);
        check(V_1_3, 11, BAD_PORT);
        check(V_1_3, 12, BAD_PACKET);
        check(V_1_3, 13, MP_BUFFER_OVERFLOW);
        check(V_1_3, 14, null);
    }

}
