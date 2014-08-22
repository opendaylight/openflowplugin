/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.IncompleteMessageException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.Instruction;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.util.net.BigPortNumber;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.util.StringUtils.EOL;

/**
 * Represents an OpenFlow FLOW_MOD message; Since 1.0.
 *
 * @author Simon Hunt
 */
public class OfmFlowMod extends OpenflowMessage {

                                    //  1.0  1.1  1.2  1.3
                                    //--------------------
    long cookie;                    //   X    X    X    X
    long cookieMask;                //        X    X    X
    TableId tableId;                //        X    X    X
    FlowModCommand command;         //   X    X    X    X
    int idleTimeout;                //   X    X    X    X
    int hardTimeout;                //   X    X    X    X
    int priority;                   //   X    X    X    X
    BufferId bufferId;              //   X    X    X    X
    BigPortNumber outPort;          //  (X)   X    X    X
    GroupId outGroup;               //        X    X    X
    Set<FlowModFlag> flags;         //   X    X    X    X
    Match match;                    //   X    X    X    X
    List<Action> actions;           //   X
    List<Instruction> instructions; //        X    X    X

    /**
     * Constructs an OpenFlow FLOW_MOD message.
     *
     * @param header the message header
     */
    OfmFlowMod(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len - 1, len, ",cmd=").append(command)
                .append(",match=").append(match)
                .append(",...}");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        ProtocolVersion pv = getVersion();
        sb.append(EOLI).append("Cookie   : ").append(hex(cookie));

        if (pv.gt(V_1_0)) {
            sb.append(EOLI).append("C Mask   : ").append(hex(cookieMask));
            sb.append(EOLI).append("Table ID : ").append(tableId);
        }

        sb.append(EOLI).append("FMod Cmd : ").append(command)
          .append(EOLI).append("Idle t/o : ").append(idleTimeout).append("s")
          .append(EOLI).append("Hard t/o : ").append(hardTimeout).append("s")
          .append(EOLI).append("Priority : ").append(priority)
          .append(EOLI).append("Buffer ID: ").append(bufferId)
          .append(EOLI).append("Out Port : ")
                .append(Port.portNumberToString(outPort));

        if (pv.gt(V_1_0))
            sb.append(EOLI).append("Out Group: ").append(outGroup);

        sb.append(EOLI).append("FMod Flgs: ").append(flags)
          .append(EOLI).append("Match    : ")
          .append(match == null ? NULL_REP : match.toDebugString(INDENT_SIZE));

        if (pv == V_1_0) {
            sb.append(EOLI).append("Actions  : ");
            if (actions.size() == 0)
                sb.append(NONE);
            else
                for (Action a: actions)
                    sb.append(EOL).append("    ").append(a);
        } else {
            sb.append(EOLI).append("Instructions:");
            if (instructions == null)
                sb.append(NULL_REP);
            else
                for (Instruction i: instructions)
                    sb.append(EOLI).append(i.toDebugString(INDENT_SIZE));
        }
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteMessageException {
        // NOTES:
        //  outPort, outGroup, and bufferId can never be null, nor can flags.
        //  actions/instructions will be set correctly based on version.
        //  tableId can only be null if version is 1.0.
        notNullIncompleteMsg(command, match);
        if (header.version.gt(V_1_0))
            notNullIncompleteMsg(tableId);
    }

    /** Returns the opaque controller-issued identifier; Since 1.0.
     *
     * @return the cookie
     */
    public long getCookie() {
        return cookie;
    }

    /** Returns the cookie mask; Since 1.1. <br>
     * The mask is used to restrict the cookie bits that must match when the
     * command is {@code MODIFY*} or {@code DELETE*}. A value of 0 indicates
     * no restrictions.
     * <p>
     * For 1.0 messages this method returns 0.
     *
     * @return the cookie mask
     */
    public long getCookieMask() {
        return cookieMask;
    }

    /** Returns the table ID; Since 1.1. <br>
     * The ID of the table to put the flow in. For {@code DELETE*} commands,
     * {@link TableId#ALL} can also be used to delete matching flows from all
     * tables.
     * <p>
     * For 1.0 messages this method returns 0.
     *
     * @return the table ID
     */
    public TableId getTableId() {
        return tableId;
    }

    /** Returns the flow mod command; Since 1.0.
     *
     * @return the command
     */
    public FlowModCommand getCommand() {
        return command;
    }

    /** Returns the idle time before discarding (seconds); Since 1.0.
     *
     * @return the idle time before discarding the flow (seconds)
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /** Returns the max time before discarding (seconds); Since 1.0.
     *
     * @return the max time before discarding the flow (seconds)
     */
    public int getHardTimeout() {
        return hardTimeout;
    }

    /** Returns the priority level of the flow entry; Since 1.0.
     *
     * @return the priority level
     */
    public int getPriority() {
        return priority;
    }

    /** Returns the ID of the packet buffer; Since 1.0.<br>
     * This may be {@code OFP_NO_BUFFER}.  TODO: where is this defined?
     * <p>
     * Not meaningful for {@code DELETE*} commands.
     *
     * @return the ID of the packet buffer
     */
    public BufferId getBufferId() {
        return bufferId;
    }

    /** For {@code DELETE*} commands, requires matching entries to include
     * this port as an output port; Since 1.0.<br>
     * A value of {@link Port#ANY} indicates no restriction.
     *
     * @return the required output port number
     */
    public BigPortNumber getOutPort() {
        return outPort;
    }

    /** For {@code DELETE*} commands, requires matching entries to include
     * this group as an output group; Since 1.1.<br>
     * A value of {@code Group#ANY} indicates no restriction.
     *
     * @return the required output group number
     */
    public GroupId getOutGroup() {
        return outGroup;
    }

    /** Returns the set of flow mod flags; Since 1.0.
     *
     * @return the set of flags
     */
    public Set<FlowModFlag> getFlags() {
        return flags == null ? null : Collections.unmodifiableSet(flags);
    }

    /** Returns the match; Since 1.0.
     *
     * @return the match
     */
    public Match getMatch() {
        return match;
    }

    /** Returns the list of actions; Since 1.0; Removed at 1.1.<br>
     * For messages 1.1 and higher, this method will always return null.
     *
     * @return the list of associated actions
     * @see #getInstructions()
     */
    public List<Action> getActions() {
        return actions == null ? null : Collections.unmodifiableList(actions);
    }

    /** Returns the list of instructions; Since 1.1.<br>
     * For 1.0 messages, this method will always return null.
     *
     * @return the list of associated instructions
     * @see #getActions()
     */
    public List<Instruction> getInstructions() {
        return instructions == null ? null :
                Collections.unmodifiableList(instructions);
    }

    /** Patches this flow mod message with a new priority. Provided as an
     * efficient means of flow mod brokering and arbitration where a logical
     * priority needs to be replaced with an actual priority before sending to
     * the switch.
     *
     * @param newPriority new priority to assign to the flow mod
     * @return patched flow mod
     */
    OfmFlowMod patchPriority(int newPriority) {
        this.priority = newPriority;
        return this;
    }

}
