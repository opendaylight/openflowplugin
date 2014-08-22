/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.packet.ProtocolId.ARP;
import static org.opendaylight.util.packet.ProtocolId.DHCP;
import static org.opendaylight.util.packet.ProtocolId.IP;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test to verify the protocol ID bit mask functionality.
 *
 * @author Thomas Vachuska
 */
public class PacketMaskTest extends PacketTest {

    private static final String ETH2_DHCP_DISCO = "eth2-ip-udp-dhcp-disco.hex";

    @Test
    public void protocolIdMask() {
        long mask = Packet.computeProtocolMask(ARP, IP, DHCP);
        assertEquals(AM_NEQ, 16 + 32 + 16384, mask);
    }

    @Test
    public void protocolMask() {
        Packet p = Codec.decodeEthernet(getPacketReader(ETH2_DHCP_DISCO));
        assertEquals(AM_NEQ, 2 + 32 + 4096 + 16384, p.protocolMask());
        assertTrue(AM_HUH, p.has(IP));
    }

}
