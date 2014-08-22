/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.net.MacAddress.MAC_ADDR_SIZE;

import java.util.ArrayList;
import java.util.List;


/**
 * LLDP encoder and decoder.
 *
 * @author Frank Wood
 */
class LldpCodec {

    private static final int MAX_LEN = 500;
    private static final int TTL_LEN = 2;
    private static final int CAPS_LEN = 4;
    
    private static final int TLV_HDR_TYPE_MASK = 0x07f;
    private static final int TLV_HDR_TYPE_BIT_SHIFT = 9;
    private static final int TLV_HDR_LEN_MASK = 0x01ff;
    
    /**
     * Type-length structure.
     * <pre>
     * Type   Length  Value
     * 7 bits 9 bits  0-510 bytes
     * </pre>
     */
    private static class TlvHdr {
        public LldpTlv.Type type;
        public int len;
    }
    
    private static TlvHdr decodeTlvHdr(PacketReader r) {
        TlvHdr tlvHdr = new TlvHdr();
        int u16 = r.readU16();
        tlvHdr.type = LldpTlv.Type.get((u16 >> TLV_HDR_TYPE_BIT_SHIFT)
                                           & TLV_HDR_TYPE_MASK);
        tlvHdr.len = u16 & TLV_HDR_LEN_MASK;
        return tlvHdr;
    }

    private static void encodeTlvHdr(LldpTlv.Type t, int len, PacketWriter w) {
        w.writeU16( ((t.code() & TLV_HDR_TYPE_MASK) << TLV_HDR_TYPE_BIT_SHIFT)
                       | (TLV_HDR_LEN_MASK & len) );
    }
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the frame bytes
     * @return the new LLDP protocol
     */
    static Lldp decode(PacketReader r) {
        Lldp.Builder b = new Lldp.Builder();

        try {
            // Chassis ID.
            TlvHdr hdr = decodeTlvHdr(r);
            LldpTlv.ChassisIdSubType ciSubType = LldpTlv.ChassisIdSubType.get(r.readU8());
            
            LldpTlv.Builder tlvb = new LldpTlv.Builder()
                .type(hdr.type).chassisIdSubType(ciSubType);
            
            switch (ciSubType) {
                case MAC_ADDR:
                    tlvb.macAddr(r.readMacAddress());
                    break;
                case INTF_NAME:
                case LOCAL:
                case CHASSIS_COMP:
                case INTF_ALIAS:
                case PORT_COMP:
                    tlvb.name(r.readString(hdr.len - 1));
                    break;                    
                default:
                    tlvb.bytes(r.readBytes(hdr.len - 1));
                    break;
            }
            b.chassisId(tlvb.build());
    
            // Port ID.
            hdr = decodeTlvHdr(r);
            LldpTlv.PortIdSubType piSubType = LldpTlv.PortIdSubType.get(r.readU8());
            
            tlvb = new LldpTlv.Builder()
                .type(hdr.type).portIdSubType(piSubType);
            
            switch (piSubType) {
                case MAC_ADDR:
                    tlvb.macAddr(r.readMacAddress());
                    break;
                case INTF_NAME:
                case LOCAL:
                case AGENT_CIRC_ID:
                case INTF_ALIAS:
                case PORT_COMP:
                    tlvb.name(r.readString(hdr.len - 1));
                    break;
                default:
                    tlvb.bytes(r.readBytes(hdr.len - 1));
                    break;
            }
            b.portId(tlvb.build());
    
            // TTL.
            hdr = decodeTlvHdr(r);
            tlvb = new LldpTlv.Builder().type(hdr.type).number(r.readU16());
            b.ttl(tlvb.build());
            
            List<LldpTlv> options = new ArrayList<LldpTlv>();
            List<LldpTlv> privateOptions = new ArrayList<LldpTlv>();
            
            LldpTlv option = null;
            do {
                option = decodeOption(r);
                if (option.type() != LldpTlv.Type.END) {
                    if (option.type() != LldpTlv.Type.PRIVATE)
                        options.add(option);
                    else
                        privateOptions.add(option);
                }
                
            } while (LldpTlv.Type.END != option.type());
    
            b.options(options.toArray(new LldpTlv[options.size()]));
            b.privateOptions(privateOptions.toArray(
                                 new LldpTlv[privateOptions.size()]));
        
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build(); 
    }        
        
    private static LldpTlv decodeOption(PacketReader r) {
        
        TlvHdr hdr = decodeTlvHdr(r);
        LldpTlv.Builder b = new LldpTlv.Builder().type(hdr.type);
        
        switch (hdr.type) {
        
            // Read the option as the END marker.
            case END:
                return b.build();
                
            // Read the option as a string.
            case PORT_DESC:
            case SYS_NAME:
            case SYS_DESC:
                return b.name(r.readString(hdr.len)).build();
                
            // Read the option as capability bits.
            case CAPS:
                return b.supportedMask(r.readU16())
                        .enabledMask(r.readU16()).build();
            
            // Read the option as an array of bytes.
            default:
                return b.bytes(r.readBytes(hdr.len)).build();
        }
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param lldp protocol instance
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Lldp lldp) {
        PacketWriter w = new PacketWriter(MAX_LEN);
        
        byte[] bytes = null;
        String name = null;
        
        LldpTlv tlv = lldp.chassisId();
        LldpTlv.ChassisIdSubType ciSubType = tlv.chassisIdSubType();
        
        switch (ciSubType) {
            case MAC_ADDR:
                encodeTlvHdr(LldpTlv.Type.CHASSIS_ID, 1 + MAC_ADDR_SIZE, w);
                w.writeU8(ciSubType.code());
                w.write(tlv.macAddr());
                break;

            case INTF_NAME:
            case LOCAL:
            case CHASSIS_COMP:
            case INTF_ALIAS:
            case PORT_COMP:
                name = tlv.name();
                encodeTlvHdr(LldpTlv.Type.CHASSIS_ID, 1 + name.length(), w);
                w.writeU8(ciSubType.code());
                w.writeString(name);
                break;
                
            default:
                bytes = tlv.bytesArray();
                encodeTlvHdr(LldpTlv.Type.CHASSIS_ID, 1 + bytes.length, w);
                w.writeU8(ciSubType.code());
                w.writeBytes(bytes);
                break;
        }

        tlv = lldp.portId();
        LldpTlv.PortIdSubType piSubType = tlv.portIdSubType();
        
        switch (piSubType) {
            case MAC_ADDR:
                encodeTlvHdr(LldpTlv.Type.PORT_ID, 1 + MAC_ADDR_SIZE, w);
                w.writeU8(piSubType.code());
                w.write(tlv.macAddr());
                break;
        
            case INTF_NAME:
            case LOCAL:
            case AGENT_CIRC_ID:
            case INTF_ALIAS:
            case PORT_COMP:
                name = tlv.name();
                encodeTlvHdr(LldpTlv.Type.PORT_ID, 1 + name.length(), w);
                w.writeU8(piSubType.code());
                w.writeString(name);
                break;
                
            default:
                bytes = tlv.bytesArray();
                encodeTlvHdr(LldpTlv.Type.PORT_ID, 1 + bytes.length, w);
                w.writeU8(piSubType.code());
                w.writeBytes(bytes);
                break;
        }
        
        encodeTlvHdr(LldpTlv.Type.TTL, TTL_LEN, w);
        w.writeU16(lldp.ttl().number());

        encodeOptions(lldp, w);
        encodePrivateOptions(lldp, w);
        
        encodeTlvHdr(LldpTlv.Type.END, 0, w);
        
        return w;
    }
    
    private static void encodeOptions(Lldp lldp, PacketWriter w) {
        
        for (LldpTlv tlv: lldp.optionsArray()) {
            switch (tlv.type()) {
            
                case END:
                    encodeTlvHdr(LldpTlv.Type.END, 0, w);
                    break;
            
                case PORT_DESC:
                case SYS_NAME:
                case SYS_DESC:
                    encodeTlvHdr(tlv.type(), tlv.name().length(), w);
                    w.writeString(tlv.name());
                    break;
                    
                case CAPS:
                    encodeTlvHdr(tlv.type(), CAPS_LEN, w);
                    w.writeU16(tlv.supportedMask());
                    w.writeU16(tlv.enabledMask());
                    break;
                    
                default:
                    byte[] bytes = tlv.bytesArray();
                    encodeTlvHdr(tlv.type(), bytes.length, w);
                    w.writeBytes(bytes);
                    break;
            }
        }
    }
    
    private static void encodePrivateOptions(Lldp lldp, PacketWriter w) {
        for (LldpTlv tlv: lldp.privateOptionsArray()) {
            byte[] bytes = tlv.bytesArray();
            encodeTlvHdr(tlv.type(), bytes.length, w);
            w.writeBytes(bytes);
        }
    }    

}
