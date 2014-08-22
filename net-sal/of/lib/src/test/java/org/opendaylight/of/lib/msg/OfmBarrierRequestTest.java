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
import static org.opendaylight.of.lib.msg.MessageType.BARRIER_REQUEST;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit test for the OfmBarrierRequestTest message.
 *
 * @author Shruthy Mohanram
 */
public class OfmBarrierRequestTest extends OfmTest {

    // test files
    private static final String TF_BREQ_10 = "v10/barrierRequest";
    private static final String TF_BREQ_11 = "v11/barrierRequest";
    private static final String TF_BREQ_12 = "v12/barrierRequest";
    private static final String TF_BREQ_13 = "v13/barrierRequest";

    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void barrierRequest13() {
        print(EOL + "barrierRequest13()");
        verifyMsgHeader(TF_BREQ_13, V_1_3, BARRIER_REQUEST, 8);
    }

    @Test
    public void barrierRequest12() {
        print(EOL + "barrierRequest12()");
        verifyNotSupported(TF_BREQ_12);
    }

    @Test
    public void barrierRequest11() {
        print(EOL + "barrierRequest11()");
        verifyNotSupported(TF_BREQ_11);
    }

    @Test
    public void barrierRequest10() {
        print(EOL + "barrierRequest10()");
        verifyMsgHeader(TF_BREQ_10, V_1_0, BARRIER_REQUEST, 8);
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeBarrierRequest13() {
        print(EOL + "encodeBarrierRequest13()");
        mm = MessageFactory.create(V_1_3, BARRIER_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, BARRIER_REQUEST, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_BREQ_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeBarrierRequest12() {
        mm = MessageFactory.create(V_1_2, BARRIER_REQUEST);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeBarrierRequest11() {
        mm = MessageFactory.create(V_1_1, BARRIER_REQUEST);
    }

    @Test
    public void encodeBarrierRequest10() {
        print(EOL + "encodeBarrierRequest10()");
        mm = MessageFactory.create(V_1_0, BARRIER_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, BARRIER_REQUEST, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_BREQ_10);
    }

}
