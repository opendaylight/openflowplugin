/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import junit.framework.Assert;
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.util.junit.TestTools;
import org.opendaylight.util.net.*;

/**
 * Provides a base class for JSON codec unit test classes.
 *
 * @author Simon Hunt
 */
public abstract class AbstractCodecTest {

    private static final String DOT_JSON = ".json";
    private static final String ROOT_DIR = "org/opendaylight/of/json/";
    private static final ClassLoader cl =
            AbstractCodecTest.class.getClassLoader();

    /** Returns the contents of the specified file as a string. Note that
     * a prefix of {@code "org/opendaylight/of/json/"} and a suffix of {@code ".json"}
     * is applied to the parameter to derive the full file pathname.
     *
     * @param testfile the test file
     * @return the contents of that file as a string
     */
    protected static String getJsonContents(String testfile) {
        final String path = ROOT_DIR + testfile + DOT_JSON;
        String contents = null;
        try {
            contents = TestTools.getFileContents(path, cl);
        } catch (Exception e) {
            Assert.fail("unable to load datafile: " + path);
        }
        return contents;
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

    /** Returns the given int as the equivalent ICMPv4 Type.
     *
     * @param n the number
     * @return the corresponding ICMPv4 Type
     */
    protected static ICMPv4Type icmpv4Type(int n) {
        return ICMPv4Type.valueOf(n);
    }

    /** Returns the given int as the equivalent ICMPv6 Type.
     *
     * @param n the number
     * @return the corresponding ICMPv6 Type
     */
    protected static ICMPv6Type icmpv6Type(int n) {
        return ICMPv6Type.valueOf(n);
    }

}
