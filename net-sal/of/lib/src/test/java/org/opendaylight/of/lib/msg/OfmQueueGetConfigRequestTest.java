/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.util.net.BigPortNumber;

import static junit.framework.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.QUEUE_GET_CONFIG_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the {@link OfmQueueGetConfigRequest}
 */
public class OfmQueueGetConfigRequestTest extends OfmTest {

    // Test files...
    private static final String TF_QGC_13 = "v13/queueGetConfigRequest";
    private static final String TF_QGC_10 = "v10/queueGetConfigRequest";

    private static final int MSG_LEN_13 = 16;
    private static final int MSG_LEN_10 = 12;
    private static final BigPortNumber EXP_PORT = BigPortNumber.valueOf(0x0a);

    // ========================================================= PARSING ====

    @Test
    public void queueGetConfigRequest13() {
        print(EOL + "queueGetConfigRequest13()");
        OfmQueueGetConfigRequest msg =
                (OfmQueueGetConfigRequest) verifyMsgHeader(TF_QGC_13, V_1_3,
                        QUEUE_GET_CONFIG_REQUEST, MSG_LEN_13);
        assertEquals(AM_NEQ, EXP_PORT, msg.getPort());
    }

    @Test
    public void queueGetConfigRequest10() {
        print(EOL + "queueGetConfigRequest10()");
        OfmQueueGetConfigRequest msg =
                (OfmQueueGetConfigRequest) verifyMsgHeader(TF_QGC_10, V_1_0,
                        QUEUE_GET_CONFIG_REQUEST, MSG_LEN_10);
        assertEquals(AM_NEQ, EXP_PORT, msg.getPort());
    }


    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeQueueGetConfigRequest13() {
        print(EOL + "encodeQueueGetConfigRequest13()");
        OfmMutableQueueGetConfigRequest mod = (OfmMutableQueueGetConfigRequest)
                MessageFactory.create(V_1_3, QUEUE_GET_CONFIG_REQUEST);
        mod.clearXid();
        verifyMutableHeader(mod, V_1_3, QUEUE_GET_CONFIG_REQUEST, 0);
        mod.setPort(EXP_PORT);
        // now encode and verify
        encodeAndVerifyMessage(mod.toImmutable(), TF_QGC_13);
    }

    @Test
    public void encodeQueueGetConfigRequest10() {
        print(EOL + "encodeQueueGetConfigRequest10()");
        OfmMutableQueueGetConfigRequest mod = (OfmMutableQueueGetConfigRequest)
                MessageFactory.create(V_1_0, QUEUE_GET_CONFIG_REQUEST);
        mod.clearXid();
        verifyMutableHeader(mod, V_1_0, QUEUE_GET_CONFIG_REQUEST, 0);
        mod.setPort(EXP_PORT);
        // now encode and verify
        encodeAndVerifyMessage(mod.toImmutable(), TF_QGC_10);
    }
}
