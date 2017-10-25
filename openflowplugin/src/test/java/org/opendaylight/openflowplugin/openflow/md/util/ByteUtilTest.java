/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.junit.Test;

/**
 * Created by Martin Bobak mbobak@cisco.com on 6/30/14.
 */
public class ByteUtilTest {

    private static final String hexString = "64,65,66,ff";
    private static final String hexString00 = "00,00,00,00";
    private static final String hexStringFF = "ff,ff,ff,ff";

    private static final byte[] testBytes = {100, 101, 102, (byte) 255};
    private static final byte[] testBytes00 = {0, 0, 0, 0};
    private static final byte[] testBytesFF = {(byte) 255, (byte) 255, (byte) 255, (byte) 255};

    private static final byte[] test3Bytes = {100, 101, 102};
    private static final byte[] test3Bytes00 = {0, 0, 0};
    private static final byte[] test3BytesFF = {(byte) 255, (byte) 255, (byte) 255};

    private static final BigInteger bigInteger = new BigInteger("1684367103");
    private static final BigInteger bigIntFF = new BigInteger("4294967295");

    private static final Integer mediumInteger = new Integer("6579558");
    private static final Integer mediumIntegerFF = new Integer("16777215");
    private static final int int00 = 0;

    private static final int shortByteLength = 2;
    private static final int mediumByteLength = 3;
    private static final int intByteLength = 4;

    /**
     * test of {@link ByteUtil#bytesToHexstring(byte[], String)}
     */
    @Test
    public void testBytesToHexstring() {
        assertEquals(hexString, ByteUtil.bytesToHexstring(testBytes, ","));
        assertEquals(hexString00, ByteUtil.bytesToHexstring(testBytes00, ","));
        assertEquals(hexStringFF, ByteUtil.bytesToHexstring(testBytesFF, ","));
    }

    @Test
    public void testConvertBigIntegerToNBytes() {
        byte[] bigIntAsBytes = ByteUtil.convertBigIntegerToNBytes(bigInteger, 4);
        assertEquals(4, bigIntAsBytes.length);

        bigIntAsBytes = ByteUtil.convertBigIntegerToNBytes(bigInteger, 6);
        assertEquals(6, bigIntAsBytes.length);

        bigIntAsBytes = ByteUtil.convertBigIntegerToNBytes(bigInteger, 8);
        assertEquals(8, bigIntAsBytes.length);
    }

    @Test
    public void testBytesToUnsignedInt() {
        long unsigned = ByteUtil.bytesToUnsignedInt(testBytes);
        assertEquals(bigInteger.longValue(), unsigned);

        unsigned = ByteUtil.bytesToUnsignedInt(testBytes00);
        assertEquals(0, unsigned);

        unsigned = ByteUtil.bytesToUnsignedInt(testBytesFF);
        assertEquals(bigIntFF.longValue(), unsigned);
    }

    @Test
    public void testBytesToUnsignedShort() {

        byte[] twoBytes = {100, 101};
        int unsigned = ByteUtil.bytesToUnsignedShort(twoBytes);
        assertEquals(bigInteger.shiftRight(16).shortValue(), unsigned);

        twoBytes = new byte[]{0, 0};
        unsigned = ByteUtil.bytesToUnsignedShort(twoBytes);
        assertEquals(int00, unsigned);

        twoBytes = new byte[]{(byte) 255, (byte) 255};
        unsigned = ByteUtil.bytesToUnsignedShort(twoBytes);
        assertEquals(bigIntFF.shiftRight(16).intValue(), unsigned);
    }

    @Test
    public void testBytesToUnsignedMedium() {
        long unsigned = ByteUtil.bytesToUnsignedMedium(test3Bytes);
        assertEquals(mediumInteger.longValue(), unsigned);

        unsigned = ByteUtil.bytesToUnsignedMedium(test3Bytes00);
        assertEquals(0, unsigned);

        unsigned = ByteUtil.bytesToUnsignedMedium(test3BytesFF);
        assertEquals(mediumIntegerFF.longValue(), unsigned);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionTestBytesToUnsignedShort() {
        ByteUtil.bytesToUnsignedShort(testBytes);
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

        assertTrue(bytes.length == intByteLength);

        intValue += 256;
        bytes = ByteUtil.unsignedIntToBytes(intValue);
        assertTrue(bytes.length == intByteLength);

        intValue += 256;
        bytes = ByteUtil.unsignedIntToBytes(intValue);
        assertTrue(bytes.length == intByteLength);
    }

    @Test
    public void testUnsignedShortToBytes() {
        int intValue = 255;
        byte[] bytes = ByteUtil.unsignedShortToBytes(intValue);

        assertTrue(bytes.length == shortByteLength);

        intValue += 256;
        bytes = ByteUtil.unsignedShortToBytes(intValue);
        assertTrue(bytes.length == shortByteLength);

        intValue += 256;
        bytes = ByteUtil.unsignedShortToBytes(intValue);
        assertTrue(bytes.length == shortByteLength);
    }

    @Test
    public void testUnsignedMediumToBytes() {
        long intValue = 255;
        byte[] bytes = ByteUtil.unsignedMediumToBytes(intValue);

        assertTrue(bytes.length == mediumByteLength);

        intValue += 256;
        bytes = ByteUtil.unsignedMediumToBytes(intValue);
        assertTrue(bytes.length == mediumByteLength);

        intValue += 256;
        bytes = ByteUtil.unsignedMediumToBytes(intValue);
        assertTrue(bytes.length == mediumByteLength);
    }

}
