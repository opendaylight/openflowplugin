/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

/**
 * A listener interested in hearing about OpenFlow message events.
 * <p>
 * The event types associated with this listener type are:
 * <ul>
 *     <li> {@link OpenflowEventType#MESSAGE_RX} </li>
 *     <li> {@link OpenflowEventType#DROPPED_EVENTS_CHECKPOINT} </li>
 * </ul>
 *
 * @author Simon Hunt
 */
public interface MessageListener extends OpenflowListener<MessageEvent> {
}
