/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.packet.IpCodecV6.OPTION_FIXED_HDR_LEN;
import static org.opendaylight.util.packet.IpCodecV6.OPTION_LEN_MULTIPLE;

import java.util.Arrays;

import org.opendaylight.util.net.IpAddress;


/**
 * IPv6 data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>IPv6 source address</li>
 * <li>IPv6 destination address</li>
 * <li>IPv6 flow label</li>
 * </ul>
 * @author Frank Wood
 */
public class IpV6 implements Protocol {

    /** IPv6 option data store (immutable). */
    public static class Option {

        private IpType type;
        private IpType nextHdr;
        private byte[] bytes;
        
        /**
         * Create a new IPv6 option data store.
         * 
         * @param type this option's header protocol type
         * @param nextHdr next header protocol type
         * @param bytes payload bytes
         */
        public Option(IpType type, IpType nextHdr, byte[] bytes) {
            this.type = type;
            this.nextHdr = nextHdr;
            this.bytes = Arrays.copyOf(bytes, bytes.length);
        }
        
        /**
         * Returns this option's protocol type.
         * 
         * @return this option's protocol type
         */
        public IpType type() {
            return type;
        }
        
        /**
         * Returns the next header protocol type.
         * 
         * @return the next header protocol type
         */
        public IpType nextHdr() {
            return nextHdr;
        }
        
        /**
         * Internally used by the package to access the option payload bytes.
         * 
         * @return the option payload bytes
         */
        byte[] bytesArray() {
            return bytes;
        }
        
        /**
         * Returns a copy of the option payload bytes.
         * 
         * @return the option payload bytes
         */
        public byte[] bytes() {
            return Arrays.copyOf(bytes,  bytes.length);
        }
        
        @Override
        public String toString() {
            return "[" + type + ",n=" + nextHdr + ",len=" + bytes.length + "]";
        }
    }
    
    private static final Option[] NO_OPTIONS = new Option[0];
    
    private static final String E_BAD_OPTION_V6_LEN =
            "IPv6 option length must be a multiple of 8-bits (padded)";
    
    private static final String E_BAD_ADDR =
            "Addresses family must be IPv6";
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {
        
        private IpTosDsfc tosDsfc = IpTosDsfc.CS0;
        private IpTosEcn tosEcn = IpTosEcn.NOT_ECT;
        private int flowLabel;
        private int payloadLen;
        private IpType nextHdr;
        private IpType nextProtocol;
        private int nextProtocolLen;
        private int hopLimit;
        private IpAddress srcAddr;
        private IpAddress dstAddr;
        private Option[] options = NO_OPTIONS;
        
        private Data() {}
        
        private Data(Data data) {
            tosDsfc = data.tosDsfc;
            tosEcn = data.tosEcn;
            flowLabel = data.flowLabel;
            payloadLen = data.payloadLen;
            nextHdr = data.nextHdr;
            nextProtocol = data.nextProtocol;
            nextProtocolLen = data.nextProtocolLen;
            hopLimit = data.hopLimit;
            dstAddr = data.dstAddr;
            srcAddr = data.srcAddr;
            options = Arrays.copyOf(data.options, data.options.length);
        }

        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(tosDsfc, tosEcn, dstAddr, srcAddr, nextHdr, options);
            
            if (srcAddr.getFamily() != IpAddress.Family.IPv6 ||
                    dstAddr.getFamily() != IpAddress.Family.IPv6)
                throw new ProtocolException(E_BAD_ADDR);
            
            for (Option opt: options) {
                if ( ( (OPTION_FIXED_HDR_LEN + opt.bytesArray().length)
                            % OPTION_LEN_MULTIPLE ) != 0 )
                    throw new ProtocolException(E_BAD_OPTION_V6_LEN);
            }
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * tosDsfc = IpTosDsfc.CS0
         * tosEcn = IpTosEcn.NOT_ECT
         * options = NO_OPTIONS
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param ip builder is initialed from this protocol's data
         */
        public Builder(IpV6 ip) {
            this.data = new Data(ip.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public IpV6 build() {
            return new IpV6(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        IpV6 buildNoVerify() {
            return new IpV6(data, false);
        }
        
        /**
         * Internally used by the package to set the decoded payload length.
         * 
         * @param payloadLen payload length
         * @return this instance
         */
        Builder payloadLen(int payloadLen) {
            data.payloadLen = payloadLen;
            return this;
        }
        
        /**
         * Sets the TOS Differentiated Services Field Codepoints (DSFC) type.
         *  
         * @param tosDsfc TOS DSFC type
         * @return this instance
         */
        public Builder tosDsfc(IpTosDsfc tosDsfc) {
            data.tosDsfc = tosDsfc;
            return this;
        }
        
        /**
         * Sets the TOS Explicit Congestion Notification (ECN) type.
         * 
         * @param tosEcn TOS ECN type
         * @return this instance
         */
        public Builder tosEcn(IpTosEcn tosEcn) {
            data.tosEcn = tosEcn;
            return this;
        }

        /**
         * Sets the flow label.
         * 
         * @param flowLabel flow label
         * @return this instance
         */
        public Builder flowLabel(int flowLabel) {
            data.flowLabel = flowLabel;
            return this;
        }  
        
        /**
         * Sets the next header field which could be a IPv6 extension option
         * or the next protocol layer.
         * 
         * @param nextHdr next header protocol type
         * @return this instance
         */
        public Builder nextHdr(IpType nextHdr) {
            data.nextHdr = nextHdr;
            return this;
        }
        
        /**
         * Internally used by the package to set the type of the next protocol
         * layer (ICMPv6 for example). This can be the same value as the
         * {@code nextHdr} if there are no options. If options exist, it will
         * be the last {@code nextHdr} defined in the last option. If set to
         * {@code IpType.IPV6_NO_NEXT_HDR} there are no protocol layers
         * following.
         * 
         * @param nextProtocol next protocol layer type
         * @return this instance
         */
        Builder nextProtocol(IpType nextProtocol) {
            data.nextProtocol = nextProtocol;
            return this;
        }
        
        /**
         * Internally used by the package to sets the length of the next
         * protocol layer (ICMPv6 for example). This can be the same value as
         * the {@code payloadLen} if there are no options.
         * 
         * @param nextProtocolLen next protocol layer length
         * @return this instance
         */
        Builder nextProtocolLen(int nextProtocolLen) {
            data.nextProtocolLen = nextProtocolLen;
            return this;
        }

        /**
         * Sets the hop limit.
         * 
         * @param hopLimit hop limit
         * @return this instance
         */
        public Builder hopLimit(int hopLimit) {
            data.hopLimit = hopLimit;
            return this;
        }

        /**
         * Sets the source address.
         * 
         * @param srcAddr source address
         * @return this instance
         */
        public Builder srcAddr(IpAddress srcAddr) {
            data.srcAddr = srcAddr;
            return this;
        }
        
        /**
         * Sets the destination address.
         * 
         * @param dstAddr destination address
         * @return this instance
         */
        public Builder dstAddr(IpAddress dstAddr) {
            data.dstAddr = dstAddr;
            return this;
        }

        /**
         * Sets the options.
         * 
         * @param options option array
         * @return this instance
         */
        public Builder options(Option[] options) {
            data.options = options;
            return this;
        }
    }  
    
    private Data data;

    private IpV6(Data data, boolean verify) {
        this.data = new Data(data);
        ProtocolUtils.verify(verify, this, this.data);
    }

    @Override
    public ProtocolId id() {
        return ProtocolId.IPV6;
    }

    /**
     * Returns the TOS Differentiated Services Field Codepoints (DSFC) type.
     * 
     * @return the TOS DSFC type
     */
    public IpTosDsfc tosDsfc() {
        return data.tosDsfc;
    }

    /**
     * Returns the TOS Explicit Congestion Notification (ECN) type.
     * 
     * @return the TOS ECN type
     */    
    public IpTosEcn tosEcn() {
        return data.tosEcn;
    }    

    /**
     * Returns the flow Label.
     * 
     * @return the flow label
     */
    public int flowLabel() {
        return data.flowLabel;
    }

    /**
     * Returns the decoded payload length (in octets) of the datagram including
     * any extension headers.
     * 
     * @return the payload length
     */
    public int payloadLen() {
        return data.payloadLen;
    }
    
    /**
     * Returns the next header protocol type.
     * 
     * @return the next header protocol type
     */
    public IpType nextHdr() {
        return data.nextHdr;
    }
    
    /**
     * Returns the decoded type of the next protocol layer (ICMPv6 for example).
     * This can be the same value as the {@code nextHdr} if there are no
     * options. If options exist, it will be the last {@code nextHdr} defined
     * in the last option. If set to {@code IpType.IPV6_NO_NEXT_HDR} there are
     * no protocol layers following.
     * 
     * @return the next protocol layer type
     */
    public IpType nextProtocol() {
        return data.nextProtocol;
    }

    /**
     * Returns the decoded length of the next protocol layer
     * (ICMPv6 for example). This can be the same value as the
     * {@code payloadLen} if there are no options.
     * 
     * @return the next protocol layer length
     */    
    public int nextProtocolLen() {
        return data.nextProtocolLen;
    }
    
    /**
     * Returns the hop limit.
     * 
     * @return the hop limit
     */
    public int hopLimit() {
        return data.hopLimit;
    }
    
    /**
     * Returns the destination address.
     * 
     * @return the destination address.
     */
    public IpAddress dstAddr() {
        return data.dstAddr;
    }
    
    /**
     * Returns the source address.
     * 
     * @return the source address
     */
    public IpAddress srcAddr() {
        return data.srcAddr;
    }
    
    /**
     * Internally used by the packet to access the options array.
     * 
     * @return the option array
     */
    Option[] optionsArray() {
        return data.options;
    }
    
    /**
     * Returns a copy of the option array.
     * 
     * @return the options array
     */
    public Option[] options() {
        return Arrays.copyOf(data.options, data.options.length);
    }
    
    @Override
    public String toString() {
        return id() + "," + nextProtocol() +
                ",d=" + dstAddr() + ",s=" + srcAddr(); 
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("tosDsfc: ").append(tosDsfc())
            .append(eoli).append("tosEcn: ").append(tosEcn())
            .append(eoli).append("flowLabel: ").append(flowLabel())
            .append(eoli).append("payloadLen (decode-only): ")
                         .append(payloadLen())
            .append(eoli).append("nextHdr: ").append(nextHdr())
            .append(eoli).append("nextProtocol (decode-only): ")
                         .append(nextProtocol())
            .append(eoli).append("nextProtocolLen (decode-only): ")
                         .append(nextProtocolLen())
            .append(eoli).append("hopLimit: ").append(hopLimit())
            .append(eoli).append("srcAddr: ").append(srcAddr())
            .append(eoli).append("dstAddr: ").append(dstAddr())
            ;
        
        for (Option o: optionsArray())
            sb.append(eoli).append("option: ").append(o);
            
        return sb.toString();
    }        
    
}
