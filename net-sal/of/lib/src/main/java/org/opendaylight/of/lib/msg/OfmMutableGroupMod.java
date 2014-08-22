/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.GroupId;

import static org.opendaylight.of.lib.CommonUtils.*;

/**
 * Mutable subclass of {@link OfmGroupMod}.
 *
 * @author Simon Hunt
 */
public class OfmMutableGroupMod extends OfmGroupMod implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow GROUP_MOD message.
     *
     * @param header the message header
     */
    OfmMutableGroupMod(Header header) {
        super(header);
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
        OfmGroupMod msg = new OfmGroupMod(header);
        msg.command = this.command;
        msg.groupType = this.groupType;
        msg.groupId = this.groupId;
        msg.buckets = this.buckets;
        return msg;
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

    /** Sets the group mod command; Since 1.1.
     *
     * @param cmd the group-mod command
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1
     * @throws NullPointerException if cmd is null
     */
    public OfmMutableGroupMod command(GroupModCommand cmd) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(cmd);
        this.command = cmd;
        return this;
    }

    /** Sets the group type; Since 1.1.
     *
     * @param type the group type
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1
     * @throws NullPointerException if type is null
     */
    public OfmMutableGroupMod groupType(GroupType type) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(type);
        this.groupType = type;
        // TODO: Review - validation? see 5.6 Group Table desc on page 14
        return this;
    }

    /** Sets the group id; Since 1.1.
     *
     * @param id the group id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1
     * @throws NullPointerException if id is null
     */
    public OfmMutableGroupMod groupId(GroupId id) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(id);
        this.groupId = id;
        return this;
    }

    /** Adds a bucket to this group-mod message; Since 1.1.
     *
     * @param bucket the bucket to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is &lt; 1.1 or if the
     *          bucket version is not the same as this instance
     * @throws NullPointerException if bucket is null
     * @throws IllegalArgumentException if bucket is mutable
     */
    public OfmMutableGroupMod addBucket(Bucket bucket) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(bucket);
        notMutable(bucket);
        sameVersion("GroupMod / Bucket", header.version, bucket.getVersion());
        // TODO: bucket validation - see 5.6 Group Table desc on page 14
        buckets.add(bucket);
        header.length += bucket.length;
        return this;
    }
}
