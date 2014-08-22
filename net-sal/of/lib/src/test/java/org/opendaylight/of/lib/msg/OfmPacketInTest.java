/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_IN;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmPacketIn message.
 *
 * @author Simon Hunt
 */
public class OfmPacketInTest extends OfmTest {

    // test files
    private static final String TF_PIN_13 = "v13/packetIn";
    private static final String TF_PIN_13_NO_DATA = "v13/packetInNoData";
    private static final String TF_PIN_13_PARTIAL_DATA = "v13/packetInPartialData";
    private static final String TF_PIN_10 = "v10/packetIn";

    private static final BufferId BUFFER_ID = BufferId.NO_BUFFER;
    private static final BufferId BUFFER_ID_2 = bid(257);
    private static final BigPortNumber IN_PORT = bpn(0x30);
    private static final int TOTAL_LEN = 273; // We think this is wrong
    private static final PacketInReason REASON = PacketInReason.ACTION;
    private static final TableId TABLE_ID = tid(7);
    private static final int FRAME_LENGTH = 261;
    private static final long COOKIE = 0xcafe;

    // frame data
    private static final String TF_LLDP = "lldpPacket";
    private static final MacAddress DST_MAC = mac("0180c2:00000e");
    private static final MacAddress SRC_MAC = mac("082e5f:69c47b");


    private MutableMessage mm;

    // ========================================================= PARSING ====


    private void verifyPacketIn13Stuff(OfmPacketIn msg) {
        assertEquals(AM_NEQ, IN_PORT, msg.getInPort());
        assertEquals(AM_NEQ, IN_PORT, msg.getInPhyPort());
        assertEquals(AM_NEQ, REASON, msg.getReason());
        assertEquals(AM_NEQ, TOTAL_LEN, msg.getTotalLen());
        assertEquals(AM_NEQ, TABLE_ID, msg.getTableId());
        assertEquals(AM_NEQ, COOKIE, msg.getCookie());
        assertNotNull(AM_HUH, msg.getMatch());
        Match match = msg.getMatch();
        Iterator<MatchField> mfIter = match.getMatchFields().iterator();
        verifyMatchField(mfIter.next(), OxmBasicFieldType.IN_PORT, IN_PORT);
        verifyMatchField(mfIter.next(), OxmBasicFieldType.ETH_DST, DST_MAC);
        verifyMatchField(mfIter.next(), OxmBasicFieldType.ETH_SRC, SRC_MAC);
        assertFalse(AM_HUH, mfIter.hasNext());
    }

    @Test
    public void lldpPacketIn13() {
        print(EOL + "lldpPacketIn13()");
        OfmPacketIn msg = (OfmPacketIn)
                verifyMsgHeader(TF_PIN_13, V_1_3, PACKET_IN, 319);
        assertEquals(AM_NEQ, BUFFER_ID, msg.getBufferId());
        verifyPacketIn13Stuff(msg);
        byte[] frameData = msg.getData();
        assertEquals(AM_UXS, FRAME_LENGTH, frameData.length);
        byte[] expData = getExpByteArray(TF_LLDP);
        assertArrayEquals(AM_NEQ, expData, frameData);
    }

    @Test
    public void packetIn13NoData() {
        print(EOL + "packetIn13NoData");
        OfmPacketIn msg = (OfmPacketIn)
                verifyMsgHeader(TF_PIN_13_NO_DATA, V_1_3, PACKET_IN, 58);
        assertEquals(AM_NEQ, BUFFER_ID_2, msg.getBufferId());
        verifyPacketIn13Stuff(msg);
        assertEquals(AM_UXS, 0, msg.getData().length);
    }

    @Test
    public void packetIn13PartialData() {
        print(EOL + "packetIn13PartialData");
        OfmPacketIn msg = (OfmPacketIn)
                verifyMsgHeader(TF_PIN_13_PARTIAL_DATA, V_1_3, PACKET_IN, 186);
        assertEquals(AM_NEQ, BUFFER_ID_2, msg.getBufferId());
        verifyPacketIn13Stuff(msg);
        assertEquals(AM_UXS, 128, msg.getData().length);
    }

    @Test
    public void lldpPacketIn10() {
        print(EOL + "lldpPacketIn10()");
        OfmPacketIn msg = (OfmPacketIn)
                verifyMsgHeader(TF_PIN_10, V_1_0, PACKET_IN, 279);
        assertEquals(AM_NEQ, BUFFER_ID, msg.getBufferId());
        assertEquals(AM_NEQ, IN_PORT, msg.getInPort());
        assertEquals(AM_NEQ, null, msg.getInPhyPort());
        assertEquals(AM_NEQ, REASON, msg.getReason());
        assertEquals(AM_NEQ, TOTAL_LEN, msg.getTotalLen());
        assertNull(AM_HUH, msg.getTableId());
        assertEquals(AM_HUH, 0, msg.getCookie());
        byte[] frameData = msg.getData();
        assertEquals(AM_UXS, FRAME_LENGTH, frameData.length);
        byte[] expData = getExpByteArray(TF_LLDP);
        assertArrayEquals(AM_NEQ, expData, frameData);

        assertEquals(AM_NEQ, LLDP_PACKET_IN_TO_STRING, msg.toString());
    }

    private static final String LLDP_PACKET_IN_TO_STRING =
        "{ofm:[V_1_0,PACKET_IN,279,0],inPort=0x30(48),reason=ACTION," +
        "packet=[[ETHERNET, LLDP], dst=01:80:c2:00:00:0e, src=08:2e:5f:69:c4:7b]}";

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodePacketIn13() {
        print(EOL + "encodePacketIn13()");
        final ProtocolVersion pv = V_1_3;

        mm = MessageFactory.create(pv, PACKET_IN);
        mm.clearXid();
        verifyMutableHeader(mm, pv, PACKET_IN, 0);

        // assemble the pieces
        OfmMutablePacketIn pin = (OfmMutablePacketIn) mm;
        // Setting inPort/inPhyPort should throw a version mismatch exception
        pin.bufferId(BUFFER_ID).totalLen(TOTAL_LEN).reason(REASON)
                .tableId(TABLE_ID).cookie(COOKIE).match(makeMatch13())
                .data(getExpByteArray(TF_LLDP));
        // finally...
        encodeAndVerifyMessage(mm.toImmutable(), TF_PIN_13);
    }

    @Test
    public void encodePacketIn13WithReason() {
        print(EOL + "encodePacketIn13WithReason()");
        final ProtocolVersion pv = V_1_3;
        mm = MessageFactory.create(pv, PACKET_IN, REASON);
        mm.clearXid();
        verifyMutableHeader(mm, pv, PACKET_IN, 0);

        // assemble the pieces
        OfmMutablePacketIn pin = (OfmMutablePacketIn) mm;
        // Setting inPort/inPhyPort should throw a version mismatch exception
        pin.bufferId(BUFFER_ID).totalLen(TOTAL_LEN)
                .tableId(TABLE_ID).cookie(COOKIE).match(makeMatch13())
                .data(getExpByteArray(TF_LLDP));
        // finally...
        encodeAndVerifyMessage(mm.toImmutable(), TF_PIN_13);
    }

    private Match makeMatch13() {
        final ProtocolVersion pv = V_1_3;
        // assemble a match definition
        MutableMatch match = MatchFactory.createMatch(pv)
                .addField(createBasicField(pv, OxmBasicFieldType.IN_PORT, IN_PORT))
                .addField(createBasicField(pv, OxmBasicFieldType.ETH_DST, DST_MAC))
                .addField(createBasicField(pv, OxmBasicFieldType.ETH_SRC, SRC_MAC));
        return (Match) match.toImmutable();
    }

    @Test
    public void encodePacketIn10() {
        print(EOL + "encodePacketIn10()");
        final ProtocolVersion pv = V_1_0;

        mm = MessageFactory.create(pv, PACKET_IN);
        mm.clearXid();
        verifyMutableHeader(mm, pv, PACKET_IN, 0);

        // assemble the pieces
        OfmMutablePacketIn pin = (OfmMutablePacketIn) mm;
        pin.bufferId(BUFFER_ID).inPort(IN_PORT).totalLen(TOTAL_LEN)
                .reason(REASON);

        // get the frame data and add it
        byte[] frameData = getExpByteArray(TF_LLDP);
        pin.data(frameData);

        // finally...
        encodeAndVerifyMessage(mm.toImmutable(), TF_PIN_10);
    }

    @Test
    public void encodePacketIn10WithReason() {
        print(EOL + "encodePacketIn10WithReason()");
        final ProtocolVersion pv = V_1_0;
        mm = MessageFactory.create(pv, PACKET_IN, REASON);
        mm.clearXid();
        verifyMutableHeader(mm, pv, PACKET_IN, 0);
        // assemble the pieces
        OfmMutablePacketIn pin = (OfmMutablePacketIn) mm;
        pin.bufferId(BUFFER_ID).inPort(IN_PORT).totalLen(TOTAL_LEN)
                .data(getExpByteArray(TF_LLDP));
        // finally...
        encodeAndVerifyMessage(mm.toImmutable(), TF_PIN_10);
    }

    @Test
    public void createWithReason() {
        print(EOL + "createWithReason()");
        OfmMutablePacketIn m = (OfmMutablePacketIn)
                MessageFactory.create(V_1_3, PACKET_IN, REASON);
        m.clearXid();
        verifyMutableHeader(m, V_1_3, PACKET_IN, 0);
        assertEquals(AM_NEQ, REASON, m.getReason());
    }
}
