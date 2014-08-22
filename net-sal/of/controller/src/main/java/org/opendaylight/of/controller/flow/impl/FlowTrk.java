/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow.impl;

import org.opendaylight.of.controller.*;
import org.opendaylight.of.controller.flow.*;
import org.opendaylight.of.controller.impl.AbstractSubComponent;
import org.opendaylight.of.controller.impl.ControllerConfig;
import org.opendaylight.of.controller.impl.ListenerService;
import org.opendaylight.of.controller.pipeline.PipelineReader;
import org.opendaylight.of.controller.pipeline.impl.PipelineMgmt;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.Log;
import org.opendaylight.util.api.NotFoundException;
import org.opendaylight.util.event.EventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.mp.MultipartType.*;
import static org.opendaylight.of.lib.msg.MessageBatchFuture.createBatchFuture;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.ResourceUtils.getBundledResource;

/**
 * Implements basic flow management for the controller.
 *
 * @author Radhika Hegde
 * @author Simon Hunt
 * @author Frank Wood
 */
public class FlowTrk extends AbstractSubComponent
        implements FlowTracker, GroupTracker, MeterTracker {

    /*
     * IMPLEMENTATION NOTE:
     * For now, FlowTrk extends AbstractSubComponent, but we intend to
     * refactor to have FlowTrk be a "client" of ResponseManager, which
     * will allow us to have one listener for MP-Replies, rather than
     * N listeners (and N msg-Qs) who all have to filter out by MP-Type.
     *
     * Also, it is intended that GroupTrk and MeterTrk will handle the
     * other interfaces - but for now, we are just doing one step at a time.
     */

    private static final ResourceBundle RES = 
            getBundledResource(FlowTrk.class, "flowTrk");

    private static final String E_NO_DATA = RES.getString("e_no_data");
    private static final String E_NO_DEVICE = RES.getString("e_no_device");
    private static final String E_NO_GROUP = RES.getString("e_no_group");
    private static final String E_NO_METER = RES.getString("e_no_meter");
    private static final String E_ON_DEV = RES.getString("e_on_dev");
    private static final String E_USE_OTHER_INIT = 
            RES.getString("e_use_other_init");
    private static final String E_NO_WRITE = RES.getString("e_no_write");

    private static final String LM_FAILED_SEND = 
            RES.getString("lm_failed_send");

    private static final String E_TIMEOUT = RES.getString("e_timeout");

    private static final String MSG_FLOWS_PURGED =
            "Flows purged from {}";

    // max amount of time (ms) we'll wait for a response
    private static final long MAX_BLOCKED_WAIT_MS = 10000; // 10 seconds

    // TODO: use Match.getMatchAll(pv) when available
    private static final Match MATCH_ALL_10 =
            (Match) createMatch(V_1_0).toImmutable();
    private static final Match MATCH_ALL_13 =
            (Match) createMatch(V_1_3).toImmutable();

    private final MsgListener msgListener = new MsgListener();
    private final DpListener dpListener   = new DpListener();

    // Initialization entities
    private PipelineReader pr;
    private EventDispatcher ed;
    private ControllerService cs;
    private FlowModAdvisor fma;
    private InitialFlowUtils ifu;

    // Rate-limiter Experimenter body for PV 1.0
    private static final byte[] M_EXP_BODY = {0x00, 0x00, 0x00, 0x00};

    // Flag indicating if we should operate in pure OF or hybrid OF mode.
    private boolean hybridOpenflow = false;

    // logger
    private static final Logger DEFAULT_LOG = LoggerFactory.getLogger(FlowTracker.class);
    static Logger log = DEFAULT_LOG;

    static void setLogger(Logger testLogger) {
        log = testLogger;
    }

    static void restoreLogger() {
        log = DEFAULT_LOG;
    }

    // === HELPER Methods

    // Either returns the negotiated version for the given dpid, or throws
    // NOT-FOUND exception.
    private ProtocolVersion dpiPv(DataPathId dpid) {
        ProtocolVersion pv = listenerService.versionOf(dpid);
        if (pv == null)
            throw new NotFoundException(E_NO_DEVICE + dpid);
        return pv;
    }

    // Either returns the negotiated version for the given dpid, or throws
    // appropriate exception: NOT-FOUND, or ILL-ARG (not master).
    private ProtocolVersion dpiMasterPv(DataPathId dpid) {
        confirmMaster(dpid);
        return dpiPv(dpid);
    }

    // Throw exception if we are not the master of the given dpid.
    private void confirmMaster(DataPathId dpid) {
        if (!roleAdvisor.isMasterFor(dpid))
            throw new IllegalArgumentException(E_NO_WRITE + dpid);
    }

    // TODO: use Match.getMatchAll(pv) when available
    // Returns a Match-all match for the given protocol version
    private Match getMatchAll(ProtocolVersion pv) {
        return pv == V_1_0 ? MATCH_ALL_10 : MATCH_ALL_13;
    }

    // Generates a barrier request with XID matching that of given message
    private OpenflowMessage barrierRequest(OpenflowMessage msg) {
        return create(msg, BARRIER_REQUEST).toImmutable();
    }

    // Waits for the specified future to be satisfied
    private void await(DataPathMessageFuture f) {
        boolean completed = f.awaitUninterruptibly(MAX_BLOCKED_WAIT_MS);
        if (!completed) {
            IllegalStateException e = new IllegalStateException(E_TIMEOUT);
            listenerService.failFuture(f, e);
            throw e;
        }
        switch (f.result()) {
            case SUCCESS:
                break;

            case OFM_ERROR:
                throw new IllegalStateException(f.reply().toString());

            case EXCEPTION:
                throw new IllegalStateException(E_NO_DATA, f.cause());
        }
    }

    // === FlowTracker implementation

    @Override
    public List<MBodyFlowStats> getFlowStats(DataPathId dpid, TableId tableId) {
        notNull(dpid);
        ProtocolVersion pv = dpiPv(dpid);

        // Formulate the request
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, FLOW);
        MBodyMutableFlowStatsRequest body =
                (MBodyMutableFlowStatsRequest) req.getBody();
        body.match(getMatchAll(pv));

        // Patch-in the table id only if the protocol version requires it.
        if (pv.gt(V_1_0))
            body.tableId(tableId == null ? TableId.ALL : tableId);

        OpenflowMessage msg = req.toImmutable();
        FlowStatsFuture future = new FlowStatsFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        return future.flows;
    }

    @Override
    public void sendFlowMod(OfmFlowMod fm, DataPathId dpid)
            throws OpenflowException {
        // need to convert List<OfmFlowMod> to List<OpenflowMessage> ...
        List<OpenflowMessage> msgs = new ArrayList<>();
        msgs.addAll(validateParamsAndAdjustFlow(fm, dpid));
        listenerService.send(msgs, dpid);
    }

    @Override
    public MessageFuture sendConfirmedFlowMod(OfmFlowMod fm, DataPathId dpid)
            throws OpenflowException {
        List<OfmFlowMod> mods = validateParamsAndAdjustFlow(fm, dpid);
        MessageBatchFuture batchFuture = createBatchFuture(mods, dpid);
        ifu.sendMessageBatch(batchFuture);
        return batchFuture;
    }

    private List<OfmFlowMod> validateParamsAndAdjustFlow(OfmFlowMod fm,
                                                         DataPathId dpid)
            throws OpenflowException {
        notNull(fm, dpid);
        notMutable(fm);
        confirmMaster(dpid);
        DataPathInfo dpi = listenerService.getDataPathInfo(dpid);
        return fma.adjustFlowMod(dpi, fm);
    }

    // === GroupTracker implementation

    @Override
    public List<MBodyGroupDescStats> getGroupDescription(DataPathId dpid) {
        notNull(dpid);
        ProtocolVersion pv = dpiPv(dpid);
        verMin13(pv);
        // Formulate the request
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, GROUP_DESC);
        OpenflowMessage msg = req.toImmutable();
        GroupDescFuture future = new GroupDescFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        return future.groups;
    }

    @Override
    public MBodyGroupDescStats getGroupDescription(DataPathId dpid,
                                                   GroupId groupId) {
        notNull(groupId);
        /* IMPLEMENTATION NOTE: In the future, we will be caching this info
         * so will be able to return just the specified group data efficiently.
         * With no cache now, we have to get all and throw away all but one.
         */
        // Request body of group desc is empty; we need to get all descriptions.
        List<MBodyGroupDescStats> groups = getGroupDescription(dpid);
        for (MBodyGroupDescStats g: groups)
            if (g.getGroupId().equals(groupId))
                return g;
        throw new NotFoundException(E_NO_GROUP + groupId + E_ON_DEV + dpid);
    }

    @Override
    public MessageFuture sendGroupMod(OfmGroupMod groupMod, DataPathId dpid)
            throws OpenflowException {
        notNull(groupMod, dpid);
        notMutable(groupMod);
        ProtocolVersion pv = dpiMasterPv(dpid);
        verMin13(pv);

        DataPathMessageFuture future =
                new DataPathMessageFuture(groupMod, dpid);

        // send the message and follow up barrier request
        listenerService.sendFuture(future, groupMod, barrierRequest(groupMod));

        // return the future to the caller
        return future;
    }

    @Override
    public List<MBodyGroupStats> getGroupStats(DataPathId dpid) {
        notNull(dpid);
        ProtocolVersion pv = dpiPv(dpid);
        verMin13(pv);
        // Formulate the request
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, GROUP);
        OpenflowMessage msg = req.toImmutable();
        GroupStatsFuture future = new GroupStatsFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        return future.stats;
    }

    @Override
    public MBodyGroupStats getGroupStats(DataPathId dpid, GroupId groupId) {
        notNull(dpid, groupId);
        ProtocolVersion pv = dpiPv(dpid);
        verMin13(pv);
        // Formulate the request for the specified group
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, GROUP);
        MBodyMutableGroupStatsRequest body =
                (MBodyMutableGroupStatsRequest) req.getBody();
        body.groupId(groupId);
        OpenflowMessage msg = req.toImmutable();
        GroupStatsFuture future = new GroupStatsFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        List<MBodyGroupStats> gs = future.stats;
        if (gs.size() > 0)
            return gs.get(0);
        throw new NotFoundException(E_NO_GROUP + groupId + E_ON_DEV + dpid);
    }

    @Override
    public MBodyGroupFeatures getGroupFeatures(DataPathId dpid) {
        notNull(dpid);
        ProtocolVersion pv = dpiPv(dpid);
        verMin13(pv);

        OpenflowMessage msg =
                create(pv, MULTIPART_REQUEST, GROUP_FEATURES).toImmutable();
        GroupFeaturesFuture future = new GroupFeaturesFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        return future.features;
    }

    // === MeterTracker implementation

    @Override
    public List<MBodyMeterConfig> getMeterConfig(DataPathId dpid) {
        notNull(dpid);
        ProtocolVersion pv = dpiPv(dpid);
        verMin13(pv);

        // Formulate the request
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, METER_CONFIG);
        OpenflowMessage msg = req.toImmutable();
        MeterConfigFuture future = new MeterConfigFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        return future.meters;
    }

    @Override
    public MBodyMeterConfig getMeterConfig(DataPathId dpid, MeterId meterId) {
        notNull(dpid, meterId);
        ProtocolVersion pv = dpiPv(dpid);
        verMin13(pv);

        // Formulate the request for the specified group
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, METER_CONFIG);
        MBodyMutableMeterConfigRequest body =
                (MBodyMutableMeterConfigRequest) req.getBody();
        body.meterId(meterId);
        OpenflowMessage msg = req.toImmutable();
        MeterConfigFuture future = new MeterConfigFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        List<MBodyMeterConfig> ms = future.meters;
        if (ms.size() > 0)
            return ms.get(0);
        throw new NotFoundException(E_NO_METER + meterId + E_ON_DEV + dpid);
    }

    @Override
    public List<MBodyMeterStats> getMeterStats(DataPathId dpid) {
        notNull(dpid);
        ProtocolVersion pv = dpiPv(dpid);
        verMin13(pv);

        // Formulate the request
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, METER);
        OpenflowMessage msg = req.toImmutable();
        MeterStatsFuture future = new MeterStatsFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        return future.meters;
    }

    @Override
    public MBodyMeterStats getMeterStats(DataPathId dpid, MeterId meterId) {
        notNull(dpid, meterId);
        ProtocolVersion pv = dpiPv(dpid);
        verMin13(pv);
        // Formulate the request for the specified group
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, METER);
        MBodyMutableMeterStatsRequest body =
                (MBodyMutableMeterStatsRequest) req.getBody();
        body.meterId(meterId);
        OpenflowMessage msg = req.toImmutable();
        MeterStatsFuture future = new MeterStatsFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        List<MBodyMeterStats> ms = future.meters;
        if (ms.size() > 0)
            return ms.get(0);
        throw new NotFoundException(E_NO_METER + meterId + E_ON_DEV + dpid);
    }

    @Override
    public MBodyMeterFeatures getMeterFeatures(DataPathId dpid) {
        notNull(dpid);
        ProtocolVersion pv = dpiPv(dpid);
        verMin13(pv);

        OpenflowMessage msg =
                create(pv, MULTIPART_REQUEST, METER_FEATURES).toImmutable();
        MeterFeaturesFuture future = new MeterFeaturesFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        return future.features;
    }

    @Override
    public List<MBodyExperimenter> getExperimenter(DataPathId dpid) {
        notNull(dpid);
        ProtocolVersion pv = dpiPv(dpid);

        // Formulate the request
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                create(pv, MULTIPART_REQUEST, MultipartType.EXPERIMENTER);
        /* IMPLEMENTATION NOTE: For OF 1.0 the rate-limiter experimenter
         * request requires us to set the vendor ID and a body of four bytes.
         * Hence the code below.
         */
        if (pv == V_1_0) {
            MBodyMutableExperimenter body =
                    (MBodyMutableExperimenter) req.getBody();
            body.expId(ExperimenterId.HP);
            body.data(M_EXP_BODY);
        }

        OpenflowMessage msg = req.toImmutable();
        ExpFuture future = new ExpFuture(msg, dpid);

        try {
            listenerService.sendFuture(future, msg);
        } catch (OpenflowException e) {
            log.error(LM_FAILED_SEND, msg, Log.stackTraceSnippet(e));
            throw new IllegalStateException(e);
        }

        await(future);
        return future.exp;
    }

    @Override
    public MessageFuture sendMeterMod(OfmMeterMod meterMod, DataPathId dpid)
            throws OpenflowException {
        notNull(meterMod, dpid);
        notMutable(meterMod);
        ProtocolVersion pv = dpiMasterPv(dpid);
        verMin13(pv);

        DataPathMessageFuture future =
                new DataPathMessageFuture(meterMod, dpid);

        // send the message and follow up barrier request
        listenerService.sendFuture(future, meterMod, barrierRequest(meterMod));

        // return the future to the caller
        return future;
    }


    // =======================================================================
    // Only want to hear about Flow-Related messages. Here's the filter.
    private static final Set<MessageType> FLOW_RELATED_TYPES =
            EnumSet.of(MULTIPART_REPLY, BARRIER_REPLY, ERROR, FLOW_REMOVED);

    /**
     * Initializes the sub-component with a reference to required services.
     * This method also registers message and datapath listeners if the
     * subclass has overridden the appropriate methods.
     *
     * @param ls the listener service
     * @param fma the flow mod advisor
     * @param ra the role advisor
     * @param ed the event dispatcher
     * @param pr the pipeline reader
     * @param cfg configuration parameters
     * @param cs the controller service
     * @return self, for chaining
     */
    protected AbstractSubComponent init(ListenerService ls,
                                        FlowModAdvisor fma,
                                        RoleAdvisor ra,
                                        EventDispatcher ed,
                                        PipelineReader pr,
                                        ControllerConfig cfg,
                                        ControllerService cs) {
        super.init(ls, ra);
        this.fma = fma;
        this.ed = ed;
        this.pr = pr;
        this.cs = cs;
        updateNonBounceConfig(cfg);
        ifu = new InitialFlowUtils(log, listenerService, fma);
        return this;
    }

    @Override
    protected AbstractSubComponent init(ListenerService ls, RoleAdvisor ra) {
        throw new UnsupportedOperationException(E_USE_OTHER_INIT);
    }

    @Override
    protected void updateNonBounceConfig(ControllerConfig cfg) {
        hybridOpenflow = cfg.hybridMode();
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    @Override
    protected MessageListener getMyMessageListener() {
        return msgListener;
    }

    @Override
    protected DataPathListener getMyDataPathListener() {
        return dpListener;
    }

    @Override
    public Set<MessageType> getMessageTypes() {
        return FLOW_RELATED_TYPES;
    }

    // ============================================================

    // our message listener implementation
    private class MsgListener implements MessageListener {
        @Override
        public void queueEvent(QueueEvent event) {
            // do nothing
        }

        @Override
        public void event(MessageEvent msgEvent) {
            if (msgEvent.type() == OpenflowEventType.MESSAGE_RX) {
                OpenflowMessage msg = msgEvent.msg();
                DataPathId dpid = msgEvent.dpid();
                switch (msg.getType()) {
                    case MULTIPART_REPLY:
                        handleMpReply((OfmMultipartReply) msg, dpid);
                        break;

                    case BARRIER_REPLY:
                        handleBarrierReply((OfmBarrierReply) msg, dpid);
                        break;

                    case ERROR:
                        handleErrorMsg((OfmError) msg, dpid);
                        break;

                    case FLOW_REMOVED:
                        handleFlowRemoved((OfmFlowRemoved) msg, dpid);
                        break;

                    default:
                        //do nothing
                        break;
                }
            }
        }
    }

    private void handleFlowRemoved(OfmFlowRemoved msg, DataPathId dpid) {
        ed.post(new FlowEvt(FlowEventType.FLOW_REMOVED, dpid, msg));
    }

    private void handleBarrierReply(OfmBarrierReply reply, DataPathId dpid) {
        DataPathMessageFuture f = listenerService.findFuture(reply, dpid);
        if (f != null) {
            listenerService.successFuture(f, reply);
            switch (f.request().getType()) {
                case FLOW_MOD:
                    ed.post(new FlowEvt(FlowEventType.FLOW_MOD_PUSHED, dpid,
                            (OfmFlowMod) f.request()));
                    break;
                case GROUP_MOD:
                    ed.post(new GroupEvt(GroupEventType.GROUP_MOD_PUSHED, dpid,
                            (OfmGroupMod) f.request()));
                    break;
                case METER_MOD:
                    ed.post(new MeterEvt(MeterEventType.METER_MOD_PUSHED, dpid,
                            (OfmMeterMod) f.request()));
                    break;
                default:
                    // ignore all others
                    break;
            }
        }
    }

    private void handleErrorMsg(OfmError reply, DataPathId dpid) {
        DataPathMessageFuture f = listenerService.findFuture(reply, dpid);
        if (f != null) {
            listenerService.failFuture(f, reply);
            switch (f.request().getType()) {
                case FLOW_MOD:
                    ed.post(new FlowEvt(FlowEventType.FLOW_MOD_PUSH_FAILED,
                            dpid, (OfmFlowMod) f.request()));
                    break;
                case GROUP_MOD:
                    ed.post(new GroupEvt(GroupEventType.GROUP_MOD_PUSH_FAILED,
                            dpid, (OfmGroupMod) f.request()));
                    break;
                case METER_MOD:
                    ed.post(new MeterEvt(MeterEventType.METER_MOD_PUSH_FAILED,
                            dpid, (OfmMeterMod) f.request()));
                    break;
                default:
                    // ignore all others
                    break;
            }
        }
    }

    // Methods handling incoming MP Reply messages
    private void handleMpReply(OfmMultipartReply reply, DataPathId dpid) {
        switch (reply.getMultipartType()) {
            case FLOW:
                collateFlows(reply, dpid);
                break;
            case GROUP_DESC:
                collateGroupDesc(reply, dpid);
                break;
            case GROUP:
                collateGroupStats(reply, dpid);
                break;
            case GROUP_FEATURES:
                collateGroupFeatures(reply, dpid);
                break;
            case METER_CONFIG:
                collateMeterConfig(reply, dpid);
                break;
            case METER:
                collateMeterStats(reply, dpid);
                break;
            case METER_FEATURES:
                collateMeterFeatures(reply, dpid);
                break;
            case EXPERIMENTER:
                collateExperimenter(reply, dpid);
                break;
            default:
                // ignore all other type of reply
                break;
        }
    }

    private void collateFlows(OfmMultipartReply reply, DataPathId dpid) {
        FlowStatsFuture f = (FlowStatsFuture)
                listenerService.findFuture(reply, dpid);
        if (f != null) {
            // add the payload to the future
            MBodyFlowStats.Array array = (MBodyFlowStats.Array) reply.getBody();
            f.flows.addAll(array.getList());
            // add a pseudo entry if stats are incomplete
            if (array.incomplete()) {
                Throwable cause = array.parseErrorCause();
                ProtocolVersion pv = array.getVersion();
                f.flows.add(MpBodyFactory.createSyntheticFlowStats(pv, cause));
            }

            // If we do not expect any more replies, satisfy the future
            if (!reply.hasMore())
                listenerService.successFuture(f, reply);
        }
    }

    private void collateGroupDesc(OfmMultipartReply reply, DataPathId dpid) {
        GroupDescFuture f = (GroupDescFuture)
                listenerService.findFuture(reply, dpid);
        if (f != null) {
            // add the payload to the future
            f.groups.addAll(
                    ((MBodyGroupDescStats.Array) reply.getBody()).getList());

            // If we do not expect any more replies, satisfy the future
            if (!reply.hasMore())
                listenerService.successFuture(f, reply);
        }
    }

    private void collateGroupStats(OfmMultipartReply reply, DataPathId dpid) {
        GroupStatsFuture f = (GroupStatsFuture)
                listenerService.findFuture(reply, dpid);
        if (f != null) {
            // add the payload to the future
            f.stats.addAll(((MBodyGroupStats.Array) reply.getBody()).getList());

            // If we do not expect any more replies, satisfy the future
            if (!reply.hasMore())
                listenerService.successFuture(f, reply);
        }
    }

    private void collateGroupFeatures(OfmMultipartReply reply,
                DataPathId dpid) {
        GroupFeaturesFuture f = (GroupFeaturesFuture)
                listenerService.findFuture(reply, dpid);
        if (f != null) {
            f.features = (MBodyGroupFeatures) reply.getBody();
            listenerService.successFuture(f, reply);
        }
    }

    private void collateMeterConfig(OfmMultipartReply reply, DataPathId dpid) {
        MeterConfigFuture f = (MeterConfigFuture)
                listenerService.findFuture(reply, dpid);
        if (f != null) {
            // add the payload to the future
            f.meters.addAll(
                    ((MBodyMeterConfig.Array) reply.getBody()).getList());

            // If we do not expect any more replies, satisfy the future
            if (!reply.hasMore())
                listenerService.successFuture(f, reply);
        }
    }

    private void collateMeterStats(OfmMultipartReply reply, DataPathId dpid) {
        MeterStatsFuture f = (MeterStatsFuture)
                listenerService.findFuture(reply, dpid);
        if (f != null) {
            // add the payload to the future
            f.meters.addAll(
                    ((MBodyMeterStats.Array) reply.getBody()).getList());

            // If we do not expect any more replies, satisfy the future
            if (!reply.hasMore())
                listenerService.successFuture(f, reply);
        }
    }

    private void collateMeterFeatures(OfmMultipartReply reply,
                DataPathId dpid) {
        MeterFeaturesFuture f = (MeterFeaturesFuture)
                listenerService.findFuture(reply, dpid);
        if (f != null) {
            f.features = (MBodyMeterFeatures) reply.getBody();
            listenerService.successFuture(f, reply);
        }
    }

    private void collateExperimenter(OfmMultipartReply reply, DataPathId dpid) {
        ExpFuture f = (ExpFuture) listenerService.findFuture(reply, dpid);
        if (f != null) {
            // add the payload to the future
            f.exp.add(((MBodyExperimenter) reply.getBody()));

            // If we do not expect any more replies, satisfy the future
            if (!reply.hasMore())
                listenerService.successFuture(f, reply);
        }
    }

    // ====================================================================

    /** Our DataPath listener implementation that will be used to register
     with the controller to listen for DataPath-Related events.*/
    class DpListener implements DataPathListener {

        @Override
        public void queueEvent(QueueEvent event) {
            // do nothing
        }

        @Override
        public void event(DataPathEvent dpEvent) {
            OpenflowEventType type  = dpEvent.type();

            //process OpenFlow DataPath events
            switch (type) {
                // NOTE: we are now signaled directly, so no longer need
                //   to listen for DATAPATH_CONNECTED events

                case DATAPATH_DISCONNECTED:
                    handleDpDisconnectedEvt(dpEvent);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Sends "delete all flowmods" command to the specified datapath.
     * This is an internal method, which should only be invoked by the
     * post-handshake task, just prior to installing initial flows. Note
     * that this method blocks until the delete method has been confirmed
     * via barrier request.
     *
     * @param dpid the target datapath
     */
    protected void purgeFlows(DataPathId dpid) {
        confirmMaster(dpid);
        DataPathInfo dpi = listenerService.getDataPathInfo(dpid);

        // TODO: consider adding getDeleteAll() to FlowModFacet (but for now...)
        OfmFlowMod deleteAllFlows = createDeleteAllFlowMod(dpi.negotiated());
        List<OfmFlowMod> mods = new ArrayList<>(1);

        mods.add(deleteAllFlows);
        MessageBatchFuture batch = MessageBatchFuture.createBatchFuture(mods, dpid);

        if (ifu.sendMessageBatch(batch))
                ifu.waitForBarrierReply(batch, "Purge All Flows");
    }

    private OfmFlowMod createDeleteAllFlowMod(ProtocolVersion pv) {
        OfmMutableFlowMod fm = (OfmMutableFlowMod)
                create(pv, FLOW_MOD, FlowModCommand.DELETE);
        fm.match(getMatchAll(pv));
        if (pv.gt(V_1_0))
            fm.tableId(TableId.ALL);
        /*
         * Note: default values for OfmMutableFlowMod:
         *  - OutPort : Port.ANY
         *  - OutGroup : GroupId.ANY
         *  - CookieMask : 0
         */
        return (OfmFlowMod) fm.toImmutable();
    }

    protected boolean pushDefaultFlows(DataPathId dpid,
                                       List<OfmFlowMod> contributedFlows) {
        return ifu.pushDefaultFlows(dpid, contributedFlows,
                                    pr.getDefinition(dpid), hybridOpenflow);
    }

    private void handleDpDisconnectedEvt(DataPathEvent dpEvent) {
        ((PipelineMgmt) pr).removeDefinition(dpEvent.dpid());
    }


    // =======================================================================
    // Custom message futures which can collate multipart replies

    // Message future used to track requests for flow stats
    private static class FlowStatsFuture extends DataPathMessageFuture {
        private final List<MBodyFlowStats> flows = new ArrayList<>();

        private FlowStatsFuture(OpenflowMessage request, DataPathId dpid) {
            super(request, dpid);
        }
    }

    // Message future used to track requests for group descriptions
    private static class GroupDescFuture extends DataPathMessageFuture {
        private final List<MBodyGroupDescStats> groups = new ArrayList<>();

        private GroupDescFuture(OpenflowMessage request, DataPathId dpid) {
            super(request, dpid);
        }
    }

    // Message future used to track requests for group statistics
    private static class GroupStatsFuture extends DataPathMessageFuture {
        private final List<MBodyGroupStats> stats = new ArrayList<>();

        private GroupStatsFuture(OpenflowMessage request, DataPathId dpid) {
            super(request, dpid);
        }
    }

    // Message future used to track requests for group statistics
    private static class GroupFeaturesFuture extends DataPathMessageFuture {
        private MBodyGroupFeatures features;

        private GroupFeaturesFuture(OpenflowMessage request, DataPathId dpid) {
            super(request, dpid);
        }
    }

    // Message future used to track requests for meter configuration
    private static class MeterConfigFuture extends DataPathMessageFuture {
        private final List<MBodyMeterConfig> meters = new ArrayList<>();

        private MeterConfigFuture(OpenflowMessage request, DataPathId dpid) {
            super(request, dpid);
        }
    }

    // Message future used to track requests for meter statistics
    private static class MeterStatsFuture extends DataPathMessageFuture {
        private final List<MBodyMeterStats> meters = new ArrayList<>();

        private MeterStatsFuture(OpenflowMessage request, DataPathId dpid) {
            super(request, dpid);
        }
    }

    // Message future used to track requests for meter features
    private static class MeterFeaturesFuture extends DataPathMessageFuture {
        private MBodyMeterFeatures features;

        private MeterFeaturesFuture(OpenflowMessage request, DataPathId dpid) {
            super(request, dpid);
        }
    }

    // Message future used to track requests for experimenter details
    private static class ExpFuture extends DataPathMessageFuture {
        private final List<MBodyExperimenter> exp =
                new ArrayList<>();

        private ExpFuture(OpenflowMessage request, DataPathId dpid) {
            super(request, dpid);
        }
    }
}
