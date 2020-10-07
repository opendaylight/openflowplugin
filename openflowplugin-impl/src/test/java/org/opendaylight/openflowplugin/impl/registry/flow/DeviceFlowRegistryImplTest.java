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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link DeviceFlowRegistryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceFlowRegistryImplTest {
    private static final String NODE_ID = "openflow:1";
    private static final Pattern INDEX_PATTERN = Pattern.compile("^#UF\\$TABLE\\*1-([0-9]+)$");
    private static final Uint8 DUMMY_TABLE_ID = Uint8.ONE;

    private DeviceFlowRegistryImpl deviceFlowRegistry;
    private FlowRegistryKey key;
    private FlowDescriptor descriptor;
    private KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private ReadTransaction readOnlyTransaction;

    @Before
    public void setUp() {
        nodeInstanceIdentifier =
                InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(NODE_ID)));
        when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        deviceFlowRegistry =
                new DeviceFlowRegistryImpl(OFConstants.OFP_VERSION_1_3, dataBroker, nodeInstanceIdentifier);
        final FlowAndStatisticsMapList flowStats = TestFlowHelper.createFlowAndStatisticsMapListBuilder(1).build();
        key = FlowRegistryKeyFactory.create(OFConstants.OFP_VERSION_1_3, flowStats);
        descriptor = FlowDescriptorFactory.create(Uint8.valueOf(key.getTableId()), new FlowId("ut:1"));

        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());
        deviceFlowRegistry.storeDescriptor(key, descriptor);
        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testFill() throws Exception {
        final InstanceIdentifier<FlowCapableNode> path = nodeInstanceIdentifier.augmentation(FlowCapableNode.class);

        final Flow flow = new FlowBuilder()
                .setTableId(Uint8.ONE)
                .setPriority(Uint16.TEN)
                .setCookie(new FlowCookie(Uint64.TEN))
                .setId(new FlowId("HELLO"))
                .build();

        final Table table = new TableBuilder()
                .setId(Uint8.ONE)
                .setFlow(BindingMap.of(flow))
                .build();

        final FlowCapableNode flowCapableNode = new FlowCapableNodeBuilder()
                .setTable(BindingMap.of(table))
                .build();

        final Map<FlowRegistryKey, FlowDescriptor> allFlowDescriptors = fillRegistry(path, flowCapableNode);
        key = FlowRegistryKeyFactory.create(OFConstants.OFP_VERSION_1_3, flow);

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

        fillRegistry(path, null);

        fillRegistry(path, new FlowCapableNodeBuilder().build());

        fillRegistry(path, new FlowCapableNodeBuilder()
                .setTable(Collections.emptyMap())
                .build());

        fillRegistry(path, new FlowCapableNodeBuilder()
                .setTable(BindingMap.of(new TableBuilder().build()))
                .build());

        fillRegistry(path, new FlowCapableNodeBuilder()
                .setTable(BindingMap.of(new TableBuilder()
                        .setFlow(Collections.emptyMap())
                        .build()))
                .build());

        fillRegistry(path, new FlowCapableNodeBuilder()
                .setTable(BindingMap.of(new TableBuilder()
                        .setFlow(BindingMap.of(new FlowBuilder()
                                .setId(null)
                                .build()))
                        .build()))
                .build());

        verify(dataBroker, times(12)).newReadOnlyTransaction();
        verify(readOnlyTransaction, times(6)).read(LogicalDatastoreType.CONFIGURATION, path);
        verify(readOnlyTransaction, times(6)).read(LogicalDatastoreType.OPERATIONAL, path);

        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    private Map<FlowRegistryKey, FlowDescriptor> fillRegistry(final InstanceIdentifier<FlowCapableNode> path,
                                                              final FlowCapableNode flowCapableNode) throws Exception {
        doReturn(FluentFutures.immediateFluentFuture(Optional.ofNullable(flowCapableNode))).when(readOnlyTransaction)
            .read(any(), any());
        deviceFlowRegistry.fill().get();
        return deviceFlowRegistry.getAllFlowDescriptors();
    }

    @Test
    public void testRetrieveIdForFlow() {
        Assert.assertEquals(descriptor, deviceFlowRegistry.retrieveDescriptor(key));
    }

    @Test
    public void testStore() {
        //store the same key with different value
        final FlowDescriptor descriptor2 = FlowDescriptorFactory.create(Uint8.valueOf(key.getTableId()),
            new FlowId("ut:2"));
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
    public void testStoreIfNecessary() {
        FlowId newFlowId;

        //store existing key
        deviceFlowRegistry.store(key);
        newFlowId = deviceFlowRegistry.retrieveDescriptor(key).getFlowId();

        Assert.assertEquals(1, deviceFlowRegistry.getAllFlowDescriptors().size());
        Assert.assertEquals(descriptor, deviceFlowRegistry.retrieveDescriptor(key));
        Assert.assertEquals(descriptor.getFlowId(), newFlowId);

        //store new key
        final String alienPrefix = "#UF$TABLE*2-";
        final FlowRegistryKey key2 = FlowRegistryKeyFactory.create(OFConstants.OFP_VERSION_1_3,
                TestFlowHelper.createFlowAndStatisticsMapListBuilder(2).build());
        deviceFlowRegistry.store(key2);
        newFlowId = deviceFlowRegistry.retrieveDescriptor(key2).getFlowId();

        Assert.assertTrue(newFlowId.getValue().startsWith(alienPrefix));
        Assert.assertTrue(deviceFlowRegistry.retrieveDescriptor(key2).getFlowId().getValue().startsWith(alienPrefix));
        Assert.assertEquals(2, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testRemoveDescriptor() {
        deviceFlowRegistry.addMark(key);
        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void testClose() {
        deviceFlowRegistry.close();
        Assert.assertEquals(0, deviceFlowRegistry.getAllFlowDescriptors().size());
    }

    @Test
    public void createAlienFlowIdTest() {
        final String alienFlowId1 = DeviceFlowRegistryImpl.createAlienFlowId(DUMMY_TABLE_ID).getValue();
        final Integer index1 = parseIndex(alienFlowId1);
        final String alienFlowId2 = DeviceFlowRegistryImpl.createAlienFlowId(DUMMY_TABLE_ID).getValue();
        final Integer index2 = parseIndex(alienFlowId2);

        assertNotNull("index1 parsing failed: " + alienFlowId1, index1);
        assertNotNull("index2 parsing failed: " + alienFlowId2, index2);
        assertTrue(index1 < index2);
    }

    @Test
    public void testForEach() {
        final AtomicInteger counter = new AtomicInteger(0);
        deviceFlowRegistry.forEach(k -> counter.incrementAndGet());
        Assert.assertEquals(1, counter.get());
    }

    private static Integer parseIndex(final String alienFlowIdValue) {
        final Matcher mach = INDEX_PATTERN.matcher(alienFlowIdValue);

        if (mach.find()) {
            return Integer.valueOf(mach.group(1));
        }

        return null;
    }
}
