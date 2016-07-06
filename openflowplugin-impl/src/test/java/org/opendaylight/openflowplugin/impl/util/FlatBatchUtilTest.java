/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.impl.services.batch.BatchPlanStep;
import org.opendaylight.openflowplugin.impl.services.batch.BatchStepType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.Batch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.BatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.BatchChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.flow._case.FlatBatchAddFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.group._case.FlatBatchAddGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.meter._case.FlatBatchAddMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.flow._case.FlatBatchRemoveFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.group._case.FlatBatchRemoveGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.meter._case.FlatBatchRemoveMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.flow._case.FlatBatchUpdateFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.group._case.FlatBatchUpdateGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.meter._case.FlatBatchUpdateMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureFlowIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link FlatBatchUtil}.
 */
public class FlatBatchUtilTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlatBatchUtilTest.class);

    @Test
    public void testMarkBarriersWhereNeeded_noBarrier() throws Exception {
        final List<Batch> batches = Lists.newArrayList(
                //general part - no flush required
                createBatch(BatchStepType.GROUP_REMOVE),
                createBatch(BatchStepType.METER_REMOVE),
                createBatch(BatchStepType.FLOW_ADD),
                createBatch(BatchStepType.FLOW_REMOVE, 2),
                createBatch(BatchStepType.FLOW_ADD),
                createBatch(BatchStepType.FLOW_UPDATE),
                createBatch(BatchStepType.GROUP_ADD),
                createBatch(BatchStepType.GROUP_UPDATE),
                createBatch(BatchStepType.METER_ADD),
                createBatch(BatchStepType.METER_UPDATE)
        );

        final List<BatchPlanStep> batchPlan = FlatBatchUtil.assembleBatchPlan(batches);
        FlatBatchUtil.markBarriersWhereNeeded(batchPlan);

        Assert.assertEquals(10, batchPlan.size());
        for (int i = 0; i < batchPlan.size(); i++) {
            final BatchPlanStep planStep = batchPlan.get(i);
            final boolean barrierBefore = planStep.isBarrierAfter();
            LOG.debug("checking barrier mark @ {} {} -> {}",
                    i, planStep.getStepType(), barrierBefore);

            Assert.assertFalse(barrierBefore);
        }
    }

    @Test
    public void testMarkBarriersWhereNeeded_allBarriers() throws Exception {
        // need to flush G+/F+
        checkBarriersBetween(BatchStepType.GROUP_ADD, BatchStepType.FLOW_ADD);
        // need to flush G+/F*
        checkBarriersBetween(BatchStepType.GROUP_ADD, BatchStepType.FLOW_UPDATE);
        // need to flush F-/G-
        checkBarriersBetween(BatchStepType.FLOW_REMOVE, BatchStepType.GROUP_REMOVE);
        // need to flush F*/G-
        checkBarriersBetween(BatchStepType.FLOW_UPDATE, BatchStepType.GROUP_REMOVE);

        // need to flush G+/G+
        checkBarriersBetween(BatchStepType.GROUP_ADD, BatchStepType.GROUP_ADD);
        // need to flush G-/G-
        checkBarriersBetween(BatchStepType.GROUP_REMOVE, BatchStepType.GROUP_REMOVE);
        // need to flush G*/G+
        checkBarriersBetween(BatchStepType.GROUP_UPDATE, BatchStepType.GROUP_ADD);
        // need to flush G*/G-
        checkBarriersBetween(BatchStepType.GROUP_UPDATE, BatchStepType.GROUP_REMOVE);

        // need to flush M+/F+
        checkBarriersBetween(BatchStepType.METER_ADD, BatchStepType.FLOW_ADD);
        // need to flush M+/F*
        checkBarriersBetween(BatchStepType.METER_ADD, BatchStepType.FLOW_UPDATE);
        // need to flush F-/M-
        checkBarriersBetween(BatchStepType.FLOW_REMOVE, BatchStepType.METER_REMOVE);
        // need to flush F*/M-
        checkBarriersBetween(BatchStepType.FLOW_UPDATE, BatchStepType.METER_REMOVE);
    }

    private void checkBarriersBetween(final BatchStepType typeOfFirst, final BatchStepType typeOfSecond) {
        final List<Batch> batches = Lists.newArrayList(createBatch(typeOfFirst), createBatch(typeOfSecond));
        final List<BatchPlanStep> batchPlan = FlatBatchUtil.assembleBatchPlan(batches);
        FlatBatchUtil.markBarriersWhereNeeded(batchPlan);
        LOG.debug("checking barrier between {} / {}", typeOfFirst, typeOfSecond);
        Assert.assertEquals(2, batchPlan.size());
        Assert.assertTrue("barrier expected between " + typeOfFirst + " / " + typeOfSecond, batchPlan.get(0).isBarrierAfter());
        Assert.assertFalse(batchPlan.get(1).isBarrierAfter());
    }

    @Test
    public void testMarkBarriersWhereNeeded_single() throws Exception {
        final List<Batch> batches = Lists.newArrayList(
                //general part - no flush required
                createBatch(BatchStepType.GROUP_REMOVE)
        );

        final List<BatchPlanStep> batchPlan = FlatBatchUtil.assembleBatchPlan(batches);
        FlatBatchUtil.markBarriersWhereNeeded(batchPlan);

        Assert.assertEquals(1, batchPlan.size());
        Assert.assertFalse(batchPlan.get(0).isBarrierAfter());
    }

    @Test
    public void testDecideBarrier() throws Exception {
        Assert.assertTrue(FlatBatchUtil.decideBarrier(EnumSet.of(BatchStepType.GROUP_ADD), BatchStepType.FLOW_ADD));
        Assert.assertTrue(FlatBatchUtil.decideBarrier(EnumSet.of(BatchStepType.GROUP_ADD), BatchStepType.FLOW_UPDATE));

        Assert.assertTrue(FlatBatchUtil.decideBarrier(EnumSet.of(BatchStepType.FLOW_REMOVE), BatchStepType.GROUP_REMOVE));
        Assert.assertTrue(FlatBatchUtil.decideBarrier(EnumSet.of(BatchStepType.FLOW_UPDATE), BatchStepType.GROUP_REMOVE));

        Assert.assertTrue(FlatBatchUtil.decideBarrier(EnumSet.of(BatchStepType.METER_ADD), BatchStepType.FLOW_ADD));
        Assert.assertTrue(FlatBatchUtil.decideBarrier(EnumSet.of(BatchStepType.METER_ADD), BatchStepType.FLOW_UPDATE));

        Assert.assertTrue(FlatBatchUtil.decideBarrier(EnumSet.of(BatchStepType.FLOW_REMOVE), BatchStepType.METER_REMOVE));
        Assert.assertTrue(FlatBatchUtil.decideBarrier(EnumSet.of(BatchStepType.FLOW_UPDATE), BatchStepType.METER_REMOVE));
    }

    @Test
    public void testAssembleBatchPlan() throws Exception {
        final List<Batch> batches = Lists.newArrayList(
                createBatch(BatchStepType.GROUP_ADD),
                createBatch(BatchStepType.GROUP_REMOVE, 2),
                createBatch(BatchStepType.GROUP_REMOVE),
                createBatch(BatchStepType.GROUP_ADD),
                createBatch(BatchStepType.GROUP_UPDATE, 3)
        );

        final List<BatchPlanStep> batchPlanSteps = FlatBatchUtil.assembleBatchPlan(batches);
        Assert.assertEquals(5, batchPlanSteps.size());

        int i = 0;
        checkSegment(batchPlanSteps.get(i++), BatchStepType.GROUP_ADD, 1);
        checkSegment(batchPlanSteps.get(i++), BatchStepType.GROUP_REMOVE, 2);
        checkSegment(batchPlanSteps.get(i++), BatchStepType.GROUP_REMOVE, 1);
        checkSegment(batchPlanSteps.get(i++), BatchStepType.GROUP_ADD, 1);
        checkSegment(batchPlanSteps.get(i++), BatchStepType.GROUP_UPDATE, 3);
    }

    private void checkSegment(final BatchPlanStep planStep, final BatchStepType stepType, final int expected) {
        Assert.assertEquals(stepType, planStep.getStepType());
        Assert.assertEquals(expected, planStep.getTaskBag().size());
    }

    @Test
    public void testDetectBatchStepType() throws Exception {
        for (BatchStepType stepType : BatchStepType.values()) {
            LOG.debug("checking detection of: {}", stepType);
            final Batch batch = createBatch(stepType);
            final BatchStepType actualType = FlatBatchUtil.detectBatchStepType(batch.getBatchChoice());
            Assert.assertEquals(stepType, actualType);
        }
    }

    private Batch createBatch(BatchStepType type) {
        return createBatch(type, 1);
    }

    private Batch createBatch(BatchStepType type, final int size) {
        final BatchChoice batchCase;
        switch (type) {
            case FLOW_ADD:
                batchCase = new FlatBatchAddFlowCaseBuilder()
                        .setFlatBatchAddFlow(repeatIntoList(new FlatBatchAddFlowBuilder().build(), size))
                        .build();
                break;
            case FLOW_REMOVE:
                batchCase = new FlatBatchRemoveFlowCaseBuilder()
                        .setFlatBatchRemoveFlow(repeatIntoList(new FlatBatchRemoveFlowBuilder().build(), size))
                        .build();
                break;
            case FLOW_UPDATE:
                batchCase = new FlatBatchUpdateFlowCaseBuilder()
                        .setFlatBatchUpdateFlow(repeatIntoList(new FlatBatchUpdateFlowBuilder().build(), size))
                        .build();
                break;
            case GROUP_ADD:
                batchCase = new FlatBatchAddGroupCaseBuilder()
                        .setFlatBatchAddGroup(repeatIntoList(new FlatBatchAddGroupBuilder().build(), size))
                        .build();
                break;
            case GROUP_REMOVE:
                batchCase = new FlatBatchRemoveGroupCaseBuilder()
                        .setFlatBatchRemoveGroup(repeatIntoList(new FlatBatchRemoveGroupBuilder().build(), size))
                        .build();
                break;
            case GROUP_UPDATE:
                batchCase = new FlatBatchUpdateGroupCaseBuilder()
                        .setFlatBatchUpdateGroup(repeatIntoList(new FlatBatchUpdateGroupBuilder().build(), size))
                        .build();
                break;
            case METER_ADD:
                batchCase = new FlatBatchAddMeterCaseBuilder()
                        .setFlatBatchAddMeter(repeatIntoList(new FlatBatchAddMeterBuilder().build(), size))
                        .build();
                break;
            case METER_REMOVE:
                batchCase = new FlatBatchRemoveMeterCaseBuilder()
                        .setFlatBatchRemoveMeter(repeatIntoList(new FlatBatchRemoveMeterBuilder().build(), size))
                        .build();
                break;
            case METER_UPDATE:
                batchCase = new FlatBatchUpdateMeterCaseBuilder()
                        .setFlatBatchUpdateMeter(repeatIntoList(new FlatBatchUpdateMeterBuilder().build(), size))
                        .build();
                break;
            default:
                LOG.warn("unsupported batch type: {}", type);
                throw new IllegalArgumentException("unsupported batch type: " + type);
        }

        return new BatchBuilder()
                .setBatchChoice(batchCase)
                .build();
    }

    private <T> List<T> repeatIntoList(final T element, final int size) {
        final List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(element);
        }
        return list;
    }

    @Test
    public void testMergeJobsResultsFutures() throws Exception {
        final BatchFailure batchFailure = new BatchFailureBuilder()
                .setBatchOrder(9)
                .setBatchItemIdChoice(new FlatBatchFailureFlowIdCaseBuilder()
                        .setFlowId(new FlowId("11"))
                        .build())
                .build();

        final ProcessFlatBatchOutput output = new ProcessFlatBatchOutputBuilder().setBatchFailure(Lists.newArrayList(batchFailure)).build();

        final RpcResult<ProcessFlatBatchOutput> rpcResultFailed = RpcResultBuilder.<ProcessFlatBatchOutput>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-rpcError")
                .withResult(output).build();
        final RpcResult<ProcessFlatBatchOutput> rpcResultSuccess = RpcResultBuilder.<ProcessFlatBatchOutput>success()
                .withResult(new ProcessFlatBatchOutputBuilder().setBatchFailure(new ArrayList<>())).build();

        final RpcResult<ProcessFlatBatchOutput> rpcResult1 = FlatBatchUtil.mergeRpcResults().apply(Lists.newArrayList(rpcResultFailed, rpcResultSuccess));
        Assert.assertEquals(1, rpcResult1.getErrors().size());
        Assert.assertFalse(rpcResult1.isSuccessful());

        final RpcResult<ProcessFlatBatchOutput> rpcResult2 = FlatBatchUtil.mergeRpcResults().apply(Lists.newArrayList(rpcResultFailed, rpcResultFailed));
        Assert.assertEquals(2, rpcResult2.getErrors().size());
        Assert.assertFalse(rpcResult2.isSuccessful());

        final RpcResult<ProcessFlatBatchOutput> rpcResult3 = FlatBatchUtil.mergeRpcResults().apply(Lists.newArrayList(rpcResultSuccess, rpcResultSuccess));
        Assert.assertEquals(0, rpcResult3.getErrors().size());
        Assert.assertTrue(rpcResult3.isSuccessful());
    }
}