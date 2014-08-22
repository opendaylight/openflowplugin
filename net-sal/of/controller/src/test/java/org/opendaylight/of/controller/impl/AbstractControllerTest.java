/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.common.MessageSink;
import org.opendaylight.of.controller.*;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.msg.MessageFuture;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.of.mockswitch.MockOpenflowSwitch;
import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.event.DefaultEventSinkBroker;
import org.opendaylight.util.event.Event;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.net.IpAddress;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Base class for controller unit tests.
 *
 * @author Simon Hunt
 * @author Sudheer Duggisetty
 */
public abstract class AbstractControllerTest extends AbstractTest {

    protected static final TimeUtils TIME = TimeUtils.getInstance();

    protected static final int MAIN_ID = 0;

    protected static final ControllerConfig DEFAULT_CTRL_CFG =
            new ControllerConfig.Builder().workerCount(6).build();

    protected static final String IMPL_ROOT = "org/opendaylight/of/controller/impl/";
    protected static final String SW13P32 = IMPL_ROOT + "simple13sw32port.def";
    protected static final String SW13P8 = IMPL_ROOT + "simple13sw8port.def";
    protected static final String SW10P4 = IMPL_ROOT + "simple10sw4port.def";
    protected static final String SW10P12 = IMPL_ROOT + "simple10sw12port.def";

    protected static final String LLDP = IMPL_ROOT + "lldpPacket.hex";
    protected static final String ETH2 = IMPL_ROOT + "eth2-arp-req.hex";


    protected static final DataPathId SW13P32_DPID = dpid("42/0016b9:068000");
    protected static final DataPathId SW13P8_DPID = dpid("13/0016b9:068003");
    protected static final DataPathId SW10P4_DPID = dpid("42/0016b9:006502");
    protected static final DataPathId SW10P12_DPID = dpid("84/0016b9:006502");

    // Description expected strings:
    protected static final String SW13P32_MFR_DESC = "Acme Switch Co.";
    protected static final String SW13P32_HW_DESC = "RoadRunner 9000";
    protected static final String SW13P32_SW_DESC = "OF-BeepBeep 1.6.0";
    protected static final String SW13P32_SER_NUM = "WB-11954-TAF";
    protected static final String SW13P32_DP_DESC =
            "9000 in Secret Hideout, Somewhere Safe";

    protected static final String SW10P4_MFR_DESC = "Acme Switch Co.";
    protected static final String SW10P4_HW_DESC = "Coyote WB10";
    protected static final String SW10P4_SW_DESC = "OF-Sneaky 1.0";
    protected static final String SW10P4_SER_NUM = "WB-11937-TAF";
    protected static final String SW10P4_DP_DESC =
            "WB10 at top of Mesa, Top Shelf";

    protected static final String FMT_EVENT = "{} - {}: event => {}";

    // NOTE: Mock Switches should be connecting within MILLISECONDS; if they
    //  are not connected within 5 seconds, then something is seriously wrong
    //  and the test should fail, prompting an investigation into why the
    //  switch failed to connect. (Increasing the timeout simply makes you 
    //  wait longer for the test to fail).
    static final int MAX_LATCH_WAIT_MS = 5000; // 5 secs
    static final int MAX_FUTURE_WAIT_MS = 250; // quarter of a sec

    protected static final String E_FUTURE_TIMEOUT =
            "Timed-Out waiting for a future";

    private StopWatch watch;

    protected CountDownLatch switchesReady;
    protected CountDownLatch switchesGone;
    protected CountDownLatch internalMsgLatch;
    protected CountDownLatch messagesReceived;
    protected CountDownLatch listenersProcessed;
    protected CountDownLatch queueEventsProcessed;
    protected CountDownLatch workersDone;

    protected AlertSink alertSink;
    protected RoleAdvisor roleAdvisor;
    protected EventDispatchService eds;

    protected ControllerService cs;
    protected ControllerManager cmgr;

    protected ListenerManager lmgr;
    protected ListenerService ls;

    protected TxRxControl txrx;
    protected TxRxCtrl txrxc;

    static final String PKI_ROOT =
            "src/test/resources/com/hp/of/ctl/impl/";
    static final String DEFAULT_KS_NAME = PKI_ROOT + "controller.jks";
    static final String DEFAULT_KS_PASS = "skyline";
    static final String DEFAULT_TS_NAME = PKI_ROOT + "controller.jks";
    static final String DEFAULT_TS_PASS = "skyline";

    // =====================

    /** Begins a unit test by printing the method name and starting a
     * stop-watch.
     *
     * @param methodName the test method name
     */
    protected void beginTest(String methodName) {
        print("{}=============== {}() ===============", EOL, methodName);
        watch = new StopWatch(methodName);
    }

    /** Ends a unit test by stopping and printing the stop-watch. */
    protected void endTest() {
        print(watch.stop());
    }

    /** Initializes the TX/RX control fields.
     *
     * @param ctlr the controller
     */
    protected void initTxRxControl(OpenflowController ctlr) {
        txrx = ctlr.getTxRxControl();
        txrxc = (TxRxCtrl) txrx;
    }

    /** Start recording TX/RX openflow message trace.
     *
     * @param seconds max number of seconds before auto-shutoff
     */
    protected void startRecording(int seconds) {
        txrx.startRecording(seconds);
    }

    /** Stop recording TX/RX trace and print the results. */
    protected void stopRecordingAndPrintDebugTrace() {
        txrx.stopRecording();
        printDebugTxRx();
    }

    /** Stop recording TX/RX trace and print the results. */
    protected void stopRecordingAndPrintDetailedDebugTrace() {
        txrx.stopRecording();
        printDetailedDebugTxRx();
    }

    /** Outputs the debug string of the TX/RX control. */
    protected void printDebugTxRx() {
        print(EOL + ((TxRxCtrl) txrx).toDebugString());
    }

    /** Outputs the debug string of the TX/RX control. */
    protected void printDetailedDebugTxRx() {
        print(EOL + ((TxRxCtrl) txrx).toDetailedDebugString());
    }

    /** Outputs a section header, and writes a checkpoint record to
     * the TX/RX queue.
     *
     * @param label the section label
     */
    protected void sectionHeader(String label) {
        print("=== {} ===", label);
        lmgr.txRxCheckpoint(label);
    }


    /** Initializes the TX/RX control fields.
     *
     * @param lmgr the listener manager
     */
    protected void initTxRxControl(ListenerManager lmgr) {
        txrx = lmgr.getTxRxControl();
        txrxc = (TxRxCtrl) txrx;
    }

    /** Create a basic mock switch using the given definition file.
     *
     * @param defPath definition file pathname
     * @return an instantiated mock switch
     */
    protected BasicSwitch createAndActivateSwitch(String defPath) {
        BasicSwitch sw = null;
        try {
            sw = new BasicSwitch(null, defPath);
        } catch (IOException e) {
            fail(AM_UNEX + " " + e);
        }
        sw.activate();
        print("... switch activated : {} ...", sw.getDpid());
        return sw;
    }

    /** Create a secure mock switch using the given definition file.
     *
     * @param defPath definition file pathname
     * @return an instantiated mock switch
     */
    protected MockOpenflowSwitch createAndActivateSecureSwitch(String defPath) {
        MockOpenflowSwitch sw = new MockOpenflowSwitch(defPath, showOutput);
        sw.setSecure(PKI_ROOT + "device.jks", DEFAULT_KS_PASS);
        sw.activate();
        print("... switch activated : {} ...", sw.getDpid());
        return sw;
    }

    protected void waitForHandshake() {
        waitForHandshake(MAX_LATCH_WAIT_MS);
    }

    protected void waitForHandshake(long timeout) {
        print("... waiting for handshake ... {} ms timeout", timeout);
        try {
            assertTrue("Switch(es) not ready in time",
                       switchesReady.await(timeout, MILLISECONDS));
        } catch (InterruptedException e) {
            fail("Switch(es) not ready: " + e);
        }
    }

    protected void waitForDisconnect() {
        print("... waiting for disconnect ...");
        try {
            assertTrue("Switch(es) not disconnected in time",
                       switchesGone.await(MAX_LATCH_WAIT_MS, MILLISECONDS));
        } catch (InterruptedException e) {
            fail("Switch(es) not disconnected: " + e);
        }
    }

    protected void waitForInternalMessages() {
        print("... waiting for internal messages ...");
        try {
            boolean hitZero =
                    internalMsgLatch.await(MAX_LATCH_WAIT_MS, MILLISECONDS);
            if (!hitZero)
                fail("  HIT internal msg timeout");

        } catch (InterruptedException e) {
            fail("Internal Message(s) not ready: " + e);
        }
    }

    protected void waitForWorkers(final int maxTimeoutMs) {
        print("... waiting for workers ... (timeout {}ms)", maxTimeoutMs);
        try {
            boolean hitZero = workersDone.await(maxTimeoutMs, MILLISECONDS);
            if (!hitZero)
                fail("  HIT worker timeout");
        } catch (InterruptedException e) {
            fail("Worker(s) not finished: " + e);
        }
    }

    protected void waitForMessages(final int maxTimeoutMs) {
        print("... waiting for messages ... (timeout {}ms)", maxTimeoutMs);
        try {
            boolean hitZero =
                    messagesReceived.await(maxTimeoutMs, MILLISECONDS);
            if (!hitZero)
                fail("  HIT msg timeout");
        } catch (InterruptedException e) {
            fail("Message(s) not ready: " + e);
        }
    }

    protected void waitForMessages() {
        waitForMessages(MAX_LATCH_WAIT_MS);
    }

    protected void waitForListeners() {
        print("... waiting for listeners ...");
        try {
            assertTrue("Listeners failed to process in time",
                       listenersProcessed.await(MAX_LATCH_WAIT_MS, MILLISECONDS));
        } catch (InterruptedException e) {
            fail("Listener(s) not ready: " + e);
        }
    }

    protected void waitForQueueEvents(String label) {
        print("... waiting for queue events ...");
        try {
            boolean hitZero =
                    queueEventsProcessed.await(MAX_LATCH_WAIT_MS, MILLISECONDS);
            if (!hitZero)
                fail("  HIT timeout: " + label);
        } catch (InterruptedException e) {
            fail("Queue Events(s) not ready: " + e);
        }
    }

    private static final long MAX_MSG_WAIT_MS = 50;
    private static final String WAIT_TIMED_OUT = "Timed out waiting for future";

    /**
     * Waits for the message future to be satisfied (timeout after 50ms).
     * Will fail the test if more than 50ms elapses, or if the thread is
     * interrupted.
     *
     * @param f the future on which to wait
     * @return the satisfied future (same as parameter f)
     */
    protected MessageFuture waitForFuture(MessageFuture f) {
        try {
            boolean ok = f.await(MAX_MSG_WAIT_MS);
            if (!ok)
                fail(WAIT_TIMED_OUT);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        return f;
    }

    /**
     * Shorthand for {@code new ControllerConfig.Builder()}.
     *
     * @return a new controller config builder
     */
    protected ControllerConfig.Builder bld() {
        return new ControllerConfig.Builder();
    }

    //=======================================================================

    protected static final PostHandshakeSink PH_SINK = new PostHandshakeSink() {
        @Override
        public PostHandshakeTask doPostHandshake(IpAddress ip, DataPathId dpid,
                                                 MBodyDesc desc,
                                                 PostHandshakeCallback cb) {
            return null;
        }
    };

    protected static final PostHandshakeCallback PH_CB = new PostHandshakeCallback() {
        @Override
        public void handshakeComplete(DataPathId dpid, String deviceTypeName) {
        }
    };

    // fake flow mod advisor
    protected static final FlowModAdvisor FM_ADV = new FlowModAdvisor() {
        @Override
        public List<OfmFlowMod> getDefaultFlowMods(DataPathInfo dpi,
                                                   List<OfmFlowMod> contributedFlows,
                                                   PipelineDefinition pipelineDefinition,
                                                   boolean isHybrid) {
            // TODO: implement, when we have a test that uses this call
            return null;
        }

        @Override
        public List<OfmFlowMod> adjustFlowMod(DataPathInfo dpi, OfmFlowMod fm) {
            List<OfmFlowMod> adjusted = new ArrayList<>(1);
            adjusted.add(fm);
            return adjusted;
        }
    };

    /**
     * Augments the controller to print messages as they are sent.
     * Also provides a handle on the port-state-tracker.
     */
    static class TestController extends OpenflowController {
        private final PortStateTracker tracker;

        TestController(int openflowPort, PortStateTracker tracker,
                       MessageSink sink, AlertSink as) {
            super(new ControllerConfig.Builder().listenPort(openflowPort)
                  .build(), tracker, sink, as, PH_SINK, PH_CB, FM_ADV);
            this.tracker = tracker;
        }

        @Override
        void send(OpenflowMessage msg, DataPathId dpid, int auxId)
                throws OpenflowException {
            print("{}<<SEND>> TX to {},aux={} => {}", EOL, dpid, auxId,
                    msg.toDebugString());
            super.send(msg, dpid, auxId);
        }

        PortStateTracker getTracker() {
            return tracker;
        }
    }

    /**
     * Provides a test role advisor implementation, which always returns
     * {@code true} when asked if the controller is <em>Master</em>.
     */
    static class MockRoleAdvisor implements RoleAdvisor {
        private final Set<DataPathId> masterFor;
        private final boolean masterForAll;

        /** Use this constructor if you want to be master for all DPIDs. */
        public MockRoleAdvisor() {
            masterFor = null;
            masterForAll = true;
        }

        /** Use this constructor if you want to be master for only the
         * specified DPIDs.
         *
         * @param masterDpids the dpids for which we are master
         */

        public MockRoleAdvisor(DataPathId... masterDpids) {
            masterFor = new HashSet<>(Arrays.asList(masterDpids));
            masterForAll = false;
        }

        @Override
        public boolean isMasterFor(DataPathId dpid) {
            return masterForAll || masterFor.contains(dpid);
        }
    }

    /**
     * Provides a test event dispatcher.
     */
    static class MockEventDispatcher extends DefaultEventSinkBroker
                                        implements EventDispatchService {
        @Override
        public void post(Event event) { }
    }

    /**
     * Test ControllerService implementation with fake post-handshake and
     * flow-mod advisor components.
     */
    static class TestControllerManager extends ControllerManager {

        public TestControllerManager(ControllerConfig cfg, AlertSink as,
                                     RoleAdvisor ra, EventDispatchService eds) {
            super(cfg, as, PH_SINK, FM_ADV, ra, eds);
        }
    }

}
