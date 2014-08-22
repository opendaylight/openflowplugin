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
import java.util.EnumMap;

import org.opendaylight.util.net.MacAddress;


/**
 * LLDP data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * There are no OpenFlow match fields that reference this protocol.
 *
 * @author Frank Wood
 */
public class Lldp implements Protocol {

    /** Standards-defined destination MAC address used by this protocol */
    public static final MacAddress DST_MAC =
            MacAddress.valueOf("01:80:c2:00:00:0e");

    private static final LldpTlv[] NO_OPTIONS = new LldpTlv[0];
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {
        
        private LldpTlv chassisId;
        private LldpTlv portId;
        private LldpTlv ttl;
        private LldpTlv[] options = NO_OPTIONS;
        private LldpTlv[] privateOptions = NO_OPTIONS;
        
        private Data() {}
        
        private Data(Data data) {
            chassisId = data.chassisId;
            portId = data.portId;
            ttl = data.ttl;
            options = Arrays.copyOf(data.options, data.options.length);
            privateOptions = Arrays.copyOf(data.privateOptions,
                                           data.privateOptions.length);
        }

        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(chassisId, portId, ttl, options, privateOptions);
        }
    }
    
    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * options = NO_OPTIONS
         * privateOptions = NO_OPTIONS
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }

        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param lldp builder is initialed from this protocol's data
         */
        public Builder(Lldp lldp) {
            this.data = new Data(lldp.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Lldp build() {
            return new Lldp(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Lldp buildNoVerify() {
            return new Lldp(data, false);
        }
        
        /**
         * Sets the chassis ID.
         * 
         * @param chassisId chassis ID
         * @return this instance
         */
        public Builder chassisId(LldpTlv chassisId) {
            data.chassisId = chassisId;
            return this;
        }

        /**
         * Sets the port ID.
         * 
         * @param portId port ID
         * @return this instance
         */
        public Builder portId(LldpTlv portId) {
            data.portId = portId;
            return this;
        }
        
        /**
         * Sets the time to live value.
         * 
         * @param ttl time to live value
         * @return this instance
         */
        public Builder ttl(LldpTlv ttl) {
            data.ttl = ttl;
            return this;
        }
        
        /**
         * Sets the options array. Note that there is a separate method
         * {@link Builder#privateOptions(LldpTlv[])} used to set the private
         * options.
         * 
         * @param options array of options
         * @return this instance
         */
        public Builder options(LldpTlv[] options) {
            data.options = options;
            return this;
        }
        
        /**
         * Sets the private options array. Note that there is a separate method
         * {@link Builder#options(LldpTlv[])} used to set the non-private
         * options.
         * 
         * @param privateOptions array of private options
         * @return this instance
         */
        public Builder privateOptions(LldpTlv[] privateOptions) {
            data.privateOptions = privateOptions;
            return this;
        }
    }  

    private Data data;

    private Lldp(Data data, boolean verify) {
        this.data = new Data(data);
        ProtocolUtils.verify(verify, this, this.data);
    }

    /**
     * Internally used by sub-classes to create protocol instances.
     * 
     * @param lldp LLDP protocol instance
     */
    protected Lldp(Lldp lldp) {
        this(lldp.data, true);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.LLDP;
    }

    /**
     * Returns the chassis ID.
     * 
     * @return the chassis ID
     */
    public LldpTlv chassisId() {
        return data.chassisId;
    }
    
    /**
     * Returns the port ID.
     * 
     * @return the port ID
     */
    public LldpTlv portId() {
        return data.portId;
    }
    
    /**
     * Returns the time to live value.
     * 
     * @return the time to live value
     */
    public LldpTlv ttl() {
        return data.ttl;
    }
    
    /**
     * Internally used by the package to return the array of options.
     * 
     * @return the options array
     */
    LldpTlv[] optionsArray() {
        return data.options;
    }
    
    /**
     * Internally used by the package to return the array of private options.
     * 
     * @return the private options array
     */    
    LldpTlv[] privateOptionsArray() {
        return data.privateOptions;
    }    
    
    /**
     * Returns the options in the form of a map to allow access based on
     * {@link LldpTlv}. Note that there is a also a 
     * {@link Lldp#privateOptions()} method to return the private options.
     * 
     * @return the options in the form of a map
     */
    public EnumMap<LldpTlv.Type, LldpTlv> options() {
        EnumMap<LldpTlv.Type, LldpTlv> m =
            new EnumMap<LldpTlv.Type, LldpTlv>(LldpTlv.Type.class);
        
        for (LldpTlv tlv: data.options)
            m.put(tlv.type(), tlv);
        
        return m;
    }  
    
    /**
     * Returns a copy of the private options. Note that there is also a
     * {@link Lldp#options()} method to return the non-private options.
     * 
     * @return the private options
     */
    public LldpTlv[] privateOptions() {
        return Arrays.copyOf(data.privateOptions, data.privateOptions.length);
    }  
    
    @Override
    public String toString() {
        return id() + "," + chassisId() + "," + portId() + "," + ttl(); 
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("chassisId: ").append(chassisId())
            .append(eoli).append("portId: ").append(portId())
            .append(eoli).append("ttl: ").append(ttl())
            ;
        
        for (LldpTlv tlv: optionsArray())
            sb.append(eoli).append("option " + tlv);

        for (LldpTlv tlv: privateOptionsArray())
            sb.append(eoli).append("privateOption " + tlv);
        
        return sb.toString();
    }     
}
