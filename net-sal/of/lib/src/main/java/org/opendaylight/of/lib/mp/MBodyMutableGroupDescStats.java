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
import org.opendaylight.of.lib.msg.Bucket;
import org.opendaylight.of.lib.msg.BucketFactory;
import org.opendaylight.of.lib.msg.GroupType;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link MBodyGroupDescStats}.
 *
 * @author Prashant Nayak
 */

public class MBodyMutableGroupDescStats extends MBodyGroupDescStats
        implements MutableStructure {

    private static final int FIXED_LEN = 8;

    private final Mutable mutt = new Mutable();

    public MBodyMutableGroupDescStats(ProtocolVersion pv) {
        super(pv);
        buckets = new ArrayList<Bucket>();
        length = FIXED_LEN;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyGroupDescStats gd = new MBodyGroupDescStats(version);
        gd.groupId = this.groupId;
        gd.length = this.length;
        gd.type = this.type;
        gd.buckets = this.buckets;
        return gd;
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
    public MBodyMutableGroupDescStats groupId(GroupId groupId) {
        mutt.checkWritable(this);
        notNull(groupId);
        this.groupId = groupId;
        return this;
    }

    /** Sets the group Type; Since 1.1.
     *
     * @param groupType the group type
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if groupType is null
     */
    public MBodyMutableGroupDescStats groupType(GroupType groupType) {
        mutt.checkWritable(this);
        notNull(groupType);
        this.type = groupType;
        return this;
    }

    /** Sets the buckets for this group; Since 1.1.
     *
     * @param bkts the number of buckets
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableGroupDescStats buckets(List<Bucket> bkts) {
        mutt.checkWritable(this);
        notNull(bkts);
        this.buckets.clear();
        int bktsLen = 0;
        for (Bucket bk: bkts) {
            buckets.add(bk);
            bktsLen += BucketFactory.getLength(bk);
        }
        length = FIXED_LEN + bktsLen;
        return this;
    }
}
