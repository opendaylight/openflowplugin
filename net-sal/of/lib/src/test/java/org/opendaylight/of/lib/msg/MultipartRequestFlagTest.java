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
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MultipartRequestFlag.
 *
 * @author Simon Hunt
 */
public class MultipartRequestFlagTest extends
        BitmappedEnumTest<MultipartRequestFlag> {

    @Override
    protected Set<MultipartRequestFlag> decodeBitmap(int bitmap,
                                                     ProtocolVersion pv) {
        return MultipartRequestFlag.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<MultipartRequestFlag> flags, ProtocolVersion pv) {
        return MultipartRequestFlag.encodeBitmap(flags, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (MultipartRequestFlag f: MultipartRequestFlag.values())
            print(f);
        assertEquals(AM_UXCC, 1, MultipartRequestFlag.values().length);
    }

    @Test
    public void v10Decode() {
        print(EOL + "v10Decode()");
        verifyNaU16(V_1_0, 0x1);        // not supported before 1.3
    }

    @Test
    public void v11Decode() {
        print(EOL + "v11Decode()");
        verifyNaU16(V_1_1, 0x1);        // not supported before 1.3
    }

    @Test
    public void v12Decode() {
        print(EOL + "v12Decode()");
        verifyNaU16(V_1_2, 0x1);        // not supported before 1.3
    }

    @Test
    public void v13Decode() {
        print(EOL + "v13Decode()");
        verifyBit(V_1_3, 0x1, MultipartRequestFlag.REQUEST_MORE);
        verifyNaU16(V_1_3, 0x2);
    }

    @Test
    public void sampler() {
        print(EOL + "sampler()");
        verifyBitmappedFlags(V_1_3, 0x1, MultipartRequestFlag.REQUEST_MORE);
    }
}
