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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;

public class MeterStatNotificationSupplierImplTest {
    private static final String FLOW_NODE_ID = "openflow:111";
    private static final Uint32 FLOW_METER_ID = Uint32.valueOf(111);

    private MeterStatNotificationSupplierImpl notifSupplierImpl;
    private NotificationPublishService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationPublishService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new MeterStatNotificationSupplierImpl(notifProviderService, dataBroker);
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
        final MeterStatisticsUpdated notification = notifSupplierImpl.createNotification(createTestMeterStat(),
                createTestMeterStatPath());
        assertNotNull(notification);
        assertEquals(FLOW_NODE_ID, notification.getId().getValue());
    }

    @Test
    public void testCreateChangeEvent() throws InterruptedException {
        final TestData<MeterStatistics> testData = new TestData<>(createTestMeterStatPath(),null,createTestMeterStat(),
                DataObjectModification.ModificationType.WRITE);
        Collection<DataTreeModification<MeterStatistics>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).putNotification(any(MeterStatisticsUpdated.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestMeterStatPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullPath() {
        notifSupplierImpl.createNotification(createTestMeterStat(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestMeterStatPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullPath() {
        notifSupplierImpl.createNotification(createTestMeterStat(), null);
    }

    private static InstanceIdentifier<MeterStatistics> createTestMeterStatPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(FLOW_NODE_ID)))
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(new MeterId(FLOW_METER_ID)))
                .augmentation(NodeMeterStatistics.class).child(MeterStatistics.class);
    }

    private static MeterStatistics createTestMeterStat() {
        final MeterStatisticsBuilder builder = new MeterStatisticsBuilder();
        return builder.build();
    }
}

