/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 *  Class for common operations on ByteBuf.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
public abstract class ByteBufUtils {
    public static final Splitter DOT_SPLITTER = Splitter.on('.');
    public static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Splitter HEXSTRING_SPLITTER =  Splitter.onPattern("\\s+").omitEmptyStrings();
    private static final Splitter HEXSTRING_NOSPACE_SPLITTER = Splitter.onPattern("(?<=\\G.{2})").omitEmptyStrings();

    private ByteBufUtils() {
        //not called
    }

    /**
     * Converts ByteBuf into String.
     *
     * @param bb input ByteBuf
     * @return String
     */
    public static String byteBufToHexString(final ByteBuf bb) {
        StringBuilder sb = new StringBuilder();
        for (int i = bb.readerIndex(); i < bb.readerIndex() + bb.readableBytes(); i++) {
            sb.append(String.format(" %02x", bb.getUnsignedByte(i)));
        }
        return sb.toString().trim();
    }

    /**
     * Converts String into byte[].
     *
     * @param hexSrc input String
     * @return byte[] filled with input data
     */
    public static byte[] hexStringToBytes(final String hexSrc) {
        return hexStringToBytes(hexSrc, true);
    }

    /**
     * Converts String into byte[].
     *
     * @param hexSrc input String
     * @param withSpaces if there are spaces in string
     * @return byte[] filled with input data
     */
    public static byte[] hexStringToBytes(final String hexSrc, final boolean withSpaces) {
        final Splitter splitter = withSpaces ? HEXSTRING_SPLITTER : HEXSTRING_NOSPACE_SPLITTER;
        List<String> byteChips = Lists.newArrayList(splitter.split(hexSrc));
        byte[] result = new byte[byteChips.size()];
        int index = 0;
        for (String chip : byteChips) {
            result[index] = (byte) Short.parseShort(chip, 16);
            index++;
        }
        return result;
    }

    /**
     * Creates ByteBuf filled with specified data.
     *
     * @param hexSrc input String of bytes in hex format
     * @return ByteBuf with specified hexString converted
     */
    public static ByteBuf hexStringToByteBuf(final String hexSrc) {
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        hexStringToByteBuf(hexSrc, out);
        return out;
    }

    /**
     * Creates ByteBuf filled with specified data.
     *
     * @param hexSrc input String of bytes in hex format
     * @param out ByteBuf with specified hexString converted
     */
    public static void hexStringToByteBuf(final String hexSrc, final ByteBuf out) {
        out.writeBytes(hexStringToBytes(hexSrc));
    }

    /**
     * Create standard OF header.
     *
     * @param msgType message code
     * @param message POJO
     * @param out writing buffer
     * @param length ofheader length
     */
    public static <E extends OfHeader> void writeOFHeader(final byte msgType, final E message, final ByteBuf out,
            final int length) {
        out.writeByte(message.getVersion().toJava());
        out.writeByte(msgType);
        out.writeShort(length);
        out.writeInt(message.getXid().intValue());
    }

    /**
     * Write length standard OF header.
     *
     * @param out writing buffer
     */
    public static void updateOFHeaderLength(final ByteBuf out) {
        out.setShort(EncodeConstants.OFHEADER_LENGTH_INDEX, out.readableBytes());
    }

    /**
     * Write length OF header.
     *
     * @param out writing buffer
     * @param index writing index
     */
    public static void updateOFHeaderLength(final ByteBuf out, int index) {
        out.setShort(index + EncodeConstants.OFHEADER_LENGTH_INDEX, out.writerIndex() - index);
    }

    /**
     * Fills the bitmask from boolean map where key is bit position.
     *
     * @param booleanMap bit to boolean mapping
     * @return bit mask
     */
    public static int fillBitMaskFromMap(final Map<Integer, Boolean> booleanMap) {
        int bitmask = 0;

        for (Entry<Integer, Boolean> iterator : booleanMap.entrySet()) {
            if (iterator.getValue() != null && iterator.getValue().booleanValue()) {
                bitmask |= 1 << iterator.getKey();
            }
        }
        return bitmask;
    }

    /**
     * Fills the bitmask from a set of bit values, starting at specified offset.
     *
     * @param offset Bit offset to start at
     * @param values boolean bit values to fill
     * @return Filled-in bitmask
     */
    public static int fillBitMask(final int offset, final boolean... values) {
        int bitmask = 0;

        int index = offset;
        for (boolean v : values) {
            if (v) {
                bitmask |= 1 << index;
            }
            ++index;
        }

        return bitmask;
    }

    /**
     * Fills the bitmask from boolean list where key is bit position.
     *
     * @param booleanList bit to boolean mapping
     * @return bit mask
     */
    public static int[] fillBitMaskFromList(final List<Boolean> booleanList) {
        int[] bitmask;
        int index = 0;
        int arrayIndex = 0;
        if (booleanList.size() % Integer.SIZE != 0) {
            bitmask = new int[booleanList.size() / Integer.SIZE + 1];
        } else {
            bitmask = new int[booleanList.size() / Integer.SIZE];
        }
        for (Boolean currElement : booleanList) {
            if (currElement != null && currElement.booleanValue()) {
                bitmask[arrayIndex] |= 1 << index;
            }
            index++;
            arrayIndex = index / Integer.SIZE;
        }
        return bitmask;
    }

    /**
     * Converts byte array into String.
     *
     * @param array input byte array
     * @return String
     */
    public static String bytesToHexString(final byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte element : array) {
            sb.append(String.format(" %02x", element));
        }
        return sb.toString().trim();
    }

    /**
     * Reads and parses null-terminated string from ByteBuf.
     *
     * @param rawMessage the message to parse
     * @param length maximal length of String
     * @return String with name of port
     */
    public static String decodeNullTerminatedString(final ByteBuf rawMessage, final int length) {
        byte[] name = new byte[length];
        rawMessage.readBytes(name);
        return new String(name, StandardCharsets.UTF_8).trim();
    }

    /**
     * Read an IPv4 address from a buffer and format it into dotted-quad string.
     *
     * @param buf Input buffer
     * @return Dotted-quad string
     */
    public static String readIpv4Address(final ByteBuf buf) {
        final StringBuilder sb = new StringBuilder(EncodeConstants.GROUPS_IN_IPV4_ADDRESS * 4 - 1);

        sb.append(buf.readUnsignedByte());
        for (int i = 1; i < EncodeConstants.GROUPS_IN_IPV4_ADDRESS; i++) {
            sb.append('.');
            sb.append(buf.readUnsignedByte());
        }

        return sb.toString();
    }

    public static Ipv4Address readIetfIpv4Address(final ByteBuf buf) {
        final byte[] tmp = new byte[4];
        buf.readBytes(tmp);
        return IetfInetUtil.INSTANCE.ipv4AddressFor(tmp);
    }

    public static Ipv6Address readIetfIpv6Address(final ByteBuf buf) {
        final byte[] tmp = new byte[16];
        buf.readBytes(tmp);
        return IetfInetUtil.INSTANCE.ipv6AddressFor(tmp);
    }

    public static MacAddress readIetfMacAddress(final ByteBuf buf) {
        final byte[] tmp = new byte[EncodeConstants.MAC_ADDRESS_LENGTH];
        buf.readBytes(tmp);
        return IetfYangUtil.INSTANCE.macAddressFor(tmp);
    }

    public static byte[] serializeList(final List<Short> list) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(list.size() * 2);
        for (Short shortValue : list) {
            byteBuffer.putShort(shortValue);
        }
        return byteBuffer.array();
    }
}
