/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.packet.ProtocolUtils.EOLI;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.opendaylight.util.packet.ProtocolUtils.verify;
import static org.opendaylight.util.packet.ProtocolUtils.verifyNotNull;

import org.opendaylight.util.net.TcpUdpPort;


/**
 * UDP data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>UDP source port</li>
 * <li>UDP destination port</li>
 * </ul>
 * @author Frank Wood
 */
public class Udp implements Protocol {

    /** Internal private data store. */
    private static class Data implements ProtocolData {
        
        private TcpUdpPort srcPort;
        private TcpUdpPort dstPort;
        private int len;
        private int checkSum;
        
        private Data() {}
        
        private Data(Data data) {
            srcPort = data.srcPort;
            dstPort = data.dstPort;
            len = data.len;
            checkSum = data.checkSum;
        }

        @Override
        public void verify() {
            verifyNotNull(dstPort, srcPort);
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * srcPort = null
         * dstPort = null
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param udp builder is initialed from this protocol's data
         */
        public Builder(Udp udp) {
            this.data = new Data(udp.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Udp build() {
            return new Udp(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Udp buildNoVerify() {
            return new Udp(data, false);
        }
        
        /**
         * Internally used by the package to set the decoded length field.
         * 
         * @param len decoded length field
         * @return this instance 
         */
        Builder len(int len) {
            data.len = len;
            return this;
        }
        
        /**
         * Internally used by the package to set the decoded checksum value.
         * 
         * @param checkSum checksum value
         * @return this instance
         */
        Builder checkSum(int checkSum) {
            data.checkSum = checkSum;
            return this;
        }

        /**
         * Sets the source port.
         * 
         * @param srcPort source port
         * @return this instance
         */
        public Builder srcPort(TcpUdpPort srcPort) {
            data.srcPort = srcPort;
            return this;
        }
        
        /**
         * Sets the destination port.
         * 
         * @param dstPort destination port
         * @return this instance
         */
        public Builder dstPort(TcpUdpPort dstPort) {
            data.dstPort = dstPort;
            return this;
        }
    }      
    
    private Data data;
    
    private Udp(Data data, boolean verify) {
        this.data = new Data(data);
        verify(verify, this, this.data);
    }    
    
    @Override
    public ProtocolId id() {
        return ProtocolId.UDP;
    }

    /**
     * Returns the source port.
     * 
     * @return the source port
     */
    public TcpUdpPort srcPort() {
        return data.srcPort;
    }
    
    /**
     * Returns the destination port.
     * 
     * @return the destination port
     */
    public TcpUdpPort dstPort() {
        return data.dstPort;
    }
    
    /**
     * Returns the decoded length field
     * 
     * @return the decoded length field
     */
    public int len() {
        return data.len;
    }
    
    /**
     * Returns the decoded checksum value.
     * 
     * @return the decoded checksum value
     */
    public int checkSum() {
        return data.checkSum;
    }
   
    @Override
    public String toString() {
        return id() + ",dPort=" + dstPort() + ",sPort=" + srcPort(); 
    }
    
    @Override
    public String toDebugString() {
        String eoli = EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("srcPort: ").append(srcPort())
            .append(eoli).append("dstPort: ").append(dstPort())
            .append(eoli).append("len (decode-only): ").append(len())
            .append(eoli).append("checkSum (decode-only): ")
                         .append(hex(checkSum()))
            ;
        return sb.toString();
    }
    
}
