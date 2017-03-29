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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginMastershipChangeServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeRegistration;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.impl.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
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
    private ForwardingRulesManagerImpl forwardingRulesManager;
    private final static NodeId NODE_ID = new NodeId("testnode:1");
    private final static NodeKey s1Key = new NodeKey(new NodeId(NODE_ID));
    RpcProviderRegistry rpcProviderRegistryMock = new RpcProviderRegistryMock();
    @Mock
    OpenFlowPluginMastershipChangeServiceProvider provider;
    @Mock
    MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    MastershipChangeRegistration mastershipChangeRegistration;
    @Mock
    DeviceMastershipManager deviceMastershipManager;



    @Before
    public void setUp() {
        forwardingRulesManager = new ForwardingRulesManagerImpl(
                getDataBroker(),
                rpcProviderRegistryMock,
                getConfig(),
                provider);
        forwardingRulesManager.setDeviceMastershipManager(deviceMastershipManager);
        forwardingRulesManager.setMastershipChangeManager(mastershipChangeServiceManager);
        Mockito.when(deviceMastershipManager.isDeviceMastered(NODE_ID)).thenReturn(true);
        Mockito.when(provider.getMastershipChangeServiceManager()).thenReturn(mastershipChangeServiceManager);
        Mockito.when(mastershipChangeServiceManager.register(Mockito.any())).thenReturn(mastershipChangeRegistration);
        forwardingRulesManager.start();
    }

    @Test
    public void addRemoveNodeTest() throws Exception {
        addFlowCapableNode(s1Key);

        InstanceIdentifier<FlowCapableNode> nodeII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key)
                .augmentation(FlowCapableNode.class);
        boolean nodeActive = forwardingRulesManager.isNodeActive(nodeII);
        assertTrue(nodeActive);
        removeNode(s1Key);
        nodeActive = forwardingRulesManager.isNodeActive(nodeII);
        assertFalse(nodeActive);
    }

    @After
    public void tearDown() throws Exception {
        forwardingRulesManager.close();
    }

}
