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
import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.MessageListener;
import org.opendaylight.of.controller.QueueEvent;
import org.opendaylight.of.controller.pkt.PacketSequencerSink;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.msg.*;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.msg.FlowModCommand.ADD;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.BARRIER_REQUEST;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_IN;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for ControllerManager.
 *
 * @author Simon Hunt
 */
public class ControllerManagerTest extends AbstractControllerTest {
    private static OfmPacketIn PKT_IN_10;
    static {
        OfmMutablePacketIn pi = (OfmMutablePacketIn) create(V_1_0, PACKET_IN);
        pi.inPort(bpn(3));
        PKT_IN_10 = (OfmPacketIn) pi.toImmutable();
    }

    private static final DataPathId DPID = dpid("1/222222333333");

    private static final Match MATCH = (Match) createMatch(V_1_3).toImmutable();

    private static final OpenflowMessage FLOW_MOD =
            ((OfmMutableFlowMod) create(V_1_3,
                    MessageType.FLOW_MOD, ADD)).tableId(tid(7)).bufferId(bid(1))
                    .match(MATCH).toImmutable();
    private static final OpenflowMessage BARRIER =
            create(V_1_3, BARRIER_REQUEST).toImmutable();

    private static class TestMsgListener implements MessageListener {
        @Override public void queueEvent(QueueEvent event) { }
        @Override public void event(MessageEvent event) { }
    }

    @BeforeClass
    public static void classSetUp() {
        Assume.assumeTrue(!isUnderCoverage());
        setUpLogger();
    }

    @After
    public void tearDown() {
        cmgr.shutdown();
    }

    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor();
        eds = new MockEventDispatcher();

        cmgr = new ControllerManager(DEFAULT_CTRL_CFG, alertSink, PH_SINK,
                FM_ADV, roleAdvisor, eds);
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();
    }


    @Test
    public void cannotRegisterForPacketIns() {
        beginTest("cannotRegisterForPacketIns");
        initController();

        Set<MessageType> interest = EnumSet.of(PACKET_IN);
        try {
            cs.addMessageListener(new MessageListenerAdapter(), interest);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
        }
        endTest();
    }

    private class PacketInPhobic extends MessageListenerAdapter {
        @Override
        public void event(MessageEvent event) {
            if (event.msg().getType() == PACKET_IN)
                fail("Saw a packet-in: " + event.msg());
        }
    }

    @Test
    public void onlySequencerGetsPacketIns() {
        beginTest("onlySequencerGetsPacketIns");
        initController();
        // register for all message types...
        cs.addMessageListener(new PacketInPhobic(), null);
        lmgr.msgRx(PKT_IN_10, DPID, MAIN_ID, V_1_0);
        endTest();
    }

    @Test
    public void cantRegisterSecondSequencer() {
        beginTest("cantRegisterSecondSequencer");
        initController();

        PacketSequencerSink foo = new PacketSequencerSink() {
            @Override public void processPacket(MessageEvent ev) {}
        };

        try {
            lmgr.registerSequencer(foo);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            print(FMT_EX, e);
        }
        endTest();
    }

    @Test
    public void cantSendFlowMod() throws OpenflowException {
        beginTest("cantSendFlowMod");
        initController();

        // directly...
        try {
            cs.send(FLOW_MOD, DPID);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
        }

        // or from a list...
        try {
            cs.send(Arrays.asList(BARRIER, FLOW_MOD), DPID);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
        }
        endTest();
    }

    // MSG_LISTENERS: FlowTrk2, MessageSatisfier; not Sequencer
    private static final int BASE_MSG_LISTENERS = 2;

    @Test
    public void addAndRemoveListener() {
        beginTest("addAndRemoveListener");
        initController();
        print(lmgr);
        assertEquals(AM_NEQ, BASE_MSG_LISTENERS, lmgr.msgListenerCount());

        MessageListener m = new TestMsgListener();
        cs.addMessageListener(m, null);
        print(lmgr);
        assertEquals(AM_NEQ, BASE_MSG_LISTENERS + 1, lmgr.msgListenerCount());

        cs.removeMessageListener(m);
        print(lmgr);
        assertEquals(AM_NEQ, BASE_MSG_LISTENERS, lmgr.msgListenerCount());
        endTest();
    }

    @Test
    public void isHybridMode() {
        beginTest("isHybridMode");
        initController();

        ControllerConfig newCfg =
                new ControllerConfig.Builder().hybridMode(true).build();
        ControllerManager hybridMgr;
        hybridMgr = new ControllerManager(newCfg, alertSink, PH_SINK,
                FM_ADV, roleAdvisor, eds);
        assertTrue(AM_NEQ, hybridMgr.isHybridMode());
        endTest();
    }
}
