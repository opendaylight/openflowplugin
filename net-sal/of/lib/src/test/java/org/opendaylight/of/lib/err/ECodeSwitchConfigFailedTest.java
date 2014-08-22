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
import static org.opendaylight.of.lib.err.ECodeSwitchConfigFailed.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ECodeSwitchConfigFailed}.
 *
 * @author Pramod Shanbhag
 */
public class ECodeSwitchConfigFailedTest
    extends AbstractCodeBasedEnumTest<ECodeSwitchConfigFailed> {

    @Override
    protected ECodeSwitchConfigFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeSwitchConfigFailed.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeSwitchConfigFailed ec: ECodeSwitchConfigFailed.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.SWITCH_CONFIG_FAILED, parent);
        }
        assertEquals(AM_UXCC, 3, ECodeSwitchConfigFailed.values().length);
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
        check(V_1_1, 0, BAD_FLAGS);
        check(V_1_1, 1, BAD_LEN);
        check(V_1_1, 2, null, true);
        check(V_1_1, 3, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, BAD_FLAGS);
        check(V_1_2, 1, BAD_LEN);
        check(V_1_2, 2, EPERM);
        check(V_1_2, 3, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, BAD_FLAGS);
        check(V_1_3, 1, BAD_LEN);
        check(V_1_3, 2, EPERM);
        check(V_1_3, 3, null);
    }
}
