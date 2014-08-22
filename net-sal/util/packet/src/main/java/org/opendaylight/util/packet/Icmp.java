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


/**
 * ICMPv4 data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>ICMPv4 type</li>
 * <li>ICMPv4 code</li>
 * </ul>
 * @author Frank Wood
 */
public class Icmp implements Protocol {
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {

        private IcmpTypeCode typeCode = IcmpTypeCode.ECHO_REQ;
        private int checkSum;
        private int ident;
        private int seqNum;
        private byte[] bytes = ProtocolUtils.EMPTY_BYTES;
        
        private Data() {}
        
        private Data(Data data) {
            typeCode = data.typeCode;
            checkSum = data.checkSum;
            ident = data.ident;
            seqNum = data.seqNum;
            bytes = Arrays.copyOf(data.bytes, data.bytes.length);
        }

        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(typeCode, bytes);
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * typeCode = ECHO_REQ
         * bytes = EMPTY_BYTES
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param icmp builder is initialed from this protocol's data
         */
        public Builder(Icmp icmp) {
            this.data = new Data(icmp.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Icmp build() {
            return new Icmp(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Icmp buildNoVerify() {
            return new Icmp(data, false);
        }
        
        /**
         * Internally used by the package to set the checksum.
         * 
         * @param checkSum check sum
         * @return this instance
         */
        Builder checkSum(int checkSum) {
            data.checkSum = checkSum;
            return this;
        }
        
        /**
         * Sets the type/code enumeration..
         *  
         * @param typeCode type/code enumeration
         * @return this instance
         */
        public Builder typeCode(IcmpTypeCode typeCode) {
            data.typeCode = typeCode;
            return this;
        }
        
        /**
         * Sets the identifier for {@link IcmpTypeCode#ECHO_REQ} or
         * {@link IcmpTypeCode#ECHO_REPLY} messages.
         * 
         * @param ident identifier value
         * @return this instance
         */
        public Builder ident(int ident) {
            data.ident = ident;
            return this;
        }
        
        /**
         * Sets the sequence number for {@link IcmpTypeCode#ECHO_REQ} or
         * {@link IcmpTypeCode#ECHO_REPLY} messages.
         * 
         * @param seqNum sequence number value
         * @return this instance
         */
        public Builder seqNum(int seqNum) {
            data.seqNum = seqNum;
            return this;
        }

        /**
         * Sets the payload bytes.
         * 
         * @param bytes payload bytes
         * @return this instance
         */
        public Builder bytes(byte[] bytes) {
            data.bytes = bytes;
            return this;
        }
    }    
    
    private Data data;

    private Icmp(Data data, boolean verify) {
        this.data = new Data(data);
        ProtocolUtils.verify(verify, this, this.data);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.ICMP;
    }

    /**
     * Returns the type/code enumeration.
     * 
     * @return the type/code enumeration.
     */
    public IcmpTypeCode typeCode() {
        return data.typeCode;
    }
    
    /**
     * Returns the decoded check sum.
     * 
     * @return the check sum
     */
    public int checkSum() {
        return data.checkSum;
    }
    
    /**
     * Returns the identifier for {@link IcmpTypeCode#ECHO_REQ} or
     * {@link IcmpTypeCode#ECHO_REPLY} messages.
     * 
     * @return the identifier
     */
    public int ident() {
        return data.ident;
    }
    
    /**
     * Returns the sequence number for {@link IcmpTypeCode#ECHO_REQ} or
     * {@link IcmpTypeCode#ECHO_REPLY} messages.
     * 
     * @return the sequence number
     */
    public int seqNum() {
        return data.seqNum;
    }
    
    /**
     * Internally used to return the payload bytes.
     * 
     * @return the payload bytes
     */
    byte[] bytesArray() {
        return data.bytes;
    }
    
    /**
     * Returns a copy of the payload bytes.
     * 
     * @return the payload bytes
     */
    public byte[] bytes() {
        return Arrays.copyOf(data.bytes, data.bytes.length);
    }
    
    @Override
    public String toString() {
        return id() + "," + typeCode();
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("typeCode: ").append(typeCode())
            .append(eoli).append("checkSum (decode-only): ")
                         .append(ProtocolUtils.hex(checkSum()))
            .append(eoli).append("ident (req/reply-only): ").append(ident())
            .append(eoli).append("seqNum (req/reply-only): ").append(seqNum())
            .append(eoli).append("bytes: ").append(ProtocolUtils.hex(bytesArray()));
            ;
        return sb.toString();
    }      
    
}
