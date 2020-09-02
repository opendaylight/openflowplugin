/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Created by Martin Bobak mbobak@cisco.com on 6/30/14.
 */
public class ByteUtilTest {

    private static final String HEX_STRING = "64,65,66,ff";
    private static final String HEX_STRING00 = "00,00,00,00";
    private static final String HEX_STRINGFF = "ff,ff,ff,ff";

    private static final byte[] TEST_BYTES = {100, 101, 102, (byte) 255};
    private static final byte[] TEST_BYTES00 = {0, 0, 0, 0};
    private static final byte[] TEST_BYTESFF = {(byte) 255, (byte) 255, (byte) 255, (byte) 255};

    private static final byte[] TEST3_BYTES = {100, 101, 102};
    private static final byte[] TEST3_BYTES00 = {0, 0, 0};
    private static final byte[] TEST3_BYTESFF = {(byte) 255, (byte) 255, (byte) 255};

    private static final BigInteger BIG_INTEGER = new BigInteger("1684367103");
    private static final BigInteger BIG_INTFF = new BigInteger("4294967295");

    private static final long MEDIUM_INTEGER = 6579558;
    private static final long MEDIUM_INTEGERFF = 16777215;
    private static final int INT00 = 0;

    private static final int SHORT_BYTE_LENGTH = 2;
    private static final int MEDIUM_BYTE_LENGTH = 3;
    private static final int INT_BYTE_LENGTH = 4;

    /**
     * test of {@link ByteUtil#bytesToHexstring(byte[], String)}.
     */
    @Test
    public void testBytesToHexstring() {
        assertEquals(HEX_STRING, ByteUtil.bytesToHexstring(TEST_BYTES, ","));
        assertEquals(HEX_STRING00, ByteUtil.bytesToHexstring(TEST_BYTES00, ","));
        assertEquals(HEX_STRINGFF, ByteUtil.bytesToHexstring(TEST_BYTESFF, ","));
    }

    @Test
    public void testConvertBigIntegerToNBytes() {
        byte[] bigIntAsBytes = ByteUtil.convertBigIntegerToNBytes(BIG_INTEGER, 4);
        assertEquals(4, bigIntAsBytes.length);

        bigIntAsBytes = ByteUtil.convertBigIntegerToNBytes(BIG_INTEGER, 6);
        assertEquals(6, bigIntAsBytes.length);

        bigIntAsBytes = ByteUtil.convertBigIntegerToNBytes(BIG_INTEGER, 8);
        assertEquals(8, bigIntAsBytes.length);
    }

    @Test
    public void testUint64toBytes() {
        final Uint64 value = Uint64.valueOf("0102030405060708", 16);
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8}, ByteUtil.uint64toBytes(value));
    }

    @Test
    public void testBytesToUnsignedInt() {
        long unsigned = ByteUtil.bytesToUnsignedInt(TEST_BYTES);
        assertEquals(BIG_INTEGER.longValue(), unsigned);

        unsigned = ByteUtil.bytesToUnsignedInt(TEST_BYTES00);
        assertEquals(0, unsigned);

        unsigned = ByteUtil.bytesToUnsignedInt(TEST_BYTESFF);
        assertEquals(BIG_INTFF.longValue(), unsigned);
    }

    @Test
    public void testBytesToUnsignedShort() {

        byte[] twoBytes = {100, 101};
        int unsigned = ByteUtil.bytesToUnsignedShort(twoBytes);
        assertEquals(BIG_INTEGER.shiftRight(16).shortValue(), unsigned);

        twoBytes = new byte[]{0, 0};
        unsigned = ByteUtil.bytesToUnsignedShort(twoBytes);
        assertEquals(INT00, unsigned);

        twoBytes = new byte[]{(byte) 255, (byte) 255};
        unsigned = ByteUtil.bytesToUnsignedShort(twoBytes);
        assertEquals(BIG_INTFF.shiftRight(16).intValue(), unsigned);
    }

    @Test
    public void testBytesToUnsignedMedium() {
        long unsigned = ByteUtil.bytesToUnsignedMedium(TEST3_BYTES);
        assertEquals(MEDIUM_INTEGER, unsigned);

        unsigned = ByteUtil.bytesToUnsignedMedium(TEST3_BYTES00);
        assertEquals(0, unsigned);

        unsigned = ByteUtil.bytesToUnsignedMedium(TEST3_BYTESFF);
        assertEquals(MEDIUM_INTEGERFF, unsigned);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionTestBytesToUnsignedShort() {
        ByteUtil.bytesToUnsignedShort(TEST_BYTES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionTestBytesToUnsignedInt() {
        byte[] fiveBytes = {0, 0, 0, 0, 0};
        ByteUtil.bytesToUnsignedInt(fiveBytes);
    }

    @Test
    public void testUnsignedIntToBytes() {
        long intValue = 255;
        byte[] bytes = ByteUtil.unsignedIntToBytes(intValue);

        assertTrue(bytes.length == INT_BYTE_LENGTH);

        intValue += 256;
        bytes = ByteUtil.unsignedIntToBytes(intValue);
        assertTrue(bytes.length == INT_BYTE_LENGTH);

        intValue += 256;
        bytes = ByteUtil.unsignedIntToBytes(intValue);
        assertTrue(bytes.length == INT_BYTE_LENGTH);
    }

    @Test
    public void testUnsignedShortToBytes() {
        int intValue = 255;
        byte[] bytes = ByteUtil.unsignedShortToBytes(intValue);

        assertTrue(bytes.length == SHORT_BYTE_LENGTH);

        intValue += 256;
        bytes = ByteUtil.unsignedShortToBytes(intValue);
        assertTrue(bytes.length == SHORT_BYTE_LENGTH);

        intValue += 256;
        bytes = ByteUtil.unsignedShortToBytes(intValue);
        assertTrue(bytes.length == SHORT_BYTE_LENGTH);
    }

    @Test
    public void testUnsignedMediumToBytes() {
        long intValue = 255;
        byte[] bytes = ByteUtil.unsignedMediumToBytes(intValue);

        assertTrue(bytes.length == MEDIUM_BYTE_LENGTH);

        intValue += 256;
        bytes = ByteUtil.unsignedMediumToBytes(intValue);
        assertTrue(bytes.length == MEDIUM_BYTE_LENGTH);

        intValue += 256;
        bytes = ByteUtil.unsignedMediumToBytes(intValue);
        assertTrue(bytes.length == MEDIUM_BYTE_LENGTH);
    }

}
