/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.event;

/**
 * Abstraction of an event sink, capable of dispatching a single event data to
 * all of its listeners.
 *
 * @author Thomas Vachuska
 */
public interface EventSink {

    /**
     * Dispatches the specified event to all listeners or delegates.
     *
     * @param event event data
     */
    void dispatch(Event event);

}
