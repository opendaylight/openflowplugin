/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.packet.LldpTlv.Type.END;
import static org.opendaylight.util.packet.ProtocolUtils.hex;

import java.util.Arrays;
import java.util.List;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;


/**
 * LLDP Type-Length-Value data store (immutable).
 *
 * @author Frank Wood
 */
public class LldpTlv {

    /** Main TLV types. */
    public enum Type implements ProtocolEnum {
        
        /** Signifies the last TLV. */
        END(0x00),
        /** Chassis ID type. */
        CHASSIS_ID(0x01),
        /** Port ID type. */
        PORT_ID(0x02),
        /** Time to live type. */
        TTL(0x03),
        /** Port description type. */
        PORT_DESC(0x04),
        /** System name type. */
        SYS_NAME(0x05),
        /** System description type. */
        SYS_DESC(0x06),
        /** Capabilities type. */
        CAPS(0x07),
        /** Management Address type. */
        MGMT_ADDR(0x08),
        /** Private (organization) type. */
        PRIVATE(0x07f),
        ;

        private int code;
        
        private Type(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static Type get(int code) {
            return ProtocolUtils.getEnum(Type.class, code, END);
        }
    }  
    
    /** Chassis ID sub-types. */
    public enum ChassisIdSubType implements ProtocolEnum {
        
        /** Chassis component sub-type. */
        CHASSIS_COMP(0x01),
        /** Interface alias sub-type. */
        INTF_ALIAS(0x02),
        /** Port component sub-type. */
        PORT_COMP(0x03),
        /** MAC address sub-type. */
        MAC_ADDR(0x04),
        /** Network address sub-type. */
        NET_ADDR(0x05),
        /** Interface name sub-type. */
        INTF_NAME(0x06),
        /** Local sub-type. */
        LOCAL(0x07),
        ;
        
        private int code;
        
        private ChassisIdSubType(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static ChassisIdSubType get(int code) {
            return ProtocolUtils.getEnum(ChassisIdSubType.class, code, MAC_ADDR);
        }
    }
        
    /** Port ID sub-types. */
    public enum PortIdSubType implements ProtocolEnum {
        
        /** Interface alias sub-type. */
        INTF_ALIAS(0x01),
        /** Port component sub-type. */
        PORT_COMP(0x02),
        /** MAC address sub-type. */
        MAC_ADDR(0x03),
        /** Network address sub-type. */
        NET_ADDR(0x04),
        /** Interface name sub-type. */
        INTF_NAME(0x05),
        /** Agent circuit ID sub-type. */
        AGENT_CIRC_ID(0x06),
        /** Local sub-type */
        LOCAL(0x07),
        ;
        
        private int code;
        
        private PortIdSubType(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static PortIdSubType get(int code) {
            return ProtocolUtils.getEnum(PortIdSubType.class, code, INTF_NAME);
        }
    }

    /** Capabilities (bitmask enumeration). */
    public enum Capability implements ProtocolEnum {
        
        /** Default (not set). */
        NONE(0x0000),
        /** Other capability type. */
        OTHER(0x0001),
        /** Repeater capability type. */
        REPEATER(0x0002),
        /** Bridge capability type */
        BRIDGE(0x0004),
        /** WLAN access point capability type. */
        WLAN_AP(0x0008),
        /** Router capability type. */
        ROUTER(0x0010),
        /** Phone capability type. */
        PHONE(0x0020),
        /** DOCSIS capability type. */
        DOCSIS(0x0040),
        /** Station-only capability type. */
        STATION_ONLY(0x0080),
        ;
            
        private int code;
        
        private Capability(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }

        static int mask(Capability[] caps) {
            return ProtocolUtils.getMask(Capability.class, caps);
        }
        
        static Capability[] get(int mask) {
            List<Capability> l = ProtocolUtils.getEnums(Capability.class, mask);
            return l.toArray(new Capability[l.size()]);
        }
        
        static boolean has(int mask, Capability... caps) {
            return ProtocolUtils.has(mask, caps);
        }
        
        /**
         * Returns true if all the {@code caps2} are contained within the
         * {@code caps1} array.
         * 
         * @param caps1 array of capabilities
         * @param caps2 array of capabilities
         * @return true if {@code caps2} is contained within {@code caps1}
         */
        public static boolean has(Capability[] caps1, Capability... caps2) {
            return ProtocolUtils.contains(caps1, caps2);
        }
    }    
    
    /** Internal private data store. */
    private static class Data {
        
        private Type type = END;
        private byte[] bytes = ProtocolUtils.EMPTY_BYTES;
        private String name;
        private Integer number;
        private IpAddress ipAddr;
        private MacAddress macAddr;
        private int supportedMask = Capability.NONE.code();
        private int enabledMask = Capability.NONE.code();
        private ChassisIdSubType chassisIdSubType;
        private PortIdSubType portIdSubType;
        
        private Data() {}
        
        private Data(Data data) {
            type = data.type;
            bytes = data.bytes;
            name = data.name;
            number = data.number;
            ipAddr = data.ipAddr;
            macAddr = data.macAddr;
            chassisIdSubType = data.chassisIdSubType;
            portIdSubType = data.portIdSubType;
            supportedMask = data.supportedMask;
            enabledMask = data.enabledMask;
        }
    }
    
    /** Builder (mutable) used to create a new instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * type = END
         * bytes = EMPTY_BYTES
         * supported = NO_CAPS
         * enabled = NO_CAPS
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Create a new builder based on the specified type.
         * 
         * @param type TLV type 
         */
        public Builder(Type type) {
            this.data = new Data();
            type(type);
        }
        
        /**
         * Creates a new TLV instance from this builder.
         * 
         * @return the new TLV instance
         */
        public LldpTlv build() {
            return new LldpTlv(data);
        }
    
        /**
         * Sets the TLV type.
         * 
         * @param type TLV type
         * @return this instance
         */
        public Builder type(Type type) {
            data.type = type;
            return this;
        }

        /**
         * Sets the bytes array.
         * 
         * @param bytes array of bytes
         * @return this instance
         */
        public Builder bytes(byte[] bytes) {
            data.bytes = bytes;
            return this;
        }
        
        /**
         * Sets the name.
         * 
         * @param name string name
         * @return this instance
         */
        public Builder name(String name) {
            data.name = name;
            return this;
        }
        
        /**
         * Sets the number (scalar).
         * 
         * @param number numeric value
         * @return this instance
         */
        public Builder number(int number) {
            data.number = number;
            return this;
        }
        
        /**
         * Sets the IP address.
         * 
         * @param ipAddr IP address
         * @return this instance
         */
        public Builder ipAddr(IpAddress ipAddr) {
            data.ipAddr = ipAddr;
            return this;
        }
    
        /**
         * Sets the MAC address.
         * 
         * @param macAddr MAC address
         * @return this instance
         */
        public Builder macAddr(MacAddress macAddr) {
            data.macAddr = macAddr;
            return this;
        }
        
        /**
         * Sets the list of supported capabilities.
         * 
         * @param supportedMask capability mask
         * @return this instance
         */
        public Builder supportedMask(int supportedMask) {
            data.supportedMask = supportedMask;
            return this;
        }

        /**
         * Sets the list of supported capabilities.
         * 
         * @param supported capability array
         * @return this instance
         */
        public Builder supported(Capability... supported) {
            data.supportedMask = Capability.mask(supported);
            return this;
        }

        /**
         * Sets the list of enabled capabilities. Must be a subset of the
         * {@link LldpTlv#supported()} capabilities.
         * 
         * @param enabledMask capability mask
         * @return this instance
         */
        public Builder enabledMask(int enabledMask) {
            data.enabledMask = enabledMask;
            return this;
        }
        
        /**
         * Sets the list of enabled capabilities. Must be a subset of the
         * {@link LldpTlv#supported()} capabilities.
         * 
         * @param enabled capability array
         * @return this instance
         */
        public Builder enabled(Capability... enabled) {
            data.enabledMask = Capability.mask(enabled);
            return this;
        }        
        
        /**
         * Sets the Chassis ID sub-type.
         * 
         * @param chassisIdSubType Chassis ID sub-type
         * @return this instance
         */
        public Builder chassisIdSubType(ChassisIdSubType chassisIdSubType) {
            data.chassisIdSubType = chassisIdSubType;
            return this;
        }
        
        /**
         * Sets the Port ID sub-type.
         * 
         * @param portIdSubType port ID sub-type
         * @return this instance
         */
        public Builder portIdSubType(PortIdSubType portIdSubType) {
            data.portIdSubType = portIdSubType;
            return this;
        }
    }
    
    /**
     * Build a Chassis ID {@link LldpTlv} of {@link ChassisIdSubType#MAC_ADDR}.
     * 
     * @param mac MAC address 
     * @return the immutable LLDP TLV instance using the MAC address field
     */
    public static LldpTlv chassisIdMacAddr(MacAddress mac) {
        return new Builder()
            .type(Type.CHASSIS_ID)
            .chassisIdSubType(ChassisIdSubType.MAC_ADDR)
            .macAddr(mac)
            .build();
    }

    /**
     * Build a Chassis ID {@link LldpTlv} for one of the following sub-types:
     * {@link ChassisIdSubType#INTF_NAME},
     * {@link ChassisIdSubType#INTF_NAME},  {@link ChassisIdSubType#LOCAL},
     * {@link ChassisIdSubType#CHASSIS_COMP},
     * {@link ChassisIdSubType#INTF_ALIAS},
     * {@link ChassisIdSubType#PORT_COMP}
     * 
     * @param st chassis ID sub-type
     * @param name chassis ID string value
     * @return the immutable LLDP TLV instance using the name field
     */
    public static LldpTlv chassisIdName(ChassisIdSubType st, String name) {
        return new Builder()
            .type(Type.CHASSIS_ID)
            .chassisIdSubType(st)
            .name(name)
            .build();
    }
    
    /**
     * Build a Port ID {@link LldpTlv} of {@link PortIdSubType#MAC_ADDR}.
     * 
     * @param mac MAC address 
     * @return the immutable LLDP TLV instance using the MAC address field
     */
    public static LldpTlv portIdMacAddr(MacAddress mac) {
        return new Builder()
            .type(Type.PORT_ID)
            .portIdSubType(PortIdSubType.MAC_ADDR)
            .macAddr(mac)
            .build();
    }

    /**
     * Build a Port ID {@link LldpTlv} for one of the following sub-types:
     * {@link PortIdSubType#INTF_NAME},
     * {@link PortIdSubType#INTF_NAME},  {@link ChassisIdSubType#LOCAL},
     * {@link PortIdSubType#AGENT_CIRC_ID},
     * {@link PortIdSubType#INTF_ALIAS},
     * {@link PortIdSubType#PORT_COMP}
     * 
     * @param st port ID sub-type
     * @param name port ID string value
     * @return the immutable LLDP TLV instance using the name field
     */
    public static LldpTlv portIdName(PortIdSubType st, String name) {
        return new Builder()
            .type(Type.PORT_ID)
            .portIdSubType(st)
            .name(name)
            .build();
    }
    
    /**
     * Build a Time-to-Live {@link LldpTlv}.
     * 
     * @param value TTL value 
     * @return the immutable LLDP TLV instance using the number field
     */
    public static LldpTlv ttl(int value) {
        return new Builder().type(Type.TTL).number(value).build();
    }
    
    /** PrivateBuilder (mutable) used to create a new instances. */
    public static class PrivateBuilder extends Builder {
        public PrivateBuilder() {
            super(Type.PRIVATE);
        }
    }    
    
    private Data data;
    
    private LldpTlv(Data data) {
        this.data = data;
    }
    
    /**
     * Returns the type.
     * 
     * @return the type
     */
    public Type type() {
        return data.type;
    }

    /**
     * Returns a copy of the bytes array.
     * 
     * @return the bytes array
     */
    public byte[] bytes() {
        return Arrays.copyOf(data.bytes, data.bytes.length);
    }
    
    /**
     * Internally used by the package to return the bytes array.
     * 
     * @return the bytes array
     */
    byte[] bytesArray() {
        return data.bytes;
    }
    
    /**
     * Returns the MAC address.
     * 
     * @return the MAC address
     */
    public MacAddress macAddr() {
        return data.macAddr;
    }

    /**
     * Returns the string name.
     * 
     * @return the string name
     */
    public String name() {
        return data.name;
    }
    
    /**
     * Returns the number value (scalar).
     * 
     * @return the number value
     */
    public Integer number() {
        return data.number;
    }
    
    /**
     * Returns the IP address.
     * 
     * @return the IP address
     */
    public IpAddress ipAddr() {
        return data.ipAddr;
    }
    
    /**
     * Returns a copy of the supported capabilities.
     * 
     * @return the supported capabilities
     */
    public Capability[] supported() {
        return Capability.get(data.supportedMask);
    }
    
    /**
     * Internally used by the package to return the supported capabilities bit
     * mask.
     * 
     * @return the supported capabilities bit mask
     */
    int supportedMask() {
        return data.supportedMask;
    }
    
    /**
     * Returns a copy of the enabled capabilities.
     * 
     * @return the enabled capabilities
     */
    public Capability[] enabled() {
        return Capability.get(data.enabledMask);
    }
    
    /**
     * Internally used by the package to return the enabled capabilities bit
     * mask.
     * 
     * @return the enabled capabilities bit mask
     */
    int enabledMask() {
        return data.enabledMask;
    }
    
    /**
     * Returns the chassis ID sub-type.
     * 
     * @return the chassis ID sub-type
     */
    public ChassisIdSubType chassisIdSubType() {
        return data.chassisIdSubType;
    }
    
    /**
     * Returns the port ID sub-type.
     * 
     * @return the port ID sub-type
     */
    public PortIdSubType portIdSubType() {
        return data.portIdSubType;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        Capability[] supCaps = supported();
        Capability[] enaCaps = enabled();
        
        sb.append("[").append(type()).append(",");
        if (0 < data.bytes.length)
            sb.append(ProtocolUtils.hex(data.bytes));
        else if (null != macAddr())
            sb.append(macAddr());
        else if (null != name())
            sb.append(name());
        else if (null != number())
            sb.append(number());
        else if (null != ipAddr())
            sb.append(ipAddr());
        else if (0 < supCaps.length) {
            sb.append("s:").append(Arrays.asList(supCaps))
              .append(",e:").append(Arrays.asList(enaCaps));
        }
        else
            sb.append("-");
        sb.append("]");
        return sb.toString();
    }
    
}
