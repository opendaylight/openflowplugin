/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

/**
 * An OpenFlow Controller Queue event.
 * <p>
 * These events are consumed by all listeners. They represent queue management
 * events, alerting the listener to when the queue is approaching capacity
 * ("queue-warning") and when the queue has exceeded capacity ("queue-full").
 * <p>
 * Events are also sent when the "reset" thresholds are reached, while
 * draining events off the queue.
 * <p>
 * The event types associated with this event are:
 * <ul>
 *     <li> {@link OpenflowEventType#QUEUE_FULL} </li>
 *     <li> {@link OpenflowEventType#QUEUE_FULL_RESET} </li>
 * </ul>
 *
 * @author Simon Hunt
 */
public interface QueueEvent extends OpenflowEvent {
}
