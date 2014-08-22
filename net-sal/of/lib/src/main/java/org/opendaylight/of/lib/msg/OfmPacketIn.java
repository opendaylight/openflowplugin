/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.IncompleteMessageException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.PacketUtils.packetSummary;

/**
 * Represents an OpenFlow PACKET_IN message; Since 1.0.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfmPacketIn extends OpenflowMessage {

    /** ID assigned by datapath (BufferId.NO_BUFFER if none); since 1.0. */
    BufferId bufferId;
    /** Port on which frame was received; Since 1.0. */
    BigPortNumber inPort;
    /** Physical port on which frame was received; Since 1.1. */
    BigPortNumber inPhyPort;
    /** Full length of frame; Since 1.0. */
    int totalLen;
    /** Reason the packet is being sent; Since 1.0. */
    PacketInReason reason;
    /** ID of the table that was looked up; Since 1.1. */
    TableId tableId;
    /** Cookie of the flow entry that caused the packet to be sent to the
     * controller; Since 1.3.
     */
    long cookie;
    /** Packet metadata. Variable size; Since 1.2. */
    Match match;
    /** Ethernet frame; Since 1.0. */
    byte[] data;

    /**
     * Constructs an OpenFlow PACKET_IN message.
     *
     * @param header the message header
     */
    OfmPacketIn(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        final ProtocolVersion pv = getVersion();
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",inPort=")
                .append(Port.portNumberToString(inPort))
                .append(",reason=").append(reason);
        if (pv.ge(V_1_1))
            sb.append(",tabId=").append(tableId);
        sb.append(",packet=").append(packetSummary(data, bufferId));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        final ProtocolVersion pv = getVersion();
        sb.append(EOLI).append("Buffer ID : ").append(bufferId);
        sb.append(EOLI).append("In Port   : ").append(inPort);
        if (pv.ge(V_1_1))
            sb.append(EOLI).append("In PhyPort: ").append(inPhyPort);
        sb.append(EOLI).append("Total Frame Len : ").append(totalLen);
        sb.append(EOLI).append("Reason: ").append(reason);
        if (pv.ge(V_1_1))
            sb.append(EOLI).append("Table ID: ").append(tableId);
        if (pv.ge(V_1_3))
            sb.append(EOLI).append("cookie: ").append(hex(cookie));
        if (pv.ge(V_1_2))
            sb.append(EOLI).append("Match: ")
                    .append(match == null ? NULL_REP : match.toDebugString(2));
        sb.append(EOLI).append("Frame Data length: ").append(aSize(data));
        sb.append(EOLI).append("Packet=").append(packetSummary(data, bufferId));
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteMessageException {
        notNullIncompleteMsg(bufferId, reason);
    }

    /** ID assigned by datapath (BufferId.NO_BUFFER if none); since 1.0.
     *
     * @return the buffer ID
     */
    public BufferId getBufferId() {
        return bufferId;
    }

    /** Returns the port on which the frame was received; Since 1.0.
     *
     * @return the ingress port
     */
    public BigPortNumber getInPort() {
        return inPort;
    }

    /** Returns the physical port on which the frame was received; Since 1.1.
     *
     * @return the ingress physical port
     */
    public BigPortNumber getInPhyPort() {
        return inPhyPort;
    }

    /** Returns the total length value from the in-port message header;
     * Since 1.0.
     *
     * @return the total length value
     */
    public int getTotalLen() {
        return totalLen;
    }

    /** Returns the reason the packet is being sent; Since 1.0.
     *
     * @return the reason
     */
    public PacketInReason getReason() {
        return reason;
    }

    /** Returns the ID of the table that was looked up; Since 1.1.
     *
     * @return the table ID
     */
    public TableId getTableId() {
        return tableId;
    }

    /** Returns the cookie of the flow entry that caused the packet to be
     * sent to the controller; Since 1.3.
     *
     * @return the cookie
     */
    public long getCookie() {
        return cookie;
    }

    /** Returns the packet metadata; Since 1.2.
     * This match structure reflects the packet's headers and context when
     * the event that triggers the PACKET_IN message occurred.
     *
     * @return the packet metadata
     */
    public Match getMatch() {
        return match;
    }

    /** Returns a copy of the packet data; Since 1.0.
     *  Contains the packet itself, or a fraction of the packet if the
     *  packet is buffered.
     *
     * @return a copy of the packet data
     */
    public byte[] getData() {
        return data == null ? null : data.clone();
    }
}
