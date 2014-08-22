/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

/**
 * A listener interested in hearing about OpenFlow datapath events.
 * <p>
 * The event types associated with this listener type are:
 * <ul>
 *     <li> {@link OpenflowEventType#DATAPATH_CONNECTED} </li>
 *     <li> {@link OpenflowEventType#DATAPATH_READY} </li>
 *     <li> {@link OpenflowEventType#DATAPATH_DISCONNECTED} </li>
 *     <li> {@link OpenflowEventType#DATAPATH_REVOKED} </li>
 *     <li> {@link OpenflowEventType#DROPPED_EVENTS_CHECKPOINT} </li>
 * </ul>
 *
 * @see DataPathEvent
 * @author Simon Hunt
 */
public interface DataPathListener extends OpenflowListener<DataPathEvent> {
}
