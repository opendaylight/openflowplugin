package org.openflow.codec.util;

import org.openflow.codec.util.U8;

import junit.framework.TestCase;

public class U8Test extends TestCase {
    /**
     * Tests that we correctly translate unsigned values in and out of a byte
     *
     * @throws Exception
     */
    public void test() throws Exception {
        short val = 0xff;
        TestCase.assertEquals(-1, U8.t(val));
        TestCase.assertEquals(val, U8.f((byte) -1));
    }
}
