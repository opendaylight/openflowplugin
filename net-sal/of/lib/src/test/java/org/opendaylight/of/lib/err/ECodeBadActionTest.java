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
import static org.opendaylight.of.lib.err.ECodeBadAction.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ECodeBadAction}.
 *
 * @author Pramod Shanbhag
 */
public class ECodeBadActionTest extends AbstractCodeBasedEnumTest<ECodeBadAction> {

    @Override
    protected ECodeBadAction decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeBadAction.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeBadAction ec: ECodeBadAction.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.BAD_ACTION, parent);
        }
        assertEquals(AM_UXCC, 16, ECodeBadAction.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, BAD_TYPE);
        check(V_1_0, 1, BAD_LEN);
        check(V_1_0, 2, BAD_EXPERIMENTER);
        check(V_1_0, 3, BAD_EXP_TYPE);
        check(V_1_0, 4, BAD_OUT_PORT);
        check(V_1_0, 5, BAD_ARGUMENT);
        check(V_1_0, 6, EPERM);
        check(V_1_0, 7, TOO_MANY);
        check(V_1_0, 8, BAD_QUEUE);
        check(V_1_0, 9, null, true);
        check(V_1_0, 10, null, true);
        check(V_1_0, 11, null, true);
        check(V_1_0, 12, null, true);
        check(V_1_0, 13, null, true);
        check(V_1_0, 14, null, true);
        check(V_1_0, 15, null, true);
        check(V_1_0, 16, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, BAD_TYPE);
        check(V_1_1, 1, BAD_LEN);
        check(V_1_1, 2, BAD_EXPERIMENTER);
        check(V_1_1, 3, BAD_EXP_TYPE);
        check(V_1_1, 4, BAD_OUT_PORT);
        check(V_1_1, 5, BAD_ARGUMENT);
        check(V_1_1, 6, EPERM);
        check(V_1_1, 7, TOO_MANY);
        check(V_1_1, 8, BAD_QUEUE);
        check(V_1_1, 9, BAD_OUT_GROUP);
        check(V_1_1, 10, MATCH_INCONSISTENT);
        check(V_1_1, 11, UNSUPPORTED_ORDER);
        check(V_1_1, 12, BAD_TAG);
        check(V_1_1, 13, null, true);
        check(V_1_1, 14, null, true);
        check(V_1_1, 15, null, true);
        check(V_1_1, 16, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, BAD_TYPE);
        check(V_1_2, 1, BAD_LEN);
        check(V_1_2, 2, BAD_EXPERIMENTER);
        check(V_1_2, 3, BAD_EXP_TYPE);
        check(V_1_2, 4, BAD_OUT_PORT);
        check(V_1_2, 5, BAD_ARGUMENT);
        check(V_1_2, 6, EPERM);
        check(V_1_2, 7, TOO_MANY);
        check(V_1_2, 8, BAD_QUEUE);
        check(V_1_2, 9, BAD_OUT_GROUP);
        check(V_1_2, 10, MATCH_INCONSISTENT);
        check(V_1_2, 11, UNSUPPORTED_ORDER);
        check(V_1_2, 12, BAD_TAG);
        check(V_1_2, 13, BAD_SET_TYPE);
        check(V_1_2, 14, BAD_SET_LEN);
        check(V_1_2, 15, BAD_SET_ARGUMENT);
        check(V_1_2, 16, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, BAD_TYPE);
        check(V_1_3, 1, BAD_LEN);
        check(V_1_3, 2, BAD_EXPERIMENTER);
        check(V_1_3, 3, BAD_EXP_TYPE);
        check(V_1_3, 4, BAD_OUT_PORT);
        check(V_1_3, 5, BAD_ARGUMENT);
        check(V_1_3, 6, EPERM);
        check(V_1_3, 7, TOO_MANY);
        check(V_1_3, 8, BAD_QUEUE);
        check(V_1_3, 9, BAD_OUT_GROUP);
        check(V_1_3, 10, MATCH_INCONSISTENT);
        check(V_1_3, 11, UNSUPPORTED_ORDER);
        check(V_1_3, 12, BAD_TAG);
        check(V_1_3, 13, BAD_SET_TYPE);
        check(V_1_3, 14, BAD_SET_LEN);
        check(V_1_3, 15, BAD_SET_ARGUMENT);
        check(V_1_3, 16, null);
    }
}
