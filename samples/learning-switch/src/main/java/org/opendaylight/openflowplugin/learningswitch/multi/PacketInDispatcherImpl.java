/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.learningswitch.multi;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * 
 */
public class PacketInDispatcherImpl implements PacketProcessingListener {
    
    private Map<InstanceIdentifier<Node>, PacketProcessingListener> handlerMapping;
    
    /**
     * default ctor
     */
    public PacketInDispatcherImpl() {
        handlerMapping = new HashMap<>();
    }

    @Override
    public void onPacketReceived(PacketReceived notification) {
        // find corresponding handler
        InstanceIdentifier<Node> nodeOfPacket = notification.getIngress().getValue().firstIdentifierOf(Node.class);
        PacketProcessingListener nodeHandler = handlerMapping.get(nodeOfPacket);
        
        if (nodeHandler != null) {
            nodeHandler.onPacketReceived(notification);
        }
    }
    
    /**
     * @return the handlerMapping
     */
    public Map<InstanceIdentifier<Node>, PacketProcessingListener> getHandlerMapping() {
        return handlerMapping;
    }
}
