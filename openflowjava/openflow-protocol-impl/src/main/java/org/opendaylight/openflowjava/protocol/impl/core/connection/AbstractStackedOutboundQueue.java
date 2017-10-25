/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractStackedOutboundQueue implements OutboundQueue {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractStackedOutboundQueue.class);
    protected static final AtomicLongFieldUpdater<AbstractStackedOutboundQueue> LAST_XID_OFFSET_UPDATER = AtomicLongFieldUpdater
            .newUpdater(AbstractStackedOutboundQueue.class, "lastXid");

    @GuardedBy("unflushedSegments")
    protected volatile StackedSegment firstSegment;
    @GuardedBy("unflushedSegments")
    protected final List<StackedSegment> unflushedSegments = new ArrayList<>(2);
    @GuardedBy("unflushedSegments")
    protected final List<StackedSegment> uncompletedSegments = new ArrayList<>(2);

    private volatile long lastXid = -1;
    private volatile long allocatedXid = -1;

    @GuardedBy("unflushedSegments")
    protected Integer shutdownOffset;

    // Accessed from Netty only
    protected int flushOffset;

    protected final AbstractOutboundQueueManager<?, ?> manager;

    AbstractStackedOutboundQueue(final AbstractOutboundQueueManager<?, ?> manager) {
        this.manager = Preconditions.checkNotNull(manager);
        firstSegment = StackedSegment.create(0L);
        uncompletedSegments.add(firstSegment);
        unflushedSegments.add(firstSegment);
    }

    @Override
    public void commitEntry(final Long xid, final OfHeader message, final FutureCallback<OfHeader> callback) {
        commitEntry(xid, message, callback, OutboundQueueEntry.DEFAULT_IS_COMPLETE);
    }

    @GuardedBy("unflushedSegments")
    protected void ensureSegment(final StackedSegment first, final int offset) {
        final int segmentOffset = offset / StackedSegment.SEGMENT_SIZE;
        LOG.debug("Queue {} slow offset {} maps to {} segments {}", this, offset, segmentOffset, unflushedSegments.size());

        for (int i = unflushedSegments.size(); i <= segmentOffset; ++i) {
            final StackedSegment newSegment = StackedSegment.create(first.getBaseXid() + (StackedSegment.SEGMENT_SIZE * i));
            LOG.debug("Adding segment {}", newSegment);
            unflushedSegments.add(newSegment);
        }

        allocatedXid = unflushedSegments.get(unflushedSegments.size() - 1).getEndXid();
    }

    /*
     * This method is expected to be called from multiple threads concurrently.
     */
    @Override
    public Long reserveEntry() {
        final long xid = LAST_XID_OFFSET_UPDATER.incrementAndGet(this);
        final StackedSegment fastSegment = firstSegment;

        if (xid >= fastSegment.getBaseXid() + StackedSegment.SEGMENT_SIZE) {
            if (xid >= allocatedXid) {
                // Multiple segments, this a slow path
                LOG.debug("Queue {} falling back to slow reservation for XID {}", this, xid);

                synchronized (unflushedSegments) {
                    LOG.debug("Queue {} executing slow reservation for XID {}", this, xid);

                    // Shutdown was scheduled, need to fail the reservation
                    if (shutdownOffset != null) {
                        LOG.debug("Queue {} is being shutdown, failing reservation", this);
                        return null;
                    }

                    // Ensure we have the appropriate segment for the specified XID
                    final StackedSegment slowSegment = firstSegment;
                    final int slowOffset = (int) (xid - slowSegment.getBaseXid());
                    Verify.verify(slowOffset >= 0);

                    // Now, we let's see if we need to allocate a new segment
                    ensureSegment(slowSegment, slowOffset);

                    LOG.debug("Queue {} slow reservation finished", this);
                }
            } else {
                LOG.debug("Queue {} XID {} is already backed", this, xid);
            }
        }

        LOG.trace("Queue {} allocated XID {}", this, xid);
        return xid;
    }

    /**
     * Write some entries from the queue to the channel. Guaranteed to run
     * in the corresponding EventLoop.
     *
     * @param channel Channel onto which we are writing
     * @param now
     * @return Number of entries written out
     */
    int writeEntries(@Nonnull final Channel channel, final long now) {
        // Local cache
        StackedSegment segment = firstSegment;
        int entries = 0;

        while (channel.isWritable()) {
            final OutboundQueueEntry entry = segment.getEntry(flushOffset);
            if (!entry.isCommitted()) {
                LOG.debug("Queue {} XID {} segment {} offset {} not committed yet", this, segment.getBaseXid() + flushOffset, segment, flushOffset);
                break;
            }

            LOG.trace("Queue {} flushing entry at offset {}", this, flushOffset);
            final OfHeader message = entry.takeMessage();
            flushOffset++;
            entries++;

            if (message != null) {
                manager.writeMessage(message, now);
            } else {
                entry.complete(null);
            }

            if (flushOffset >= StackedSegment.SEGMENT_SIZE) {
                /*
                 * Slow path: purge the current segment unless it's the last one.
                 * If it is, we leave it for replacement when a new reservation
                 * is run on it.
                 *
                 * This costs us two slow paths, but hey, this should be very rare,
                 * so let's keep things simple.
                 */
                synchronized (unflushedSegments) {
                    LOG.debug("Flush offset {} unflushed segments {}", flushOffset, unflushedSegments.size());

                    // We may have raced ahead of reservation code and need to allocate a segment
                    ensureSegment(segment, flushOffset);

                    // Remove the segment, update the firstSegment and reset flushOffset
                    final StackedSegment oldSegment = unflushedSegments.remove(0);
                    if (oldSegment.isComplete()) {
                        uncompletedSegments.remove(oldSegment);
                        oldSegment.recycle();
                    }

                    // Reset the first segment and add it to the uncompleted list
                    segment = unflushedSegments.get(0);
                    uncompletedSegments.add(segment);

                    // Update the shutdown offset
                    if (shutdownOffset != null) {
                        shutdownOffset -= StackedSegment.SEGMENT_SIZE;
                    }

                    // Allow reservations back on the fast path by publishing the new first segment
                    firstSegment = segment;

                    flushOffset = 0;
                    LOG.debug("Queue {} flush moved to segment {}", this, segment);
                }
            }
        }

        return entries;
    }

    boolean pairRequest(final OfHeader message) {
        Iterator<StackedSegment> it = uncompletedSegments.iterator();
        while (it.hasNext()) {
            final StackedSegment queue = it.next();
            final OutboundQueueEntry entry = queue.pairRequest(message);
            if (entry == null) {
                continue;
            }

            LOG.trace("Queue {} accepted response {}", queue, message);

            // This has been a barrier request, we need to flush all
            // previous queues
            if (entry.isBarrier() && uncompletedSegments.size() > 1) {
                LOG.trace("Queue {} indicated request was a barrier", queue);

                it = uncompletedSegments.iterator();
                while (it.hasNext()) {
                    final StackedSegment q = it.next();

                    // We want to complete all queues before the current one, we will
                    // complete the current queue below
                    if (!queue.equals(q)) {
                        LOG.trace("Queue {} is implied finished", q);
                        q.completeAll();
                        it.remove();
                        q.recycle();
                    } else {
                        break;
                    }
                }
            }

            if (queue.isComplete()) {
                LOG.trace("Queue {} is finished", queue);
                it.remove();
                queue.recycle();
            }

            return true;
        }

        LOG.debug("Failed to find completion for message {}", message);
        return false;
    }

    boolean needsFlush() {
        // flushOffset always points to the first entry, which can be changed only
        // from Netty, so we are fine here.
        if (firstSegment.getBaseXid() + flushOffset > lastXid) {
            return false;
        }

        if (shutdownOffset != null && flushOffset >= shutdownOffset) {
            return false;
        }

        return firstSegment.getEntry(flushOffset).isCommitted();
    }

    long startShutdown() {
        /*
         * We are dealing with a multi-threaded shutdown, as the user may still
         * be reserving entries in the queue. We are executing in a netty thread,
         * so neither flush nor barrier can be running, which is good news.
         * We will eat up all the slots in the queue here and mark the offset first
         * reserved offset and free up all the cached queues. We then schedule
         * the flush task, which will deal with the rest of the shutdown process.
         */
        synchronized (unflushedSegments) {
            // Increment the offset by the segment size, preventing fast path allocations,
            // since we are holding the slow path lock, any reservations will see the queue
            // in shutdown and fail accordingly.
            final long xid = LAST_XID_OFFSET_UPDATER.addAndGet(this, StackedSegment.SEGMENT_SIZE);
            shutdownOffset = (int) (xid - firstSegment.getBaseXid() - StackedSegment.SEGMENT_SIZE);

            // Fails all uncompleted entries, because they will never be completed due to disconnected channel.
            return lockedFailSegments(uncompletedSegments.iterator());
        }
    }

    /**
     * Checks if the shutdown is in final phase -> all allowed entries (number of entries < shutdownOffset) are flushed
     * and fails all not completed entries (if in final phase)
     * @param channel netty channel
     * @return true if in final phase, false if a flush is needed
     */
    boolean finishShutdown(final Channel channel) {
        boolean needsFlush;
        synchronized (unflushedSegments) {
            // Fails all entries, that were flushed in shutdownOffset (became uncompleted)
            // - they will never be completed due to disconnected channel.
            lockedFailSegments(uncompletedSegments.iterator());
            // If no further flush is needed or we are not able to write to channel anymore, then we fail all unflushed
            // segments, so that each enqueued entry is reported as unsuccessful due to channel disconnection.
            // No further entries should be enqueued by this time.
            needsFlush = channel.isWritable() && needsFlush();
            if (!needsFlush) {
                lockedFailSegments(unflushedSegments.iterator());
            }
        }
        return !needsFlush;
    }

    protected OutboundQueueEntry getEntry(final Long xid) {
        final StackedSegment fastSegment = firstSegment;
        final long calcOffset = xid - fastSegment.getBaseXid();
        Preconditions.checkArgument(calcOffset >= 0, "Commit of XID %s does not match up with base XID %s", xid, fastSegment.getBaseXid());

        Verify.verify(calcOffset <= Integer.MAX_VALUE);
        final int fastOffset = (int) calcOffset;

        if (fastOffset >= StackedSegment.SEGMENT_SIZE) {
            LOG.debug("Queue {} falling back to slow commit of XID {} at offset {}", this, xid, fastOffset);

            final StackedSegment segment;
            final int slowOffset;
            synchronized (unflushedSegments) {
                final StackedSegment slowSegment = firstSegment;
                final long slowCalcOffset = xid - slowSegment.getBaseXid();
                Verify.verify(slowCalcOffset >= 0 && slowCalcOffset <= Integer.MAX_VALUE);
                slowOffset = (int) slowCalcOffset;

                LOG.debug("Queue {} recalculated offset of XID {} to {}", this, xid, slowOffset);
                segment = unflushedSegments.get(slowOffset / StackedSegment.SEGMENT_SIZE);
            }

            final int segOffset = slowOffset % StackedSegment.SEGMENT_SIZE;
            LOG.debug("Queue {} slow commit of XID {} completed at offset {} (segment {} offset {})", this, xid, slowOffset, segment, segOffset);
            return segment.getEntry(segOffset);
        }
        return fastSegment.getEntry(fastOffset);
    }

    /**
     * Fails not completed entries in segments and frees completed segments
     * @param iterator list of segments to be failed
     * @return number of failed entries
     */
    @GuardedBy("unflushedSegments")
    private long lockedFailSegments(Iterator<StackedSegment> iterator) {
        long entries = 0;

        // Fail all queues
        while (iterator.hasNext()) {
            final StackedSegment segment = iterator.next();

            entries += segment.failAll(OutboundQueueException.DEVICE_DISCONNECTED);
            if (segment.isComplete()) {
                LOG.trace("Cleared segment {}", segment);
                iterator.remove();
            }
        }

        return entries;
    }

}
