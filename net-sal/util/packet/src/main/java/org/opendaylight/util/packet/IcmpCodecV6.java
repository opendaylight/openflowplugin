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

import org.opendaylight.util.net.IpAddress;


/**
 * ICMPv6 encoder and decoder.
 *
 * @author Frank Wood
 */
class IcmpCodecV6 {

    private static final int HDR_LEN = 4;
    private static final int OPTION_HDR_LEN = 2;
    private static final int OPTION_MTU_PAYLOAD_LEN = 6;
    private static final int OPTION_LEN_MULTIPLE = 8;
    
    private static final int NEIGHBOR_SOLICIT_HDR_LEN = 20;
    private static final int NEIGHBOR_ADVERTISE_HDR_LEN = 20;
    private static final int ROUTER_ADVERTISE_HDR_LEN = 12;
    
    private static final int ROUTER_MASK    = 0x080;
    private static final int SOLICIT_MASK   = 0x040;
    private static final int OVERRIDE_MASK  = 0x020;
    
    private static final int MANAGED_ADDR_CFG_MASK  = 0x080;
    private static final int OTHER_CFG_MASK         = 0x040;

    /**
     * Decodes the protocol for ICMPv6.
     *
     * @param r packet reader containing the frame bytes
     * @param len number of bytes for ICMPv6 payload as specified by IP
     * @return the new ICMPv6 protocol
     */
    static IcmpV6 decode(PacketReader r, int len) {
        IcmpV6.Builder b = new IcmpV6.Builder();

        try {
            IcmpTypeCodeV6 tc = IcmpTypeCodeV6.get(r.readU16());
            b.typeCode(tc);
            b.checkSum(r.readU16());
        
            int payloadLen = len - HDR_LEN;
            IpAddress tgtAddr;
            int flags;
            
            switch (tc) {
                case NEIGHBOR_SOLICIT_NDP:
                    r.readU32(); // skip reserved bytes
                    tgtAddr = r.readIPv6Address();
                    payloadLen -= NEIGHBOR_SOLICIT_HDR_LEN;
                    b.neighborSolicitData(new IcmpV6.NeighborSolicitData(tgtAddr));
                    b.options(decodeOptions(r, payloadLen));
                    break;

                case NEIGHBOR_ADVERTISE_NDP:
                    flags = r.readU8();
                    r.readU24(); // skip reserved bytes
                    tgtAddr = r.readIPv6Address();
                    payloadLen -= NEIGHBOR_ADVERTISE_HDR_LEN;
                    b.neighborAdvertiseData(new IcmpV6.NeighborAdvertiseData(
                        0 != (flags & ROUTER_MASK),
                        0 != (flags & SOLICIT_MASK),
                        0 != (flags & OVERRIDE_MASK),
                        tgtAddr
                    ));
                    b.options(decodeOptions(r, payloadLen));
                    break;
                    
                case ROUTER_ADVERTISE_NDP:
                    int hopLimit = r.readU8();
                    flags = r.readU8();
                    int routerLifetime = r.readU16();
                    long reachTime = r.readU32();
                    long retransTimer = r.readU32();
                    payloadLen -= ROUTER_ADVERTISE_HDR_LEN;
                    b.routerAdvertiseData(new IcmpV6.RouterAdvertiseData(
                        hopLimit,
                        0 != (flags & MANAGED_ADDR_CFG_MASK),
                        0 != (flags & OTHER_CFG_MASK),
                        routerLifetime,
                        reachTime,
                        retransTimer
                    ));
                    b.options(decodeOptions(r, payloadLen));
                    break;
                    
                default:
                    b.bytes(r.readBytes(payloadLen));
                    break;
            }
            
        } catch (Exception  e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build();
    }

    private static IcmpOptionV6[] decodeOptions(PacketReader r, int len) {
        if (0 == len)
            return IcmpV6.NO_OPTIONS;

        int payloadLen = len;
        List<IcmpOptionV6> options = new ArrayList<IcmpOptionV6>();
        
        while (payloadLen > 0) {
            IcmpOptionV6.Type t = IcmpOptionV6.Type.get(r.readU8());
            int optLen = (r.readU8() * OPTION_LEN_MULTIPLE) - OPTION_HDR_LEN;
            
            payloadLen -= OPTION_HDR_LEN;
            
            switch (t) {
                case SRC_LL_ADDR:
                case TARGET_LL_ADDR:
                    if (optLen == MAC_ADDR_SIZE)
                        options.add(new IcmpOptionV6(t, r.readMacAddress()));
                    else
                        options.add(new IcmpOptionV6(t, r.readBytes(optLen)));
                    break;
                
                case MTU:
                    r.readU16(); // bypass reserved
                    options.add(new IcmpOptionV6(t, r.readU32()));
                    break;
                    
                case PREFIX_INFO:
                case REDIRECT_HDR:
                default:
                    options.add(new IcmpOptionV6(t, r.readBytes(optLen)));
                    break;
            }
            
            payloadLen -= optLen;
        }
        
        return options.toArray(new IcmpOptionV6[options.size()]);
    }
    
    /**
     * Used to determine the encode length of the options.
     * 
     * @param opts ICMPv6 options
     * @return the options encode length
     */
    private static int optionsLen(IcmpOptionV6[] opts) {
        int optionsLen = 0;
        for (IcmpOptionV6 option: opts) {
            int len = OPTION_HDR_LEN;
            switch (option.type()) {
                case SRC_LL_ADDR:
                case TARGET_LL_ADDR:
                    len += MAC_ADDR_SIZE;
                    break;
                    
                case MTU:
                    len += OPTION_MTU_PAYLOAD_LEN;
                    break;
                    
                case PREFIX_INFO:
                case REDIRECT_HDR:
                default: 
                    len += option.bytesArray().length;
                    break;
            }
            optionsLen += len;
        }
        return optionsLen;
    }
    
    private static void encodeOptions(PacketWriter w, IcmpOptionV6[] opts) {
        for (IcmpOptionV6 option: opts) {
            
            w.writeU8(option.type().code());
            
            switch (option.type()) {
                case SRC_LL_ADDR:
                case TARGET_LL_ADDR:
                    w.writeU8((OPTION_HDR_LEN + MAC_ADDR_SIZE)
                                  / OPTION_LEN_MULTIPLE);
                    w.write(option.linkLayerAddr());
                    break;
                    
                case MTU:
                    w.writeU8((OPTION_HDR_LEN + OPTION_MTU_PAYLOAD_LEN)
                                / OPTION_LEN_MULTIPLE);
                    w.writeU16(0); // reserved
                    w.writeU32(option.mtu());
                    break;
                    
                case PREFIX_INFO:
                case REDIRECT_HDR:
                default: 
                    w.writeU8((OPTION_HDR_LEN + option.bytesArray().length)
                                / OPTION_LEN_MULTIPLE);
                    w.writeBytes(option.bytesArray());
                    break;
            }
        }
    }    
    
    /**
     * Encodes the protocol for ICMPv6.
     *
     * @param icmp protocol instance
     * @param ipPseudoHdr IP pseudo header needed for checksum 
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(IcmpV6 icmp, IpPseudoHdr ipPseudoHdr) {
        
        IcmpTypeCodeV6 tc = icmp.typeCode();
        
        int len = HDR_LEN;
        
        switch (tc) {
            case NEIGHBOR_SOLICIT_NDP:
                len += NEIGHBOR_SOLICIT_HDR_LEN;
                len += optionsLen(icmp.optionsArray());
                break;

            case NEIGHBOR_ADVERTISE_NDP:
                len += NEIGHBOR_ADVERTISE_HDR_LEN;
                len += optionsLen(icmp.optionsArray());
                break;

            case ROUTER_ADVERTISE_NDP:
                len += ROUTER_ADVERTISE_HDR_LEN;
                len += optionsLen(icmp.optionsArray());
                break;
                
            default:
                len += icmp.bytesArray().length;
                break;
        }
        
        PacketWriter w = new PacketWriter(len);

        w.writeU16(icmp.typeCode().code());

        int cswi = w.wi();
        w.writeU16(0); // checksum starts as 0x0000

        int flags;
        
        switch (tc) {
            case NEIGHBOR_SOLICIT_NDP:
                w.writeU32(0); // skip reserved bytes
                w.write(icmp.neighborSolicitData().targetAddr());
                encodeOptions(w, icmp.optionsArray());
                break;

            case NEIGHBOR_ADVERTISE_NDP:
                IcmpV6.NeighborAdvertiseData na = icmp.neighborAdvertiseData();
                flags = (na.isSenderRouter() ? ROUTER_MASK : 0)
                        | (na.isSolicitResponse() ? SOLICIT_MASK : 0)
                        | (na.override() ? OVERRIDE_MASK : 0);
                w.writeU8(flags);
                w.writeU24(0); // skip reserved bytes
                w.write(na.targetAddr());
                encodeOptions(w, icmp.optionsArray());
                break;
                
            case ROUTER_ADVERTISE_NDP:
                IcmpV6.RouterAdvertiseData ra = icmp.routerAdvertiseData();
                w.writeU8(ra.hopLimit());
                flags = (ra.managedAddrConfig() ? MANAGED_ADDR_CFG_MASK : 0)
                        | (ra.otherConfig() ? OTHER_CFG_MASK : 0);
                w.writeU8(flags);
                w.writeU16(ra.routerLifetime());
                w.writeU32(ra.reachableTime());
                w.writeU32(ra.retransTimer());
                encodeOptions(w, icmp.optionsArray());
                break;                
                
            default:
                w.writeBytes(icmp.bytesArray());
                break;
        }
        
        w.setU16(cswi, ProtocolUtils.checkSumU16(ipPseudoHdr.len(len), w));
        
        return w;
    }

}
