/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.net.UnsignedId.parseIntStr;
import static org.opendaylight.util.net.UnsignedId.parseLongStr;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for UnsignedId, plus a base class for other unsigned id
 * classes.
 *
 * @author Simon Hunt
 */
public abstract class UnsignedIdTest {

    protected static final int ID_UNDER = -1;
    protected static final String ID_UNDER_STR_DEC = "-1";

    protected static final int ID_MIN = 0;
    protected static final String ID_MIN_STR_DEC = "0";
    protected static final String ID_MIN_STR_HEX = "0x0";

    // most significant bit not set
    protected static final int ID_LOW = 23;
    protected static final String ID_LOW_STR_DEC = "23";
    protected static final String ID_LOW_STR_HEX = "0x17";

    protected static final String FOOEY = "fooey";
    protected static final int B = 256;



    @Test
    public void parseIntMin() {
        assertEquals(AM_NEQ, ID_MIN, parseIntStr(ID_MIN_STR_DEC));
        assertEquals(AM_NEQ, ID_MIN, parseIntStr(ID_MIN_STR_HEX));
    }

    @Test
    public void parseLongMin() {
        assertEquals(AM_NEQ, ID_MIN, parseLongStr(ID_MIN_STR_DEC));
        assertEquals(AM_NEQ, ID_MIN, parseLongStr(ID_MIN_STR_HEX));
    }

    @Test
    public void parseIntLow() {
        assertEquals(AM_NEQ, ID_LOW, parseIntStr(ID_LOW_STR_DEC));
        assertEquals(AM_NEQ, ID_LOW, parseIntStr(ID_LOW_STR_HEX));
    }

    @Test
    public void parseLongLow() {
        assertEquals(AM_NEQ, ID_LOW, parseLongStr(ID_LOW_STR_DEC));
        assertEquals(AM_NEQ, ID_LOW, parseLongStr(ID_LOW_STR_HEX));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseIntFooey() {
        parseIntStr(FOOEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseLongFooey() {
        parseLongStr(FOOEY);
    }

    @Test(expected = NullPointerException.class)
    public void parseIntNull() {
        parseIntStr(null);
    }

    @Test(expected = NullPointerException.class)
    public void parseLongNull() {
        parseLongStr(null);
    }
}
