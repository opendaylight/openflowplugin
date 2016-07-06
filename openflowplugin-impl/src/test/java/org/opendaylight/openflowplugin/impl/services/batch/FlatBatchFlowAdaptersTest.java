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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.flow._case.FlatBatchAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.flow._case.FlatBatchAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.flow._case.FlatBatchRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.flow._case.FlatBatchRemoveFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.flow._case.FlatBatchUpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.flow._case.FlatBatchUpdateFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureFlowIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureFlowIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutputBuilder;
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
 * Test for {@link FlatBatchFlowAdapters}.
 */
public class FlatBatchFlowAdaptersTest {

    private static final NodeId NODE_ID = new NodeId("ut-node-id");
    private static final InstanceIdentifier<Node> NODE_II = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID));
    private static final NodeRef NODE_REF = new NodeRef(NODE_II);

    @Test
    public void testAdaptFlatBatchAddFlow() throws Exception {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_ADD);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(Lists.newArrayList(
                createAddFlowBatch("1"),
                createAddFlowBatch("2")));

        final AddFlowsBatchInput addFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchAddFlow(planStep, NODE_REF);

        Assert.assertTrue(addFlowsBatchInput.isBarrierAfter());
        Assert.assertEquals(2, addFlowsBatchInput.getBatchAddFlows().size());
        Assert.assertEquals("1", addFlowsBatchInput.getBatchAddFlows().get(0).getFlowId().getValue());
        Assert.assertEquals("2", addFlowsBatchInput.getBatchAddFlows().get(1).getFlowId().getValue());
    }

    private FlatBatchAddFlow createAddFlowBatch(final String flowIdValue) {
        return new FlatBatchAddFlowBuilder()
                .setFlowId(new FlowId(flowIdValue))
                .build();
    }

    private FlatBatchRemoveFlow createRemoveFlowBatch(final String flowIdValue) {
        return new FlatBatchRemoveFlowBuilder()
                .setFlowId(new FlowId(flowIdValue))
                .build();
    }

    private FlatBatchUpdateFlow createUpdateFlowBatch(final String flowIdValue) {
        return new FlatBatchUpdateFlowBuilder()
                .setFlowId(new FlowId(flowIdValue))
                .build();
    }

    @Test
    public void testAdaptFlatBatchRemoveFlow() throws Exception {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_REMOVE);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(Lists.newArrayList(
                createRemoveFlowBatch("1"),
                createRemoveFlowBatch("2")));

        final RemoveFlowsBatchInput removeFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchRemoveFlow(planStep, NODE_REF);

        Assert.assertTrue(removeFlowsBatchInput.isBarrierAfter());
        Assert.assertEquals(2, removeFlowsBatchInput.getBatchRemoveFlows().size());
        Assert.assertEquals("1", removeFlowsBatchInput.getBatchRemoveFlows().get(0).getFlowId().getValue());
        Assert.assertEquals("2", removeFlowsBatchInput.getBatchRemoveFlows().get(1).getFlowId().getValue());
    }

    @Test
    public void testAdaptFlatBatchUpdateFlow() throws Exception {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_UPDATE);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(Lists.newArrayList(
                createUpdateFlowBatch("1"),
                createUpdateFlowBatch("2")));

        final UpdateFlowsBatchInput updateFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchUpdateFlow(planStep, NODE_REF);

        Assert.assertTrue(updateFlowsBatchInput.isBarrierAfter());
        Assert.assertEquals(2, updateFlowsBatchInput.getBatchUpdateFlows().size());
        Assert.assertEquals("1", updateFlowsBatchInput.getBatchUpdateFlows().get(0).getFlowId().getValue());
        Assert.assertEquals("2", updateFlowsBatchInput.getBatchUpdateFlows().get(1).getFlowId().getValue());
    }

    @Test
    public void testCreateBatchFlowChainingFunction_failures() throws Exception {
        final RpcResult<BatchFlowOutputListGrouping> input = RpcResultBuilder.<BatchFlowOutputListGrouping>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-flowError")
                .withResult(new AddFlowsBatchOutputBuilder()
                        .setBatchFailedFlowsOutput(Lists.newArrayList(
                                createBatchFailedFlowsOutput(0, "f1"),
                                createBatchFailedFlowsOutput(1, "f2")
                        ))
                        .build())
                .build();

        final RpcResult<ProcessFlatBatchOutput> rpcResult = FlatBatchFlowAdapters
                .convertBatchFlowResult(3).apply(input);

        Assert.assertFalse(rpcResult.isSuccessful());
        Assert.assertEquals(1, rpcResult.getErrors().size());
        Assert.assertEquals(2, rpcResult.getResult().getBatchFailure().size());
        Assert.assertEquals(3, rpcResult.getResult().getBatchFailure().get(0).getBatchOrder().intValue());
        Assert.assertEquals(4, rpcResult.getResult().getBatchFailure().get(1).getBatchOrder().intValue());
        Assert.assertEquals("f2", ((FlatBatchFailureFlowIdCase) rpcResult.getResult().getBatchFailure().get(1).getBatchItemIdChoice()).getFlowId().getValue());
    }

    @Test
    public void testCreateBatchFlowChainingFunction_successes() throws Exception {
        final RpcResult<BatchFlowOutputListGrouping> input = RpcResultBuilder
                .<BatchFlowOutputListGrouping>success(new AddFlowsBatchOutputBuilder().build())
                .build();

        final RpcResult<ProcessFlatBatchOutput> rpcResult = FlatBatchFlowAdapters
                .convertBatchFlowResult(0).apply(input);

        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(0, rpcResult.getErrors().size());
        Assert.assertEquals(0, rpcResult.getResult().getBatchFailure().size());
    }

    private BatchFailedFlowsOutput createBatchFailedFlowsOutput(final Integer batchOrder, final String flowIdValue) {
        return new BatchFailedFlowsOutputBuilder()
                .setFlowId(new FlowId(flowIdValue))
                .setBatchOrder(batchOrder)
                .build();
    }

    private BatchFailure createChainFailure(final int batchOrder, final String flowIdValue) {
        return new BatchFailureBuilder()
                .setBatchOrder(batchOrder)
                .setBatchItemIdChoice(new FlatBatchFailureFlowIdCaseBuilder()
                        .setFlowId(new FlowId(flowIdValue))
                        .build())
                .build();
    }
}