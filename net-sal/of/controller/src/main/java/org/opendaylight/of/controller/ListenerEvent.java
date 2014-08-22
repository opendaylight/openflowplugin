/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

/**
 * An OpenFlow Controller Listener event.
 * <p>
 * These events are consumed by the {@link RegistrationListener} who wishes
 * to be notified of the addition or removal of other listeners to the
 * controller.
 * <p>
 * The event types associated with this event are:
 * <ul>
 *     <li> {@link OpenflowEventType#LISTENER_ADDED} </li>
 *     <li> {@link OpenflowEventType#LISTENER_REMOVED} </li>
 * </ul>
 *
 * @author Simon Hunt
 */
public interface ListenerEvent extends OpenflowEvent {

    /** Returns the listener associated with this event.
     *
     * @return the listener
     */
    OpenflowListener<?> listener();

}
