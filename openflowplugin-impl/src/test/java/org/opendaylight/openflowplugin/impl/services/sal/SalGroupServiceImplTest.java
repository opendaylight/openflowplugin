/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;

public class SalGroupServiceImplTest extends ServiceMocking {
    private static final Uint32 DUMMY_GROUP_ID = Uint32.valueOf(15);
    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_II =
        InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(DUMMY_NODE_ID)));

    private final NodeRef noderef = new NodeRef(NODE_II.toIdentifier());

    @Mock
    private DeviceGroupRegistry mockedDeviceGroupRegistry;

    private AddGroupImpl addGroup;
    private RemoveGroupImpl removeGroup;
    private UpdateGroupImpl updateGroup;

    @Override
    protected void setup() {
        final var convertorManager = ConvertorManagerFactory.createDefaultManager();
        addGroup = new AddGroupImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
        removeGroup = new RemoveGroupImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
        updateGroup = new UpdateGroupImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
    }

    @Test
    public void testAddGroup() {
        addGroup();
    }

    private void addGroup() {
        final var dummyGroupId = new GroupId(DUMMY_GROUP_ID);
        final var addGroupInput = new AddGroupInputBuilder().setGroupId(dummyGroupId).setNode(noderef).build();

        this.<AddGroupOutput>mockSuccessfulFuture();

        addGroup.invoke(addGroupInput);
        verify(mockedRequestContextStack).createRequestContext();

    }

    @Test
    public void testUpdateGroup() {
        updateGroup();
    }

    @Test
    public void testUpdateGroupWithItemLifecycle() {
        updateGroup();
    }

    private void updateGroup() {
        final var updatedGroup = new UpdatedGroupBuilder().setGroupId(new GroupId(DUMMY_GROUP_ID)).build();
        final var originalGroup = new OriginalGroupBuilder().setGroupId(new GroupId(DUMMY_GROUP_ID)).build();
        final var updateGroupInput =
                new UpdateGroupInputBuilder().setUpdatedGroup(updatedGroup).setOriginalGroup(originalGroup).build();

        this.<UpdateGroupOutput>mockSuccessfulFuture();
        updateGroup.invoke(updateGroupInput);
        verify(mockedRequestContextStack).createRequestContext();

    }

    @Test
    public void testRemoveGroup() {
        removeGroup();
    }

    @Test
    public void testRemoveGroupWithItemLifecycle() {
        removeGroup();
    }

    private void removeGroup() {
        final var dummyGroupId = new GroupId(DUMMY_GROUP_ID);
        final var removeGroupInput = new RemoveGroupInputBuilder().setGroupId(dummyGroupId).build();
        this.<RemoveGroupOutput>mockSuccessfulFuture();
        removeGroup.invoke(removeGroupInput);
        verify(mockedRequestContextStack).createRequestContext();

    }
}
