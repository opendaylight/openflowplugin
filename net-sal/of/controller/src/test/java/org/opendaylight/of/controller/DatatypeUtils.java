/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

import org.opendaylight.of.lib.dt.*;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.PortNumber;

/**
 * Houses useful convenience methods for declaring datatype values.
 *
 * @author Simon Hunt
 */
public class DatatypeUtils {

    /** Returns the given value formatted as a hex string.
     *
     * @param v the value
     * @return the value as a hex string
     */
    public static String hex(int v) {
        return "0x" + Integer.toHexString(v);
    }

    /** Returns the given value formatted as a hex string.
     *
     * @param v the value
     * @return the value as a hex string
     */
    public static String hex(long v) {
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
    public static BigPortNumber bpn(long n) {
        return BigPortNumber.valueOf(n);
    }

    /** Returns the given integer as the equivalent port number.
     *
     * @param n the number
     * @return the corresponding port number
     */
    public static PortNumber pn(int n) {
        return PortNumber.valueOf(n);
    }

    /** Returns the given long as the equivalent queue ID.
     *
     * @param n the number
     * @return the corresponding queue ID
     */
    public static QueueId qid(long n) {
        return QueueId.valueOf(n);
    }

    /** Returns the given integer as the equivalent table ID.
     *
     * @param n the number
     * @return the corresponding table ID
     */
    public static TableId tid(int n) {
        return TableId.valueOf(n);
    }

    /** Returns the given long as the equivalent buffer ID.
     *
     * @param n the number
     * @return the corresponding buffer ID
     */
    public static BufferId bid(long n) {
        return BufferId.valueOf(n);
    }

    /** Returns the given long as the equivalent group ID.
     *
     * @param n the number
     * @return the corresponding group ID
     */
    public static GroupId gid(long n) {
        return GroupId.valueOf(n);
    }

    /** Returns the given long as the equivalent meter ID.
     *
     * @param n the number
     * @return the corresponding meter ID
     */
    public static MeterId mid(long n) {
        return MeterId.valueOf(n);
    }
}
