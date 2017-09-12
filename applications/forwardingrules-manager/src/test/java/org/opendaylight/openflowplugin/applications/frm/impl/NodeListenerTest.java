/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import test.mock.util.FRMTest;
import test.mock.util.RpcProviderRegistryMock;

@RunWith(MockitoJUnitRunner.class)
public class NodeListenerTest extends FRMTest {
    private static final NodeKey NODE_KEY = new NodeKey(new NodeId("testnode:1"));

    private ForwardingRulesManagerImpl forwardingRulesManager;
    private RpcProviderRegistry rpcProviderRegistryMock = new RpcProviderRegistryMock();

    @Mock
    private MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    private ReconciliationManager reconciliationManager;
    @Mock
    private DeviceMastershipManager deviceMastershipManager;

    @Before
    public void setUp() {
        forwardingRulesManager = new ForwardingRulesManagerImpl(
                getDataBroker(),
                rpcProviderRegistryMock,
                getConfig(),
                mastershipChangeServiceManager,
                getConfigurationService(),
                reconciliationManager);

        forwardingRulesManager.start();
        forwardingRulesManager.setDeviceMastershipManager(deviceMastershipManager);
    }

    @Test
    public void addRemoveNodeTest() throws Exception {
        addFlowCapableNode(NODE_KEY);

        final InstanceIdentifier<FlowCapableNode> nodeII = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class);

        assertTrue(forwardingRulesManager.checkNodeInOperationalDataStore(nodeII));
        removeNode(NODE_KEY);
        assertFalse(forwardingRulesManager.checkNodeInOperationalDataStore(nodeII));
    }

    @After
    public void tearDown() throws Exception {
        forwardingRulesManager.close();
    }
}
