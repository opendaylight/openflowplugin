/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.packet.ProtocolUtils.hex;

import java.util.Arrays;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;


/**
 * DHCP option data store (immutable).
 *
 * @author Frank Wood
 */
public class DhcpOption {

    /** Message types. */
    public enum MessageType implements ProtocolEnum {
        
        /** Discover message. */
        DISCOVER(1),
        /** Offer message. */
        OFFER(2),
        /** Request message. */
        REQ(3),
        /** Decline message. */
        DECLINE(4),
        /** Acknowledge message. */
        ACK(5),
        /** Negative Acknowledgment message. */
        NAK(6),
        /** Release message. */
        RELEASE(7),
        /** Inform message. */
        INFORM(8),
        /** Force Renew message. */
        FORCE_RENEW(9),
        /** Please Query message. */
        PLEASE_QUERY(10),
        /** Please Unassigned message. */
        PLEASE_UNASSIGNED(11),
        /** Please Unknown message. */
        PLEASE_UNKNOWN(12),
        /** Please Active message. */
        PLEASE_ACTIVE(13),
        /** Bulk Lease Query message. */
        BULK_LEASE_QUERY(14),
        /** Please Query Done message. */
        PLEASE_QUERY_DONE(15),
        ;

        private int code;
        
        private MessageType(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static MessageType get(int code) {
            return ProtocolUtils.getEnum(MessageType.class, code, DISCOVER);
        }
    }    
    
    /**
     * Option codes (used in messages types, request params, ...).
     * http://www.ietf.org/assignments/bootp-dhcp-parameters/
     *      bootp-dhcp-parameters.txt
     * */
    public enum Code implements ProtocolEnum {
        /** Pad indicator option code. */
        PAD(0),
        /** Subnet mask option code. */ 
        SUBNET_MASK(1),
        /** Time offset option code. */
        TIME_OFFSET(2),
        /** Routers option code. */
        ROUTER(3),
        /** Time server option code. */
        TIME_SERVER(4),
        /** Time server option code. */
        NAME_SERVER(5),
        /** Domain server option code. */
        DOMAIN_SERVER(6),
        /** Log server option code. */
        LOG_SERVER(7),
        /** Quotes server option code. */
        QUOTES_SERVER(8),
        /** Printer server option code. */
        PRINTER_SERVER(9),
        /** Impress server option code. */
        IMPRESS_SERVER(10),
        /** RLP server option code. */
        RLP_SERVER(11),
        /** Host name option code. */
        HOST_NAME(12),
        /** Boot file size option code. */
        BOOT_FILE_SIZE(13),
        /** Merit dump file option code. */
        MERIT_DUMP_FILE(14),
        /** Domain name option code. */
        DOMAIN_NAME(15),
        /** Swap server address option code. */
        SWAP_SERVER(16),
        /** Root path name option code. */
        ROOT_PATH(17),
        /** Extension file option code. */
        EXTENSION_FILE(18),
        /** Forward on/off option code. */
        FORWARD_ON_OFF(19),
        /** Source routing on/off option code. */
        SOURCE_ROUTE_ON_OFF(20),
        /** Policy filter option code. */
        POLICY_FILTER(21),
        /** Max datagram re-assembly size option code. */
        MAX_DG_REASSEM_SIZE(22),
        /** Default Time to Live code. */
        DEFAULT_IP_TTL(23),
        /** Path MTU aging timeout code. */
        MTU_TIMEOUT(24),
        /** Path MTU plateau table code. */
        MTU_PLATEAU(25),
        /** Interface MTU size code. */
        MTU_INTERFACE(26),
        /** MTU subnet code. */
        MTU_SUBNET(27),
        /** Broadcast address option code. */ 
        BROADCAST_ADDR(28),
        /** Mask discovery option code. */
        MASK_DISCO(29),
        /** Mask Supplier option code. */
        MASK_SUPPLIER(30),
        /** Router discovery option code. */
        ROUTER_DISCO(31),
        /** Router request option code. */
        ROUTER_REQ(32),
        /** Static route option code. */
        STATIC_ROUTE(33),
        /** Trailers option code. */
        TRAILERS(34),
        /** ARP timeout option code. */
        ARP_TIMEOUT(35),
        /** Ethernet option code. */
        ETHERNET(36),
        /** Default TCP Time to Live option code. */
        DEFAULT_TCP_TTL(37),
        /** Keep alive time option code. */
        KEEPALIVE_TIME(38),
        /** Keep alive data option code. */
        KEEPALIVE_DATA(39),
        /** NIS domain option code. */
        NIS_DOMAIN(40),
        /** NIS servers option code. */
        NIS_SERVERS(41),
        /** NTP server addresses option code. */
        NTP_SERVERS(42),
        /** Vendor-Specific information option code. */
        VENDOR_SPECIFIC(43),
        /** NetBIOS name server option code. */
        NETBIOS_NAME_SVR(44),
        /** NetBIOS datagram distribution option code. */
        NETBIOS_DIST_SVR(45),
        /** NetBIOS node type option code. */
        NETBIOS_NODE_TYPE(46),
        /** NetBIOS scope option code. */
        NETBIOS_SCOPE(47),
        /** X-Window font option code. */
        X_WIN_FONT(48),
        /** X-Window manager option code. */
        X_WIN_MANAGER(49),
        /** Address requested option code. */
        ADDR_REQ(50),
        /** Address least time option code. */
        ADDR_LEASE_TIME(51),
        /** Overload option code. */
        OVERLOAD(52),
        /** Message type option code. */
        MSG_TYPE(53),
        /** DHCP server ID option code. */
        SERVER_ID(54),
        /** Parameter request option code. */
        PARAM_REQ(55),
        /** DHCP error message option code. */
        MSG_ERR(56),
        /** DHCP max message size option code. */
        MAX_MSG_SIZE(57),
        /** Class ID option code. */
        VENDOR_CLASS_ID(60),
        /** Client ID option code. */
        CLIENT_ID(61),
        /** TFTP server option code. */
        TFTP_SERVER_NAME(66),
        /** Agent information option code. */
        AGENT_INFO(82),
        /** Authentication option code. */
        AUTH(90),
        /** Domain search option code. */
        DOMAIN_SERACH(119),
        /** SIP servers option code. */
        SIP_SERVERS(120),
        /** Classless static route option code. */
        CLASSLESS_STATIC_ROUTE(121),
        /** Reserved (used as unknown) option code. */
        RESERVED(254),
        /** End indicator option code. */
        END(255),
        ;
        
        private int code;
        
        private Code(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static Code get(int code) {
            return ProtocolUtils.getEnum(Code.class, code, RESERVED);
        }
    }
    
    /** Singleton of the {@link Code#END}. */
    public static final DhcpOption END_OPTION = new DhcpOption(Code.END);
    
    /** Singleton of the {@link Code#PAD}. */
    public static final DhcpOption PAD_OPTION = new DhcpOption(Code.PAD);
    
    private Code code;
    private MessageType msgType;
    private byte[] bytes;
    private String name;
    private IpAddress[] ipAddrs;
    private MacAddress macAddr;
    private Code[] codes;
    private Long number;
        
    /**
     * Constructor for a no-payload option.
     * 
     * @param code option code
     */
    public DhcpOption(Code code) {
        this.code = code;
    }
    
    /**
     * Constructor for the message type option.
     * 
     * @param msgType message type
     */
    public DhcpOption(MessageType msgType) {
        this.code = Code.MSG_TYPE;
        this.msgType = msgType;
    }
    
    /**
     * Constructor for a byte array (opaque) option.
     * 
     * @param code option code
     * @param bytes payload byte array
     */
    public DhcpOption(Code code, byte[] bytes) {
        this.code = code;
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }
    
    /**
     * Constructor for a string option.
     * 
     * @param code option code
     * @param name string value
     */
    public DhcpOption(Code code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Constructor for a IP address option.
     * 
     * @param code option code
     * @param ipAddr IP address
     */
    public DhcpOption(Code code, IpAddress ipAddr) {
        this.code = code;
        this.ipAddrs = new IpAddress[] { ipAddr };
    }        
    
    /**
     * Constructor for an option holding a list of IP addresses.
     * 
     * @param code option code
     * @param ipAddrs array of IP addresses
     */
    public DhcpOption(Code code, IpAddress[] ipAddrs) {
        this.code = code;
        this.ipAddrs = Arrays.copyOf(ipAddrs, ipAddrs.length);
    }

    /**
     * Constructor for an option holding a MAC address.
     *  
     * @param code option code
     * @param macAddr MAC address
     */
    public DhcpOption(Code code, MacAddress macAddr) {
        this.code = code;
        this.macAddr = macAddr;
    }
    
    /**
     * Constructor for an option holding a list of {@link Code} values.
     * 
     * @param code option code
     * @param codes array of codes
     */
    public DhcpOption(Code code, Code[] codes) {
        this.code = code;
        this.codes = Arrays.copyOf(codes, codes.length);
    }

    /**
     * Constructor for a scalar (number) option.
     * 
     * @param code option code
     * @param number scalar value
     */
    public DhcpOption(Code code, long number) {
        this.code = code;
        this.number = number;
    }

    /**
     * Returns the option code.
     * 
     * @return the code
     */
    public Code code() {
        return code;
    }

    /**
     * Returns the message type or null.
     * 
     * @return the message type or null
     */
    MessageType msgType() {
        return msgType;
    }
    
    /**
     * Returns a copy of the byte array or null.
     *  
     * @return the byte array or null
     */
    public byte[] bytes() {
        if (null == bytes)
            return null;
        return Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Internally used by the package to get the byte array for this option or
     * null.
     * 
     * @return the byte array or null
     */
    byte[] bytesArray() {
        return bytes;
    }

    /**
     * Returns the MAC address or null.
     * 
     * @return the MAC address or null
     */
    public MacAddress macAddr() {
        return macAddr;
    }

    /**
     * Returns the number (scalar) value.
     * 
     * @return the number value
     */
    public long number() {
        return number;
    }
    
    /**
     * Returns the string value or null.
     * 
     * @return the string value
     */
    public String name() {
        return name;
    }
    
    /**
     * Returns a copy of the list of codes or null.
     * 
     * @return array of codes or null
     */
    public Code[] codes() {
        if (null == codes)
            return null;
        return Arrays.copyOf(codes, codes.length);
    }

    /**
     * Internally used by the package to return a list of codes or null.
     * 
     * @return array of codes or null
     */
    Code[] codesArray() {
        return codes;
    }
    
    /**
     * Returns a copy of the list of IP addresses or null.
     * 
     * @return array of IP addresses or null
     */
    public IpAddress[] ipAddrs() {
        if (null == ipAddrs)
            return null;
        return Arrays.copyOf(ipAddrs, ipAddrs.length);
    }
    
    /**
     * Internally used by the package to return the array of IP addresses or
     * null.
     * 
     * @return array of IP addresses or null
     */
    IpAddress[] ipAddrsArray() {
        return ipAddrs;
    }
    
    /**
     * Returns the IP address for this option or null.
     * 
     * @return the IP address or null
     */
    public IpAddress ipAddr() {
        return (null == ipAddrs || ipAddrs.length == 0) ? null : ipAddrs[0];
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(code).append(",");
        if (null != bytes)
            sb.append(ProtocolUtils.hex(bytes));
        else if (null != name)
            sb.append(name);
        else if (null != ipAddrs)
            sb.append(Arrays.asList(ipAddrs));
        else if (null != macAddr)
            sb.append(macAddr);
        else if (null != codes)
            sb.append(Arrays.asList(codes));
        else if (null != number)
            sb.append(number);
        else if (null != msgType)
            sb.append(msgType);
        else
            sb.append("-");
        sb.append("]");
        return sb.toString();
    }
    
}
