/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
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
import static org.opendaylight.util.junit.TestTools.*;


/**
 * Unit tests for ControllerRole.
 *
 * @author Simon Hunt
 */
public class ControllerRoleTest extends AbstractCodeBasedEnumTest<ControllerRole> {
    @Override
    protected ControllerRole decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ControllerRole.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ControllerRole r: ControllerRole.values())
            print(r);
        assertEquals(AM_UXCC, 4, ControllerRole.values().length);
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
        check(V_1_2, -1, null);
        check(V_1_2, 0, ControllerRole.NO_CHANGE);
        check(V_1_2, 1, ControllerRole.EQUAL);
        check(V_1_2, 2, ControllerRole.MASTER);
        check(V_1_2, 3, ControllerRole.SLAVE);
        check(V_1_2, 4, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, ControllerRole.NO_CHANGE);
        check(V_1_3, 1, ControllerRole.EQUAL);
        check(V_1_3, 2, ControllerRole.MASTER);
        check(V_1_3, 3, ControllerRole.SLAVE);
        check(V_1_3, 4, null);
    }

}
