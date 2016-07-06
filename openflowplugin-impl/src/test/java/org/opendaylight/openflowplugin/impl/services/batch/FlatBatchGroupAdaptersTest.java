/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.batch;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.group._case.FlatBatchAddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.group._case.FlatBatchAddGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.group._case.FlatBatchRemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.group._case.FlatBatchRemoveGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.group._case.FlatBatchUpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.group._case.FlatBatchUpdateGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureGroupIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureGroupIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.BatchGroupOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.OriginalBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.UpdatedBatchedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link FlatBatchGroupAdapters}.
 */
public class FlatBatchGroupAdaptersTest {

    private static final NodeId NODE_ID = new NodeId("ut-node-id");
    private static final InstanceIdentifier<Node> NODE_II = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID));
    private static final NodeRef NODE_REF = new NodeRef(NODE_II);

    @Test
    public void testAdaptFlatBatchAddGroup() throws Exception {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_ADD);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(Lists.newArrayList(
                createAddGroupBatch(1L),
                createAddGroupBatch(2L)));

        final AddGroupsBatchInput addGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchAddGroup(planStep, NODE_REF);

        Assert.assertTrue(addGroupsBatchInput.isBarrierAfter());
        Assert.assertEquals(2, addGroupsBatchInput.getBatchAddGroups().size());
        Assert.assertEquals(1L, addGroupsBatchInput.getBatchAddGroups().get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(2L, addGroupsBatchInput.getBatchAddGroups().get(1).getGroupId().getValue().longValue());
    }

    private FlatBatchAddGroup createAddGroupBatch(final long groupIdValue) {
        return new FlatBatchAddGroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .build();
    }

    private FlatBatchRemoveGroup createRemoveGroupBatch(final long groupIdValue) {
        return new FlatBatchRemoveGroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .build();
    }

    private FlatBatchUpdateGroup createUpdateGroupBatch(final long groupIdValue) {
        return new FlatBatchUpdateGroupBuilder()
                .setOriginalBatchedGroup(new OriginalBatchedGroupBuilder()
                        .setGroupId(new GroupId(groupIdValue))
                        .build())
                .setUpdatedBatchedGroup(new UpdatedBatchedGroupBuilder()
                        .setGroupId(new GroupId(groupIdValue))
                        .build())
                .build();
    }

    @Test
    public void testAdaptFlatBatchRemoveGroup() throws Exception {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_REMOVE);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(Lists.newArrayList(
                createRemoveGroupBatch(1L),
                createRemoveGroupBatch(2L)));

        final RemoveGroupsBatchInput removeGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchRemoveGroup(planStep, NODE_REF);

        Assert.assertTrue(removeGroupsBatchInput.isBarrierAfter());
        Assert.assertEquals(2, removeGroupsBatchInput.getBatchRemoveGroups().size());
        Assert.assertEquals(1L, removeGroupsBatchInput.getBatchRemoveGroups().get(0).getGroupId().getValue().longValue());
        Assert.assertEquals(2L, removeGroupsBatchInput.getBatchRemoveGroups().get(1).getGroupId().getValue().longValue());
    }

    @Test
    public void testAdaptFlatBatchUpdateGroup() throws Exception {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_UPDATE);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(Lists.newArrayList(
                createUpdateGroupBatch(1L),
                createUpdateGroupBatch(2L)));

        final UpdateGroupsBatchInput updateGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchUpdateGroup(planStep, NODE_REF);

        Assert.assertTrue(updateGroupsBatchInput.isBarrierAfter());
        Assert.assertEquals(2, updateGroupsBatchInput.getBatchUpdateGroups().size());
        Assert.assertEquals(1L, updateGroupsBatchInput.getBatchUpdateGroups().get(0).getUpdatedBatchedGroup().getGroupId().getValue().longValue());
        Assert.assertEquals(2L, updateGroupsBatchInput.getBatchUpdateGroups().get(1).getUpdatedBatchedGroup().getGroupId().getValue().longValue());
    }

    @Test
    public void testCreateBatchGroupChainingFunction_failures() throws Exception {
        final RpcResult<BatchGroupOutputListGrouping> input = RpcResultBuilder.<BatchGroupOutputListGrouping>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-groupError")
                .withResult(new AddGroupsBatchOutputBuilder()
                        .setBatchFailedGroupsOutput(Lists.newArrayList(
                                createBatchFailedGroupsOutput(0, 1L),
                                createBatchFailedGroupsOutput(1, 2L)
                        ))
                        .build())
                .build();

        final RpcResult<ProcessFlatBatchOutput> rpcResult = FlatBatchGroupAdapters
                .convertBatchGroupResult(3).apply(input);

        Assert.assertFalse(rpcResult.isSuccessful());
        Assert.assertEquals(1, rpcResult.getErrors().size());
        Assert.assertEquals(2, rpcResult.getResult().getBatchFailure().size());
        Assert.assertEquals(3, rpcResult.getResult().getBatchFailure().get(0).getBatchOrder().intValue());
        Assert.assertEquals(4, rpcResult.getResult().getBatchFailure().get(1).getBatchOrder().intValue());
        Assert.assertEquals(2L, ((FlatBatchFailureGroupIdCase) rpcResult.getResult().getBatchFailure().get(1).getBatchItemIdChoice()).getGroupId().getValue().longValue());
    }

    @Test
    public void testCreateBatchGroupChainingFunction_successes() throws Exception {
        final RpcResult<BatchGroupOutputListGrouping> input = RpcResultBuilder
                .<BatchGroupOutputListGrouping>success(new AddGroupsBatchOutputBuilder().build())
                .build();

        final RpcResult<ProcessFlatBatchOutput> rpcResult = FlatBatchGroupAdapters
                .convertBatchGroupResult(0).apply(input);

        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(0, rpcResult.getErrors().size());
        Assert.assertEquals(0, rpcResult.getResult().getBatchFailure().size());
    }

    private BatchFailedGroupsOutput createBatchFailedGroupsOutput(final Integer batchOrder, final long groupIdValue) {
        return new BatchFailedGroupsOutputBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .setBatchOrder(batchOrder)
                .build();
    }

    private BatchFailure createChainFailure(final int batchOrder, final long groupIdValue) {
        return new BatchFailureBuilder()
                .setBatchOrder(batchOrder)
                .setBatchItemIdChoice(new FlatBatchFailureGroupIdCaseBuilder()
                        .setGroupId(new GroupId(groupIdValue))
                        .build())
                .build();
    }
}