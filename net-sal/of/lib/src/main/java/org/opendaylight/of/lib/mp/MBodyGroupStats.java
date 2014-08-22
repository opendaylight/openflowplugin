/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.util.StringUtils;

import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;

/**
 * Represents a group stats element; part of a reply to a
 * group-stats request multipart message; Since 1.1.
 *
 * @see MBodyGroupStatsRequest
 * @author Pramod Shanbhag
 */
public class MBodyGroupStats extends OpenflowStructure
        implements MultipartBody {
    int length;
    GroupId groupId;
    long refCount;
    long packetCount;
    long byteCount;
    long durationSec;
    long durationNsec;
    List<BucketCounter> bucketStats;

    /**
     * Constructs a multipart body GROUP type.
     *
     * @param pv the protocol version
     */
    public MBodyGroupStats(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{gstats:gpId=").append(groupId).append(",#refs=")
            .append(refCount).append(",#pkts=").append(packetCount)
            .append(",#byts=").append(byteCount);
        if (version == V_1_3)
            sb.append(",dur=").append(durationSec).append("s ")
                .append(durationNsec).append("ns");

        sb.append(",#bktStats=").append(cSize(bucketStats))
            .append("}");
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(groupId);
    }

    @Override
    public int getTotalLength() {
        return length;
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns a multi-line representation of this Group Stats object.
     *
     * @param indent the additional indent (number of spaces)
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(in).append("Group ID : ").append(groupId)
            .append(in).append("# Flows/Groups forwarded : ")
                .append(refCount)
            .append(in).append("# Packets : ").append(packetCount)
            .append(in).append("# Bytes : ").append(byteCount);
        if (version == V_1_3)
            sb.append(in).append("Duration : ").append(durationSec)
                .append("s ").append(durationNsec).append("ns ");

        sb.append(in).append("Bucket stats : ")
            .append(BucketCounter.bucketCountersToDebugString(bucketStats,
                                                              indent));
        return sb.toString();
    }

    /** Returns the group ID; Since 1.1.
     *
     * @return the group ID
     */
    public GroupId getGroupId() {
        return groupId;
    }

    /** Returns the number of flows or groups that directly forward to
     * this group; Since 1.1.
     *
     * @return the number of flows or groups
     */
    public long getRefCount() {
        return refCount;
    }

    /** Returns the number of packets processed by this group; Since 1.1.
     *
     * @return the number of packets
     */
    public long getPacketCount() {
        return packetCount;
    }

    /** Returns the number of bytes processed by this group; Since 1.1.
     *
     * @return the number of bytes
     */
    public long getByteCount() {
        return byteCount;
    }

    /** Returns the time this group has been alive in seconds; Since 1.3.
     * This value is u32.
     *
     * @return the time the group has been alive (seconds)
     */
    public long getDurationSec() {
        return durationSec;
    }

    /** Returns the time this group has been alive in nanoseconds beyond
     * {@link #getDurationSec()}; Since 1.3.
     * This value is u32.
     *
     * @return the additional time the group has been alive (nanoseconds)
     */
    public long getDurationNsec() {
        return durationNsec;
    }

    /** Returns the list of bucket stats for this group; Since 1.1.
     *
     * @return the list of bucket stats
     */
    public List<BucketCounter> getBucketStats() {
        return bucketStats == null ? null
                : Collections.unmodifiableList(bucketStats);
    }

    //======================================================================
    /**
     * Represents a bucket counter element; part of group-stats reply
     * multipart message; Since 1.1.
     */
    public static class BucketCounter {

        long packetCount;
        long byteCount;

        /**
         * Constructs a bucket counter element.
         *
         * @param packetCount the number of packets
         * @param byteCount the number of bytes
         */
        BucketCounter(long packetCount, long byteCount) {
            this.packetCount = packetCount;
            this.byteCount = byteCount;
        }

        @Override
        public String toString() {
            return "[bkt:#p=" + packetCount + ",#b=" + byteCount + "]";
        }

        /** Returns a string representation useful for debugging.
         *
         * @return a (possibly multi-line) string representation of this element
         */
        public String toDebugString() {
            return toDebugString(0);
        }

        /** Returns a multi-line representation of this bucket counter object.
         *
         * @param indent the additional indent (number of spaces)
         * @return a multi-line representation
         */
        public String toDebugString(int indent) {
            final String in = EOLI + StringUtils.spaces(indent);
            StringBuilder sb = new StringBuilder();
            sb.append(in).append("# Packets : ").append(packetCount)
                .append(in).append("# Bytes : ").append(byteCount);
            return sb.toString();
        }

        static String bucketCountersToDebugString(List<BucketCounter> bcList,
                                                  int indent) {
            final String in = EOLI + StringUtils.spaces(indent);
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (BucketCounter bucketCounter: bcList)
                sb.append(in).append("Bucket ").append(index++).append(" : ")
                    .append(bucketCounter.toDebugString(indent+2));

            return sb.toString();
        }

        /** Returns the number of packets processed by this bucket; Since 1.1.
         *
         * @return the number of packets processed by this bucket
         */
        public long getPacketCount() {
            return packetCount;
        }

        /** Returns the number of bytes processed by this bucket; Since 1.1.
         *
         * @return the number of bytes processed by this bucket
         */
        public long getByteCount() {
            return byteCount;
        }
    }

    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- GROUP ";
    private static final String LINE = " ----------------";

    /** Represents an array of group stats elements. */
    public static class Array extends MBodyList<MBodyGroupStats> {
        Array(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public Class<MBodyGroupStats> getElementClass() {
            return MBodyGroupStats.class;
        }

        @Override
        public String toString() {
            return "{GroupStats: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyGroupStats gs: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                    .append(gs.toDebugString());
            return sb.toString();
        }
    }

    /** A mutable array of group stats elements. */
    public static class MutableArray extends Array implements MutableStructure {

        private final Mutable mutt = new Mutable();

        /** Constructor, initializing the internal list.
         *
         * @param pv the protocol version
         */
        MutableArray(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public OpenflowStructure toImmutable() {
            // Can only do this once
            mutt.invalidate(this);
            // Transfer the payload to an immutable instance
            MBodyGroupStats.Array array = new Array(version);
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

        /** Adds a group stats object to this mutable array.
         *
         * @param stats the stats object to add
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if stats is null
         * @throws IllegalArgumentException if port is mutable
         * @throws IncompleteStructureException if the group stats is incomplete
         */
        public MutableArray addGroupStats(MBodyGroupStats stats)
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
