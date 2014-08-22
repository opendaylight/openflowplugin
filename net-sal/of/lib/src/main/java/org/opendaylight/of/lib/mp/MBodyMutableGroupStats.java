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

import java.util.ArrayList;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.verMin13;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link MBodyGroupStats}.
 *
 * @author Pramod Shanbhag
 */
public class MBodyMutableGroupStats extends MBodyGroupStats 
        implements MutableStructure {

    private static final int FIXED_LEN_13 = 40;
    private static final int FIXED_LEN = 32;
    private static final int BUCKET_LEN = 16;
    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body GROUP stats element.
     * <p>
     *  A valid {@link GroupId} must be present for this element to be valid.
     *
     * @param pv the protocol version
     */
    public MBodyMutableGroupStats(ProtocolVersion pv) {
        super(pv);
        bucketStats = new ArrayList<BucketCounter>();
        length = pv == V_1_3 ? FIXED_LEN_13 : FIXED_LEN;
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyGroupStats gs = new MBodyGroupStats(version);
        gs.length = this.length;
        gs.groupId = this.groupId;
        gs.refCount = this.refCount;
        gs.packetCount = this.packetCount;
        gs.byteCount = this.byteCount;
        gs.durationSec = this.durationSec;
        gs.durationNsec = this.durationNsec;
        gs.bucketStats = this.bucketStats;
        return gs;
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
    public MBodyMutableGroupStats groupId(GroupId groupId) {
        mutt.checkWritable(this);
        notNull(groupId);
        this.groupId = groupId;
        return this;
    }
    
    /** Sets the time the group has been alive; Since 1.3.
     * <p>
     * The first parameter is the number of seconds; the second number is
     * the additional number of nanoseconds.
     *
     * @param seconds the number of seconds
     * @param nano the additional number of nanoseconds
     * @return self, for chaining
     * @throws VersionMismatchException if version &lt; 1.3
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if seconds or nanoSeconds is not u32
     */
    public MBodyMutableGroupStats duration(long seconds, long nano) {
        mutt.checkWritable(this);
        verMin13(version);
        verifyU32(seconds);
        verifyU32(nano);
        this.durationSec = seconds;
        this.durationNsec = nano;
        return this;
    }
    
    /** Sets the number of flows or groups that directly forward to 
     * this group; Since 1.1.
     *
     * @param refCount the number of flows or groups
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if priority is not u32
     */
    public MBodyMutableGroupStats refCount(long refCount) {
        mutt.checkWritable(this);
        verifyU32(refCount);
        this.refCount = refCount;
        return this;
    }
    
    /** Sets the number of packets processed by this group; Since 1.1.
     *
     * @param packetCount the number of packets 
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableGroupStats packetCount(long packetCount) {
        mutt.checkWritable(this);
        this.packetCount = packetCount;
        return this;
    }
    
    /** Sets the number of bytes processed by this group; Since 1.1.
     *
     * @param byteCount the number of bytes
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableGroupStats byteCount(long byteCount) {
        mutt.checkWritable(this);
        this.byteCount = byteCount;
        return this;
    }

   /** Sets the list of bucket stats for this group; Since 1.1.
    *
    * @param packets the number of packets
    * @param bytes the number of bytes
    * @return self, for chaining
    * @throws InvalidMutableException if this instance is no longer writable
    */
    public MBodyMutableGroupStats addBucketStats(long packets, long bytes) {
        mutt.checkWritable(this);
        this.bucketStats.add(new BucketCounter(packets, bytes));
        length += BUCKET_LEN;
        return this;
    }
}
