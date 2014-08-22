/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.net.EthernetType.IPv4;
import static org.opendaylight.util.packet.Ethernet.Control.NO_CONTROL;
import static org.opendaylight.util.packet.Ethernet.Dsap.NO_DSAP;
import static org.opendaylight.util.packet.Ethernet.Ssap.NO_SSAP;
import static org.opendaylight.util.packet.Ethernet.VlanPriority.PRIORITY_1;

import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.MacAddress;


/**
 * Ethernet data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>Ethernet destination address</li>
 * <li>Ethernet source address</li>
 * <li>Ethernet frame type</li>
 * <li>VLAN ID</li>
 * <li>VLAN priority</li>
 * </ul>
 * @author Frank Wood
 */
public class Ethernet implements Protocol {

    /** Used to indicate 802.3 Snap packet types. */
    public static final EthernetType NONE = EthernetType.valueOf(0);
    
    /** VLAN Priorities. */
    public enum VlanPriority implements ProtocolEnum {
        
        /** Background priority (level 0 - lowest). */
        PRIORITY_0(1),
        /** Best effort priority (level 1 - normal). Note that encoding is 0. */
        PRIORITY_1(0),
        /** Excellent effort priority (level 2). */
        PRIORITY_2(2),
        /** Critical application priority (level 3). */
        PRIORITY_3(3),
        /** Video priority (level 4). */
        PRIORITY_4(4),
        /** Voice priority (level 5). */
        PRIORITY_5(5),
        /** Inter-network control priority (level 6). */
        PRIORITY_6(6),
        /** Network control priority (level 7 - highest). */
        PRIORITY_7(7),
        ;
        
        private int code;
        
        private VlanPriority(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static VlanPriority get(int code) {
            return ProtocolUtils.getEnum(VlanPriority.class, code, PRIORITY_1);
        }
    } 
    
    /** Destination service access point types. */
    public enum Dsap implements ProtocolEnum {
        
        /** No destination service access point type. */
        NO_DSAP(0),
        /** SNAP destination service access point type. */
        SNAP(0xaa),
        ;
        
        private int code;
        
        private Dsap(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static Dsap get(int code) {
            return ProtocolUtils.getEnum(Dsap.class, code, NO_DSAP);
        }
    }   
    
    /** Source service access point types. */
    public enum Ssap implements ProtocolEnum {
        
        /** No source service access point type. */
        NO_SSAP(0),
        /** SNAP source service access point type. */
        SNAP(0xaa),
        ;
        
        private int code;
        
        private Ssap(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static Ssap get(int code) {
            return ProtocolUtils.getEnum(Ssap.class, code, NO_SSAP);
        }
    }
    
    /** Control types. */
    public enum Control implements ProtocolEnum {
        
        /** No control type. */
        NO_CONTROL(0),
        /** Unnumbered control type. */
        UNNUMBERED(0x03),
        ;
        
        private int code;
        
        private Control(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static Control get(int code) {
            return ProtocolUtils.getEnum(Control.class, code, NO_CONTROL);
        }
    } 
    
    /** SNAP ID (vendor and local) data store (immutable). */
    public static class SnapId {
        
        private long vendor;
        private int local;
        
        public SnapId(long vendor, int local) {
            this.vendor = vendor;
            this.local = local;
        }
        
        public long vendor() {
            return vendor;
        }
        
        public int local() {
            return local;
        }
        
        @Override
        public String toString() {
            return "[" + vendor + "," + local + "]";
        }
    }
    
    private static final SnapId NO_SNAP_ID = new SnapId(0, 0);
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {
        
        private MacAddress dstAddr;
        private MacAddress srcAddr;
        private EthernetType type = IPv4;
        private int len;
        private int vlanId;
        private boolean vlanDei;
        private VlanPriority vlanPriority = PRIORITY_1;
        private Dsap dsap = NO_DSAP;
        private Ssap ssap = NO_SSAP;
        private Control control = NO_CONTROL;
        private SnapId snapId = NO_SNAP_ID;
        
        private Data() {}
        
        private Data(Data data) {
            dstAddr = data.dstAddr;
            srcAddr = data.srcAddr;
            type = data.type;
            len = data.len;
            vlanId = data.vlanId;
            vlanDei = data.vlanDei;
            vlanPriority = data.vlanPriority;
            dsap = data.dsap;
            ssap = data.ssap;
            control = data.control;
            snapId = data.snapId;
        }

        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(dstAddr, srcAddr, type, dsap, ssap, control, snapId);
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * type = IPv4 (NONE indicates 802.3 Snap)
         * vlanId = 0 (0 < indicates 802.1Q VLAN ID)
         * vlanPriority = PRIORITY_1
         * dsap = NO_DSAP
         * ssap = NO_SSAP
         * control = NO_CONTROL
         * snapId = NO_SNAP_ID
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param eth builder is initialed from this protocol's data
         */
        public Builder(Ethernet eth) {
            this.data = new Data(eth.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Ethernet build() {
            return new Ethernet(data, true);
        }

        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Ethernet buildNoVerify() {
            return new Ethernet(data, false);
        }

        /**
         * Internally used by the package to set the length in 802.3 packets.
         * 
         * @param len length for 802.3 encodings
         * @return this instance
         */
        Builder len(int len) {
            data.len = len;
            return this;
        }

        /**
         * Sets the destination address.
         * 
         * @param dstAddr destination address
         * @return this instance
         */
        public Builder dstAddr(MacAddress dstAddr) {
            data.dstAddr = dstAddr;
            return this;
        }
        
        /**
         * Sets the source address.
         * 
         * @param srcAddr source address
         * @return this instance
         */
        public Builder srcAddr(MacAddress srcAddr) {
            data.srcAddr = srcAddr;
            return this;
        }

        /**
         * Sets the ethernet type. A value of {@link Ethernet#NONE} indicates
         * an 802.3 encoding. 
         * 
         * @param type ethernet type
         * @return this instance
         */
        public Builder type(EthernetType type) {
            data.type = type;
            return this;
        }
        
        /**
         * Sets the VLAN ID. A value > 0 indicates 802.1Q encoding.
         * 
         * @param vlanId VLAN ID
         * @return this instance
         */
        public Builder vlanId(int vlanId) {
            data.vlanId = vlanId;
            return this;
        }
        
        /**
         * Sets the VLAN Drop Eligibility Indicator flag.
         * 
         * @param vlanDei VLAN DEI flag value
         * @return this instance
         */
        public Builder vlanDei(boolean vlanDei) {
            data.vlanDei = vlanDei;
            return this;
        }

        /**
         * Sets the VLAN priority.
         * 
         * @param vlanPriority VLAN priority
         * @return this instance
         */
        public Builder vlanPriority(VlanPriority vlanPriority) {
            data.vlanPriority = vlanPriority;
            return this;
        }
        
        /**
         * Sets the destination service access point.
         * 
         * @param dsap destination service access point.
         * @return this instance
         */
        public Builder dsap(Dsap dsap) {
            data.dsap = dsap;
            return this;
        }
        
        /**
         * Sets the source service access point.
         * 
         * @param ssap source service access point.
         * @return this instance
         */
        public Builder ssap(Ssap ssap) {
            data.ssap = ssap;
            return this;
        }
        
        /**
         * Sets the 802.3 control type.
         * 
         * @param control 802.3 control type
         * @return this instance
         */
        public Builder control(Control control) {
            data.control = control;
            return this;
        }
        
        /**
         * Sets the SNAP ID.
         * 
         * @param snapId SNAP ID
         * @return this instance
         */
        public Builder snapId(SnapId snapId) {
            data.snapId = snapId;
            return this;
        }        
    }
    
    private Data data;
    
    private Ethernet(Data data, boolean verify) {
        this.data = new Data(data);
        ProtocolUtils.verify(verify, this, this.data);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.ETHERNET;
    }

    /**
     * Returns the destination address.
     * 
     * @return the destination address.
     */
    public MacAddress dstAddr() {
        return data.dstAddr;
    }
    
    /**
     * Returns the source address.
     * 
     * @return the source address
     */
    public MacAddress srcAddr() {
        return data.srcAddr;
    }
    
    /**
     * Returns the Ethernet type.
     * 
     * @return the Ethernet type
     */
    public EthernetType type() {
        return data.type;
    }
    
    /**
     * Returns the length in 802.3 packets.
     * 
     * @return the 802.3 length
     */

    public int len() {
        return data.len;
    }
    
    /**
     * Returns the VLAN ID.
     * 
     * @return the VLAN ID
     */
    public int vlanId() {
        return data.vlanId;
    }
    
    /**
     * Returns the VLAN Drop Eligibility Indicator flag.
     * 
     * @return the VLAN DEI flag
     */
    public boolean vlanDei() {
        return data.vlanDei;
    }
    
    /**
     * Returns the VLAN priority.
     * 
     * @return the VLAN priority
     */
    public VlanPriority vlanPriority() {
        return data.vlanPriority;
    }

    /**
     * Returns the destination service access point (802.3 encodings).
     * 
     * @return the destination service access point
     */
    public Dsap dsap() {
        return data.dsap;
    }

    /**
     * Returns the source service access point (802.3 encodings).
     * 
     * @return the source service access point
     */
    public Ssap ssap() {
        return data.ssap;
    }

    /**
     * Returns the control type (802.3 encodings).
     * 
     * @return the control type
     */
    public Control control() {
        return data.control;
    }

    /**
     * Returns the SNAP ID (802.3 encodings).
     * 
     * @return the SNAP ID
     */
    public SnapId snapId() {
        return data.snapId;
    }

    @Override
    public String toString() {
        return id() + "," + type() +
                ",dAddr=" + dstAddr() + 
                ",sAddr=" + srcAddr(); 
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("dstAddr: ").append(dstAddr())
            .append(eoli).append("srcAddr: ").append(srcAddr())
            .append(eoli).append("type: ").append(type())
            .append(eoli).append("len (only 802.3): ").append(len())
            .append(eoli).append("vlanId: ").append(vlanId())
            .append(eoli).append("vlanDei: ").append(vlanDei())
            .append(eoli).append("vlanPriority: ").append(vlanPriority())
            .append(eoli).append("dsap: ").append(dsap())
            .append(eoli).append("ssap: ").append(ssap())
            .append(eoli).append("control: ").append(control())
            .append(eoli).append("snapId: ").append(snapId())
            ;
        return sb.toString();
    }        
    
}
