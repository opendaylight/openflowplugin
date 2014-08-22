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
import org.opendaylight.util.Log;
import org.opendaylight.util.ResourceUtils;
import org.slf4j.Logger;

import java.util.ResourceBundle;

/** Generically typed event reader.
 * <p>
 * It takes events from the handler's queue and pushes them through
 * the listener's callback.
 *
 * @param <L> the type of listener
 * @param <E> the type of event
 *
 * @author Simon Hunt
 */
class EventReader<L extends OpenflowListener<E>, E extends OpenflowEvent>
        implements Runnable {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            EventReader.class, "eventReader");

    private static final String E_EVENT_CALLBACK = RES
            .getString("e_event_callback");
    private static final String E_INTERRUPT = RES.getString("e_interrupt");

    private final EventHandler<L,E> handler;
    private final Logger log;
    private volatile boolean quitting = false;

    EventReader(EventHandler<L, E> h, Logger log) {
        this.handler = h;
        this.log = log;
    }

    void shutdown() {
        quitting = true;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                E ev = handler.eventQ.take(); // BLOCKS when Q is empty
                try {
                    handler.listener.event(ev);
                } catch (Exception e) {
                    log.warn(E_EVENT_CALLBACK,
                            handler.listener.getClass().getName(), e, 
                            Log.stackTraceSnippet(e));
                }
                handler.itemRemoved();
            }
        } catch (InterruptedException ie) {
            if (!quitting)
                log.warn(E_INTERRUPT, handler.listener.getClass().getName(), ie);
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}