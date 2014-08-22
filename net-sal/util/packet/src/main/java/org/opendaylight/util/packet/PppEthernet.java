/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.packet.PppEthernet.Code.SESSION_DATA;
import static org.opendaylight.util.packet.PppEthernet.PppProtocolId.PPP_IP;
import static org.opendaylight.util.packet.ProtocolUtils.hex;

import java.util.Arrays;


/**
 * PPP-over-Ethernet data store (immutable) and associated {@link Builder}
 * (mutable).
 * <p>
 * There are no OpenFlow match fields that reference this protocol.
 *
 * @author Frank Wood
 */
public class PppEthernet implements Protocol {

    /** Code types. */
    public enum Code implements ProtocolEnum {

        /** PPP Session stage/data. */
        SESSION_DATA(0x000),
        /** PPPoE Active Discovery Offer (PADO) packet. */
        PADO(0x007),
        /** PPPoE Active Discovery Initiation (PADI) packet. */
        PADI(0x009),
        /** PPPoE Active Discovery Request (PADR) packet. */
        PADR(0x019),
        /** PPPoE Active Discovery Session-confirmation (PADS) packet. */
        PADS(0x065),
        /** PPPoE Active Discovery Terminate (PADT) packet. */
        PADT(0x0a7),
        ;
        
        private int code;
        
        private Code(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static Code get(int code) {
            return ProtocolUtils.getEnum(Code.class, code, SESSION_DATA);
        }
    }

    /** PPP protocol IDs. */
    public enum PppProtocolId implements ProtocolEnum {
        
        /** Internet Protocol. */
        PPP_IP(0x00021),
        /* AppleTalk Protocol. */
        PPP_AT(0x00029),
        /* IPX Protocol. */
        PPP_IPX(0x0002b),
        /* VJ compressed TCP. */
        PPP_VJC_COMP(0x0002d),
        /* VJ uncompressed TCP. */
        PPP_VJC_UNCOMP(0x0002f),
        /* MultiLink Protocol. */
        PPP_MP(0x0003d),
        /* Internet Protocol Version 6. */
        PPP_IPV6(0x00057),
        /* Fragment compressed below bundle. */
        PPP_COMPFRAG(0x000fb),
        /* Compressed packet. */
        PPP_COMP(0x000fd),
        /* MultiProtocol Label Switching - Unicast. */
        PPP_MPLS_UC(0x00281),
        /* MultiProtocol Label Switching - Multicast. */
        PPP_MPLS_MC(0x00283),
        /* IP Control Protocol. */
        PPP_IPCP(0x08021),
        /* AppleTalk Control Protocol. */
        PPP_ATCP(0x08029),
        /* IPX Control Protocol. */
        PPP_IPXCP(0x0802b),
        /* IPv6 Control Protocol. */
        PPP_IPV6CP(0x08057),
        /* CCP at link level (below MP bundle). */
        PPP_CCPFRAG(0x080fb),
        /* Compression Control Protocol. */
        PPP_CCP(0x080fd),
        /* MPLS Control Protocol. */
        PPP_MPLSCP(0x080fd),
        /* Link Control Protocol. */
        PPP_LCP(0x0c021),
        /* Password Authentication Protocol. */
        PPP_PAP(0x0c023),
        /* Link Quality Report Protocol. */
        PPP_LQR(0x0c025),
        /** Cryptographic Handshake Authorization Protocol. */
        PPP_CHAP(0x0c223),
        /** Callback Control Protocol. */
        PPP_CBCP(0x0c029),
        ;
        
        private int code;
        
        private PppProtocolId(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static PppProtocolId get(int code) {
            return ProtocolUtils.getEnum(PppProtocolId.class, code, PPP_IP);
        }
    }
    
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {

        private int version = 1;
        private int type = 1;
        private Code code = SESSION_DATA;
        private int sessionId;
        private int len;
        private PppProtocolId pppProtocolId = PPP_IP;
        private byte[] bytes = ProtocolUtils.EMPTY_BYTES;
        
        private Data() {}
        
        private Data(Data data) {
            version = data.version;
            type = data.type;
            code = data.code;
            sessionId = data.sessionId;
            len = data.len;
            pppProtocolId = data.pppProtocolId;
            bytes = Arrays.copyOf(data.bytes, data.bytes.length);
        }
        
        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(pppProtocolId, bytes);
        }
    }
    
    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * version = 1
         * type = 1
         * code = SESSION_DATA
         * sessionId = 0
         * pppProtocolId = PPP_IP
         * bytes = EMPTY_BYTES
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param pppe builder is initialed from this protocol's data
         */
        public Builder(PppEthernet pppe) {
            this.data = new Data(pppe.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public PppEthernet build() {
            return new PppEthernet(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        PppEthernet buildNoVerify() {
            return new PppEthernet(data, false);
        }
        
        /**
         * Sets the PPPoE version.
         * 
         * @param version PPPoE version
         * @return this instance
         */
        public Builder version(int version) {
            data.version = version;
            return this;
        }
        
        /**
         * Sets the PPPoE type.
         * 
         * @param type PPPoE type
         * @return this instance
         */
        public Builder type(int type) {
            data.type = type;
            return this;
        }
        
        /**
         * Sets the PPPoE session ID.
         * 
         * @param sessionId PPPoE session ID  
         * @return this instance
         */
        public Builder sessionId(int sessionId) {
            data.sessionId = sessionId;
            return this;
        }
        
        /**
         * Sets the PPPoE code.
         * 
         * @param code PPPoE code
         * @return this instance
         */
        public Builder code(Code code) {
            data.code = code;
            return this;
        }

        /**
         * Internally used by the package to set the length when decoding.
         * 
         * @param len decode length
         * @return this instance
         */
        Builder len(int len) {
            data.len = len;
            return this;
        }
        
        /**
         * Sets the PPPoE protocol ID.
         * 
         * @param pppProtocolId PPPoE protocol ID
         * @return this instance
         */
        public Builder pppProtocolId(PppProtocolId pppProtocolId) {
            data.pppProtocolId = pppProtocolId;
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
    
    private PppEthernet(Data data, boolean verify) {
        this.data = data;
        ProtocolUtils.verify(verify, this, this.data);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.PPP_ETHERNET;
    }

    /**
     * Returns the PPPoE version.
     * 
     * @return the PPPoE version
     */
    public int version() {
        return data.version;
    }
    
    /**
     * Returns the PPPoE type.
     * 
     * @return the PPPoE type
     */
    public int type() {
        return data.type;
    }
    
    /**
     * Returns the PPPoE code.
     * 
     * @return the PPPoE code
     */
    public Code code() {
        return data.code;
    }
    
    /**
     * Returns the PPPoE session ID.
     * 
     * @return the PPPoE session ID
     */
    public int sessionId() {
        return data.sessionId;
    }
    
    /**
     * Returns the payload length after decoding.
     * 
     * @return the decoded payload length
     */
    public int len() {
        return data.len;
    }
    
    /**
     * Returns the PPPoE protocol ID.
     * 
     * @return the PPPoE protocol ID
     */
    public PppProtocolId pppProtocolId() {
        return data.pppProtocolId;
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
        return id() + "," + code(); 
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("version: ").append(version())
            .append(eoli).append("type: ").append(type())
            .append(eoli).append("code: ").append(code())
            .append(eoli).append("sessionId: ").append(sessionId())
            .append(eoli).append("len (decode-only): ").append(len())
            .append(eoli).append("pppProtocolId: ").append(pppProtocolId())
            .append(eoli).append("bytes: ").append(ProtocolUtils.hex(bytesArray()))
            ;
        return sb.toString();
    }
    
}
