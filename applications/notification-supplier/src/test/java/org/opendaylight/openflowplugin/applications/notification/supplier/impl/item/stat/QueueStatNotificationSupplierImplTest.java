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
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestChangeEventBuildHelper;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestData;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestSupplierVerifyHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatisticsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;

public class QueueStatNotificationSupplierImplTest {

    private static final String FLOW_NODE_ID = "openflow:111";
    private static final String FLOW_CODE_CONNECTOR_ID = "test-con-111";
    private QueueStatNotificationSupplierImpl notifSupplierImpl;
    private NotificationPublishService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationPublishService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new QueueStatNotificationSupplierImpl(notifProviderService, dataBroker);
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
        final QueueStatisticsUpdate notification = notifSupplierImpl
                .createNotification(createTestQueueStat(), createTestQueueStatPath());
        assertNotNull(notification);
        assertEquals(FLOW_NODE_ID, notification.getId().getValue());
        assertEquals(FLOW_CODE_CONNECTOR_ID, notification.nonnullNodeConnector().values().iterator().next().getId()
                .getValue());
    }

    @Test
    public void testCreateChangeEvent() throws InterruptedException {
        final TestData<FlowCapableNodeConnectorQueueStatisticsData> testData = new TestData<>(createTestQueueStatPath(),
                null, createTestQueueStat(), DataObjectModification.ModificationType.WRITE);
        Collection<DataTreeModification<FlowCapableNodeConnectorQueueStatisticsData>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).putNotification(any(QueueStatisticsUpdate.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestQueueStatPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullPath() {
        notifSupplierImpl.createNotification(createTestQueueStat(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestQueueStatPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullPath() {
        notifSupplierImpl.createNotification(createTestQueueStat(), null);
    }

    private static InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> createTestQueueStatPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(FLOW_NODE_ID)))
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId(FLOW_CODE_CONNECTOR_ID)))
                .augmentation(FlowCapableNodeConnector.class).child(Queue.class, new QueueKey(new QueueId(Uint32.ZERO)))
                .augmentation(FlowCapableNodeConnectorQueueStatisticsData.class);
    }

    private static FlowCapableNodeConnectorQueueStatisticsData createTestQueueStat() {
        return new FlowCapableNodeConnectorQueueStatisticsDataBuilder()
            .setFlowCapableNodeConnectorQueueStatistics(new FlowCapableNodeConnectorQueueStatisticsBuilder().build())
            .build();
    }
}