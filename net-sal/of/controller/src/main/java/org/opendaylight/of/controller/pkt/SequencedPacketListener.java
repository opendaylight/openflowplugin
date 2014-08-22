/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.pkt;

import org.opendaylight.of.controller.ControllerService;
import org.opendaylight.of.controller.ErrorEvent;

/**
 * Classes implementing this interface may participate in the processing
 * of OpenFlow <em>Packet-In</em> message events, orchestrated by the
 * {@link PacketSequencer}.
 * <p>
 * Packet listeners are registered with the sequencer within a specific
 * {@link SequencedPacketListenerRole role}. Within each role, listeners are ordered
 * by altitude; higher altitudes coming earlier in the sequence than
 * lower altitudes.
 *
 * @see ControllerService#addPacketListener
 * @see ControllerService#removePacketListener
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 * @author Scott Simes
 */
public interface SequencedPacketListener {

    /**
     * Callback invoked when a <em>Packet-In</em> event is to be processed.
     * <p>
     * The supplied context contains the message event, and is used to capture
     * any processing hints that might be required downstream. This includes
     * possibly modifying a "work-in-progress" <em>Packet-Out</em> message
     * attached to the context, or indicating to the sequencer that the
     * packet should be sent or blocked.
     * <p>
     * Note that even after a listener indicates that the packet has been
     * handled, the context is still passed down the chain of listeners;
     * no further processing of the packet is possible, but downstream
     * listeners will be able to observe the result of processing.
     *
     * @param context the message context
     */
    void event(MessageContext context);

    /**
     * This callback will be invoked if an unexpected error occurred during
     * the sequencer's attempt to send the <em>Packet-Out</em> message back
     * to the datapath via the controller, as a result of the packet listener
     * invoking {@link PacketOut#send()}.
     *
     * @param event the error event
     */
    void errorEvent(ErrorEvent event);
}
