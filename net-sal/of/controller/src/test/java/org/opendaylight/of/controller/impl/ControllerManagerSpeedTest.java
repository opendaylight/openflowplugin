/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.controller.pkt.SequencedPacketAdapter;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.ThroughputTracker;
import org.opendaylight.util.junit.SlowestTests;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static org.junit.Assume.assumeTrue;
import static org.opendaylight.of.controller.pkt.SequencedPacketListenerRole.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests to verify speed of processing of the OpenFlow (core) controller.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
@Category(SlowestTests.class)
public class ControllerManagerSpeedTest extends AbstractControllerTest {

    private static final int LONGER_LATCH_TIMEOUT_MS = 60000;
    private static final double TWENTY_K = 20000.0;

    private static final int TOTAL_MSG_COUNT = 500000;
    private static final int MULTI_MSG_COUNT = 5000000;

    private static final int BATCH_SIZE = 1000;
    private static final int BATCH_PERMITS = 20;

    private static final DataPathId DPID_1 = dpid("0/000000:000001");
    private static final DataPathId DPID_2 = dpid("0/000000:000002");
    private static final DataPathId DPID_3 = dpid("0/000000:000003");
    private static final DataPathId DPID_4 = dpid("0/000000:000004");
    private static final DataPathId DPID_5 = dpid("0/000000:000005");
    private static final DataPathId DPID_6 = dpid("0/000000:000006");
    private static final DataPathId DPID_7 = dpid("0/000000:000007");
    private static final DataPathId DPID_8 = dpid("0/000000:000008");
    private static final DataPathId DPID_9 = dpid("0/000000:000009");


    private static final DataPathId[] DPIDS = {
        DPID_1,
        DPID_2,
        DPID_3,
        DPID_4,
        DPID_5,
        DPID_6,
        DPID_7,
        DPID_8,
        DPID_9,
    };

    private Semaphore[] semaphores;

    private static final Executor switchExec =
            Executors.newFixedThreadPool(DPIDS.length);

    @BeforeClass
    public static void classSetUp() {
        assumeTrue(!isUnderCoverage() && !ignoreSpeedTests());
    }

    @AfterClass
    public static void classTearDown() {
    }

    /** A packet observer. */
    private static class PktInListener extends SequencedPacketAdapter {
        private CountDownLatch latch;
        private boolean handle = false;

        void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        private String threadId() {
            return "[" + Thread.currentThread().toString() + "]: ";
        }

        @Override
        public void event(MessageContext context) {
            // print every 100,000th message
            if (latch != null) {
                if (latch.getCount() % 100000 == 0)
                    print(threadId() + "*** MSG RX *** {}", context.srcEvent());
                latch.countDown();
            }
            if (handle)
                context.packetOut().send();
        }
    }

    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor();
        eds = new MockEventDispatcher();

        cmgr = new TestControllerManager(DEFAULT_CTRL_CFG, alertSink,
                roleAdvisor, eds);
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();
    }

    @After
    public void tearDown() {
        cmgr.shutdown();
        semaphores = null;
    }

    private SpeedSwitch connectSwitch(DataPathId dpid) throws IOException {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);

        SpeedSwitch sw = new SpeedSwitch(dpid, SW10P12, ETH2, switchExec);
        sw.activate();
        print("... switch activated : {} ...", sw.getDpid());

        waitForHandshake();
        return sw;
    }

    private void disconnectSwitch(SpeedSwitch... sw) {
        switchesGone = new CountDownLatch(sw.length);
        lmgr.setDataPathRemovedLatch(switchesGone);
        for (SpeedSwitch s : sw)
            s.deactivate();
        waitForDisconnect();
    }


    @Ignore @Test
    public void basic() throws IOException {
        beginTest("basic");
        initController();
        SpeedSwitch sw = connectSwitch(DPID_1);

        final int n = TOTAL_MSG_COUNT;
        messagesReceived = new CountDownLatch(n);

        PktInListener pil = new PktInListener();
        cs.addPacketListener(pil, SequencedPacketListenerRole.OBSERVER, 5);
        pil.setLatch(messagesReceived);

        ThroughputTracker tt = pumpMessages(n, sw);
        waitForCompletion("basic", tt, sw);

        disconnectSwitch(sw);
        endTest();
    }

    private void createListenerCrowd() {
        // create a crowd of listeners; 3 advisors; 3 directors and 4 observers
        PktInListener spl = null;

        for (int i = 0; i < 3; i++)
            cs.addPacketListener(spl = new PktInListener(), ADVISOR, 5+i);
        for (int i = 0; i < 3; i++)
            cs.addPacketListener(spl = new PktInListener(), DIRECTOR, 5+i);
        spl.handle = true; // last director will handle messages
        for (int i = 0; i < 3; i++)
            cs.addPacketListener(spl = new PktInListener(), OBSERVER, 5+i);

        // only the last one gets the latch
        PktInListener last = new PktInListener();
        last.setLatch(messagesReceived);
        cs.addPacketListener(last, SequencedPacketListenerRole.OBSERVER, 0);
    }


    private SpeedSwitch[] connectSwitches() throws IOException {
        final int n = DPIDS.length;
        SpeedSwitch[] switches = new SpeedSwitch[n];

        for (int i = 0; i < n; i++)
            switches[i] = connectSwitch(DPIDS[i]);
        return switches;
    }


    @Ignore @Test
    public void crowd() throws IOException {
        beginTest("crowd");
        initController();

        SpeedSwitch sw = connectSwitch(DPID_1);

        // pump as many messages as the queue limit
        final int n = TOTAL_MSG_COUNT;
        messagesReceived = new CountDownLatch(n);

        createListenerCrowd();
        ThroughputTracker tt = pumpMessages(n, sw);
        waitForCompletion("crowd", tt, sw);

        disconnectSwitch(sw);
        endTest();
    }


    @Test
    public void crowdAndBank() throws IOException {
        beginTest("crowdAndBank");
        initController();

        SpeedSwitch[] switches = connectSwitches();
        attachSemaphores(switches);

        final int n = TOTAL_MSG_COUNT/DPIDS.length;
        messagesReceived = new CountDownLatch(n * DPIDS.length);

        createListenerCrowd();

        ThroughputTracker tt = pumpMessages(n, switches);
        waitForCompletion("crowdAndBank", tt, switches);

        disconnectSwitch(switches);
        endTest();
    }

    @Test
    public void multiWorkers() throws IOException {
        beginTest("multiWorkers");
        initController();

        SpeedSwitch[] switches = connectSwitches();
        attachSemaphores(switches);

        final int n = MULTI_MSG_COUNT/DPIDS.length;
        messagesReceived = new CountDownLatch(n * DPIDS.length);

        createListenerCrowd();

        ThroughputTracker tt = pumpMessages(n, switches);
        print("(for reference: {})", System.currentTimeMillis());
        waitForCompletion("multiWorkers", tt, switches);

        disconnectSwitch(switches);
//        print(cmgr.getSeqWorkerStats());
        endTest();
    }

    private void attachSemaphores(SpeedSwitch... sw) {
        semaphores = new Semaphore[sw.length];
        int i = 0;
        for (SpeedSwitch ss: sw) {
            semaphores[i] = new Semaphore(BATCH_PERMITS);
            ss.setBatchParams(semaphores[i++], BATCH_SIZE, true);
        }
    }

    /**
     * Pump n messages from each switch and returns throughput tracker primed
     * with the message count.
     *
     * @param n number of messages per switch
     * @param sw array of switches
     * @return throughput tracker primed with total message count
     */
    private ThroughputTracker pumpMessages(int n, SpeedSwitch... sw) {
        int count = sw.length;
        print("... about to send {} messages from each of {} switches...",
              n, count);

        ThroughputTracker tt = new ThroughputTracker();
        for (SpeedSwitch aSw : sw)
            aSw.pumpPackets(n, tt);

        return tt;
    }

    /**
     * Waits for test completion, then finalizes and displays throughput
     * stats.
     *
     * @param testName name of the test
     * @param tt throughput tracker
     * @param sw switches to wait for
     */
    private void waitForCompletion(String testName, ThroughputTracker tt,
                                   SpeedSwitch... sw) {
        waitForMessages(LONGER_LATCH_TIMEOUT_MS);
        for (SpeedSwitch aSw : sw)
            aSw.waitForPump();
        tt.freeze();

        // TestTools.print is bypassed on purpose to get visibility to
        // performance number at all times.
        System.out.println(testName + ": Packet-In rate = " +
                           tt.throughput() + " pkts/s");
        assertAboveThreshold("pkts/s", TWENTY_K, tt.throughput());
    }

}
