/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.BitmappedEnumTest;
import org.opendaylight.of.lib.ProtocolVersion;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.Capability.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for Capability enum.
 *
 * @author Simon Hunt
 */
public class CapabilityTest extends BitmappedEnumTest<Capability> {

    @Override
    protected Set<Capability> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return Capability.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<Capability> flags, ProtocolVersion pv) {
        return Capability.encodeBitmap(flags, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (Capability cap: Capability.values())
            print(cap);
        assertEquals(AM_UXCC, 10, Capability.values().length);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        verifyBit(V_1_0, 0x1, FLOW_STATS);
        verifyBit(V_1_0, 0x2, TABLE_STATS);
        verifyBit(V_1_0, 0x4, PORT_STATS);
        verifyBit(V_1_0, 0x8, STP);
        verifyNaBit(V_1_0, 0x8000, GROUP_STATS);
        verifyBit(V_1_0, 0x10, RESERVED);
        verifyBit(V_1_0, 0x20, IP_REASM);
        verifyBit(V_1_0, 0x40, QUEUE_STATS);
        verifyBit(V_1_0, 0x80, ARP_MATCH_IP);
        verifyNaBit(V_1_0, 0x100, PORT_BLOCKED);
        verifyNaU16(V_1_0, 0x100);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Codec()");
        verifyBit(V_1_1, 0x1, FLOW_STATS);
        verifyBit(V_1_1, 0x2, TABLE_STATS);
        verifyBit(V_1_1, 0x4, PORT_STATS);
        verifyNaBit(V_1_1, 0x8000, STP);
        verifyBit(V_1_1, 0x8, GROUP_STATS);
        verifyNaBit(V_1_1, 0x10, RESERVED);
        verifyBit(V_1_1, 0x20, IP_REASM);
        verifyBit(V_1_1, 0x40, QUEUE_STATS);
        verifyBit(V_1_1, 0x80, ARP_MATCH_IP);
        verifyNaBit(V_1_1, 0x100, PORT_BLOCKED);
        verifyNaU16(V_1_1, 0x100);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec()");
        verifyBit(V_1_2, 0x1, FLOW_STATS);
        verifyBit(V_1_2, 0x2, TABLE_STATS);
        verifyBit(V_1_2, 0x4, PORT_STATS);
        verifyNaBit(V_1_1, 0x8000, STP);
        verifyBit(V_1_2, 0x8, GROUP_STATS);
        verifyNaBit(V_1_2, 0x10, RESERVED);
        verifyBit(V_1_2, 0x20, IP_REASM);
        verifyBit(V_1_2, 0x40, QUEUE_STATS);
        verifyNaBit(V_1_2, 0x80, ARP_MATCH_IP);
        verifyBit(V_1_2, 0x100, PORT_BLOCKED);
        verifyNaU16(V_1_2, 0x200);
    }

    @Test
    public void v13Codec() {
        print(EOL + "v13Codec()");
        verifyBit(V_1_3, 0x1, FLOW_STATS);
        verifyBit(V_1_3, 0x2, TABLE_STATS);
        verifyBit(V_1_3, 0x4, PORT_STATS);
        verifyNaBit(V_1_1, 0x8000, STP);
        verifyBit(V_1_3, 0x8, GROUP_STATS);
        verifyNaBit(V_1_3, 0x10, RESERVED);
        verifyBit(V_1_3, 0x20, IP_REASM);
        verifyBit(V_1_3, 0x40, QUEUE_STATS);
        verifyNaBit(V_1_3, 0x80, ARP_MATCH_IP);
        verifyBit(V_1_3, 0x100, PORT_BLOCKED);
        verifyNaU16(V_1_3, 0x200);
    }

    @Test
    public void samples() {
        print(EOL + "samples()");
        verifyBitmappedFlags(V_1_0, 0x3c,
                RESERVED, IP_REASM, STP, PORT_STATS);
        verifyBitmappedFlags(V_1_1, 0x6c,
                IP_REASM, QUEUE_STATS, GROUP_STATS, PORT_STATS);
        verifyBitmappedFlags(V_1_2, 0x166,
                PORT_BLOCKED, IP_REASM, QUEUE_STATS, TABLE_STATS, PORT_STATS);
        verifyBitmappedFlags(V_1_3, 0x43,
                QUEUE_STATS, TABLE_STATS, FLOW_STATS);
    }
}
