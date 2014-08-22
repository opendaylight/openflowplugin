/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.junit.Test;
import org.opendaylight.util.ByteUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Utility unit test for creating hex dumps of strings.
 *
 * @author Simon Hunt
 */
public class StringDumpTest {

    private static final String ASCII = "US-ASCII";

    private String dump(String s, int totalLen)
            throws UnsupportedEncodingException {
        if (totalLen % 16 != 0)
            throw new IllegalArgumentException("totalLen must be div. by 16");

        int total = totalLen;
        byte[] bytes = s.getBytes(ASCII);
        byte[] chunk = new byte[16];
        int idx = 0;
        int bytesLeft = bytes.length;
        StringBuilder sb = new StringBuilder();
        while (idx < bytes.length) {
            int howMany = bytesLeft >= 16 ? 16 : bytesLeft;
            System.arraycopy(bytes, idx, chunk, 0, howMany);
            sb.append(EOL).append(ByteUtils.toHexString(chunk));
            Arrays.fill(chunk, (byte) 0);
            idx += howMany;
            bytesLeft -= howMany;
            total -= 16;
        }
        while (total > 0) {
            sb.append(EOL).append(ByteUtils.toHexString(chunk));
            total -= 16;
        }

        return sb.toString();
    }

    @Test
    public void stringDump() throws UnsupportedEncodingException {
        String str = "The Last Table";
        int fieldLength = 32;
        print(EOL + "stringDump()");
        print("## [{}] \"{}\"", fieldLength, str);
        print(dump(str, fieldLength));
    }
}
