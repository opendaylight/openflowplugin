/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.opendaylight.util.Log;
import org.opendaylight.util.ThroughputTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.*;
import static org.opendaylight.util.Log.stackTraceSnippet;

/**
 * Abstraction of a bidirectional message transfer buffer backed by a
 * {@link ByteChannel byte channel} and driven by an {@link IOLoop I/O
 * loop}.
 * <p>
 * Implementations are expected to provide means to get and put a message into
 * the provided byte buffers.
 * <p>
 * Writers feed the data in form of messages via one of the
 * {@link MessageBuffer#queue} methods and the data will get flushed
 *
 * @param <M> message type
 *
 * @author Thomas Vachuska
 */
public abstract class MessageBuffer<M extends Message> {

    /** Shared logger. */
    protected Logger log = LoggerFactory.getLogger(MessageBuffer.class);

    /** Initial buffer size for in-bound & out-bound byte buffers. */
    protected static final int BUFFER_SIZE = 64 * 1024;

    /** Default growth factor for the out-bound byte buffer. */
    protected static final double GROWTH_FACTOR = 1.618;

    /** Default maximum age in milliseconds before the buffer becomes stale. */
    protected static final int MAX_AGE = 1000;

    static final String E_DEQUEUE = "Unable to dequeue messages";
    static final String E_FLUSH = "Unable to flush write buffer {}";
    static final String E_CLOSE = "Unable to cleanly close connection {}";

    // Auxiliary flags to make code more readable when processing handshake
    private static final boolean BAIL = true;
    private static final boolean CONTINUE = false;

    private static final String BROKEN_PIPE = "Broken pipe";

    final ThroughputTracker imtt = new ThroughputTracker();
    final ThroughputTracker ibtt = new ThroughputTracker();
    final ThroughputTracker omtt = new ThroughputTracker();
    final ThroughputTracker obtt = new ThroughputTracker();

    private ByteBuffer ib = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private ByteBuffer ob = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private ByteBuffer ibApp;
    private ByteBuffer obApp;

    private final List<M> noMessages = Collections.emptyList();
    private final SSLEngine engine;

    private final ByteChannel ch;
    private final IOLoop<M, ?> loop;
    private final boolean secure;
    private final boolean sslClient;

    private volatile boolean discarded = false;
    private SelectionKey key;

    volatile boolean writePending;
    volatile boolean writeOccurred;
    Exception flushError;
    long lastOp;


    /**
     * Creates a message buffer backed by the specified byte channel and
     * driven by the given I/O loop.
     *
     * @param ch backing socket channel
     * @param loop driver IO loop
     */
    // TODO: Add initial buffer size?
    // FIXME: Add client/server mode for secure transport
    protected MessageBuffer(ByteChannel ch, IOLoop<M, ?> loop) {
        this(ch, loop, null);
    }

    /**
     * Creates a message buffer backed by the specified byte channel and
     * driven by the given I/O loop.
     *
     * @param ch backing socket channel
     * @param loop driver IO loop
     * @param sslContext TLS secure context
     */
    protected MessageBuffer(ByteChannel ch, IOLoop<M, ?> loop,
                            SSLContext sslContext) {
        this.secure = sslContext != null;
        this.loop = loop;
        this.ch = ch;
        this.sslClient = false; // TODO: plumb through - for now assume server

        if (secure) {
            engine = sslContext.createSSLEngine();
            engine.setUseClientMode(sslClient);
            engine.setNeedClientAuth(true);
            ibApp = ByteBuffer.allocateDirect(BUFFER_SIZE);
            obApp = ByteBuffer.allocateDirect(BUFFER_SIZE);
        } else {
            engine = null;
        }
    }

    /**
     * Returns true if this buffer has already been discarded. That is,
     * {@link #discard()} has been called.
     *
     * @return true if this buffer has been discarded
     */
    public synchronized boolean alreadyDiscarded() {
        return discarded;
    }

    /**
     * Discards the message buffer. This involves freezing all throughput
     * trackers, closing the backing channel and unregistering it from the
     * driver loop.
     */
    public void discard() {
        synchronized (this) {
            if (discarded)
                return;
            discarded = true;
        }

        imtt.freeze();
        ibtt.freeze();
        omtt.freeze();
        obtt.freeze();

        loop.discardBuffer(this);

        if (key == null)
            return;
        try {
            key.cancel();
            key.channel().close();
        } catch (IOException e) {
            log.warn(E_CLOSE, Log.stackTraceSnippet(e));
        }
    }

    /**
     * Sets the select key obtained when the backing channel of this stream
     * was registered with the IO loop selector.
     *
     * @param key select key from registration of the backing channel
     */
    public void setKey(SelectionKey key) {
        this.key = key;
        this.lastOp = currentTimeMillis();
    }

    /**
     * Gets a single message from the supplied byte buffer.
     * <p>
     * Implementations are expected not to flip, reset or clear the buffer.
     *
     * @param rb read byte buffer
     * @return read message or null if message could not be extracted due to
     *         insufficient available bytes
     */
    protected abstract M get(ByteBuffer rb);

    /**
     * Puts the specified message into the internal buffer.
     * <p>
     * Implementations are expected not to flip, reset or clear the buffer.
     *
     * @param message message to be placed to the buffer
     * @param wb write byte buffer
     */
    protected abstract void put(M message, ByteBuffer wb);


    /**
     * Appends the specified message into the internal buffer, growing the
     * buffer if required.
     *
     * @param message message to be added to the buffer
     */
    private void append(M message) {
       if (ob.remaining() < message.length())
           ob = growBuffer(ob);

       // Place the message into the buffer and bump the output trackers.
       put(message, ob);
       omtt.add(1);
       obtt.add(message.length());
    }

    /**
     * Gets a list of messages from the buffer in non-blocking fashion. If
     * there are no messages to be read, the list will be empty. If the
     * backing socket channel has been closed, null will be returned.
     *
     * @return list of read messages; null if backing channel is closed
     * @throws IOException if data could not be read
     */
    public List<M> dequeue() throws IOException {
        try {
            int read = ch.read(ib);
            if (read == -1)
                return null;
            ib.flip();

            List<M> messages;
            if (secure) {
                messages = doUnwrap();
                ib.compact();
            } else {
                messages = getMessages(ib);
            }
            return messages;

        } catch (Exception e) {
            throw new IOException(E_DEQUEUE, e);
        }
    }

    private List<M> dequeueAppData() {
        ibApp.flip();
        return getMessages(ibApp);
    }

    private List<M> getMessages(ByteBuffer buffer) {
        List<M> messages = new ArrayList<M>();
        M message;
        while ((message = get(buffer)) != null) {
            // Add the message to the list and bump the input trackers.
            messages.add(message);
            imtt.add(1);
            ibtt.add(message.length());
        }
        buffer.compact();
        lastOp = currentTimeMillis();
        return messages;
    }

    /**
     * Queues the list of messages to the buffer and flushes to the backing
     * channel if necessary.
     *
     * @param messages list of messages to be transfered
     * @throws IOException if data could not be transfered or flushed
     */
    public void queue(List<M> messages) throws IOException {
        synchronized (this) {
            if (secure) {
                for (M m : messages)
                    ourAppend(m);
                flushOutboundAppBuffer();

            } else {
                for (M m : messages)
                    append(m);
                if (!writeOccurred && !writePending)
                    flush();
            }
        }
    }

    /**
     * Queues the message to the buffer and flushes to the backing channel if
     * necessary.
     *
     * @param message message to be transfered
     * @throws IOException if data could not be transfered or flushed
     */
    public void queue(M message) throws IOException {
        synchronized (this) {
            if (secure) {
                ourAppend(message);
                flushOutboundAppBuffer();

            } else {
                append(message);
                if (!writeOccurred && !writePending)
                    flush();
            }
        }
    }

    /**
     * Flushes any pending writes accumulated in the out-bound buffer to the
     * backing socket channel.
     *
     * @throws IOException if flush failed
     */
    public void flush() throws IOException {
        synchronized (this) {
            if (writeOccurred || writePending)
                return;

            ob.flip();
            try {
                ch.write(ob);
            } catch (IOException e) {
                if (!discarded)
                    logIOException(e);
            }
            lastOp = currentTimeMillis();
            writeOccurred = true;
            writePending = ob.hasRemaining();
            ob.compact();
        }
    }

    // Only logs the exception if it is not a broken pipe which occurs
    // during closing.
    private void logIOException(IOException e) {
        if (!e.getMessage().equals(BROKEN_PIPE))
            log.warn(E_FLUSH, stackTraceSnippet(e));
        flushError = e;
    }

    /**
     * Returns true if data has been written, but not yet flushed.
     *
     * @return true if flush is required
     */
    boolean requiresFlush() {
        synchronized (this) {
            return ob.position() > 0;
        }
    }

    /**
     * Returns true if the buffer has bytes to be written to the channel.
     *
     * @return true if there are bytes to be written
     */
    boolean isWriteStillPending() {
        synchronized (this) {
            return writePending;
        }
    }

    /**
     * Attempts to flush data, internal buffer state and channel availability
     * permitting. Invoked by the driver I/O loop during handling of writable
     * selection key.
     * <p>
     * Resets the internal state flags {@code writeOccurred} and
     * {@code writePending}.
     *
     * @throws IOException if implicit flush failed
     */
    void flushIfPossible() throws IOException {
        synchronized (this) {
            writePending = false;
            writeOccurred = false;
            if (ob.position() > 0)
                flush();
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    /**
     * Attempts to flush data, internal buffer state and channel availability
     * permitting and if other writes are not pending. Invoked by the driver
     * I/O loop prior to entering select wait. Resets the internal
     * {@code writeOccurred} state flag.
     *
     * @throws IOException if implicit flush failed
     */
    void flushIfWriteNotPending() throws IOException {
        synchronized (this) {
            writeOccurred = false;
            if (!writePending && ob.position() > 0)
                flush();
        }
        if (isWriteStillPending())
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    }

    /**
     * Returns true if a prior flush encountered an error. The error can be
     * retrieved via {@link MessageBuffer#getFlushError} method.
     *
     * @return true if a write failed
     */
    public boolean flushFailed() {
        return flushError != null;
    }

    /**
     * Gets the prior flush error, if one occurred.
     *
     * @return flush error; null if none occurred
     */
    public Exception getFlushError() {
        return flushError;
    }


    /**
     * Returns the IO loop to which this stream is bound.
     *
     * @return I/O loop used to drive this stream
     */
    public IOLoop<M, ?> loop() {
        return loop;
    }

    /**
     * Returns the selection key used for registration of the backing
     * socket channel.
     *
     * @return socket channel registration selection key
     */
    public SelectionKey key() {
        return key;
    }


    /**
     * Grows the given buffer by {@link MessageBuffer#growthFactor} and returns
     * a new buffer instance with any remaining data copied into it.
     *
     * @param buffer the buffer to grow
     * @return a new (bigger) buffer instance
     */
    protected ByteBuffer growBuffer(ByteBuffer buffer) {
        int newCapacity = (int) Math.round(buffer.capacity() * growthFactor());
        ByteBuffer biggerBuffer = ByteBuffer.allocateDirect(newCapacity);
        buffer.flip();
        biggerBuffer.put(this.ob);
        return biggerBuffer;
    }

    /**
     * Returns the out-bound buffer growth-factor.
     *
     * @return growth factor; must be larger than 1.0
     */
    protected double growthFactor() {
        return GROWTH_FACTOR;
    }

    /**
     * Returns the maximum age before buffer is considered stale without any
     * dequeue/queue operations.
     *
     * @return max age in milliseconds; must be greater than 1
     */
    protected int maxAge() {
        return MAX_AGE;
    }


    /**
     * Returns true if the given stream has gone stale.
     *
     * @return true if the stream is stale
     */
    boolean isStale() {
        return key != null &&  currentTimeMillis() - lastOp > maxAge();
    }

    /**
     * Returns the in-bound messages throughput tracker.
     *
     * @return throughput tracker
     */
    public ThroughputTracker inMessages() {
        return imtt;
    }

    /**
     * Returns the in-bound bytes throughput tracker.
     *
     * @return throughput tracker
     */
    public ThroughputTracker inBytes() {
        return ibtt;
    }

    /**
     * Returns the out-bound messages throughput tracker.
     *
     * @return throughput tracker
     */
    public ThroughputTracker outMessages() {
        return omtt;
    }

    /**
     * Returns the out-bound bytes throughput tracker.
     *
     * @return throughput tracker
     */
    public ThroughputTracker outBytes() {
        return obtt;
    }

    // TODO: Add javadocs/comments to private methods

    private List<M> doUnwrap() throws IOException {
        SSLEngineResult result;
        try {
            for (;;) {
                result = engine.unwrap(ib, ibApp);

                SSLEngineResult.Status status = result.getStatus();
                switch (status) {
                    case BUFFER_UNDERFLOW:
                        // Not enough source bytes to decrypt full packet
                        return noMessages;
                    case BUFFER_OVERFLOW:
                        // Not enough room in dest buffer to write app data.
                        // Dequeue and try again
                        return dequeueAppData();
                    case OK:
                        // Operation was successful; handle handshake status
                        if (handleUnwrapHandshake(result.getHandshakeStatus())) {
                            if (noInboundNetData())
                                return dequeueAppData();
                        }
                        break;
                    case CLOSED:
                        // the connection is closed
                        flushOutboundBuffer();
                        return noMessages;
                }
            }

        } catch (SSLException e) {
            log.warn("Unwrap failed: {}", Log.stackTraceSnippet(e));
            throw new IOException(e);
        }
    }

    private boolean handleUnwrapHandshake(SSLEngineResult.HandshakeStatus hs)
            throws IOException {
        if (hs == NOT_HANDSHAKING || hs == NEED_UNWRAP)
            return BAIL;
        if (hs == NEED_WRAP)
            return doWrapIndicateUnwrapNeeded();
        if (hs == FINISHED) {
            ib.compact();
            return BAIL;
        }

        processDelegatedTasks(); // NEED_TASK
        return CONTINUE;
    }

    private boolean noInboundNetData() {
        return !ib.hasRemaining();
    }

    private void flushOutboundBuffer() throws IOException {
        if (!writeOccurred && !writePending)
            flush();
    }

    private void processDelegatedTasks() {
        Runnable task = engine.getDelegatedTask();
        while (task != null) {
            task.run();
            task = engine.getDelegatedTask();
        }
    }

    private void ourAppend(M message) {
        if (obApp.remaining() < message.length())
            obApp = growBuffer(obApp);

        // Place the message into the buffer and bump the output trackers.
        put(message, obApp);
        omtt.add(1);
        obtt.add(message.length());
    }

    private boolean doWrapIndicateUnwrapNeeded() throws IOException {
        SSLEngineResult result;
        try {
            for (;;) {
                synchronized (this) {
                    result = engine.wrap(obApp, ob);
                }

                SSLEngineResult.Status status = result.getStatus();
                flushOutboundBuffer();
                switch (status) {
                    case BUFFER_UNDERFLOW:
                        // should never happen because we either have no
                        // messages on obApp or 1 or more complete messages
                        return false;
                    case BUFFER_OVERFLOW:
                        // not enough room on outbound net buffer
                        return true;
                    case OK:
                        // Operation was successful; handle handshake status
                        SSLEngineResult.HandshakeStatus hs = result.getHandshakeStatus();
                        if (handleWrapHandshake(hs)) {
                            if (!finishedOrNeedsUnwrap(hs))
                                return CONTINUE;
                            if (finished(hs))
                                flushOutboundAppBuffer();
                            return BAIL;
                        }
                        break;
                    case CLOSED:
                        return false;
                }
            }
        } catch (SSLException e) {
            log.warn("Wrap failed: {}", Log.stackTraceSnippet(e));
            throw new IOException(e);
        }
    }

    private boolean handleWrapHandshake(SSLEngineResult.HandshakeStatus hs) {
        if (hs == NOT_HANDSHAKING || hs == NEED_UNWRAP || hs == FINISHED)
            return BAIL;
        if (hs == NEED_TASK)
            processDelegatedTasks();
        return CONTINUE; // NEED_TASK or NEED_WRAP
    }

    private boolean finishedOrNeedsUnwrap(SSLEngineResult.HandshakeStatus hs) {
        return hs == NEED_UNWRAP || hs == FINISHED;
    }

    private boolean finished(SSLEngineResult.HandshakeStatus hs) {
        return hs == FINISHED;
    }

    private void flushOutboundAppBuffer() throws IOException {
        synchronized (this) {
            obApp.flip();
            if (obApp.hasRemaining())
                doWrapIndicateUnwrapNeeded();
            obApp.compact();
        }
    }

}
