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
    public static String bytesToHexstring(byte[] bytes, String delimiter) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            sb.append(String.format("%02x%s", b, delimiter));
        }
        return sb.toString();
    }

    /**
     * Utility method to convert BigInteger to 8 element byte array
     * @param bigInteger
     * @return byte array containing 64 bits.
     */
    public static byte[] convertBigIntegerTo64Bit(BigInteger bigInteger) {
        if (bigInteger == null) {
            return null;
        }
        byte[] inputArray = bigInteger.toByteArray();
        byte[] outputArray = new byte[8];
        if (bigInteger.compareTo(BigInteger.ZERO) < 0) {
            Arrays.fill(outputArray, (byte) -1);
        } else {
            Arrays.fill(outputArray, (byte) 0);
        }
        System.arraycopy(inputArray, 0, outputArray, outputArray.length - inputArray.length, inputArray.length);
        return outputArray;
    }
}
