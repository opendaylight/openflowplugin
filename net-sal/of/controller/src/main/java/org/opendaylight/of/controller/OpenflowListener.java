/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

/**
 * The super-interface for listeners interested in
 * hearing about OpenFlow Controller events.
 *
 * @param <E> the event type
 * @author Simon Hunt
 */
public interface OpenflowListener<E extends OpenflowEvent> {

    /** This callback is invoked by the controller if the listener's
     * event queue is nearing, or has exceeded its capacity, or if the
     * queue has been drained below the associated "reset" levels.
     *
     * @param event the queue event
     */
    void queueEvent(QueueEvent event);

    /** This callback is invoked by the listener's dedicated queue-reader
     * thread every time it pulls an event off the queue.
     *
     * @param event the event
     */
    void event(E event);
}
