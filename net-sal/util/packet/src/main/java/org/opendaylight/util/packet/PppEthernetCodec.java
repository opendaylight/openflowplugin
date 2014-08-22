/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.SafeMap;
import org.opendaylight.util.packet.PppEthernet.Code;
import org.opendaylight.util.packet.PppEthernet.PppProtocolId;


/**
 * PPP-over-Ethernet encoder and decoder.
 *
 * @author Frank Wood
 */
class PppEthernetCodec {

    /**
     * Internally used to convert the PPP-over-Ethernet protocol ID to the
     * Packet Library {@link ProtocolId} enumeration. If the default
     * {@link ProtocolId#UNKNOWN} is returned it means that this class should
     * handle the payload bytes completely.
     */
    static final SafeMap<PppProtocolId, ProtocolId> PPP_ETH_2_ID =
            new SafeMap.Builder<PppProtocolId, ProtocolId>(ProtocolId.UNKNOWN)
                .add(PppProtocolId.PPP_IP, ProtocolId.IP)
                .add(PppProtocolId.PPP_IPV6, ProtocolId.IPV6)
                .build();
    
    private static final short VERSION_MASK = 0x00f0;
    private static final int VERSION_BIT_SHIFT = 4;
    
    private static final short TYPE_MASK = 0x000f;
    
    private static final int FIXED_HDR_LEN = 6;
    private static final int PPP_PROTO_ID_LEN = 2;
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the frame bytes
     * @return the new PPP-over-Ethernet protocol
     * @throws ProtocolException if there is an error parsing from reader
     */
    static PppEthernet decode(PacketReader r) {
        PppEthernet.Builder b = new PppEthernet.Builder();

        try {
            short u8 = r.readU8();
            b.version( (VERSION_MASK & u8) >> VERSION_BIT_SHIFT );
            b.type(TYPE_MASK & u8);
            
            Code code = Code.get(r.readU8());
            b.code(code);
            b.sessionId(r.readU16());
            
            int len = r.readU16();
            b.len(len);
            
            if (code == Code.SESSION_DATA) {
                PppProtocolId pppProtoId = PppProtocolId.get(r.readU16());
                b.pppProtocolId(pppProtoId);
                len -= PPP_PROTO_ID_LEN;
                if (ProtocolId.UNKNOWN == PPP_ETH_2_ID.get(pppProtoId))
                    b.bytes(r.readBytes(len));
            }
            else
                b.bytes(r.readBytes(len));
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build();
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param pppEth protocol instance
     * @param ep encoded payload
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(PppEthernet pppEth, EncodedPayload ep) {
        int len = pppEth.bytesArray().length;
        
        if (pppEth.code() == Code.SESSION_DATA)
            len += PPP_PROTO_ID_LEN;
        
        PacketWriter w = new PacketWriter(FIXED_HDR_LEN + len);
        
        int u8 = ((pppEth.version() << VERSION_BIT_SHIFT) & VERSION_MASK)
                    | (pppEth.type() & TYPE_MASK);
        w.writeU8(u8);
        w.writeU8(pppEth.code().code());
        w.writeU16(pppEth.sessionId());
        w.writeU16(len + ep.len());
        
        if (pppEth.code() == Code.SESSION_DATA)
            w.writeU16(pppEth.pppProtocolId().code());
        
        w.writeBytes(pppEth.bytesArray());
        
        return w;
    }

}
