/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.instr.Instruction;
import org.opendaylight.of.lib.instr.InstructionFactory;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.util.net.BigPortNumber;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_1;
import static org.opendaylight.of.lib.msg.MessageCopier.CopyType.IMMUTABLE_EXACT;
import static org.opendaylight.util.PrimitiveUtils.verifyU16;

/**
 * Mutable subclass of {@link OfmFlowMod}. Instances of this class are created
 * via the {@link org.opendaylight.of.lib.msg.MessageFactory}; for example:
 * <pre>
 *  OfmMutableFlowMod fm = (OfmMutableFlowMod) MessageFactory.create(
 *          ProtocolVersion.V_1_3, MessageType.FLOW_MOD, FlowModCommand.ADD);
 * </pre>
 * Note that a newly constructed instance will have the following default
 * values:
 * <ul>
 *     <li> {@link #outPort(org.opendaylight.util.net.BigPortNumber)} : {@link Port#ANY}</li>
 *     <li> {@link #outGroup(org.opendaylight.of.lib.dt.GroupId)} : {@link GroupId#ANY}</li>
 *     <li> {@link #bufferId(org.opendaylight.of.lib.dt.BufferId)} : {@link BufferId#NO_BUFFER}</li>
 * </ul>
 *
 * @author Simon Hunt
 */
public class OfmMutableFlowMod extends OfmFlowMod implements MutableMessage {
    private static final String E_BAD_COPY_TYPE = "Bad copy type: ";

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow FlowMod message.
     * Out-Port and Out-Group fields are set to their respective
     * <em>ANY</em> values; Buffer-ID is set to <em>NO_BUFFER</em>.
     *
     * @param header the message header
     */
    OfmMutableFlowMod(Header header) {
        super(header);
        outPort = Port.ANY;
        outGroup = GroupId.ANY;
        bufferId = BufferId.NO_BUFFER;
        flags = new TreeSet<>();
        if (header.version == V_1_0)
            actions = new ArrayList<>();
        else
            instructions = new ArrayList<>();
    }

    @Override
    public void clearXid() {
        mutt.checkWritable(this);
        header.xid = 0;
    }

    @Override
    public OpenflowMessage toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Copy over to read-only instance
        OfmFlowMod msg = new OfmFlowMod(header);
        msg.cookie = this.cookie;
        msg.cookieMask = this.cookieMask;
        msg.tableId = this.tableId;
        msg.command = this.command;
        msg.idleTimeout = this.idleTimeout;
        msg.hardTimeout = this.hardTimeout;
        msg.priority = this.priority;
        msg.bufferId = this.bufferId;
        msg.outPort = this.outPort;
        msg.outGroup = this.outGroup;
        msg.flags = this.flags;
        msg.actions = this.actions;
        msg.match = this.match;
        msg.instructions = this.instructions;
        return msg;
    }

    /**
     * Produces a copy of the given flow-mod message.
     *
     * @param original the original message
     * @param copyType the type of copy required
     * @return the copy
     */
    static OfmFlowMod makeCopy(OfmFlowMod original,
                               MessageCopier.CopyType copyType) {
        OfmFlowMod copy;
        // NOTE: If we are making an IMMUTABLE_EXACT copy we can re-use the
        //       same immutable header instance. Otherwise, we need to
        //       create a new header instance...
        //     If IMMUTABLE: the XID will be different
        //     If MUTABLE_EXACT: the length field may change
        //     If MUTABLE: the XID will be different and the length may change
        OpenflowMessage.Header header = original.header;
        if (!copyType.equals(IMMUTABLE_EXACT))
            header = new OpenflowMessage.Header(original.header);

        switch (copyType) {
            case IMMUTABLE:
            case IMMUTABLE_EXACT:
                copy = new OfmFlowMod(header);
                copy.flags = new TreeSet<>();
                if (header.version == V_1_0)
                    copy.actions = new ArrayList<>();
                else
                    copy.instructions = new ArrayList<>();
                break;
            case MUTABLE:
            case MUTABLE_EXACT:
                copy = new OfmMutableFlowMod(header);
                break;
            default:
                throw new IllegalStateException(E_BAD_COPY_TYPE + copyType);
        }
        // assign a new XID if need be...
        if (!copyType.exact())
            MessageFactory.stampNewXid(copy);

        copy.cookie = original.cookie;
        copy.cookieMask = original.cookieMask;
        copy.tableId = original.tableId;
        copy.command = original.command;
        copy.idleTimeout = original.idleTimeout;
        copy.hardTimeout = original.hardTimeout;
        copy.priority = original.priority;
        copy.bufferId = original.bufferId;
        copy.outPort = original.outPort;
        copy.outGroup = original.outGroup;
        copy.flags.addAll(original.flags);
        if (original.actions != null)
            copy.actions.addAll(original.actions);
        copy.match = original.match;
        if (original.instructions != null)
            copy.instructions.addAll(original.instructions);
        return copy;
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

    private static final long COOKIE_RESERVED = -1L;

    /**
     * Sets the cookie value; Since 1.0.
     * <p>
     * The cookie field is an opaque data value chosen by the controller.
     * This value appears in flow removed messages and flow statistics, and
     * can also be used to filter flow statistics, flow modification and
     * flow deletion. It is not used by the packet processing pipeline, and
     * thus does not need to reside in hardware.
     * <p>
     * The value <em>-1 (0xffffffffffffffff)</em> is reserved and must
     * not be used.
     * <p>
     * When a flow entry is inserted in a table through an
     * {@link FlowModCommand#ADD ADD} message, its cookie field is set to the
     * provided value. When a flow entry is modified
     * ({@link FlowModCommand#MODIFY MODIFY} or
     * {@link FlowModCommand#MODIFY_STRICT MODIFY_STRICT}
     * messages), its cookie field is unchanged.
     *
     * @param cookie the cookie value
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if the reserved value (-1) is specified
     */
    public OfmMutableFlowMod cookie(long cookie) {
        mutt.checkWritable(this);
        if (cookie == COOKIE_RESERVED)
            throw new IllegalArgumentException(CommonUtils.E_RESERVED + cookie);
        this.cookie = cookie;
        return this;
    }

    /**
     * Sets the cookie mask value; Since 1.1.
     * <p>
     * If the cookie mask field is non-zero, it is used with the cookie field
     * to restrict flow matching while modifying or deleting flow entries.
     * This field is ignored by {@link FlowModCommand#ADD ADD} messages.
     *
     * @param mask the cookie mask value
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1
     */
    public OfmMutableFlowMod cookieMask(long mask) {
        mutt.checkWritable(this);
        verMin11(header.version);
        this.cookieMask = mask;
        return this;
    }

    /**
     * Sets the table id; Since 1.1.
     * <p>
     * The table ID field specifies the table into which the flow entry
     * should be inserted, modified or deleted. Table <em>0</em> signifies
     * the first table in the pipeline. The use of
     * {@link TableId#ALL} is only valid for delete requests.
     *
     * @param tableId the table id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1
     * @throws NullPointerException if tableId is null
     */
    public OfmMutableFlowMod tableId(TableId tableId) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(tableId);
        this.tableId = tableId;
        return this;
    }

    /**
     * Sets the flow mod command; Since 1.0.
     *
     * @param command the flow mod command
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if command is null
     */
    public OfmMutableFlowMod command(FlowModCommand command) {
        mutt.checkWritable(this);
        notNull(command);
        this.command = command;
        return this;
    }

    /**
     * Sets the idle timeout, in seconds; Since 1.0.
     * <p>
     * If the flow is idle (no matches) for this amount of time,
     * discard the flow.
     *
     * @param idleTimeout the idle timeout, in seconds (u16)
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if idleTimeout is not u16
     */
    public OfmMutableFlowMod idleTimeout(int idleTimeout) {
        mutt.checkWritable(this);
        verifyU16(idleTimeout);
        this.idleTimeout = idleTimeout;
        return this;
    }

    /**
     * Sets the hard timeout, in seconds; Since 1.0.
     * <p>
     * Unconditionally discard the flow after this amount of time.
     *
     * @param hardTimeout the hard timeout, in seconds (u16)
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if hardTimeout is not u16
     */
    public OfmMutableFlowMod hardTimeout(int hardTimeout) {
        mutt.checkWritable(this);
        verifyU16(hardTimeout);
        this.hardTimeout = hardTimeout;
        return this;
    }

    /**
     * Sets the priority level of the flow entry; since 1.0.
     * <p>
     * The priority indicates priority within the specified flow table.
     * Higher numbers indicate higher priorities. This field is used only
     * for {@link FlowModCommand#ADD ADD} messages when matching and adding
     * flow entries, and for {@link FlowModCommand#MODIFY_STRICT MODIFY_STRICT}
     * or {@link FlowModCommand#DELETE_STRICT DELETE_STRICT} messages when
     * matching flow entries.
     *
     * @param priority the priority level
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if hardTimeout is not u16
     */
    public OfmMutableFlowMod priority(int priority) {
        mutt.checkWritable(this);
        verifyU16(priority);
        this.priority = priority;
        return this;
    }

    /**
     * Sets the buffer id; Since 1.0.
     * <p>
     * The buffer id refers to the packet buffered at the switch and sent to
     * the controller by a <em>packet-in</em> message. A flow mod that includes
     * a valid buffer id is effectively equivalent to sending a two-message
     * sequence of a <em>flow-mod</em> and a <em>packet-out</em> to
     * {@link Port#TABLE}, with the requirement that the switch must fully
     * process the <em>flow-mod</em> before the <em>packet-out</em>. These
     * semantics apply regardless of the table to which the flow mod refers,
     * or the instructions contained in the flow mod.
     * <p>
     * This field is ignored by {@link FlowModCommand#DELETE DELETE} and
     * {@link FlowModCommand#DELETE_STRICT DELETE_STRICT} flow mod messages.
     *
     * @param bufferId the buffer id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if bufferId is null
     */
    public OfmMutableFlowMod bufferId(BufferId bufferId) {
        mutt.checkWritable(this);
        notNull(bufferId);
        this.bufferId = bufferId;
        return this;
    }

    /**
     * Sets the out port; Since 1.0.
     * <p>
     * Used to filter the scope of {@link FlowModCommand#DELETE DELETE}
     * and {@link FlowModCommand#DELETE_STRICT DELETE_STRICT} messages
     * by output port. If this value is any other than
     * {@link Port#ANY}, it introduces the constraint that the flow entry
     * must contain an output action directed at that port.
     * <p>
     * This field is ignored by {@link FlowModCommand#ADD ADD},
     * {@link FlowModCommand#MODIFY MODIFY} or
     * {@link FlowModCommand#MODIFY_STRICT MODIFY_STRICT} messages.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param outPort the output port
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if outPort is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public OfmMutableFlowMod outPort(BigPortNumber outPort) {
        mutt.checkWritable(this);
        notNull(outPort);
        Port.validatePortValue(outPort, header.version);
        this.outPort = outPort;
        return this;
    }

    /**
     * Sets the out group; Since 1.1.
     * <p>
     * Used to filter the scope of {@link FlowModCommand#DELETE DELETE}
     * and {@link FlowModCommand#DELETE_STRICT DELETE_STRICT} messages
     * by output group. If this value is any other than
     * {@link GroupId#ANY}, it introduces the constraint that the flow entry
     * must contain an output action directed at that group.
     * <p>
     * This field is ignored by {@link FlowModCommand#ADD ADD},
     * {@link FlowModCommand#MODIFY MODIFY} or
     * {@link FlowModCommand#MODIFY_STRICT MODIFY_STRICT} messages.
     *
     * @param outGroup the output group
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1
     * @throws NullPointerException if outGroup is null
     */
    public OfmMutableFlowMod outGroup(GroupId outGroup) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(outGroup);
        this.outGroup = outGroup;
        return this;
    }

    /**
     * Sets the flow mod flags; Since 1.0.
     * <p>
     * When the {@link FlowModFlag#SEND_FLOW_REM SEND_FLOW_REM} flag is set,
     * the switch must send a flow removed message when the flow entry
     * expires or is deleted.
     * <p>
     * When the {@link FlowModFlag#CHECK_OVERLAP CHECK_OVERLAP} flag is set,
     * the switch must check that there are no conflicting entries with the
     * same priority prior to inserting in in the flow table. If there is one,
     * the flow mod fails and an error message is returned.
     * <p>
     * When the {@link FlowModFlag#NO_PACKET_COUNTS NO_PACKET_COUNTS} flag is
     * set, the switch does not need to keep track of the flow packet count.
     * When the {@link FlowModFlag#NO_BYTE_COUNTS NO_BYTE_COUNTS} flag is set,
     * the switch does not need to keep track of the flow byte count. Setting
     * those flags may decrease the processing load on some OpenFlow switches,
     * however, those counters may not be available in flow statistics and
     * flow removed messages for this flow entry. A switch is not required
     * to honor these flags and may keep track of a flow count and return it
     * despite the corresponding flag being set. If a switch does not keep
     * track of a flow count, the corresponding counter is not available
     * and must be set to the maximum field value.
     * <p>
     * When a flow entry is inserted in a table, its <em>flags</em> field is
     * set with the values from the message. When flow entry is matched
     * and modified ({@link FlowModCommand#MODIFY MODIFY} or
     * {@link FlowModCommand#MODIFY_STRICT MODIFY_STRICT} messages), the
     * <em>flags</em> field is ignored.
     *
     * @param flags the flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if flags is null
     */
    public OfmMutableFlowMod flowModFlags(Set<FlowModFlag> flags) {
        mutt.checkWritable(this);
        notNull(flags);
        this.flags.clear();
        this.flags.addAll(flags);
        return this;
    }

    /**
     * Adds an action to the action list; Since 1.0; Removed at 1.1.
     *
     * @param act the action to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is not 1.0
     * @throws NullPointerException if act is null
     * @throws IllegalArgumentException if the action is not appropriate for
     *          version 1.0
     */
    public OfmMutableFlowMod addAction(Action act) {
        mutt.checkWritable(this);
        ProtocolVersion pv = header.version;
        if (pv.gt(V_1_0))
            throw new VersionMismatchException(pv + E_DEPRECATED + V_1_1);
        notNull(act);
        sameVersion("FlowMod / Action", header.version, act.getVersion());
        ActionFactory.validateAction(header.version, act, "FlowMod");
        // if we are still going, the action validated okay
        actions.add(act);
        header.length += act.getTotalLength();
        return this;
    }

    /**
     * Removes all actions currently stored in this flow mod.
     *
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is not 1.0
     */
    public OfmMutableFlowMod clearActions() {
        mutt.checkWritable(this);
        ProtocolVersion pv = header.version;
        if (pv.gt(V_1_0))
            throw new VersionMismatchException(pv + E_DEPRECATED + V_1_1);
        int lengthToSubtract = 0;
        for (Action act: actions)
            lengthToSubtract += act.getTotalLength();
        header.length -= lengthToSubtract;
        actions.clear();
        return this;
    }


    /**
     * Sets the match for this flow mod; Since 1.0.
     * <p>
     * If a match has already been set on this flowmod instance, the specified
     * parameter will replace it.
     *
     * @param match the match
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if match is null
     * @throws IllegalArgumentException if match is mutable
     * @throws VersionMismatchException if match not the right version
     */
    public OfmMutableFlowMod match(Match match) {
        mutt.checkWritable(this);
        notNull(match);
        notMutable(match);
        sameVersion("FlowMod / Match", header.version, match.getVersion());
        Match oldMatch = this.match;
        this.match = match;
        if (match.getVersion().gt(V_1_0)) {
            // NOTE: in versions > 1.0, the match structure is variable in
            //      length, so we have to keep track of it...
            if (oldMatch != null)
                this.header.length -= oldMatch.getTotalLength();
            this.header.length += match.getTotalLength();
        }
        return this;
    }

    /**
     * Adds an instruction to the instruction list; Since 1.1.
     *
     * @param ins the instruction to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1 or if the
     *          instruction is not the same version as this instance
     * @throws NullPointerException if ins is null
     * @throws IllegalArgumentException if the instruction is invalid
     */
    public OfmMutableFlowMod addInstruction(Instruction ins) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(ins);
        notMutable(ins);
        sameVersion("FlowMod / Instruction", header.version, ins.getVersion());
        InstructionFactory.validateInstruction(header.version, ins, "FlowMod");
        instructions.add(ins);
        header.length += ins.getTotalLength();
        return this;
    }

    /**
     * Removes all instructions currently stored in this flow mod.
     *
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1
     */
    public OfmMutableFlowMod clearInstructions() {
        mutt.checkWritable(this);
        verMin11(header.version);
        int lengthToSubtract = 0;
        for (Instruction ins: instructions)
            lengthToSubtract += ins.getTotalLength();
        header.length -= lengthToSubtract;
        instructions.clear();
        return this;
    }
}
