/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowIdGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class FlowUtilTest {
    public static final NodeId DUMMY_NODE_ID = new NodeId("dummyNodeId");
    public static final FlowId DUMMY_FLOW_ID = new FlowId("dummyFlowId");
    public static final FlowId DUMMY_FLOW_ID_2 = new FlowId("dummyFlowId_2");
    public static final Short DUMMY_TABLE_ID = 1;

    @Test
    public void testBuildFlowPath() throws Exception {
        final InstanceIdentifier<Node> nodePath = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(DUMMY_NODE_ID));

        final FlowRef flowRef = FlowUtil.buildFlowPath(nodePath, DUMMY_TABLE_ID, DUMMY_FLOW_ID);
        final InstanceIdentifier<?> flowRefValue = flowRef.getValue();
        Assert.assertEquals(DUMMY_NODE_ID, flowRefValue.firstKeyOf(Node.class).getId());
        Assert.assertEquals(DUMMY_TABLE_ID, flowRefValue.firstKeyOf(Table.class).getId());
        Assert.assertEquals(DUMMY_FLOW_ID, flowRefValue.firstKeyOf(Flow.class).getId());
    }

    @Test
    public void testCreateCumulatingFunction() throws Exception {
        final Function<List<RpcResult<String>>, RpcResult<List<BatchFailedFlowsOutput>>> function =
                FlowUtil.createCumulatingFunction(Lists.newArrayList(createBatchFlowIdGrouping(DUMMY_FLOW_ID),
                        createBatchFlowIdGrouping(DUMMY_FLOW_ID_2)));

        final RpcResult<List<BatchFailedFlowsOutput>> summary = function.apply(Lists.newArrayList(
                RpcResultBuilder.success("a").build(),
                RpcResultBuilder.<String>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "action-failed reason")
                        .build()));

        Assert.assertFalse(summary.isSuccessful());
        Assert.assertEquals(1, summary.getResult().size());
        Assert.assertEquals(1, summary.getErrors().size());
        Assert.assertEquals(DUMMY_FLOW_ID_2, summary.getResult().get(0).getFlowId());
        Assert.assertEquals(1, summary.getResult().get(0).getBatchOrder().intValue());
    }

    protected BatchFlowIdGrouping createBatchFlowIdGrouping(final FlowId flowId) {
        final BatchFlowIdGrouping mock = Mockito.mock(BatchFlowIdGrouping.class);
        Mockito.when(mock.getFlowId()).thenReturn(flowId);
        return mock;
    }

    @Test
    public void testFLOW_ADD_TRANSFORM__failure() throws Exception {
        final RpcResult<List<BatchFailedFlowsOutput>> input = createBatchOutcomeWithError();
        checkBatchErrorOutcomeTransformation(FlowUtil.FLOW_ADD_TRANSFORM.apply(input));
    }

    @Test
    public void testFLOW_ADD_TRANSFORM__success() throws Exception {
        final RpcResult<List<BatchFailedFlowsOutput>> input = createEmptyBatchOutcome();
        checkBatchSuccessOutcomeTransformation(FlowUtil.FLOW_ADD_TRANSFORM.apply(input));
    }

    @Test
    public void testFLOW_REMOVE_TRANSFORM__failure() throws Exception {
        final RpcResult<List<BatchFailedFlowsOutput>> input = createBatchOutcomeWithError();
        checkBatchErrorOutcomeTransformation(FlowUtil.FLOW_REMOVE_TRANSFORM.apply(input));
    }

    @Test
    public void testFLOW_REMOVE_TRANSFORM__success() throws Exception {
        final RpcResult<List<BatchFailedFlowsOutput>> input = createEmptyBatchOutcome();
        checkBatchSuccessOutcomeTransformation(FlowUtil.FLOW_REMOVE_TRANSFORM.apply(input));
    }

    @Test
    public void testFLOW_UPDATE_TRANSFORM__failure() throws Exception {
        final RpcResult<List<BatchFailedFlowsOutput>> input = createBatchOutcomeWithError();
        checkBatchErrorOutcomeTransformation(FlowUtil.FLOW_UPDATE_TRANSFORM.apply(input));
    }

    @Test
    public void testFLOW_UPDATE_TRANSFORM__success() throws Exception {
        final RpcResult<List<BatchFailedFlowsOutput>> input = createEmptyBatchOutcome();
        checkBatchSuccessOutcomeTransformation(FlowUtil.FLOW_UPDATE_TRANSFORM.apply(input));
    }

    private <T extends BatchFlowOutputListGrouping> void checkBatchSuccessOutcomeTransformation(final RpcResult<T> output) {
        Assert.assertTrue(output.isSuccessful());
        Assert.assertEquals(0, output.getResult().getBatchFailedFlowsOutput().size());
        Assert.assertEquals(0, output.getErrors().size());
    }

    private RpcResult<List<BatchFailedFlowsOutput>> createEmptyBatchOutcome() {
        return RpcResultBuilder
                .<List<BatchFailedFlowsOutput>>success(Collections.<BatchFailedFlowsOutput>emptyList())
                .build();
    }

    private RpcResult<List<BatchFailedFlowsOutput>> createBatchOutcomeWithError() {
        return RpcResultBuilder.<List<BatchFailedFlowsOutput>>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-flowAddFail")
                .withResult(Collections.singletonList(new BatchFailedFlowsOutputBuilder()
                        .setFlowId(DUMMY_FLOW_ID)
                        .build()))
                .build();
    }

    private <T extends BatchFlowOutputListGrouping> void checkBatchErrorOutcomeTransformation(final RpcResult<T> output) {
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals(1, output.getResult().getBatchFailedFlowsOutput().size());
        Assert.assertEquals(DUMMY_FLOW_ID, output.getResult().getBatchFailedFlowsOutput().get(0).getFlowId());

        Assert.assertEquals(1, output.getErrors().size());
    }

    @Test
    public void testCreateComposingFunction_success_success() throws Exception {
        final Function<Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>>, RpcResult<AddFlowsBatchOutput>> compositeFunction =
                FlowUtil.createComposingFunction();

        final RpcResult<AddFlowsBatchOutput> addFlowBatchOutput = createAddFlowsBatchSuccessOutput();
        final RpcResult<Void> barrierOutput = RpcResultBuilder.<Void>success().build();
        final Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>> input = Pair.of(addFlowBatchOutput, barrierOutput);
        final RpcResult<AddFlowsBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertTrue(composite.isSuccessful());
        Assert.assertEquals(0, composite.getErrors().size());
        Assert.assertEquals(0, composite.getResult().getBatchFailedFlowsOutput().size());
    }

    @Test
    public void testCreateComposingFunction_failure_success() throws Exception {
        final Function<Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>>, RpcResult<AddFlowsBatchOutput>> compositeFunction =
                FlowUtil.createComposingFunction();

        final RpcResult<AddFlowsBatchOutput> addFlowBatchOutput = createAddFlowsBatchFailureOutcome();
        final RpcResult<Void> barrierOutput = RpcResultBuilder.<Void>success().build();
        final Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>> input = Pair.of(addFlowBatchOutput, barrierOutput);
        final RpcResult<AddFlowsBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertFalse(composite.isSuccessful());
        Assert.assertEquals(1, composite.getErrors().size());
        Assert.assertEquals(1, composite.getResult().getBatchFailedFlowsOutput().size());
    }

    @Test
    public void testCreateComposingFunction_success_failure() throws Exception {
        final Function<Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>>, RpcResult<AddFlowsBatchOutput>> compositeFunction =
                FlowUtil.createComposingFunction();

        final RpcResult<AddFlowsBatchOutput> addFlowBatchOutput = createAddFlowsBatchSuccessOutput();
        final RpcResult<Void> barrierOutput = createBarrierFailureOutcome();
        final Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>> input = Pair.of(addFlowBatchOutput, barrierOutput);
        final RpcResult<AddFlowsBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertFalse(composite.isSuccessful());
        Assert.assertEquals(1, composite.getErrors().size());
        Assert.assertEquals(0, composite.getResult().getBatchFailedFlowsOutput().size());
    }

    @Test
    public void testCreateComposingFunction_failure_failure() throws Exception {
        final Function<Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>>, RpcResult<AddFlowsBatchOutput>> compositeFunction =
                FlowUtil.createComposingFunction();

        final RpcResult<AddFlowsBatchOutput> addFlowBatchOutput = createAddFlowsBatchFailureOutcome();
        final RpcResult<Void> barrierOutput = createBarrierFailureOutcome();
        final Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>> input = Pair.of(addFlowBatchOutput, barrierOutput);
        final RpcResult<AddFlowsBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertFalse(composite.isSuccessful());
        Assert.assertEquals(2, composite.getErrors().size());
        Assert.assertEquals(1, composite.getResult().getBatchFailedFlowsOutput().size());
    }

    private RpcResult<Void> createBarrierFailureOutcome() {
        return RpcResultBuilder.<Void>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-barrier-error")
                .build();
    }

    private RpcResult<AddFlowsBatchOutput> createAddFlowsBatchSuccessOutput() {
        return RpcResultBuilder
                .success(new AddFlowsBatchOutputBuilder()
                        .setBatchFailedFlowsOutput(Collections.<BatchFailedFlowsOutput>emptyList())
                        .build())
                .build();
    }

    private RpcResult<AddFlowsBatchOutput> createAddFlowsBatchFailureOutcome() {
        final RpcResult<List<BatchFailedFlowsOutput>> batchOutcomeWithError = createBatchOutcomeWithError();
        return RpcResultBuilder.<AddFlowsBatchOutput>failed()
                .withResult(new AddFlowsBatchOutputBuilder()
                        .setBatchFailedFlowsOutput(batchOutcomeWithError.getResult())
                        .build())
                .withRpcErrors(batchOutcomeWithError.getErrors())
                .build();
    }
}
