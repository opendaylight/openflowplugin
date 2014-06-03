/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedBytes;

public class DropTestUtils {
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private static final void appendByte(final StringBuilder sb, final byte b) {
        int v = UnsignedBytes.toInt(b);
        sb.append(HEX[v >> 4]);
        sb.append(HEX[v & 15]);
    }

    public static String macToString(final byte[] mac) {
        Preconditions.checkArgument(mac.length == 6);

        final StringBuilder sb = new StringBuilder(17);
        appendByte(sb, mac[0]);

        for (int i = 1; i < mac.length; i++) {
            sb.append(':');
            appendByte(sb, mac[i]);
        }

        return sb.toString();
    }
}
