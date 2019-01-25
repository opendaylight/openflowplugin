/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.libraries.liblldp;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class BitBufferHelperTest {
    @Test
    public void testToNumberNominal() {
        byte[] array = new byte[] { -1, -1, -1, -1 };
        long expected = 0;
        for (int i = 0; i < 32; i++) {
            Assert.assertEquals(expected, BitBufferHelper.toNumber(array, i));
            expected += 1 << i;
        }
    }

    @Test
    public void testToNumberWithSmallArray() {
        byte[] array = new byte[] { -1 };
        long expected = 0;
        for (int i = 0; i < 32; i++) {
            Assert.assertEquals(expected, BitBufferHelper.toNumber(array, i));
            if (i < 8) {
                expected += 1 << i;
            }
        }
    }

    @Test
    public void testGetByte() {
        byte[] data = { 100 };
        Assert.assertEquals(100, BitBufferHelper.getByte(data));
    }

    @Test
    public void testGetBits() throws Exception {
        byte[] data = { 10, 12, 14, 20, 55, 69, 82, 97, 109, 117, 127, -50 };
        byte[] bits;

        bits = BitBufferHelper.getBits(data, 88, 8); //BYTE extraOffsetBits = extranumBits = 0
        Assert.assertEquals(bits[0], -50);

        bits = BitBufferHelper.getBits(data, 8, 16); //Short
        Assert.assertEquals(12, bits[0]);
        Assert.assertEquals(14, bits[1]);

        bits = BitBufferHelper.getBits(data, 32, 32); //Int
        Assert.assertEquals(55, bits[0]);
        Assert.assertEquals(69, bits[1]);
        Assert.assertEquals(82, bits[2]);
        Assert.assertEquals(97, bits[3]);

        bits = BitBufferHelper.getBits(data, 16, 48); //Long
        Assert.assertEquals(14, bits[0]);
        Assert.assertEquals(20, bits[1]);
        Assert.assertEquals(55, bits[2]);
        Assert.assertEquals(69, bits[3]);
        Assert.assertEquals(82, bits[4]);
        Assert.assertEquals(97, bits[5]);

        bits = BitBufferHelper.getBits(data, 40, 7); //BYTE extraOffsetBits = extranumBits != 0
        Assert.assertEquals(34, bits[0]);

        bits = BitBufferHelper.getBits(data, 8, 13); //Short
        Assert.assertEquals(1, bits[0]);
        Assert.assertEquals(bits[1], -127);

        bits = BitBufferHelper.getBits(data, 32, 28); //Int
        Assert.assertEquals(3, bits[0]);
        Assert.assertEquals(116, bits[1]);
        Assert.assertEquals(85, bits[2]);
        Assert.assertEquals(38, bits[3]);

        bits = BitBufferHelper.getBits(data, 16, 41); //Long
        Assert.assertEquals(0, bits[0]);
        Assert.assertEquals(28, bits[1]);
        Assert.assertEquals(40, bits[2]);
        Assert.assertEquals(110, bits[3]);
        Assert.assertEquals(bits[4], -118);
        Assert.assertEquals(bits[5], -92);

        bits = BitBufferHelper.getBits(data, 3, 7); //BYTE extraOffsetBits != 0; extranumBits == 0
        Assert.assertEquals(40, bits[0]);

        bits = BitBufferHelper.getBits(data, 13, 16); //Short
        Assert.assertEquals(bits[0], -127);
        Assert.assertEquals(bits[1], -62);

        bits = BitBufferHelper.getBits(data, 5, 32); //Int
        Assert.assertEquals(65, bits[0]);
        Assert.assertEquals(bits[1], -127);
        Assert.assertEquals(bits[2], -62);
        Assert.assertEquals(bits[3], -122);

        bits = BitBufferHelper.getBits(data, 23, 48); //Long
        Assert.assertEquals(10, bits[0]);
        Assert.assertEquals(27, bits[1]);
        Assert.assertEquals(bits[2], -94);
        Assert.assertEquals(bits[3], -87);
        Assert.assertEquals(48, bits[4]);
        Assert.assertEquals(bits[5], -74);

        bits = BitBufferHelper.getBits(data, 66, 9); //BYTE extraOffsetBits != 0; extranumBits != 0
        Assert.assertEquals(1, bits[0]);
        Assert.assertEquals(107, bits[1]);

        bits = BitBufferHelper.getBits(data, 13, 15); //Short
        Assert.assertEquals(64, bits[0]);
        Assert.assertEquals(bits[1], -31);

        bits = BitBufferHelper.getBits(data, 5, 29); //Int
        Assert.assertEquals(8, bits[0]);
        Assert.assertEquals(48, bits[1]);
        Assert.assertEquals(56, bits[2]);
        Assert.assertEquals(80, bits[3]);

        bits = BitBufferHelper.getBits(data, 31, 43); //Long
        Assert.assertEquals(0, bits[0]);
        Assert.assertEquals(bits[1], -35);
        Assert.assertEquals(21, bits[2]);
        Assert.assertEquals(73, bits[3]);
        Assert.assertEquals(bits[4], -123);
        Assert.assertEquals(bits[5], -75);

        bits = BitBufferHelper.getBits(data, 4, 12); //Short
        Assert.assertEquals(10, bits[0]);
        Assert.assertEquals(12, bits[1]);

        byte[] data1 = { 0, 8 };
        bits = BitBufferHelper.getBits(data1, 7, 9); //Short
        Assert.assertEquals(0, bits[0]);
        Assert.assertEquals(8, bits[1]);

        byte[] data2 = { 2, 8 };
        bits = BitBufferHelper.getBits(data2, 0, 7); //Short
        Assert.assertEquals(1, bits[0]);

        bits = BitBufferHelper.getBits(data2, 7, 9); //Short
        Assert.assertEquals(0, bits[0]);
        Assert.assertEquals(8, bits[1]);
    }

    // [01101100][01100000]
    //     [01100011]
    @Test
    public void testGetBytes() throws Exception {
        byte[] data = { 108, 96, 125, -112, 5, 6, 108, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22 };

        Assert.assertEquals(108, BitBufferHelper.getBits(data, 0, 8)[0]);
        Assert.assertEquals(96, BitBufferHelper.getBits(data, 8, 8)[0]);

        byte[]  bits = BitBufferHelper.getBits(data, 0, 10);
        Assert.assertEquals(1, bits[0]);
        Assert.assertEquals(bits[1], -79);

        bits = BitBufferHelper.getBits(data, 3, 8);
        Assert.assertEquals(99, bits[0]);
        //Assert.assertTrue(x[1] == 97);

    }

    @Test
    public void testMSBMask() {
        int numBits = 1; //MSB
        int mask = BitBufferHelper.getMSBMask(numBits);
        Assert.assertEquals(128, mask);

        numBits = 8;
        mask = BitBufferHelper.getMSBMask(numBits);
        Assert.assertEquals(255, mask);

        numBits = 2;
        mask = BitBufferHelper.getMSBMask(numBits);
        Assert.assertEquals(192, mask);
    }

    @Test
    public void testLSBMask() {
        int numBits = 1; //LSB
        int mask = BitBufferHelper.getLSBMask(numBits);
        Assert.assertEquals(1, mask);

        numBits = 3;
        mask = BitBufferHelper.getLSBMask(numBits);
        Assert.assertEquals(7, mask);

        numBits = 8;
        mask = BitBufferHelper.getLSBMask(numBits);
        Assert.assertEquals(255, mask);
    }

    @Test
    public void testToByteArray() {
        short sh = Short.MAX_VALUE;
        byte[] dataShort = BitBufferHelper.toByteArray(sh);
        Assert.assertEquals(127, dataShort[0]);
        Assert.assertEquals(dataShort[1], -1);

        short sh2 = Short.MIN_VALUE;
        byte[] dataShort2 = BitBufferHelper.toByteArray(sh2);
        Assert.assertEquals(dataShort2[0], -128);
        Assert.assertEquals(0, dataShort2[1]);

        short sh3 = 16384;
        byte[] dataShort3 = BitBufferHelper.toByteArray(sh3);
        Assert.assertEquals(64, dataShort3[0]);
        Assert.assertEquals(0, dataShort3[1]);

        short sh4 = 146; //TCP headerlenflags - startoffset = 103
        byte[] dataShort4 = BitBufferHelper.toByteArray(sh4);
        Assert.assertEquals(0, dataShort4[0]);
        Assert.assertEquals(dataShort4[1], -110);

        short sh5 = 5000; //IPv4 Offset - startOffset = 51 (to 63)
        byte[] dataShort5 = BitBufferHelper.toByteArray(sh5);
        Assert.assertEquals(19, dataShort5[0]);
        Assert.assertEquals(dataShort5[1], -120);

        short sh6 = 5312; //numEndRestBits < numBitstoShiftBy
        byte[] dataShort6 = BitBufferHelper.toByteArray(sh6);
        Assert.assertEquals(20, dataShort6[0]);
        Assert.assertEquals(dataShort6[1], -64);

        int int1 = Integer.MAX_VALUE;
        byte[] dataInt1 = BitBufferHelper.toByteArray(int1);
        Assert.assertEquals(127, dataInt1[0]);
        Assert.assertEquals(dataInt1[1], -1);
        Assert.assertEquals(dataInt1[2], -1);
        Assert.assertEquals(dataInt1[3], -1);

        int int2 = Integer.MIN_VALUE;
        byte[] dataInt2 = BitBufferHelper.toByteArray(int2);
        Assert.assertEquals(dataInt2[0], -128);
        Assert.assertEquals(0, dataInt2[1]);
        Assert.assertEquals(0, dataInt2[2]);
        Assert.assertEquals(0, dataInt2[3]);

        int int3 = 1077952576;
        byte[] dataInt3 = BitBufferHelper.toByteArray(int3);
        Assert.assertEquals(64, dataInt3[0]);
        Assert.assertEquals(64, dataInt3[1]);
        Assert.assertEquals(64, dataInt3[2]);
        Assert.assertEquals(64, dataInt3[3]);

        long long1 = Long.MAX_VALUE;
        byte[] dataLong1 = BitBufferHelper.toByteArray(long1);
        Assert.assertEquals(127, dataLong1[0]);
        Assert.assertEquals(dataLong1[1], -1);
        Assert.assertEquals(dataLong1[2], -1);
        Assert.assertEquals(dataLong1[3], -1);
        Assert.assertEquals(dataLong1[4], -1);
        Assert.assertEquals(dataLong1[5], -1);
        Assert.assertEquals(dataLong1[6], -1);
        Assert.assertEquals(dataLong1[7], -1);

        long long2 = Long.MIN_VALUE;
        byte[] dataLong2 = BitBufferHelper.toByteArray(long2);
        Assert.assertEquals(dataLong2[0], -128);
        Assert.assertEquals(0, dataLong2[1]);
        Assert.assertEquals(0, dataLong2[2]);
        Assert.assertEquals(0, dataLong2[3]);
        Assert.assertEquals(0, dataLong2[4]);
        Assert.assertEquals(0, dataLong2[5]);
        Assert.assertEquals(0, dataLong2[6]);
        Assert.assertEquals(0, dataLong2[7]);

        byte byte1 = Byte.MAX_VALUE;
        byte[] dataByte1 = BitBufferHelper.toByteArray(byte1);
        Assert.assertEquals(127, dataByte1[0]);

        byte byte2 = Byte.MIN_VALUE;
        byte[] dataByte2 = BitBufferHelper.toByteArray(byte2);
        Assert.assertEquals(dataByte2[0], -128);

        byte byte3 = 64;
        byte[] dataByte3 = BitBufferHelper.toByteArray(byte3);
        Assert.assertEquals(64, dataByte3[0]);

        byte byte4 = 32;
        byte[] dataByte4 = BitBufferHelper.toByteArray(byte4);
        Assert.assertEquals(32, dataByte4[0]);

    }

    @Test
    public void testToByteArrayVariable() {
        int len = 9;
        byte[] dataShort;
        dataShort = BitBufferHelper.toByteArray(511, len);
        Assert.assertEquals(dataShort[0], (byte) 255);
        Assert.assertEquals(dataShort[1], (byte) 128);

        dataShort = BitBufferHelper.toByteArray(511, len);
        Assert.assertEquals(dataShort[0], (byte) 255);
        Assert.assertEquals(dataShort[1], (byte) 128);

        dataShort = BitBufferHelper.toByteArray((long) 511, len);
        Assert.assertEquals(dataShort[0], (byte) 255);
        Assert.assertEquals(dataShort[1], (byte) 128);
    }

    @Test
    public void testToInt() {
        byte[] data = { 1 };
        Assert.assertEquals(1, BitBufferHelper.toNumber(data));

        byte[] data2 = { 1, 1 };
        Assert.assertEquals(257, BitBufferHelper.toNumber(data2));

        byte[] data3 = { 1, 1, 1 };
        Assert.assertEquals(65793, BitBufferHelper.toNumber(data3));
    }

    @Test
    public void testToLongGetter() {
        byte[] data = { 1, 1 };
        Assert.assertEquals(257L, BitBufferHelper.getLong(data));
    }

    @Test
    public void testSetByte() throws Exception {
        byte input;
        byte[] data = new byte[20];

        input = 125;
        BitBufferHelper.setByte(data, input, 0, Byte.SIZE);
        Assert.assertEquals(125, data[0]);

        input = 109;
        BitBufferHelper.setByte(data, input, 152, Byte.SIZE);
        Assert.assertEquals(109, data[19]);
    }

    @Test
    public void testSetBytes() throws Exception {
        byte[] input = { 0, 1 };
        byte[] data = { 6, 0 };

        BitBufferHelper.setBytes(data, input, 7, 9);
        Assert.assertEquals(6, data[0]);
        Assert.assertEquals(1, data[1]);
    }

    @Test
    @Ignore("Currently broken")
    //INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
    // [01001011] [01101110] [01101011] [10100000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]*/
    public void testInsertBits() {
        //CASE 1: startOffset%8 == 0 && numBits%8 == 0
        byte[] inputdata = { 75, 110, 107, 80, 10, 12, 35, 100, 125, 65 };
        int startOffset;
        int numBits;

        byte[] data1 = new byte[2];
        startOffset = 0;
        numBits = 16;
        BitBufferHelper.insertBits(data1, inputdata, startOffset, numBits);
        Assert.assertEquals(75, data1[0]);
        Assert.assertEquals(110, data1[1]);

        byte[] data2 = new byte[4];
        startOffset = 0;
        numBits = 32;
        BitBufferHelper.insertBits(data2, inputdata, startOffset, numBits);
        Assert.assertEquals(75, data2[0]);
        Assert.assertEquals(110, data2[1]);
        Assert.assertEquals(107, data2[2]);
        Assert.assertEquals(80, data2[3]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [10100000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [01001011] [01101000] = {75, 104}
        byte[] data10 = new byte[2];
        startOffset = 0;
        numBits = 13;
        BitBufferHelper.insertBits(data10, inputdata, startOffset, numBits);
        Assert.assertEquals(75, data10[0]);
        Assert.assertEquals(104, data10[1]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [10100000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [01001000] = {72}
        byte[] data11 = new byte[4];
        startOffset = 8;
        numBits = 6;
        BitBufferHelper.insertBits(data11, inputdata, startOffset, numBits);
        Assert.assertEquals(72, data11[1]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [10100000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [01001011] [01101110] [01101000] = {75, 110, 105}
        byte[] data12 = new byte[4];
        startOffset = 0;
        numBits = 23;
        BitBufferHelper.insertBits(data12, inputdata, startOffset, numBits);
        Assert.assertEquals(75, data12[0]);
        Assert.assertEquals(110, data12[1]);
        Assert.assertEquals(106, data12[2]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [10100000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [01001011] [01101110] [01100000] = {75, 110, 96}
        byte[] data13 = new byte[4];
        startOffset = 8;
        numBits = 20;
        BitBufferHelper.insertBits(data13, inputdata, startOffset, numBits);
        Assert.assertEquals(75, data13[1]);
        Assert.assertEquals(110, data13[2]);
        Assert.assertEquals(96, data13[3]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [10100000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [01001011] [01101110] [01101011] [10100000]= {75, 110, 107, 80}
        byte[] data14 = new byte[4];
        startOffset = 0;
        numBits = 30;
        BitBufferHelper.insertBits(data14, inputdata, startOffset, numBits);
        Assert.assertEquals(75, data14[0]);
        Assert.assertEquals(110, data14[1]);
        Assert.assertEquals(107, data14[2]);
        Assert.assertEquals(80, data14[3]);

        //CASE 3: startOffset%8 != 0, numBits%8 = 0
        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [10100000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00001001] [11000000] = {72, 96}
        byte[] data16 = new byte[5];
        startOffset = 3;
        numBits = 8;
        BitBufferHelper.insertBits(data16, inputdata, startOffset, numBits);
        Assert.assertEquals(9, data16[0]);
        Assert.assertEquals(96, data16[1]);
        Assert.assertEquals(0, data16[2]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: [00000100] [1011 0110] [1110 0000] = {4, -54, -96}

        startOffset = 3;
        numBits = 16;
        byte[] data17 = new byte[5];
        BitBufferHelper.insertBits(data17, inputdata, startOffset, numBits);
        Assert.assertEquals(9, data17[0]);
        Assert.assertEquals(109, data17[1]);
        Assert.assertEquals(data17[2], -64);
        Assert.assertEquals(0, data17[3]);

        // INPUT: {79, 110, 111}
        // = [01001111] [01101110] [01101111]
        //OUTPUT: [0000 1001] [1110 1101] [110 00000] = {9, -19, -64}
        byte[] data18 = new byte[5];
        byte[] inputdata3 = { 79, 110, 111 };
        startOffset = 3;
        numBits = 16;
        BitBufferHelper.insertBits(data18, inputdata3, startOffset, numBits);
        Assert.assertEquals(9, data18[0]);
        Assert.assertEquals(data18[1], -19);
        Assert.assertEquals(data18[2], -64);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: [0000 1001] [0110 1101] [1100 1101] [0110 1010] [0000 0001] = {9, 109, -51, 106, 0}

        startOffset = 3;
        numBits = 32;
        byte[] data19 = new byte[5];
        BitBufferHelper.insertBits(data19, inputdata, startOffset, numBits);
        Assert.assertEquals(9, data19[0]);
        Assert.assertEquals(109, data19[1]);
        Assert.assertEquals(data19[2], -51);
        Assert.assertEquals(106, data19[3]);
        Assert.assertEquals(0, data19[4]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: data[4, 5, 6] = [0 010 0101] [1 011 0111] [0 000 0000] = {37, -73, 0}
        startOffset = 33;
        numBits = 16;
        byte[] data20 = new byte[7];
        BitBufferHelper.insertBits(data20, inputdata, startOffset, numBits);
        Assert.assertEquals(37, data20[4]);
        Assert.assertEquals(data20[5], -73);
        Assert.assertEquals(0, data20[6]);

        //CASE 4: extranumBits != 0 AND extraOffsetBits != 0
        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: [0000 1001] [0100 0000]  = {9, 96}
        startOffset = 3;
        numBits = 7;
        byte[] data21 = new byte[7];
        BitBufferHelper.insertBits(data21, inputdata, startOffset, numBits);
        Assert.assertEquals(9, data21[0]);
        Assert.assertEquals(64, data21[1]);
        Assert.assertEquals(0, data21[2]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: data = [00000 010] [01011 011] [01110 000] = {37, -73, 0}
        startOffset = 5;
        numBits = 17;
        byte[] data22 = new byte[7];
        BitBufferHelper.insertBits(data22, inputdata, startOffset, numBits);
        Assert.assertEquals(2, data22[0]);
        Assert.assertEquals(91, data22[1]);
        Assert.assertEquals(112, data22[2]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: [0000 1001] [0110 1101] [110 01101] [01 00000] = {9, 109, -51, 64}
        startOffset = 3;
        numBits = 23;
        byte[] data23 = new byte[7];
        BitBufferHelper.insertBits(data23, inputdata, startOffset, numBits);
        Assert.assertEquals(9, data23[0]);
        Assert.assertEquals(109, data23[1]);
        Assert.assertEquals(data23[2], -51);
        Assert.assertEquals(64, data23[3]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: [0000 1001] [0110 1101]  = {9, 109}
        startOffset = 3;
        numBits = 13;
        byte[] data24 = new byte[7];
        BitBufferHelper.insertBits(data24, inputdata, startOffset, numBits);
        Assert.assertEquals(9, data24[0]);
        Assert.assertEquals(109, data24[1]);
        Assert.assertEquals(0, data24[2]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: [0000 0100] [1011 0110] [1110 0110]  = {4, -74, -26}
        startOffset = 4;
        numBits = 20;
        byte[] data25 = new byte[7];
        BitBufferHelper.insertBits(data25, inputdata, startOffset, numBits);
        Assert.assertEquals(4, data25[0]);
        Assert.assertEquals(data25[1], -74);
        Assert.assertEquals(data25[2], -26);
        Assert.assertEquals(data25[3], -0);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: [0000 0010] [0101 1011]   = {0, 2, 91, 0}
        startOffset = 13;
        numBits = 11;
        byte[] data26 = new byte[7];
        BitBufferHelper.insertBits(data26, inputdata, startOffset, numBits);
        Assert.assertEquals(0, data26[0]);
        Assert.assertEquals(2, data26[1]);
        Assert.assertEquals(91, data26[2]);
        Assert.assertEquals(0, data26[3]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: [000 01001] [011 01101] [110 0 0000]   = {9, 109, -64, 0}
        startOffset = 3;
        numBits = 17;
        byte[] data27 = new byte[7];
        BitBufferHelper.insertBits(data27, inputdata, startOffset, numBits);
        Assert.assertEquals(9, data27[0]);
        Assert.assertEquals(109, data27[1]);
        Assert.assertEquals(data27[2], -64);
        Assert.assertEquals(0, data27[3]);

        // INPUT: {75, 110, 107, 80, 10, 12, 35, 100, 125, 65} =
        // [01001011] [01101110] [01101011] [01010000] [00001010] [00001100] [00100011] [01100100] [11111101] [01000001]
        // OUTPUT: [00000000] [00000100] [10110110] [11100000]= {0, 4, -54, -96}
        // OUTPUT: [00 000000] [00 000000] [00 010010] [11 011011] [10 011010] [11 010100] [0000 0000] =
        //    {0, 0, 18, -37,-102,-44,0}
        startOffset = 18;
        numBits = 34;
        byte[] data28 = new byte[7];
        BitBufferHelper.insertBits(data28, inputdata, startOffset, numBits);
        Assert.assertEquals(0, data28[0]);
        Assert.assertEquals(0, data28[1]);
        Assert.assertEquals(18, data28[2]);
        Assert.assertEquals(data28[3], -37);
        Assert.assertEquals(data28[4], -102);
        Assert.assertEquals(data28[5], -44);
        Assert.assertEquals(0, data28[6]);

    }

    @Test
    public void testGetShort() {
        byte[] data = new byte[2];
        data[0] = 7;
        data[1] = 8;
        int length = 9; // num bits
        Assert.assertEquals(264, BitBufferHelper.getShort(data, length));

        data[0] = 6;
        data[1] = 8;
        short result = BitBufferHelper.getShort(data, length);
        Assert.assertEquals(8, result);

        data[0] = 8;
        data[1] = 47;
        result = BitBufferHelper.getShort(data, length);
        Assert.assertEquals(47, result);

        //[0000 0001] [0001 0100] [0110 0100]
        byte[] data1 = new byte[2];
        data1[0] = 1;
        data1[1] = 20; //data1[2] = 100;
        length = 15;
        result = BitBufferHelper.getShort(data1, length);
        Assert.assertEquals(276, result);

        byte[] data2 = new byte[2];
        data2[0] = 64;
        data2[1] = 99; //data2[2] = 100;
        length = 13;
        result = BitBufferHelper.getShort(data2, length);
        Assert.assertEquals(99, result);

        byte[] data3 = { 100, 50 };
        result = BitBufferHelper.getShort(data3);
        Assert.assertEquals(25650, result);
    }

    @Test
    public void testToIntVarLength() {
        byte[] data = { (byte) 255, (byte) 128 };
        int length = 9; // num bits
        Assert.assertEquals(384, BitBufferHelper.getInt(data, length));

        byte[] data2 = { 0, 8 };
        Assert.assertEquals(8, BitBufferHelper.getInt(data2, 9));

        byte[] data3 = { 1, 1, 1 };
        Assert.assertEquals(65793, BitBufferHelper.getInt(data3));

        byte[] data4 = { 1, 1, 1 };
        Assert.assertEquals(65793, BitBufferHelper.getInt(data4));

        byte[] data5 = { 1, 1 };
        Assert.assertEquals(257, BitBufferHelper.getInt(data5));

    }

    @Test
    public void testShiftBitstoLSB() {
        byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        byte[] data2 = { 8, 9, 10 };
        byte[] shiftedBytes2 = BitBufferHelper.shiftBitsToLSB(data2, 11);

        Assert.assertEquals(0, shiftedBytes2[0]);
        Assert.assertEquals(64, shiftedBytes2[1]);
        Assert.assertEquals(72, shiftedBytes2[2]);

        byte[] shiftedBytes = BitBufferHelper.shiftBitsToLSB(data, 49);

        Assert.assertEquals(0, shiftedBytes[0]);
        Assert.assertEquals(2, shiftedBytes[1]);
        Assert.assertEquals(4, shiftedBytes[2]);
        Assert.assertEquals(6, shiftedBytes[3]);
        Assert.assertEquals(8, shiftedBytes[4]);
        Assert.assertEquals(10, shiftedBytes[5]);
        Assert.assertEquals(12, shiftedBytes[6]);
        Assert.assertEquals(14, shiftedBytes[7]);
        Assert.assertEquals(16, shiftedBytes[8]);
        Assert.assertEquals(18, shiftedBytes[9]);

        byte[] data1 = { 1, 2, 3 };
        byte[] shiftedBytes1 = BitBufferHelper.shiftBitsToLSB(data1, 18);
        Assert.assertEquals(0, shiftedBytes1[0]);
        Assert.assertEquals(4, shiftedBytes1[1]);
        Assert.assertEquals(8, shiftedBytes1[2]);

    }

    @Test
    public void testShiftBitsToLSBAndMSB() {
        byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };

        byte[] clone = BitBufferHelper.shiftBitsToMSB(BitBufferHelper
                .shiftBitsToLSB(data, 72), 72);

        Assert.assertEquals(1, clone[0]);
        Assert.assertEquals(2, clone[1]);
        Assert.assertEquals(3, clone[2]);
        Assert.assertEquals(4, clone[3]);
        Assert.assertEquals(5, clone[4]);
        Assert.assertEquals(6, clone[5]);
        Assert.assertEquals(7, clone[6]);
        Assert.assertEquals(8, clone[7]);
        Assert.assertEquals(9, clone[8]);
        Assert.assertEquals(0, clone[9]);
    }

}
