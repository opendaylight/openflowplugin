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
 * DNS Payload (immutable) and its associated Builder to create DNS requests and
 * responses.
 *
 * @author Amar Bapu
 * @author Igor Kondrakhin
 * @author Frank Wood
 */
public class Dns implements Protocol {

    private static final String E_NOT_NULL = "invalid null value";
    private static final String E_INVALID_TX_ID = "invalid txId: ";
    
    private static final int MAX_TX_ID = 65535;

    /** Operation codes set by originator of a request (copied to response). */
    public enum OpCode implements ProtocolEnum {

        /** Standard query. */
        QUERY(0),
        /** Inverse query (Obsolete). */
        INV_QUERY(1),
        /** Server status request. */
        STATUS(2),
        /** Notify slave servers that data in the master has changed. */
        NOTIFY(4),
        /** Dynamic update. */
        UPDATE(5),
        ;
        
        private final int code;

        OpCode(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }

        static OpCode get(int code) {
            return ProtocolUtils.getEnum(OpCode.class, code, QUERY);
        }
    }    
    
    /** DNS class type codes. */
    public enum ClassType implements ProtocolEnum {

        /** Internet. */
        INTERNET(1),
        /** CSNET Class (Obsolete). */
        CSNET(2),
        /** CHAOS class. */
        CHAOS(3),
        /** Hesoid class [Dyer 87]. */
        HESOID(4),
        /** "Any" class. */
        ANY(255),
        ;
        
        private final int code;

        ClassType(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }

        static ClassType get(int code) {
            return ProtocolUtils.getEnum(ClassType.class, code, ANY);
        }
    }    
    
    /** Response codes. */
    public enum ResponseCode implements ProtocolEnum {

        /** No Error condition. */
        NO_ERROR(0),
        /** Name server was unable to interpret the query. */
        FORMAT_ERROR(1),
        /** Name server was unable to process the query - internal problem. */
        SERVER_FAILURE(2),
        /** Domain name in the query does not exist. */
        NAME_ERROR(3),
        /** Not implemented. */
        NOT_IMPL(4),
        /** Connection refused. */
        REFUSED(5),
        /** Domain name should not exist. */
        DOMAIN_NAME_SHOULD_NOT_EXIST(6),
        /** Resource record set should not exist. */
        RECORD_SHOULD_NOT_EXIST(7),
        /** Resource record set does not exist. */
        RECORD_DOES_NOT_EXIST(8),
        /** Not authoritative for zone. */
        NOT_AUTH(9),
        /** Name not in zone. */
        NOT_ZONE(10),
        /** Bad extension mechanism for DNS version. */
        BAD_VERSION(11),
        /** Bad signature. */
        BAD_SIG(12),
        /** Bad key. */
        BAD_KEY(13),
        /** Bad timestamp. */
        BAD_TIMESTAMP(14),
        ;
        
        private final int code;

        ResponseCode(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }

        static ResponseCode get(int code) {
            return ProtocolUtils.getEnum(ResponseCode.class, code, NO_ERROR);
        }
    }    
    
    /** Record types. */
    public enum RecordType implements ProtocolEnum {
        /** No record type (unknown place holder). */
        NONE(0),
        /** IPv4 address records. */
        A(1),
        /** Name service records. */
        NS(2),
        /** Obsolete. */
        MD(3),
        /** Obsolete. */
        MF(4),
        /** Canonical name records. */
        CNAME(5),
        /** Start of authority records. */
        SOA(6),
        /** Obsolete. */
        MB(7),
        /** Obsolete. */
        MG(8),
        /** Obsolete. */
        MR(9),
        /** Obsolete. */
        NULL(10),
        /** Obsolete. */
        WKS(11),
        /** Pointer records. */
        PTR(12),
        /** Host information records. */
        HINFO(13),
        /** Obsolete. */
        MINFO(14),
        /** Mail exchanger records. */
        MX(15),
        /** Text records. */
        TXT(16),
        /** Responsible person records. */
        RP(17),
        /** AFS database records. */
        AFSDB(18),
        /** Not in current use. */
        X25(19),
        /** Integrated services digital network records. */
        ISDN(20),
        /** Not in current use. */
        RT(21),
        /** Not in current use. */
        NSAP(22),
        /** Not in current use. */
        NSAPPTR(23),
        /** Signature records. */
        SIG(24),
        /** Key records. */
        KEY(25),
        /** Not in current use. */
        PX(26),
        /** Limited early version of the LOC records. */
        GPOS(27),
        /** IPv6 address records. */
        AAAA(28),
        /** Location records. */
        LOC(29),
        /** Replaced by Next-Secure. */
        NXT(30),
        /** Not in current use. */
        EID(31),
        /** Not in current use. */
        NIMLOC(32),
        /** Service locator records. */
        SRV(33),
        /** Not in current use. */
        ATMA(34),
        /** Naming authority pointer records. */
        NAPTR(35),
        /** Key eXchanger records. */
        KX(36),
        /** Certificate records. */
        CERT(37),
        A6(38),
        /** Delegation name records. */
        DNAME(39),
        /** Obsolete. */
        SINK(40),
        /** Option records. */
        OPT(41),
        /** Address prefix list records. */
        APL(42),
        /** Delegation signer records. */
        DS(43),
        /** SSH public key finger print records. */
        SSHFP(44),
        /** IPsec key records. */
        IPSECKEY(45),
        /** DNSSEC signature records. */
        RRSIG(46),
        /** Next-Secure records. */
        NSEC(47),
        /** DSN key records. */
        DNSKEY(48),
        /** DHCP identifier records. */
        DHCID(49),
        /** NSECv3 records. */
        NSEC3(50),
        /** NSECv3 parameter records. */
        NSEC3PARAM(51),
        /** TLSA certificate association records. */
        TLSA(52),
        /** Host identity protocol records. */
        HIP(55),
        /** Sender policy framework records. */
        SPF(99),
        /** Secret key records. */
        TKEY(249),
        /** Transaction signature records. */
        TSIG(250),
        /** Incremental zone transfer records. */
        IXFR(251),
        /** Authoritative zone transfer records. */
        AXFR(252),
        /** Obsolete. */
        MAILB(253),
        /** Obsolete. */
        MAILA(254),
        /** All cached records. */
        ANY(255),
        /** Certification authority authorization records. */
        CAA(257),
        ;
        
        private final int code;

        RecordType(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }

        static RecordType get(int code) {
            return ProtocolUtils.getEnum(RecordType.class, code, NONE);
        }
    }    
    
    /**
     * DNS record data store (immutable).
     */
    public static class Record {

        private final String name;
        private final RecordType recType;
        private final ClassType clsType;

        /**
         * Constructs a record. 
         *
         * @param name the record name ("www.hp.com")
         * @param recType the record type
         * @param clsType the class type
         */
        Record(String name, RecordType recType, ClassType clsType) {
            
            if (name == null || recType == null || clsType == null)
                throw new NullPointerException(E_NOT_NULL);
            
            this.name = name;
            this.recType = recType;
            this.clsType = clsType;
        }

        /**
         * Constructs a DNS record with {@link ClassType#INTERNET}.
         *
         * @param name the record name
         * @param recType the record type
         */
        Record(String name, RecordType recType) {
            this(name, recType, ClassType.INTERNET);
        }

        /**
         * Returns the name in the query.
         *
         * @return the name being queried
         */
        public String name() {
            return name;
        }

        /**
         * Returns the record type.
         *
         * @return the record type
         */
        public RecordType recType() {
            return recType;
        }

        /**
         * Returns the class of query.
         *
         * @return the query class
         */
        public ClassType clsType() {
            return clsType;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + recType.hashCode();
            result = 31 * result + clsType.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;

            if (o == null || getClass() != o.getClass())
                return false; 

            Record r = (Record) o;
            
            return name.equals(r.name)
                    && clsType == r.clsType && recType == r.recType;
        }

        @Override
        public String toString() {
            return name + ",rt=" + recType + ",ct=" + clsType;
        }
    }

    /**
     * DNS resource record data store (immutable).
     * 
     * @param <T> specific type of resource data (String, IpAddress, byte[]) 
     */
    public static class ResRecord<T> extends Record {

        private final long ttl;
        private final T data;

        /**
         * Constructs a resource record.
         *
         * @param name the domain name that was queried
         * @param recType the record type
         * @param clsType the class type
         * @param ttl the number of seconds the resource can be cached
         * @param data the resource data, can be null
         */
        public ResRecord(String name, RecordType recType, ClassType clsType,
                long ttl, T data) {
            super(name, recType, clsType);
            this.ttl = ttl;
            this.data = data;
        }

        /**
         * Constructs a resource record with {@link ClassType#INTERNET}.
         *
         * @param name the domain name that was queried
         * @param recType the record type.
         * @param ttl the number of seconds the resource can be cached.
         * @param data the resource data, can be null
         */
        public ResRecord(String name, RecordType recType, long ttl, T data) {
            this(name, recType, ClassType.INTERNET, ttl, data);
        }

        /**
         * Returns the number of seconds the resource can be cached (TTL).
         *
         * @return the TTL of this record
         */
        public long ttl() {
            return ttl;
        }

        /**
         * Returns the resource data or null if not supported.
         *
         * @return the resource data.
         */
        public T data() {
            return data;
        }
        
        @Override
        public String toString() {
            return super.toString() + ",ttl=" + ttl + ",data=" + data;
        }        
    }
    
    /**
     * DNS "MX" resource data (immutable).
     */
    public static class MxData {
        
        private final String name;
        private final int pref; 
        
        public MxData(int pref, String name) {
            this.name = name;
            this.pref = pref;
        }
        
        /**
         * Returns the name.
         * 
         * @return the name 
         */
        public String name() {
            return name;
        }

        /**
         * Returns the preference.
         * 
         * @return the preference.
         */
        public int pref() {
            return pref;
        }

        @Override
        public String toString() { 
            return "mx[n=" + name + ",pref=" + pref + "]";
        }        
    }
    
    /**
     * DNS "Start Of Authority" resource data (immutable).
     */
    public static class SoaData {
        
        private final String nameServer;
        private final String email; 
        private final long serial;
        private final long refreshSecs;
        private final long retrySecs;
        private final long expireSecs;
        private final long minTtl;
        
        /**
         * Creates "Start of Authority" resource data.
         * 
         * @param nameServer the name server
         * @param email the email name
         * @param serial the serial number
         * @param refreshSecs the refresh interval seconds
         * @param retrySecs the retry interval seconds
         * @param expireSecs the expiration time in seconds
         * @param minTtl the minimum time-to-live
         */
        public SoaData(String nameServer, String email, long serial,
                       long refreshSecs, long retrySecs, long expireSecs,
                       long minTtl) {
            this.nameServer = nameServer;
            this.email = email;
            this.serial = serial;
            this.refreshSecs = refreshSecs;
            this.retrySecs = retrySecs;
            this.expireSecs = expireSecs;
            this.minTtl = minTtl;
        }
        
        /**
         * Returns the name server that will respond authoritatively for
         * the domain.
         * 
         * @return the authoritative name server 
         */
        public String nameServer() {
            return nameServer;
        }

        /**
         * Returns Email address of the person responsible for this zone 
         * and to which email may be sent to report errors or problems.
         * 
         * @return the email address of the host-master.
         */
        public String email() {
            return email;
        }

        /**
         * Returns the serial number of this record.
         * 
         * @return the serial number.
         */
        public long serial() {
            return serial;
        }

        /**
         * Returns the time interval in seconds when the slave will try to 
         * refresh the zone from the master.
         * 
         * @return the refresh interval
         */
        public long refreshSecs() {
            return refreshSecs;
        }

        /**
         * Returns the time between retries if the slave (secondary) fails 
         * to contact the master when refresh (above) has expired.
         * 
         * @return the retry interval
         */
        public long retrySecs() {
            return retrySecs;
        }

        /**
         * Returns the expiration time of when the zone data is no longer
         * authoritative.
         * 
         * @return the expiration time
         */
        public long expireSecs() {
            return expireSecs;
        }

        /**
         * Returns the negative caching time (RFC 2308: the time a NAME ERROR/
         * NXDOMAIN result may be cached by any resolver).
         * 
         * @return the negative caching time in seconds
         */
        public long minTtl() {
            return minTtl;
        }

        @Override
        public String toString() { 
            return "soa[ns=" + nameServer + ",mail=" + email + "]";
        }        
    }

    /**
     * DNS "Signature" resource data (immutable).
     */
    public static class SigData {
        
        /** Algorithm types. */
        public enum Algorithm implements ProtocolEnum {
            /** Default algorithm type: RSA/SHA1. */
            RSA_SHA1(5),
            ;

            private final int code;

            Algorithm(int code) {
                this.code = code;
            }

            @Override
            public int code() {
                return code;
            }

            static Algorithm get(int code) {
                return ProtocolUtils.getEnum(Algorithm.class, code, RSA_SHA1);
            }
            
        }
        
        private final RecordType typeCovered;
        private final Algorithm algorithm; 
        private final int labels;
        private final long origTtl;
        private final long expirationTs;
        private final long signedTs;
        private final int signingId;
        private final String signersName;
        private final byte[] signature;
        
        /**
         * Creates "Signature" resource data.
         * 
         * @param typeCovered the type covered
         * @param algorithm the algorithm type
         * @param labels the number of labels
         * @param origTtl the original time-to-live value
         * @param expirationTs the expiration timestamp
         * @param signedTs the signed timestamp
         * @param signingId the signing ID
         * @param signersName the signer's name
         * @param signature the signature bytes
         */
        public SigData(RecordType typeCovered, Algorithm algorithm,
                       int labels, long origTtl, long expirationTs,
                       long signedTs, int signingId, String signersName,
                       byte[] signature) {
            this.typeCovered = typeCovered;
            this.algorithm = algorithm;
            this.labels = labels;
            this.origTtl = origTtl;
            this.expirationTs = expirationTs;
            this.signedTs = signedTs;
            this.signingId = signingId;
            this.signersName = signersName;
            this.signature = Arrays.copyOf(signature, signature.length);
        }
        
        /**
         * Returns the type covered in this record.
         * 
         * @return the type covered 
         */
        public RecordType typeCovered() {
            return typeCovered;
        }

        /**
         * Returns the algorithm type.
         * 
         * @return the algorithm type
         */
        public Algorithm algorithm() {
            return algorithm;
        }

        /**
         * Returns the number of labels.
         * 
         * @return the number of labels
         */
        public int labels() {
            return labels;
        }

        /**
         * Returns the original time-to-live value.
         * 
         * @return the original time-to-live value
         */
        public long origTtl() {
            return origTtl;
        }

        /**
         * Returns the expiration timestamp.
         * 
         * @return the expiration timestamp
         */
        public long expirationTs() {
            return expirationTs;
        }

        /**
         * Returns the signed timestamp.
         * 
         * @return the signed timestamp
         */
        public long signedTs() {
            return signedTs;
        }

        /**
         * Returns the signing ID.
         * 
         * @return the signing ID
         */
        public int signingId() {
            return signingId;
        }
        
        /**
         * Returns the signer's name.
         * 
         * @return the signer's name
         */
        public String signersName() {
            return signersName;
        }
        
        /**
         * Returns a copy of the signature bytes.
         * 
         * @return the signature bytes
         */
        public byte[] signature() {
            return Arrays.copyOf(signature, signature.length);
        }
        
        /**
         * Returns the signature bytes.
         * 
         * @return the signature bytes
         */
        byte[] signatureBytes() {
            return signature;
        }
        
        @Override
        public String toString() { 
            return "sig[tc=" + typeCovered + ",n=" + signersName + "]";
        }        
    }   
    
    /**
     * DNS "NextSECure" resource data (immutable).
     */
    public static class NextSecData {
        
        private final String domainName;
        private final byte[] typeBitmap; 
        
        /**
         * Creates "NextSECure" resource data.
         * 
         * @param domainName the domain name
         * @param typeBitmap the type bitmap bytes
         */
        public NextSecData(String domainName, byte[] typeBitmap) {
            this.domainName = domainName;
            this.typeBitmap = Arrays.copyOf(typeBitmap, typeBitmap.length);
        }
        
        /**
         * Returns the domain name.
         * 
         * @return the domain name 
         */
        public String domainName() {
            return domainName;
        }

        /**
         * Returns a copy of the type bitmap bytes.
         * 
         * @return the bitmap bytes
         */
        public byte[] typeBitmap() {
            return Arrays.copyOf(typeBitmap, typeBitmap.length);
        }

        @Override
        public String toString() { 
            return "nesc[n=" + domainName + "]";
        }        
    }       
    
    static final Record[] NO_REQUESTS = new Record[0];
    static final ResRecord<?>[] NO_RESPONSES = new ResRecord<?>[0];
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {
        
        // Transaction ID created by the message originator.
        private int txId;
        
        // Is this a query or a response header?
        private boolean query;
        
        // Specifies the kind of query.
        private OpCode opCode = OpCode.QUERY;
        
        // Is the responder authoritative for the domain name in the query? 
        private boolean responderAuth;
        
        // Does the total number of responses exceed the UDP datagram?
        private boolean trunc;
        
        // Is this a recursive query?
        private boolean recurDesired;
        
        // Can the server handle recursive queries?
        private boolean svrRecurAvail;

        // Is all data included in the answer and authority sections of a
        // response authenticated by the server?
        private boolean authData;
        
        // Checking disabled?
        private boolean checkDisabled;
        
        // The return code.
        private ResponseCode respCode = ResponseCode.NO_ERROR;

        private Record[] queries = NO_REQUESTS;
        private ResRecord<?>[] answers = NO_RESPONSES;
        private ResRecord<?>[] authorities = NO_RESPONSES;
        private ResRecord<?>[] additionals = NO_RESPONSES;

        private Data() {}
        
        private Data(Data data) {
            txId = data.txId;
            query = data.query;
            opCode = data.opCode;
            responderAuth = data.responderAuth;
            trunc = data.trunc;
            recurDesired = data.recurDesired;
            svrRecurAvail = data.svrRecurAvail;
            authData = data.authData;
            checkDisabled = data.checkDisabled;
            respCode = data.respCode;
            queries = Arrays.copyOf(data.queries, data.queries.length);
            answers = Arrays.copyOf(data.answers, data.answers.length);
            authorities =
                    Arrays.copyOf(data.authorities, data.authorities.length);
            additionals =
                    Arrays.copyOf(data.additionals, data.additionals.length);
        }
        
        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(opCode, respCode, queries, answers, authorities,
                                        additionals);
            
            if (txId > MAX_TX_ID || txId < 0)
                throw new ProtocolException(E_INVALID_TX_ID + txId);
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {

        private Data data;

        /**
         * Create a new builder using the defaults:
         * <pre>
         * txId = 0
         * query = false
         * opCode = QUERY
         * responderAuth = false
         * trunc = false
         * recurDesired = false
         * svrRecurAvail = false
         * authData = false
         * checkDisabled = false
         * retCode = NO_ERROR
         * queries = NO_REQUESTS
         * answers = NO_RESPONSES
         * authorities = NO_RESPONSES
         * additionals = NO_RESPONSES
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param dns builder is initialed from this protocol's data
         */
        public Builder(Dns dns) {
            this.data = new Data(dns.data);
        }

        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Dns build() {
            return new Dns(data, true);
        }
        
        /**
         * Sets the transaction ID.
         *
         * @param txId the transaction ID of this query or response
         * @return this instance
         */
        public Builder txId(int txId) {
            data.txId = txId;
            return this;
        }

        /**
         * Sets the query flag.
         *
         * @param query the query flag.
         * @return this instance
         */
        public Builder query(boolean query) {
            data.query = query;
            return this;
        }

        /**
         * Sets the opcode.
         *
         * @param opCode the DNS opcode
         * @return this instance
         */
        public Builder opCode(OpCode opCode) {
            data.opCode = opCode;
            return this;
        }

        /**
         * Sets the authoritative flag. This is only meaningful in a response.
         *
         * @param responderAuth the responder authoritative flag
         * @return this instance
         */
        public Builder responderAuth(boolean responderAuth) {
            data.responderAuth = responderAuth;
            return this;
        }

        /**
         * Sets the truncated flag.
         *
         * @param trunc the truncated flag
         * @return this instance
         */
        public Builder trunc(boolean trunc) {
            data.trunc = trunc;
            return this;
        }

        /**
         * Specifies whether recursion is desired for a query.
         *
         * @param recurDesired the recursion is desired flag
         * @return this instance
         */
        public Builder recurDesired(boolean recurDesired) {
            data.recurDesired = recurDesired;
            return this;
        }

        /**
         * Sets whether recursion is available on the server.
         *
         * @param svrRecurAvail the server recursion is available flag
         * @return this instance
         */
        public Builder svrRecurAvail(boolean svrRecurAvail) {
            data.svrRecurAvail = svrRecurAvail;
            return this;
        }
        
        /**
         * Sets whether the response data is authorized.
         *
         * @param authData the response data is authorized flag
         * @return this instance
         */
        public Builder authData(boolean authData) {
            data.authData = authData;
            return this;
        }
        
        /**
         * Sets the check disabled flag.
         *
         * @param checkDisabled the check disabled flag
         * @return this instance
         */
        public Builder checkDisabled(boolean checkDisabled) {
            data.checkDisabled = checkDisabled;
            return this;
        }

        /**
         * Sets the response code.
         *
         * @param respCode the response code
         * @return this instance
         */
        public Builder respCode(ResponseCode respCode) {
            data.respCode = respCode;
            return this;
        }

        /**
         * Sets the queries array.
         *
         * @param queries array of query requests
         * @return this instance
         */
        public Builder queries(Record... queries) {
            data.queries = queries;
            return this;
        }
        
        /**
         * Sets up the query array for a single name query.
         *
         * @param name the name text to query
         * @param rt the record type
         * @return this instance
         */
        public Builder query(String name, RecordType rt) {
            data.queries = new Record[] {new Record(name, rt)};
            return this;
        }        
        
        /**
         * Sets the answers array.
         *
         * @param answers array of answers responses
         * @return this instance
         */
        public Builder answers(ResRecord<?>... answers) {
            data.answers = answers;
            return this;
        }
        
        /**
         * Sets the answers array for a single resource record.
         *
         * @param name the name text
         * @param rt the resource record type
         * @param ttl the time to live in seconds
         * @param d the resource data
         * @return this instance
         */
        public <T> Builder answer(String name, RecordType rt, int ttl, T d) {
            data.answers =
                    new ResRecord[] {new ResRecord<T>(name, rt, ttl, d)};
            return this;
        } 

        /**
         * Sets the authorities array.
         *
         * @param authorities array of authority responses
         * @return this instance
         */
        public Builder authorities(ResRecord<?>... authorities) {
            data.authorities = authorities;
            return this;
        }        

        /**
         * Sets the authorities array for a single resource record.
         *
         * @param name the name text
         * @param rt the resource record type
         * @param ttl the time to live in seconds
         * @param d the resource data
         * @return this instance
         */
        public <T> Builder authority(String name, RecordType rt, int ttl, T d) {
            data.authorities =
                    new ResRecord[] {new ResRecord<T>(name, rt, ttl, d)};
            return this;
        }         
        
        /**
         * Sets the additionals array.
         *
         * @param additionals array of additional responses
         * @return this instance
         */
        public Builder additionals(ResRecord<?>... additionals) {
            data.additionals = additionals;
            return this;
        }  
        
        /**
         * Sets the additionals array for a single resource record.
         *
         * @param name the name text
         * @param rt the resource record type
         * @param ttl the time to live in seconds
         * @param d the resource data
         * @return this instance
         */
        public <T> Builder additional(String name, RecordType rt, int ttl,
                                      T d) {
            data.additionals =
                    new ResRecord[] {new ResRecord<T>(name, rt, ttl, d)};
            return this;
        } 
        
        /**
         * A convenience method to construct a response object for the given
         * query.
         *
         * @param query the original DNS query request
         * @return this instance
         */
        public Builder responseFor(Dns query) {
            data.query = false;
            data.txId = query.data.txId;
            data.recurDesired = query.data.recurDesired;
            data.queries = query.data.queries;
            return this;
        }
    }

    private Data data;
    
    private Dns(Data data, boolean verify) {
        this.data = new Data(data);
        ProtocolUtils.verify(verify, this, this.data);
    }   

    @Override
    public ProtocolId id() {
        return ProtocolId.DNS;
    }
    
    /**
     * Returns the transaction.
     *
     * @return the transaction ID.
     */
    public int txId() {
        return data.txId;
    }

    /**
     * Returns the query flag (true indicates that this is a query request).
     *
     * @return the query flag
     */
    public boolean query() {
        return data.query;
    }

    /**
     * Returns the operation code.
     *
     * @return the operation code.
     */
    public OpCode opCode() {
        return data.opCode;
    }

    /**
     * Returns the recursion desired flag.
     *
     * @return the recursion desired flag value
     */
    public boolean recurDesired() {
        return data.recurDesired;
    }

    /**
     * Returns the server recursion available flag.
     *
     * @return the server recursion available flag.
     */
    public boolean svrRecurAvail() {
        return data.svrRecurAvail;
    }

    /**
     * Returns the truncation flag which indicates that the query or response is
     * truncated.
     *
     * @return the truncation flag value
     */
    public boolean trunc() {
        return data.trunc;
    }

    /**
     * Returns whether this response is an authoritative response.
     *
     * @return the authoritative flag value
     */
    public boolean responderAuth() {
        return data.responderAuth;
    }

    /**
     * Returns the response's data authentication flag.
     *
     * @return the data authentication flag
     */
    public boolean authData() {
        return data.authData;
    }
    
    /**
     * Returns the checking disabled flag.
     *
     * @return the checking disabled flag
     */
    public boolean checkDisabled() {
        return data.checkDisabled;
    }     
    
    /**
     * Returns the response code.
     *
     * @return the response code
     */
    public ResponseCode respCode() {
        return data.respCode;
    }

    /**
     * Internally used by the package to return the array of query requests.
     *
     * @return the queries array
     */
    Record[] queriesArray() {
        return data.queries;
    }

    /**
     * Returns a copy of the queries.
     * 
     * @return the queries array
     */
    public Record[] queries() {
        return Arrays.copyOf(data.queries, data.queries.length);
    }     
    
    /**
     * Internally used by the package to return the array of answer responses.
     *
     * @return the queries array
     */
    ResRecord<?>[] answersArray() {
        return data.answers;
    }

    /**
     * Returns a copy of the answers.
     * 
     * @return the answers array
     */
    public ResRecord<?>[] answers() {
        return Arrays.copyOf(data.answers, data.answers.length);
    }      
    
    /**
     * Internally used by the package to return the array of authorities
     * responses.
     *
     * @return the queries array
     */
    ResRecord<?>[] authoritiesArray() {
        return data.authorities;
    }
    
    /**
     * Returns a copy of the authorities array.
     * 
     * @return the authorities array
     */
    public ResRecord<?>[] authorities() {
        return Arrays.copyOf(data.authorities, data.authorities.length);
    }  
    
    /**
     * Internally used by the package to return the array of additional
     * responses.
     *
     * @return the queries array
     */
    ResRecord<?>[] additionalsArray() {
        return data.additionals;
    }    
    
    /**
     * Returns a copy of the additionals array.
     * 
     * @return the additionals array
     */
    public ResRecord<?>[] additionals() {
        return Arrays.copyOf(data.additionals, data.additionals.length);
    }  
    
    @Override
    public String toString() {
        return txId() + "," + query() + "," + opCode(); 
    }
    
    @Override
    public String toDebugString() {
        String spcs = spaces(ProtocolUtils.INDENT_SIZE);
        String eoli = ProtocolUtils.EOLI + spcs;
        StringBuilder sb = new StringBuilder().append(id()).append(":")
                .append(eoli).append("txId: ").append(txId())
                .append(eoli).append("query: ").append(query())
                .append(eoli).append("opCode: ").append(opCode())
                .append(eoli).append("trunc: ").append(trunc())
                .append(eoli).append("recurDesired: ").append(recurDesired())
                .append(eoli).append("svrRecurAvail: ").append(svrRecurAvail())
                .append(eoli).append("authData: ").append(authData())
                .append(eoli).append("checkDisabled: ").append(checkDisabled())
                .append(eoli).append("respCode: ").append(respCode());
        
        for (Record r: queriesArray())
            sb.append(eoli).append("query " + r);

        for (ResRecord<?> r: answersArray())
            sb.append(eoli).append("answer " + r);

        for (ResRecord<?> r: authoritiesArray())
            sb.append(eoli).append("authority " + r);

        for (ResRecord<?> r: additionalsArray())
            sb.append(eoli).append("additional " + r);

        return sb.toString();
    }
}
