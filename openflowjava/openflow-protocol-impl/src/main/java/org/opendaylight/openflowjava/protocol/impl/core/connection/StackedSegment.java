/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableSoftReference;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.opendaylight.openflowjava.protocol.api.connection.DeviceRequestFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class StackedSegment {
    private static final class QueueRef extends FinalizableSoftReference<OutboundQueueEntry[]> {
        QueueRef(final FinalizableReferenceQueue queue, final OutboundQueueEntry[] referent) {
            super(referent, queue);
        }

        @Override
        public void finalizeReferent() {
            CACHE.remove(this);
        }
    }

    /**
     * Size of each individual segment
     */
    static final int SEGMENT_SIZE = 4096;

    private static final Logger LOG = LoggerFactory.getLogger(StackedSegment.class);
    private static final FinalizableReferenceQueue REF_QUEUE = new FinalizableReferenceQueue();
    private static final ConcurrentLinkedDeque<QueueRef> CACHE = new ConcurrentLinkedDeque<>();

    private final OutboundQueueEntry[] entries;
    private final long baseXid;
    private final long endXid;

    // Updated from netty only
    private int lastBarrierOffset = -1;
    private int completeCount;

    StackedSegment(final long baseXid, final OutboundQueueEntry[] entries) {
        this.baseXid = baseXid;
        this.endXid = baseXid + SEGMENT_SIZE;
        this.entries = Preconditions.checkNotNull(entries);
    }

    static StackedSegment create(final long baseXid) {
        final StackedSegment ret;
        for (;;) {
            final Reference<OutboundQueueEntry[]> item = CACHE.pollLast();
            if (item == null) {
                break;
            }

            final OutboundQueueEntry[] cached = item.get();
            if (cached != null) {
                ret = new StackedSegment(baseXid, cached);
                LOG.trace("Reusing array {} in segment {}", cached, ret);
                return ret;
            }
        }

        final OutboundQueueEntry[] entries = new OutboundQueueEntry[SEGMENT_SIZE];
        for (int i = 0; i < SEGMENT_SIZE; ++i) {
            entries[i] = new OutboundQueueEntry();
        }

        ret = new StackedSegment(baseXid, entries);
        LOG.trace("Allocated new segment {}", ret);
        return ret;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("baseXid", baseXid).add("endXid", endXid).add("completeCount", completeCount).toString();
    }

    long getBaseXid() {
        return baseXid;
    }

    long getEndXid() {
        return endXid;
    }

    OutboundQueueEntry getEntry(final int offset) {
        return entries[offset];
    }

    private boolean xidInRange(final long xid) {
        return xid < endXid && (xid >= baseXid || baseXid > endXid);
    }

    private static boolean completeEntry(final OutboundQueueEntry entry, final OfHeader response) {
        if (response instanceof Error) {
            final Error err = (Error)response;
            LOG.debug("Device-reported request XID {} failed {}:{}", response.getXid(), err.getTypeString(), err.getCodeString());
            entry.fail(new DeviceRequestFailedException("Device-side failure", err));
            return true;
        }
        return entry.complete(response);
    }

    OutboundQueueEntry findEntry(final long xid) {
        if (! xidInRange(xid)) {
            LOG.debug("Queue {} {}/{} ignoring XID {}", this, baseXid, entries.length, xid);
            return null;
        }
        final int offset = (int)(xid - baseXid);
        return entries[offset];
    }

    OutboundQueueEntry pairRequest(final OfHeader response) {
        // Explicitly 'long' to force unboxing before performing operations
        final long xid = response.getXid();
        if (!xidInRange(xid)) {
            LOG.debug("Queue {} {}/{} ignoring XID {}", this, baseXid, entries.length, xid);
            return null;
        }

        final int offset = (int) (xid - baseXid);
        final OutboundQueueEntry entry = entries[offset];
        if (entry.isCompleted()) {
            LOG.debug("Entry {} already is completed, not accepting response {}", entry, response);
            return null;
        }

        if (entry.isBarrier()) {
            // This has been a barrier -- make sure we complete all preceding requests.
            // XXX: Barriers are expected to complete in one message.
            //      If this assumption is changed, this logic will need to be expanded
            //      to ensure that the requests implied by the barrier are reported as
            //      completed *after* the barrier.
            LOG.trace("Barrier XID {} completed, cascading completion to XIDs {} to {}", xid, baseXid + lastBarrierOffset + 1, xid - 1);
            completeRequests(offset);
            lastBarrierOffset = offset;

            final boolean success = completeEntry(entry, response);
            Verify.verify(success, "Barrier request failed to complete");
            completeCount++;
        } else if (completeEntry(entry, response)) {
            completeCount++;
        }

        return entry;
    }

    private void completeRequests(final int toOffset) {
        for (int i = lastBarrierOffset + 1; i < toOffset; ++i) {
            final OutboundQueueEntry entry = entries[i];
            if (!entry.isCompleted() && entry.complete(null)) {
                completeCount++;
            }
        }
    }

    void completeAll() {
        completeRequests(entries.length);
    }

    int failAll(final OutboundQueueException cause) {
        int ret = 0;
        for (int i = lastBarrierOffset + 1; i < entries.length; ++i) {
            final OutboundQueueEntry entry = entries[i];
            if (!entry.isCommitted()) {
                break;
            }

            if (!entry.isCompleted()) {
                entry.fail(cause);
                completeCount++;
                ret++;
            }
        }

        return ret;
    }

    boolean isComplete() {
        return completeCount >= entries.length;
    }

    void recycle() {
        for (final OutboundQueueEntry e : entries) {
            e.reset();
        }

        CACHE.offer(new QueueRef(REF_QUEUE, entries));
    }
}
