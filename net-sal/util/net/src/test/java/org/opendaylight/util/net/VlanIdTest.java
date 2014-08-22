/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.net;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.VlanId.vlan;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link org.opendaylight.util.ip.VlanId}.
 *
 * @author Simon Hunt
 */
public class VlanIdTest {

    private static final int VLAN_TOO_BIG = VlanId.MAX_VALUE + 1;
    private static final int VLAN_TOO_SMALL = VlanId.MIN_VALUE - 1;

    private static final VlanId[] UNSORTED_VNIS = {
            vlan(37),
            vlan(23),
            VlanId.NONE,
            vlan(54),
            vlan(2),
            VlanId.PRESENT,
            vlan(14),
            vlan(7),
    };
    private static final VlanId[] SORTED_VNIS = {
            VlanId.NONE,
            VlanId.PRESENT,
            vlan(2),
            vlan(7),
            vlan(14),
            vlan(23),
            vlan(37),
            vlan(54),
    };

    private VlanId vlan;



    @Test
    public void basic() {
        print(EOL + "basic()");
        vlan = VlanId.valueOf(1);
        print(vlan);
        assertEquals(AM_NEQ, 1, vlan.toInt());
    }

    private void checkToString(int value, String exp) {
        vlan = VlanId.valueOf(value);
        String act = vlan.toString();
        print("{} => '{}'", value, act);
        assertEquals(AM_NEQ, exp, act);
    }

    @Test
    public void testToString() {
        print(EOL + "testToString()");
        checkToString(0, "0");
        checkToString(3, "3");
        checkToString(23, "23");
        checkToString(999, "999");
        checkToString(VlanId.MAX_VALUE, "4095");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooBig() {
        print(EOL + "tooBig()");
        VlanId.valueOf(VLAN_TOO_BIG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmall() {
        print(EOL + "tooSmall()");
        VlanId.valueOf(VLAN_TOO_SMALL);
    }

    private void checkValueOfString(int exp, String str) {
        vlan = VlanId.valueOf(str);
        int act = vlan.toInt();
        print("'{}' => {}", str, act);
        assertEquals(AM_NEQ, exp, act);
    }

    @Test
    public void valueOfString() {
        print(EOL + "valueOfString()");
        checkValueOfString(0, "0");
        checkValueOfString(3, "3");
        checkValueOfString(23, "23");
        checkValueOfString(2049, "2049");
        checkValueOfString(VlanId.MAX_VALUE, "4095");
    }

    @Test
    public void valueOfHexString() {
        checkValueOfString(0, "0x0");
        checkValueOfString(3, "0x3");
        checkValueOfString(23, "0x17");
        checkValueOfString(1195, "0x4ab");
        checkValueOfString(1195, "0X4AB");
        checkValueOfString(VlanId.MAX_VALUE, "0xfff");
    }

    @Test(expected = NullPointerException.class)
    public void valueOfStringNull() {
        VlanId.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfStringEmpty() {
        VlanId.valueOf("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfStringTooBig() {
        VlanId.valueOf("0x1ffffff");
    }

    @Test
    public void sortable() {
        print(EOL + "sortable()");
        VlanId[] vlans = Arrays.copyOf(UNSORTED_VNIS, UNSORTED_VNIS.length);
        assertArrayEquals(AM_NEQ, UNSORTED_VNIS, vlans);
        Arrays.sort(vlans);
        print(Arrays.toString(vlans));
        assertArrayEquals(AM_NEQ, SORTED_VNIS, vlans);
    }

    @Test
    public void equalsAndHashCode() {
        print(EOL + "equalsAndHashCode()");
        VlanId a = vlan(27);
        VlanId b = vlan(27);
        assertNotSame(AM_HUH, a, b);
        verifyEqual(a, b);
    }

    @Test
    public void vlanNone() {
        print(EOL + "vlanNone()");
        VlanId v = VlanId.NONE;
        print(v);
        assertEquals(AM_NEQ, "NONE", v.toString());
    }

    @Test
    public void vlanPresent() {
        print(EOL + "vlanPresent()");
        VlanId v = VlanId.PRESENT;
        print(v);
        assertEquals(AM_NEQ, "PRESENT", v.toString());
    }

    @Test
    public void vlanNoneFromString() {
        print(EOL + "vlanNoneFromString()");
        VlanId v = VlanId.valueOf("none");
        print(v);
        assertEquals(AM_NEQ, VlanId.NONE, v);
        v = VlanId.valueOf("NONE");
        print(v);
        assertEquals(AM_NEQ, VlanId.NONE, v);
    }

    @Test
    public void vlanPresentFromString() {
        print(EOL + "vlanPresentFromString()");
        VlanId v = VlanId.valueOf("present");
        print(v);
        assertEquals(AM_NEQ, VlanId.PRESENT, v);
        v = VlanId.valueOf("PRESENT");
        print(v);
        assertEquals(AM_NEQ, VlanId.PRESENT, v);
    }

    private static final int SOME_VAL = 22;

    @Test
    public void convenience() {
        assertEquals(AM_NEQ, VlanId.valueOf(SOME_VAL), vlan(SOME_VAL));
    }

    @Test
    public void bufferTests() {
        print(EOL + "bufferTests()");
        VlanId v = VlanId.valueOf(SOME_VAL);
        ByteBuffer bb = ByteBuffer.allocate(2);
        // first, write into a buffer
        v.intoBuffer(bb);
        print(Arrays.toString(bb.array()));
        assertArrayEquals(AM_NEQ, new byte[]{0, 22}, bb.array());

        // second, read from a buffer
        bb.array()[0] = 1;
        bb.array()[1] = 4;
        bb.rewind();

        v = VlanId.valueFrom(bb);
        print(Arrays.toString(bb.array()));
        assertEquals(AM_NEQ, vlan(260), v);
    }
}
