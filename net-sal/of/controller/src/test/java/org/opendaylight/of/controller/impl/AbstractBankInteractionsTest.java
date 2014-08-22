/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.opendaylight.of.controller.AlertSink;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.mockswitch.MockSwitchBank;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Base test class for unit tests that want to use the MockSwitchBank and
 * scenario files for testing controller/switch interactions.
 *
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public abstract class AbstractBankInteractionsTest
        extends AbstractControllerTest {

    protected MockSwitchBank bank;
    protected TestSink sink;
    protected AlertSink as = new AlertLogger();
    protected OpenflowController ctlr;
    protected TestController testCtlr;


    @After
    public void teardown() {
        // regardless of how the test ended... want to shutdown the controller
        if (ctlr != null) {
            print(".... controller shutdown ....");
            if (txrx != null)
                printDebugTxRx();
            ctlr.shutdown();
            ctlr = null;
        }
    }

    // === Helper methods to ease writing alternate scenarios...

    protected void initBank(String bankDefn) {
        // SETUP - bank of mock switches
        print(".... initializing switch-bank ....");
        bank = new MockSwitchBank(bankDefn, showOutput());
    }

    protected void initController() {
        initController(false);
    }

    /** Initializes the controller, making the assumption that we will wait
     * for all switches (bank.size()) to activate before proceeding.
     *
     * @param debugOut enables debug output
     */
    protected void initController(boolean debugOut) {
     // SETUP - controller
        print(".... initializing controller ....");
        switchesReady = new CountDownLatch(bank.expectedToCompleteHandshake());
        sink.setDataPathAddedLatch(switchesReady);
        testCtlr = new TestController(ControllerConfig.DEF_PORT,
                new PortStateTracker(lmgr), sink, as);
        ctlr = testCtlr;
        initTxRxControl(ctlr);
        if (debugOut)
            print(ctlr.toDebugString());
        assertEquals(AM_UXS, 0, ctlr.infoCacheSize());
        // fire her up
        OpenflowController.enableIdleDetection(false);
        ctlr.initAndStartListening();

    }

    protected void setInternalMsgLatchCount(DataPathId dpid, int msgCount) {
        internalMsgLatch = new CountDownLatch(msgCount);
        bank.setInternalMsgLatch(dpid, internalMsgLatch);
    }

    protected void cleanup() {
        // CLEANUP - make sure no assertions were missed
        print(".... finalizing assertions ....");
        sink.endReplay();
        bank.endScenario();
    }

}
