/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
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
    private static final Pattern INDEX_PATTERN = Pattern.compile("^#UF\\$TABLE\\*1-([0-9]+)$");
    private static final Short DUMMY_TABLE_ID = 1;

    private DeviceFlowRegistryImpl deviceFlowRegistry;
    private FlowRegistryKey key;
    private FlowDescriptor descriptor;
    private KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private ReadOnlyTransaction readOnlyTransaction;

    @Before
    public void setUp() throws Exception {
        nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(NODE_ID)));
        when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        deviceFlowRegistry = new DeviceFlowRegistryImpl(OFConstants.OFP_VERSION_1_3, dataBroker, nodeInstanceIdentifier);
        final FlowAndStatisticsMapList flowStats = TestFlowHelper.createFlowAndStatisticsMapListBuilder(1).build();
        key = FlowRegistryKeyFactory.create(OFConstants.OFP_VERSION_1_3, flowStats);
        descriptor = FlowDescriptorFactory.create(key.getTableId(), new FlowId("ut:1"));

        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());
        deviceFlowRegistry.storeDescriptor(key, descriptor);
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testFill() throws Exception {
        final InstanceIdentifier<FlowCapableNode> path = nodeInstanceIdentifier.augmentation(FlowCapableNode.class);

        final Flow flow = new FlowBuilder()
                .setTableId((short)1)
                .setPriority(10)
                .setCookie(new FlowCookie(BigInteger.TEN))
                .setId(new FlowId("HELLO"))
                .build();

        final Table table = new TableBuilder()
                .setFlow(Collections.singletonList(flow))
                .build();

        final FlowCapableNode flowCapableNode = new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(table))
                .build();

        final Map<FlowRegistryKey, FlowDescriptor> allFlowDescriptors = testFill(path, flowCapableNode);
        final FlowRegistryKey key = FlowRegistryKeyFactory.create(OFConstants.OFP_VERSION_1_3, flow);

        InOrder order = inOrder(dataBroker, readOnlyTransaction);
        order.verify(dataBroker).newReadOnlyTransaction();
        order.verify(readOnlyTransaction).read(LogicalDatastoreType.CONFIGURATION, path);
        order.verify(dataBroker).newReadOnlyTransaction();
        order.verify(readOnlyTransaction).read(LogicalDatastoreType.OPERATIONAL, path);
        assertTrue(allFlowDescriptors.containsKey(key));

        deviceFlowRegistry.addMark(key);
    }

    @Test
    public void testFailedFill() throws Exception {
        final InstanceIdentifier<FlowCapableNode> path = nodeInstanceIdentifier.augmentation(FlowCapableNode.class);

        testFill(path, null);

        testFill(path, new FlowCapableNodeBuilder()
                .setTable(null)
                .build());

        testFill(path, new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(null))
                .build());

        testFill(path, new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(new TableBuilder()
                        .setFlow(null)
                        .build()))
                .build());

        testFill(path, new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(new TableBuilder()
                        .setFlow(Collections.singletonList(null))
                        .build()))
                .build());

        testFill(path, new FlowCapableNodeBuilder()
                .setTable(Collections.singletonList(new TableBuilder()
                        .setFlow(Collections.singletonList(new FlowBuilder()
                                .setId(null)
                                .build()))
                        .build()))
                .build());

        verify(dataBroker, times(12)).newReadOnlyTransaction();
        verify(readOnlyTransaction, times(6)).read(LogicalDatastoreType.CONFIGURATION, path);
        verify(readOnlyTransaction, times(6)).read(LogicalDatastoreType.OPERATIONAL, path);

        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    private Map<FlowRegistryKey, FlowDescriptor> testFill(final InstanceIdentifier<FlowCapableNode> path,
                                                          final FlowCapableNode flowCapableNode) throws Exception {
        when(readOnlyTransaction.read(any(), any())).thenReturn(Futures.immediateCheckedFuture(Optional.fromNullable(flowCapableNode)));
        deviceFlowRegistry.fill().get();
        return deviceFlowRegistry.getAllFlowDescriptors();
    }

    @Test
    public void testRetrieveIdForFlow() throws Exception {
        Assert.assertEquals(descriptor, deviceFlowRegistry.retrieveDescriptor(key));
    }

    @Test
    public void testStore() throws Exception {
        //store the same key with different value
        final FlowDescriptor descriptor2 = FlowDescriptorFactory.create(key.getTableId(), new FlowId("ut:2"));
        deviceFlowRegistry.storeDescriptor(key, descriptor2);
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals("ut:2", deviceFlowRegistry.retrieveDescriptor(key).getFlowId().getValue());

        // store new key with old value
        final FlowAndStatisticsMapList flowStats = TestFlowHelper.createFlowAndStatisticsMapListBuilder(2).build();
        final FlowRegistryKey key2 = FlowRegistryKeyFactory.create(OFConstants.OFP_VERSION_1_3, flowStats);
        deviceFlowRegistry.storeDescriptor(key2, descriptor);
        Assert.assertEquals(2, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals("ut:1", deviceFlowRegistry.retrieveDescriptor(key2).getFlowId().getValue());
    }

    @Test
    public void testStoreIfNecessary() throws Exception {
        FlowId newFlowId;

        //store existing key
        deviceFlowRegistry.store(key);
        newFlowId = deviceFlowRegistry.retrieveDescriptor(key).getFlowId();

        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals(descriptor, deviceFlowRegistry.retrieveDescriptor(key));
        Assert.assertEquals(descriptor.getFlowId(), newFlowId);

        //store new key
        final String alienPrefix = "#UF$TABLE*2-";
        final FlowRegistryKey key2 = FlowRegistryKeyFactory.create(OFConstants.OFP_VERSION_1_3, TestFlowHelper.createFlowAndStatisticsMapListBuilder(2).build());
        deviceFlowRegistry.store(key2);
        newFlowId = deviceFlowRegistry.retrieveDescriptor(key2).getFlowId();

        Assert.assertTrue(newFlowId.getValue().startsWith(alienPrefix));
        Assert.assertTrue(deviceFlowRegistry.retrieveDescriptor(key2).getFlowId().getValue().startsWith(alienPrefix));
        Assert.assertEquals(2, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testRemoveDescriptor() throws Exception {
        deviceFlowRegistry.addMark(key);
        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testClose() throws Exception {
        deviceFlowRegistry.close();
        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void createAlienFlowIdTest() throws Exception {
        final String alienFlowId1 = DeviceFlowRegistryImpl.createAlienFlowId(DUMMY_TABLE_ID).getValue();
        final Integer index1 = parseIndex(alienFlowId1);
        final String alienFlowId2 = DeviceFlowRegistryImpl.createAlienFlowId(DUMMY_TABLE_ID).getValue();
        final Integer index2 = parseIndex(alienFlowId2);

        assertNotNull("index1 parsing failed: " + alienFlowId1, index1);
        assertNotNull("index2 parsing failed: " + alienFlowId2, index2);
        assertTrue(index1 < index2);
    }

    private static Integer parseIndex(String alienFlowIdValue) {
        final Matcher mach = INDEX_PATTERN.matcher(alienFlowIdValue);

        if (mach.find()) {
            return Integer.valueOf(mach.group(1));
        }

        return null;
    }
}
