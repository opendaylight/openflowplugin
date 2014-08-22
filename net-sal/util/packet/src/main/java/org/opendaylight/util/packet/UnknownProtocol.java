/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.packet.ProtocolUtils.EMPTY_BYTES;
import static org.opendaylight.util.packet.ProtocolUtils.EOLI;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.opendaylight.util.packet.ProtocolUtils.verify;

import java.util.Arrays;


/**
 * Unknown protocol (immutable) used to represent payload that we don't
 * currently support.  Consists of raw bytes.
 *
 * @author Frank Wood
 */
public class UnknownProtocol implements Protocol {

    /** Internal private data store. */
    private static class Data implements ProtocolData {
        
        public byte[] bytes = EMPTY_BYTES;

        private Data() {}
        
        private Data(Data data) {
            bytes = Arrays.copyOf(data.bytes, data.bytes.length);            
        }

        @Override
        public void verify() {
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * bytes = EMPTY_BYTES
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param up builder is initialed from this protocol's data
         */
        public Builder(UnknownProtocol up) {
            this.data = new Data(up.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public UnknownProtocol build() {
            return new UnknownProtocol(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        public UnknownProtocol buildNoVerify() {
            return new UnknownProtocol(data, false);
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

    private UnknownProtocol(Data data, boolean verify) {
        this.data = new Data(data);
        verify(verify, this, this.data);
    }   
    
    @Override
    public ProtocolId id() {
        return ProtocolId.UNKNOWN;
    }
    
    /**
     * Internally used by the package to return the payload bytes.
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
        return id() + ",len=" + bytesArray().length; 
    }
    
    @Override
    public String toDebugString() {
        String eoli = EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("bytes: ").append(hex(bytesArray()))
            ;
        return sb.toString();
    }  
    
}
