/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.dispatch.impl;

import org.opendaylight.util.event.Event;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.event.EventSink;

/**
 * Test fixture to allow in-thread event dispatching for a single sink.
 *
 * @author Thomas Vachuska
 */
public class TestEventDispatcher implements EventDispatchService {

    private EventSink sink;

    @Override
    public void post(Event event) {
        sink.dispatch(event);
    }

    @Override
    public void addSink(Class<? extends Event> aClass, EventSink eventSink) {
        this.sink = eventSink;
    }

    @Override
    public EventSink get(Class<? extends Event> aClass) {
        return sink;
    }

    @Override
    public void removeSink(Class<? extends Event> aClass) {
        sink = null;
    }
}
