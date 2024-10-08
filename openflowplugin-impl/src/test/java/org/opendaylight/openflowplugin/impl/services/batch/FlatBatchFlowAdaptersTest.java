/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.failure.ids.aug.FlatBatchFailureFlowIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.failure.ids.aug.FlatBatchFailureFlowIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.add.flow._case.FlatBatchAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.add.flow._case.FlatBatchAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.remove.flow._case.FlatBatchRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.remove.flow._case.FlatBatchRemoveFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.update.flow._case.FlatBatchUpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.update.flow._case.FlatBatchUpdateFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Test for {@link FlatBatchFlowAdapters}.
 */
public class FlatBatchFlowAdaptersTest {

    private static final NodeId NODE_ID = new NodeId("ut-node-id");
    private static final InstanceIdentifier<Node> NODE_II = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID));
    private static final NodeRef NODE_REF = new NodeRef(NODE_II.toIdentifier());

    @Test
    public void testAdaptFlatBatchAddFlow() {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_ADD);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(List.of(createAddFlowBatch(Uint16.ONE), createAddFlowBatch(Uint16.TWO)));

        final AddFlowsBatchInput addFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchAddFlow(planStep, NODE_REF);
        Iterator<BatchAddFlows> iterator = addFlowsBatchInput.nonnullBatchAddFlows().values().iterator();

        assertTrue(addFlowsBatchInput.getBarrierAfter());
        assertEquals(2, addFlowsBatchInput.getBatchAddFlows().size());
        assertEquals("1", iterator.next().getFlowId().getValue());
        assertEquals("2", iterator.next().getFlowId().getValue());
    }

    private static FlatBatchAddFlow createAddFlowBatch(final Uint16 flowIdValue) {
        return new FlatBatchAddFlowBuilder()
                .setFlowId(new FlowId(flowIdValue.toString()))
                .setBatchOrder(flowIdValue)
                .build();
    }

    private static FlatBatchRemoveFlow createRemoveFlowBatch(final Uint16 flowIdValue) {
        return new FlatBatchRemoveFlowBuilder()
                .setFlowId(new FlowId(flowIdValue.toString()))
                .setBatchOrder(flowIdValue)
                .build();
    }

    private static FlatBatchUpdateFlow createUpdateFlowBatch(final Uint16 flowIdValue) {
        return new FlatBatchUpdateFlowBuilder()
                .setFlowId(new FlowId(flowIdValue.toString()))
                .setBatchOrder(flowIdValue)
                .build();
    }

    @Test
    public void testAdaptFlatBatchRemoveFlow() {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_REMOVE);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(List.of(createRemoveFlowBatch(Uint16.ONE), createRemoveFlowBatch(Uint16.TWO)));

        final RemoveFlowsBatchInput removeFlowsBatchInput =
                FlatBatchFlowAdapters.adaptFlatBatchRemoveFlow(planStep, NODE_REF);
        Iterator<BatchRemoveFlows> iterator = removeFlowsBatchInput.nonnullBatchRemoveFlows().values().iterator();

        assertTrue(removeFlowsBatchInput.getBarrierAfter());
        assertEquals(2, removeFlowsBatchInput.getBatchRemoveFlows().size());
        assertEquals("1", iterator.next().getFlowId().getValue());
        assertEquals("2", iterator.next().getFlowId().getValue());
    }

    @Test
    public void testAdaptFlatBatchUpdateFlow() {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_UPDATE);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(List.of(createUpdateFlowBatch(Uint16.ONE), createUpdateFlowBatch(Uint16.TWO)));

        final UpdateFlowsBatchInput updateFlowsBatchInput =
                FlatBatchFlowAdapters.adaptFlatBatchUpdateFlow(planStep, NODE_REF);
        Iterator<BatchUpdateFlows> iterator = updateFlowsBatchInput.nonnullBatchUpdateFlows().values().iterator();

        assertTrue(updateFlowsBatchInput.getBarrierAfter());
        assertEquals(2, updateFlowsBatchInput.getBatchUpdateFlows().size());
        assertEquals("1", iterator.next().getFlowId().getValue());
        assertEquals("2", iterator.next().getFlowId().getValue());
    }

    @Test
    public void testCreateBatchFlowChainingFunction_failures() {
        final RpcResult<BatchFlowOutputListGrouping> input = RpcResultBuilder.<BatchFlowOutputListGrouping>failed()
                .withError(ErrorType.APPLICATION, "ut-flowError")
                .withResult(new AddFlowsBatchOutputBuilder()
                        .setBatchFailedFlowsOutput(BindingMap.ordered(
                                createBatchFailedFlowsOutput(Uint16.ZERO, "f1"),
                                createBatchFailedFlowsOutput(Uint16.ONE, "f2")
                        ))
                        .build())
                .build();

        final RpcResult<ProcessFlatBatchOutput> rpcResult = FlatBatchFlowAdapters
                .convertBatchFlowResult(3).apply(input);

        assertFalse(rpcResult.isSuccessful());
        assertEquals(1, rpcResult.getErrors().size());
        assertEquals(2, rpcResult.getResult().getBatchFailure().size());
        Iterator<BatchFailure> iterator = rpcResult.getResult().nonnullBatchFailure().values().iterator();
        assertEquals(3, iterator.next()
                .getBatchOrder().intValue());
        BatchFailure secondBatchFailure = iterator.next();
        assertEquals(4, secondBatchFailure.getBatchOrder().intValue());
        assertEquals("f2",
                ((FlatBatchFailureFlowIdCase) secondBatchFailure.getBatchItemIdChoice())
                        .getFlowId().getValue());
    }

    @Test
    public void testCreateBatchFlowChainingFunction_successes() {
        final RpcResult<BatchFlowOutputListGrouping> input = RpcResultBuilder
                .<BatchFlowOutputListGrouping>success(new AddFlowsBatchOutputBuilder().build())
                .build();

        final RpcResult<ProcessFlatBatchOutput> rpcResult = FlatBatchFlowAdapters
                .convertBatchFlowResult(0).apply(input);

        assertTrue(rpcResult.isSuccessful());
        assertEquals(0, rpcResult.getErrors().size());
        assertEquals(0, rpcResult.getResult().nonnullBatchFailure().size());
    }

    private static BatchFailedFlowsOutput createBatchFailedFlowsOutput(final Uint16 batchOrder,
            final String flowIdValue) {
        return new BatchFailedFlowsOutputBuilder()
                .setFlowId(new FlowId(flowIdValue))
                .setBatchOrder(batchOrder)
                .build();
    }

    private static BatchFailure createChainFailure(final Uint16 batchOrder, final String flowIdValue) {
        return new BatchFailureBuilder()
                .setBatchOrder(batchOrder)
                .setBatchItemIdChoice(new FlatBatchFailureFlowIdCaseBuilder()
                        .setFlowId(new FlowId(flowIdValue))
                        .build())
                .build();
    }
}
