/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

import org.opendaylight.of.lib.msg.OfmPacketIn;
import org.opendaylight.of.lib.msg.OfmPacketOut;
import org.opendaylight.of.lib.msg.OpenflowMessage;

/**
 * Represents statistics about the number of OpenFlow messages handled by
 * the controller. Note that counts for <em>PacketIn</em>/<em>PacketOut</em>
 * are separated from the remaining message types. The duration since the
 * counts were last reset to zero is also provided allowing throughput
 * statistics to be calculated.
 *
 * @author Simon Hunt
 */
public interface ControllerStats {

    /**
     * Returns the number of milliseconds since the statistics were last
     * reset to zero.
     *
     * @return milliseconds since last reset to zero
     */
    long duration();

    /**
     * Returns the number of {@link OfmPacketIn <em>PacketIn</em>} messages
     * received by the controller.
     *
     * @return the count of <em>PacketIn</em>s
     */
    long packetInCount();

    /**
     * Returns the total number of bytes reported via the
     * {@link OfmPacketIn#getTotalLen() total length} fields of all
     * <em>PacketIn</em> messages received by the controller.
     *
     * @return the total number of bytes reported by <em>PacketIn</em>
     *          messages
     */
    long packetInBytes();

    /**
     * Returns the number of {@link OfmPacketOut <em>PacketOut</em>} messages
     * issued by the controller. Note that this number includes synthetic
     * packets not otherwise associated with a <em>PacketIn</em> message.
     *
     * @return the count of <em>PacketIn</em>s
     */
    long packetOutCount();

    /**
     * Returns the total number of bytes from packets "processed" by the
     * controller, via the <em>PacketOut</em> messages issued.
     *
     * @return the total number of bytes reported by <em>PacketOut</em>
     *          messages
     */
    long packetOutBytes();

    /**
     * Returns the number of {@link OfmPacketIn <em>PacketIn</em>} messages
     * for which no {@link OfmPacketOut <em>PacketOut</em>} message was issued.
     *
     * @return the count of "dropped" packets
     */
    long packetDropCount();

    /**
     * Returns the total number of bytes reported via the
     * {@link OfmPacketIn#getTotalLen() total length} fields of all
     * <em>PacketIn</em> messages for which no <em>PacketOut</em> message
     * was issued.
     *
     * @return the total number of bytes reported by "dropped"
     *          <em>PacketIn</em> messages
     */
    long packetDropBytes();

    /**
     * Returns the number of {@link OpenflowMessage OpenFlow messages} received
     * by the controller (excluding <em>PacketIn</em>s).
     *
     * @return the count of OpenFlow messages received
     *          (excluding <em>PacketIn</em>s)
     */
    long msgRxCount();

    /**
     * Returns the number of {@link OpenflowMessage OpenFlow messages} issued
     * by the controller (excluding <em>PacketOut</em>s).
     *
     * @return the count of OpenFlow messages issued
     *          (excluding <em>PacketOut</em>s)
     */
    long msgTxCount();

}
