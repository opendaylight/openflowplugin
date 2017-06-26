/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.util.concurrent.FutureCallback;

import io.netty.channel.Channel;

import java.util.function.Function;

import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class is designed for stacking Statistics and propagate immediate response for all
 * another requests.
 */
public class StackedOutboundQueueNoBarrier extends AbstractStackedOutboundQueue {

    private static final Logger LOG = LoggerFactory.getLogger(StackedOutboundQueueNoBarrier.class);

    StackedOutboundQueueNoBarrier(final AbstractOutboundQueueManager<?, ?> manager) {
        super(manager);
    }

    /*
     * This method is expected to be called from multiple threads concurrently
     */
    @Override
    public void commitEntry(final Long xid, final OfHeader message, final FutureCallback<OfHeader> callback,
            final Function<OfHeader, Boolean> isCompletedFunction) {
        final OutboundQueueEntry entry = getEntry(xid);

        if (message instanceof FlowModInput) {
            callback.onSuccess(null);
            entry.commit(message, null, isCompletedFunction);
        } else {
            entry.commit(message, callback, isCompletedFunction);
        }

        LOG.trace("Queue {} committed XID {}", this, xid);
        manager.ensureFlushing();
    }

    @Override
    int writeEntries(@Nonnull final Channel channel, final long now) {
        // Local cache
        StackedSegment segment = firstSegment;
        int entries = 0;

        while (channel.isWritable()) {
            final OutboundQueueEntry entry = segment.getEntry(flushOffset);
            if (!entry.isCommitted()) {
                LOG.debug("Queue {} XID {} segment {} offset {} not committed yet", this, segment.getBaseXid()
                        + flushOffset, segment, flushOffset);
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
                 * This costs us two slow paths, but hey, this should be very rare,
                 * so let's keep things simple.
                 */
                synchronized (unflushedSegments) {
                    LOG.debug("Flush offset {} unflushed segments {}", flushOffset, unflushedSegments.size());

                    // We may have raced ahead of reservation code and need to allocate a segment
                    ensureSegment(segment, flushOffset);

                    // Remove the segment, update the firstSegment and reset flushOffset
                    final StackedSegment oldSegment = unflushedSegments.remove(0);
                    oldSegment.completeAll();
                    uncompletedSegments.remove(oldSegment);
                    oldSegment.recycle();

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

}
