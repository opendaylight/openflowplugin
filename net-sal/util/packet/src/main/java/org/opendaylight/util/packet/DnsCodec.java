/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.EMPTY;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.TcpUdpPort;
import org.opendaylight.util.packet.Dns.Builder;
import org.opendaylight.util.packet.Dns.ClassType;
import org.opendaylight.util.packet.Dns.MxData;
import org.opendaylight.util.packet.Dns.NextSecData;
import org.opendaylight.util.packet.Dns.OpCode;
import org.opendaylight.util.packet.Dns.Record;
import org.opendaylight.util.packet.Dns.RecordType;
import org.opendaylight.util.packet.Dns.ResRecord;
import org.opendaylight.util.packet.Dns.ResponseCode;
import org.opendaylight.util.packet.Dns.SigData;
import org.opendaylight.util.packet.Dns.SigData.Algorithm;
import org.opendaylight.util.packet.Dns.SoaData;


/**
 * DNS encoder and decoder.
 * 
 * DNS packets have a common structure described below:
 * <pre> 
 *  +------------+
 *  | Header     |
 *  +------------+
 *  | Questions  |
 *  +------------+
 *  | Answers    |
 *  +------------+
 *  | Authority  |
 *  +------------+
 *  | Additional |
 *  +------------+
 * </pre>
 * DNS Header is the set of common fields used in requests and responses:
 * <pre>
 *     0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
 *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 *   +                 TxID 16-bit                   +
 *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 *   |QR| Opcode    |AA|TC|RD|RA|    Z   |   RCode   |
 *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 *   |                 #Questions                    |
 *   +-----------------------------------------------+
 *   |                 #Answers                      |
 *   +-----------------------------------------------+
 *   |                 #Authority                    |
 *   +-----------------------------------------------+
 *   |                 #Additional                   |
 *   +-----------------------------------------------+
 *   |                 Questions[]                   |
 *   .                    ...                        .
 *   +-----------------------------------------------+
 *   |                 Answers[]                     |
 *   .                    ...                        .
 *   +-----------------------------------------------+
 *   |                 Authority[]                   |
 *   .                    ...                        .
 *   +-----------------------------------------------+
 *   |                 Additional []                 |
 *   .                    ...                        .
 *   +-----------------------------------------------+
 * </pre>
 * 
 * @author Amar Bapu
 * @author Igor Kondrakhin
 * @author Frank Wood
 */
class DnsCodec {

    private static final int MAX_LEN = 1500;
    
    private static final TcpUdpPort DNS_PORT = TcpUdpPort.udpPort(53);
    
    private static final int QUERY_MASK             = 0x08000;
    
    private static final int OPCODE_MASK            = 0x07800;
    private static final int OPCODE_BIT_SHIFT = 11;
    
    private static final int RESP_AUTH_MASK         = 0x00400;
    private static final int TRUNC_MASK             = 0x00200; 
    private static final int RECUR_DESIRED_MASK     = 0x00100;   
    private static final int SVR_RECUR_AVAIL_MASK   = 0x00080;
    
    private static final int AUTH_DATA_MASK         = 0x00020;
    private static final int CHECK_DISABLED_MASK    = 0x00010;
    private static final int RESPONSE_CODE_MASK     = 0x0000f;
    
    private static final int PTR_HIGH_BYTE_IND_MASK = 0x00c0;
    private static final int PTR_HIGH_BYTE_VAL_MASK = 0x003f;
    private static final int PTR_HIGH_BYTE_BIT_SHIFT = 8;
    
    private static final int NAME_PART_MAX_LEN = 63;
    
    private static final String NAME_DELIM = ".";
    private static final int NAME_END_BYTE = 0;
    
    private static final int MAX_RECORDS = 500;
    
    private static final int SIGNATURE_LEN = 128;
    
    private static final String E_NUM_RECORDS =
            "Invalid record count: ";
    
    private static final String E_INVALID_PTR_OFFSET =
            "Invalid name pointer offset: ";
    
    private static final String E_INVALID_NAME_PART_LEN =
            "Invalid name part length: ";
    
    /**
     * Returns true if the passed in source and destination ports signify that
     * the following payload can be decoded by this DNS decoder.
     *
     * @param srcPort source port (i.e. UDP source port)
     * @param dstPort destination port (i.e. UDP destination port)
     * @return true if the ports signify a DHCP payload
     */
    static boolean isDns(TcpUdpPort srcPort, TcpUdpPort dstPort) {
        return (srcPort == DNS_PORT || dstPort == DNS_PORT);
    }

    /**
     * Decodes the protocol.
     *
     * @param r the PacketReader source
     * @return DNS packet instance
     */
    static Dns decode(PacketReader r) {
        Builder b = new Dns.Builder();
        
        Map<Integer, String> nameReg = new HashMap<Integer, String>();
        
        try {
            int si = r.ri(); // the DNS protocol start index
            
            b.txId(r.readU16());
            
            int u16 = r.readU16();
            
            b.query((u16 & QUERY_MASK) == 0); // bit is 0 for query
            b.opCode(OpCode.get((u16 & OPCODE_MASK) >> OPCODE_BIT_SHIFT));
            b.responderAuth((u16 & RESP_AUTH_MASK) != 0);
            b.trunc((u16 & TRUNC_MASK) != 0);
            b.recurDesired((u16 & RECUR_DESIRED_MASK) != 0);
            b.svrRecurAvail((u16 & SVR_RECUR_AVAIL_MASK) != 0);
            b.authData((u16 & AUTH_DATA_MASK) != 0);
            b.checkDisabled((u16 & CHECK_DISABLED_MASK) != 0);
            b.respCode(ResponseCode.get(u16 & RESPONSE_CODE_MASK));
            
            int nQueries = r.readU16();
            int nAnswers = r.readU16();
            int nAuthority = r.readU16();
            int nAdditional = r.readU16();
            
            b.queries(decodeQueries(r, nQueries, si, nameReg));
            b.answers(decodeResRecords(r, nAnswers, si, nameReg));
            b.authorities(decodeResRecords(r, nAuthority, si, nameReg));
            b.additionals(decodeResRecords(r, nAdditional, si, nameReg));

        } catch (Exception e) {
            throw new ProtocolException(e, b.build(), r);
        }

        return b.build();
    }

    /**
     * Decode the queries.
     * 
     * @param r the packet reader
     * @param n the number of queries specified in the header
     * @param si the reader index of where this protocol started decoding
     * @param nameReg the name registry 
     * @return array of requests
     */
    private static Record[] decodeQueries(PacketReader r, int n, int si,
                                          Map<Integer, String> nameReg) {
        if (n > MAX_RECORDS)
            throw new ProtocolException(E_NUM_RECORDS + n + " queries");

        if (n == 0)
            return Dns.NO_REQUESTS;
        
        Record[] reqs = new Record[n];
        
        for (int i=0; i<n; i++) {
            String name = decodeName(r, si, nameReg);
            RecordType rt = RecordType.get(r.readU16());
            ClassType ct = ClassType.get(r.readU16());
            reqs[i] = new Record(name, rt, ct);
        }
        return reqs;
    }
    
    /**
     * Recursively decodes a name field that can be represented as either:
     * <ul>
     * <li>a sequence of labels ending in a zero octet</li>
     * <li>a pointer</li>
     * <li>a sequence of labels ending with a pointer</li>
     * </ul>
     * This code assumes that name pointer references cannot be forward. It is
     * valid to have a pointer that points to a name that was constructed with
     * a pointer.
     * <p>
     * The name registry will get updated as a side effect when this method
     * completes.
     * <p>
     * Algorithm:
     * <ol>
     * <li>stores the current reader index, this is where the name part will
     * be stored in the name registry (A->"x.y.z", B->"y.z", ...)</li>
     * <li>starts reading this name part (the length byte)</li>
     * <li>0 length indicates that we completed decoding, return an empty
     * string to stop the recursion</li>
     * <li>PTR length indicates that have a pointer to the rest of this name,
     * look up the name and return it, stopping the recursion</li>
     * <li>at this point we must have an ordinary name part, so we read the
     * string, and recur to decode the rest of the name.</li>
     * <li>at this point we have the rest of the name in "tail", if "tail" is
     * not empty we append it (preceded by a delimiter) to the original part
     * <li>finally, we register the name for this level of recursion at the
     * reader index of where we first read in the length byte, note we will only
     * register name parts in which the length is > 0 and not a PTR - which is
     * exactly what we want</li> 
     * </ol>
     * 
     * @param r the packet reader
     * @param si the reader index of where this protocol started decoding
     * @param nameReg the name registry
     * @return the name string
     */
    private static String decodeName(PacketReader r, int si,
                                     Map<Integer, String> nameReg) {
        
        // keep track of the name offset for registration
        int nameOffset = r.ri() - si;
        short len = r.readU8();
        
        if (len <= 0)
            return EMPTY;
        
        if ((len & PTR_HIGH_BYTE_IND_MASK) != 0) {
            int ptrOffset = len & PTR_HIGH_BYTE_VAL_MASK;
            ptrOffset = r.readU8() | (ptrOffset << PTR_HIGH_BYTE_BIT_SHIFT);

            String ptrName = nameReg.get(ptrOffset);
            if (null == ptrName)
                throw new ProtocolException(E_INVALID_PTR_OFFSET + ptrOffset);
            
            return ptrName;
        }

        String part = r.readString(len);
        String tail = decodeName(r, si, nameReg);

        String name = part;
        if (!tail.isEmpty())
            name = part + NAME_DELIM + tail;

        nameReg.put(nameOffset, name);
        return name;
    }
    
    /**
     * Decode the resource records for answers, authority and additional
     * response lists.
     * 
     * @param r the packet reader
     * @param n the number of records specified in the header
     * @param si the reader index of where this protocol started decoding
     * @param nameReg the name registry 
     * @return array of records
     */
    private static ResRecord<?>[] decodeResRecords(PacketReader r, int n,
            int si, Map<Integer, String> nameReg) {
        
        if (n > MAX_RECORDS)
            throw new ProtocolException(E_NUM_RECORDS + n +
                                        " response records");
        if (n == 0)
            return Dns.NO_RESPONSES;
        
        ResRecord<?>[] resps = new ResRecord<?>[n];
        
        for (int i=0; i<n; i++)
            resps[i] = decodeResRecord(r, si, nameReg);
        
        return resps;
    }
    
    /**
     * Decode a resource record.
     *
     * @param r PacketReader the packet source
     * @param si the reader index of where this protocol started decoding
     * @param nameReg the name registry 
     * @return the decoded response record
     */
    private static ResRecord<?> decodeResRecord(PacketReader r, int si,
            Map<Integer, String> nameReg) {
        
        String name = decodeName(r, si, nameReg);
        RecordType rt = RecordType.get(r.readU16());
        ClassType ct = ClassType.get(r.readU16());
        long ttl = r.readU32();
        int dataLen = r.readU16();
        
        ResRecord<?> rr = null;
        
        switch (rt) {
        
            case A:
                rr = new ResRecord<IpAddress>(name, rt, ct, ttl,
                        r.readIPv4Address());
                break;
                
            case AAAA:
                rr = new ResRecord<IpAddress>(name, rt, ct, ttl,
                        r.readIPv6Address());
                break;
                
            case CNAME:
            case NS:
            case PTR:
                rr = new ResRecord<String>(name, rt, ct, ttl,
                        decodeName(r, si, nameReg));
                break;
                
            case SOA:
                SoaData sd = new SoaData(decodeName(r, si, nameReg),
                        decodeName(r, si, nameReg),
                        r.readU32(), 
                        r.readU32(),
                        r.readU32(),
                        r.readU32(),
                        r.readU32());
                rr = new ResRecord<SoaData>(name, rt, ct, ttl, sd);
                break;
                
            case TXT:
                rr = new ResRecord<String>(name, rt, ct, ttl,
                        r.readString(dataLen));
                break;
                
            case MX:
                MxData mx = new MxData(r.readU16(),
                                       decodeName(r, si, nameReg));
                rr = new ResRecord<MxData>(name, rt, ct, ttl, mx);
                break;

            case RRSIG:
                SigData sig = new SigData(RecordType.get(r.readU16()),
                        Algorithm.get(r.readU8()),
                        r.readU8(),
                        r.readU32(),
                        r.readU32(),
                        r.readU32(),
                        r.readU16(),
                        decodeName(r, si, nameReg),
                        r.readBytes(SIGNATURE_LEN));
                rr = new ResRecord<SigData>(name, rt, ct, ttl, sig);
                break;
                
            case NSEC:
                int bitmapLen = dataLen; 
                int nsi = r.ri();
                String domainName = decodeName(r, si, nameReg);
                bitmapLen -= r.ri() - nsi;
                byte[] typeBitmap = r.readBytes(bitmapLen);
                NextSecData ns = new NextSecData(domainName, typeBitmap);
                rr = new ResRecord<NextSecData>(name, rt, ct, ttl, ns);
                break;                
                
            default:
                rr = new ResRecord<byte[]>(name, rt, ct, ttl,
                        r.readBytes(dataLen));
                break;
        }

        return rr;
    }

    /**
     * Encodes the protocol.
     *
     * @param dns protocol instance.
     * @return the packet writer with the encoded bytes.
     */
    static PacketWriter encode(Dns dns) {
        PacketWriter w = new PacketWriter(MAX_LEN);
        
        w.writeU16(dns.txId());
        
        int u16 = 0x00;
        u16 |= dns.query() ? 0 : QUERY_MASK; // bit is 0 for query
        
        u16 |= dns.opCode().code() << OPCODE_BIT_SHIFT;
        
        u16 |= dns.responderAuth() ? RESP_AUTH_MASK : 0;
        u16 |= dns.trunc() ? TRUNC_MASK : 0;
        u16 |= dns.recurDesired() ? RECUR_DESIRED_MASK : 0;
        u16 |= dns.svrRecurAvail() ? SVR_RECUR_AVAIL_MASK : 0;
        u16 |= dns.authData() ? AUTH_DATA_MASK : 0;
        u16 |= dns.checkDisabled() ? CHECK_DISABLED_MASK : 0;
        
        u16 |= dns.respCode().code() & RESPONSE_CODE_MASK;
        w.writeU16(u16);
        
        w.writeU16(dns.queriesArray().length);
        w.writeU16(dns.answersArray().length);
        w.writeU16(dns.authoritiesArray().length);
        w.writeU16(dns.additionalsArray().length);
        
        Map<String, Integer> nameReg = new HashMap<String, Integer>();

        for (Record r: dns.queriesArray()) {
            encodeName(r.name(), w, nameReg);
            w.writeU16(r.recType().code());
            w.writeU16(r.clsType().code());
        }

        for (ResRecord<?> r: dns.answersArray())
            encodeResRecord(r, w, nameReg);

        for (ResRecord<?> r: dns.authoritiesArray())
            encodeResRecord(r, w, nameReg);
        
        for (ResRecord<?> r: dns.additionalsArray())
            encodeResRecord(r, w, nameReg);

        return w;
    }

    /**
     * Recursively encodes the dot delimited name ("x.y.z").
     * 
     * @param name the name to encode
     * @param w the packet writer
     * @param nameReg the name registry (path -> writer index), if null no
     *        compression will be used
     */
    private static void encodeName(String name, PacketWriter w,
            Map<String, Integer> nameReg) {

        if (name.isEmpty())
            return;

        if (null != nameReg) {
            // check for an existing name (pointer DNS compression)
            Integer ptr = nameReg.get(name);
            if (null != ptr) {
                w.writeU16(PTR_HIGH_BYTE_IND_MASK << PTR_HIGH_BYTE_BIT_SHIFT
                           | ptr);
                // name is completely encoded at this point
                return;
            }
            
            // register the name for possible use later
            nameReg.put(name, w.wi());
        }
            
        int idx = name.indexOf(NAME_DELIM);
        if (idx <= 0) { // should never have .y
            encodeNamePart(name, w);
            w.writeU8(NAME_END_BYTE);
            // name is completely encoded at this point
            return;
        }

        // encode this part and use tail recursion to encode the rest
        String part = name.substring(0, idx);
        encodeNamePart(part, w);
        encodeName(name.substring(idx + 1), w, nameReg);
    }

    private static void encodeNamePart(String part, PacketWriter w) {
        int len = part.length();
        if (0 == len || len > NAME_PART_MAX_LEN)
            throw new ProtocolException(E_INVALID_NAME_PART_LEN + len);
        
        w.writeU8(len);
        w.writeString(part);
    }
       
    private static void encodeResRecord(ResRecord<?> r, PacketWriter w,
            Map<String, Integer> nameReg) {
 
        encodeName(r.name(), w, nameReg);
        w.writeU16(r.recType().code());
        w.writeU16(r.clsType().code());
        w.writeU32(r.ttl());
        
        // write a 0 for length now and record the current writer index
        int lengthWriteIdx = w.wi();
        w.writeU16(0);
        
        int dataStartIdx = w.wi();

        if (null != r.data()) {
            switch (r.recType()) {
                case A:
                case AAAA:
                    IpAddress addr = (IpAddress) r.data();
                    w.write(addr);
                    break;
                    
                case CNAME:
                case NS:
                case PTR:
                    String name = (String) r.data();
                    encodeName(name, w, nameReg);
                    break;
                    
                case SOA:
                    SoaData soa = (SoaData) r.data();
                    encodeName(soa.nameServer(), w, nameReg);
                    encodeName(soa.email(), w, nameReg);
                    w.writeU32(soa.serial());
                    w.writeU32(soa.refreshSecs());
                    w.writeU32(soa.retrySecs());
                    w.writeU32(soa.expireSecs());
                    w.writeU32(soa.minTtl());
                    break;
                    
                case TXT:
                    String s = (String) r.data();
                    w.writeString(s);
                    break;
                    
                case MX:
                    MxData mx = (MxData) r.data();
                    w.writeU16(mx.pref());
                    encodeName(mx.name(), w, nameReg);
                    break;
                    
                case RRSIG:
                    SigData sig = (SigData) r.data();
                    w.writeU16(sig.typeCovered().code());
                    w.writeU8(sig.algorithm().code());
                    w.writeU8(sig.labels());
                    w.writeU32(sig.origTtl());
                    w.writeU32(sig.expirationTs());
                    w.writeU32(sig.signedTs());
                    w.writeU16(sig.signingId());
                    // DNS name compression is not allowed per specification
                    encodeName(sig.signersName(), w, null);
                    w.writeBytes(sig.signature());
                    break;
                    
                case NSEC:
                    NextSecData nsd = (NextSecData) r.data();
                    // DNS name compression is not allowed per specification
                    encodeName(nsd.domainName(), w, null);
                    w.writeBytes(nsd.typeBitmap());
                    break;                      
                    
                default:
                    byte[] bytes = (byte[]) r.data();
                    w.writeBytes(bytes);
                    break;
            }
        }
        
        w.setU16(lengthWriteIdx, w.wi() - dataStartIdx);
    }
}
