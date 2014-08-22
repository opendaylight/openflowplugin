/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.ByteUtils.getU8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.StringUtils;


/**
 * Network protocol utilities.
 *
 * @author Frank Wood
 */
class ProtocolUtils {

    /** Constant used mostly to signify an empty payload. */
    static final byte[] EMPTY_BYTES = new byte[0];

    /** Number of spaces to indent for toDebugString() implementations. */
    static final int INDENT_SIZE = 2;

    /** Default indentation for toDebugString() implementations. */
    static final String INDENT = "  ";

    /** Combined EOL and spaced indent for toDebugString() implementations. */
    static final String EOLI = StringUtils.EOL + INDENT;    

    static final int U8_MASK = 0x0ff;
    static final int U8_BIT_SHIFT = 8;
    static final int U8_LEN = 1;
    
    static final int U16_MASK = 0x0ffff;
    static final int U16_BIT_SHIFT = 16;  
    static final int U16_LEN = 2;
    
    static final int U32_MASK = 0x0ffff;
    static final int U32_BIT_SHIFT = 32;  
    static final int U32_LEN = 4;
    
    private static final String E_NOT_NULL = "invalid null value";
    
    private static HashMap<Class<?>, HashMap<Integer, Enum<?>>> ENUM_MAP =
            new HashMap<Class<?>, HashMap<Integer, Enum<?>>>();

    private static final int PSEUDO_HDR_LEN_V4 = 12;
    private static final int PSEUDO_HDR_LEN_V6 = 40;
    private static final int PSEUDO_HDR_ZEROS_LEN_V6 = 3;
    
    
    /**
     * Returns the enumeration constant for the passed in code.
     * 
     * @param <T> enumeration type
     * @param cls enumeration class 
     * @param code target code to find matching enumeration
     * @param def default value to return if code is not found
     * @return the enumeration constant 
     */
    @SuppressWarnings("unchecked")
    static <T extends Enum<T> & ProtocolEnum> T getEnum(Class<T> cls,
                                                        int code,
                                                        T def) {
        HashMap<Integer, Enum<?>> m = ENUM_MAP.get(cls);
        if (null == m) {
            m = new HashMap<Integer, Enum<?>>();
            for (T enumConst: EnumSet.allOf(cls))
                m.put(enumConst.code(), enumConst);
            ENUM_MAP.put(cls, m);
        }
        Enum<?> enumConst = m.get(code);
        return (null != enumConst) ? (T) m.get(code) : def;
    }
    
    /**
     * Returns the enumeration constants for the passed in bitmask.
     * 
     * @param <T> enumeration type
     * @param cls enumeration class 
     * @param mask target mask to find matching enumerations
     * @return the enumeration constants 
     */
    static <T extends Enum<T> & ProtocolEnum> List<T>
            getEnums(Class<T> cls, int mask) {
        
        List<T> enums = new ArrayList<T>();
        for (T enumConst: EnumSet.allOf(cls)) {
            if (0 != (enumConst.code() & mask))
                enums.add(enumConst);
        }
        return enums;
    }
    
    /**
     * Returns true if the enumeration list is included in the mask.
     * 
     * @param <T> enumeration type
     * @param mask target mask to find matching enumerations
     * @param enumConsts enumeration constants
     * @return true if all the enumerations are specified in the mask  
     */
    static <T extends Enum<T> & ProtocolEnum> boolean
            has(int mask, T... enumConsts) {
        
        for (T ec: enumConsts) {
            if (0 == (ec.code() & mask))
                return false;
        }
        return true;
    }

    /**
     * Returns the bitmask for the enumeration constants.
     * 
     * @param <T> enumeration type
     * @param cls enumeration class 
     * @param enumConsts target enumeration constants to build the mask from
     * @return the bitmask 
     */
    static <T extends Enum<T> & ProtocolEnum> int
            getMask(Class<T> cls, T... enumConsts) {
        int mask = 0;
        for (T ec: enumConsts)
            mask |= ec.code();
        return mask;
    }
    
    /**
     * Returns true if the first array contains all the elements in the second.
     * 
     * @param <T> element type
     * @param a1 source array
     * @param a2 target array
     * @return true if the first array contains all the elements in the second 
     */
    static <T> boolean contains(T[] a1, T... a2) {
        List<T> a1List = Arrays.asList(a1);
        for (T e2: a2) {
            if (!a1List.contains(e2))
                return false;
        }
        return true;
    }
    
    /**
     * Calls the protocol data verify and re-throws if any exceptions.
     * 
     * @param verify if true perform the verification step, else NOP
     * @param p the protocol which contains the data we are verifying
     * @param d protocol data to verify
     * @throws ProtocolException if any constraint is violated
     */
    static void verify(boolean verify, Protocol p, ProtocolData d) {
        if (verify) {
            try {
                d.verify();
            } catch (ProtocolException e) {
                throw new ProtocolException(e, p);
            }
        }
    }    
    
    /**
     * Asserts that none of the passed in list of objects are null.
     * 
     * @param objs list of objects
     * @throws ProtocolException if any of the objects are null
     */
    static void verifyNotNull(Object... objs) {
        for (Object f: objs) {
            if (f == null)
                throw new ProtocolException(E_NOT_NULL);
        }
    }
    
    /**
     * Computes the partial checksum of the passed in buffer. Note that we are
     * not "finalizing" the checksum we are simply processing the buffer part.
     * 
     * @param startSum starting check sum value
     * @param w packet writer
     * @return the ending check sum value
     */
    static long checkSumPart(long startSum, PacketWriter w) {
        long sum = startSum;
        for (int i = 0; i < w.wi(); i += 2) {
            sum += getU8(w.array(), i) << 8;
            if (i+1 < w.wi())
                sum += getU8(w.array(), i+1);
        }
        return sum;
    }
    
    /**
     * Generates a U16 checksum based on the packet writer bytes. Iterates
     * over the bytes in the buffer by twos computing 1's complement on
     * each successive 16-bit value.
     * 
     * @param w packet writer
     * @return the U16 checksum
     */
    static int checkSumU16(PacketWriter w) {
        long sum = checkSumPart(0, w);
        return (int) (~((sum & U32_MASK) + (sum >> U16_BIT_SHIFT))) & U32_MASK;
    }
    
    /**
     * Generates a U16 checksum based on the packet writer bytes. Iterates
     * over the bytes in the pseudo header and header by twos computing 1's
     * complement on each successive 16-bit value. This code is similar to
     * what is done in the {@link EncodedPayload} and is used when it is known
     * that no payload exists.
     *
     * @param ipPseudoHdr IPv4/IPv6 pseudo header for the checksum calculation
     * @param hdr header for the checksum calculation (TCP, UDP)
     * @return the U16 checksum
     */
    static int checkSumU16(IpPseudoHdr ipPseudoHdr, PacketWriter hdr) {
        PacketWriter pwh = encodePseudoHdrIp(ipPseudoHdr);
        
        long sum = 0;
        sum = checkSumPart(sum, pwh);
        sum = checkSumPart(sum, hdr);
        
        return (int) (~((sum & U32_MASK) + (sum >> 16))) & U32_MASK;
    }     
    
    /**
     * Encodes an IPv4/IPv6 pseudo headed needed during encoding of TCP and
     * UDP protocols.
     * 
     * @param ipPseudoHdr IP pseudo header
     * @return the new packet writer consisting of the encoded pseudo header
     */
    static PacketWriter encodePseudoHdrIp(IpPseudoHdr ipPseudoHdr) {
        if (ipPseudoHdr.version() == IpVersion.V4) {
            PacketWriter w = new PacketWriter(PSEUDO_HDR_LEN_V4);
            w.write(ipPseudoHdr.srcAddr());
            w.write(ipPseudoHdr.dstAddr());
            w.writeU8(0);
            w.writeU8(ipPseudoHdr.type().code());
            w.writeU16(ipPseudoHdr.len());
            return w;
        }
            
        PacketWriter w = new PacketWriter(PSEUDO_HDR_LEN_V6);
        w.write(ipPseudoHdr.srcAddr());
        w.write(ipPseudoHdr.dstAddr());
        w.writeU32(ipPseudoHdr.len());
        w.writeZeros(PSEUDO_HDR_ZEROS_LEN_V6);
        w.writeU8(ipPseudoHdr.type().code());
        return w;
    }
    
    /**
     * Returns the given value formatted as a hex string.
     *
     * @param v the value
     * @return the value as a hex string
     */
    static String hex(int v) {
        return "0x" + Integer.toHexString(v);
    }

    /**
     * Returns the given value formatted as a hex string.
     *
     * @param v the value
     * @return the value as a hex string
     */
    static String hex(long v) {
        return "0x" + Long.toHexString(v);
    }
   
   /**
    * Returns the given value formatted as a hex string.
    *
    * @param b the byte array
    * @return the value as a hex string
    */
    static String hex(byte[] b) {
        return "0x" + ByteUtils.hex(b);
    }

    /**
     * Computes the CRC32C checksum (used in SCTP).
     */
    static class Crc32c {

        private final static int CRC32C_POLY = 0x1edc6f41;
        private final static int[] POLYS = new int[256];

        static {
            for (int i=255; i>0; i--)
                POLYS[i] = build(i);
        }

        private final static int build(int index) {
            int r = reflect(index);
            for (int i=0; i<8; i++) {
                if ((r & 0x080000000) != 0)
                    r = (r << 1) ^ CRC32C_POLY;
                else
                    r <<= 1;
            }
            return reflect(r);
        }
        
        private static int reflect(int b) {
            return
                ((b >>> 31) & 0x000000001) |
                ((b >>> 29) & 0x000000002) |
                ((b >>> 27) & 0x000000004) |
                ((b >>> 25) & 0x000000008) |
                ((b >>> 23) & 0x000000010) |
                ((b >>> 21) & 0x000000020) |
                ((b >>> 19) & 0x000000040) |
                ((b >>> 17) & 0x000000080) |
                ((b >>> 15) & 0x000000100) |
                ((b >>> 13) & 0x000000200) |
                ((b >>> 11) & 0x000000400) |
                ((b >>>  9) & 0x000000800) |
                ((b >>>  7) & 0x000001000) |
                ((b >>>  5) & 0x000002000) |
                ((b >>>  3) & 0x000004000) |
                ((b >>>  1) & 0x000008000) |
                ((b <<   1) & 0x000010000) |
                ((b <<   3) & 0x000020000) |
                ((b <<   5) & 0x000040000) |
                ((b <<   7) & 0x000080000) |
                ((b <<   9) & 0x000100000) |
                ((b <<  11) & 0x000200000) |
                ((b <<  13) & 0x000400000) |
                ((b <<  15) & 0x000800000) |
                ((b <<  17) & 0x001000000) |
                ((b <<  19) & 0x002000000) |
                ((b <<  21) & 0x004000000) |
                ((b <<  23) & 0x008000000) |
                ((b <<  25) & 0x010000000) |
                ((b <<  27) & 0x020000000) |
                ((b <<  29) & 0x040000000) |
                ((b <<  31) & 0x080000000);
        }

        public static long checkSumU32(byte[] b) {
            int value = 0;
            
            value = ( ((value >>> 24) & 0x0000000ff) |
                      ((value >>>  8) & 0x00000ff00) |
                      ((value <<   8) & 0x000ff0000) |
                      ((value <<  24) & 0x0ff000000) ) ^ 0x0ffffffff;
                
            for (int i=0; i<b.length; i++)
                value = ((value >>> 8) ^ POLYS[(value ^ (b[i])) & 0x0ff]);
                
            value = ( ((value >>> 24) & 0x0000000ff) |
                      ((value >>>  8) & 0x00000ff00) |
                      ((value <<   8) & 0x000ff0000) |
                      ((value <<  24) & 0x0ff000000) ) ^ 0x0ffffffff;
            
            return 0x0ffffffffL & value;
        }
    }

}
