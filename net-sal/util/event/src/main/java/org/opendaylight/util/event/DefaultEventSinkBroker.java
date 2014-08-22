/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base implementation of {@link EventSinkBroker}.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class DefaultEventSinkBroker implements EventSinkBroker {

    // Registry of all event sinks
    private final Map<Class<? extends Event>, EventSink> sinks =
            new ConcurrentHashMap<Class<? extends Event>, EventSink>();

    @Override
    public void addSink(Class<? extends Event> eventClass, EventSink sink) {
        sinks.put(eventClass, sink);
    }

    @Override
    public void removeSink(Class<? extends Event> eventClass) {
        sinks.remove(eventClass);
    }

    @Override
    public EventSink get(Class<? extends Event> eventClass) {
        EventSink sink = sinks.get(eventClass);
        if (sink != null)
            return sink;

        // not (yet) bound by the instance class
        // perhaps it (or an ancestor) implements the event class...
        Class<?> cls = eventClass;
        while (cls != Object.class) {
            for (Class<?> inf: cls.getInterfaces()) {
                // has to be tagged by the Event interface...
                if (Event.class.isAssignableFrom(inf)) {
                    // we can suppress the warning because we just tested that
                    //  the interface is assignable to Event
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> e = (Class<? extends Event>) inf;
                    sink = sinks.get(e);
                    if (sink != null) {
                        // bind the sink by implementing class...
                        addSink(eventClass, sink);
                        // FIXME: note additional binding, for clean "remove"
                        return sink;
                    }
                }
            }
            cls = cls.getSuperclass();
        }
        return null;
    }

}
