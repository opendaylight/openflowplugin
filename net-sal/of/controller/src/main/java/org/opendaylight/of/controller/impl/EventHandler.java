/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.OpenflowEvent;
import org.opendaylight.of.controller.OpenflowListener;
import org.opendaylight.of.lib.msg.MessageType;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static org.opendaylight.of.controller.OpenflowEventType.QUEUE_FULL;
import static org.opendaylight.of.controller.OpenflowEventType.QUEUE_FULL_RESET;
import static org.opendaylight.of.controller.impl.ListenerManager.Q_FULL_RESET_THRESHOLD;

/** Generically typed handler for event listeners.
 * Associates a listener with its event queue and event reader.
 *
 * @param <L> the type of listener
 * @param <E> the type of event
 *
 * @author Simon Hunt
 */
abstract class EventHandler<L extends OpenflowListener<E>,
                            E extends OpenflowEvent> {
    final L listener;
    final Set<MessageType> careAbout;
    final LinkedBlockingQueue<E> eventQ;
    final double capacity;
    EventReader<L,E> reader;
    Future<?> future;
    volatile boolean onProbation;

    /** Constructs an event handler for the given listener, constructing
     * a bounded queue of the specified capacity.
     *
     * @param listener the event listener
     * @param qCapacity the queue capacity
     */
    EventHandler(L listener, int qCapacity) {
        this.listener = listener;
        this.capacity = qCapacity;
        careAbout = null;
        eventQ = new LinkedBlockingQueue<E>(qCapacity);
    }

    /** Constructs an event handler for the given listener, making note
     * of the message types that the listener is interested in, constructing
     * a bounded queue of the specified capacity.
     *
     * @param listener the (message) event listener
     * @param types the message types of interest
     * @param qCapacity the queue capacity
     */
    EventHandler(L listener, Set<MessageType> types, int qCapacity) {
        this.listener = listener;
        this.capacity = qCapacity;
        careAbout = (types == null || types.isEmpty())
                ? ListenerManager.ALL_TYPES : types;
        eventQ = new LinkedBlockingQueue<E>(qCapacity);
    }

    /** Constructs a neutered event handler for the purpose of
     * removing an existing handler from our set of listeners.
     *
     * @param listener the event listener
     */
    EventHandler(L listener) {
        this.listener = listener;
        capacity = 0;
        careAbout = null;
        eventQ = null;
    }

    // NOTE: toString() reflects the implementation class of the listener
    @Override
    public String toString() {
        return "{OpenflowListener: " + listener.getClass().getName() + "}";
    }

    /** Allows us to mark the handler as "on probation", meaning that no more
     * events will be posted to the queue until it has been drained below the
     * {@link ListenerManager#Q_FULL_RESET_THRESHOLD reset threshold}.
     * When no longer on probation, {@code b == false},
     * a "dropped events checkpoint" event is added to the queue, and normal
     * event-posting service is resumed.
     *
     * @param b true, if on probation; false otherwise
     * @return true if a state change occurred
     */
    public synchronized boolean putOnProbation(boolean b) {
        // if already on probation, bail
        if ((onProbation && b) || (!onProbation && !b))
            return false;

        onProbation = b;

        if (onProbation)
            listener.queueEvent(new QueueEvt(QUEUE_FULL));
        else {
            eventQ.add(getResumedEvent());
            listener.queueEvent(new QueueEvt(QUEUE_FULL_RESET));
        }
        return true;
    }

    /** Subclasses must return a DROPPED_EVENTS_CHECKPOINT checkpoint event
     * of the appropriate class.
     * @return a freshly minted checkpoint event
     */
    protected abstract E getResumedEvent();

    /** Called every time the event reader pulls an item off the queue. */
    void itemRemoved() {
        // if we are on probation, we need to determine if the queue has
        // been drained below the reset threshold percentage.
        if (onProbation) {
            double currentLevel = eventQ.size() / capacity;
            if (currentLevel < Q_FULL_RESET_THRESHOLD)
                putOnProbation(false);
        }
    }

    /* IMPLEMENTATION NOTE:
     *  equals() and hashCode() have been overridden to declare two
     *  EventHandlers equivalent IF AND ONLY IF their listener
     *  reference is the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventHandler<?, ?> that = (EventHandler<?, ?>) o;
        // Force equivalence by ref only (do NOT use .equals())
        return listener == that.listener;
    }

    @Override
    public int hashCode() {
        return listener.hashCode();
    }

}
