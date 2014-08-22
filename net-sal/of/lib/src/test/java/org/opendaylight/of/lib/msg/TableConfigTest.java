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
import static org.opendaylight.of.lib.msg.TableConfig.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for TableConfig enum.
 *
 * @author Simon Hunt
 */
public class TableConfigTest extends BitmappedEnumTest<TableConfig> {

    @Override
    protected Set<TableConfig> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return TableConfig.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<TableConfig> flags, ProtocolVersion pv) {
        return TableConfig.encodeBitmap(flags, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (TableConfig tc: TableConfig.values())
            print(tc);
        assertEquals(AM_UXCC, 3, TableConfig.values().length);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        // not supported in 1.0
        verifyNaU32(V_1_0, 0x1);
        // NOTE: No exception thrown for TMC, since encoded as zero (removed)
//        verifyNaBit(V_1_0, 0x8000, TABLE_MISS_CONTROLLER);
        verifyNaBit(V_1_0, 0x8000, TABLE_MISS_CONTINUE);
        verifyNaBit(V_1_0, 0x8000, TABLE_MISS_DROP);
    }

    @Test
    public void v11Decoder() {
        print(EOL + "v11Decoder()");
        // yeah, the "bit" value is ZERO - the decoder must return T_M_CNTRLR
        verifyBit(V_1_1, 0x0, TABLE_MISS_CONTROLLER);
        verifyBit(V_1_1, 0x1, TABLE_MISS_CONTINUE);
        verifyBit(V_1_1, 0x2, TABLE_MISS_DROP);
        verifyNaU32(V_1_1, 0x4);
    }

    @Test
    public void v12Decoder() {
        print(EOL + "v12Decoder()");
        // yeah, the "bit" value is ZERO - the decoder must return T_M_CNTRLR
        verifyBit(V_1_2, 0x0, TABLE_MISS_CONTROLLER);
        verifyBit(V_1_2, 0x1, TABLE_MISS_CONTINUE);
        verifyBit(V_1_2, 0x2, TABLE_MISS_DROP);
        verifyNaU32(V_1_2, 0x4);
    }

    @Test
    public void v13Decoder() {
        print(EOL + "v13Decoder()");
        // not supported in 1.3 (reserved)
        verifyNaU32(V_1_3, 0x1);
        // NOTE: No exception thrown for TMC, since encoded as zero (removed)
//        verifyNaBit(V_1_3, 0x8000, TABLE_MISS_CONTROLLER);
        verifyNaBit(V_1_3, 0x8000, TABLE_MISS_CONTINUE);
        verifyNaBit(V_1_3, 0x8000, TABLE_MISS_DROP);
    }

    @Test
    public void mutexDecode() {
        print(EOL + "mutexDecode()");
        for (ProtocolVersion pv: PV_12) {
            try {
                TableConfig.decodeBitmap(0x3, pv);
                fail(AM_NOEX);
            } catch (IllegalStateException ise) {
                print(FMT_EX, ise);
                assertTrue(AM_HUH,
                        ise.getMessage().startsWith(TableConfig.E_MUTEX));
            } catch (Exception e) {
                print(e);
                fail(AM_WREX);
            }
        }
    }

    private void checkMutex(ProtocolVersion pv, TableConfig... flags) {
        Set<TableConfig> set = new HashSet<TableConfig>(Arrays.asList(flags));
        try {
            TableConfig.encodeBitmap(set, pv);
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
        for (ProtocolVersion pv: PV_12) {
            checkMutex(pv, TABLE_MISS_CONTROLLER, TABLE_MISS_CONTINUE);
            checkMutex(pv, TABLE_MISS_CONTROLLER, TABLE_MISS_DROP);
            checkMutex(pv, TABLE_MISS_CONTINUE, TABLE_MISS_DROP);
            checkMutex(pv, TABLE_MISS_CONTROLLER, TABLE_MISS_CONTINUE,
                            TABLE_MISS_DROP);
        }
    }
}
