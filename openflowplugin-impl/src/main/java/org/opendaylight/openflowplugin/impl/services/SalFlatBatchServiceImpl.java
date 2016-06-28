/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.impl.services.batch.BatchPlanStep;
import org.opendaylight.openflowplugin.impl.services.batch.BatchStepJob;
import org.opendaylight.openflowplugin.impl.services.batch.FlatBatchFlowAdapters;
import org.opendaylight.openflowplugin.impl.services.batch.FlatBatchGroupAdapters;
import org.opendaylight.openflowplugin.impl.services.batch.FlatBatchMeterAdapters;
import org.opendaylight.openflowplugin.impl.util.FlatBatchUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * default implementation of {@link SalFlowsBatchService} - delegates work to {@link SalFlowService}
 */
public class SalFlatBatchServiceImpl implements SalFlatBatchService {
    private static final Logger LOG = LoggerFactory.getLogger(SalFlatBatchServiceImpl.class);

    private final SalFlowsBatchService salFlowService;
    private final SalGroupsBatchService salGroupService;
    private final SalMetersBatchService salMeterService;

    public SalFlatBatchServiceImpl(final SalFlowsBatchService salFlowBatchService,
                                   final SalGroupsBatchService salGroupsBatchService,
                                   final SalMetersBatchService salMetersBatchService) {
        this.salFlowService = Preconditions.checkNotNull(salFlowBatchService, "delegate flow service must not be null");
        this.salGroupService = Preconditions.checkNotNull(salGroupsBatchService, "delegate group service must not be null");
        this.salMeterService = Preconditions.checkNotNull(salMetersBatchService, "delegate meter service must not be null");
    }

    @Override
    public Future<RpcResult<ProcessFlatBatchOutput>> processFlatBatch(final ProcessFlatBatchInput input) {
        LOG.trace("processing flat batch @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatch().size());

        // create plan
        final List<BatchPlanStep> batchPlan = FlatBatchUtil.assembleBatchPlan(input.getBatch());
        // add barriers where needed
        FlatBatchUtil.markBarriersWhereNeeded(batchPlan);
        // prepare chain elements
        final List<BatchStepJob> batchChainElements = prepareBatchChain(batchPlan, input.getNode());
        // execute plan with barriers and collect outputs chain correspondingly, collect results
        return executeBatchPlanBarrier(batchChainElements, input.isExitOnFirstError());
    }

    @VisibleForTesting
    Future<RpcResult<ProcessFlatBatchOutput>> executeBatchPlan(final List<AsyncFunction<RpcResult<ProcessFlatBatchOutput>,
            RpcResult<ProcessFlatBatchOutput>>> batchChainElements) {
        ListenableFuture<RpcResult<ProcessFlatBatchOutput>> chainSummaryResult =
                RpcResultBuilder.success(new ProcessFlatBatchOutputBuilder().build()).buildFuture();

        for (AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>> chainElement : batchChainElements) {
            chainSummaryResult = Futures.transform(chainSummaryResult, chainElement);
        }

        return chainSummaryResult;
    }

    @VisibleForTesting
    Future<RpcResult<ProcessFlatBatchOutput>> executeBatchPlanBarrier(final List<BatchStepJob> batchJobsChain,
                                                                      final boolean exitOnFirstError) {
        ListenableFuture<RpcResult<ProcessFlatBatchOutput>> chainSummaryResult =
//                RpcResultBuilder.success(new ProcessFlatBatchOutputBuilder().build()).buildFuture();
            RpcResultBuilder.<ProcessFlatBatchOutput>status(true)
                .withRpcErrors(new ArrayList<>())
                .withResult(new ProcessFlatBatchOutputBuilder().setBatchFailure(new ArrayList<>()).build())
                .buildFuture();


        final List<ListenableFuture<RpcResult<ProcessFlatBatchOutput>>> firedJobs = new ArrayList<>();

        for (BatchStepJob batchJob : batchJobsChain) {
            // TODO implement usage of exitOnFirstError flag
            firedJobs.add(Futures.transform(chainSummaryResult, batchJob.getStepFunction()));
            if (batchJob.getPlanStep().isBarrierAfter()) {
                firedJobs.add(chainSummaryResult);
                chainSummaryResult = mergeJobsResultsFutures(firedJobs);
                firedJobs.clear();
            }
        }
        // TODO last one/ first one
        firedJobs.add(chainSummaryResult);
        chainSummaryResult = mergeJobsResultsFutures(firedJobs);
        firedJobs.clear();
        return chainSummaryResult;
    }

    private static ListenableFuture<RpcResult<ProcessFlatBatchOutput>> mergeJobsResultsFutures(
            final List<ListenableFuture<RpcResult<ProcessFlatBatchOutput>>> firedJobs) {

        ListenableFuture<List<RpcResult<ProcessFlatBatchOutput>>> jobs = Futures.successfulAsList(firedJobs);

        return Futures.transform(jobs, new AsyncFunction<List<RpcResult<ProcessFlatBatchOutput>>, RpcResult<ProcessFlatBatchOutput>>() {
            @Override
            public ListenableFuture<RpcResult<ProcessFlatBatchOutput>> apply(List<RpcResult<ProcessFlatBatchOutput>> firedResults) {
                List<RpcResult<ProcessFlatBatchOutput>> jobsResults = new ArrayList<>(firedResults);
                RpcResult<ProcessFlatBatchOutput> chainedResult = jobsResults.remove(jobsResults.size()-1);
                boolean isSuccessful = chainedResult.isSuccessful();
                List<RpcError> rpcErrors = new ArrayList<>(chainedResult.getErrors());
                List<BatchFailure> batchFailures = new ArrayList<>(chainedResult.getResult().getBatchFailure());

                for (RpcResult<ProcessFlatBatchOutput> jobResult : jobsResults) {
                    if (jobResult != null) {
                        isSuccessful = (isSuccessful && jobResult.isSuccessful());
                        rpcErrors.addAll(jobResult.getErrors());
                        batchFailures.addAll(jobResult.getResult().getBatchFailure());
                    }
                }

                ProcessFlatBatchOutputBuilder outputBuilder = new ProcessFlatBatchOutputBuilder().setBatchFailure(batchFailures);
                return RpcResultBuilder.<ProcessFlatBatchOutput>status(isSuccessful)
                        .withRpcErrors(rpcErrors)
                        .withResult(outputBuilder.build())
                        .buildFuture();
            }
        });
    }

    @VisibleForTesting
    List<BatchStepJob> prepareBatchChain(final List<BatchPlanStep> batchPlan, final NodeRef node) {
        // create batch API calls based on plan steps
        final List<BatchStepJob> chainJobs = new ArrayList<>();
        int stepOffset = 0;
        for (final BatchPlanStep planStep : batchPlan) {
            final int currentOffset = stepOffset;
            chainJobs.add(new BatchStepJob(planStep, new AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>>() {
                @Override
                public ListenableFuture<RpcResult<ProcessFlatBatchOutput>> apply(final RpcResult<ProcessFlatBatchOutput> chainInput) {
                    LOG.trace("batch progressing on step type {}", planStep.getStepType());
//                    LOG.trace("batch progressing previous step result: {}", chainInput.isSuccessful());

                    final ListenableFuture<RpcResult<ProcessFlatBatchOutput>> chainOutput;
                    switch (planStep.getStepType()) {
                        case FLOW_ADD:
                            final AddFlowsBatchInput addFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchAddFlow(planStep, node);
                            final Future<RpcResult<AddFlowsBatchOutput>> resultAddFlowFuture = salFlowService.addFlowsBatch(addFlowsBatchInput);
                            chainOutput = FlatBatchFlowAdapters.adaptFlowBatchFutureForChain(resultAddFlowFuture, currentOffset);
                            break;
                        case FLOW_REMOVE:
                            final RemoveFlowsBatchInput removeFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchRemoveFlow(planStep, node);
                            final Future<RpcResult<RemoveFlowsBatchOutput>> resultRemoveFlowFuture = salFlowService.removeFlowsBatch(removeFlowsBatchInput);
                            chainOutput = FlatBatchFlowAdapters.adaptFlowBatchFutureForChain(resultRemoveFlowFuture, currentOffset);
                            break;
                        case FLOW_UPDATE:
                            final UpdateFlowsBatchInput updateFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchUpdateFlow(planStep, node);
                            final Future<RpcResult<UpdateFlowsBatchOutput>> resultUpdateFlowFuture = salFlowService.updateFlowsBatch(updateFlowsBatchInput);
                            chainOutput = FlatBatchFlowAdapters.adaptFlowBatchFutureForChain(resultUpdateFlowFuture, currentOffset);
                            break;
                        case GROUP_ADD:
                            final AddGroupsBatchInput addGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchAddGroup(planStep, node);
                            final Future<RpcResult<AddGroupsBatchOutput>> resultAddGroupFuture = salGroupService.addGroupsBatch(addGroupsBatchInput);
                            chainOutput = FlatBatchGroupAdapters.adaptGroupBatchFutureForChain(resultAddGroupFuture, currentOffset);
                            break;
                        case GROUP_REMOVE:
                            final RemoveGroupsBatchInput removeGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchRemoveGroup(planStep, node);
                            final Future<RpcResult<RemoveGroupsBatchOutput>> resultRemoveGroupFuture = salGroupService.removeGroupsBatch(removeGroupsBatchInput);
                            chainOutput = FlatBatchGroupAdapters.adaptGroupBatchFutureForChain(resultRemoveGroupFuture, currentOffset);
                            break;
                        case GROUP_UPDATE:
                            final UpdateGroupsBatchInput updateGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchUpdateGroup(planStep, node);
                            final Future<RpcResult<UpdateGroupsBatchOutput>> resultUpdateGroupFuture = salGroupService.updateGroupsBatch(updateGroupsBatchInput);
                            chainOutput = FlatBatchGroupAdapters.adaptGroupBatchFutureForChain(resultUpdateGroupFuture, currentOffset);
                            break;
                        case METER_ADD:
                            final AddMetersBatchInput addMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchAddMeter(planStep, node);
                            final Future<RpcResult<AddMetersBatchOutput>> resultAddMeterFuture = salMeterService.addMetersBatch(addMetersBatchInput);
                            chainOutput = FlatBatchMeterAdapters.adaptMeterBatchFutureForChain(resultAddMeterFuture, currentOffset);
                            break;
                        case METER_REMOVE:
                            final RemoveMetersBatchInput removeMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchRemoveMeter(planStep, node);
                            final Future<RpcResult<RemoveMetersBatchOutput>> resultRemoveMeterFuture = salMeterService.removeMetersBatch(removeMetersBatchInput);
                            chainOutput = FlatBatchMeterAdapters.adaptMeterBatchFutureForChain(resultRemoveMeterFuture, currentOffset);
                            break;
                        case METER_UPDATE:
                            final UpdateMetersBatchInput updateMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchUpdateMeter(planStep, node);
                            final Future<RpcResult<UpdateMetersBatchOutput>> resultUpdateMeterFuture = salMeterService.updateMetersBatch(updateMetersBatchInput);
                            chainOutput = FlatBatchMeterAdapters.adaptMeterBatchFutureForChain(resultUpdateMeterFuture, currentOffset);
                            break;
                        default:
                            LOG.warn("Unsupported plan-step type occurred: {} -> OMITTING", planStep.getStepType());
                            chainOutput = RpcResultBuilder.success(new ProcessFlatBatchOutputBuilder().build()).buildFuture();
                    }
                    return chainOutput;
                }
            }));
            stepOffset += planStep.getTaskBag().size();
        }

        return chainJobs;
    }

}
