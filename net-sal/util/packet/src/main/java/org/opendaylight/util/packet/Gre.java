/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.EthernetType;


/**
 * GRE data store (immutable) and associated {@link Builder} (mutable).
 * 
 * @author Frank Wood
 */
public class Gre implements Protocol {

    /** Used to signify no key or sequence number. */
    public static final long NONE = -1L;
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {

        private int version;
        private EthernetType protoType;
        private long key = NONE;
        private long seqNum = NONE;
        private int checkSum;
        
        private Data() {}
        
        private Data(Data data) {
            version = data.version;
            protoType = data.protoType;
            key = data.key;
            seqNum = data.seqNum;
            checkSum = data.checkSum;
        }
        
        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(protoType);
        }
    }
    
    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * version = 0
         * key = NONE
         * seqNum = NONE
         * checkSum = NONE
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param gre builder is initialed from this protocol's data
         */
        public Builder(Gre gre) {
            this.data = new Data(gre.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Gre build() {
            return new Gre(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Gre buildNoVerify() {
            return new Gre(data, false);
        }
        
        /**
         * Sets the protocol type.
         * 
         * @param protoType hardware type enumeration
         * @return this instance
         */
        public Builder protoType(EthernetType protoType) {
            data.protoType = protoType;
            return this;
        }
        
        /**
         * Sets the version.
         * 
         * @param version the version number
         * @return this instance
         */
        public Builder version(int version) {
            data.version = version;
            return this;
        }
      
        /**
         * Sets the optional checksum value.
         * 
         * @param checkSum the checksum value
         * @return this instance
         */
        public Builder checkSum(int checkSum) {
            data.checkSum = checkSum;
            return this;
        }
        
        /**
         * Sets the optional key (can be {@link #NONE}).
         * 
         * @param key the key value
         * @return this instance
         */
        public Builder key(long key) {
            data.key = key;
            return this;
        }
        
        /**
         * Sets the optional sequence number (can be {@link #NONE}).
         * 
         * @param seqNum the sequence number value
         * @return this instance
         */
        public Builder seqNum(long seqNum) {
            data.seqNum = seqNum;
            return this;
        }
    }
    
    private Data data;
    
    private Gre(Data data, boolean verify) {
        this.data = data;
        ProtocolUtils.verify(verify, this, this.data);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.GRE;
    }

    /**
     * Returns the protocol type.
     * 
     * @return the protocol type
     */
    public EthernetType protoType() {
        return data.protoType;
    }
    
    /**
     * Returns the version.
     * 
     * @return the version
     */
    public int version() {
        return data.version;
    }
    
    /**
     * Returns the key or {@link #NONE}.
     * 
     * @return the key
     */
    public long key() {
        return data.key;
    }
    
    /**
     * Returns the sequence number or {@link #NONE}.
     * 
     * @return the sequence number
     */
    public long seqNum() {
        return data.seqNum;
    }
    
    /**
     * Returns the checksum value or {@link #NONE}.
     * 
     * @return the checksum value
     */
    public int checkSum() {
        return data.checkSum;
    }
 
    @Override
    public String toString() {
        return id() + ",ver=" + version() +
                ",proto=" + protoType() + 
                ",key=" + key() + 
                ",seqNum=" + seqNum() +
                ",chkSum=" + checkSum(); 
    }
    
    @Override
    public String toDebugString() {
        return toString();
    }
    
}
