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

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link MBodyGroupStatsRequest}.
 *
 * @author Pramod Shanbhag
 * @author Simon Hunt
 */
public class MBodyMutableGroupStatsRequest extends MBodyGroupStatsRequest
        implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body GROUP request type.
     * <p>
     * Note that a freshly constructed instance has the following default
     * value:
     * <ul>
     *     <li>group ID: {@link GroupId#ALL}</li>
     * </ul>
     *
     * @param pv the protocol version
     */
    public MBodyMutableGroupStatsRequest(ProtocolVersion pv) {
        super(pv);
        groupId = GroupId.ALL;
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyGroupStatsRequest req = new MBodyGroupStatsRequest(version);
        req.groupId = this.groupId;
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

    /** Sets the group ID; Since 1.1.
     *
     * @param groupId the group ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if groupId is null
     */
    public MBodyMutableGroupStatsRequest groupId(GroupId groupId) {
        mutt.checkWritable(this);
        notNull(groupId);
        this.groupId = groupId;
        return this;
    }
}
