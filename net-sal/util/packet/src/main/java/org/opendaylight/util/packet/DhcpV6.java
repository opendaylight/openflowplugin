/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.packet.DhcpV6.MessageType.SOLICIT;

import java.util.Arrays;


/**
 * DHCPv6 data store (immutable) add associated {@link Builder} (mutable).
 * <p>
 * There are no OpenFlow match fields that reference this protocol.
 *
 * @author Frank Wood
 */
public class DhcpV6 implements Protocol {

    /** DHCPv6 Message types. */
    public enum MessageType implements ProtocolEnum {
        
        /** Solicit message to locate servers. */
        SOLICIT(1),
        /** Server advertise. */
        ADVERTISE(2),
        /** Request message. */
        REQ(3),
        /** Confirm message. */
        CONFIRM(4),
        /** Renew message. */
        RENEW(5),
        /** Re-bind message. */
        REBIND(6),
        /** Replay message. */
        REPLY(7),
        /** Release message. */
        RELEASE(8),
        /** Decline message. */
        DECLINE(9),
        /** Reconfigure message. */
        RECONFIG(10),
        /** Information-request message. */
        INFO_REQ(11),
        /** Relay-forward message. */
        RELAY_FORWORD(12),
        /** Relay-reply message. */
        RELAY_REPLY(13),
        ;
        
        private int code;
        
        private MessageType(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static MessageType get(int code) {
            return ProtocolUtils.getEnum(MessageType.class, code, SOLICIT);
        }
    }    
    
    static final DhcpOptionV6[] NO_OPTIONS = new DhcpOptionV6[0];

    /** Internal private data store. */
    private static class Data implements ProtocolData {

        private MessageType msgType = SOLICIT;
        private int transId;
        private DhcpOptionV6[] options = NO_OPTIONS;
        
        private Data() {}
        
        private Data(Data data) {
            msgType = data.msgType;
            transId = data.transId;
            options = Arrays.copyOf(data.options, data.options.length);
        }
        
        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(msgType, options);
        }
    }

    /** Builder (mutable) used to create a new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * msgType = SOLICIT
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
        public Builder(DhcpV6 dhcp) {
            this.data = new Data(dhcp.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public DhcpV6 build() {
            return new DhcpV6(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        DhcpV6 buildNoVerify() {
            return new DhcpV6(data, false);
        }
        
        /**
         * Sets the message type.
         * 
         * @param msgType message type enumeration
         * @return this instance
         */
        public Builder msgType(MessageType msgType) {
            data.msgType = msgType;
            return this;
        }
        
        /**
         * Sets the transaction ID.
         * 
         * @param transId transaction ID
         * @return this instance
         */
        public Builder transId(int transId) {
            data.transId = transId;
            return this;
        }
        
        /**
         * Sets the options array.
         * 
         * @param options array of options
         * @return this instance
         */
        public Builder options(DhcpOptionV6[] options) {
            data.options = options;
            return this;
        }        
    }
    
    private Data data;
    
    private DhcpV6(Data data, boolean verify) {
        this.data = new Data(data);
        ProtocolUtils.verify(verify, this, this.data);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.DHCPV6;
    }

    /**
     * Returns the message type.
     * 
     * @return the message type
     */
    public MessageType msgType() {
        return data.msgType;
    }
    
    /**
     * Returns the transaction ID.
     * 
     * @return the transaction ID
     */
    public int transId() {
        return data.transId;
    }
    
    /**
     * Internally used by the package to return the array of options.
     * 
     * @return the options array
     */
    DhcpOptionV6[] optionsArray() {
        return data.options;
    }
    
    /**
     * Returns a copy of the options array.
     * 
     * @return the options array
     */
    public DhcpOptionV6[] options() {
        return Arrays.copyOf(data.options, data.options.length);
    }
    
    @Override
    public String toString() {
        return id() + "," + msgType() + ",transId=" + transId();
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("msgType: ").append(msgType())
            .append(eoli).append("transId: ").append(transId())
            ;
        
        for (DhcpOptionV6 o: optionsArray())
            sb.append(eoli).append("option " + o);
                
        return sb.toString();
    }    
    
}
