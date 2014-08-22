/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;



/**
 * Unknown protocol Encoder/Decoder.
 *
 * @author Frank Wood
 */
class UnknownProtocolCodec {

    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the protocol bytes
     * @param len number of bytes to read
     * @return the new unknown protocol
     */
    static UnknownProtocol decode(PacketReader r, int len) {
        UnknownProtocol.Builder b = new UnknownProtocol.Builder();
        try {
            b.bytes(r.readBytes(len));
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build();         
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param p protocol instance
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(UnknownProtocol p) {
        byte[] bytes = p.bytesArray();
        PacketWriter w = new PacketWriter(bytes.length);
        w.writeBytes(bytes);
        return w;
    }
    
}
