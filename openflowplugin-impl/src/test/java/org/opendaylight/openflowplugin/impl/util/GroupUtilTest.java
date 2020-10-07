/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.BatchGroupOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link GroupUtil}.
 */
public class GroupUtilTest {

    private static final NodeId DUMMY_NODE_ID = new NodeId("dummyNodeId");
    private static final GroupId DUMMY_GROUP_ID = new GroupId(Uint32.valueOf(42));
    private static final GroupId DUMMY_GROUP_ID_2 = new GroupId(Uint32.valueOf(43));
    private static final long GROUP_ACTION_BITMAP =
            0b00000000000000000000000000000000000001111111111111001100000000001L;

    @Test
    public void testBuildGroupPath() {
        final InstanceIdentifier<Node> nodePath = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(DUMMY_NODE_ID));

        final GroupRef groupRef = GroupUtil.buildGroupPath(nodePath, DUMMY_GROUP_ID);
        final InstanceIdentifier<?> groupRefValue = groupRef.getValue();
        Assert.assertEquals(DUMMY_NODE_ID, groupRefValue.firstKeyOf(Node.class).getId());
        Assert.assertEquals(DUMMY_GROUP_ID, groupRefValue.firstKeyOf(Group.class).getGroupId());
    }

    @Test
    public void testCreateCumulatingFunction() {
        final Function<List<RpcResult<String>>, RpcResult<List<BatchFailedGroupsOutput>>> function =
                GroupUtil.createCumulatingFunction(Lists.newArrayList(createBatchGroup(DUMMY_GROUP_ID),
                        createBatchGroup(DUMMY_GROUP_ID_2)));

        final RpcResult<List<BatchFailedGroupsOutput>> summary = function.apply(Lists.newArrayList(
                RpcResultBuilder.success("a").build(),
                RpcResultBuilder.<String>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "action-failed reason")
                        .build()));

        Assert.assertFalse(summary.isSuccessful());
        Assert.assertEquals(1, summary.getResult().size());
        Assert.assertEquals(1, summary.getErrors().size());
        Assert.assertEquals(DUMMY_GROUP_ID_2, summary.getResult().get(0).getGroupId());
        Assert.assertEquals(1, summary.getResult().get(0).getBatchOrder().intValue());
    }

    protected Group createBatchGroup(final GroupId groupId) {
        return new GroupBuilder().setGroupId(groupId).build();
    }

    @Test
    public void testGroupAddTransformFailure() {
        final RpcResult<List<BatchFailedGroupsOutput>> input = createBatchOutcomeWithError();
        checkBatchErrorOutcomeTransformation(GroupUtil.GROUP_ADD_TRANSFORM.apply(input));
    }

    @Test
    public void testGroupAddTransformFailureSuccess() {
        final RpcResult<List<BatchFailedGroupsOutput>> input = createEmptyBatchOutcome();
        checkBatchSuccessOutcomeTransformation(GroupUtil.GROUP_ADD_TRANSFORM.apply(input));
    }

    @Test
    public void testGroupRemoveTransformFailure() {
        final RpcResult<List<BatchFailedGroupsOutput>> input = createBatchOutcomeWithError();
        checkBatchErrorOutcomeTransformation(GroupUtil.GROUP_REMOVE_TRANSFORM.apply(input));
    }

    @Test
    public void testFlowRemoveTransformSuccess() {
        final RpcResult<List<BatchFailedGroupsOutput>> input = createEmptyBatchOutcome();
        checkBatchSuccessOutcomeTransformation(GroupUtil.GROUP_REMOVE_TRANSFORM.apply(input));
    }

    @Test
    public void testFlowUpdateTransformFailure() {
        final RpcResult<List<BatchFailedGroupsOutput>> input = createBatchOutcomeWithError();
        checkBatchErrorOutcomeTransformation(GroupUtil.GROUP_UPDATE_TRANSFORM.apply(input));
    }

    @Test
    public void testFlowUpdateTransformSuccess() {
        final RpcResult<List<BatchFailedGroupsOutput>> input = createEmptyBatchOutcome();
        checkBatchSuccessOutcomeTransformation(GroupUtil.GROUP_UPDATE_TRANSFORM.apply(input));
    }

    private static <T extends BatchGroupOutputListGrouping> void checkBatchSuccessOutcomeTransformation(
            final RpcResult<T> output) {
        Assert.assertTrue(output.isSuccessful());
        Assert.assertEquals(0, output.getResult().nonnullBatchFailedGroupsOutput().size());
        Assert.assertEquals(0, output.getErrors().size());
    }

    private static RpcResult<List<BatchFailedGroupsOutput>> createEmptyBatchOutcome() {
        return RpcResultBuilder
                .success(Collections.<BatchFailedGroupsOutput>emptyList())
                .build();
    }

    private static RpcResult<List<BatchFailedGroupsOutput>> createBatchOutcomeWithError() {
        return RpcResultBuilder.<List<BatchFailedGroupsOutput>>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-flowAddFail")
                .withResult(Collections.singletonList(new BatchFailedGroupsOutputBuilder()
                        .setBatchOrder(Uint16.ZERO)
                        .setGroupId(DUMMY_GROUP_ID)
                        .build()))
                .build();
    }

    private static <T extends BatchGroupOutputListGrouping> void checkBatchErrorOutcomeTransformation(
            final RpcResult<T> output) {
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals(1, output.getResult().nonnullBatchFailedGroupsOutput().size());
        Assert.assertEquals(DUMMY_GROUP_ID,
            output.getResult().nonnullBatchFailedGroupsOutput().values().iterator().next().getGroupId());

        Assert.assertEquals(1, output.getErrors().size());
    }

    @Test
    public void testCreateComposingFunction_success_success() {
        final Function<Pair<RpcResult<AddGroupsBatchOutput>, RpcResult<SendBarrierOutput>>,
                RpcResult<AddGroupsBatchOutput>> compositeFunction = GroupUtil.createComposingFunction();

        final RpcResult<AddGroupsBatchOutput> addGroupBatchOutput = createAddGroupsBatchSuccessOutput();
        final RpcResult<SendBarrierOutput> barrierOutput = RpcResultBuilder.<SendBarrierOutput>success().build();
        final Pair<RpcResult<AddGroupsBatchOutput>, RpcResult<SendBarrierOutput>> input =
                Pair.of(addGroupBatchOutput, barrierOutput);
        final RpcResult<AddGroupsBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertTrue(composite.isSuccessful());
        Assert.assertEquals(0, composite.getErrors().size());
        Assert.assertEquals(0, composite.getResult().nonnullBatchFailedGroupsOutput().size());
    }

    @Test
    public void testCreateComposingFunction_failure_success() {
        final Function<Pair<RpcResult<AddGroupsBatchOutput>, RpcResult<SendBarrierOutput>>,
                RpcResult<AddGroupsBatchOutput>> compositeFunction = GroupUtil.createComposingFunction();

        final RpcResult<AddGroupsBatchOutput> addGroupBatchOutput = createAddGroupsBatchFailureOutcome();
        final RpcResult<SendBarrierOutput> barrierOutput = RpcResultBuilder.<SendBarrierOutput>success().build();
        final Pair<RpcResult<AddGroupsBatchOutput>, RpcResult<SendBarrierOutput>> input =
                Pair.of(addGroupBatchOutput, barrierOutput);
        final RpcResult<AddGroupsBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertFalse(composite.isSuccessful());
        Assert.assertEquals(1, composite.getErrors().size());
        Assert.assertEquals(1, composite.getResult().getBatchFailedGroupsOutput().size());
    }

    @Test
    public void testCreateComposingFunction_success_failure() {
        final Function<Pair<RpcResult<AddGroupsBatchOutput>, RpcResult<SendBarrierOutput>>,
                RpcResult<AddGroupsBatchOutput>> compositeFunction = GroupUtil.createComposingFunction();

        final RpcResult<AddGroupsBatchOutput> addGroupBatchOutput = createAddGroupsBatchSuccessOutput();
        final RpcResult<SendBarrierOutput> barrierOutput = createBarrierFailureOutcome();
        final Pair<RpcResult<AddGroupsBatchOutput>, RpcResult<SendBarrierOutput>> input =
                Pair.of(addGroupBatchOutput, barrierOutput);
        final RpcResult<AddGroupsBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertFalse(composite.isSuccessful());
        Assert.assertEquals(1, composite.getErrors().size());
        Assert.assertEquals(0, composite.getResult().nonnullBatchFailedGroupsOutput().size());
    }

    @Test
    public void testCreateComposingFunction_failure_failure() {
        final Function<Pair<RpcResult<AddGroupsBatchOutput>, RpcResult<SendBarrierOutput>>,
                RpcResult<AddGroupsBatchOutput>> compositeFunction = GroupUtil.createComposingFunction();

        final RpcResult<AddGroupsBatchOutput> addGroupBatchOutput = createAddGroupsBatchFailureOutcome();
        final RpcResult<SendBarrierOutput> barrierOutput = createBarrierFailureOutcome();
        final Pair<RpcResult<AddGroupsBatchOutput>, RpcResult<SendBarrierOutput>> input =
                Pair.of(addGroupBatchOutput, barrierOutput);
        final RpcResult<AddGroupsBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertFalse(composite.isSuccessful());
        Assert.assertEquals(2, composite.getErrors().size());
        Assert.assertEquals(1, composite.getResult().getBatchFailedGroupsOutput().size());
    }

    @Test
    public void testExtractGroupActionsSupportBitmap() {
        ActionType actionSupported = new ActionType(true,true, true, true, true, true, true, true, true, true, true,
                true, true, true, true, true, true);
        final List<Uint32> groupActionsSupportBitmap =
                GroupUtil.extractGroupActionsSupportBitmap(Lists.newArrayList(actionSupported));
        assertEquals(1, groupActionsSupportBitmap.size());
        assertEquals(GROUP_ACTION_BITMAP,  groupActionsSupportBitmap.get(0).toJava());
    }

    private static RpcResult<SendBarrierOutput> createBarrierFailureOutcome() {
        return RpcResultBuilder.<SendBarrierOutput>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-barrier-error")
                .build();
    }

    private static RpcResult<AddGroupsBatchOutput> createAddGroupsBatchSuccessOutput() {
        return RpcResultBuilder
                .success(new AddGroupsBatchOutputBuilder()
                        .setBatchFailedGroupsOutput(Collections.emptyList())
                        .build())
                .build();
    }

    private static RpcResult<AddGroupsBatchOutput> createAddGroupsBatchFailureOutcome() {
        final RpcResult<List<BatchFailedGroupsOutput>> batchOutcomeWithError = createBatchOutcomeWithError();
        return RpcResultBuilder.<AddGroupsBatchOutput>failed()
                .withResult(new AddGroupsBatchOutputBuilder()
                        .setBatchFailedGroupsOutput(batchOutcomeWithError.getResult())
                        .build())
                .withRpcErrors(batchOutcomeWithError.getErrors())
                .build();
    }
}
