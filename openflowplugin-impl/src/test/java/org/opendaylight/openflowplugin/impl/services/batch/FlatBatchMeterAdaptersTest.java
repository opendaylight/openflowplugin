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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.meter._case.FlatBatchAddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.meter._case.FlatBatchAddMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.meter._case.FlatBatchRemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.meter._case.FlatBatchRemoveMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.meter._case.FlatBatchUpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.meter._case.FlatBatchUpdateMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureMeterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureMeterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.BatchMeterOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.OriginalBatchedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.UpdatedBatchedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link FlatBatchMeterAdapters}.
 */
public class FlatBatchMeterAdaptersTest {

    private static final NodeId NODE_ID = new NodeId("ut-node-id");
    private static final InstanceIdentifier<Node> NODE_II = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID));
    private static final NodeRef NODE_REF = new NodeRef(NODE_II);

    @Test
    public void testAdaptFlatBatchAddMeter() throws Exception {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_ADD);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(Lists.newArrayList(
                createAddMeterBatch(1L),
                createAddMeterBatch(2L)));

        final AddMetersBatchInput addMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchAddMeter(planStep, NODE_REF);

        Assert.assertTrue(addMetersBatchInput.isBarrierAfter());
        Assert.assertEquals(2, addMetersBatchInput.getBatchAddMeters().size());
        Assert.assertEquals(1L, addMetersBatchInput.getBatchAddMeters().get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(2L, addMetersBatchInput.getBatchAddMeters().get(1).getMeterId().getValue().longValue());
    }

    private FlatBatchAddMeter createAddMeterBatch(final long groupIdValue) {
        return new FlatBatchAddMeterBuilder()
                .setMeterId(new MeterId(groupIdValue))
                .build();
    }

    private FlatBatchRemoveMeter createRemoveMeterBatch(final long groupIdValue) {
        return new FlatBatchRemoveMeterBuilder()
                .setMeterId(new MeterId(groupIdValue))
                .build();
    }

    private FlatBatchUpdateMeter createUpdateMeterBatch(final long groupIdValue) {
        return new FlatBatchUpdateMeterBuilder()
                .setOriginalBatchedMeter(new OriginalBatchedMeterBuilder()
                        .setMeterId(new MeterId(groupIdValue))
                        .build())
                .setUpdatedBatchedMeter(new UpdatedBatchedMeterBuilder()
                        .setMeterId(new MeterId(groupIdValue))
                        .build())
                .build();
    }

    @Test
    public void testAdaptFlatBatchRemoveMeter() throws Exception {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_REMOVE);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(Lists.newArrayList(
                createRemoveMeterBatch(1L),
                createRemoveMeterBatch(2L)));

        final RemoveMetersBatchInput removeMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchRemoveMeter(planStep, NODE_REF);

        Assert.assertTrue(removeMetersBatchInput.isBarrierAfter());
        Assert.assertEquals(2, removeMetersBatchInput.getBatchRemoveMeters().size());
        Assert.assertEquals(1L, removeMetersBatchInput.getBatchRemoveMeters().get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(2L, removeMetersBatchInput.getBatchRemoveMeters().get(1).getMeterId().getValue().longValue());
    }

    @Test
    public void testAdaptFlatBatchUpdateMeter() throws Exception {
        final BatchPlanStep planStep = new BatchPlanStep(BatchStepType.FLOW_UPDATE);
        planStep.setBarrierAfter(true);
        planStep.getTaskBag().addAll(Lists.newArrayList(
                createUpdateMeterBatch(1L),
                createUpdateMeterBatch(2L)));

        final UpdateMetersBatchInput updateMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchUpdateMeter(planStep, NODE_REF);

        Assert.assertTrue(updateMetersBatchInput.isBarrierAfter());
        Assert.assertEquals(2, updateMetersBatchInput.getBatchUpdateMeters().size());
        Assert.assertEquals(1L, updateMetersBatchInput.getBatchUpdateMeters().get(0).getUpdatedBatchedMeter().getMeterId().getValue().longValue());
        Assert.assertEquals(2L, updateMetersBatchInput.getBatchUpdateMeters().get(1).getUpdatedBatchedMeter().getMeterId().getValue().longValue());
    }

    @Test
    public void testCreateBatchMeterChainingFunction_failures() throws Exception {
        final RpcResult<BatchMeterOutputListGrouping> input = RpcResultBuilder.<BatchMeterOutputListGrouping>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-meterError")
                .withResult(new AddMetersBatchOutputBuilder()
                        .setBatchFailedMetersOutput(Lists.newArrayList(
                                createBatchFailedMetersOutput(0, 1L),
                                createBatchFailedMetersOutput(1, 2L)
                        ))
                        .build())
                .build();

        final RpcResult<ProcessFlatBatchOutput> rpcResult = FlatBatchMeterAdapters
                .convertBatchMeterResult(3).apply(input);

        Assert.assertFalse(rpcResult.isSuccessful());
        Assert.assertEquals(1, rpcResult.getErrors().size());
        Assert.assertEquals(2, rpcResult.getResult().getBatchFailure().size());
        Assert.assertEquals(3, rpcResult.getResult().getBatchFailure().get(0).getBatchOrder().intValue());
        Assert.assertEquals(4, rpcResult.getResult().getBatchFailure().get(1).getBatchOrder().intValue());
        Assert.assertEquals(2L, ((FlatBatchFailureMeterIdCase) rpcResult.getResult().getBatchFailure().get(1).getBatchItemIdChoice()).getMeterId().getValue().longValue());
    }

    @Test
    public void testCreateBatchMeterChainingFunction_successes() throws Exception {
        final RpcResult<BatchMeterOutputListGrouping> input = RpcResultBuilder
                .<BatchMeterOutputListGrouping>success(new AddMetersBatchOutputBuilder().build())
                .build();

        final RpcResult<ProcessFlatBatchOutput> rpcResult = FlatBatchMeterAdapters
                .convertBatchMeterResult(0).apply(input);

        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(0, rpcResult.getErrors().size());
        Assert.assertEquals(0, rpcResult.getResult().getBatchFailure().size());
    }

    private BatchFailedMetersOutput createBatchFailedMetersOutput(final Integer batchOrder, final long groupIdValue) {
        return new BatchFailedMetersOutputBuilder()
                .setMeterId(new MeterId(groupIdValue))
                .setBatchOrder(batchOrder)
                .build();
    }

    private BatchFailure createChainFailure(final int batchOrder, final long groupIdValue) {
        return new BatchFailureBuilder()
                .setBatchOrder(batchOrder)
                .setBatchItemIdChoice(new FlatBatchFailureMeterIdCaseBuilder()
                        .setMeterId(new MeterId(groupIdValue))
                        .build())
                .build();
    }
}