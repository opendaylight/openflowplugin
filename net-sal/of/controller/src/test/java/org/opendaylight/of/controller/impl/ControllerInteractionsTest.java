/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.*;
import org.opendaylight.of.controller.ControllerStats;
import org.opendaylight.of.controller.DataPathEvent;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.controller.pkt.SequencedPacketAdapter;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.opendaylight.of.controller.OpenflowEventType.DATAPATH_CONNECTED;
import static org.opendaylight.of.controller.OpenflowEventType.DATAPATH_DISCONNECTED;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Testing some of the controller-to-switch-and-back interactions.
 *
 * @author Simon Hunt
 */
public class ControllerInteractionsTest extends AbstractControllerTest {

    // same as in simple13sw32port.def
    private static final String DEF_M = SW13P32;
    private static final DataPathId DPID_MASTER = SW13P32_DPID;

    private static final String DEF_S = SW10P12;
    private static final DataPathId DPID_SLAVE = SW10P12_DPID;

    private static final BigPortNumber INPORT = bpn(3);
    private static final BufferId BUFFER = BufferId.NO_BUFFER;
    private static final TableId TABLE = tid(4);

    private static final int PI_TOTAL_LEN = 120;
    private static final byte[] FAKE_PACKET = new byte[PI_TOTAL_LEN];


    // A test switch that will fire off a Packet-In message when desired
    private static class PiSwitch extends BasicSwitch {
        public PiSwitch(DataPathId dpid, String defPath) throws IOException {
            super(dpid, defPath);
        }

        public void doPi() {
            OfmMutablePacketIn pi = (OfmMutablePacketIn)
                    create(V_1_3, PACKET_IN, PacketInReason.NO_MATCH);
            pi.totalLen(PI_TOTAL_LEN).data(FAKE_PACKET);
            MutableMatch m = MatchFactory.createMatch(V_1_3);
            m.addField(createBasicField(V_1_3, OxmBasicFieldType.IN_PORT, INPORT));
            pi.bufferId(BUFFER).tableId(TABLE).match((Match) m.toImmutable());
            send(pi.toImmutable());
        }
    }

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
    private static class TestObserver extends SequencedPacketAdapter {
        private CountDownLatch latch;
        @Override
        public void event(MessageContext context) {
            if (latch != null)
                latch.countDown();
        }
    }

    ControllerStats stats;
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
        if (cmgr != null)
            cmgr.shutdown();
    }

    // ======================================================================
    // === HELPER methods

    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor(DPID_MASTER);
        eds = new MockEventDispatcher();

        cmgr = new TestControllerManager(DEFAULT_CTRL_CFG, alertSink,
                roleAdvisor, eds);
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();
        lmgr.resetStats();
        initTxRxControl(lmgr);

        print("... controller activated ...");
    }

    private BasicSwitch connectSwitch(DataPathId dpid, String def) {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        BasicSwitch sw = null;
        try {
            sw = new BasicSwitch(dpid, def);
            sw.activate();
            print("... switch activated : {} ...", sw.getDpid());
            waitForHandshake();
        } catch (IOException e) {
            print(e);
            fail(AM_UNEX);
        }
        return sw;
    }

    private PiSwitch connectPiSwitch() {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        PiSwitch sw = null;
        try {
            sw = new PiSwitch(DPID_MASTER, DEF_M);
            sw.activate();
            print("... PI switch activated : {} ...", sw.getDpid());
            waitForHandshake();
        } catch (IOException e) {
            print(e);
            fail(AM_UNEX);
        }
        return sw;
    }

    private void disconnectSwitch(BasicSwitch sw) {
        switchesGone = new CountDownLatch(1);
        lmgr.setDataPathRemovedLatch(switchesGone);
        sw.deactivate();
        waitForDisconnect();
    }

    private OpenflowMessage createMpDesc(ProtocolVersion pv) {
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, MultipartType.DESC);
        return req.toImmutable();
    }

    private static final Set<ConfigFlag> C_FLAGS =
            EnumSet.of(ConfigFlag.FRAG_REASM);

    private OpenflowMessage createSetConfig(ProtocolVersion pv) {
        OfmMutableSetConfig sc = (OfmMutableSetConfig)
                create(pv, SET_CONFIG);
        sc.setConfigFlags(C_FLAGS);
        return sc.toImmutable();
    }

    private void firePi(PiSwitch sw) {
        listenersProcessed = new CountDownLatch(1);
        obs.latch = listenersProcessed;
        sw.doPi();
        waitForListeners();
    }

    // =======

    @Test
    @Ignore // FIXME Disabled this test because it fails "fp initial"
    public void basic() throws OpenflowException {
        beginTest("basic");
        initController();

        BasicSwitch sw = connectSwitch(DPID_MASTER, DEF_M);
        DataPathInfo dpi = cs.getDataPathInfo(DPID_MASTER);
        ProtocolVersion pv = dpi.negotiated();

        MessageFuture f = waitForFuture(cs.send(createMpDesc(pv), DPID_MASTER));
        assertEquals(AM_NEQ, MessageFuture.Result.SUCCESS, f.result());
        OpenflowMessage msg = f.reply();
        print(msg.toDebugString());
        assertEquals(AM_NEQ, MessageType.MULTIPART_REPLY, msg.getType());
        OfmMultipartReply mp = (OfmMultipartReply) msg;
        assertEquals(AM_NEQ, MultipartType.DESC, mp.getMultipartType());
        MBodyDesc desc = (MBodyDesc) mp.getBody();
        assertEquals(AM_NEQ, "WB-11954-TAF", desc.getSerialNum());

        disconnectSwitch(sw);
        endTest();
    }
    
    @Test
    public void deviceDescriptionIsStandard() {
        beginTest("deviceDescriptionIsStandard");        
        initController();
        
        // Mp-Reply/DESC is done for us by the full handshake
        //  we don't need to send one ourselves explicitly...
        BasicSwitch sw = connectSwitch(DPID_MASTER, DEF_M);
        DataPathInfo dpi = cs.getDataPathInfo(DPID_MASTER);
        print("Manuf. {}", dpi.manufacturerDescription());
        print("Ser #. {}", dpi.serialNumber());
        print("H/W    {}", dpi.hardwareDescription());
        print("S/W    {}", dpi.softwareDescription());
        print("Desc.  {}", dpi.datapathDescription());
        assertEquals(AM_NEQ, SW13P32_MFR_DESC, dpi.manufacturerDescription());
        assertEquals(AM_NEQ, SW13P32_SER_NUM, dpi.serialNumber());
        assertEquals(AM_NEQ, SW13P32_HW_DESC, dpi.hardwareDescription());
        assertEquals(AM_NEQ, SW13P32_SW_DESC, dpi.softwareDescription());
        assertEquals(AM_NEQ, SW13P32_DP_DESC, dpi.datapathDescription());

        disconnectSwitch(sw);
        endTest();
    }
    
    private static final String EMPTY = "";
    
    @Test
    public void checkDpInfoSnapshotMBodyDescNull() {
        beginTest("checkDpInfoSnapshotMBodyDescNull");

        OpenflowConnection ocMock = createMock(OpenflowConnection.class);
        expect(ocMock.isMain()).andReturn(true);
        expect(ocMock.getNegotiated()).andReturn(null);
        expect(ocMock.getFeaturesReply()).andReturn(null);
        replay(ocMock);

        DpInfo dpi = new DpInfo(ocMock, null, null);
        DpInfo.DpInfoSnapshot snapshot = dpi.new DpInfoSnapshot(dpi);
        assertEquals(AM_NEQ, EMPTY, snapshot.manufacturerDescription());
        assertEquals(AM_NEQ, EMPTY, snapshot.serialNumber());
        assertEquals(AM_NEQ, EMPTY, snapshot.hardwareDescription());
        assertEquals(AM_NEQ, EMPTY, snapshot.softwareDescription());
        assertEquals(AM_NEQ, EMPTY, snapshot.datapathDescription());

        endTest();
    }

    
    @Test
    public void gettingCachedDeviceDesc() {
        beginTest("gettingCachedDeviceDesc");
        initController();
        
        // check that we can retrieve the device desc internally. 
        BasicSwitch sw = connectSwitch(DPID_MASTER, DEF_M);
        MBodyDesc desc = lmgr.getCachedDeviceDesc(DPID_MASTER);
        print(desc.toDebugString());
        assertEquals(AM_NEQ, SW13P32_MFR_DESC, desc.getMfrDesc());
        assertEquals(AM_NEQ, SW13P32_SER_NUM, desc.getSerialNum());
        assertEquals(AM_NEQ, SW13P32_HW_DESC, desc.getHwDesc());
        assertEquals(AM_NEQ, SW13P32_SW_DESC, desc.getSwDesc());
        assertEquals(AM_NEQ, SW13P32_DP_DESC, desc.getDpDesc());

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void nonExistentDpid() {
        beginTest("nonExistentDpid");
        initController();
        try {
            cs.send(createMpDesc(V_1_3), DPID_MASTER);
            fail(AM_NOEX);
        } catch (OpenflowException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, "No datapath with id: 00:2a:00:16:b9:06:80:00",
                    e.getMessage());
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
        endTest();
    }

    private void trySending(MessageType mt) {
        MutableMessage mm = create(V_1_3, mt);
        OpenflowMessage m = mm.toImmutable();
        try {
            cs.send(m, DPID_MASTER);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, "Non-Controller-Initiated message type: " + mt,
                    e.getMessage());
        } catch (OpenflowException e) {
            print(e);
            fail(AM_WREX);
        }
    }

    @Test
    public void notAllowedToSend() {
        beginTest("notAllowedToSend");
        initController();

        trySending(FEATURES_REQUEST);
        trySending(FEATURES_REPLY);
        trySending(GET_CONFIG_REPLY);
        trySending(PACKET_IN);
        trySending(FLOW_REMOVED);
        trySending(PORT_STATUS);
        trySending(MULTIPART_REPLY);
        trySending(BARRIER_REPLY);
        trySending(QUEUE_GET_CONFIG_REPLY);
        trySending(GET_ASYNC_REPLY);

        endTest();
    }

    @Test
    public void flowModNotFromSend() {
        beginTest("flowModNotFromSend");
        initController();

        MutableMessage mm = create(V_1_3, FLOW_MOD);
        OpenflowMessage m = mm.toImmutable();
        try {
            cs.send(m, DPID_MASTER);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, "FlowMod disallowed via send()",
                    e.getMessage());
        } catch (OpenflowException e) {
            print(e);
            fail(AM_WREX);
        }

        endTest();
    }


    @Test @Ignore
    public void mastersAndSlaves() throws OpenflowException {
        beginTest("mastersAndSlaves");
        initController();

        // a switch for which we are master
        BasicSwitch swMaster = connectSwitch(DPID_MASTER, DEF_M);
        DataPathInfo dpiMaster = cs.getDataPathInfo(DPID_MASTER);
        ProtocolVersion pvMaster = dpiMaster.negotiated();
        assertEquals(AM_NEQ, V_1_3, pvMaster);

        // a switch for which we are slave (not master)
        BasicSwitch swSlave = connectSwitch(DPID_SLAVE, DEF_S);
        DataPathInfo dpiSlave = cs.getDataPathInfo(DPID_SLAVE);
        ProtocolVersion pvSlave = dpiSlave.negotiated();
        assertEquals(AM_NEQ, V_1_0, pvSlave);

        // we should be able to send a SET_CONFIG to swMaster
        MessageFuture f =
                waitForFuture(cs.send(createSetConfig(pvMaster), DPID_MASTER));
        assertEquals(AM_NEQ, MessageFuture.Result.SUCCESS_NO_REPLY, f.result());
        print(f);

        // we should NOT be able to send a SET_CONFIG to swSlave
        try {
            cs.send(createSetConfig(pvSlave), DPID_SLAVE);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, "Write op disallowed (not master): SET_CONFIG",
                    e.getMessage());
        } catch (Exception e) {
            fail(AM_WREX);
        }

        // but we CAN send a MP/DESC, for example, to swSlave
        f = waitForFuture(cs.send(createMpDesc(pvSlave), DPID_SLAVE));
        assertEquals(AM_NEQ, MessageFuture.Result.SUCCESS, f.result());
        print(f.reply().toDebugString());

        disconnectSwitch(swMaster);
        disconnectSwitch(swSlave);
        endTest();
    }

    private class MyDpListener extends DataPathListenerAdapter {
        private DataPathEvent conn;
        private DataPathEvent disc;

        @Override
        public void event(DataPathEvent event) {
            print("DP Event: {}", event);
            switch (event.type()) {
                case DATAPATH_CONNECTED:
                    conn = event;
                    break;
                case DATAPATH_DISCONNECTED:
                    disc = event;
                    break;
            }
        }
    }


    @Test
    public void updatedDataPathEvent() {
        beginTest("updatedDataPathEvent");
        initController();
        MyDpListener dpl = new MyDpListener();
        cs.addDataPathListener(dpl);
        BasicSwitch sw = connectSwitch(DPID_MASTER, DEF_M);
        delay(10); // allow events to filter through
        disconnectSwitch(sw);
        delay(10); // allow events to filter through

        print("Saw conn: {}", dpl.conn);
        print("Saw disc: {}", dpl.disc);

        assertNotNull(dpl.conn);
        assertEquals(AM_NEQ, DATAPATH_CONNECTED, dpl.conn.type());
        assertEquals(AM_NEQ, DPID_MASTER, dpl.conn.dpid());
        assertEquals(AM_NEQ, IpAddress.LOOPBACK_IPv4, dpl.conn.ip());

        assertNotNull(dpl.disc);
        assertEquals(AM_NEQ, DATAPATH_DISCONNECTED, dpl.disc.type());
        assertEquals(AM_NEQ, DPID_MASTER, dpl.disc.dpid());
        assertEquals(AM_NEQ, IpAddress.LOOPBACK_IPv4, dpl.disc.ip());
        endTest();
    }


    private void verifyPacketCounts(int in, int out, int drop) {
        assertEquals(AM_NEQ, in, stats.packetInCount());
        assertEquals(AM_NEQ, out, stats.packetOutCount());
        assertEquals(AM_NEQ, drop, stats.packetDropCount());
        int expInBytes = in * PI_TOTAL_LEN;
        int expOutBytes = out * PI_TOTAL_LEN;
        int expDropBytes = drop * PI_TOTAL_LEN;
        assertEquals(AM_NEQ, expInBytes, stats.packetInBytes());
        assertEquals(AM_NEQ, expOutBytes, stats.packetOutBytes());
        assertEquals(AM_NEQ, expDropBytes, stats.packetDropBytes());
    }

    @Test
    @Ignore // FIXME Disabled this test because it fails "fp initial" often
    public void controllerStats() throws OpenflowException {
        beginTest("controllerStats");
        initController();
        startRecording(2);
        
        BasicSwitch sw = connectSwitch(DPID_MASTER, DEF_M);
        DataPathInfo dpi = cs.getDataPathInfo(DPID_MASTER);
        ProtocolVersion pv = dpi.negotiated();

        MessageFuture f = waitForFuture(cs.send(createMpDesc(pv), DPID_MASTER));
        assertEquals(AM_NEQ, MessageFuture.Result.SUCCESS, f.result());
        OpenflowMessage msg = f.reply();
        print(msg.toDebugString());
        assertEquals(AM_NEQ, MessageType.MULTIPART_REPLY, msg.getType());
        OfmMultipartReply mp = (OfmMultipartReply) msg;
        assertEquals(AM_NEQ, MultipartType.DESC, mp.getMultipartType());
        MBodyDesc desc = (MBodyDesc) mp.getBody();
        assertEquals(AM_NEQ, "WB-11954-TAF", desc.getSerialNum());

        stopRecordingAndPrintDebugTrace();
        print("-----------------------------------");
//        stopRecordingAndPrintDetailedDebugTrace();

        stats = cs.getStats();
        print(stats);
        verifyPacketCounts(0, 0, 0);


        /* Message Sequence...
          (RX)  null  (initial connect; not counted in stats)
          TX>>  [V_1_3,HELLO,len=16,xid=0]
          <<RX  [V_1_3,HELLO,len=16,xid=0]
          TX>>  [V_1_3,FEATURES_REQUEST,len=8,xid=103]
          <<RX  [V_1_3,FEATURES_REPLY,len=32,xid=103]

    [a]   TX>>  [V_1_3,SET_CONFIG,len=12,xid=104]

    [b]   TX>>  [V_1_3,MULTIPART_REQUEST/DESC,len=16,xid=105]
    [b]   TX>>  [V_1_3,MULTIPART_REQUEST/PORT_DESC,len=16,xid=106]
    [b]   TX>>  [V_1_3,MULTIPART_REQUEST/TABLE_FEATURES,len=16,xid=107]
    [b]   <<RX  [V_1_3,MULTIPART_REPLY/DESC,len=1072,xid=105]
    [b]   <<RX  [V_1_3,MULTIPART_REPLY/PORT_DESC,len=2064,xid=106]
    [b]   <<RX  [V_1_3,MULTIPART_REPLY/TABLE_FEATURES,len=16,xid=107]

    [e]   TX>>  [V_1_3,FLOW_MOD,len=???,xid=???]

    [c]   TX>>  [V_1_3,MULTIPART_REQUEST/DESC,len=16,xid=108]
    [c]   <<RX  [V_1_3,MULTIPART_REPLY/DESC,len=1072,xid=108]

        Notes:
            [a] controller auto set-config
            [b] mock switch is 1.3, so we ask for MP/Desc, PortDesc, TableFeats
            [c] MP/Desc sent from this unit test
            [e] default flow mod (sometimes seen, sometimes not)
         */

        // FIXME: sometimes we catch the outgoing default flowmod, sometimes not
//        assertEquals(AM_NEQ, 7, stats.msgTxCount());
        long tx = stats.msgTxCount();
        assertTrue(AM_HUH, tx >= 7 && tx <= 8);

        assertEquals(AM_NEQ, 6, stats.msgRxCount());

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void controllerStatsPackets() {
        beginTest("controllerStatsPackets");
        initController();

        PiSwitch sw = connectPiSwitch();
        stats = cs.getStats();
        print(stats);
        verifyPacketCounts(0, 0, 0);

        // let's tee up some packet listeners
        dir = new TestDirector();
        dir.willHandle = true;
        obs = new TestObserver();

        cs.addPacketListener(dir, SequencedPacketListenerRole.DIRECTOR, 5);
        cs.addPacketListener(obs, SequencedPacketListenerRole.OBSERVER, 0);

        // now fire off a packet-in
        firePi(sw);
        stats = cs.getStats();
        print(stats);
        verifyPacketCounts(1, 1, 0);

        // let's NOT handle a packet-in
        /*
         * NOTE: we can't have the not-handled case be the last case before
         *      verifying the counts, because the event-wait will possibly
         *      unblock before count is updated. Therefore, we'll follow the
         *      not-handled packet-in with a handled one; and then verify
         *      the combined counts.
         */
        dir.willHandle = false;
        firePi(sw);
        dir.willHandle = true;
        firePi(sw);

        stats = cs.getStats();
        print(stats);
        verifyPacketCounts(3, 2, 1);

        disconnectSwitch(sw);
        endTest();
    }

    // TODO: test controller stats for reset statistics
    // TODO: consider using TimeUtils to control timestamps in stats

}
