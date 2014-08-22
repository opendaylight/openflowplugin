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
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.*;

/**
 * Mutable subclass of {@link MBodyFlowStatsRequest}.
 *
 * @author Simon Hunt
 */
public class MBodyMutableFlowStatsRequest extends MBodyFlowStatsRequest
        implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body FLOW request type.
     * <p>
     * Note that a freshly constructed instance has the following default
     * values:
     * <ul>
     *     <li>table ID: {@link TableId#ALL}</li>
     *     <li>out port: {@link Port#ANY}</li>
     *     <li>out group: {@link GroupId#ANY}</li>
     *     <li>cookie: {@code 0}</li>
     *     <li>cookie mask: {@code 0}</li>
     *     <li>match: {@code null}</li>
     * </ul>
     * <p>
     * A valid {@link Match} must be {@link #match set} for this
     * request to be valid.
     *
     * @param pv the protocol version
     */
    public MBodyMutableFlowStatsRequest(ProtocolVersion pv) {
        super(pv);
        tableId = TableId.ALL;
        outPort = Port.ANY;
        outGroup = GroupId.ANY;
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyFlowStatsRequest req = new MBodyFlowStatsRequest(version);
        req.tableId = this.tableId;
        req.outPort = this.outPort;
        req.outGroup = this.outGroup;
        req.cookie = this.cookie;
        req.cookieMask = this.cookieMask;
        req.match = this.match;
        return req;
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

    /** Sets the ID of the table to read; Since 1.0.
     *
     * @param tableId the table ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if tableId is null
     */
    public MBodyMutableFlowStatsRequest tableId(TableId tableId) {
        mutt.checkWritable(this);
        notNull(tableId);
        this.tableId = tableId;
        return this;
    }

    /** Sets the requirement that matching flow entries include this as an
     * output port; Since 1.0.
     * A value of {@link Port#ANY} indicates no restriction.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param outPort the out port to match
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if outPort is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public MBodyMutableFlowStatsRequest outPort(BigPortNumber outPort) {
        mutt.checkWritable(this);
        notNull(outPort);
        Port.validatePortValue(outPort, version);
        this.outPort = outPort;
        return this;
    }

    /** Sets the requirement that matching flow entries include this as an
     * output group; Since 1.1.
     * A value of {@link GroupId#ANY} indicates no restriction.
     *
     * @param outGroup the out group to match
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if outGroup is null
     */
    public MBodyMutableFlowStatsRequest outGroup(GroupId outGroup) {
        mutt.checkWritable(this);
        notNull(outGroup);
        this.outGroup = outGroup;
        return this;
    }

    /** Sets the requirement that matching flow entries contain this cookie
     * value; since 1.1.
     *
     * @see #cookieMask(long)
     * @param cookie the cookie value
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableFlowStatsRequest cookie(long cookie) {
        mutt.checkWritable(this);
        this.cookie = cookie;
        return this;
    }

    /** Sets the mask used to restrict the cookie bits that must match;
     * Since 1.1.
     *
     * @see #cookie(long)
     * @param cookieMask the cookie mask value
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableFlowStatsRequest cookieMask(long cookieMask) {
        mutt.checkWritable(this);
        this.cookieMask = cookieMask;
        return this;
    }

    /** Describes the fields to match; Since 1.0.
     *
     * @param match the match descriptor
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if match is null
     * @throws IllegalArgumentException if match is mutable
     * @throws VersionMismatchException if match is not the same version
     *          as this instance
     */
    public MBodyMutableFlowStatsRequest match(Match match) {
        mutt.checkWritable(this);
        notNull(match);
        notMutable(match);
        sameVersion("FlowStatsRequest / Match", version, match.getVersion());
        this.match = match;
        return this;
    }
}
