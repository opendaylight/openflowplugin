/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;

import java.util.Arrays;


/**
 * MPLS data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>MPLS label</li>
 * <li>MPLS TTL</li>
 * <li>MPLS Bottom of Label Stack bit</li>
 * </ul>
 * @author Frank Wood
 */
public class Mpls implements Protocol {

    /** MPLS label switching header (immutable). */
    public static class Header {
        private int label;
        private int ttl;
        
        /**
         * Constructor to create a new MPLS label switching header.
         * 
         * @param label label number
         * @param ttl time to live in seconds
         */
        public Header(int label, int ttl) {
            this.label = label;
            this.ttl = ttl;
        }
        
        @Override
        public String toString() {
            return "[l=" + label + ",ttl=" + ttl + "]";
        }
        
        /**
         * Returns the header label.
         * 
         * @return the label value
         */
        public int label() {
            return label;
        }
        
        /**
         * Returns the time to live in seconds.
         * 
         * @return the TTL in seconds
         */
        public int ttl() {
            return ttl;
        }
    }
    
    private static final Header[] NO_HEADERS = new Header[0];
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {

        private Header[] headers = NO_HEADERS;
        
        private Data() {}
        
        private Data(Data data) {
            headers = Arrays.copyOf(data.headers, data.headers.length);
        }
        
        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull((Object) headers);
        }
    }
    
    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * headers = NO_HEADERS
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param mpls builder is initialed from this protocol's data
         */
        public Builder(Mpls mpls) {
            this.data = new Data(mpls.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Mpls build() {
            return new Mpls(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Mpls buildNoVerify() {
            return new Mpls(data, false);
        }
        
        /**
         * Sets the headers.
         * 
         * @param headers new array of headers
         * @return this instance
         */
        public Builder headers(Header[] headers) {
            data.headers = headers;
            return this;
        }
    }
    
    private Data data;
    
    private Mpls(Data data, boolean verify) {
        this.data = data;
        ProtocolUtils.verify(verify, this, this.data);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.MPLS;
    }

    /**
     * Internally used by the packet to access the headers array.
     * 
     * @return the headers array
     */
    Header[] headersArray() {
        return data.headers;
    }
    
    /**
     * Returns a copy of the headers array.
     * 
     * @return the headers array
     */
    public Header[] headers() {
        return Arrays.copyOf(data.headers, data.headers.length);
    }  

    @Override
    public String toString() {
        return id() + ",nh=" + headersArray().length; 
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            ;
        
        for (Header h: headersArray())
            sb.append(eoli).append("header: ").append(h);
        
        return sb.toString();
    }
    
}
