/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.msg.MessageLibTestUtils;
import org.opendaylight.util.api.NotFoundException;
import org.opendaylight.util.junit.TestLogger;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test to validate the handling of receiving a duplicated DPIDs
 * reported by more than one switch.
 *
 *
 * @author Frank Wood
 * @author Simon Hunt
 */
public class DupDpidInteractionsTest extends AbstractControllerTest {

    private static final TestLogger tlog = new TestLogger();

    // ======================================================================
    // utility class to give us access to the message parser logger
    private static class MsgLibTestUtils extends MessageLibTestUtils {
        public void setTestLogger() {
            setMessageParserLogger(tlog);
        }
        public void restoreLogger() {
            restoreMessageParserLogger();
        }
    }

    // ======================================================================

    private static MsgLibTestUtils msgLibTestUtils;

    @BeforeClass
    public static void classSetUp() {
        setUpLogger();
        msgLibTestUtils = new MsgLibTestUtils();
        msgLibTestUtils.setTestLogger();
        ListenerManager.setLogger(tlog);
    }

    @AfterClass
    public static void classTearDown() {
        msgLibTestUtils.restoreLogger();
        ListenerManager.restoreLogger();
    }

    @After
    public void tearDown() {
        cmgr.shutdown();
    }

    // ======================================================================
    // === HELPER methods

    private static final String DEF = SW13P8;
    private static final DataPathId DPID = dpid("1/bad000:f00d00");

    private void initController() {
        OpenflowController.enableIdleDetection(false);

        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor(DPID);
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

    private BasicSwitch connectSwitch() {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        BasicSwitch sw = null;
        try {
            sw = new BasicSwitch(DPID, DEF);
            sw.activate();
            print("... mock switch activated : {} ...", sw.getDpid());
            waitForHandshake();
        } catch (IOException e) {
            print(e);
            fail(AM_UNEX);
        }
        return sw;
    }

    private void disconnectSwitch(BasicSwitch sw) {
        try {
            lmgr.getDataPathInfo(DPID);
            switchesGone = new CountDownLatch(1);
            lmgr.setDataPathRemovedLatch(switchesGone);
            sw.deactivate();
            waitForDisconnect();
        } catch (NotFoundException e) {
            // already gone
        }
    }

    private void pause() {
        delay(200);
    }

    @Test
    public void duplicatedDpid() {
        beginTest("duplicatedDpid");
        initController();
        startRecording(10);

        BasicSwitch sw = connectSwitch();
        pause();
        assertOneDatapath();
        tlog.assertInfoContains("Datapath added: 00:01:ba:d0:00:f0:0d:00");
        tlog.assertWarning(null);

        connectSwitch();
        pause();
        tlog.assertInfo(null);
        tlog.assertWarningContains("Datapath REVOKED: 00:01:ba:d0:00:f0:0d:00");
        assertOneDatapath();

        disconnectSwitch(sw);
        pause();
        tlog.assertInfoContains("Datapath removed: 00:01:ba:d0:00:f0:0d:00");
        stopRecordingAndPrintDebugTrace();
        endTest();
    }

    private void assertOneDatapath() {
        Set<DataPathInfo> dpis = lmgr.getAllDataPathInfo();
        assertEquals(AM_UXS, 1, dpis.size());
        DataPathInfo dpi = dpis.iterator().next();
        assertEquals(AM_NEQ, DPID, dpi.dpid());
    }

}
