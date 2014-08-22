/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.net.IpAddress;

/**
 * An OpenFlow Controller DataPath event.
 * <p>
 * These events are consumed by {@link DataPathListener}s who wish to be
 * notified when datapaths connect to, or disconnect from, the controller.
 * <p>
 * The event types associated with this event are:
 * <ul>
 *     <li> {@link OpenflowEventType#DATAPATH_CONNECTED} </li>
 *     <li> {@link OpenflowEventType#DATAPATH_READY} </li>
 *     <li> {@link OpenflowEventType#DATAPATH_DISCONNECTED} </li>
 *     <li> {@link OpenflowEventType#DATAPATH_REVOKED} </li>
 *     <li> {@link OpenflowEventType#DROPPED_EVENTS_CHECKPOINT} </li>
 * </ul>
 * <p>
 * The {@code DATAPATH_CONNECTED} event is emitted after handshaking
 * with the device has completed, but before default flows are laid down.
 * <p>
 * The {@code DATAPATH_READY} event is emitted after default flows have
 * been installed on the device.
 * <p>
 * The {@code DATAPATH_DISCONNECTED} event is emitted when a device
 * disconnects from the controller.
 * <p>
 * The {@code DATAPATH_REVOKED} event is emitted in the rare case when a
 * device attempts to connect with a datapath ID that is a duplicate of an
 * already-connected device.
 * <p>
 * The {@code DROPPED_EVENTS_CHECKPOINT} event is a place-holder indicating
 * where in a consumer's event queue events were dropped because of a
 * queue-full condition.
 *
 * @author Simon Hunt
 */
public interface DataPathEvent extends OpenflowEvent {

    /** Returns the id of the datapath associated with this event.
     *
     * @return the datapath id
     */
    DataPathId dpid();

    /** Returns the protocol version negotiated between the controller and
     * the associated datapath.
     *
     * @return the negotiated protocol version
     */
    ProtocolVersion negotiated();

    /** Returns the IP address of the datapath (i.e.&nbsp;the remote address
     * of the network connection from the switch).
     *
     * @return the IP address of the datapath
     */
    IpAddress ip();

}
