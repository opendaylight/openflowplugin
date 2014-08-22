/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

import org.junit.Test;
import org.opendaylight.of.controller.impl.AbstractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link HintType}.
 *
 * @author Simon Hunt
 */
public class HintTypeTest extends AbstractTest {

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (HintType h: HintType.values())
            print(h);
        assertEquals(AM_UXCC, 2, HintType.values().length);
    }

    private void check(int code, HintType type) {
        print("code: {} -> {}", code, type);
        HintType ht = HintType.decode(code);
        assertEquals(AM_NEQ, type, ht);
        assertEquals(AM_NEQ, code, ht.encodedType());
    }

    @Test
    public void checkValues() {
        print(EOL + "checkValues()");
        check(0, HintType.HANDLER);
        check(1, HintType.TEST_PACKET);
    }

    private void checkNull(int code) {
        assertNull(HintType.decode(code));
    }

    @Test
    public void checkSampleOfUnknowns() {
        print(EOL + "checkSampleOfUnknowns()");
        checkNull(-5);
        checkNull(-1);
        checkNull(101);
    }

    @Test
    public void idToString() {
        print(EOL + "idToString()");
        checkString(0, "0(HANDLER)");
        checkString(1, "1(TEST_PACKET)");
        checkString(2, "2");
        checkString(3, "3");
        checkString(-1, "-1");
    }

    private void checkString(int code, String expString) {
        String s = HintType.typeToString(code);
        print("{} => {}", code, s);
        assertEquals(AM_NEQ, expString, s);
    }
}
