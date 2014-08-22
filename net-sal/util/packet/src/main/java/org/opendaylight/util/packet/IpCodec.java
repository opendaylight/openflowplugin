/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.packet.ProtocolUtils.checkSumU16;

/**
 * IPv4 encoder and decoder.
 *
 * @author Frank Wood
 */
class IpCodec {

    private static final int FIXED_HDR_LEN = 20;
    
    private static final int VERSION_BIT_SHIFT = 4;
    
    private static final int HDR_LEN_MASK = 0x000f;
    private static final int HDR_LEN_MULTIPLE = 4;
    
    private static final int FLAG_DF_MASK   = 0x4000;
    private static final int FLAG_MF_MASK   = 0x2000;
    private static final int FLAG_FRAG_MASK = 0x1fff;

    private static final int DSFC_MASK = 0x3f;
    private static final int DSFC_BIT_SHIFT = 2;
    private static final int ECN_MASK  = 0x03;

    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the protocol bytes
     * @return the new IP protocol
     */
    static Ip decode(PacketReader r) {
        Ip.Builder b = new Ip.Builder();
        
        try {
            int hdrLen = HDR_LEN_MULTIPLE * (HDR_LEN_MASK & r.readU8());
            b.hdrLen(hdrLen);
            
            short u8 = r.readU8();
            b.tosDsfc(IpTosDsfc.get(DSFC_MASK & (u8 >> DSFC_BIT_SHIFT)));
            b.tosEcn(IpTosEcn.get(ECN_MASK & u8));
            
            b.totalLen(r.readU16());
            b.ident(r.readU16());
            
            int flagFrag = r.readU16();
            
            b.doNotFrag(0 != (flagFrag & FLAG_DF_MASK));
            b.moreFragToCome(0 != (flagFrag & FLAG_MF_MASK));
            b.fragOffset(flagFrag & FLAG_FRAG_MASK);
            
            b.ttl(r.readU8());
            
            b.type(IpType.get(r.readU8()));
            
            b.checkSum(r.readU16());
            
            b.srcAddr(r.readIPv4Address());
            b.dstAddr(r.readIPv4Address());
            
            b.options(r.readBytes(hdrLen - FIXED_HDR_LEN));
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }
            
        return b.build(); 
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param ip protocol instance
     * @param ep encoded payload
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Ip ip, EncodedPayload ep) {

        int len = FIXED_HDR_LEN + ip.optionsArray().length;
        
        PacketWriter w = new PacketWriter(len);

        int u8 = (IpVersion.V4.code() << VERSION_BIT_SHIFT)
                    | (HDR_LEN_MASK & (len / HDR_LEN_MULTIPLE)); 
        w.writeU8(u8);
        
        u8 = (ip.tosDsfc().code() << DSFC_BIT_SHIFT) | ip.tosEcn().code();
        w.writeU8(u8);
        
        w.writeU16(len + ep.len());
        w.writeU16(ip.ident());
        
        int flagFrag = ip.fragOffset() & FLAG_FRAG_MASK;
        if (ip.doNotFrag())
            flagFrag |= FLAG_DF_MASK;
        if (ip.moreFragToCome())
            flagFrag |= FLAG_MF_MASK;
                
        w.writeU16(flagFrag);
        
        w.writeU8(ip.ttl());
        w.writeU8(ip.type().code());
        
        // record the checksum index and fill with 0's
        int cswi = w.wi();
        w.writeU16(0);
        
        w.write(ip.srcAddr());
        w.write(ip.dstAddr());

        w.writeBytes(ip.optionsArray());

        // back fill checksum value at the saved position
        w.setU16(cswi, ProtocolUtils.checkSumU16(w));
            
        return w;
    }
    
}
