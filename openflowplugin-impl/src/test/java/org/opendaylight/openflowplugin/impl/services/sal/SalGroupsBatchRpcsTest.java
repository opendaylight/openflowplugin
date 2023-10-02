/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.OriginalBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.UpdatedBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link org.opendaylight.openflowplugin.impl.services.sal.SalGroupsBatchRpcs}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalGroupsBatchRpcsTest {

    public static final NodeId NODE_ID = new NodeId("ut-dummy-node");
    public static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    public static final NodeRef NODE_REF =
            new NodeRef(InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY));

    @Mock
    private SalGroupRpcs salGroupService;
    @Mock
    private SendBarrier sendBarrier;
    @Mock
    private RemoveGroup removeGroup;
    @Mock
    private UpdateGroup updateGroup;
    @Mock
    private AddGroup addGroup;
    @Mock
    private FlowCapableTransactionRpc transactionService;
    @Captor
    private ArgumentCaptor<RemoveGroupInput> removeGroupInputCpt;
    @Captor
    private ArgumentCaptor<UpdateGroupInput> updateGroupInputCpt;
    @Captor
    private ArgumentCaptor<AddGroupInput> addGroupInputCpt;

    private SalGroupsBatchRpcs salGroupsBatchService;


    @Before
    public void setUp() {
        salGroupsBatchService = new SalGroupsBatchRpcs(salGroupService, transactionService);

        Mockito.when(transactionService.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(SendBarrier.class, sendBarrier));
        Mockito.when(sendBarrier.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.<SendBarrierOutput>success().buildFuture());
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(addGroup, removeGroup, updateGroup, sendBarrier);
    }

    @Test
    public void testUpdateGroupsBatch_success() throws Exception {
        Mockito.when(salGroupService.getRpcClassToInstanceMap())
            .thenReturn(ImmutableClassToInstanceMap.of(UpdateGroup.class, updateGroup));
        Mockito.when(updateGroup.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new UpdateGroupOutputBuilder().build()).buildFuture());

        final UpdateGroupsBatchInput input = new UpdateGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateGroups(Lists.newArrayList(
                        createEmptyBatchUpdateGroup(42L),
                        createEmptyBatchUpdateGroup(44L)))
                .build();

        final Future<RpcResult<UpdateGroupsBatchOutput>> resultFuture = salGroupsBatchService.updateGroupsBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertTrue(resultFuture.get().isSuccessful());

        final InOrder inOrder = Mockito.inOrder(updateGroup, sendBarrier);
        inOrder.verify(updateGroup, Mockito.times(2)).invoke(updateGroupInputCpt.capture());
        final List<UpdateGroupInput> allValues = updateGroupInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getOriginalGroup().getGroupId().getValue().longValue());
        Assert.assertEquals(43, allValues.get(0).getUpdatedGroup().getGroupId().getValue().longValue());
        Assert.assertEquals(44, allValues.get(1).getOriginalGroup().getGroupId().getValue().longValue());
        Assert.assertEquals(45, allValues.get(1).getUpdatedGroup().getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testUpdateGroupsBatch_failure() throws Exception {
        Mockito.when(salGroupService.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(UpdateGroup.class, updateGroup));
        Mockito.when(updateGroup.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.<UpdateGroupOutput>failed()
                        .withError(ErrorType.APPLICATION, "ur-groupUpdateError")
                        .buildFuture());

        final UpdateGroupsBatchInput input = new UpdateGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateGroups(Lists.newArrayList(
                        createEmptyBatchUpdateGroup(42L),
                        createEmptyBatchUpdateGroup(44L)))
                .build();

        final Future<RpcResult<UpdateGroupsBatchOutput>> resultFuture = salGroupsBatchService.updateGroupsBatch(input);
        Iterator<BatchFailedGroupsOutput> iterator = resultFuture.get().getResult().nonnullBatchFailedGroupsOutput()
                .values().iterator();

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertFalse(resultFuture.get().isSuccessful());
        Assert.assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedGroupsOutput().size());
        Assert.assertEquals(43L, iterator.next().getGroupId().getValue().longValue());
        Assert.assertEquals(45L, iterator.next().getGroupId().getValue().longValue());
        Assert.assertEquals(2, resultFuture.get().getErrors().size());


        final InOrder inOrder = Mockito.inOrder(updateGroup, sendBarrier);
        inOrder.verify(updateGroup, Mockito.times(2)).invoke(updateGroupInputCpt.capture());
        final List<UpdateGroupInput> allValues = updateGroupInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getOriginalGroup().getGroupId().getValue().longValue());
        Assert.assertEquals(43, allValues.get(0).getUpdatedGroup().getGroupId().getValue().longValue());
        Assert.assertEquals(44, allValues.get(1).getOriginalGroup().getGroupId().getValue().longValue());
        Assert.assertEquals(45, allValues.get(1).getUpdatedGroup().getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }


    @Test
    public void testAddGroupsBatch_success() throws Exception {
        Mockito.when(salGroupService.getRpcClassToInstanceMap())
            .thenReturn(ImmutableClassToInstanceMap.of(AddGroup.class, addGroup));
        Mockito.when(addGroup.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new AddGroupOutputBuilder().build()).buildFuture());

        final AddGroupsBatchInput input = new AddGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddGroups(BindingMap.ordered(
                        createEmptyBatchAddGroup(42L),
                        createEmptyBatchAddGroup(43L)))
                .build();

        final Future<RpcResult<AddGroupsBatchOutput>> resultFuture = salGroupsBatchService.addGroupsBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertTrue(resultFuture.get().isSuccessful());

        final InOrder inOrder = Mockito.inOrder(addGroup, sendBarrier);
        inOrder.verify(addGroup, Mockito.times(2)).invoke(addGroupInputCpt.capture());
        final List<AddGroupInput> allValues = addGroupInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42L, allValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(43L, allValues.get(1).getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testAddGroupsBatch_failure() throws Exception {
        Mockito.when(salGroupService.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(AddGroup.class, addGroup));
        Mockito.when(addGroup.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.<AddGroupOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-groupAddError")
                        .buildFuture());

        final AddGroupsBatchInput input = new AddGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddGroups(BindingMap.ordered(
                        createEmptyBatchAddGroup(42L),
                        createEmptyBatchAddGroup(43L)))
                .build();

        final Future<RpcResult<AddGroupsBatchOutput>> resultFuture = salGroupsBatchService.addGroupsBatch(input);
        Iterator<BatchFailedGroupsOutput> iterator = resultFuture.get().getResult().nonnullBatchFailedGroupsOutput()
                .values().iterator();

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertFalse(resultFuture.get().isSuccessful());
        Assert.assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedGroupsOutput().size());
        Assert.assertEquals(42L, iterator.next().getGroupId().getValue().longValue());
        Assert.assertEquals(43L, iterator.next().getGroupId().getValue().longValue());
        Assert.assertEquals(2, resultFuture.get().getErrors().size());


        final InOrder inOrder = Mockito.inOrder(addGroup, sendBarrier);
        inOrder.verify(addGroup, Mockito.times(2)).invoke(addGroupInputCpt.capture());
        final List<AddGroupInput> allValues = addGroupInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42L, allValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(43L, allValues.get(1).getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testRemoveGroupsBatch_success() throws Exception {
        Mockito.when(salGroupService.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(RemoveGroup.class, removeGroup));
        Mockito.when(removeGroup.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.success(new RemoveGroupOutputBuilder().build()).buildFuture());

        final RemoveGroupsBatchInput input = new RemoveGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveGroups(BindingMap.ordered(
                        createEmptyBatchRemoveGroup(42L),
                        createEmptyBatchRemoveGroup(43L)))
                .build();

        final Future<RpcResult<RemoveGroupsBatchOutput>> resultFuture = salGroupsBatchService.removeGroupsBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertTrue(resultFuture.get().isSuccessful());

        final InOrder inOrder = Mockito.inOrder(removeGroup, sendBarrier);

        inOrder.verify(removeGroup, Mockito.times(2)).invoke(removeGroupInputCpt.capture());
        final List<RemoveGroupInput> allValues = removeGroupInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42L, allValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(43L, allValues.get(1).getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testRemoveGroupsBatch_failure() throws Exception {
        Mockito.when(salGroupService.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(RemoveGroup.class, removeGroup));
        Mockito.when(removeGroup.invoke(ArgumentMatchers.any()))
                .thenReturn(RpcResultBuilder.<RemoveGroupOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-groupRemoveError")
                        .buildFuture());

        final RemoveGroupsBatchInput input = new RemoveGroupsBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveGroups(BindingMap.ordered(
                        createEmptyBatchRemoveGroup(42L),
                        createEmptyBatchRemoveGroup(43L)))
                .build();

        final Future<RpcResult<RemoveGroupsBatchOutput>> resultFuture = salGroupsBatchService.removeGroupsBatch(input);
        Iterator<BatchFailedGroupsOutput> iterator = resultFuture.get().getResult().nonnullBatchFailedGroupsOutput()
                .values().iterator();

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertFalse(resultFuture.get().isSuccessful());
        Assert.assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedGroupsOutput().size());
        Assert.assertEquals(42L, iterator.next().getGroupId().getValue().longValue());
        Assert.assertEquals(43L, iterator.next().getGroupId().getValue().longValue());
        Assert.assertEquals(2, resultFuture.get().getErrors().size());

        final InOrder inOrder = Mockito.inOrder(removeGroup, sendBarrier);

        inOrder.verify(removeGroup, Mockito.times(2)).invoke(removeGroupInputCpt.capture());
        final List<RemoveGroupInput> allValues = removeGroupInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42L, allValues.get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(43L, allValues.get(1).getGroupId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
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