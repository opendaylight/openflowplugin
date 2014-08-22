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
import java.util.zip.Adler32;

import org.opendaylight.util.packet.Sctp.CheckSumType;
import org.opendaylight.util.packet.Sctp.Chunk;
import org.opendaylight.util.packet.Sctp.Chunk.Type;


/**
 * SCTP encoder and decoder.
 *
 * @author Frank Wood
 */
class SctpCodec {

    static final int FIXED_CHUNK_HDR_LEN = 4;
    static final int FIXED_HDR_LEN = 12;

    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the protocol bytes
     * @param len number of bytes in SCTP protocol
     * @return the new SCTP protocol
     */
    static Sctp decode(PacketReader r, int len) {
        Sctp.Builder b = new Sctp.Builder();

        try {
            b.srcPort(r.readU16());
            b.dstPort(r.readU16());
            b.verifyTag(r.readU32());
            b.checkSum(r.readU32());

            int bytesleft = len - FIXED_HDR_LEN;
            
            List<Chunk> chunks = new ArrayList<Chunk>();
            while (bytesleft > 0) {
                Type chunkType = Type.get(r.readU8());
                int chunkFlags = r.readU8();
                int chunkLen = r.readU16();
                
                byte[] chunkData = r.readBytes(chunkLen - FIXED_CHUNK_HDR_LEN);
                
                chunks.add(new Chunk(chunkType, chunkFlags, chunkData));

                bytesleft -= FIXED_CHUNK_HDR_LEN + chunkData.length;
            }
            b.chunks(chunks.toArray(new Chunk[chunks.size()]));
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build(); 
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param sctp protocol instance
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Sctp sctp) {
        
        int len = FIXED_HDR_LEN;
        
        for (Chunk c: sctp.chunksArray())
            len += FIXED_CHUNK_HDR_LEN + c.dataArray().length;
        
        PacketWriter w = new PacketWriter(len);
        
        w.writeU16(sctp.srcPort());
        w.writeU16(sctp.dstPort());
        w.writeU32(sctp.verifyTag());
        
        int cswi = w.wi();
        w.writeU32(0); // checksum starts as 0x00000000

        for (Chunk c: sctp.chunksArray()) {
            w.writeU8(c.type().code());
            w.writeU8(c.flags());
            w.writeU16(FIXED_CHUNK_HDR_LEN + c.dataArray().length);
            w.writeBytes(c.dataArray());
        }
        
        if (sctp.checkSumType() == CheckSumType.CRC32C)
            w.setU32(cswi, ProtocolUtils.Crc32c.checkSumU32(w.array()));
        else {
            Adler32 a32 = new Adler32();
            a32.update(w.array());
            w.setU32(cswi, a32.getValue());
        }
        
        return w;
    }
    
}
