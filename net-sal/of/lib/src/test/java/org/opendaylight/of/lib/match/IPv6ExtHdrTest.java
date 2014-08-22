/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import junit.framework.Assert;
import org.junit.Test;
import org.opendaylight.of.lib.BitmappedEnumTest;
import org.opendaylight.of.lib.ProtocolVersion;

import java.util.Set;

import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.match.IPv6ExtHdr.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for IPv6ExtHDr.
 *
 * @author Simon Hunt
 */
public class IPv6ExtHdrTest extends BitmappedEnumTest<IPv6ExtHdr> {

    @Override
    protected Set<IPv6ExtHdr> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return IPv6ExtHdr.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<IPv6ExtHdr> flags, ProtocolVersion pv) {
        return IPv6ExtHdr.encodeBitmap(flags, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (IPv6ExtHdr ieh: IPv6ExtHdr.values())
            print(ieh);
        Assert.assertEquals(AM_UXCC, 9, IPv6ExtHdr.values().length);
    }

    private void v10v11v12Codec(ProtocolVersion pv) {
        // all bits throw exception!
        verifyNaU16(pv, 0x1, false);
        // all flags throw exception
        for (IPv6ExtHdr flag: IPv6ExtHdr.values())
            verifyNaBit(pv, 0x8000, flag, false);
    }


    @Test
    public void v10Codec() {
        print(EOL + "v10Codec");
        v10v11v12Codec(V_1_0);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Codec");
        v10v11v12Codec(V_1_1);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec");
        v10v11v12Codec(V_1_2);
    }

    @Test
    public void v13Codec() {
        print(EOL + "v13Codec()");
        verifyBit(V_1_3, 0x0001, NO_NEXT);
        verifyBit(V_1_3, 0x0002, ESP);
        verifyBit(V_1_3, 0x0004, AUTH);
        verifyBit(V_1_3, 0x0008, DEST);
        verifyBit(V_1_3, 0x0010, FRAG);
        verifyBit(V_1_3, 0x0020, ROUTER);
        verifyBit(V_1_3, 0x0040, HOP);
        verifyBit(V_1_3, 0x0080, UN_REP);
        verifyBit(V_1_3, 0x0100, UN_SEQ);
        verifyNaU16(V_1_3, 0x0200);
    }

    @Test
    public void decodeSample() {
        print(EOL + "decodeSample()");
        verifyBitmappedFlags(V_1_3, 0x015f,
                UN_SEQ, HOP, FRAG, DEST, AUTH, ESP, NO_NEXT);
    }
}
