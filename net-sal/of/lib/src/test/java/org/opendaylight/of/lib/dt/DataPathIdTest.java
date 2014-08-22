/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.junit.Assume;
import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.RandomUtils;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.MacRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link DataPathId}.
 *
 * @author Simon Hunt
 */
public class DataPathIdTest extends AbstractTest {

    private DataPathId dpid;
    private DataPathId dpidAlt;

    private static final String MAC_A_STR = "AA:AA:AA:AA:AA:AA";
    private static final MacAddress MAC_A = mac(MAC_A_STR);
    private static final String MAC_B_STR = "BB:BB:BB:BB:BB:BB";
    private static final MacAddress MAC_B = mac(MAC_B_STR);

    private static final VId VID_1 = vid(1);
    private static final VId VID_2 = vid(2);

    private static final String DPID_1_A = "00:01:aa:aa:aa:aa:aa:aa";
    private static final String DPID_1_B = "00:01:bb:bb:bb:bb:bb:bb";
    private static final String DPID_2_A = "00:02:aa:aa:aa:aa:aa:aa";
    private static final String DPID_2_B = "00:02:bb:bb:bb:bb:bb:bb";

    private static final String DPID_234_1F = "00:ea:1f:1f:1f:1f:1f:1f";
    private static final String DPID_234_0F = "00:ea:0f:ff:ff:ff:ff:0f";

    private static final String SPEC_1 = "123/00:05:53:AF:AA:C0";
    private static final String SPEC_2 = "0x7b/000553:afaac0";
    private static final String SPEC_3 = "007b000553AFAAC0";
    private static final String SPEC_4 = "00:7B:00:05:53:AF:AA:C0";

    @Test(expected = NullPointerException.class)
    public void nullVid() {
        dpid = DataPathId.valueOf(null, MAC_A);
    }

    @Test(expected = NullPointerException.class)
    public void nullMac() {
        dpid = DataPathId.valueOf(VID_1, null);
    }

    @Test(expected = NullPointerException.class)
    public void bothNull() {
        dpid = DataPathId.valueOf(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringSpec() {
        dpid = DataPathId.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArray() {
        dpid = DataPathId.valueOf((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortByteArray() {
        dpid = DataPathId.valueOf(new byte[7]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void longByteArray() {
        dpid = DataPathId.valueOf(new byte[9]);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        dpid = DataPathId.valueOf(VID_1, MAC_A);
        print(dpid);
        assertEquals(AM_NEQ, VID_1, dpid.getVid());
        assertEquals(AM_NEQ, MAC_A, dpid.getMacAddress());
    }

    @Test
    public void fromString() {
        print(EOL + "fromString()");
        dpid = DataPathId.valueOf(DPID_1_A);
        print(dpid);
        assertEquals(AM_NEQ, VID_1, dpid.getVid());
        assertEquals(AM_NEQ, MAC_A, dpid.getMacAddress());
    }
    
    @Test
    public void convenience() {
        print(EOL + "convenience()");
        dpid = DataPathId.dpid(DPID_1_A);
        dpidAlt = DataPathId.valueOf(DPID_1_A);
        assertEquals(AM_NEQ, dpidAlt, dpid);
    }

    private static final String[] UNSORTED = {
            DPID_234_0F,
            DPID_1_B,
            DPID_2_B,
            DPID_234_1F,
            DPID_2_A,
            DPID_1_A,
    };
    private static final String[] SORTED = {
            DPID_1_A,
            DPID_1_B,
            DPID_2_A,
            DPID_2_B,
            DPID_234_0F,
            DPID_234_1F,
    };

    @Test
    public void comparison() {
        print(EOL + "comparison()");
        print("Unsorted (strings)...");
        print(Arrays.toString(UNSORTED));
        int i = 0;
        int count = UNSORTED.length;
        DataPathId[] dpids = new DataPathId[count];
        for (String s: UNSORTED) {
            dpids[i++] = DataPathId.valueOf(s);
        }
        print("Unsorted (dpids)...");
        print(Arrays.toString(dpids));
        Arrays.sort(dpids);
        print("Sorted (dpids)...");
        print(Arrays.toString(dpids));
        // verify sort order
        i = 0;
        for (String s: SORTED) {
            assertEquals(AM_NEQ, s, dpids[i++].toString());
        }
    }

    @Test
    public void sameRef() {
        dpid = DataPathId.valueOf(VID_2, MAC_B);
        dpidAlt = DataPathId.valueOf(DPID_2_B);
        assertSame(AM_NSR, dpid, dpidAlt);
    }

    @Test
    public void eq() {
        dpid = DataPathId.valueOf(DPID_1_A);
        dpidAlt = DataPathId.valueOf(VID_1, MAC_A);
        verifyEqual(dpid, dpidAlt);
        dpidAlt = DataPathId.valueOf(DPID_234_0F);
        verifyNotEqual(dpid, dpidAlt);
    }

    private static final String[] BAD_SPECS = {
            "",
            "/",
            "//",
            "1234",
            "1234/",
            "/aa:bb:cc:dd:11:22:33:44",
            "-1/mac",
            "-1/aa:bb:cc:11:22:33",
            "65537/11:22:33:44:55:66",
            "65500/11:22:33:44:55:66/0",
    };

    @Test
    public void badSpecs() {
        print(EOL + "badSpecs()");
        for (String s: BAD_SPECS) {
            try {
                dpid = DataPathId.valueOf(s);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print("EX> {}", e);
            }
        }
    }

    private static final String SAME_BASE = "65/AA:1B:CC:2D:EE:3F";
    private static final String[] SAME_THING = {
            SAME_BASE,
            "65/aa:1b:cc:2d:ee:3f",
            "65/aa-1b-cc-2d-ee-3f",
            "65/aa1bcc:2dee3f",
            "0x41/aa1bcc:2dee3f",
            "65/aa1bcc-2dee3f",
            "65/AA1BCC2DEE3F",
            "0041aa1bcc2dee3f",
            "dp0041aa1bcc2dee3f",
            "DP0041AA1BCC2DEE3F",
            "00:41:AA:1B:CC:2D:EE:3F",
            "00:41:aa:1b:cc:2d:ee:3f",
    };

    @Test
    public void sameThing() {
        print(EOL + "sameThing()");
        dpid = DataPathId.valueOf(SAME_BASE);
        for (String s: SAME_THING) {
            print(s);
            dpidAlt = DataPathId.valueOf(s);
            assertSame(AM_NSR, dpid, dpidAlt);
        }
    }

    @Test
    public void notYetAskedForThisOne() {
        dpid = DataPathId.valueOf(VId.valueOf(9988), MAC_A);
        assertEquals(AM_NEQ, 9988, dpid.getVid().toInt());
        dpidAlt = DataPathId.valueOf("9988/aaaaaaaaaaaa");
        assertSame(dpid, dpidAlt);
    }

    private static final byte[] DPID_BYTES = new byte[] {
            0x00, 0x40, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66
    };

    @Test
    public void valueOfFromBytes() {
        print(EOL + "valueOfFromBytes()");
        dpid = DataPathId.valueOf(DPID_BYTES);
        print(dpid);
        assertEquals(AM_NEQ, "00:40:11:22:33:44:55:66", dpid.toString());
        assertEquals(AM_NEQ, VId.valueOf(64), dpid.getVid());
        assertEquals(AM_NEQ, MacAddress.valueOf("11-22-33-44-55-66"),
                dpid.getMacAddress());
    }

    @Test
    public void toBytes() {
        print(EOL + "toBytes()");
        dpid = DataPathId.valueOf("64/11:22:33:44:55:66");
        byte[] bytes = dpid.toByteArray();
        print(ByteUtils.toHexArrayString(bytes));
        assertArrayEquals(AM_NEQ, DPID_BYTES, bytes);
    }

    @Test
    public void toAltString() {
        print(EOL + "toAltString()");
        dpid = DataPathId.valueOf("2989/de:ad:be:ef:ee:fb");
        print(dpid.toString());
        print(dpid.toAltString());
        assertEquals(AM_NEQ, "0b:ad:de:ad:be:ef:ee:fb", dpid.toString());
        assertEquals(AM_NEQ, "2989/deadbe:efeefb", dpid.toAltString());
    }

    @Test
    public void fourSpecs() {
        print(EOL + "fourSpecs");
        dpid = DataPathId.valueOf(SPEC_1);
        DataPathId same = DataPathId.valueOf(SPEC_2);
        DataPathId also = DataPathId.valueOf(SPEC_3);
        DataPathId copy = DataPathId.valueOf(SPEC_4);
        assertSame(AM_NSR, dpid, same);
        assertSame(AM_NSR, dpid, also);
        assertSame(AM_NSR, dpid, copy);
    }

    @Test
    public void valueOfLongOne() {
        print(EOL + "valueOfLong()");
        final long value = 255;
        dpid = DataPathId.valueOf(value);
        print("{} => {}", value, dpid);
        assertEquals(AM_NEQ, dpid("0/000000:0000ff"), dpid);
        long opaque = dpid.toLong();
        assertEquals(AM_NEQ, value, opaque);
    }

    @Test
    public void valueOfLongTwo() {
        print(EOL + "valueOfLong()");
        final long value = -1L;
        dpid = DataPathId.valueOf(value);
        print("{} => {}", value, dpid);
        assertEquals(AM_NEQ, dpid("ff:ff:ff:ff:ff:ff:ff:ff"), dpid);
        long opaque = dpid.toLong();
        assertEquals(AM_NEQ, value, opaque);
    }

    // ======================== DPID Hashing ==============================

    private static final MacRange MR = MacRange.valueOf("*:*:*:*:*:*");
    private int vid = 1;

    private DataPathId getRandomDpid() {
        return DataPathId.valueOf(VId.valueOf(vid++), MR.random());
    }

    // Make sure hashing algorithm distributes evenly across N buckets.
    @Test
    public void dpidHashing() {
        print(EOL + "dpidHashing");
        // seeing if we can come up with a hashing function of
        // DataPathId -> index, for selecting an executor from a pool
        int total = 2000;
        int buckets = 10;

        int expMeanPerBucket = total / buckets;
        int tolerance = expMeanPerBucket / 2;
        int minCount = expMeanPerBucket - tolerance;
        int maxCount = expMeanPerBucket + tolerance;

        int counts[] = new int[buckets];
        for (int c = 0; c < total; c++)
            counts[getRandomDpid().hashBucket(buckets)]++;
        print(Arrays.toString(counts));
        for (int i=0; i<buckets; i++)
            assertTrue("[" + i + "] out of tolerance",
                    counts[i] >= minCount && counts[i] <= maxCount);
    }

    // Make sure same DPID always hashes to the same bucket
    @Test
    public void dpidHashToSame() {
        print(EOL + "dpidHashToSame()");
        int loBuckets = 1;
        int hiBuckets = 30;
        int dpidsToTest = 10;
        int repeats = 120;

        print("Each run will test {} random dpids, for {} repeats...",
                dpidsToTest, repeats);
        List<DataPathId> dpids = new ArrayList<DataPathId>(dpidsToTest);
        for (int i=0; i<dpidsToTest; i++)
            dpids.add(getRandomDpid());

        for (int nBkts = loBuckets; nBkts <= hiBuckets; nBkts++) {
            print("{}Testing with {} buckets...", EOL, nBkts);
            for (DataPathId d: dpids) {
                int h = d.hashBucket(nBkts);
                print("  {} hashBucket({}) => {}", d, nBkts, h);
                assertTrue("hash out of bounds", h >= 0 && h < nBkts);
                for (int j=0; j<repeats; j++)
                    assertEquals(AM_NEQ, h, d.hashBucket(nBkts));
            }
        }

    }


    // ======================== Speed Tests ==============================

    // === DPID lookup by byte array - speed test

    private static final int ITERATIONS = 500000;
    private static final double ONE_MILLION = 1000000.0;

    private static final int POOL_SIZE = 100;
    private static final MacRange mr = MacRange.valueOf("1:1:1:1:*:*");

    private List<byte[]> poolBytes = new ArrayList<byte[]>(POOL_SIZE);

    private void fillPool() {
        poolBytes.clear();
        for (int i=0; i<POOL_SIZE; i++) {
            // cache some dpid instances
            dpid = DataPathId.valueOf(VID_1, mr.random());
            poolBytes.add(dpid.toByteArray());
        }
    }

    @Test
    public void lookupSpeedTest() {
        Assume.assumeTrue(!isUnderCoverage() && !ignoreSpeedTests());
        print(EOL + "lookupSpeedTest()");
        fillPool();

        long start = ts();
        for (int i=0; i<ITERATIONS; i++)
            dpid = DataPathId.valueOf(RandomUtils.select(poolBytes));
        long duration = ts() - start;
        print("{} Iterations: duration = {}ms", ITERATIONS, duration);
        double ips = (ITERATIONS * 1000.0) / duration;
        print("{} lookups per second", String.format("%.2f", ips));
        assertAboveThreshold("lookups/s", ONE_MILLION, ips);
    }

}
