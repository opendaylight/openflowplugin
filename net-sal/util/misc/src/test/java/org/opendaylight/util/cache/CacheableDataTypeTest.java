/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.AM_UXS;
import static org.opendaylight.util.junit.TestTools.print;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Unit tests for CacheableDataType.
 *
 * @author Simon Hunt
 */
public class CacheableDataTypeTest {

    private static final byte[] DP_01_0B = {0x00, 0x00, 0x01, 0x0b};
    private static final byte[] DP_0B_01 = {0x00, 0x00, 0x0b, 0x01};

    // Unit test to prove fix for CR# 152415 works as advertised
    @Test
    public void keyFromBytesAlgorithm() {
        String dp1b = CacheableDataType.keyFromBytes(DP_01_0B);
        String dpb1 = CacheableDataType.keyFromBytes(DP_0B_01);
        assertFalse("Strings same, but should not be", dp1b.equals(dpb1));
    }

    private static final int DIM = 65536;

    @Test @Ignore("Takes approx 30 seconds to run")
    public void exhaustiveKeyComparison() {
        List<String> twoByteKeys = genKeys();
        assertEquals(AM_UXS, DIM, twoByteKeys.size());
        int dups = 0;

        for (int i=0; i<DIM; i++) {
            for (int j=0; j<DIM; j++) {
                if (i == j)
                    continue;

                String k1 = twoByteKeys.get(i);
                String k2 = twoByteKeys.get(j);

                if (k1.equals(k2))
                    dups++;

            }
        }
        print("{} dups found", dups);
        assertEquals("Dups found!!!", 0, dups);
    }

    private List<String> genKeys() {
        List<String> result = new ArrayList<>(DIM);
        byte[] bytes = new byte[2];

        for (int a=0; a<256; a++) {
            bytes[0] = (byte) a;

            for (int b=0; b<256; b++) {
                bytes[1] = (byte) b;

                result.add(CacheableDataType.keyFromBytes(bytes));
            }
        }
        return result;
    }

}
