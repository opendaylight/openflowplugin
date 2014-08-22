/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.*;

/**
 * Represents a meter stats element; part of a reply to a meter stats request
 * multipart message; Since 1.3.
 *
 * @author Scott Simes
 */
public class MBodyMeterStats extends OpenflowStructure
        implements MultipartBody {

    MeterId meterId;
    int length;
    long flowCount;
    long pktInCount;
    long byteInCount;
    long durationSec;
    long durationNSec;
    List<MeterBandStats> bandStats = new ArrayList<MeterBandStats>();

    /**
     * Constructs a multipart body METER_STATS type.
     *
     * @param pv the protocol version
     */
    public MBodyMeterStats(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public int getTotalLength() {
        return length;
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(meterId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",id=").append(meterId).append(",flwCnt=")
                .append(flowCount).append(",pktCnt=").append(pktInCount)
                .append(",byteCnt=").append(byteInCount).append(",dur=")
                .append(durationSec).append("s").append(",#bs=")
                .append(cSize(bandStats)).append(",...}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /**
     * Returns a multi-line representation of the Meter Stats object.
     *
     * @param indent the additional indent (number of spaces)
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(in).append("Meter ID: ").append(meterId)
                .append(in).append("Flow Count: ").append(flowCount)
                .append(in).append("Pkt In Count: ").append(pktInCount)
                .append(in).append("Byte In Count: ").append(byteInCount)
                .append(in).append("Duration: ").append(durationSec)
                .append("s ").append(durationNSec).append("ns");
        for (MeterBandStats mbs: bandStats)
            sb.append(in).append(mbs);

        return sb.toString();
    }

    // ============================================== Getters ============

    /**
     * Returns the meter id; Since 1.3.
     *
     * @return the meter id
     */
    public MeterId getMeterId() {
        return meterId;
    }

    /**
     * Returns the number of flows bound to this meter; Since 1.3.
     *
     * @return the count of flows
     */
    public long getFlowCount() {
        return flowCount;
    }

    /**
     * Returns the number of packets in input; Since 1.3.
     *
     * @return the count of packet in
     */
    public long getPktInCount() {
        return pktInCount;
    }

    /**
     * Returns the number of bytes in input; Since 1.3.
     *
     * @return the count of bytes in
     */
    public long getByteInCount() {
        return byteInCount;
    }

    /**
     * Returns the time this meter has been alive in seconds; Since 1.3.
     * This values is u32.
     *
     * @return the time the meter has been alive (seconds)
     */
    public long getDurationSec() {
        return durationSec;
    }

    /**
     * Returns the time this meter has been alive in nanoseconds beyond
     * {@link #getDurationSec()}; Since 1.3.
     * This value is u32.
     *
     * @return the additional time the meter has been alive (nanoseconds)
     */
    public long getDurationNSec() {
        return durationNSec;
    }

    /**
     * Returns the {@link MeterBandStats} for the meter bands applied to this
     * meter; Since 1.3.
     *
     * @return the meter band stats
     */
    public List<MeterBandStats> getBandStats() {
        return Collections.unmodifiableList(bandStats);
    }

    //=======================================================================
    // Meter Band Statistics

    /**
     * Represents statistics associated with a given meter band.
     */
    public static class MeterBandStats {
        private final static int METER_BAND_STATS_LENGTH = 16;

        final long pktBandCnt;
        final long byteBandCnt;

        /**
         * Constructs a meter band statistic; Since 1.3.
         *
         * @param pktBandCnt the packet count
         * @param byteBandCnt the byte count
         */
        public MeterBandStats(long pktBandCnt,
                              long byteBandCnt) {
            this.pktBandCnt = pktBandCnt;
            this.byteBandCnt = byteBandCnt;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{MeterBandStats:#pkts=").append(pktBandCnt)
                    .append(",#bytes=").append(byteBandCnt).append("}");
            return sb.toString();
        }

        /**
         * Returns the number of packets in the band; Since 1.3.
         *
         * @return the packet count
         */
        public long getPacketBandCount() {
            return pktBandCnt;
        }

        /**
         * Returns the number of bytes in the band; Since 1.3.
         *
         * @return the byte count
         */
        public long getByteBandCount() {
            return byteBandCnt;
        }

        /**
         * Returns the size of this meter band stats, in bytes.
         *
         * @return the length
         */
        int getLength() {
            return METER_BAND_STATS_LENGTH;
        }
    }

    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- Meter Stat ";
    private static final String LINE = " ---------------- ";

    /** Represents an array of meter stats elements. */
    public static class Array extends MBodyList<MBodyMeterStats> {

        /**
         * Constructor, initializing the internal list.
         *
         * @param pv protocol version
         */
        Array(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public Class<MBodyMeterStats> getElementClass() {
            return MBodyMeterStats.class;
        }

        @Override
        public String toString() {
            return "{MeterStats: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyMeterStats ms: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                        .append(ms.toDebugString());
            return sb.toString();
        }
    }

    /** A mutable array of meter stats elements. **/
    public static class MutableArray extends Array implements MutableStructure {

        private final Mutable mutt = new Mutable();

        /**
         * Constructor, initializing the internal list.
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
            MBodyMeterStats.Array array = new Array(version);
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

        // =================================================================
        // ==== ADDERS

        /**
         * Adds a meter stats object to this mutable array.
         *
         * @param meterStats the meter stats object to add
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if meterStats is null
         * @throws IncompleteStructureException if the meter stats is incomplete
         */
        public MutableArray addMeterStats(MBodyMeterStats meterStats)
                throws IncompleteStructureException {
            mutt.checkWritable(this);
            notNull(meterStats);
            notMutable((OpenflowStructure) meterStats);
            meterStats.validate();
            list.add(meterStats);
            return this;
        }
    }
}
