/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.instr.Instruction;
import org.opendaylight.of.lib.instr.InstructionFactory;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.msg.FlowModFlag;
import org.opendaylight.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;

/**
 * Represents a flow stats element; part of a reply to a
 * flow-stats request multipart message; Since 1.0.
 *
 * @see MBodyFlowStatsRequest
 * @author Simon Hunt
 */
public class MBodyFlowStats extends OpenflowStructure implements MultipartBody {
    int length;
    TableId tableId;
    long durationSec;
    long durationNsec;
    int priority;
    int idleTimeout;
    int hardTimeout;
    Set<FlowModFlag> flags;
    long cookie;
    long packetCount;
    long byteCount;
    Match match;
    List<Instruction> instructions;
    List<Action> actions;

    /**
     * Constructs an OpenFlow structure.
     *
     * @param pv the protocol version
     */
    public MBodyFlowStats(ProtocolVersion pv) {
        super(pv);
    }


    @Override
    public String toString() {
        return "{fstats:tid=" + tableId + ",pri=" + priority +
                ",match=" + match + ",...}";
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns a multi-line representation of this Flow Stats object.
     *
     * @param indent the additional indent (number of spaces)
     * @return a multi-line representation
     */
    public String toDebugString(int indent) {
        final String in = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(in).append("Table ID : ").append(tableId)
            .append(in).append("Duration : ").append(durationSec).append("s ")
                .append(durationNsec).append("ns")
            .append(in).append("Priority : ").append(priority)
            .append(in).append("Idle t/o : ").append(idleTimeout).append("s")
            .append(in).append("Hard t/o : ").append(hardTimeout).append("s");
        if (version.ge(V_1_3))
            sb.append(in).append("Flags    : ").append(flags);
        sb.append(in).append("Cookie   : ").append(hex(cookie))
            .append(in).append("# Packets: ").append(packetCount)
            .append(in).append("# Bytes  : ").append(byteCount)
            .append(in).append("Match    : ").append(match.toDebugString(2));
        if (version == V_1_0) {
            sb.append(in).append("Actions: ")
                    .append(ActionFactory.toDebugString(1, actions));
        } else {
            sb.append(in).append("Instructions: ")
                    .append(InstructionFactory.toDebugString(1, instructions));
        }
        sb.append(EOLI);
        return sb.toString();
    }

    @Override
    public int getTotalLength() {
        return length;
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(tableId, match);
    }


    /** Returns the id of the table this flow came from; Since 1.0.
     *
     * @return the table id
     */
    public TableId getTableId() {
        return tableId;
    }

    /** Returns the time this flow has been alive in seconds; Since 1.0.
     * This value is u32.
     *
     * @return the time the flow has been alive (seconds)
     */
    public long getDurationSec() {
        return durationSec;
    }

    /** Returns the time this flow has been alive in nanoseconds beyond
     * {@link #getDurationSec()}; Since 1.0.
     * This value is u32.
     *
     * @return the additional time the flow has been alive (nanoseconds)
     */
    public long getDurationNsec() {
        return durationNsec;
    }

    /** Returns the priority of this flow; Since 1.0.
     * This value is u16.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /** Returns the idle timeout for this flow, in seconds; Since 1.0.
     * That is, this flow will expire after it has been idle for this
     * number of seconds.
     * This value is u16.
     *
     * @return the idle timeout for this flow
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /** Returns the hard timeout for this flow, in seconds; Since 1.0.
     * That is, this flow will expire after this number of seconds.
     * This value is u16.
     *
     * @return the hard timeout for this flow
     */
    public int getHardTimeout() {
        return hardTimeout;
    }

    /** Returns the set of flow mod flags that apply to this flow; Since 1.3.
     * <p>
     * For versions prior to 1.3, this method will return null.
     *
     * @return the set of flow mod flags
     */
    public Set<FlowModFlag> getFlags() {
        return flags == null ? null : Collections.unmodifiableSet(flags);
    }

    /** Returns this flow's cookie; Since 1.0.
     *
     * @return the cookie
     */
    public long getCookie() {
        return cookie;
    }

    /** Returns the number of packets in the flow; Since 1.0.
     *
     * @return the number of packets in the flow
     */
    public long getPacketCount() {
        return packetCount;
    }

    /** Returns the number of bytes in the flow; Since 1.0.
     *
     * @return the number of bytes in the flow
     */
    public long getByteCount() {
        return byteCount;
    }

    /** Returns the match for this flow; Since 1.0. That is, the fields of
     * a packet that must match in order to be associated with this flow.
     *
     * @return the match criteria
     */
    public Match getMatch() {
        return match;
    }

    /** Returns the list of instructions for this flow; Since 1.1.
     * That is, the instructions to be carried out when a packet matches the
     * flow.
     * <p>
     * For 1.0, this method will return null.
     *
     * @return the list of instructions
     */
    public List<Instruction> getInstructions() {
        return instructions == null ? null
                : Collections.unmodifiableList(instructions);
    }

    /** Returns the list of actions for this flow; Since 1.0; Dropped at 1.1.
     * That is, the actions to be carried out when a packet matches the flow.
     * <p>
     * For versions later than 1.0, this method will return null.
     *
     * @return the list of actions
     */
    public List<Action> getActions() {
        return actions == null ? null
                : Collections.unmodifiableList(actions);
    }

    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- FLOW ";
    private static final String LINE = " ----------------";

    /** Represents an array of flow stats elements. */
    public static class Array extends MBodyList<MBodyFlowStats> {
        Array(ProtocolVersion pv) {
            super(pv);
        }

        void markIncomplete(Throwable cause) {
            parseErrorCause = cause;
        }

        @Override
        public Class<MBodyFlowStats> getElementClass() {
            return MBodyFlowStats.class;
        }

        @Override
        public String toString() {
            return "{FlowStats: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyFlowStats fs: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                        .append(fs.toDebugString());
            return sb.toString();
        }
    }

    /** A mutable array of flow stats elements. */
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
            MBodyFlowStats.Array array = new Array(version);
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

        /** Adds a flow stats object to this mutable array.
         *
         * @param stats the stats object to add
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if stats is null
         * @throws IncompleteStructureException if the flow stats is incomplete
         */
        public MutableArray addFlowStats(MBodyFlowStats stats)
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
