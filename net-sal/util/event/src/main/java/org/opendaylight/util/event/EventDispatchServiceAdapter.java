/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;


/**
 * An adapter implementation of the {@link EventDispatchService}. Intended for
 * use in implementations of the service which wish to override a handful of
 * selected behaviors, such as unit testing.
 */
public class EventDispatchServiceAdapter implements EventDispatchService {

    @Override
    public void addSink(Class<? extends Event> eventClass, EventSink sink) {
    }

    @Override
    public EventSink get(Class<? extends Event> eventClass) {
        return null;
    }

    @Override
    public void removeSink(Class<? extends Event> eventClass) {
    }

    @Override
    public void post(Event event) {
    }

}
