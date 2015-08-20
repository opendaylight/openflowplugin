/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.helper.TestChangeEventBuildHelper;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.helper.TestSupplierVerifyHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class FlowNotificationSupplierImplTest {

    private static final String FLOW_NODE_ID = "test-111";
    private static final Short FLOW_TABLE_ID = 111;
    private static final String FLOW_ID = "test-flow-111";
    private FlowNotificationSupplierImpl notifSupplierImpl;
    private NotificationProviderService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationProviderService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new FlowNotificationSupplierImpl(notifProviderService, dataBroker);
        TestSupplierVerifyHelper.verifyDataChangeRegistration(dataBroker);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullChangeEvent() {
        notifSupplierImpl.onDataChanged(null);
    }

    @Test
    public void testNullableChangeEvent() {
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createEmptyTestDataEvent());
    }

    @Test
    public void testEmptyChangeEvent() {
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createEmptyTestDataEvent());
    }

    @Test
    public void testCreate() {
        final FlowAdded notification = notifSupplierImpl.createNotification(createTestFlow(), createTestFlowPath());
        assertNotNull(notification);
        assertEquals(FLOW_ID, notification.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId().getValue());
        assertEquals(FLOW_TABLE_ID, notification.getFlowRef().getValue().firstKeyOf(Table.class, TableKey.class).getId());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testCreateChangeEvent() {
        final Map<InstanceIdentifier<?>, DataObject> createdData = new HashMap<>();
        createdData.put(createTestFlowPath(), createTestFlow());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(createdData, null, null));
        verify(notifProviderService, times(1)).publish(Matchers.any(FlowAdded.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestFlowPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullPath() {
        notifSupplierImpl.createNotification(createTestFlow(), null);
    }

    @Test
    public void testUpdate() {
        final FlowUpdated notification = notifSupplierImpl.updateNotification(createTestFlow(), createTestFlowPath());
        assertNotNull(notification);
        assertEquals(FLOW_ID, notification.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId().getValue());
        assertEquals(FLOW_TABLE_ID, notification.getFlowRef().getValue().firstKeyOf(Table.class, TableKey.class).getId());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testUpdateChangeEvent() {
        final Map<InstanceIdentifier<?>, DataObject> createdData = new HashMap<>();
        createdData.put(createTestFlowPath(), createTestFlow());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(createdData, null, null));
        verify(notifProviderService, times(1)).publish(Matchers.any(FlowUpdated.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestFlowPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullPath() {
        notifSupplierImpl.createNotification(createTestFlow(), null);
    }

    @Test
    public void testDelete() {
        final FlowRemoved notification = notifSupplierImpl.deleteNotification(createTestFlowPath());
        assertNotNull(notification);
        assertEquals(FLOW_ID, notification.getFlowRef().getValue().firstKeyOf(Flow.class, FlowKey.class).getId().getValue());
        assertEquals(FLOW_TABLE_ID, notification.getFlowRef().getValue().firstKeyOf(Table.class, TableKey.class).getId());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testDeleteChangeEvent() {
        final Set<InstanceIdentifier<?>> removeData = new HashSet<>();
        removeData.add(createTestFlowPath());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(null, null, removeData));
        verify(notifProviderService, times(1)).publish(Matchers.any(FlowRemoved.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFromNullPath() {
        notifSupplierImpl.deleteNotification(null);
    }

    private static InstanceIdentifier<Flow> createTestFlowPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(FLOW_NODE_ID)))
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(FLOW_TABLE_ID))
                .child(Flow.class, new FlowKey(new FlowId(FLOW_ID)));
    }

    private static Flow createTestFlow() {
        final FlowBuilder builder = new FlowBuilder();
        builder.setId(new FlowId(FLOW_ID));
        builder.setTableId(FLOW_TABLE_ID);
        return builder.build();
    }
}

