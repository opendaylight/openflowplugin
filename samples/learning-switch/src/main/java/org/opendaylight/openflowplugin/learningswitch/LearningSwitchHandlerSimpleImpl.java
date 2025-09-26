/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.NotificationService.Listener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Learning Switch implementation which does mac learning for one switch.
 */
public class LearningSwitchHandlerSimpleImpl implements LearningSwitchHandler, Listener<PacketReceived> {
    private static final Logger LOG = LoggerFactory.getLogger(LearningSwitchHandlerSimpleImpl.class);
    private static final byte[] ETH_TYPE_IPV4 = new byte[] { 0x08, 0x00 };
    private static final Uint16 DIRECT_FLOW_PRIORITY = Uint16.valueOf(512);

    private final DataTreeChangeListenerRegistrationHolder registrationPublisher;
    private final FlowCommitWrapper dataStoreAccessor;
    private final TransmitPacket transmitPacket;

    private volatile boolean isLearning = false;

    private NodeId nodeId;
    private final AtomicLong flowIdInc = new AtomicLong();
    private final AtomicLong flowCookieInc = new AtomicLong(0x2a00000000000000L);

    private DataObjectIdentifier<Node> nodePath;
    private volatile DataObjectIdentifier<Table> tablePath;

    private Map<MacAddress, NodeConnectorRef> mac2portMapping;
    private final Set<String> coveredMacPaths = new HashSet<>();

    public LearningSwitchHandlerSimpleImpl(final @NonNull FlowCommitWrapper dataStoreAccessor,
            final @NonNull TransmitPacket transmitPacket,
            final @Nullable DataTreeChangeListenerRegistrationHolder registrationPublisher) {
        this.dataStoreAccessor = requireNonNull(dataStoreAccessor);
        this.transmitPacket = requireNonNull(transmitPacket);
        this.registrationPublisher = registrationPublisher;
    }

    @Override
    public synchronized void onSwitchAppeared(final DataObjectIdentifier<Table> appearedTablePath) {
        if (isLearning) {
            LOG.debug("already learning a node, skipping {}", nodeId.getValue());
            return;
        }

        LOG.debug("expected table acquired, learning ..");

        // disable listening - simple learning handles only one node (switch)
        if (registrationPublisher != null) {
            LOG.debug("closing dataTreeChangeListenerRegistration");
            registrationPublisher.getDataTreeChangeListenerRegistration().close();
        }

        isLearning = true;

        tablePath = appearedTablePath;
        nodePath = tablePath.trimTo(Node.class);
        nodeId = nodePath.getFirstKeyOf(Node.class).getId();
        mac2portMapping = new HashMap<>();

        // start forwarding all packages to controller
        FlowId flowId = new FlowId(String.valueOf(flowIdInc.getAndIncrement()));
        FlowKey flowKey = new FlowKey(flowId);
        final var flowPath = InstanceIdentifierUtils.createFlowPath(tablePath, flowKey);

        Uint16 priority = Uint16.ZERO;
        // create flow in table with id = 0, priority = 4 (other params are
        // defaulted in OFDataStoreUtil)
        FlowBuilder allToCtrlFlow = FlowUtils.createFwdAllToControllerFlow(
                InstanceIdentifierUtils.getTableId(tablePath), priority, flowId);

        LOG.debug("writing packetForwardToController flow");
        dataStoreAccessor.writeFlowToConfig(flowPath, allToCtrlFlow.build());
    }

    @Override
    public void onNotification(final PacketReceived notification) {
        if (!isLearning) {
            // ignoring packets - this should not happen
            return;
        }

        LOG.debug("Received packet via match: {}", notification.getMatch());

        // detect and compare node - we support one switch
        if (!nodePath.toLegacy().contains(
            ((DataObjectIdentifier<?>) notification.getIngress().getValue()).toLegacy())) {
            return;
        }

        // read src MAC and dst MAC
        byte[] dstMacRaw = PacketUtils.extractDstMac(notification.getPayload());
        byte[] srcMacRaw = PacketUtils.extractSrcMac(notification.getPayload());
        byte[] etherType = PacketUtils.extractEtherType(notification.getPayload());

        MacAddress dstMac = PacketUtils.rawMacToMac(dstMacRaw);
        MacAddress srcMac = PacketUtils.rawMacToMac(srcMacRaw);

        NodeConnectorKey ingressKey = InstanceIdentifierUtils.getNodeConnectorKey(notification.getIngress().getValue());

        LOG.debug("Received packet from MAC match: {}, ingress: {}", srcMac, ingressKey.getId());
        LOG.debug("Received packet to   MAC match: {}", dstMac);
        LOG.debug("Ethertype: {}", Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));

        // learn by IPv4 traffic only
        if (Arrays.equals(ETH_TYPE_IPV4, etherType)) {
            NodeConnectorRef previousPort = mac2portMapping.put(srcMac, notification.getIngress());
            if (previousPort != null && !notification.getIngress().equals(previousPort)) {
                NodeConnectorKey previousPortKey = InstanceIdentifierUtils.getNodeConnectorKey(previousPort.getValue());
                LOG.debug("mac2port mapping changed by mac {}: {} -> {}", srcMac, previousPortKey, ingressKey.getId());
            }
            // if dst MAC mapped:
            NodeConnectorRef destNodeConnector = mac2portMapping.get(dstMac);
            if (destNodeConnector != null) {
                synchronized (coveredMacPaths) {
                    if (!destNodeConnector.equals(notification.getIngress())) {
                        // add flow
                        addBridgeFlow(srcMac, dstMac, destNodeConnector);
                        addBridgeFlow(dstMac, srcMac, notification.getIngress());
                    } else {
                        LOG.debug("useless rule ignoring - both MACs are behind the same port");
                    }
                }
                LOG.debug("packetIn-directing.. to {}",
                        InstanceIdentifierUtils.getNodeConnectorKey(destNodeConnector.getValue()).getId());
                sendPacketOut(notification.getPayload(), notification.getIngress(), destNodeConnector);
            } else {
                // flood
                LOG.debug("packetIn-still flooding.. ");
                flood(notification.getPayload(), notification.getIngress());
            }
        } else {
            // non IPv4 package
            flood(notification.getPayload(), notification.getIngress());
        }
    }

    private void addBridgeFlow(final MacAddress srcMac, final MacAddress dstMac,
            final NodeConnectorRef destNodeConnector) {
        synchronized (coveredMacPaths) {
            String macPath = srcMac.toString() + dstMac.toString();
            if (!coveredMacPaths.contains(macPath)) {
                LOG.debug("covering mac path: {} by [{}]", macPath,
                    destNodeConnector.getValue().getFirstKeyOf(NodeConnector.class).getId());

                coveredMacPaths.add(macPath);
                FlowId flowId = new FlowId(String.valueOf(flowIdInc.getAndIncrement()));
                FlowKey flowKey = new FlowKey(flowId);
                // Path to the flow we want to program.
                final var flowPath = InstanceIdentifierUtils.createFlowPath(tablePath, flowKey);

                Uint8 tableId = InstanceIdentifierUtils.getTableId(tablePath);
                FlowBuilder srcToDstFlow = FlowUtils.createDirectMacToMacFlow(tableId, DIRECT_FLOW_PRIORITY, srcMac,
                        dstMac, destNodeConnector);
                srcToDstFlow.setCookie(new FlowCookie(Uint64.valueOf(flowCookieInc.getAndIncrement())));

                dataStoreAccessor.writeFlowToConfig(flowPath, srcToDstFlow.build());
            }
        }
    }

    private void flood(final byte[] payload, final NodeConnectorRef ingress) {
        NodeConnectorKey nodeConnectorKey = new NodeConnectorKey(nodeConnectorId("0xfffffffb"));
        final var nodeConnectorPath = InstanceIdentifierUtils.createNodeConnectorPath(nodePath, nodeConnectorKey);
        NodeConnectorRef egressConnectorRef = new NodeConnectorRef(nodeConnectorPath);

        sendPacketOut(payload, ingress, egressConnectorRef);
    }

    private NodeConnectorId nodeConnectorId(final String connectorId) {
        NodeKey nodeKey = nodePath.firstKeyOf(Node.class);
        StringBuilder stringId = new StringBuilder(nodeKey.getId().getValue()).append(":").append(connectorId);
        return new NodeConnectorId(stringId.toString());
    }

    private void sendPacketOut(final byte[] payload, final NodeConnectorRef ingress, final NodeConnectorRef egress) {
        LoggingFutures.addErrorLogging(transmitPacket.invoke(new TransmitPacketInputBuilder()
            .setPayload(payload)
            .setNode(new NodeRef(InstanceIdentifierUtils.getNodePath((DataObjectIdentifier<?>) egress.getValue())))
            .setEgress(egress)
            .setIngress(ingress)
            .build()), LOG, "transmitPacket");
    }
}
