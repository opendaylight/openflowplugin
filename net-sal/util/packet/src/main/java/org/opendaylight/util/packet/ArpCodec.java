/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.net.IpAddress.IP_V4_ADDR_SIZE;
import static org.opendaylight.util.net.IpAddress.IP_V6_ADDR_SIZE;
import static org.opendaylight.util.net.MacAddress.MAC_ADDR_SIZE;

import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.packet.Arp.OpCode;


/**
 * ARP encoder and decoder.
 *
 * @author Frank Wood
 */
class ArpCodec {

    private static final int MAX_LEN = 52;
    
    private static final String E_UNS_HW_ADDR_LEN =
            "Unsupported H/W address len: ";
    
    private static final String E_UNS_IP_ADDR_LEN =
            "Unsupported IP address len: ";

    private static final String E_UNS_PROTO_TYPE =
            "Unsupported Protocol type: ";
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the frame bytes
     * @return the new ARP protocol
     * @throws ProtocolException if there is an error parsing from reader
     */
    static Arp decode(PacketReader r) {
        Arp.Builder b = new Arp.Builder();

        try {
            b.hwType(HardwareType.get(r.readU16()));
            
            EthernetType ethType = EthernetType.valueOf(r.readU16());
            if (ethType != EthernetType.IPv4) {
                throw new ProtocolException(E_UNS_PROTO_TYPE + ethType,
                                            b.buildNoVerify(), r);
            }
            
            int hwAddrLen = r.readU8();
            if (hwAddrLen != MAC_ADDR_SIZE) {
                throw new ProtocolException(E_UNS_HW_ADDR_LEN + hwAddrLen,
                                            b.buildNoVerify(), r);
            }
            
            int ipAddrLen = r.readU8();
            if (ipAddrLen != IP_V4_ADDR_SIZE && ipAddrLen != IP_V6_ADDR_SIZE) {
                throw new ProtocolException(E_UNS_IP_ADDR_LEN + ipAddrLen,
                                            b.buildNoVerify(), r);
            }
            
            b.opCode(OpCode.get(r.readU16()));
            b.senderMacAddr(r.readMacAddress());
            b.senderIpAddr(r.readIPv4Address());
            b.targetMacAddr(r.readMacAddress());
            b.targetIpAddr(r.readIPv4Address());
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build();
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param arp protocol instance
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Arp arp) {
        PacketWriter w = new PacketWriter(MAX_LEN);
        
        w.writeU16(arp.hwType().code());
        w.writeU16(EthernetType.IPv4.getNumber());
        w.writeU8(MAC_ADDR_SIZE);
        w.writeU8(IP_V4_ADDR_SIZE);
        w.writeU16(arp.opCode().code());
        w.write(arp.senderMacAddr());
        w.write(arp.senderIpAddr());
        w.write(arp.targetMacAddr());
        w.write(arp.targetIpAddr());
        
        return w;
    }

}
