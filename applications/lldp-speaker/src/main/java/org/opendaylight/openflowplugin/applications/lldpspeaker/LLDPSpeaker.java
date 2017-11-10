/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
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
public class LLDPSpeaker implements NodeConnectorEventsObserver, Runnable, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPSpeaker.class);

    private static final long LLDP_FLOOD_PERIOD = 5;
    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("lldp-speaker-%d").setDaemon
            (true).build();

    private final PacketProcessingService packetProcessingService;
    private final DeviceOwnershipStatusService deviceOwnershipStatusService;
    private final Map<InstanceIdentifier<NodeConnector>, TransmitPacketInput> nodeConnectorMap =
            new ConcurrentHashMap<>();
    private final MacAddress addressDestionation;
    private ScheduledFuture<?> scheduledSpeakerTask;
    private ScheduledExecutorService scheduledExecutorService;
    private long Current_Flood_Period = LLDP_FLOOD_PERIOD;

    private volatile OperStatus operationalStatus = OperStatus.RUN;

    public LLDPSpeaker(final PacketProcessingService packetProcessingService, final MacAddress addressDestionation,
                       final EntityOwnershipService entityOwnershipService) {
        this(packetProcessingService, Executors.newSingleThreadScheduledExecutor(threadFactory), addressDestionation,
                entityOwnershipService);
    }

    public LLDPSpeaker(final PacketProcessingService packetProcessingService,
                       final ScheduledExecutorService scheduledExecutorService,
                       final MacAddress addressDestionation,
                       final EntityOwnershipService entityOwnershipService) {
        this.addressDestionation = addressDestionation;
        this.scheduledExecutorService = scheduledExecutorService;
        this.deviceOwnershipStatusService = new DeviceOwnershipStatusService(entityOwnershipService);
        scheduledSpeakerTask = this.scheduledExecutorService
                .scheduleAtFixedRate(this, LLDP_FLOOD_PERIOD,LLDP_FLOOD_PERIOD, TimeUnit.SECONDS);
        this.packetProcessingService = packetProcessingService;
        LOG.info("LLDPSpeaker started, it will send LLDP frames each {} seconds", LLDP_FLOOD_PERIOD);
    }

    public void setOperationalStatus(final OperStatus operationalStatus) {
        LOG.info("LLDP speaker operational status set to {}", operationalStatus);
        this.operationalStatus = operationalStatus;
        if (operationalStatus.equals(OperStatus.STANDBY)) {
            nodeConnectorMap.clear();
        }
    }

    public OperStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setLldpFloodInterval(long time) {
        this.Current_Flood_Period = time;
        scheduledSpeakerTask.cancel(false);
        scheduledSpeakerTask = this.scheduledExecutorService
                .scheduleAtFixedRate(this, time, time, TimeUnit.SECONDS);
        LOG.info("LLDPSpeaker restarted, it will send LLDP frames each {} seconds", time);
    }

    public long getLldpFloodInterval() {
        return Current_Flood_Period;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     */
    @Override
    public void close() {
        nodeConnectorMap.clear();
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
        if (scheduledSpeakerTask != null) {
            scheduledSpeakerTask.cancel(true);
        }
        LOG.trace("LLDPSpeaker stopped sending LLDP frames.");
    }

    /**
     * Send LLDPDU frames to all known openflow switch ports.
     */
    @Override
    public void run() {
        if (OperStatus.RUN.equals(operationalStatus)) {
            try {
                LOG.debug("Sending LLDP frames to nodes {}", Arrays.toString(deviceOwnershipStatusService
                        .getOwnedNodes().toArray()));
                LOG.debug("Sending LLDP frames to total {} ports", getOwnedPorts());
                nodeConnectorMap.keySet().forEach(ncIID -> {
                    NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(ncIID).getId();
                    NodeId nodeId = ncIID.firstKeyOf(Node.class, NodeKey.class).getId();
                    if (deviceOwnershipStatusService.isEntityOwned(nodeId.getValue())) {
                        LOG.debug("Node is owned by this controller, sending LLDP packet through port {}",
                                nodeConnectorId.getValue());
                        packetProcessingService.transmitPacket(nodeConnectorMap.get(ncIID));
                    } else {
                        LOG.trace("Node {} is not owned by this controller, so skip sending LLDP packet on port {}",
                                nodeId.getValue(), nodeConnectorId.getValue());
                    }
                });
            } catch (Exception e) {
                LOG.warn("Sending LLDP packet failed due to exception.", e);
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
            LOG.trace(
                    "Port {} is already in LLDPSpeaker node-connector map, no need for additional processing",
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
            LOG.debug("Port {} is local, not sending LLDP frames through it", nodeConnectorId.getValue());
            return;
        }

        // Generate packet with destination switch and port
        TransmitPacketInput packet = new TransmitPacketInputBuilder()
                .setEgress(new NodeConnectorRef(nodeConnectorInstanceId))
                .setNode(new NodeRef(nodeInstanceId))
                .setPayload(LLDPUtil
                        .buildLldpFrame(nodeId, nodeConnectorId, srcMacAddress, outputPortNo, addressDestionation))
                .build();

        // Save packet to node connector id -> packet map to transmit it periodically on the configured interval.
        nodeConnectorMap.put(nodeConnectorInstanceId, packet);
        LOG.debug("Port {} added to LLDPSpeaker.nodeConnectorMap", nodeConnectorId.getValue());

        // Transmit packet for first time immediately
        packetProcessingService.transmitPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeConnectorRemoved(final InstanceIdentifier<NodeConnector> nodeConnectorInstanceId) {
        Preconditions.checkNotNull(nodeConnectorInstanceId);

        nodeConnectorMap.remove(nodeConnectorInstanceId);
        NodeConnectorId nodeConnectorId = InstanceIdentifier.keyOf(nodeConnectorInstanceId).getId();
        LOG.trace("Port removed from node-connector map : {}", nodeConnectorId.getValue());
    }

    private int getOwnedPorts() {
        AtomicInteger ownedPorts = new AtomicInteger();
        nodeConnectorMap.keySet().forEach(ncIID -> {
            NodeId nodeId = ncIID.firstKeyOf(Node.class, NodeKey.class).getId();
            if (deviceOwnershipStatusService.isEntityOwned(nodeId.getValue())) {
                ownedPorts.incrementAndGet();
            }
        });
        return ownedPorts.get();
    }
}
