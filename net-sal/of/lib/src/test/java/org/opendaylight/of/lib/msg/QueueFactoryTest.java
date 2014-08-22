/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.util.net.BigPortNumber;

import java.util.List;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ExperimenterId.HP;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.QueueFactory.createProperty;
import static org.opendaylight.of.lib.msg.QueuePropType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for QueueFactory.
 *
 * @author Simon Hunt
 */
public class QueueFactoryTest extends OfmTest {

    private static final String Q_V01_MIN = "struct/queueV01Min";
    private static final String Q_V23_MIN = "struct/queueV23Min";
    private static final String Q_V23_MIN_MAX = "struct/queueV23MinMax";
    private static final String Q_V23_EXPER = "struct/queueV23Exper";

    private static final String E_PARSE_FAIL = "Failed to parse queue.";
    private static final String AM_NOT_MIN_RATE = "not min rate";

    private static final int HP_ID = 0x00002481;
    private static final byte[] DATA_BYTES = {
            // a bunch of primes...
             1,  2,  3,  5,  7, 11, 13, 17,
            19, 23, 29, 31, 37, 41, 43, 47
    };


    private static final QueueId EXP_01_QID = QueueId.valueOf(18);
    private static final int EXP_MIN_RATE = 450;
    private static final int EXP_MAX_RATE = 850;
    private static final QueueId EXP_23_QID = QueueId.valueOf(19);
    private static final BigPortNumber EXP_23_PORT = BigPortNumber.valueOf(7);
    private static final QueueId EXP_23MM_QID = QueueId.valueOf(21);
    private static final BigPortNumber EXP_23MM_PORT = BigPortNumber.valueOf(12);
    private static final QueueId EXP_23E_QID = QueueId.valueOf(25);
    private static final BigPortNumber EXP_23E_PORT = BigPortNumber.valueOf(13);


    @Test
    public void minRate01() {
        print(EOL + "minRate01()");
        OfPacketReader pkt = getMsgPkt(Q_V01_MIN);

        for (ProtocolVersion pv: PV_01) {
            print(pv);
            pkt.resetIndex();
            try {
                Queue queue = QueueFactory.parseQueue(pkt, pv);
                print(queue);

                // See sampleQueueV01Min.hex for expected values
                assertEquals(AM_NEQ, EXP_01_QID, queue.getId());
                assertNull(AM_HUH, queue.getPort());
                assertEquals(AM_NEQ, 24, queue.length); // no getter!
                List<QueueProperty> props = queue.getProps();
                assertEquals(AM_UXS, 1, props.size());
                QueueProperty qp = props.get(0);
                assertEquals(AM_NEQ, MIN_RATE, qp.getType());
                assertTrue(AM_NOT_MIN_RATE, qp instanceof QPropMinRate);
                QPropMinRate min = (QPropMinRate) qp;
                assertEquals(AM_NEQ, 16, min.header.length); // no getter!
                assertFalse(AM_HUH, min.isDisabled());
                assertEquals(AM_NEQ, EXP_MIN_RATE, min.getRate());

            } catch (MessageParseException e) {
                print(e);
                fail(E_PARSE_FAIL);
            }
            checkEOBuffer(pkt);
        }
    }

    @Test
    public void minRate23() {
        print(EOL + "minRate23()");
        OfPacketReader pkt = getMsgPkt(Q_V23_MIN);

        for (ProtocolVersion pv: PV_23) {
            print(pv);
            pkt.resetIndex();
            try {
                Queue queue = QueueFactory.parseQueue(pkt, pv);
                print(queue);

                // See sampleQueueV23Min.hex for expected values
                assertEquals(AM_NEQ, EXP_23_QID, queue.getId());
                assertEquals(AM_NEQ, EXP_23_PORT, queue.getPort());
                assertEquals(AM_NEQ, 32, queue.length); // no getter!
                List<QueueProperty> props = queue.getProps();
                assertEquals(AM_UXS, 1, props.size());
                QueueProperty qp = props.get(0);
                assertEquals(AM_NEQ, MIN_RATE, qp.getType());
                assertTrue(AM_NOT_MIN_RATE, qp instanceof QPropMinRate);
                QPropMinRate min = (QPropMinRate) qp;
                assertEquals(AM_NEQ, 16, min.header.length); // no getter!
                assertFalse(AM_HUH, min.isDisabled());
                assertEquals(AM_NEQ, 450, min.getRate());

            } catch (MessageParseException e) {
                print(e);
                fail(E_PARSE_FAIL);
            }
            checkEOBuffer(pkt);
        }
    }

    @Test
    public void minMaxRate23() {
        print(EOL + "minMaxRate23()");
        OfPacketReader pkt = getMsgPkt(Q_V23_MIN_MAX);

        for (ProtocolVersion pv: PV_23) {
            print(pv);
            pkt.resetIndex();
            try {
                Queue queue = QueueFactory.parseQueue(pkt, pv);
                print(queue);

                // See sampleQueueV23MinMax.hex for expected values
                assertEquals(AM_NEQ, EXP_23MM_QID, queue.getId());
                assertEquals(AM_NEQ, EXP_23MM_PORT, queue.getPort());
                assertEquals(AM_NEQ, 48, queue.length); // no getter!
                List<QueueProperty> props = queue.getProps();
                assertEquals(AM_UXS, 2, props.size());
                QueueProperty qp = props.get(0);
                assertTrue(AM_NOT_MIN_RATE, qp instanceof QPropMinRate);
                QPropMinRate min = (QPropMinRate) qp;
                assertEquals(AM_NEQ, MIN_RATE, qp.getType());
                assertEquals(AM_NEQ, 16, min.header.length); // no getter!
                assertFalse(AM_HUH, min.isDisabled());
                assertEquals(AM_NEQ, EXP_MIN_RATE, min.getRate());
                qp = props.get(1);
                assertEquals(AM_NEQ, QueuePropType.MAX_RATE, qp.getType());
                assertTrue("not max rate", qp instanceof QPropMaxRate);
                QPropMaxRate max = (QPropMaxRate) qp;
                assertEquals(AM_NEQ, 16, max.header.length); // no getter!
                assertFalse(AM_HUH, max.isDisabled());
                assertEquals(AM_NEQ, EXP_MAX_RATE, max.getRate());


            } catch (MessageParseException e) {
                print(e);
                fail(E_PARSE_FAIL);
            }
            checkEOBuffer(pkt);
        }
    }

    public void writeMinRate10() {
        print(EOL + "writeMinRate10()");
        final ProtocolVersion pv = V_1_0;
        print("{}......", pv);
        // first, create the queue, to match the test hex file
        MutableQueue queue = QueueFactory.createQueue(pv).id(EXP_01_QID)
                .addProperty(createProperty(MIN_RATE, EXP_MIN_RATE));
        // get an immutable copy
        Queue copy = (Queue) queue.toImmutable();
        print(copy.toDebugString());
        // get expected data
        byte[] expData = getExpByteArray(Q_V01_MIN);
        // encode the queue into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        QueueFactory.encodeQueue(copy, pkt);
        byte[] encoded = pkt.array();
        printHexArray(encoded);
        // check we got what we expected
        assertArrayEquals(AM_NEQ, expData, encoded);
    }

    @Test
    public void exper13() {
        print(EOL + "exper13()");
        OfPacketReader pkt = getMsgPkt(Q_V23_EXPER);

        ProtocolVersion pv = V_1_3;
        print(pv);
        pkt.resetIndex();
        try {
            Queue queue = QueueFactory.parseQueue(pkt, pv);
            print(queue);

            // See sampleQueueV23Exper.hex for expected values
            assertEquals(AM_NEQ, EXP_23E_QID, queue.getId());
            assertEquals(AM_NEQ, EXP_23E_PORT, queue.getPort());
            assertEquals(AM_NEQ, 48, queue.length); // no getter!
            List<QueueProperty> props = queue.getProps();
            assertEquals(AM_UXS, 1, props.size());
            QueueProperty qp = props.get(0);
            assertEquals(AM_NEQ, QueuePropType.EXPERIMENTER, qp.getType());
            assertTrue("not experimenter", qp instanceof QPropExperimenter);
            QPropExperimenter exp = (QPropExperimenter) qp;
            assertEquals(AM_NEQ, 32, exp.header.length); // no getter!
            assertEquals(AM_NEQ, HP_ID, exp.getId());
            Assert.assertEquals(AM_NEQ, HP, exp.getExpId());
            assertArrayEquals("data bytes", DATA_BYTES, exp.getData());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void writeMinRate13() {
        print(EOL + "writeMinRate13()");
        ProtocolVersion pv = V_1_3;
        print("{}.....", pv);
        // first, create the queue
        MutableQueue queue = QueueFactory.createQueue(pv)
                .id(EXP_23_QID).port(EXP_23_PORT)
                .addProperty(createProperty(MIN_RATE, EXP_MIN_RATE));
        // get an immutable copy
        Queue copy = (Queue) queue.toImmutable();
        print(copy.toDebugString());
        // get expected data
        byte[] expData = getExpByteArray(Q_V23_MIN);
        // encode the queue into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        QueueFactory.encodeQueue(copy, pkt);
        byte[] encoded = pkt.array();
        printHexArray(encoded);
        // check we got what we expected
        assertArrayEquals(AM_NEQ, expData, encoded);
    }

    @Test
    public void writeMinMaxRate13() {
        print(EOL + "writeMinMaxRate13()");
        ProtocolVersion pv = V_1_3;
        print("{}.....", pv);
        // create the queue
        MutableQueue queue = QueueFactory.createQueue(pv)
                .id(EXP_23MM_QID).port(EXP_23MM_PORT)
                .addProperty(createProperty(MIN_RATE, EXP_MIN_RATE))
                .addProperty(createProperty(MAX_RATE, EXP_MAX_RATE));
        // get an immutable copy
        Queue copy = (Queue) queue.toImmutable();
        print(copy.toDebugString());
        // get expected data
        byte[] expData = getExpByteArray(Q_V23_MIN_MAX);
        // encode the queue into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        QueueFactory.encodeQueue(copy, pkt);
        byte[] encoded = pkt.array();
        printHexArray(encoded);
        // check we got what we expected
        assertArrayEquals(AM_NEQ, expData, encoded);
    }

    @Test
    public void writeExper13() {
        print(EOL + "writeExper13()");
        ProtocolVersion pv = V_1_3;
        print("{}.....", pv);
        // create the queue
        MutableQueue queue = QueueFactory.createQueue(pv)
                .id(EXP_23E_QID).port(EXP_23E_PORT)
                .addProperty(createProperty(EXPERIMENTER, HP, DATA_BYTES));
        // get an immutable copy
        Queue copy = (Queue) queue.toImmutable();
        print(copy.toDebugString());
        // get expected data
        byte[] expData = getExpByteArray(Q_V23_EXPER);
        // encode the queue into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        QueueFactory.encodeQueue(copy, pkt);
        byte[] encoded = pkt.array();
        printHexArray(encoded);
        // check we got what we expected
        assertArrayEquals(AM_NEQ, expData, encoded);
    }
}
