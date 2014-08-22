/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.CommonUtils;
import org.opendaylight.of.lib.IncompleteMessageException;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.BigPortNumber;

import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.msg.PacketUtils.packetSummary;
import static org.opendaylight.of.lib.msg.Port.portNumberToString;

/**
 * Represents an OpenFlow PACKET_OUT message; Since 1.0.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public class OfmPacketOut extends OpenflowMessage {

    /** ID assigned by datapath (BufferId.NO_BUFFER if none); since 1.0. */
    BufferId bufferId;
    /** Packet's input port or Port.CONTROLLER; Since 1.0. */
    BigPortNumber inPort;
    /** Size of action array in bytes; Since 1.0.
     * Note - this is not exposed as it is internal detail.
     */
    int actionsLen;
    /** Action list defining how the packet should be processed by the
     * switch; Since 1.0.
     */
    List<Action> actions;
    /** Packet data; Since 1.0.
     * The length is inferred from the length field in the header. Only
     * meaningful if bufferId is BufferId.NO_BUFFER.
     */
    byte[] data;

    /**
     * Constructs an OpenFlow message.
     *
     * @param header the message header
     */
    OfmPacketOut(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",acts=").append(actionsSummary())
                .append(",frameSize=").append(CommonUtils.aSize(data))
                .append(",packet=").append(packetSummary(data, bufferId))
        .append("}");
        return sb.toString();
    }

    private String actionsSummary() {
        // TODO: pick out OUTPUT port and show just that, with indicator or other actions
        return actions.toString();
    }

    /** Returns a multi-line string representation of this packet out message.
     *
     * @param indent the additional indent
     * @return a multi-line string representation
     */
    public String toDebugString(int indent) {
        final String eoli = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder(toString());
        sb.append(eoli).append("Buffer ID: ").append(bufferId)
                .append(eoli).append("In Port  : ")
                .append(portNumberToString(inPort, header.version))
                .append(eoli).append("Actions  : ");
        if (actions.size() == 0)
            sb.append(NONE);
        else
            for (Action a: actions)
                sb.append(eoli).append("  ").append(a);
        sb.append(eoli).append("Frame Data length: ").append(aSize(data));
        sb.append(eoli).append("Packet=").append(packetSummary(data, bufferId));
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    @Override
    public void validate() throws IncompleteMessageException {
        notNullIncompleteMsg(bufferId, inPort, actions);
    }

    /** Returns the buffer ID assigned by datapath
     * (BufferId.NO_BUFFER if none); since 1.0.
     *
     * @return the buffer ID
     */
    public BufferId getBufferId() {
        return bufferId;
    }

    /** Returns the packet's input port or Port.CONTROLLER; Since 1.0.
     *
     * @return the input port
     */
    public BigPortNumber getInPort() {
        return inPort;
    }

    /** Returns the action list defining how the packet should be processed by
     * the switch; Since 1.0.
     *
     * @return the actions
     */
    public List<Action> getActions() {
        return actions == null ? null : Collections.unmodifiableList(actions);
    }

    /** Returns a copy of the packet data; Since 1.0.
     *
     * @return a copy of the packet data
     */
    public byte[] getData() {
        return data == null ? null : data.clone();
    }

    /** Returns the number of bytes of packet data.
     *
     * @return the number of bytes of packet data
     */
    public int getDataLength() {
        return data == null ? 0 : data.length;
    }
}
