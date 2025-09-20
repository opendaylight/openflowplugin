/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Test for {@link GroupForwarder}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupForwarderTest {

    private final NodeKey s1Key = new NodeKey(new NodeId("S1"));
    private final GroupId groupId = new GroupId(Uint32.valueOf(42));
    private final GroupKey groupKey = new GroupKey(groupId);
    private final Group group = new GroupBuilder()
            .setGroupId(groupId)
            .setGroupName("test-group")
            .setBuckets(new BucketsBuilder().build())
            .build();

    private final DataObjectIdentifier.WithKey<Node, NodeKey> nodePath =
        DataObjectIdentifier.builder(Nodes.class).child(Node.class, s1Key).build();
    private final DataObjectIdentifier<FlowCapableNode> flowCapableNodePath =
        nodePath.toBuilder().augmentation(FlowCapableNode.class).build();
    private final DataObjectIdentifier<Group> groupPath =
        flowCapableNodePath.toBuilder().child(Group.class, groupKey).build();

    @Mock
    private RpcService rpcConsumerRegistry;
    @Mock
    private AddGroup addGroup;
    @Mock
    private UpdateGroup updateGroup;
    @Mock
    private RemoveGroup removeGroup;
    @Captor
    private ArgumentCaptor<AddGroupInput> addGroupInputCpt;
    @Captor
    private ArgumentCaptor<RemoveGroupInput> removeGroupInputCpt;
    @Captor
    private ArgumentCaptor<UpdateGroupInput> updateGroupInputCpt;

    private TransactionId txId;

    private GroupForwarder groupForwarder;

    @Before
    public void setUp() {
        Mockito.when(rpcConsumerRegistry.getRpc(RemoveGroup.class))
            .thenReturn(removeGroup);
        Mockito.when(rpcConsumerRegistry.getRpc(UpdateGroup.class))
            .thenReturn(updateGroup);
        Mockito.when(rpcConsumerRegistry.getRpc(AddGroup.class))
            .thenReturn(addGroup);

        groupForwarder = new GroupForwarder(rpcConsumerRegistry);
        txId = new TransactionId(Uint64.ONE);
    }

    @Test
    public void testRemove() throws Exception {
        Mockito.when(removeGroup.invoke(removeGroupInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(
                        new RemoveGroupOutputBuilder()
                                .setTransactionId(txId)
                                .build())
                        .buildFuture()
        );

        final Future<RpcResult<RemoveGroupOutput>> addResult =
                groupForwarder.remove(groupPath, group, flowCapableNodePath);

        Mockito.verify(removeGroup).invoke(ArgumentMatchers.any());

        Assert.assertTrue(addResult.isDone());
        final RpcResult<RemoveGroupOutput> result = addResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(result.isSuccessful());

        Assert.assertEquals(1, result.getResult().getTransactionId().getValue().intValue());

        final RemoveGroupInput removeGroupInput = removeGroupInputCpt.getValue();
        Assert.assertEquals(groupPath, removeGroupInput.getGroupRef().getValue());
        Assert.assertNull(removeGroupInput.getBuckets());
        Assert.assertEquals(nodePath, removeGroupInput.getNode().getValue());
        Assert.assertEquals("test-group", removeGroupInput.getGroupName());
    }

    @Test
    public void testUpdate() throws Exception {
        Mockito.when(updateGroup.invoke(updateGroupInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(
                        new UpdateGroupOutputBuilder()
                                .setTransactionId(txId)
                                .build())
                        .buildFuture()
        );

        Group groupOriginal = new GroupBuilder(group).build();
        Group groupUpdate = new GroupBuilder(group)
                .setGroupName("another-test")
                .build();

        final Future<RpcResult<UpdateGroupOutput>> addResult =
                groupForwarder.update(groupPath, groupOriginal, groupUpdate, flowCapableNodePath);

        Mockito.verify(updateGroup).invoke(ArgumentMatchers.any());

        Assert.assertTrue(addResult.isDone());
        final RpcResult<UpdateGroupOutput> result = addResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(result.isSuccessful());

        Assert.assertEquals(1, result.getResult().getTransactionId().getValue().intValue());

        final UpdateGroupInput updateGroupInput = updateGroupInputCpt.getValue();
        Assert.assertEquals(groupPath, updateGroupInput.getGroupRef().getValue());
        Assert.assertEquals(nodePath, updateGroupInput.getNode().getValue());
        Assert.assertNotNull(updateGroupInput.getOriginalGroup().getBuckets());
        Assert.assertNotNull(updateGroupInput.getUpdatedGroup().getBuckets());

        Assert.assertEquals("test-group", updateGroupInput.getOriginalGroup().getGroupName());
        Assert.assertEquals("another-test", updateGroupInput.getUpdatedGroup().getGroupName());
    }

    @Test
    public void testAdd() throws Exception {
        Mockito.when(addGroup.invoke(addGroupInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(
                        new AddGroupOutputBuilder()
                                .setTransactionId(txId)
                                .build())
                        .buildFuture()
        );

        final Future<RpcResult<AddGroupOutput>> addResult = groupForwarder.add(groupPath, group, flowCapableNodePath);

        Mockito.verify(addGroup).invoke(ArgumentMatchers.any());

        Assert.assertTrue(addResult.isDone());
        final RpcResult<AddGroupOutput> result = addResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(result.isSuccessful());

        Assert.assertEquals(1, result.getResult().getTransactionId().getValue().intValue());

        final AddGroupInput addGroupInput = addGroupInputCpt.getValue();
        Assert.assertEquals(groupPath, addGroupInput.getGroupRef().getValue());
        Assert.assertEquals(nodePath, addGroupInput.getNode().getValue());
        Assert.assertNotNull(addGroupInput.getBuckets());
        Assert.assertEquals("test-group", addGroupInput.getGroupName());
    }
}
