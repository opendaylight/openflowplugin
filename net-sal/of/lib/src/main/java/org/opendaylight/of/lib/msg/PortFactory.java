/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.util.PrimitiveUtils;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.PortNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;

/**
 * Provides facilities for parsing, creating, and encoding {@link Port}
 * instances.
 *
 * @author Simon Hunt
 */
public class PortFactory extends AbstractFactory {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            PortFactory.class, "portFactory");

    /** Exception message for invalid port number. */
    public static final String E_BAD_PORT_NUM = RES.getString("e_bad_port_num");

    static final int LIB_PORT_V123 = 64;
    static final int LIB_PORT_V0 = 48;

    private static final int PAD_1_LEN = 4;
    private static final int PAD_2_LEN = 2;

    static final int NAME_FIELD_LEN = 16;

    private static final PortFactory PF = new PortFactory();

    // no instantiation except here.
    private PortFactory() {}

    /** Returns an identifying tag for the port factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "PF";
    }

    // ========================================================= PARSING ====

    /** Parses a list of port structures from the supplied buffer.
     * It is assumed that buffer has been marked with the target index
     * so we know when to stop.
     *  <p>
     *  Note that this method causes the reader index of the underlying
     *  packet reader to be advanced by the length of the list,
     *  which should leave the reader index at the target index.
     *  <p>
     *  This method delegates to {@link #parsePort} for each individual port.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed ports
     * @throws MessageParseException if unable to parse the structure
     */
    public static List<Port> parsePortList(OfPacketReader pkt,
                                           ProtocolVersion pv)
            throws MessageParseException {
        List<Port> portList = new ArrayList<Port>();
        final int targetRi = pkt.targetIndex();
        while(pkt.ri() < targetRi) {
            Port p = parsePort(pkt, pv);
            portList.add(p);
        }
        return portList;
    }

    /** Parses the given packet buffer as a port structure.
     *
     * @param pkt the packet buffer
     * @param pv the protocol version
     * @return the parsed port instance
     * @throws MessageParseException if a problem parsing the buffer
     */
    public static Port parsePort(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        Port port = new Port(pv);
        try {
            port.portNumber = parsePortNumber(pkt, pv);
            if (pv.gt(V_1_0))
                pkt.skip(PAD_1_LEN);
            port.hwAddress = pkt.readMacAddress();
            if (pv.gt(V_1_0))
                pkt.skip(PAD_2_LEN);

            port.name = pkt.readString(NAME_FIELD_LEN);
            port.config = PortConfig.decodeBitmap(pkt.readInt(), pv);
            port.state = PortState.decodeBitmap(pkt.readInt(), pv);
            port.current = PortFeature.decodeBitmap(pkt.readInt(), pv);
            port.advertised = PortFeature.decodeBitmap(pkt.readInt(), pv);
            port.supported = PortFeature.decodeBitmap(pkt.readInt(), pv);
            port.peer = PortFeature.decodeBitmap(pkt.readInt(), pv);

            if (pv.ge(V_1_1)) {
                port.currentSpeed = pkt.readU32();
                port.maxSpeed = pkt.readU32();
            }
        } catch (VersionMismatchException vme) {
            throw PF.mpe(pkt, vme);
        }
        return port;
    }

    /** Parses a port number from the specified buffer, for the given
     * protocol version. If the version is 1.0, two bytes (u16) are read;
     * if the value is 1.1 or higher, four bytes (u32) are read.
     * If the value parsed is greater than the maximum defined in the
     * OpenFlow specification, an exception is thrown.
     * <p>
     * Note that, for v1.0, "special" port numbers are translated into
     * their u32 counterparts and returned.
     *
     * @param pkt the data buffer from which the port is to be read
     * @param pv the protocol version
     * @return the parsed port number
     * @throws IllegalArgumentException if the parsed port is not valid for
     *          the specified protocol version
     *
     * @see Port
     */
    public static BigPortNumber parsePortNumber(OfPacketReader pkt,
                                                ProtocolVersion pv) {
        BigPortNumber bpn;
        if (pv == V_1_0) {
            int u16 = pkt.readU16();
            PortNumber pn = PortNumber.valueOf(u16);
            BigPortNumber special = Port.equivalentSpecial(pn);
            if (special != null)
                bpn = special;
            else
                bpn = BigPortNumber.valueOf(u16);

            // check for invalid values (v > MAX && v < IN_PORT)
            if (pn.compareTo(Port.MAX_V10) > 0 &&
                    pn.compareTo(Port.IN_PORT_V10) < 0)
                throw new IllegalArgumentException(pv + E_BAD_PORT_NUM +
                        hex(u16));

        } else {
            bpn = pkt.readBigPortNumber();
            // check for invalid values (v > MAX && v < IN_PORT)
            if (bpn.compareTo(Port.MAX) > 0 && bpn.compareTo(Port.IN_PORT) < 0)
                throw new IllegalArgumentException(pv + E_BAD_PORT_NUM +
                        hex(bpn.toLong()));
        }
        return bpn;
    }


    // ======================================================== CREATING ====

    /** Creates a mutable port. Initially, no fields are set.
     *
     * @param pv the required protocol version
     * @return the port structure
     * @throws VersionNotSupportedException if the version is not supported
     */
    public static MutablePort createPort(ProtocolVersion pv) {
        MessageFactory.checkVersionSupported(pv);
        return new MutablePort(pv);
    }

    // ======================================================== ENCODING ====

    /** Encodes the specified port structure into the specified buffer.
     * It is assumed that the writer index is in the correct place for the
     * start of the structure. At the end of this method call, the
     * writer index will have advanced by the length of the encoded port.
     * <p>
     * Note that, for v1.0 ports, "special" port numbers are translated to
     * their u16 counterparts.
     *
     * @param port the port to encode
     * @param pkt the buffer to write into
     * @throws IllegalArgumentException if the port is mutable, or if the
     *  protocol version is 1.0 and the port number is not u16
     * @throws IncompleteStructureException if the port is
     *      missing required fields
     */
    public static void encodePort(Port port, OfPacketWriter pkt)
            throws IncompleteStructureException {
        notMutable(port);
        notNullIncompleteStruct(port.portNumber, port.hwAddress);
        final ProtocolVersion pv = port.getVersion();
        encodePortNumber(port.portNumber, pkt, pv);

        if (pv.gt(V_1_0))
            pkt.writeZeros(PAD_1_LEN);
        pkt.write(port.hwAddress);
        if (pv.gt(V_1_0))
            pkt.writeZeros(PAD_2_LEN);

        pkt.writeString(port.name, NAME_FIELD_LEN);
        pkt.writeInt(PortConfig.encodeBitmap(port.config, pv));
        pkt.writeInt(PortState.encodeBitmap(port.state, pv));
        pkt.writeInt(PortFeature.encodeBitmap(port.current, pv));
        pkt.writeInt(PortFeature.encodeBitmap(port.advertised, pv));
        pkt.writeInt(PortFeature.encodeBitmap(port.supported, pv));
        pkt.writeInt(PortFeature.encodeBitmap(port.peer, pv));

        if (pv.ge(V_1_1)) {
            pkt.writeU32(port.currentSpeed);
            pkt.writeU32(port.maxSpeed);
        }
    }

    /** Encodes the given port number, writing it to the specified buffer,
     * for the given protocol version. If the version is 1.0, two bytes (u16)
     * are written; if the value is 1.1 or higher, four bytes (u32) are written.
     * If the port number is greater than the maximum defined in the
     * OpenFlow specification, an exception is thrown. If the {@code port}
     * argument is null, the bytes are zero-filled.
     * <p>
     * Note that, for v1.0, "special" port numbers are translated to
     * their u16 counterparts.
     *
     * @param port the port number to encode
     * @param pkt the data buffer into which the port is to be written
     * @param pv the protocol version
     *
     * @see Port
     */
    public static void encodePortNumber(BigPortNumber port, OfPacketWriter pkt,
                                        ProtocolVersion pv) {
        if (port == null) {
            pkt.writeZeros(pv == V_1_0 ? PortNumber.LENGTH_IN_BYTES
                                       : BigPortNumber.LENGTH_IN_BYTES);
            return;
        }

        if (pv == V_1_0) {
            int u16 = (int) port.toLong();
            PortNumber special = Port.equivalentSpecial(port);
            if (special != null)
                u16 = special.toInt();
            else
                PrimitiveUtils.verifyU16(u16);

            // check for invalid values (v > MAX && v < IN_PORT)
            if (u16 > Port.MAX_V10.toInt() && u16 < Port.IN_PORT_V10.toInt())
                throw new IllegalArgumentException(pv + E_BAD_PORT_NUM +
                        hex(u16));

            pkt.writeU16(u16);
        } else {
            // check for invalid values (v > MAX && v < IN_PORT)
            if (port.compareTo(Port.MAX) > 0 &&
                    port.compareTo(Port.IN_PORT) < 0)
                throw new IllegalArgumentException(pv + E_BAD_PORT_NUM +
                        hex(port.toLong()));
            pkt.write(port);
        }
    }

    /** Encodes a list of ports, writing them into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the written
     * ports.
     *
     * @param ports the list of ports
     * @param pkt the buffer into which the ports are to be written
     * @throws IncompleteStructureException if any of the ports are
     *      missing required fields
     */
    public static void encodePortList(List<Port> ports, OfPacketWriter pkt)
            throws IncompleteStructureException {
        for (Port port: ports)
            encodePort(port, pkt);
    }

    // ========================================================= COPYING ====

    /** Returns a copy (immutable) of the given port.
     *
     * @param p the original port
     * @return the copy
     */
    public static Port copy(Port p) {
        return MutablePort.makeCopy(p, false);
    }

    /** Returns a mutable copy of the given port.
     *
     * @param p the original port
     * @return the mutable copy
     */
    public static MutablePort mutableCopy(Port p) {
        return (MutablePort) MutablePort.makeCopy(p, true);
    }

    // ======================================================== UTILITIES ===

    /** Returns the length of a port structure in bytes.
     *
     * @param pv the protocol version
     * @return the length in bytes
     */
    public static int getPortLength(ProtocolVersion pv) {
        return pv == V_1_0 ? LIB_PORT_V0 : LIB_PORT_V123;
    }

    /** Outputs a list of ports in debug string format.
     *
     * @param ports the list of ports
     * @return a multi-line string representation of the list of ports
     */
    public static String toDebugString(List<Port> ports) {
        return toDebugString(0, ports);
    }

    /** Outputs a list of ports in debug string format.
     *
     * @param indent the additional indent (number of spaces)
     * @param ports the list of ports
     * @return a multi-line string representation of the list of ports
     */
    public static String toDebugString(int indent, List<Port> ports) {
        if (ports == null)
            return StringUtils.EMPTY;
        final String indStr = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        for (Port p: ports)
            sb.append(indStr).append(p.toDebugString(indent));
        return sb.toString();
    }
}