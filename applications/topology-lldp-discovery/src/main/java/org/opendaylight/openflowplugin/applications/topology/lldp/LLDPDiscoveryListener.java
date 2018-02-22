/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.openflowplugin.applications.topology.lldp.utils.LLDPDiscoveryUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscoveredBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLDPDiscoveryListener implements PacketProcessingListener {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPDiscoveryListener.class);
    private final LLDPLinkAger lldpLinkAger;
    private final NotificationProviderService notificationService;
    private final ContextChainHolder contextChainHolder;

    private static final String ENTITY_TYPE = "org.opendaylight.mdsal.ServiceEntityType";

    public LLDPDiscoveryListener(final NotificationProviderService notificationService,
                                 final LLDPLinkAger lldpLinkAger,
                                 final ContextChainHolder contextChainHolder) {
        this.notificationService = notificationService;
        this.lldpLinkAger = lldpLinkAger;
        this.contextChainHolder = contextChainHolder;
    }

    @Override
    public void onPacketReceived(PacketReceived lldp) {
        handleEntityOwnership(lldp);
    }

    private void handleEntityOwnership(final PacketReceived lldpPacket) {
        final NodeConnectorRef src = LLDPDiscoveryUtils
                .lldpToNodeConnectorRef(lldpPacket.getPayload(), true);
        final NodeKey nodeKey = src.getValue().firstKeyOf(Node.class);
        final boolean isEntityOwned = contextChainHolder.isOwner(nodeKey.getId().getValue());

        LOG.debug("LLDP packet received for node {}", nodeKey);
        if (nodeKey != null && isEntityOwned) {
            final LinkDiscoveredBuilder builder = new LinkDiscoveredBuilder();
            builder.setDestination(lldpPacket.getIngress());
            builder.setSource(src);
            final LinkDiscovered linkDiscovered = builder.build();

            notificationService.publish(linkDiscovered);
            lldpLinkAger.put(linkDiscovered);
        } else {
            LOG.debug("LLDP packet ignored, as this controller is not owner of the device.");
        }
    }
}
