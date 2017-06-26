/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class capsulate basic processing for stacking requests for netty channel
 * and provide functionality for pairing request/response device message communication.
 */
abstract class AbstractOutboundQueueManager<T extends OutboundQueueHandler, O extends AbstractStackedOutboundQueue>
        extends ChannelInboundHandlerAdapter
        implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractOutboundQueueManager.class);

    private static enum PipelineState {
        /**
         * Netty thread is potentially idle, no assumptions
         * can be made about its state.
         */
        IDLE,
        /**
         * Netty thread is currently reading, once the read completes,
         * if will flush the queue in the {@link #WRITING} state.
         */
        READING,
        /**
         * Netty thread is currently performing a flush on the queue.
         * It will then transition to {@link #IDLE} state.
         */
        WRITING,
    }

    /**
     * Default low write watermark. Channel will become writable when number of outstanding
     * bytes dips below this value.
     */
    private static final int DEFAULT_LOW_WATERMARK = 128 * 1024;

    /**
     * Default write high watermark. Channel will become un-writable when number of
     * outstanding bytes hits this value.
     */
    private static final int DEFAULT_HIGH_WATERMARK = DEFAULT_LOW_WATERMARK * 2;

    private final AtomicBoolean flushScheduled = new AtomicBoolean();
    protected final ConnectionAdapterImpl parent;
    protected final InetSocketAddress address;
    protected final O currentQueue;
    private final T handler;

    // Accessed concurrently
    private volatile PipelineState state = PipelineState.IDLE;

    // Updated from netty only
    private boolean alreadyReading;
    protected boolean shuttingDown;

    // Passed to executor to request triggering of flush
    protected final Runnable flushRunnable = new Runnable() {
        @Override
        public void run() {
            flush();
        }
    };

    AbstractOutboundQueueManager(final ConnectionAdapterImpl parent, final InetSocketAddress address, final T handler) {
        this.parent = Preconditions.checkNotNull(parent);
        this.handler = Preconditions.checkNotNull(handler);
        this.address = address;
        /* Note: don't wish to use reflection here */
        currentQueue = initializeStackedOutboudnqueue();
        LOG.debug("Queue manager instantiated with queue {}", currentQueue);

        handler.onConnectionQueueChanged(currentQueue);
    }

    /**
     * Method has to initialize some child of {@link AbstractStackedOutboundQueue}
     *
     * @return correct implementation of StacketOutboundqueue
     */
    protected abstract O initializeStackedOutboudnqueue();

    @Override
    public void close() {
        handler.onConnectionQueueChanged(null);
    }

    @Override
    public String toString() {
        return String.format("Channel %s queue [flushing=%s]", parent.getChannel(), flushScheduled.get());
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        /*
         * Tune channel write buffering. We increase the writability window
         * to ensure we can flush an entire queue segment in one go. We definitely
         * want to keep the difference above 64k, as that will ensure we use jam-packed
         * TCP packets. UDP will fragment as appropriate.
         */
        ctx.channel().config().setWriteBufferHighWaterMark(DEFAULT_HIGH_WATERMARK);
        ctx.channel().config().setWriteBufferLowWaterMark(DEFAULT_LOW_WATERMARK);

        super.handlerAdded(ctx);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        conditionalFlush();
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);

        // Run flush regardless of writability. This is not strictly required, as
        // there may be a scheduled flush. Instead of canceling it, which is expensive,
        // we'll steal its work. Note that more work may accumulate in the time window
        // between now and when the task will run, so it may not be a no-op after all.
        //
        // The reason for this is to fill the output buffer before we go into selection
        // phase. This will make sure the pipe is full (in which case our next wake up
        // will be the queue becoming writable).
        writeAndFlush();
        alreadyReading = false;
    }

    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);

        // The channel is writable again. There may be a flush task on the way, but let's
        // steal its work, potentially decreasing latency. Since there is a window between
        // now and when it will run, it may still pick up some more work to do.
        LOG.debug("Channel {} writability changed, invoking flush", parent.getChannel());
        writeAndFlush();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        // First of all, delegates disconnect event notification into ConnectionAdapter -> OF Plugin -> queue.close()
        // -> queueHandler.onConnectionQueueChanged(null). The last call causes that no more entries are enqueued
        // in the queue.
        super.channelInactive(ctx);

        LOG.debug("Channel {} initiating shutdown...", ctx.channel());

        // Then we start queue shutdown, start counting written messages (so that we don't keep sending messages
        // indefinitely) and failing not completed entries.
        shuttingDown = true;
        final long entries = currentQueue.startShutdown();
        LOG.debug("Cleared {} queue entries from channel {}", entries, ctx.channel());

        // Finally, we schedule flush task that will take care of unflushed entries. We also cover the case,
        // when there is more than shutdownOffset messages enqueued in unflushed segments
        // (AbstractStackedOutboundQueue#finishShutdown()).
        scheduleFlush();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        // Netty does not provide a 'start reading' callback, so this is our first
        // (and repeated) chance to detect reading. Since this callback can be invoked
        // multiple times, we keep a boolean we check. That prevents a volatile write
        // on repeated invocations. It will be cleared in channelReadComplete().
        if (!alreadyReading) {
            alreadyReading = true;
            state = PipelineState.READING;
        }
        super.channelRead(ctx, msg);
    }

    /**
     * Invoked whenever a message comes in from the switch. Runs matching
     * on all active queues in an attempt to complete a previous request.
     *
     * @param message Potential response message
     * @return True if the message matched a previous request, false otherwise.
     */
    boolean onMessage(final OfHeader message) {
        LOG.trace("Attempting to pair message {} to a request", message);

        return currentQueue.pairRequest(message);
    }

    T getHandler() {
        return handler;
    }

    void ensureFlushing() {
        // If the channel is not writable, there's no point in waking up,
        // once we become writable, we will run a full flush
        if (!parent.getChannel().isWritable()) {
            return;
        }

        // We are currently reading something, just a quick sync to ensure we will in fact
        // flush state.
        final PipelineState localState = state;
        LOG.debug("Synchronize on pipeline state {}", localState);
        switch (localState) {
        case READING:
            // Netty thread is currently reading, it will flush the pipeline once it
            // finishes reading. This is a no-op situation.
            break;
        case WRITING:
        case IDLE:
        default:
            // We cannot rely on the change being flushed, schedule a request
            scheduleFlush();
        }
    }

    /**
     * Method immediately response on Echo message.
     *
     * @param message incoming Echo message from device
     */
    void onEchoRequest(final EchoRequestMessage message) {
        final EchoReplyInput reply = new EchoReplyInputBuilder().setData(message.getData())
                .setVersion(message.getVersion()).setXid(message.getXid()).build();
        parent.getChannel().writeAndFlush(makeMessageListenerWrapper(reply));
    }

    /**
     * Wraps outgoing message and includes listener attached to this message
     * which is send to OFEncoder for serialization. Correct wrapper is
     * selected by communication pipeline.
     *
     * @param message
     * @param now
     */
    void writeMessage(final OfHeader message, final long now) {
        final Object wrapper = makeMessageListenerWrapper(message);
        parent.getChannel().write(wrapper);
    }

    /**
     * Wraps outgoing message and includes listener attached to this message
     * which is send to OFEncoder for serialization. Correct wrapper is
     * selected by communication pipeline.
     *
     * @return
     */
    protected Object makeMessageListenerWrapper(@Nonnull final OfHeader msg) {
        Preconditions.checkArgument(msg != null);

        if (address == null) {
            return new MessageListenerWrapper(msg, LOG_ENCODER_LISTENER);
        }
        return new UdpMessageListenerWrapper(msg, LOG_ENCODER_LISTENER, address);
    }

    /* NPE are coming from {@link OFEncoder#encode} from catch block and we don't wish to lost it */
    private static final GenericFutureListener<Future<Void>> LOG_ENCODER_LISTENER = new GenericFutureListener<Future<Void>>() {

        private final Logger LOG = LoggerFactory.getLogger(GenericFutureListener.class);

        @Override
        public void operationComplete(final Future<Void> future) throws Exception {
            if (future.cause() != null) {
                LOG.warn("Message encoding fail !", future.cause());
            }
        }
    };

    /**
     * Perform a single flush operation. We keep it here so we do not generate
     * syntetic accessors for private fields. Otherwise it could be moved into {@link #flushRunnable}.
     */
    protected void flush() {
        // If the channel is gone, just flush whatever is not completed
        if (!shuttingDown) {
            LOG.trace("Dequeuing messages to channel {}", parent.getChannel());
            writeAndFlush();
            rescheduleFlush();
        } else {
            close();
            if (currentQueue.finishShutdown(parent.getChannel())) {
            	LOG.debug("Channel {} shutdown complete", parent.getChannel());
            } else {
            	LOG.trace("Channel {} current queue not completely flushed yet", parent.getChannel());
            	rescheduleFlush();
            }
        }
    }

    private void scheduleFlush() {
        if (flushScheduled.compareAndSet(false, true)) {
            LOG.trace("Scheduling flush task on channel {}", parent.getChannel());
            parent.getChannel().eventLoop().execute(flushRunnable);
        } else {
            LOG.trace("Flush task is already present on channel {}", parent.getChannel());
        }
    }

    private void writeAndFlush() {
        state = PipelineState.WRITING;

        final long start = System.nanoTime();

        final int entries = currentQueue.writeEntries(parent.getChannel(), start);
        if (entries > 0) {
            LOG.trace("Flushing channel {}", parent.getChannel());
            parent.getChannel().flush();
        }

        if (LOG.isDebugEnabled()) {
            final long stop = System.nanoTime();
            LOG.debug("Flushed {} messages to channel {} in {}us", entries, parent.getChannel(),
                    TimeUnit.NANOSECONDS.toMicros(stop - start));
        }

        state = PipelineState.IDLE;
    }

    private void rescheduleFlush() {
        /*
         * We are almost ready to terminate. This is a bit tricky, because
         * we do not want to have a race window where a message would be
         * stuck on the queue without a flush being scheduled.
         * So we mark ourselves as not running and then re-check if a
         * flush out is needed. That will re-synchronized with other threads
         * such that only one flush is scheduled at any given time.
         */
        if (!flushScheduled.compareAndSet(true, false)) {
            LOG.warn("Channel {} queue {} flusher found unscheduled", parent.getChannel(), this);
        }

        conditionalFlush();
    }

    /**
     * Schedule a queue flush if it is not empty and the channel is found
     * to be writable. May only be called from Netty context.
     */
    private void conditionalFlush() {
        if (currentQueue.needsFlush()) {
            if (shuttingDown || parent.getChannel().isWritable()) {
                scheduleFlush();
            } else {
                LOG.debug("Channel {} is not I/O ready, not scheduling a flush", parent.getChannel());
            }
        } else {
            LOG.trace("Queue is empty, no flush needed");
        }
    }
}
