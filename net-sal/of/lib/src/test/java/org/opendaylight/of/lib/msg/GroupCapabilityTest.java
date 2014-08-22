/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
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
import static org.opendaylight.of.lib.msg.GroupCapability.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for GroupCapabilities.
 *
 * @author Simon Hunt
 */
public class GroupCapabilityTest
        extends BitmappedEnumTest<GroupCapability> {

    @Override
    protected Set<GroupCapability> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return GroupCapability.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<GroupCapability> flags, ProtocolVersion pv) {
        return GroupCapability.encodeBitmap(flags, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (GroupCapability c: GroupCapability.values())
            print(c);
        assertEquals(AM_UXCC, 4, GroupCapability.values().length);
    }

    private void v10v11Codec(ProtocolVersion pv) {
        // not supported before 1.2
        verifyNaU32(V_1_0, 0x1);
        verifyNaBit(pv, 0x8000, SELECT_WEIGHT);
        verifyNaBit(pv, 0x8000, SELECT_LIVENESS);
        verifyNaBit(pv, 0x8000, CHAINING);
        verifyNaBit(pv, 0x8000, CHAINING_CHECKS);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        v10v11Codec(V_1_0);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Decode()");
        v10v11Codec(V_1_1);
    }

    private void v12v13Codec(ProtocolVersion pv) {
        verifyBit(pv, 0x1, SELECT_WEIGHT);
        verifyBit(pv, 0x2, SELECT_LIVENESS);
        verifyBit(pv, 0x4, CHAINING);
        verifyBit(pv, 0x8, CHAINING_CHECKS);
        verifyNaU32(pv, 0x10);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec()");
        v12v13Codec(V_1_2);
    }

    @Test
    public void v13Codec() {
        print(EOL + "v13Codec()");
        v12v13Codec(V_1_3);
    }

    @Test
    public void sampler() {
        print(EOL + "sampler()");
        verifyBitmappedFlags(V_1_2, 0xa, SELECT_LIVENESS, CHAINING_CHECKS);
        verifyBitmappedFlags(V_1_3, 0x5, SELECT_WEIGHT, CHAINING);
    }

}
