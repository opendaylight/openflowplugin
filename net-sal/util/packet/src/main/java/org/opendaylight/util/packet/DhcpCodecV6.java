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

import org.opendaylight.util.net.TcpUdpPort;


/**
 * DHCPv6 encoder and decoder.
 *
 * @author Frank Wood
 */
class DhcpCodecV6 {

    static final TcpUdpPort CLIENT_PORT = TcpUdpPort.udpPort(546);
    static final TcpUdpPort SERVER_PORT = TcpUdpPort.udpPort(547);
    
    /**
     * Returns true if the passed in source and destination ports signify that
     * the following payload can be decoded by this DHCPv6 decoder.
     * 
     * @param srcPort source port (i.e. UDP source port)
     * @param dstPort destination port (i.e. UDP destination port)
     * @return true if the ports signify a DHCPv6 payload
     */
    static boolean isDhcpV6(TcpUdpPort srcPort, TcpUdpPort dstPort) {
        return (srcPort == CLIENT_PORT || srcPort == SERVER_PORT) &&
               (dstPort == CLIENT_PORT || dstPort == SERVER_PORT);        
    }

    private static final int FIXED_HDR_LEN = 4;
    private static final int OPTION_FIXED_HDR_LEN = 4;
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the protocol bytes
     * @param len number of bytes for DHCPv6 payload as specified by IP 
     * @return the new DHCPv6 protocol
     */
    static DhcpV6 decode(PacketReader r, int len) {
        DhcpV6.Builder b = new DhcpV6.Builder();

        try {
            
            b.msgType(DhcpV6.MessageType.get(r.readU8()));
            b.transId(r.readU24());
            b.options(decodeOptions(r, len - FIXED_HDR_LEN));
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build(); 
    }

    private static DhcpOptionV6[] decodeOptions(PacketReader r, int len) {
        if (0 == len)
            return DhcpV6.NO_OPTIONS;

        int payloadLen = len;
        List<DhcpOptionV6> options = new ArrayList<DhcpOptionV6>();
        
        while (payloadLen > 0) {
            DhcpOptionV6.Type t = DhcpOptionV6.Type.get(r.readU16());
            int optLen = r.readU16();
            byte[] optBytes = r.readBytes(optLen);
            
            options.add(new DhcpOptionV6(t, optBytes));
            
            payloadLen -= OPTION_FIXED_HDR_LEN + optLen;
        }
        
        return options.toArray(new DhcpOptionV6[options.size()]);
    }    
    
    /**
     * Encodes the protocol.
     * 
     * @param dhcpV6 protocol instance
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(DhcpV6 dhcpV6) {
        
        int optsLen = 0;
        for (DhcpOptionV6 opt: dhcpV6.options())
            optsLen += OPTION_FIXED_HDR_LEN + opt.bytesArray().length;
        
        PacketWriter w = new PacketWriter(FIXED_HDR_LEN + optsLen);
        
        w.writeU8(dhcpV6.msgType().code());
        w.writeU24(dhcpV6.transId());
        
        for (DhcpOptionV6 o: dhcpV6.optionsArray()) {
            w.writeU16(o.type().code());
            w.writeU16(o.bytesArray().length);
            w.writeBytes(o.bytesArray());
        }
        
        return w;         
    }
    
}
