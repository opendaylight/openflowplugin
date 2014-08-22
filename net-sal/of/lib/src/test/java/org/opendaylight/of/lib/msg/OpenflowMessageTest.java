/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.util.ByteUtils;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.ByteUtils.parseHex;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link OpenflowMessage}.
 *
 * @author Simon Hunt
 */
public class OpenflowMessageTest {
    private static final String FMT_EX = "EX> {}";

    private static final String HDR_10_HELLO_8_0 = "0100000800000000";
    private static final String HDR_11_ERROR_76_100 = "0201004c00000064";
    private static final String HDR_12_ECHOREQ_8_103 = "0302000800000067";
    private static final String HDR_13_PACKETIN_256_106 = "040a01000000006a";

    private static final String BAD_HDR_PV = "db0a01000000006a";
    private static final String BAD_HDR_TYPE = "04fd01000000006a";

    private static class SomeMessage extends OpenflowMessage {
        SomeMessage(Header header) {
            super(header);
        }
    }


    private OpenflowMessage msg;
    private OpenflowMessage.Header hdr;

    @Test
    public void hexLong() {
        print(EOL + "hexLong()");
        assertEquals(AM_NEQ, "0x1", OpenflowMessage.hex(1L));
        assertEquals(AM_NEQ, "0xa", OpenflowMessage.hex(10L));
        assertEquals(AM_NEQ, "0x64", OpenflowMessage.hex(100L));
    }

    @Test
    public void hexInt() {
        print(EOL + "hexInt()");
        assertEquals(AM_NEQ, "0x1", OpenflowMessage.hex(1));
        assertEquals(AM_NEQ, "0xa", OpenflowMessage.hex(10));
        assertEquals(AM_NEQ, "0x64", OpenflowMessage.hex(100));
    }


    private OfPacketReader wrapBytes(String hexString) {
        return new OfPacketReader(parseHex(hexString));
    }

    private void assertHeader(OpenflowMessage.Header hdr, ProtocolVersion pv,
                              MessageType mt, int len, long xid) {
        print(hdr);
        assertEquals(AM_NEQ, pv, hdr.version);
        assertEquals(AM_NEQ, mt, hdr.type);
        assertEquals(AM_NEQ, len, hdr.length);
        assertEquals(AM_NEQ, xid, hdr.xid);
    }

    @Test
    public void parseHeaderOne() throws DecodeException, HeaderParseException {
        print(EOL + "parseHeaderOne()");
        hdr = OpenflowMessage.parseHeader(wrapBytes(HDR_10_HELLO_8_0));
        assertHeader(hdr, V_1_0, HELLO, 8, 0);
    }

    @Test
    public void parseHeaderTwo() throws DecodeException, HeaderParseException {
        print(EOL + "parseHeaderTwo()");
        hdr = OpenflowMessage.parseHeader(wrapBytes(HDR_11_ERROR_76_100));
        assertHeader(hdr, V_1_1, ERROR, 76, 100);
    }

    @Test
    public void parseHeaderThree() throws DecodeException, HeaderParseException {
        print(EOL + "parseHeaderThree()");
        hdr = OpenflowMessage.parseHeader(wrapBytes(HDR_12_ECHOREQ_8_103));
        assertHeader(hdr, V_1_2, ECHO_REQUEST, 8, 103);
    }

    @Test
    public void parseHeaderFour() throws DecodeException, HeaderParseException {
        print(EOL + "parseHeaderFour()");
        hdr = OpenflowMessage.parseHeader(wrapBytes(HDR_13_PACKETIN_256_106));
        assertHeader(hdr, V_1_3, PACKET_IN, 256, 106);
    }

    @Test
    public void badHeaderPv() {
        print(EOL + "badHeaderPv()");
        try {
            hdr = OpenflowMessage.parseHeader(wrapBytes(BAD_HDR_PV));
            fail(AM_NOEX);
        } catch (DecodeException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, "Unknown OpenFlow Protocol version code: 0xdb",
                    e.getMessage());
        }
    }

    @Test
    public void badHeaderType() {
        print(EOL + "badHeaderType()");
        try {
            hdr = OpenflowMessage.parseHeader(wrapBytes(BAD_HDR_TYPE));
            fail(AM_NOEX);
        } catch (DecodeException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, "Unknown V_1_3 type code: 253", e.getMessage());
        }
    }

    private void verifyDecodedHeader(String exp, String headerBytes) {
        String result = OpenflowMessage.decodeHeader(parseHex(headerBytes));
        print("{} => {}", headerBytes, result);
        assertEquals(AM_NEQ, exp, result);
    }

    @Test
    public void decodeHeader() {
        print(EOL + "decodeHeader()");
        verifyDecodedHeader("[V_1_0,HELLO,8,0]", HDR_10_HELLO_8_0);
        verifyDecodedHeader("[V_1_1,ERROR,76,100]", HDR_11_ERROR_76_100);
        verifyDecodedHeader("[V_1_2,ECHO_REQUEST,8,103]", HDR_12_ECHOREQ_8_103);
        verifyDecodedHeader("[V_1_3,PACKET_IN,256,106]", HDR_13_PACKETIN_256_106);
        verifyDecodedHeader("(Cannot decode header)", BAD_HDR_PV);
        verifyDecodedHeader("(Cannot decode header)", BAD_HDR_TYPE);
    }

    @Test(expected = NullPointerException.class)
    public void decodeHeaderNpe() {
        OpenflowMessage.decodeHeader(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeHeaderInsufficientBytes() {
        OpenflowMessage.decodeHeader(new byte[0]);
    }


    private OpenflowMessage.Header makeHeader(ProtocolVersion pv, MessageType mt,
                                              int len, long xid) {
        OpenflowMessage.Header h = new OpenflowMessage.Header(pv, mt, xid);
        h.length = len;
        return h;
    }

    private void verifyWriteHeader(String exp, OpenflowMessage.Header hdr) {
        OfPacketWriter pw = new OfPacketWriter(8);
        OpenflowMessage.writeHeader(hdr, pw);
        String hex = ByteUtils.hex(pw.array());
        print("{} => {}", hdr, hex);
        assertEquals(AM_NEQ, exp, hex);
    }

    @Test
    public void writeHeaderOne() {
        print(EOL + "writeHeaderOne()");
        hdr = makeHeader(V_1_0, HELLO, 8, 0);
        verifyWriteHeader(HDR_10_HELLO_8_0, hdr);
    }

    @Test
    public void writeHeaderTwo() {
        print(EOL + "writeHeaderTwo()");
        hdr = makeHeader(V_1_1, ERROR, 76, 100);
        verifyWriteHeader(HDR_11_ERROR_76_100, hdr);
    }

    @Test
    public void writeHeaderThree() {
        print(EOL + "writeHeaderThree()");
        hdr = makeHeader(V_1_2, ECHO_REQUEST, 8, 103);
        verifyWriteHeader(HDR_12_ECHOREQ_8_103, hdr);
    }

    @Test
    public void writeHeaderFour() {
        print(EOL + "writeHeaderFour()");
        hdr = makeHeader(V_1_3, PACKET_IN, 256, 106);
        verifyWriteHeader(HDR_13_PACKETIN_256_106, hdr);
    }

    // ===
    private static final long XID = 1491625;
    private static final int LENGTH = 8;

    private OpenflowMessage createSomeMessage() {
        return new SomeMessage(makeHeader(V_1_3, BARRIER_REPLY, LENGTH, XID));
    }

    @Test
    public void accessors() {
        print(EOL + "accessors()");
        msg = createSomeMessage();
        print(msg);
        assertEquals(AM_NEQ, V_1_3, msg.getVersion());
        assertEquals(AM_NEQ, BARRIER_REPLY, msg.getType());
        assertEquals(AM_NEQ, LENGTH, msg.length());
        assertEquals(AM_NEQ, XID, msg.getXid());
    }

    @Test
    public void strings() {
        print(EOL + "strings()");
        // note that the default implementation of toDebugString is to
        // return the same as toString() - which just shows the header..
        msg = createSomeMessage();
        print("toString():      {}{}toDebugString(): {}{}",
                msg, EOL, msg.toDebugString(), EOL);
        assertEquals(AM_NEQ, msg.toString(), msg.toDebugString());
    }

    @Test
    public void defaultValidate() {
        print(EOL + "defaultValidate()");
        try {
            createSomeMessage().validate();
            print("no exception thrown by default");
        } catch (IncompleteMessageException e) {
            print(e);
            fail(AM_UNEX);
        }
    }

    @Test
    public void headerCopyConstructor() {
        print(EOL + "headerCopyConstructor()");
        hdr = makeHeader(V_1_3, ECHO_REPLY, LENGTH, XID);
        OpenflowMessage.Header hdrCopy  = new OpenflowMessage.Header(hdr);
        SomeMessage msg = new SomeMessage(hdr);
        SomeMessage msgCopy = new SomeMessage(hdrCopy);
        assertEquals(AM_NEQ, msg.getVersion(), msgCopy.getVersion());
        assertEquals(AM_NEQ, msg.getType(), msgCopy.getType());
        assertEquals(AM_NEQ, msg.length(), msgCopy.length());
        assertEquals(AM_NEQ, msg.getXid(), msgCopy.getXid());
        assertNotSame("same header instance!", msg.header, msgCopy.header);
    }
}
