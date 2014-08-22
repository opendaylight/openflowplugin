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
 * IPv6 encoder and decoder.
 *
 * @author Frank Wood
 */
class IpCodecV6 {

    private static final int FIXED_HDR_LEN = 40;

    private static final int VERSION_BIT_SHIFT = 28;
    
    private static final int TC_FL_MASK = 0x0fffffff;
    
    private static final int DSCP_MASK = 0x0fc00000;
    private static final int DSCP_BIT_SHIFT = 22;
    
    private static final int ECN_MASK  = 0x00300000;
    private static final int ECN_BIT_SHIFT = 20;
    
    private static final int FL_MASK   = 0x000fffff;
    
    /*
     * The HdrExtLen field is the length of the header, in multiples of 8
     * bytes, not including the first 8 bytes.  For example:
     *  If HdrExtLen = 0 the length of the header is 8 bytes:
     *      nextHdr: 1 byte, hdrExtLen: 1 byte, we need to read 6 more bytes
     *  If HdrExtLen = 1 the length of the header is 16 bytes:
     *      nextHdr: 1 byte, hdrExtLen: 1 byte, we need to read 14 more bytes
     * (package-private because used in IPv6 verification)
     */
    static final int OPTION_FIXED_HDR_LEN = 2;
    static final int OPTION_LEN_MULTIPLE = 8;
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the protocol bytes
     * @return the new IPv6 protocol
     */
    static IpV6 decode(PacketReader r) {
        IpV6.Builder b = new IpV6.Builder();
        
        try {
            int u28 = (int) (TC_FL_MASK & r.readU32());
            
            b.tosDsfc(IpTosDsfc.get((DSCP_MASK & u28) >> DSCP_BIT_SHIFT));
            b.tosEcn(IpTosEcn.get((ECN_MASK & u28) >> ECN_BIT_SHIFT));
            b.flowLabel(FL_MASK & u28);
                
            int payloadLen = r.readU16();
            b.payloadLen(payloadLen);
            
            int nextProtocolLen = payloadLen;
            b.nextProtocolLen(nextProtocolLen);
                
            IpType nextHdr = IpType.get(r.readU8());
            b.nextHdr(nextHdr);
            b.nextProtocol(nextHdr);
            b.hopLimit(r.readU8());
            b.srcAddr(r.readIPv6Address());
            b.dstAddr(r.readIPv6Address());
                
            List<IpV6.Option> options = new ArrayList<IpV6.Option>();

            while (IpType.isExtHdrV6(nextHdr)
                        && nextHdr != IpType.IPV6_NO_NEXT_HDR) {
                
                IpType t = nextHdr;
                
                nextHdr = IpType.get(r.readU8());
                b.nextProtocol(nextHdr);
                
                // HdrExtLen field is a multiple of 8 bytes, but doesn't
                // include the first 8 bytes (fragment extension is 0).
                int len = (1 + r.readU8()) * OPTION_LEN_MULTIPLE;
                
                nextProtocolLen -= len;
                b.nextProtocolLen(nextProtocolLen);
                
                len -= OPTION_FIXED_HDR_LEN;
                
                options.add(new IpV6.Option(t, nextHdr, r.readBytes(len)));
            }
            b.options(options.toArray(new IpV6.Option[options.size()]));
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }
            
        return b.build(); 
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param ip IPv6 protocol instance
     * @param ep encoded payload
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(IpV6 ip, EncodedPayload ep) {

        int optionsLen = 0;
        for (IpV6.Option opt: ip.optionsArray())
            optionsLen += OPTION_FIXED_HDR_LEN + opt.bytesArray().length;
        
        PacketWriter w = new PacketWriter(FIXED_HDR_LEN + optionsLen);

        int payloadLen = ep.len() + optionsLen;
        
        long u32 = (IpVersion.V6.code() << VERSION_BIT_SHIFT) |
                   (ip.tosDsfc().code() << DSCP_BIT_SHIFT) |
                   (ip.tosEcn().code() << ECN_BIT_SHIFT) |
                   ip.flowLabel();
        w.writeU32(u32);
        
        w.writeU16(payloadLen);
        w.writeU8(ip.nextHdr().code());
        w.writeU8(ip.hopLimit());
        w.write(ip.srcAddr());
        w.write(ip.dstAddr());
        
        for (IpV6.Option o: ip.optionsArray()) {
            w.writeU8(o.nextHdr().code());
            w.writeU8(((o.bytesArray().length + OPTION_FIXED_HDR_LEN)
                        / OPTION_LEN_MULTIPLE) - 1);
            w.writeBytes(o.bytesArray());
        }
        
        return w;
    }
    
}
