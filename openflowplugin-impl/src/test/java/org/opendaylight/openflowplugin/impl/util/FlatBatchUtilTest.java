/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.impl.services.batch.BatchPlanStep;
import org.opendaylight.openflowplugin.impl.services.batch.BatchStepType;
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
    public void testMarkBarriersWhereNeeded() throws Exception {
        final List<Batch> batches = Lists.newArrayList(
                //general part - no flush required
                createBatch(BatchStepType.GROUP_REMOVE),
                createBatch(BatchStepType.METER_REMOVE),
                createBatch(BatchStepType.FLOW_ADD),
                createBatch(BatchStepType.FLOW_REMOVE),
                createBatch(BatchStepType.FLOW_REMOVE),
                createBatch(BatchStepType.FLOW_ADD),
                createBatch(BatchStepType.FLOW_UPDATE),
                createBatch(BatchStepType.GROUP_ADD),
                createBatch(BatchStepType.GROUP_UPDATE),
                createBatch(BatchStepType.METER_ADD),
                createBatch(BatchStepType.METER_UPDATE),

                // need to flush G+/F+
                createBatch(BatchStepType.GROUP_ADD),
                createBatch(BatchStepType.FLOW_ADD),
                // need to flush G+/F*
                createBatch(BatchStepType.GROUP_ADD),
                createBatch(BatchStepType.FLOW_UPDATE),
                // need to flush F-/G-
                createBatch(BatchStepType.FLOW_REMOVE),
                createBatch(BatchStepType.GROUP_REMOVE),
                // need to flush F*/G-
                createBatch(BatchStepType.FLOW_UPDATE),
                createBatch(BatchStepType.GROUP_REMOVE),

                // need to flush M+/F+
                createBatch(BatchStepType.METER_ADD),
                createBatch(BatchStepType.FLOW_ADD),
                // need to flush M+/F*
                createBatch(BatchStepType.METER_ADD),
                createBatch(BatchStepType.FLOW_UPDATE),
                // need to flush F-/M-
                createBatch(BatchStepType.FLOW_REMOVE),
                createBatch(BatchStepType.METER_REMOVE),
                // need to flush F*/M-
                createBatch(BatchStepType.FLOW_UPDATE),
                createBatch(BatchStepType.METER_REMOVE)
        );

        final List<BatchPlanStep<? extends BatchChoice>> batchPlan = FlatBatchUtil.assembleBatchPlan(batches);
        FlatBatchUtil.markBarriersWhereNeeded(batchPlan);

        final Set<Integer> barriers = Sets.newHashSet(10, 12, 14, 16, 18, 20, 22, 24);

        Assert.assertEquals(26, batchPlan.size());
        for (int i = 0; i < batchPlan.size(); i++) {
            final BatchPlanStep planStep = batchPlan.get(i);
            final boolean barrierBefore = planStep.isBarrierAfter();
            LOG.debug("checking barrier mark @ {} {} -> {}",
                    i, planStep.getStepType(), barrierBefore);

            Assert.assertEquals(barriers.contains(i), barrierBefore);
        }
    }

    @Test
    public void testMarkBarriersWhereNeeded_single() throws Exception {
        final List<Batch> batches = Lists.newArrayList(
                //general part - no flush required
                createBatch(BatchStepType.GROUP_REMOVE)
        );

        final List<BatchPlanStep<? extends BatchChoice>> batchPlan = FlatBatchUtil.assembleBatchPlan(batches);
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
                createBatch(BatchStepType.GROUP_REMOVE),
                createBatch(BatchStepType.GROUP_REMOVE),
                createBatch(BatchStepType.GROUP_ADD),
                createBatch(BatchStepType.GROUP_UPDATE),
                createBatch(BatchStepType.GROUP_UPDATE),
                createBatch(BatchStepType.GROUP_UPDATE)
        );

        final List<BatchPlanStep<? extends BatchChoice>> batchPlanSteps = FlatBatchUtil.assembleBatchPlan(batches);
        Assert.assertEquals(4, batchPlanSteps.size());

        int i = 0;
        checkSegment(batchPlanSteps.get(i++), BatchStepType.GROUP_ADD, 1);
        checkSegment(batchPlanSteps.get(i++), BatchStepType.GROUP_REMOVE, 2);
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
        final BatchChoice batchCase;
        switch (type) {
            case FLOW_ADD:
                batchCase = new FlatBatchAddFlowCaseBuilder().build();
                break;
            case FLOW_REMOVE:
                batchCase = new FlatBatchRemoveFlowCaseBuilder().build();
                break;
            case FLOW_UPDATE:
                batchCase = new FlatBatchUpdateFlowCaseBuilder().build();
                break;
            case GROUP_ADD:
                batchCase = new FlatBatchAddGroupCaseBuilder().build();
                break;
            case GROUP_REMOVE:
                batchCase = new FlatBatchRemoveGroupCaseBuilder().build();
                break;
            case GROUP_UPDATE:
                batchCase = new FlatBatchUpdateGroupCaseBuilder().build();
                break;
            case METER_ADD:
                batchCase = new FlatBatchAddMeterCaseBuilder().build();
                break;
            case METER_REMOVE:
                batchCase = new FlatBatchRemoveMeterCaseBuilder().build();
                break;
            case METER_UPDATE:
                batchCase = new FlatBatchUpdateMeterCaseBuilder().build();
                break;
            default:
                LOG.warn("unsupported batch type: {}", type);
                throw new IllegalArgumentException("unsupported batch type: " + type);
        }

        return new BatchBuilder()
                .setBatchChoice(batchCase)
                .build();
    }

    @Test
    public void testMergeRpcResults() throws Exception {
        final RpcResult<String> rpcResultFailed = RpcResultBuilder.<String>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-rpcError").build();
        final RpcResult<String> rpcResultSuccess = RpcResultBuilder.<String>success().build();

        final RpcResult<String> rpcResult1 = FlatBatchUtil.mergeRpcResults(rpcResultFailed, rpcResultSuccess).build();
        Assert.assertEquals(1, rpcResult1.getErrors().size());
        Assert.assertFalse(rpcResult1.isSuccessful());

        final RpcResult<String> rpcResult2 = FlatBatchUtil.mergeRpcResults(rpcResultFailed, rpcResultFailed).build();
        Assert.assertEquals(2, rpcResult2.getErrors().size());
        Assert.assertFalse(rpcResult2.isSuccessful());

        final RpcResult<String> rpcResult3 = FlatBatchUtil.mergeRpcResults(rpcResultSuccess, rpcResultSuccess).build();
        Assert.assertEquals(0, rpcResult3.getErrors().size());
        Assert.assertTrue(rpcResult3.isSuccessful());
    }
}