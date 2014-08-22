/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.common.MessageSink;
import org.opendaylight.of.controller.*;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.dt.DataPathUtils;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MBodyPortDesc;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.nbio.SecureContextFactory;
import org.opendaylight.util.net.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.opendaylight.of.controller.OpenflowEventType.*;
import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.NONE;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_IN;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_OUT;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static org.opendaylight.util.ResourceUtils.getBundledResource;

/**
 * An SDN OpenFlow Controller.
 * <p>
 * Responsible for listening on the OpenFlow port for connections from
 * OpenFlow datapaths, performing the handshake, maintaining
 * connectivity and providing a mapping between {@link DataPathId} and
 * communications channel.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Thomas Vachuska
 * @author Frank Wood
 * @author Sudheer Duggisetty
 */
class OpenflowController {

    // TODO: Review synchronization scope and consider use of private object locks instead.

    private Logger log = LoggerFactory.getLogger(OpenflowController.class);

    private static final ResourceBundle RES = 
            getBundledResource(OpenflowController.class, "openflowController");

    static final String E_BAD_VERSION = RES.getString("e_bad_version");

    private static final String E_NO_DP = RES.getString("e_no_dp");
    private static final String E_IDLE_CONNECTION =
            RES.getString("e_idle_connection");
    private static final String E_FUTURE_TIMEOUT =
            RES.getString("e_future_timeout");
    private static final String E_IO_LOOPS = RES.getString("e_io_loops");
    private static final String E_FINISH_FAILED =
            RES.getString("e_finish_failed");
    private static final String E_START_FAILED =
            RES.getString("e_start_failed");
    private static final String E_FUTURE_DP = RES.getString("e_future_dp");
    private static final String E_FUTURE_XID = RES.getString("e_future_xid");
    private static final String E_CLOSE_WHEN_DPID_NULL =
            RES.getString("e_close_when_dpid_null");
    private static final String E_TLS_NOT_ENABLED =
            RES.getString("e_tls_not_enabled");
    private static final String E_NO_LISTEN_PORTS =
            RES.getString("e_no_listen_ports");

    private static final String MSG_STARTED_LISTENING =
            RES.getString("msg_started_listening");
    private static final String MSG_STOPPED_LISTENING =
            RES.getString("msg_stopped_listening");
    private static final String MSG_AGED_OUT_FUTURES =
            RES.getString("msg_aged_out_futures");

    private static final String E_NO_MAIN_CONN =
            "No main connection established for dpid {} (auxId={})";
    private static final String E_EXISTING_MAIN_CONN =
            "Main connection already established for dpid {}";

    private static final String CKPT_DP_EXT_HS_DONE =
            "Datapath {} -- Extended handshake complete";

    private static final String I_DPID_TYPED = "Device Type for {} set to [{}]";

    // if a listen port is set to zero, it means don't listen
    private static final int DO_NOT_LISTEN = 0;
    private static final long SHUTDOWN_GRACE_MS = 1000;
    private static final int PENDING_FUTURE_CHECK_MS = 120000;
    // Aux ID == 0 is the main connection to the switch.
    private static final int MAIN_ID = 0;
    
    private static final IllegalStateException EXCEPT_FUTURE_TIMEOUT =
            new IllegalStateException(E_FUTURE_TIMEOUT);
    
    // Loop and executor for accepting TCP/TLS/UDP connections
    private ConnectionAcceptLoop acceptLoop;
    private ExecutorService boss;

    // Loops & executors for processing OpenFlow message I/O
    private final List<OpenflowIOLoop> ioLoops = new ArrayList<>();
    private ExecutorService workers;
    private final int workerCount;

    // Index of the most recently used I/O worker; for round-robin assignment
    // TODO: Use even-load assignment instead of round-robin
    private int lastWorker = -1;

    // Is the I/O processing active
    private volatile boolean ioRunning = false;

    private final MessageSink sink;
    private final AlertSink as;
    private final PortStateTracker portTracker;
    private final PostHandshakeSink phSink;
    private final FlowModAdvisor fmAdvisor;

    // TODO: eventualy these will be a single port (6653) per IANA
    private final int openflowPort;
    private final int openflowTlsPort;
//    private final int openflowUdpPort;
    private boolean noListenPortsAreEnabled = false;
    private SocketAddress[] addresses;

    private final SecureContextFactory secureCtxFactory;

    // Suppress sending a SetConfig message to new datapath connections?
    private boolean suppressSetConfig = false;

    // Holds hand-shake message trace events that require additional
    // information to be injected after hand-shake completes.
    // Does not require to be concurrent as it is guarded by synchronize.
    private final Set<HandshakeMessageEvt.DpidFuture> pendingDpids = 
            new HashSet<>();

    // An unbounded queue to hold all intercepted OpenFlow message events
    // This should be safe given that the requests to records are time-bound.
    private final BlockingQueue<MessageEvent> txRxMsgEvents =
            new LinkedBlockingQueue<>();
    private final TxRxCtrl txRxCtrl = new TxRxCtrl(txRxMsgEvents);

    // Cache of dpid-to-dpinfo bindings; access to this cache is synchronized
    private final Map<DataPathId, DpInfo> infoCache = new ConcurrentHashMap<>();


    // Packet handling statistics
    private final CtrlStats stats;

    // DpUtil instance for accessing the datapath ID memento
    private final DpUtil dpUtil = new DpUtil();

    // Get the handle on the openflow connection configuration.
    private OpenflowConnection.Config config = OpenflowConnection.getConfig();

    // Provisional implementation of idle-connection detection
    // TODO: Replace with some form of hashed-wheel timer
    private Timer idleConnectionTimer;

    // Pending future age-out timer.
    private Timer pendingFutureTimer;

    // Unit test support
    private static boolean idleDetectionEnabled = true;

    private PostHandshakeCallback phCallback;

    /**
     * Constructs a controller with the given configuration.
     * We are also given references to:
     * <ul>
     *     <li>
     *         the port state tracker - to be injected into DpInfo objects
     *     </li>
     *     <li>
     *         the message sink - where to post incoming openflow messages
     *     </li>
     *     <li>
     *         the alert sink - where to post alerts
     *     </li>
     *     <li>
     *         the post-handshake sink - where to invoke post-handshake
     *          processing
     *     </li>
     *     <li>
     *         the post-handshake completion callback - where to call once
     *          a post-handshake task has completed
     *     </li>
     *     <li>
     *         the flow mod advisor - where to get default flows, and how
     *          to adjust flows (via the device driver subsystem behind
     *          the scenes)
     *     </li>
     * </ul>
     *
     * @param cfg the configuration parameters
     * @param portTracker the port state tracker
     * @param sink post incoming openflow messages here
     * @param as post alerts here
     * @param ph invoke post-handshake processing here
     * @param phc post-handshake completion callback
     * @param fma invoke flow mod advising here
     */
    OpenflowController(ControllerConfig cfg, PortStateTracker portTracker,
                       MessageSink sink, AlertSink as, PostHandshakeSink ph,
                       PostHandshakeCallback phc, FlowModAdvisor fma) {
        this.portTracker = portTracker;
        this.sink = sink;
        this.as = as;
        this.phSink = ph;
        this.phCallback = phc;
        this.fmAdvisor = fma;

        stats = new CtrlStats();

        workerCount = cfg.workerCount();
        openflowPort = cfg.listenPort();
        openflowTlsPort = cfg.tlsListenPort();
//        openflowUdpPort = cfg.udpPort();

        config.idleCheckMs = cfg.idleCheckMs();

        // TODO: move check for errors in secure context factory to cfg.
        secureCtxFactory = new SecureContextFactory(cfg.securityContext());
        if (secureCtxFactory.hasErrors()) {
            for (Exception e: secureCtxFactory.getErrors())
                alertCritical(e.toString());
            alertCritical(E_TLS_NOT_ENABLED);
        }

        updateNonBounceConfig(cfg);

        addresses = makeAddressArray(openflowPort, openflowTlsPort);
        if (addresses.length == 0) {
            noListenPortsAreEnabled = true;
            log.error(E_NO_LISTEN_PORTS);
            return;
        }

        try {
            acceptLoop = new ConnectionAcceptLoop(this, addresses);
            acceptLoop.setSslContext(secureCtxFactory.secureContext());
            acceptLoop.setSecureAddress(openflowTlsPort);

            // TODO: Should receive/send buffer sizes be separate ?
            acceptLoop.receiveBufferSize = cfg.rcvBufSize();
            acceptLoop.sendBufferSize = cfg.rcvBufSize();
            // TODO: re-instate UDP
//            acceptLoop.udpRcvBufSize = cfg.udpRcvBufSize();

            // Create I/O loops and the host worker threads.
            for (int i = 0; i < workerCount; i++)
                ioLoops.add(new OpenflowIOLoop(this));

        } catch (IOException e) {
            log.error(E_IO_LOOPS, e);
            throw new RuntimeException(E_IO_LOOPS, e);
        }
    }

    private SocketAddress[] makeAddressArray(int nonSecure, int secure) {
        return validateAddresses(createSocketAddress(nonSecure),
                                 createSocketAddress(secure));
    }

    private InetSocketAddress createSocketAddress(int port) {
        return port == DO_NOT_LISTEN ? null : new InetSocketAddress(port);
    }

    private SocketAddress[] validateAddresses(SocketAddress address,
                                              SocketAddress tlsAddress) {
        List<SocketAddress> addrs = new ArrayList<>();
        if (address != null)
            addrs.add(address);
        if (tlsAddress != null)
            addrs.add(tlsAddress);
        return addrs.toArray(new SocketAddress[addrs.size()]);
    }

    /**
     * Update configuration parameters.
     *
     * @param cfg the new configuration
     */
    void updateNonBounceConfig(ControllerConfig cfg) {
        MessageFactory.setStrictMessageParsing(cfg.strictMessageParsing());
        suppressSetConfig = cfg.suppressSetConfig();
        config.maxIdleMs = cfg.maxIdleMs();
        config.maxEchoMs = cfg.maxEchoMs();
        config.maxEchoAttempts = cfg.maxEchoAttempts();
    }

    // post a Critical severity alert to the outside world
    private void alertCritical(String msg) {
        as.postAlert(new DefaultAlert(Alert.Severity.CRITICAL, msg));
    }

    //===================================================================
    // these methods are invoked by the ControllerManager

    void initAndStartListening() {
        init();
        startIOProcessing();
    }

    void init() {
        // TODO: Add back key/trust store stuff for TLS
    }

    int getOpenflowPort() {
        return openflowPort;
    }

    /**
     * Starts up the I/O loops and opens up the listening ports to allow
     * incoming connections.
     */
    synchronized void startIOProcessing() {
        if (!ioRunning) {
            if (noListenPortsAreEnabled) {
                log.warn(E_NO_LISTEN_PORTS);
                return;
            }


            ioRunning = true;

            // Create the thread-pools to host the I/O loops.
            boss = newFixedThreadPool(1, namedThreads("of-accept"));
            workers = newFixedThreadPool(workerCount, namedThreads("of-io"));

            // Submit the workers and then the boss
            for (OpenflowIOLoop loop : ioLoops)
                workers.submit(loop);
            boss.submit(acceptLoop);

            if (openflowPort != DO_NOT_LISTEN)
                log.info("TCP non-secure bound to port: {}", openflowPort);
            if (openflowTlsPort != DO_NOT_LISTEN)
                log.info("TCP secure (TLS) bound to port: {}", openflowTlsPort);
            // FIXME: Add back UDP support

            pendingFutureTimer = new Timer("of-pend-future-timer");
            pendingFutureTimer.schedule(new PendingFutureSweeper(),
                    PENDING_FUTURE_CHECK_MS, PENDING_FUTURE_CHECK_MS);
            
            idleConnectionTimer = new Timer("of-idle-timer", true);
            if (idleDetectionEnabled)
                idleConnectionTimer.schedule(new IdleSweeper(),
                    config.idleCheckMs, config.idleCheckMs);

            waitForLoopsToStart();
            log.info(MSG_STARTED_LISTENING, Arrays.toString(addresses));
        }
    }

    /**
     * Stops all I/O loops and stops listening on ports.
     */
    synchronized void stopIOProcessing() {
        if (noListenPortsAreEnabled || !ioRunning)
            return;

        idleConnectionTimer.cancel();
        pendingFutureTimer.cancel();
        
        closeAllConnections();
        terminateIOLoops();
        ioRunning = false;
        log.info(MSG_STOPPED_LISTENING, Arrays.toString(addresses));
    }

    // Closes all open connections.
    private void closeAllConnections() {
        for (DpInfo info : infoCache.values())
            // Close just the main connection and let the handler close
            // any aux connections as a result.
            info.mainConn.close();
    }

    // Terminates all I/O loops.
    private void terminateIOLoops() {
        // Send cease and desist message to all I/O loops first.
        acceptLoop.cease();
        for (OpenflowIOLoop loop : ioLoops)
            loop.cease();

        waitForLoopsToFinish();

        // Shutdown the executors.
        boss.shutdown();
        workers.shutdown();
    }

    // Waits for the I/O loops to finish execution
    private void waitForLoopsToFinish() {
        boolean ok = acceptLoop.waitForFinish(SHUTDOWN_GRACE_MS);
        for (OpenflowIOLoop l : ioLoops)
            ok = ok && l.waitForFinish(SHUTDOWN_GRACE_MS);
        if (!ok)
            log.warn(E_FINISH_FAILED);
    }

    // Waits for the I/O loops to start execution
    private void waitForLoopsToStart() {
        boolean ok = true;
        for (OpenflowIOLoop l : ioLoops)
            ok = ok && l.waitForStart(SHUTDOWN_GRACE_MS);
        ok = ok && acceptLoop.waitForStart(SHUTDOWN_GRACE_MS);
        if (!ok)
            log.warn(E_START_FAILED);
    }

    /**
     * Requests graceful shutdown of the controller.
     */
    void shutdown() {
        stopIOProcessing();
    }

    /**
     * Sets the internal flag that indicates whether the default behavior of
     * sending a <em>SetConfig</em> message to a newly connected datapath
     * should be suppressed.
     *
     * @param suppress true to suppress default behavior
     */
    void suppressSetConfig(boolean suppress) {
        suppressSetConfig = suppress;
    }

    /**
     * Returns a snapshot of the controller statistics.
     *
     * @return a statistics snapshot
     */
    ControllerStats getStats() {
        return stats.snapshot();
    }

    /**
     * Returns a set of DpInfoSnapshot instances.
     *
     * @return the set of datapath info snapshots
     */
    Set<DataPathInfo> getAllDataPathInfo() {
        // Note: use a tree-set for maintaining sort order
        Set<DataPathInfo> allInfo = new TreeSet<>();
        for (DpInfo info: infoCache.values())
            allInfo.add(info.snapshot());
        return allInfo;
    }

    /**
     * Returns a DpInfo descriptor for the given datapath ID.
     *
     * @param dpid the datapath ID
     * @return a datapath info attachment; null if datapath id not found
     */
    DpInfo getDpInfo(DataPathId dpid) {
        // Inspect the dpid memento; if it is present and valid, return it
        DpInfo info = dpUtil.getMemento(dpid);
        if (info != null && info.isValid() && info.controller == this)
            return info;

        // Otherwise, look in the cache
        info = infoCache.get(dpid);
        if (info != null)
            dpUtil.attachMemento(dpid, info);
        else
            dpUtil.detachMemento(dpid);
        return info;
    }

    /**
     * Returns a DpInfo descriptor for the given datapath ID or throws a
     * {@link OpenflowException} if non exist.
     *
     * @param dpid the datapath ID
     * @return a datapath info attachment
     * @throws OpenflowException if no DpInfo descriptor exists
     */
    DpInfo getDpInfoNotNull(DataPathId dpid) throws OpenflowException {
        DpInfo info = getDpInfo(dpid);
        if (info == null)
            throw new OpenflowException(E_NO_DP + dpid);
        return info;
    }

    /**
     * Returns datapath info snapshot for the given datapath ID.
     *
     * @param dpid the datapath ID
     * @return a datapath info snapshot; null if datapath id not found
     */
    DataPathInfo getDataPathInfo(DataPathId dpid) {
        DpInfo info = getDpInfo(dpid);
        return info == null ? null : info.snapshot();
    }

    /**
     * Returns the negotiated protocol version for the given datapath, or
     * null if no such datapath is connected.
     *
     * @param dpid the datapath ID
     * @return the negotiated version
     */
    ProtocolVersion versionOf(DataPathId dpid) {
        DpInfo info = getDpInfo(dpid);
        return info == null ? null : info.negotiated;
    }

    /**
     * Returns a set of DpDetailSnapshot instances.
     *
     * @return the set of datapath detail snapshots
     */
    Set<DataPathDetails> getAllDataPathDetails() {
        // Note: use a tree-set for maintaining sort order
        Set<DataPathDetails> allInfo = new TreeSet<>();
        for (DpInfo info: infoCache.values())
            allInfo.add(info.detailedSnapshot());
        return allInfo;
    }

    /**
     * Returns a DpDetailSnapshot for the given datapath ID.
     *
     * @param dpid the datapath id
     * @return a datapath detail snapshot; null if datapath id not found
     */
    DataPathDetails getDataPathDetails(DataPathId dpid) {
        DpInfo info = getDpInfo(dpid);
        return info == null ? null : info.detailedSnapshot();
    }

    /**
     * Returns the table feature arrays cached during extended handshake, for
     * the specified datapath. If no such datapath exists, null is returned.
     * 
     * @param dpid the target dpid
     * @return the table feature arrays
     */
    List<MBodyTableFeatures.Array> getCachedTableFeatures(DataPathId dpid) {
        DpInfo info = getDpInfo(dpid);
        if (info == null)
            return null;
        return Collections.unmodifiableList(info.mainConn.tableFeats);
        // TODO: Consider making this a destructive read
        //  Since it should only be the PipelineManager reading from it once.
    }

    /**
     * Returns the device description cached during extended handshake, for
     * the specified datapath. If no such datapath exists, null is returned.
     * 
     * @param dpid the target dpid
     * @return the device description
     */
    public MBodyDesc getCachedDeviceDesc(DataPathId dpid) {
        DpInfo info = getDpInfo(dpid);
        return info == null ? null : info.deviceDescription;
    }

    /**
     * Associates the given device type name with the indicated datapath.
     *
     * @param dpid the datapath ID
     * @param deviceTypeName the device type name
     */
    void associateDeviceType(DataPathId dpid, String deviceTypeName) {
        DpInfo info = getDpInfo(dpid);
        info.setDeviceType(deviceTypeName);
        log.info(I_DPID_TYPED, dpid, deviceTypeName);
    }

    /**
     * Returns the TX/RX queue control.
     *
     * @return the queue control
     */
    TxRxControl getTxRxControl() {
        return txRxCtrl;
    }

    /**
     * Sends the given message to the specified datapath via the main (control)
     * connection.
     * 
     * @param msg the message to send
     * @param dpid the target dpid
     * @throws OpenflowException
     */
    void send(OpenflowMessage msg, DataPathId dpid) throws OpenflowException {
        send(msg, dpid, MAIN_ID);
    }
    
    /**
     * Sends the given message to the specified datapath via the connection
     * tagged with the given auxiliary ID.
     *
     * @param msg the message to send
     * @param dpid the target dpid
     * @param auxId the auxiliary connection ID
     * @throws OpenflowException if no connected datapath with given ID
     */
    void send(OpenflowMessage msg, DataPathId dpid, int auxId)
            throws OpenflowException {
        // This method is only called by ListenerManager, which has
        // already validated that the parameters are not null, and that the
        // message is not mutable.
        send(msg, selectConnection(msg, dpid, auxId));
    }

    /**
     * Selects the openflow connection on which the outbound message is to be
     * sent.
     *
     * @param msg the message to send
     * @param dpid the datapath to send the message to
     * @param auxId the auxiliary channel ID
     * @return the connection to use
     * @throws OpenflowException if issues arise during connection selection
     * @throws IllegalArgumentException if message version is not the
     *         negotiated version
     */
    private OpenflowConnection selectConnection(OpenflowMessage msg,
                                                DataPathId dpid, int auxId)
            throws OpenflowException {
        DpInfo info = getDpInfoNotNull(dpid);
        if (msg.getVersion() != info.negotiated)
            throw new IllegalArgumentException(E_BAD_VERSION + msg.getVersion());

        OpenflowConnection conn = info.getConnection(auxId);

        // Per spec, if aux connection is not available, use the main one.
        return conn != null ? conn: info.mainConn;
    }

    /**
     * Sends the given message to the specified connection. 
     *
     * @param msg the message to send
     * @param conn the target connection
     * @throws OpenflowException if no connected datapath with given ID
     */
    void send(OpenflowMessage msg, OpenflowConnection conn)
            throws OpenflowException {
        stats.countTx(msg);
        if (txRxCtrl.recording)
            recordTx(null, msg, conn.dpid, conn.auxId);
        conn.send(msg);
    }

    /**
     * Attempts to cache the message future based on its datapath ID and
     * request transfer ID (XID). If successful, the given messages are sent
     * to the connection.  It is assumed that the future is associated with
     * the last message.  If an exception occurs, the future will be removed
     * from the cache and satisfied as a failure.
     *
     * @param f the message future to add
     * @param msgs the array of one or more messages to send
     * @throws OpenflowException if no data path info exists for the
     *      datapath ID; or if a pending future already exists for the given
     *      datapath ID and XID
     */
    void sendFuture(DataPathMessageFuture f, OpenflowMessage... msgs)
            throws OpenflowException {

        DpInfo info = getDpInfoNotNull(f.dpid());
        long xid = f.request().getXid();

        MessageFuture prev = info.pendingFutures.put(xid, f);
        if (prev != null) {
            // Duplicate transfer IDs?
            info.pendingFutures.remove(xid);
            OpenflowException e = new OpenflowException(
                    E_FUTURE_DP + f.dpid() + E_FUTURE_XID + xid);
            f.setFailure(e);
            throw e;
        }

        try {
            for (OpenflowMessage m: msgs)
                send(m, f.dpid());
        } catch (OpenflowException e) {
            info.pendingFutures.remove(xid);
            f.setFailure(e);
            throw e;
        }
    }

    /**
     * Finds the message future based on the messages's transfer ID and
     * specified datapath ID.
     *
     * @param msg the message containing the transfer ID
     * @param dpid the id of the datapath
     * @return the datapath message future or null if no future was found
     */
    DataPathMessageFuture findFuture(OpenflowMessage msg, DataPathId dpid) {
        DpInfo info = getDpInfo(dpid);
        if (info == null)
            return null;
        return info.pendingFutures.get(msg.getXid());
    }

    /**
     * Removes the message future from the cache based on its datapath ID and
     * request transfer ID. The future is satisfied as a failure using the
     * given cause.
     *
     * @param f the message future to remove and satisfy
     * @param cause the reason for the future failure
     */
    void failFuture(DataPathMessageFuture f, Throwable cause) {
        DpInfo info = getDpInfo(f.dpid());
        if (info != null)
            info.pendingFutures.remove(f.request().getXid());
        f.setFailure(cause);
    }

    /**
     * Removes the message future from the cache based on its datapath ID and
     * request transfer ID. The future is satisfied as a failure using the
     * given error message.
     *
     * @param f the message future to remove and satisfy
     * @param msg the failure error message
     */
    void failFuture(DataPathMessageFuture f, OfmError msg) {
        DpInfo info = getDpInfo(f.dpid());
        if (info != null)
            info.pendingFutures.remove(f.request().getXid());
        f.setFailure(msg);
    }

    /**
     * Removes the message future from the cache based on its datapath ID and
     * request transfer ID. The future is satisfied as a success using the
     * given message.
     *
     * @param f the message future to remove and satisfy
     * @param msg the message containing the transfer ID
     */
    void successFuture(DataPathMessageFuture f, OpenflowMessage msg) {
        DpInfo info = getDpInfo(f.dpid());
        if (info != null)
            info.pendingFutures.remove(msg.getXid());
        f.setSuccess(msg);
    }

    /**
     * Removes the message future from the cache based on its datapath ID and
     * request transfer ID. The future is left unsatisfied.
     *
     * @param f the message future to remove
     */
    void cancelFuture(DataPathMessageFuture f) {
        DpInfo info = getDpInfo(f.dpid());
        if (info != null)
            info.pendingFutures.remove(f.request().getXid());
    }

    /**
     * Returns the next I/O loop to which a connection should be assigned.
     *
     * @return selected worker
     */
    synchronized OpenflowIOLoop nextWorker() {
        lastWorker = (lastWorker + 1) % ioLoops.size();
        return ioLoops.get(lastWorker);
    }


    /**
     * Invoked by the message handler once a new connection handshake has
     * completed on an OpenFlow connection.
     *
     * @param connection the connection object
     */
    synchronized void handshakeComplete(OpenflowConnection connection) {
        DataPathId dpid = connection.dpid;
        int auxId = connection.auxId;
        ProtocolVersion pv = connection.negotiated;
        satisfyFuture(connection, dpid, auxId, pv);
        if (connection.isMain())
            newMainConnectionReady(connection);
        else
            newAuxConnectionReady(connection);
    }

    private void newMainConnectionReady(OpenflowConnection conn) {
        DataPathId dpid = conn.dpid;
        DpInfo dpInfo = infoCache.get(dpid);
        if (dpInfo != null) {
            conn.revokeConnection();
            conn.close();
            panic(E_EXISTING_MAIN_CONN, dpid);
        }

        dpInfo = new DpInfo(conn, portTracker, this);
        infoCache.put(dpid, dpInfo);

        // FIXME: move this into extended-handshake module
        if (!suppressSetConfig)
            BasicSwitchConfig.sendConfig(this, conn);
    }

    private void newAuxConnectionReady(OpenflowConnection conn) {
        DataPathId dpid = conn.dpid;
        DpInfo dpInfo = infoCache.get(dpid);
        if (dpInfo == null)
            panic(E_NO_MAIN_CONN, dpid, conn.auxId);
        else
            dpInfo.addConnection(conn);
    }

    void portDataReady(DataPathId dpid, List<Port> ports) {
        portTracker.portInit(dpid, ports);
    }

    void portDataReadyMp(DataPathId dpid, List<MBodyPortDesc.Array> portDescs) {
        List<Port> ports = new ArrayList<>();
        for (MBodyPortDesc.Array array : portDescs) 
            ports.addAll(array.getPorts());                
        portTracker.portInit(dpid, ports);
    }

    /**
     * Invoked (for main connections) after the asynchronous data collection 
     * from the newly connected datapath has completed (that is, the extended
     * handshake). This will result in invocation of post-handshake processing
     * and a notification to the message sink that a new datapath has connected.
     *
     * @param conn the connection object
     */
    synchronized void newMainConnectionComplete(OpenflowConnection conn) {
        // copy the cached datapath description into the DpInfo instance
        DpInfo info = getDpInfo(conn.dpid);
        info.deviceDescription = conn.deviceDesc;
        info.noTableFeatures = conn.noTF;

        // add a synthetic checkpoint to the Tx/Rx trace log
        if (txRxCtrl.recording)
            txRxCheckpoint(StringUtils.format(CKPT_DP_EXT_HS_DONE, conn.dpid));

        // prompt the device driver framework to determine device type,
        // purge any flows, and lay down initial flows...
        PostHandshakeTask task =
            phSink.doPostHandshake(conn.remoteAddress, conn.dpid, conn.deviceDesc,
                                   phCallback);
        info.setPhTask(task);

        // tell the world that a datapath has connected
        sink.dataPathAdded(conn.dpid, conn.negotiated, conn.remoteAddress);
    }

    /**
     * Throws an IllegalStateException with a message composed of the format
     * string and inserted arguments. 
     * 
     * @param fmt the message format
     * @param args the message arguments
     *             
     * @see StringUtils#format(String, Object...) 
     */
    private void panic(String fmt, Object... args) {
        throw new IllegalStateException(StringUtils.format(fmt, args));
    }

    
    /**
     * Handles notification about a closed connection. Prunes any internal
     * caches and makes sure that if the closed connection is a main one,
     * closes all associated aux connections as well.
     *
     * @param connection connection that was closed
     */
    synchronized void connectionClosed(OpenflowConnection connection) {
        DataPathId dpid = connection.dpid;
        int aux = connection.auxId;
        ProtocolVersion negotiated = connection.negotiated;
        IpAddress remoteAddress = connection.remoteAddress;

        if (connection.revoked()) {
            recordRevoke(dpid, aux);
            sink.dataPathRevoked(dpid, negotiated, remoteAddress);
            return;
        }

        // Failsafe, in case the connection was closed before the dpid was
        // established:
        if (dpid == null) {
            log.warn(E_CLOSE_WHEN_DPID_NULL, remoteAddress);
            return;
        }

        log.debug("Datapath connection closed: {} aux:{}", dpid, aux);

        if (connection.isMain()) {
            DpInfo info = getDpInfo(dpid);
            info.invalidate();

            // Shutdown the auxiliary connections first...
            closeAuxConnections(info);

            // Next record the event and prune internal cache
            recordDisconnect(dpid, aux);
            infoCache.remove(dpid);

            // Remove the memento from the dpid
            dpUtil.detachMemento(dpid);

            // inform listeners that the datapath was removed
            sink.dataPathRemoved(dpid, info.negotiated, remoteAddress);
        }
        // TODO: how to handle aux connections cleanly (the else clause)
    }

    /**
     * Hook to instruct the controller to close the connection due to being
     * idle too long.
     *
     * @param connection idle connection to be closed
     */
    void closingIdleConnection(OpenflowConnection connection) {
        log.warn(E_IDLE_CONNECTION, connection.remoteAddress);
        connection.close();
    }


    // Closes all aux connections for this specified datapath info
    private void closeAuxConnections(DpInfo info) {
        if (info != null)
            for (OpenflowConnection c : info.conns.values())
                if (!c.isMain())
                    c.close();
    }

    /**
     * Backfills all handshake message events with the dpid and auxId of the
     * connection.
     *
     * @param connection the OpenFlow connection to match
     * @param dpid the dpid to insert into the events
     * @param auxId the auxiliary channel ID to insert into the events
     * @param pv the negotiated protocol version to insert into the events
     */
    private void satisfyFuture(OpenflowConnection connection,
                               DataPathId dpid, int auxId, ProtocolVersion pv) {
        Set<HandshakeMessageEvt.DpidFuture> found = new HashSet<>();
        synchronized (pendingDpids) {
            for (HandshakeMessageEvt.DpidFuture future: pendingDpids) {
                if (future.connection == connection) {
                    future.satisfy(dpid, auxId, pv);
                    found.add(future);
                }
            }
            pendingDpids.removeAll(found);
        } // sync
    }


    /**
     * Invoked by message handlers when sending an openflow message to a
     * datapath during handshake. Note that at this point we have not
     * established the datapath ID or auxiliary connection ID. However, we do
     * have information about the connection.
     *
     * @param conn the connection
     * @param msg the message we are sending
     */
    void sentDuringHandshake(OpenflowConnection conn, OpenflowMessage msg) {
        // NOTE: we are thread-safe (by inspection)
        stats.countTx(msg);
        if (txRxCtrl.recording)
            recordTx(conn, msg, null, -1);
    }

    /**
     * Invoked by message handlers when receiving an openflow message from a
     * datapath during handshake. Note that for the HELLO message response we
     * still have not established the datapath ID, so the argument will be
     * null. When we receive the FEATURES_REPLY, we will know the dpid and aux
     * ID and can supply them. Nevertheless, we do have information about the
     * connection.
     *
     * @param conn the connection
     * @param msg the message we received
     */
    void receivedDuringHandshake(OpenflowConnection conn, OpenflowMessage msg) {
        // NOTE: we are thread-safe (by inspection)
        if (msg != null)
            stats.countRx(msg);
        if (txRxCtrl.recording)
            recordRx(conn, msg, null, -1);
    }

    /**
     * Invoked by message handlers when an openflow message is received from a
     * datapath connection. This results in the message being published to the
     * listener manager (and all the registered listeners).
     *
     * @param conn the connection from which the message came
     * @param msg the incoming message
     */
    void incomingMsg(OpenflowConnection conn, OpenflowMessage msg) {
        // NOTE: we are thread-safe (by inspection)
        tallyAndRecordMsg(conn, msg);
        // forward the message to our message sink
        sink.msgRx(msg, conn.dpid, conn.auxId, conn.negotiated);
    }

    /**
     * Tallies the specified message for controller stats, and records the
     * message event. 
     * 
     * @param conn the connection from which the message came
     * @param msg the incoming message
     */
    void tallyAndRecordMsg(OpenflowConnection conn, OpenflowMessage msg) {
        // NOTE: we are thread-safe (by inspection)
        stats.countRx(msg);
        if (txRxCtrl.recording)
            recordRx(null, msg, conn.dpid, conn.auxId);
    }

    //===================================================================

    /**
     * Record a transmitted message.
     *
     * @param conn connection information
     * @param msg the message
     * @param dpid the target dpid
     * @param auxId the auxiliary channel ID
     */
    private void recordTx(OpenflowConnection conn, OpenflowMessage msg,
                          DataPathId dpid, int auxId) {
        addToQ(MESSAGE_TX, conn, msg, dpid, auxId);
    }

    /**
     * Record a received message.
     *
     * @param conn connection information
     * @param msg the message
     * @param dpid the source dpid
     * @param auxId the auxiliary channel ID
     */
    private void recordRx(OpenflowConnection conn, OpenflowMessage msg,
                          DataPathId dpid, int auxId) {
        // if both msg and dpid are null, this represents a new connection
        OpenflowEventType evType = (msg == null && dpid == null)
                ? DATAPATH_CONNECTED : MESSAGE_RX;
        addToQ(evType, conn, msg, dpid, auxId);
    }

    /**
     * Record a disconnection.
     *
     * @param dpid the source dpid
     * @param auxId the auxiliary channel ID
     */
    private void recordDisconnect(DataPathId dpid, int auxId) {
        addToQ(DATAPATH_DISCONNECTED, null, null, dpid, auxId);
    }

    /**
     * Record a revocation.
     *
     * @param dpid the source dpid
     * @param auxId the auxiliary channel ID
     */
    private void recordRevoke(DataPathId dpid, int auxId) {
        addToQ(DATAPATH_REVOKED, null, null, dpid, auxId);
    }

    /**
     * Adds a message event to the TX/RX queue. During the handshake sequence,
     * connection information is also supplied, so we can retroactively
     * back-fill the dpid and auxId once we know what they are at the end of
     * the handshake. After the handshake, conn will be null.
     * <p>
     * The event type should be one of the following:
     * <ul>
     * <li> {@code DATAPATH_CONNECTED} - for the initial connection</li>
     * <li> {@code DATAPATH_REVOKED} - for a revoked connection</li>
     * <li> {@code MESSAGE_RX} - for a received message</li>
     * <li> {@code MESSAGE_TX} - for a transmitted message</li>
     * </ul>
     *
     * @param eventType the event type
     * @param conn connection information (only during handshake)
     * @param msg the message
     * @param dpid the datapath id
     * @param auxId the auxiliary ID
     */
    private void addToQ(OpenflowEventType eventType, OpenflowConnection conn,
                        OpenflowMessage msg, DataPathId dpid, int auxId) {
        MessageEvent msgEvent;
        if (conn != null) {
            // we don't know the dpid yet
            msgEvent = new HandshakeMessageEvt(eventType, msg, conn);
            synchronized (pendingDpids) {
                pendingDpids.add(((HandshakeMessageEvt)msgEvent).dpidFuture);
            } // sync
        } else {
            // even now, we may not know the dpid, if we are rejecting a switch
            // that tried to negotiate a protocol version that the controller
            // does not support...
            ProtocolVersion pv = dpid == null ? null : getDpInfo(dpid).negotiated;
            msgEvent = new MessageEvt(eventType, msg, dpid, auxId, pv);
        }
        // NOTE: LinkedBlockingQueue.add() is threadsafe (sync'd internally)
        txRxMsgEvents.add(msgEvent);
    }

    //===================================================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{OfCtlr:listenPort=");
        sb.append(openflowPort);
        if (!ioRunning)
            sb.append("[UNBOUND]");
        sb.append(",#cdp=").append(infoCache.size()).append("}");
        return sb.toString();
    }

    /**
     * Returns a multi-line representation of the current state of the
     * controller, showing all the cached connections.
     *
     * @return a multi-line representation of the state of the controller
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        if (infoCache.size() == 0)
            sb.append(EOLI).append(NONE).append(EOLI);
        else {
            // start by sorting the keys
            DataPathId[] keys = new DataPathId[infoCache.size()];
            int i = 0;
            for (DataPathId dpid: infoCache.keySet())
                keys[i++] = dpid;
            Arrays.sort(keys);

            for (DataPathId dpid: keys) {
                DpInfo info = infoCache.get(dpid);
                sb.append(EOLI).append(info.toDebugString(2));
            }
        }
        return sb.toString();
    }

    /**
     * Increment the dropped packet and byte counts. Dropped packet count will
     * be incremented by 1; dropped bytes count will be incremented by the
     * given value.
     *
     * @param byteCount the number of bytes dropped
     */
    void countDrop(int byteCount) {
        stats.countDrop(byteCount);
    }

    /** Reset the controller statistics. */
    void resetStats() {
        stats.reset();
    }

    // ====================================================================
    // Temporary implementation of idle connection detection

    private void processIdleConnections() {
        // TODO: Consider use of hashed-wheel timer instead
        long now = TimeUtils.getInstance().currentTimeMillis();
        for (DpInfo info : infoCache.values()) {
            for (OpenflowConnection c : info.conns.values())
                if (!c.checkLiveness(now)) {
                    info.removeConnection(c);
                }
        }
    }

    // Auxiliary timer task to check for idle connections
    private class IdleSweeper extends TimerTask {
        @Override
        public void run() {
            processIdleConnections();
        }
    }

    void prunePendingFutures() {
        int agedOut = 0;
        for (DpInfo info : infoCache.values()) {
            info.pendingFutures.prune();
            Set<DataPathMessageFuture> stale =
                    info.pendingFutures.clearDeadwood();
            agedOut += stale.size();
            for (DataPathMessageFuture f: stale)
                f.setFailure(EXCEPT_FUTURE_TIMEOUT);
        }
        if (agedOut > 0)
            log.warn(MSG_AGED_OUT_FUTURES, agedOut, PENDING_FUTURE_CHECK_MS);
    }

    private class PendingFutureSweeper extends TimerTask {
        @Override
        public void run() {
            prunePendingFutures();
        }
    }
    
    // ====================================================================
    // === Encapsulates the controller statistics
    /*
     * Design choice is to NOT use synchronization. Better to be off by a
     * couple of counts than to bottle-neck with synchronization.
     */
    // TODO : consider using volatile?
    private static class CtrlStats {
        private long resetAt;
        private long pktIns;
        private long pktInBytes;
        private long pktOuts;
        private long pktOutBytes;
        private long pktDrop;
        private long pktDropBytes;
        private long msgRx;
        private long msgTx;

        CtrlStats() {
            reset();
        }

        private ControllerStats snapshot() {
            return new Snapshot(this);
        }

        private void reset() {
            resetAt = System.currentTimeMillis();
            pktIns = 0;
            pktInBytes = 0;
            pktOuts = 0;
            pktOutBytes = 0;
            pktDrop = 0;
            pktDropBytes = 0;
            msgRx = 0;
            msgTx = 0;
        }

        private void countTx(OpenflowMessage msg) {
            if (msg.getType() == PACKET_OUT) {
                pktOuts++;
                pktOutBytes += ((OfmPacketOut) msg).getDataLength();
            } else {
                msgTx++;
            }
        }

        private void countRx(OpenflowMessage msg) {
            if (msg.getType() == PACKET_IN) {
                pktIns++;
                pktInBytes += ((OfmPacketIn) msg).getTotalLen();
            } else {
                msgRx++;
            }
        }

        private void countDrop(int byteCount) {
            pktDrop++;
            pktDropBytes += byteCount;
        }

        // === Inner snapshot class
        private static class Snapshot implements ControllerStats {
            private final long duration;
            private final long pktIns;
            private final long pktInBytes;
            private final long pktOuts;
            private final long pktOutBytes;
            private final long pktDrop;
            private final long pktDropBytes;
            private final long msgRx;
            private final long msgTx;

            public Snapshot(CtrlStats stats) {
                // TODO: consider using TimeUtils
                duration = System.currentTimeMillis() - stats.resetAt;
                pktIns = stats.pktIns;
                pktInBytes = stats.pktInBytes;
                pktOuts = stats.pktOuts;
                pktOutBytes = stats.pktOutBytes;
                pktDrop = stats.pktDrop;
                pktDropBytes = stats.pktDropBytes;
                msgRx = stats.msgRx;
                msgTx = stats.msgTx;
            }

            @Override public long duration() { return duration; }
            @Override public long packetInCount() { return pktIns; }
            @Override public long packetInBytes() { return pktInBytes; }
            @Override public long packetOutCount() { return pktOuts; }
            @Override public long packetOutBytes() { return pktOutBytes; }
            @Override public long packetDropCount() { return pktDrop; }
            @Override public long packetDropBytes() { return pktDropBytes; }
            @Override public long msgRxCount() { return msgRx; }
            @Override public long msgTxCount() { return msgTx; }

            @Override
            public String toString() {
                return "{ControllerStats:dur=" + duration +
                        "ms,#pktIn=" + pktIns + ",inBytes=" + pktInBytes +
                        ",#pktOut=" + pktOuts + ",outBytes=" + pktOutBytes +
                        ",#pktDrop=" + pktDrop + ",dropBytes=" + pktDropBytes +
                        ",#msgRx=" + msgRx + ",#msgTx=" + msgTx + "}";
            }
        }
    }

    // Our hook to resolve and manipulate DataPathId mementos
    private static class DpUtil extends DataPathUtils {
        @Override
        protected void attachMemento(DataPathId dpid, Object dpi) {
            super.attachMemento(dpid, dpi);
        }

        @Override
        protected void detachMemento(DataPathId dpid) {
            super.detachMemento(dpid);
        }

        @Override
        protected DpInfo getMemento(DataPathId dpid) {
            return (DpInfo) super.getMemento(dpid);
        }
    }

    // ====================================================================
    // ==== UNIT TEST Support ====

    /** Returns the number of connected datapaths.
     *
     * @return the number of connected datapaths
     */
    int infoCacheSize() {
        return infoCache.size();
    }

    /** Insert a checkpoint record into the TX/RX queue.
     *
     * @param text text to be included in the checkpoint
     */
    void txRxCheckpoint(String text) {
        txRxMsgEvents.add(new CheckpointEvt(CheckpointEvent.Code.GENERIC, text));
    }


    /**
     * Disables the openflow controller idle detection mechanism.
     *
     * @param on true to enable; false to disable
     */
    static void enableIdleDetection(boolean on) {
        idleDetectionEnabled = on;
    }
}