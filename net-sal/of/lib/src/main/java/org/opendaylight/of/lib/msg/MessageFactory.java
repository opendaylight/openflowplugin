/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.test.UnitTestSupport;
import org.opendaylight.util.test.UnitTestSupportAdapter;
import org.opendaylight.util.test.UnitTestSupportProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.opendaylight.of.lib.CommonUtils.notMutable;
import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.*;

/**
 * Provides facilities for parsing, creating, copying and encoding
 * OpenFlow messages.
 *
 * @author Simon Hunt
 * @author Radhika Hegde
 */
public class MessageFactory extends AbstractFactory {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MessageFactory.class, "messageFactory");

    private static final String MSG_STRICT_PARSING = RES
            .getString("msg_strict_parsing");
    private static final String OFF = "OFF";
    private static final String ON = "ON";

    private static final Logger LOG = LoggerFactory.getLogger(MessageFactory.class);

    /** First XID value to be assigned will be 101. */
    static final long BASE_XID = 100;

    /** Last XID value to be assigned before wrapping (255 below the max). */
    static final long LAST_XID = 0xffffff00L;

    /** Our transaction ID counter. */
    private static final AtomicLong nextXid = new AtomicLong(BASE_XID);

    /** Transaction ID of zero. */
    private static final long XID_ZERO = 0L;

    // message factory instance for convenience MPE throwing
    static final MessageFactory MSGF = new MessageFactory();

    // Length-in-Bytes... "(fixed)" means fixed part only - not total msg length
    static final int LIB_ERROR = 12;          // (fixed)  // 1.0  1.1  1.2  1.3
    static final int LIB_ERROR_EXPER = 16;    // (fixed)  // ---  ---  1.2  1.3
    static final int LIB_EXPERIMENTER = 16;   // (fixed)  // ---  1.1  1.2  1.3
    static final int LIB_VENDOR = 12;         // (fixed)  // 1.0  ---  ---  ---
    static final int LIB_FEATURES_REPLY = 32;             // fxd  fxd  fxd  1.3
    static final int LIB_SWITCH_CONFIG = 12;              // 1.0  1.1  1.2  1.3
    static final int LIB_PACKET_IN = 26;      // (fixed)  // ---  ---  ---  1.3
    static final int LIB_PACKET_IN_10 = 18;   // (fixed)  // 1.0  ---  ---  ---
    static final int LIB_FLOW_REMOVED = 48;   // (fixed)  // ---  ---  ---  1.3
    static final int LIB_FLOW_REMOVED_10 = 88;            // 1.0  ---  ---  ---
    static final int LIB_PORT_STATUS = 80;                // ---  ---  ---  1.3
    static final int LIB_PORT_STATUS_10 = 64;             // 1.0  ---  ---  ---
    static final int LIB_PACKET_OUT = 24;     // (fixed)  // ---  ---  ---  1.3
    static final int LIB_PACKET_OUT_10 = 16;  // (fixed)  // 1.0  ---  ---  ---
    static final int LIB_FLOW_MOD = 48;       // (fixed)  // ---  1.1  1.2  1.3
    static final int LIB_FLOW_MOD_10 = 72;    // (fixed)  // 1.0  ---  ---  ---
    static final int LIB_GROUP_MOD = 16;      // (fixed)  // ---  1.1  1.2  1.3
    static final int LIB_PORT_MOD = 40;                   // ---  1.1  1.2  1.3
    static final int LIB_PORT_MOD_10 = 32;                // 1.0  ---  ---  ---
    static final int LIB_TABLE_MOD = 16;                  // ---  1.1  1.2  1.3
    static final int LIB_MP_HEADER = 16;      // (fixed)  // ---  1.1  1.2  1.3
    static final int LIB_MP_HEADER_10 = 12;   // (fixed)  // 1.0  ---  ---  ---
    static final int LIB_Q_GET_CFG = 16;                  // ---  1.1  1.2  1.3
    static final int LIB_Q_GET_CFG_10 = 12;               // 1.0  ---  ---  ---
    static final int LIB_ROLE = 24;                       // ---  ---  1.2  1.3
    static final int LIB_ASYNC_CONFIG = 32;               // ---  ---  ---  1.3
    static final int LIB_METER_MOD = 16;      // (fixed)  // ---  ---  ---  1.3

    private static boolean strictParse;

    static {
        // by default, use non-strict parsing
        setStrictMessageParsing(false);
    }

    // No instantiation except here
    private MessageFactory() {}

    /** Returns an identifying tag for the message factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "MsgF";
    }

    //=====================================================================
    // SUPPORTED VERSIONS
    // ------------------
    //  Here is the authoritative answer on which versions of the
    //  OpenFlow protocol this library supports.
    //
    //  As of March 2013, this is just 1.0 and 1.3.

    static final String E_SUPP_VERSIONS = RES.getString("e_supp_versions");

    private static final Set<ProtocolVersion> SUPPORTED_VERSIONS =
            new HashSet<ProtocolVersion>(Arrays.asList(V_1_0, V_1_3));

    private static final Set<MessageType> ALL_VERSIONS_TYPES =
            new HashSet<MessageType>(Arrays.asList(HELLO, ERROR));


    /**
     * Returns the set of OpenFlow protocol versions that this library
     * supports.
     *
     * @return the set of supported versions
     */
    public static Set<ProtocolVersion> supportedVersions() {
        return Collections.unmodifiableSet(SUPPORTED_VERSIONS);
    }

    /**
     * Throws an exception if the specified version is not supported by
     * this OpenFlow message library.
     *
     * @param pv the protocol version to check
     * @throws VersionNotSupportedException if the version is not supported
     */
    public static void checkVersionSupported(ProtocolVersion pv) {
        if (!SUPPORTED_VERSIONS.contains(pv))
            throw new VersionNotSupportedException(pv + E_SUPP_VERSIONS);
    }

    /**
     * Returns true if the specified version is supported by this
     * OpenFlow message library.
     *
     * @param pv the protocol version to check
     * @return true if supported; false otherwise
     */
    public static boolean isVersionSupported(ProtocolVersion pv) {
        return SUPPORTED_VERSIONS.contains(pv);
    }

    /**
     * Makes sure that the specified type may be created, for the given
     * protocol version.
     *
     * @param pv the protocol version
     * @param t the message type
     */
    private static void checkCreateAllowed(ProtocolVersion pv, MessageType t) {
        notNull(pv, t);
        if (!ALL_VERSIONS_TYPES.contains(t))
            checkVersionSupported(pv);
    }

    /**
     * Sets message parsing to be either strict or non-strict.
     * <p>
     * In strict mode, the message parser will expect all non-spec-defined
     * bits in bitmaps, and reserved fields, to be zero; if a 1-bit is found,
     * an exception will be thrown.
     * <p>
     * In non-strict mode, the message parser will zero-out all
     * non-spec-defined bits in bitmaps, and reserved fields, before parsing.
     *
     * @param strict true, for strict message parsing; false for non-strict
     */
    public static void setStrictMessageParsing(boolean strict) {
        strictParse = strict;
        LOG.info(MSG_STRICT_PARSING, strictParse ? ON : OFF);
    }

    /**
     * Returns true if strict message parsing is in effect.
     *
     * @return true if configured for strict message parsing
     *
     * @see #setStrictMessageParsing(boolean)
     */
    public static boolean isStrictMessageParsing() {
        return strictParse;
    }

    //=====================================================================
    // PARSING MESSAGES

    private static final String E_PARSED_WRONG_VER = RES
            .getString("e_parsed_wrong_ver");

    /**
     * Parses a single OpenFlow message from the supplied byte buffer
     * consuming the bytes in the process.
     *
     * @param buffer byte buffer containing the message encoding
     * @return an OpenFlow message
     * @throws NullPointerException if buffer is null
     * @throws MessageParseException if a message cannot be parsed from the
     *         reader contents
     */
    public static OpenflowMessage parseMessage(ByteBuffer buffer)
            throws MessageParseException {
        notNull(buffer);
        return MessageParser.parse(buffer);
    }

    private static final String E_NOT_ENOUGH_BYTES = RES
            .getString("e_not_enough_bytes");
    private static final int OF_HEADER_LEN_OFFSET = 2;

    /**
     * Parses the supplied packet reader as a single OpenFlow message.
     *
     * @param pkt the packet reader
     * @return an OpenFlow message
     * @throws NullPointerException if pkt is null
     * @throws MessageParseException if a message cannot be parsed from
     *          the reader contents
     */
    public static OpenflowMessage parseMessage(OfPacketReader pkt)
            throws MessageParseException {
        notNull(pkt);
        if (pkt.readableBytes() < OpenflowMessage.OFM_HEADER_LEN)
            throw MSGF.mpe(pkt, E_NOT_ENOUGH_BYTES);
        return MessageParser.parse(pkt, pkt.peekU16(OF_HEADER_LEN_OFFSET));
    }

    /**
     * Parses the supplied packet reader as a single OpenFlow message, but
     * patches in the XID of the specified message. This method is provided
     * to allow unit tests to read in "canned" message replies, but to patch
     * the XID to match that of the supplied message, assumed to be the
     * "request" to which we are generating the "reply".
     *
     * @param pkt the packet reader
     * @param msg the (request) message from which to take the XID
     * @return an OpenFlow message
     * @throws NullPointerException if either parameter is null
     * @throws MessageParseException if a message cannot be parsed from
     *          the reader contents
     */
    public static OpenflowMessage parseMessage(OfPacketReader pkt,
                                               OpenflowMessage msg)
            throws MessageParseException {
        OpenflowMessage parsed = parseMessage(pkt);
        if (msg.getVersion() != parsed.getVersion())
            throw new VersionMismatchException(E_PARSED_WRONG_VER +
                    parsed.getVersion() + " / " + msg.getVersion());
        // copy over XID
        parsed.header.xid = msg.header.xid;
        return parsed;
    }

    //=====================================================================
    // CREATING MESSAGES

    /**
     * Creates a mutable OpenFlow message of the specified type and subtype,
     * with the specified protocol version, assigning it the next available
     * transaction ID.
     * Note that if the subtype is not appropriate for the given type, an
     * exception will be thrown.
     *
     * @param pv the protocol version to set
     * @param type the required message type
     * @param subtype the required message subtype
     * @return a mutable instance of the required message type
     * @throws NullPointerException if any parameter is null
     * @throws VersionNotSupportedException if the required message cannot be
     *          created for the given version
     */
    public static MutableMessage create(ProtocolVersion pv, MessageType type,
                                        Enum<?> subtype) {
        checkCreateAllowed(pv, type);
        MutableMessage mm = MessageCreator.create(pv, type, subtype, XID_ZERO);
        assignXid(mm);
        return mm;
    }

    /**
     * Creates a mutable OpenFlow message of the specified type, with the
     * specified protocol version, assigning it the next available
     * transaction ID.
     *
     * @param pv the protocol version to set
     * @param type the required message type
     * @return a mutable instance of the required message type
     * @throws NullPointerException if either parameter is null
     * @throws VersionNotSupportedException if the required message cannot be
     *          created for the given version
     */
    public static MutableMessage create(ProtocolVersion pv, MessageType type) {
        checkCreateAllowed(pv, type);
        MutableMessage mm = MessageCreator.create(pv, type, null, XID_ZERO);
        assignXid(mm);
        return mm;
    }

    /**
     * Creates a mutable OpenFlow message of the specified type, with
     * protocol version and transaction ID matching those of the given
     * message. This form is useful when formulating a response to
     * a given OpenFlow message.
     *
     * @param msg the message from which to copy the protocol version
     *            and transaction id
     * @param type the required message type
     * @return a mutable instance of the required message type
     * @throws NullPointerException if either parameter is null
     * @throws VersionNotSupportedException if the required message cannot be
     *          created for the given version
     */
    public static MutableMessage create(Message msg, MessageType type) {
        checkCreateAllowed(msg.getVersion(), type);
        return MessageCreator.create(msg.getVersion(), type, null,
                msg.getXid());
    }

    /**
     * Creates a mutable OpenFlow message of the specified type and subtype,
     * with protocol version and transaction ID matching those of the given
     * message. This form is useful when formulating a response to
     * a given OpenFlow message.
     * Note that if the subtype is not appropriate for the given type, an
     * exception will be thrown.
     *
     * @param msg the message from which to copy the protocol version
     *            and transaction id
     * @param type the required message type
     * @param subtype the required message subtype
     * @return a mutable instance of the required message type
     * @throws NullPointerException if either parameter is null
     * @throws VersionNotSupportedException if the required message cannot be
     *          created for the given version
     */
    public static MutableMessage create(Message msg, MessageType type,
                                        Enum<?> subtype) {
        checkCreateAllowed(msg.getVersion(), type);
        return MessageCreator.create(msg.getVersion(), type, subtype,
                msg.getXid());
    }

    /**
     * Creates a mutable packet-out message pre-populated with salient data
     * from the given packet-in message.
     *
     * @param pi the source packet-in
     * @return a mutable packet-out instance
     * @throws IllegalArgumentException if the packet-in is mutable
     */
    public static OfmMutablePacketOut createPacketOutFromPacketIn(OfmPacketIn pi) {
        notMutable(pi);
        OfmMutablePacketOut po = (OfmMutablePacketOut) create(pi, PACKET_OUT);
        // FIXME: Use inPort(bpn) variant instead after it is fixed.
        po.bufferId(pi.getBufferId()).inPort(pi).dataNoClone(pi.getData());
        return po;
    }

    /**
     * Patches the specified flow mod message with a new priority.
     *
     * @param flowMod flow mod message to be patched
     * @param newPriority new priority to assign to the flow mod
     * @return patched flow mod
     */
    public static OfmFlowMod patchFlowModPriority(OfmFlowMod flowMod,
                                                  int newPriority) {
        return flowMod.patchPriority(newPriority);
    }

    /**
     * Assigns the next available transaction ID to the specified message.
     * Note that transaction ID assignment is <em>only</em> available through
     * this method, the {@link #copyXid} method, or implicitly through the
     * {@link #create} methods (which call this method).
     *
     * @param msg the mutable message to which the next transaction ID
     *             is to be assigned
     * @throws NullPointerException if msg is null
     * @throws InvalidMutableException if the message is not writable
     */
    public static void assignXid(MutableMessage msg) {
        if (!msg.writable())
            throw new InvalidMutableException(msg.toString());
        OpenflowMessage ofm = (OpenflowMessage) msg;
        stampNewXid(ofm);
    }

    /**
     * Package private method that stamps a new XID into the specified
     * message, even if it is otherwise immutable.
     *
     * @param msg the message (presumable just copied) to stamp
     */
    static void stampNewXid(OpenflowMessage msg) {
        /* NOTE:
            2^32 = 4294967296
            XIDs = 4294966939  (LAST_XID - BASE_XID - 1)
            At the continuous production rate of 1/ms...
            Time to XID rollover: 0 years 9 weeks 0 days 11 hours 2 mins
         */
        msg.header.xid = nextXid.incrementAndGet();
        // Make sure that u32 wraps back to XID_BASE
        // Note - we are keeping this unsynchronized by design
        if (nextXid.get() > LAST_XID)
            nextXid.set(BASE_XID);
    }

    /**
     * Copies the transaction ID from given source message to the specified
     * destination message.
     *
     * @param src the message whose XID should be copied
     * @param dst the (mutable) message into which the XID should be copied
     * @throws NullPointerException if either parameter is null
     * @throws InvalidMutableException if dst is not writable
     */
    public static void copyXid(OpenflowMessage src, MutableMessage dst) {
        if (!dst.writable())
            throw new InvalidMutableException(dst.toString());
        OpenflowMessage ofm = (OpenflowMessage) dst;
        ofm.header.xid = src.getXid();
    }

    // private method ==FOR UNIT TEST SUPPORT ONLY==
    private static void resetXid() {
        nextXid.set(BASE_XID);
    }


    //=====================================================================
    // COPYING MESSAGES

    /**
     * Creates an immutable (deep) copy of the specified OpenFlow message.
     * (Note that a new transaction ID will be assigned to the copy).
     *
     * @param msg the message to copy
     * @return an immutable copy
     */
    public static OpenflowMessage copy(OpenflowMessage msg) {
        return MessageCopier.copy(msg);
    }

    /**
     * Creates an immutable (deep) copy of the specified OpenFlow message.
     * (Note that the transaction ID of the original message is retained
     * in the copy).
     *
     * @param msg the message to copy
     * @return an immutable copy
     */
    public static OpenflowMessage exactCopy(OpenflowMessage msg) {
        return MessageCopier.exactCopy(msg);
    }

    /**
     * Creates a mutable (deep) copy of the specified OpenFlow message.
     * (Note that a new transaction ID will be assigned to the copy).
     *
     * @param msg the message to copy
     * @return a mutable copy
     */
    public static MutableMessage mutableCopy(OpenflowMessage msg) {
        return MessageCopier.mutableCopy(msg);
    }

    /**
     * Creates a mutable (deep) copy of the specified OpenFlow message.
     * (Note that the transaction ID of the original message is retained
     * in the copy).
     *
     * @param msg the message to copy
     * @return a mutable copy
     */
    public static MutableMessage exactMutableCopy(OpenflowMessage msg) {
        return MessageCopier.exactMutableCopy(msg);
    }


    //=====================================================================
    // ENCODING MESSAGES

    /**
     * Encodes the specified OpenFlow message into the supplied byte buffer.
     *
     * @param msg the message to encode
     * @param buffer byte buffer to receive message bytes
     * @throws IllegalArgumentException if the message is mutable
     * @throws IncompleteMessageException if the message was
     *          insufficiently initialized
     * @throws IncompleteStructureException if any internal structure
     *          was insufficiently initialized
     */
    public static void encodeMessage(OpenflowMessage msg, ByteBuffer buffer)
            throws IncompleteMessageException, IncompleteStructureException {
        MessageEncoder.encode(msg, buffer);
    }


    /**
     * Encodes the specified OpenFlow message, returning the result in
     * a newly allocated byte array.
     *
     * @param msg the message to encode
     * @return the encoded message as a byte array
     * @throws IllegalArgumentException if the message is mutable
     * @throws IncompleteMessageException if the message was
     *          insufficiently initialized
     * @throws IncompleteStructureException if any internal structure
     *          was insufficiently initialized
     */
    public static byte[] encodeMessage(OpenflowMessage msg)
            throws IncompleteMessageException, IncompleteStructureException {
        return MessageEncoder.encode(msg);
    }


    //=====================================================================
    // UNIT TEST SUPPORT

    /**
     * Denotes things that can be reset, during unit tests.
     */
    public static enum TestReset {
        /** The message factory XID generator value. */
        XID,
        ;
    }

    /**
     * Returns a unit test support instance, to allow priviledged operations
     * to be performed by test code.
     *
     * @return the test support instance
     */
    public static UnitTestSupport getTestSupport() {
        return UnitTestSupportProxy.injectProxy(new TestSupport());
    }

    // Our implementing class
    private static class TestSupport extends UnitTestSupportAdapter {
        /** Resets the factory XID generator to the base value. */
        @Override
        public void reset(Enum<?> e) {
            if (e == TestReset.XID)
                resetXid();
        }
    }
}