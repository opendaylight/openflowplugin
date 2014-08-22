/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.ByteUtils.getU8;
import static org.opendaylight.util.packet.Ethernet.NONE;
import static org.opendaylight.util.packet.Ethernet.SnapId;
import static org.opendaylight.util.packet.Ethernet.Control.UNNUMBERED;

import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.packet.Ethernet.Control;
import org.opendaylight.util.packet.Ethernet.Dsap;
import org.opendaylight.util.packet.Ethernet.Ssap;
import org.opendaylight.util.packet.Ethernet.VlanPriority;


/**
 * Ethernet encoder and decoder.
 * <pre>
 * Ethernet_II (Ethernet)
 *     6 bytes       6 bytes       2 bytes         Up to 1500 bytes
 * +-------------+-------------+-------------+-------------------------+
 * | Destination |   Source    |    E-type   | Network Protocol Packet |
 * | MAC Address | MAC Address | (IPX: 8137) |                         |
 * +-------------+-------------+-------------+-------------------------+
 * 
 * Ethernet_802.3 Raw (rare)
 *     6 bytes       6 bytes       2 bytes         Up to 1500 bytes
 * +-------------+-------------+--------------+------------------------+
 * | Destination |   Source    | Total packet |       IPX Packet       |
 * | MAC Address | MAC Address |    length    | first two bytes: FF,FF |
 * +-------------+-------------+--------------+------------------------+
 * No protocol ID: Can only carry IPX packets.
 * Distinguishable from Ethernet_802.2 only because the first two bytes of all
 * IPX packets carried on Ethernet_802.3 must be all ones, which makes no sense
 * in Ethernet_802.2.
 * 
 * Ethernet 802.3 LLC
 * 6 bytes 6 bytes 2 bytes 1 byte 1 byte  1 byte    Up to 1497 bytes
 * +------+------+--------+------+------+--------+---------------------+
 * | Dest | Src  | length | DSAP | SSAP | Control|   Network Packet    |
 * | Addr | Addr |        | (E0) | (E0) |  (03)  |                     |
 * +------+------+--------+------+------+--------+---------------------+
 * Used for OSI packets on 802.3 networks.
 * Numbers in parentheses are the values used by IPX.
 * 
 * Ethernet 802.3 SNAP
 *  6 b  6 b  2 b 1 byte 1 byte 1 byte     5 bytes     Up to 1492 bytes
 * +----+----+---+------+------+------+---------------+----------------+
 * |Dst |Src |len| DSAP | SSAP | Ctrl |    SNAP ID    | Network Packet |
 * |Addr|Addr|   | 0xAA | 0xAA | 0x03 | (0,0,0,81,37) |                |
 * +----+----+---+------+------+------+---------------+----------------+
 * </pre>
 * @author Frank Wood
 */
class EthernetCodec {

    // Buffer sized needed to fit all types of headers.
    private static final int MAX_HDR_LEN = 22;

    private static final int SNAP_ID_LEN = 5;
    
    private static final int VLAN_PRI_MASK = 0x0e000; // 1110 0000 0000 0000
    private static final int VLAN_PRI_BIT_SHIFT = 13;
    
    private static final int VLAN_DEI_MASK = 0x01000; // 0001 0000 0000 0000
    private static final int VLAN_ID_MASK  = 0x00fff; // 0000 1111 1111 1111
    
    
    // smallest valid ethernet2 type
    protected static final int ETH_TYPE_START = 0x0600;
    
    // specifies a VLAN ID header
    protected static final int ETH_TYPE_802_1Q = 0x8100;
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the frame bytes
     * @return the new Ethernet protocol
     * @throws ProtocolException if there is an error parsing from reader
     */
    static Ethernet decode(PacketReader r) {
        Ethernet.Builder b = new Ethernet.Builder();
        
        // Make sure we decode the ethernet type from the packet and not
        // use the default from the builder.
        b.type(NONE);

        try {
            b.dstAddr(r.readMacAddress());
            b.srcAddr(r.readMacAddress());
            
            int typeOrLen = r.readU16();
            
            if (typeOrLen >= ETH_TYPE_START) {
                // Ethernet2
                if (typeOrLen == ETH_TYPE_802_1Q) {
                    // 802.1Q - VLAN Field
                    int field = r.readU16();
                    int pri = (field & VLAN_PRI_MASK) >> VLAN_PRI_BIT_SHIFT;
                    b.vlanPriority(VlanPriority.get(pri));
                    b.vlanDei(0 != (field & VLAN_DEI_MASK));
                    b.vlanId(field & VLAN_ID_MASK);
                    b.type(EthernetType.valueOf(r.readU16()));
                } else {
                    b.type(EthernetType.valueOf(typeOrLen));
                }
            } else {
                // 802.3
                b.len(typeOrLen);
                
                Dsap dsap = Dsap.get(r.readU8());
                Ssap ssap = Ssap.get(r.readU8());
                Control ctrl = Control.get(r.readU8());
                
                b.dsap(dsap);
                b.ssap(ssap);
                b.control(ctrl);
                
                if (dsap == Dsap.SNAP && ssap == Ssap.SNAP
                        && ctrl == UNNUMBERED) {
                    // 802.3 SNAP
                    b.snapId(decodeSnapId(r));
                }
            }
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build(); 
    }
    
    private static SnapId decodeSnapId(PacketReader r) {
        byte[] enc = r.readBytes(SNAP_ID_LEN);
        int vendor = getU8(enc, 0) << ProtocolUtils.U16_BIT_SHIFT
                        | getU8(enc, 1) << ProtocolUtils.U8_BIT_SHIFT
                        | getU8(enc, 2);
        int local = getU8(enc, 3) << ProtocolUtils.U8_BIT_SHIFT | getU8(enc, 4);
        return new SnapId(vendor, local);
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param eth protocol instance
     * @param ep encoded payload
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Ethernet eth, EncodedPayload ep) {
        PacketWriter w = new PacketWriter(MAX_HDR_LEN);
        
        w.write(eth.dstAddr());
        w.write(eth.srcAddr());
        
        if (eth.type() == null || eth.type().getNumber() < ETH_TYPE_START) {
            // 802.3
            w.writeU16(ep.len());
            w.writeU8(eth.dsap().code());
            w.writeU8(eth.ssap().code());
            w.writeU8(eth.control().code());
            if (eth.dsap() == Dsap.SNAP && eth.ssap() == Ssap.SNAP
                    && eth.control() == Control.UNNUMBERED) {
                // 802.3 SNAP
                encodeSnapId(eth.snapId(), w);
            }
        } else {
            // Ethernet2
            if (0 < eth.vlanId()) {
                // 802.1Q - VLAN ID
                w.write(EthernetType.valueOf(ETH_TYPE_802_1Q));
                int field = eth.vlanId()
                        | (eth.vlanDei() ? VLAN_DEI_MASK : 0)
                        | VLAN_PRI_MASK &
                            (eth.vlanPriority().code() << VLAN_PRI_BIT_SHIFT);
                w.writeU16(field);
            }
            w.write(eth.type());
        }
        
        return w;
    }

    private static void encodeSnapId(SnapId snapId, PacketWriter w) {
        byte[] enc = new byte[SNAP_ID_LEN];
        long v = snapId.vendor();
        int l = snapId.local(); 
        enc[0] = (byte) (ProtocolUtils.U8_MASK & (v >> ProtocolUtils.U16_BIT_SHIFT));
        enc[1] = (byte) (ProtocolUtils.U8_MASK & (v >> ProtocolUtils.U8_BIT_SHIFT));
        enc[2] = (byte) (ProtocolUtils.U8_MASK & v);
        enc[3] = (byte) (ProtocolUtils.U8_MASK & (l >> ProtocolUtils.U8_BIT_SHIFT));
        enc[4] = (byte) (ProtocolUtils.U8_MASK & l);
        w.writeBytes(enc);;
    }
    
}
