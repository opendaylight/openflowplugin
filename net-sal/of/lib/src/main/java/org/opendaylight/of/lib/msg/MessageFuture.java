/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

/**
 * The result of an asynchronous OpenFlow message request/reply interaction.
 * <p>
 * Services can choose to return pending OpenFlow message results in the form
 * of message futures. Clients can use these to wait for the results using
 * various forms of the {@code await} methods and then query the future to
 * determine success or failure.
 *
 * @see MessageFutureBag
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Thomas Vachuska
 */
public interface MessageFuture {

    /** Denotes the different states that a message future can be in. */
    public static enum Result {
        /**
         * The message future has not yet been satisfied.
         */
        UNSATISFIED,

        /**
         * The message request was a success; the reply message is
         * available via {@link MessageFuture#reply}.
         */
        SUCCESS,

        /**
         * The message request was a success; there is no associated reply.
         */
        SUCCESS_NO_REPLY,

        /**
         * The message request failed due to an error returned from the
         * datapath; the {@link OfmError error message} is available via
         * {@link MessageFuture#reply}.
         */
        OFM_ERROR,

        /**
         * The message request failed due to an exception; the cause
         * is available via {@link MessageFuture#cause}.
         */
        EXCEPTION,

        /**
         * The message request failed due to a timeout; the datapath did
         * not respond within a reasonable amount of time.
         */
        TIMEOUT,
        ;
        
        /**
         * Returns whether or not the result was a success of any kind.
         * The result is considered a success if it is a
         * {@link MessageFuture.Result#SUCCESS} or
         * {@link MessageFuture.Result#SUCCESS_NO_REPLY}.
         * 
         * @return whether the result was a success of any kind
         */
        public boolean isSuccess() {
            return (this == SUCCESS || this == SUCCESS_NO_REPLY);
        }

        /**
         * Returns whether or not the result was a failure of any kind.
         * The result is considered a failure if it is a
         * {@link MessageFuture.Result#OFM_ERROR},
         * {@link MessageFuture.Result#EXCEPTION}, or
         * {@link MessageFuture.Result#TIMEOUT}.
         *
         * @return whether the result was a failure of any kind
         */
        public boolean isFailure() {
            return (this == OFM_ERROR || this == EXCEPTION || this == TIMEOUT);
        }
    }

    /**
     * Returns the xid (message sequence number) associated with this future.
     *
     * @return the transaction id
     */
    long xid();

    /**
     * Returns the original request message.
     *
     * @return the OpenFlow message
     */
    OpenflowMessage request();

    /**
     * Returns the reply resulting from the asynchronous request. If the
     * request was successful, the message will be the corresponding reply.
     * If the request was successful (but no reply was expected), this will
     * be null. If the request failed, the message will be the corresponding
     * {@link OfmError} message.
     * <p>
     * This will be null if the future has not yet been satisfied.
     *
     * @return the OpenFlow message
     */
    OpenflowMessage reply();

    /**
     * Returns a string description of the problem if there was one. This will
     * be the string representation of either the {@link OfmError} reply from
     * the switch, or the exception thrown (if the send failed), if the future
     * was satisfied as a failure; otherwise it will be the string
     * {@code "(none)"}.
     *
     * @return the problem string
     */
    String problemString();

    /**
     * Returns the cause of failure if an exception was thrown.
     *
     * @return the cause of failure
     */
    Throwable cause();

    /**
     * Returns the future's result.
     *
     * @return the future's result
     */
    Result result();

    /**
     * Marks this future as a success (with no expected reply) and notifies
     * all listeners. {@code true} is returned if this future is successfully
     * marked as a success; {@code false} is returned if this future was
     * already marked as either success or failure.
     *
     * @return {@code true} if successfully marked as a success;
     *          {@code false} otherwise
     */
    boolean setSuccess();

    /**
     * Marks this future as a success, attaching the specified reply message,
     * and notifies all listeners. {@code true} is returned if this future is
     * successfully marked as a success; {@code false} is returned if this
     * future was already marked as either success or failure.
     *
     * @param msg the OpenFlow message reply to the original request
     * @return {@code true} if successfully marked as a success;
     *          {@code false} otherwise
     * @throws IllegalArgumentException if the message is mutable
     */
    boolean setSuccess(OpenflowMessage msg);

    /**
     * Marks this future as a failure, attaching the specified error response
     * message, and notifies all listeners. {@code true} is returned if this
     * future is successfully marked as a failure; {@code false} is returned
     * if this future was already marked as either success or failure.
     *
     * @param error the OpenFlow error message resulting from the original
     *        request
     * @return {@code true} if successfully marked as a failure;
     *          {@code false} otherwise
     * @throws IllegalArgumentException if the error message is mutable
     */
    boolean setFailure(OfmError error);

    /**
     * Marks this future as a failure, caused by the specified exception,
     * and notifies all listeners. {@code true} is returned if this future is
     * successfully marked as a failure; {@code false} is returned if this
     * future was already marked as either success or failure.
     *
     * @param cause the cause of the failure
     * @return {@code true} if successfully marked as a failure;
     *          {@code false} otherwise
     */
    boolean setFailure(Throwable cause);

    /**
     * Marks this future as a failure, caused by a timeout, and notifies all
     * listeners. {@code true} is returned if this future is successfully
     * marked as a failure; {@code false} is returned if this future was
     * already marked as either success or failure.
     *
     * @return {@code true} if successfully marked as a failure;
     *          {@code false} otherwise
     */
    boolean setFailureTimeout();

    /**
     * Waits for this future to be completed. This call blocks until the future
     * is marked as success or failure.
     *
     * @return self (for method chaining)
     * @throws InterruptedException if the current thread was interrupted
     */
    MessageFuture await() throws InterruptedException;

    /**
     * Waits for this future to be completed without interruption. This call
     * blocks until the future is marked as success or failure. This method
     * catches {@link InterruptedException}s and discards them silently.
     *
     * @return self (for method chaining)
     */
    MessageFuture awaitUninterruptibly();

    /**
     * Waits for this future to be completed within the specified time limit.
     * This call blocks until the future is marked as success or failure, or
     * until the specified amount of time has elapsed. {@code true} is returned
     * if the future was satisfied within the time limit; {@code false} is
     * returned if the timeout expired.
     *
     * @param timeoutMs timeout expressed in milliseconds
     * @return {@code true} if the future was completed within the specified
     *          time limit; {@code false} otherwise
     * @throws InterruptedException if the current thread was interrupted
     */
    boolean await(long timeoutMs) throws InterruptedException;

    /**
     * Waits for this future to be completed within the specified time limit
     * without interruption. This call blocks until the future is marked as
     * success or failure, or until the specified amount of time has elapsed.
     * {@code true} is returned if the future was satisfied within the time
     * limit; {@code false} is returned if the timeout expired. This method
     * catches {@link InterruptedException}s and discards them silently.
     *
     * @param timeoutMs timeout expressed in milliseconds
     * @return {@code true} if the future was completed within the specified
     *          time limit; {@code false} otherwise
     */
    boolean awaitUninterruptibly(long timeoutMs);
}
