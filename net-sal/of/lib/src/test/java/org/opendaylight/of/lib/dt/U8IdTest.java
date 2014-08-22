/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

/**
 * Base class for u8 id unit tests.
 *
 * @author Simon Hunt
 */
public abstract class U8IdTest extends UnsignedIdTest {

    public static final int ID_OVER = 256;
    public static final String ID_OVER_STR_DEC = "256";

    public static final int ID_MAX = 255;
    public static final String ID_MAX_STR_DEC = "255";
    public static final String ID_MAX_STR_HEX = "0xff";
    public static final byte ID_MAX_BYTE = (byte) 0xff;

    // most significant bit set
    public static final int ID_HIGH = 200;
    public static final String ID_HIGH_STR_DEC = "200";
    public static final String ID_HIGH_STR_HEX = "0xc8";

    protected static final int[] UNSORTED = {
            5,
            3,
            200,
            127,
            42,
            69,
            199,
            254,
            213,
    };
    protected static final int[] SORTED = {
            3,
            5,
            42,
            69,
            127,
            199,
            200,
            213,
            254,
    };

}
