/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.of.lib.ExperimenterId.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for ExperimenterId.
 *
 * @author Simon Hunt
 */
public class ExperimenterIdTest extends AbstractTest {

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ExperimenterId id: ExperimenterId.values())
            print(id);
        assertEquals(AM_UXCC, 6, ExperimenterId.values().length);
    }

    private void check(int code, ExperimenterId id) {
        print("code: 0x" + Integer.toHexString(code) + " -> " + id);
        ExperimenterId ei = ExperimenterId.decode(code);
        assertEquals(AM_NEQ, id, ei);
        assertEquals(AM_NEQ, code, ei.encodedId());
    }

    @Test
    public void checkValues() {
        print(EOL + "checkValues()");
        check(0x00002320, NICIRA);
        check(0x005c16c7, BIG_SWITCH);
        check(0x00b0d2f5, VELLO);
        check(0x00002481, HP);
        check(0xff000001, BUDAPEST_U);
    }

    private void checkNull(int code) {
        assertNull(ExperimenterId.decode(code));
    }

    @Test
    public void checkSampleOfUnknowns() {
        print(EOL + "checkSampleOfUnknowns()");
        checkNull(0x00000000);
        checkNull(0xffffffff);
        checkNull(0x000023ff);
        checkNull(0x00002482);
    }

    private void checkString(int id, String exp) {
        String s = ExperimenterId.idToString(id);
        print("{} => {}", hex(id), s);
        assertEquals(AM_NEQ, exp, s);
    }

    @Test
    public void idToString() {
        print(EOL + "idToString()");
        checkString(0x00002320, "0x2320(NICIRA)");
        checkString(0x005c16c7, "0x5c16c7(BIG_SWITCH)");
        checkString(0x00b0d2f5, "0xb0d2f5(VELLO)");
        checkString(0x00002481, "0x2481(HP)");
        checkString(0x000004ea, "0x4ea(HP_LABS)");
        checkString(0xff000001, "0xff000001(BUDAPEST_U)");
        checkString(0x00000000, "0x0");
        checkString(0xffffffff, "0xffffffff");
        checkString(0x000023ff, "0x23ff");
        checkString(0x00002482, "0x2482");
    }
}
