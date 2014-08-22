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
 * ICMPv4 encoder and decoder.
 *
 * @author Frank Wood
 */
class IcmpCodec {

    private static final int HDR_LEN = 4;
    private static final int REQ_REPLY_HDR_LEN = HDR_LEN + 4;

    /**
     * Decodes the protocol.
     *
     * @param r packet reader containing the frame bytes
     * @param len number of bytes for ICMPv4 payload as specified by IP
     * @return the new ICMP protocol
     */
    static Icmp decode(PacketReader r, int len) {
        Icmp.Builder b = new Icmp.Builder();

        try {
            IcmpTypeCode tc = IcmpTypeCode.get(r.readU16());
            b.typeCode(tc);
            b.checkSum(r.readU16());
    
            switch (tc) {
                case ECHO_REQ:
                case ECHO_REPLY:
                    b.ident(r.readU16());
                    b.seqNum(r.readU16());
                    b.bytes(r.readBytes(len - REQ_REPLY_HDR_LEN));
                    break;
                  
                default:
                    b.bytes(r.readBytes(len - HDR_LEN));
                    break;
            }
        } catch (Exception  e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build();
    }
    
    /**
     * Encodes the protocol.
     *
     * @param icmp protocol instance
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Icmp icmp) {
        
        IcmpTypeCode tc = icmp.typeCode();
        
        int len = (tc == IcmpTypeCode.ECHO_REQ || tc == IcmpTypeCode.ECHO_REPLY)
                ? REQ_REPLY_HDR_LEN : HDR_LEN;
        
        len += icmp.bytesArray().length;
        
        PacketWriter w = new PacketWriter(len);

        w.writeU16(icmp.typeCode().code());

        int cswi = w.wi();
        w.writeU16(0); // checksum starts as 0x0000

        switch (icmp.typeCode()) {
            case ECHO_REQ:
            case ECHO_REPLY:
                w.writeU16(icmp.ident());
                w.writeU16(icmp.seqNum());
                break;

            default:
                break;
        }

        w.writeBytes(icmp.bytesArray());
        w.setU16(cswi, ProtocolUtils.checkSumU16(w));
        
        return w;
    }

}
