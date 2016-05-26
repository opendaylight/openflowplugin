/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Test for {@link DeviceFlowRegistryImpl}.
 */
public class DeviceFlowRegistryImplTest {
    private static final String NODE_ID = "openflow:1";
    private DeviceFlowRegistryImpl deviceFlowRegistry;
    private FlowRegistryKey key;
    private FlowDescriptor descriptor;
    private KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;

    @Before
    public void setUp() throws Exception {
        nodeInstanceIdentifier = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(NODE_ID)));

        deviceFlowRegistry = new DeviceFlowRegistryImpl();
        final FlowAndStatisticsMapList flowStats = TestFlowHelper.createFlowAndStatisticsMapListBuilder(1).build();
        key = FlowRegistryKeyFactory.create(flowStats);
        descriptor = FlowDescriptorFactory.create(key.getTableId(), new FlowId("ut:1"));

        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());
        deviceFlowRegistry.store(key, descriptor);
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testRetrieveIdForFlow() throws Exception {
        Assert.assertEquals(descriptor, deviceFlowRegistry.retrieveIdForFlow(key, nodeInstanceIdentifier));
    }

    @Test
    public void testStore() throws Exception {
        //store the same key with different value
        final FlowDescriptor descriptor2 = FlowDescriptorFactory.create(key.getTableId(), new FlowId("ut:2"));
        deviceFlowRegistry.store(key, descriptor2);
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals("ut:2", deviceFlowRegistry.retrieveIdForFlow(key, nodeInstanceIdentifier).getFlowId().getValue());

        // store new key with old value
        final FlowAndStatisticsMapList flowStats = TestFlowHelper.createFlowAndStatisticsMapListBuilder(2).build();
        final FlowRegistryKey key2 = FlowRegistryKeyFactory.create(flowStats);
        deviceFlowRegistry.store(key2, descriptor);
        Assert.assertEquals(2, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals("ut:1", deviceFlowRegistry.retrieveIdForFlow(key2, nodeInstanceIdentifier).getFlowId().getValue());
    }

    @Test
    public void testStoreIfNecessary() throws Exception {
        FlowId newFlowId;

        //store existing key
        newFlowId = deviceFlowRegistry.storeIfNecessary(key, key.getTableId());

        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals(descriptor, deviceFlowRegistry.retrieveIdForFlow(key, nodeInstanceIdentifier));
        Assert.assertEquals(descriptor.getFlowId(), newFlowId);

        //store new key
        final String alienPrefix = "#UF$TABLE*2-";
        final FlowRegistryKey key2 = FlowRegistryKeyFactory.create(TestFlowHelper.createFlowAndStatisticsMapListBuilder(2).build());
        newFlowId = deviceFlowRegistry.storeIfNecessary(key2, key2.getTableId());

        Assert.assertTrue(newFlowId.getValue().startsWith(alienPrefix));
        Assert.assertTrue(deviceFlowRegistry.retrieveIdForFlow(key2, nodeInstanceIdentifier).getFlowId().getValue().startsWith(alienPrefix));
        Assert.assertEquals(2, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testRemoveMarked() throws Exception {
        deviceFlowRegistry.markToBeremoved(key);
        deviceFlowRegistry.removeMarked();
        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testRemoveMarkedNegative() throws Exception {
        final FlowAndStatisticsMapList flowStats = TestFlowHelper.createFlowAndStatisticsMapListBuilder(2).build();
        FlowRegistryKey key2 = FlowRegistryKeyFactory.create(flowStats);
        deviceFlowRegistry.markToBeremoved(key2);
        deviceFlowRegistry.removeMarked();
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testClose() throws Exception {
        deviceFlowRegistry.markToBeremoved(key);
        deviceFlowRegistry.close();
        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());

        deviceFlowRegistry.store(key, descriptor);
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
        deviceFlowRegistry.removeMarked();
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
    }
}