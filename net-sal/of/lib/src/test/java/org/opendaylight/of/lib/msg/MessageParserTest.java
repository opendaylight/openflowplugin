/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.junit.TestLogger;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.VlanId;
import org.slf4j.Logger;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MessageParser}.
 *
 * @author Simon Hunt
 */
public class MessageParserTest {
    private static final String FMT_EX = "EX> {}";

    private static final String ECHO_REQUEST = "0402000a0000007ebabe";
    private static final String ECHO_REPLY = "0403000a0000007ebabe";
    private static final String BARRIER_REPLY = "041500080000abcd";

    private static final String BARRIER_REPLY_12 = "031500080000abcd";

    private static final String BAD_PV = "da1500080000abcd";
    private static final String BAD_MTYPE = "04ee00080000abcd";

    private static final String PACKET_IN = "040a003600000065ffffffff" +
            "00000009000000000000000000010018800000040000000380000a020800" +
            "80000c02106400007e6f7e6f";
    private static final String BAD_PACKET_IN = "040a003600000065ffffffff" +
            "00000009000000000000000000010018800000040000000380000a020800" +
            "80000c02006400007e6f7e6f";

    private static final BigPortNumber IN_PORT = BigPortNumber.valueOf(3);
    private static final TableId TID = TableId.valueOf(9);
    private static final VlanId VLAN = VlanId.valueOf(100);

    private static final byte[] DATA = { 0x7e, 0x6f, 0x7e, 0x6f };

    private static final byte[] BABE_ARRAY = {(byte) 0xba, (byte) 0xbe};

    private ByteBuffer bb;
    private OpenflowMessage ofm;

    private static TestLogger tlog = new TestLogger();
    private static Logger productionLogger = null;

    @BeforeClass
    public static void classSetUp() {
        if (productionLogger != null)
            throw new IllegalStateException();
        productionLogger = MessageParser.log;
        MessageParser.log = tlog;
    }

    @AfterClass
    public static void classTearDown() {
        if (productionLogger == null)
            throw new IllegalStateException();
        MessageParser.log = productionLogger;
        productionLogger = null;
    }

    private static void verifyLogWarning(String expected) {
        tlog.assertWarning(expected);
    }

    /**
     * Creates and returns a byte buffer filled with the bytes encoded in
     * the given hex strings.
     *
     * @param hexStrings the hex strings with which to populate the buffer
     * @return the filled buffer
     */
    private ByteBuffer getBuffer(String... hexStrings) {
        ByteBuffer bb;
        if (hexStrings.length == 0) {
            bb = ByteBuffer.allocate(0);
            print(dumpBuffer(bb));
            return bb;
        }

        // convert hex strings to byte arrays
        byte[][] bytes = new byte[hexStrings.length][];
        int totalBytes = 0;
        int i = 0;
        for (String hex: hexStrings) {
            bytes[i] = ByteUtils.parseHex(hex);
            totalBytes += bytes[i].length;
            i++;
        }

        // allocate a buffer big enough to hold all the bytes, and populate
        bb = ByteBuffer.allocate(totalBytes);
        for (byte[] b: bytes)
            bb.put(b);

        // make ready for reading
        bb.flip();
        print(dumpBuffer(bb));
        return bb;
    }

    private String dumpBuffer(ByteBuffer bb) {
        StringBuilder sb = new StringBuilder(bb.toString());
        sb.append(" [").append(ByteUtils.hex(bb.array())).append("]");
        return sb.toString();
    }

    private String startBytes(String hexString, int count) {
        int byteLength = hexString.length() / 2;
        int byteCount = Math.min(count, byteLength);
        return hexString.substring(0, byteCount * 2);
    }

    // invoke parse; fail the test if MPE is thrown
    private OpenflowMessage parseBuffer(ByteBuffer bb) {
        OpenflowMessage msg = null;
        try {
            msg = MessageParser.parse(bb);
        } catch (MessageParseException e) {
            fail(AM_WREX);
        }
        return msg;
    }

    private void verifyPosLimCap(ByteBuffer bb, int pos, int lim, int cap) {
        assertEquals(AM_NEQ, pos, bb.position());
        assertEquals(AM_NEQ, lim, bb.limit());
        assertEquals(AM_NEQ, cap, bb.capacity());
    }

    private void verifyRemaining(ByteBuffer bb, int rem) {
        assertEquals(AM_NEQ, rem, bb.remaining());
    }


    @Test(expected = NullPointerException.class)
    public void parseNull() {
        print(EOL + "parseNull()");
        ofm = parseBuffer(null);
    }

    @Test
    public void parseZeroBytes() {
        print(EOL + "parseZeroBytes()");
        bb = getBuffer();
        // nothing to parse
        ofm = parseBuffer(bb);
        assertNull(AM_HUH, ofm);
    }

    @Test
    public void parseTwoBytes() {
        print(EOL + "parseTwoBytes()");
        bb = getBuffer(startBytes(ECHO_REQUEST, 2));
        assertEquals(AM_NEQ, 2, bb.remaining());
        // not enough bytes to read the length field
        ofm = parseBuffer(bb);
        assertNull(AM_HUH, ofm);
    }

    @Test
    public void parseSixBytes() {
        print(EOL + "parseSixBytes()");
        bb = getBuffer(startBytes(ECHO_REQUEST, 6));
        assertEquals(AM_NEQ, 6, bb.remaining());
        // we can read the length field, but too few bytes for message length
        ofm = parseBuffer(bb);
        assertNull(AM_HUH, ofm);
    }

    private void verifyHeader(OpenflowMessage ofm, ProtocolVersion pv,
                              MessageType mt, int len, long xid) {
        assertEquals(AM_NEQ, pv, ofm.getVersion());
        assertEquals(AM_NEQ, mt, ofm.getType());
        assertEquals(AM_NEQ, len, ofm.length());
        assertEquals(AM_NEQ, xid, ofm.getXid());
    }

    @Test
    public void parseFullEchoRequest() {
        print(EOL + "parseFullEchoRequest()");
        bb = getBuffer(ECHO_REQUEST);
        assertEquals(AM_NEQ, 10, bb.remaining());
        ofm = parseBuffer(bb);
        assertNotNull(AM_HUH, ofm);
        print(ofm.toDebugString());
        verifyHeader(ofm, V_1_3, MessageType.ECHO_REQUEST, 10, 126);
        OfmEchoRequest echo = (OfmEchoRequest) ofm;
        assertArrayEquals(AM_NEQ, BABE_ARRAY, echo.getData());
    }

    @Test
    public void parseBadPvInHeader() {
        print(EOL + "parseBadPvInHeader()");
        bb = getBuffer(BAD_PV);
        try {
            ofm = MessageParser.parse(bb);
            fail(AM_NOEX);
        } catch (MessageParseException e) {
            print(FMT_EX, e);
            assertEquals(AM_WREXMSG,
                    "MsgF:java.nio.HeapByteBuffer[pos=1 lim=8 cap=8] " +
                    "OFM:null > org.opendaylight.of.lib.DecodeException: Unknown " +
                    "OpenFlow Protocol version code: 0xda", e.getMessage());
            // we should also have logged a warning
            // TODO: augment test logger to handle multiple messages
//            verifyLogWarning("Parse FAILED: hdr={null}, " +
//                    "bytes=[" + BAD_PV + "]");
            verifyLogWarning("Parse terminated before end. Start=0, Target=8, " +
                    "Read=1, Remaining=7");
        }
    }

    @Test
    public void parseBadMsgTypeInHeader() {
        print(EOL + "parseBadMsgTypeInHeader()");
        bb = getBuffer(BAD_MTYPE);
        try {
            ofm = MessageParser.parse(bb);
            fail(AM_NOEX);
        } catch (MessageParseException e) {
            print(FMT_EX, e);
            assertEquals(AM_WREXMSG,
                "MsgF:java.nio.HeapByteBuffer[pos=2 lim=8 cap=8] OFM:null " +
                "> org.opendaylight.of.lib.DecodeException: Unknown V_1_3 type code: 238",
                e.getMessage());
            // TODO: augment test logger to handle multiple messages
//            verifyLogWarning("Parse FAILED: hdr={null}, " +
//                    "bytes=[" + BAD_MTYPE + "]");
            verifyLogWarning("Parse terminated before end. Start=0, Target=8, " +
                    "Read=2, Remaining=6");
        }
    }

    @Test
    public void parseUnsupportedVersion() {
        print(EOL + "parseUnsupportedVersion()");
        bb = getBuffer(BARRIER_REPLY_12);
        try {
            ofm = MessageParser.parse(bb);
            fail(AM_NOEX);
        } catch (MessageParseException e) {
            fail(AM_WREX);
        } catch (VersionNotSupportedException e) {
            print(FMT_EX, e);
            assertEquals(AM_WREXMSG,
                "[V_1_2,BARRIER_REPLY,8,43981]: OpenFlow Library only " +
                "supports 1.0 and 1.3", e.getMessage());
            verifyLogWarning("Parse UNSUPPORTED: [V_1_2,BARRIER_REPLY,8,43981]");
        }
    }

    @Test
    public void createPacketIn()
            throws IncompleteStructureException, IncompleteMessageException {
        OfmMutablePacketIn pi = (OfmMutablePacketIn)
                MessageFactory.create(V_1_3, MessageType.PACKET_IN);
        pi.reason(PacketInReason.NO_MATCH).tableId(TID).data(DATA)
                .bufferId(BufferId.NO_BUFFER).match(createMatch());
        byte[] bytes = MessageFactory.encodeMessage(pi.toImmutable());
        String hex = ByteUtils.hex(bytes);
        print(hex);
        // can't assert because the XID is globally assigned and could be
        //  different, based on what messages were created in other tests
//        assertEquals(AM_NEQ, PACKET_IN, hex);
    }

    private Match createMatch() {
        MutableMatch mm = MatchFactory.createMatch(V_1_3);
        mm.addField(createBasicField(V_1_3, OxmBasicFieldType.IN_PORT, IN_PORT));
        mm.addField(createBasicField(V_1_3, OxmBasicFieldType.ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(V_1_3, OxmBasicFieldType.VLAN_VID, VLAN));
        return (Match) mm.toImmutable();
    }


    @Test
    public void parsePacketIn() {
        print(EOL + "parsePacketIn()");
        bb = getBuffer(PACKET_IN);
        ofm = parseBuffer(bb);
        print(ofm.toDebugString());
        verifyHeader(ofm, V_1_3, MessageType.PACKET_IN, 54, 101);
        OfmPacketIn pi = (OfmPacketIn) ofm;
        assertEquals(AM_NEQ, IN_PORT, pi.getInPort());
        assertArrayEquals(AM_NEQ, DATA, pi.getData());
    }

    @Test
    public void parseBadPacketIn() {
        print(EOL + "parseBadPacketIn()");
        bb = getBuffer(BAD_PACKET_IN);
        try {
            MessageParser.parse(bb);
            fail(AM_NOEX);
        } catch (MessageParseException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ,
                "MsgF:java.nio.HeapByteBuffer[pos=48 lim=54 cap=54] " +
                "OFM:[V_1_3,PACKET_IN,54,101] > " +
                "org.opendaylight.of.lib.MessageParseException: " +
                "FF:java.nio.HeapByteBuffer[pos=48 lim=54 cap=54] " +
                "VLAN_VID: PRESENT bit not set for VLAN id", e.getMessage());
            // TODO: augment test logger to handle multiple messages
//            verifyLogWarning("Parse FAILED: hdr=[V_1_3,PACKET_IN,54,101], " +
//                    "bytes=[" + BAD_PACKET_IN + "]");
            verifyLogWarning("Parse terminated before end. Start=0, Target=54, " +
                    "Read=48, Remaining=6");
        }
        // verify that the remainder of the message was consumed
        verifyRemaining(bb, 0);
    }

    @Test
    public void goodBadGood() {
        print(EOL + "goodBadGood()");
        bb = getBuffer(ECHO_REPLY, BAD_PACKET_IN, BARRIER_REPLY);
        verifyPosLimCap(bb, 0, 72, 72);
        verifyRemaining(bb, 72);

        // first, the echo reply
        ofm = parseBuffer(bb);
        print(EOL + ofm.toDebugString());
        verifyHeader(ofm, V_1_3, MessageType.ECHO_REPLY, 10, 126);
        verifyLogWarning(null);
        verifyPosLimCap(bb, 10, 72, 72);
        verifyRemaining(bb, 62);

        // next, the bad packet in
        try {
            MessageParser.parse(bb);
            fail(AM_NOEX);
        } catch (MessageParseException e) {
            print(EOL + FMT_EX, e);
            assertEquals(AM_NEQ,
                "MsgF:java.nio.HeapByteBuffer[pos=58 lim=72 cap=72] " +
                "OFM:[V_1_3,PACKET_IN,54,101] > " +
                "org.opendaylight.of.lib.MessageParseException: " +
                "FF:java.nio.HeapByteBuffer[pos=58 lim=72 cap=72] " +
                "VLAN_VID: PRESENT bit not set for VLAN id", e.getMessage());
// TODO: augment TestLogger to handle multiple messages
//            verifyLogWarning("Parse FAILED: hdr=[V_1_3,PACKET_IN,54,101], " +
//                    "bytes=[" + BAD_PACKET_IN + "]");
            verifyLogWarning("Parse terminated before end. Start=10, Target=64, " +
                    "Read=48, Remaining=6");
        }
        // NOTE: we have consumed the "unparsed" bytes in this message, hence
        //  position is at 64 (not at 58 when the exception was thrown)
        verifyPosLimCap(bb, 64, 72, 72);
        verifyRemaining(bb, 8);

        // finally, the barrier reply
        ofm = parseBuffer(bb);
        print(EOL + ofm.toDebugString());
        verifyHeader(ofm, V_1_3, MessageType.BARRIER_REPLY, 8, 0xabcd);
        verifyLogWarning(null);
        verifyPosLimCap(bb, 72, 72, 72);
        verifyRemaining(bb, 0);
    }
}
