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
import static org.opendaylight.util.packet.ProtocolUtils.verifyNotNull;

import java.util.Arrays;

import org.opendaylight.util.net.TcpUdpPort;


/**
 * TCP data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>TCP source port</li>
 * <li>TCP destination port</li>
 * </ul>
 * @author Frank Wood
 */
public class Tcp implements Protocol {

    private static final String E_BAD_OPTION_LEN =
            "Option length must be a multiple of 32-bits (padded)";
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {
        
        private TcpUdpPort dstPort;
        private TcpUdpPort srcPort;
        private long seqNum;
        private long ackNum;
        private int flags;
        private int winSize;
        private int checkSum;
        private int urgentPtr;
        private byte[] options = EMPTY_BYTES;
        private int hdrLen;
        
        private Data() {}
        
        private Data(Data data) {
            dstPort = data.dstPort;
            srcPort = data.srcPort;
            seqNum = data.seqNum;
            ackNum = data.ackNum;
            hdrLen = data.hdrLen;
            flags = data.flags;
            winSize = data.winSize;
            checkSum = data.checkSum;
            urgentPtr = data.urgentPtr;
            options = Arrays.copyOf(data.options, data.options.length);
        }

        @Override
        public void verify() {
            verifyNotNull(dstPort, srcPort, options);
            
            if ((options.length % 4) != 0)
                throw new ProtocolException(E_BAD_OPTION_LEN);            
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * dstPort = 0
         * srcPort = 0
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param tcp builder is initialed from this protocol's data
         */
        public Builder(Tcp tcp) {
            this.data = new Data(tcp.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Tcp build() {
            return new Tcp(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Tcp buildNoVerify() {
            return new Tcp(data, false);
        }
        
        /**
         * Internally used by the package to set the decoded header length.
         * 
         * @param hdrLen decoded header length
         * @return this instance
         */
        Builder hdrLen(int hdrLen) {
            data.hdrLen = hdrLen;
            return this;
        }

        /**
         * Internally used by the package to set the checksum value.
         * 
         * @param checkSum decoded checksum
         * @return this instance
         */
        Builder checkSum(int checkSum) {
            data.checkSum = checkSum;
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
         * Sets the sequence number.
         * 
         * @param seqNum sequence number
         * @return this instance
         */
        public Builder seqNum(long seqNum) {
            data.seqNum = seqNum;
            return this;
        }
        
        /**
         * Sets the acknowledgment number.
         * 
         * @param ackNum acknowledgment number
         * @return this instance
         */
        public Builder ackNum(long ackNum) {
            data.ackNum = ackNum;
            return this;
        }

        /**
         * Sets the flags.
         * 
         * @param flags flag bits
         * @return this instance
         */
        public Builder flags(int flags) {
            data.flags = flags;
            return this;
        }
        
        /**
         * Sets the window size.
         * 
         * @param winSize window size
         * @return this instance
         */
        public Builder winSize(int winSize) {
            data.winSize = winSize;
            return this;
        }
        
        /**
         * Sets the urgent pointer.
         * 
         * @param urgentPtr urgent pointer
         * @return this instance
         */
        public Builder urgentPtr(int urgentPtr) {
            data.urgentPtr = urgentPtr;
            return this;
        }
        
        /**
         * Sets the options.
         * 
         * @param options options byte array
         * @return this instance
         */
        public Builder options(byte[] options) {
            data.options = options;
            return this;
        }
    }      
    
    private Data data;
    
    private Tcp(Data data, boolean verify) {
        this.data = new Data(data);
        verify(verify, this, this.data);
    }    
    
    @Override
    public ProtocolId id() {
        return ProtocolId.TCP;
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
     * Returns the source port.
     * 
     * @return the source port
     */
    public TcpUdpPort srcPort() {
        return data.srcPort;
    }
    
    /**
     * Returns the sequence number.
     * 
     * @return the sequence number
     */
    public long seqNum() {
        return data.seqNum;
    }    

    /**
     * Returns the acknowledgment number.
     * 
     * @return the acknowledgment number
     */
    public long ackNum() {
        return data.ackNum;
    }
    
    /**
     * Returns the decoded header length.
     * 
     * @return the decoded header length
     */
    public int hdrLen() {
        return data.hdrLen;
    }
    
    /**
     * Returns the flag bits.
     * 
     * @return the flag bits
     */
    public int flags() {
        return data.flags;
    }

    /**
     * Returns the window size.
     * 
     * @return the window size
     */
    public int winSize() {
        return data.winSize;
    }
    
    /**
     * Returns the decoded checksum.
     * 
     * @return the decoded checksum
     */
    public int checkSum() {
        return data.checkSum;
    }

    /**
     * Returns the urgent pointer.
     * 
     * @return the urgent pointer
     */
    public int urgentPtr() {
        return data.urgentPtr;
    }
    
    /**
     * Internally used to return the options byte array.
     * 
     * @return the options byte array
     */
    byte[] optionsArray() {
        return data.options;
    }

    /**
     * Returns a copy of the options byte array.
     * 
     * @return the options byte array
     */
    public byte[] options() {
        return Arrays.copyOf(data.options, data.options.length);
    }
   
    @Override
    public String toString() {
        return id() + ",dPort=" + dstPort() + ",sPort=" + srcPort(); 
    }
    
    @Override
    public String toDebugString() {
        String eoli = EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("dstPort: ").append(dstPort())
            .append(eoli).append("srcPort: ").append(srcPort())
            .append(eoli).append("seqNum: ").append(seqNum())
            .append(eoli).append("ackNum: ").append(ackNum())
            .append(eoli).append("hdrLen (decode-only): ").append(hdrLen())
            .append(eoli).append("flags: ").append(flags())
            .append(eoli).append("winSize: ").append(winSize())
            .append(eoli).append("urgentPtr: ").append(urgentPtr())
            .append(eoli).append("checkSum (decode-only): ")
                         .append(hex(checkSum()))
            .append(eoli).append("options: ").append(hex(optionsArray()))
            ;
        return sb.toString();
    }
    
}
