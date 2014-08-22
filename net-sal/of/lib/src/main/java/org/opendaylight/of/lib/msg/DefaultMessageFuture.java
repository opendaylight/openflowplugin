/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.opendaylight.of.lib.CommonUtils.notMutable;
import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Default implementation of a {@link MessageFuture}.
 *
 * @author Simon Hunt
 */
public class DefaultMessageFuture implements MessageFuture {

    private static final String NONE = "(none)";
    private static final String E_BAD_XID = "xid <= 0 : ";
    private static final long MIL = 1000000;

    private final OpenflowMessage request;
    private final long xid;

    private volatile boolean done;
    private int waiters;
    private Result result;
    private OpenflowMessage reply;
    private Throwable cause;

    /**
     * Constructs a default message future for the given request.
     *
     * @param request the out-bound request associated with this future
     * @throws IllegalArgumentException if the xid is 0 or negative
     */
    public DefaultMessageFuture(OpenflowMessage request) {
        notNull(request);
        this.request = request;
        this.xid = request.getXid();
        if (xid <= 0)
            throw new IllegalArgumentException(E_BAD_XID + xid);
        result = Result.UNSATISFIED;
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder("{MsgFuture:xid=");
        sb.append(xid).append(",").append(result);
        switch (result) {
            case SUCCESS:
            case OFM_ERROR:
                sb.append(",reply=").append(reply);
                break;
            case EXCEPTION:
                sb.append(",ex=").append(cause);
                break;
            default:
                break;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public long xid() {
        return xid;
    }

    @Override
    public OpenflowMessage request() {
        return request;
    }

    @Override
    public synchronized OpenflowMessage reply() {
        return reply;
    }

    @Override
    public synchronized String problemString() {
        if (result == Result.EXCEPTION)
            return cause.toString();
        if (result == Result.OFM_ERROR)
            return reply.toString();
        return NONE;
    }

    @Override
    public synchronized Throwable cause() {
        return cause;
    }

    @Override
    public synchronized Result result() {
        return result;
    }

    /**
     * Invoked when the future is satisfied. This default implementation
     * does nothing. Subclasses may override to do housekeeping, before
     * listeners are awoken.
     */
    protected void satisfied() { }

    @Override
    public boolean setSuccess() {
        synchronized (this) {
            // Allow only once
            if (done)
                return false;
            done = true;
            result = Result.SUCCESS_NO_REPLY;
            satisfied();
            if (waiters > 0)
                notifyAll();
        } // sync
        return true;
    }

    @Override
    public boolean setSuccess(OpenflowMessage msg) {
        notMutable(msg);
        synchronized (this) {
            // Allow only once
            if (done)
                return false;
            done = true;
            result = Result.SUCCESS;
            this.reply = msg;
            satisfied();
            if (waiters > 0)
                notifyAll();
        } // sync
        return true;
    }

    @Override
    public boolean setFailure(OfmError error) {
        notMutable(error);
        synchronized (this) {
            // Allow only once
            if (done)
                return false;
            done = true;
            result = Result.OFM_ERROR;
            this.reply = error;
            satisfied();
            if (waiters > 0)
                notifyAll();
        } // sync
        return true;
    }

    @Override
    public boolean setFailure(Throwable cause) {
        synchronized (this) {
            // Allow only once
            if (done)
                return false;
            done = true;
            result = Result.EXCEPTION;
            this.cause = cause;
            satisfied();
            if (waiters > 0)
                notifyAll();
        } // sync
        return true;
    }

    @Override
    public boolean setFailureTimeout() {
        synchronized (this) {
            // Allow only once
            if (done)
                return false;
            done = true;
            result = Result.TIMEOUT;
            satisfied();
            if (waiters > 0)
                notifyAll();
        } // sync
        return true;
    }


    @Override
    public MessageFuture await() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();

        synchronized (this) {
            while (!done) {
                waiters++;
                try {
                    this.wait(); // BLOCKS
                } finally {
                    waiters--;
                }
            }
        } // sync
        return this;
    }

    @Override
    public MessageFuture awaitUninterruptibly() {
        boolean interrupted = false;
        synchronized (this) {
            while (!done) {
                waiters++;
                try {
                    this.wait(); // BLOCKS
                } catch (InterruptedException e) {
                    interrupted = true;
                } finally {
                    waiters--;
                }
            }
        } // sync

        if (interrupted)
            Thread.currentThread().interrupt();

        return this;
    }

    @Override
    public boolean await(long timeoutMs) throws InterruptedException {
        return doTimedWait(timeoutMs, true);
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMs) {
        try {
            return doTimedWait(timeoutMs, false);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Should never happen", e);
        }
    }

    // private service method
    private boolean doTimedWait(long timeoutMs, boolean interruptable)
            throws InterruptedException {
        if (interruptable && Thread.interrupted())
            throw new InterruptedException();

        long timeoutNanos = MILLISECONDS.toNanos(timeoutMs);
        long startTime = timeoutNanos <= 0 ? 0 : nanoTime();
        long waitTime = timeoutNanos;
        boolean interrupted = false;

        try {
            synchronized (this) {
                if (done || waitTime <= 0)
                    return done;

                waiters++;
                try {
                    for (;;) {
                        try {
                            this.wait(waitTime / MIL, (int) (waitTime % MIL));
                        } catch (InterruptedException e) {
                            if (interruptable)
                                throw e;
                            interrupted = true;
                        }

                        if (done)
                            return true;
                        waitTime = timeoutNanos - (nanoTime() - startTime);
                        if (waitTime <= 0)
                            return done;
                    }
                } finally {
                    waiters--;
                }
            } // sync
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
