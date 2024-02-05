/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.impl.services.batch.BatchPlanStep;
import org.opendaylight.openflowplugin.impl.services.batch.BatchStepJob;
import org.opendaylight.openflowplugin.impl.services.batch.FlatBatchFlowAdapters;
import org.opendaylight.openflowplugin.impl.services.batch.FlatBatchGroupAdapters;
import org.opendaylight.openflowplugin.impl.services.batch.FlatBatchMeterAdapters;
import org.opendaylight.openflowplugin.impl.util.FlatBatchUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link SalFlowsBatchService} - delegates work to {@link SalFlowService}.
 */
public class SalFlatBatchServiceImpl implements SalFlatBatchService {
    private static final Logger LOG = LoggerFactory.getLogger(SalFlatBatchServiceImpl.class);

    private final SalFlowsBatchService salFlowService;
    private final SalGroupsBatchService salGroupService;
    private final SalMetersBatchService salMeterService;

    public SalFlatBatchServiceImpl(final SalFlowsBatchService salFlowBatchService,
                                   final SalGroupsBatchService salGroupsBatchService,
                                   final SalMetersBatchService salMetersBatchService) {
        salFlowService = requireNonNull(salFlowBatchService, "delegate flow service must not be null");
        salGroupService = requireNonNull(salGroupsBatchService, "delegate group service must not be null");
        salMeterService = requireNonNull(salMetersBatchService, "delegate meter service must not be null");
    }

    @Override
    public ListenableFuture<RpcResult<ProcessFlatBatchOutput>> processFlatBatch(final ProcessFlatBatchInput input) {
        LOG.trace("processing flat batch @ {} : {}",
                  PathUtil.extractNodeId(input.getNode()).getValue(),
                  input.getBatch().size());
        // create plan
        final List<BatchPlanStep> batchPlan = FlatBatchUtil.assembleBatchPlan(input.nonnullBatch().values());
        // add barriers where needed
        FlatBatchUtil.markBarriersWhereNeeded(batchPlan);
        // prepare chain elements
        final List<BatchStepJob> batchChainElements =
                prepareBatchChain(batchPlan, input.getNode(), input.getExitOnFirstError());
        // execute plan with barriers and collect outputs chain correspondingly, collect results
        return executeBatchPlan(batchChainElements);
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<ProcessFlatBatchOutput>> executeBatchPlan(final List<BatchStepJob> batchJobsChain) {
        BatchStepJob batchJob;
        final List<ListenableFuture<RpcResult<ProcessFlatBatchOutput>>> firedJobs = new ArrayList<>();
        ListenableFuture<RpcResult<ProcessFlatBatchOutput>> chainSummaryResult =
                FlatBatchUtil.createEmptyRpcBatchResultFuture(true);

        for (int i = 0; i < batchJobsChain.size(); i++)  {
            batchJob = batchJobsChain.get(i);
            // wire actual job with chain
            firedJobs.add(Futures.transformAsync(chainSummaryResult, batchJob.getStepFunction(),
                    MoreExecutors.directExecutor()));
            // if barrier after actual job is needed or it is the last job -> merge fired job results with chain result
            if (batchJob.getPlanStep().isBarrierAfter() || i == batchJobsChain.size() - 1) {
                firedJobs.add(0, chainSummaryResult);
                chainSummaryResult = FlatBatchUtil.mergeJobsResultsFutures(firedJobs);
                firedJobs.clear();
            }
        }
        return chainSummaryResult;
    }

    @VisibleForTesting
    List<BatchStepJob> prepareBatchChain(final List<BatchPlanStep> batchPlan,
                                         final NodeRef node,
                                         final boolean exitOnFirstError) {
        // create batch API calls based on plan steps
        final List<BatchStepJob> chainJobs = new ArrayList<>();
        int stepOffset = 0;
        for (final BatchPlanStep planStep : batchPlan) {
            final int currentOffset = stepOffset;
            chainJobs.add(new BatchStepJob(planStep, chainInput -> {
                if (exitOnFirstError && !chainInput.isSuccessful()) {
                    LOG.debug("error on flat batch chain occurred -> skipping step {}", planStep.getStepType());
                    return FlatBatchUtil.createEmptyRpcBatchResultFuture(false);
                }
                LOG.trace("batch progressing on step type {}, previous steps result: {}", planStep.getStepType(),
                        chainInput.isSuccessful());
                return getChainOutput(node, planStep, currentOffset);
            }));
            stepOffset += planStep.getTaskBag().size();
        }

        return chainJobs;
    }

    private ListenableFuture<RpcResult<ProcessFlatBatchOutput>> getChainOutput(final NodeRef node,
            final BatchPlanStep planStep, final int currentOffset) {
        return switch (planStep.getStepType()) {
            case FLOW_ADD -> FlatBatchFlowAdapters.convertFlowBatchFutureForChain(
                salFlowService.addFlowsBatch(FlatBatchFlowAdapters.adaptFlatBatchAddFlow(planStep, node)),
                currentOffset);
            case FLOW_REMOVE -> FlatBatchFlowAdapters.convertFlowBatchFutureForChain(
                salFlowService.removeFlowsBatch(FlatBatchFlowAdapters.adaptFlatBatchRemoveFlow(planStep, node)),
                currentOffset);
            case FLOW_UPDATE -> FlatBatchFlowAdapters.convertFlowBatchFutureForChain(
                salFlowService.updateFlowsBatch(FlatBatchFlowAdapters.adaptFlatBatchUpdateFlow(planStep, node)),
                currentOffset);
            case GROUP_ADD -> FlatBatchGroupAdapters.convertGroupBatchFutureForChain(
                salGroupService.addGroupsBatch(FlatBatchGroupAdapters.adaptFlatBatchAddGroup(planStep, node)),
                currentOffset);
            case GROUP_REMOVE -> FlatBatchGroupAdapters.convertGroupBatchFutureForChain(
                salGroupService.removeGroupsBatch(FlatBatchGroupAdapters.adaptFlatBatchRemoveGroup(planStep, node)),
                currentOffset);
            case GROUP_UPDATE -> FlatBatchGroupAdapters.convertGroupBatchFutureForChain(
                salGroupService.updateGroupsBatch(FlatBatchGroupAdapters.adaptFlatBatchUpdateGroup(planStep, node)),
                currentOffset);
            case METER_ADD -> FlatBatchMeterAdapters.convertMeterBatchFutureForChain(
                salMeterService.addMetersBatch(FlatBatchMeterAdapters.adaptFlatBatchAddMeter(planStep, node)),
                currentOffset);
            case METER_REMOVE -> FlatBatchMeterAdapters.convertMeterBatchFutureForChain(
                salMeterService.removeMetersBatch(FlatBatchMeterAdapters.adaptFlatBatchRemoveMeter(planStep, node)),
                currentOffset);
            case METER_UPDATE -> FlatBatchMeterAdapters.convertMeterBatchFutureForChain(
                salMeterService.updateMetersBatch(FlatBatchMeterAdapters.adaptFlatBatchUpdateMeter(planStep, node)),
                currentOffset);
        };
    }
}
