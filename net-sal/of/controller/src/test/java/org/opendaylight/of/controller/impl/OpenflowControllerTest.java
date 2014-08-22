/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.common.AbstractMsgAssertor;
import org.opendaylight.of.common.MessageSink;
import org.opendaylight.of.controller.AlertSink;
import org.opendaylight.of.controller.ControllerStats;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OfmMultipartReply;
import org.opendaylight.of.lib.msg.OfmMutableMultipartRequest;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.of.mockswitch.MockOpenflowSwitch;
import org.opendaylight.util.api.security.SecurityContext;
import org.opendaylight.util.junit.SlowTests;
import org.opendaylight.util.junit.TestTools;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.opendaylight.of.controller.impl.ControllerConfig.*;
import static org.opendaylight.of.controller.impl.OpenflowController.E_BAD_VERSION;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link org.opendaylight.of.controller.impl.OpenflowController}.
 *
 * @author Simon Hunt
 * @author Scoot Simes
 * @author Sudheer Duggisetty
 */
@Category(SlowTests.class)
public class OpenflowControllerTest extends AbstractControllerTest {

    private PortStateTracker tracker = new PortStateTracker(lmgr);
    private OpenflowController ctlr;
    private TestSink sink;
    private AlertSink as = new AlertLogger();

    private void initControllerUnprogrammedSink(boolean enableEchoes) {
        sink = new TestSink(SW10P4_DPID);
        initController(sink, as, enableEchoes);
    }

    private void initControllerDontCareSink(boolean enableEchoes) {
        sink = new TestSink(SW10P4_DPID);
        sink.setNonStrict().replay();
        initController(sink, as, enableEchoes);
    }

    // initialize the controller with a message sink
    private void initController(MessageSink sink, AlertSink as, boolean echoes) {
        OpenflowController.enableIdleDetection(echoes);
        ControllerConfig cfg = bld().listenPort(DEF_PORT)
                .tlsListenPort(DEF_TLS_PORT)
                .udpPort(DEF_UDP_PORT)
                .idleCheckMs(10).maxIdleMs(30).maxEchoAttempts(2).maxEchoMs(50)
                .build();
        ctlr = new OpenflowController(cfg, tracker, sink, as, PH_SINK, PH_CB, FM_ADV);
        print(ctlr.toDebugString());
        assertEquals(AM_UXS, 0, ctlr.infoCacheSize());
        ctlr.suppressSetConfig(true);
        // fire up the controller
        ctlr.initAndStartListening();
    }

    // initialize the secure controller with a message sink
    private void initSecureController(MessageSink sink) {
        SecurityContext sc =
                new SecurityContext(DEFAULT_KS_NAME, DEFAULT_KS_PASS,
                                    DEFAULT_TS_NAME, DEFAULT_TS_PASS);
        ControllerConfig cfg = bld().listenPort(DEF_PORT)
                .tlsListenPort(DEF_TLS_PORT)
                .udpPort(DEF_UDP_PORT)
                .securityContext(sc)
                .build();
        ctlr = new OpenflowController(cfg, tracker, sink, as, PH_SINK, PH_CB, FM_ADV);
        print(ctlr.toDebugString());
        assertEquals(AM_UXS, 0, ctlr.infoCacheSize());
        ctlr.suppressSetConfig(true);
        // fire up the controller
        ctlr.initAndStartListening();
    }

    @BeforeClass
    public static void classSetUp() {
        setUpLogger();
    }

    @After
    public void tearDown() {
        // shutdown the controller
        ctlr.shutdown();
    }

    // the stats for connecting a single switch:
    // 2 TX (HELLO, FEATURES_REQUEST, MP-Request/DESC)
    // 2 RX (HELLO, FEATURES_REPLY, MP-Reply/DESC)
    private static final String EXP_STATS_SUFFIX = ",#pktIn=0,inBytes=0," +
            "#pktOut=0,outBytes=0,#pktDrop=0,dropBytes=0,#msgRx=3,#msgTx=3}";

    @Test
    public void basic() {
        beginTest("basic");
        initControllerDontCareSink(false);
        switchesReady = new CountDownLatch(1); // expecting just one switch
        sink.setDataPathAddedLatch(switchesReady);
        initTxRxControl(ctlr);
        startRecording(2);
        
        // fire up a switch
        createAndActivateSwitch(SW10P4);
        waitForHandshake();
        print(ctlr.toDebugString());
        assertEquals(AM_UXS, 1, ctlr.infoCacheSize());

        DataPathInfo info = ctlr.getDataPathInfo(SW10P4_DPID);
        assertNotNull(AM_HUH, info);
        assertEquals(AM_NEQ, SW10P4_DPID, info.dpid());

        ControllerStats stats = ctlr.getStats();
        print(stats);
        delay(200);
        stopRecordingAndPrintDebugTrace();
        
        assertTrue("toString wrong", stats.toString().endsWith(EXP_STATS_SUFFIX));
        endTest();
    }

    @Test @Ignore ("Certificate seems to have expired - needs replacing")
    public void basicSecure() {
        beginTest("basicSecure");
        sink = new TestSink(SW10P4_DPID);
        initSecureController(sink);
        switchesReady = new CountDownLatch(1); // expecting just one switch
        sink.setDataPathAddedLatch(switchesReady);
        // fire up a switch
        createAndActivateSecureSwitch(SW10P4);
        waitForHandshake();
        print(ctlr.toDebugString());
        assertEquals(AM_UXS, 1, ctlr.infoCacheSize());

        DataPathInfo info = ctlr.getDataPathInfo(SW10P4_DPID);
        assertNotNull(AM_HUH, info);
        assertEquals(AM_NEQ, SW10P4_DPID, info.dpid());
        endTest();
    }

    @Test
    public void twoSwitches() {
        beginTest("twoSwitches");
        initControllerDontCareSink(false);
        switchesReady = new CountDownLatch(2); // expecting 2 switches
        sink.setDataPathAddedLatch(switchesReady);

        // fire up a couple of switches
        createAndActivateSwitch(SW10P4);
        createAndActivateSwitch(SW13P32);
        waitForHandshake();
        print(ctlr.toDebugString());
        assertEquals(AM_UXS, 2, ctlr.infoCacheSize());

        DataPathInfo info = ctlr.getDataPathInfo(SW10P4_DPID);
        print(info);
        assertNotNull(AM_HUH, info);
        assertEquals(AM_NEQ, SW10P4_DPID, info.dpid());

        info = ctlr.getDataPathInfo(SW13P32_DPID);
        print(info);
        assertNotNull(AM_HUH, info);
        assertEquals(AM_NEQ, SW13P32_DPID, info.dpid());

        endTest();
    }

    // =====================================================================
    // === Test sending and receiving a multipart-request/reply for DESC

    @Test
    public void mpDesc() throws OpenflowException {
        beginTest("mpDesc");

        // create a mock switch
        MockOpenflowSwitch sw = new MockOpenflowSwitch(SW13P32, showOutput);
        DataPathId dpid = sw.getDpid();

        // create and program a test sink
        sink = new TestSink(SW13P32_DPID);
        sink.expect(MULTIPART_REPLY,
                new AbstractMsgAssertor("MP-Reply", dpid, sink) {
            @Override
            public void runAssertions(OpenflowMessage msg) {
                OfmMultipartReply ofm = (OfmMultipartReply) msg;
                assertEquals("mpType", MultipartType.DESC,
                        ofm.getMultipartType());
                MBodyDesc desc = (MBodyDesc) ofm.getBody();
                assertEquals("mfr", SW13P32_MFR_DESC, desc.getMfrDesc());
                assertEquals("hw", SW13P32_HW_DESC, desc.getHwDesc());
                assertEquals("sw", SW13P32_SW_DESC, desc.getSwDesc());
                assertEquals("ser#", SW13P32_SER_NUM, desc.getSerialNum());
                assertEquals("dpdesc", SW13P32_DP_DESC, desc.getDpDesc());
            }
        });
        sink.replay();
        initController(sink, as, false);
        switchesReady = new CountDownLatch(1); // expecting a single switch
        sink.setDataPathAddedLatch(switchesReady);

        // fire up a switch that handles MP-Req/DESC message from controller
        sw.activate();
        waitForHandshake();
        print(ctlr.toDebugString() + EOL);

        // we want to send a DESC request to the switch
        messagesReceived = new CountDownLatch(1); // just the one message
        sink.setMsgRxLatch(messagesReceived);
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(MultipartType.DESC);
        sink.assocXid(dpid, req.getXid());
        ctlr.send(req.toImmutable(), dpid);

        waitForMessages();
        sink.endReplay();
        endTest();
    }

    @Test @Ignore("Certificate seems to have expired - needs replacing")
    public void mpDescSecure() throws OpenflowException {
        beginTest("mpDescSecure");
        // Start TLS client connection; using keystore and truststore
        // as same file.
        // create a mock switch
        MockOpenflowSwitch sw = new MockOpenflowSwitch(SW13P32, showOutput);
        sw.setSecure(PKI_ROOT + "device.jks", DEFAULT_KS_PASS);
        DataPathId dpid = sw.getDpid();

        // create and program a test sink
        sink = new TestSink(SW13P32_DPID);
        sink.expect(MULTIPART_REPLY,
                new AbstractMsgAssertor("MP-Reply", dpid, sink) {
            @Override
            public void runAssertions(OpenflowMessage msg) {
                OfmMultipartReply ofm = (OfmMultipartReply) msg;
                assertEquals("mpType", MultipartType.DESC,
                        ofm.getMultipartType());
                MBodyDesc desc = (MBodyDesc) ofm.getBody();
                assertEquals("mfr", SW13P32_MFR_DESC, desc.getMfrDesc());
                assertEquals("hw", SW13P32_HW_DESC, desc.getHwDesc());
                assertEquals("sw", SW13P32_SW_DESC, desc.getSwDesc());
                assertEquals("ser#", SW13P32_SER_NUM, desc.getSerialNum());
                assertEquals("dpdesc", SW13P32_DP_DESC, desc.getDpDesc());
            }
        });
        sink.replay();
        initSecureController(sink);
        switchesReady = new CountDownLatch(1); // expecting a single switch
        sink.setDataPathAddedLatch(switchesReady);

        // fire up a switch that handles MP-Req/DESC message from controller
        sw.activate();
        waitForHandshake();
        print(ctlr.toDebugString() + EOL);

        // we want to send a DESC request to the switch
        messagesReceived = new CountDownLatch(1); // just the one message
        sink.setMsgRxLatch(messagesReceived);
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(MultipartType.DESC);
        sink.assocXid(dpid, req.getXid());
        ctlr.send(req.toImmutable(), dpid);

        waitForMessages();
        sink.endReplay();
        endTest();
    }

    @Test
    public void notNegotiatedVersion() {
        beginTest("notNegotiatedVersion");
        initControllerDontCareSink(false);
        switchesReady = new CountDownLatch(1); // expecting just one switch
        sink.setDataPathAddedLatch(switchesReady);

        // fire up a switch
        createAndActivateSwitch(SW10P4);
        waitForHandshake();
        print(ctlr.toDebugString());
        assertEquals(AM_UXS, 1, ctlr.infoCacheSize());

        DataPathInfo info = ctlr.getDataPathInfo(SW10P4_DPID);
        assertEquals(AM_NEQ, V_1_0, info.negotiated());

        // try and send a 1.3 message to a 1.0 switch
        OpenflowMessage echo =
                MessageFactory.create(V_1_3, ECHO_REQUEST).toImmutable();
        try {
            ctlr.send(echo, info.dpid());
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, E_BAD_VERSION + V_1_3, e.getMessage());
        } catch (Exception e) {
            fail(AM_WREX);
        }
        endTest();
    }

    // Mock switch capable of echo or not...
    private class EchoMockOpenflowSwitch extends MockOpenflowSwitch {
        final boolean isZombie;
        boolean echoRequestReceived;

        EchoMockOpenflowSwitch(boolean isZombie) {
            super(SW10P4, showOutput);
            this.isZombie = isZombie;
        }

        @Override
        protected void msgRx(OpenflowMessage msg) {
            if (msg.getType() == ECHO_REQUEST) {
                echoRequestReceived = true;
                if (!isZombie)
                    send(MessageFactory.create(msg, ECHO_REPLY).toImmutable());
            } else {
                super.msgRx(msg);
            }
        }
    }

    private void configForZombieTests() {
        OpenflowConnection.Config cfg = OpenflowConnection.getConfig();
        cfg.idleCheckMs = 10;
        cfg.maxIdleMs = 30;
        cfg.maxEchoMs = 30;
        cfg.maxEchoAttempts = 2;
    }

    @Test
    public void notZombie() {
        beginTest("notZombie");
        configForZombieTests();
        initControllerUnprogrammedSink(true);
        switchesReady = new CountDownLatch(1); // expecting just one switch

        EchoMockOpenflowSwitch sw = new EchoMockOpenflowSwitch(false);

        sink.setDataPathAddedLatch(switchesReady);
        sink.expect(ECHO_REPLY,
                    new AbstractMsgAssertor("Echo-Reply", sw.getDpid(), sink) {
                @Override
                public void runAssertions(OpenflowMessage msg) {
                }
            });
        sink.setNonStrict().replay();

        // fire up a switch
        sw.activate();
        waitForHandshake();
        assertEquals(AM_UXS, 1, ctlr.infoCacheSize());

        messagesReceived = new CountDownLatch(1); // just the one message
        sink.setMsgRxLatch(messagesReceived);

        // allow a little extra time for the idle-detect to kick in
        TestTools.delay(200);

        // and make sure that the switch is still connected
        assertEquals(AM_UXS, 1, ctlr.infoCacheSize());
        assertTrue(AM_HUH, sw.echoRequestReceived);
        sink.endReplay();

        endTest();
    }

    @Test
    public void zombieDetect() {
        beginTest("zombieDetect");
        configForZombieTests();
        initControllerDontCareSink(true);
        switchesReady = new CountDownLatch(1); // expecting just one switch

        MockOpenflowSwitch sw = new EchoMockOpenflowSwitch(true);

        sink.setDataPathAddedLatch(switchesReady);

        // fire up a switch
        sw.activate();
        waitForHandshake();
        assertEquals(AM_UXS, 1, ctlr.infoCacheSize());

        messagesReceived = new CountDownLatch(1); // just the one message
        sink.setMsgRxLatch(messagesReceived);

        // allow a little extra time for the idle-detect to kick in
        TestTools.delay(500);

        // and make sure that the switch is not still connected
        assertEquals(AM_UXS, 0, ctlr.infoCacheSize());
        sink.endReplay();

        endTest();
    }

}
