/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.util.PrimitiveUtils.verifyU16;

/**
 * Mutable subclass of {@link Bucket}.
 *
 * @author Simon Hunt
 */
public class MutableBucket extends Bucket implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable bucket.
     *
     * @param pv the protocol version
     */
    public MutableBucket(ProtocolVersion pv) {
        super(pv);
        this.length = BucketFactory.BUCKET_LEN;
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        Bucket bucket = new Bucket(version);
        bucket.length = this.length;
        bucket.weight = this.weight;
        bucket.watchPort = this.watchPort;
        bucket.watchGroup = this.watchGroup;
        bucket.actions.addAll(this.actions);
        return bucket;
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

    /** Sets the weight (u16) for this bucket; Since 1.1.
     *
     * @param weight the bucket weight
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if the weight is not u16
     */
    public MutableBucket weight(int weight) {
        mutt.checkWritable(this);
        verifyU16(weight);
        this.weight = weight;
        return this;
    }

    /** Sets the watch port for this bucket; Since 1.1.
     *
     * @param watchPort the watch port
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if watchPort is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public MutableBucket watchPort(BigPortNumber watchPort) {
        mutt.checkWritable(this);
        notNull(watchPort);
        Port.validatePortValue(watchPort, version);
        this.watchPort = watchPort;
        return this;
    }

    /** Sets the watch group for this bucket; Since 1.1.
     *
     * @param watchGroup the watch group
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if watchGroup is null
     */
    public MutableBucket watchGroup(GroupId watchGroup) {
        mutt.checkWritable(this);
        notNull(watchGroup);
        this.watchGroup = watchGroup;
        return this;
    }

    /** Adds the specified action to this bucket; Since 1.1.
     *
     * @param action the action to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if action is null
     */
    public MutableBucket addAction(Action action) {
        mutt.checkWritable(this);
        notNull(action);
        // TODO: Review - some validation required?
        actions.add(action);
        length += action.getTotalLength();
        return this;
    }
}
