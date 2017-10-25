/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author michal.polkorab
 */
public class BinContentTest {

    /**
     * Testing correct conversion from signed int value to unsigned long value
     */
    @Test
    public void testIntToUnsignedLong() {
        int a = 0;
        int b = 1;
        int c = -1;
        int d = Integer.MAX_VALUE;
        int e = Integer.MIN_VALUE;
        int f = 12345;
        Assert.assertEquals("Wrong conversion", 0, BinContent.intToUnsignedLong(a));
        Assert.assertEquals("Wrong conversion", 1, BinContent.intToUnsignedLong(b));
        Assert.assertEquals("Wrong conversion", 4294967295L, BinContent.intToUnsignedLong(c));
        Assert.assertEquals("Wrong conversion", Integer.MAX_VALUE, BinContent.intToUnsignedLong(d));
        Assert.assertEquals("Wrong conversion", ((long) Integer.MAX_VALUE) + 1, BinContent.intToUnsignedLong(e));
        Assert.assertEquals("Wrong conversion", 12345, BinContent.intToUnsignedLong(f));
    }

    /**
     * Testing correct conversion from unsigned long value to signed int value
     */
    @Test
    public void testLongToSignedInt() {
        long a = 0;
        long b = 1;
        long c = -1;
        long d = Integer.MAX_VALUE;
        long e = Integer.MIN_VALUE;
        long f = 12345;
        long g = Long.MAX_VALUE;
        long h = 1094624935644L;
        long i = ((long) Integer.MAX_VALUE) + 1;
        Assert.assertEquals("Wrong conversion", 0, BinContent.longToSignedInt(a));
        Assert.assertEquals("Wrong conversion", 1, BinContent.longToSignedInt(b));
        Assert.assertEquals("Wrong conversion", -1, BinContent.longToSignedInt(c));
        Assert.assertEquals("Wrong conversion", Integer.MAX_VALUE, BinContent.longToSignedInt(d));
        Assert.assertEquals("Wrong conversion", Integer.MIN_VALUE, BinContent.longToSignedInt(e));
        Assert.assertEquals("Wrong conversion", 12345, BinContent.longToSignedInt(f));
        Assert.assertEquals("Wrong conversion", -1, BinContent.longToSignedInt(g));
        Assert.assertEquals("Wrong conversion", -591724836, BinContent.longToSignedInt(h));
        Assert.assertEquals("Wrong conversion", Integer.MIN_VALUE, BinContent.longToSignedInt(i));
    }

}
