/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.controller.pkt.SequencedPacketAdapter;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.junit.PerformanceTests;
import org.opendaylight.util.net.BigPortNumber;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Testing some of the auxiliary channel rules from the spec.
 * See pg.32,33 of the OF 1.3.1 spec (6.3.5 Auxiliary Connections)
 *
 * @author Simon Hunt
 */
@Category(PerformanceTests.class)
public class AuxChanInteractionsTest extends AbstractControllerTest {

    private static final String E_PROG = "Programming Error: Fix it!";

    // same as in simple13sw32port.def
    private static final String DEF = SW13P32;
    private static final DataPathId DPID = SW13P32_DPID;
    private static final ProtocolVersion PV = V_1_3;
    private static final BigPortNumber INPORT = bpn(3);
    private static final BufferId BUFFER = BufferId.NO_BUFFER;
    private static final TableId TABLE = tid(4);

    // A test "switch" where we can set the auxiliary ID on the connection.
    private static class AuxSwitch extends BasicSwitch {
        private final MultiConnSwitch parent;
        private final int auxId;
        private long expXid;
        private int poCount = 0;
        private CountDownLatch latch;

        public AuxSwitch(MultiConnSwitch parent, DataPathId dpid, int auxId,
                         String defPath) throws IOException {
            super(dpid, defPath);
            this.parent = parent;
            this.auxId = auxId;
        }

        @Override
        public String toString() {
            return "{AuxSw: " + dpid + ", aux=" + auxId + "}";
        }

        @Override
        protected int auxId() {
            return auxId;
        }

        public void doPi() {
            OfmMutablePacketIn pi = (OfmMutablePacketIn)
                    create(V_1_3, PACKET_IN, PacketInReason.NO_MATCH);
            MutableMatch m = MatchFactory.createMatch(V_1_3);
            m.addField(createBasicField(V_1_3, OxmBasicFieldType.IN_PORT, INPORT));
            pi.bufferId(BUFFER).tableId(TABLE).match((Match) m.toImmutable());
            expXid = pi.getXid();
            latch = new CountDownLatch(1);
            OpenflowMessage msg = pi.toImmutable();
            print("{} sending pi: {}", this, msg);
            send(msg);
        }

        @Override
        protected void msgRx(OpenflowMessage msg) {
            print("[AUX={}] msgRx: {}", auxId, msg);
            parent.setLastHeard(auxId);

            if (msg.getType().equals(PACKET_OUT))
                handlePacketOut((OfmPacketOut) msg);
            else
                super.msgRx(msg);
        }

        private void handlePacketOut(OfmPacketOut po) {
            print("{} receiving po: {}", this, po);
            poCount++;
            latch.countDown();
            long xid = po.getXid();
            if (xid != expXid)
                stowAndThrow("mismatch XID; exp=" + expXid + ", act=" + xid);
        }

        public void waitForLatch() {
            try {
                boolean ok = latch.await(MAX_LATCH_WAIT_MS, MILLISECONDS);
                if (!ok)
                    fail("TIMEOUT : Packet-Out not received");

            } catch (InterruptedException e) {
                fail("Packet-Out not received: " + e);
            }
        }

    }

    // an aggregation of "switches" to represent a switch with aux channels.
    private static class MultiConnSwitch {
        private final DataPathId dpid;
        private final Map<Integer, AuxSwitch> auxes = new HashMap<>();
        private final int conns;
        private CountDownLatch readyLatch;
        private CountDownLatch msgRxLatch;
        private int actualAux;


        /** Creates a multi-connection switch with the given datapath
         * and definition file, and a number of auxiliary connections.
         * For example, if conns is 2, there will be the main connection
         * (auxId = 0), and two auxiliary connections (1 and 2).
         *
         * @param dpid the datapath ID
         * @param defPath the path to the switch definition file
         * @param conns the number of auxiliary connections
         * @throws IOException if a problem reading the definition file
         */
        public MultiConnSwitch(DataPathId dpid, String defPath, int conns)
                throws IOException {
            this.dpid = dpid;
            this.conns = conns;
            for (int i=0; i <= conns; i++)
                auxes.put(i, new AuxSwitch(this, dpid, i, defPath));
        }

        @Override
        public String toString() {
          return "{MultiConnSwitch: " + dpid + ", #aux=" + conns + "}";
        }

        // activate each channel (with 5 ms delay between each)
        public void activate() {
            readyLatch = new CountDownLatch(conns + 1);
            // start with main channel
            for (int i=0; i <= conns; i++) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    // fooey
                }
                AuxSwitch a = auxes.get(i);
                a.activate();
                a.waitForHandshake(true);
                readyLatch.countDown();
            }
        }

        // deactivate each channel
        public void deactivate() {
            // end with main channel
            for (int i=conns; i >= 0; i--)
                auxes.get(i).deactivate();
        }

        public void waitForReady() {
            try {
                boolean ok = readyLatch.await(MAX_LATCH_WAIT_MS, MILLISECONDS);
                if (!ok)
                    fail("TIMEOUT : switches not ready");
            } catch (InterruptedException e) {
                fail("Connection(s) not ready: " + e);
            }
        }

        public void waitForMsgRx() {
            try {
                boolean ok = msgRxLatch.await(MAX_LATCH_WAIT_MS, MILLISECONDS);
                if (!ok)
                    fail("TIMEOUT : msg not received");
                msgRxLatch = null;

            } catch (InterruptedException e) {
                fail("msg not received: " + e);
            }
        }

        public void doPi(int which) {
            AuxSwitch a = auxes.get(which);
            if (a == null)
                throw new RuntimeException(E_PROG);
            a.doPi();
        }

        // verify the number of packet outs received on each channel
        public void verifyPo(int... counts) {
            int numChan = conns + 1;
            if (counts.length != numChan)
                fail(E_PROG + " (expected " + numChan + " channel counts");
            for (int i=0; i<counts.length; i++) {
                AuxSwitch a = auxes.get(i);
                assertEquals(AM_NEQ, counts[i], a.poCount);
            }
        }

        private void setLastHeard(int auxId) {
            actualAux = auxId;
            if (msgRxLatch != null)
                msgRxLatch.countDown();
        }

        public void waitForPo(int which) {
            auxes.get(which).waitForLatch();
        }

        public void verifyInboundOn(int expInboundAux) {
            assertEquals("wrong connection", expInboundAux, actualAux);
        }

        public void setMsgRxLatch() {
            msgRxLatch = new CountDownLatch(1);
        }
    }

    // ======================================================================

    // a simple packet director
    private static class TestDirector extends SequencedPacketAdapter {
        private boolean willHandle = false;
        @Override
        public void event(MessageContext context) {
            if (willHandle)
                context.packetOut().send();
        }
    }

    // a simple packet observer
    private static class TestObserver extends SequencedPacketAdapter { }

    TestDirector dir;
    TestObserver obs;

    // ======================================================================

    @BeforeClass
    public static void classSetUp() {
        Assume.assumeTrue(!isUnderCoverage());
        setUpLogger();
    }

    @After
    public void tearDown() {
        cmgr.shutdown();
    }

    // ======================================================================
    // === HELPER methods

    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor(DPID);
        eds = new MockEventDispatcher();

        cmgr = new TestControllerManager(DEFAULT_CTRL_CFG, alertSink, roleAdvisor, eds);
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();
        print("... controller activated ...");
    }

    private MultiConnSwitch connectMultiSwitch(int conns) {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);

        MultiConnSwitch multi = null;
        try {
            multi = new MultiConnSwitch(DPID, DEF, conns);
            multi.activate();
            multi.waitForReady();
        } catch (IOException e) {
            print(e);
            fail(AM_UNEX);
        }
        return multi;
    }

    private void disconnectMultiSwitch(MultiConnSwitch sw) {
        switchesGone = new CountDownLatch(1);
        lmgr.setDataPathRemovedLatch(switchesGone);
        sw.deactivate();
        waitForDisconnect();
    }

    private void firePi(MultiConnSwitch sw, int which) {
        sw.doPi(which);
        sw.waitForPo(which);
    }


    @Test
    public void packetOutOnSameConnAsPacketIn() {
        beginTest("packetOutOnSameConnAsPacketIn");
        initController();

        // let's tee up some packet listeners
        dir = new TestDirector();
        dir.willHandle = true;
        obs = new TestObserver();

        cs.addPacketListener(dir, SequencedPacketListenerRole.DIRECTOR, 5);
        cs.addPacketListener(obs, SequencedPacketListenerRole.OBSERVER, 0);

        MultiConnSwitch sw = connectMultiSwitch(2);
        print(sw);
        waitForHandshake(); // wait for full handshake to complete

        firePi(sw, 0);
        sw.verifyPo(1, 0, 0);
        firePi(sw, 1);
        sw.verifyPo(1, 1, 0);
        firePi(sw, 2);
        sw.verifyPo(1, 1, 1);

        disconnectMultiSwitch(sw);
        endTest();
    }

    @Test
    public void requestedConnNotAvailable() {
        beginTest("requestedConnNotAvailable");
        initController();

        // switch with MAIN(0), AUX(1), AUX(2) ...
        MultiConnSwitch sw = connectMultiSwitch(2);
        print(sw);
        waitForHandshake(); // wait for full handshake to complete

        verifySelectedAux(sw, 0, 0);
        verifySelectedAux(sw, 1, 1);
        verifySelectedAux(sw, 2, 2);
        verifySelectedAux(sw, 3, 0);
        verifySelectedAux(sw, 4, 0);
        verifySelectedAux(sw, 5, 0);

        disconnectMultiSwitch(sw);
        endTest();
    }

    private void verifySelectedAux(MultiConnSwitch sw, int requestedAux,
                                   int expActualAux) {
        sw.setMsgRxLatch();
        try {
            lmgr.send(testMessage(), DPID, requestedAux);
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }
        sw.waitForMsgRx();
        sw.verifyInboundOn(expActualAux);
    }

    private OpenflowMessage testMessage() {
        return MessageFactory.create(PV, BARRIER_REQUEST).toImmutable();
    }

}
