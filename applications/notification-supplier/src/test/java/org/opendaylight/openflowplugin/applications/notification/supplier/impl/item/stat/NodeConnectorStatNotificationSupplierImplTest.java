/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestChangeEventBuildHelper;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestData;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestSupplierVerifyHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatisticsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NodeConnectorStatNotificationSupplierImplTest {

    private static final String FLOW_NODE_ID = "openflow:111";
    private static final String FLOW_CODE_CONNECTOR_ID = "test-con-111";
    private NodeConnectorStatNotificationSupplierImpl notifSupplierImpl;
    private NotificationProviderService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationProviderService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new NodeConnectorStatNotificationSupplierImpl(notifProviderService, dataBroker);
        TestSupplierVerifyHelper.verifyDataTreeChangeListenerRegistration(dataBroker);
    }

    @Test(expected = NullPointerException.class)
    public void testNullChangeEvent() {
        notifSupplierImpl.onDataTreeChanged(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullableChangeEvent() {
        notifSupplierImpl.onDataTreeChanged(TestChangeEventBuildHelper.createNullTestDataTreeEvent());
    }

    @Test
    public void testEmptyChangeEvent() {
        notifSupplierImpl.onDataTreeChanged(TestChangeEventBuildHelper.createEmptyTestDataTreeEvent());
    }

    @Test
    public void testCreate() {
        final NodeConnectorStatisticsUpdate notification = notifSupplierImpl
                .createNotification(createTestConnectorStat(), createTestConnectorStatPath());
        assertNotNull(notification);
        assertEquals(FLOW_NODE_ID, notification.getId().getValue());
        assertEquals(FLOW_CODE_CONNECTOR_ID, notification.getNodeConnector().get(0).getId().getValue());
    }

    @Test
    public void testCreateChangeEvent() {
        final TestData testData = new TestData(createTestConnectorStatPath(), null, createTestConnectorStat(),
                                               DataObjectModification.ModificationType.WRITE);
        Collection<DataTreeModification<FlowCapableNodeConnectorStatistics>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).publish(any(NodeConnectorStatisticsUpdate.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestConnectorStatPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullPath() {
        notifSupplierImpl.createNotification(createTestConnectorStat(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestConnectorStatPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullPath() {
        notifSupplierImpl.createNotification(createTestConnectorStat(), null);
    }

    private static InstanceIdentifier<FlowCapableNodeConnectorStatistics> createTestConnectorStatPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(FLOW_NODE_ID)))
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId(FLOW_CODE_CONNECTOR_ID)))
                .augmentation(FlowCapableNodeConnectorStatisticsData.class)
                .child(FlowCapableNodeConnectorStatistics.class);
    }

    private static FlowCapableNodeConnectorStatistics createTestConnectorStat() {
        final FlowCapableNodeConnectorStatisticsBuilder builder = new FlowCapableNodeConnectorStatisticsBuilder();
        return builder.build();
    }
}

