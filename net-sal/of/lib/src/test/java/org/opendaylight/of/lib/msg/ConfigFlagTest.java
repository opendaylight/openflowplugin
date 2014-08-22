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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.ConfigFlag.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for ConfigFlag enum.
 *
 * @author Simon Hunt
 */
public class ConfigFlagTest extends BitmappedEnumTest<ConfigFlag> {

    @Override
    protected Set<ConfigFlag> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return ConfigFlag.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<ConfigFlag> flags, ProtocolVersion pv) {
        return ConfigFlag.encodeBitmap(flags, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ConfigFlag cf: ConfigFlag.values())
            print(cf);
        assertEquals(AM_UXCC, 4, ConfigFlag.values().length);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        // yeah, the "bit" value is ZERO - the decoder must return FRAG_NORMAL
        verifyBit(V_1_0, 0x0, FRAG_NORMAL);
        verifyBit(V_1_0, 0x1, FRAG_DROP);
        verifyBit(V_1_0, 0x2, FRAG_REASM);
        verifyNaBit(V_1_0, 0x4, INV_TTL_TO_CTRLR);
        verifyNaU16(V_1_0, 0x4);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Codec()");
        // yeah, the "bit" value is ZERO - the decoder must return FRAG_NORMAL
        verifyBit(V_1_1, 0x0, FRAG_NORMAL);
        verifyBit(V_1_1, 0x1, FRAG_DROP);
        verifyBit(V_1_1, 0x2, FRAG_REASM);
        verifyBit(V_1_1, 0x4, INV_TTL_TO_CTRLR, false);
        verifyNaU16(V_1_1, 0x8);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec()");
        // yeah, the "bit" value is ZERO - the decoder must return FRAG_NORMAL
        verifyBit(V_1_2, 0x0, FRAG_NORMAL);
        verifyBit(V_1_2, 0x1, FRAG_DROP);
        verifyBit(V_1_2, 0x2, FRAG_REASM);
        verifyBit(V_1_2, 0x4, INV_TTL_TO_CTRLR, false);
        verifyNaU16(V_1_2, 0x8);
    }

    @Test
    public void v13Codec() {
        print(EOL + "v13Codec()");
        // yeah, the "bit" value is ZERO - the decoder must return FRAG_NORMAL
        verifyBit(V_1_3, 0x0, FRAG_NORMAL);
        verifyBit(V_1_3, 0x1, FRAG_DROP);
        verifyBit(V_1_3, 0x2, FRAG_REASM);
        verifyNaBit(V_1_3, 0x4, INV_TTL_TO_CTRLR);
        verifyNaU16(V_1_3, 0x4);
    }

    @Test
    public void mutexDecode() {
        print(EOL + "mutexDecode()");
        for (ProtocolVersion pv: PV_0123) {
            try {
                ConfigFlag.decodeBitmap(0x3, pv);
                fail(AM_NOEX);
            } catch (IllegalStateException ise) {
                print(FMT_EX, ise);
                assertTrue(AM_HUH,
                        ise.getMessage().startsWith(ConfigFlag.E_MUTEX));
            } catch (Exception e) {
                print(e);
                fail(AM_WREX);
            }
        }
    }

    private void checkMutex(ProtocolVersion pv, ConfigFlag... flags) {
        try {
            Set<ConfigFlag> set = new HashSet<ConfigFlag>(Arrays.asList(flags));
            ConfigFlag.encodeBitmap(set, pv);
            fail(AM_NOEX);
        } catch (IllegalStateException ise) {
            print(FMT_EX, ise);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    @Test
    public void mutexEncode() {
        print(EOL + "mutexEncode()");
        for (ProtocolVersion pv: PV_0123) {
            checkMutex(pv, FRAG_NORMAL, FRAG_DROP);
            checkMutex(pv, FRAG_NORMAL, FRAG_REASM);
            checkMutex(pv, FRAG_DROP, FRAG_REASM);
            checkMutex(pv, FRAG_NORMAL, FRAG_DROP, FRAG_REASM);
        }
    }

}
