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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.GroupNotificationSupplierImpl}.
 */
public class GroupNotificationSupplierImplTest {

    private static final String FLOW_NODE_ID = "openflow:111";
    private static final Long GROUP_ID = 111L;
    private static final Long  UPDATED_GROUP_ID = 100L;

    private GroupNotificationSupplierImpl notifSupplierImpl;
    private NotificationProviderService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationProviderService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new GroupNotificationSupplierImpl(notifProviderService, dataBroker);
        TestSupplierVerifyHelper.verifyDataTreeChangeListenerRegistration(dataBroker);
    }

    @Test(expected = NullPointerException.class)
    public void testNullChangeEvent() {
        notifSupplierImpl.onDataTreeChanged(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullableChangeEvent() {
        notifSupplierImpl.onDataTreeChanged( TestChangeEventBuildHelper.createNullTestDataTreeEvent());
    }

    @Test
    public void testEmptyChangeEvent() {
        notifSupplierImpl.onDataTreeChanged( TestChangeEventBuildHelper.createEmptyTestDataTreeEvent());
    }

    @Test
    public void testCreate() {
        final GroupAdded notification = notifSupplierImpl.createNotification(createTestGroup(), createTestGroupPath());
        assertNotNull(notification);
        assertEquals(GROUP_ID, notification.getGroupId().getValue());
        assertEquals(GROUP_ID, notification.getGroupRef().getValue().firstKeyOf(Group.class, GroupKey.class).getGroupId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testCreateChangeEvent() {
        final TestData testData = new TestData(createTestGroupPath(),null,createTestGroup(),
                DataObjectModification.ModificationType.WRITE);
        Collection<DataTreeModification<Group>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).publish(Matchers.any(GroupAdded.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestGroupPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullPath() {
        notifSupplierImpl.createNotification(createTestGroup(), null);
    }

    @Test
    public void testUpdate() {
        final GroupUpdated notification = notifSupplierImpl.updateNotification(createTestGroup(), createTestGroupPath());
        assertNotNull(notification);
        assertEquals(GROUP_ID, notification.getGroupId().getValue());
        assertEquals(GROUP_ID, notification.getGroupRef().getValue().firstKeyOf(Group.class, GroupKey.class).getGroupId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testUpdateChangeEvent() {
        final TestData testData = new TestData(createTestGroupPath(),createTestGroup(),createUpdatedTestGroup(),
                DataObjectModification.ModificationType.SUBTREE_MODIFIED);
        Collection<DataTreeModification<Group>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).publish(Matchers.any(GroupUpdated.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullNodeConnector() {
        notifSupplierImpl.createNotification(null, createTestGroupPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromNullPath() {
        notifSupplierImpl.createNotification(createTestGroup(), null);
    }

    @Test
    public void testDelete() {
        final GroupRemoved notification = notifSupplierImpl.deleteNotification(createTestGroupPath());
        assertNotNull(notification);
        assertEquals(GROUP_ID, notification.getGroupId().getValue());
        assertEquals(GROUP_ID, notification.getGroupRef().getValue().firstKeyOf(Group.class, GroupKey.class).getGroupId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testDeleteChangeEvent() {
        final TestData testData = new TestData(createTestGroupPath(),createTestGroup(),null,
                DataObjectModification.ModificationType.DELETE);
        Collection<DataTreeModification<Group>> collection = new ArrayList<>();
        collection.add(testData);
        notifSupplierImpl.onDataTreeChanged(collection);
        verify(notifProviderService, times(1)).publish(Matchers.any(GroupRemoved.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFromNullPath() {
        notifSupplierImpl.deleteNotification(null);
    }

    private static InstanceIdentifier<Group> createTestGroupPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(FLOW_NODE_ID)))
                .augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(new GroupId(GROUP_ID)));
    }

    private static Group createTestGroup() {
        final GroupBuilder builder = new GroupBuilder();
        builder.setGroupId(new GroupId(GROUP_ID));
        return builder.build();
    }

    private static Group createUpdatedTestGroup() {
        final GroupBuilder builder = new GroupBuilder();
        builder.setGroupId(new GroupId(UPDATED_GROUP_ID));
        return builder.build();
    }

}

