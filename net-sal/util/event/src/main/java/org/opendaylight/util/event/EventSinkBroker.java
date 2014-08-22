/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;

/**
 * Broker for registering various event sinks capable of handling designated
 * event types.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public interface EventSinkBroker {

    /**
     * Adds a new event sink associated with the specified event class.
     *
     * @param eventClass class of events handled by the sink
     * @param sink event sink
     * @throws IllegalStateException if the event class already has a sink
     */
    void addSink(Class<? extends Event> eventClass, EventSink sink);

    /**
     * Get the event sink associated with the given event class.
     *
     * @param eventClass class of events
     * @return event sink; null if no sink has been registered for the class
     */
    EventSink get(Class<? extends Event> eventClass);

    /**
     * Removes a previously added event sink.
     *
     * @param eventClass class of events handled by the sink
     * @throws IllegalStateException if the given sink is not found
     */
    void removeSink(Class<? extends Event> eventClass);

}
