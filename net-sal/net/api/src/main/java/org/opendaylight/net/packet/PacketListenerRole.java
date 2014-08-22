/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.packet;

/**
 * Represents the major bands of responsibility for packet processing.
 *
 * @author Simon Hunt
 * @author Uyen Chau
 * @author Thomas Vachuska
 * @author Scott Simes
 */
public enum PacketListenerRole {

    /**
     * The <em>Advisor</em> role is provided for those packet listeners who
     * wish to annotate the {@link org.opendaylight.net.packet.PacketContext}
     * with information useful to listeners further downstream.
     * It is expected that <em>Advisors</em> will not be modifying the packet
     * in any way.
     */
    ADVISOR,

    /**
     * The <em>Director</em> role is provided for those packet listeners who
     * will be programming the <em>Packet-Out</em> message in some way.
     */
    DIRECTOR,

    /**
     * The <em>Observer</em> role is provided for those packet listeners who
     * wish to observe the outcome of packet processing, but who will not be
     * taking an active part.
     */
    OBSERVER

}
