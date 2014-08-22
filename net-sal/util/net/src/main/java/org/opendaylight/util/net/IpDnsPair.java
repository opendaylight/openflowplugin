/*
 * (c) Copyright 2009-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;

import org.opendaylight.util.cache.CacheableDataType;

/**
 * Links together an {@link IpAddress} and a {@link DnsName} as a pair.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code IpDnsPair} is done via the static {@link #valueOf} methods on the
 * class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of pairs is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class IpDnsPair extends CacheableDataType
                             implements Comparable<IpDnsPair> {

    private static final long serialVersionUID = 9007439931742527253L;

    private static final String DASH = " - ";

    /** The IP Address.
     * @serial ip address
     */
    private final IpAddress ip;

    /** The DNS Name.
     * @serial host name
     */
    private final DnsName dns;

    // === PRIVATE CONSTRUCTORS ===============================================

    /** Private constructor.
     *
     * @param ip the IP address
     * @param dns the DNS name
     * @throws NullPointerException if either parameter is null
     */
    private IpDnsPair(IpAddress ip, DnsName dns) {
        if (ip == null || dns == null)
            throw new NullPointerException("parameters cannot be null");

        this.ip = ip;
        this.dns = dns;
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      Default serialization is used for IP address and DNS Name.

    private Object readResolve() throws ObjectStreamException {
        // when this is called, ip and dns have been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(ip, dns);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }



    //=== Public API ==========================================================

    /** Returns the IP address.
     *
     * @return the IP address
     */
    public IpAddress getIp() {
        return ip;
    }

    /** Returns the DNS name.
     *
     * @return the DNS name
     */
    public DnsName getDns() {
        return dns;
    }

    /** Returns true if the DNS name stored is an "Unresolvable" instance.
     *
     * @return true if the DNS name is "Unresolvable"
     */
    public boolean hasUnresolvableDnsName() {
        return DnsName.UNRESOLVABLE.equals(dns);
    }

    @Override
    public String toString() {
        return ip + DASH + dns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return ip.equals(((IpDnsPair) o).ip) && dns.equals(((IpDnsPair) o).dns);
    }

    @Override
    public int hashCode() {
        return 31 * ip.hashCode() + dns.hashCode();
    }

    /** Delegates our comparable to the IpAddress implementation.
     *
     * @param o the other pair
     * @return a result indicating our relative ordering
     */
    @Override
    public int compareTo(IpDnsPair o) {
        return ip.compareTo(o.ip);
    }

    //=== PUBLIC static API ===================================================

    /** Returns an {@code IpDnsPair} object that represents the specified
     * {@code IpAddress} and {@code DnsName} paired together.
     *
     * @param ip the ip address
     * @param dns the dns name
     * @return the IP / DNS name pair
     * @throws NullPointerException if either parameter is null
     */
    public static IpDnsPair valueOf(IpAddress ip, DnsName dns) {
        return new IpDnsPair(ip, dns);
    }

    /** Calls {@link #valueOf(IpAddress,DnsName)} after converting the string
     * parameters to their respective data-types.
     *
     * @param ip a string representation of an IP address
     * @param dns a string representation of a DNS name
     * @return the IP / DNS name pair
     * @throws NullPointerException if either parameter is null
     */
    public static IpDnsPair valueOf(String ip, String dns) {
        return valueOf(IpAddress.valueOf(ip), DnsName.valueOf(dns));
    }

    /** Returns an {@code IpDnsPair} object that represents the specified
     * {@code IpAddress} and the "unresolvable" {@code DnsName} paired together.
     * This convenience method is equivalent to invoking:
     * <pre>
     *   IpDnsPair p = IpDnsPair.valueOf(ip, DnsName.getUnresolvableInstance());
     * </pre>
     *
     * @param ip the ip address
     * @return the IP / Unresolvable DNS name pair
     * @throws NullPointerException if the parameter is null
     */
    public static IpDnsPair valueOf(IpAddress ip) {
        return new IpDnsPair(ip, DnsName.UNRESOLVABLE);
    }

    /** Calls {@link #valueOf(IpAddress)} after converting the
     * string parameter to an IpAddress instance.
     *
     * @param ip a string representation of an IP address
     * @return the IP / Unresolvable DNS name pair
     * @throws NullPointerException if the parameter is null
     */
    public static IpDnsPair valueOf(String ip) {
        return valueOf(IpAddress.valueOf(ip));
    }
}
