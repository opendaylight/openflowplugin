/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.controller.*;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.IpAddress;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Specifically testing known message interactions.
 *
 * @author Simon Hunt
 */
public class HandshakeInteractionsTest extends AbstractControllerTest {

    private static final OpenflowEventType CON = OpenflowEventType.DATAPATH_CONNECTED;
    private static final OpenflowEventType TX = OpenflowEventType.MESSAGE_TX;
    private static final OpenflowEventType RX = OpenflowEventType.MESSAGE_RX;
    private static final CheckpointEvt.Code REC_START =
            CheckpointEvt.Code.RECORDING_STARTED;
    private static final CheckpointEvt.Code REC_STOP =
            CheckpointEvt.Code.RECORDING_STOPPED;
    private static final CheckpointEvt.Code GEN =
            CheckpointEvt.Code.GENERIC;


    private static final String DEF = SW13P32;
    private static final DataPathId DPID = SW13P32_DPID;
    private static final String FAKE_DEVICE_TYPE_NAME = "FakeFooDevice";
    private static final long TASK_MAX_WAIT_MS = 3000;

    private XhsSwitch xhssw;
    private XhsSwitch xhssw2;
    private DataPathInfo dpi;
    private TestPhSink phs;

    private CountDownLatch taskGate;
    private PostHandshakeTask phTask;
    private CountDownLatch blockerLatch;
    private boolean blockTheQueue;

    private boolean phTaskRunInvoked;
    private boolean phTaskRunAborted;
    private boolean phTaskRunCompleted;


    //======================================================================
    // === Test Fixture classes

    // a test switch that will allow us to examine the EXTENDED HANDSHAKE
    // conversation behaviour from the controller
    private static class XhsSwitch extends BasicSwitch {
        public XhsSwitch(DataPathId dpid) throws IOException {
            super(dpid, DEF);
        }

        // TODO: add hooks for different make/model combos
    }


    // a test post-handshake sink.
    private class TestPhSink implements PostHandshakeSink {
        // Note use of SINGLE thread executor, so we can BLOCK the queue at will
        private final ExecutorService phQueuePool =
                Executors.newSingleThreadExecutor(namedThreads("PhQPool"));


        @Override
        public PostHandshakeTask doPostHandshake(IpAddress ip, DataPathId dpid,
                                                 MBodyDesc desc, PostHandshakeCallback cb) {
            print("doPostHandshake() called : {}, {}, {}", ip, dpid, desc);

            // block up the queue, if the test requires it...
            if (blockTheQueue) {
                print("..Blocking up the queue..");
                QueueBlocker qb = new QueueBlocker();
                blockerLatch = qb.getLatch();
                phQueuePool.submit(qb);
            }

            // submit the proper post-handshake task as usual...
            print("..Submitting the PH-task..");
            PostHandshakeTask task = new PostHandshakeTaskImpl(ip, dpid, desc, cb);
            phQueuePool.submit(task);
            phTask = task; // a reference for the unit test
            return task;
        }


        // A post-handshake task that the unit test can use to block
        //  queue processing until it is ready...
        private class QueueBlocker implements PostHandshakeTask {
            private final CountDownLatch latch;

            QueueBlocker() {
                latch = new CountDownLatch(1);
            }

            CountDownLatch getLatch() { return latch; }

            @Override public void invalidate() { }
            @Override public boolean isValid() { return false; }

            @Override
            public void run() {
                print("BLOCKER run()...");
                try {
                    if (!latch.await(TASK_MAX_WAIT_MS, TimeUnit.MILLISECONDS))
                        fail("blocker task timed-out");
                    print("BLOCKER .. unblocked");
                } catch (InterruptedException e) {
                    print(e);
                    fail(AM_UNEX);
                }
            }
        }


        // an instrumented post-handshake task...
        private class PostHandshakeTaskImpl implements PostHandshakeTask {
            private final IpAddress ip;
            private final DataPathId dpid;
            private final MBodyDesc desc;
            private final PostHandshakeCallback cb;

            private volatile boolean valid = true;

            public PostHandshakeTaskImpl(IpAddress ip, DataPathId dpid,
                                        MBodyDesc desc, PostHandshakeCallback cb) {
                this.ip = ip;
                this.dpid = dpid;
                this.desc = desc;
                this.cb = cb;
            }

            @Override
            public void run() {
                phTaskRunInvoked = true;
                print("PH-Task run()...");
                if (!valid) {
                    phTaskRunAborted = true;
                    print("PH-Task ABORTED - not valid");
                    taskGate.countDown();
                    return;
                }

                // In the production code, we ask the Device Subsystem to
                // determine the type of device. But here we use a mock
                // device type:
                String dtn = FAKE_DEVICE_TYPE_NAME;
                // exercise the push-default-flows code
                cb.handshakeComplete(dpid, dtn);  // BLOCKS
                print("PH-Task .. back from pushing flows");
                phTaskRunCompleted = true;
                // signal to the unit test that we are done...
                taskGate.countDown();
            }

            @Override
            public void invalidate() {
                print("PH-Task : invalidate() called");
                valid = false;
            }

            @Override
            public boolean isValid() {
                return valid;
            }
        }

    }

    private static final long COOKIE_A = 0xaaaa;
    private static final long COOKIE_B = 0xbbbb;

    // fixture flow mod advisor - to provide some default flows
    private static final FlowModAdvisor fmAdv = new FlowModAdvisor() {
        @Override
        public List<OfmFlowMod> getDefaultFlowMods(DataPathInfo dpi,
                                                   List<OfmFlowMod> contributedFlows,
                                                   PipelineDefinition pipelineDefinition,
                                                   boolean isHybrid) {
            List<OfmFlowMod> fms = new ArrayList<>();
            fms.add(createFlowMod(dpi.negotiated(), COOKIE_A));
            fms.add(createFlowMod(dpi.negotiated(), COOKIE_B));
            return fms;
        }

        @Override
        public List<OfmFlowMod> adjustFlowMod(DataPathInfo dpi, OfmFlowMod fm) {
            List<OfmFlowMod> adjusted = new ArrayList<>(1);
            adjusted.add(fm);
            return adjusted;
        }

        private OfmFlowMod createFlowMod(ProtocolVersion pv, long cookie) {
            OfmMutableFlowMod fm =
                    (OfmMutableFlowMod) create(pv, FLOW_MOD, FlowModCommand.ADD);
            fm.match(createMatch(pv)).cookie(cookie);
            if (pv.gt(ProtocolVersion.V_1_0))
                fm.tableId(TableId.valueOf(42));
            return (OfmFlowMod) fm.toImmutable();
        }

        private Match createMatch(ProtocolVersion pv) {
            return (Match) MatchFactory.createMatch(pv).toImmutable();
        }
    };


    //======================================================================
    // Helper methods

    @BeforeClass
    public static void classSetUp() {
        assumeTrue(!isUnderCoverage());
    }

    @Before
    public void setUp() {
        // make sure XID assignment always starts from 101.
        MessageFactory.getTestSupport().reset(MessageFactory.TestReset.XID);
        // by default, don't block the task queue
        blockTheQueue = false;
        blockerLatch = null;

        phTaskRunInvoked = false;
        phTaskRunAborted = false;
        phTaskRunCompleted = false;
    }


    @After
    public void tearDown() {
        cmgr.shutdown();
    }

    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor();
        eds = new MockEventDispatcher();
        phs = new TestPhSink();

        // create a controller manager instance
        cmgr = new ControllerManager(DEFAULT_CTRL_CFG, alertSink,
                phs, fmAdv, roleAdvisor, eds);
        // reference as controller service
        cs = cmgr;
        // start up the IO loops
        cmgr.startIOProcessing();
        // reference to the internal listener manager
        lmgr = cmgr.getListenerManager();
        initTxRxControl(lmgr);

        print("... controller activated ... {}", lmgr);
    }

    private XhsSwitch connectXhsSwitch() {
        return connectXhsSwitch(DPID);
    }

    private XhsSwitch connectXhsSwitch(DataPathId dpid) {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        XhsSwitch sw = null;
        try {
            sw = new XhsSwitch(dpid);
            sw.activate();
            print("...XHS switch activated : {} ... ", sw.getDpid());
            waitForHandshake();
        } catch (IOException e) {
            print(e);
            fail(AM_UNEX);
        }
        print("XHS switch connected...");
        return sw;
    }

    private void disconnectSwitch(BasicSwitch sw) {
        switchesGone = new CountDownLatch(1);
        lmgr.setDataPathRemovedLatch(switchesGone);
        sw.deactivate();
        waitForDisconnect();
    }

    private List<MessageEvent> removeMessagesFromQueue() {
        List<MessageEvent> messages = new ArrayList<>();
        while(!txrx.isEmpty()) {
            try {
                messages.add(txrx.take());
            } catch (InterruptedException e) {
                print(e);
                fail("interrupted");
            }
        }
        return messages;
    }

    private void initTaskGate() {
        taskGate = new CountDownLatch(1);
    }

    private void waitForTaskToComplete() {
        print(".. waiting for PH task to complete ({}ms) ..", TASK_MAX_WAIT_MS);
        try {
            taskGate.await(TASK_MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            print("(Task did not complete in time!)");
            fail("PH Task TIMEOUT");
        }
        print(".. task completed ..");
    }

    //======================================================================
    // The actual tests

    private void validateCkpt(MessageEvent ev, CheckpointEvent.Code code) {
        validateCkpt(ev, code, null);
    }

    private void validateCkpt(MessageEvent ev, CheckpointEvent.Code code,
                              String s) {
        CheckpointEvent ckpt = (CheckpointEvent) ev;
        assertEquals(AM_NEQ, code, ckpt.code());
        if (s != null)
            assertTrue("not a substring", ckpt.text().contains(s));
    }

    private void validateConn(MessageEvent ev) {
        assertEquals(AM_NEQ, OpenflowEventType.DATAPATH_CONNECTED, ev.type());
    }

    private OpenflowMessage validateMsg(MessageEvent ev, OpenflowEventType et,
                                        MessageType mt, long xid) {
        assertEquals(AM_NEQ, et, ev.type());
        OpenflowMessage msg = ev.msg();
        assertEquals(AM_NEQ, mt, msg.getType());
        assertEquals(AM_NEQ, xid, msg.getXid());
        return msg;
    }


    private void validateBarrier(MessageEvent brq, MessageEvent brp, long xid) {
        validateMsg(brq, TX, BARRIER_REQUEST, xid);
        validateMsg(brp, RX, BARRIER_REPLY, xid);
    }


    private void validateMessageEventStack(List<MessageEvent> events) {
        Iterator<MessageEvent> it = events.iterator();
        validateCkpt(it.next(), REC_START);

        // switch connects
        validateConn(it.next());

        // standard openflow handshake
        validateMsg(it.next(), RX, HELLO, 0);
        validateMsg(it.next(), TX, HELLO, 0);
        validateMsg(it.next(), TX, FEATURES_REQUEST, 103);
        validateMsg(it.next(), RX, FEATURES_REPLY, 103);
        validateMsg(it.next(), TX, SET_CONFIG, 104);

        // extended handshake - ask for desc, ports, and table features...
        OfmMultipartRequest mrq = (OfmMultipartRequest)
                validateMsg(it.next(), TX, MULTIPART_REQUEST, 105);
        assertEquals(AM_NEQ, MultipartType.DESC, mrq.getMultipartType());

        mrq = (OfmMultipartRequest)
                validateMsg(it.next(), TX, MULTIPART_REQUEST, 106);
        assertEquals(AM_NEQ, MultipartType.PORT_DESC, mrq.getMultipartType());

        mrq = (OfmMultipartRequest)
                validateMsg(it.next(), TX, MULTIPART_REQUEST, 107);
        assertEquals(AM_NEQ, MultipartType.TABLE_FEATURES, mrq.getMultipartType());

        // replies to above requests...
        OfmMultipartReply mrp = (OfmMultipartReply)
                validateMsg(it.next(), RX, MULTIPART_REPLY, 105);
        assertEquals(AM_NEQ, MultipartType.DESC, mrp.getMultipartType());

        mrp = (OfmMultipartReply)
                validateMsg(it.next(), RX, MULTIPART_REPLY, 106);
        assertEquals(AM_NEQ, MultipartType.PORT_DESC, mrp.getMultipartType());

        mrp = (OfmMultipartReply)
                validateMsg(it.next(), RX, MULTIPART_REPLY, 107);
        assertEquals(AM_NEQ, MultipartType.TABLE_FEATURES, mrp.getMultipartType());

        validateCkpt(it.next(), GEN, "Extended handshake complete");
        validateCkpt(it.next(), GEN, "Type Determined [FakeFooDevice]");

        // now we request all flows be purged.. (with barrier)
        OfmFlowMod fm = (OfmFlowMod) validateMsg(it.next(), TX, FLOW_MOD, 108);
        assertEquals(AM_NEQ, FlowModCommand.DELETE, fm.getCommand());
        assertEquals(AM_NEQ, TableId.ALL, fm.getTableId());

        validateBarrier(it.next(), it.next(), 109);

        // now we lay down initial flows (these are faked)
        fm = (OfmFlowMod) validateMsg(it.next(), TX, FLOW_MOD, 110);
        assertEquals(AM_NEQ, FlowModCommand.ADD, fm.getCommand());
        assertEquals(AM_NEQ, COOKIE_A, fm.getCookie());

        fm = (OfmFlowMod) validateMsg(it.next(), TX, FLOW_MOD, 111);
        assertEquals(AM_NEQ, FlowModCommand.ADD, fm.getCommand());
        assertEquals(AM_NEQ, COOKIE_B, fm.getCookie());

        validateBarrier(it.next(), it.next(), 112);

        validateCkpt(it.next(), GEN, "READY!!");
        validateCkpt(it.next(), REC_STOP);
    }

    @Test
    public void basic() {
        beginTest("basic");
        initController();
        txrxc.startRecording(10);
        assertTrue(AM_HUH, txrx.isRecording());
        initTaskGate();

        xhssw = connectXhsSwitch(); // waits till switch connected
        dpi = cs.getDataPathInfo(DPID);
        print("HW: {}", dpi.hardwareDescription());

        waitForTaskToComplete(); // waits till post-handshake completed
        assertTrue(AM_HUH, phTask.isValid());
        assertTrue(AM_HUH, phTaskRunInvoked);
        assertFalse(AM_HUH, phTaskRunAborted);
        assertTrue(AM_HUH, phTaskRunCompleted);

        stopRecordingAndPrintDebugTrace();
        disconnectSwitch(xhssw); // waits till switch disconnected
        validateMessageEventStack(removeMessagesFromQueue());
        endTest();
    }

    @Test
    public void phTaskIsInvalidated() {
        beginTest("phTaskIsInvalidated");
        initController();
        initTaskGate();

        blockTheQueue = true;
        xhssw = connectXhsSwitch(); // waits till switch connected

        dpi = cs.getDataPathInfo(DPID);
        print("HW: {}", dpi.hardwareDescription());

        disconnectSwitch(xhssw); // waits till switch disconnected
        blockerLatch.countDown();

        waitForTaskToComplete(); // waits till post-handshake completed
        assertFalse(AM_HUH, phTask.isValid());
        assertTrue(AM_HUH, phTaskRunInvoked);
        assertTrue(AM_HUH, phTaskRunAborted);
        assertFalse(AM_HUH, phTaskRunCompleted);

        endTest();
    }

    // DPIDs as described in CR# 152415
    private static final DataPathId DP_FIRST = dpid("0/000000:00010b");
    private static final DataPathId DP_XPOSE = dpid("0/000000:000b01");

    @Test
    public void cr152415() {
        beginTest("cr152415");
        initController();
        txrxc.startRecording(10);
        assertTrue(AM_HUH, txrx.isRecording());

        initTaskGate();
        xhssw = connectXhsSwitch(DP_FIRST); // waits till switch connected
        dpi = cs.getDataPathInfo(DP_FIRST);
        print("HW: {}", dpi.hardwareDescription());
        waitForTaskToComplete(); // waits till post-handshake completed

        initTaskGate();
        xhssw2 = connectXhsSwitch(DP_XPOSE);
        dpi = cs.getDataPathInfo(DP_XPOSE);
        print("HW: {}", dpi.hardwareDescription());
        waitForTaskToComplete(); // waits till post-handshake completed

        stopRecordingAndPrintDebugTrace();

        Set<DataPathInfo> connected = lmgr.getAllDataPathInfo();
        Set<DataPathId> dpids = new HashSet<>();
        for (DataPathInfo info: connected)
            dpids.add(info.dpid());

        assertTrue("missing dpid 010b", dpids.contains(DP_FIRST));
        assertTrue("missing dpid 0b01", dpids.contains(DP_XPOSE));
        assertEquals(AM_UXS, 2, dpids.size());

        disconnectSwitch(xhssw); // waits till switch disconnected
        disconnectSwitch(xhssw2); // waits till switch disconnected
        endTest();
    }



}
