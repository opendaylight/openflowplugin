/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.VersionNotSupportedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.ECHO_REPLY;
import static org.opendaylight.util.junit.TestTools.*;


/**
 * Unit test for the OfmEchoReply message.
 *
 * @author Scott Simes
 */
public class OfmEchoReplyTest extends OfmTest {

    // test files
    private static final String TF_ECREP_10 = "v10/echoReply";
    private static final String TF_ECREP_NODATA_10 = "v10/echoReplyNoData";

    private static final String TF_ECREP_11 = "v11/echoReply";
    private static final String TF_ECREP_NODATA_11 = "v11/echoReplyNoData";

    private static final String TF_ECREP_12 = "v12/echoReply";
    private static final String TF_ECREP_NODATA_12 = "v12/echoReplyNoData";

    private static final String TF_ECREP_13 = "v13/echoReply";
    private static final String TF_ECREP_NODATA_13 = "v13/echoReplyNoData";

    // private static final String EXP_STR = "Echo Echo Echo";
    // private static final int STR_FLD_LEN = 16;
    // private static final byte [] EXP_DATA
    //                      = ByteUtils.writeStringField(EXP_STR, STR_FLD_LEN);

    private static final byte [] EXP_DATA =
        {0x45, 0x63, 0x68, 0x6f, 0x20, 0x45, 0x63, 0x68, 0x6f, 0x20, 0x45,
         0x63, 0x68, 0x6f, 0, 0};

    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void echoReply13() {
        print(EOL + "echoReply13()");
        OfmEchoReply msg =
        (OfmEchoReply) verifyMsgHeader(TF_ECREP_13, V_1_3, ECHO_REPLY, 24);
        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
        // verifyString
        // ByteUtils.getStringField(mm.getData());
    }

    @Test
    public void echoReplyNoData13() {
        print(EOL + "echoReplyNoData13()");
        OfmEchoReply msg =
        (OfmEchoReply) verifyMsgHeader(TF_ECREP_NODATA_13, V_1_3, ECHO_REPLY, 8);
        assertNull(AM_HUH, msg.getData());
    }

    @Test
    public void echoReply12() {
        print(EOL + "echoReply12()");
        verifyNotSupported(TF_ECREP_12);
//        OfmEchoReply msg =
//        (OfmEchoReply) verifyMsgHeader(TF_ECREP_12, V_1_2, ECHO_REPLY, 24);
//        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
    }

    @Test
    public void echoReplyNoData12() {
        print(EOL + "echoReplyNoData12()");
        verifyNotSupported(TF_ECREP_NODATA_12);
//        OfmEchoReply msg =
//        (OfmEchoReply) verifyMsgHeader(TF_ECREP_NODATA_12, V_1_2, ECHO_REPLY, 8);
//        assertNull(AM_HUH, msg.getData());
    }

    @Test
    public void echoReply11() {
        print(EOL + "echoReply11()");
        verifyNotSupported(TF_ECREP_11);
//        OfmEchoReply msg =
//        (OfmEchoReply) verifyMsgHeader(TF_ECREP_11, V_1_1, ECHO_REPLY, 24);
//        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
    }

    @Test
    public void echoReplyNoData11() {
        print(EOL + "echoReplyNoData11()");
        verifyNotSupported(TF_ECREP_NODATA_11);
//        OfmEchoReply msg =
//        (OfmEchoReply) verifyMsgHeader(TF_ECREP_NODATA_11, V_1_1, ECHO_REPLY, 8);
//        assertNull(AM_HUH, msg.getData());
    }

    @Test
    public void echoReply10() {
        print(EOL + "echoReply10()");
        OfmEchoReply msg =
        (OfmEchoReply) verifyMsgHeader(TF_ECREP_10, V_1_0, ECHO_REPLY, 24);
        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
    }

    @Test
    public void echoReplyNoData10() {
        print(EOL + "echoReplyNoData10()");
        OfmEchoReply msg =
        (OfmEchoReply) verifyMsgHeader(TF_ECREP_NODATA_10, V_1_0, ECHO_REPLY, 8);
        assertNull(AM_HUH, msg.getData());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeEchoReply13() {
        print(EOL + "encodeEchoReply13()");
        mm = MessageFactory.create(V_1_3, ECHO_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, ECHO_REPLY, 0);
        // add in the payload data
        OfmMutableEchoReply rep = (OfmMutableEchoReply) mm;
        rep.data(EXP_DATA);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECREP_13);

    }
//
    @Test
    public void encodeEchoReplyNoData13() {
        print(EOL + "encodeEchoReplyNoData13()");
        mm = MessageFactory.create(V_1_3, ECHO_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, ECHO_REPLY, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECREP_NODATA_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeEchoReply12() {
        mm = MessageFactory.create(V_1_2, ECHO_REPLY);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeEchoReplyNoData12() {
        mm = MessageFactory.create(V_1_2, ECHO_REPLY);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeEchoReply11() {
        mm = MessageFactory.create(V_1_1, ECHO_REPLY);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeEchoReplyNoData11() {
        mm = MessageFactory.create(V_1_1, ECHO_REPLY);
    }

    @Test
    public void encodeEchoReply10() {
        print(EOL + "encodeEchoReply10()");
        mm = MessageFactory.create(V_1_0, ECHO_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, ECHO_REPLY, 0);
        // add in the payload data
        OfmMutableEchoReply rep = (OfmMutableEchoReply) mm;
        rep.data(EXP_DATA);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECREP_10);
    }

    @Test
    public void encodeEchoReplyNoData10() {
        print(EOL + "encodeEchoReplyNoData10()");
        mm = MessageFactory.create(V_1_0, ECHO_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, ECHO_REPLY, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECREP_NODATA_10);
    }
}
