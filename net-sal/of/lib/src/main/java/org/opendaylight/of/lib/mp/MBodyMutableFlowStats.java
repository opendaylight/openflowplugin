/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.Instruction;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.msg.FlowModFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.PrimitiveUtils.verifyU16;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link MBodyFlowStats}.
 *
 * @author Simon Hunt
 */
public class MBodyMutableFlowStats extends MBodyFlowStats
        implements MutableStructure {

    private static final int FIXED_LEN_10 = 88;
    private static final int FIXED_LEN_11 = 136;
    private static final int FIXED_LEN = 48;

    private final Mutable mutt = new Mutable();
    private int fixedLen;
    private int matchLen;
    private int insLen;

    /**
     * Constructs a mutable multipart body FLOW stats element.
     * <p>
     * A valid {@link TableId} and {@link Match} must be present
     * for this element to be valid.
     *
     * @param pv the protocol version
     */
    public MBodyMutableFlowStats(ProtocolVersion pv) {
        super(pv);
        if (pv == V_1_0) {
            actions = new ArrayList<Action>();
            fixedLen = FIXED_LEN_10;
        } else {
            fixedLen = pv == V_1_1 ? FIXED_LEN_11 : FIXED_LEN;
            instructions = new ArrayList<Instruction>();
            if (pv.ge(V_1_3))
                flags = new TreeSet<FlowModFlag>();
        }
        length = fixedLen;
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyFlowStats fs = new MBodyFlowStats(version);
        fs.length = this.length;
        fs.tableId = this.tableId;
        fs.durationSec = this.durationSec;
        fs.durationNsec = this.durationNsec;
        fs.priority = this.priority;
        fs.idleTimeout = this.idleTimeout;
        fs.hardTimeout = this.hardTimeout;
        fs.flags = this.flags;
        fs.cookie = this.cookie;
        fs.packetCount = this.packetCount;
        fs.byteCount = this.byteCount;
        fs.match = this.match;
        fs.instructions = this.instructions;
        fs.actions = this.actions;
        return fs;
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
    // ==== SETTERS

    /** Sets the ID of the table this flow came from; Since 1.0.
     *
     * @param tableId the table ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if tableId is null
     */
    public MBodyMutableFlowStats tableId(TableId tableId) {
        mutt.checkWritable(this);
        notNull(tableId);
        this.tableId = tableId;
        return this;
    }

    /** Sets the time the flow has been alive; Since 1.0.
     * <p>
     * The first parameter is the number of seconds; the second number is
     * the additional number of nanoseconds.
     *
     * @param seconds the number of seconds
     * @param nano the additional number of nanoseconds
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if nanoSeconds is not u32
     */
    public MBodyMutableFlowStats duration(long seconds, long nano) {
        mutt.checkWritable(this);
        verifyU32(seconds);
        verifyU32(nano);
        this.durationSec = seconds;
        this.durationNsec = nano;
        return this;
    }

    /** Sets the priority of the flow; Since 1.0.
     *
     * @param priority the priority
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if priority is not u16
     */
    public MBodyMutableFlowStats priority(int priority) {
        mutt.checkWritable(this);
        verifyU16(priority);
        this.priority = priority;
        return this;
    }

    /** Sets the number of seconds idle before flow expiration; Since 1.0.
     *
     * @param idleTimeout the idle timeout
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if idleTimeout is not u16
     */
    public MBodyMutableFlowStats idleTimeout(int idleTimeout) {
        mutt.checkWritable(this);
        verifyU16(priority);
        this.idleTimeout = idleTimeout;
        return this;
    }

    /** Sets the number of seconds before flow expiration; Since 1.0.
     *
     * @param hardTimeout the hard timeout
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if hardTimeout is not u16
     */
    public MBodyMutableFlowStats hardTimeout(int hardTimeout) {
        mutt.checkWritable(this);
        verifyU16(priority);
        this.hardTimeout = hardTimeout;
        return this;
    }

    /** Sets the flow mod flags; Since 1.3.
     *
     * @param flags the flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flags is null
     */
    public MBodyMutableFlowStats flags(Set<FlowModFlag> flags) {
        mutt.checkWritable(this);
        notNull(flags);
        this.flags.clear();
        this.flags.addAll(flags);
        return this;
    }

    /** Sets the cookie; Since 1.0.
     *
     * @param cookie the cookie
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableFlowStats cookie(long cookie) {
        mutt.checkWritable(this);
        this.cookie = cookie;
        return this;
    }

    /** Sets the number of packets in the flow; Since 1.0.
     *
     * @param packetCount the number of packets
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableFlowStats packetCount(long packetCount) {
        mutt.checkWritable(this);
        this.packetCount = packetCount;
        return this;
    }

    /** Sets the number of bytes in the flow; Since 1.0.
     *
     * @param byteCount the number of bytes
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableFlowStats byteCount(long byteCount) {
        mutt.checkWritable(this);
        this.byteCount = byteCount;
        return this;
    }

    /** Sets the match; Since 1.0.
     *
     * @param match the match descriptor
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if match is null
     * @throws IllegalArgumentException if match is mutable
     * @throws VersionMismatchException if match not the right version
     */
    public MBodyMutableFlowStats match(Match match) {
        mutt.checkWritable(this);
        notNull(match);
        notMutable(match);
        sameVersion("MutableFlowStats / Match", version, match.getVersion());
        this.match = match;
        matchLen = match.getTotalLength();
        if (version.ge(V_1_2))
            length = fixedLen + matchLen + insLen;
        return this;
    }

    /** Sets the instructions; Since 1.1.
     *
     * @param ins the instructions
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version &lt; 1.1
     * @throws NullPointerException if ins is null
     */
    public MBodyMutableFlowStats instructions(List<Instruction> ins) {
        mutt.checkWritable(this);
        verMin11(version);
        notNull(ins);
        this.instructions.clear();
        insLen = 0;
        for (Instruction i: ins) {
            instructions.add(i);
            insLen += i.getTotalLength();
        }
        length = fixedLen + matchLen + insLen;
        return this;
    }

    /** Sets the actions; Since 1.0; Removed at 1.1.
     *
     * @param acts the actions
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version &gt; 1.0
     * @throws NullPointerException if acts is null
     */
    public MBodyMutableFlowStats actions(List<Action> acts) {
        mutt.checkWritable(this);
        if (version != V_1_0)
            throw new VersionMismatchException(E_DEPRECATED + V_1_1);
        notNull(acts);
        this.actions.clear();
        int actsLen = 0;
        for (Action a: acts) {
            actions.add(a);
            actsLen += a.getTotalLength();
        }
        length = fixedLen + actsLen;
        return this;
    }
}
