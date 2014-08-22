/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import static org.opendaylight.util.Log.stackTraceSnippet;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.net.ssl.SSLContext;

/**
 * Abstraction of an I/O loop for driving buffered message transfer across
 * network socket connections.
 * <p>
 * Loosely based on I/O loop algorithm authored by David Erickson.
 *
 * @param <M> message type
 * @param <B> message buffer type
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public abstract class IOLoop<M extends Message, B extends MessageBuffer<M>>
                                        extends SelectLoop {

    private static final String E_REGISTER = "Unable to register inbound connection {}";
    private static final String E_FINISH = "Unable to finish connection {}";
    private static final String E_PROCESS = "Unable to process I/O {}";
    
    private static final String RESET_BY_PEER = "Connection reset by peer";


    // Set of registered message buffers
    private final Set<B> buffers = new CopyOnWriteArraySet<B>();

    // Queue of pending message buffer registrations
    private final Queue<Registration> registrations =
            new ConcurrentLinkedQueue<Registration>();



    // Auxiliary carrier of a registration request for a new message buffer.
    private class Registration {
        private SelectableChannel ch;
        private B mb;
        private int op;

        public Registration(SelectableChannel ch, B mb, int op) {
            this.ch = ch;
            this.mb = mb;
            this.op = op;
        }
    }

    /**
     * Creates an IO loop with default timeout.
     *
     * @throws IOException if unable to open selector
     */
    public IOLoop() throws IOException {
    }

    /**
     * Creates an IO loop with the specified timeout.
     *
     * @param timeout select timeout in milliseconds
     * @throws IOException if unable to open selector
     */
    public IOLoop(long timeout) throws IOException {
        super(timeout);
    }

    /**
     * Creates a new message buffer backed by the given socket channel and
     * driven by the specified loop.
     * If an SSL context is specified, the IOLoop is expected to provide a
     * secure {@link MessageBuffer}.
     *
     * @param ch backing byte channel
     * @param sslContext optional SSL context
     * @return newly created message buffer
     */
    protected abstract B createBuffer(ByteChannel ch, SSLContext sslContext);

    /**
     * Discards the specified message buffer from the list of registered
     * buffers.
     * <p>
     * One should not invoke this method directly as this is merely a hook
     * invoked by the default implementation of
     * {@link MessageBuffer#discard()} method, which is what should be
     * used directly instead.
     *
     * @param b message buffer
     */
    protected void discardBuffer(MessageBuffer<M> b) {
        buffers.remove(b);
    }

    /**
     * Processes the list of messages received from the specified message
     * buffer.
     *
     * @param b source message buffer
     * @param messages list of received messages; guaranteed not to be null
     *        and not empty
     */
    protected abstract void processMessages(MessageBuffer<M> b, List<M> messages);


    /**
     * Processes a connection request represented by the supplied select key.
     * Default implementation sets the key's {@code interestedOps} to
     * {@link SelectionKey#OP_READ} and then attempts to finish the connection
     * on the key socket channel.
     *
     * @param key selection key holding the pending connect operation.
     */
    protected void connect(SelectionKey key) {
       try {
           SocketChannel ch = (SocketChannel) key.channel();
           ch.finishConnect();
       } catch (IOException e) {
           log.warn(E_FINISH, stackTraceSnippet(e));
       } catch (IllegalStateException e) {
           log.warn(E_FINISH, stackTraceSnippet(e));
       }

       if (key.isValid())
           key.interestOps(SelectionKey.OP_READ);
    }

    /**
     * Processes a single IO request represented by the supplied select key.
     *
     * @param key selection key holding the pending I/O operation.
     */
    protected void processIO(SelectionKey key) {
        @SuppressWarnings("unchecked")
        B b = (B) key.attachment();

        try {
            if (!key.isValid()) {
                // Bail if the key is invalid.
                b.discard();
                return;
            }

            if (key.isConnectable())
                connect(key);

            if (key.isReadable()) {
                List<M> messages = b.dequeue();
                if (messages == null || b.flushFailed()) {
                    // If we got nothing, bail; we are being disconnected.
                    b.discard();
                    return;
                }
                if (!messages.isEmpty()) {
                    try {
                        processMessages(b, messages);
                    } catch (RuntimeException e) {
                        handleMessageProcessingException(b, e);
                    }
                }
            }

            if (key.isWritable())
                b.flushIfPossible();

            if (b.flushFailed())
                b.discard();

        } catch (CancelledKeyException e) {
            // Key was cancelled, so silently discard the buffer
            b.discard();
        } catch (IOException e) {
            if (!b.alreadyDiscarded())
                logIOException(e);
            b.discard();
        }
    }
    
    // Only logs the exception if it is not caused by peer reset which occurs
    // during closing.
    private void logIOException(IOException e) {
        Throwable cause = e.getCause();
        if (cause != null && IOException.class.isInstance(cause) &&
                cause.getMessage().equals(RESET_BY_PEER))
            // Ignore IO errors caused by peer reset 
            return;
        log.warn(E_PROCESS, stackTraceSnippet(e));
    }

    /**
     * Invoked if a runtime exception is thrown from {@link #processMessages}.
     * <p>
     * This default implementation simply rethrows the exception.
     *
     * @param b the buffer that was being processed
     * @param e the runtime exception that was thrown
     */
    protected void handleMessageProcessingException(B b, RuntimeException e) {
        throw e;
    }

    /**
     * Prunes the registered buffers by discarding any stale ones.
     */
    public synchronized void prune() {
        for (B b : buffers)
            if (b.isStale())
                b.discard();
    }

    /**
     * Registers a new socket channel by creating a new message buffer backed
     * by the socket channel and driven by this loop. If an SSL context is
     * specified, the IOLoop is expected to provide a secure
     * {@link MessageBuffer}.
     *
     * @param ch socket channel
     * @param sslContext optional SSL context
     */
    public void registerAccept(SocketChannel ch, SSLContext sslContext) {
        register(ch, SelectionKey.OP_READ, sslContext);
    }


    /**
     * Registers a new client-side socket channel by creating a new
     * message buffer backed by the socket channel and driven by this loop.
     * If an SSL context is specified, the IOLoop is expected to provide a
     * secure {@link MessageBuffer}.
     *
     * @param ch socket channel
     * @param sslContext optional SSL context
     */
    public void registerConnect(SocketChannel ch, SSLContext sslContext) {
        register(ch, SelectionKey.OP_CONNECT, sslContext);
    }

    /**
     * Registers a new socket channel by creating a new message buffer backed
     * by the socket channel and driven by this loop.
     * If an SSL context is specified, the IOLoop is expected to provide a
     * secure {@link MessageBuffer}.
     *
     * @param ch socket channel
     * @param op initial interestedOps to be applied to the key upon the
     *        selector registration with the channel
     * @param sslContext optional SSL context
     */
    private synchronized void register(SocketChannel ch, int op,
                                       SSLContext sslContext) {
        B b = createBuffer(ch, sslContext);
        buffers.add(b);
        registrations.add(new Registration(ch, b, op));
        selector.wakeup();
    }

    /**
     * Processes the queue of pending registrations to perform selector
     * channel register operations safely within the I/O loop thread.
     */
    private void processRegistrations() {
        Iterator<Registration> it = registrations.iterator();
        while (!stopped && it.hasNext()) {
            Registration r = it.next();
            try {
                r.mb.setKey(r.ch.register(selector, r.op, r.mb));
            } catch (ClosedChannelException e) {
                log.warn(E_REGISTER, stackTraceSnippet(e));
            }
            it.remove();
        }
    }

    @Override
    protected void loop() throws IOException {
        signalStart();

        // Outer event loop
        int count;
        while (!stopped) {
            processRegistrations();

            // Process flushes & write selects on all streams
            for (B b : buffers)
                b.flushIfWriteNotPending();

            // Select keys and process them.
            count = selector.select(timeout);
            if (count > 0 && !stopped) {
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    processIO(it.next());
                    it.remove();
                }
            }
        }
    }

}
