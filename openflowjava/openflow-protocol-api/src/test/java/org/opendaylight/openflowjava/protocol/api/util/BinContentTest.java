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
 * Unit tests for BinContent.
 *
 * @author michal.polkorab
 */
public class BinContentTest {

    /**
     * Testing correct conversion from signed int value to unsigned long value.
     */
    @Test
    public void testIntToUnsignedLong() {
        Assert.assertEquals("Wrong conversion", 0, BinContent.intToUnsignedLong(0));
        Assert.assertEquals("Wrong conversion", 1, BinContent.intToUnsignedLong(1));
        Assert.assertEquals("Wrong conversion", 4294967295L, BinContent.intToUnsignedLong(-1));
        Assert.assertEquals("Wrong conversion", Integer.MAX_VALUE, BinContent.intToUnsignedLong(Integer.MAX_VALUE));
        Assert.assertEquals("Wrong conversion", (long) Integer.MAX_VALUE + 1,
                BinContent.intToUnsignedLong(Integer.MIN_VALUE));
        Assert.assertEquals("Wrong conversion", 12345, BinContent.intToUnsignedLong(12345));
    }

    /**
     * Testing correct conversion from unsigned long value to signed int value.
     */
    @Test
    public void testLongToSignedInt() {
        Assert.assertEquals("Wrong conversion", 0, BinContent.longToSignedInt(0L));
        Assert.assertEquals("Wrong conversion", 1, BinContent.longToSignedInt(1L));
        Assert.assertEquals("Wrong conversion", -1, BinContent.longToSignedInt(-1L));
        Assert.assertEquals("Wrong conversion", Integer.MAX_VALUE, BinContent.longToSignedInt(Integer.MAX_VALUE));
        Assert.assertEquals("Wrong conversion", Integer.MIN_VALUE, BinContent.longToSignedInt(Integer.MIN_VALUE));
        Assert.assertEquals("Wrong conversion", 12345, BinContent.longToSignedInt(12345L));
        Assert.assertEquals("Wrong conversion", -1, BinContent.longToSignedInt(Long.MAX_VALUE));
        Assert.assertEquals("Wrong conversion", -591724836, BinContent.longToSignedInt(1094624935644L));
        Assert.assertEquals("Wrong conversion", Integer.MIN_VALUE, BinContent.longToSignedInt(Integer.MAX_VALUE + 1L));
    }

}
