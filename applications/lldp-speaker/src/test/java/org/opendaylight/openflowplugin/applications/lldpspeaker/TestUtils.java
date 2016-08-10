/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Helper methods that are used by multiple tests.
 */
public class TestUtils {
    static InstanceIdentifier<NodeConnector> createNodeConnectorId(String nodeKey, String nodeConnectorKey) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(nodeKey)))
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId(nodeConnectorKey)))
                .build();
    }

    static FlowCapableNodeConnectorBuilder createFlowCapableNodeConnector() {
        return createFlowCapableNodeConnector(false, false);
    }

    static FlowCapableNodeConnectorBuilder createFlowCapableNodeConnector(boolean linkDown, boolean adminDown) {
        return createFlowCapableNodeConnector(linkDown, adminDown, new MacAddress("13:37:BA:AD:F0:0D"), 1L);
    }

    static FlowCapableNodeConnectorBuilder createFlowCapableNodeConnector(MacAddress mac, long port) {
        return createFlowCapableNodeConnector(false, false, mac, port);
    }

    private static FlowCapableNodeConnectorBuilder createFlowCapableNodeConnector(boolean linkDown, boolean adminDown,
                                                                          MacAddress mac, long port) {
        return new FlowCapableNodeConnectorBuilder()
                .setHardwareAddress(mac)
                .setPortNumber(new PortNumberUni(port))
                .setState(new StateBuilder().setLinkDown(linkDown).build())
                .setConfiguration(new PortConfig(false, false, false, adminDown));
    }

}
