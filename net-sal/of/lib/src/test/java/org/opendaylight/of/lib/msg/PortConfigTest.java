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
import static org.opendaylight.of.lib.msg.PortConfig.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for PortConfig.
 *
 * @author Simon Hunt
 */
public class PortConfigTest extends BitmappedEnumTest<PortConfig> {

    @Override
    protected Set<PortConfig> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return PortConfig.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<PortConfig> flags, ProtocolVersion pv) {
        return PortConfig.encodeBitmap(flags, pv);
    }

    // TODO : refactor into superclass
    @Test
    public void basic() {
        print(EOL + "basic()");
        print(BASIC_HEADER);
        for (PortConfig pc: PortConfig.values())
            print(FMT_ENUM_STRINGS, padName(pc.name()), padName(pc),
                    padDisplay(pc.toDisplayString()));
        assertEquals(AM_UXCC, 7, PortConfig.values().length);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        verifyBit(V_1_0, 0x1, PORT_DOWN);
        verifyBit(V_1_0, 0x2, NO_STP);
        verifyBit(V_1_0, 0x4, NO_RECV);
        verifyBit(V_1_0, 0x8, NO_RECV_STP);
        verifyBit(V_1_0, 0x10, NO_FLOOD);
        verifyBit(V_1_0, 0x20, NO_FWD);
        verifyBit(V_1_0, 0x40, NO_PACKET_IN);
        verifyNaU32(V_1_0, 0x80);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Codec()");
        verifyBit(V_1_1, 0x1, PORT_DOWN);
        verifyNaBit(V_1_1, 0x2, NO_STP);
        verifyBit(V_1_1, 0x4, NO_RECV);
        verifyNaBit(V_1_1, 0x8, NO_RECV_STP);
        verifyNaBit(V_1_1, 0x10, NO_FLOOD);
        verifyBit(V_1_1, 0x20, NO_FWD);
        verifyBit(V_1_1, 0x40, NO_PACKET_IN);
        verifyNaU32(V_1_1, 0x80);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec()");
        verifyBit(V_1_2, 0x1, PORT_DOWN);
        verifyNaBit(V_1_2, 0x2, NO_STP);
        verifyBit(V_1_2, 0x4, NO_RECV);
        verifyNaBit(V_1_2, 0x8, NO_RECV_STP);
        verifyNaBit(V_1_2, 0x10, NO_FLOOD);
        verifyBit(V_1_2, 0x20, NO_FWD);
        verifyBit(V_1_2, 0x40, NO_PACKET_IN);
        verifyNaU32(V_1_2, 0x80);
    }

    @Test
    public void v13Decoder() {
        print(EOL + "v13Decoder()");
        verifyBit(V_1_3, 0x1, PORT_DOWN);
        verifyNaBit(V_1_3, 0x2, NO_STP);
        verifyBit(V_1_3, 0x4, NO_RECV);
        verifyNaBit(V_1_3, 0x8, NO_RECV_STP);
        verifyNaBit(V_1_3, 0x10, NO_FLOOD);
        verifyBit(V_1_3, 0x20, NO_FWD);
        verifyBit(V_1_3, 0x40, NO_PACKET_IN);
        verifyNaU32(V_1_3, 0x80);
    }

    @Test
    public void sampler() {
        print(EOL + "sampler()");
        verifyBitmappedFlags(V_1_0, 0x0f,
                PORT_DOWN, NO_STP, NO_RECV, NO_RECV_STP);
        verifyBitmappedFlags(V_1_1, 0x45, NO_PACKET_IN, NO_RECV, PORT_DOWN);
        verifyBitmappedFlags(V_1_2, 0x60, NO_PACKET_IN, NO_FWD);
        verifyBitmappedFlags(V_1_3, 0x21, NO_FWD, PORT_DOWN);
    }

}
