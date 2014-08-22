/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.*;
import org.opendaylight.of.controller.flow.FlowListener;
import org.opendaylight.of.controller.flow.GroupListener;
import org.opendaylight.of.controller.flow.MeterListener;
import org.opendaylight.of.controller.flow.impl.FlowTrk;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pipeline.PipelineReader;
import org.opendaylight.of.controller.pipeline.impl.PipelineManager;
import org.opendaylight.of.controller.pkt.PacketSequencer;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.controller.pkt.SplMetric;
import org.opendaylight.of.controller.pkt.impl.Sequencer;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.event.EventDispatcher;
import org.opendaylight.util.event.EventSinkBroker;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.packet.ProtocolId;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_IN;

/**
 * Provides the implementation of the {@link ControllerService}.
 * Note that this class represents a thin facade which delegates to the
 * appropriate sub-component of the controller, or to the core-controller
 * itself.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Radhika Hegde
 * @author Shruthy Mohanram
 */
public class ControllerManager implements ControllerService {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ControllerManager.class, "controllerManager");
    private static final String E_PACKET_IN = RES.getString("e_packet_in");

    private static final String E_NOT_AUTH = "Caller is not authorized";
    private static final String CKPT_DP_TYPED =
            "Datapath {} -- Type Determined [{}]";
    private static final String CKPT_DP_READY =
            "Datapath {} -- READY!!";
    private static final String CKPT_DP_DEF_FLOWS_FAILED =
            "Datapath {} -- Failed to install default flows :(";


    // core controller...
    // TODO:  Consider using ListenerManager
    private ListenerService ls;

    // controller sub-components...
    private EventManager eventManager;
    private MessageSender msgSender;
    private PacketSequencer packetSequencer;


    private PipelineReader pipelineReader;
    // TODO : split out Flows/Groups/Meters into separate modules
    //  and use the interface reference, not the implementing class
    private FlowTrk flowTrk;
    private boolean hybridMode;

    private final InitialFlowManager iFlowMgr;
    private final PostHandshakeCallback phc = new MyPostHandshakeCallback();

    /**
     * Creates and initializes the controller manager for pure OpenFlow mode.
     */
    protected ControllerManager() {
        hybridMode = false;
        iFlowMgr = new InitialFlowManager();
    }

    /**
     * Constructs and initializes the Controller Manager by instantiating
     * its sub-components and stitching them together. The caller must provide
     * the configuration parameters, including listen port for OpenFlow
     * connections, etc., an {@link AlertSink} implementation for
     * any internally generated alerts, and a {@link RoleAdvisor}
     * implementation to provide teaming related advice.  The controller will
     * not accept connections.  See the {@link ControllerMx} to enable the
     * processing of incoming connections.
     *
     * @param cfg the controller configuration parameters
     * @param as injected alert sink
     * @param ph injected post handshake sink
     * @param fma injected flow mod advisor
     * @param ra injected role advisor
     * @param eds injected event dispatch service
     */
    public ControllerManager(ControllerConfig cfg, AlertSink as,
                             PostHandshakeSink ph, FlowModAdvisor fma,
                             RoleAdvisor ra, EventDispatchService eds) {
        this(cfg);
        init(cfg, as, ph, fma, ra, eds);
    }

    /**
     * Constructs, but does not initialize, the Controller Manager.
     * The caller must provide the configuration parameters, including listen
     * port for OpenFlow connections, etc.  The controller will not accept
     * connections.  See the {@link ControllerMx} to enable the processing of
     * incoming connections.
     *
     * @param cfg the controller configuration parameters
     */
    public ControllerManager(ControllerConfig cfg) {
        hybridMode = cfg.hybridMode();
        iFlowMgr = new InitialFlowManager();
    }

    /**
     * Initializes the Controller Manager by instantiating its sub-components
     * and stitching them together. The caller must provide the
     * configuration parameters, including listen port for OpenFlow
     * connections, etc., a {@link RoleAdvisor}
     * implementation to provide teaming related advice.  The controller will
     * not accept connections.  See the {@link ControllerMx} to enable the
     * processing of incoming connections.
     *
     * @param cfg the controller configuration parameters
     * @param as injected alert sink
     * @param ph post handshake sink
     * @param ra injected role advisor
     * @param eds injected event dispatch service
     */
    public void init(ControllerConfig cfg, AlertSink as, PostHandshakeSink ph,
                     FlowModAdvisor fma, RoleAdvisor ra,
                     EventDispatchService eds) {
        ls = new ListenerManager(cfg, as, ph, phc, fma, ra);
        eventManager = createAndInitEventManager(eds);
        msgSender = createAndInitMessageSender(ls, ra);
        packetSequencer = createAndInitPacketSequencer(ls, ra);
        pipelineReader = createAndInitPipelineReader(ls, ra);
        flowTrk = createAndInitFlowTrk(ls, fma, ra, eds, pipelineReader, cfg);
    }

    /**
     * Starts the IO processing loop on the Open Flow controller.
     */
    protected void startIOProcessing() {
        ((ListenerManager)ls).startIOProcessing();
    }

    /** Creates, initializes and returns the event manager component.
     *
     * @param esb injected event sink broker
     * @return the event manager
     */
    protected EventManager createAndInitEventManager(EventSinkBroker esb) {
        return new EventManager().init(esb);
    }

    /** Creates, initializes and returns the message sender component.
     *
     * @param ls injected listener service
     * @param ra injected role advisor
     * @return the message sender
     */
    protected MessageSender createAndInitMessageSender(ListenerService ls,
                                                       RoleAdvisor ra) {
        return (MessageSender) new MsgSender().init(ls, ra);
    }

    /** Creates and returns the packet sequencer component.
     *
     * @param ls injected listener service
     * @param ra injected role advisor
     * @return the packet sequencer
     */
    protected PacketSequencer createAndInitPacketSequencer(ListenerService ls,
                                                           RoleAdvisor ra) {
        // different package, so need local inner class to reach across..
        return new MySequencer().initialize(ls, ra);
    }

    /** Creates and returns the pipeline reader component.
     *
     * @param ls injected listener service
     * @param ra injected role advisor
     * @return the pipeline reader
     */
    protected PipelineReader createAndInitPipelineReader(ListenerService ls,
                                                         RoleAdvisor ra) {
        // different package, so need local inner class to reach across..
        return new MyPipelineReader().initialize(ls, ra);
    }

    /** Creates and returns the flow tracker component.
     *
     * @param ls injected listener service
     * @param fma injected flow mod advisor
     * @param ra injected role advisor
     * @param ed injected event dispatcher
     * @param pr injected pipeline reader
     * @param cfg controller configuration descriptor
     * @return the flow tracker
     */
    // TODO: split out FLOWS/GROUPS/METERS
    protected FlowTrk createAndInitFlowTrk(ListenerService ls,
                                           FlowModAdvisor fma,
                                           RoleAdvisor ra,
                                           EventDispatcher ed,
                                           PipelineReader pr,
                                           ControllerConfig cfg) {
        // different package, so need local inner class to reach across..
        return new MyFlowTrk().initialize(ls, fma, ra, ed, pr, cfg, this);
    }

    /**
     * Prompts the flow tracker to install the default flows on the specified
     * device.
     *
     * @param dpid the target datapath
     * @param deviceTypeName the device type name
     */
    private void setTypeAndPushDefaultFlows(DataPathId dpid,
                                              String deviceTypeName) {
        ((ListenerManager) ls).associateDeviceType(dpid, deviceTypeName);

        // inject a DP-TYPED checkpoint into the TX/RX rec.
        String msg = StringUtils.format(CKPT_DP_TYPED, dpid, deviceTypeName);
        ((ListenerManager) ls).txRxCheckpoint(msg);

        // tell flow tracker to clear all flows before starting
        ((MyFlowTrk) flowTrk).purgeFlows(dpid); // BLOCKS

        // tell flow tracker to install default flows
        DataPathInfo dataPathInfo = getDataPathInfo(dpid);
        boolean ok = dataPathInfo != null &&
                ((MyFlowTrk) flowTrk).pushDefaultFlows(dpid, getContributedFlows(dataPathInfo));
        if (ok) {
            msg = StringUtils.format(CKPT_DP_READY, dpid);
            ((ListenerManager) ls).txRxCheckpoint(msg);
            ((ListenerManager) ls).signalDataPathReady(dpid);
        } else {
            msg = StringUtils.format(CKPT_DP_DEF_FLOWS_FAILED, dpid);
            ((ListenerManager) ls).txRxCheckpoint(msg);
        }
    }

    /**
     * Returns the collected list of contributed flows that should be installed
     * on a newly connected datapath.
     *
     * @param dpi the target datapath info
     * @return the list of flows to be installed
     */
    protected List<OfmFlowMod> getContributedFlows(DataPathInfo dpi) {
        return iFlowMgr.collateFlowMods(dpi, hybridMode);
    }

    /**
     * Updates controller configuration parameters; invoked when a bounce is
     * not required.
     *
     * @param cfg the new configuration
     */
    protected void updateNonBounceConfig(ControllerConfig cfg) {
        ((ListenerManager) ls).updateNonBounceConfig(cfg);
        ((MyFlowTrk) flowTrk).updateNonBounceConfig(cfg);

        // FIXME: pass the configuration on to those who need it.
    }

    /** Gracefully shuts down the controller. */
    protected void shutdown() {
        // note: for symmetry, we shut down in reverse order of initialization
        ((AbstractSubComponent) flowTrk).shutdown();
        ((AbstractSubComponent) pipelineReader).shutdown();
        ((AbstractSubComponent) packetSequencer).shutdown();
        ((AbstractSubComponent) msgSender).shutdown();
        eventManager.shutdown();
        ((ListenerManager) ls).shutdown();
    }

    // ======================================================================
    // === Unit test support

    /** Returns a reference to our listener service implementation.
     *
     * @return the listener manager
     */
    ListenerManager getListenerManager() {
        return (ListenerManager) this.ls;
    }

    /** Returns the pipeline reader implementation for unit tests.
     *
     * @return the pipeline manager
     */
    PipelineReader getPipelineMgr() {
        return pipelineReader;
    }

    // ======================================================================
    // === Implementation of ControllerService

    // Just delegate to the appropriate sub-component

    @Override
    public void addPacketListener(SequencedPacketListener listener,
                                  SequencedPacketListenerRole role, int altitude) {
        packetSequencer.addPacketListener(listener, role, altitude);
    }

    @Override
    public void addPacketListener(SequencedPacketListener listener,
                                  SequencedPacketListenerRole role, int altitude,
                                  Set<ProtocolId> interest) {
        packetSequencer.addPacketListener(listener, role, altitude, interest);
    }

    @Override
    public void removePacketListener(SequencedPacketListener listener) {
        packetSequencer.removePacketListener(listener);
    }

    @Override
    public List<SplMetric> getSplMetrics() {
        return packetSequencer.getSplMetrics();
    }

    @Override
    public void addMessageListener(MessageListener listener,
                                   Set<MessageType> types) {
        // do not allow caller to register for Packet-In messages
        if (types != null && types.contains(PACKET_IN))
            throw new IllegalArgumentException(E_PACKET_IN);

        ls.addMessageListener(listener, types);
    }

    @Override
    public void removeMessageListener(MessageListener listener) {
        ls.removeMessageListener(listener);
    }

    @Override
    public void addDataPathListener(DataPathListener listener) {
        ls.addDataPathListener(listener);
    }

    @Override
    public void removeDataPathListener(DataPathListener listener) {
        ls.removeDataPathListener(listener);
    }

    @Override
    public Set<DataPathInfo> getAllDataPathInfo() {
        return ls.getAllDataPathInfo();
    }

    @Override
    public DataPathInfo getDataPathInfo(DataPathId dpid) {
        return ls.getDataPathInfo(dpid);
    }

    @Override
    public ProtocolVersion versionOf(DataPathId dpid) {
        return ls.versionOf(dpid);
    }

    @Override
    public ControllerStats getStats() {
        return ls.getStats();
    }

    @Override
    public List<MBodyPortStats> getPortStats(DataPathId dpid) {
        return ls.getPortStats(dpid);
    }

    @Override
    public MBodyPortStats getPortStats(DataPathId dpid, BigPortNumber port) {
        return ls.getPortStats(dpid, port);
    }

    @Override
    public MessageFuture enablePort(DataPathId dpid, BigPortNumber port,
                                    boolean enable) {
        return ls.enablePort(dpid, port, enable);
    }

    @Override
    public MessageFuture send(OpenflowMessage msg, DataPathId dpid)
            throws OpenflowException {
        return msgSender.send(msg, dpid);
    }

    @Override
    public List<MessageFuture> send(List<OpenflowMessage> msgs, DataPathId dpid)
            throws OpenflowException {
        return msgSender.send(msgs, dpid);
    }

    @Override
    public synchronized ControllerMx getControllerMx() {
        return ((ListenerManager) ls).getMx();
    }

    @Override
    public List<MBodyFlowStats> getFlowStats(DataPathId dpid, TableId tableId) {
        return flowTrk.getFlowStats(dpid, tableId);
    }

    @Override
    public void registerInitialFlowContributor(InitialFlowContributor ifc) {
        iFlowMgr.register(ifc);
    }

    @Override
    public void unregisterInitialFlowContributor(InitialFlowContributor ifc) {
        iFlowMgr.unregister(ifc);
    }

    @Override
    public void sendFlowMod(OfmFlowMod flowMod, DataPathId dpid)
            throws OpenflowException {
        flowTrk.sendFlowMod(flowMod, dpid);
    }

    @Override
    public MessageFuture sendConfirmedFlowMod(OfmFlowMod flowMod, 
                                              DataPathId dpid)
            throws OpenflowException {
        return flowTrk.sendConfirmedFlowMod(flowMod, dpid);
    }

    @Override
    public void addFlowListener(FlowListener listener) {
        notNull(listener);
        eventManager.add(listener, eventManager.flowSink);
    }

    @Override
    public void removeFlowListener(FlowListener listener) {
        notNull(listener);
        eventManager.remove(listener, eventManager.flowSink);
    }

    @Override
    public List<MBodyGroupDescStats> getGroupDescription(DataPathId dpid) {
        return flowTrk.getGroupDescription(dpid);
    }

    @Override
    public MBodyGroupDescStats getGroupDescription(DataPathId dpid,
                                                   GroupId groupId) {
        return flowTrk.getGroupDescription(dpid, groupId);
    }

    @Override
    public List<MBodyGroupStats> getGroupStats(DataPathId dpid) {
        return flowTrk.getGroupStats(dpid);
    }

    @Override
    public MBodyGroupStats getGroupStats(DataPathId dpid, GroupId groupId) {
        return flowTrk.getGroupStats(dpid, groupId);
    }

    @Override
    public MBodyGroupFeatures getGroupFeatures(DataPathId dpid) {
        return flowTrk.getGroupFeatures(dpid);
    }

    @Override
    public MessageFuture sendGroupMod(OfmGroupMod groupMod, DataPathId dpid)
            throws OpenflowException {
        return flowTrk.sendGroupMod(groupMod, dpid);
    }

    @Override
    public void addGroupListener(GroupListener listener) {
        notNull(listener);
        eventManager.add(listener, eventManager.groupSink);
    }

    @Override
    public void removeGroupListener(GroupListener listener) {
        notNull(listener);
        eventManager.remove(listener, eventManager.groupSink);
    }

    @Override
    public List<MBodyMeterConfig> getMeterConfig(DataPathId dpid) {
        return flowTrk.getMeterConfig(dpid);
    }

    @Override
    public MBodyMeterConfig getMeterConfig(DataPathId dpid, MeterId meterId) {
        return flowTrk.getMeterConfig(dpid, meterId);
    }

    @Override
    public List<MBodyMeterStats> getMeterStats(DataPathId dpid) {
        return flowTrk.getMeterStats(dpid);
    }

    @Override
    public MBodyMeterStats getMeterStats(DataPathId dpid, MeterId meterId) {
        return flowTrk.getMeterStats(dpid, meterId);
    }

    @Override
    public MBodyMeterFeatures getMeterFeatures(DataPathId dpid) {
        return flowTrk.getMeterFeatures(dpid);
    }

    @Override
    public List<MBodyExperimenter> getExperimenter(DataPathId dpid) {
        return flowTrk.getExperimenter(dpid);
    }

    @Override
    public MessageFuture sendMeterMod(OfmMeterMod meterMod, DataPathId dpid)
            throws OpenflowException {
        return flowTrk.sendMeterMod(meterMod, dpid);
    }

    @Override
    public void addMeterListener(MeterListener listener) {
        notNull(listener);
        eventManager.add(listener, eventManager.meterSink);
    }

    @Override
    public void removeMeterListener(MeterListener listener) {
        notNull(listener);
        eventManager.remove(listener, eventManager.meterSink);
    }

    @Override
    public PipelineDefinition getPipelineDefinition(DataPathId dpid) {
        return pipelineReader.getDefinition(dpid);
    }

    @Override
    public boolean isHybridMode() {
        return hybridMode;
    }


    // ========================================================================
    // === Sub-Component extended classes, to allow access to init()/shutdown()
    // ===  Required because the extended classes are in a different package

    private static class MySequencer extends Sequencer {
        private PacketSequencer initialize(ListenerService ls, RoleAdvisor ra) {
            super.init(ls, ra);
            ListenerManager lm = (ListenerManager) ls;
            lm.registerSequencer(this);
            return this;
        }

        @Override
        protected void shutdown() {
            super.shutdown();
        }
    }

    private static class MyPipelineReader extends PipelineManager {
        private PipelineManager initialize(ListenerService ls, RoleAdvisor ra) {
            super.init(ls, ra);
            return this;
        }

        @Override
        protected void shutdown() {
            super.shutdown();
        }
    }

    private static class MyFlowTrk extends FlowTrk {
        private FlowTrk initialize(ListenerService ls,
                                   FlowModAdvisor fma,
                                   RoleAdvisor ra,
                                   EventDispatcher ed, PipelineReader pr,
                                   ControllerConfig cfg,
                                   ControllerService cs) {
            super.init(ls, fma, ra, ed, pr, cfg, cs);
            return this;
        }

        @Override
        protected void updateNonBounceConfig(ControllerConfig cfg) {
            super.updateNonBounceConfig(cfg);
        }

        @Override
        protected void purgeFlows(DataPathId dpid) {
            super.purgeFlows(dpid);
        }

        @Override
        protected boolean pushDefaultFlows(DataPathId dpid,
                                           List<OfmFlowMod> contributedFlows) {
            return super.pushDefaultFlows(dpid, contributedFlows);
        }

        @Override
        protected void shutdown() {
            super.shutdown();
        }
    }

    private class MyPostHandshakeCallback implements PostHandshakeCallback {
        @Override
        public void handshakeComplete(DataPathId dpid, String deviceTypeName) {
            setTypeAndPushDefaultFlows(dpid, deviceTypeName);
        }
    }
}
