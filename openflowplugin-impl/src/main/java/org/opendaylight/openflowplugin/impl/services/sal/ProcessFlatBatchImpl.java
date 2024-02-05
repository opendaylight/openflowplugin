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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatch;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProcessFlatBatchImpl implements ProcessFlatBatch {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessFlatBatchImpl.class);

    private final AddFlowsBatch addFlowsBatch;
    private final RemoveFlowsBatch removeFlowsBatch;
    private final UpdateFlowsBatch updateFlowsBatch;
    private final AddGroupsBatch addGroupsBatch;
    private final RemoveGroupsBatch removeGroupsBatch;
    private final UpdateGroupsBatch updateGroupsBatch;
    private final AddMetersBatch addMetersBatch;
    private final RemoveMetersBatch removeMetersBatch;
    private final UpdateMetersBatch updateMetersBatch;

    public ProcessFlatBatchImpl(final AddFlowsBatch addFlowsBatch, final RemoveFlowsBatch removeFlowsBatch,
            final UpdateFlowsBatch updateFlowsBatch, final AddGroupsBatch addGroupsBatch,
            final RemoveGroupsBatch removeGroupsBatch, final UpdateGroupsBatch updateGroupsBatch,
            final AddMetersBatch addMetersBatch, final RemoveMetersBatch removeMetersBatch,
            final UpdateMetersBatch updateMetersBatch) {
        this.addFlowsBatch = requireNonNull(addFlowsBatch);
        this.removeFlowsBatch = requireNonNull(removeFlowsBatch);
        this.updateFlowsBatch = requireNonNull(updateFlowsBatch);
        this.addGroupsBatch = requireNonNull(addGroupsBatch);
        this.removeGroupsBatch = requireNonNull(removeGroupsBatch);
        this.updateGroupsBatch = requireNonNull(updateGroupsBatch);
        this.addMetersBatch = requireNonNull(addMetersBatch);
        this.removeMetersBatch = requireNonNull(removeMetersBatch);
        this.updateMetersBatch = requireNonNull(updateMetersBatch);
    }

    @Override
    public ListenableFuture<RpcResult<ProcessFlatBatchOutput>> invoke(final ProcessFlatBatchInput input) {
        final var batch = input.nonnullBatch().values();
        if (LOG.isTraceEnabled()) {
            LOG.trace("processing flat batch @ {} : {}", PathUtil.extractNodeId(input.getNode()).getValue(),
                batch.size());
        }

        // create plan
        final var batchPlan = FlatBatchUtil.assembleBatchPlan(batch);
        // add barriers where needed
        FlatBatchUtil.markBarriersWhereNeeded(batchPlan);
        // prepare chain elements
        final var batchChainElements = prepareBatchChain(batchPlan, input.getNode(), input.getExitOnFirstError());
        // execute plan with barriers and collect outputs chain correspondingly, collect results
        return executeBatchPlan(batchChainElements);
    }

    @VisibleForTesting
    static ListenableFuture<RpcResult<ProcessFlatBatchOutput>> executeBatchPlan(
            final List<BatchStepJob> batchJobsChain) {
        BatchStepJob batchJob;
        final var firedJobs = new ArrayList<ListenableFuture<RpcResult<ProcessFlatBatchOutput>>>();
        var chainSummaryResult = FlatBatchUtil.createEmptyRpcBatchResultFuture(true);

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
    List<BatchStepJob> prepareBatchChain(final List<BatchPlanStep> batchPlan, final NodeRef node,
            final boolean exitOnFirstError) {
        // create batch API calls based on plan steps
        final var chainJobs = new ArrayList<BatchStepJob>();
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
                addFlowsBatch.invoke(FlatBatchFlowAdapters.adaptFlatBatchAddFlow(planStep, node)), currentOffset);
            case FLOW_REMOVE -> FlatBatchFlowAdapters.convertFlowBatchFutureForChain(
                removeFlowsBatch.invoke(FlatBatchFlowAdapters.adaptFlatBatchRemoveFlow(planStep, node)), currentOffset);
            case FLOW_UPDATE -> FlatBatchFlowAdapters.convertFlowBatchFutureForChain(
                updateFlowsBatch.invoke(FlatBatchFlowAdapters.adaptFlatBatchUpdateFlow(planStep, node)), currentOffset);
            case GROUP_ADD -> FlatBatchGroupAdapters.convertGroupBatchFutureForChain(
                addGroupsBatch.invoke(FlatBatchGroupAdapters.adaptFlatBatchAddGroup(planStep, node)), currentOffset);
            case GROUP_REMOVE -> FlatBatchGroupAdapters.convertGroupBatchFutureForChain(
                removeGroupsBatch.invoke(FlatBatchGroupAdapters.adaptFlatBatchRemoveGroup(planStep, node)),
                currentOffset);
            case GROUP_UPDATE -> FlatBatchGroupAdapters.convertGroupBatchFutureForChain(
                updateGroupsBatch.invoke(FlatBatchGroupAdapters.adaptFlatBatchUpdateGroup(planStep, node)),
                currentOffset);
            case METER_ADD -> FlatBatchMeterAdapters.convertMeterBatchFutureForChain(
                addMetersBatch.invoke(FlatBatchMeterAdapters.adaptFlatBatchAddMeter(planStep, node)), currentOffset);
            case METER_REMOVE -> FlatBatchMeterAdapters.convertMeterBatchFutureForChain(
                removeMetersBatch.invoke(FlatBatchMeterAdapters.adaptFlatBatchRemoveMeter(planStep, node)),
                currentOffset);
            case METER_UPDATE -> FlatBatchMeterAdapters.convertMeterBatchFutureForChain(
                updateMetersBatch.invoke(FlatBatchMeterAdapters.adaptFlatBatchUpdateMeter(planStep, node)),
                currentOffset);
        };
    }
}
