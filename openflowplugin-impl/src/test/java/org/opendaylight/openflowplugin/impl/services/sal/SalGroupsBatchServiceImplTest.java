/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.OriginalBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.UpdatedBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link org.opendaylight.openflowplugin.impl.services.sal.SalGroupsBatchServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalGroupsBatchServiceImplTest {
    public static final NodeId NODE_ID = new NodeId("ut-dummy-node");
    public static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    public static final NodeRef NODE_REF =
            new NodeRef(InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY).toIdentifier());

    @Mock
    private AddGroup addGroup;
    @Mock
    private RemoveGroup removeGroup;
    @Mock
    private UpdateGroup updateGroup;
    @Mock
    private SendBarrier sendBarrier;
    @Captor
    private ArgumentCaptor<RemoveGroupInput> removeGroupInputCpt;
    @Captor
    private ArgumentCaptor<UpdateGroupInput> updateGroupInputCpt;
    @Captor
    private ArgumentCaptor<AddGroupInput> addGroupInputCpt;

    private AddGroupsBatchImpl addGroupsBatch;
    private RemoveGroupsBatchImpl removeGroupsBatch;
    private UpdateGroupsBatchImpl updateGroupsBatch;

    @Before
    public void setUp() {
        addGroupsBatch = new AddGroupsBatchImpl(addGroup, sendBarrier);
        removeGroupsBatch = new RemoveGroupsBatchImpl(removeGroup, sendBarrier);
        updateGroupsBatch = new UpdateGroupsBatchImpl(updateGroup, sendBarrier);

        when(sendBarrier.invoke(any())).thenReturn(RpcResultBuilder.<SendBarrierOutput>success().buildFuture());
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(addGroup, removeGroup, updateGroup, sendBarrier);
    }

    @Test
    public void testUpdateGroupsBatch_success() throws Exception {
        when(updateGroup.invoke(any()))
                .thenReturn(RpcResultBuilder.success(new UpdateGroupOutputBuilder().build()).buildFuture());

        final var input = new UpdateGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateGroups(List.of(
                        createEmptyBatchUpdateGroup(42L),
                        createEmptyBatchUpdateGroup(44L)))
                .build();

        final var resultFuture = updateGroupsBatch.invoke(input);

        assertTrue(resultFuture.isDone());
        assertTrue(resultFuture.get().isSuccessful());

        final var inOrder = inOrder(updateGroup, sendBarrier);
        inOrder.verify(updateGroup, times(2)).invoke(updateGroupInputCpt.capture());
        final var allValues = updateGroupInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getOriginalGroup().getGroupId().getValue().longValue());
        assertEquals(43, allValues.get(0).getUpdatedGroup().getGroupId().getValue().longValue());
        assertEquals(44, allValues.get(1).getOriginalGroup().getGroupId().getValue().longValue());
        assertEquals(45, allValues.get(1).getUpdatedGroup().getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testUpdateGroupsBatch_failure() throws Exception {
        when(updateGroup.invoke(any()))
                .thenReturn(RpcResultBuilder.<UpdateGroupOutput>failed()
                        .withError(ErrorType.APPLICATION, "ur-groupUpdateError")
                        .buildFuture());

        final var input = new UpdateGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateGroups(List.of(
                        createEmptyBatchUpdateGroup(42L),
                        createEmptyBatchUpdateGroup(44L)))
                .build();

        final var resultFuture = updateGroupsBatch.invoke(input);
        final var iterator = resultFuture.get().getResult().nonnullBatchFailedGroupsOutput()
                .values().iterator();

        assertTrue(resultFuture.isDone());
        assertFalse(resultFuture.get().isSuccessful());
        assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedGroupsOutput().size());
        assertEquals(43L, iterator.next().getGroupId().getValue().longValue());
        assertEquals(45L, iterator.next().getGroupId().getValue().longValue());
        assertEquals(2, resultFuture.get().getErrors().size());


        final var inOrder = inOrder(updateGroup, sendBarrier);
        inOrder.verify(updateGroup, times(2)).invoke(updateGroupInputCpt.capture());
        final var allValues = updateGroupInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getOriginalGroup().getGroupId().getValue().longValue());
        assertEquals(43, allValues.get(0).getUpdatedGroup().getGroupId().getValue().longValue());
        assertEquals(44, allValues.get(1).getOriginalGroup().getGroupId().getValue().longValue());
        assertEquals(45, allValues.get(1).getUpdatedGroup().getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }


    @Test
    public void testAddGroupsBatch_success() throws Exception {
        when(addGroup.invoke(any()))
                .thenReturn(RpcResultBuilder.success(new AddGroupOutputBuilder().build()).buildFuture());

        final var input = new AddGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddGroups(BindingMap.ordered(
                        createEmptyBatchAddGroup(42L),
                        createEmptyBatchAddGroup(43L)))
                .build();

        final var resultFuture = addGroupsBatch.invoke(input);

        assertTrue(resultFuture.isDone());
        assertTrue(resultFuture.get().isSuccessful());

        final var inOrder = inOrder(addGroup, sendBarrier);
        inOrder.verify(addGroup, times(2)).invoke(addGroupInputCpt.capture());
        final var allValues = addGroupInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42L, allValues.get(0).getGroupId().getValue().longValue());
        assertEquals(43L, allValues.get(1).getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testAddGroupsBatch_failure() throws Exception {
        when(addGroup.invoke(any()))
                .thenReturn(RpcResultBuilder.<AddGroupOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-groupAddError")
                        .buildFuture());

        final var input = new AddGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddGroups(BindingMap.ordered(
                        createEmptyBatchAddGroup(42L),
                        createEmptyBatchAddGroup(43L)))
                .build();

        final var resultFuture = addGroupsBatch.invoke(input);
        final var iterator = resultFuture.get().getResult().nonnullBatchFailedGroupsOutput().values().iterator();

        assertTrue(resultFuture.isDone());
        assertFalse(resultFuture.get().isSuccessful());
        assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedGroupsOutput().size());
        assertEquals(42L, iterator.next().getGroupId().getValue().longValue());
        assertEquals(43L, iterator.next().getGroupId().getValue().longValue());
        assertEquals(2, resultFuture.get().getErrors().size());


        final var inOrder = inOrder(addGroup, sendBarrier);
        inOrder.verify(addGroup, times(2)).invoke(addGroupInputCpt.capture());
        final var allValues = addGroupInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42L, allValues.get(0).getGroupId().getValue().longValue());
        assertEquals(43L, allValues.get(1).getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testRemoveGroupsBatch_success() throws Exception {
        when(removeGroup.invoke(any()))
                .thenReturn(RpcResultBuilder.success(new RemoveGroupOutputBuilder().build()).buildFuture());

        final var input = new RemoveGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveGroups(BindingMap.ordered(
                        createEmptyBatchRemoveGroup(42L),
                        createEmptyBatchRemoveGroup(43L)))
                .build();

        final var resultFuture = removeGroupsBatch.invoke(input);

        assertTrue(resultFuture.isDone());
        assertTrue(resultFuture.get().isSuccessful());

        final var inOrder = inOrder(removeGroup, sendBarrier);

        inOrder.verify(removeGroup, times(2)).invoke(removeGroupInputCpt.capture());
        final var allValues = removeGroupInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42L, allValues.get(0).getGroupId().getValue().longValue());
        assertEquals(43L, allValues.get(1).getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testRemoveGroupsBatch_failure() throws Exception {
        when(removeGroup.invoke(any()))
                .thenReturn(RpcResultBuilder.<RemoveGroupOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-groupRemoveError")
                        .buildFuture());

        final var input = new RemoveGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveGroups(BindingMap.ordered(
                        createEmptyBatchRemoveGroup(42L),
                        createEmptyBatchRemoveGroup(43L)))
                .build();

        final var resultFuture = removeGroupsBatch.invoke(input);
        final var iterator = resultFuture.get().getResult().nonnullBatchFailedGroupsOutput().values().iterator();

        assertTrue(resultFuture.isDone());
        assertFalse(resultFuture.get().isSuccessful());
        assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedGroupsOutput().size());
        assertEquals(42L, iterator.next().getGroupId().getValue().longValue());
        assertEquals(43L, iterator.next().getGroupId().getValue().longValue());
        assertEquals(2, resultFuture.get().getErrors().size());

        final var inOrder = inOrder(removeGroup, sendBarrier);

        inOrder.verify(removeGroup, times(2)).invoke(removeGroupInputCpt.capture());
        final var allValues = removeGroupInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42L, allValues.get(0).getGroupId().getValue().longValue());
        assertEquals(43L, allValues.get(1).getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    private static BatchAddGroups createEmptyBatchAddGroup(final long groupIdValue) {
        return new BatchAddGroupsBuilder()
                .setGroupId(new GroupId(Uint32.valueOf(groupIdValue)))
                .build();
    }

    private static BatchRemoveGroups createEmptyBatchRemoveGroup(final long groupIdValue) {
        return new BatchRemoveGroupsBuilder()
                .setGroupId(new GroupId(Uint32.valueOf(groupIdValue)))
                .build();
    }

    private static BatchUpdateGroups createEmptyBatchUpdateGroup(final long groupIdValue) {
        return new BatchUpdateGroupsBuilder()
                .setOriginalBatchedGroup(
                        new OriginalBatchedGroupBuilder(createEmptyBatchAddGroup(groupIdValue)).build())
                .setUpdatedBatchedGroup(
                        new UpdatedBatchedGroupBuilder(createEmptyBatchAddGroup(groupIdValue + 1)).build())
                .build();
    }
}
