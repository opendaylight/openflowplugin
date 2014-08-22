/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.net.IpAddress.IP_V4_ADDR_SIZE;
import static org.opendaylight.util.net.MacAddress.MAC_ADDR_SIZE;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.TcpUdpPort;
import org.opendaylight.util.packet.Dhcp.Flag;
import org.opendaylight.util.packet.Dhcp.OpCode;


/**
 * DHCP encoder and decoder.
 *
 * @author Frank Wood
 */
class DhcpCodec {

    private static final int MAX_LEN = 1500;
    private static final int MIN_LEN = 300;
    
    private static final int CLIENT_HW_ADDR_PAD = 10;
    private static final int SVR_HOST_NAME_SIZE = 64;
    private static final int BOOT_FILE_NAME_SIZE = 128;
    
    static final TcpUdpPort BOOTPC = TcpUdpPort.udpPort(68);
    static final TcpUdpPort BOOTPS = TcpUdpPort.udpPort(67);
    
    static final long MAGIC_COOKIE = 0x63825363;
    
    /**
     * Returns true if the passed in source and destination ports signify that
     * the following payload can be decoded by this DHCP decoder.
     * 
     * @param srcPort source port (i.e. UDP source port)
     * @param dstPort destination port (i.e. UDP destination port)
     * @return true if the ports signify a DHCP payload
     */
    static boolean isDhcp(TcpUdpPort srcPort, TcpUdpPort dstPort) {
        return (srcPort == BOOTPC || srcPort == BOOTPS) &&
               (dstPort == BOOTPC || dstPort == BOOTPS);        
    }
    

    private static final String E_BAD_COOKIE =
            "Bad DHCP cookie value";
    
    private static final String E_UNS_HW_ADDR_LEN =
            "Unsupported H/W address len: ";
    
    /**
     * Decodes the protocol.
     * 
     * @param r packet reader containing the protocol bytes
     * @return the new DHCP protocol
     */
    static Dhcp decode(PacketReader r) {
        Dhcp.Builder b = new Dhcp.Builder();

        try {
            
            b.opCode(OpCode.get(r.readU8()));
            b.hwType(HardwareType.get(r.readU8()));
            
            int hwAddrLen = r.readU8();
            if (hwAddrLen != MAC_ADDR_SIZE) {
                throw new ProtocolException(E_UNS_HW_ADDR_LEN + hwAddrLen,
                                            b.buildNoVerify(), r);
            }

            b.hopCount(r.readU8());
            b.transId(r.readU32());
            b.numSecs(r.readU16());
            b.flag(Flag.get(r.readU16()));
            b.clientAddr(r.readIPv4Address());
            b.yourAddr(r.readIPv4Address());
            b.serverAddr(r.readIPv4Address());
            b.gatewayAddr(r.readIPv4Address());
            b.clientHwAddr(r.readMacAddress());
            r.skip(CLIENT_HW_ADDR_PAD);
            b.serverHostName(r.readString(SVR_HOST_NAME_SIZE));
            b.bootFileName(r.readString(BOOT_FILE_NAME_SIZE));
            
            long cookie = r.readU32();
            if (cookie != MAGIC_COOKIE) {
                throw new ProtocolException(E_BAD_COOKIE + cookie,
                                            b.buildNoVerify(), r);
            }
            
            List<DhcpOption> options = new ArrayList<DhcpOption>();
            DhcpOption option = null;
            do {
                option = decodeOption(r);
                if (option.code() != DhcpOption.Code.END)
                    options.add(option);
                
            } while (DhcpOption.Code.END != option.code());
            
            b.options(options.toArray(new DhcpOption[options.size()]));
            
        } catch (Exception e) {
            throw new ProtocolException(e, b.buildNoVerify(), r);
        }

        return b.build(); 
    }

    private static IpAddress[] decodeIpsOption(PacketReader r, int len) {
        if (len % IP_V4_ADDR_SIZE == 0) {
            int n = len / IP_V4_ADDR_SIZE;
            IpAddress[] ips = new IpAddress[n];
            for (int i=0; i<n; i++)
                ips[i] = r.readIPv4Address();
            return ips;
        }
        return null;
    }
    
    private static DhcpOption decodeOption(PacketReader r) {
        DhcpOption.Code oc = DhcpOption.Code.get(r.readU8());
        
        if (DhcpOption.Code.END == oc)
            return DhcpOption.END_OPTION;
        
        if (DhcpOption.Code.PAD == oc)
            return DhcpOption.PAD_OPTION;
        
        int len = r.readU8();
        IpAddress[] ips = null;

        switch (oc) {
        
            // Read the option as the message type.
            case MSG_TYPE:
                return new DhcpOption(DhcpOption.MessageType.get(r.readU8()));

            // Read the option as an IPv4 address.
            case SUBNET_MASK:
            case ADDR_REQ:
            case SERVER_ID:
                return (len == IP_V4_ADDR_SIZE) ?
                        new DhcpOption(oc, r.readIPv4Address()) :
                        new DhcpOption(oc, r.readBytes(len));

            // Read the option as a list of IPv4 addresses.
            case ROUTER:
            case DOMAIN_SERVER:
                ips = decodeIpsOption(r, len);
                return (null != ips) ?
                        new DhcpOption(oc, ips) :
                        new DhcpOption(oc, r.readBytes(len));
                
            // Read the option as a string.
            case DOMAIN_NAME:
            case TFTP_SERVER_NAME:
            case VENDOR_CLASS_ID:
            case HOST_NAME:
                return new DhcpOption(oc, r.readString(len));

            // Read the option as a MAC address or string.
            case CLIENT_ID:
                HardwareType t = HardwareType.get(r.readU8());
                return (t == HardwareType.ETHERNET) ?
                        new DhcpOption(oc, r.readMacAddress()) :
                        new DhcpOption(oc, r.readString(len - 1));

            // Read the option as a list of codes.
            case PARAM_REQ:
                DhcpOption.Code[] rps = new DhcpOption.Code[len];
                for (int i=0; i<len; i++)
                    rps[i] = DhcpOption.Code.get(r.readU8());
                return new DhcpOption(oc, rps);
                
            // Read the option as an U32.
            case ADDR_LEASE_TIME:
                return new DhcpOption(oc, r.readU32());
                
            // Read the option as an U16.
            case MAX_MSG_SIZE:
                return new DhcpOption(oc, r.readU16());

            // Read the option as an array of bytes.
            default:
                return new DhcpOption(oc, r.readBytes(len));
        }
    }
    
    /**
     * Encodes the protocol.
     * 
     * @param dhcp protocol instance
     * @return the packet writer with the encoded bytes
     */
    static PacketWriter encode(Dhcp dhcp) {
        PacketWriter w = new PacketWriter(MAX_LEN);
        
        w.writeU8(dhcp.opCode().code());
        w.writeU8(dhcp.hwType().code());
        w.writeU8(MAC_ADDR_SIZE);
        w.writeU8(dhcp.hopCount());
        w.writeU32(dhcp.transId());
        w.writeU16(dhcp.numSecs());
        w.writeU16(dhcp.flag().code());
        w.write(dhcp.clientAddr());
        w.write(dhcp.yourAddr());
        w.write(dhcp.serverAddr());
        w.write(dhcp.gatewayAddr());
        w.write(dhcp.clientHwAddr());
        w.writeZeros(CLIENT_HW_ADDR_PAD);
        w.writeString(dhcp.serverHostName(), SVR_HOST_NAME_SIZE);
        w.writeString(dhcp.bootFileName(), BOOT_FILE_NAME_SIZE);
        w.writeU32(MAGIC_COOKIE);
        
        encodeOptions(dhcp, w);
        
        int padLen = MIN_LEN - w.wi();
        if (0 < padLen)
            w.writeZeros(padLen);
        
        return w;         
    }
    
    private static void encodeOptions(Dhcp dhcp, PacketWriter w) {
        
        for (DhcpOption o: dhcp.optionsArray()) {
            
            w.writeU8(o.code().code());
            
            switch (o.code()) {
            
                case MSG_TYPE:
                    w.writeU8(ProtocolUtils.U8_LEN);
                    w.writeU8(o.msgType().code());
                    break;
                    
                case SUBNET_MASK:
                case ADDR_REQ:
                case SERVER_ID:
                case ROUTER:
                case DOMAIN_SERVER:
                    if (null != o.ipAddrsArray()) {
                        w.writeU8(IP_V4_ADDR_SIZE * o.ipAddrsArray().length);
                        for (IpAddress ip: o.ipAddrsArray())
                            w.write(ip);
                    } else {
                        w.writeU8(o.bytesArray().length);
                        w.writeBytes(o.bytesArray());
                    }
                    break;
                    
                case DOMAIN_NAME:
                case TFTP_SERVER_NAME:
                case VENDOR_CLASS_ID:
                case HOST_NAME:
                    w.writeU8(o.name().length());
                    w.writeString(o.name());
                    break;
                    
                // Read the option as a CLientIdOption.
                case CLIENT_ID:
                    if (null != o.macAddr()) {
                        w.writeU8(ProtocolUtils.U8_LEN + MAC_ADDR_SIZE);
                        w.writeU8(HardwareType.ETHERNET.code());
                        w.write(o.macAddr());
                    } else {
                        w.writeU8(ProtocolUtils.U8_LEN + o.name().length());
                        w.writeU8(HardwareType.NONE.code());
                        w.writeString(o.name());
                    }
                    break;
        
                case PARAM_REQ:
                    w.writeU8(o.codesArray().length);
                    for (DhcpOption.Code oc: o.codesArray())
                        w.writeU8(oc.code());
                    break;
                    
                case ADDR_LEASE_TIME:
                    w.writeU8(ProtocolUtils.U32_LEN);
                    w.writeU32(o.number());
                    break;
                    
                    
                case MAX_MSG_SIZE:
                    w.writeU8(ProtocolUtils.U16_LEN);
                    w.writeU16((int)o.number());
                    break;                    
        
                default:
                    if (null != o.bytesArray()) {
                        w.writeU8(o.bytesArray().length);
                        w.writeBytes(o.bytesArray());
                    }
                    break;
            }           
        }

        w.writeU8(DhcpOption.Code.END.code());
    }    
    
}
