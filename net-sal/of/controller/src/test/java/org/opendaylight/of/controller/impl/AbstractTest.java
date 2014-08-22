/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.junit.TestTools;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.PortNumber;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Base class for unit tests, providing some useful convenience methods.
 *
 * @author Simon Hunt
 */
public abstract class AbstractTest {
    // PRIVATE - keep out!!
    private static final String E_BAD_FILE = "Couldn't read test data from: ";

    // stuff useful to subclasses
    protected static final String TEST_FILE_ROOT = "org/opendaylight/of/lib/";

    /** Our class loader reference. */
    protected static final ClassLoader CL = AbstractTest.class.getClassLoader();

    /** True if output is enabled. */
    protected final boolean showOutput = TestTools.showOutput();

    /** Exception printing format. */
    protected static final String FMT_EX = "EX> {}";

    protected static final String EMSG_NOT_SUP_BEFORE_13 =
            "Not supported before version 1.3";

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

    /** Returns the datapath ID instance for the corresponding string.
     *
     * @param d the dpid as a string
     * @return the dpid
     */
    public static DataPathId dpid(String d) {
        return DataPathId.valueOf(d);
    }

    /** Returns the virtual ID instance for the corresponding string.
     *
     * @param v the vid as a string
     * @return the virtual ID
     */
    public static VId vid(String v) {
        return VId.valueOf(v);
    }

    /** Returns the MAC address instance for the corresponding string.
     *
     * @param m the MAC as a string
     * @return the MAC address
     */
    public static MacAddress mac(String m) {
        return MacAddress.valueOf(m);
    }

    /** Returns the IP address instance for the corresponding string.
     *
     * @param ip the IP address as a string
     * @return the IP address
     */
    public static IpAddress ip(String ip) {
        return IpAddress.valueOf(ip);
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

    /** Asserts that the specified set contains exactly the set of flags given
     * as the remaining arguments.
     *
     * @param set the set to test
     * @param flags the expected flags
     * @param <E> the flag enumeration
     */
    protected <E extends Enum<E>> void verifyFlags(Set<E> set, E... flags) {
        assertEquals("incorrect number of flags", flags.length, set.size());
        for (E e: flags)
            assertTrue("missing flag: " + e, set.contains(e));
    }
}
