/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
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
import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.MessageListener;
import org.opendaylight.of.controller.QueueEvent;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OfmMutableMultipartRequest;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.DESC;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the {@link MessageListener} portion of the
 * {@link ListenerManager}.
 *
 * @author Simon Hunt
 */
public class MessageListenerTest extends AbstractControllerTest {

    //======= Test Message Listener
    private static class TestMsgListener implements MessageListener {
        int msgCount = 0;
        private CountDownLatch msgProcLatch;
        private CountDownLatch qProcLatch;
        private final String name;

        TestMsgListener(String name) {
            this.name = name;
        }

        void setMsgProcLatch(CountDownLatch latch) {
            msgProcLatch = latch;
        }

        private String ts() {
            return TIME.hhmmssnnn(TIME.currentTimeMillis());
        }

        @Override
        public void queueEvent(QueueEvent event) {
            print(FMT_EVENT, ts(), name, event);
            if (qProcLatch != null)
                qProcLatch.countDown();
        }

        @Override
        public void event(MessageEvent event) {
            print(FMT_EVENT, ts(), name, event);
            msgCount++;
            if (msgProcLatch != null)
                msgProcLatch.countDown();
        }
    }

    //===== Tardy Message Listener
    private static class TardyMsgListener extends TestMsgListener {
        private final int procrastinateMs;

        TardyMsgListener(String name, int tardiness) {
            super(name);
            procrastinateMs = tardiness;
        }

        @Override
        public void event(MessageEvent event) {
            delay(procrastinateMs);
            super.event(event);
        }
    }

    //===============

    @BeforeClass
    public static void classSetUp() {
        setUpLogger();
    }

    @Before
    public void setUp() {
        lmgr = new ListenerManager(DEFAULT_CTRL_CFG, new AlertLogger(),
                                   PH_SINK, PH_CB, FM_ADV, new MockRoleAdvisor());
        ls = lmgr;
        lmgr.startIOProcessing();
        initTxRxControl(lmgr);
    }

    @After
    public void tearDown() {
        if (lmgr != null)
            lmgr.shutdown();
    }


    private void sendDpDesc() throws OpenflowException {
        print("... sending an MP/DESC request ...");
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(DESC);
        ls.send(req.toImmutable(), SW13P32_DPID);
    }

    private void initSingleSwitch() {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        createAndActivateSwitch(SW13P32);
        waitForHandshake();
        print(lmgr.toDebugString());
    }

    @Test
    public void basic() throws OpenflowException {
        beginTest("basic");
        print(lmgr.toDebugString());
        initSingleSwitch();
        assertEquals(AM_UXS, 0, lmgr.msgListenerCount());

        MessageListener ml = new TestMsgListener("Tester");
        TestMsgListener tml = (TestMsgListener) ml;
        ls.addMessageListener(ml, null);
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 1, lmgr.msgListenerCount());

        assertEquals(AM_UXCC, 0, tml.msgCount);

        // let's send an MP/DESC request to the switch
        messagesReceived = new CountDownLatch(1);
        lmgr.setMsgRxLatch(messagesReceived);
        listenersProcessed = new CountDownLatch(1);
        tml.setMsgProcLatch(listenersProcessed);

        sendDpDesc();
        waitForMessages();
        waitForListeners();
        assertEquals(AM_UXCC, 1, tml.msgCount);

        endTest();
    }

    @Test
    public void addAndRemove() throws OpenflowException {
        beginTest("addAndRemove");
        print(lmgr.toDebugString());
        initSingleSwitch();

        TestMsgListener one = new TestMsgListener("One");
        TestMsgListener two = new TestMsgListener("Two");
        txrxc.startRecording(500L);

        // start with no listeners
        sectionHeader("no listeners");
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 0, lmgr.msgListenerCount());
        messagesReceived = new CountDownLatch(1);
        lmgr.setMsgRxLatch(messagesReceived);
        sendDpDesc();
        waitForMessages();

        sectionHeader("one listener");
        ls.addMessageListener(one, null);
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 1, lmgr.msgListenerCount());
        messagesReceived = new CountDownLatch(1);
        lmgr.setMsgRxLatch(messagesReceived);
        listenersProcessed = new CountDownLatch(1);
        one.setMsgProcLatch(listenersProcessed);
        sendDpDesc();
        waitForMessages();
        waitForListeners();

        sectionHeader("two listeners");
        ls.addMessageListener(two, null);
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 2, lmgr.msgListenerCount());
        messagesReceived = new CountDownLatch(1);
        lmgr.setMsgRxLatch(messagesReceived);
        listenersProcessed = new CountDownLatch(2);
        one.setMsgProcLatch(listenersProcessed);
        two.setMsgProcLatch(listenersProcessed);
        sendDpDesc();
        waitForMessages();
        waitForListeners();

        sectionHeader("back to one listener");
        ls.removeMessageListener(two);
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 1, lmgr.msgListenerCount());
        messagesReceived = new CountDownLatch(1);
        lmgr.setMsgRxLatch(messagesReceived);
        listenersProcessed = new CountDownLatch(1);
        one.setMsgProcLatch(listenersProcessed);
        sendDpDesc();
        waitForMessages();
        waitForListeners();

        sectionHeader("back to no listeners");
        ls.removeMessageListener(one);
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 0, lmgr.msgListenerCount());
        messagesReceived = new CountDownLatch(1);
        lmgr.setMsgRxLatch(messagesReceived);
        sendDpDesc();
        waitForMessages();

        // let's see when the messages REALLY came in...
        printDebugTxRx();
        txrxc.stopRecording();
        endTest();
    }

    @Test
    public void tardyListeners() throws OpenflowException {
        beginTest("tardyListeners");
        print(lmgr.toDebugString());
        initSingleSwitch();
        assertEquals(AM_UXS, 0, lmgr.msgListenerCount());

        TestMsgListener one = new TestMsgListener("Speedy");
        ls.addMessageListener(one, null);
        TardyMsgListener two = new TardyMsgListener("Snail", 43);
        ls.addMessageListener(two, null);
        TardyMsgListener three = new TardyMsgListener("SlowPoke", 17);
        ls.addMessageListener(three, null);
        print(lmgr.toDebugString());

        assertEquals(AM_UXS, 3, lmgr.msgListenerCount());
        assertEquals(AM_UXCC, 0, one.msgCount);
        assertEquals(AM_UXCC, 0, two.msgCount);
        assertEquals(AM_UXCC, 0, three.msgCount);

        // let's record the following message exchange
        txrxc.startRecording(300L);

        // let's fire off 4 MP/DESC requests to the switch
        messagesReceived = new CountDownLatch(4);
        lmgr.setMsgRxLatch(messagesReceived);
        listenersProcessed = new CountDownLatch(12);
        one.setMsgProcLatch(listenersProcessed);
        two.setMsgProcLatch(listenersProcessed);
        three.setMsgProcLatch(listenersProcessed);

        for (int i=0; i<4; i++)
            sendDpDesc();

        waitForMessages();
        waitForListeners();
        assertEquals(AM_UXCC, 4, one.msgCount);
        assertEquals(AM_UXCC, 4, two.msgCount);
        assertEquals(AM_UXCC, 4, three.msgCount);

        // let's see when the messages REALLY came in...
        printDebugTxRx();

        endTest();
    }

}
