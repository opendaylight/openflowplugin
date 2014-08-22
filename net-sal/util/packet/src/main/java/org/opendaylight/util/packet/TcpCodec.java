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
 * TCP encoder and decoder.
 *
 * @author Frank Wood
 */
class TcpCodec {

    static final int FIXED_HDR_LEN = 20;
    
    private static final int OFFSET_MASK = 0x0f000;
    private static final int OFFSET_BIT_SHIFT = 12;
    
    private static final int HDR_LEN_MULTIPLE = 4;
    
    private static final int FLAGS_MASK  = 0x00fff;
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the protocol bytes
     * @return new IP protocol
     */
    static Tcp decode(PacketReader r) {
        Tcp.Builder b = new Tcp.Builder();

        try {
            b.srcPort(TcpUdpPort.tcpPort(r.readU16()));
            b.dstPort(TcpUdpPort.tcpPort(r.readU16()));
            b.seqNum(r.readU32());
            b.ackNum(r.readU32());
            
            int offsetFlags = r.readU16();
            
            int hdrLen = ((offsetFlags & OFFSET_MASK) >> OFFSET_BIT_SHIFT)
                            * HDR_LEN_MULTIPLE;
            b.hdrLen(hdrLen);
            
            b.flags(offsetFlags & FLAGS_MASK);
            
            b.winSize(r.readU16());
            b.checkSum(r.readU16());
            b.urgentPtr(r.readU16());
            b.options(r.readBytes(hdrLen - FIXED_HDR_LEN));
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build(); 
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param tcp protocol instance
     * @param ep encoded payload
     * @param ipPseudoHdr IP pseudo header needed for checksum
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Tcp tcp, EncodedPayload ep,
            IpPseudoHdr ipPseudoHdr) {
        
        int hdrLen = FIXED_HDR_LEN + tcp.optionsArray().length;
        
        PacketWriter w = new PacketWriter(hdrLen);
        
        w.writeU16(tcp.srcPort().getNumber());
        w.writeU16(tcp.dstPort().getNumber());
        w.writeU32(tcp.seqNum());
        w.writeU32(tcp.ackNum());
        
        w.writeU16(((hdrLen / HDR_LEN_MULTIPLE) << OFFSET_BIT_SHIFT)
                       | (FLAGS_MASK & tcp.flags()));
        w.writeU16(tcp.winSize());
        
        int cswi = w.wi();
        w.writeU16(0); // checksum starts as 0x0000

        w.writeU16(tcp.urgentPtr());
        w.writeBytes(tcp.optionsArray());
        
        w.setU16(cswi, ep.checkSumU16(ipPseudoHdr.len(hdrLen + ep.len()), w));
        
        return w;
    }
    
}
