/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OpenflowMessage;

/**
 * An OpenFlow Controller Message event.
 * <p>
 * These events are consumed by {@link MessageListener}s who wish to be
 * notified when OpenFlow messages arrive at the controller.
 * <p>
 * The event types associated with this event are:
 * <ul>
 *     <li> {@link OpenflowEventType#MESSAGE_RX} </li>
 *     <li> {@link OpenflowEventType#MESSAGE_TX} </li>
 *     <li> {@link OpenflowEventType#DROPPED_EVENTS_CHECKPOINT} </li>
 * </ul>
 * <p>
 * Note that the {@code MESSAGE_TX} event type will never be seen by a
 * message listener; however, the management interface allows the recording
 * of messages both transmitted and received by the controller, and the
 * corresponding list of message events will contain both {@code TX} and
 * {@code RX} types.
 *
 * @author Simon Hunt
 */
public interface MessageEvent extends OpenflowEvent {

    /** Returns the OpenFlow message associated with this event.
     *
     * @return the OpenFlow message
     */
    OpenflowMessage msg();

    /** Returns the ID of the datapath associated with this event.
     *
     * @return the datapath ID
     */
    DataPathId dpid();

    /** Returns the ID of the auxiliary channel.
     *
     * @return the auxiliary channel ID
     */
    int auxId();

    /** Returns the protocol version negotiated between the controller and
     * the associated datapath.
     *
     * @return the negotiated protocol version
     */
    ProtocolVersion negotiated();

    /**
     * Returns an identifier for the remote end of the connection for this
     * event.
     * If successful resolution of the datapath ID, returns the datapath ID.
     * Otherwise, returns the remote address and port information.
     *
     * @return an identifier for this event
     */
    String remoteId();

}
