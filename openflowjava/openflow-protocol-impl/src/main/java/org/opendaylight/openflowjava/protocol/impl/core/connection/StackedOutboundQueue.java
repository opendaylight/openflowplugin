/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.util.concurrent.FutureCallback;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Function;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class StackedOutboundQueue extends AbstractStackedOutboundQueue {
    private static final Logger LOG = LoggerFactory.getLogger(StackedOutboundQueue.class);
    private static final AtomicLongFieldUpdater<StackedOutboundQueue> BARRIER_XID_UPDATER = AtomicLongFieldUpdater.newUpdater(StackedOutboundQueue.class, "barrierXid");

    private volatile long barrierXid = -1;

    StackedOutboundQueue(final AbstractOutboundQueueManager<?, ?> manager) {
        super(manager);
    }

    /*
     * This method is expected to be called from multiple threads concurrently
     */
    @Override
    public void commitEntry(final Long xid, final OfHeader message, final FutureCallback<OfHeader> callback,
            final Function<OfHeader, Boolean> isCompletedFunction) {
        final OutboundQueueEntry entry = getEntry(xid);

        entry.commit(message, callback, isCompletedFunction);
        if (entry.isBarrier()) {
            long my = xid;
            for (;;) {
                final long prev = BARRIER_XID_UPDATER.getAndSet(this, my);
                if (prev < my) {
                    LOG.debug("Queue {} recorded pending barrier XID {}", this, my);
                    break;
                }

                // We have traveled back, recover
                LOG.debug("Queue {} retry pending barrier {} >= {}", this, prev, my);
                my = prev;
            }
        }

        LOG.trace("Queue {} committed XID {}", this, xid);
        manager.ensureFlushing();
    }

    Long reserveBarrierIfNeeded() {
        if (isBarrierNeeded()) {
            return reserveEntry();
        }
        return null;
    }

    /**
     * Checks if Barrier Request is the last message enqueued. If not, one needs
     * to be scheduled in order to collect data about previous messages.
     * @return true if last enqueued message is Barrier Request, false otherwise
     */
    boolean isBarrierNeeded() {
        final long bXid = barrierXid;
        final long fXid = firstSegment.getBaseXid() + flushOffset;
        if (bXid >= fXid) {
            LOG.debug("Barrier found at XID {} (currently at {})", bXid, fXid);
            return false;
        }
        return true;
    }
}
