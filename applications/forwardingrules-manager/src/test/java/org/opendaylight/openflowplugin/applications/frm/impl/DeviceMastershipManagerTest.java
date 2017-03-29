/**
 * Copyright (c) 2016, 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link DeviceMastershipManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMastershipManagerTest {
    private static final NodeId NODE_ID = new NodeId("testNode");
    private DeviceMastershipManager deviceMastershipManager;
    @Mock
    private FlowNodeReconciliation reconciliationAgent;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private DeviceInfo deviceInfo;

    @Before
    public void setUp() throws Exception {
        deviceMastershipManager = new DeviceMastershipManager(reconciliationAgent, dataBroker);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(NODE_ID);
    }

    @Test
    public void testIsDeviceMasteredOrSlaved() throws Exception {
        // no context
        Assert.assertNull(deviceMastershipManager.getDeviceMasterships().get(NODE_ID));
        deviceMastershipManager.onBecomeOwner(deviceInfo);
        DeviceMastership serviceInstance = deviceMastershipManager.getDeviceMasterships().get(NODE_ID);
        Assert.assertNotNull(serviceInstance);
        // destroy context - unregister
        Assert.assertNotNull(deviceMastershipManager.getDeviceMasterships().get(NODE_ID));
        deviceMastershipManager.onLoseOwnership(deviceInfo);
        Assert.assertNull(deviceMastershipManager.getDeviceMasterships().get(NODE_ID));
    }
}