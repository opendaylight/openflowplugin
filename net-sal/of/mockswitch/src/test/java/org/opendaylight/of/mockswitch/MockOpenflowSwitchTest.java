/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.err.ECodeHelloFailed;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MBodyPortDesc;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.junit.SlowTests;
import org.opendaylight.util.net.MacAddress;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.CommonUtils.notMutable;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.Capability.*;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MockOpenflowSwitch.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Sudheer Duggisetty
 */
@Category(SlowTests.class)
public class MockOpenflowSwitchTest extends SwTest {

    // =====================================================
    // === This first section is the test harness where we
    // === emulate the controller side of the connection.
    // ===

    private static final int OPENFLOW_PORT = 6633;
    private static final int OPENFLOW_TLS_PORT = 6634;
    // TODO: research a good value for this
    private static final int RCV_BUF_SIZE = 1048576;

    private TestMessageHandler msgHandler;
    private TestMessageHandler secureMsgHandler;

    private ChannelGroup allChannels = new DefaultChannelGroup();

    private Executor boss = newSingleThreadExecutor(namedThreads("CtlBoss"));
    private Executor worker = newSingleThreadExecutor(namedThreads("CtlWorker"));
    private final ChannelFactory channelFactory =
            new OioServerSocketChannelFactory(boss, worker);

    private ServerBootstrap sBoot = new ServerBootstrap(channelFactory);

    private Channel chToSwitch;
    // TLS
    private Executor tlsBoss = newSingleThreadExecutor(namedThreads("CtlSecBoss"));
    private Executor tlsWorker = newSingleThreadExecutor(namedThreads("CtlSecWorker"));
    private final ChannelFactory tlsChannelFactory =
            new NioServerSocketChannelFactory(tlsBoss, tlsWorker);

    private ServerBootstrap tlsBoot = new ServerBootstrap(tlsChannelFactory);

    static final String ALGORITHM = "SunX509";
    static final String STORE_PASS = "skyline";
    static final String PKI_ROOT = "src/test/resources/org/opendaylight/of/mockswitch/";
    static final String CONTROLLER_KS_FILE = PKI_ROOT + "controller.jks";
    static final String DEVICE_KS_FILE = PKI_ROOT + "device.jks";

    // ======== TEST HARNESS (mocked controller) PIPELINE FACTORY ======

    private class TestHarnessPipelineFactory implements ChannelPipelineFactory {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
            msgHandler = new TestMessageHandler(false);
            ChannelPipeline p = Channels.pipeline();
            p.addLast("decoder", new OfmDecoder());
            p.addLast("encoder", new OfmEncoder());
            p.addLast("messageHandler", msgHandler);
            return p;
        }
    }

    private class TestSecurePipelineFactory implements ChannelPipelineFactory {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
            secureMsgHandler = new TestMessageHandler(true);
            ChannelPipeline p = Channels.pipeline();
            SSLEngine engine =
                    SslContextFactory.getServerContext().createSSLEngine();
            engine.setUseClientMode(false);
            engine.setWantClientAuth(true);
            p.addLast("ssl", new SslHandler(engine));
            p.addLast("decoder", new OfmDecoder());
            p.addLast("encoder", new OfmEncoder());
            p.addLast("messageHandler", secureMsgHandler);
            return p;
        }
    }

    private static class SslContextFactory {
        static SSLContext sslContext;

        static {
            try {
                // set up key manager to do server authentication
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new FileInputStream(CONTROLLER_KS_FILE),
                        STORE_PASS.toCharArray());
                KeyManagerFactory kmf =
                        KeyManagerFactory.getInstance(ALGORITHM);
                kmf.init(ks, STORE_PASS.toCharArray());

                KeyStore ts = KeyStore.getInstance("JKS");
                ts.load(new FileInputStream(CONTROLLER_KS_FILE),
                        STORE_PASS.toCharArray());
                TrustManagerFactory tmf =
                        TrustManagerFactory.getInstance(ALGORITHM);
                tmf.init(ts);

                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                sslContext = ctx;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static SSLContext getServerContext() {
            return sslContext;
        }
    }

    private static final String MSG_RX = "msg rcvd from mockswitch: {}";

    private static final CfgHello CTL_HELLO_CFG =
            new CfgHello(CfgHello.Behavior.EAGER, V_1_0, V_1_3);
    private static final OpenflowMessage CTL_HELLO =
            CTL_HELLO_CFG.createHelloMsg();

    // ======== TEST HARNESS (mocked controller) MESSAGE HANDLER ======
    /** Handle messages received from the mock switch. */
    class TestMessageHandler extends SimpleChannelUpstreamHandler {
        private OfConnection ofConn;
        private boolean secure = false;

        public TestMessageHandler(boolean secure) {
            this.secure = secure;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx,
                                    ExceptionEvent e) throws Exception {
            print(e);
            fail(AM_UNEX + e);
        }

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
                throws Exception {
            allChannels.add(e.getChannel());
        }

        @Override
        public void channelConnected(final ChannelHandlerContext ctx,
                                     final ChannelStateEvent e) throws Exception {
            if (!secure) {
                processChannelConnected(ctx, e);
            } else {
                final SslHandler sslHandler =
                        ctx.getPipeline().get(SslHandler.class);
                ChannelFuture handshakeFuture = sslHandler.handshake();
                handshakeFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future)
                            throws Exception {
                        if (future.isSuccess()) {
                            print("TLS handshake is completed.");
                            print("  Starting Openflow handshake..");
                            processChannelConnected(ctx, e);
                        }
                        else
                            future.getChannel().close();
                    }
                });
            }
        }

        private void processChannelConnected(ChannelHandlerContext ctx,
                                             ChannelStateEvent e) {
            // Send the initial HELLO to the switch (using worker thread?)
            print("channelConnected: ctx={}, e={}", ctx, e);
            chToSwitch = e.getChannel();
            ofConn = new OfConnection(chToSwitch.getId(), chToSwitch.toString());
            ConnMemo memo = new ConnMemo(this, ctx);
            ofConn.outBoundHello((OfmHello) CTL_HELLO, memo);
            send(ctx, CTL_HELLO);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
                throws Exception {
            OpenflowMessage msg = (OpenflowMessage) e.getMessage();
            processMessage(ctx, msg);
        }

        /** Process a single OpenFlow message received from the mock-switch.
         *
         * @param ctx channel handler context
         * @param msg the message
         */
        private void processMessage(ChannelHandlerContext ctx,
                                    OpenflowMessage msg) {
            lastMsgReceived = msg;
            print(MSG_RX, msg.toDebugString());

            switch (msg.getType()) {

                // EMULATE THE CONTROLLER SIDE OF THE HANDSHAKE

                // FIXME -- When these assertions fail, the test does not!
                //  Probably because the assertion is not in the test thread
                case HELLO: {
                    assertEquals(AM_NEQ, expVersion, msg.getVersion());
                    // we don't expect the XID to be set
                    assertEquals(AM_NEQ, 0, msg.getXid());
                    ConnMemo memo = new ConnMemo(this, ctx);
                    ofConn.inBoundHello((OfmHello) msg, memo);
                    break;
                }

                case FEATURES_REPLY: {
                    OfmFeaturesReply rep = (OfmFeaturesReply) msg;

                    ConnMemo memo = new ConnMemo(this, ctx);
                    ofConn.inBoundFeaturesReply((OfmFeaturesReply) msg, memo);

                    // assertions based on values set up in the unit test
                    assertEquals(AM_NEQ, expVersion, msg.getVersion());
                    assertEquals(AM_NEQ, expXid, rep.getXid());
                    assertEquals(AM_NEQ, expDpid, rep.getDpid());
                    assertEquals(AM_NEQ, expNumBuffers, rep.getNumBuffers());
                    assertEquals(AM_NEQ, expNumTables, rep.getNumTables());
                    assertEquals(AM_NEQ, expCapsSet, rep.getCapabilities());
                    handshakeLatch.countDown();
                    break;
                }

                default:
                    messageLatch.countDown();
                    break;
            }
        }

        private void send(ChannelHandlerContext ctx, OpenflowMessage msg) {
            notMutable(msg);
            ctx.getChannel().write(msg);
        }

        private void send(OpenflowMessage msg) {
            if (chToSwitch == null)
                fail("chToSwitch is null");
            notMutable(msg);
            chToSwitch.write(msg);
        }

        public void versionNegotiatedAs(ProtocolVersion pv, ConnMemo memo) {
            if (pv != null) {
                // send features request using the negotiated version
                MutableMessage freq =
                        MessageFactory.create(pv, FEATURES_REQUEST);
                expXid = freq.getXid();
                sendViaMemo(memo, freq);
            } else {
                // switch's protocol version...
                ProtocolVersion swPv = ofConn.getInBoundHelloVersion();

                OfmMutableError err = (OfmMutableError)
                        MessageFactory.create(swPv, ERROR);
                err.errorType(ErrorType.HELLO_FAILED);
                err.errorCode(ECodeHelloFailed.INCOMPATIBLE);
                err.errorMessage("Not compat, dude!");
                sendViaMemo(memo, err);
            }
        }

        public void handshakeComplete() {
            print("--- handshake complete ---");
        }

        private void sendViaMemo(ConnMemo memo, MutableMessage mm) {
            OpenflowMessage toSend = mm.toImmutable();
            send(memo.ctx, toSend);
        }
    }


    // expected values from switch's features reply

    private DataPathId expDpid;
    private ProtocolVersion expVersion;
    private long expXid;
    private int expNumBuffers;
    private int expNumTables;
    private Set<Capability> expCapsSet;

    // checks and balances...
    private OpenflowMessage lastMsgReceived;
    private CountDownLatch handshakeLatch;
    private CountDownLatch messageLatch;
    private StopWatch watch;


    // ========

    @Before
    public void setUp() {
        sBoot.setOption("localAddress", new InetSocketAddress(OPENFLOW_PORT));
        sBoot.setOption("reuseAddress", true);
        // Options for its children
        sBoot.setOption("child.tcpNoDelay", true);
        sBoot.setOption("child.receiveBufferSize", RCV_BUF_SIZE);
        sBoot.setPipelineFactory(new TestHarnessPipelineFactory());
        print("About to bind to {}", OPENFLOW_PORT);
        Channel serverChannel = sBoot.bind();
        allChannels.add(serverChannel);

        //TLS Server
        tlsBoot.setOption("localAddress",
                          new InetSocketAddress(OPENFLOW_TLS_PORT));
        tlsBoot.setOption("reuseAddress", true);
        // Options for its children
        tlsBoot.setOption("child.tcpNoDelay", true);
        tlsBoot.setOption("child.receiveBufferSize", RCV_BUF_SIZE);
        tlsBoot.setPipelineFactory(new TestSecurePipelineFactory());
        print("About to bind to {}", OPENFLOW_PORT);
        Channel tlsChannel = tlsBoot.bind();
        allChannels.add(tlsChannel);
    }

    @After
    public void tearDown() {
        allChannels.close().awaitUninterruptibly(); // BLOCKS
        sBoot.releaseExternalResources();
    }

    private static final long MAX_LATCH_DELAY_MS = 500; // milliseconds

    private static final String SW10_PATH = "org/opendaylight/of/mockswitch/simple10sw4port.def";
    private static final String SW13_PATH = "org/opendaylight/of/mockswitch/simple13sw32port.def";

    private static final Capability[] SW_CAPS = {
            FLOW_STATS, TABLE_STATS, PORT_STATS, IP_REASM, QUEUE_STATS,
    };
    private static final Set<Capability> SW_CAPS_SET =
            new HashSet<Capability>(Arrays.asList(SW_CAPS));

    MockOpenflowSwitch sw;

    private MacAddress mac(String s) {
        return MacAddress.valueOf(s);
    }

    private void waitForHandshake() {
        try {
            handshakeLatch.await(MAX_LATCH_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    private void waitForMessages() {
        try {
            messageLatch.await(MAX_LATCH_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    private void set10ExpectedValues() {
        // set up our expected values from the features reply
        // see simple10sw4port.def
        expDpid = DataPathId.valueOf("42/0016b9:006502");
        expVersion = V_1_0;
        // expXid is set up in TestMessageHandler.versionNegotiatedAs()
        expNumBuffers = 256;
        expNumTables = 12;
        expCapsSet = SW_CAPS_SET;
    }

    private void set13ExpectedValues() {
        // set up our expected values from the features reply
        // see simple13sw32port.def
        expDpid = DataPathId.valueOf("42/0016b9:068000");
        expVersion = V_1_3;
        // expXid is set up in TestMessageHandler.versionNegotiatedAs()
        expNumBuffers = 256;
        expNumTables = 12;
        expCapsSet = SW_CAPS_SET;
    }

    // ===== Some actual tests :)

    @Test
    public void sw10Test() {
        print(EOL + "sw10Test()");
        watch = new StopWatch("sw10Test");
        sw = new MockOpenflowSwitch(SW10_PATH, showOutput);
        print("instantiated mock switch:");
        print(sw.toDebugString());

        set10ExpectedValues();
        handshakeLatch = new CountDownLatch(1);
        sw.activate();
        waitForHandshake();
        print(watch.stop());
    }

    @Test
    public void sw13Test() {
        print(EOL + "sw13Test()");
        watch = new StopWatch("sw13Test");
        sw = new MockOpenflowSwitch(SW13_PATH, showOutput);
        print("instantiated mock switch:");
        print(sw.toDebugString());

        set13ExpectedValues();
        handshakeLatch = new CountDownLatch(1);
        sw.activate();
        waitForHandshake();
        print(watch.stop());
    }

    @Test
    public void sw13PortDescTest() {
        print(EOL + "sw13PortDescTest()");
        watch = new StopWatch("sw13PortDescTest");
        sw = new MockOpenflowSwitch(SW13_PATH, showOutput);
        print("instantiated mock switch:");
        print(sw.toDebugString());

        set13ExpectedValues();
        handshakeLatch = new CountDownLatch(1);
        sw.activate();
        waitForHandshake();

        // now send a multipart request for port description
        messageLatch = new CountDownLatch(1);
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(MultipartType.PORT_DESC);
        msgHandler.send(req.toImmutable());
        waitForMessages();

        assertEquals(AM_NEQ, MULTIPART_REPLY, lastMsgReceived.getType());
        OfmMultipartReply rep = (OfmMultipartReply) lastMsgReceived;
        assertEquals(AM_NEQ, MultipartType.PORT_DESC, rep.getMultipartType());
        MBodyPortDesc.Array array = (MBodyPortDesc.Array) rep.getBody();
        List<MBodyPortDesc> ports = array.getList();
        assertEquals(AM_UXS, 32, ports.size());
        Port p = ports.get(2).getPort();
        assertEquals(AM_NEQ, mac("00:16:b9:0d:01:02"), p.getHwAddress());

        print(watch.stop());
    }

    @Test
    public void sw13DescTest() {
        print(EOL + "sw13DescTest()");
        watch = new StopWatch("sw13DescTest");
        sw = new MockOpenflowSwitch(SW13_PATH, showOutput);
        print("instantiated mock switch:");
        print(sw.toDebugString());

        set13ExpectedValues();
        handshakeLatch = new CountDownLatch(1);
        sw.activate();
        waitForHandshake();

        // now send a multipart request for description
        messageLatch = new CountDownLatch(1);
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(MultipartType.DESC);
        msgHandler.send(req.toImmutable());
        waitForMessages();

        assertEquals(AM_NEQ, MULTIPART_REPLY, lastMsgReceived.getType());
        OfmMultipartReply rep = (OfmMultipartReply) lastMsgReceived;
        assertEquals(AM_NEQ, MultipartType.DESC, rep.getMultipartType());
        MBodyDesc desc = (MBodyDesc) rep.getBody();
        assertEquals(AM_NEQ, "Acme Switch Co.", desc.getMfrDesc());
        assertEquals(AM_NEQ, "RoadRunner 9000", desc.getHwDesc());
        assertEquals(AM_NEQ, "OF-BeepBeep 1.6.0", desc.getSwDesc());
        assertEquals(AM_NEQ, "WB-11954-TAF", desc.getSerialNum());
        assertEquals(AM_NEQ, "9000 in Secret Hideout, Somewhere Safe",
                desc.getDpDesc());

        print(watch.stop());
    }

    // TLS tests
    @Test @Ignore ("Certificate seems to have expired - needs replacing")
    public void sw10SecureTest() {
        print(EOL + "sw10SecureTest()");
        watch = new StopWatch("sw10SecureTest");
        sw = new MockOpenflowSwitch(SW10_PATH, showOutput);
        sw.setSecure(DEVICE_KS_FILE, STORE_PASS);
        print("instantiated mock switch:");
        print(sw.toDebugString());

        set10ExpectedValues();
        handshakeLatch = new CountDownLatch(1);
        sw.activate();
        waitForHandshake();
        print(watch.stop());
    }

    @Test @Ignore ("Certificate seems to have expired - needs replacing")
    public void sw13SecureTest() {
        print(EOL + "sw13SecureTest()");
        watch = new StopWatch("sw13SecureTest");
        sw = new MockOpenflowSwitch(SW13_PATH, showOutput);
        sw.setSecure(DEVICE_KS_FILE, STORE_PASS);
        print("instantiated mock switch:");
        print(sw.toDebugString());

        set13ExpectedValues();
        handshakeLatch = new CountDownLatch(1);
        sw.activate();
        waitForHandshake();
        print(watch.stop());
    }

    @Test @Ignore ("Certificate seems to have expired - needs replacing")
    public void sw13PortDescSecureTest() {
        print(EOL + "sw13PortDescSecureTest()");
        watch = new StopWatch("sw13PortDescSecureTest");
        sw = new MockOpenflowSwitch(SW13_PATH, showOutput);
        sw.setSecure(DEVICE_KS_FILE, STORE_PASS);
        print("instantiated mock switch:");
        print(sw.toDebugString());

        set13ExpectedValues();
        handshakeLatch = new CountDownLatch(1);
        sw.activate();
        waitForHandshake();

        // now send a multipart request for port description
        messageLatch = new CountDownLatch(1);
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(MultipartType.PORT_DESC);
        secureMsgHandler.send(req.toImmutable());
        waitForMessages();

        assertEquals(AM_NEQ, MULTIPART_REPLY, lastMsgReceived.getType());
        OfmMultipartReply rep = (OfmMultipartReply) lastMsgReceived;
        assertEquals(AM_NEQ, MultipartType.PORT_DESC, rep.getMultipartType());
        MBodyPortDesc.Array array = (MBodyPortDesc.Array) rep.getBody();
        List<MBodyPortDesc> ports = array.getList();
        assertEquals(AM_UXS, 32, ports.size());
        Port p = ports.get(2).getPort();
        assertEquals(AM_NEQ, mac("00:16:b9:0d:01:02"), p.getHwAddress());

        print(watch.stop());
    }

    @Test @Ignore ("Certificate seems to have expired - needs replacing")
    public void sw13DescSecureTest() {
        print(EOL + "sw13DescSecureTest()");
        watch = new StopWatch("sw13DescSecureTest");
        sw = new MockOpenflowSwitch(SW13_PATH, showOutput);
        sw.setSecure(DEVICE_KS_FILE, STORE_PASS);
        print("instantiated mock switch:");
        print(sw.toDebugString());

        set13ExpectedValues();
        handshakeLatch = new CountDownLatch(1);
        sw.activate();
        waitForHandshake();

        // now send a multipart request for description
        messageLatch = new CountDownLatch(1);
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(V_1_3, MULTIPART_REQUEST);
        req.type(MultipartType.DESC);
        secureMsgHandler.send(req.toImmutable());
        waitForMessages();

        assertEquals(AM_NEQ, MULTIPART_REPLY, lastMsgReceived.getType());
        OfmMultipartReply rep = (OfmMultipartReply) lastMsgReceived;
        assertEquals(AM_NEQ, MultipartType.DESC, rep.getMultipartType());
        MBodyDesc desc = (MBodyDesc) rep.getBody();
        assertEquals(AM_NEQ, "Acme Switch Co.", desc.getMfrDesc());
        assertEquals(AM_NEQ, "RoadRunner 9000", desc.getHwDesc());
        assertEquals(AM_NEQ, "OF-BeepBeep 1.6.0", desc.getSwDesc());
        assertEquals(AM_NEQ, "WB-11954-TAF", desc.getSerialNum());
        assertEquals(AM_NEQ, "9000 in Secret Hideout, Somewhere Safe",
                desc.getDpDesc());

        print(watch.stop());
    }
}
