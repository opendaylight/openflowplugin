/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.packet;

import org.opendaylight.util.packet.ProtocolId;

import java.util.List;
import java.util.Set;

/**
 * Abstraction of a service which allows consumers to participate in packet
 * processing pipeline.
 *
 * @author Uyen Chau
 * @author Thomas Vachuska
 */
public interface PacketProcessingService {

    /**
     * Adds the specified packet listener to the pipeline, in the
     * specified role and at the specified altitude.
     * Higher altitudes come earlier in the sequence than lower altitudes
     * (within the specific role).
     * Note that no two listeners (in the same role) may register with
     * the same altitude; if this is attempted an exception will be thrown.
     * <p/>
     * The listener's {@link PacketListener#processPacket} callback
     * will be invoked for every inbound <em>packet</em> event that the
     * controller passes through the packet pipeline.
     *
     * @param listener the listener to be added
     * @param role     the role the listener wishes to assume
     * @param altitude the listener's altitude
     * @throws NullPointerException if listener or role is null
     * @throws IllegalArgumentException if altitude is negative
     * @throws IllegalStateException if the altitude (for the role)
     * has already been claimed
     */
    void addListener(PacketListener listener,
                     PacketListenerRole role, int altitude);

    /**
     * Adds the specified packet listener to the pipeline, in the
     * specified role and at the specified altitude.
     * Higher altitudes come earlier in the sequence than lower altitudes
     * (within the specific role).
     * Note that no two listeners (in the same role) may register with
     * the same altitude; if this is attempted an exception will be thrown.
     * <p/>
     * The {@code interest} argument specifies the protocols that the listener
     * cares about. As each <em>packet</em> event traverses the pipeline
     * it will be handed to the listener only if the packet contains any
     * protocol that is a member of the specified interest set.
     *
     * @param listener the listener to be added
     * @param role     the role the listener wishes to assume
     * @param altitude the listener's altitude
     * @param interest the protocols the listener is interested in
     * @throws NullPointerException if listener or role is null
     * @throws IllegalArgumentException if altitude is negative
     * @throws IllegalStateException if the altitude (for the role)
     * has already been claimed
     */
    void addListener(PacketListener listener,
                     PacketListenerRole role, int altitude,
                     Set<ProtocolId> interest);

    /**
     * Removes the specified packet listener from the packet processing pipeline.
     *
     * @param listener the listener to be removed
     * @throws NullPointerException if listener is null
     */
    void removeListener(PacketListener listener);

    /**
     * Returns a snapshot of the metrics collected for the registered
     * {@link PacketListener  packet listeners}. Note that
     * the list is returned in the same order that the packet listeners get
     * to see and process the packets.
     *
     * @return the packet listener metrics
     */
    List<PacketProcessingMetric> getMetrics();

}
