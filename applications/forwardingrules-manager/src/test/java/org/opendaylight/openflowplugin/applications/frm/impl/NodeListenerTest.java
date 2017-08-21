/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.impl.util.FRMTest;
import org.opendaylight.openflowplugin.applications.frm.impl.util.RpcProviderRegistryMock;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class NodeListenerTest extends FRMTest {
    private final static NodeKey s1Key = new NodeKey(new NodeId("testnode:1"));

    private ForwardingRulesManagerImpl forwardingRulesManager;
    private RpcProviderRegistry rpcProviderRegistryMock = new RpcProviderRegistryMock();
    @Mock
    private MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    private ReconciliationManager reconciliationManager;
    @Mock
    private MastershipChangeRegistration mastershipChangeRegistration;

    @Before
    public void setUp() {
        when(mastershipChangeServiceManager.register(any())).thenReturn(mastershipChangeRegistration);
        forwardingRulesManager = new ForwardingRulesManagerImpl(
                getDataBroker(),
                rpcProviderRegistryMock,
                getConfig(),
                mastershipChangeServiceManager,
                getConfigurationService(),
                reconciliationManager);

        forwardingRulesManager.start();
    }

    @Test
    public void addRemoveNodeTest() throws Exception {
        addFlowCapableNode(s1Key);
        final InstanceIdentifier<FlowCapableNode> nodeII = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, s1Key)
                .augmentation(FlowCapableNode.class);

        assertTrue(forwardingRulesManager.checkNodeInOperationalDataStore(nodeII));
        removeNode(s1Key);
        assertFalse(forwardingRulesManager.checkNodeInOperationalDataStore(nodeII));
    }

    @After
    public void tearDown() throws Exception {
        forwardingRulesManager.close();
        verify(mastershipChangeRegistration).close();
    }

}
