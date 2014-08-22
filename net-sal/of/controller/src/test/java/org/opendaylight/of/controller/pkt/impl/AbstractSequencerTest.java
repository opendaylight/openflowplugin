/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt.impl;

import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.controller.impl.AbstractTest;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.packet.ProtocolId;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.fail;
import static org.opendaylight.of.controller.OpenflowEventType.MESSAGE_RX;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.BARRIER_REQUEST;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_IN;
import static org.opendaylight.util.ByteUtils.slurpBytesFromHexFile;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Abstract superclass of sequencer unit tests. This contains some useful
 * fixtures, etc.
 *
 * @author Simon Hunt
 */
public abstract class AbstractSequencerTest extends AbstractTest {

    private static final String ROOT_PATH = "org/opendaylight/of/controller/pkt/impl/";
    private static final String HEXFILE = "eth2-ip-tcp.hex";
    private static final String PARTIAL = "packetInPartialData.hex";

    protected static final TimeUtils TIME = TimeUtils.getInstance();

    protected static final DataPathId DPID = dpid("42/00001e:123456");
    protected static final DataPathId DPID_OTHER  = dpid("42/00001e:ffffff");
    protected static final BigPortNumber IN_PORT = bpn(7);

    protected static OfmPacketIn PI_10;
    protected static byte[] PACKET_BYTES;
    protected static OfmPacketIn PARTIAL_PKT_IN;

    protected static final ProtocolId[] EXP_PROTOCOLS = {
            ProtocolId.ETHERNET, ProtocolId.IP, ProtocolId.TCP
    };


    static {
        try {
            PACKET_BYTES = slurpBytesFromHexFile(ROOT_PATH + HEXFILE, CL);
            byte[] ppi = slurpBytesFromHexFile(ROOT_PATH + PARTIAL, CL);
            PARTIAL_PKT_IN = (OfmPacketIn)
                MessageFactory.parseMessage(getPacketReader(ppi));
        } catch (IOException e) {
            e.printStackTrace();  // BLAH
        } catch (MessageParseException e) {
            e.printStackTrace();  // BLAH
        }

        OfmMutablePacketIn mpi = (OfmMutablePacketIn)
                MessageFactory.create(V_1_0, PACKET_IN, PacketInReason.NO_MATCH);
        PI_10 = (OfmPacketIn) mpi.inPort(IN_PORT).data(PACKET_BYTES).toImmutable();
    }

    protected static final OpenflowMessage BARRIER_13 =
            create(V_1_3, BARRIER_REQUEST).toImmutable();

    /** Fixture implementation of message event. */
    protected static class TestMessageEvent implements MessageEvent {
        private final long ts = TIME.currentTimeMillis();
        private final OpenflowEventType et = MESSAGE_RX;
        private final OpenflowMessage msg;
        private final ProtocolVersion pv;
        private final DataPathId dpid;
        private final int auxId;

        TestMessageEvent(DataPathId dpid, int auxId, ProtocolVersion pv,
                         OpenflowMessage msg) {
            this.dpid = dpid;
            this.auxId = auxId;
            this.pv = pv;
            this.msg = msg;
        }

        @Override public OpenflowMessage msg() { return msg; }
        @Override public DataPathId dpid() { return dpid; }
        @Override public int auxId() { return auxId; }
        @Override public ProtocolVersion negotiated() { return pv; }
        @Override public String remoteId() { return "fooey"; }
        @Override public long ts() { return ts; }
        @Override public OpenflowEventType type() { return et; }

        @Override public String toString() {
            return "{Evt:" + TIME.hhmmssnnn(ts) + "," + et + ",msg=" + msg + "}";
        }
    }

    protected static final MessageEvent SOME_EVENT =
            new TestMessageEvent(DPID, 0, V_1_0, PI_10);

    protected static final MessageEvent SOME_EVENT_AUX_1 =
            new TestMessageEvent(DPID, 1, V_1_0, PI_10);

    protected static final MessageEvent SOME_EVENT_AUX_2 =
            new TestMessageEvent(DPID, 2, V_1_0, PI_10);

    protected static final MessageEvent SOME_OTHER_EVENT =
            new TestMessageEvent(DPID_OTHER, 0, V_1_0, PI_10);

    protected static final int MAX_LATCH_WAIT_MS = 500; // half a sec

    protected CountDownLatch eventsProcessed;

    protected void waitForEvents() {
        print("... waiting for events ...");
        try {
            eventsProcessed.await(MAX_LATCH_WAIT_MS, MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Not all Events(s) processed: " + e);
        }
    }

}
