/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.event;

/**
 * A "typed" event that is timestamped. The event types are defined in an
 * enumeration.
 *
 * @param <T> Event type class
 *
 * @author Simon Hunt
 */
public interface TypedEvent<T extends Enum<?>> extends Event {

    /** Returns the event timestamp - when it happened.
     * <p>
     * The timestamp is set to {@link System#currentTimeMillis()} at the
     * time the event was created.
     *
     * @return the timestamp
     */
    long ts();

    /** Returns the type of event - what happened.
     *
     * @return the event type
     */
    T type();

}
