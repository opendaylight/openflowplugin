/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.controller.impl.FlowSwitch.Expect;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.of.lib.msg.MessageFuture.Result;
import org.opendaylight.util.api.NotFoundException;
import org.opendaylight.util.junit.SlowTests;
import org.opendaylight.util.net.BigPortNumber;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.msg.BucketFactory.createMutableBucket;
import static org.opendaylight.of.lib.msg.MeterBandFactory.createBand;
import static org.opendaylight.of.lib.msg.MeterBandType.DROP;
import static org.opendaylight.of.lib.msg.MeterBandType.DSCP_REMARK;
import static org.opendaylight.of.lib.msg.MeterFlag.BURST;
import static org.opendaylight.of.lib.msg.MeterFlag.KBPS;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test harness for FlowTrk.
 *
 * @author Radhika Hegde
 * @author Simon Hunt
 */
@Category(SlowTests.class)
public class FlowInteractionsTest extends AbstractControllerTest {

    // simple10sw4port.def
    private static final String DEF = SW10P4;
    private static final DataPathId DPID = SW10P4_DPID;

    // simple13sw32port.def
    private static final String DEF_13 = SW13P32;
    private static final DataPathId DPID_13 = SW13P32_DPID;

    // simple10sw12port.def
    private static final String DEF_SLAVE = SW10P12;
    private static final DataPathId DPID_SLAVE = SW10P12_DPID;

    private static final GroupId G_ID = gid(10);
    private static final MeterId M_ID = mid(10);
    private static final BigPortNumber W_PORT = bpn(99);
    private static final int B_WEIGHT = 5;
    private static final GroupId W_GROUP = gid(8);
    private static final int MB_SIZE = 2048;
    private static final int RATE_B1 = 4000;

    private static final Set<FlowModFlag> FM_FLAGS =
            EnumSet.of(FlowModFlag.SEND_FLOW_REM);
    private static final Match MATCH = (Match) createMatch(V_1_0).toImmutable();
    private static final BigPortNumber OUT_PORT = bpn(3);
    private static final OfmFlowMod FAKE_FLOW = fakeFlow();
    private static final Bucket BUCKET = fakeBucket();
    private static final OfmGroupMod FAKE_GROUP = fakeGroup();
    private static final OfmGroupMod FAKE_INC_GROUP = fakeIncompleteGroup();
    private static final MeterBand M_BAND_DROP = fakeBandDrop();
    private static final OfmMeterMod FAKE_METER = fakeMeter();

    private static final long MAX_WAIT_MS = 500;


    @BeforeClass
    public static void classSetUp() {
        Assume.assumeTrue(!isUnderCoverage());
        setUpLogger();
    }

    @After
    public void tearDown() {
        if (txrx != null)
            printDebugTxRx();
        cmgr.shutdown();

    }

    //================================================================

    private static OfmFlowMod fakeFlow() {
        OfmMutableFlowMod m = (OfmMutableFlowMod)
                MessageFactory.create(V_1_0, MessageType.FLOW_MOD, FlowModCommand.ADD);
        m.bufferId(BufferId.NO_BUFFER).outPort(OUT_PORT).flowModFlags(FM_FLAGS)
                .match(MATCH);
        return (OfmFlowMod) m.toImmutable();
    }

    private static OfmGroupMod fakeGroup() {
        OfmMutableGroupMod m = (OfmMutableGroupMod)
                MessageFactory.create(V_1_3, MessageType.GROUP_MOD,
                        GroupModCommand.ADD);
        m.groupId(G_ID);
        m.groupType(GroupType.ALL);
        m.addBucket(BUCKET);
        return (OfmGroupMod) m.toImmutable();
    }

    private static OfmGroupMod fakeIncompleteGroup() {
        OfmMutableGroupMod m = (OfmMutableGroupMod)
                MessageFactory.create(V_1_3, MessageType.GROUP_MOD,
                        GroupModCommand.ADD);
        m.groupType(GroupType.ALL);
        m.addBucket(BUCKET);
        return (OfmGroupMod) m.toImmutable();
    }

    private static Bucket fakeBucket() {
        MutableBucket bkt = createMutableBucket(V_1_3);
        bkt.weight(B_WEIGHT).watchPort(W_PORT).watchGroup(W_GROUP)
                .addAction(createAction(V_1_3, ActionType.DEC_NW_TTL));
        return (Bucket) bkt.toImmutable();
    }

    private static OfmMeterMod fakeMeter() {
        OfmMutableMeterMod m = (OfmMutableMeterMod)
                MessageFactory.create(V_1_3, MessageType.METER_MOD,
                        MeterModCommand.ADD);
        m.meterId(M_ID);
        m.addBand(M_BAND_DROP);
        return (OfmMeterMod) m.toImmutable();
    }

    private static MeterBand fakeBandDrop() {
       return createBand(V_1_3, DROP, RATE_B1, MB_SIZE);
    }

    private void verifyMeterConfig(MBodyMeterConfig mc, int mid) {
        print(mc.toDebugString());
        assertEquals(AM_NEQ, mid(mid), mc.getMeterId());
        assertEquals(AM_NEQ, 2, mc.getBands().size());
        assertEquals(AM_NEQ, DROP, mc.getBands().get(0).getType());
        assertEquals(AM_NEQ, DSCP_REMARK, mc.getBands().get(1).getType());

    }
    private void verifyGroupDescStats(MBodyGroupDescStats s, int gid, int bsize) {
        print(s.toDebugString());
        assertEquals(AM_NEQ, gid(gid), s.getGroupId());
        assertEquals(AM_NEQ, bsize, s.getBuckets().size());
        assertEquals(AM_NEQ, GroupType.ALL, s.getType());
    }

    private void verifyGroupStats(MBodyGroupStats s, int gid, int bsize) {
        print(s.toDebugString());
        assertEquals(AM_NEQ, gid(gid), s.getGroupId());
        assertEquals(AM_NEQ, bsize, s.getBucketStats().size());
        assertEquals(AM_NEQ, 9999 * gid, s.getByteCount());
        for (int i=0; i<bsize; i++) {
            assertEquals(AM_NEQ, 1500 * (i +1),
                         s.getBucketStats().get(i).getByteCount());
        }
    }

    private void verifyFlowStats(MBodyFlowStats s, int t, long p, long b) {
        print(s.toDebugString());
        assertEquals(AM_NEQ, tid(t), s.getTableId());
        assertEquals(AM_NEQ, p, s.getPacketCount());
        assertEquals(AM_NEQ, b, s.getByteCount());
    }

    private void verifyMeterStats(MBodyMeterStats ms, int mid, int p) {
        print(ms.toDebugString());
        assertEquals(AM_NEQ, mid(mid), ms.getMeterId());
        assertEquals(AM_NEQ, 1, ms.getBandStats().size());
        assertEquals(AM_NEQ, p, ms.getPktInCount());
    }

    // ======================================================================
    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor(DPID, DPID_13);
        eds = new MockEventDispatcher();

        OpenflowController.enableIdleDetection(false);
        cmgr = new ControllerManager(DEFAULT_CTRL_CFG, alertSink, PH_SINK,
                FM_ADV, roleAdvisor, eds);
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();
        initTxRxControl(lmgr);
        print("... controller activated ...");
    }

    private FlowSwitch connectSwitch(DataPathId dpid, String def) {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        FlowSwitch sw = null;
        try {
            sw = new FlowSwitch(dpid, def);
            sw.activate();
            print("... switch activated : {} ...", sw.getDpid());
            waitForHandshake();
        } catch (IOException e) {
            print(e);
            fail(AM_UNEX);
        }
        return sw;
    }

    private void disconnectSwitch(FlowSwitch sw) {
        switchesGone = new CountDownLatch(1);
        lmgr.setDataPathRemovedLatch(switchesGone);
        sw.deactivate();
        waitForDisconnect();
    }

    // ===========================================================


    @Test(expected = IllegalArgumentException.class)
    public void notMasterSendFlow() {
        beginTest("notMasterSendFlow");
        initController();
        FlowSwitch sw = connectSwitch(DPID_SLAVE, DEF_SLAVE);
        sw.expected(Expect.BARRIER_SUCCESS);

        try {
            cs.sendConfirmedFlowMod(FAKE_FLOW, DPID_SLAVE);
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }
        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingle() {
        beginTest("mpReplySingle");
        initController();
        FlowSwitch sw = connectSwitch(DPID, DEF);
        // one flow-stats per message part (x1)
        sw.expected(Expect.MP_REPLY_SINGLE);

        List<MBodyFlowStats> stats = cs.getFlowStats(DPID, null);
        assertEquals(AM_NEQ, 1, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyFlowStats> mIter = stats.iterator();
        verifyFlowStats(mIter.next(), 0, 1000, 3300);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultiple() {
        beginTest("mpReplyMultiple");
        initController();
        FlowSwitch sw = connectSwitch(DPID, DEF);
        // one flow-stats per message part (x3)
        sw.expected(Expect.MP_REPLY_MULTIPLE);

        List<MBodyFlowStats> stats = cs.getFlowStats(DPID, null);
        assertEquals(AM_NEQ, 3, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyFlowStats> mIter = stats.iterator();
        verifyFlowStats(mIter.next(), 0, 1000, 3300);
        verifyFlowStats(mIter.next(), 0, 1020, 3370);
        verifyFlowStats(mIter.next(), 0, 1040, 3440);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingleList() {
        beginTest("mpReplySingleList");
        initController();
        FlowSwitch sw = connectSwitch(DPID, DEF);
        // three flow-stats per message part (x1)
        sw.expected(Expect.MP_REPLY_SINGLE_LIST);

        List<MBodyFlowStats> stats = cs.getFlowStats(DPID, null);
        assertEquals(AM_NEQ, 3, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyFlowStats> mIter = stats.iterator();
        verifyFlowStats(mIter.next(), 0, 1000, 3300);
        verifyFlowStats(mIter.next(), 1, 1001, 3301);
        verifyFlowStats(mIter.next(), 2, 1002, 3302);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultiList() {
        beginTest("mpReplyMultiList");
        initController();
        FlowSwitch sw = connectSwitch(DPID, DEF);
        // one flow-stats per message part (x3)
        sw.expected(Expect.MP_REPLY_MULTIPLE_LIST);

        List<MBodyFlowStats> stats = cs.getFlowStats(DPID, null);
        assertEquals(AM_NEQ, 4, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyFlowStats> mIter = stats.iterator();
        verifyFlowStats(mIter.next(), 0, 1000, 3300);
        verifyFlowStats(mIter.next(), 1, 1001, 3301);
        verifyFlowStats(mIter.next(), 0, 1020, 3370);
        verifyFlowStats(mIter.next(), 1, 1021, 3371);

        disconnectSwitch(sw);
        endTest();
    }

    // FIXME: validate this test, now that we are not waiting for the future..
    @Test @Ignore("Needs validation - result is SUCCESS_NO_REPLY")
    public void noBarrierButError() {
        beginTest("noBarrierButError");
        initController();
        FlowSwitch sw = connectSwitch(DPID, DEF);
        sw.expected(Expect.NO_BARRIER_BUT_ERROR);

        MessageFuture future;
        try {
            future = cs.sendConfirmedFlowMod(FAKE_FLOW, DPID);
            future.awaitUninterruptibly(MAX_WAIT_MS);
            assertEquals(AM_NEQ, Result.OFM_ERROR, future.result());
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void barrierSuccessFlowPush() {
        beginTest("barrierSuccessFlowPush");
        initController();
        FlowSwitch sw = connectSwitch(DPID, DEF);
        sw.expected(Expect.BARRIER_SUCCESS);

        MessageFuture future;
        try {
            future = cs.sendConfirmedFlowMod(FAKE_FLOW, DPID);
            future.awaitUninterruptibly(MAX_WAIT_MS);
            assertEquals(AM_NEQ, Result.SUCCESS, future.result());
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }

        disconnectSwitch(sw);
        endTest();
    }
    @Test
    public void barrierSuccessGroupPush() {
        beginTest("barrierSuccessGroupPush");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);
        sw.expected(Expect.BARRIER_SUCCESS);

        MessageFuture future;
        try {
            future = cs.sendGroupMod(FAKE_GROUP, DPID_13);
            future.awaitUninterruptibly(MAX_WAIT_MS);
            assertEquals(AM_NEQ, Result.SUCCESS, future.result());
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }
        disconnectSwitch(sw);
        endTest();
    }

    @Ignore @Test
    public void failGroupPush() {
        beginTest("failGroupPush");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);
        MessageFuture future = null;
        try {
            future = cs.sendGroupMod(FAKE_INC_GROUP, DPID_13);
        } catch (OpenflowException e) {
            // expected exception
        }
        if (future != null) {
            future.awaitUninterruptibly(MAX_WAIT_MS);
            assertEquals(AM_NEQ, Result.EXCEPTION, future.result());
        }
        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void barrierSuccessMeterPush() {
        beginTest("barrierSuccessMeterPush");
        initController();
        txrx.startRecording(5);
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);
        sw.expected(Expect.BARRIER_SUCCESS);
        MessageFuture future;
        try {
            future = cs.sendMeterMod(FAKE_METER, DPID_13);
            future.awaitUninterruptibly(MAX_WAIT_MS);
            assertEquals(AM_NEQ, Result.SUCCESS, future.result());
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }
        disconnectSwitch(sw);
        txrx.stopRecording();
        endTest();
    }
    @Test
    public void noBarrierButErrorSendGroup() {
        beginTest("noBarrierButErrorSendGroup");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);
        sw.expected(Expect.NO_BARRIER_BUT_ERROR);
        MessageFuture future;
        try {
            future = cs.sendGroupMod(FAKE_GROUP, DPID_13);
            future.awaitUninterruptibly(MAX_WAIT_MS);
            assertEquals(AM_NEQ, Result.OFM_ERROR, future.result());
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }
        disconnectSwitch(sw);
        endTest();
    }
    @Test
    public void noBarrierButErrorSendMeter() {
        beginTest("noBarrierButErrorSendMeter");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);
        sw.expected(Expect.NO_BARRIER_BUT_ERROR);
        MessageFuture future;
        try {
            future = cs.sendMeterMod(FAKE_METER, DPID_13);
            future.awaitUninterruptibly(MAX_WAIT_MS);
            assertEquals(AM_NEQ, Result.OFM_ERROR, future.result());
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }
        disconnectSwitch(sw);
        endTest();
    }
    @Test(expected = NullPointerException.class)
    public void checkNullFlowStats() {
        beginTest("checkNullFlowStats");
        initController();
        cs.getFlowStats(null, null);
        endTest();
    }
    @Test(expected = NullPointerException.class)
    public void checkNullGroupStats() {
        beginTest("checkNullGroupStats");
        initController();
        cs.getGroupStats(null);
        endTest();
    }
    @Test(expected = NullPointerException.class)
    public void checkNullGroupDescr() {
        beginTest("checkNullGroupDescr");
        initController();
        cs.getGroupDescription(null);
        endTest();
    }
    @Test(expected = NullPointerException.class)
    public void checkNullMeterStats() {
        beginTest("checkNullMeterStats");
        initController();
        cs.getMeterStats(null);
        endTest();
    }
    @Test(expected = NullPointerException.class)
    public void checkNullMeterConfig() {
        beginTest("checkNullMeterConfig");
        initController();
        cs.getMeterConfig(null);
        endTest();
    }
    @Test(expected = VersionMismatchException.class)
    public void checkNotSupportedMeterConfig() {
        beginTest("checkNotSupportedMeterConfig");
        initController();
        connectSwitch(DPID, DEF);
        cs.getMeterConfig(DPID, M_ID);
        endTest();
    }

    @Test(expected = NotFoundException.class)
    public void checkExceptionHandlingNotFound() {
        beginTest("checkExceptionHandlingNotFound");
        initController();
        cs.getFlowStats(DPID, null);
        endTest();
    }

    @Test
    public void mpReplySingleGroup() {
        beginTest("mpReplySingleGroup");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_SINGLE);

        List<MBodyGroupDescStats> stats = cs.getGroupDescription(DPID_13);
        assertEquals(AM_NEQ, 1, stats.size());

        // see FlowSwitch for why these values...
        // nth group = n buckets..
        Iterator<MBodyGroupDescStats> mIter = stats.iterator();
        verifyGroupDescStats(mIter.next(), 1, 1);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultipleListsGroup() {
        beginTest("mpReplyMultipleListsGroup");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_MULTIPLE_LIST);

        List<MBodyGroupDescStats> stats = cs.getGroupDescription(DPID_13);
        assertEquals(AM_NEQ, 5, stats.size());

        // see FlowSwitch for why these values...
        // nth group = n buckets..
        Iterator<MBodyGroupDescStats> mIter = stats.iterator();
        verifyGroupDescStats(mIter.next(), 1, 1);
        verifyGroupDescStats(mIter.next(), 2, 2);
        verifyGroupDescStats(mIter.next(), 3, 1);
        verifyGroupDescStats(mIter.next(), 4, 2);
        verifyGroupDescStats(mIter.next(), 5, 1);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultipleGroup() {
        beginTest("mpReplyMultipleGroup");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_MULTIPLE);

        List<MBodyGroupDescStats> stats = cs.getGroupDescription(DPID_13);
        assertEquals(AM_NEQ, 3, stats.size());

        // see FlowSwitch for why these values...
        // nth group = n buckets..
        Iterator<MBodyGroupDescStats> mIter = stats.iterator();
        verifyGroupDescStats(mIter.next(), 1, 1);
        verifyGroupDescStats(mIter.next(), 2, 1);
        verifyGroupDescStats(mIter.next(), 3, 1);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingleListGroup() {
        beginTest("mpReplySingleListGroup");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_SINGLE_LIST);

        List<MBodyGroupDescStats> stats = cs.getGroupDescription(DPID_13);
        assertEquals(AM_NEQ, 5, stats.size());

        // see FlowSwitch for why these values...
        // nth group = n buckets..
        Iterator<MBodyGroupDescStats> mIter = stats.iterator();
        verifyGroupDescStats(mIter.next(), 1, 1);
        verifyGroupDescStats(mIter.next(), 2, 2);
        verifyGroupDescStats(mIter.next(), 3, 3);
        verifyGroupDescStats(mIter.next(), 4, 4);
        verifyGroupDescStats(mIter.next(), 5, 5);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingleGroupStats() {
        beginTest("mpReplySingleGroupStats");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_SINGLE);

        List<MBodyGroupStats> stats = cs.getGroupStats(DPID_13);
        assertEquals(AM_NEQ, 1, stats.size());

        // see FlowSwitch for why these values...
        // nth group = n buckets..
        Iterator<MBodyGroupStats> mIter = stats.iterator();
        verifyGroupStats(mIter.next(), 1, 1);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultipleListsGroupStats() {
        beginTest("mpReplyMultipleListsGroupStats");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_MULTIPLE_LIST);

        List<MBodyGroupStats> stats = cs.getGroupStats(DPID_13);
        assertEquals(AM_NEQ, 5, stats.size());

        // see FlowSwitch for why these values...
        // nth group = n buckets..
        Iterator<MBodyGroupStats> mIter = stats.iterator();
        verifyGroupStats(mIter.next(), 1, 1);
        verifyGroupStats(mIter.next(), 2, 2);
        verifyGroupStats(mIter.next(), 3, 1);
        verifyGroupStats(mIter.next(), 4, 2);
        verifyGroupStats(mIter.next(), 5, 1);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultipleGroupStats() {
        beginTest("mpReplyMultipleGroupStats");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_MULTIPLE);

        List<MBodyGroupStats> stats = cs.getGroupStats(DPID_13);
        assertEquals(AM_NEQ, 3, stats.size());

        // see FlowSwitch for why these values...
        // nth group = n buckets..
        Iterator<MBodyGroupStats> mIter = stats.iterator();
        verifyGroupStats(mIter.next(), 1, 1);
        verifyGroupStats(mIter.next(), 2, 1);
        verifyGroupStats(mIter.next(), 3, 1);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingleListGroupStats() {
        beginTest("mpReplySingleListGroupStats");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_SINGLE_LIST);

        List<MBodyGroupStats> stats = cs.getGroupStats(DPID_13);
        assertEquals(AM_NEQ, 5, stats.size());

        // see FlowSwitch for why these values...
        // nth group = n buckets..
        Iterator<MBodyGroupStats> mIter = stats.iterator();
        verifyGroupStats(mIter.next(), 1, 1);
        verifyGroupStats(mIter.next(), 2, 2);
        verifyGroupStats(mIter.next(), 3, 3);
        verifyGroupStats(mIter.next(), 4, 4);
        verifyGroupStats(mIter.next(), 5, 5);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingleMeterConfig() {
        beginTest("mpReplySingleMeterConfig");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_SINGLE);

        List<MBodyMeterConfig> stats = cs.getMeterConfig(DPID_13);
        assertEquals(AM_NEQ, 1, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyMeterConfig> mIter = stats.iterator();
        verifyMeterConfig(mIter.next(), 1);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultipleListsMeterConfig() {
        beginTest("mpReplyMultipleListsMeterConfig");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_MULTIPLE_LIST);

        List<MBodyMeterConfig> stats = cs.getMeterConfig(DPID_13);
        assertEquals(AM_NEQ, 4, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyMeterConfig> mIter = stats.iterator();
        verifyMeterConfig(mIter.next(), 1);
        verifyMeterConfig(mIter.next(), 2);
        verifyMeterConfig(mIter.next(), 3);
        verifyMeterConfig(mIter.next(), 4);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultipleMeterConfig() {
        beginTest("mpReplyMultipleMeterConfig");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_MULTIPLE);

        List<MBodyMeterConfig> stats = cs.getMeterConfig(DPID_13);
        assertEquals(AM_NEQ, 3, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyMeterConfig> mIter = stats.iterator();
        verifyMeterConfig(mIter.next(), 1);
        verifyMeterConfig(mIter.next(), 2);
        verifyMeterConfig(mIter.next(), 3);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingleListMeterConfig() {
        beginTest("mpReplySingleListMeterConfig");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_SINGLE_LIST);

        List<MBodyMeterConfig> stats = cs.getMeterConfig(DPID_13);
        assertEquals(AM_NEQ, 3, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyMeterConfig> mIter = stats.iterator();
        verifyMeterConfig(mIter.next(), 1);
        verifyMeterConfig(mIter.next(), 2);
        verifyMeterConfig(mIter.next(), 3);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingleMeterStats() {
        beginTest("mpReplySingleMeterStats");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_SINGLE);

        List<MBodyMeterStats> stats = cs.getMeterStats(DPID_13);
        assertEquals(AM_NEQ, 1, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyMeterStats> mIter = stats.iterator();
        verifyMeterStats(mIter.next(), 1, 100);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultipleListsMeterStats() {
        beginTest("mpReplyMultipleListsMeterStats");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_MULTIPLE_LIST);

        List<MBodyMeterStats> stats = cs.getMeterStats(DPID_13);
        assertEquals(AM_NEQ, 3, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyMeterStats> mIter = stats.iterator();
        verifyMeterStats(mIter.next(), 1, 100);
        verifyMeterStats(mIter.next(), 2, 200);
        verifyMeterStats(mIter.next(), 3, 100);
        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultipleMeterStats() {
        beginTest("mpReplyMultipleMeterStats");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_MULTIPLE);

        List<MBodyMeterStats> stats = cs.getMeterStats(DPID_13);
        assertEquals(AM_NEQ, 4, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyMeterStats> mIter = stats.iterator();
        verifyMeterStats(mIter.next(), 1, 100);
        verifyMeterStats(mIter.next(), 2, 100);
        verifyMeterStats(mIter.next(), 3, 100);
        verifyMeterStats(mIter.next(), 4, 100);
        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingleListMeterStats() {
        beginTest("mpReplySingleListMeterStats");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);

        sw.expected(Expect.MP_REPLY_SINGLE_LIST);

        List<MBodyMeterStats> stats = cs.getMeterStats(DPID_13);
        assertEquals(AM_NEQ, 3, stats.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyMeterStats> mIter = stats.iterator();
        verifyMeterStats(mIter.next(), 1, 100);
        verifyMeterStats(mIter.next(), 2, 200);
        verifyMeterStats(mIter.next(), 3, 300);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplySingleExperimenter() {
        beginTest("mpReplySingleExperimenter");
        initController();
        FlowSwitch sw = connectSwitch(DPID, DEF);

        sw.expected(Expect.MP_REPLY_SINGLE);

        List<MBodyExperimenter> exp = cs.getExperimenter(DPID);
        assertEquals(AM_NEQ, 1, exp.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyExperimenter> mIter = exp.iterator();
        MBodyExperimenter mExp = mIter.next();
        assertEquals(AM_NEQ, ExperimenterId.HP,mExp.getExpId());
        assertEquals(AM_NEQ, 28, mExp.getData().length);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void mpReplyMultipleExperimenter() {
        beginTest("mpReplyMultipleExperimenter");
        initController();
        FlowSwitch sw = connectSwitch(DPID, DEF);

        sw.expected(Expect.MP_REPLY_MULTIPLE);

        List<MBodyExperimenter> exp = cs.getExperimenter(DPID);
        assertEquals(AM_NEQ, 2, exp.size());

        // see FlowSwitch for why these values...
        Iterator<MBodyExperimenter> mIter = exp.iterator();
        MBodyExperimenter mExp = mIter.next();
        assertEquals(AM_NEQ, ExperimenterId.HP,mExp.getExpId());
        assertEquals(AM_NEQ, 28, mExp.getData().length);
        // type 0 for OF 1.0 version
        assertEquals(AM_NEQ, 0, mIter.next().getExpType());

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void groupFeatures() {
        beginTest("groupFeatures");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);
        MBodyGroupFeatures gf = cs.getGroupFeatures(DPID_13);
        print(gf.toDebugString());

        // see FlowSwitch for these expected values...
        verifyFlags(gf.getTypes(), GroupType.ALL, GroupType.SELECT,
                GroupType.INDIRECT, GroupType.FF);
        verifyFlags(gf.getCapabilities(), GroupCapability.SELECT_LIVENESS,
                GroupCapability.SELECT_WEIGHT);
        assertEquals(AM_NEQ, 3, gf.getMaxGroupsForType(GroupType.ALL));
        assertEquals(AM_NEQ, 6, gf.getMaxGroupsForType(GroupType.SELECT));
        assertEquals(AM_NEQ, 8, gf.getMaxGroupsForType(GroupType.INDIRECT));
        assertEquals(AM_NEQ, 2, gf.getMaxGroupsForType(GroupType.FF));
        verifyFlags(gf.getActionsForType(GroupType.ALL),
                ActionType.COPY_TTL_IN, ActionType.COPY_TTL_OUT);
        verifyFlags(gf.getActionsForType(GroupType.SELECT),
                ActionType.PUSH_VLAN, ActionType.POP_VLAN);
        verifyFlags(gf.getActionsForType(GroupType.INDIRECT),
                ActionType.SET_MPLS_TTL, ActionType.SET_FIELD,
                ActionType.SET_NW_TTL);
        verifyFlags(gf.getActionsForType(GroupType.FF), ActionType.DEC_NW_TTL);

        disconnectSwitch(sw);
        endTest();
    }


    @Test
    public void meterFeatures() {
        beginTest("meterFeatures");
        initController();
        FlowSwitch sw = connectSwitch(DPID_13, DEF_13);
        MBodyMeterFeatures mf = cs.getMeterFeatures(DPID_13);
        print(mf.toDebugString());

        // see FlowSwitch for these expected values...
        assertEquals(AM_NEQ, 36, mf.getMaxMeters());
        verifyFlags(mf.getBandTypes(), DROP, DSCP_REMARK);
        verifyFlags(mf.getCapabilities(), BURST, KBPS);
        assertEquals(AM_NEQ, 4, mf.getMaxBands());
        assertEquals(AM_NEQ, 15, mf.getMaxColor());

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void meterFeaturesNot10() {
        beginTest("meterFeaturesNot10");
        initController();
        FlowSwitch sw = connectSwitch(DPID, DEF);
        try {
            cs.getMeterFeatures(DPID);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertEquals(AM_WREXMSG, EMSG_NOT_SUP_BEFORE_13, e.getMessage());
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
        disconnectSwitch(sw);
        endTest();
    }

    @Test(expected = NotFoundException.class)
    public void meterFeaturesNoSuchDatapath() {
        initController();
        cs.getMeterFeatures(DPID);
    }
}
