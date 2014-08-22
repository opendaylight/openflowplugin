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
import static org.opendaylight.of.lib.msg.MessageType.ECHO_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;


/**
 * Unit test for the OfmEchoRequest message.
 *
 * @author Scott Simes
 */
public class OfmEchoRequestTest extends OfmTest {

    // test files
    private static final String TF_ECREQ_10 = "v10/echoRequest";
    private static final String TF_ECREQ_NODATA_10 = "v10/echoRequestNoData";

    private static final String TF_ECREQ_11 = "v11/echoRequest";
    private static final String TF_ECREQ_NODATA_11 = "v11/echoRequestNoData";

    private static final String TF_ECREQ_12 = "v12/echoRequest";
    private static final String TF_ECREQ_NODATA_12 = "v12/echoRequestNoData";

    private static final String TF_ECREQ_13 = "v13/echoRequest";
    private static final String TF_ECREQ_NODATA_13 = "v13/echoRequestNoData";

    private static final int B = 256;

    private static final byte[] EXP_DATA =
        {0x9a-B, 0x9d-B, 0x15, 0x08, 0x88-B, 0xd8-B, 0, 0};


    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void echo13() {
        print(EOL + "echo13()");
        OfmEchoRequest msg =
        (OfmEchoRequest) verifyMsgHeader(TF_ECREQ_13, V_1_3, ECHO_REQUEST, 16);
        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
    }

    @Test
    public void echoNoData13() {
        print(EOL + "echoNoData13()");
        OfmEchoRequest msg =
        (OfmEchoRequest) verifyMsgHeader(TF_ECREQ_NODATA_13, V_1_3, ECHO_REQUEST, 8);
        assertNull(AM_HUH, msg.getData());
    }

    @Test
    public void echo12() {
        print(EOL + "echo12()");
        verifyNotSupported(TF_ECREQ_12);
//        OfmEchoRequest msg =
//        (OfmEchoRequest) verifyMsgHeader(TF_ECREQ_12, V_1_2, ECHO_REQUEST, 16);
//        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
    }

    @Test
    public void echoNoData12() {
        print(EOL + "echoNoData12()");
        verifyNotSupported(TF_ECREQ_NODATA_12);
//        OfmEchoRequest msg =
//        (OfmEchoRequest) verifyMsgHeader(TF_ECREQ_NODATA_12, V_1_2, ECHO_REQUEST, 8);
//        assertNull(AM_HUH, msg.getData());
    }

    @Test
    public void echo11() {
        print(EOL + "echo11()");
        verifyNotSupported(TF_ECREQ_11);
//        OfmEchoRequest msg =
//        (OfmEchoRequest) verifyMsgHeader(TF_ECREQ_11, V_1_1, ECHO_REQUEST, 16);
//        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
    }

    @Test
    public void echoNoData11() {
        print(EOL + "echoNoData11()");
        verifyNotSupported(TF_ECREQ_NODATA_11);
//        OfmEchoRequest msg =
//        (OfmEchoRequest) verifyMsgHeader(TF_ECREQ_NODATA_11, V_1_1, ECHO_REQUEST, 8);
//        assertNull(AM_HUH, msg.getData());
    }

    @Test
    public void echo10() {
        print(EOL + "echo10()");
        OfmEchoRequest msg =
        (OfmEchoRequest) verifyMsgHeader(TF_ECREQ_10, V_1_0, ECHO_REQUEST, 16);
        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
    }

    @Test
    public void echoNoData10() {
        print(EOL + "echoNoData10()");
        OfmEchoRequest msg =
        (OfmEchoRequest) verifyMsgHeader(TF_ECREQ_NODATA_10, V_1_0, ECHO_REQUEST, 8);
        assertNull(AM_HUH, msg.getData());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeEcho13() {
        print(EOL + "encodeEcho13()");
        mm = MessageFactory.create(V_1_3, ECHO_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, ECHO_REQUEST, 0);
        // add in the payload data
        OfmMutableEchoRequest rep = (OfmMutableEchoRequest) mm;
        rep.data(EXP_DATA);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECREQ_13);
    }

    @Test
    public void encodeEchoNoData13() {
        print(EOL + "encodeEchoNoData13()");
        mm = MessageFactory.create(V_1_3, ECHO_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, ECHO_REQUEST, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECREQ_NODATA_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeEcho12() {
        mm = MessageFactory.create(V_1_2, ECHO_REQUEST);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeEchoNoData12() {
        mm = MessageFactory.create(V_1_2, ECHO_REQUEST);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeEcho11() {
        mm = MessageFactory.create(V_1_1, ECHO_REQUEST);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeEchoNoData11() {
        mm = MessageFactory.create(V_1_1, ECHO_REQUEST);
    }

    @Test
    public void encodeEcho10() {
        print(EOL + "encodeEcho10()");
        mm = MessageFactory.create(V_1_0, ECHO_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, ECHO_REQUEST, 0);
        // add in the payload data
        OfmMutableEchoRequest rep = (OfmMutableEchoRequest) mm;
        rep.data(EXP_DATA);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECREQ_10);
    }

    @Test
    public void encodeEchoNoData10() {
        print(EOL + "encodeEchoNoData10()");
        mm = MessageFactory.create(V_1_0, ECHO_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, ECHO_REQUEST, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECREQ_NODATA_10);
    }
}
