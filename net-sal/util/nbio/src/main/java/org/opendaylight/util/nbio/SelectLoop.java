/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.opendaylight.util.Log;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.channels.Selector;

import static java.lang.System.currentTimeMillis;

/**
 * Base abstraction of an I/O loop.
 *
 * @author Thomas Vachuska
 */
public abstract class SelectLoop implements Runnable {

    /** Shared logger. */
    protected Logger log = Log.NBIO.getLogger();

    /** Default select timeout of {@value} milliseconds. */
    public static final long DEFAULT_TIMEOUT = 500;

    private static final String E_IO = "Unable to perform I/O";

    /** Shared selector used to drive the loop operation. */
    protected Selector selector;

    /** When true, implementations are expected to break their loop. */
    protected volatile boolean stopped = false;

    /** Exception,that caused the loop to break; null if stopped normally. */
    private Throwable error;

    /** Select timeout in milliseconds. */
    protected long timeout;

    // Indicates that loop execution has started.
    private volatile boolean started = false;

    // Indicates that loop execution has finished.
    private volatile boolean finished = false;

    /**
     * Creates a select loop with default timeout.
     *
     * @throws IOException if unable to open selector
     */
    public SelectLoop() throws IOException {
        this(DEFAULT_TIMEOUT);
    }

    /**
     * Creates a select loop with the specified timeout.
     *
     * @param timeout select timeout in milliseconds
     * @throws IOException if unable to open selector
     */
    public SelectLoop(long timeout) throws IOException {
        this.timeout = timeout;
        this.selector = openSelector();
    }

    /**
     * Opens a new selector for the use by the loop.
     *
     * @return newly open selector
     * @throws IOException if unable to open selector
     */
    protected Selector openSelector() throws IOException {
        return Selector.open();
    }

    /**
     * Gracefully stops the loop.
     */
    public void cease() {
        stopped = true;
        selector.wakeup();
    }

    /**
     * I/O event loop. Implementations are expected to break when
     * {@link #stopped} becomes {@code true} via {@link #cease()} invocation.
     * Also, implementations are expected to {@link #signalStart()} when
     * ready.
     *
     * @throws IOException if unable to properly select channels or an I/O
     *         error is propagated by an event handler.
     */
    protected abstract void loop() throws IOException;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation first resets the stopped state to false, then
     * allocates the selector and finally invokes the {@link SelectLoop#loop}
     * method.
     */
    @Override
    public void run() {
        error = null;
        try {
            loop();
        } catch (Throwable e) {
            error = e;
            log.error(E_IO, e);
        }
        signalFinish();
    }

    /**
     * Signals all observers that loop has started.
     * <p>
     * Implementations are expected to invoke this method from their
     * {@link #loop()} method when ready to start processing requests.
     */
    protected synchronized void signalStart() {
        stopped = false;
        finished = false;
        started = true;
        notifyAll();
    }

    /**
     * Signals all observers that loop has finished.
     */
    private synchronized void signalFinish() {
        started = false;
        finished = true;
        stopped = true;
        notifyAll();
    }

    /**
     * Waits for the loop execution to start.
     *
     * @param timeout number of milliseconds to wait
     * @return true if loop started in time
     */
    public final synchronized boolean waitForStart(long timeout) {
        long max = currentTimeMillis() + timeout;
        while (!started && (currentTimeMillis() < max)) {
            try {
                wait(timeout);
            } catch (InterruptedException e) {
            }
        }
        return started;
    }

    /**
     * Waits for the loop execution to stop.
     *
     * @param timeout number of milliseconds to wait
     * @return true if loop finished in time
     */
    public final synchronized boolean waitForFinish(long timeout) {
        long max = currentTimeMillis() + timeout;
        while (!finished && (currentTimeMillis() < max)) {
            try {
                wait(timeout);
            } catch (InterruptedException e) {
            }
        }
        return finished;
    }

    /**
     * Gets the prior I/O error, if one occurred.
     *
     * @return flush error; null if none occurred
     */
    public Throwable getError() {
        return error;
    }

}
