/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.OperStatus;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Objects of this class send LLDP frames over all flow-capable ports that can
 * be discovered through inventory.
 */
public class LLDPSpeaker implements AutoCloseable, NodeConnectorEventsObserver,
        Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LLDPSpeaker.class);
    private static final long LLDP_FLOOD_PERIOD = 5;

    private final PacketProcessingService packetProcessingService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<InstanceIdentifier<NodeConnector>, TransmitPacketInput> nodeConnectorMap = new ConcurrentHashMap<>();
    private ScheduledFuture<?> scheduledSpeakerTask;
    private final MacAddress addressDestionation;
    private OperStatus operationalStatus = OperStatus.RUN;

    public LLDPSpeaker(final PacketProcessingService packetProcessingService, final MacAddress addressDestionation) {
        this(packetProcessingService, Executors.newSingleThreadScheduledExecutor(), addressDestionation);
    }

    public LLDPSpeaker(final PacketProcessingService packetProcessingService,
            final ScheduledExecutorService scheduledExecutorService,
            final MacAddress addressDestionation) {
        this.addressDestionation = addressDestionation;
        this.scheduledExecutorService = scheduledExecutorService;
        this.packetProcessingService = packetProcessingService;
        LOG.info("LLDPSpeaker started, it will send LLDP frames each {} seconds", LLDP_FLOOD_PERIOD);
    }

    public void setOperationalStatus(final OperStatus operationalStatus) {
        LOG.info("Setting operational status to {}", operationalStatus);
        this.operationalStatus = operationalStatus;
        if (operationalStatus.equals(OperStatus.STANDBY)) {
            scheduledSpeakerTask.cancel(false);
        } else if (operationalStatus.equals(OperStatus.RUN)) {
            if (scheduledSpeakerTask == null || scheduledSpeakerTask.isCancelled()) {
                scheduledSpeakerTask = this.scheduledExecutorService.scheduleAtFixedRate(this, LLDP_FLOOD_PERIOD,
                        LLDP_FLOOD_PERIOD, TimeUnit.SECONDS);
            }
        }
    }

    public OperStatus getOperationalStatus() {
        return operationalStatus;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     */
    @Override
    public void close() {
        nodeConnectorMap.clear();
        scheduledExecutorService.shutdown();
        scheduledSpeakerTask.cancel(true);
        LOG.trace("LLDPSpeaker stopped sending LLDP frames.");
    }

    /**
     * Send LLDPDU frames to all known openflow switch ports.
     */
    @Override
    public void run() {
        if (OperStatus.RUN.equals(operationalStatus)) {
            LOG.debug("Sending LLDP frames to {} ports...", nodeConnectorMap.keySet().size());

            for (InstanceIdentifier<NodeConnector> nodeConnectorInstanceId : nodeConnectorMap.keySet()) {
                NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(nodeConnectorInstanceId).getId();
                LOG.trace("Sending LLDP through port {}", nodeConnectorId.getValue());
                packetProcessingService.transmitPacket(nodeConnectorMap.get(nodeConnectorInstanceId));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeConnectorAdded(final InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,
            final FlowCapableNodeConnector flowConnector) {
        NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(nodeConnectorInstanceId).getId();

        // nodeConnectorAdded can be called even if we already sending LLDP
        // frames to
        // port, so first we check if we actually need to perform any action
        if (nodeConnectorMap.containsKey(nodeConnectorInstanceId)) {
            LOG.trace("Port {} already in LLDPSpeaker.nodeConnectorMap, no need for additional processing",
                    nodeConnectorId.getValue());
            return;
        }

        // Prepare to build LLDP payload
        InstanceIdentifier<Node> nodeInstanceId = nodeConnectorInstanceId.firstIdentifierOf(Node.class);
        NodeId nodeId = InstanceIdentifier.keyOf(nodeInstanceId).getId();
        MacAddress srcMacAddress = flowConnector.getHardwareAddress();
        Long outputPortNo = flowConnector.getPortNumber().getUint32();

        // No need to send LLDP frames on local ports
        if (outputPortNo == null) {
            LOG.trace("Port {} is local, not sending LLDP frames through it", nodeConnectorId.getValue());
            return;
        }

        // Generate packet with destination switch and port
        TransmitPacketInput packet = new TransmitPacketInputBuilder()
                .setEgress(new NodeConnectorRef(nodeConnectorInstanceId))
                .setNode(new NodeRef(nodeInstanceId))
                .setPayload(LLDPUtil.buildLldpFrame(
                        nodeId, nodeConnectorId, srcMacAddress, outputPortNo, addressDestionation)).build();

        // Save packet to node connector id -> packet map to transmit it every 5 seconds
        nodeConnectorMap.put(nodeConnectorInstanceId, packet);
        LOG.trace("Port {} added to LLDPSpeaker.nodeConnectorMap", nodeConnectorId.getValue());

        if (OperStatus.RUN.equals(operationalStatus)) {
            // Transmit packet for first time immediately
            packetProcessingService.transmitPacket(packet);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeConnectorRemoved(final InstanceIdentifier<NodeConnector> nodeConnectorInstanceId) {
        nodeConnectorMap.remove(nodeConnectorInstanceId);
        NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(nodeConnectorInstanceId).getId();
        LOG.trace("Port {} removed from LLDPSpeaker.nodeConnectorMap", nodeConnectorId.getValue());
    }
}
