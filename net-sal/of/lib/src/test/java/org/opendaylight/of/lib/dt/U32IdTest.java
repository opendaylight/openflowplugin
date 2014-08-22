/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

/**
 * Base class for u32 id unit tests.
 *
 * @author Simon Hunt
 */
public abstract class U32IdTest extends UnsignedIdTest {
    public static final long ID_OVER = 65536L * 65536L;
    public static final String ID_OVER_STR_DEC = "4294967296";

    public static final long ID_MAX = 4294967295L;
    public static final String ID_MAX_STR_DEC = "4294967295";
    public static final String ID_MAX_STR_HEX = "0xffffffff";

    // most significant bit set
    public static final long ID_HIGH = 2435007850L;
    public static final String ID_HIGH_STR_DEC = "2435007850";
    public static final String ID_HIGH_STR_HEX = "0x9123456a";
    public static final String ID_HIGH_STR_HEX_PLUS = "0x9123456a(2435007850)";
    public static final byte[] ID_HIGH_BYTES = {0x91-B, 0x23, 0x45, 0x6a};

    protected static final long[] UNSORTED = {
            0x5,
            0x3,
            0x200,
            0x127,
            0x42,
            0x65000,
            0x199,
            0x254,
            0x213,
            0x32111,
            0x8765,
            0xfffacb,
            0xffdacb,
            0xaa04,
    };

    protected static final long[] SORTED = {
            0x3,
            0x5,
            0x42,
            0x127,
            0x199,
            0x200,
            0x213,
            0x254,
            0x8765,
            0xaa04,
            0x32111,
            0x65000,
            0xffdacb,
            0xfffacb,
    };

}
