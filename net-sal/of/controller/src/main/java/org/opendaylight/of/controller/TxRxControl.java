/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

/**
 * Message transmission recording control API.
 * <p>
 * This interface has two functions:
 * <ul>
 * <li>It provides the controls to enable the recording of all transmitted and
 * received OpenFlow messages for a short period of time.</li>
 * <li>It exposes the consumer side of the TX/RX message queue, so that the
 * recorded messages can be retrieved.</li>
 * </ul>
 * The queue implementation is an unbounded, blocking queue.
 * <p>
 * Note that the messages reported through this mechanism include
 * pseudo-messages marking the initial connection requests from the switches,
 * as well as the "handshaking" and "heartbeat" messages handled internally by
 * the controller. These messages are not visible above
 * {@link ControllerService}.
 * <p>
 * The intended use of this mechanism is to allow a logging service to record
 * all the OpenFlow messages for a brief span of time, for field support or
 * debugging purposes.
 * 
 * @see ControllerMx
 * @author Simon Hunt
 */
public interface TxRxControl {

    /** Enables message recording for the specified amount of time, starting
     * immediately. After the specified amount of time has elapsed,
     * recording will shut off automatically.
     * <p>
     * An exception will be thrown if we are already recording, or if a
     * trigger has been set and is waiting to fire. To clear
     * this condition, call {@link #stopRecording()}.
     *
     * @param seconds the number of seconds to elapse before message
     *                recording is shut off
     * @throws IllegalArgumentException if seconds is &lt; 1
     * @throws IllegalStateException if recording is already in progress
     */
    void startRecording(int seconds);

    /** Enables message recording for the specified amount of time, starting
     * when the specified trigger condition is met. After the specified
     * amount of time has elapsed,
     * recording will shut off automatically.
     * <p>
     * An exception will be thrown if we are already recording, or if a
     * trigger has been set and is waiting to fire. To clear
     * this condition, call {@link #stopRecording()}.
     * <p>
     * <strong>This is a place holder method: not yet implemented.</strong>
     *
     * @param trigger the condition required to trigger the start of
     *                recording
     * @param seconds the number of seconds to elapse after recording has
     *                started, before message recording is shut off
     * @throws NullPointerException if trigger is null
     * @throws IllegalArgumentException if seconds is &lt; 1
     * @throws IllegalStateException if recording is already in progress
     */
    void triggerRecording(TxRxTrigger trigger, int seconds);

    /** Shuts off recording of messages immediately, and clears any trigger
     * that might have been set.
     */
    void stopRecording();

    /** Returns true if TX/RX messages are being recorded.
     *
     * @return {@code true} if TX/RX messages are being recorded
     */
    boolean isRecording();

    /** Returns true if a trigger has been set, but has not yet fired.
     *
     * @return {@code true} if a trigger is set but has not been fired
     */
    boolean isTriggerWaiting();

    /** Retrieves and removes the head of the TX/RX message event queue,
     * waiting if necessary until an element becomes available.
     * <p>
     * This method blocks if there are no elements in the queue.
     *
     * @return the head of the TX/RX queue
     * @throws InterruptedException if interrupted while waiting
     */
    MessageEvent take() throws InterruptedException;

    /** Retrieves and removes the head of the TX/RX message event queue,
     * waiting up to the specified number of milliseconds for an element
     * to become available.
     *
     * @param timeoutMs the maximum number of milliseconds to wait
     * @return the head of the TX/RX queue, or {@code null} if the specified
     *  wait time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     */
    MessageEvent poll(long timeoutMs) throws InterruptedException;

    /** Returns the number of elements in the TX/RX queue.
     *
     * @return the number of elements in the queue
     */
    int size();

    /** Returns {@code true} if the TX/RX queue contains no elements.
     *
     * @return {@code true} if the queue contains no elements
     */
    boolean isEmpty();
}
