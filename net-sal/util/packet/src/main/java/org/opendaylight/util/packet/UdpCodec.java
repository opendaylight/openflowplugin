/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.TcpUdpPort;


/**
 * UDP encoder and decoder.
 *
 * @author Frank Wood
 */
class UdpCodec {

    static final int FIXED_HDR_LEN = 8;

    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the protocol bytes
     * @return the new UDP protocol
     */
    static Udp decode(PacketReader r) {
        Udp.Builder b = new Udp.Builder();

        try {
            b.srcPort(TcpUdpPort.udpPort(r.readU16()));
            b.dstPort(TcpUdpPort.udpPort(r.readU16()));
            b.len(r.readU16());
            b.checkSum(r.readU16());
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build(); 
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param udp protocol instance
     * @param ep encoded payload
     * @param ipPseudoHdr IP pseudo header needed for checksum
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Udp udp, EncodedPayload ep,
            IpPseudoHdr ipPseudoHdr) {
        
        int len = FIXED_HDR_LEN + ep.len();
        PacketWriter w = new PacketWriter(len);
        
        w.writeU16(udp.srcPort().getNumber());
        w.writeU16(udp.dstPort().getNumber());
        w.writeU16(len);
        
        int cswi = w.wi();
        w.writeU16(0); // checksum starts as 0x0000
        
        w.setU16(cswi, ep.checkSumU16(ipPseudoHdr.len(len), w));
        
        return w;
    }
    
}
