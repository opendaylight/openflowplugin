/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class NodeNotificationSupplierImplTest {

    private static final String FLOW_NODE_ID = "test-111";
    private NodeNotificationSupplierImpl notifSupplierImpl;
    private NotificationProviderService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationProviderService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new NodeNotificationSupplierImpl(notifProviderService, dataBroker);
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
        final NodeUpdated notification = notifSupplierImpl.createNotification(createTestFlowCapableNode(),
                createTestFlowCapableNodePath());
        assertNotNull(notification);
        assertEquals(FLOW_NODE_ID, notification.getId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNodeRef().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testCreateChangeEvent() {
        final Map<InstanceIdentifier<?>, DataObject> createdData = new HashMap<>();
        createdData.put(createTestFlowCapableNodePath(), createTestFlowCapableNode());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(createdData, null, null));
        verify(notifProviderService, times(1)).publish(Matchers.any(NodeUpdated.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullNode() {
        notifSupplierImpl.createNotification(null, createTestFlowCapableNodePath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullPath() {
        notifSupplierImpl.createNotification(createTestFlowCapableNode(), null);
    }

    @Test
    public void testDelete() {
        final NodeRemoved notification = notifSupplierImpl.deleteNotification(createTestFlowCapableNodePath());
        assertNotNull(notification);
        assertEquals(FLOW_NODE_ID, notification.getNodeRef().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testDeleteChangeEvent() {
        final Set<InstanceIdentifier<?>> removeData = new HashSet<>();
        removeData.add(createTestFlowCapableNodePath());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(null, null, removeData));
        verify(notifProviderService, times(1)).publish(Matchers.any(NodeRemoved.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFromNullPath() {
        notifSupplierImpl.deleteNotification(null);
    }

    private static InstanceIdentifier<FlowCapableNode> createTestFlowCapableNodePath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(FLOW_NODE_ID)))
                .augmentation(FlowCapableNode.class);
    }

    private static FlowCapableNode createTestFlowCapableNode() {
        final FlowCapableNodeBuilder builder = new FlowCapableNodeBuilder();
        return builder.build();
    }
}

