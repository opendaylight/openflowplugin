/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Channel handler which bypasses wraps on top of normal Netty pipeline, allowing
 * writes to be enqueued from any thread, it then schedules a task pipeline task,
 * which shuffles messages from the queue into the pipeline.
 *
 * Note this is an *Inbound* handler, as it reacts to channel writability changing,
 * which in the Netty vocabulary is an inbound event. This has already changed in
 * the Netty 5.0.0 API, where Handlers are unified.
 */
final class ChannelOutboundQueue extends ChannelInboundHandlerAdapter {
    public interface MessageHolder<T> {
        /**
         * Take ownership of the encapsulated listener. Guaranteed to
         * be called at most once.
         *
         * @return listener encapsulated in the holder, may be null
         * @throws IllegalStateException if the listener is no longer
         *         available (for example because it has already been
         *         taken).
         */
        GenericFutureListener<Future<Void>> takeListener();

        /**
         * Take ownership of the encapsulated message. Guaranteed to be
         * called at most once.
         *
         * @return message encapsulated in the holder, may not be null
         * @throws IllegalStateException if the message is no longer
         *         available (for example because it has already been
         *         taken).
         */
        T takeMessage();
    }

    /**
     * This is the default upper bound we place on the flush task running
     * a single iteration. We relinquish control after about this amount
     * of time.
     */
    private static final long DEFAULT_WORKTIME_MICROS = TimeUnit.MILLISECONDS.toMicros(100);

    /**
     * We re-check the time spent flushing every this many messages. We do this because
     * checking after each message may prove to be CPU-intensive. Set to Integer.MAX_VALUE
     * or similar to disable the feature.
     */
    private static final int WORKTIME_RECHECK_MSGS = 64;
    private static final Logger LOG = LoggerFactory.getLogger(ChannelOutboundQueue.class);

    // Passed to executor to request triggering of flush
    private final Runnable flushRunnable = new Runnable() {
        @Override
        public void run() {
            ChannelOutboundQueue.this.flush();
        }
    };

    /*
     * Instead of using an AtomicBoolean object, we use these two. It saves us
     * from allocating an extra object.
     */
    private static final AtomicIntegerFieldUpdater<ChannelOutboundQueue> FLUSH_SCHEDULED_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(ChannelOutboundQueue.class, "flushScheduled");
    private volatile int flushScheduled = 0;

    private final Queue<MessageHolder<?>> queue;
    private final long maxWorkTime;
    private final Channel channel;
    private final InetSocketAddress address;

    public ChannelOutboundQueue(final Channel channel, final int queueDepth, final InetSocketAddress address) {
        Preconditions.checkArgument(queueDepth > 0, "Queue depth has to be positive");

        /*
         * This looks like a good trade-off for throughput. Alternative is
         * to use an ArrayBlockingQueue -- but that uses a single lock to
         * synchronize both producers and consumers, potentially leading
         * to less throughput.
         */
        this.queue = new LinkedBlockingQueue<>(queueDepth);
        this.channel = Preconditions.checkNotNull(channel);
        this.maxWorkTime = TimeUnit.MICROSECONDS.toNanos(DEFAULT_WORKTIME_MICROS);
        this.address = address;
    }

    /**
     * Enqueue a message holder for transmission. Is a thread-safe entry point
     * for the channel. If the cannot be placed on the queue, this
     *
     * @param holder MessageHolder which should be enqueue
     * @return Success indicator, true if the enqueue operation succeeded,
     *         false if the queue is full.
     */
    public boolean enqueue(final MessageHolder<?> holder) {
        LOG.trace("Enqueuing message {}", holder);
        if (queue.offer(holder)) {
            LOG.trace("Message enqueued");
            conditionalFlush();
            return true;
        }

        LOG.debug("Message queue is full");
        return false;
    }

    private void scheduleFlush(final EventExecutor executor) {
        if (FLUSH_SCHEDULED_UPDATER.compareAndSet(this, 0, 1)) {
            LOG.trace("Scheduling flush task");
            executor.execute(flushRunnable);
        } else {
            LOG.trace("Flush task is already present");
        }
    }

    /**
     * Schedule a queue flush if it is not empty and the channel is found
     * to be writable.
     */
    private void conditionalFlush() {
        if (queue.isEmpty()) {
            LOG.trace("Queue is empty, flush not needed");
            return;
        }
        if (!channel.isWritable()) {
            LOG.trace("Channel {} is not writable, not issuing a flush", channel);
            return;
        }

        scheduleFlush(channel.pipeline().lastContext().executor());
    }

    /*
     * The synchronized keyword should be unnecessary, really, but it enforces
     * queue order should something go terribly wrong. It should be completely
     * uncontended.
     */
    private synchronized void flush() {

        final long start = System.nanoTime();
        final long deadline = start + maxWorkTime;

        LOG.debug("Dequeuing messages to channel {}", channel);

        long messages = 0;
        for (;; ++messages) {
            if (!channel.isWritable()) {
                LOG.trace("Channel is no longer writable");
                break;
            }

            final MessageHolder<?> h = queue.poll();
            if (h == null) {
                LOG.trace("The queue is completely drained");
                break;
            }

            final GenericFutureListener<Future<Void>> l = h.takeListener();

            final ChannelFuture p;
            if (address == null) {
                p = channel.write(new MessageListenerWrapper(h.takeMessage(), l));
            } else {
                p = channel.write(new UdpMessageListenerWrapper(h.takeMessage(), l, address));
            }
            if (l != null) {
                p.addListener(l);
            }

            /*
             * Check every WORKTIME_RECHECK_MSGS for exceeded time.
             *
             * XXX: given we already measure our flushing throughput, we
             *      should be able to perform dynamic adjustments here.
             *      is that additional complexity needed, though?
             */
            if ((messages % WORKTIME_RECHECK_MSGS) == 0 && System.nanoTime() >= deadline) {
                LOG.trace("Exceeded allotted work time {}us",
                        TimeUnit.NANOSECONDS.toMicros(maxWorkTime));
                break;
            }
        }

        if (messages > 0) {
            LOG.debug("Flushing {} message(s) to channel {}", messages, channel);
            channel.flush();
        }

        if (LOG.isDebugEnabled()) {
            final long stop = System.nanoTime();
            LOG.debug("Flushed {} messages in {}us to channel {}",
                messages, TimeUnit.NANOSECONDS.toMicros(stop - start), channel);
        }

        /*
         * We are almost ready to terminate. This is a bit tricky, because
         * we do not want to have a race window where a message would be
         * stuck on the queue without a flush being scheduled.
         *
         * So we mark ourselves as not running and then re-check if a
         * flush out is needed. That will re-synchronized with other threads
         * such that only one flush is scheduled at any given time.
         */
        if (!FLUSH_SCHEDULED_UPDATER.compareAndSet(this, 1, 0)) {
            LOG.warn("Channel {} queue {} flusher found unscheduled", channel, queue);
        }

        conditionalFlush();
    }

    private void conditionalFlush(final ChannelHandlerContext ctx) {
        Preconditions.checkState(ctx.channel().equals(channel), "Inconsistent channel %s with context %s", channel, ctx);
        conditionalFlush();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        conditionalFlush(ctx);
    }

    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        conditionalFlush(ctx);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        long entries = 0;
        LOG.debug("Channel shutdown, flushing queue...");
        final Future<Void> result = ctx.newFailedFuture(new RejectedExecutionException("Channel disconnected"));
        while (true) {
            final MessageHolder<?> e = queue.poll();
            if (e == null) {
                break;
            }

            e.takeListener().operationComplete(result);
            entries++;
        }

        LOG.debug("Flushed {} queue entries", entries);
    }

    @Override
    public String toString() {
        return String.format("Channel %s queue [%s messages flushing=%s]", channel, queue.size(), flushScheduled);
    }
}
