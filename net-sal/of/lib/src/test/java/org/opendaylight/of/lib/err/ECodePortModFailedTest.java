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
import static org.opendaylight.of.lib.err.ECodePortModFailed.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ECodePortModFailed}.
 *
 * @author Pramod Shanbhag
 */
public class ECodePortModFailedTest extends AbstractCodeBasedEnumTest<ECodePortModFailed> {

    @Override
    protected ECodePortModFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodePortModFailed.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodePortModFailed ec: ECodePortModFailed.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.PORT_MOD_FAILED, parent);
        }
        assertEquals(AM_UXCC, 5, ECodePortModFailed.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, BAD_PORT);
        check(V_1_0, 1, BAD_HW_ADDR);
        check(V_1_0, 2, null, true);
        check(V_1_0, 3, null, true);
        check(V_1_0, 4, null, true);
        check(V_1_0, 5, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, BAD_PORT);
        check(V_1_1, 1, BAD_HW_ADDR);
        check(V_1_1, 2, BAD_CONFIG);
        check(V_1_1, 3, BAD_ADVERTISE);
        check(V_1_1, 4, null, true);
        check(V_1_1, 5, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, BAD_PORT);
        check(V_1_2, 1, BAD_HW_ADDR);
        check(V_1_2, 2, BAD_CONFIG);
        check(V_1_2, 3, BAD_ADVERTISE);
        check(V_1_2, 4, EPERM);
        check(V_1_2, 5, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, BAD_PORT);
        check(V_1_3, 1, BAD_HW_ADDR);
        check(V_1_3, 2, BAD_CONFIG);
        check(V_1_3, 3, BAD_ADVERTISE);
        check(V_1_3, 4, EPERM);
        check(V_1_3, 5, null);
    }
}
