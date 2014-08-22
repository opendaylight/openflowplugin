/*
 * (c) Copyright 2007-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.opendaylight.util.junit.TestTools;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.ByteUtils.*;
import static org.opendaylight.util.StringUtils.EOL;
import static org.junit.Assert.*;


/**
 * Set of tests of the byte utilities.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class ByteUtilsTest {

    // used to help in expressing bytes (with sign bit set) concisely
    //  e.g. you can say   0xff-B   instead of casting with   (byte) 0xff
    private static final int B = 256;

    private static final String E_STRING = "incorrect string value";
    private static final String E_SHORT = "incorrect short value";
    private static final String E_INT = "incorrect int value";
    private static final String E_LONG = "incorrect long value";
    private static final String E_INDEX = "incorrect index";
    private static final String E_BIT_COUNT = "incorrect bit count";

    private byte[] b;

    @Test
    public void byteToIntAndBack() {
        print(EOL + "byteToIntAndBack()");
        for (int i=0; i<256; i++) {
            byte b = intToByte(i);
            print ("int " + i + " -> byte " + b);

            int back = byteToInt(b);
            assertEquals("inverse failed", i, back);
        }
        assertEquals(AM_HUH, 0, byteToInt((byte) 0));
        assertEquals(AM_HUH, 255, byteToInt((byte) -1));
        assertEquals(AM_HUH, 127, byteToInt((byte) 127));
        assertEquals(AM_HUH, 128, byteToInt((byte) -128));
    }


    @Test
    public void testToHexString() {
        print(TestTools.EOL + "testToHexString()");
        b = new byte[1];
        b[0] = 18;
        String s = toHexString(b);
        print(s);
        assertEquals(E_STRING, "12", s);

        b = new byte[2];
        setShort(b, 0, -1);
        s = toHexString(b);
        print(s);
        assertEquals(E_STRING, "ffff", s);

        b = new byte[] { 254-B, 127, 3 };
        s = toHexString(b);
        print(s);
        assertEquals(E_STRING, "fe7f 03", s);

        b = new byte[] { 0xfe-B, 0x7f, 0x03 };
        s = toHexString(b);
        print(s);
        assertEquals(E_STRING, "fe7f 03", s);

        b = new byte[4];
        setInteger(b, 0, Integer.MAX_VALUE);
        s = toHexString(b);
        print(s);
        assertEquals(E_STRING, "7fff ffff", s);
    }

    @Test
    public void testShort() {
        print(EOL + "testShort()");
        b = new byte[32];
        setShort(b, 0, (short) 1234);
        setShort(b, 16, (short) 4321);
        setShort(b, 24, 4321);
        print(toHexString(b));
        assertEquals(E_SHORT, (short) 1234, getShort(b, 0));
        assertEquals(E_SHORT, (short) 4321, getShort(b, 16));
        assertEquals(E_SHORT, (short) 4321, getShort(b, 24));

        setShort(b, 0, (short) 0);
        setShort(b, 16, (short) 0xffff);
        print(toHexString(b));
        assertEquals(E_SHORT, (short) 0, getShort(b, 0));
        assertEquals(E_SHORT, (short) 0xffff, getShort(b, 16));
    }

    @Test
    public void testInteger() {
        print(EOL + "testInteger()");
        b = new byte[32];
        setInteger(b, 0, 123456);
        setInteger(b, 16, 654321);
        setInteger(b, 24, 654321L);
        print(toHexString(b));
        assertEquals(E_INT, 123456, getInteger(b, 0));
        assertEquals(E_INT, 654321, getInteger(b, 16));
        assertEquals(E_INT, 654321, getInteger(b, 24));

        setInteger(b, 0, 0);
        setInteger(b, 16, 0xffffffff);
        print(toHexString(b));
        assertEquals(E_INT, 0, getInteger(b, 0));
        assertEquals(E_INT, 0xffffffff, getInteger(b, 16));

        b = new byte[20];
        setInteger(b, 9, 0xABCDEF12);
        print(toHexString(b));
        assertArrayEquals(AM_NEQ, b, new byte[] {
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xAB-B,
                0xCD-B, 0xEF-B, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        });
    }

    @Test
    public void testLong() {
        print(EOL + "testLong()");
        b = new byte[32];
        setLong(b, 0, 1234567890L);
        setLong(b, 16, 1098765432L);
        print(toHexString(b));
        assertEquals(E_LONG, 1234567890L, getLong(b, 0));
        assertEquals(E_LONG, 1098765432L, getLong(b, 16));

        setLong(b, 0, 0L);
        setLong(b, 16, 0xffffffffffffffffL);
        print(toHexString(b));
        assertEquals(E_LONG, 0L, getLong(b, 0));
        assertEquals(E_LONG, 0xffffffffffffffffL, getLong(b, 16));

        setLong(b, 16, 7680002016L);
        print(toHexString(b));
        assertEquals(E_LONG, 7680002016L, getLong(b, 16));
    }

    @Test
    public void testIndexOf() {
        print(EOL + "testIndexOf()");
        String msg = "Hello World!! What's going on?";
        b = msg.getBytes();
        print(msg);
        assertEquals(E_INDEX, -1, indexOf(b, (byte) ';', 0));
        assertEquals(E_INDEX, 11, indexOf(b, (byte) '!', 0));
        assertEquals(E_INDEX, 11, indexOf(b, (byte) '!', 11));
        assertEquals(E_INDEX, 12, indexOf(b, (byte) '!', 12));
        assertEquals(E_INDEX, 29, indexOf(b, (byte) '?', 13));
        assertEquals(E_INDEX, 6, indexOf(b, (byte) 'W'));
        assertEquals(E_INDEX, 6, indexOf(b, (byte) 'W', 4));
        assertEquals(E_INDEX, 6, indexOf(b, (byte) 'W', 6));
        assertEquals(E_INDEX, 14, indexOf(b, (byte) 'W', 7));
        assertEquals(E_INDEX, -1, indexOf(b, (byte) 'W', 7, 14));
    }

    @Test
    public void testBitsInBytes() {
        print(EOL + "testBitsInBytes()");
        assertEquals(E_BIT_COUNT, 0, countBitsInByte((byte) 0));
        assertEquals(E_BIT_COUNT, 1, countBitsInByte((byte) 1));
        assertEquals(E_BIT_COUNT, 2, countBitsInByte((byte) 10));
        assertEquals(E_BIT_COUNT, 3, countBitsInByte((byte) 7));
        assertEquals(E_BIT_COUNT, 3, countBitsInByte((byte) 14));
        assertEquals(E_BIT_COUNT, 4, countBitsInByte((byte) 15));
        assertEquals(E_BIT_COUNT, 1, countBitsInByte((byte) 16));
        assertEquals(E_BIT_COUNT, 7, countBitsInByte((byte) 254));
        assertEquals(E_BIT_COUNT, 8, countBitsInByte((byte) 255));

        b = new byte[] { 1, 1, 10, 7, 14, 15, 16, 254-B, 255-B, 255-B};
        //       # bits: 1  1   2  3   3   4   1   7      8      8
        //                  ------------------------------- =29
        //               ----------- = 7
        //                             ----- = 7
        //                         ------------------------------- = 34
        //                                          -------------- = 23  *
        //               ----------------------------------------- = 38  **
        print(toHexString(b));
        verifyBitCount(b, 1, 8, 29);
        verifyBitCount(b, 0, 4, 7);
        verifyBitCount(b, 4, 2, 7);
        verifyBitCount(b, 3, 7, 34);
        assertEquals(E_BIT_COUNT, 23, countBits(b, 7));     // *
        assertEquals(E_BIT_COUNT, 38, countBits(b));        // **
    }

    private void verifyBitCount(byte[] b, int offset, int length,
                                long expectedCount) {
        long result = countBits(b, offset, length);
        print("  off=" + offset + ", len=" + length +
                ", expected=" + expectedCount);
        assertEquals(E_BIT_COUNT, expectedCount, result);
    }

    @Test
    public void testHexWithOffset() {
        print(EOL + "testHexWithOffset()");
        assertEquals(AM_NEQ, "1234fedc", hex(ARRAY_C, 0));
        assertEquals(AM_NEQ, "34fedc", hex(ARRAY_C, 1));
        assertEquals(AM_NEQ, "fedc", hex(ARRAY_C, 2));
        assertEquals(AM_NEQ, "dc", hex(ARRAY_C, 3));
    }

    @Test
    public void testHex() {
        print(EOL + "testHex()");
        String s = "foobar";
        String a = hex(s.getBytes());
        b = parseHex(a);
        String t = new String(b);
        assertEquals("incorrect hex string", s, t);
    }

    @Test
    public void testHexEmpty() {
        print(EOL + "testHexEmpty()");
        assertEquals("incorrect hex string", "", hex(new byte[0]));
    }

    @Test
    public void testBadHex() {
        print(EOL + "testBadHex()");
        try {
            parseHex("deadbeeff");
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX> " + e);
            assertTrue(AM_HUH, e.getMessage().startsWith(ByteUtils.E_NOT_EVEN));
        }
    }

    @Test
    public void testNonHexParsing() {
        print(EOL + "testNonHexParsing()");
        try {
            parseHex("failme");
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX> " + e);
            assertTrue(AM_HUH, e.getMessage().startsWith(ByteUtils.E_NON_HEX));
        }
    }

    @Test
    public void testGetPadded64BitBytes() {
        print(EOL + "testGetPadded64BitBytes()");
        byte[] inputBa = new byte[] {
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15,
                0x16, 0x17
        };
        b = getPadded64BitBytes(inputBa);
        print(toHexString(b));
        byte[] targetBa = new byte[] {
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15,
                0x16, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
        assertArrayEquals(AM_NEQ, b, targetBa);

        assertArrayEquals(AM_NEQ, new byte[] {},
                getPadded64BitBytes(new byte[] {}));
    }

    private static final String ALL_HEX =
            "000102030405060708090a0b0c0d0e0f" +
            "101112131415161718191a1b1c1d1e1f" +
            "202122232425262728292a2b2c2d2e2f" +
            "303132333435363738393a3b3c3d3e3f" +
            "404142434445464748494a4b4c4d4e4f" +
            "505152535455565758595a5b5c5d5e5f" +
            "606162636465666768696a6b6c6d6e6f" +
            "707172737475767778797a7b7c7d7e7f" +
            "00000000000000000000000000000000" +
            "00000000000000000000000000000000" +
            "a0a1a2a3a4a5a6a7a8a9aaabacadaeaf" +
            "b0b1b2b3b4b5b6b7b8b9babbbcbdbebf" +
            "c0c1c2c3c4c5c6c7c8c9cacbcccdcecf" +
            "d0d1d2d3d4d5d6d7d8d9dadbdcdddedf" +
            "e0e1e2e3e4e5e6e7e8e9eaebecedeeef" +
            "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff";

    private static final String ALL_HEX_SP =
            " 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f" + EOL +
            " 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f" + EOL +
            " 20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f" + EOL +
            " 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f" + EOL +
            " 40 41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f" + EOL +
            " 50 51 52 53 54 55 56 57 58 59 5a 5b 5c 5d 5e 5f" + EOL +
            " 60 61 62 63 64 65 66 67 68 69 6a 6b 6c 6d 6e 6f" + EOL +
            " 70 71 72 73 74 75 76 77 78 79 7a 7b 7c 7d 7e 7f" + EOL +
            " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00" + EOL +
            " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00" + EOL +
            " a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa ab ac ad ae af" + EOL +
            " b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd be bf" + EOL +
            " c0 c1 c2 c3 c4 c5 c6 c7 c8 c9 ca cb cc cd ce cf" + EOL +
            " d0 d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df" + EOL +
            " e0 e1 e2 e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef" + EOL +
            " f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc fd fe ff" + EOL;

    private byte[] createByteArray() {
        byte[] ba = new byte[256];
        for (int i = 0; i < ba.length; i++) {
            if (128 > i || 159 < i)
                ba[i] = (byte) i;
        }
        return ba;
    }

    @Test
    public void testStringRawBytesConversion() {
        print(EOL + "testStringRawBytesConversion()");
        String s1 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                     "1234567890!@#$%^&*()-_=+[]{}\\|;':,./<>?`~";
        b = getRawBytes(s1);
        String s2 = getStringFromRawBytes(b);
        assertEquals(E_STRING, s1, s2);
    }

    private static final String LAZY_DOG_STR =
            "The Lazy Dog snarled at the Quick Brown Fox!";

    private static final String LAZY_DOG_NTA =
            "5468 6520 4c61 7a79 2044 6f67 2073 6e61 726c 6564 2061 7420 " +
            "7468 6520 5175 6963 6b20 4272 6f77 6e20 466f 7821 0000 0000";

    @Test
    public void testNullTerminatedAscii() {
        print(EOL + "testNullTerminatedAscii()");
        byte[] asciiBytes = parseHex(LAZY_DOG_NTA);
        String lazy = getNullTerminatedAscii(asciiBytes);
        print("LAZY> \"{}\"", lazy);
        assertEquals(AM_NEQ, LAZY_DOG_STR, lazy);
    }

    @Test
    public void testGetDebugStringOffset() {
        print(EOL + "testGetDebugStringOffset()");
        byte[] targetBa = createByteArray();
        String s = getDebugString("ABCabc", targetBa, 30);
        assertEquals(E_STRING, s, "ABCabc (30 bytes):" + EOL +
                                  " 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f" + EOL +
                                  " 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d" + EOL);
    }

    @Test
    public void testGetDebugString() {
        print(EOL + "testGetDebugString()");
        byte[] targetBa = createByteArray();
        String s = getDebugString("ABCabc", targetBa);
        assertEquals(E_STRING, s, "ABCabc (256 bytes):" + EOL + ALL_HEX_SP + EOL);
    }

    @Test
    public void testHex2() {
        print(EOL + "testHex2()");
        String s = hex(createByteArray());
        assertEquals(E_STRING, s, ALL_HEX);
    }

    @Test
    public void testParseHex() {
        print(EOL + "testParseHex()");
        byte[] targetBa = createByteArray();
        b = parseHex(ALL_HEX);
        assertArrayEquals(AM_NEQ, b, targetBa);
    }

    @Test
    public void testGetByteArrayStringOffset() {
        print(EOL + "testGetByteArrayStringOffset()");
        byte[] targetBa = createByteArray();
        String s = getByteArrayString(targetBa, 15);
        assertEquals(E_STRING, s, " 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e" + EOL);
    }

    @Test
    public void testGetByteArrayString() {
        print(EOL + "testGetByteArrayString()");
        byte[] targetBa = createByteArray();
        String s = getByteArrayString(targetBa);
        assertEquals(E_STRING, s, ALL_HEX_SP + EOL);
    }

    @Test
    public void testGetFilteredString() {
        print(EOL + "testGetByteArrayString()");
        byte[] targetBa = createByteArray();
        String s = getFilteredString(targetBa);
        // Test the start and end of the string matching since the middle is a muck of platform/encoding dependent content.
        assertTrue(E_STRING + ":" + s, s.startsWith(
                "[................................................0123456789" +
                ".......ABCDEFGHIJKLMNOPQRSTUVWXYZ......abcdefghijklmnopqrst" +
                "uvwxyz....................................................." +
                "..........................................................." +
                "....................."));
        assertTrue(E_STRING + ":" + s, s.endsWith(
                "] [00 01 02 03 04 05" +
                " 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f 20 21 22 23 24 25" +
                " 26 27 28 29 2a 2b 2c 2d 2e 2f 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f 40 41 42 43 44 45" +
                " 46 47 48 49 4a 4b 4c 4d 4e 4f 50 51 52 53 54 55 56 57 58 59 5a 5b 5c 5d 5e 5f 60 61 62 63 64 65" +
                " 66 67 68 69 6a 6b 6c 6d 6e 6f 70 71 72 73 74 75 76 77 78 79 7a 7b 7c 7d 7e 7f 00 00 00 00 00 00" +
                " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 a0 a1 a2 a3 a4 a5" +
                " a6 a7 a8 a9 aa ab ac ad ae af b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd be bf c0 c1 c2 c3 c4 c5" +
                " c6 c7 c8 c9 ca cb cc cd ce cf d0 d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df e0 e1 e2 e3 e4 e5" +
                " e6 e7 e8 e9 ea eb ec ed ee ef f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc fd fe ff]"));
    }

    // ==== Some tests for the logical bit operations

    private static final byte[] ARRAY_A       = { 0xaa-B, 0x55 };
    private static final byte[] NOT_A         = { 0x55,   0xaa-B };
    private static final byte[] TOP_NOT_A     = { 0x55,   0x55 };

    private static final byte[] ARRAY_B       = { 0xf0-B, 0x0f };
    private static final byte[] NOT_B         = { 0x0f,   0xf0-B };
    private static final byte[] TAIL_NOT_B    = { 0xf0-B, 0xf0-B };

    private static final byte[] ARRAY_C       = { 0x12,   0x34,   0xfe-B, 0xdc-B };
    private static final byte[] NOT_C         = { 0xed-B, 0xcb-B, 0x01,   0x23 };
    private static final byte[] MID_NOT_C     = { 0x12,   0xcb-B, 0x01,   0xdc-B };

    private static final byte[] AND_A_B       = { 0xa0-B, 0x05 };
    private static final byte[] TOP_AND_A_B   = { 0xa0-B, 0x55 };
    private static final byte[] AND_A_C       = { 0x02,   0x14 };
    private static final byte[] TOP_AND_A_C   = { 0x02,   0x55 };
    private static final byte[] AND_C_B       = { 0x10,   0x04,   0xfe-B, 0xdc-B };
    private static final byte[] MID_AND_C_B   = { 0x12,   0x30,   0x0e,   0xdc-B };

    private static final byte[] OR_A_B        = { 0xfa-B, 0x5f };
    private static final byte[] TOP_OR_A_B    = { 0xfa-B, 0x55 };
    private static final byte[] OR_A_C        = { 0xba-B, 0x75 };
    private static final byte[] TOP_OR_A_C    = { 0xba-B, 0x55 };
    private static final byte[] OR_C_B        = { 0xf2-B, 0x3f,   0xfe-B, 0xdc-B };
    private static final byte[] MID_OR_C_B    = { 0x12,   0xf4-B, 0xff-B, 0xdc-B };

    private static final byte[] XOR_A_B        = { 0x5a,   0x5a };
    private static final byte[] TOP_XOR_A_B    = { 0x5a,   0x55 };
    private static final byte[] XOR_A_C        = { 0xb8-B, 0x61 };
    private static final byte[] TOP_XOR_A_C    = { 0xb8-B, 0x55 };
    private static final byte[] XOR_C_B        = { 0xe2-B, 0x3b,   0xfe-B, 0xdc-B };
    private static final byte[] MID_XOR_C_B    = { 0x12,   0xc4-B, 0xf1-B, 0xdc-B };

    @Test
    public void bitsXor() {
        print(EOL + "bitsXor()");

        verifyBitsXor(ARRAY_A, ARRAY_B, XOR_A_B, TOP_XOR_A_B, 0, 1);
        verifyBitsXor(ARRAY_A, ARRAY_C, XOR_A_C, TOP_XOR_A_C, 0, 1);
        verifyBitsXor(ARRAY_C, ARRAY_B, XOR_C_B, MID_XOR_C_B, 1, 3);
    }

    @Test
    public void bitsOr() {
        print(EOL + "bitsOr()");

        verifyBitsOr(ARRAY_A, ARRAY_B, OR_A_B, TOP_OR_A_B, 0, 1);
        verifyBitsOr(ARRAY_A, ARRAY_C, OR_A_C, TOP_OR_A_C, 0, 1);
        verifyBitsOr(ARRAY_C, ARRAY_B, OR_C_B, MID_OR_C_B, 1, 3); // note length is longer than array B
    }

    private void verifyBitsXor(byte[] orig, byte[] other, byte[] expAll, byte[] expPart, int off, int len) {
        printVerify(orig);
        printOther(other);
        b = orig.clone();
        xor(b, other);
        printAll(b);
        assertArrayEquals(AM_NEQ, b, expAll);

        b = orig.clone();
        xor(b, other, off, len);
        printPart(b, off, len);
        assertArrayEquals(AM_NEQ, b, expPart);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsXorAioobNegative() {
        xor(ARRAY_A.clone(), ARRAY_B, -1);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsXorAioobTooBig() {
        xor(ARRAY_A.clone(), ARRAY_B, 4);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsXorAioobTooLong() {
        xor(ARRAY_A.clone(), ARRAY_B, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void xorLengthTooSmall() {
        xor(ARRAY_A.clone(), ARRAY_B, 0, -1);
    }

    private void verifyBitsOr(byte[] orig, byte[] other, byte[] expAll, byte[] expPart, int off, int len) {
        printVerify(orig);
        printOther(other);
        b = orig.clone();
        or(b, other);
        printAll(b);
        assertArrayEquals(AM_NEQ, b, expAll);

        b = orig.clone();
        or(b, other, off, len);
        printPart(b, off, len);
        assertArrayEquals(AM_NEQ, b, expPart);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsOrAioobNegative() {
        or(ARRAY_A.clone(), ARRAY_B, -1);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsOrAioobTooBig() {
        or(ARRAY_A.clone(), ARRAY_B, 4);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsOrAioobTooLong() {
        or(ARRAY_A.clone(), ARRAY_B, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void orLengthTooSmall() {
        or(ARRAY_A.clone(), ARRAY_B, 0, -1);
    }

    @Test
    public void bitsAnd() {
        print(EOL + "bitsAnd()");

        verifyBitsAnd(ARRAY_A, ARRAY_B, AND_A_B, TOP_AND_A_B, 0, 1);
        verifyBitsAnd(ARRAY_A, ARRAY_C, AND_A_C, TOP_AND_A_C, 0, 1);
        verifyBitsAnd(ARRAY_C, ARRAY_B, AND_C_B, MID_AND_C_B, 1, 3); // note length is longer than array B
    }

    private void verifyBitsAnd(byte[] orig, byte[] other, byte[] expAll, byte[] expPart, int off, int len) {
        printVerify(orig);
        printOther(other);
        b = orig.clone();
        and(b, other);
        printAll(b);
        assertArrayEquals(AM_NEQ, b, expAll);

        b = orig.clone();
        and(b, other, off, len);
        printPart(b, off, len);
        assertArrayEquals(AM_NEQ, b, expPart);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsAndAioobNegative() {
        and(ARRAY_A.clone(), ARRAY_B, -1);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsAndAioobTooBig() {
        and(ARRAY_A.clone(), ARRAY_B, 4);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsAndAioobTooLong() {
        and(ARRAY_A.clone(), ARRAY_B, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void andLengthTooSmall() {
        and(ARRAY_A.clone(), ARRAY_B, 0, -1);
    }

    @Test
    public void bitsNot() {
        print(EOL + "bitsNot()");

        verifyBitsNot(ARRAY_A, NOT_A, TOP_NOT_A, 0, 1);
        verifyBitsNot(ARRAY_B, NOT_B, TAIL_NOT_B, 1, 1);
        verifyBitsNot(ARRAY_C, NOT_C, MID_NOT_C, 1, 2);
    }

    private void verifyBitsNot(byte[] orig, byte[] expAll, byte[] expPart, int off, int len) {
        printVerify(orig);
        b = orig.clone();
        not(b);
        printAll(b);
        assertArrayEquals(AM_NEQ, b, expAll);

        b = orig.clone();
        not(b, off, len);
        printPart(b, off, len);
        assertArrayEquals(AM_NEQ, b, expPart);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsNotAioobNegative() {
        not(ARRAY_A.clone(), -1);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsNotAioobTooBig() {
        not(ARRAY_A.clone(), 4);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void bitsNotAioobTooLong() {
        not(ARRAY_A.clone(), 1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notLengthTooSmall() {
        not(ARRAY_A.clone(), 0, -1);
    }

    private void printVerify(byte[] b) {
        print(EOL + "   verify: " + toHexString(b));
    }

    private void printOther(byte[] b) {
        print("    OTHER: " + toHexString(b));
    }

    private void printAll(byte[] b) {
        print("      ALL: " + toHexString(b));
    }

    private void printPart(byte[] b, int off, int len) {
        print("     PART: " + toHexString(b) + "  (offset:" + off + ", length:" + len + ")");
    }


    @Test
    public void testHexParsingWithWhitespace() {
        print(EOL + "testHexParsingWithWhitespace()");
        final String s = "0f 55 a7  99";
        print("input: " + s);
        byte[] result = parseHex(s);
        print("result: " + toHexString(result));

    }

    @Test
    public void testHexParsingWithWhitespaceAndNewLines() {
        print(EOL + "testHexParsingWithWhitespaceAndNewLines()");
        final String s = "0f 55 a7  99" + EOL +
                         "babe c001" + EOL +
                         "f00b" + EOL;
        print("input: " + s);
        byte[] result = parseHex(s);
        print("result: " + toHexString(result));

    }

    @Test
    public void testHexParsingWithPunct() {
        print(EOL + "testHexParsingWithPunct()");
        final String s = "00:45:56:af:77:2b";
        print("input: " + s);
        byte[] result = parseHex(s);
        print("result: " + toHexString(result));
    }

    @Test
    public void testByteToHex() {
        print(EOL + "testByteToHex()");
        assertEquals(AM_NEQ, "ff", byteToHex((byte) 0xff));
        assertEquals(AM_NEQ, "00", byteToHex((byte) 0x00));
        assertEquals(AM_NEQ, "24", byteToHex((byte) 0x24));
        assertEquals(AM_NEQ, "e6", byteToHex((byte) 0xe6));
    }

    @Test(expected = NullPointerException.class)
    public void testToHexArrayStringNull() {
        toHexArrayString(null);
    }

    private static final byte[] BYTES_ONE = new byte[] {
            0xff-B, 0xee-B, 0x34, 0x00
    };

    @Test
    public void testToHexArrayStringOne() {
        print(EOL + "testToHexArrayStringOne()");
        String s = toHexArrayString(BYTES_ONE);
        print(s);
        assertEquals(AM_NEQ, "0x[ ff, ee, 34, 00 ]", s);
    }

    // ==== the following test is not of ByteUtils functionality per se
    // ==== but testing the slurping of a "Human Readable Hex Packet"

    private static final byte[] EXP_PSEUDO_PACKET = new byte[]{
            0xb3-B, 0x09, 0x0e, 0x00, 0x45, 0xf7-B,
            0x00, 0x0c, 0x54, 0x45, 0xff-B, 0x2b
    };

    private static final byte[] EXP_MY_DATA = new byte[]{
            0x65, 0x02,
            0x00, 0x01, 0x02, 0xab-B, 0xcd-B, 0xef-B
    };

    @Test
    public void humanReadableByteArray() throws IOException {
        print(EOL + "humanReadableByteArray()");
        String path = "org/opendaylight/util/byteutils/pseudopacket.hex";
        ClassLoader classloader = getClass().getClassLoader();

        byte[] bytes = slurpBytesFromHexFile(path, classloader);
        String s = ByteUtils.toHexArrayString(bytes);
        print(s);
        assertArrayEquals(AM_NEQ, EXP_PSEUDO_PACKET, bytes);
    }

    @Test
    public void slurpNoBytes() throws IOException {
        print(EOL + "slurpNoBytes()");
        String path = "path/to/nowhere.hex";
        ClassLoader classloader = getClass().getClassLoader();

        byte[] bytes = slurpBytesFromHexFile(path, classloader);
        assertNull(AM_HUH, bytes);
    }

    @Test
    public void slurpUsingClass() throws IOException {
        print(EOL + "slurpUsingClass()");
        byte[] bytes = slurpBytesFromHexFile(ByteUtils.class, "mydata");
        assertArrayEquals(AM_NEQ, EXP_MY_DATA, bytes);
    }

    // === Unit tests for the get/set U8/16/32/64 methods

    private static final byte[] UBYTES = {
            0xff-B,                     // u8 = 255
            0xb0-B, 0x00,               // u16 = 45056
            0x80-B, 0x00, 0x00, 0x00,   // u32 = 2147483648L
            0x01                        // u64 (from first byte)
                                            // = 18424226625328513025
    };
    private static final short EXP_U8 = 255;
    private static final int EXP_U16 = 45056;
    private static final long EXP_U32 = 2147483648L;
    private static final BigInteger EXP_U64 =
            new BigInteger("18424226625328513025");

    @Test
    public void testGetU8() {
        assertEquals(AM_NEQ, EXP_U8, getU8(UBYTES, 0));
    }

    @Test
    public void testGetU16() {
        assertEquals(AM_NEQ, EXP_U16, getU16(UBYTES, 1));
    }

    @Test
    public void testGetU32() {
        assertEquals(AM_NEQ, EXP_U32, getU32(UBYTES, 3));
    }

    @Test
    public void testGetU64() {
        assertEquals(AM_NEQ, EXP_U64, getU64(UBYTES, 0));
    }

    @Test
    public void testSetU8() {
        byte[] bytes = new byte[1];
        setU8(bytes, 0, EXP_U8);
        assertEquals(AM_NEQ, 0xff-B, bytes[0]);
    }

    @Test
    public void testSetU16() {
        byte[] bytes = new byte[2];
        setU16(bytes, 0, EXP_U16);
        assertEquals(AM_NEQ, 0xb0 - B, bytes[0]);
        assertEquals(AM_NEQ, 0x00, bytes[1]);
    }

    @Test
    public void testSetU32() {
        byte[] bytes = new byte[4];
        setU32(bytes, 0, EXP_U32);
        assertEquals(AM_NEQ, 0x80-B, bytes[0]);
        assertEquals(AM_NEQ, 0x00, bytes[1]);
        assertEquals(AM_NEQ, 0x00, bytes[2]);
        assertEquals(AM_NEQ, 0x00, bytes[3]);
    }

    @Test
    public void testSetU64() {
        byte[] bytes = new byte[8];
        setU64(bytes, 0, EXP_U64);
        assertArrayEquals(AM_NEQ, UBYTES, bytes);
    }

    @Test
    public void testHexStringWithPrefix() {
        String expected = "0xffb0008000000001";
        String actual = hexWithPrefix(UBYTES);
        assertEquals(AM_NEQ, expected, actual);
    }

    @Test
    public void testGetByteArray() {
        byte[] bytes = parseHexWithPrefix("0xffb0008000000001");
        assertArrayEquals(AM_NEQ, UBYTES, bytes);
    }

    @Test
    public void fastByte() {
        print(EOL + "fastByte()");
        for (int i = 0; i < 256; i++) {
            byte b = (byte) i;
            print("{}  {}", hexLookupLower(b), hexLookupUpper(b));
        }
        verifyFastByte((byte) 0x00, "00", "00");
        verifyFastByte((byte) 0xff, "ff", "FF");
        verifyFastByte((byte) 0xab, "ab", "AB");
        verifyFastByte((byte) 0x6e, "6e", "6E");
        verifyFastByte((byte) 0xdc, "dc", "DC");
        verifyFastByte((byte) 0x42, "42", "42");
    }

    private void verifyFastByte(byte b, String expLc, String expUc) {
        assertEquals(AM_NEQ, expLc, hexLookupLower(b));
        assertEquals(AM_NEQ, expUc, hexLookupUpper(b));
    }

}
