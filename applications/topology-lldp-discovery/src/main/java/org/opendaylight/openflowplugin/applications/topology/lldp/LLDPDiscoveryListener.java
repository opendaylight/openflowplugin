/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.topology.lldp.utils.LLDPDiscoveryUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscoveredBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;

public class LLDPDiscoveryListener implements PacketProcessingListener {
    private final LLDPLinkAger lldpLinkAger;
    private final NotificationProviderService notificationService;

    public LLDPDiscoveryListener(NotificationProviderService notificationService, LLDPLinkAger lldpLinkAger) {
        this.notificationService = notificationService;
        this.lldpLinkAger = lldpLinkAger;
    }

    @Override
    public void onPacketReceived(PacketReceived lldp) {
        NodeConnectorRef src = LLDPDiscoveryUtils.lldpToNodeConnectorRef(lldp.getPayload(), true);
        if(src != null) {
            LinkDiscoveredBuilder ldb = new LinkDiscoveredBuilder();
            ldb.setDestination(lldp.getIngress());
            ldb.setSource(new NodeConnectorRef(src));
            LinkDiscovered ld = ldb.build();

            notificationService.publish(ld);
            lldpLinkAger.put(ld);
        }
    }
}