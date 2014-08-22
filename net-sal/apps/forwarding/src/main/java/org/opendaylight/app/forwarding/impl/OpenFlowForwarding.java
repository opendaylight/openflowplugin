/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.app.forwarding.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.opendaylight.net.device.DeviceEvent;
import org.opendaylight.net.device.DeviceListener;
import org.opendaylight.net.device.DeviceService;
import org.opendaylight.net.host.HostService;
import org.opendaylight.net.model.*;
import org.opendaylight.net.path.PathSelectionService;
import org.opendaylight.of.controller.ControllerService;
import org.opendaylight.of.controller.ErrorEvent;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.*;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;
import org.opendaylight.of.lib.match.MutableMatch;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.VlanId;
import org.opendaylight.util.packet.Ethernet;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import static org.apache.felix.scr.annotations.ReferenceCardinality.MANDATORY_UNARY;
import static org.apache.felix.scr.annotations.ReferencePolicy.DYNAMIC;
import static org.opendaylight.of.controller.pkt.SequencedPacketListenerRole.DIRECTOR;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.InstructionFactory.createMutableInstruction;
import static org.opendaylight.of.lib.instr.InstructionType.APPLY_ACTIONS;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.of.lib.msg.FlowModFlag.SEND_FLOW_REM;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.util.CommonUtils.itemSet;

/**
 * Simple reactive path provisioning and forwarding.
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
public class OpenFlowForwarding {

    private final Logger log = LoggerFactory.getLogger(OpenFlowForwarding.class);

    private static final String MSG_STARTED = "OpenFlowForwarding started";
    private static final String MSG_STOPPED = "OpenFlowForwarding stopped";
    private static final String E_SEND_FLOW = "Unable to send flow mod {} from {} due to {}";
    private static final String MSG_DELETE_FLOW = "Flow being deleted is: {}";
    private static final String E_NO_TABLE_ID = "No table id is received from PipelineDefinition.";

    private static final int ALTITUDE = 29999;
    private static final String E_ERROR = "PacketListener: {}";

    private static final int L2_PRIORITY = 29999;

    // Pre-fabbed flood action for V1.0
    private static final Action FLOOD_10 =
            createAction(V_1_0, ActionType.OUTPUT, Port.FLOOD,
                         ActOutput.CONTROLLER_NO_BUFFER);

    // Pre-fabbed flood action for V1.3
    private static final Action FLOOD_13 =
            createAction(V_1_3, ActionType.OUTPUT, Port.FLOOD,
                         ActOutput.CONTROLLER_NO_BUFFER);

    private final SequencedPacketListener packetListener = new PacketListener();
    private final SwitchListener deviceListener = new SwitchListener();

    @Reference(name = "ControllerService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile ControllerService controllerService;

    @Reference(name = "HostService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile HostService hostService;

    @Reference(name = "DeviceService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile DeviceService deviceService;

    @Reference(name = "PathSelectionService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile PathSelectionService pathSelectionService;


    private static final Set<FlowModFlag> FLAGS = EnumSet.of(SEND_FLOW_REM);

    // Make these configurable
    private long cookie = 9000;
    private int idleTimeout = 60;
    private int hardTimeout = 0;

    @Activate
    public void activate() {
        deviceService.addListener(deviceListener);
        controllerService.addPacketListener(packetListener, DIRECTOR, ALTITUDE,
                                            itemSet(ProtocolId.ARP, ProtocolId.IP));
        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        deviceService.removeListener(deviceListener);
        controllerService.removePacketListener(packetListener);
        log.info(MSG_STOPPED);
    }

    // Processes the packet-in context.
    private void processPacket(MessageContext context) {
        DataPathId dpid = context.srcEvent().dpid();

        OfmPacketIn packetIn = context.getPacketIn();
        Ethernet eth = context.decodedPacket().get(ProtocolId.ETHERNET);

        MacAddress src = eth.srcAddr();
        MacAddress dst = eth.dstAddr();
        BigPortNumber inPort = packetIn.getInPort();
        ProtocolId inner = context.decodedPacket().innermostId();

        DefaultConnectionPoint ingress =
                new DefaultConnectionPoint(DeviceId.valueOf(dpid.toString()),
                                           InterfaceId.valueOf(inPort));

        // If the destination is multicast or broadcast, flood if permissible
        if ((dst.isBroadcast() || dst.isMulticast())) {
            floodIfPossible(context, ingress);
            return;
        }

        SegmentId segmentId = getSegmentId(eth, packetIn);
        pavePathOrFlood(context, ingress, inner, src, dst, segmentId);
    }

    // Paves the path for the specified packet or floods the packet if
    // appropriate.
    private void pavePathOrFlood(MessageContext context, ConnectionPoint ingress,
                                 ProtocolId inner, MacAddress src, MacAddress dst,
                                 SegmentId segmentId) {
        // Attempt to locate the dest host; flood if not located
        Host dstHost = getHost(dst, segmentId);
        if (dstHost == null || dstHost.location() == null) {
            floodIfPossible(context, ingress);
            return;
        }

        // Attempt to locate the source host; flood if not located
        Host srcHost = getHost(src, segmentId);
        if (srcHost == null || srcHost.location() == null) {
            floodIfPossible(context, ingress);
            return;
        }

        // Locate end-to-end path
        Set<Path> paths = pathSelectionService.getPaths(srcHost, dstHost);
        if (paths == null || paths.isEmpty()) {
            floodIfPossible(context, ingress);
            return;
        }

        pavePathAndForward(paths, context, ingress, src, dst);
    }

    // Selects the first path and paves it from the specified connection
    // point and onward.
    private void pavePathAndForward(Set<Path> paths, MessageContext context,
                                    ConnectionPoint ingress,
                                    MacAddress src, MacAddress dst) {
        for (Path path : paths) {
            // Find the device in the path and forward out the next port...
            ConnectionPoint egress = pavePath(path, context, ingress, src, dst);
            if (egress != null) {
                sendTo(context, egress);
                break;
            }
        }
    }

    // Scans the given path for the specified connection point and if it
    // finds it, it paves the path from there forward for the supplied
    // packet.
    private ConnectionPoint pavePath(Path path, MessageContext context,
                                     ConnectionPoint ingress,
                                     MacAddress src, MacAddress dst) {
        ConnectionPoint egress = null;
        Iterator<Link> links = path.links().iterator();
        while (links.hasNext()) {
            Link link = links.next();

            // If the link dest matches the ingress point, skip to the
            // next link and mark it as the egress point
            if (isEqual(link.dst(), ingress) && links.hasNext()) {
                link = links.next();
                egress = link.src();
            }

            // Once we find an egress point, keep paving along the link sources.
            if (egress != null)
                paveHop(context, link.src(), src, dst);
        }
        return egress;
    }

    // Returns true if the two connection points are equivalent.
    private boolean isEqual(ConnectionPoint cpa, ConnectionPoint cpb) {
        return cpa.elementId().equals(cpb.elementId())
                && cpa.interfaceId().equals(cpb.interfaceId());
    }

    // Paves a single hop for the specified packet, along the given egress point.
    private void paveHop(MessageContext context, ConnectionPoint egress,
                         MacAddress src, MacAddress dst) {
        DataPathId dpid = DataPathId.dpid(((DeviceId) egress.elementId()).fingerprint());
        OfmFlowMod flowMod = createFlowMod(controllerService.versionOf(dpid), src, dst,
                                           egress.interfaceId().port(),
                                           context.decodedPacket());
        sendFlowMod(dpid, flowMod);
    }

    // Sends the given flow mod out to the specified device.
    private void sendFlowMod(DataPathId dpid, OfmFlowMod flowMod) {
        try {
            controllerService.sendFlowMod(flowMod, dpid);
        } catch (OpenflowException e) {
            log.warn(E_SEND_FLOW, flowMod, dpid, e);
        }
    }

    /**
     * Creates a flow mod object, with basic fields populated. This is dressed
     * up for a ADD command and OUT_PORT action.
     *
     * @param pv      protocol version of the target switch
     * @param src     src host MAC address
     * @param dst     dst host MAC address
     * @param outPort port to which Action is directed at
     * @param packet  decoded packet
     * @return synthesized FlowMod structure with above attributes
     */
    private OfmFlowMod createFlowMod(ProtocolVersion pv,
                                     MacAddress src, MacAddress dst,
                                     BigPortNumber outPort, Packet packet) {
        Ethernet ethPkt = packet.get(ProtocolId.ETHERNET);

        OfmMutableFlowMod flow = (OfmMutableFlowMod)
                create(pv, MessageType.FLOW_MOD, FlowModCommand.ADD);

        MutableMatch match = createMatch(pv)
                .addField(createBasicField(pv, ETH_SRC, src))
                .addField(createBasicField(pv, ETH_DST, dst))
                .addField(createBasicField(pv, ETH_TYPE, ethPkt.type()));

        Action action = createAction(pv, ActionType.OUTPUT, outPort);

        if (pv.gt(V_1_0)) {
            InstrMutableAction instr =
                    createMutableInstruction(pv, APPLY_ACTIONS).addAction(action);

            flow.idleTimeout(idleTimeout)
                    .hardTimeout(hardTimeout)
                    .bufferId(BufferId.NO_BUFFER)
                    .cookie(cookie)
                    .priority(L2_PRIORITY)
                    .match((Match) (match.toImmutable()))
                    .outPort(Port.ANY)
                    .outGroup(GroupId.ANY)
                    .flowModFlags(FLAGS)
                    .addInstruction((Instruction) instr.toImmutable());

            // Note: Table ID is patched by Pipeline Manager in the Controller.
        } else {
            flow.idleTimeout(idleTimeout)
                    .hardTimeout(hardTimeout)
                    .bufferId(BufferId.NO_BUFFER)
                    .cookie(cookie)
                    .priority(L2_PRIORITY)
                    .match((Match) (match.toImmutable()))
                    .addAction(action)
                    .flowModFlags(FLAGS);
        }
        return (OfmFlowMod) flow.toImmutable();
    }

    // Sends the specified packet to the given connection point.
    private void sendTo(MessageContext context, ConnectionPoint cp) {
        setOutputAction(context, cp.interfaceId().port());
        context.packetOut().send();
        log.debug("{}: Sending {}", cp, context);
    }

    // Floods the specified packet if appropriate for the given igress
    // connection point.
    private void floodIfPossible(MessageContext context, ConnectionPoint cp) {
        if (pathSelectionService.isBroadcastAllowed(cp)) {
            setFloodAction(context);
            context.packetOut().send();
            log.debug("{}: Flooding {}", cp, context);
        }
    }

    // Extracts the network segment ID from the supplied packet.
    private SegmentId getSegmentId(Ethernet eth, OfmPacketIn packetIn) {
        // FIXME: provide proper implementation
        VlanId vlanId = VlanId.NONE;
        return vlanId == VlanId.NONE ?
                SegmentId.UNKNOWN : SegmentId.valueOf(vlanId);
    }

    // Returns the host with the specified MAC and belonging to the given
    // network segment.
    private Host getHost(MacAddress mac, SegmentId segmentId) {
        // FIXME: clearly, this is very non-deterministic and simplistic to boot
        Set<Host> hosts = hostService.getHosts(mac, segmentId);
        return hosts == null || hosts.isEmpty() ? null : hosts.iterator().next();
    }

    // Specifies the output action to Port.FLOOD on the packetOut.
    private void setFloodAction(MessageContext context) {
        context.packetOut().addAction(context.getVersion() == V_1_0 ? FLOOD_10 : FLOOD_13);
    }

    private void setOutputAction(MessageContext context, BigPortNumber outPort) {
        Action outAction = createAction(context.getVersion(),
                                        ActionType.OUTPUT, outPort,
                                        ActOutput.CONTROLLER_NO_BUFFER);
        context.packetOut().addAction(outAction);
    }

    // Packet interceptor responsible for directing packets along end-to-end paths.
    private class PacketListener implements SequencedPacketListener {
        @Override
        public void event(MessageContext context) {
            if (!context.isHandled())
                processPacket(context);
        }

        @Override
        public void errorEvent(ErrorEvent event) {
            log.warn(E_ERROR, event);
        }
    }

    // Interceptor for device & interface events.
    private class SwitchListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            DeviceEvent.Type type = event.type();
            if (type == DeviceEvent.Type.INTERFACE_STATE_CHANGED ||
                    type == DeviceEvent.Type.INTERFACE_REMOVED) {
                deleteFlows(event.subject(), event.netInterface());
            }
        }
    }

    private void deleteFlows(Device device, Interface netInterface) {
        DataPathId dpid = device.dpid();
        BigPortNumber outPort = netInterface.id().port();

        OfmMutableFlowMod flow;
        DataPathInfo dpi = controllerService.getDataPathInfo(dpid);
        PipelineDefinition pipeDef;
        // This "if" block is a workaround for the Thames switch firmware
        // bug with CR ID 140018. This code needs to be removed once the
        // bug is fixed. FIXME: How do we know we're talking with Thames?
        if (dpi.negotiated().gt(V_1_0)) {
            try {
                pipeDef = controllerService.getPipelineDefinition(dpid);
            } catch (IllegalStateException e) {
                log.debug(e.getMessage());
                return;
            }
            if (pipeDef == null || pipeDef.getTableIds() == null) {
                log.debug(E_NO_TABLE_ID);
                return;
            }
            // Delete matching flows from all tables.
            for (TableId tid : pipeDef.getTableIds()) {
                // Prepare a new flow each time to avoid immutable.
                flow = prepareFlowModForDelete(dpi.negotiated(), outPort);
                flow.tableId(tid);
                log.debug(MSG_DELETE_FLOW, flow.toDebugString());
                sendFlowMod(dpid, (OfmFlowMod) flow.toImmutable());
            }
        } else {
            flow = prepareFlowModForDelete(dpi.negotiated(), outPort);
            log.debug(MSG_DELETE_FLOW, flow.toDebugString());
            sendFlowMod(dpid, (OfmFlowMod) flow.toImmutable());
        }
    }

    /**
     * Creates a flow mod object, with basic fields populated. This is dressed
     * up for a DELETE command.
     *
     * @param pv protocol version of the target switch
     * @param outPort port to which Action is directed at
     * @return synthesized FlowMod structure with above attributes
     */
    private OfmMutableFlowMod prepareFlowModForDelete(ProtocolVersion pv,
                                                      BigPortNumber outPort) {
        OfmMutableFlowMod flow = (OfmMutableFlowMod)
                MessageFactory.create(pv, MessageType.FLOW_MOD,
                                      FlowModCommand.DELETE);
        MutableMatch match = MatchFactory.createMatch(pv);
        Action outAction = ActionFactory.createAction(pv, ActionType.OUTPUT,
                                                      outPort);

        if (pv.gt(V_1_0)) {
            InstrMutableAction mutInst = createMutableInstruction(pv,
                                                                  APPLY_ACTIONS);
            mutInst.addAction(outAction);
            flow.idleTimeout(idleTimeout)
                    .bufferId(BufferId.NO_BUFFER)
                    .cookie(cookie)
                    .priority(L2_PRIORITY)
                    .match((Match) (match.toImmutable()))
                    .outPort(outPort)
                    .outGroup(GroupId.ANY)
                    .flowModFlags(FLAGS)
                    .tableId(TableId.ALL) // Delete matched flows in all tables
                    .addInstruction((Instruction) mutInst.toImmutable());
        } else {
            flow.idleTimeout(idleTimeout)
                    .bufferId(BufferId.NO_BUFFER)
                    .cookie(cookie)
                    .priority(L2_PRIORITY)
                    .match((Match) (match.toImmutable()))
                    .outPort(outPort)
                    .addAction(outAction)
                    .flowModFlags(FLAGS);
        }
        return flow;
    }

}
