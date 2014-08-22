/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.of.lib.msg.MessageFuture.Result;

/**
 * A message future aggregate, allowing the consumer to wait for completion
 * of a number of message futures.
 * <p>
 * This class is not thread-safe; it is expected that only a single consumer
 * will create one, populate it with futures, and then wait for the futures to
 * be satisfied.
 *
 * @see MessageFuture
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public class MessageFutureBag {

    private static final String E_WAITING = "Already waiting";
    private static final String E_NO_FUTURE = "No futures to wait for";

    /** Denotes the different states that a message future bag can be in. */
    public static enum BagResult {
        /**
         * One or more message futures have still not been satisfied.
         */
        UNSATISFIED,

        /**
         * All message futures have been satisfied successfully.
         */
        SUCCESS,

        /**
         * All message futures have been satisfied; some successfully, but
         * at least one failed with an {@link OfmError} reply.
         */
        SUCCESS_WITH_ERRORS,

        /**
         * All message futures have been satisfied; some successfully, but
         * at least one failed with an exception. There may also be futures
         * that failed with an {@link OfmError} reply.
         */
        SUCCESS_WITH_EXCEPTIONS,

        /**
         * All message futures have been satisfied, but all failed with
         * an {@link OfmError} reply.
         */
        FAILED_WITH_ERRORS,

        /**
         * All message futures have been satisfied, but all failed with either
         * an {@link OfmError} reply or an exception (at least one).
         */
        FAILED_WITH_EXCEPTIONS,

        /**
         * A time-out occurred waiting for the futures to be satisfied.
         * Note that some of the futures <em>may</em> be satisfied, but
         * the results are indeterminate.
         */
        TIMEOUT,

        ;
    }

    private final Set<MessageFuture> futures = new HashSet<>();
    private BagResult result = BagResult.UNSATISFIED;
    private boolean waiting = false;

    /**
     * Constructs a message future bag, initializing it with the given futures
     * to track.
     *
     * @param futures the initial futures to track
     */
    public MessageFutureBag(MessageFuture... futures) {
        add(futures);
    }

    /**
     * Adds more futures to the bag for tracking.
     *
     * @param futures additional futures to track
     * @throws IllegalStateException if await() has already been invoked
     */
    public void add(MessageFuture... futures) {
        if (waiting)
            throw new IllegalStateException(E_WAITING);
        Collections.addAll(this.futures, futures);
    }

    // TODO: void addAll(Collection<MessageFuture>)

    @Override
    public String toString() {
        int satisfied = 0;
        for (MessageFuture f: futures)
            if (f.result() != Result.UNSATISFIED)
                satisfied++;

        return "{MsgFutureBag:#satisfied=" + satisfied + "/" + futures.size() +
                ",result=" + result + "}";
    }

    /**
     * Returns the result of this bag.
     *
     * @return the result
     */
    public BagResult result() {
        return result;
    }

    /**
     * Returns the number of futures in this bag.
     *
     * @return the number of futures
     */
    public int size() {
        return futures.size();
    }

    /**
     * Returns an unmodifiable view of the futures in this bag.
     *
     * @return the futures
     */
    public Set<MessageFuture> futures() {
        return Collections.unmodifiableSet(futures);
    }

    /**
     * Waits for all the futures in this bag to be satisfied, at which point
     * the aggregate result is set.
     *
     * @return self (for method chaining)
     * @throws IllegalStateException if there are no futures in the bag
     * @throws InterruptedException if the current thread was interrupted
     */
    public MessageFutureBag await() throws InterruptedException {
        if (futures.isEmpty())
            throw new IllegalStateException(E_NO_FUTURE);
        if (Thread.interrupted())
            throw new InterruptedException();
        waiting = true;

        // Doesn't matter which order the futures are satisfied...
        for (MessageFuture f: futures)
            f.await(); // BLOCKS

        computeResult(false);
        return this;
    }

    /**
     * Waits for all the futures in this bag to be satisfied, at which point
     * the aggregate result is set.
     *
     * @return self (for method chaining)
     * @throws IllegalStateException if there are no futures in the bag
     */
    public MessageFutureBag awaitUninterruptibly() {
        if (futures.isEmpty())
            throw new IllegalStateException(E_NO_FUTURE);
        waiting = true;

        // Doesn't matter which order the futures are satisfied...
        for (MessageFuture f: futures)
            f.awaitUninterruptibly(); // BLOCKS

        computeResult(false);
        return this;
    }

    /**
     * Waits for all the futures in this bag to be satisfied, at which point
     * the aggregate result is set.
     * 
     * @param eachTimeoutMs number of milliseconds to wait for each future to
     *        timeout
     * @return {@code true} if every future was completed within the specified
     *          time limit; {@code false} otherwise
     * @throws IllegalStateException if there are no futures in the bag
     */
    public boolean awaitUninterruptibly(long eachTimeoutMs) {
        if (futures.isEmpty())
            throw new IllegalStateException(E_NO_FUTURE);
        waiting = true;

        // Doesn't matter which order the futures are satisfied...
        boolean timedOut = false;
        boolean ok;
        for (MessageFuture f: futures) {
            ok = f.awaitUninterruptibly(eachTimeoutMs); // BLOCKS
            if (!ok)
                timedOut = true;
        }

        computeResult(timedOut);
        return !timedOut;
    }

    // TODO: consider adding timeout await() variants

    // scans the message futures to determine the aggregate result
    // note: either we timed out, or all futures were satisfied
    private void computeResult(boolean timedOut) {
        boolean seenSuccess = false;
        boolean seenError = false;
        boolean seenException = false;

        if (timedOut) {
            result = BagResult.TIMEOUT;
            return;
        }

        for (MessageFuture f: futures) {
            switch (f.result()) {
                case SUCCESS:
                case SUCCESS_NO_REPLY:
                    seenSuccess = true;
                    break;
                case OFM_ERROR:
                    seenError = true;
                    break;
                case EXCEPTION:
                    seenException = true;
                    break;

                // unsatisfied or timeout (keep FindBugs happy)
                default:
                    break;
            }
        }

        if (seenSuccess) {
            if (!seenError && !seenException)
                result = BagResult.SUCCESS;
            else
                result = seenException ? BagResult.SUCCESS_WITH_EXCEPTIONS
                        : BagResult.SUCCESS_WITH_ERRORS;
        } else {
            // no successes
            result = seenException ? BagResult.FAILED_WITH_EXCEPTIONS
                    : BagResult.FAILED_WITH_ERRORS;
        }
    }
}