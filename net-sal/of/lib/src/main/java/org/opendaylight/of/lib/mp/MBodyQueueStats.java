/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.of.lib.msg.QueueFactory;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.Port.portNumberToString;

/**
 * Represents queue statistics; part of a reply to a queue-statistics
 * request multipart message; since 1.0.
 *
 * @see MBodyQueueStatsRequest
 * @author Shruthy Mohanram
 */
public class MBodyQueueStats extends OpenflowStructure implements MultipartBody {
    BigPortNumber port;
    QueueId queueId;
    long txBytes;
    long txPackets;
    long txErrors;
    long durationSec;
    long durationNsec;

    /**
     * Constructs an OpenFlow structure.
     *
     * @param pv the protocol version
     */
    public MBodyQueueStats(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{qstats:port=").append(portNumberToString(port))
                .append(",qId=").append(queueId)
                .append(",txByt=").append(txBytes)
                .append(",txPkt=").append(txPackets)
                .append(",txErr=").append(txErrors);
        if(version == V_1_3)
            sb.append(",dur=").append(durationSec).append("s")
                    .append(durationNsec).append("ns");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns a multi-line representation of this Queue Stats object.
     *
     * @param indent the additional indent (number of spaces)
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(in).append("Port      : ").append(portNumberToString(port))
                .append(in).append("Queue ID  : ").append(queueId)
                .append(in).append("TX Bytes  : ").append(txBytes)
                .append(in).append("TX Packets: ").append(txPackets)
                .append(in).append("TX Errors : ").append(txErrors);
        if (version == V_1_3)
            sb.append(in).append("Duration  : ")
                    .append(durationSec).append("s ")
                    .append(durationNsec).append("ns");
        sb.append(EOLI);
        return sb.toString();
    }

    @Override
    public int getTotalLength() {
        return QueueFactory.getQueueStatsLength(version);
    }

    /** Returns the port for which queue statistics are requested; Since 1.0.
     * <p>
     * A value of {@link Port#ANY} indicates no restriction.
     * Note that in 1.0, port numbers are u16.
     *
     * @return the requested port
     */
    public BigPortNumber getPort() {
        return port;
    }

    /** Returns the ID of the queue configured for specified port; Since 1.0.
     * A value of {@link QueueId#ALL} indicates all queues.
     *
     * @return the queue ID
     */
    public QueueId getQueueId() {
        return queueId;
    }

    /** Returns the number of transmitted bytes; Since 1.0.
     *
     * @return the transmitted byte count
     */
    public long getTxBytes() {
        return txBytes;
    }

    /** Returns the number of transmitted packets; Since 1.0.
     *
     * @return the transmitted packet count
     */
    public long getTxPackets() {
        return txPackets;
    }

    /** Returns the number of packets dropped due to overrun; Since 1.0.
     *
     * @return the dropped packet count
     */
    public long getTxErrors() {
        return txErrors;
    }

    /** Returns the time the queue has been alive in seconds; Since 1.3.
     *
     * @return the time in seconds
     */
    public long getDurationSec() {
        return durationSec;
    }

    /** Returns the additional time the queue has been alive in nanoseconds;
     * Since 1.3.
     *
     * @return the additional time in nanoseconds
     */
    public long getDurationNsec() {
        return durationNsec;
    }

    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- QUEUE ";
    private static final String LINE = " ----------------";

    /** Represents an array of port descriptions. */
    public static class Array extends MBodyList<MBodyQueueStats> {
        Array(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public Class<MBodyQueueStats> getElementClass() {
            return MBodyQueueStats.class;
        }

        @Override
        public String toString() {
            return "{QueueStats: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyQueueStats qs: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                        .append(qs.toDebugString());
            return sb.toString();
        }
    }

    /** A mutable array of queue statistics. */
    public static class MutableArray extends Array implements MutableStructure {

        private final Mutable mutt = new Mutable();

        /** Constructor, initializing the internal list.
         *
         * @param pv protocol version
         */
        MutableArray(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public OpenflowStructure toImmutable() {
            // Can only do this once
            mutt.invalidate(this);
            // Transfer the payload to an immutable instance
            MBodyQueueStats.Array array = new Array(version);
            // copy elements across
            array.addAll(this.list);
            return array;
        }

        @Override
        public boolean writable() {
            return mutt.writable();
        }

        @Override
        public String toString() {
            return mutt.tagString(super.toString());
        }

        // =====================================================================
        // ==== ADDERS

        /** Adds a queue stats object to this mutable array.
         *
         * @param stats the stats object to add
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if stats is null
         * @throws IllegalArgumentException if stats is mutable
         * @throws IncompleteStructureException if the queue stats is incomplete
         */
        public MutableArray addQueueStats(MBodyQueueStats stats)
                throws IncompleteStructureException {
            mutt.checkWritable(this);
            notNull(stats);
            notMutable((OpenflowStructure) stats);
            stats.validate();
            list.add(stats);
            return this;
        }
    }
}
