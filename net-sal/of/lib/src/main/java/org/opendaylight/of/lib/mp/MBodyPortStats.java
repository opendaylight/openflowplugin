/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;

/**
 * Represents a port stats element; part of a reply to a
 * port-stats request multipart message; Since 1.0.
 *
 * @see MBodyPortStatsRequest
 * @author Pramod Shanbhag
 */
public class MBodyPortStats extends OpenflowStructure implements MultipartBody {

    BigPortNumber port;
    long rxPackets;
    long txPackets;
    long rxBytes;
    long txBytes;
    long rxDropped;
    long txDropped;
    long rxErrors;
    long txErrors;
    long rxFrameErr;
    long rxOverErr;
    long rxCrcErr;
    long collisions;
    long durationSec;
    long durationNsec;
    
    private static final int TOTAL_LENGTH = 104;
    private static final int TOTAL_LENGTH_13 = 112;

    /**
     * Constructs a multipart body PORT_STATS type.
     *
     * @param pv the protocol version
     */    
    public MBodyPortStats(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{pstats:port=").append(Port.portNumberToString(port))
            .append(",rxPkts=").append(rxPackets)
            .append(",txPkts=").append(txPackets)
            .append(",rxBytes=").append(rxBytes)
            .append(",txBytes=").append(txBytes)
            .append(",...}");
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(port);
    }
    
    @Override
    public int getTotalLength() {
        return version == V_1_3 ? TOTAL_LENGTH_13 : TOTAL_LENGTH;
    }
    
    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns a multi-line representation of this Port Stats object.
    *
    * @param indent the additional indent (number of spaces)
    * @return a multi-line representation
    */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(in).append("Port = ").append(Port.portNumberToString(port))
            .append(in).append("# RX Packets = ").append(rxPackets)
            .append(in).append("# TX Packets = ").append(txPackets)
            .append(in).append("# RX Bytes = ").append(rxBytes)
            .append(in).append("# TX Bytes = ").append(txBytes)
            .append(in).append("# RX Dropped Packets = ").append(rxDropped)
            .append(in).append("# TX Dropped Packets = ").append(txDropped)
            .append(in).append("# RX Errors = ").append(rxErrors)
            .append(in).append("# TX Errors = ").append(txErrors)
            .append(in).append("# RX Frame Errors = ").append(rxFrameErr)
            .append(in).append("# RX Overrun Errors = ").append(rxOverErr)
            .append(in).append("# RX CRC Errors = ").append(rxCrcErr)
            .append(in).append("# Collisions = ").append(collisions);
        if (version == V_1_3) 
            sb.append(in).append("Duration = ").append(durationSec).append("s ")
                .append(durationNsec).append("ns");
        
        sb.append(EOLI);
        return sb.toString();  
    }

    /** Returns the port to which the statistics belong; Since 1.0.
     * <p>
     * A value of {@link Port#ANY} indicates no restriction.
     * Note that in 1.0, port numbers are u16.
     *
     * @return the port to which the statistics belong
     */
    public BigPortNumber getPort() {
        return port;
    }

    /** Returns the number of packets received by this port; Since 1.0.
     * 
     * @return the number of packets
     */
    public long getRxPackets() {
        return rxPackets;
    }
    
    /** Returns the number of packets transmitted by this port; Since 1.0.
     * 
     * @return the number of packets
     */
    public long getTxPackets() {
        return txPackets;
    }
    
    /** Returns the number of bytes received by this port; Since 1.0.
     * 
     * @return the number of bytes
     */
    public long getRxBytes() {
        return rxBytes;
    }
    
    /** Returns the number of bytes transmitted by this port; Since 1.0.
     * 
     * @return the number of bytes
     */
    public long getTxBytes() {
        return txBytes;
    }

    /** Returns the number of packets dropped by this port at the receiving
     *  end; Since 1.0.
     * 
     * @return the number of dropped packets
     */
    public long getRxDropped() {
        return rxDropped;
    }
    
    /** Returns the number of packets dropped by this port at transmitting 
     * end; Since 1.0.
     * 
     * @return the number of dropped packets
     */
    public long getTxDropped() {
        return txDropped;
    }
    
    /** Returns the number of packets received with error; Since 1.0.
     * 
     * @return the number of error packets
     */
    public long getRxErrors() {
        return rxErrors;
    }
    
    /** Returns the number of packets transmitted with errors; Since 1.0. 
     * 
     * @return the number of error packets
     */
    public long getTxErrors() {
        return txErrors;
    }
    
    /** Returns the number of packets received with frame alignment error;
     *  Since 1.0.
     * 
     * @return the number of frame alignment error packets
     */
    public long getRxFrameErr() {
        return rxFrameErr;
    }    

    /** Returns the number of packets received with overrun error;
     *  Since 1.0.
     * 
     * @return the number of overrun error packets
     */
    public long getRxOverErr() {
        return rxOverErr;
    } 
    
    /** Returns the number of packets received with CRC error; Since 1.0.
     * 
     * @return the number of CRC error packets
     */
    public long getRxCRCErr() {
        return rxCrcErr;
    } 
    
    /** Returns the number of collisions; Since 1.0.
     * 
     * @return the number of collisions
     */
    public long getCollisions() {
        return collisions;
    }
    
    /** Returns the time this port has been alive in seconds; Since 1.3.
     * This value is u32.
     *
     * @return the time the port has been alive (seconds)
     */
    public long getDurationSec() {
        return durationSec;
    }

    /** Returns the time this port has been alive in nanoseconds beyond
     * {@link #getDurationSec()}; Since 1.3.
     * This value is u32.
     *
     * @return the additional time the port has been alive (nanoseconds)
     */
    public long getDurationNsec() {
        return durationNsec;
    }
    
    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- PORT ";
    private static final String LINE = " ----------------";

    /** Represents an array of port stats elements. */
    public static class Array extends MBodyList<MBodyPortStats> {
        Array(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public Class<MBodyPortStats> getElementClass() {
            return MBodyPortStats.class;
        }

        @Override
        public String toString() {
            return "{PortStats: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyPortStats ps: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                    .append(ps.toDebugString());
            
            return sb.toString();
        }    
    }

    /** A mutable array of port stats elements. */
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
            MBodyPortStats.Array array = new Array(version);
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

        /** Adds a port stats object to this mutable array.
         *
         * @param stats the port stats object to add
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if stats is null
         * @throws IllegalArgumentException if stats is mutable
         * @throws IncompleteStructureException if the port stats is incomplete
         */
        public MutableArray addPortStats(MBodyPortStats stats)
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
