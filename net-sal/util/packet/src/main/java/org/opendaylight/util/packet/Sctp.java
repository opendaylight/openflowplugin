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
import static org.opendaylight.util.packet.ProtocolUtils.getEnum;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.opendaylight.util.packet.ProtocolUtils.verify;
import static org.opendaylight.util.packet.ProtocolUtils.verifyNotNull;
import static org.opendaylight.util.packet.Sctp.CheckSumType.CRC32C;

import java.util.Arrays;


/**
 * SCTP (Stream Control Transmission Protocol) data store (immutable) and
 * associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>SCTP source port</li>
 * <li>SCTP destination port</li>
 * </ul>
 * @author Frank Wood
 */
public class Sctp implements Protocol {

    /** Chunk data store (immutable). */
    public static class Chunk {
        
        /** Chunk types. */
        public enum Type implements ProtocolEnum {
            
            /** Payload data chunk. */
            DATA(0),
            /** Initiation chunk. */
            INIT(1),
            /** Initiation acknowledgment chunk. */
            INIT_ACK(2),
            /** Selective acknowledgment chunk. */
            SACK(3),
            /** Heartbeat request chunk. */
            HEARTBEAT(4),
            /** Heartbeat acknowledgment chunk. */
            HEARTBEAT_ACK(5),
            /** Abort chunk. */
            ABORT(6),
            /** Shutdown chunk. */
            SHUTDOWN(7),
            /** Shutdown acknowledgment chunk. */
            SHUTDOWN_ACK(8),
            /** Operation error chunk. */
            ERROR(9),
            /** State cookie chunk. */
            COOKIE_ECHO(10),
            /** Cookie acknowledgment chunk. */
            COOKIE_ACK(11),
            /** Explicit congestion notification echo (reserved) chunk. */
            ECNE(12),
            /** Congestion window reduced (reserved) chunk. */
            CWR(13),
            /** Shutdown complete chunk. */
            SHUTDOWN_COMPLETE(14),
            ;
            
            private int code;
            
            private Type(int code) {
                this.code = code;
            }
            
            @Override
            public int code() {
                return code;
            }
            
            static Type get(int code) {
                return getEnum(Type.class, code, DATA);
            }
        }
        
        private Type type;
        private int flags;
        private byte[] data;
        
        /**
         * Constructor to create a new chunk.
         * 
         * @param type chunk type
         * @param flags chunk flags
         * @param data chunk payload data
         */
        public Chunk(Type type, int flags, byte[] data) {
            this.type = type;
            this.flags = flags;
            this.data = Arrays.copyOf(data, data.length);
        }
        
        /**
         * Returns the chunk type.
         * 
         * @return the chunk type
         */
        public Type type() {
            return type;
        }
        
        /**
         * Returns the chunk flags.
         * 
         * @return the chunk flags
         */
        public int flags() {
            return flags;
        }
        
        /**
         * Internally used to return the chunk data.
         * 
         * @return the chunk data
         */
        byte[] dataArray() { return data; }

        /**
         * Returns a copy of the chunk data bytes.
         * 
         * @return the chunk data
         */
        public byte[] data() {
            return Arrays.copyOf(data, data.length);
        }
        
        @Override
        public String toString() {
            return "[" + type + ",nBytes=" + data.length + "]";
        }
    }
    
    private static final Chunk[] NO_CHUNKS = new Chunk[0];
    
    /** Checksum type needed during encoding. */
    public enum CheckSumType {
        /** Used to set the checksum algorithm to use CRC32c. */
        CRC32C,
        /** Used to set the checksum algorithm to use Adler32. */
        ADLER32, 
    }
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {
        
        private int srcPort;
        private int dstPort;
        private long verifyTag;
        private long checkSum;
        private CheckSumType checkSumType = CRC32C;
        private Chunk[] chunks = NO_CHUNKS;
        
        private Data() {}
        
        private Data(Data data) {
            srcPort = data.srcPort;
            dstPort = data.dstPort;
            verifyTag = data.verifyTag;
            checkSumType = data.checkSumType;
            checkSum = data.checkSum;
            chunks =  Arrays.copyOf(data.chunks, data.chunks.length);
        }

        @Override
        public void verify() {
            verifyNotNull( (Object)chunks );
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * checkSumType = CRC32C
         * chunks = NO_CHUNKS
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param sctp builder is initialed from this protocol's data
         */
        public Builder(Sctp sctp) {
            this.data = new Data(sctp.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Sctp build() {
            return new Sctp(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Sctp buildNoVerify() {
            return new Sctp(data, false);
        }
        
        /**
         * Internally used by the package to set the decoded checksum value.
         * 
         * @param checkSum decoded checksum value
         * @return this instance
         */
        Builder checkSum(long checkSum) {
            data.checkSum = checkSum;
            return this;
        }
        
        /**
         * Internally used by the package to set the decoded verify tag.
         * 
         * @param verifyTag decoded verify tag
         * @return this instance
         */
        Builder verifyTag(long verifyTag) {
            data.verifyTag = verifyTag;
            return this;
        }
        
        /**
         * Sets the checksum algorithm type.
         * 
         * @param checkSumType checksum algorithm type
         * @return this instance
         */
        public Builder checkSumType(CheckSumType checkSumType) {
            data.checkSumType = checkSumType;
            return this;
        }        

        /**
         * Sets the source port.
         * 
         * @param srcPort source port
         * @return this instance
         */
        public Builder srcPort(int srcPort) {
            data.srcPort = srcPort;
            return this;
        }
        
        /**
         * Sets the destination port.
         * 
         * @param dstPort destination port
         * @return this instance
         */
        public Builder dstPort(int dstPort) {
            data.dstPort = dstPort;
            return this;
        }
        
        /**
         * Sets the chunks array.
         * 
         * @param chunks chunks array
         * @return this instance
         */
        public Builder chunks(Chunk[] chunks) {
            data.chunks = chunks;
            return this;
        }
    }      
    
    private Data data;
    
    private Sctp(Data data, boolean verify) {
        this.data = new Data(data);
        verify(verify, this, this.data);
    }    
    
    @Override
    public ProtocolId id() {
        return ProtocolId.SCTP;
    }

    /**
     * Returns the source port.
     * 
     * @return the source port
     */
    public int srcPort() {
        return data.srcPort;
    }
    
    /**
     * Returns the destination port.
     * 
     * @return the destination port
     */
    public int dstPort() {
        return data.dstPort;
    }
    
    /**
     * Returns the checksum algorithm type.
     * 
     * @return the checksum algorithm type
     */
    public CheckSumType checkSumType() {
        return data.checkSumType;
    }

    /**
     * Returns the decoded checksum value.
     * 
     * @return the decoded checksum value
     */
    public long checkSum() {
        return data.checkSum;
    }

    /**
     * Returns the decoded verify tag.
     * 
     * @return the decoded verify tag
     */
    public long verifyTag() {
        return data.verifyTag;
    }

    /**
     * Returns a copy of the chunks array.
     * 
     * @return the chunks array
     */
    public Chunk[] chunks() {
        return Arrays.copyOf(data.chunks, data.chunks.length);
    }
    
    /**
     * Internally used by the package to return the chunks array. 
     * 
     * @return the chunks array
     */
    Chunk[] chunksArray() {
        return data.chunks;
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
            .append(eoli).append("checkSumType :").append(checkSumType())
            .append(eoli).append("checkSum (decode-only): ")
                         .append(hex(checkSum()))
            ;
        
        for (Chunk c: chunksArray())
            sb.append(eoli).append("chunk " + c);

        return sb.toString();
    }
    
}
