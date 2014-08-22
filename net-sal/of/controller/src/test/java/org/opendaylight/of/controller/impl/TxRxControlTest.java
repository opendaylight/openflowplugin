/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.controller.*;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.MessageType;
import org.opendaylight.of.lib.msg.OfmMutableMultipartRequest;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.of.mockswitch.MockSwitchBank;
import org.opendaylight.util.junit.SlowTests;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.opendaylight.of.controller.OpenflowEventType.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the {@link TxRxControl} implementation in the
 * {@link OpenflowController}.
 *
 * @author Simon Hunt
 */
@Category(SlowTests.class)
public class TxRxControlTest extends AbstractControllerTest {

    private static final long REC_TIME = 2000L; // 2000 milliseconds

    private OpenflowController ctlr;
    private TestSink sink;
    private AlertSink as = new AlertLogger();

    private void initController() {
        print("... init controller ...");
        sink = new TestSink();
        ctlr = new TestController(ControllerConfig.DEF_PORT,
                new PortStateTracker(lmgr), sink, as);
        initTxRxControl(ctlr);

        assertEquals(AM_NEQ, 0, txrx.size());
        assertTrue(AM_HUH, txrx.isEmpty());
        assertFalse(AM_HUH, txrx.isRecording());
        assertFalse(AM_HUH, txrx.isTriggerWaiting());
        // suppress the SET_CONFIG
        ctlr.suppressSetConfig(true);
        // fire up the controller
        ctlr.initAndStartListening();
        print(ctlr);
    }


    private static final String FMT_Q_FIN = EOL + "Finished with Queue: {}";
    private static final String FMT_Q_STOP = "Queue stopped: {}";

    private void stopRecording() {
        print("... stop recording ...");
        print(FMT_Q_FIN, txrx);
        txrx.stopRecording();
        print(FMT_Q_STOP, txrx);
    }

    @Before
    public void setUp() {
        MessageFactory.getTestSupport().reset(MessageFactory.TestReset.XID);
    }

    @After
    public void tearDown() {
        // shutdown the controller
        print("... controller shutdown ...");
        ctlr.shutdown();
    }

    @Test
    public void basic() {
        beginTest("basic");
        initController();
        endTest();
    }

    @Test
    public void basic2() {
        beginTest("basic2");
        initController();
        endTest();
    }

    @Test
    public void simpleOneShot() {
        beginTest("simpleOneShot");
        initController();
        printDebugTxRx();
        print("... start recording ...");
        txrxc.startRecording(100L);
        assertTrue(AM_HUH, txrx.isRecording());
        assertEquals(AM_UXS, 1, txrx.size()); // START checkpoint

        print("... delay for 50 ms ...");
        delay(50);
        printDebugTxRx();
        assertTrue(AM_HUH, txrx.isRecording());
        assertEquals(AM_UXS, 1, txrx.size()); // START checkpoint

        print("... delay for 60 ms ...");
        delay(60);
        printDebugTxRx();
        assertFalse(AM_HUH, txrx.isRecording());
        assertEquals(AM_UXS, 2, txrx.size()); // START & STOP checkpoints

        stopRecording(); // idempotent (if you don't know, look it up!) :)
        assertEquals(AM_UXS, 2, txrx.size()); // START & STOP checkpoints

        // final look at the queue
        printDebugTxRx();
        endTest();
    }


    //=====

    private List<MessageEvent> removeMessagesFromQueue() {
        List<MessageEvent> messages = new ArrayList<MessageEvent>();
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

    private static final MessageType[] HANDSHAKE_SEQUENCE_13 = {
            null,               // RX (initial connect)
            HELLO,              // TX
            HELLO,              // RX
            FEATURES_REQUEST,   // TX
            FEATURES_REPLY,     // RX (basic handshake done)
            MULTIPART_REQUEST,  // TX MP/DESC
            MULTIPART_REQUEST,  // TX MP/PORT_DESC
            MULTIPART_REQUEST,  // TX MP/TABLE_FEATURES
            MULTIPART_REPLY,    // RX MP/DESC
            MULTIPART_REPLY,    // RX MP/PORT_DESC
            MULTIPART_REPLY,    // RX MP/TABLE_FEATURES
    };
    private static final int SEQ_DONE_13 = HANDSHAKE_SEQUENCE_13.length;

    private static final MessageType[] HANDSHAKE_SEQUENCE_10 = {
            null,               // RX (initial connect)
            HELLO,              // TX
            HELLO,              // RX
            FEATURES_REQUEST,   // TX
            FEATURES_REPLY,     // RX (basic handshake done)
            MULTIPART_REQUEST,  // TX MP/DESC
            MULTIPART_REPLY,    // RX MP/DESC
    };
    private static final int SEQ_DONE_10 = HANDSHAKE_SEQUENCE_10.length;

    private static final int IDX_H_REQ = 1;
    private static final int IDX_H_REPLY = 2;
    private static final int IDX_F_REQ = 3;
    private static final int IDX_F_REPLY = 4;
    
    private static final int IDX_13_MP_REQ_DESC = 5;
    private static final int IDX_13_MP_REQ_PORT_DESC = 6;
    private static final int IDX_13_MP_REQ_TABLE_FEATURES = 7;
    private static final int IDX_13_MP_REP_DESC = 8;
    private static final int IDX_13_MP_REP_PORT_DESC = 9;
    private static final int IDX_13_MP_REP_TABLE_FEATURES = 10;
    
    private static final boolean ASSERT_ERROR_13_SEQ = 
            SEQ_DONE_13 != IDX_13_MP_REP_TABLE_FEATURES + 1;
    
    private static final int IDX_10_MP_REQ_DESC = 5;
    private static final int IDX_10_MP_REP_DESC = 6;

    private static final boolean ASSERT_ERROR_10_SEQ =
            SEQ_DONE_10 != IDX_10_MP_REP_DESC + 1;
    
    private static final boolean ASSERT_ERROR = 
            ASSERT_ERROR_10_SEQ || ASSERT_ERROR_13_SEQ;
    
    // # expected handshake messages for 1.0 and 1.3 switches 
    private static final int HS_MSGS_10 = SEQ_DONE_10;
    private static final int HS_MSGS_13 = SEQ_DONE_13;


    private void verifyHandshakes13(List<MessageEvent> msgEvents,
                                  DataPathId... dpids) {
        verifyHandshakes(msgEvents, V_1_3, dpids);
    }
    
    private void verifyHandshakes10(List<MessageEvent> msgEvents,
                                  DataPathId... dpids) {
        verifyHandshakes(msgEvents, V_1_0, dpids);
    }
    
    private void verifyHandshakes(List<MessageEvent> msgEvents,
                                  ProtocolVersion pv, 
                                  DataPathId... dpids) {
        /* expect (x N):
         *   <connect>, HELLO, HELLO, FEATURES_REQUEST, FEATURES_REPLY,
         *   MP-Req/DESC, [MP-Req/PORT_DESC, MP-Req/TABLE_FEATURES,]
         *   MP-Reply/DESC, [MP-Reply/PORT_DESC, MP-Reply/TABLE_FEATURES]
         *   
         * But note that we cannot assert the exact order, since the 
         * "conversations" are interleaved indeterminately. However, per
         * switch, the order should be as defined above...
         * .. note that 1.0 handshake does not include PORT_DESC or TAB_FEAT ..
         */
        final int n = dpids.length;
        final int hs = pv == V_1_3 ? HS_MSGS_13 : HS_MSGS_10; 
        final int seqDone = pv == V_1_3 ? SEQ_DONE_13 : SEQ_DONE_10;
        final MessageType[] hsSequence = pv == V_1_3 ? HANDSHAKE_SEQUENCE_13 
                                                     : HANDSHAKE_SEQUENCE_10;
        
        assertEquals(AM_NEQ, n*hs, msgEvents.size());
        print("... verifying handshake{} for {} switch{} ...",
                n==1 ? "" : "s", n, n == 1 ? "" : "es");

        Map<DataPathId, Integer> lookup = new HashMap<>(n);
        int i = 0;
        for (DataPathId d: dpids)
            lookup.put(d, i++);
        int[] msgSeq = new int[n];
        long[] expXid = new long[n];
        for (MessageEvent me: msgEvents) {
            DataPathId dpid = me.dpid();
            Integer dpidIdx = lookup.get(dpid);
            assertNotNull("Unexpected DPID: " + dpid, dpidIdx);
            int seq = msgSeq[dpidIdx];
            if (seq == seqDone)
                fail("Too many messages for " + me.dpid());
            MessageType expType = hsSequence[seq];
            MessageType actType = me.msg() == null ? null : me.msg().getType();
            assertEquals(AM_NEQ, expType, actType);
            if (seq == IDX_H_REQ || seq == IDX_F_REQ)
                expXid[dpidIdx] = me.msg().getXid();
            if (seq == IDX_H_REPLY || seq == IDX_F_REPLY)
                assertEquals(AM_VMM, expXid[dpidIdx], me.msg().getXid());
            // TODO: add more thorough assertions about the MP/Request/Replies
            msgSeq[dpidIdx]++;
        }
    }


    private void verifyMessage(MessageEvent me, OpenflowEventType etype,
                               DataPathId dpid, MessageType mt, long xid) {
        print(me);
        OpenflowMessage msg = me.msg();
        MessageType actMt = msg == null ? null : msg.getType();
        long actXid = msg == null ? 0 : msg.getXid();
        assertEquals(AM_VMM, etype, me.type());
        assertEquals(AM_NEQ, dpid, me.dpid());
        assertEquals(AM_NEQ, mt, actMt);
        assertEquals(AM_NEQ, xid, actXid);
    }

    //=====

    private static final String TO_STR[] = {
      "<HandshakeMessageEvt> DATAPATH_CONNECTED,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=null}",
      "<HandshakeMessageEvt> MESSAGE_RX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,HELLO,len=16,xid=0]}",
      "<HandshakeMessageEvt> MESSAGE_TX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,HELLO,len=16,xid=0]}",
      "<HandshakeMessageEvt> MESSAGE_TX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,FEATURES_REQUEST,len=8,xid=103]}",
      "<HandshakeMessageEvt> MESSAGE_RX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,FEATURES_REPLY,len=32,xid=103]}",
      "<MessageEvt> MESSAGE_TX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,MULTIPART_REQUEST/DESC,len=16,xid=104]}",
      "<MessageEvt> MESSAGE_TX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,MULTIPART_REQUEST/PORT_DESC,len=16,xid=105]}",
      "<MessageEvt> MESSAGE_TX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,MULTIPART_REQUEST/TABLE_FEATURES,len=16,xid=106]}",
      "<MessageEvt> MESSAGE_RX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,MULTIPART_REPLY/DESC,len=1072,xid=104]}",
      "<MessageEvt> MESSAGE_RX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,MULTIPART_REPLY/PORT_DESC,len=2064,xid=105]}",
      "<MessageEvt> MESSAGE_RX,pv=V_1_3," +
              "dpid=00:2a:00:16:b9:06:80:00,aux=0," +
              "msg=[V_1_3,MULTIPART_REPLY/TABLE_FEATURES,len=16,xid=106]}",
    };

    /** Utility method to strip out checkpoint events.
     *
     * @param messages the message event list
     * @return the events sans checkpoints
     */
    private List<MessageEvent> stripCheckpoints(List<MessageEvent> messages) {
        List<MessageEvent> msgs = new ArrayList<MessageEvent>();
        for (MessageEvent m: messages)
            if (!CheckpointEvent.class.isInstance(m))
                msgs.add(m);
        return msgs;
    }

    @Test
    public void recordSwitchHandshake() {
        beginTest("recordSwitchHandshake");
        initController();
        txrxc.startRecording(REC_TIME);
        assertTrue(AM_HUH, txrx.isRecording());
        printDebugTxRx();

        switchesReady = new CountDownLatch(1); // expecting just one switch
        sink.setDataPathAddedLatch(switchesReady);
        createAndActivateSwitch(SW13P32);
        waitForHandshake();
        assertEquals(AM_UXS, 1, ctlr.infoCacheSize());
        stopRecording();

        printDebugTxRx();
        // We expect
        // CKPT: <recording started>
        //   RX: <connect>
        //   TX: HELLO
        //   RX: HELLO
        //   TX: FEATURES_REQUEST
        //   RX: FEATURES_REPLY
        //   TX: MP-Request/DESC
        //   TX: MP-Request/PORT_DESC
        //   TX: MP-Request/TABLE_FEATURES
        //   RX: MP-Reply/DESC
        //   RX: MP-Reply/PORT_DESC
        //   RX: MP-Reply/TABLE_FEATURES
        // CKPT: <full handshake complete>
        // CKPT: <recording stopped>

        assertEquals(AM_NEQ, 14, txrx.size());
        List<MessageEvent> messages =
                stripCheckpoints(removeMessagesFromQueue());
        verifyHandshakes13(messages, SW13P32_DPID);
        for (int i = 0; i<HS_MSGS_13; i++)
            assertTrue("bad toString()", messages.get(i).toString()
                    .contains(TO_STR[i]));
        endTest();
    }

    @Test
    public void recordDoubleSwitch() throws OpenflowException {
        beginTest("recordDoubleSwitch");
        initController();
        sink.setNonStrict().replay();
        txrxc.startRecording(REC_TIME);
        printDebugTxRx();
        final int nSwitches = 2;

        switchesReady = new CountDownLatch(nSwitches);
        sink.setDataPathAddedLatch(switchesReady);
        createAndActivateSwitch(SW13P32);
        createAndActivateSwitch(SW10P4);
        waitForHandshake();
        assertEquals(AM_UXS, nSwitches, ctlr.infoCacheSize());

        printDebugTxRx();
        assertEquals(AM_NEQ, 2 + HS_MSGS_10 + 1 + HS_MSGS_13, txrx.size());
        removeMessagesFromQueue();

        /* Keeping track of XID's:
         *  101 for switch A's FEATURE_REQUEST/REPLY
         *  102 for switch B's FEATURE_REQUEST/REPLY
         */

        // Let's send an MP/DESC request to ONE of the switches:
        messagesReceived = new CountDownLatch(1);
        sink.setMsgRxLatch(messagesReceived);

        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(MultipartType.DESC);
        ctlr.send(req.toImmutable(), SW13P32_DPID);
        long expXid = req.getXid();

        waitForMessages();
        printDebugTxRx();
        assertEquals(AM_NEQ, 2, txrx.size());
        List<MessageEvent> messages = removeMessagesFromQueue();
        print(EOL + "Verifying messages:");
        Iterator<MessageEvent> iter = messages.iterator();
        verifyMessage(iter.next(), MESSAGE_TX, SW13P32_DPID, MULTIPART_REQUEST, expXid);
        verifyMessage(iter.next(), MESSAGE_RX, SW13P32_DPID, MULTIPART_REPLY, expXid);

        stopRecording();
        assertEquals(AM_NEQ, 1, txrx.size()); // Recording Stopped
        printDebugTxRx();
        endTest();
    }

    @Test
    public void takeABreakFromRecording() {
        beginTest("takeABreakFromRecording");
        initController();
        sink.setNonStrict().replay();
        txrxc.startRecording(60L);
        printDebugTxRx();
        final int nSwitches = 2;

        switchesReady = new CountDownLatch(nSwitches);
        sink.setDataPathAddedLatch(switchesReady);
        createAndActivateSwitch(SW13P32);

        // let recording session time out
        print("... twiddling thumbs for 60ms ...");
        delay(90);
        createAndActivateSwitch(SW10P4);

        waitForHandshake();
        print(ctlr.toDebugString());
        assertEquals(AM_UXS, nSwitches, ctlr.infoCacheSize());

        printDebugTxRx();
        // NOTE: connection of 1.0 switch not recorded...
        assertEquals(AM_NEQ, HS_MSGS_13 + 3, txrx.size());
        List<MessageEvent> messages =
                stripCheckpoints(removeMessagesFromQueue());
        verifyHandshakes13(messages, SW13P32_DPID);
        endTest();
    }

    private static final String BANK_SEVEN =
            "org/opendaylight/of/controller/impl/switchBankSeven.def";

    @Test
    public void aSwathOfSwitches() {
        beginTest("aSwathOfSwitches");
        initController();
        sink.setNonStrict().replay();
        txrxc.startRecording(REC_TIME);
        printDebugTxRx();
        MockSwitchBank bank = new MockSwitchBank(BANK_SEVEN, showOutput());
        final int hsMsgCount = (HS_MSGS_10+1)*4 + (HS_MSGS_13+1)*3 + 2;
        // see BANK_SEVEN def file for switch count of each version
        
        int nSwitches = bank.size();
        switchesReady = new CountDownLatch(bank.expectedToCompleteHandshake());
        sink.setDataPathAddedLatch(switchesReady);
        print("... activiating bank of {} switches ...", nSwitches);
        bank.activate();
        waitForHandshake();
        assertEquals(AM_UXS, nSwitches, ctlr.infoCacheSize());
        stopRecording();

        printDebugTxRx();
        assertEquals(AM_NEQ, hsMsgCount, txrx.size());
        endTest();
    }

    @Test
    public void upAndDown() {
        beginTest("upAndDown");
        initController();
        sink.setNonStrict().replay();
        txrxc.startRecording(REC_TIME);
        printDebugTxRx();
        final DataPathId dpid = SW10P4_DPID;
        switchesReady = new CountDownLatch(1);
        sink.setDataPathAddedLatch(switchesReady);
        switchesGone = new CountDownLatch(1);
        sink.setDataPathRemovedLatch(switchesGone);
        print("... activating one switch ...");
        BasicSwitch sw = createAndActivateSwitch(SW10P4);
        waitForHandshake();
        delay(52);  // forced wait of 52ms
        sw.deactivate();
        waitForDisconnect();
        stopRecording();
        printDebugTxRx();
        List<MessageEvent> messages = removeMessagesFromQueue();
        assertEquals(AM_UXS, HS_MSGS_10 + 4, messages.size());
        // extra 3 for rec-start, rec-stop, dp-disconnected
        
        MessageEvent disc = messages.get(messages.size()-2); // second to last
        assertEquals(AM_NEQ, DATAPATH_DISCONNECTED, disc.type());
        assertEquals(AM_NEQ, dpid, disc.dpid());
        endTest();
    }
}
