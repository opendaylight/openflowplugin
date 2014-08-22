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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.controller.DataPathEvent;
import org.opendaylight.of.controller.DataPathListener;
import org.opendaylight.of.controller.QueueEvent;
import org.opendaylight.of.mockswitch.MockOpenflowSwitch;
import org.opendaylight.util.junit.SlowTests;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the {@link DataPathListener} portion of the
 * {@link ListenerManager}.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
@Category(SlowTests.class)
public class DataPathListenerTest extends AbstractControllerTest {

    //========= Test Datapath Listener
    private static class TestDpListener implements DataPathListener {
        int eventCount = 0;
        private CountDownLatch eventProcLatch;
        private final String name;
        private final int tardiness;

        TestDpListener(String name) {
            this.name = name;
            tardiness = 0;
        }

        void setEventProcLatch(CountDownLatch latch) {
            eventProcLatch = latch;
        }

        private String ts() {
            return TIME.hhmmssnnn(TIME.currentTimeMillis());
        }

        @Override
        public void queueEvent(QueueEvent event) {
            print(FMT_EVENT, ts(), name, event);
        }

        @Override
        public void event(DataPathEvent event) {
            delay(tardiness);
            print(FMT_EVENT, ts(), name, event);
            eventCount++;
            if (eventProcLatch != null)
                eventProcLatch.countDown();
        }
    }

    //========  Faulty Datapath Listener that breaks its event() callback
    //          every 3rd time
    private static class FaultyDpListener extends TestDpListener {
        private int fault = 0;

        FaultyDpListener(String name) {
            super(name);
        }

        @Override
        public void event(DataPathEvent event) {
            if (++fault % 3 == 0)
                throw new RuntimeException("(expected) Fault Occurred");
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


    private MockOpenflowSwitch initSingleSwitch() {
        return initSingleSwitch(SW10P4);
    }

    private MockOpenflowSwitch initSingleSwitch(String path) {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        BasicSwitch sw = createAndActivateSwitch(path);
        waitForHandshake();
        print(lmgr.toDebugString());
        return sw;
    }

    private void shutdownSwitch(MockOpenflowSwitch sw) {
        switchesGone = new CountDownLatch(1);
        lmgr.setDataPathRemovedLatch(switchesGone);
        sw.deactivate();
        waitForDisconnect();
        print(lmgr.toDebugString());
    }

    @Test
    public void basic() {
        beginTest("basic");
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 0, lmgr.msgListenerCount());

        DataPathListener dpl = new TestDpListener("Tester");
        TestDpListener tdpl = (TestDpListener) dpl;
        ls.addDataPathListener(dpl);
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 1, lmgr.dpListenerCount());
        assertEquals(AM_UXCC, 0, tdpl.eventCount);

        // let's add a couple of switches and shut them down
        //  expecting four events (connected, disconnected x 2)
        listenersProcessed = new CountDownLatch(4);
        tdpl.setEventProcLatch(listenersProcessed);
        print("event latch set to 4...");

        MockOpenflowSwitch sw1 = initSingleSwitch(SW10P12);
        MockOpenflowSwitch sw2 = initSingleSwitch(SW13P32);
        delay(200);
        shutdownSwitch(sw1);
        shutdownSwitch(sw2);
        print("wait for 4 events to complete...");
        waitForListeners();
        assertEquals(AM_UXCC, 4, tdpl.eventCount);

        endTest();
    }

    @Test
    public void faulty() {
        beginTest("faulty");
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 0, lmgr.dpListenerCount());

        DataPathListener dpl = new FaultyDpListener("McNaulty");
        FaultyDpListener fdpl = (FaultyDpListener) dpl;
        ls.addDataPathListener(dpl);
        print(lmgr.toDebugString());
        assertEquals(AM_UXS, 1, lmgr.dpListenerCount());
        assertEquals(AM_UXCC, 0, fdpl.eventCount);

        MockOpenflowSwitch sw;
        for (int i=0; i<3; i++) {
            sw = initSingleSwitch();
            delay(20);
            shutdownSwitch(sw);
        }
        // NOTE: If our implementation was working properly, the expected
        //       event count would be 6, but since we broke on the 3rd and 6th
        //       call, before updating our event counter, the result is 4.
        assertEquals(AM_UXCC, 4, fdpl.eventCount);

        endTest();
    }

}
