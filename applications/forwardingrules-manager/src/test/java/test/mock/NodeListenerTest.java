/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.rev140925.ReconcilEnum;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import test.mock.util.FRMTest;
import test.mock.util.RpcProviderRegistryMock;

public class NodeListenerTest extends FRMTest {

    RpcProviderRegistry rpcProviderRegistryMock = new RpcProviderRegistryMock();
    NodeKey s1Key = new NodeKey(new NodeId("S1"));

    @Test
    public void addRemoveNodeTest() throws Exception {
        try (ForwardingRulesManagerImpl forwardingRulesManager = new ForwardingRulesManagerImpl(getDataBroker(), rpcProviderRegistryMock, ReconcilEnum.DEFAULT)) {
            forwardingRulesManager.start();

            addFlowCapableNode(s1Key);

            InstanceIdentifier<FlowCapableNode> nodeII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key)
                    .augmentation(FlowCapableNode.class);

            boolean nodeActive = forwardingRulesManager.isNodeActive(nodeII);
            assertTrue(nodeActive);

            removeNode(s1Key);

            nodeActive = forwardingRulesManager.isNodeActive(nodeII);
            assertFalse(nodeActive);
        }
    }


}
