/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.opendaylight.of.lib.dt.*;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.PortNumber;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Basis of unit test classes for openflow.
 *
 * @author Simon Hunt
 */
public abstract class AbstractTest {

    // PRIVATE - keep out!!
    private static final String E_BAD_FILE = "Couldn't read test data from: ";
    private static final ClassLoader CL = AbstractTest.class.getClassLoader();

    // stuff useful to subclasses
    protected static final String TEST_FILE_ROOT = "org/opendaylight/of/lib/";
    protected static final String AM_UNEX_MISMATCH = "unexpected mismatch";
    protected static final String AM_UNREAD_BYTES = "unread bytes";

    protected static final String FMT_EX = "EX> {}";
    protected static final String FMT_EX_CAUSE = " cause> {}";
    protected static final String FMT_PV_CODE_ENUM = "{} code: {} -> {}";
    protected static final String FMT_PV_BITS_FLAGS = "{} bits to flags: {} -> {}";
    protected static final String FMT_FLAGS_BITS = "      flags to bits: {} -> {}";

    protected static final String HEX = ".hex";

    protected static final ProtocolVersion[] PV_01 = {V_1_0, V_1_1};
    protected static final ProtocolVersion[] PV_012 = {V_1_0, V_1_1, V_1_2};
    protected static final ProtocolVersion[] PV_12 = {V_1_1, V_1_2};
    protected static final ProtocolVersion[] PV_23 = {V_1_2, V_1_3};
    protected static final ProtocolVersion[] PV_123 = {V_1_1, V_1_2, V_1_3};
    protected static final ProtocolVersion[] PV_0123 = {V_1_0, V_1_1, V_1_2, V_1_3};

    private static final String ASCII = "US-ASCII";

    /** Converts a string to a byte array, using ASCII encoding.
     *
     * @param s the string to encode
     * @return the string encoded as bytes
     */
    protected static byte[] bytesFromString(String s) {
        try {
            return s.getBytes(ASCII);
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new IllegalStateException(e);
        }
    }

    /** Returns a byte array slurped from a test .hex file.
     * The given path is relative to com/hp/net/of/.
     *
     * @param path the test file path
     * @return a byte array containing the data
     */
    protected static byte[] slurpedBytes(String path) {
        String filename = TEST_FILE_ROOT + path;
        byte[] packet = null;
        try {
            packet = ByteUtils.slurpBytesFromHexFile(filename, CL);
            if (packet == null) {
                fail(E_BAD_FILE + filename);
            }
        } catch (IOException e) {
            fail(E_BAD_FILE + filename);
        }
        return packet;
    }

    /** Returns an OpenFlow packet reader wrapping a channel buffer wrapping
     * a byte array slurped from a test file.
     * The given path is relative to com/hp/net/of/.
     *
     * @param path the test file path
     * @return a packet reader for the given file
     */
    protected static OfPacketReader getPacketReader(String path) {
        return getPacketReader(slurpedBytes(path));
    }

    /** Returns an OpenFlow packet reader wrapping a channel buffer wrapping
     * the given byte array.
     *
     * @param bytes the bytes to wrap in the buffer
     * @return a packet reader for the given bytes
     */
    protected static OfPacketReader getPacketReader(byte[] bytes) {
        return new OfPacketReader(bytes);
    }


    /** Verifies that the given set contains exactly the enumerations
     * specified as the remaining arguments.
     * @param act the actual set of constants
     * @param exp the expected constants
     */
    protected void verifyFlags(Set<? extends Enum<?>> act, Enum<?>... exp) {
        for (Enum<?> e: exp)
            assertTrue("missing flag: " + e, act.contains(e));
        assertEquals(AM_UXS, exp.length, act.size());
    }

    /** Checks that the given buffer has no more readable bytes. That is,
     * we are at the end of the buffer.
     *
     * @param pkt the packet buffer to check
     */
    protected void checkEOBuffer(OfPacketReader pkt) {
        assertEquals(AM_UNREAD_BYTES, 0, pkt.readableBytes());
    }

    /** Returns the given value formatted as a hex string.
     *
     * @param v the value
     * @return the value as a hex string
     */
    protected String hex(int v) {
        return "0x" + Integer.toHexString(v);
    }

    /** Returns the given value formatted as a hex string.
     *
     * @param v the value
     * @return the value as a hex string
     */
    protected String hex(long v) {
        return "0x" + Long.toHexString(v);
    }

    /** Returns the given string as the equivalent datapath ID.
     *
     * @param s the string
     * @return the corresponding datapath ID
     */
    protected static DataPathId dpid(String s) {
        return DataPathId.valueOf(s);
    }

    /** Returns the given int as the equivalent virtual ID.
     *
     * @param n the number
     * @return the corresponding virtual ID
     */
    protected static VId vid(int n) {
        return VId.valueOf(n);
    }

    /** Returns the given string as the equivalent MAC address.
     *
     * @param s the string
     * @return the corresponding MAC address
     */
    protected static MacAddress mac(String s) {
        return MacAddress.valueOf(s);
    }

    /** Returns the given string as the equivalent IP address.
     *
     * @param s the string
     * @return the corresponding IP address
     */
    protected static IpAddress ip(String s) {
        return IpAddress.valueOf(s);
    }

    /** Returns the given long as the equivalent big port number.
     *
     * @param n the number
     * @return the corresponding big port number
     */
    protected static BigPortNumber bpn(long n) {
        return BigPortNumber.valueOf(n);
    }

    /** Returns the given integer as the equivalent port number.
     *
     * @param n the number
     * @return the corresponding port number
     */
    protected static PortNumber pn(int n) {
        return PortNumber.valueOf(n);
    }

    /** Returns the given long as the equivalent queue ID.
     *
     * @param n the number
     * @return the corresponding queue ID
     */
    protected static QueueId qid(long n) {
        return QueueId.valueOf(n);
    }

    /** Returns the given integer as the equivalent table ID.
     *
     * @param n the number
     * @return the corresponding table ID
     */
    protected static TableId tid(int n) {
        return TableId.valueOf(n);
    }

    /** Returns the given long as the equivalent buffer ID.
     *
     * @param n the number
     * @return the corresponding buffer ID
     */
    protected static BufferId bid(long n) {
        return BufferId.valueOf(n);
    }

    /** Returns the given long as the equivalent group ID.
     *
     * @param n the number
     * @return the corresponding group ID
     */
    protected static GroupId gid(long n) {
        return GroupId.valueOf(n);
    }

    /** Returns the given long as the equivalent meter ID.
     *
     * @param n the number
     * @return the corresponding meter ID
     */
    protected static MeterId mid(long n) {
        return MeterId.valueOf(n);
    }

    /** Outputs expected and actual byte arrays, with a label.
     *
     * @param label the label (usually method name)
     * @param expData expected data
     * @param encoded actual data
     */
    protected void debugPrint(String label, byte[] expData, byte[] encoded) {
        print("{}---{}--------------------", EOL, label);
        print(Arrays.toString(expData));
        print(Arrays.toString(encoded));
        print("-------------------------------");
    }

    /** Shorthand for returning System.currentTimeMillis().
     *
     * @return current time in ms
     */
    protected static long ts() {
        return System.currentTimeMillis();
    }

    /** Calculates the difference between the longer and shorter times,
     * prints and returns the percent savings.
     *
     * @param longer the longer of two times
     * @param shorter the short of two times
     * @return the percent savings
     */
    protected double printSavings(long longer, long shorter) {
        long saved = longer - shorter;
        double percentSavings = ((double) saved) / ((double) longer) * 100.0;
        print("Percent savings: {}%", String.format("%.2f", percentSavings));
        return percentSavings;
    }

    /** Calculates the difference between the longer and shorter times, and
     * asserts that the percent savings are at least as much as the specified
     * value.
     *
     * @param longer the longer of two times
     * @param shorter the short of two times
     * @param minSavings the minimum percent savings for the test to pass
     */
    protected void verifyPercentSavings(long longer, long shorter,
                                        double minSavings) {
        double percentSavings = printSavings(longer, shorter);
        assertTrue("Less than min % savings", percentSavings > minSavings);
    }

}

