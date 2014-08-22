/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.net;

import org.junit.Test;

import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.Vni.vni;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link Vni}.
 *
 * @author Simon Hunt
 * @author Jesse Hummer
 * @author Scott Simes
 */
public class VniTest {

    private static final int VNI_TOO_BIG = Vni.MAX_VALUE + 1;
    private static final int VNI_TOO_SMALL = Vni.MIN_VALUE - 1;

    private static final Vni[] UNSORTED_VNIS = {
            vni(37),
            vni(23),
            vni(54),
            vni(2),
            vni(14),
            vni(7),
    };
    private static final Vni[] SORTED_VNIS = {
            vni(2),
            vni(7),
            vni(14),
            vni(23),
            vni(37),
            vni(54),
    };

    private Vni vni;



    @Test
    public void basic() {
        print(EOL + "basic()");
        vni = Vni.valueOf(1);
        print(vni);
        assertEquals(AM_NEQ, 1, vni.toInt());
    }

    private void checkToString(int value, String exp) {
        vni = Vni.valueOf(value);
        String act = vni.toString();
        print("{} => '{}'", value, act);
        assertEquals(AM_NEQ, exp, act);
    }

    @Test
    public void testToString() {
        print(EOL + "testToString()");
        checkToString(0, "0");
        checkToString(3, "3");
        checkToString(23, "23");
        checkToString(10785, "10785");
        checkToString(Vni.MAX_VALUE, "16777215");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooBig() {
        print(EOL + "tooBig()");
        Vni.valueOf(VNI_TOO_BIG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmall() {
        print(EOL + "tooSmall()");
        Vni.valueOf(VNI_TOO_SMALL);
    }

    private void checkValueOfString(int exp, String str) {
        vni = Vni.valueOf(str);
        int act = vni.toInt();
        print("'{}' => {}", str, act);
        assertEquals(AM_NEQ, exp, act);
    }

    @Test
    public void valueOfString() {
        print(EOL + "valueOfString()");
        checkValueOfString(0, "0");
        checkValueOfString(3, "3");
        checkValueOfString(23, "23");
        checkValueOfString(10785, "10785");
        checkValueOfString(Vni.MAX_VALUE, "16777215");
    }

    @Test
    public void valueOfHexString() {
        checkValueOfString(0, "0x0");
        checkValueOfString(3, "0x3");
        checkValueOfString(23, "0x17");
        checkValueOfString(10785, "0x2a21");
        checkValueOfString(10785, "0X2A21");
        checkValueOfString(Vni.MAX_VALUE, "0xffffff");
    }

    @Test(expected = NullPointerException.class)
    public void valueOfStringNull() {
        Vni.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfStringEmpty() {
        Vni.valueOf("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfStringTooBig() {
        Vni.valueOf("0x1ffffff");
    }

    @Test
    public void sortable() {
        print(EOL + "sortable()");
        Vni[] vnis = Arrays.copyOf(UNSORTED_VNIS, UNSORTED_VNIS.length);
        assertArrayEquals(AM_NEQ, UNSORTED_VNIS, vnis);
        Arrays.sort(vnis);
        print(Arrays.toString(vnis));
        assertArrayEquals(AM_NEQ, SORTED_VNIS, vnis);
    }

    @Test
    public void equalsAndHashCode() {
        print(EOL + "equalsAndHashCode()");
        Vni a = vni(27);
        Vni b = vni(27);
        assertNotSame(AM_HUH, a, b);
        verifyEqual(a, b);
    }
}
