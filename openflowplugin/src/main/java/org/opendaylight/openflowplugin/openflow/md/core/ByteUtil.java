/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

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
}
