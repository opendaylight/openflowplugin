/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.libraries.liblldp;

import java.math.BigInteger;

/**
 * The class provides methods to convert hex encode strings.
 */
public final class HexEncode {
    private HexEncode() {
    }

    /**
     * This method converts byte array into String format without ":" inserted.
     *
     * @param bytes
     *            The byte array to convert to string
     * @return The hexadecimal representation of the byte array. If bytes is
     *         null, "null" string is returned
     */
    public static String bytesToHexString(final byte[] bytes) {

        if (bytes == null) {
            return "null";
        }

        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            short u8byte = (short) (b & 0xff);
            String tmp = Integer.toHexString(u8byte);
            if (tmp.length() == 1) {
                buf.append("0");
            }
            buf.append(tmp);
        }
        return buf.toString();
    }

    public static String longToHexString(final long val) {
        char[] arr = Long.toHexString(val).toCharArray();
        StringBuilder buf = new StringBuilder();
        // prepend the right number of leading zeros
        int index = 0;
        for (; index < 16 - arr.length; index++) {
            buf.append("0");
            if ((index & 0x01) == 1) {
                buf.append(":");
            }
        }
        for (int j = 0; j < arr.length; j++) {
            buf.append(arr[j]);
            if ((index + j & 0x01) == 1 && j < arr.length - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }


    public static byte[] bytesFromHexString(final String values) {
        String target = "";
        if (values != null) {
            target = values;
        }
        String[] octets = target.split(":");

        byte[] ret = new byte[octets.length];
        for (int i = 0; i < octets.length; i++) {
            ret[i] = Integer.valueOf(octets[i], 16).byteValue();
        }
        return ret;
    }

    public static long stringToLong(final String values) {
        return new BigInteger(values.replaceAll(":", ""), 16).longValue();
    }

    /**
     * This method converts byte array into HexString format with ":" inserted.
     */
    public static String bytesToHexStringFormat(final byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                buf.append(":");
            }
            short u8byte = (short) (bytes[i] & 0xff);
            String tmp = Integer.toHexString(u8byte);
            if (tmp.length() == 1) {
                buf.append("0");
            }
            buf.append(tmp);
        }
        return buf.toString();
    }
}
