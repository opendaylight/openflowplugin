/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import java.util.ArrayList;
import java.util.List;


/**
 * Internally used encoded payload that keeps track of each of the encoded
 * layers. The layer order is indexed from innermost to outermost (reverse of
 * the {@link Packet} layer order). For example:
 * <pre>
 * encodes[0] = (PacketWriter encoding DHCP - innermost)
 * encodes[1] = (PacketWriter encoding UDP)
 * encodes[2] = (PacketWriter encoding IP)
 * encodes[3] = (PacketWriter encoding ETHERNET - outermost)
 * </pre>
 * 
 * @author Frank Wood
 */
class EncodedPayload {

    private static final int U32_MASK = 0x0ffff;
    private static final int U16_BIT_SHIFT = 16;
    
    private List<PacketWriter> encodes;
    
    /**
     * Constructor that provides initial capacity of the layers.
     * 
     * @param initCapacity initial layer capacity
     */
    EncodedPayload(int initCapacity) {
        encodes = new ArrayList<PacketWriter>(initCapacity);
    }
    
    /**
     * Adds a new layer around the layers already encoded.
     * 
     * @param pw new encoded payload layer to add
     */
    void add(PacketWriter pw) {
        encodes.add(pw);
    }
 
    /**
     * Number of bytes for the entire encoded payload.
     * 
     * @return number of bytes of the entire encoded payload
     */
    int len() {
        int len = 0;
        for (PacketWriter pw: encodes)
            len += pw.wi();
        return len;
    }
    
    /**
     * Produces a single packer writer combining all encoded layers. The output
     * will be in stream encoding order. For example:
     * <pre>
     * [ETHERNET bytes...IP bytes...UDP bytes...DHCP bytes...padding bytes]
     * </pre>
     * If the encoding length is less than the minimum length, padding is added.
     * <p>
     * @param minLen minimum encoding length
     * @return single packet writer combining all the encoded layers.
     */
    PacketWriter flatten(int minLen) {
        int encLen = len();
        int padLen = (encLen < minLen) ? minLen - encLen : 0;
        
        PacketWriter fpw = new PacketWriter(encLen + padLen);
        
        for (int i=encodes.size()-1; i>=0; i--) {
            PacketWriter w = encodes.get(i);
            fpw.writeBytes(w);
        }
        
        fpw.writeZeros(padLen);
        
        return fpw;
    }
    
    /**
     * Generates a U16 checksum based on the packet writer bytes. Iterates
     * over the bytes in the pseudo header, header and each of the encoded
     * payload buffers in the "correct" encoding order by twos computing 1's
     * complement on each successive 16-bit value.
     *
     * @param ipPseudoHdr IPv4/IPv6 pseudo header for the checksum calculation
     * @param hdr header for the checksum calculation (TCP, UDP)
     * @return the U16 checksum
     */
    int checkSumU16(IpPseudoHdr ipPseudoHdr, PacketWriter hdr) {
        PacketWriter pwh = ProtocolUtils.encodePseudoHdrIp(ipPseudoHdr);
        
        long sum = 0;
        sum = ProtocolUtils.checkSumPart(sum, pwh);
        sum = ProtocolUtils.checkSumPart(sum, hdr);
        
        for (int ei=encodes.size()-1; ei>=0; ei--)
            sum = ProtocolUtils.checkSumPart(sum, encodes.get(ei));
        
        return (int) (~((sum & U32_MASK) + (sum >> U16_BIT_SHIFT))) & U32_MASK;
    }    
    
}
