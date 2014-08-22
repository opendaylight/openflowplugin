/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.packet;

/**
 * Classes implementing this interface may participate in the processing
 * of <em>packet</em> events, orchestrated by the {@link PacketProcessingService}.
 * <p>
 * Packet listeners are registered with the packet processing pipeline using a
 * specific {@link org.opendaylight.of.controller.pkt.SequencedPacketListenerRole role}.
 * Within each role, listeners are ordered by altitude; listeners with higher
 * altitudes will process packets earlier in the sequence than those with
 * lower altitudes.
 *
 * @see org.opendaylight.net.packet.PacketProcessingService#addListener
 * @see org.opendaylight.net.packet.PacketProcessingService#removeListener
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Uyen Chau
 * @author Thomas Vachuska
 */
public interface PacketListener {

    /**
     * Callback invoked when an incoming <em>packet</em> event is to be processed.
     * <p>
     * The supplied context contains the packet event, and is used to capture
     * any processing hints that might be required downstream. This includes
     * possibly modifying a "work-in-progress" response <em>packet</em>
     * attached to the context, or indicating to the pipeline that the
     * packet should be either sent or blocked.
     * <p>
     * Note that even after a listener indicates that the packet has been
     * handled, the context is still passed down the chain of downstream
     * listeners. While no further handling of the packet is possible, the
     * downstream listeners will be able to observe the packet event and how
     * it has been handled.
     *
     * @param context the packet context
     */
    void processPacket(PacketContext context);

    /**
     * This callback will be invoked if an unexpected error occurred during
     * the packet pipeline's attempt to send the response <em>packet</em> back
     * to the network device, as a result of this packet listener
     * invoking {link org.opendaylight.net.packet.ResponsePacket#send()}.
     *
     * @param error the packet handling error
     */
    void processError(PacketError error);
}
