/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.util.net.BigPortNumber;

import static junit.framework.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.QUEUE_GET_CONFIG_REPLY;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the {@link OfmQueueGetConfigReply}
 *
 * @author Scott Simes
 */
public class OfmQueueGetConfigReplyTest extends OfmTest {

    // Test files...
    private static final String TF_QGCR_13 = "v13/queueGetConfigReply";

    private static final String TF_QGCR_10 = "v10/queueGetConfigReply";

    private static final BigPortNumber EXP_PORT = BigPortNumber.valueOf(0x0a);
    private static final QueueId EXP_QUEUE_ID_1 = QueueId.valueOf(0x4b);
    private static final QueueId EXP_QUEUE_ID_2 = QueueId.valueOf(0x19);

    private static final byte[] DATA_BYTES = {
            // a bunch of primes...
            1,  2,  3,  5,  7, 11, 13, 17,
            19, 23, 29, 31, 37, 41, 43, 47
    };

    // ========================================================= PARSING ====

    @Test
    public void queueGetConfigReply13() {
        print(EOL + "queueGetConfigReply13()");
        OfmQueueGetConfigReply msg = (OfmQueueGetConfigReply)
                verifyMsgHeader(TF_QGCR_13, V_1_3, QUEUE_GET_CONFIG_REPLY, 112);
        assertEquals(AM_NEQ, EXP_PORT, msg.getPort());
        assertEquals(AM_NEQ, 2, msg.getQueues().size());
        Queue q1 = msg.getQueues().get(0);
        Queue q2 = msg.getQueues().get(1);
        assertEquals(AM_NEQ, EXP_QUEUE_ID_1, q1.getId());
        assertEquals(AM_NEQ, EXP_QUEUE_ID_2, q2.getId());
        assertEquals(AM_NEQ, 2, q1.getProps().size());
        assertEquals(AM_NEQ, 1, q2.getProps().size());
    }

    @Test
    public void queueGetConfigReply10() {
        print(EOL + "queueGetConfigReply10()");
        OfmQueueGetConfigReply msg = (OfmQueueGetConfigReply)
                verifyMsgHeader(TF_QGCR_10, V_1_0, QUEUE_GET_CONFIG_REPLY, 64);
        assertEquals(AM_NEQ, EXP_PORT, msg.getPort());
        assertEquals(AM_NEQ, 2, msg.getQueues().size());
        Queue q1 = msg.getQueues().get(0);
        Queue q2 = msg.getQueues().get(1);
        assertEquals(AM_NEQ, EXP_QUEUE_ID_1, q1.getId());
        assertEquals(AM_NEQ, EXP_QUEUE_ID_2, q2.getId());
        assertEquals(AM_NEQ, 1, q1.getProps().size());
        assertEquals(AM_NEQ, 1, q2.getProps().size());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeQueueGetConfigReply13() {
        print(EOL + "encodeQueueGetConfigReply13()");
        OfmMutableQueueGetConfigReply mut = (OfmMutableQueueGetConfigReply)
                MessageFactory.create(V_1_3, QUEUE_GET_CONFIG_REPLY);
        mut.clearXid();
        verifyMutableHeader(mut, V_1_3, QUEUE_GET_CONFIG_REPLY, 0);
        mut.setPort(EXP_PORT);

        MutableQueue mutQueue1 = QueueFactory.createQueue(V_1_3);
        mutQueue1.id(EXP_QUEUE_ID_1);
        mutQueue1.port(EXP_PORT);
        QueueProperty min =
                QueueFactory.createProperty(QueuePropType.MIN_RATE, 570);
        mutQueue1.addProperty(min);
        QueueProperty max =
                QueueFactory.createProperty(QueuePropType.MAX_RATE, 999);
        mutQueue1.addProperty(max);

        MutableQueue mutQueue2 = QueueFactory.createQueue(V_1_3);
        mutQueue2.id(EXP_QUEUE_ID_2);
        mutQueue2.port(EXP_PORT);
        QueueProperty expProp =
                QueueFactory.createProperty(QueuePropType.EXPERIMENTER,
                        ExperimenterId.HP, DATA_BYTES);
        mutQueue2.addProperty(expProp);

        mut.addQueue((Queue) mutQueue1.toImmutable());
        mut.addQueue((Queue) mutQueue2.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(mut.toImmutable(), TF_QGCR_13);
    }

    @Test
    public void encodeQueueGetConfigReply10() {
        print(EOL + "encodeQueueGetConfigReply10()");
        OfmMutableQueueGetConfigReply mut = (OfmMutableQueueGetConfigReply)
                MessageFactory.create(V_1_0, QUEUE_GET_CONFIG_REPLY);
        mut.clearXid();
        verifyMutableHeader(mut, V_1_0, QUEUE_GET_CONFIG_REPLY, 0);
        mut.setPort(EXP_PORT);

        MutableQueue mutableQueue1 = QueueFactory.createQueue(V_1_0);
        mutableQueue1.id(EXP_QUEUE_ID_1);
        QueueProperty min1 =
                QueueFactory.createProperty(QueuePropType.MIN_RATE, 570);
        mutableQueue1.addProperty(min1);

        MutableQueue mutableQueue2 = QueueFactory.createQueue(V_1_0);
        mutableQueue2.id(EXP_QUEUE_ID_2);
        QueueProperty min2 =
                QueueFactory.createProperty(QueuePropType.MIN_RATE, 210);
        mutableQueue2.addProperty(min2);

        mut.addQueue((Queue) mutableQueue1.toImmutable());
        mut.addQueue((Queue) mutableQueue2.toImmutable());

        // now encode and verify
        encodeAndVerifyMessage(mut.toImmutable(), TF_QGCR_10);
    }
}
