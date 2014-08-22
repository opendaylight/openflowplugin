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
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.util.net.*;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.msg.MessageType.FLOW_REMOVED;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test class for OfmFlowRemoved message.
 *
 * @author Sudheer Duggisetty
 * @author Simon Hunt
 */
public class OfmFlowRemovedTest extends OfmTest {

    /** Flow Removed message in Hex format. */
    private static final String FR_MSG_HEX_FILE_V_1_3 = "v13/flowRemoved";

    private static final String FR_MSG_HEX_FILE_V_1_0 = "v10/flowRemoved";

    /** Expected values. */
    private static final long COOKIE = 0x1234;
    private static final int PRIORITY = 64;
    private static final FlowRemovedReason REASON =
            FlowRemovedReason.IDLE_TIMEOUT;
    private static final TableId TABLE_ID = tid(3);
    private static final int DURATION_SEC = 30;
    private static final int DURATION_NANO_SEC = 30;
    private static final int IDLE_TIMEOUT = 30;
    private static final int HARD_TIMEOUT = 300;
    private static final long PACKET_COUNT = 4660;
    private static final long BYTE_COUNT = 4660;
    private static final BigPortNumber EXP_IN_PORT = bpn(0x0e);
    private static final MacAddress EXP_DST_MAC = mac("00001e:222222");
    private static final MacAddress EXP_SRC_MAC = mac("00001e:111111");
    private static final MacAddress EXP_SRC_MAC_V3 = mac("112233:445566");
    private static final MacAddress EXP_MAC_MASK_V3 = mac("ffffff:000000");
    private static final PortNumber EXP_MF_V4 = pn(25);
    private static final int EXP_IP_DSCP = 5;
    private static final IpAddress EXP_SRC_IP = ip("15.255.76.3");
    private static final IpAddress EXP_DST_IP = ip("15.255.77.1");
    private static final IpAddress EXP_SRC_IP_MASK = ip("255.255.255.0");
    private static final IpAddress EXP_DST_IP_MASK = ip("255.240.0.0");
    private static final PortNumber EXP_TCP_SRC_PORT = pn(25);
    private static final PortNumber EXP_TCP_DST_PORT = pn(32001);

    private MutableMessage mm;

    private void verifyFlowRemovedHeader(OfmFlowRemoved flowRem,
            long expCook, int expPrior, FlowRemovedReason expRea,
            TableId expTabId, int expDurSec, int expDurNanSec,
            int expIdleTo, int expHardTo,
            long expPktCnt, long expByCnt) {

        assertEquals(AM_NEQ, expCook, flowRem.getCookie());
        assertEquals(AM_NEQ, expPrior, flowRem.getPriority());
        assertEquals(AM_NEQ, expRea, flowRem.getReason());

        if (flowRem.getVersion() == V_1_3) {
            assertEquals(AM_NEQ, expTabId, flowRem.getTableId());
            assertEquals(AM_NEQ, expHardTo, flowRem.getHardTimeout());
        }

        assertEquals(AM_NEQ, expDurSec, flowRem.getDurationSeconds());
        assertEquals(AM_NEQ, expDurNanSec, flowRem.getDurationNanoSeconds());
        assertEquals(AM_NEQ, expIdleTo, flowRem.getIdleTimeout());

        assertEquals(AM_NEQ, expPktCnt, flowRem.getPacketCount());
        assertEquals(AM_NEQ, expByCnt, flowRem.getByteCount());
    }

    @Test
    public void flowRemoved13() {
        print(EOL + "flowRemoved13()");

        OfmFlowRemoved msg = (OfmFlowRemoved)
                verifyMsgHeader(FR_MSG_HEX_FILE_V_1_3, V_1_3, FLOW_REMOVED, 88);

        verifyFlowRemovedHeader(msg, COOKIE, PRIORITY, REASON, TABLE_ID,
                DURATION_SEC, DURATION_NANO_SEC, IDLE_TIMEOUT, HARD_TIMEOUT,
                PACKET_COUNT, BYTE_COUNT);

        Match match = msg.getMatch();
        assertNotNull(AM_HUH, match);
        print(match.toDebugString());

        Iterator<MatchField> mfIter = match.getMatchFields().iterator();
        verifyMatchField(mfIter.next(), ETH_SRC, EXP_SRC_MAC_V3, EXP_MAC_MASK_V3);
        verifyMatchField(mfIter.next(), ETH_TYPE, EthernetType.IPv4);
        verifyMatchField(mfIter.next(), IP_PROTO, IpProtocol.TCP);
        verifyMatchField(mfIter.next(), TCP_DST, EXP_MF_V4);
        assertFalse(AM_HUH, mfIter.hasNext());
    }

    @Test
    public void flowRemoved10() {
        print(EOL + "flowRemoved10()");

        OfmFlowRemoved msg = (OfmFlowRemoved)
                verifyMsgHeader(FR_MSG_HEX_FILE_V_1_0, V_1_0, FLOW_REMOVED, 88);

        verifyFlowRemovedHeader(msg, COOKIE, PRIORITY, REASON, null,
                DURATION_SEC, DURATION_NANO_SEC, IDLE_TIMEOUT, -1,
                PACKET_COUNT, BYTE_COUNT);

        Match match = msg.getMatch();
        assertNotNull(AM_HUH, match);
        print(match.toDebugString());

        // NOTE: match fields are returned in the order defined in
        //  OxmBasicFieldType enum...
        Iterator<MatchField> mfIter = match.getMatchFields().iterator();
        verifyMatchField(mfIter.next(), IN_PORT, EXP_IN_PORT);
        verifyMatchField(mfIter.next(), ETH_DST, EXP_DST_MAC);
        verifyMatchField(mfIter.next(), ETH_SRC, EXP_SRC_MAC);
        verifyMatchField(mfIter.next(), ETH_TYPE, EthernetType.IPv4);
        verifyMatchField(mfIter.next(), IP_DSCP, EXP_IP_DSCP);
        verifyMatchField(mfIter.next(), IP_PROTO, IpProtocol.TCP);
        verifyMatchField(mfIter.next(), IPV4_SRC, EXP_SRC_IP, EXP_SRC_IP_MASK);
        verifyMatchField(mfIter.next(), IPV4_DST, EXP_DST_IP, EXP_DST_IP_MASK);
        verifyMatchField(mfIter.next(), TCP_SRC, EXP_TCP_SRC_PORT);
        verifyMatchField(mfIter.next(), TCP_DST, EXP_TCP_DST_PORT);
        assertFalse(AM_HUH, mfIter.hasNext());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeFlowRemoved13() {
        print(EOL + "encodeFlowRemoved13()");
        final ProtocolVersion pv = V_1_3;
        mm = MessageFactory.create(pv, FLOW_REMOVED);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, FLOW_REMOVED, 0);

        // set the fixed fields
        OfmMutableFlowRemoved rem = (OfmMutableFlowRemoved) mm;
        rem.cookie(COOKIE).priority(PRIORITY).reason(REASON).tableId(TABLE_ID)
                .duration(DURATION_SEC, DURATION_NANO_SEC)
                .idleTimeout(IDLE_TIMEOUT).hardTimeout(HARD_TIMEOUT)
                .packetCount(PACKET_COUNT).byteCount(BYTE_COUNT)
                .match(createMatch13());
        print(mm.toDebugString() + EOL);

        // now encode and verify
        encodeAndVerifyMessage(mm.toImmutable(), FR_MSG_HEX_FILE_V_1_3);
    }

    @Test
    public void encodeFlowRemoved13WithReason() {
        print(EOL + "encodeFlowRemoved13WithReason()");
        final ProtocolVersion pv = V_1_3;
        mm = MessageFactory.create(pv, FLOW_REMOVED, REASON);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, FLOW_REMOVED, 0);
        OfmMutableFlowRemoved rem = (OfmMutableFlowRemoved) mm;
        rem.cookie(COOKIE).priority(PRIORITY).tableId(TABLE_ID)
                .duration(DURATION_SEC, DURATION_NANO_SEC)
                .idleTimeout(IDLE_TIMEOUT).hardTimeout(HARD_TIMEOUT)
                .packetCount(PACKET_COUNT).byteCount(BYTE_COUNT)
                .match(createMatch13());
        print(mm.toDebugString() + EOL);
        encodeAndVerifyMessage(mm.toImmutable(), FR_MSG_HEX_FILE_V_1_3);
    }

    private Match createMatch13() {
        final ProtocolVersion pv = V_1_3;
        // assemble a match definition
        MutableMatch match = MatchFactory.createMatch(pv)
                .addField(createBasicField(pv, ETH_SRC, EXP_SRC_MAC_V3, EXP_MAC_MASK_V3))
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(pv, IP_PROTO, IpProtocol.TCP))
                .addField(createBasicField(pv, TCP_DST, EXP_MF_V4));
        return (Match) match.toImmutable();
    }

    @Test
    public void encodeFlowRemoved10() {
        print(EOL + "encodeFlowRemoved10()");
        final ProtocolVersion pv = V_1_0;
        mm = MessageFactory.create(pv, FLOW_REMOVED);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, FLOW_REMOVED, 0);

        // set the fixed fields
        OfmMutableFlowRemoved rem = (OfmMutableFlowRemoved)mm;
        rem.cookie(COOKIE).priority(PRIORITY).reason(REASON)
                .duration(DURATION_SEC, DURATION_NANO_SEC)
                .idleTimeout(IDLE_TIMEOUT).packetCount(PACKET_COUNT)
                .byteCount(BYTE_COUNT)
                .match(createMatch10());
        print(mm.toDebugString() + EOL);

        // now encode and verify
        encodeAndVerifyMessage(mm.toImmutable(), FR_MSG_HEX_FILE_V_1_0);
    }

    @Test
    public void encodeFlowRemoved10WithReason() {
        print(EOL + "encodeFlowRemoved10WithReason()");
        final ProtocolVersion pv = V_1_0;
        mm = MessageFactory.create(pv, FLOW_REMOVED, REASON);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, FLOW_REMOVED, 0);
        OfmMutableFlowRemoved rem = (OfmMutableFlowRemoved)mm;
        rem.cookie(COOKIE).priority(PRIORITY)
                .duration(DURATION_SEC, DURATION_NANO_SEC)
                .idleTimeout(IDLE_TIMEOUT).packetCount(PACKET_COUNT)
                .byteCount(BYTE_COUNT)
                .match(createMatch10());
        print(mm.toDebugString() + EOL);
        encodeAndVerifyMessage(mm.toImmutable(), FR_MSG_HEX_FILE_V_1_0);
    }

    private Match createMatch10() {
        final ProtocolVersion pv = V_1_0;
        // assemble a match definition
        MutableMatch match = MatchFactory.createMatch(pv)
                .addField(createBasicField(pv, IN_PORT, EXP_IN_PORT))
                .addField(createBasicField(pv, ETH_SRC, EXP_SRC_MAC))
                .addField(createBasicField(pv, ETH_DST, EXP_DST_MAC))
                .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(pv, IP_DSCP, EXP_IP_DSCP))
                .addField(createBasicField(pv, IP_PROTO, IpProtocol.TCP))
                .addField(createBasicField(pv, IPV4_SRC, EXP_SRC_IP, EXP_SRC_IP_MASK))
                .addField(createBasicField(pv, IPV4_DST, EXP_DST_IP, EXP_DST_IP_MASK))
                .addField(createBasicField(pv, TCP_SRC, EXP_TCP_SRC_PORT))
                .addField(createBasicField(pv, TCP_DST, EXP_TCP_DST_PORT));
        return (Match) match.toImmutable();
    }

    @Test
    public void createWithReason() {
        print(EOL + "createWithReason()");
        OfmMutableFlowRemoved m = (OfmMutableFlowRemoved)
                MessageFactory.create(V_1_3, FLOW_REMOVED,
                        FlowRemovedReason.HARD_TIMEOUT);
        m.clearXid();
        verifyMutableHeader(m, V_1_3, FLOW_REMOVED, 0);
        assertEquals(AM_NEQ, FlowRemovedReason.HARD_TIMEOUT, m.getReason());
    }
}
