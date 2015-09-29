/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import java.math.BigInteger;
import java.util.Arrays;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

/**
 * @author mirehak
 */
public abstract class ByteUtil {

    /** default hex string separator */
    private static final String DEFAULT_HEX_SEPARATOR = ":";
    /** basic hex string encoding */
    private static final BaseEncoding PLAIN_HEX_16_ENCODING = BaseEncoding.base16().lowerCase();
    /** hex string encoding involving {@link #DEFAULT_HEX_SEPARATOR} as searator */
    private static final BaseEncoding HEX_16_ENCODING = PLAIN_HEX_16_ENCODING.withSeparator(DEFAULT_HEX_SEPARATOR, 2);

    /**
     * @param bytes bytes that needs to be converted to hex
     * @param delimiter string delimiter
     * @return hexString containing bytes, separated with delimiter
     */
    public static String bytesToHexstring(final byte[] bytes, final String delimiter) {
        BaseEncoding be = HEX_16_ENCODING;
        if (delimiter != DEFAULT_HEX_SEPARATOR) {
            be = PLAIN_HEX_16_ENCODING.withSeparator(delimiter, 2);
        }
        return be.encode(bytes);
    }

    /**
     * Utility method to convert BigInteger to n element byte array
     *
     * @param bigInteger big integer value that needs to be converted to byte
     * @param numBytes convert to number of bytes
     * @return byte array containing n * 8 bits.
     */
    public static byte[] convertBigIntegerToNBytes(final BigInteger bigInteger, final int numBytes) {
        if (bigInteger == null) {
            return null;
        }
        byte[] inputArray = bigInteger.toByteArray();
        byte[] outputArray = new byte[numBytes];
        if (bigInteger.compareTo(BigInteger.ZERO) < 0) {
            Arrays.fill(outputArray, (byte) -1);
        } else {
            Arrays.fill(outputArray, (byte) 0);
        }
        System.arraycopy(inputArray,
                Math.max(0, inputArray.length - outputArray.length),
                outputArray,
                Math.max(0, outputArray.length - inputArray.length),
                Math.min(outputArray.length, inputArray.length));
        return outputArray;
    }

    /**
     * Converts a 4 byte array of unsigned bytes to unsigned int
     *
     * @param bytes an array of 4 unsigned bytes
     * @return a long representing the unsigned int
     */
    public static final long bytesToUnsignedInt(final byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 4, "Input byte array must be exactly four bytes long.");
        long unsignedInt = 0;
        unsignedInt |= bytes[0] & 0xFF;
        unsignedInt <<= 8;
        unsignedInt |= bytes[1] & 0xFF;
        unsignedInt <<= 8;
        unsignedInt |= bytes[2] & 0xFF;
        unsignedInt <<= 8;
        unsignedInt |= bytes[3] & 0xFF;
        return unsignedInt;
    }

    /**
     * Converts a 3 byte array of unsigned bytes to unsigned int
     *
     * @param bytes an array of 4 unsigned bytes
     * @return a long representing the unsigned int
     */
    public static final long bytesToUnsignedMedium(final byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 3, "Input byte array must be exactly three bytes long.");
        long unsignedMedium = 0;
        unsignedMedium |= bytes[0] & 0xFF;
        unsignedMedium <<= 8;
        unsignedMedium |= bytes[1] & 0xFF;
        unsignedMedium <<= 8;
        unsignedMedium |= bytes[2] & 0xFF;
        return unsignedMedium;
    }

    /**
     * Converts a 2 byte array of unsigned bytes to unsigned short
     *
     * @param bytes an array of 2 unsigned bytes
     * @return an int representing the unsigned short
     */
    public static final int bytesToUnsignedShort(final byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 2, "Input byte array must be exactly two bytes long.");
        int unsignedShort = 0;
        unsignedShort |= bytes[0] & 0xFF;
        unsignedShort <<= 8;
        unsignedShort |= bytes[1] & 0xFF;
        return unsignedShort;
    }

    /**
     * Converts unsigned integer to a 4 byte array of unsigned bytes
     *
     * @param unsignedInt representing the unsigned integer
     * @return bytes an array of 4 unsigned bytes
     */
    public static byte[] unsignedIntToBytes(final Long unsignedInt) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (unsignedInt & 0xFF);
        bytes[2] = (byte) ((unsignedInt >> 8) & 0xFF);
        bytes[1] = (byte) ((unsignedInt >> 16) & 0xFF);
        bytes[0] = (byte) ((unsignedInt >> 24) & 0xFF);
        return bytes;
    }

    /**
     * Converts unsigned integer to a 3 byte array of unsigned bytes
     *
     * @param unsignedInt representing the unsigned integer
     * @return bytes an array of 3 unsigned bytes
     */
    public static byte[] unsignedMediumToBytes(final Long unsignedInt) {
        byte[] bytes = new byte[3];
        bytes[2] = (byte) (unsignedInt & 0xFF);
        bytes[1] = (byte) ((unsignedInt >> 8) & 0xFF);
        bytes[0] = (byte) ((unsignedInt >> 16) & 0xFF);
        return bytes;
    }

    /**
     * Converts unsigned short to a 2 byte array of unsigned bytes
     *
     * @param unsignedShort representing the unsigned short
     * @return bytes an array of 2 unsigned bytes
     */
    public static byte[] unsignedShortToBytes(final Integer unsignedShort) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (unsignedShort & 0xFF);
        bytes[0] = (byte) ((unsignedShort >> 8) & 0xFF);
        return bytes;
    }
}
