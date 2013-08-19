package org.openflow.codec.util;

import java.math.BigInteger;

import org.openflow.codec.util.U64;

import junit.framework.TestCase;

public class U64Test extends TestCase {
    /**
     * Tests that we correctly translate unsigned values in and out of a long
     *
     * @throws Exception
     */
    public void test() throws Exception {
        BigInteger val = new BigInteger("ffffffffffffffff", 16);
        TestCase.assertEquals(-1, U64.t(val));
        TestCase.assertEquals(val, U64.f(-1));
    }
}
