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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class GroupNotificationSupplierImplTest {

    private static final String FLOW_NODE_ID = "test-111";
    private static final Long GROUP_ID = 111L;
    private GroupNotificationSupplierImpl notifSupplierImpl;
    private NotificationProviderService notifProviderService;
    private DataBroker dataBroker;

    @Before
    public void initalization() {
        notifProviderService = mock(NotificationProviderService.class);
        dataBroker = mock(DataBroker.class);
        notifSupplierImpl = new GroupNotificationSupplierImpl(notifProviderService, dataBroker);
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
        final GroupAdded notification = notifSupplierImpl.createNotification(createTestGroup(), createTestGroupPath());
        assertNotNull(notification);
        assertEquals(GROUP_ID, notification.getGroupId().getValue());
        assertEquals(GROUP_ID, notification.getGroupRef().getValue().firstKeyOf(Group.class, GroupKey.class).getGroupId().getValue());
        assertEquals(FLOW_NODE_ID, notification.getNode().getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue());
    }

    @Test
    public void testCreateChangeEvent() {
        final Map<InstanceIdentifier<?>, DataObject> createdData = new HashMap<>();
        createdData.put(createTestGroupPath(), createTestGroup());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(createdData, null, null));
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
        final Map<InstanceIdentifier<?>, DataObject> createdData = new HashMap<>();
        createdData.put(createTestGroupPath(), createTestGroup());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(createdData, null, null));
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
        final Set<InstanceIdentifier<?>> removeData = new HashSet<>();
        removeData.add(createTestGroupPath());
        notifSupplierImpl.onDataChanged(TestChangeEventBuildHelper.createTestDataEvent(null, null, removeData));
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
}

