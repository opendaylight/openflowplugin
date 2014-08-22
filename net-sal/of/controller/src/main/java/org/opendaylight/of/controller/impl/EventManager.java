/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.flow.*;
import org.opendaylight.util.event.AbstractEventSink;
import org.opendaylight.util.event.EventSinkBroker;

/**
 * A controller module for handling the registration of event listeners, and
 * the dispatching of events to them.
 *
 * @author Simon Hunt
 */
class EventManager {

    final FlowEventSink flowSink = new FlowEventSink();
    final GroupEventSink groupSink = new GroupEventSink();
    final MeterEventSink meterSink = new MeterEventSink();

    /** Initialize the event manager by registering the event classes with
     * the sinks that will handle the dispatching of those events.
     *
     * @param esb the event sink broker
     * @return self, for chaining
     */
    EventManager init(EventSinkBroker esb) {
        esb.addSink(FlowEvent.class, flowSink);
        esb.addSink(GroupEvent.class, groupSink);
        esb.addSink(MeterEvent.class, meterSink);
        return this;
    }

    /** Called at controller shutdown. */
    void shutdown() {
        // TODO: review - do we need to unregister from the event dispatcher?
    }

    // =====================================================================

    /** Adds an event listener to the specified event sink.
     *
     * @param sink the sink
     * @param listener the listener to add
     */
    <L> void add(L listener, AbstractEventSink<?,L> sink) {
            sink.addListener(listener);
    }

    /** Removes an event listener from the specified event sink.
     *
     * @param sink the sink
     * @param listener the listener to remove
     */
    <L> void remove(L listener, AbstractEventSink<?,L> sink) {
            sink.removeListener(listener);
    }

    // =====================================================================
    // === The event sink implementations

    private static class FlowEventSink
            extends AbstractEventSink<FlowEvent, FlowListener> {
        @Override
        protected void dispatch(FlowEvent event, FlowListener listener) {
            listener.event(event);
        }
    }

    private static class GroupEventSink
            extends AbstractEventSink<GroupEvent, GroupListener> {
        @Override
        protected void dispatch(GroupEvent event, GroupListener listener) {
            listener.event(event);
        }
    }

    private static class MeterEventSink
            extends AbstractEventSink<MeterEvent, MeterListener> {
        @Override
        protected void dispatch(MeterEvent event, MeterListener listener) {
            listener.event(event);
        }
    }
}
