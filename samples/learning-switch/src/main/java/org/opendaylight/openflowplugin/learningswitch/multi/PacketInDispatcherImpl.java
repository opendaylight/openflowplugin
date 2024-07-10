/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch.multi;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.mdsal.binding.api.NotificationService.Listener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PacketInDispatcherImpl implements Listener<PacketReceived> {
    private final Map<InstanceIdentifier<Node>, Listener<PacketReceived>> handlerMapping = new HashMap<>();

    @Override
    public void onNotification(final PacketReceived notification) {
        // find corresponding handler
        /*
         * Notification contains reference to ingress port
         * in a form of path in inventory: /nodes/node/node-connector
         *
         * In order to get path we shorten path to the first node reference
         * by using firstIdentifierOf helper method provided by InstanceIdentifier,
         * this will effectively shorten the path to /nodes/node.
         */
        InstanceIdentifier<?> ingressPort = ((DataObjectIdentifier<?>) notification.getIngress().getValue()).toLegacy();
        InstanceIdentifier<Node> nodeOfPacket = ingressPort.firstIdentifierOf(Node.class);
        /**
         * We lookup up the the packet-in listener for this node.
         */
        Listener<PacketReceived> nodeHandler = handlerMapping.get(nodeOfPacket);

        /**
         * If we have packet-processing listener, we delegate notification.
         */
        if (nodeHandler != null) {
            nodeHandler.onNotification(notification);
        }
    }

    public Map<InstanceIdentifier<Node>, Listener<PacketReceived>> getHandlerMapping() {
        return handlerMapping;
    }
}
