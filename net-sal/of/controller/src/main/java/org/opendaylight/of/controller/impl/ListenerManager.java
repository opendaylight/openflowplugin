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
import org.opendaylight.of.controller.pkt.PacketSequencerSink;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.mp.MBodyPortStats;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.NotYetImplementedException;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.api.NotFoundException;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.opendaylight.of.controller.OpenflowEventType.*;
import static org.opendaylight.of.lib.CommonUtils.notMutable;
import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_IN;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static org.opendaylight.util.StringUtils.EOL;

/**
 * An implementation of the {@link ListenerService}.
 * <p>
 * When a listener registers, it is allocated a private, bounded queue of
 * events. A thread pool is used to allocate a thread to pull items off the
 * queue and invoke the listener's
 * {@link OpenflowListener#event(OpenflowEvent) event()} callback. As long as
 * the number of items on the queue is kept below the queue size, everything
 * should be good. If the queue is allowed to fill up such that an incoming
 * event cannot be added to the queue, an out-of-band
 * {@link OpenflowEventType#QUEUE_FULL QUEUE_FULL} event is passed to the
 * listener's {@link OpenflowListener#queueEvent(QueueEvent) queueEvent()}
 * callback, and no more events will be added to the queue until it has been
 * drained below the "reset" threshold (currently 50%).
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Thomas Vachuska
 * @author Sudheer Duggisetty
 * @author Frank Wood
 */
class ListenerManager implements ListenerService {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ListenerManager.class, "listenerManager");

    private static final String MSG_Q_FULL_FOR = RES
            .getString("msg_q_full_for");
    private static final String ALERT_Q_FULL_FOR = "Queue FULL for ";

    private static final String MSG_SEND_FAILED_MP_PORT_DESC = RES
            .getString("msg_send_failed_mp_port_desc");
    private static final String MSG_CTL_UP = RES.getString("msg_ctl_up");
    private static final String MSG_ASSOC_MSG = RES.getString("msg_assoc_msg");

    private static final String E_SML_ALREADY = RES.getString("e_sml_already");
    private static final String E_NO_DEVICE = RES.getString("e_no_device");
    private static final String E_NO_WRITE = RES.getString("e_no_write");
    private static final String E_SEQUENCER = RES.getString("e_sequencer");

    // ==
    private static final String MSG_DP_ADDED =
            "Datapath added: {}, neg={}, ip={}";
    private static final String MSG_DP_REMOVED =
            "Datapath removed: {}, neg={}, ip={}";
    private static final String MSG_DP_REVOKED =
            "Datapath REVOKED: {}, neg={}, ip={}";
    private static final String MSG_DP_READY =
            "Datapath READY: {}, neg={}, ip={}";


    private static final Logger DEFAULT_LOG =
            LoggerFactory.getLogger(OpenflowController.class);

    private static Logger log = DEFAULT_LOG;

    // provided to install a test logger
    static void setLogger(Logger logger) {
        log = logger;
    }

    // provided to restore the production logger
    static void restoreLogger() {
        log = DEFAULT_LOG;
    }

    // Aux ID == 0 is the main connection to the switch.
    private static final int MAIN_ID = 0;

    // An empty set representing the desire to be informed of ALL messages types
    static final Set<MessageType> ALL_TYPES =
            Collections.unmodifiableSet(new HashSet<MessageType>());

    // Our datapath listeners
    private final CopyOnWriteArraySet<DpHandler> dpHandlers =
            new CopyOnWriteArraySet<DpHandler>();

    /// Our message listeners
    private final CopyOnWriteArraySet<MsgHandler> msgHandlers =
            new CopyOnWriteArraySet<MsgHandler>();


    //======================================================================
    // TODO : Make these tunable parameters (at manager startup?)

    /** Maximum number of MessageEvents in a MessageListener's queue.
     * If the queue exceeds capacity:
     * <ul>
     *     <li>
     *      a QUEUE_FULL event is sent, (out of band)
     *     </li>
     *     <li>
     *      the listener is marked as "on probation"
     *     </li>
     *     <li>
     *      no more events will be posted until the queue has been drained
     *      below the {@link #Q_FULL_RESET_THRESHOLD reset threshold}
     *     </li>
     * </ul>
     */
    static int RX_Q_CAPACITY = 2000;

    /** Maximum number of DataPathEvents in a DataPathListener's queue.
     * If the queue exceeds capacity:
     * <ul>
     *     <li>
     *      a QUEUE_FULL event is sent, (out of band)
     *     </li>
     *     <li>
     *      the listener is marked as "on probation"
     *     </li>
     *     <li>
     *      no more events will be posted until the queue has been drained
     *      below the {@link #Q_FULL_RESET_THRESHOLD reset threshold}
     *     </li>
     * </ul>
     */
    static int DP_Q_CAPACITY = 500;

    /** Queue-Full Probation reset threshold.
     * If the number of events on the queue drops below this value
     * (as a ratio of the queue capacity):
     * <ul>
     *     <li>
     *      a DROPPED_EVENTS_CHECKPOINT is added to the queue (in band)
     *     </li>
     *     <li>
     *      a QUEUE_FULL_RESET event is sent (out of band)
     *     </li>
     *     <li>
     *      the "on probation" state is cleared
     *     </li>
     *     <li>
     *      we'll start adding events to the queue again
     *     </li>
     * </ul>
     */
    static final double Q_FULL_RESET_THRESHOLD = 0.5;


    //======================================================================

    // TODO: Use tunable thread pools with min/max threads??
    /** A thread pool for servicing the RX-queues of message listeners. */
    private final ExecutorService rxQueuePool =
            newCachedThreadPool(namedThreads("RxQPool"));

    /** A thread pool for servicing the DP-queues of datapath listeners. */
    private final ExecutorService dpQueuePool =
            newCachedThreadPool(namedThreads("DpQPool"));

    // Where we send alerts when we generate 'em.
    private AlertSink alertSink;

    // Who to ask about roles.
    private RoleAdvisor roleAdvisor;

    // How we decide which connection to send messages over.
//    private ConnectionSelector connSelector;

    // Where we keep track of the state of ports on datapaths.
    private PortStateTracker portTracker;

    // Our message sink instance.
    private Sink messageSink;

    // Our OpenFlow controller instance.
    private OpenflowController controller;

    // Our controller management API implementation.
    private CtrlMx ctrlMx;

    // The packet sequencer backend for sending messages.
    private PacketSequencerSink seqSink;

    /**
     * Constructs the manager by creating the message sink, constructing the
     * controller and initializing with the given configuration parameters and
     * services.  The controller will not be accepting incoming connections.
     * See the {@link ControllerMx} to enable the processing of incoming
     * connections.
     *
     * @param cfg the configuration parameters
     * @param as where to send any alerts
     * @param ph where to kick off post handshake processing
     * @param fma where to process flow mods
     * @param phc post-handshake completion callback
     * @param ra where to ask about controller roles
     */
    ListenerManager(ControllerConfig cfg, AlertSink as, PostHandshakeSink ph,
                    PostHandshakeCallback phc, FlowModAdvisor fma, RoleAdvisor ra) {
        alertSink = as;
        roleAdvisor = ra;

        portTracker = new PortStateTracker(this);
        messageSink = new Sink();
        controller = new OpenflowController(cfg, portTracker, messageSink,
                                            as, ph, phc, fma);
        ctrlMx = new CtrlMx(this);

        controller.init();
    }

    /**
     * Start the IO processing loop on the Open Flow controller.
     */
    void startIOProcessing() {
        controller.startIOProcessing();
        if (alertSink != null)
            alertInfo(MSG_CTL_UP + controller.getOpenflowPort());
    }

    /**
     * Update configuration parameters. We just delegate to the controller.
     *
     * @param cfg the new config
     */
    void updateNonBounceConfig(ControllerConfig cfg) {
        controller.updateNonBounceConfig(cfg);
    }

    /** Gracefully shuts down the core controller. */
    // package private so unit tests can access by extension.
    void shutdown() {
        removeAllListeners();
        controller.shutdown();
    }

    @Override
    public String toString() {
        return "{ctlrMgr:" +
                "#dpl=" + dpHandlers.size() +
                ",#ml=" + msgHandlers.size() +
                "}";
    }

    /** Returns a multi-line string representation of the controller manager
     * suitable for debugging.
     *
     * @return a multi-line string representation of the controller manager
     */
    public String toDebugString() {
        return toDebugString(false);
    }

    /** Returns a multi-line string representation of the controller manager
     * suitable for debugging.
     * <p>
     * If {@code includingController} is true, the full debug
     * string of the controller is included.
     *
     * @param includingController {@code true} includes the controller
     *            toDebugString(); {@code false} includes just the
     *            controller toString().
     * @return a multi-line string representation of the controller manager
     */
    public String toDebugString(boolean includingController) {
        StringBuilder sb = new StringBuilder(toString());
        String ctlr = includingController ? controller.toDebugString()
                : controller.toString() + EOL;
        sb.append(EOL).append(ctlr);
        return sb.toString();
    }

    //===================================================================
    // ControllerService implementation

    // === Listener Registration

    @Override
    public void addDataPathListener(DataPathListener listener) {
        notNull(listener);
        DpHandler h = new DpHandler(listener, DP_Q_CAPACITY);
        startListening(h);
        dpHandlers.add(h);
        ctrlMx.notifyRegListener(listener, true);
    }

    @Override
    public void removeDataPathListener(DataPathListener listener) {
        notNull(listener);
        // first, need to cancel the event reader task
        for (EventHandler<?,?> handler: dpHandlers)
            if (handler.listener.equals(listener)) {
                stopListening(handler);
                break;
            }
        dpHandlers.remove(new DpHandler(listener));
        ctrlMx.notifyRegListener(listener, false);
    }

    @Override
    public void addMessageListener(MessageListener listener,
                                   Set<MessageType> types) {
        notNull(listener);
        MsgHandler h = new MsgHandler(listener, types, RX_Q_CAPACITY);
        startListening(h);
        msgHandlers.add(h);
        ctrlMx.notifyRegListener(listener, true);
    }

    @Override
    public void removeMessageListener(MessageListener listener) {
        notNull(listener);
        // first, need to cancel the event reader task
        for (EventHandler<?,?> handler: msgHandlers)
            if (handler.listener.equals(listener)) {
                stopListening(handler);
                break;
            }
        msgHandlers.remove(new MsgHandler(listener));
        ctrlMx.notifyRegListener(listener, false);
    }

    private void removeAllListeners() {
        for (EventHandler<?,?> handler: dpHandlers)
            stopListening(handler);
        dpHandlers.clear();

        for (EventHandler<?,?> handler: msgHandlers)
            stopListening(handler);
        msgHandlers.clear();
    }

    // === Information about connected datapaths

    @Override
    public Set<DataPathInfo> getAllDataPathInfo() {
        return controller.getAllDataPathInfo();
    }

    @Override
    public DataPathInfo getDataPathInfo(DataPathId dpid) {
        notNull(dpid);
        DataPathInfo dpi = controller.getDataPathInfo(dpid);
        if (dpi == null)
            throw new NotFoundException(E_NO_DEVICE + dpid);
        return dpi;
    }

    @Override
    public ProtocolVersion versionOf(DataPathId dpid) {
        notNull(dpid);
        ProtocolVersion pv = controller.versionOf(dpid);
        if (pv == null)
            throw new NotFoundException(E_NO_DEVICE + dpid);
        return pv;
    }

    @Override
    public ControllerStats getStats() {
        return controller.getStats();
    }

    @Override
    public List<MBodyPortStats> getPortStats(DataPathId dpid) {
        return portTracker.getPortStats(versionOf(dpid), dpid);
    }

    @Override
    public MBodyPortStats getPortStats(DataPathId dpid, BigPortNumber port) {
        return portTracker.getPortStats(versionOf(dpid), dpid, port);
    }

    @Override
    public MessageFuture enablePort(DataPathId dpid, BigPortNumber port,
                                    boolean enable) {
        if (!roleAdvisor.isMasterFor(dpid))
            throw new IllegalArgumentException(E_NO_WRITE + dpid);

        return portTracker.enablePort(versionOf(dpid), dpid, port, enable);
    }

    @Override
    public void send(OpenflowMessage msg, DataPathId dpid, int auxId)
            throws OpenflowException {
        // should be called ONLY from the packet Sequencer 
        notNull(msg, dpid);
        notMutable(msg);
        controller.send(msg, dpid, auxId);
    }

    @Override
    public void send(OpenflowMessage msg, DataPathId dpid)
            throws OpenflowException {
        notNull(msg, dpid);
        notMutable(msg);
        controller.send(msg, dpid);
    }

    @Override
    public void send(List<OpenflowMessage> msgs, DataPathId dpid)
            throws OpenflowException {
        notNull(msgs, dpid);
        for (OpenflowMessage m: msgs)
            notMutable(m);
        for (OpenflowMessage m: msgs)
            controller.send(m, dpid);
    }

    @Override
    public void sendFuture(DataPathMessageFuture f, OpenflowMessage... msgs)
            throws OpenflowException {
        notNull(f, msgs);
        for (OpenflowMessage m: msgs)
            notMutable(m);
        controller.sendFuture(f, msgs);
    }

    @Override
    public void sendFuture(DataPathMessageFuture f) throws OpenflowException {
        notNull(f);
        notMutable(f.request());
        controller.sendFuture(f, f.request());
    }

    @Override
    public DataPathMessageFuture findFuture(OpenflowMessage msg, 
                                            DataPathId dpid) {
        notNull(msg, dpid);
        return controller.findFuture(msg, dpid);
    }

    @Override
    public void failFuture(DataPathMessageFuture f, OfmError msg) {
        notNull(f, msg);
        controller.failFuture(f, msg);
    }

    @Override
    public void failFuture(DataPathMessageFuture f, Throwable cause) {
        notNull(f, cause);
        controller.failFuture(f, cause);
    }

    @Override
    public void successFuture(DataPathMessageFuture f, OpenflowMessage msg) {
        notNull(f, msg);
        controller.successFuture(f, msg);
    }

    @Override
    public void cancelFuture(DataPathMessageFuture f) {
        notNull(f);
        controller.cancelFuture(f);
    }

    @Override
    public void countDrop(int byteCount) {
        controller.countDrop(byteCount);
    }

    @Override
    public List<MBodyTableFeatures.Array> 
    getCachedTableFeatures(DataPathId dpid) {
        return controller.getCachedTableFeatures(dpid);
    }

    @Override
    public MBodyDesc getCachedDeviceDesc(DataPathId dpid) {
        return controller.getCachedDeviceDesc(dpid);
    }

    // =====================================================================
    // === Event Listener processing

    /** Returns the appropriate executor service for the given listener.
     *
     * @param listener the listener
     * @param <E> the listener's event type
     * @return the executor service
     */
    private <E extends OpenflowEvent>
    ExecutorService execService(OpenflowListener<E> listener) {
        ExecutorService es = null;
        if (DataPathListener.class.isInstance(listener))
            es = dpQueuePool;
        else if (MessageListener.class.isInstance(listener))
            es = rxQueuePool;
        else if (RegistrationListener.class.isInstance(listener))
            throw new NotYetImplementedException("TODO - registration " +
                    "listener single thread executor");
        return es;
    }

    /** Creates an event reader, setting it up to pull events from the
     * handler's event queue and feed them through the listener API, then
     * submits the reader to the appropriate thread pool.
     *
     * @param handler the handler to start listening on
     */
    private <L extends OpenflowListener<E>, E extends OpenflowEvent>
    void startListening(EventHandler<L,E> handler) {
        handler.reader = new EventReader<L,E>(handler, log);
        handler.future = execService(handler.listener).submit(handler.reader);
    }

    /** Cancels the event reader task for the given handler
     * and cleans up resources.
     *
     * @param handler the handler to stop listening on
     */
    private <L extends OpenflowListener<E>, E extends OpenflowEvent>
    void stopListening(EventHandler<L,E> handler) {
        handler.future.cancel(true);
        handler.reader.shutdown();
    }

    // =====================================================================
    // === MX module support

    /** Provide the MX module with a reference to the controller.
     *
     * @return the controller
     */
    OpenflowController getController() {
        return controller;
    }

    /** Provide the MX module with a snapshot of our datapath listeners.
     *
     * @return the current datapath listeners
     */
    Set<DataPathListener> getDpListeners() {
        Set<DataPathListener> result = new HashSet<DataPathListener>();
        for (DpHandler handler: dpHandlers)
            result.add(handler.listener);
        return result;
    }

    /** Provide the MX module with a snapshot of our message listeners.
     *
     * @return the current message listeners
     */
    Set<MessageListener> getMsgListeners() {
        Set<MessageListener> result = new HashSet<MessageListener>();
        for (MsgHandler handler: msgHandlers)
            result.add(handler.listener);
        return result;
    }

    // =====================================================================
    // === UNIT TEST Support

    /** Sets the datapath ready latch. For unit test support.
     *
     * @param latch the countdown latch
     */
    void setDataPathAddedLatch(CountDownLatch latch) {
        messageSink.dpReady = latch;
    }

    /** Sets the datapath gone latch. For unit test support.
     *
     * @param latch the countdown latch
     */
    void setDataPathRemovedLatch(CountDownLatch latch) {
        messageSink.dpGone = latch;
    }

    /** Sets the message received countdown latch. For unit test support.
     *
     * @param latch the countdown latch
     */
    void setMsgRxLatch(CountDownLatch latch) {
        messageSink.msgIn = latch;
    }

    /** Return the TX/RX control.
     *
     * @return the TX/RX control
     */
    TxRxControl getTxRxControl() {
        return controller.getTxRxControl();
    }

    /** Returns the number of registered message listeners.
     *
     * @return the number of message listeners
     */
    int msgListenerCount() {
        return msgHandlers.size();
    }

    /** Returns the number of registered datapath listeners.
     *
     * @return the number of datapath listeners
     */
    int dpListenerCount() {
        return dpHandlers.size();
    }

    /** Inserts a checkpoint record into the TX/RX queue.
     *
     * @param text text to be included in the checkpoint
     */
    void txRxCheckpoint(String text) {
        controller.txRxCheckpoint(text);
    }

    /** Resets the controller statistics object. */
    void resetStats() {
        controller.resetStats();
    }

    // =====================================================================
    // === Internal linkage

    /** Invokes the message received callback on the message sink.
     *
     * @param msg the message
     * @param dpid the datapath ID
     * @param auxId the auxiliary connection ID
     * @param pv negotiated protocol version
     */
    void msgRx(OpenflowMessage msg, DataPathId dpid, int auxId,
               ProtocolVersion pv) {
        messageSink.msgRx(msg, dpid, auxId, pv);
    }

    /** 
     * Gives components internal to the core controller a hook on newly
     * established datapaths.
     *
     * @param dpid the source datapath
     * @param pv the negotiated protocol version
     */
    private void handshakeCompleteHook(DataPathId dpid, ProtocolVersion pv) {
    }


    /** 
     * Gives components internal to the core controller a hook on recently
     * removed datapaths.
     *
     * @param dpid the source datapath
     * @param pv the negotiated protocol version
     */
    private void datapathRemovedHook(DataPathId dpid, ProtocolVersion pv) {
        portTracker.dpRemoved(dpid);
    }

    /** 
     * Gives components internal to the core controller (not "sub-components")
     * a hook on incoming messages.
     *
     * @param mt the message type
     * @param msg the message
     * @param dpid the source datapath
     * @param pv the protocol version
     */
    private void incomingMessageHook(MessageType mt, OpenflowMessage msg,
                                     DataPathId dpid, ProtocolVersion pv) {
        switch (mt) {
            case PORT_STATUS:
                portTracker.portStatus((OfmPortStatus) msg, dpid);
                break;
            case BARRIER_REPLY:
                portTracker.incomingBarrier((OfmBarrierReply) msg);
                break;
            case MULTIPART_REPLY:
                incomingMultipartHook((OfmMultipartReply) msg, dpid);
                break;
            case ERROR:
                portTracker.incomingError((OfmError) msg);
                break;
            default:
                // do nothing (other than keep findbugs happier)
                break;
        }
    }

    /** 
     * Handles incoming MultipartReply messages.
     *
     * @param msg the MultipartReply message
     * @param dpid the source datapath
     */
    private void incomingMultipartHook(OfmMultipartReply msg, DataPathId dpid) {
        switch (msg.getMultipartType()) {
            case PORT_STATS:
                portTracker.incomingPortStats(msg);
                break;
            default:
                // do nothing (other than keep findbugs happier)
                break;
        }
    }

    /** 
     * Returns the ControllerMx implementation.
     *
     * @return the managament interface
     */
    ControllerMx getMx() {
        return ctrlMx;
    }

    /** Registers the packet sequencer sink.
     *
     * @param sml the sequencer sink
     */
    void registerSequencer(PacketSequencerSink sml) {
        notNull(sml);
        if (seqSink != null)
            throw new IllegalStateException(E_SML_ALREADY + seqSink);
        seqSink = sml;
    }

    void associateDeviceType(DataPathId dpid, String deviceTypeName) {
        controller.associateDeviceType(dpid, deviceTypeName);
    }

    void signalDataPathReady(DataPathId dpid) {
        DataPathInfo dpi = getDataPathInfo(dpid);
        ProtocolVersion pv = dpi.negotiated();
        IpAddress ip = dpi.remoteAddress();
        messageSink.dataPathReady(dpid, pv, ip);
    }

    // =====================================================================

    /**
     *  An implementation of {@code MessageSink} that scans through its
     *  set of registered listeners and invokes their
     *  callbacks as appropriate.
     */
    private class Sink implements MessageSink {
        // For unit test support
        // TODO: rename dpReady to dpConn, since 'ready' means something else!
        private CountDownLatch dpReady;
        private CountDownLatch dpGone;
        private CountDownLatch msgIn;

        // TODO: ADD dataPathReady to MessageSink API
        public void dataPathReady(DataPathId dpid, ProtocolVersion pv,
                                  IpAddress ip) {
            log.info(MSG_DP_READY, dpid, pv, ip);
            DataPathEvt ev = new DataPathEvt(DATAPATH_READY, dpid, pv, ip);
            for (DpHandler h: dpHandlers)
                if (!h.onProbation)
                    enqueue(h, ev);

            // For unit test support...
//            if (dpReady != null)
//                dpReady.countDown();
        }

        @Override
        public void dataPathAdded(DataPathId dpid, ProtocolVersion pv,
                                  IpAddress ip) {
            log.info(MSG_DP_ADDED, dpid, pv, ip);
            // TODO: consider ripping out the unused hook...
            handshakeCompleteHook(dpid, pv);
            DataPathEvt ev = new DataPathEvt(DATAPATH_CONNECTED, dpid, pv, ip);
            for (DpHandler h: dpHandlers)
                if (!h.onProbation)
                    enqueue(h, ev);

            // For unit test support...
            if (dpReady != null)
                dpReady.countDown();
        }

        @Override
        public void dataPathRemoved(DataPathId dpid, ProtocolVersion pv,
                                    IpAddress ip) {
            log.info(MSG_DP_REMOVED, dpid, pv, ip);
            datapathRemovedHook(dpid, pv);
            DataPathEvt ev =
                    new DataPathEvt(DATAPATH_DISCONNECTED, dpid, pv, ip);
            for (DpHandler h: dpHandlers)
                if (!h.onProbation)
                    enqueue(h, ev);

            // For unit test support...
            if (dpGone != null)
                dpGone.countDown();
        }

        @Override
        public void dataPathRevoked(DataPathId dpid, ProtocolVersion pv,
                                    IpAddress ip) {
            log.warn(MSG_DP_REVOKED, dpid, pv, ip);
            DataPathEvt ev = new DataPathEvt(DATAPATH_REVOKED, dpid, pv, ip);
            for (DpHandler h: dpHandlers)
                if (!h.onProbation)
                    enqueue(h, ev);

            // For unit test support...
            // the assumption is that the unit test is waiting for a switch
            //  to connect... which will not happen now...
            if (dpReady != null)
                dpReady.countDown();
        }

        @Override
        public void msgRx(OpenflowMessage msg, DataPathId dpid, int auxId,
                          ProtocolVersion pv) {
            final MessageType mt = msg.getType();
            incomingMessageHook(mt, msg, dpid, pv);
            MessageEvt ev = new MessageEvt(MESSAGE_RX, msg, dpid, auxId, pv);

            // special handling of packet-in messages
            if (mt == PACKET_IN) {
                try {
                    seqSink.processPacket(ev);
                } catch (Exception e) {
                    log.error(E_SEQUENCER, e);
                    log.warn(MSG_ASSOC_MSG, msg.toDebugString());
                }

            } else {
                for (MsgHandler h: msgHandlers)
                    if (!h.onProbation &&
                            (h.careAbout == ALL_TYPES || h.careAbout.contains(mt)))
                        enqueue(h, ev);
            }

            // For unit test support...
            if (msgIn != null)
                msgIn.countDown();
        }

        private void enqueue(DpHandler h, DataPathEvent ev) {
            try {
                h.eventQ.add(ev);
            } catch (IllegalStateException e) {
                if (h.putOnProbation(true)) {
                    log.warn(MSG_Q_FULL_FOR, h);
                    alertWarn(ALERT_Q_FULL_FOR + h);
                }
            }
        }

        private void enqueue(MsgHandler h, MessageEvent ev) {
            try {
                h.eventQ.add(ev);
            } catch (IllegalStateException e) {
                if (h.putOnProbation(true)) {
                    log.warn(MSG_Q_FULL_FOR, h);
                    alertWarn(ALERT_Q_FULL_FOR + h);
                }
            }
        }

    }

    // post a Warning severity alert to the outside world
    private void alertWarn(String msg) {
        alertSink.postAlert(new DefaultAlert(Alert.Severity.WARNING, msg));
    }

    // post an informational severity alert to the outside world
    private void alertInfo(String msg) {
        alertSink.postAlert(new DefaultAlert(Alert.Severity.INFO, msg));
    }
}
