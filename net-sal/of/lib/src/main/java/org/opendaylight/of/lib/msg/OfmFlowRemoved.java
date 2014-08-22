/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;


import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.NULL_REP;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;

/**
 * Represents an Openflow FLOW_REMOVED message; Since 1.0.
 *
 * This is an asynchronous message which comes from datapath.
 *
 * @author Sudheer Duggisetty
 */
public class OfmFlowRemoved extends OpenflowMessage {

    /** Description of fields; Since 1.0. */
    Match match;

    /** Opaque controller-issued identifier; Since 1.0. (u64)*/
    long cookie;

    /** Priority level of flow entry; Since 1.0. (u16) */
    int priority;

    /** The reason the flow was removed; Since 1.0 */
    FlowRemovedReason reason;

    /** ID of the table; Since 1.3 */
    TableId tableId;

    /** Time flow was alive in seconds; Since 1.0. (u32) */
    long durationSec;

    /** Time flow was alive in nanoseconds beyond duration seconds;
     * Since 1.0. (u32)*/
    long durationNsec;

    /** Idle timeout from original flow mod in seconds; Since 1.0. (u16) */
    int idleTimeout;

    /** Hard timeout from original flow mod in seconds; Since 1.2. (u16) */
    int hardTimeout;

    /** The packet count indicates the number of packets associated with this
     * flow; Since 1.0. (u64)
     */
    long packetCount;

    /** The byte count indicates the number of bytes associated with this flow;
     * Since 1.0. (u64)
     */
    long byteCount;

    OfmFlowRemoved(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",match=").append(match);
        sb.append(",reason=").append(reason).append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(super.toString());
        ProtocolVersion pv = getVersion();

        sb.append(EOLI).append("Match : ")
                .append(match == null ? NULL_REP : match.toDebugString())
                .append(EOLI).append("Cookie : ").append(hex(cookie))
                .append(EOLI).append("Priority : ").append(priority)
                .append(EOLI).append("Flow Removed Reason : ").append(reason);

        if (pv.gt(V_1_0))
            sb.append(EOLI).append("Table ID : ").append(tableId);

        sb.append(EOLI).append("Duration : ")
            .append(durationSec).append("s ")
            .append(durationNsec).append("ns")
            .append(EOLI).append("Idle timeout : ")
            .append(idleTimeout).append("s");

        if ( pv.gt(V_1_2 ))
            sb.append(EOLI).append("Hard Timeout : ")
                    .append(hardTimeout).append("s");

        sb.append(EOLI).append("Packet Count : ").append(packetCount);
        sb.append(EOLI).append("Byte Count : ").append(byteCount);

        return sb.toString();
    }

    /**
     * Returns the match; Since 1.0.
     *
     * @return the match
     */
    public Match getMatch() {
        return match;
    }

    /**
     * Returns the Opaque controller-issued identifier; Since 1.0.
     *
     * @return the cookie
     */
    public long getCookie() {
        return cookie;
    }

    /**
     * Returns the priority level of the flow entry; Since 1.0.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the reason the flow was removed; Since 1.0.
     *
     * @return the reason
     */
    public FlowRemovedReason getReason() {
        return reason;
    }

    /**
     * Returns the table ID; Since 1.1. <br>
     * The ID of the table from which the flow was removed.
     * <p>
     * For 1.0 messages this method returns 0.
     *
     * @return the tableId
     */
    public TableId getTableId() {
        return tableId;
    }

    /**
     * Returns the time flow was alive in seconds; Since 1.0.
     *
     * @return the number of seconds the flow was alive
     */
    public long getDurationSeconds() {
        return durationSec;
    }

    /**
     * Returns the time flow was alive in nanoseconds beyond the
     * {@link #getDurationSeconds number of seconds}; Since 1.0.
     *
     * @return  the additional nano seconds
     */
    public long getDurationNanoSeconds() {
        return durationNsec;
    }

    /**
     * Returns the idle timeout from the original flow mod; Since 1.0.
     *
     * @return the idle timeout
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Returns the hard timeout from the original flow mod; Since 1.2.
     *
     * @return the hard timeout
     */
    public int getHardTimeout() {
        return hardTimeout;
    }

    /**
     * Returns the number of packets associated with this flow; Since 1.0.
     *
     * @return the number of packets
     */
    public long getPacketCount() {
        return packetCount;
    }

    /**
     * Returns the number of bytes associated with this flow; Since 1.0.
     *
     * @return the number of bytes
     */
    public long getByteCount() {
        return byteCount;
    }
}
