/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MBodyMutableDesc;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.DESC;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for OfmMultipartRequest and OfmMultipartReply messages
 * of type MultipartType.DESC.
 *
 * @author Simon Hunt
 */
public class OfmMultipartDescTest extends OfmMultipartTest {

    // Test files...
    private static final String TF_REQ_DESC_13 = "v13/mpRequestDesc";
    private static final String TF_REP_DESC_13 = "v13/mpReplyDesc";

    private static final String TF_REQ_DESC_10 = "v10/statsRequestDesc";
    private static final String TF_REP_DESC_10 = "v10/statsReplyDesc";

    private static final String EXP_MFR =
            "Hewlett-Packard Development Company, L.P.";
    private static final String EXP_HW = "Foo Switch 6502";
    private static final String EXP_SW =
            "ProCurve OpenFlow 1.3 Capable build.25.112";
    private static final String EXP_SER = "EZ123OF13945";
    private static final String EXP_DP =
            "The switch at the end of the rainbow.";


    // ========================================================= PARSING ====

    @Test
    public void mpRequestDesc13() {
        print(EOL + "mpRequestDesc13()");
        OfmMultipartRequest msg =
                (OfmMultipartRequest) verifyMsgHeader(TF_REQ_DESC_13,
                        V_1_3, MULTIPART_REQUEST, 16);
        verifyMpHeader(msg, DESC);
    }

    @Test
    public void statRequestDesc10() {
        print(EOL + "statRequestDesc10()");
        OfmMultipartRequest msg =
                (OfmMultipartRequest) verifyMsgHeader(TF_REQ_DESC_10,
                        V_1_0, MULTIPART_REQUEST, 12);
        verifyMpHeader(msg, DESC);
    }

    @Test
    public void mpReplyDesc13() {
        print(EOL + "mpReplyDesc13()");
        OfmMultipartReply msg =
                (OfmMultipartReply) verifyMsgHeader(TF_REP_DESC_13,
                        V_1_3, MULTIPART_REPLY, 1072);

        MBodyDesc body = (MBodyDesc) verifyMpHeader(msg, DESC);
        assertEquals(AM_NEQ, EXP_MFR, body.getMfrDesc());
        assertEquals(AM_NEQ, EXP_HW, body.getHwDesc());
        assertEquals(AM_NEQ, EXP_SW, body.getSwDesc());
        assertEquals(AM_NEQ, EXP_SER, body.getSerialNum());
        assertEquals(AM_NEQ, EXP_DP, body.getDpDesc());
    }
    @Test
    public void mpReplyDesc10() {
        print(EOL + "mpReplyDesc10()");
        OfmMultipartReply msg =
                (OfmMultipartReply) verifyMsgHeader(TF_REP_DESC_10,
                        V_1_0, MULTIPART_REPLY, 1068);

        MBodyDesc body = (MBodyDesc) verifyMpHeader(msg, DESC);
        assertEquals(AM_NEQ, EXP_MFR, body.getMfrDesc());
        assertEquals(AM_NEQ, EXP_HW, body.getHwDesc());
        assertEquals(AM_NEQ, EXP_SW, body.getSwDesc());
        assertEquals(AM_NEQ, EXP_SER, body.getSerialNum());
        assertEquals(AM_NEQ, EXP_DP, body.getDpDesc());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeMpRequestDesc13() {
        print(EOL + "encodeMpRequestDesc13()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.clearXid();
        req.type(DESC);

        // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_DESC_13);
    }

    @Test
    public void encodeMpRequestDesc13WithType() {
        print(EOL + "encodeMpRequestDesc13WithType()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST, DESC);
        req.clearXid();
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_DESC_13);
    }

    @Test
    public void encodeStatsRequestDesc10() {
        print(EOL + "encodeStatsRequestDesc10()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_0, MULTIPART_REQUEST);
        req.clearXid();
        req.type(DESC);

         // now encode and verify
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_DESC_10);
    }

    @Test
    public void encodeStatsRequestDesc10WithType() {
        print(EOL + "encodeStatsRequestDesc10WithType()");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_0, MULTIPART_REQUEST, DESC);
        req.clearXid();
        encodeAndVerifyMessage(req.toImmutable(), TF_REQ_DESC_10);
    }

    @Test
    public void encodeMpReplyDesc13() {
        print(EOL + "encodeMpReplyDesc13()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY);
        rep.clearXid();

        MBodyMutableDesc desc =
                (MBodyMutableDesc) MpBodyFactory.createReplyBody(V_1_3, DESC);
        desc.mfrDesc(EXP_MFR).hwDesc(EXP_HW).swDesc(EXP_SW)
                .serialNum(EXP_SER).dpDesc(EXP_DP);
        rep.body((MultipartBody) desc.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_REP_DESC_13);
    }

    @Test
    public void encodeMpReplyDesc13WithType() {
        print(EOL + "encodeMpReplyDesc13WithType()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_3, MULTIPART_REPLY, DESC);
        rep.clearXid();
        MBodyMutableDesc desc = (MBodyMutableDesc) rep.getBody();
        desc.mfrDesc(EXP_MFR).hwDesc(EXP_HW).swDesc(EXP_SW)
                .serialNum(EXP_SER).dpDesc(EXP_DP);
        encodeAndVerifyMessage(rep.toImmutable(), TF_REP_DESC_13);
    }

    @Test
    public void encodeMpReplyDesc10() {
        print(EOL + "encodeMpReplyDesc10()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_0, MULTIPART_REPLY);
        rep.clearXid();

        MBodyMutableDesc desc =
                (MBodyMutableDesc) MpBodyFactory.createReplyBody(V_1_0, DESC);
        desc.mfrDesc(EXP_MFR).hwDesc(EXP_HW).swDesc(EXP_SW)
                .serialNum(EXP_SER).dpDesc(EXP_DP);
        rep.body((MultipartBody) desc.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(rep.toImmutable(), TF_REP_DESC_10);
    }

    @Test
    public void encodeMpReplyDesc10WithType() {
        print(EOL + "encodeMpReplyDesc10WithType()");
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(V_1_0, MULTIPART_REPLY, DESC);
        rep.clearXid();
        MBodyMutableDesc desc = (MBodyMutableDesc) rep.getBody();
        desc.mfrDesc(EXP_MFR).hwDesc(EXP_HW).swDesc(EXP_SW)
                .serialNum(EXP_SER).dpDesc(EXP_DP);
        encodeAndVerifyMessage(rep.toImmutable(), TF_REP_DESC_10);
    }

    @Test
    public void requestReply13() {
        print(EOL + "requestReply13()");
        OpenflowMessage request = MessageFactory.create(V_1_3,
                MULTIPART_REQUEST, DESC).toImmutable();
        print(request.toDebugString());
        OpenflowMessage reply = MessageFactory.create(request,
                MULTIPART_REPLY, DESC).toImmutable();
        print(reply.toDebugString());
        assertEquals(AM_NEQ, V_1_3, reply.getVersion());
        assertEquals(AM_NEQ, request.getXid(), reply.getXid());
    }
}
