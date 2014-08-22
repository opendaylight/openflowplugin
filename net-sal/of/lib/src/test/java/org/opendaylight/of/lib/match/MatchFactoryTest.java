/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.ICMPv4Type;
import org.opendaylight.util.net.IpProtocol;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.msg.MessageType.FLOW_MOD;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Some unit tests for MatchFactory.
 * <p>
 * Please note that Unit testing of {@link MatchFactory#parseMatch} is
 * performed in {@link MatchSampleTest}.
 *
 * @author Simon Hunt
 */
public class MatchFactoryTest extends OfmTest {

    @Test
    public void calcPadding() {
        print(EOL + "calcPadding()");
        checkPadding(0, 0, 0);
        checkPadding(1, 7, 8);
        checkPadding(2, 6, 8);
        checkPadding(3, 5, 8);
        checkPadding(4, 4, 8);
        checkPadding(5, 3, 8);
        checkPadding(6, 2, 8);
        checkPadding(7, 1, 8);
        checkPadding(8, 0, 8);
        checkPadding(9, 7, 16);
        checkPadding(10, 6, 16);
        checkPadding(11, 5, 16);
        checkPadding(12, 4, 16);
        checkPadding(13, 3, 16);
        checkPadding(14, 2, 16);
        checkPadding(15, 1, 16);
        checkPadding(16, 0, 16);
        checkPadding(17, 7, 24);
    }

    private void checkPadding(int len, int expPad, int expTotal) {
        int pad = MatchFactory.calcPadding(len);
        int total = len + pad;
        print("  " + len + " -> " + pad + " (total=" + total + ")");
        assertEquals(AM_NEQ, expPad, pad);
        assertEquals(AM_NEQ, expTotal, total);
    }

    // Miscellaneous unit tests

    @Test
    public void tcpMatchField()
            throws IncompleteStructureException, IncompleteMessageException {
        print(EOL + "tcpMatchField()");
        MutableMatch mm = createMatch(V_1_0);
        mm.addField(createBasicField(V_1_0, ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(V_1_0, IP_PROTO, IpProtocol.TCP));
        mm.addField(createBasicField(V_1_0, TCP_SRC, pn(42)));
        mm.addField(createBasicField(V_1_0, TCP_DST, pn(43)));
        Match m = (Match) mm.toImmutable();

        OfmMutableFlowMod mfm = (OfmMutableFlowMod)
                MessageFactory.create(V_1_0, FLOW_MOD, FlowModCommand.ADD);
        mfm.bufferId(BufferId.NO_BUFFER)
                .match(m);
        OfmFlowMod fm = (OfmFlowMod) mfm.toImmutable();
        print(fm.toDebugString());
        // so far, so good...
        byte[] b = MessageFactory.encodeMessage(fm);
        assertTrue(AM_HUH, b.length > 8);
        // actually the assert is arbitrary - this test will pass as long as
        //  we don't throw any exception while encoding.
    }

    @Test
    public void udpMatchField()
            throws IncompleteStructureException, IncompleteMessageException {
        print(EOL + "udpMatchField()");
        MutableMatch mm = createMatch(V_1_0);
        mm.addField(createBasicField(V_1_0, ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(V_1_0, IP_PROTO, IpProtocol.UDP));
        mm.addField(createBasicField(V_1_0, UDP_SRC, pn(42)));
        mm.addField(createBasicField(V_1_0, UDP_DST, pn(43)));
        Match m = (Match) mm.toImmutable();

        OfmMutableFlowMod mfm = (OfmMutableFlowMod)
                MessageFactory.create(V_1_0, FLOW_MOD, FlowModCommand.ADD);
        mfm.bufferId(BufferId.NO_BUFFER)
                .match(m);
        OfmFlowMod fm = (OfmFlowMod) mfm.toImmutable();
        print(fm.toDebugString());
        // so far, so good...
        byte[] b = MessageFactory.encodeMessage(fm);
        assertTrue(AM_HUH, b.length > 8);
        // actually the assert is arbitrary - this test will pass as long as
        //  we don't throw any exception while encoding.
    }

    private static final String MATCH_ICMP = "v10/matchWithIcmp";

    private byte[] getExpBytes(String filename) {
        return getExpByteArray("../match/" + filename);
    }
    private OfPacketReader getPkt(String basename) {
        return getPacketReader("match/" + basename + HEX);
    }


    private void encodeAndVerifyMatch(String label, Match m, String fname) {
        print(m.toDebugString());
        byte[] expData = getExpBytes(fname);
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        MatchFactory.encodeMatch(m, pkt);
        byte[] encoded = pkt.array();
        debugPrint(label, expData, encoded);
        assertArrayEquals(AM_NEQ, expData, encoded);
    }

    @Test
    public void encodeIcmpTypeAndCodeFields() {
        print(EOL + "encodeIcmpTypeAndCodeFields()");
        MutableMatch mm = createMatch(V_1_3);
        mm.addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(V_1_3, IP_PROTO, IpProtocol.ICMP));
        mm.addField(createBasicField(V_1_3, ICMPV4_TYPE, ICMPv4Type.ECHO_REQ));
        mm.addField(createBasicField(V_1_3, ICMPV4_CODE, 7));
        Match m = (Match) mm.toImmutable();
        print(m.toDebugString());
        assertEquals(AM_UXS, 4, m.getMatchFields().size());

        mm = createMatch(V_1_0);
        mm.addField(createBasicField(V_1_0, ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(V_1_0, IP_PROTO, IpProtocol.ICMP));
        mm.addField(createBasicField(V_1_0, ICMPV4_TYPE, ICMPv4Type.ECHO_REQ));
        mm.addField(createBasicField(V_1_0, ICMPV4_CODE, 7));
        m = (Match) mm.toImmutable();
        print(m.toDebugString());
        assertEquals(AM_UXS, 4, m.getMatchFields().size());

        // verify that the match encodes correctly..
        OfPacketWriter pkt = new OfPacketWriter(m.getTotalLength());
        MatchFactory.encodeMatch(m, pkt);
        byte[] b = pkt.array();
        print(ByteUtils.toHexString(b));
        // TODO: compare with expected structure from .hex file
        encodeAndVerifyMatch("encodeMatchIcmp", m, MATCH_ICMP);
    }

    @Test
    public void parseIcmpTypeAndCodeFields() {
        print(EOL + "parseIcmpTypeAndCodeFields()");
        OfPacketReader pkt = getPkt(MATCH_ICMP);
        try {
            Match m = MatchFactory.parseMatch(pkt, V_1_0);
            print(m.toDebugString());

            // see matchWithIcmp.hex for expected values
            assertEquals(AM_NEQ, MatchType.STANDARD, m.getMatchType());
            assertEquals(AM_NEQ, 40, m.header.length); // no getter!
            assertEquals(AM_NEQ, 4, m.getMatchFields().size());
            Iterator<MatchField> mfIter = m.getMatchFields().iterator();

            MfbEthType met = (MfbEthType) mfIter.next();
            assertEquals(AM_NEQ, EthernetType.IPv4, met.getEthernetType());

            MfbIpProto mip = (MfbIpProto) mfIter.next();
            assertEquals(AM_NEQ, IpProtocol.ICMP, mip.getIpProtocol());

            MfbIcmpv4Type mi4t = (MfbIcmpv4Type) mfIter.next();
            assertEquals(AM_NEQ, ICMPv4Type.ECHO_REQ, mi4t.getICMPv4Type());

            MfbIcmpv4Code mi4c = (MfbIcmpv4Code) mfIter.next();
            assertEquals(AM_NEQ, 7, mi4c.getValue());


        } catch (MessageParseException mpe) {
            print(mpe);
            fail("failed to parse match struct:" + mpe);
        }
        checkEOBuffer(pkt);
    }

}
