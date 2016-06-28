/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
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
@RunWith(MockitoJUnitRunner.class)
public class DeviceFlowRegistryImplTest {
    private static final String NODE_ID = "openflow:1";
    private DeviceFlowRegistryImpl deviceFlowRegistry;
    private FlowRegistryKey key;
    private FlowDescriptor descriptor;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private ReadOnlyTransaction readOnlyTransaction;

    @Before
    public void setUp() throws Exception {
        when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        when(readOnlyTransaction.read(any(), any())).thenReturn(Futures.immediateCheckedFuture(Optional.absent()));
        deviceFlowRegistry = new DeviceFlowRegistryImpl(dataBroker);
        final FlowAndStatisticsMapList flowStats = TestFlowHelper.createFlowAndStatisticsMapListBuilder(1).build();
        key = FlowRegistryKeyFactory.create(flowStats);
        descriptor = FlowDescriptorFactory.create(key.getTableId(), new FlowId("ut:1"));

        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());
        deviceFlowRegistry.store(key, descriptor);
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testFill() throws Exception {
        final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(NODE_ID)));
        final InstanceIdentifier<FlowCapableNode> path = nodeInstanceIdentifier.augmentation(FlowCapableNode.class);

        deviceFlowRegistry.fill(nodeInstanceIdentifier);

        verify(dataBroker, times(2)).newReadOnlyTransaction();
        verify(readOnlyTransaction).read(LogicalDatastoreType.CONFIGURATION, path);
        verify(readOnlyTransaction).read(LogicalDatastoreType.OPERATIONAL, path);
    }

    @Test
    public void testRetrieveIdForFlow() throws Exception {
        Assert.assertEquals(descriptor, deviceFlowRegistry.retrieveIdForFlow(key));
    }

    @Test
    public void testStore() throws Exception {
        //store the same key with different value
        final FlowDescriptor descriptor2 = FlowDescriptorFactory.create(key.getTableId(), new FlowId("ut:2"));
        deviceFlowRegistry.store(key, descriptor2);
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals("ut:2", deviceFlowRegistry.retrieveIdForFlow(key).getFlowId().getValue());

        // store new key with old value
        final FlowAndStatisticsMapList flowStats = TestFlowHelper.createFlowAndStatisticsMapListBuilder(2).build();
        final FlowRegistryKey key2 = FlowRegistryKeyFactory.create(flowStats);
        deviceFlowRegistry.store(key2, descriptor);
        Assert.assertEquals(2, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals("ut:1", deviceFlowRegistry.retrieveIdForFlow(key2).getFlowId().getValue());
    }

    @Test
    public void testStoreIfNecessary() throws Exception {
        FlowId newFlowId;

        //store existing key
        newFlowId = deviceFlowRegistry.storeIfNecessary(key);

        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals(descriptor, deviceFlowRegistry.retrieveIdForFlow(key));
        Assert.assertEquals(descriptor.getFlowId(), newFlowId);

        //store new key
        final String alienPrefix = "#UF$TABLE*2-";
        final FlowRegistryKey key2 = FlowRegistryKeyFactory.create(TestFlowHelper.createFlowAndStatisticsMapListBuilder(2).build());
        newFlowId = deviceFlowRegistry.storeIfNecessary(key2);

        Assert.assertTrue(newFlowId.getValue().startsWith(alienPrefix));
        Assert.assertTrue(deviceFlowRegistry.retrieveIdForFlow(key2).getFlowId().getValue().startsWith(alienPrefix));
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