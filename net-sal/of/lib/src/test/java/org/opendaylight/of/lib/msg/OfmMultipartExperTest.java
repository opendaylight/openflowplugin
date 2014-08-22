/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.mp.*;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.mp.MultipartType.EXPERIMENTER;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OfmMultipartRequest and OfmMultipartReply messages
 * of type MultipartType.EXPERIMENTER.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfmMultipartExperTest extends OfmMultipartTest {
    // Test files...
    private static final String TF_REQ_EXP_13 = "v13/mpRequestExperimenter";
    private static final String TF_REQ_UNEXP_13 = "v13/mpRequestExperimenter2";
    private static final String TF_REPLY_EXP_13 = "v13/mpReplyExperimenter";
    private static final String TF_REPLY_EXP_13_TWICE =
            "v13/mpReplyExperimenterTwice";

    private static final String TF_REQ_EXP_10 = "v10/statsRequestExperimenter";
    private static final String TF_REPLY_EXP_10 = "v10/statsReplyExperimenter";

    private static final int B = 256;

    private static final int MSG_LEN_13 = 64;
    private static final int MSG_LEN_10 = 56;
    private static final int EXP_TYPE = 42;

    private static final int INTEL_EXP_ID = 0x4c8093;

    private static final byte[] EXP_DATA_REQ = {
            0, 0, 0, 0x12, 0, 0, 0x01, 0x90-B, 0x45, 0x6e, 0x61, 0x62, 0x6c,
            0x65, 0x20, 0x53, 0x65, 0x6c, 0x66, 0x20, 0x44, 0x65, 0x73, 0x74,
            0x72, 0x75, 0x63, 0x74, 0x20, 0x53, 0x65, 0x71, 0x75, 0x65, 0x6e,
            0x63, 0x65, 0x21, 0, 0
    };

    private static final byte[] EXP_DATA_REPLY = {
            0, 0, 0, 0x02, 0x53, 0x65, 0x6c, 0x66, 0x20, 0x44, 0x65, 0x73,
            0x74, 0x72, 0x75, 0x63, 0x74, 0x20, 0x53, 0x65, 0x71, 0x75, 0x65,
            0x6e, 0x63, 0x65, 0x20, 0x49, 0x6e, 0x69, 0x74, 0x69, 0x61, 0x74,
            0x65, 0x64, 0x21, 0, 0, 0
    };

    // ========================================================= PARSING ====

    @Test
    public void mpRequestExperimenter13() {
        print(EOL + "mpRequestExperimenter13()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_EXP_13, V_1_3,
                        MULTIPART_REQUEST, MSG_LEN_13);

        MBodyExperimenter body =
                (MBodyExperimenter) verifyMpHeader(msg, EXPERIMENTER);

        assertEquals(AM_NEQ, ExperimenterId.HP, body.getExpId());
        assertEquals(AM_NEQ, EXP_TYPE, body.getExpType());
        assertArrayEquals(AM_NEQ, EXP_DATA_REQ, body.getData());
    }

    @Test
    public void mpRequestExperimenterUnknown13() {
        print(EOL + "mpRequestExperimenterUnknown13()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_UNEXP_13, V_1_3,
                        MULTIPART_REQUEST, MSG_LEN_13);

        MBodyExperimenter body =
                (MBodyExperimenter) verifyMpHeader(msg, EXPERIMENTER);

        assertNull(AM_HUH, body.getExpId());
        assertEquals(AM_NEQ, INTEL_EXP_ID, body.getId());
        assertEquals(AM_NEQ, EXP_TYPE, body.getExpType());
        assertArrayEquals(AM_NEQ, EXP_DATA_REQ, body.getData());
    }

    @Test
    public void statRequestExperimenter10() {
        print(EOL + "statRequestExperimenter10()");
        OfmMultipartRequest msg = (OfmMultipartRequest)
                verifyMsgHeader(TF_REQ_EXP_10, V_1_0,
                        MULTIPART_REQUEST, MSG_LEN_10);

        MBodyExperimenter body =
                (MBodyExperimenter) verifyMpHeader(msg, EXPERIMENTER);

        assertEquals(AM_NEQ, ExperimenterId.HP, body.getExpId());
        assertEquals(AM_NEQ, 0, body.getExpType());
        assertArrayEquals(AM_NEQ, EXP_DATA_REQ, body.getData());
    }

    @Test
    public void mpReplyExperimenter13() throws MessageParseException {
        print(EOL + "mpReplyExperimenter13()");
        OfPacketReader pkt = getOfmTestReader(TF_REPLY_EXP_13);
        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyExperimenter13(m);
    }

    @Test
    public void mpReplyExperimenter13Twice() throws MessageParseException {
        print(EOL + "mpReplyExperimenter13Twice()");
        OfPacketReader pkt = getOfmTestReader(TF_REPLY_EXP_13_TWICE);

        OpenflowMessage m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyExperimenter13(m);

        m = MessageFactory.parseMessage(pkt);
        print(FMT_START_TARGET, pkt.startIndex(), pkt.targetIndex());
        validateReplyExperimenter13(m);
    }

    private void validateReplyExperimenter13(OpenflowMessage m) {
        print(m.toDebugString());

        assertEquals(AM_NEQ, V_1_3, m.getVersion());
        assertEquals(AM_NEQ, MULTIPART_REPLY, m.getType());
        assertEquals(AM_UXS, 64, m.length());

        MBodyExperimenter body = (MBodyExperimenter)
                verifyMpHeader((OfmMultipartReply) m, EXPERIMENTER);

        assertEquals(AM_NEQ, ExperimenterId.HP, body.getExpId());
        assertEquals(AM_NEQ, EXP_TYPE, body.getExpType());
        assertArrayEquals(AM_NEQ, EXP_DATA_REPLY, body.getData());
    }

    @Test
    public void statsReplyExperimenter10() {
        print(EOL + "statsReplyExperimenter10()");
        OfmMultipartReply msg = (OfmMultipartReply)
                verifyMsgHeader(TF_REPLY_EXP_10, V_1_0,
                        MULTIPART_REPLY, MSG_LEN_10);

        MBodyExperimenter body =
                (MBodyExperimenter) verifyMpHeader(msg, EXPERIMENTER);

        assertEquals(AM_NEQ, ExperimenterId.HP, body.getExpId());
        assertEquals(AM_NEQ, 0, body.getExpType());
        assertArrayEquals(AM_NEQ, EXP_DATA_REPLY, body.getData());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMpRequestExperimenter13() {
        print(EOL + "encodeMpRequestExperimenter13()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.clearXid();

        MBodyMutableExperimenter exper = (MBodyMutableExperimenter)
                MpBodyFactory.createRequestBody(V_1_3, EXPERIMENTER);
        exper.expId(ExperimenterId.HP).expType(EXP_TYPE).data(EXP_DATA_REQ);
        req.body((MultipartBody) exper.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_EXP_13);
    }

    @Test
    public void encodeMpRequestExperimenter13WithMpType() {
        print(EOL + "encodeMpRequestExperimenter13WithMpType()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, EXPERIMENTER);
        req.clearXid();
        MBodyMutableExperimenter exper =
                (MBodyMutableExperimenter) req.getBody();
        exper.expId(ExperimenterId.HP).expType(EXP_TYPE).data(EXP_DATA_REQ);
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_EXP_13);
    }

    @Test
    public void encodeStatsRequestExperimenter10() {
        print(EOL + "encodeStatsRequestExperimenter10()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_0, MULTIPART_REQUEST);
        req.clearXid();

        MBodyMutableExperimenter exper = (MBodyMutableExperimenter)
                MpBodyFactory.createRequestBody(V_1_0, EXPERIMENTER);
        exper.expId(ExperimenterId.HP).data(EXP_DATA_REQ);
        req.body((MultipartBody) exper.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_EXP_10);
    }

    @Test
    public void encodeStatsRequestExperimenter10WithMpType() {
        print(EOL + "encodeStatsRequestExperimenterMp10()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_0, MULTIPART_REQUEST, EXPERIMENTER);
        req.clearXid();
        MBodyMutableExperimenter exper =
                (MBodyMutableExperimenter) req.getBody();
        exper.expId(ExperimenterId.HP).data(EXP_DATA_REQ);
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_EXP_10);
    }

    @Test
    public void encodeStatsRequestExperimenter10SetType() {
        print(EOL + "encodeStatsRequestExperimenter10SetType()");
        MBodyMutableExperimenter exper = (MBodyMutableExperimenter)
                MpBodyFactory.createRequestBody(V_1_0, EXPERIMENTER);
        exper.expId(ExperimenterId.HP);
        try {
            exper.expType(EXP_TYPE);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print("EX> " + e);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    @Test(expected = VersionNotSupportedException.class)
    public void createMpExperBodyRequest11() {
        MpBodyFactory.createRequestBody(V_1_1, MultipartType.EXPERIMENTER);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void createMpExperBodyRequest12() {
        MpBodyFactory.createRequestBody(V_1_2, MultipartType.EXPERIMENTER);
    }

    @Test
    public void encodeMpReplyExperimenter13() {
        print(EOL + "encodeMpReplyExperimenter13()");
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY);
        reply.clearXid();

        MBodyMutableExperimenter exper = (MBodyMutableExperimenter)
                MpBodyFactory.createReplyBody(V_1_3, EXPERIMENTER);
        exper.expId(ExperimenterId.HP).expType(EXP_TYPE).data(EXP_DATA_REPLY);
        reply.body((MultipartBody) exper.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(reply.toImmutable(), TF_REPLY_EXP_13);
    }

    @Test
    public void encodeMpReplyExperimenter13WithMpType() {
        print(EOL + "encodeMpReplyExperimenter13WithMpType()");
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, EXPERIMENTER);
        reply.clearXid();
        MBodyMutableExperimenter exper =
                (MBodyMutableExperimenter) reply.getBody();
        exper.expId(ExperimenterId.HP).expType(EXP_TYPE).data(EXP_DATA_REPLY);
        encodeAndVerifyMessage(reply.toImmutable(), TF_REPLY_EXP_13);
    }

    @Test
    public void encodeStatsReplyExperimenter10() {
        print(EOL + "encodeStatsReplyExperimenter10()");
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_0, MULTIPART_REPLY);
        reply.clearXid();

        MBodyMutableExperimenter exper = (MBodyMutableExperimenter)
                MpBodyFactory.createReplyBody(V_1_0, EXPERIMENTER);
        exper.expId(ExperimenterId.HP).data(EXP_DATA_REPLY);
        reply.body((MultipartBody) exper.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(reply.toImmutable(), TF_REPLY_EXP_10);
    }

    @Test
    public void encodeStatsReplyExperimenter10WithMpType() {
        print(EOL + "encodeStatsReplyExperimenter10WithMpType()");
        OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_0, MULTIPART_REPLY, EXPERIMENTER);
        reply.clearXid();
        MBodyMutableExperimenter exper =
                (MBodyMutableExperimenter) reply.getBody();
        exper.expId(ExperimenterId.HP).data(EXP_DATA_REPLY);
        encodeAndVerifyMessage(reply.toImmutable(), TF_REPLY_EXP_10);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void createMpExperBodyReply11() {
        MpBodyFactory.createReplyBody(V_1_1, MultipartType.EXPERIMENTER);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void createMpExperBodyReply12() {
        MpBodyFactory.createReplyBody(V_1_2, MultipartType.EXPERIMENTER);
    }

    // tests to verify that we don't (currently) support 1.1 or 1.2
    @Test(expected = VersionNotSupportedException.class)
    public void createMpExper11() {
        MessageFactory.create(V_1_1, MULTIPART_REQUEST);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void createMpExper12() {
        MessageFactory.create(V_1_2, MULTIPART_REQUEST);
    }
}
