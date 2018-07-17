/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
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

@Singleton
public class LLDPDiscoveryListener implements PacketProcessingListener {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPDiscoveryListener.class);

    private final LLDPLinkAger lldpLinkAger;
    private final NotificationPublishService notificationService;
    private final EntityOwnershipService eos;

    @Inject
    public LLDPDiscoveryListener(@Reference final NotificationPublishService notificationService,
            final LLDPLinkAger lldpLinkAger, @Reference final EntityOwnershipService entityOwnershipService) {
        this.notificationService = notificationService;
        this.lldpLinkAger = lldpLinkAger;
        this.eos = entityOwnershipService;
    }

    @Override
    public void onPacketReceived(final PacketReceived lldp) {
        NodeConnectorRef src = LLDPDiscoveryUtils.lldpToNodeConnectorRef(lldp.getPayload(), true);
        if (src != null) {
            final NodeKey nodeKey = lldp.getIngress().getValue().firstKeyOf(Node.class);
            LOG.info("LLDP packet received for destination node {}", nodeKey);
            if (nodeKey != null) {
                final LinkDiscoveredBuilder ldb = new LinkDiscoveredBuilder();
                ldb.setDestination(lldp.getIngress());
                ldb.setSource(new NodeConnectorRef(src));
                final LinkDiscovered ld = ldb.build();
                final boolean linkWasPresent = lldpLinkAger.isLinkPresent(ld);
                lldpLinkAger.put(ld);
                if (LLDPDiscoveryUtils.isEntityOwned(this.eos, nodeKey.getId().getValue())) {
                    if (linkWasPresent) {
                        LOG.info("Skipping link already present: {}", ld);
                    } else {
                        LOG.info("Publish add event for link {}", ld);
                        try {
                            notificationService.putNotification(ld);
                        } catch (InterruptedException e) {
                            LOG.warn("Interrupted while publishing notification {}", ld, e);
                        }
                    }
                } else {
                    LOG.info("Skip publishing the add event for link because controller is non-owner of the "
                            + "node {}. Link : {}", nodeKey.getId().getValue(), ld);
                }
            } else {
                LOG.info("LLDP packet ignored. Unable to extract node-key from packet-in ingress.");
            }
        }
    }
}
