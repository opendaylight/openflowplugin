/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;


import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.opendaylight.of.lib.IncompleteMessageException;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.err.ECodeHelloFailed;
import org.opendaylight.of.lib.err.ErrorCode;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.mp.MBodyPortDesc;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.MacRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.err.ErrorType.HELLO_FAILED;
import static org.opendaylight.of.lib.mp.MultipartType.PORT_DESC;
import static org.opendaylight.of.lib.mp.MultipartType.TABLE_FEATURES;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;

/**
 * Represents a mock switch that talks OpenFlow; useful for
 * OpenFlow Controller and Application unit testing.
 * <p>
 * The mock switch is configured via a text-based definition file. Blank lines
 * and lines beginning with "#" are considered comments and thus ignored by
 * the parser. The remaining lines take the form:
 * <pre>
 * keyword: set of parameters
 * </pre>
 * Long parameter lists may be extended over multiple lines by the use of
 * trailing backslashes:
 * <pre>
 * keyword: this parameter list is too long \
 *          to comfortably fit \
 *          on a single line
 * </pre>
 * Specific keywords may also be <em>indexable</em>, by which we mean that
 * entries may take the form:
 * <pre>
 * keyword[i]: parameters
 * </pre>
 * where <em>i</em> is a zero-based index.
 * <p>
 * In addition to the defined set of keywords, a set of arbitrary custom
 * properties may also be defined. These properties are minimally parsed, and
 * made available via {@link org.opendaylight.of.mockswitch.SwitchDefn#getCustomProps()}. This
 * allows for subclasses of {@code MockOpenflowSwitch} to use the definition
 * file as a source of their own specialized configuration. For example:
 * <pre>
 *     public class MySwitch extends MockOpenflowSwitch {
 *         public MySwitch(String defnPath) {
 *             super(defnPath);
 *             configure();
 *         }
 *         private void configure() {
 *             for (SwitchDefn.CustomProp prop: getDefn().getCustomProps()) {
 *                 String name = prop.name();
 *                 String params = prop.value();
 *                 // ... do something with the property
 *             }
 *         }
 *     }
 * </pre>
 * <p>
 * An example definition file might look like this:
 * <pre>
 * ## Simple OpenFlow 1.0 Mock Switch (4 ports) Definition file
 *
 * ### Define the switch's reported datapath ID
 * dpid: 42/0016b9:006502
 *
 * ### Define the switch's HELLO message
 * #   LAZY = wait for controller's HELLO msg; EAGER = send HELLO immediately
 * #   space-separated list of versions (for bitmap)
 * #   optional LEGACY token suppresses the version-bitmap hello-element
 * hello: LAZY V_1_0 LEGACY
 *
 * ### Define the switch's base configuration
 * controller: 127.0.0.1 6633
 * buffers: 256
 * tables: 12
 * capabilities: FLOW_STATS TABLE_STATS PORT_STATS IP_REASM QUEUE_STATS
 *
 * ### 1.0 switch supported actions
 * supportedActions: OUTPUT SET_VLAN_VID STRIP_VLAN \
 *                   SET_DL_SRC SET_DL_DST SET_NW_SRC SET_NW_DST
 *
 * ### Number of ports on this switch (256 max)
 * portCount: 4
 * ### MAC address of each port (last byte is 0..portCount-1)
 * portMac: 00:16:b9:0a:01:*
 *
 * ### Default port config and features - these values will be used to
 * #   configure each port that is not overridden with an indexed entry.
 * portConfig:
 * portFeature: RATE_100MB_FD COPPER
 *
 * ### Specific port overrides (index must be 0..3 for this 4-port switch)
 * portConfig[1]: PORT_DOWN
 * portConfig[3]: NO_FLOOD NO_FWD
 *
 * ### Description strings for MP/DESC reply
 * descMfr:    Acme Switch Co.
 * descHw:     Coyote WB10
 * descSw:     OF-Sneaky 1.0
 * descSerial: WB-11937-TAF
 * descDp:     WB10 at top of Mesa, Top Shelf
 *
 * ### Define some custom properties...
 * [custom]
 * foo: value for FOO
 * bar: value for BAR
 * </pre>
 * Note that everything after the <em>[custom]</em> line is interpreted as
 * a custom property.
 * <p>
 * Some code to instantiate and activate a mock switch:
 * <pre>
 *     MockOpenflowSwitch mockswitch = new MockOpenflowSwitch("path/to/switch.def");
 *     DataPathId dpid = mockswitch.getDpid();
 *     // when we are ready for the switch to connect to the controller...
 *     mockswitch.activate();
 * </pre>
 * <p>
 * Right out of the box, a mock openflow switch will perform the handshake
 * and version negotiation with the controller, (including consuming
 * <em>Error/HELLO_FAILED</em> messages). It will also respond to
 * <em>Multipart/DESC</em> requests,  
 * <em>Multipart/PORT_DESC</em> requests (v1.3 only), and (minimally)
 * <em>Multipart/TABLE_FEATURES</em> requests (v1.3 only), from the controller.
 * All other messages from the controller are passed to
 * {@link #msgRx(OpenflowMessage)} which has the default behavior of throwing
 * a runtime exception announcing that an "unexpected message" was received
 * from the controller.
 * <p>
 * For the mock switch to do anything more useful than handshake and provide
 * a description of itself, this class must be subclassed and the
 * {@link #msgRx(OpenflowMessage)} method overridden. For example, to program
 * the mock switch to respond to a <em>Multipart/FLOW</em> request, one
 * might use code like this:
 * <pre>
 *     &#64;Override
 *     protected void msgRx(OpenflowMessage msg) {
 *         switch (msg.getType()) {
 *             case: MULTIPART_REQUEST:
 *                 if (!handleMpRequest((OfmMultipartRequest) msg)
 *                     super.msgRx(msg);
 *                 break;
 *             default:
 *                 super.msgRx(msg);
 *                 break;
 *         }
 *     }
 *
 *     private boolean handleMpRequest(OfmMultipartRequest request) {
 *         switch (request.getMultipartType()) {
 *             case FLOW:
 *                 send(createMpFlowStatsReply(request));
 *                 return true;
 *         }
 *         // message was not handled
 *         return false;
 *     }
 *
 *    private OpenflowMessage createMpFlowStatsReply(OpenflowMessage req) 
 *            throws IncompleteStructureException {
 *        OfmMutableMultipartReply reply = (OfmMutableMultipartReply) 
 *                MessageFactory.create(req, MessageType.MULTIPART_REPLY, 
 *                        MultipartType.FLOW);
 *        MBodyFlowStats.MutableArray array = 
 *                (MBodyFlowStats.MutableArray) reply.getBody();
 *
 *        MBodyMutableFlowStats fs = (MBodyMutableFlowStats) 
 *                MpBodyFactory.createReplyBodyElement(req.getVersion(), 
 *                        MultipartType.FLOW);
 *        // Note: not advocating "magic" constants - for instruction only...
 *        Match flowMatch = .... ;
 *        fs.tableId(TableId.valueOf(0)).match(flowMatch)
 *                .packetCount(1000).duration(30, 123000); // etc.
 *        array.addFlowStats(fs);
 *        // create and add further flow stats elements.. if required
 *
 *        reply.body((MultipartBody) array.toImmutable());
 *        return reply.toImmutable();
 *    }
 * </pre>
 *
 * @see MockSwitchBank
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Sudheer Duggisetty
 */
public class MockOpenflowSwitch implements Comparable<MockOpenflowSwitch> {

    private static final String MSG_ACTIVATE = "Activate {}";
    private static final String MSG_DEACTIVATE = "Deactivate {} successful:{}";
    private static final String MSG_NEGOTIATED = "Negotiated Version: {}";
    private static final String MSG_HANDSHAKE_COMPLETE = "Handshake complete";

    private static final String E_NO_ECHO = "Unexpected EchoReply: ";
    private static final String E_UNEX_MSG = "Unexpected message from controller: ";
    private static final String E_BAD_PORT_NUM = "Invalid port number: ";
    private static final String E_INTERRUPTED_WAIT = "Interrupted from wait";
    private static final String E_TIMEOUT_WAIT = "Wait Timed-Out: ";
    private static final String E_UNEX_HANDSHAKE_RESULT = "Unexpected handshake result: success = ";
    private static final String E_NO_SPEAKIE_DER_LANGUAGE = "Parlez vous Flow-der-Open?";

    protected static final String E_PROG_ERR = "Fix programming error";

    // anything more than 2 seconds is too long
    private static final int MAX_WAIT_MS = 2000;

    private final Logger log = LoggerFactory.getLogger(MockOpenflowSwitch.class);

    /** Cache of outgoing messages (from switch to controller). */
    private final Map<MessageType, OpenflowMessage> outgoingMsgCache =
            new HashMap<>();

    /** For now, we are only modeling a main connection (auxId == 0).
     *  Later we might add auxiliary connections.
     */
    final SwConnection mainConn = new SwConnection();

    volatile CountDownLatch handshakeLatch;
    volatile boolean handshakeSuccessful;
    private volatile boolean handshakeCompleted = false;

    /** Our parsed definition. */
    final SwitchDefn defn;

    /** Whether we should be writing stuff to STDOUT. */
    boolean showOutput;

    // convenience references to the configuration modules
    protected DataPathId dpid;  // non-final so subclasses can change it
    final CfgHello cfgHello;
    final CfgBase cfgBase;
    final CfgFeat cfgFeat;
    final CfgDesc cfgDesc;

    final List<Port> ports;

    private final List<String> failedAssertions = new ArrayList<>();
    private boolean secure = false;
    private String keyStoreName;
    private String keyStorePass;

    /** The protocol version that we negotiated with the controller. */
    protected ProtocolVersion negotiated;

    /** 
     * Constructs a mock-switch configured from the specified
     * text-based definition file.
     *
     * @param path the switch definition file path
     * @param showOutput true if output is enabled
     */
    public MockOpenflowSwitch(String path, boolean showOutput) {
        this.showOutput = showOutput;
        // read the switch definition file
        defn = new SwitchDefn(path);
        // dereference the various configuration components
        dpid = defn.getDpid();
        cfgHello = defn.getCfgHello();
        cfgBase = defn.getCfgBase();
        cfgFeat = defn.getCfgFeat();
        cfgDesc = defn.getCfgDesc();
        // generate an in-memory model of the ports
        ports = createPorts(cfgHello.getMaxVersion());
    }

    /**
     * Sets the flag to determine whether we output stuff.
     *
     * @param show true to show output; false to suppress output
     */
    public void showOutput(boolean show) {
        showOutput = show;
    }

    /** 
     * Sets the switch to secure mode (uses TLS connection) using the given
     * (combined truststore/keystore) keystore parameters.
     *
     * @param keyStoreName the keystore filename with relative path
     * @param keyStorePass the keystore password
     */
    public void setSecure(String keyStoreName, String keyStorePass) {
        this.keyStoreName = keyStoreName;
        this.keyStorePass = keyStorePass;
        this.secure = true;
    }

    /** 
     * Returns the keystore name.
     *
     * @return the keystore name
     */
    public String getKeystoreName() {
        return keyStoreName;
    }

    /** 
     * Returns the keystore password.
     *
     * @return the keystore password
     */
    public String getKeystorePass() {
        return keyStorePass;
    }

    /** 
     * Returns the switch definition.
     *
     * @return the switch definition
     */
    public SwitchDefn getDefn() {
        return defn;
    }

    /** 
     * Convenience method that returns the datapath ID as configured in
     * the switch definition.
     *
     * @return the configured datapath id
     */
    public DataPathId getDpid() {
        return dpid;
    }

    //======================================================================
    // NETTY Configuration for setting up our main network channel

    // TCP or TLS
    private Executor boss = newSingleThreadExecutor(namedThreads("MockSwBoss"));
    private Executor worker = newSingleThreadExecutor(namedThreads("MockSwWorker"));
    private ChannelFactory channelFactory;
    private ChannelPipelineFactory pipelineFactory;
    private ClientBootstrap boot = new ClientBootstrap();
    private SwMessageHandler msgHandler = createSwMessageHandler();
    private final Object channelLock = new Object();
    private volatile Channel channel;

    /** Instructs the mock-switch to connect to the controller. */
    public void activate() {
        handshakeLatch = new CountDownLatch(1);
        if (secure)
            initSecureServer();
        else
            initTcpServer();
    }

    /**
     * Returns the message handler to use for this mock switch.
     *
     * @return the message handler
     */
    protected SwMessageHandler createSwMessageHandler() {
        return new SwMessageHandler(this);
    }

    /**
     * Returns the channel pipeline factory to use for this mock switch.
     * This default implementation returns a standard pipeline factory that
     * encodes and decodes openflow messages.
     *
     * @return the pipeline factory
     */
    protected PipelineFactory createPipelineFactory() {
        return new PipelineFactory();
    }

    private void initTcpServer() {
        log.debug(MSG_ACTIVATE, dpid);
        channelFactory = new OioClientSocketChannelFactory(boss);
        pipelineFactory = createPipelineFactory();
        ((PipelineFactory)pipelineFactory).setMsgHandler(msgHandler);
        boot.setOption("tcpNoDelay", true);
        // TODO: Review - other options required?
        boot.setFactory(channelFactory);
        boot.setPipelineFactory(pipelineFactory);

        InetSocketAddress sock = new InetSocketAddress(
                cfgBase.getControllerAddress().toInetAddress(),
                cfgBase.getOpenflowPort()
        );
        ChannelFuture future = boot.connect(sock);
        if (!future.awaitUninterruptibly(MAX_WAIT_MS)) // BLOCKS
            throw new RuntimeException(E_TIMEOUT_WAIT);
        channel = future.getChannel();
        if (cfgHello.getBehavior() == CfgHello.Behavior.EAGER)
            sendMyHello();
    }

    private void initSecureServer() {
        log.debug(MSG_ACTIVATE, dpid);
        channelFactory = new NioClientSocketChannelFactory(boss, worker);
        pipelineFactory = new TlsPipelineFactory(msgHandler, this);
        msgHandler.setSecureEnabled(secure);
        boot.setFactory(channelFactory);
        boot.setPipelineFactory(pipelineFactory);

        InetSocketAddress sock = new InetSocketAddress(
                cfgBase.getControllerAddress().toInetAddress(),
                cfgBase.getOpenflowTlsPort()
        );
        ChannelFuture future = boot.connect(sock);
        if (!future.awaitUninterruptibly(MAX_WAIT_MS)) // BLOCKS
            throw new RuntimeException(E_TIMEOUT_WAIT);
        channel = future.getChannel();
        if (cfgHello.getBehavior() == CfgHello.Behavior.EAGER)
            sendMyHello();
    }

    /** Instruct the mock-switch to disconnect from the controller. */
    public void deactivate() {
        msgHandler.shuttingDown();
        ChannelFuture future = channel.close();
        if (!future.awaitUninterruptibly(MAX_WAIT_MS)) // BLOCKS
            throw new RuntimeException(E_TIMEOUT_WAIT);
        log.debug(MSG_DEACTIVATE, dpid, future.isSuccess());
    }

    @Override
    public String toString() {
        return "{MockSw:" + dpid + "," + cfgHello.getMaxVersion() +
                ",#ports=" + cfgFeat.getPortCount() + "}";
    }

    /** 
     * Returns a multi-line string representation of this mock switch.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        sb.append(EOLI).append("Datapath ID: ").append(dpid)
          .append(EOLI).append("Hello      : ").append(cfgHello)
          .append(EOLI).append("Base       : ").append(cfgBase)
          .append(EOLI).append("Features   : ").append(cfgFeat)
          .append(EOLI).append("Description: ").append(cfgDesc)
                .append(EOLI);
        return sb.toString();
    }

    /** 
     * Returns true if this switch is connected to the controller; false
     * otherwise.
     *
     * @return true if connected to the controller
     */
    public boolean isActive() {
        return channel != null && channel.isConnected();
    }

    /** 
     * Sets the "state" of the specified port.
     *
     * @param portNum the port number (1-based)
     * @param state the state to which the port should be set
     */
    public void setPortState(int portNum, CmdPortStatus.State state) {
        if (portNum < 1 || portNum > ports.size())
            throw new IllegalArgumentException(E_BAD_PORT_NUM + portNum);
        final int idx = portNum - 1;

        MutablePort copy = PortFactory.mutableCopy(ports.get(idx));
        Set<PortConfig> configCopy = new TreeSet<>(copy.getConfig());
        switch (state) {
            case UP:
                configCopy.remove(PortConfig.PORT_DOWN);
                break;
            case DOWN:
                configCopy.add(PortConfig.PORT_DOWN);
                break;
        }
        copy.config(configCopy);
        ports.set(idx, (Port) copy.toImmutable());
    }

    /** 
     * Returns the specified port.
     *
     * @param portNum the port number (1-based)
     * @return the port
     */
    public Port getPort(int portNum) {
        return ports.get(portNum - 1);
    }

    /** 
     * A blocking call that returns once the handshake with the controller
     * has completed. If success is expected, and the handshake does not
     * complete successfully, an exception is thrown. Conversely, an exception
     * is thrown if success is <em>not</em> expected and the switch does not
     * issue an error response.
     *
     * @param successExpected true if expecting a successful handshake
     */
    public void waitForHandshake(boolean successExpected) {
        if (handshakeCompleted || handshakeLatch == null)
            return;
        try {
            if (!handshakeLatch.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS))
                stowAndThrow(E_TIMEOUT_WAIT + MAX_WAIT_MS);
            if (handshakeSuccessful != successExpected)
                stowAndThrow(E_UNEX_HANDSHAKE_RESULT + handshakeSuccessful);
            // we made it through....so now we just return
        } catch (InterruptedException e) {
            throw new IllegalStateException(E_INTERRUPTED_WAIT, e);
        }
    }

    /** 
     * The controller has told us that our hello has failed.
     *
     * @param err the error message from the controller
     */
    public void helloFailed(OfmError err) {
        // Do something with the error message??
        handshakeSuccessful = false;
        handshakeCompleted = true;
        if (handshakeLatch != null)
            handshakeLatch.countDown();
    }

    /** 
     * Check in our cache for the EchoRequest we sent, and validate against
     * the EchoReply just received from the controller.
     *
     * @param msg the echo reply message
     */
    public void reconcileEcho(OfmEchoReply msg) {
        OfmEchoRequest req =
                (OfmEchoRequest) outgoingMsgCache.remove(ECHO_REQUEST);

        if (req == null || req.getXid() != msg.getXid() ||
                !Arrays.equals(req.getData(), msg.getData()))
            stowAndThrow(E_NO_ECHO + msg);
    }

    // ===================================================================

    /** 
     * A countdown latch can be set that will count down for every
     * message handled by the SwMessageHandler, including internal messages
     * such as HELLO, FEATURES_REQUEST and ECHO_REPLY.
     *
     * @param latch the count down latch
     */
    public void setInternalMsgLatch(CountDownLatch latch) {
        msgHandler.setInternalMsgLatch(latch);
    }

    /** 
     * The command processor has asked us to cache this outgoing message
     * for validation later.
     *
     * @param messageType the message type used as a key
     * @param msg the message to cache
     */
    public void cache(MessageType messageType, OpenflowMessage msg) {
        outgoingMsgCache.put(messageType, msg);
    }

    /** 
     * Stows the error string in the failed assertions list, and throws
     * an IllegalStateException.
     *
     * @param error the error message
     */
    protected void stowAndThrow(String error) {
        failedAssertions.add(error);
        throw new IllegalStateException(error);
    }

    /** 
     * Returns any failed assertions that were collected during the lifetime
     * of the mock switch.
     *
     * @return the list of failed assertions
     */
    public List<String> failedAssertions() {
        return Collections.unmodifiableList(failedAssertions);
    }

    // ===================================================================

    /** Memo to maintain context between mock-switch and connection object. */
    static class Memo {
        private MockOpenflowSwitch sw;

        Memo(MockOpenflowSwitch sw) {
            this.sw = sw;
        }

        void negotiatedAs(ProtocolVersion pv) {
            sw.negotiatedAs(pv);
        }

        void handshakeComplete() {
            sw.handshakeComplete();
        }

        HelloMode helloMode() {
            return sw.helloMode;
        }
    }

    private void negotiatedAs(ProtocolVersion pv) {
        negotiated = pv;
        log.debug(MSG_NEGOTIATED, pv);

        if (negotiated == null &&
                helloMode == HelloMode.NOT_10_RETURN_OFM_ERROR) {
            sendHelloError(ECodeHelloFailed.INCOMPATIBLE,
                    E_NO_SPEAKIE_DER_LANGUAGE);
        }
    }

    private byte[] first64bytes(OpenflowMessage m) {
        byte[] bytes;
        try {
            bytes = MessageFactory.encodeMessage(m);
        } catch (IncompleteMessageException e) {
            throw new IllegalStateException(E_PROG_ERR, e);
        } catch (IncompleteStructureException e) {
            throw new IllegalStateException(E_PROG_ERR, e);
        }
        if (bytes.length > 64) {
            byte[] trunc = new byte[64];
            System.arraycopy(bytes, 0, trunc, 0, 64);
            bytes = trunc;
        }
        return bytes;
    }

    /** 
     * Send an error message back to the controller.
     *
     * @param et error type
     * @param ec error code
     * @param m the message that was the cause of the error
     */
    protected void sendError(ErrorType et, ErrorCode ec, OpenflowMessage m) {
        OfmMutableError err = (OfmMutableError) create(m, ERROR, et);
        err.errorCode(ec).setData(first64bytes(m));
        send(err.toImmutable());
    }


    /** 
     * Send a hello-failed error message back to the controller.
     *
     * @param ec the error code
     * @param message the message string
     */
    protected void sendHelloError(ECodeHelloFailed ec, String message) {
        ProtocolVersion pv = cfgHello.getMaxVersion();
        OfmMutableError err = (OfmMutableError) create(pv, ERROR, HELLO_FAILED);
        err.errorCode(ec).errorMessage(message);
        send(err.toImmutable());
    }

    private void handshakeComplete() {
        handshakeSuccessful = true;
        handshakeCompleted = true;
        if (handshakeLatch != null)
            handshakeLatch.countDown();
        log.debug(MSG_HANDSHAKE_COMPLETE);
    }

    // ===================================================================

    /** Sends our HELLO message to the controller. */
    void sendMyHello() {
        OfmHello hello = cfgHello.createHelloMsg();
        mainConn.outBoundHello(hello, new Memo(this));
        send(hello);
    }

    /** 
     * Sends a FEATURES_REPLY back to the controller.
     * Invoked by the message handler.
     * The version of the message is our configured version... hopefully
     * it matches the requested version, because we already told the
     * controller the version we speak (in our HELLO reply).
     *
     * @param msg the incoming FEATURES_REQUEST message from the controller
     * @throws IllegalStateException if invoked at the wrong time
     */
    void sendFeaturesReply(OfmFeaturesRequest msg) {
        OfmMutableFeaturesReply fr = (OfmMutableFeaturesReply)
                create(msg, FEATURES_REPLY);

        fr.dpid(dpid)
                .numBuffers(cfgBase.getBufferCount())
                .numTables(cfgBase.getTableCount())
                .capabilities(cfgBase.getCapabilities());
        if (negotiated.ge(V_1_3))
            fr.auxId(auxId());
        if (negotiated == V_1_0)
            fr.supportedActions(cfgFeat.getSuppActs());
        if (negotiated.lt(V_1_3))
            for (Port p: ports)
                fr.addPort(p);
        OfmFeaturesReply frep = (OfmFeaturesReply) fr.toImmutable();
        mainConn.outBoundFeatures(frep, new Memo(this));
        send(frep);
    }

    /** 
     * Returns the auxiliary connection ID to be returned in the features
     * reply. This default implementation returns 0, i.e. the main connection.
     *
     * @return the auxiliary ID
     */
    protected int auxId() {
        return 0;
    }

    /** 
     * Creates ports based on the {@link CfgFeat feature configuration}
     * information, using the protocol version specified in the
     * {@link CfgHello hello configuration}.
     *
     * @param pv the protocol version for this switch
     * @return the list of (mock) ports that this (mock) switch reports
     */
    protected List<Port> createPorts(ProtocolVersion pv) {
        final int nPorts = cfgFeat.getPortCount();
        List<Port> ports = new ArrayList<>(nPorts);

        Iterator<MacAddress> mi =
                MacRange.valueOf(cfgFeat.getPortMac()).iterator();

        for (int portIdx = 0; portIdx < nPorts; portIdx++) {
            MutablePort port = new MutablePort(pv);
            // NOTE: Switch Ports are numbered starting at 1.
            port.portNumber(BigPortNumber.valueOf(portIdx+1));
            port.hwAddress(mi.next());
            // TODO port friendly name (blank for now)
            port.config(cfgFeat.getPortConfig(portIdx));
            // TODO port state
//            port.state(cfgFeat.getPortState(portIdx));
            port.state(new HashSet<PortState>());
            port.current(cfgFeat.getPortFeatures(portIdx));
            // TODO: port advertised, supported, peer features (null for now)
            if (pv.ge(V_1_1)) {
                PortFeature rate = CfgFeat.pickRate(cfgFeat.getPortFeatures());
                port.currentSpeed(CfgFeat.pickCurrentSpeed(rate));
                port.maxSpeed(CfgFeat.pickMaxSpeed(rate));
            }
            ports.add((Port) port.toImmutable());
        }
        return ports;
    }

    /** 
     * Creates a multipart PORT_DESC reply from the configured data. The
     * request is passed in so we can replicate the transaction ID.
     *
     * @param request the request message
     * @return the multipart PORT_DESC reply
     * @throws VersionMismatchException if the negotiated version is &lt;1.3
     */
    protected OpenflowMessage createMpPortDescReply(OpenflowMessage request) {
        verMin13(negotiated);
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                create(request, MULTIPART_REPLY);

        MBodyPortDesc.MutableArray array = (MBodyPortDesc.MutableArray)
                MpBodyFactory.createReplyBody(rep.getVersion(), PORT_DESC);
        try {
            for (Port p: ports)
                array.addPort(p);
            rep.body((MultipartBody) array.toImmutable());
        } catch (IncompleteStructureException e) {
            throw new RuntimeException(e);
        }
        return rep.toImmutable();
    }

    /**
     * Creates a multipart DESC reply from the configured data. The request
     * is passed in so we can replicate the transaction ID.
     * 
     * @param request the request message
     * @return the multipart DESC reply
     */
    protected OpenflowMessage createMpDescReply(OpenflowMessage request) {
        return cfgDesc.createMpDescReply(request);
    }
    

    /** 
     * Creates a multipart TABLE_FEATURES reply (empty) to satisfy the basic
     * request from the controller (for 1.3 switches). 
     * Subclasses can override this method to provide a fuller reply.
     * 
     * @param request the request message
     * @return the multipart TABLE_FEATURES reply
     * @throws VersionMismatchException if the negotiated version is &lt;1.3
     */
    protected OpenflowMessage createMpTableFeaturesReply(OpenflowMessage request) {
        verMin13(negotiated);
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply) 
                create(request, MULTIPART_REPLY);
        MBodyTableFeatures.MutableArray array = (MBodyTableFeatures.MutableArray) 
                MpBodyFactory.createReplyBody(rep.getVersion(), TABLE_FEATURES);
        // NOTE: this is where one might add table feature entries to the array
        rep.body((MultipartBody) array.toImmutable());
        return rep.toImmutable();
    } 
        

    /**
     * Responds to multipart request for TABLE_FEATURES.
     * <p>
     * This default implementation sends a response created 
     * via {@link #createMpTableFeaturesReply}, and returns true to indicate
     * that the request was handled.
     * 
     * @param request the inbound request
     * @return true to indicate request was handled, false otherwise
     */
    protected boolean sendMpTableFeaturesReply(OfmMultipartRequest request) {
        send(createMpTableFeaturesReply(request));
        return true;
    }

    /**
     * Responds to multipart request for PORT_DESC.
     * <p>
     * This default implementation sends a response created 
     * via {@link #createMpPortDescReply}, and returns true to indicate
     * that the request was handled.
     *
     * @param request the inbound request
     * @return true to indicate request was handled, false otherwise
     */
    protected boolean sendMpPortDescReply(OfmMultipartRequest request) {
        send(createMpPortDescReply(request));
        return true;
    }

    /**
     * Responds to multipart request for DESC.
     * <p>
     * This default implementation sends a response created 
     * via {@link #createMpDescReply}, and returns true to indicate
     * that the request was handled.
     *
     * @param request the inbound request
     * @return true to indicate request was handled, false otherwise
     */
    protected boolean sendMpDescReply(OfmMultipartRequest request) {
        send(createMpDescReply(request));
        return true;
    }

    /**
     * Called by the message handler when a multipart request comes in.
     * If it is of type {@code DESC}, {@code PORT_DESC}, or 
     * {@code TABLE_FEATURES} we will handle it internally; otherwise we tell 
     * the handler to pass it through {@link #msgRx(OpenflowMessage)}.
     * <p>
     * Note, however, that for each handled type, we call an overridable
     * method:
     * <ul>
     *     <li> DESC -> {@link #sendMpDescReply}</li>
     *     <li> PORT_DESC -> {@link #sendMpPortDescReply}</li>
     *     <li> TABLE_FEATURES -> {@link #sendMpTableFeaturesReply}</li>
     * </ul>
     * These methods, or the corresponding "createMp...Reply" methods they
     * invoke, can be overridden to change the default behavior.
     * <p>
     * Alternatively, to route the request for one of the above multipart types 
     * through {@link #msgRx} instead, simply override the appropriate 
     * "sendMp...Reply" method to return false:
     * <pre>
     *     protected boolean sendMpDescReply(OpenflowMessage request) {
     *         return false;
     *     }
     * </pre>
     * 
     *
     * @param request the inbound request
     * @return true, if we handled the message; false otherwise
     */
    public boolean handleMultipartRequest(OfmMultipartRequest request) {
        switch (request.getMultipartType()) {
            case DESC:
                return sendMpDescReply(request);
            case PORT_DESC:
                return sendMpPortDescReply(request);
            case TABLE_FEATURES:
                return sendMpTableFeaturesReply(request);
        }
        // message was not handled internally
        return false;
    }

    // ===================================================================

    /** 
     * Sends the specified openflow message to the controller.
     *
     * @param msg the message to send.
     * @throws NullPointerException if msg is null
     * @throws IllegalArgumentException if the message is mutable
     */
    protected void send(OpenflowMessage msg) {
        notNull(msg);
        notMutable(msg);        
        synchronized (channelLock) {
            // TODO: verify that isConnected is correct (isOpen? isWritable?)
            if (channel != null && channel.isConnected()) {
                if (showOutput)
                    print(">>> mockswitch.send >>>> " + msg);
                channel.write(msg);
            }
        }
    }

    /** 
     * Invoked when a message is received from the controller.
     * <p>
     * More precisely, messages that are <u>not</u>:
     * <ul>
     *     <li>Type HELLO</li>
     *     <li>Type ERROR with subtype HELLO_FAILED</li>
     *     <li>Type FEATURES_REQUEST</li>
     *     <li>Type SET_CONFIG</li>
     *     <li>Type MULTIPART_REQUEST with subtype DESC</li>
     *     <li>Type MULTIPART_REQUEST with subtype PORT_DESC</li>
     *     <li>Type MULTIPART_REQUEST with subtype TABLE_FEATURES</li>
     * </ul>
     * <p>
     * The above messages are handled internally and do not get passed
     * to this method. However, note that the default behavior for multipart
     * requests can be overridden; see {@link #handleMultipartRequest}.
     * <p>
     * This default implementation throws a runtime exception. It is
     * expected that subclasses will override this method to handle
     * the messages arriving from the controller, to provide the required
     * mock-switch behavior.
     *
     * @param msg the message from the controller
     */
    protected void msgRx(OpenflowMessage msg) {
        throw new RuntimeException(E_UNEX_MSG + msg);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockOpenflowSwitch that = (MockOpenflowSwitch) o;
        return cfgFeat.equals(that.cfgFeat) && dpid.equals(that.dpid);
    }

    @Override
    public int hashCode() {
        return 31 * dpid.hashCode() + cfgFeat.hashCode();
    }

    @Override
    public int compareTo(MockOpenflowSwitch o) {
        return this.dpid.compareTo(o.dpid);
    }

    /** 
     * Prints the given string, if output is enabled.
     *
     * @param s the string to print
     */
    private void print(String s) {
        if (showOutput)
            System.out.println(s);
    }

    // ========================================================================
    // additional stuff to alter Handshake behavior
    HelloMode helloMode = HelloMode.DEFAULT;

    /** 
     * Sets the hello mode for this switch.
     *
     * @param mode the mode
     */
    public void setHelloMode(HelloMode mode) {
        helloMode = mode;
    }

    /** Designates the alternative ways to respond to handshaking. */
    public static enum HelloMode {
        /** The default behavior. */
        DEFAULT,
        /** Return an error and disconnect. */
        NOT_10_RETURN_OFM_ERROR,
        /** Do nothing (don't disconnect). */
        NOT_8_BYTES_NO_RESPONSE,
    }
}
