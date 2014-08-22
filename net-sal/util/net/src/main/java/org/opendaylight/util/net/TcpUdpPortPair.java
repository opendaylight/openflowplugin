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
import org.opendaylight.util.cache.WeakValueCache;

/**
 * Represents a pair of ports defined by a source {@link IpAddress} and
 * {@link TcpUdpPort} and a destination {@link IpAddress} and
 * {@link TcpUdpPort}.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code TcpUdpPortPair} is done via the static {@link #valueOf} methods
 * on the class.
 * <p>
 * Instances of this class are immutable, making them inherently thread-safe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of pairs is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class TcpUdpPortPair extends CacheableDataType
                    implements Comparable<TcpUdpPortPair> {

    private static final long serialVersionUID = 5639844869051298706L;

    private static final String ARROW = " --> ";
    private static final String OPEN_P = " (";
    private static final String CLOSE_P = ")";


    /** Source IP Address.
     * @serial source address
     */
    private final IpAddress sourceIp;

    /** Source port.
     * @serial source port
     */
    private final TcpUdpPort sourcePort;

    /** Destination IP Address.
     * @serial target address
     */
    private final IpAddress destIp;
    /** Destination port.
     * @serial target port
     */
    private final TcpUdpPort destPort;


    private final transient int cachedHashCode;
    private transient String asString;
    private transient String asShortString;

    // === PRIVATE CONSTRUCTORS ===============================================

    /** Constructs a port pair.
     *
     * @param sourceIp the IP address of the source
     * @param sourcePort the port of the source
     * @param destIp the IP address of the destination
     * @param destPort the port of the destination
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if the port protocols don't match
     */
    private TcpUdpPortPair(IpAddress sourceIp, TcpUdpPort sourcePort,
                           IpAddress destIp, TcpUdpPort destPort) {

        if (sourceIp == null || sourcePort == null ||
                destIp == null || destPort == null)
            throw new NullPointerException("null parameter");

        if (sourcePort.getProtocol() != destPort.getProtocol()) {
            String msg = "Protocol Mismatch : source = " +
                    sourcePort.getProtocol().getShortName() +
                    ", destination = " + destPort.getProtocol().getShortName();
            throw new IllegalArgumentException(msg);
        }
        this.sourceIp = sourceIp;
        this.sourcePort = sourcePort;
        this.destIp = destIp;
        this.destPort = destPort;
        cachedHashCode = calculateHashCode();
    }

    //=== PRIVATE helper methods ==============================================

    /** precompute the hashcode.
     *
     * @return the hashcode
     */
    private int calculateHashCode() {
        int result = sourceIp.hashCode();
        result = 31 * result + sourcePort.hashCode();
        result = 31 * result + destIp.hashCode();
        result = 31 * result + destPort.hashCode();
        return result;
    }

    /** Creates the short and long string representations.
     *
     * @param incPortNameLookups if true, the well-known port names
     *                              will be included
     * @return the string representation
     */
    private String createString(boolean incPortNameLookups) {
        StringBuilder sb = new StringBuilder();
        sb.append(formattedIpPort(sourceIp, sourcePort));
        if (incPortNameLookups) {
            sb.append(OPEN_P).append(sourcePort.getName()).append(CLOSE_P);
        }
        sb.append(ARROW);
        sb.append(formattedIpPort(destIp, destPort));
        if (incPortNameLookups) {
            sb.append(OPEN_P).append(destPort.getName()).append(CLOSE_P);
        }
        return sb.toString();
    }

    private String formattedIpPort(IpAddress ip, TcpUdpPort port) {
        StringBuilder sb = new StringBuilder();
        sb.append(ip.toStringWithPort(port.getNumber()));
        sb.append("/").append(port.getProtocolName());
        return sb.toString();
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      Default serialization is used for source/destination,
    //      IP address/port.

    private Object readResolve() throws ObjectStreamException {
        // when this is called, sourceIp/sourcePort/destIp/destPort have
        // been populated. return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(sourceIp, sourcePort, destIp, destPort);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }


    //=== Public API ==========================================================

    /** Returns the IP address of the source.
     *
     * @return the source IP address
     */
    public IpAddress getSourceIp() {
        return sourceIp;
    }

    /** Returns the port of the source.
     *
     * @return the source port
     */
    public TcpUdpPort getSourcePort() {
        return sourcePort;
    }

    /** Returns the IP address of the destination.
     *
     * @return the destination IP address
     */
    public IpAddress getDestinationIp() {
        return destIp;
    }

    /** Returns the port of the destination.
     *
     * @return the destination port
     */
    public TcpUdpPort getDestinationPort() {
        return destPort;
    }


    /** Returns the string representation of this port pair, including the
     * well-known names of the ports.
     *
     * @return the string representation
     * @see #toShortString
     */
    @Override
    public String toString() {
        synchronized (this) {
            if (asString == null) {
                asString = createString(true);
            }
            return asString;
        }
    }

    /** Returns the string representation of this port pair, without including
     * the well-known names of the ports.
     *
     * @return the short string representation
     * @see #toString
     */
    public String toShortString() {
        synchronized (this) {
            if (asShortString == null) {
                asShortString = createString(false);
            }
            return asShortString;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TcpUdpPortPair other = (TcpUdpPortPair) o;

        return sourceIp.equals(other.sourceIp) &&
                sourcePort.equals(other.sourcePort) &&
                destIp.equals(other.destIp) &&
                destPort.equals(other.destPort);
    }


    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    /** This implementation of the {@code Comparable} interface sorts by
     * source IP/port first, then destination IP/port.
     *
     * @param o the other pair
     * @return a number indicating relative ordering
     */
    @Override
    public int compareTo(TcpUdpPortPair o) {
        int result = sourceIp.compareTo(o.sourceIp);
        if (result == 0) {
            result = sourcePort.compareTo(o.sourcePort);
            if (result == 0) {
                result = destIp.compareTo(o.destIp);
                if (result == 0) {
                    result = destPort.compareTo(o.destPort);
                }
            }
        }
        return result;
    }

    //=== PRIVATE static methods ==============================================

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, TcpUdpPortPair> cachedPairs =
                     new WeakValueCache<String, TcpUdpPortPair>(getRefQ());

    /** Ensures that all equivalent port pairs map to the same instance.
     *
     * @param pp a newly constructed TcpUdpPortPair (which may get dropped)
     * @return a reference to the appropriate unique instance
     */
    private static TcpUdpPortPair intern(TcpUdpPortPair pp) {
        synchronized (cachedPairs) {
            final String key = pp.toShortString();
            TcpUdpPortPair alreadyCached = cachedPairs.get(key);
            TcpUdpPortPair keeper = alreadyCached != null ? alreadyCached : pp;
            cachedPairs.put(key, keeper);
            return keeper;
        }
    }

    //=== PUBLIC static API ===================================================

    /** Returns a {@code TcpUdpPortPair} object that represents the source and
     * destination ports specified by the given IP addresses and TCP/UDP ports.
     *
     * @param sourceIp the IP address of the source
     * @param sourcePort the TCP/UDP port of the source
     * @param destIp the IP address of the destination
     * @param destPort the TCP/UDP port of the destination
     * @return the source / destination port pair
     * @throws NullPointerException if any of the parameters are null
     * @throws IllegalArgumentException if the port protocols don't match
     */
    public static TcpUdpPortPair valueOf(IpAddress sourceIp,
                                         TcpUdpPort sourcePort,
                                         IpAddress destIp,
                                         TcpUdpPort destPort) {
        return intern(new TcpUdpPortPair(sourceIp, sourcePort,
                                         destIp, destPort));
    }

    // todo: valueOf(IpAddress, TcpUdpPort) for "not defined yet" source ?? (listening port)

}
