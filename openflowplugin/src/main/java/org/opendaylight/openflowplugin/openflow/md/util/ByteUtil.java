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

/**
 * @author mirehak
 *
 */
public abstract class ByteUtil {

    /**
     * @param bytes
     * @param delimiter
     * @return hexString containing bytes, separated with delimiter
     */
    public static String bytesToHexstring(final byte[] bytes, final String delimiter) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            sb.append(String.format("%02x%s", b, delimiter));
        }
        return sb.toString();
    }

    /**
     * Utility method to convert BigInteger to n element byte array
     * @param bigInteger
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
     * @param bytes an array of 4 unsigned bytes
     * @return a long representing the unsigned int
     */
    public static final long bytesToUnsignedInt(final byte[] bytes)
    {
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
     * Converts a 2 byte array of unsigned bytes to unsigned short
     * @param bytes an array of 2 unsigned bytes
     * @return an int representing the unsigned short
     */
    public static final int bytesToUnsignedShort(final byte[] bytes)
    {
      int unsignedShort = 0;
      unsignedShort |= bytes[0] & 0xFF;
      unsignedShort <<= 8;
      unsignedShort |= bytes[1] & 0xFF;
      return unsignedShort;
    }

    /**
     * Converts unsigned integer to a 4 byte array of unsigned bytes
     * @param unsignedInt representing the unsigned integer
     * @return bytes an array of 4 unsigned bytes
     */
    public static byte[] unsignedIntToBytes(final Long unsignedInt)
    {
      byte[] bytes = new byte[4];
      bytes[3] = (byte) (unsignedInt & 0xFF);
      bytes[2] = (byte) ((unsignedInt >> 8) & 0xFF);
      bytes[1] = (byte) ((unsignedInt >> 16) & 0xFF);
      bytes[0] = (byte) ((unsignedInt >> 24) & 0xFF);
      return bytes;
    }

    /**
     * Converts unsigned short to a 2 byte array of unsigned bytes
     * @param unsignedShort representing the unsigned short
     * @return bytes an array of 2 unsigned bytes
     */
    public static byte[] unsignedShortToBytes(final Integer unsignedShort)
    {
      byte[] bytes = new byte[2];
      bytes[1] = (byte) (unsignedShort & 0xFF);
      bytes[0] = (byte) ((unsignedShort >> 8) & 0xFF);
      return bytes;
    }
}
