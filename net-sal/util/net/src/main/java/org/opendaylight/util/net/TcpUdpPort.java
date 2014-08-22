/*
 * (c) Copyright 2009-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.cache.CacheableDataType;
import org.opendaylight.util.cache.WeakValueCache;

/**
 * Represents a tcp/udp port.
 * <p>
 * All constructors for this class are private. Creating instances
 * of {@code WellKnownPort} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of ports is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class TcpUdpPort extends CacheableDataType
                              implements Comparable<TcpUdpPort> {

    private static final long serialVersionUID = 8850365061963177505L;
    private static final String F_PORT_NUM = "number";
    private static final String F_PROTOCOL_NUM = "protNum";

    /** Defines the fields that will be serialized. */
    private static final ObjectStreamField[] serialPersistentFields = {
            // port number
            new ObjectStreamField(F_PORT_NUM, Integer.TYPE),
            // protocol number (6=TCP, 17=UDP)
            new ObjectStreamField(F_PROTOCOL_NUM, Integer.TYPE),
    };

    private static final ResourceBundle PORT_LOOKUP =
            ResourceUtils.getBundledResource(TcpUdpPort.class, "tcpUdpPorts");

    private static final String COLON = ":";
    private static final String SLASH = "/";
    private static final int MAX_PORT = 65535;
    private static final int UNDETERMINED_PORT_NUMBER = -1;

    // package private (for unit testing)
    static final String UNKNOWN = PORT_LOOKUP.getString("UNKNOWN");
    static final String UNDETERMINED = PORT_LOOKUP.getString("UNDETERMINED");

    /** This is the port number.
     * @serial port number
     */
    private int number;

    /** @serialField protNum int protocol number. */

    /** This is the IP protocol. */
    private transient IpProtocol protocol;

    /** This is the descriptive name for the port.
     * E.g. "qotd : Quote of the Day".
     */
    private transient String name;

    /** This is the short name for the port. E.g. "qotd" */
    private transient String shortName;

    /** This is the port number and protocol specified as a string.
     * E.g. "17/tcp"
     */
    private transient String spec;

    // === PRIVATE CONSTRUCTORS ===============================================

    /**
     * Constructs a tcp/udp port instance from the port number and the
     * protocol. Protocol value has been validated in the static method.
     *
     * @param number the port number
     * @param proto the protocol
     * @see #valueOf
     */
    private TcpUdpPort(int number, IpProtocol proto) {
        this.number = number;
        this.protocol = proto;
        spec = number + SLASH + getProtocolName();
        String s = UNDETERMINED;
        if (number != UNDETERMINED_PORT_NUMBER) {
            try {
                s = PORT_LOOKUP.getString(spec);
            } catch (MissingResourceException e) {
                s = UNKNOWN;
            }
        }
        name = s;
        String[] stuff = s.split(COLON);
        shortName = stuff[0].trim();
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      In serializing this class, we embed the protocol number as an int,
    //      rather than serializing the IpProtocol object. On deserializing,
    //      we have to reverse this.

    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put(F_PORT_NUM, number);
        fields.put(F_PROTOCOL_NUM, protocol.getNumber());
        out.writeFields();
    }

    private void readObject(ObjectInputStream in)
        throws ClassNotFoundException, IOException {

        ObjectInputStream.GetField fields = in.readFields();
        number = fields.get(F_PORT_NUM, -1);
        int protocolNumber = fields.get(F_PROTOCOL_NUM, -1);

        IpProtocol ipp = IpProtocol.valueOf(protocolNumber);
        try {
            validateProtocol(ipp);
        } catch (NullPointerException e) {
            throw new IOException(E_PROT_NUM + protocolNumber, e);
        } catch (IllegalArgumentException e) {
            throw new IOException(E_PROT_NUM + protocolNumber, e);
        }

        // protocol is ok
        protocol = ipp;
    }

    private static final String E_PROT_NUM = "Invalid IP Protocol number : ";

    private Object readResolve() throws ObjectStreamException {
        // When this is called, we have the port number and protocol set
        // in our instance. Return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(number, protocol);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    // === PUBLIC instance API ================================================

    /** Returns a string consisting of the port number followed by
     * a slash character followed by the protocol.
     * E.g. "17/tcp"
     *
     * @return the port number and protocol as a string
     */
    @Override
    public String toString() {
        return spec;
    }

    /** Returns the descriptive name of the port.
     * E.g. "qotd : Quote of the Day"
     *
     * @return the descriptive name
     */
    public String getName() {
        return name;
    }

    /** Returns the short name of the port.
     * E.g. "qotd"
     *
     * @return the short name
     */
    public String getShortName() {
        return shortName;
    }


    /** Returns the port number.
     *
     * @return the port number
     */
    public int getNumber() {
        return number;
    }

    /** Returns the IP Protocol.
     *
     * @return the protocol
     */
    public IpProtocol getProtocol() {
        return protocol;
    }

    /** Returns the short name of the protocol in lowercase. For example,
     * "tcp" or "udp".
     *
     * @return the short name of the protocol
     */
    public String getProtocolName() {
        return protocol.getShortName().toLowerCase(Locale.getDefault());
    }

    /** Returns true if this instance of port number
     * represents an "undetermined" port.
     *
     * @return true if an "undetermined" port
     */
    public boolean isUndetermined() {
        return number == UNDETERMINED_PORT_NUMBER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return number == ((TcpUdpPort)o).number &&
               protocol == ((TcpUdpPort)o).protocol;
    }

    @Override
    public int hashCode() {
        return 31 * number + protocol.hashCode();
    }

    /** Implements the Comparable interface, to return ports in a natural order.
     * Sorted by number first, then by protocol.
     *
     * @param o the other port to compare to
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(TcpUdpPort o) {
        int result = this.number - o.number;
        return (result == 0) ? protocol.compareTo(o.protocol) : result;
    }

    // === STATIC methods =====================================================

    /** Regular expression matching  "nnnn/tcp" or "nnnn/udp" */
    private static final Pattern RE_PORTSPEC =
            Pattern.compile("^(\\d+)\\/(tcp|udp)$");

    /** Our self-trimming cache */
    private static final WeakValueCache<String, TcpUdpPort> cachedPorts =
                     new WeakValueCache<String, TcpUdpPort>(getRefQ());

    /** Ensures that all equivalent tcp/udp port encoding keys
     * map to the same instance of TcpUdpPort.
     * <p>
     * Note that this method is always called from inside
     * a block synchronized on (@link #cachedPorts}
     *
     * @param port a newly constructed TcpUdpPort (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique TcpUdpPort instance
     */
    private static TcpUdpPort intern(TcpUdpPort port, String key) {
        TcpUdpPort alreadyCached = cachedPorts.get(port.spec);
        TcpUdpPort keeper = alreadyCached != null ? alreadyCached : port;
        cachedPorts.put(port.spec, keeper); // cached by normalized string rep
        cachedPorts.put(key, keeper); // cached by given key
        return keeper;
    }

    /** Validates the protocol. If the specified parameter is
     * {@code IpProtocol.TCP} or {@code IpProtocol.UDP} the method
     * silently returns; otherwise it throws an exception.
     *
     * @param ipp the protocol
     * @throws NullPointerException if protocol is null
     * @throws IllegalArgumentException if protocol is not TCP or UDP
     */
    private static void validateProtocol(IpProtocol ipp) {
        if (ipp == null)
            throw new NullPointerException("protocol cannot be null");
        if (!ipp.equals(IpProtocol.TCP) && !ipp.equals(IpProtocol.UDP))
            throw new IllegalArgumentException(
                    "protocol must be TCP or UDP: " + ipp);
    }

    /** Validates the port number. If the specified parameter is in the
     * range -1 .. 65535, the method silently returns; otherwise it throws
     * an exception. Note that -1 is used to indicate an "undetermined" port
     * number, and is thus a valid value.
     *
     * @param portNum the port number
     * @throws IllegalArgumentException  if the port number is
     *          &lt; -1 or &gt; 65535
     */
    private static void validatePortNumber(int portNum) {
        if (portNum < -1 || portNum > MAX_PORT)
            throw new IllegalArgumentException("bad port number: " + portNum);
    }


    // === PUBLIC static API ==================================================
    /** A value representing an undetermined TCP port. */
    public static final TcpUdpPort UNDETERMINED_TCP =
            valueOf(UNDETERMINED_PORT_NUMBER, IpProtocol.TCP);

    /** A value representing an undetermined UDP port. */
    public static final TcpUdpPort UNDETERMINED_UDP =
            valueOf(UNDETERMINED_PORT_NUMBER, IpProtocol.UDP);

    /** Returns an object that represents the value of the tcp/udp port
     * identified by the specified string. The format of the string should
     * be {@code "nnnn/ppp"} where 'nnnn' is the port number, and 'ppp' is
     * the protocol: either {@code "tcp"} or {@code "udp"}. For example:
     * <pre>
     *   TcpUdpPort p = TcpUdpPort.valueOf("7/tcp");
     * </pre>
     *
     * @param value the port specification
     * @return an object representing the specified port / protocol combination
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if the string is incorrectly formatted
     */
    public static TcpUdpPort valueOf(String value) {
        if (value == null)
            throw new NullPointerException("value cannot be null");

        Matcher m = RE_PORTSPEC.matcher(value.toLowerCase(Locale.getDefault()));
        if (!m.matches())
            throw new IllegalArgumentException(
                    "Bad port spec (not 'nnnn/(tcp|udp)': " + value);

        int portNum = Integer.valueOf(m.group(1));
        IpProtocol ipp = IpProtocol.valueOf(m.group(2));
        return valueOf(portNum, ipp);
    }


    /** Returns an object that represents the value of the tcp/udp port
     * identified by the specified port number and protocol.  For example:
     * <pre>
     *   TcpUdpPort p = TcpUdpPort.valueOf(7, IpProtocol.TCP)
     * </pre>
     *
     * @param portNumber the port number
     * @param protocol the protocol (either {@code IpProtocol.TCP} or
     *          {@code IpProtocol.UDP})
     * @return an object representing the specified port / protocol combination
     * @throws NullPointerException if protocol is null
     * @throws IllegalArgumentException if protocol is not TCP or UDP, or if
     *          the port number is &lt; 0 or &gt; 65535
     */
    public static TcpUdpPort valueOf(int portNumber, IpProtocol protocol) {
        validatePortNumber(portNumber);
        validateProtocol(protocol);

        final String pStr = protocol.getShortName()
                                    .toLowerCase(Locale.getDefault());
        final String key = portNumber + SLASH + pStr;

        synchronized (cachedPorts) {
            TcpUdpPort result = cachedPorts.get(key);
            return (result == null) ?
                    intern(new TcpUdpPort(portNumber, protocol), key) : result;
        }
    }

    /** Delegates to {@link #valueOf(int,IpProtocol)}, specifying the
     * protocol to be TCP.
     *
     * @param portNumber the port number
     * @return an object representing the TCP port with the specified number
     * @throws IllegalArgumentException if the port number
     *          is &lt; 0 or &gt; 65535
     */
    public static TcpUdpPort tcpPort(int portNumber) {
        return valueOf(portNumber, IpProtocol.TCP);
    }

    /** Delegates to {@link #valueOf(int,IpProtocol)}, specifying the
     * protocol to be UDP.
     *
     * @param portNumber the port number
     * @return an object representing the UDP port with the specified number
     * @throws IllegalArgumentException if the port number
     *          is &lt; 0 or &gt; 65535
     */
    public static TcpUdpPort udpPort(int portNumber) {
        return valueOf(portNumber, IpProtocol.UDP);
    }
}
