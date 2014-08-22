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

import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.BARRIER_REPLY;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit test for the OfmBarrierReply message.
 *
 * @author Shruthy Mohanram
 */
public class OfmBarrierReplyTest extends OfmTest  {

    // test files
    private static final String TF_BREP_10 = "v10/barrierReply";
    private static final String TF_BREP_11 = "v11/barrierReply";
    private static final String TF_BREP_12 = "v12/barrierReply";
    private static final String TF_BREP_13 = "v13/barrierReply";

    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void barrierReply13() {
        print(EOL + "barrierReply13()");
        verifyMsgHeader(TF_BREP_13, V_1_3, BARRIER_REPLY, 8);
    }

    @Test
    public void barrierReply12() {
        print(EOL + "barrierReply12()");
        verifyNotSupported(TF_BREP_12);
    }

    @Test
    public void barrierReply11() {
        print(EOL + "barrierReply11()");
        verifyNotSupported(TF_BREP_11);
    }

    @Test
    public void barrierRequest10() {
        print(EOL + "barrierReply10()");
        verifyMsgHeader(TF_BREP_10, V_1_0, BARRIER_REPLY, 8);
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeBarrierReply13() {
        print(EOL + "encodeBarrierReply13()");
        mm = MessageFactory.create(V_1_3, BARRIER_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, BARRIER_REPLY, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_BREP_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeBarrierReply12() {
        mm = MessageFactory.create(V_1_2, BARRIER_REPLY);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeBarrierReply11() {
        mm = MessageFactory.create(V_1_1, BARRIER_REPLY);
    }

    @Test
    public void encodeBarrierReply10() {
        print(EOL + "encodeBarrierReply10()");
        mm = MessageFactory.create(V_1_0, BARRIER_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, BARRIER_REPLY, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_BREP_10);
    }
}
