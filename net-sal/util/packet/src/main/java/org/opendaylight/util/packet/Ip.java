/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.packet.ProtocolUtils.hex;

import java.util.Arrays;

import org.opendaylight.util.net.IpAddress;


/**
 * IPv4 data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>IPv4 TOS Differentiated Services Field Codepoints (DSFC)</li>
 * <li>IPv4 TOS Explicit Congestion Notification (ECN)</li>
 * <li>IPv4 protocol</li>
 * <li>IPv4 source address</li>
 * <li>IPv4 destination address</li>
 * </ul>
 * @author Frank Wood
 */
public class Ip implements Protocol {

    private static final String E_BAD_OPTION_LEN =
            "Option length must be a multiple of 32-bits (padded)";

    private static final String E_BAD_ADDR =
            "Addresses family must be IPv4";
    
    private static final int OPTION_LEN_MULTIPLE = 4;
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {
        
        private IpTosDsfc tosDsfc = IpTosDsfc.CS0;
        private IpTosEcn tosEcn = IpTosEcn.NOT_ECT;
        private int ident;
        private boolean doNotFrag;
        private boolean moreFragToCome;
        private int fragOffset;
        private int ttl;
        private IpType type;
        private int checkSum;
        private IpAddress dstAddr;
        private IpAddress srcAddr;
        private byte[] options = ProtocolUtils.EMPTY_BYTES;
        private int hdrLen;
        private int totalLen;
        
        private Data() {}
        
        private Data(Data data) {
            tosDsfc = data.tosDsfc;
            tosEcn = data.tosEcn;
            ident = data.ident;
            doNotFrag = data.doNotFrag;
            moreFragToCome = data.moreFragToCome;
            fragOffset = data.fragOffset;
            ttl = data.ttl;
            type = data.type;
            checkSum = data.checkSum;
            dstAddr = data.dstAddr;
            srcAddr = data.srcAddr;
            options = Arrays.copyOf(data.options, data.options.length);
            hdrLen = data.hdrLen;
            totalLen = data.totalLen;
        }

        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(dstAddr, srcAddr, options);
            
            if (srcAddr.getFamily() != IpAddress.Family.IPv4 ||
                    dstAddr.getFamily() != IpAddress.Family.IPv4)
                throw new ProtocolException(E_BAD_ADDR);
            
            if ((options.length % OPTION_LEN_MULTIPLE) != 0)
                throw new ProtocolException(E_BAD_OPTION_LEN);
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;

        /**
         * Create a new builder using the defaults:
         * <pre>
         * tosDsfc = CS0
         * tosEcn = NOT_ECT
         * ident = 0
         * doNotFrag = false
         * moreFragToCome = false
         * fragOffset = 0
         * ttl = 0
         * type = null
         * options = EMPTY_BYTES
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
        public Builder(Ip ip) {
            this.data = new Data(ip.data);
        }

        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Ip build() {
            return new Ip(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Ip buildNoVerify() {
            return new Ip(data, false);
        }
        
        /**
         * Internally used by the package to set the decoded check sum.
         * 
         * @param checkSum check sum value
         * @return this instance
         */
        Builder checkSum(int checkSum) {
            data.checkSum = checkSum;
            return this;
        }

        /**
         * Internally used by the package to set the decoded header length.
         * 
         * @param hdrLen header length
         * @return this instance
         */
        Builder hdrLen(int hdrLen) {
            data.hdrLen = hdrLen;
            return this;
        }

        /**
         * Internally used by the package to set the decoded total length.
         * 
         * @param totalLen total length
         * @return this instance
         */
        Builder totalLen(int totalLen) {
            data.totalLen = totalLen;
            return this;
        }

        /**
         * Sets the identifier value assigned by the sender.
         * 
         * @param ident identifier value
         * @return this instance
         */
        public Builder ident(int ident) {
            data.ident = ident;
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
         * Sets the fragment offset.
         * 
         * @param fragOffset fragment offset
         * @return this instance
         */
        public Builder fragOffset(int fragOffset) {
            data.fragOffset = fragOffset;
            return this;
        }

        /**
         * Sets the flag to not fragment this packet.
         * 
         * @param doNotFrag do not fragment
         * @return this instance
         */
        public Builder doNotFrag(boolean doNotFrag) {
            data.doNotFrag = doNotFrag;
            return this;
        }
        
        /**
         * Sets the flag to indicate that more fragments are coming.
         * 
         * @param moreFragToCome if true, more fragments are coming
         * @return this instance
         */
        public Builder moreFragToCome(boolean moreFragToCome) {
            data.moreFragToCome = moreFragToCome;
            return this;
        }

        /**
         * Sets the time to live seconds.
         * 
         * @param ttl time to live seconds 
         * @return this instance
         */
        public Builder ttl(int ttl) {
            data.ttl = ttl;
            return this;
        }
        
        /**
         * Sets the protocol type.
         * 
         * @param type protocol type
         * @return this instance
         */
        public Builder type(IpType type) {
            data.type = type;
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
         * Sets the options bytes.
         * 
         * @param options encoded option bytes
         * @return this instance
         */
        public Builder options(byte[] options) {
            data.options = options;
            return this;
        }
        
    }  
    
    private Data data;

    private Ip(Data data, boolean verify) {
        this.data = new Data(data);
        ProtocolUtils.verify(verify, this, this.data);
    }

    @Override
    public ProtocolId id() {
        return ProtocolId.IP;
    }

    /**
     * Returns the identifier value assigned by the sender.
     * 
     * @return the identifier value
     */
    public int ident() {
        return data.ident;
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
     * Returns the fragment offset.
     * 
     * @return the fragment offset
     */
    public int fragOffset() {
        return data.fragOffset;
    }
    
    /**
     * Returns the flag indicating not to fragment this packet.
     *  
     * @return the do not fragment flag
     */
    public boolean doNotFrag() {
        return data.doNotFrag;
    }
    
    /**
     * Returns the flag indicating that more fragments are coming.
     * 
     * @return the more fragments to come flag 
     */
    public boolean moreFragToCome() {
        return data.moreFragToCome;
    }
    
    /**
     * Returns the Time to Live seconds.
     * 
     * @return the TTL seconds
     */
    public int ttl() {
        return data.ttl;
    }
    
    /**
     * Returns the next (payload) protocol type.
     * 
     * @return the payload protocol type
     */
    public IpType type() {
        return data.type;
    }
    
    /**
     * Returns the decoded check sum. This value is only available in decoded
     * packets. 
     * 
     * @return the check sum
     */
    public int checkSum() {
        return data.checkSum;
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
     * Internally used by the packet to access the option bytes.
     * 
     * @return the option bytes
     */
    byte[] optionsArray() {
        return data.options;
    }
    
    /**
     * Returns a copy of the option bytes.
     * 
     * @return the option bytes
     */
    public byte[] options() {
        return Arrays.copyOf(data.options, data.options.length);
    }    
    
    /**
     * Returns the decoded header length (in octets). This value is only
     * available in decoded packets.
     * 
     * @return the header length
     */
    public int hdrLen() {
        return data.hdrLen;
    }

    /**
     * Returns the decoded length (in octets) of the datagram including the
     * internet header and data. This value is only available in decoded
     * packets. 
     * 
     * @return the total length
     */
    public int totalLen() {
        return data.totalLen;
    }
    
    @Override
    public String toString() {
        return id() + "," + type() + ",d=" + dstAddr() + ",s=" + srcAddr(); 
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("hdrLen (decode-only): ").append(hdrLen())
            .append(eoli).append("tosDsfc: ").append(tosDsfc())
            .append(eoli).append("tosEcn: ").append(tosEcn())
            .append(eoli).append("totalLen (decode-only): ").append(totalLen())
            .append(eoli).append("ident: ").append(ident())
            .append(eoli).append("doNotFrag: ").append(doNotFrag())
            .append(eoli).append("moreFragToCome: ").append(moreFragToCome())
            .append(eoli).append("fragOffset: ").append(fragOffset())
            .append(eoli).append("ttl: ").append(ttl())
            .append(eoli).append("type: ").append(type())
            .append(eoli).append("checkSum (decode-only): ")
                         .append(ProtocolUtils.hex(checkSum()))
            .append(eoli).append("srcAddr: ").append(srcAddr())
            .append(eoli).append("dstAddr: ").append(dstAddr())
            .append(eoli).append("options: ").append(ProtocolUtils.hex(optionsArray()))
            ;
        return sb.toString();
    }        
    
}
