/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.controller.RoleAdvisor;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.mp.MBodyPortStats;
import org.opendaylight.of.lib.msg.MessageFuture;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.of.lib.msg.PortConfig;
import org.opendaylight.util.api.NotFoundException;
import org.opendaylight.util.junit.SlowTests;
import org.opendaylight.util.net.BigPortNumber;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.msg.PortConfig.NO_RECV;
import static org.opendaylight.of.lib.msg.PortConfig.PORT_DOWN;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests to verify the behavior of the PortStateTracker.
 * The behavior of the {@code PortStateTracker} is validated via access to the
 * port information maintained within the {@code DataPathInfo}.  The
 * DataPathInfo has a reference to the PortStateTracker, and will use this
 * component when queried about port data.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
@Category(SlowTests.class)
public class PortStateInteractionsTest extends AbstractBankInteractionsTest {

    private static final String E_FIND_PORT = "failed to find target port";

    private static final String BANK_DEFN =
            IMPL_ROOT + "switchBankPortStateTest.def";

    // switch dpid as defined in simple10sw4port.def
    private static final DataPathId DPID10 = SW10P4_DPID;
    private static final String DEF10 = SW10P4;

    // expected values
    private static final BigPortNumber PORT_2_NUM = bpn(2l);
    private static final BigPortNumber PORT_4_NUM = bpn(4l);

    private static final Set<PortConfig> EXP_PORT_DOWN_CONFIG =
            new TreeSet<>(Arrays.asList(PORT_DOWN, NO_RECV));

    private static final Set<PortConfig> EXP_PORT_UP_CONFIG =
            new TreeSet<>(Arrays.asList(NO_RECV));

    private static final RoleAdvisor MOCK_ROLE_ADVISOR = new MockRoleAdvisor();

    @BeforeClass
    public static void classSetUp() {
        Assume.assumeTrue(!isUnderCoverage());
        setUpLogger();
    }
    
    @After
    public void tearDown() {
        if (lmgr != null)
            lmgr.shutdown();
        lmgr = null;
    }

    private PortSwitch connectSwitch(DataPathId dpid, String def) {
        switchesReady = new CountDownLatch(1);
        lmgr.setDataPathAddedLatch(switchesReady);
        PortSwitch sw = null;
        try {
            sw = new PortSwitch(dpid, def);
            sw.activate();
            print(".. switch activated : {} ...", sw.getDpid());
            waitForHandshake();
        } catch (IOException e) {
            print(e);
            fail(AM_UNEX);
        }
        return sw;
    }

    private void disconnectSwitch(PortSwitch sw) {
        switchesGone = new CountDownLatch(1);
        lmgr.setDataPathRemovedLatch(switchesGone);
        sw.deactivate();
        waitForDisconnect();
    }


    private void validatePortConfig(List<Port> ports, BigPortNumber portNum,
                                    Set<PortConfig> expPortConfig) {
        Port foundPort = null;
        for (Port p : ports) {
            if (p.getPortNumber().equals(portNum)) {
                foundPort = p;
                break;
            }
        }

        if (foundPort == null)
            fail(E_FIND_PORT);

        assertEquals(AM_NEQ, expPortConfig, foundPort.getConfig());
    }

    @Test
    public void basic() {
        beginTest("basic");
        initBank(BANK_DEFN);

        switchesReady = new CountDownLatch(bank.expectedToCompleteHandshake());
        lmgr = new ListenerManager(DEFAULT_CTRL_CFG, null, PH_SINK, PH_CB,
                                   FM_ADV, MOCK_ROLE_ADVISOR);
        ls = lmgr;
        lmgr.startIOProcessing();
        lmgr.setDataPathAddedLatch(switchesReady);

        bank.activate();
        // BLOCK until the switch is ready, or the timer pops
        waitForHandshake();
        DataPathInfo dpi = ls.getDataPathInfo(DPID10);
        List<Port> ports = dpi.ports();
        assertEquals(AM_NEQ, 4, ports.size());
        print("validate port 2 is down");
        validatePortConfig(ports, PORT_2_NUM, EXP_PORT_DOWN_CONFIG);

        // first portStatus message (p4 down)
        messagesReceived = new CountDownLatch(1);
        lmgr.setMsgRxLatch(messagesReceived);
        bank.resume();
        waitForMessages();

        dpi = ls.getDataPathInfo(DPID10);
        ports = dpi.ports();
        assertEquals(AM_NEQ, 4, ports.size());
        print("validate that port 4 is down");
        validatePortConfig(ports, PORT_4_NUM, EXP_PORT_DOWN_CONFIG);

        // second portStatus message (p2 up)
        messagesReceived = new CountDownLatch(1);
        lmgr.setMsgRxLatch(messagesReceived);
        bank.resume();
        waitForMessages();

        dpi = ls.getDataPathInfo(DPID10);
        ports = dpi.ports();
        assertEquals(AM_NEQ, 4, ports.size());
        print("validate port 2 is up");
        validatePortConfig(ports, PORT_2_NUM, EXP_PORT_UP_CONFIG);


        // third portStatus message (p4 up)
        messagesReceived = new CountDownLatch(1);
        lmgr.setMsgRxLatch(messagesReceived);
        bank.resume();
        waitForMessages();

        dpi = ls.getDataPathInfo(DPID10);
        ports = dpi.ports();
        assertEquals(AM_NEQ, 4, ports.size());
        print("validate port 4 is up");
        validatePortConfig(ports, PORT_4_NUM, EXP_PORT_UP_CONFIG);
        endTest();
    }

    private void verifyPortStats(MBodyPortStats ps, int rxp, int txp,
                                 int rxb, int txb, int rxd, int txd,
                                 int rxe, int txe, int rxfe, int rxoe,
                                 int rxce, int coll, int durs, int durns) {
        print(ps.toDebugString());
        assertEquals(AM_NEQ, rxp, ps.getRxPackets());
        assertEquals(AM_NEQ, txp, ps.getTxPackets());
        assertEquals(AM_NEQ, rxb, ps.getRxBytes());
        assertEquals(AM_NEQ, txb, ps.getTxBytes());
        assertEquals(AM_NEQ, rxd, ps.getRxDropped());
        assertEquals(AM_NEQ, txd, ps.getTxDropped());
        assertEquals(AM_NEQ, rxe, ps.getRxErrors());
        assertEquals(AM_NEQ, txe, ps.getTxErrors());
        assertEquals(AM_NEQ, rxfe, ps.getRxFrameErr());
        assertEquals(AM_NEQ, rxoe, ps.getRxOverErr());
        assertEquals(AM_NEQ, rxce, ps.getRxCRCErr());
        assertEquals(AM_NEQ, coll, ps.getCollisions());
        assertEquals(AM_NEQ, durs, ps.getDurationSec());
        assertEquals(AM_NEQ, durns, ps.getDurationNsec());
    }

    @Test
    public void getPortStatsAll() {
        beginTest("getPortStatsAll");
        lmgr = new ListenerManager(DEFAULT_CTRL_CFG, null, PH_SINK, PH_CB,
                                   FM_ADV, MOCK_ROLE_ADVISOR);
        lmgr.startIOProcessing();
        ls = lmgr;
        PortSwitch sw = connectSwitch(DPID10, DEF10);
        print(sw.toDebugString());

        // this call should block until satisfied
        List<MBodyPortStats> stats = ls.getPortStats(DPID10);
        assertEquals(AM_UXS, 4, stats.size());

        Iterator<MBodyPortStats> iter = stats.iterator();
        verifyPortStats(iter.next(),
                101, 201, 1001, 2001, 18, 28, 14, 24, 15, 16, 17, 34, 0, 0);
        verifyPortStats(iter.next(),
                102, 202, 1002, 2002, 19, 29, 15, 25, 16, 17, 18, 35, 0, 0);
        verifyPortStats(iter.next(),
                103, 203, 1003, 2003, 20, 30, 16, 26, 17, 18, 19, 36, 0, 0);
        verifyPortStats(iter.next(),
                104, 204, 1004, 2004, 21, 31, 17, 27, 18, 19, 20, 37, 0, 0);

        disconnectSwitch(sw);
        endTest();
    }

    @Test
    public void getPortStatsPortTwo() {
        beginTest("getPortStatsPortTwo");
        lmgr = new ListenerManager(DEFAULT_CTRL_CFG, null, PH_SINK, PH_CB,
                                   FM_ADV, MOCK_ROLE_ADVISOR);
        lmgr.startIOProcessing();
        ls = lmgr;
        PortSwitch sw = connectSwitch(DPID10, DEF10);
        print(sw.toDebugString());

        // this call should block until satisfied
        MBodyPortStats stats = ls.getPortStats(DPID10, bpn(2));
        verifyPortStats(stats,
                102, 202, 1002, 2002, 19, 29, 15, 25, 16, 17, 18, 35, 0, 0);

        disconnectSwitch(sw);
        endTest();
    }

    @Test(expected = NotFoundException.class)
    public void getPortStatsNoSuchPort() {
        beginTest("getPortStatsNoSuchPort");
        lmgr = new ListenerManager(DEFAULT_CTRL_CFG, null, PH_SINK, PH_CB,
                                   FM_ADV, MOCK_ROLE_ADVISOR);
        lmgr.startIOProcessing();
        ls = lmgr;
        PortSwitch sw = connectSwitch(DPID10, DEF10);
        print(sw.toDebugString());

        try {
            ls.getPortStats(DPID10, bpn(5));
        } finally {
            disconnectSwitch(sw);
            endTest();
        }
    }

    // TODO: test a 1.3 switch - all ports
    // TODO: test a 1.3 switch - one port
    // TODO: test a 1.3 switch - out-of-bounds port

    // TODO: test a non-existent dpid

    // === Testing enable/disable of a port

    private static final BigPortNumber PORT_4 = bpn(4);

    @Test
    public void disableEnablePort10() {
        beginTest("disableEnablePort10");
        lmgr = new ListenerManager(DEFAULT_CTRL_CFG, null, PH_SINK, PH_CB,
                                   FM_ADV, new MockRoleAdvisor(DPID10));
        lmgr.startIOProcessing();
        ls = lmgr;
        PortSwitch sw = connectSwitch(DPID10, DEF10);
        print(sw.toDebugString());

        Port port4 = ls.getDataPathInfo(DPID10).ports().get(3);
        print(port4);
        assertEquals(AM_NEQ, PORT_4, port4.getPortNumber());
        assertTrue(AM_HUH, port4.isEnabled());

        // disable port 4
        sw.mode(PortSwitch.Mode.EXP_DISABLE);
        MessageFuture f = ls.enablePort(DPID10, PORT_4, false);
        boolean ok = f.awaitUninterruptibly(MAX_FUTURE_WAIT_MS);
        print(f);
        if (!ok)
            fail(E_FUTURE_TIMEOUT);
        assertEquals(AM_NEQ, MessageFuture.Result.SUCCESS, f.result());
        sw.checkForTestFail();

        // enable port 4
        sw.mode(PortSwitch.Mode.EXP_ENABLE);
        f = ls.enablePort(DPID10, PORT_4, true);
        ok = f.awaitUninterruptibly(MAX_FUTURE_WAIT_MS);
        print(f);
        if (!ok)
            fail(E_FUTURE_TIMEOUT);
        assertEquals(AM_NEQ, MessageFuture.Result.SUCCESS, f.result());
        sw.checkForTestFail();

        // attempt an out of bounds port number
        try {
            ls.enablePort(DPID10, bpn(37), true);
            fail(AM_NOEX);
        } catch (NotFoundException e) {
            print(FMT_EX, e);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }

        disconnectSwitch(sw);
        endTest();
    }

}
