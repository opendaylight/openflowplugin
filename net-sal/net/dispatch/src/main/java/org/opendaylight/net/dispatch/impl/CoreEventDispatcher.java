/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.dispatch.impl;

import org.opendaylight.util.event.DefaultEventSinkBroker;
import org.opendaylight.util.event.Event;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.event.EventSink;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static org.opendaylight.util.ResourceUtils.getBundledResource;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Centralized asynchronous event dispatch mechanism.
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
@Service
public class CoreEventDispatcher extends DefaultEventSinkBroker
        implements EventDispatchService {

    private final Logger log = LoggerFactory.getLogger(CoreEventDispatcher.class);

    // Queue of events to be dispatched
    private final BlockingQueue<Event> events = new LinkedBlockingQueue<>();

    // Event for dispatcher shutdown
    private static final Event KILL_PILL = new Event() {
    };

    // Dispatcher and its host executor.
    private final Dispatcher dispatcher = new Dispatcher();
    private final Executor executor =
            newSingleThreadExecutor(namedThreads("EventDispatch"));

    // log message strings
    private static final ResourceBundle RES = getBundledResource(CoreEventDispatcher.class);

    private static final String MSG_EVT_DISPATCH_ACTIVE = RES
            .getString("msg_evt_dispatch_active");
    private static final String MSG_EVT_DISPATCH_DEACTIVE = RES
            .getString("msg_evt_dispatch_deactive");
    private static final String E_NO_EVT_REGISTER = RES
            .getString("e_no_evt_register");
    private static final String E_EVT_INTERCEPT = RES
            .getString("e_evt_intercept");
    private static final String E_EVT_INTERRUPT = RES
            .getString("e_evt_interrupt");
    private static final String MSG_EVT_DISPATCH_DOWN = RES
            .getString("msg_evt_dispatch_down");

    @Override
    public void post(Event event) {
        events.add(event);
    }

    @Activate
    public void activate() {
        log.info(MSG_EVT_DISPATCH_ACTIVE);
        // Clear any prior events, then reset and resubmit the dispatcher.
        events.clear();
        dispatcher.stopped = false;
        executor.execute(dispatcher);
    }

    @Deactivate
    public void deactivate() {
        log.info(MSG_EVT_DISPATCH_DEACTIVE);
        // Mark the dispatcher as stopped and feed it the kill-pill.
        dispatcher.stopped = true;
        post(KILL_PILL);
    }

    // Auxiliary responsible for removing events from the queue and
    // dispatching them to their respective event sink.
    private class Dispatcher implements Runnable {

        volatile boolean stopped = false;

        @Override
        public void run() {
            while (!stopped) {
                try {
                    Event event = events.take();
                    if (event == KILL_PILL)
                        break;

                    // Find the event sink and make sure we have one.
                    EventSink sink = get(event.getClass());
                    if (sink == null) {
                        log.warn(E_NO_EVT_REGISTER, event.getClass());
                        continue;
                    }

                    // Dispatch the event, while capturing any escaped errors.
                    try {
                        sink.dispatch(event);
                    } catch (Exception e) {
                        log.warn(E_EVT_INTERCEPT, e);
                    }

                } catch (InterruptedException e) {
                    log.warn(E_EVT_INTERRUPT);
                }
            }
            log.info(MSG_EVT_DISPATCH_DOWN);
            events.clear();
        }
    }
}