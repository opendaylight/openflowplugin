/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.common.AbstractMsgAssertor;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MBodyPortDesc;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.of.mockswitch.MockSwitchBank;
import org.opendaylight.util.junit.SlowTests;
import org.opendaylight.util.junit.TestTools;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test to verify the behavior of the mock switches defined in the
 * "switchBankFour" definition file.
 * <p>
 * Note that this class is not a direct unit test
 * of the switch bank and does not reside in the same package.
 *
 * @author Simon Hunt
 */
@Category(SlowTests.class)
public class MockSwitchBankInteractionsTest extends AbstractBankInteractionsTest {

    private static final String BANK_FOUR = IMPL_ROOT + "switchBankFour.def";
    private static final String BANK_ECHO = IMPL_ROOT + "switchBankEcho.def";

    // === Helper methods to ease writing alternate scenarios...

    private static class MockBankFour extends MockSwitchBank {

        public MockBankFour(String path) {
            super(path, TestTools.showOutput());
        }
        
        @Override
        public int expectedToCompleteHandshake() {
            return 2;   // only 1.0 and 1.3 are expected to complete
        }
        
    }

    private void activateSwitches() {
        activateSwitches(false);
    }

    private void activateSwitches(boolean debugOut) {
        // RUN - fire up the switches in the switch bank
        print(".... activating switches ....");
        bank.activate();

        // BLOCK until the switches are ready, or the timer pops
        waitForHandshake();
        if (debugOut)
            print(ctlr.toDebugString() + EOL);

        // not a full set of assertions, but enough to inspire confidence...
        assertEquals(AM_UXS, 2, ctlr.infoCacheSize());
        Iterator<DataPathInfo> dpiIter = ctlr.getAllDataPathInfo().iterator();
        DataPathInfo dpi = dpiIter.next();
        assertEquals(AM_NEQ, SW10P4_DPID, dpi.dpid());
        assertEquals(AM_NEQ, V_1_0, dpi.negotiated());

        dpi = dpiIter.next();
        assertEquals(AM_NEQ, SW13P32_DPID, dpi.dpid());
        assertEquals(AM_NEQ, V_1_3, dpi.negotiated());

    }

    @Test
    public void bankOfSwitches() {
        beginTest("bankOfSwitches");
        bank = new MockBankFour(BANK_FOUR);

        // GREAT EXPECTATIONS - create and program the message sink
        sink = new TestSink();
        // (no additional programming for this test)
        sink.replay();

        initController(true);
        txrx.startRecording(5);
        activateSwitches();

        print(ctlr.toDebugString());

        // MESSAGE EXCHANGE - additional openflow conversation with switches
        // (none for this test)

        cleanup();
        endTest();
    }

    @Test
    public void descTest() {
        beginTest("descTest");
        bank = new MockBankFour(BANK_FOUR);

        /*
         * We are going to send some multipart-request/DESC messages to a
         * couple of switches, and see what they have to say for themselves...
         */

        // GREAT EXPECTATIONS - create and program the message sink
        sink = new TestSink();
        sink.expect(SW10P4_DPID, MessageType.MULTIPART_REPLY,
                new AbstractMsgAssertor("Sw10p4 MP/DESC", SW10P4_DPID, sink) {
                    @Override
                    protected void runAssertions(OpenflowMessage msg) {
                        OfmMultipartReply rep = (OfmMultipartReply) msg;
                        assertEquals(AM_NEQ, MultipartType.DESC,
                                rep.getMultipartType());
                        MBodyDesc desc = (MBodyDesc) rep.getBody();
                        assertEquals(AM_NEQ, SW10P4_MFR_DESC, desc.getMfrDesc());
                        assertEquals(AM_NEQ, SW10P4_HW_DESC, desc.getHwDesc());
                        assertEquals(AM_NEQ, SW10P4_SW_DESC, desc.getSwDesc());
                        assertEquals(AM_NEQ, SW10P4_SER_NUM, desc.getSerialNum());
                        assertEquals(AM_NEQ, SW10P4_DP_DESC, desc.getDpDesc());
                    }
                });
        sink.expect(SW13P32_DPID, MessageType.MULTIPART_REPLY,
                new AbstractMsgAssertor("Sw13p32 MP/DESC", SW13P32_DPID, sink) {
                    @Override
                    protected void runAssertions(OpenflowMessage msg) {
                        OfmMultipartReply rep = (OfmMultipartReply) msg;
                        assertEquals(AM_NEQ, MultipartType.DESC,
                                rep.getMultipartType());
                        MBodyDesc desc = (MBodyDesc) rep.getBody();
                        assertEquals(AM_NEQ, SW13P32_MFR_DESC, desc.getMfrDesc());
                        assertEquals(AM_NEQ, SW13P32_HW_DESC, desc.getHwDesc());
                        assertEquals(AM_NEQ, SW13P32_SW_DESC, desc.getSwDesc());
                        assertEquals(AM_NEQ, SW13P32_SER_NUM, desc.getSerialNum());
                        assertEquals(AM_NEQ, SW13P32_DP_DESC, desc.getDpDesc());
                    }
                });

        sink.replay();
        initController();
        activateSwitches();

        messagesReceived = new CountDownLatch(2);
        sink.setMsgRxLatch(messagesReceived);

        // MESSAGE EXCHANGE - additional openflow conversation with switches
        sendMpDesc(SW10P4_DPID, V_1_0);
        sendMpDesc(SW13P32_DPID, V_1_3);

        // BLOCK until 2 messages are received, or the timer pops
        waitForMessages();
        cleanup();
        endTest();
    }

    /** Sends a MP-Request/DESC to the specified switch.
     *
     * @param dpid the datapath id of the switch
     * @param pv the protocol version
     */
    private void sendMpDesc(DataPathId dpid, ProtocolVersion pv) {
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(pv, MULTIPART_REQUEST);
        req.type(MultipartType.DESC);
        sink.assocXid(dpid, req.getXid());
        try {
            ctlr.send(req.toImmutable(), dpid);
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }
    }

    @Test
    public void portDescTest() {
        beginTest("portDescTest");
        bank = new MockBankFour(BANK_FOUR);

        /*
         * We are going to send multipart-request/PORT_DESC messages to a
         * 1.3 switch.
         */

        // GREAT EXPECTATIONS - create and program the message sink
        sink = new TestSink();
        sink.expect(SW13P32_DPID, MessageType.MULTIPART_REPLY,
                new AbstractMsgAssertor("Sw13p32 MP/PORT_DESC", SW13P32_DPID, sink) {
                    @Override
                    protected void runAssertions(OpenflowMessage msg) {
                        OfmMultipartReply rep = (OfmMultipartReply) msg;
                        assertEquals(AM_NEQ, MultipartType.PORT_DESC,
                                rep.getMultipartType());
                        MBodyPortDesc.Array ports =
                                (MBodyPortDesc.Array) rep.getBody();

                        List<MBodyPortDesc> pds = ports.getList();
                        assertEquals(AM_UXS, 32, pds.size());
                        Port p32 = pds.get(31).getPort(); // 0-indexed
                        assertEquals(AM_NEQ, mac("0016B9:0D011F"),
                                p32.getHwAddress());
                    }
                });


        sink.replay();
        initController();
        activateSwitches();

        messagesReceived = new CountDownLatch(1);
        sink.setMsgRxLatch(messagesReceived);

        // MESSAGE EXCHANGE - additional openflow conversation with switches
        sendMpPortDesc(SW13P32_DPID);

        // BLOCK until a message is received, or the timer pops
        waitForMessages();
        cleanup();
        endTest();
    }

    private void sendMpPortDesc(DataPathId dpid) {
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(MultipartType.PORT_DESC);
        sink.assocXid(dpid, req.getXid());
        try {
            ctlr.send(req.toImmutable(), dpid);
        } catch (OpenflowException e) {
            print(e);
            fail(AM_UNEX);
        }
    }

    @Test @Ignore
    public void echoTest() {
        beginTest("echoTest");
        initBank(BANK_ECHO);

        /*
         * Note that the controller consumes the echo-request from the switch
         * and does not pass it up through the message sink.
         */
        sink = new TestSink();
        sink.replay();
        initController();

        // set up to listen for HELLO, FEAT_REQ, ECHO_REPLY...
        setInternalMsgLatchCount(SW10P4_DPID, 3);
        activateSwitches();
        waitForInternalMessages();

        // set up to listen for second ECHO_REPLY...
        setInternalMsgLatchCount(SW10P4_DPID, 1);
        bank.resume();
        waitForInternalMessages();

        cleanup();
        endTest();
    }

}
