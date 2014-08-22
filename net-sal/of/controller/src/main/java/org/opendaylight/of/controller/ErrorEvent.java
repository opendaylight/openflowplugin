/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.of.controller.pkt.PacketSequencer;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;

/**
 * An OpenFlow Controller Error event. These events are consumed by listeners
 * who have only an indirect link to a controller service, and need to be
 * informed of an exception thrown within controller code from a call made
 * on their behalf.
 * <p>
 * For example, the {@link SequencedPacketListener} only gets to instruct the
 * controller to send a <em>Packet-Out</em> message indirectly, via the boolean
 * return value from its {@link SequencedPacketListener#event event()}
 * callback. If an error occurs during the send, the {@link PacketSequencer}
 * will inform the packet listener of the error via its
 * {@link SequencedPacketListener#errorEvent errorEvent()} callback.
 * <p>
 * The event types associated with this event are:
 * <ul>
 *     <li> {@link OpenflowEventType#ERROR} </li>
 * </ul>
 *
 * @author Simon Hunt
 */
public interface ErrorEvent extends OpenflowEvent {

    /** Returns a text message describing the error.
     *
     * @return the error message
     */
    String text();

    /** Returns the cause of the error.
     *
     * @return the cause
     */
    Throwable cause();

    /** Returns an object that provides the context of the error.
     *
     * @return the context
     */
    Object context();
}
