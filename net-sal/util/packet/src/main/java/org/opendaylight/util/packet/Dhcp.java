/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.EMPTY;
import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.packet.Dhcp.Flag.UNICAST;
import static org.opendaylight.util.packet.Dhcp.OpCode.BOOT_REQ;

import java.util.Arrays;
import java.util.EnumMap;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;


/**
 * DHCP data store (immutable) add associated {@link Builder} (mutable).
 * <p>
 * There are no OpenFlow match fields that reference this protocol.
 *
 * @author Frank Wood
 */
public class Dhcp implements Protocol {

    /** Operation codes. */
    public enum OpCode implements ProtocolEnum {
        
        /** Boot request. */
        BOOT_REQ(1),
        /** Boot reply. */
        BOOT_REPLY(2),
        ;
        
        private int code;
        
        private OpCode(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static OpCode get(int code) {
            return ProtocolUtils.getEnum(OpCode.class, code, BOOT_REQ);
        }
    }    
    
    /** Flags (bitmask enumeration). */
    public enum Flag implements ProtocolEnum {
        
        /** Unicast flag. */
        UNICAST(0x00000),   // U16 flags: 0... .... .... ....
        /** Broadcast flag. */
        BROADCAST(0x08000), // U16 flags: 1... .... .... ....
        ;
        
        private int code;
        
        private Flag(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
        
        static int mask(Flag... flags) {
            return ProtocolUtils.getMask(Flag.class, flags);
        }
        
        static Flag get(int mask) {
            return ProtocolUtils.getEnum(Flag.class, mask, UNICAST);
        }

        static boolean has(int mask, Flag flag) {
            return 0 != (mask & flag.code());
        }
    }
    
    private static final DhcpOption[] NO_OPTIONS = new DhcpOption[0];

    /** Internal private data store. */
    private static class Data implements ProtocolData {

        private OpCode opCode = BOOT_REQ;
        private HardwareType hwType = HardwareType.ETHERNET;
        private int hopCount;
        private long transId;
        private int numSecs;
        private Flag flag = UNICAST;
        private IpAddress clientAddr;
        private IpAddress yourAddr;
        private IpAddress serverAddr;
        private IpAddress gatewayAddr;
        private MacAddress clientHwAddr;
        private String serverHostName = EMPTY;
        private String bootFileName = EMPTY;
        private DhcpOption[] options = NO_OPTIONS;
        
        private Data() {}
        
        private Data(Data data) {
            opCode = data.opCode;
            hwType = data.hwType;
            hopCount = data.hopCount;
            transId = data.transId;
            numSecs = data.numSecs;
            flag = data.flag;
            clientAddr = data.clientAddr;
            yourAddr = data.yourAddr;
            serverAddr = data.serverAddr;
            gatewayAddr = data.gatewayAddr;
            clientHwAddr = data.clientHwAddr;
            serverHostName = data.serverHostName;
            bootFileName = data.bootFileName;
            options = Arrays.copyOf(data.options, data.options.length);
        }
        
        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(opCode, hwType, flag, clientAddr, yourAddr,
                                        serverAddr, gatewayAddr, clientHwAddr, serverHostName,
                                        bootFileName, options);
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * opCode = BOOT_REQ
         * hwType = ETHERNET
         * flag = UNICAST
         * serverHostName = EMPTY
         * bootFileName = EMPTY
         * options = NO_OPTIONS
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param dhcp builder is initialed from this protocol's data
         */
        public Builder(Dhcp dhcp) {
            this.data = new Data(dhcp.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Dhcp build() {
            return new Dhcp(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Dhcp buildNoVerify() {
            return new Dhcp(data, false);
        }
        
        /**
         * Sets the operation code.
         * 
         * @param opCode operation code enumeration
         * @return this instance
         */
        public Builder opCode(OpCode opCode) {
            data.opCode = opCode;
            return this;
        }
        
        /**
         * Sets the hardware type.
         * 
         * @param hwType hardware type enumeration
         * @return this instance
         */
        public Builder hwType(HardwareType hwType) {
            data.hwType = hwType;
            return this;
        }

        
        /**
         * Sets the hop count.
         * 
         * @param hopCount hop count
         * @return this instance
         */
        public Builder hopCount(int hopCount) {
            data.hopCount = hopCount;
            return this;
        }

        /**
         * Sets the transaction ID which is a 32-bit identification field
         * generated by the client to allow it to match up requests with
         * replies.
         * 
         * @param transId transaction ID
         * @return this instance
         */
        public Builder transId(long transId) {
            data.transId = transId;
            return this;
        }
        
        /**
         * Sets the number of seconds elapsed since a client began an attempt
         * to acquire or renew a lease.
         * 
         * @param numSecs number of seconds
         * @return this instance
         */
        public Builder numSecs(int numSecs) {
            data.numSecs = numSecs;
            return this;
        }
        
        /**
         * Sets the {@link Flag#BROADCAST} or {@link Flag#UNICAST} (default)
         * type.
         * 
         * @param flag broadcast or unicast
         * @return this instance
         */
        public Builder flag(Flag flag) {
            data.flag = flag;
            return this;
        }
        
        /**
         * Sets the client IP address. The client puts its own current IP
         * address in this field if and only if it has a valid IP address.
         * 
         * @param clientAddr client IP address
         * @return this instance
         */
        public Builder clientAddr(IpAddress clientAddr) {
            data.clientAddr = clientAddr;
            return this;
        }
        
        /**
         * Sets the "Your" IP address which is the IP address that the server
         * is assigning to the client.
         * 
         * @param yourAddr "Your" IP address
         * @return this instance
         */
        public Builder yourAddr(IpAddress yourAddr) {
            data.yourAddr = yourAddr;
            return this;
        }
        
        /**
         * Sets the server IP address. This is the address of the server that
         * the client should use for the next step in the bootstrap process.
         * 
         * @param serverAddr server IP address
         * @return this instance
         */
        public Builder serverAddr(IpAddress serverAddr) {
            data.serverAddr = serverAddr;
            return this;
        }
        
        /**
         * Sets the gateway IP address. This address used to route messages
         * when relay agents are involved to facilitate communication between
         * subnets.
         * 
         * @param gatewayAddr gateway IP address
         * @return this instance
         */
        public Builder gatewayAddr(IpAddress gatewayAddr) {
            data.gatewayAddr = gatewayAddr;
            return this;
        }        
        
        /**
         * Sets the client layer-2 hardware address.
         * 
         * @param clientHwAddr client hardware address
         * @return this instance
         */
        public Builder clientHwAddr(MacAddress clientHwAddr) {
            data.clientHwAddr = clientHwAddr;
            return this;
        }        

        /**
         * Sets the server host name (nickname).
         * 
         * @param serverHostName server host name
         * @return this instance
         */
        public Builder serverHostName(String serverHostName) {
            data.serverHostName = serverHostName;
            return this;
        }        
        
        /**
         * Sets the boot file name.
         * 
         * @param bootFileName boot file name
         * @return this instance
         */
        public Builder bootFileName(String bootFileName) {
            data.bootFileName = bootFileName;
            return this;
        }        
        
        /**
         * Sets the options array.
         * 
         * @param options array of options
         * @return this instance
         */
        public Builder options(DhcpOption[] options) {
            data.options = options;
            return this;
        }        
    }
    
    private Data data;
    
    private Dhcp(Data data, boolean verify) {
        this.data = new Data(data);
        ProtocolUtils.verify(verify, this, this.data);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.DHCP;
    }

    /**
     * Returns the operation code.
     * 
     * @return the operation code
     */
    public OpCode opCode() {
        return data.opCode;
    }
    
    /**
     * Returns the hardware type.
     * 
     * @return the hardware type
     */
    public HardwareType hwType() {
        return data.hwType;
    }
    
    /**
     * Returns the hop count.
     * 
     * @return the hop count
     */
    public int hopCount() {
        return data.hopCount;
    }
    
    /**
     * Returns the transaction ID which is 32-bit identification field
     * generated by the client to allow it to match up requests with replies.
     * 
     * @return the transaction ID
     */
    public long transId() {
        return data.transId;
    }
    
    /**
     * Returns the number of seconds elapsed since a client begain an attempt
     * to acquire or renew a lease.
     * 
     * @return the number of seconds
     */
    public int numSecs() {
        return data.numSecs;
    }

    /**
     * Returns the {@link Flag#BROADCAST} or {@link Flag#UNICAST} type.
     * 
     * @return the broadcast or unicast type
     */
    public Flag flag() {
        return data.flag;
    }
    
    /**
     * Returns the client IP address. The client puts its own current IP
     * address in this field if and only if it has a valid IP address.
     * 
     * @return the client IP address
     */
    public IpAddress clientAddr() {
        return data.clientAddr;
    }
    
    /**
     * Returns "Your" IP address which is the IP address that the server
     * is assigning to the client.
     * 
     * @return "Your" IP address
     */

    public IpAddress yourAddr() {
        return data.yourAddr;
    }
    
    /**
     * Returns the server IP address. This is the address of the server that
     * the client should use for the next step in the bootstrap process.
     * 
     * @return the server IP address
     */

    public IpAddress serverAddr() {
        return data.serverAddr;
    }
    
    /**
     * Returns the gateway IP address. This address used to route messages
     * when relay agents are involved to facilitate communication between
     * subnets.
     * 
     * @return the gateway IP address
     */

    public IpAddress gatewayAddr() {
        return data.gatewayAddr;
    }
    
    /**
     * Returns the client layer-2 hardware address.
     * 
     * @return the client hardware address
     */
    public MacAddress clientHwAddr() {
        return data.clientHwAddr;
    }
    
    /**
     * Returns the server host name (nickname).
     * 
     * @return the server host name
     */
    public String serverHostName() {
        return data.serverHostName;
    }

    /**
     * Returns the boot file name.
     * 
     * @return the boot file name
     */
    public String bootFileName() {
        return data.bootFileName;
    }

    /**
     * Returns the {@link DhcpOption.MessageType} specified in the
     * {@link DhcpOption.Code#MSG_TYPE} option.
     * 
     * @return the message type or null if not found
     */
    public DhcpOption.MessageType msgType() {
        for (DhcpOption o: data.options) {
            if (o.code() == DhcpOption.Code.MSG_TYPE)
                return o.msgType();
        }
        return null;
    }
    
    /**
     * Internally used by the package to return the array of options.
     * 
     * @return the options array
     */
    DhcpOption[] optionsArray() {
        return data.options;
    }
    
    /**
     * Returns the options in the form of a map to allow access based on
     * {@link DhcpOption}.
     * 
     * @return the options in the form of a map
     */
    public EnumMap<DhcpOption.Code, DhcpOption> options() {
        EnumMap<DhcpOption.Code, DhcpOption> m =
            new EnumMap<DhcpOption.Code, DhcpOption>(DhcpOption.Code.class);
        
        for (DhcpOption o: data.options)
            m.put(o.code(), o);
        
        return m;
    }
    
    @Override
    public String toString() {
        return id() + "," + opCode() + ",msg=" + msgType() +
                ",cIp=" + clientAddr() + 
                ",sIp=" + serverAddr();
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("opCode: ").append(opCode())
            .append(eoli).append("hwType: ").append(hwType())
            .append(eoli).append("hopCount: ").append(hopCount())
            .append(eoli).append("transId: ").append(transId())
            .append(eoli).append("numSecs: ").append(numSecs())
            .append(eoli).append("flag: ").append(flag())
            .append(eoli).append("clientAddr: ").append(clientAddr())
            .append(eoli).append("yourAddr: ").append(yourAddr())
            .append(eoli).append("serverAddr: ").append(serverAddr())
            .append(eoli).append("gatewayAddr: ").append(gatewayAddr())
            .append(eoli).append("clientHwAddr: ").append(clientHwAddr())
            .append(eoli).append("msgType: ").append(msgType())
            ;
        
        for (DhcpOption o: optionsArray())
            sb.append(eoli).append("option " + o);
                
        return sb.toString();
    }    
    
}
