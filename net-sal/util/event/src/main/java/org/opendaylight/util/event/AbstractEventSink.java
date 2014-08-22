/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Abstraction of an event sink suitable as a base implementation.
 *
 * @param <E> event class
 * @param <L> listener class
 * @author Thomas Vachuska
 */
public abstract class AbstractEventSink<E, L> implements EventSink {

    /**
     * Set of our listeners.
     */
    protected final CopyOnWriteArraySet<L> listeners =
            new CopyOnWriteArraySet<L>();

    /**
     * Adds a new listener.
     *
     * @param listener listener to be added
     */
    public void addListener(L listener) {
        notNull(listener);
        listeners.add(listener);
    }

    /**
     * Removes the specified listener.
     *
     * @param listener listener to be removed
     */
    public void removeListener(L listener) {
        notNull(listener);
        listeners.remove(listener);
    }

    /**
     * Gets all registered listeners.
     *
     * @return set of registered listeners
     */
    public Set<L> getListeners() {
        return Collections.unmodifiableSet(listeners);
    }

    /**
     * Clears all registered listeners.
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Dispatches an event to the specified listener.
     *
     * @param event    event to be dispatched
     * @param listener target listener
     */
    protected abstract void dispatch(E event, L listener);

    @SuppressWarnings("unchecked")
    @Override
    public void dispatch(Event event) {
        for (L listener : listeners) {
            try {
                dispatch((E) event, listener);
            } catch (Throwable t) {
                reportError((E) event, listener, t);
            }
        }
    }

    /**
     * Reports error encountered while dispatching an event to a listener.
     * <p>
     * The base implementation is a no-op.
     *
     * @param event    event that encountered error
     * @param listener that induced the error
     * @param error    error to be reportes
     */
    protected void reportError(E event, L listener, Throwable error) {
        // Do nothing at this point. This is simply to protect against
        // thread death induced by improperly coded dispatch.
    }

}
