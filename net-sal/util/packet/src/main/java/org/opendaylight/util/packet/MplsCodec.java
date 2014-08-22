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
import org.opendaylight.util.packet.Mpls.Header;


/**
 * MPLS encoder and decoder.
 *
 * @author Frank Wood
 */
class MplsCodec {

    private static final int HDR_LEN = 4;

    private static final long LABEL_MASK = 0x0fffff000;
    private static final long LABEL_BIT_SHIFT = 12;
    
    private static final long BOS_MASK = 0x000000100;

    private static final long TTL_MASK = 0x0000000ff;
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the frame bytes
     * @return the new MPLS protocol
     * @throws ProtocolException if there is an error parsing from reader
     */
    static Mpls decode(PacketReader r) {
        Mpls.Builder b = new Mpls.Builder();

        List<Header> headers = new ArrayList<Header>();
        
        try {
            boolean bos = false;
            while (!bos) {
                long u32 = r.readU32();
                
                int label = (int) ((LABEL_MASK & u32) >> LABEL_BIT_SHIFT);
                bos = 0 != (BOS_MASK & u32);
                int ttl = (int) (TTL_MASK & u32);
                
                headers.add(new Header(label, ttl));
            }
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        b.headers(headers.toArray(new Header[headers.size()]));
        
        return b.build();
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param mpls protocol instance
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Mpls mpls) {
        int n = mpls.headersArray().length;
        
        PacketWriter w = new PacketWriter(HDR_LEN * n);
        
        if (n > 0) {
            int lastIdx = n - 1;
            for (int i=0; i<n; i++) {
                Header h = mpls.headersArray()[i];
                
                long u32 = h.label() << LABEL_BIT_SHIFT;
                u32 |= (lastIdx == i) ? BOS_MASK : 0;
                u32 |= TTL_MASK & h.ttl();
                
                w.writeU32(u32);
            }
        }
            
        return w;
    }

}
