/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl;

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
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestChangeEventBuildHelper;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestSupplierVerifyHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class NodeConnectorNotificationSupplierImplTest {

    private static final String FLOW_NODE_ID = "test-111";
    private static final String FLOW_CODE_CONNECTOR_ID = "test-con-111";
    private NodeConnectorNotificationSupplierImpl notifSupplierImpl;
    private NotificationProviderService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationProviderService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new NodeConnectorNotificationSupplierImpl(notifProviderService, dataBroker);
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
        final NodeConnectorUpdated notification = notifSupplierImpl.createNotification(createTestFlowCapableNodeConnecor(),
                createTestFlowCapableConnectorNodePath());
        assertNotNull(notification);
        assertEquals(FLOW_CODE_CONNECTOR_ID, notification.getId().getValue());
        assertEquals(FLOW_CODE_CONNECTOR_ID, notification.getNodeConnectorRef().getValue()
                .firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNodeConnectorRef().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testCreateChangeEvent() {
        final Map<InstanceIdentifier<?>, DataObject> createdData = new HashMap<>();
        createdData.put(createTestFlowCapableConnectorNodePath(), createTestFlowCapableNodeConnecor());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(createdData, null, null));
        verify(notifProviderService, times(1)).publish(Matchers.any(NodeConnectorUpdated.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestFlowCapableConnectorNodePath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullPath() {
        notifSupplierImpl.createNotification(createTestFlowCapableNodeConnecor(), null);
    }

    @Test
    public void testDelete() {
        final NodeConnectorRemoved notification = notifSupplierImpl.deleteNotification(createTestFlowCapableConnectorNodePath());
        assertNotNull(notification);
        assertEquals(FLOW_CODE_CONNECTOR_ID, notification.getNodeConnectorRef().getValue()
                .firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNodeConnectorRef().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testDeleteChangeEvent() {
        final Set<InstanceIdentifier<?>> removeData = new HashSet<>();
        removeData.add(createTestFlowCapableConnectorNodePath());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(null, null, removeData));
        verify(notifProviderService, times(1)).publish(Matchers.any(NodeConnectorRemoved.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFromNullPath() {
        notifSupplierImpl.deleteNotification(null);
    }

    private static InstanceIdentifier<FlowCapableNodeConnector> createTestFlowCapableConnectorNodePath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(FLOW_NODE_ID)))
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId(FLOW_CODE_CONNECTOR_ID))).augmentation(FlowCapableNodeConnector.class);
    }

    private static FlowCapableNodeConnector createTestFlowCapableNodeConnecor() {
        final FlowCapableNodeConnectorBuilder builder = new FlowCapableNodeConnectorBuilder();
        return builder.build();
    }
}

