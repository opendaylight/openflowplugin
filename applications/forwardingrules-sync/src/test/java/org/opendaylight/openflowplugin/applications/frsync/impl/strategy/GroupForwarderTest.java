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
import org.opendaylight.openflowplugin.impl.services.sal.SalGroupRpcs;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
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

    private final KeyedInstanceIdentifier<Node, NodeKey> nodePath = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, s1Key);
    private final InstanceIdentifier<FlowCapableNode> flowCapableNodePath = nodePath
            .augmentation(FlowCapableNode.class);
    private final InstanceIdentifier<Group> groupPath = flowCapableNodePath.child(Group.class, groupKey);

    @Mock
    private SalGroupRpcs salGroupRpcs;
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
        groupForwarder = new GroupForwarder(salGroupRpcs);
        txId = new TransactionId(Uint64.ONE);
    }

    @Test
    public void testRemove() throws Exception {
        Mockito.when(salGroupRpcs.getRpcClassToInstanceMap().getInstance(RemoveGroup.class)
            .invoke(removeGroupInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(
                        new RemoveGroupOutputBuilder()
                                .setTransactionId(txId)
                                .build())
                        .buildFuture()
        );

        final Future<RpcResult<RemoveGroupOutput>> addResult =
                groupForwarder.remove(groupPath, group, flowCapableNodePath);

        Mockito.verify(salGroupRpcs).getRpcClassToInstanceMap().getInstance(RemoveGroup.class)
            .invoke(ArgumentMatchers.any());

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
        Mockito.when(salGroupRpcs.getRpcClassToInstanceMap().getInstance(UpdateGroup.class)
            .invoke(updateGroupInputCpt.capture())).thenReturn(
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

        Mockito.verify(salGroupRpcs).getRpcClassToInstanceMap().getInstance(UpdateGroup.class)
            .invoke(ArgumentMatchers.any());

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
        Mockito.when(salGroupRpcs.getRpcClassToInstanceMap().getInstance(AddGroup.class)
            .invoke(addGroupInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(
                        new AddGroupOutputBuilder()
                                .setTransactionId(txId)
                                .build())
                        .buildFuture()
        );

        final Future<RpcResult<AddGroupOutput>> addResult = groupForwarder.add(groupPath, group, flowCapableNodePath);

        Mockito.verify(salGroupRpcs).getRpcClassToInstanceMap().getInstance(AddGroup.class)
            .invoke(ArgumentMatchers.any());

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
