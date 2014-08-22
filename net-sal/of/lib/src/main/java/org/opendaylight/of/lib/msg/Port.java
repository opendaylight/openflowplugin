/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.PrimitiveUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.PortNumber;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.PortConfig.PORT_DOWN;
import static org.opendaylight.of.lib.msg.PortFactory.E_BAD_PORT_NUM;
import static org.opendaylight.of.lib.msg.PortState.*;
import static org.opendaylight.util.net.BigPortNumber.bpn;

/**
 * Represents physical ports, switch-defined logical ports, and OpenFlow
 * specified reserved ports.
 * <p>
 * Instances of this class are immutable.
 * <p>
 * In OpenFlow, ports are numbered starting from 1. The maximum legal value
 * for an individual port is {@link #MAX} (but see below).
 * Reserved OpenFlow Port numbers (fake output "ports") are also defined
 * on this class:
 * <ul>
 *     <li>{@link #IN_PORT} - Send packet out the input port.</li>
 *     <li>{@link #TABLE} - Submit the packet to the first flow table.</li>
 *     <li>{@link #NORMAL} - Process with normal L2/L3 switching.</li>
 *     <li>{@link #FLOOD} - All physical ports in VLAN (with exceptions).</li>
 *     <li>{@link #ALL} - All physical ports except input port.</li>
 *     <li>{@link #CONTROLLER} - Send to the controller.</li>
 *     <li>{@link #LOCAL} - Local openflow "port".</li>
 *     <li>{@link #ANY} - Wildcard port used only for flow mod (delete) and
 *          flow stats requests.</li>
 * </ul>
 * <p>
 * Note that in OpenFlow version 1.0, port numbers were defined as
 * unsigned 16-bit (u16) values; Since version 1.1 port numbers have been
 * defined as unsigned 32-bit (u32) values. Since this class represents
 * ports spanning all versions of the protocol, the port number is expressed
 * using the {@link BigPortNumber} class. When creating a version 1.0 port,
 * the value of the port number is validated as u16. Additionally, when
 * encoding the "special" values (listed above as {@code MAX}, and the
 * <em>Reserved</em> values), their u16 counterparts are substituted at the
 * time the port structure is encoded as bytes.
 *
 *
 * @see PortFactory
 * @author Simon Hunt
 * @author Liem Nguyen
 */
public class Port extends OpenflowStructure {

    private static final long NA_U32 = 0;

    BigPortNumber portNumber;
    MacAddress hwAddress;
    String name;
    Set<PortConfig> config;
    Set<PortState> state;
    Set<PortFeature> current;
    Set<PortFeature> advertised;
    Set<PortFeature> supported;
    Set<PortFeature> peer;
    long currentSpeed = NA_U32;
    long maxSpeed = NA_U32;

    /**
     * Constructs an OpenFlow port structure.
     *
     * @param pv the protocol version
     * @throws NullPointerException if the parameter is null
     */
    Port(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{port(").append(version.name())
                .append("):")
                    .append(Port.portNumberToString(portNumber, version))
                .append(",hw=").append(hwAddress)
                .append(",name='").append(name)
                .append("',cfg=").append(config)
                .append(",st=").append(state)
                .append(",cur=").append(current)
                .append(",...");
        if (version.ge(ProtocolVersion.V_1_1))
            sb.append(",curS=").append(currentSpeed)
                .append(",maxS=").append(maxSpeed);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Returns a multi-line representation of this port.
     * Useful for debug output.
     *
     * @return a multi-line representation
     */
    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /**
     * Returns a multi-line representation of this port.
     * Useful for debug output.
     *
     * @param indent depth of indent
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in2 = StringUtils.spaces(indent + 2);
        final String eoli2 = EOLI + in2;
        StringBuilder sb = new StringBuilder()
                .append("Port(")
                .append(version.name()).append("): Number: ")
                    .append(Port.portNumberToString(portNumber, version))
                .append(eoli2).append("HW Address: ").append(hwAddress)
                .append(eoli2).append("Name: '").append(name).append("'")
                .append(eoli2).append("Config: ").append(config)
                .append(eoli2).append("State: ").append(state)
                .append(eoli2).append("Current features: ").append(current)
                .append(eoli2).append("Advertised features: ").append(advertised)
                .append(eoli2).append("Supported features: ").append(supported)
                .append(eoli2).append("Peer features: ").append(peer);
        if (version.ge(ProtocolVersion.V_1_1))
            sb.append(eoli2).append("Current Speed: ").append(currentSpeed)
                    .append(" kbit/s")
                    .append(eoli2).append("Maximum Speed: ").append(maxSpeed)
                    .append(" kbit/s");
        sb.append(EOLI);
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(portNumber, hwAddress);
    }

    /**
     * The port number uniquely identifies a port within a switch; Since 1.0.
     *
     * @return the port number
     */
    public BigPortNumber getPortNumber() {
        return portNumber;
    }

    /**
     * Returns the decimal number of this port, unless this is a "special"
     * reserved number, in which case the logical name for the port is
     * returned instead.
     *
     * @return the port number (or logical name, if defined)
     */
    public String getLogicalNumber() {
        return getLogicalNumber(portNumber, version);
    }

    /**
     * Returns the MAC address for the port; Since 1.0.
     *
     * @return the MAC address
     */
    public MacAddress getHwAddress() {
        return hwAddress;
    }

    /**
     * Returns the human readable name for the interface; Since 1.0.
     * Limited to 15 characters.
     *
     * @return the port name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the port administrative settings.
     *
     * @return the current configuration settings
     */
    public Set<PortConfig> getConfig() {
        return config == null ? null : Collections.unmodifiableSet(config);
    }

    /**
     * Returns the state of the physical link or switch protocols
     * outside of OpenFlow; Since 1.0.
     *
     * @return the port state
     */
    public Set<PortState> getState() {
        return state == null ? null : Collections.unmodifiableSet(state);
    }

    /**
     * Returns the set of current features; Since 1.0.
     *
     * @return the set of current features
     */
    public Set<PortFeature> getCurrent() {
        return current == null ? null : Collections.unmodifiableSet(current);
    }

    /**
     * Returns the set of features advertised by the port; Since 1.0.
     *
     * @return the set of advertised features
     */
    public Set<PortFeature> getAdvertised() {
        return advertised == null ? null :
                Collections.unmodifiableSet(advertised);
    }

    /**
     * Returns the set of features supported by the port; Since 1.0.
     *
     * @return the set of supported features
     */
    public Set<PortFeature> getSupported() {
        return supported == null ? null :
                Collections.unmodifiableSet(supported);
    }

    /**
     * Returns the set of features advertised by the peer; Since 1.0.
     *
     * @return the peer advertised features
     */
    public Set<PortFeature> getPeer() {
        return peer == null ? null : Collections.unmodifiableSet(peer);
    }

    /**
     * Returns the current port speed (bitrate) in kbps; Since 1.1.
     *
     * @return current port speed (kbps)
     */
    public long getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * Returns the maximum port speed (bitrate) in kbps; Since 1.1.
     *
     * @return maximum port speed (kbps)
     */
    public long getMaxSpeed() {
        return maxSpeed;
    }


    // === Convenience Methods

    /**
     * Returns true if the port is administratively enabled. This is a
     * convenience method that checks for the absence of the
     * {@link PortConfig#PORT_DOWN PORT_DOWN} configuration flag.
     *
     * @see #getConfig()
     *
     * @return true if the port is enabled
     */
    public boolean isEnabled() {
        return config != null && !config.contains(PORT_DOWN);
    }

    /**
     * Returns true if this port's link is up. This is a
     * convenience method that checks for the absence of the
     * {@link PortState#LINK_DOWN LINK_DOWN} state flag.
     *
     * @see #getState()
     *
     * @return true if the port's link is up
     */
    public boolean isLinkUp() {
        // TODO: Review - make sure State is included soon after handshake
        return state != null && !state.contains(LINK_DOWN);
    }

    /**
     * Returns true if this port is prevented from being used for flooding.
     * <p>
     * The "blocked" flag indicates that a switch protocol outside of
     * OpenFlow, such as 802.1D Spanning Tree, is preventing the use of
     * the port with {@link Port#FLOOD}.
     * <p>
     * This convenience method checks for the presence of the
     * {@link PortState#STP_BLOCK STP_BLOCK} or
     * {@link PortState#BLOCKED BLOCKED} state flag, depending on the
     * protocol version.
     *
     * @return true if the port is blocked
     */
    public boolean isBlocked() {
        return state != null &&
                state.contains(version == V_1_0 ? STP_BLOCK : BLOCKED);
    }

    // === RESERVED BIG PORT NUMBERS ===

    /**
     * Maximum number of physical and logical switch ports; Since 1.0.
     * <p>
     * Note: in 1.0, this value is {@code 0xff00}.
     */
    public static final BigPortNumber MAX = bpn(0xffffff00L);

    /**
     * Send the packet out the input port; Since 1.0. This reserved port
     * must be explicitly used in order to send back out of the input port.
     * <p>
     * Note: in 1.0, this value is {@code 0xfff8}.
     */
    public static final BigPortNumber IN_PORT = bpn(0xfffffff8L);

    /**
     * Submit the packet to the first flow table; Since 1.0. NB: This
     * destination port can only be used in packet-out messages.
     * <p>
     * Note: in 1.0, this value is {@code 0xfff9}.
     */
    public static final BigPortNumber TABLE = bpn(0xfffffff9L);

    /**
     * Process with normal L2/L3 switching; Since 1.0.
     * <p>
     * Note: in 1.0, this value is {@code 0xfffa}.
     */
    public static final BigPortNumber NORMAL = bpn(0xfffffffaL);

    /**
     * All physical ports in VLAN, except input port and those blocked
     * or link down; Since 1.0.
     * <p>
     * Note: in 1.0, this value is {@code 0xfffb}.
     */
    public static final BigPortNumber FLOOD = bpn(0xfffffffbL);

    /**
     * All physical ports except input port; Since 1.0.
     * <p>
     * Note: in 1.0, this value is {@code 0xfffc}.
     */
    public static final BigPortNumber ALL = bpn(0xfffffffcL);

    /**
     * Send to the Controller; Since 1.0.
     * <p>
     * Note: in 1.0, this value is {@code 0xfffd}.
     */
    public static final BigPortNumber CONTROLLER = bpn(0xfffffffdL);

    /**
     * Local openflow "port"; Since 1.0.
     * <p>
     * Note: in 1.0, this value is {@code 0xfffe}.
     */
    public static final BigPortNumber LOCAL = bpn(0xfffffffeL);

    /**
     * Wildcard port used only for flow mod (delete) and flow stats
     * requests; Since 1.1. Selects all flows regardless of output port
     * (including flows with no output port).
     * <p>
     * See {@link #NONE} for 1.0.
     */
    public static final BigPortNumber ANY = bpn(0xffffffffL);

    /**
     * Not associated with a physical port; Since 1.0.
     * <p>
     * Note: in 1.0, this value is {@code 0xffff}.
     * <p>
     * See {@link #ANY} for 1.1 and higher.
     */
    public static final BigPortNumber NONE = ANY;


    // === RESERVED PORT NUMBERS ===
    /*
     * IMPLEMENTATION NOTE:
     *  The u16 port numbers (supporting 1.0) are an implementation detail
     *  that are deliberately hidden from the API; i.e. not public.
     */

    /** Maximum number of physical switch ports; v1.0. */
    static final PortNumber MAX_V10 = PortNumber.valueOf(0xff00);

    /**
     * Send the packet out the input port; v1.0. This virtual port
     * must be explicitly used in order to send back out of the input port.
     */
    static final PortNumber IN_PORT_V10 = PortNumber.valueOf(0xfff8);

    /**
     * Performs actions in the flow table; v1.0. NB: This can only be the
     * destination port for packet-out messages.
     */
    static final PortNumber TABLE_V10 = PortNumber.valueOf(0xfff9);

    /** Process with normal L2/L3 switching; v1.0. */
    static final PortNumber NORMAL_V10 = PortNumber.valueOf(0xfffa);

    /** All physical ports except input port and those disabled by STP; v1.0. */
    static final PortNumber FLOOD_V10 = PortNumber.valueOf(0xfffb);

    /** All physical ports except input port; v1.0. */
    static final PortNumber ALL_V10 = PortNumber.valueOf(0xfffc);

    /** Send to controller; v1.0. */
    static final PortNumber CONTROLLER_V10 = PortNumber.valueOf(0xfffd);

    /** Local openflow "port"; v1.0. */
    static final PortNumber LOCAL_V10 = PortNumber.valueOf(0xfffe);

    /** Not associated with a physical port; v1.0. */
    static final PortNumber NONE_V10 = PortNumber.valueOf(0xffff);

    /** Optimize capacity of lookup maps. */
    private static final int NUM_RESERVED_PORTS = 9;

    /** Reserved port number to logical name lookup. */
    private static final Map<BigPortNumber, String> RESERVED_MAP =
            new HashMap<BigPortNumber, String>(NUM_RESERVED_PORTS);
    static {
        RESERVED_MAP.put(MAX, "MAX");
        RESERVED_MAP.put(IN_PORT, "IN_PORT");
        RESERVED_MAP.put(TABLE, "TABLE");
        RESERVED_MAP.put(NORMAL, "NORMAL");
        RESERVED_MAP.put(FLOOD, "FLOOD");
        RESERVED_MAP.put(ALL, "ALL");
        RESERVED_MAP.put(CONTROLLER, "CONTROLLER");
        RESERVED_MAP.put(LOCAL, "LOCAL");
        RESERVED_MAP.put(ANY, "ANY");
    }
    /** Logical name to reserved port lookup. */
    private static final Map<String, BigPortNumber> RESERVED_PAM = 
            new HashMap<String, BigPortNumber>(NUM_RESERVED_PORTS);
    static {
        RESERVED_PAM.put("MAX", MAX);
        RESERVED_PAM.put("IN_PORT", IN_PORT);
        RESERVED_PAM.put("TABLE", TABLE);
        RESERVED_PAM.put("NORMAL", NORMAL);
        RESERVED_PAM.put("FLOOD", FLOOD);
        RESERVED_PAM.put("ALL", ALL);
        RESERVED_PAM.put("CONTROLLER", CONTROLLER);
        RESERVED_PAM.put("LOCAL", LOCAL);
        RESERVED_PAM.put("ANY", ANY);
        RESERVED_PAM.put("NONE", NONE);
    }

    /** Reserved port number to logical name lookup (v1.0 values). */
    private static final Map<PortNumber, String> RESERVED_V10_MAP =
            new HashMap<PortNumber, String>(NUM_RESERVED_PORTS);
    static {
        RESERVED_V10_MAP.put(MAX_V10, "MAX");
        RESERVED_V10_MAP.put(IN_PORT_V10, "IN_PORT");
        RESERVED_V10_MAP.put(TABLE_V10, "TABLE");
        RESERVED_V10_MAP.put(NORMAL_V10, "NORMAL");
        RESERVED_V10_MAP.put(FLOOD_V10, "FLOOD");
        RESERVED_V10_MAP.put(ALL_V10, "ALL");
        RESERVED_V10_MAP.put(CONTROLLER_V10, "CONTROLLER");
        RESERVED_V10_MAP.put(LOCAL_V10, "LOCAL");
        RESERVED_V10_MAP.put(NONE_V10, "NONE");
    }

    private static final Map<PortNumber, BigPortNumber> PN_2_BPN =
            new HashMap<PortNumber, BigPortNumber>(NUM_RESERVED_PORTS);
    static {
        PN_2_BPN.put(MAX_V10, MAX);
        PN_2_BPN.put(IN_PORT_V10, IN_PORT);
        PN_2_BPN.put(TABLE_V10, TABLE);
        PN_2_BPN.put(NORMAL_V10, NORMAL);
        PN_2_BPN.put(FLOOD_V10, FLOOD);
        PN_2_BPN.put(ALL_V10, ALL);
        PN_2_BPN.put(CONTROLLER_V10, CONTROLLER);
        PN_2_BPN.put(LOCAL_V10, LOCAL);
        PN_2_BPN.put(NONE_V10, NONE);
    }

    private static final Map<BigPortNumber, PortNumber> BPN_2_PN =
            new HashMap<BigPortNumber, PortNumber>(NUM_RESERVED_PORTS);
    static {
        BPN_2_PN.put(MAX, MAX_V10);
        BPN_2_PN.put(IN_PORT, IN_PORT_V10);
        BPN_2_PN.put(TABLE, TABLE_V10);
        BPN_2_PN.put(NORMAL, NORMAL_V10);
        BPN_2_PN.put(FLOOD, FLOOD_V10);
        BPN_2_PN.put(ALL, ALL_V10);
        BPN_2_PN.put(CONTROLLER, CONTROLLER_V10);
        BPN_2_PN.put(LOCAL, LOCAL_V10);
        BPN_2_PN.put(NONE, NONE_V10);
    }

    /**
     * Returns a string representation of the port number, for the given
     * protocol version.
     * Reserved values will include the logical name of the port.
     *
     * @param port the port number
     * @param pv the protocol version
     * @return a string representation of the port number
     */
    public static String portNumberToString(BigPortNumber port,
                                            ProtocolVersion pv) {
        if (port == null)
            return NULL_REP;
        if (pv == V_1_0) {
            PortNumber special = equivalentSpecial(port);
            PortNumber pn = special != null ? special
                    : PortNumber.valueOf((int) port.toLong());
            return portNumberToString(pn);
        }
        StringBuilder sb = new StringBuilder(port.toString());
        String name = RESERVED_MAP.get(port);
        sb.append("(").append(name != null ? name : port.toLong()).append(")");
        return sb.toString();
    }

    /**
     * Returns a string representation of the port number. Reserved values
     * will include the logical name of the port.
     *
     * @param port the port number
     * @return a string representation of the port number
     */
    public static String portNumberToString(BigPortNumber port) {
        return portNumberToString(port, V_1_3);
    }

    /**
     * Returns a string representation of the port number. Reserved values
     * will include the logical name of the port.
     *
     * @param port the port number
     * @return a string representation of the port number
     */
    public static String portNumberToString(PortNumber port) {
        if (port == null)
            return NULL_REP;

        StringBuilder sb = new StringBuilder(hex(port.toInt()));
        String name = RESERVED_V10_MAP.get(port);
        sb.append("(").append(name != null ? name : port.toInt()).append(")");
        return sb.toString();
    }

    /**
     * Returns the 1.3 "special" big port number (u32) equivalent to the
     * given 1.0 "special" port number (u16).
     * This will be null if the given port number is not "special".
     * <p>
     * For example:
     * <pre>
     *     BigPortNumber max = Port.equivalentSpecial(Port.MAX_V10);
     *     assert(Port.MAX.equals(max));
     * </pre>
     *
     * @param pn the 1.0 special port number
     * @return the equivalent 1.3 special big port number; or null
     */
    public static BigPortNumber equivalentSpecial(PortNumber pn) {
        return PN_2_BPN.get(pn);
    }

    /**
     * Returns the 1.0 "special" port number (u16) equivalent to the
     * given 1.3 "special" big port number (u32).
     * This will be null if the given port number is not "special".
     * <p>
     * For example:
     * <pre>
     *     PortNumber max = Port.equivalentSpecial(Port.MAX);
     *     assert(Port.MAX_V10.equals(max));
     * </pre>
     *
     * @param bpn the 1.3 special big port number
     * @return the equivalent 1.0 special port number; or null
     */
    public static PortNumber equivalentSpecial(BigPortNumber bpn) {
        return BPN_2_PN.get(bpn);
    }

    /**
     * Validates that the given port number is an acceptable value
     * for encoding as a port number for the given protocol.
     * If all is good, returns silently; else throws an exception.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param bpn the big port number to validate
     * @param pv the protocol version
     * @throws IllegalArgumentException if the port number is invalid for
     *          the specified version
     */
    public static void validatePortValue(BigPortNumber bpn,
                                         ProtocolVersion pv) {
        if (pv == V_1_0) {
            int u16 = (int) bpn.toLong();
            PortNumber special = Port.equivalentSpecial(bpn);
            if (special == null)
                PrimitiveUtils.verifyU16(u16);
            // check for invalid values (v > MAX && v < IN_PORT)
            if (u16 > Port.MAX_V10.toInt() && u16 < Port.IN_PORT_V10.toInt())
                throw new IllegalArgumentException(pv + E_BAD_PORT_NUM +
                                                    hex(u16));
        } else {
            // check for invalid values (v > MAX && v < IN_PORT)
            long pn = bpn.toLong();
            if (pn > Port.MAX.toLong() && pn < Port.IN_PORT.toLong())
                throw new IllegalArgumentException(pv + E_BAD_PORT_NUM +
                                                    hex(pn));
        }
    }

    /**
     * Returns true if the given port number is in the range
     * 1 to <em>MAX</em>; false otherwise.
     *
     * @param bpn the port number
     * @param pv the protocol version
     * @return true if the port number is in the range 1 to <em>MAX</em>
     */
    public static boolean isStandardPort(BigPortNumber bpn,
                                         ProtocolVersion pv) {
        if (pv == V_1_0) {
            // allow Port.MAX (which is actually a u32 value)
            if (Port.MAX.equals(bpn))
                return true;
            
            int u16 = (int) bpn.toLong();
            return u16 >= 1 && u16 <= Port.MAX_V10.toInt();
        }
        long n = bpn.toLong();
        return n >= 1 && n <= Port.MAX.toLong();
    }

    /**
     * Returns the logical name of the port, or null if the port number is
     * not a special value (for the given protocol version).
     *
     * @param port the port number
     * @param pv the protocol version
     * @return the logical name of the port (or null)
     */
    public static String logicalName(BigPortNumber port, ProtocolVersion pv) {
        if (port == null)
            return null;
        if (pv == V_1_0) {
            PortNumber special = equivalentSpecial(port);
            return RESERVED_V10_MAP.get(special);
        }
        return RESERVED_MAP.get(port);
    }

    /**
     * Returns either the logical name of the port, if it is a special value,
     * or the port number as a string.
     *
     * @param bpn the port number
     * @param pv the protocol version
     * @return the logical name or number of the port
     */
    public static String getLogicalNumber(BigPortNumber bpn, ProtocolVersion pv) {
        String result = logicalName(bpn, pv);
        if (result == null)
            result = Long.toString(bpn.toLong());
        return result;
    }

    /**
     * Returns the port, given its string representation, which can include a
     * logical name or the numeric representation of the port.
     * 
     * @param portNumber string representation of the port
     * @return port number, or null if portNumber is null or empty
     */
    public static BigPortNumber getBigPortNumber(String portNumber) {
        if (portNumber == null || portNumber.isEmpty())
            return null;
        BigPortNumber special = RESERVED_PAM.get(portNumber.toUpperCase());
        return (special != null) ? special : bpn(portNumber);
    }
    
    /**
     * Returns the port, given its string representation, which can include a
     * logical name or the numeric representation of the port.
     * 
     * @param portNumber string representation of the port
     * @return port number, or null if portNumber is null or empty
     */
    public static PortNumber getPortNumber(String portNumber) {
        if (portNumber == null || portNumber.isEmpty())
            return null;
        BigPortNumber special = RESERVED_PAM.get(portNumber.toUpperCase());
        return (special != null) ? equivalentSpecial(special) : PortNumber
                .valueOf(portNumber);
    }
}
