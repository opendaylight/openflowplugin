/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.EthernetType;


/**
 * GRE encoder and decoder.
 *
 * @author Frank Wood
 */
class GreCodec {
    
    private static final int MAX_LEN = 16;

    private static final int VERSION_MASK   = 0x00007;
    private static final int CHECKSUM_MASK  = 0x08000;
    private static final int KEY_MASK       = 0x02000;
    private static final int SEQ_NUM_MASK   = 0x01000;
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the frame bytes
     * @return the new GRE protocol
     * @throws ProtocolException if there is an error parsing from reader
     */
    static Gre decode(PacketReader r) {
        Gre.Builder b = new Gre.Builder();

        try {
            int u16 = r.readU16();
            b.version(u16 & VERSION_MASK);
            b.protoType(EthernetType.valueOf(r.readU16()));
            
            if (0 != (u16 & CHECKSUM_MASK)) {
                b.checkSum(r.readU16());
                r.readU16(); // skip reserved field
            }
            
            if (0 != (u16 & KEY_MASK))
                b.key(r.readU32());
            
            if (0 != (u16 & SEQ_NUM_MASK))
                b.seqNum(r.readU32());
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build();
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param gre protocol instance
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Gre gre) {
        PacketWriter w = new PacketWriter(MAX_LEN);
        
        int u16 = VERSION_MASK & gre.version();
        u16 |= (gre.checkSum() > 0) ? CHECKSUM_MASK : 0;
        u16 |= (gre.key() != Gre.NONE) ? KEY_MASK : 0;
        u16 |= (gre.seqNum() != Gre.NONE) ? SEQ_NUM_MASK : 0;
        
        w.writeU16(u16);
        w.writeU16(gre.protoType().getNumber());
        
        if (gre.checkSum() > 0) {
            w.writeU16(gre.checkSum());
            w.writeU16(0); // reserved field
        }

        if (gre.key() != Gre.NONE)
            w.writeU32(gre.key());

        if (gre.seqNum() != Gre.NONE)
            w.writeU32(gre.key());
        
        return w;
    }

}
