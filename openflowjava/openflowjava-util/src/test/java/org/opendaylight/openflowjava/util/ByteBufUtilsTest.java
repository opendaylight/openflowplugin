/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for ByteBufUtils.
 *
 * @author michal.polkorab
 */
public class ByteBufUtilsTest {

    private static final byte[] EXPECTED = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, (byte) 0xff};
    private static final byte[] EXPECTEDVALUES1AND255 = new byte[]{0x00, 0x01, 0x00, (byte) 0xff};

    /**
     * Test of {@link org.opendaylight.openflowjava.util.ByteBufUtils#hexStringToBytes(String)}.
     */
    @Test
    public void testHexStringToBytes() {
        byte[] data = ByteBufUtils.hexStringToBytes("01 02 03 04 05 ff");

        Assert.assertArrayEquals(EXPECTED, data);
    }

    /**
     * Test of {@link ByteBufUtils#hexStringToBytes(String, boolean)}.
     */
    @Test
    public void testHexStringToBytes2() {
        byte[] data = ByteBufUtils.hexStringToBytes("0102030405ff", false);

        Assert.assertArrayEquals(EXPECTED, data);
    }

    /**
     * Test of {@link ByteBufUtils#hexStringToByteBuf(String)}.
     */
    @Test
    public void testHexStringToByteBuf() {
        ByteBuf bb = ByteBufUtils.hexStringToByteBuf("01 02 03 04 05 ff");

        Assert.assertArrayEquals(EXPECTED, byteBufToByteArray(bb));
    }

    /**
     * Test of {@link ByteBufUtils#hexStringToByteBuf(String, ByteBuf)}.
     */
    @Test
    public void testHexStringToGivenByteBuf() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        ByteBufUtils.hexStringToByteBuf("01 02 03 04 05 ff", buffer);

        Assert.assertArrayEquals(EXPECTED, byteBufToByteArray(buffer));
    }

    private static byte[] byteBufToByteArray(ByteBuf bb) {
        byte[] result = new byte[bb.readableBytes()];
        bb.readBytes(result);
        return result;
    }

    /**
     * Test of {@link ByteBufUtils#fillBitMaskFromMap(java.util.Map)}.
     */
    @Test
    public void testFillBitmaskByEmptyMap() {
        Map<Integer, Boolean> emptyMap = new HashMap<>();
        String expectedBinaryString = "00000000000000000000000000000000";
        String bitmaskInBinaryString = toBinaryString(emptyMap, 32);

        Assert.assertEquals("Not null string", expectedBinaryString, bitmaskInBinaryString);
    }

    private static String toBinaryString(Map<Integer, Boolean> emptyMap, int length) {
        String binaryString = Integer.toBinaryString(ByteBufUtils.fillBitMaskFromMap(emptyMap));
        return String.format("%" + length + "s", binaryString).replaceAll(" ", "0");
    }

    /**
     * Test of {@link ByteBufUtils#fillBitMaskFromMap(java.util.Map)}.
     */
    @Test
    public void testFillBitmaskByFullMap() {
        Map<Integer, Boolean> fullMap = new HashMap<>();
        String expectedBinaryString = "11111111111111111111111111111111";
        String bitmaskValueInBinarySytring;
        for (Integer i = 0; i <= 31; i++) {
            fullMap.put(i, true);
        }
        bitmaskValueInBinarySytring = toBinaryString(fullMap, 32);
        Assert.assertEquals("Strings does not match", expectedBinaryString, bitmaskValueInBinarySytring);
    }

    /**
     * Test of {@link ByteBufUtils#fillBitMaskFromMap(java.util.Map)}.
     */
    @Test
    public void testFillBitmaskByZeroMap() {
        Map<Integer, Boolean> zeroMap = new HashMap<>();
        String expectedBinaryString = "00000000000000000000000000000000";
        String bitmaskValueInBinarySytring;
        for (Integer i = 0; i <= 31; i++) {
            zeroMap.put(i, false);
        }
        bitmaskValueInBinarySytring = toBinaryString(zeroMap, 32);
        Assert.assertEquals("Strings does not match", expectedBinaryString, bitmaskValueInBinarySytring);
    }

    /**
     * Test of {@link ByteBufUtils#fillBitMaskFromMap(java.util.Map)}.
     */
    @Test
    public void testFillBitmaskByRandomSet() {
        Map<Integer, Boolean> randomMap = new HashMap<>();
        String expectedBinaryString = "00000000000000000111100000000000";
        String bitmaskValueInBinarySytring;
        Boolean mapValue;
        for (Integer i = 0; i <= 31; i++) {
            mapValue = false;
            if (i >= 11 && i <= 14) {
                mapValue = true;
            }
            randomMap.put(i, mapValue);
        }
        bitmaskValueInBinarySytring = toBinaryString(randomMap, 32);
        Assert.assertEquals("Strings does not match", expectedBinaryString, bitmaskValueInBinarySytring);
    }

    /**
     * Test of {@link ByteBufUtils#fillBitMaskFromList(List)}.
     */
    @Test
    public void testFillBitmaskByEmptyList() {
        List<Boolean> emptyList = new ArrayList<>();
        emptyList.add(null);
        String expectedBinaryString = "00000000000000000000000000000000";
        String bitmaskInBinaryString = listToBinaryString(emptyList, 32);

        Assert.assertEquals("Not null string", expectedBinaryString, bitmaskInBinaryString);
    }

    private static String listToBinaryString(List<Boolean> emptyList, int length) {
        int[] bitMaskArray;
        bitMaskArray = ByteBufUtils.fillBitMaskFromList(emptyList);
        String binaryString = Integer.toBinaryString(bitMaskArray[0]);
        return String.format("%" + length + "s", binaryString).replaceAll(" ", "0");
    }

    /**
     * Test of {@link ByteBufUtils#fillBitMaskFromList(List)}.
     */
    @Test
    public void testFillBitmaskByFullList() {
        List<Boolean> fullList = new ArrayList<>();
        String expectedBinaryString = "11111111111111111111111111111111";
        String bitmaskValueInBinarySytring;
        for (Integer i = 0; i <= 31; i++) {
            fullList.add(true);
        }
        bitmaskValueInBinarySytring = listToBinaryString(fullList, 32);
        Assert.assertEquals("Strings does not match", expectedBinaryString, bitmaskValueInBinarySytring);
    }

    /**
     * Test of {@link ByteBufUtils#fillBitMaskFromList(List)}.
     */
    @Test
    public void testFillBitmaskByZeroList() {
        List<Boolean> zeroList = new ArrayList<>();
        String expectedBinaryString = "00000000000000000000000000000000";
        String bitmaskValueInBinarySytring;
        for (Integer i = 0; i <= 31; i++) {
            zeroList.add(false);
        }
        bitmaskValueInBinarySytring = listToBinaryString(zeroList, 32);
        Assert.assertEquals("Strings does not match", expectedBinaryString, bitmaskValueInBinarySytring);
    }

    /**
     * Test of {@link ByteBufUtils#fillBitMaskFromList(List)}.
     */
    @Test
    public void testFillBitmaskFromRandomList() {
        List<Boolean> randomList = new ArrayList<>();
        String expectedBinaryString = "00000000000000000111100000000000";
        String bitmaskValueInBinarySytring;
        Boolean listValue;
        for (Integer i = 0; i <= 31; i++) {
            listValue = false;
            if (i >= 11 && i <= 14) {
                listValue = true;
            }
            randomList.add(listValue);
        }
        bitmaskValueInBinarySytring = listToBinaryString(randomList, 32);
        Assert.assertEquals("Strings does not match", expectedBinaryString, bitmaskValueInBinarySytring);
    }

    /**
     * Test of {@link ByteBufUtils#decodeNullTerminatedString(ByteBuf, int)}.
     */
    @Test
    public void testDecodeString() {
        ByteBuf buf = ByteBufUtils.hexStringToByteBuf("4A 41 4D 45 53 20 42 4F 4E 44 00 00 00 00 00 00");
        Assert.assertEquals("Wrong string decoded", "JAMES BOND", ByteBufUtils.decodeNullTerminatedString(buf, 16));

        ByteBuf buf2 = ByteBufUtils.hexStringToByteBuf("53 50 49 44 45 52 4D 41 4E 00 00 00 00 00 00");
        Assert.assertEquals("Wrong string decoded", "SPIDERMAN", ByteBufUtils.decodeNullTerminatedString(buf2, 15));
    }

    /**
     * Test of {@link ByteBufUtils#byteBufToHexString(ByteBuf)}.
     */
    @Test
    public void testByteBufToHexString() {
        ByteBuf buf = ByteBufUtils.hexStringToByteBuf("00 01 02 03 04 05 06 07");
        buf.skipBytes(4);
        Assert.assertEquals("Wrong data read", "04 05 06 07", ByteBufUtils.byteBufToHexString(buf));
    }

    /**
     * Write OF header test.
     */
    @Test
    public void testWriteHeader() {
        HelloInputBuilder helloBuilder = new HelloInputBuilder();
        helloBuilder.setVersion(EncodeConstants.OF_VERSION_1_3);
        helloBuilder.setXid(Uint32.valueOf(12345));
        helloBuilder.setElements(null);
        HelloInput helloInput = helloBuilder.build();
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
        ByteBufUtils.writeOFHeader((byte) 0, helloInput, buf, EncodeConstants.OFHEADER_SIZE);
        Assert.assertEquals("Wrong version", EncodeConstants.OF13_VERSION_ID, buf.readUnsignedByte());
        Assert.assertEquals("Wrong type", 0, buf.readUnsignedByte());
        Assert.assertEquals("Wrong length", EncodeConstants.OFHEADER_SIZE, buf.readUnsignedShort());
        Assert.assertEquals("Wrong xid", 12345, buf.readUnsignedInt());
        Assert.assertTrue("Unexpected data", buf.readableBytes() == 0);
    }

    /**
     * Fill bitmask test.
     */
    @Test
    public void testFillBitmask() {
        Assert.assertEquals("Wrong bitmask", 0, ByteBufUtils.fillBitMask(0, false));
        Assert.assertEquals("Wrong bitmask", 1, ByteBufUtils.fillBitMask(0, true));
        Assert.assertEquals("Wrong bitmask", 3, ByteBufUtils.fillBitMask(0, true, true));
        Assert.assertEquals("Wrong bitmask", 2, ByteBufUtils.fillBitMask(0, false, true));
        Assert.assertEquals("Wrong bitmask", 1, ByteBufUtils.fillBitMask(0, true, false));
        Assert.assertEquals("Wrong bitmask", 2, ByteBufUtils.fillBitMask(1, true, false));
        Assert.assertEquals("Wrong bitmask", 4, ByteBufUtils.fillBitMask(1, false, true));
        Assert.assertEquals("Wrong bitmask", 6, ByteBufUtils.fillBitMask(1, true, true));
        Assert.assertEquals("Wrong bitmask", 0, ByteBufUtils.fillBitMask(1));
    }

    /**
     * Test bytes to hex string.
     */
    @Test
    public void testBytesToHexString() {
        byte[] array = new byte[]{10, 11, 12, 13, 14, 15, 16};
        Assert.assertEquals("Wrong conversion", "0a 0b 0c 0d 0e 0f 10", ByteBufUtils.bytesToHexString(array));
        byte[] empty = new byte[0];
        Assert.assertEquals("Wrong conversion", "", ByteBufUtils.bytesToHexString(empty));
    }

    /**
     * Test ipv4 address conversion.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadIpv4Address() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeByte(10);
        buffer.writeByte(20);
        buffer.writeByte(30);
        buffer.writeByte(40);
        String ipv4Address = ByteBufUtils.readIpv4Address(buffer);
        Assert.assertEquals("Wrong conversion", "10.20.30.40", ipv4Address);
        Assert.assertTrue("Unexpected data", buffer.readableBytes() == 0);

        ByteBuf buffer2 = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeByte(10);
        ipv4Address = ByteBufUtils.readIpv4Address(buffer2);
    }

    @Test
    public void testSerializeList() {

        List<Short> shorts = new ArrayList<>();
        shorts.add((short) 1);
        shorts.add((short) 255);

        final byte[] bytes = ByteBufUtils.serializeList(shorts);
        Assert.assertTrue(bytes.length == shorts.size() * 2);
        Assert.assertArrayEquals(EXPECTEDVALUES1AND255, bytes);
    }

    @Test
    public void testUpdateHeader() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeInt(1);
        final int start = buffer.writerIndex();
        buffer.writeShort(4);
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeLong(8);
        final int end = buffer.writerIndex();

        ByteBufUtils.updateOFHeaderLength(buffer, start);
        Assert.assertEquals(buffer.readInt(), 1);
        Assert.assertEquals(buffer.readShort(), 4);
        Assert.assertEquals(buffer.readShort(), 12);
        Assert.assertEquals(buffer.readLong(), 8L);
        Assert.assertEquals(buffer.getShort(start + EncodeConstants.OFHEADER_LENGTH_INDEX), end - start);
    }
}
