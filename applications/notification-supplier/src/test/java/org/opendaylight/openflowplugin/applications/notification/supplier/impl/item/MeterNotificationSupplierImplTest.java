/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestChangeEventBuildHelper;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestData;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.helper.TestSupplierVerifyHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.MeterNotificationSupplierImpl}.
 */
public class MeterNotificationSupplierImplTest {

    private static final String FLOW_NODE_ID = "openfow:111";
    private static final Long METER_ID = 111L;
    private static final Long UPDATED_METER_ID = 100L;

    private MeterNotificationSupplierImpl notifSupplierImpl;
    private NotificationProviderService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationProviderService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new MeterNotificationSupplierImpl(notifProviderService, dataBroker);
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
        final MeterAdded notification = notifSupplierImpl.createNotification(createTestMeter(), createTestMeterPath());
        assertNotNull(notification);
        assertEquals(METER_ID, notification.getMeterId().getValue());
        assertEquals(METER_ID, notification.getMeterRef().getValue().firstKeyOf(Meter.class, MeterKey.class).getMeterId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testCreateChangeEvent() {
        final TestData testData = new TestData(createTestMeterPath(),null,createTestMeter(),
                DataObjectModification.ModificationType.WRITE);
        Collection<DataTreeModification<Meter>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).publish(Matchers.any(MeterAdded.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestMeterPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullPath() {
        notifSupplierImpl.createNotification(createTestMeter(), null);
    }

    @Test
    public void testUpdate() {
        final MeterUpdated notification = notifSupplierImpl
                .updateNotification(createTestMeter(), createTestMeterPath());
        assertNotNull(notification);
        assertEquals(METER_ID, notification.getMeterId().getValue());
        assertEquals(METER_ID, notification.getMeterRef().getValue().firstKeyOf(Meter.class, MeterKey.class).getMeterId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testUdateChangeEvent() {
        final TestData testData = new TestData(createTestMeterPath(),createTestMeter(),createUpdatedTestMeter(),
                DataObjectModification.ModificationType.SUBTREE_MODIFIED);
        Collection<DataTreeModification<Meter>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).publish(Matchers.any(MeterUpdated.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestMeterPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullPath() {
        notifSupplierImpl.createNotification(createTestMeter(), null);
    }

    @Test
    public void testDelete() {
        final MeterRemoved notification = notifSupplierImpl.deleteNotification(createTestMeterPath());
        assertNotNull(notification);
        assertEquals(METER_ID, notification.getMeterId().getValue());
        assertEquals(METER_ID, notification.getMeterRef().getValue().firstKeyOf(Meter.class, MeterKey.class).getMeterId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testDeleteChangeEvent() {
        final TestData testData = new TestData(createTestMeterPath(),createTestMeter(),null,
                DataObjectModification.ModificationType.DELETE);
        Collection<DataTreeModification<Meter>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).publish(Matchers.any(MeterRemoved.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFromNullPath() {
        notifSupplierImpl.deleteNotification(null);
    }

    private static InstanceIdentifier<Meter> createTestMeterPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(FLOW_NODE_ID)))
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(new MeterId(METER_ID)));
    }

    private static Meter createTestMeter() {
        final MeterBuilder builder = new MeterBuilder();
        builder.setMeterId(new MeterId(METER_ID));
        return builder.build();
    }

    private static Meter createUpdatedTestMeter(){
        final MeterBuilder builder = new MeterBuilder();
        builder.setMeterId(new MeterId(UPDATED_METER_ID));
        return builder.build();
    }
}

