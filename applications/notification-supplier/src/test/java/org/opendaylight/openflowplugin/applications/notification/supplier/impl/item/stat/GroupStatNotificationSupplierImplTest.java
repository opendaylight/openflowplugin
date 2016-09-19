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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GroupStatNotificationSupplierImplTest {

    private static final String FLOW_NODE_ID = "openflow:111";
    private static final Long FLOW_TABLE_ID = 111L;
    private GroupStatNotificationSupplierImpl notifSupplierImpl;
    private NotificationProviderService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationProviderService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new GroupStatNotificationSupplierImpl(notifProviderService, dataBroker);
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
        notifSupplierImpl.onDataTreeChanged( TestChangeEventBuildHelper.createEmptyTestDataTreeEvent());
    }

    @Test
    public void testCreate() {
        final GroupStatisticsUpdated notification = notifSupplierImpl.createNotification(createTestGroupStat(),
                createTestGroupStatPath());
        assertNotNull(notification);
        assertEquals(FLOW_NODE_ID, notification.getId().getValue());
    }

    @Test
    public void testCreateChangeEvent() {
        final TestData testData = new TestData(createTestGroupStatPath(),null,createTestGroupStat(),
                DataObjectModification.ModificationType.WRITE);
        Collection<DataTreeModification<GroupStatistics>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).publish(Matchers.any(GroupStatisticsUpdated.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestGroupStatPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullPath() {
        notifSupplierImpl.createNotification(createTestGroupStat(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestGroupStatPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullPath() {
        notifSupplierImpl.createNotification(createTestGroupStat(), null);
    }

    private static InstanceIdentifier<GroupStatistics> createTestGroupStatPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(FLOW_NODE_ID)))
                .augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(new GroupId(FLOW_TABLE_ID)))
                .augmentation(NodeGroupStatistics.class).child(GroupStatistics.class);
    }

    private static GroupStatistics createTestGroupStat() {
        final GroupStatisticsBuilder builder = new GroupStatisticsBuilder();
        return builder.build();
    }
}

