/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

// FIXME: We should move this to the .impl package to remove it from the
// public view as it is no longer the public facade.

import org.opendaylight.util.packet.ProtocolId;

import java.util.List;
import java.util.Set;

/**
 * A packet sequencer provides the functionality of an OpenFlow
 * <em>Packet-In</em> message event processing engine.
 * <p>
 * Packet {@link SequencedPacketListener listeners} register with the
 * sequencer to participate in the processing chain orchestrated by the
 * sequencer.
 *
 * @author Scott Simes
 * @author Thomas Vachuska
 * @author Simon Hunt
 * @author Frank Wood
 */
public interface PacketSequencer {

    /**
     * Adds the specified packet listener to the sequencer, in the
     * specified role and at the specified altitude.
     * Higher altitudes come earlier in the sequence than lower altitudes
     * (within the specific role).
     * Note that no two listeners (in the same role) may register with
     * the same altitude; if this is attempted an exception will be thrown.
     * <p>
     * The listener's {@link SequencedPacketListener#event event()} callback
     * will be invoked for every <em>Packet-In</em> message that the controller
     * passes to the packet sequencer.
     *
     * @param listener the listener to be added
     * @param role the role the listener wishes to assume
     * @param altitude the listener's altitude
     * @throws NullPointerException if listener or role is null
     * @throws IllegalArgumentException if altitude is negative
     * @throws IllegalStateException if the altitude (for the role)
     *          has already been claimed
     */
    void addPacketListener(SequencedPacketListener listener,
                           SequencedPacketListenerRole role, int altitude);

    /**
     * Adds the specified packet listener to the sequencer, in the
     * specified role and at the specified altitude.
     * Higher altitudes come earlier in the sequence than lower altitudes
     * (within the specific role).
     * Note that no two listeners (in the same role) may register with
     * the same altitude; if this is attempted an exception will be thrown.
     * <p>
     * The {@code interest} argument specifies the protocols that the listener
     * cares about. When the sequencer receives a <em>Packet-In</em> message,
     * it will be forwarded to the listener if the packet contains any protocol
     * that is a member of the specified set.
     *
     * @param listener the listener to be added
     * @param role the role the listener wishes to assume
     * @param altitude the listener's altitude
     * @param interest the protocols the listener is interested in
     * @throws NullPointerException if listener or role is null
     * @throws IllegalArgumentException if altitude is negative
     * @throws IllegalStateException if the altitude (for the role)
     *      has already been claimed
     */
    void addPacketListener(SequencedPacketListener listener,
                           SequencedPacketListenerRole role, int altitude,
                           Set<ProtocolId> interest);

    /**
     * Removes the specified packet listener from the sequencer.
     *
     * @param listener the listener to be removed
     * @throws NullPointerException if listener is null
     */
    void removePacketListener(SequencedPacketListener listener);

    /**
     * Returns a snapshot of the metrics collected for the registered
     * {@link SequencedPacketListener sequenced packet listeners}. Note that
     * the list is returned in the same order that the packet listeners get
     * to see and process the packets.
     *
     * @return the packet listener metrics
     */
    List<SplMetric> getSplMetrics();
}
