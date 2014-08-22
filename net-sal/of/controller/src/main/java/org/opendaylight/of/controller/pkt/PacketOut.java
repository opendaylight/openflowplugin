/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.pkt;

import org.opendaylight.of.lib.instr.Action;

/**
 * Presents {@link SequencedPacketListener}s with the operations they can
 * perform on the <em>Packet-Out</em> currently under construction in
 * the {@link MessageContext}.
 * <p>
 * This approach provides an abstraction for packet listeners, allowing us
 * to present a narrowed API to the mutable <em>Packet-Out</em> message.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface PacketOut {

    // NOTE: Setting the ingress port is not an option here, since the
    //  Sequencer will do that for us, when it examines the packet-in and
    //  instantiates the context.

    /**
     * Adds an action to the list of actions for this packet-out message.
     *
     * @param action the action to add
     * @throws IllegalStateException if caller is not a Director, or if
     *          the packet has already been handled
     */
    void addAction(Action action);

    /**
     * Clears any actions accrued so far, but leaves open the possibility
     * to add others.
     * @throws IllegalStateException if caller is not a Director, or if
     *          the packet has already been handled
     */
    void clearActions();

    /**
     * Blocks any downstream directors from emitting a packet-out response.
     * This is irreversible.
     * @throws IllegalStateException if caller is not a Director, or if
     *          the packet has already been handled
     */
    void block();

    /**
     * Instructs the sequencer to send the packet-out response.
     * This is irreversible.
     * @throws IllegalStateException if caller is not a Director, or if
     *          the packet has already been handled
     */
    void send();

}
