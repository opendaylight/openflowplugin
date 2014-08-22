/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.mockswitch.MockOpenflowSwitch;
import org.opendaylight.util.api.NotFoundException;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.opendaylight.of.mockswitch.MockOpenflowSwitch.HelloMode.NOT_10_RETURN_OFM_ERROR;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for HELLO interactions between switch and controller, to hit
 * some corner cases of less than conformant switches.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class HelloInteractionsTest extends AbstractControllerTest {

    private static final String DEF_EAGER = IMPL_ROOT + "eager10.def";
    private static final String DEF_LAZY = IMPL_ROOT + "lazy10.def";
    private static final DataPathId DPID = dpid("42/0016b9:006502");


    // TODO: test grace period (mockswitch doesn't respond - controller sends hello)
    //  (implies Timer task)
    // TODO
    /*
    (1) is the inbound HELLO 1.0 ?
       - YES: ok
       - NO:   (A) throw OFPT_ERROR (checked version and wasn't 1.0)
               (B) do not respond (expected 8 bytes, but got 16)
    --------
    (2) if inbound HELLO happened before I sent my outbound (I'm eager?)
    */

    // TODO: test 1.1, 1.2, 1.3 switches also
    // TODO: make sure XID is copied (controller responding to mockswitch HELLO)


    @After
    public void tearDown() {
        cmgr.shutdown();
    }


    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor(DPID);
        eds = new MockEventDispatcher();

        cmgr = new TestControllerManager(DEFAULT_CTRL_CFG, alertSink,
                roleAdvisor, eds);
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();
        print("... controller activated ...");
    }

    private BasicSwitch connectSwitch(DataPathId dpid, String def,
                                      MockOpenflowSwitch.HelloMode mode) {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        BasicSwitch sw = null;
        try {
            sw = new BasicSwitch(dpid, def);
            sw.setHelloMode(mode);
            sw.activate();
            print("... hello switch activated : {} ...", sw.getDpid());
            waitForHandshake();
        } catch (Exception e) {
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

    // =======

    private BasicSwitch sw;

    @Test
    public void simulatedSwitchRejects13Hello() throws OpenflowException {
        beginTest("simulatedSwitchRejects13Hello");
        initController();

        sw = connectSwitch(DPID, DEF_LAZY, NOT_10_RETURN_OFM_ERROR);

        try {
            cs.getDataPathInfo(DPID);
            fail(AM_NOEX);
        } catch (NotFoundException e) {
            print(FMT_EX, e);
        }

        // NOTE: nothing to disconnect, since the switch got revoked.
        endTest();
    }


    @Test @Ignore("Not sure why this fails from the command line :(")
    public void simulatedSwitchSends10Hello() throws OpenflowException {
        beginTest("simulatedSwitchSends10Hello");
        initController();

        sw = connectSwitch(DPID, DEF_EAGER, NOT_10_RETURN_OFM_ERROR);
        DataPathInfo dpi = cs.getDataPathInfo(DPID);
        print(dpi.toString());
        assertNotNull(dpi);

        delay(300);

        disconnectSwitch(sw);
        endTest();
    }


}
