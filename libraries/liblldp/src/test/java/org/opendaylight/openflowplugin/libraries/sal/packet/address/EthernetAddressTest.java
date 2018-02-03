/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * @file   EthernetAddressTest.java
 *
 * @brief  Unit Tests for EthernetAddress class
 *
 * Unit Tests for EthernetAddress class
 */
package org.opendaylight.openflowplugin.libraries.sal.packet.address;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.libraries.liblldp.EthernetAddress;

public class EthernetAddressTest {
    @Test
    public void testNonValidConstructor() {
        @SuppressWarnings("unused")
        EthernetAddress ea1;
        // Null input array
        try {
            ea1 = new EthernetAddress((byte[]) null);

            // Exception is expected if NOT raised test will fail
            fail("Expected NullPointerException");
        } catch (final NullPointerException e) {
            // expected
        }

        // Array too short
        try {
            ea1 = new EthernetAddress(new byte[] { (byte) 0x0, (byte) 0x0 });

            // Exception is expected if NOT raised test will fail
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }

        // Array too long
        try {
            ea1 = new EthernetAddress(new byte[] { (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                (byte) 0x0, (byte) 0x0, (byte) 0x0 });

            // Exception is expected if NOT raised test will fail
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testEquality() {
        EthernetAddress ea1 = new EthernetAddress(new byte[] { (byte) 0x0, (byte) 0x0,
            (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1 });

        EthernetAddress ea2 = new EthernetAddress(new byte[] { (byte) 0x0, (byte) 0x0,
            (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1 });
        Assert.assertTrue(ea1.equals(ea2));

        ea1 = new EthernetAddress(new byte[] { (byte) 0x0, (byte) 0x0,
            (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1 });

        ea2 = ea1.clone();
        Assert.assertTrue(ea1.equals(ea2));

        // Check for well knowns

        ea1 = EthernetAddress.BROADCASTMAC;
        ea2 = new EthernetAddress(new byte[] { (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff });
        Assert.assertTrue(ea1.equals(ea2));
    }

    @Test
    public void testUnEquality() {
        EthernetAddress ea1 = new EthernetAddress(new byte[] { (byte) 0x0, (byte) 0x0,
            (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x2 });

        EthernetAddress ea2 = new EthernetAddress(new byte[] { (byte) 0x0, (byte) 0x0,
            (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1 });
        Assert.assertTrue(!ea1.equals(ea2));
    }
}
