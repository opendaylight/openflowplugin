/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.pkt.impl;

import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolId;

import java.util.Set;

/** A packet listener that has been bound to a role and altitude. */
class BoundListener {
    final SequencedPacketListener spl;
    final SequencedPacketListenerRole role;
    final int altitude;
    final Set<ProtocolId> careAbout;
    final long careMask;
    final SplMetricData metric;

    /**
     * Constructor to associate the listener with the chosen role,
     * altitude and the protocols of interest.
     *
     * @param spl the packet listener
     * @param role the role
     * @param altitude the altitude
     * @param careAbout the set of protocols we care about
     */
    BoundListener(SequencedPacketListener spl, SequencedPacketListenerRole role,
                  int altitude, Set<ProtocolId> careAbout) {
        this.spl = spl;
        this.role = role;
        this.altitude = altitude;
        this.careAbout = careAbout;
        this.careMask = careAbout == Sequencer.ALL_PROTOCOLS
                ? Sequencer.ALL_PROTOCOLS_MASK : computeMask(careAbout);
        this.metric = new SplMetricData(spl, role, altitude);
    }

    private long computeMask(Set<ProtocolId> careAbout) {
        return Packet.computeProtocolMask(
                careAbout.toArray(new ProtocolId[careAbout.size()]));
    }

    /**
     * Constructor for a listener that is about to be removed.
     *
     * @param spl the packet listener
     */
    BoundListener(SequencedPacketListener spl) {
        this.spl = spl;
        this.role = null;
        this.altitude = 0;
        this.careAbout = null;
        this.careMask = 0;
        this.metric = null;
    }

    @Override
    public String toString() {
        return "{" + spl.getClass().getName() + "," + role +
                ",alt=" + altitude + ",care=" + careString() +
                "}";
    }

    private String careString() {
        return (careAbout == Sequencer.ALL_PROTOCOLS
                ? "ALL_PROTOCOLS" : careAbout.toString());
    }


    /*
     * IMPLEMENTATION NOTE: This equals/hashCode is written so that
     * BoundListener instances are considered equal IF AND ONLY IF the
     * SequencedPacketListener instance is the same. This is to make the
     * list removal work correctly.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BoundListener that = (BoundListener) o;
        // YES, using == for object reference equality, not .equals()
        return spl == that.spl;
    }

    @Override
    public int hashCode() {
        return spl.hashCode();
    }
}
