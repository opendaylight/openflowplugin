/*
 * (c) Copyright 2013-4 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.mp.MBodyFlowStats;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.Log;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.packet.Codec;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolId;

import java.util.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.msg.MessageFactory.parseMessage;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * A collection of unit tests for specific instances of OpenFlow messages,
 * collected from our switch team and the field...
 *
 * @author Simon Hunt
 */
public class MiscMessageTests extends AbstractTest {

    private static final String ROOT = "msg/misc/";
    private static final String TF_BAD = "badPacketIn.hex";
    private static final String CW_PI_A = "comwareBadPacketInA.hex";
    private static final String CW_PI_B = "comwareBadPacketInB.hex";
    private static final String BDDP_1759 = "bddpIssueFrame1759.hex";
    private static final String BDDP_1761 = "bddpIssueFrame1761.hex";
    private static final String THAMES_TF_CFG = "thamesTabFeatConfig.hex";
    private static final String COMWARE_TF_1275 = "comwareTabFeatFrame1275.hex";
    private static final String COMWARE_TF_1278 = "comwareTabFeatFrame1278.hex";
    private static final String SHANGHAI_BAD_FREP = "shanghaiBadFeaturesReply.hex";
    private static final String THAMES_BAD_VLAN_MATCH = "badThamesVlanVidMatchField.hex";
    private static final String NBIO_BAD = "nbioBadPacketIn.hex";
    private static final String BAD_IXIA = "badIxiaFeaturesReply.hex";
    private static final String LIEM_TRUNK_PORT = "liemTrunkPort.hex";
    private static final String CR_140743 = "cr140743.hex";
    private static final String BAD_NEC_A = "badNec13MpReplyA.hex";
    private static final String BAD_NEC_B = "badNec13MpReplyB.hex";
    private static final String BAD_NEC_C = "badNec13MpReplyC.hex";
    private static final String SENTINEL = "sentinel.hex";
    private static final String MANY_MULTIPARTS = "manyMultiparts.hex";
    private static final String INS_HEAD_LEN_0 = "flowStatsInsHeaderLenZero.hex";
    private static final String NEC_EXPR_MF = "necExprMatchFieldMpReply.hex";
    private static final String BAD_TABLE_FEATURES = "badTableFeatures.hex";
    private static final String RAPHAEL_FLOW = "raphaelBadFlowStats.hex";
    private static final String SHAILA_ONE = "shailaOneMissingPrereq.hex";
    private static final String SHAILA_TWO = "shailaTwoMissingPrereq.hex";
    private static final String SHAILA_THREE = "shailaThreeMissingPrereq.hex";
    private static final String JULIE_PRUNES = "jpFrame11.hex";

    
    private OfPacketReader getPkt(String file) {
        return getPacketReader(ROOT + file);
    }

    @Test @Ignore("keep for posterity")
    public void unparsedMessage() {
        print(EOL + "unparsedMessage()");
        try {
            parseMessage(getPkt(TF_BAD));
        } catch (MessageParseException e) {
            e.printStackTrace();
        }
    }

    @Test @Ignore("keep for posterity")
    public void analyseComwarePacketInA() {
        print(EOL + "analyseComwarePacketInA()");
        try {
            parseMessage(getPkt(CW_PI_A));
        } catch (MessageParseException e) {
            e.printStackTrace();
        }
    }

    @Test @Ignore("keep for posterity")
    public void analyseComwarePacketInB() {
        print(EOL + "analyseComwarePacketInB()");
        try {
            parseMessage(getPkt(CW_PI_B));
        } catch (MessageParseException e) {
            e.printStackTrace();
        }
    }

    private static final List<ProtocolId> PROTOCOLS = new ArrayList<ProtocolId>(
            Arrays.asList(ProtocolId.ETHERNET, ProtocolId.BDDP)
    );

    @Test @Ignore("keep for posterity")
    public void bddp1759And1761() throws MessageParseException {
        print(EOL + "bddp1759And1761()");
        print(EOL + "Frame 1759...");
        OpenflowMessage m = parseMessage(getPkt(BDDP_1759));
        print(m.toDebugString());
        OfmPacketOut pktOut = (OfmPacketOut) m;
        byte[] poData = pktOut.getData();

        print(EOL + "Frame 1761...");
        m = parseMessage(getPkt(BDDP_1761));
        print(m.toDebugString());
        OfmPacketIn pktIn = (OfmPacketIn) m;
        byte[] piData = pktIn.getData();

        assertArrayEquals(AM_NEQ, poData, piData);

        Packet p;
        try {
            // the following NO LONGER throws IndexOutOfBoundsException
            p = Codec.decodeEthernet(piData);
            print(EOL + "Packet decoded:");
            print(p.toDebugString());
            assertEquals(AM_NEQ, PROTOCOLS, p.protocolIds());
        } catch (Exception e) {
            e.printStackTrace();
            fail(AM_UNEX);
        }
    }


    private void verifyThamesTf(MBodyTableFeatures tf, int tid, String name,
                                long mMatch, long mWrite, long maxEnt) {
        assertEquals(AM_NEQ, tid(tid), tf.getTableId());
        assertEquals(AM_NEQ, name, tf.getName());
        assertEquals(AM_NEQ, mMatch, tf.getMetadataMatch());
        assertEquals(AM_NEQ, mWrite, tf.getMetadataWrite());
        assertEquals(AM_NEQ, maxEnt, tf.getMaxEntries());
        // All the table feature property types (except experimenter/-miss)...
        assertEquals(AM_NEQ, 14, tf.getProps().size());
    }

    private static final long ALL_1s = 0xffffffffffffffffL;

    @Test @Ignore("keep for posterity")
    public void thamesTableFeatureConfigBits() throws MessageParseException {
        print(EOL + "thamesTableFeatureConfigBits()");
        /* This Thames Table Features MP/Reply has 0x3 in the Config fields
         * (which we are now ignoring), and also non-zero payload lengths in
         * the OXM basic field headers of the OXM table properties structures
         * (which we are also ignoring now).
         */
        OpenflowMessage m = parseMessage(getPkt(THAMES_TF_CFG));
        print(m.toDebugString());
        // basic assertions as a sanity check
        OfmMultipartReply rep = (OfmMultipartReply) m;
        MBodyTableFeatures.Array body = (MBodyTableFeatures.Array) rep.getBody();
        assertEquals(AM_NEQ, 3, body.getList().size());
        Iterator<MBodyTableFeatures> iter = body.getList().iterator();
        verifyThamesTf(iter.next(), 0, "Start", 0, 0, 1);
        verifyThamesTf(iter.next(), 100, "Policy Table", 0, 0, 0);
        verifyThamesTf(iter.next(), 200, "SW Table 1", ALL_1s, ALL_1s, 65536);
    }

    @Test @Ignore("keep for posterity")
    public void comwareTableFeatures() throws MessageParseException {
        print(EOL + "comwareTableFeatures()");
        OpenflowMessage m;

        print(EOL + "Frame 1275...");
        m = parseMessage(getPkt(COMWARE_TF_1275));
        print(m.toDebugString());

        print(EOL + "Frame 1278...");
        m = parseMessage(getPkt(COMWARE_TF_1278));
        print(m.toDebugString());
    }

    @Test @Ignore("keep for posterity")
    public void digitalChinaShanghaiBadFeaturesReply()
            throws MessageParseException {
        print(EOL + "digitalChinaShanghai()");
        OpenflowMessage m = parseMessage(getPkt(SHANGHAI_BAD_FREP));
        print(m.toDebugString());
        OfmFeaturesReply fr = (OfmFeaturesReply) m;
        print(fr.getDpid().getMacAddress().getEthernetCompany());
    }

    @Test @Ignore("keep for posterity")
    public void thamesBadVlanVidMatchField() {
        print(EOL + "thamesBadVlanVidMatchField()");
        try {
            OpenflowMessage m = parseMessage(getPkt(THAMES_BAD_VLAN_MATCH));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
        }
    }

    @Test @Ignore("keep for posterity")
    public void nbioBadPacketIn() {
        print(EOL + "nbioBadPacketIn()");
        try {
            OpenflowMessage m = parseMessage(getPkt(NBIO_BAD));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
        }
    }

    @Test @Ignore("keep for posterity")
    public void ixiaBadFeaturesReply() {
        print(EOL + "ixiaBadFeaturesReply()");
        try {
            OpenflowMessage m = parseMessage(getPkt(BAD_IXIA));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
        }
    }

    @Test @Ignore("keep for posterity")
    public void liemTrunkPort() {
        print(EOL + "liemTrunkPort()");
        try {
            OpenflowMessage m = parseMessage(getPkt(LIEM_TRUNK_PORT));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
        }
    }

    @Test @Ignore("keep for posterity")
    public void cr140743() {
        print(EOL + "cr140743()");
        try {
            OpenflowMessage m = parseMessage(getPkt(CR_140743));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
        }
    }

    @Test @Ignore("keep for posterity")
    public void badNecA() {
        print(EOL + "badNecA()");
        try {
            OpenflowMessage m = parseMessage(getPkt(BAD_NEC_A));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
        }
    }

    @Test @Ignore("keep for posterity")
    public void badNecB() {
        print(EOL + "badNecB()");
        try {
            OpenflowMessage m = parseMessage(getPkt(BAD_NEC_B));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
        }
    }

    @Test @Ignore("keep for posterity")
    public void badNecC() {
        print(EOL + "badNecC()");
        try {
            OpenflowMessage m = parseMessage(getPkt(BAD_NEC_C));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
            print(FMT_EX_CAUSE, Log.stackTraceSnippet(e));
        }
    }


    private static final long BLOCK_COOKIE = 0xcbabe;
    private static final int BLOCK_PRIO = 9000;
    private static final int NO_TIMEOUT = 0;
    private static final Set<FlowModFlag> flowFlags = Collections.emptySet();

    @Test @Ignore("keep for posterity")
    public void sentinel() {
        print(EOL + "sentinel()");
        try {
            OpenflowMessage m = parseMessage(getPkt(SENTINEL));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
            print(FMT_EX_CAUSE, Log.stackTraceSnippet(e));
        }
        print("---");

        final ProtocolVersion pv = ProtocolVersion.V_1_0;
        MutableMatch mm = MatchFactory.createMatch(pv)
            .addField(createBasicField(pv, ETH_SRC, mac("6c:3b:35:39:f8:b4")))
            .addField(createBasicField(pv, IPV4_SRC, ip("192.168.10.3")))
            .addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        print(mm.toDebugString());

        OfPacketWriter pkt = new OfPacketWriter(64);
        MatchFactory.encodeMatch((Match) mm.toImmutable(), pkt);
        print(ByteUtils.hex(pkt.array()));

        print("-------");

        OfmMutableFlowMod fMod = (OfmMutableFlowMod) MessageFactory
                .create(pv, MessageType.FLOW_MOD);

        mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(pv, IPV4_SRC, ip("192.168.10.3")));
        mm.addField(createBasicField(pv, ETH_SRC, mac("6c:3b:35:39:f8:b4")));
        Match m = (Match) mm.toImmutable();
        fMod.cookie(BLOCK_COOKIE)
                .priority(BLOCK_PRIO)
                .bufferId(BufferId.NO_BUFFER)
                .hardTimeout(NO_TIMEOUT)
                .idleTimeout(NO_TIMEOUT)
                .match(m)
                .command(FlowModCommand.ADD)
                .flowModFlags(flowFlags);

        OfmFlowMod fm = (OfmFlowMod) fMod.toImmutable();
        print(fm.toDebugString());

        try {
            byte[] encoded = MessageFactory.encodeMessage(fm);
            print(ByteUtils.hex(encoded));
            print("Encoded length = {}", encoded.length);
        } catch (IncompleteMessageException | IncompleteStructureException e) {
            fail(AM_UNEX + " " + e);
        }
    }



    @Test
    public void manyMultiparts() throws MessageParseException {
        print("manyMultiparts()");
        OfPacketReader pkt = getPkt(MANY_MULTIPARTS);
        OfmMultipartReply reply;
        reply = parseFromReader(pkt);
        validateReply(reply, 108);
        reply = parseFromReader(pkt);
        validateReply(reply, 109);
        reply = parseFromReader(pkt);
        validateReply(reply, 110);
    }

    private OfmMultipartReply parseFromReader(OfPacketReader pkt) {
        OpenflowMessage m = null;
        try {
            m = parseMessage(pkt);
            print(m.toDebugString());
            print("---");
        } catch (MessageParseException e) {
            print(FMT_EX, e);
            print(FMT_EX_CAUSE, Log.stackTraceSnippet(e));
        }
        return (OfmMultipartReply) m;
    }

    private void validateReply(OfmMultipartReply reply, long xid) {
        print("{}** Validating {} .. **", EOL, xid);
        assertEquals(AM_NEQ, xid, reply.getXid());
        assertEquals(AM_NEQ, MultipartType.TABLE_FEATURES, reply.getMultipartType());
        List<MBodyTableFeatures> tflist =
                ((MBodyTableFeatures.Array) reply.getBody()).getList();
        assertEquals(AM_UXS, 2, tflist.size());
        print("** {} is good to go **", xid);
    }


    private static final String HEADER_LEN_ZERO_MSG =
            "MsgF:java.nio.HeapByteBuffer[pos=1020 lim=23072 cap=23072]" +
            " OFM:[V_1_3,MULTIPART_REPLY,23072,498744] > " +
            "org.opendaylight.of.lib.MessageParseException: " +
            "IF:java.nio.HeapByteBuffer[pos=1020 lim=23072 cap=23072]" +
            " > org.opendaylight.of.lib.HeaderParseException: V_1_3 bad " +
            "length for InstructionType.APPLY_ACTIONS expected " +
            "at least: 8 but found: 0";

    @Test @Ignore("keep for posterity")
    public void instructionHeaderLengthZero() {
        try {
            OpenflowMessage m = parseMessage(getPkt(INS_HEAD_LEN_0));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, HEADER_LEN_ZERO_MSG, e.getMessage());
        }
    }

    @Test @Ignore("keep for posterity")
    public void necExprMatchField() {
        print(EOL + "necExprMatchField()");
        try {
            OpenflowMessage m = parseMessage(getPkt(NEC_EXPR_MF));
            print(m.toDebugString());
        } catch (MessageParseException e) {
            print(e);
            fail(AM_UNEX);
        }
    }

    @Test @Ignore("keep for posterity")
    public void badTFReply() {
        print(EOL + "badTFReply()");
        try {
            OpenflowMessage m = parseMessage(getPkt(BAD_TABLE_FEATURES));
            print(m.toDebugString());
            fail(AM_NOEX);
        } catch (MessageParseException e) {
            print(FMT_EX, e);
        }
    }

    @Test @Ignore("keep for posterity")
    public void raphael() {
        OfPacketReader pkt;
        OpenflowMessage m;

        pkt = getPkt(RAPHAEL_FLOW);
        assertEquals(AM_NEQ, 1072, pkt.limit());

        try {
            m = parseMessage(pkt);

            // we know we have 10 good flows, and 1 bad
            assertEquals(AM_NEQ, MessageType.MULTIPART_REPLY, m.getType());
            OfmMultipartReply rep = (OfmMultipartReply) m;
            assertEquals(AM_NEQ, MultipartType.FLOW, rep.getMultipartType());
            MBodyFlowStats.Array array = (MBodyFlowStats.Array) rep.getBody();
            Throwable t = array.parseErrorCause();
            print(array.toDebugString());
            print(FMT_EX, t);
            assertTrue(AM_HUH, array.incomplete());
            assertNotNull("no exception captured", t);
            assertTrue(AM_WRCL, t instanceof MessageParseException);
            assertEquals(AM_NEQ, 10, array.getList().size());


        } catch (MessageParseException e) {
            // should not have an exception anymore
            print(e);
            fail(AM_UNEX);
        }
    }

    private void checkShaila(String filename) {
        try {
            OpenflowMessage m = parseMessage(getPkt(filename));
            print(m.toDebugString());
        } catch (MessageParseException e) {
//            print(FMT_EX, e);
        }
    }

    @Test @Ignore("but keep for posterity")
    public void shailaMissingPrereqs() {
        print(EOL + "shailaMissingPrereqs()");
        MessageFactory.setStrictMessageParsing(true);
        checkShaila(SHAILA_ONE);
        checkShaila(SHAILA_TWO);
        checkShaila(SHAILA_THREE);
    }

    @Test
    public void juliePrunes() {
        print(EOL + "juliePrunes()");
        MessageFactory.setStrictMessageParsing(true);
        OfPacketReader pkt = getPkt(JULIE_PRUNES);
        int counter = 0;
        while (pkt.readableBytes() > 0) {
            print("{} === Message {} === {}", EOL, counter++, EOL);
            try {
                OpenflowMessage m = parseMessage(pkt);
                print(m.toDebugString());
            } catch (MessageParseException e) {
                print(FMT_EX, e);
            }
        }
        print("{} === DONE === {}", EOL, EOL);
    }
}
