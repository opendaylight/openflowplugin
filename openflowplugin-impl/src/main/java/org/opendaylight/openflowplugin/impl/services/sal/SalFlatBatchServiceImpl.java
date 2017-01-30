/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
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
        LOG.trace("processing flat batch @ {} : {}", PathUtil.extractNodeId(input.getNode()).getValue(), input.getBatch().size());
        // create plan
        final List<BatchPlanStep> batchPlan = FlatBatchUtil.assembleBatchPlan(input.getBatch());
        // add barriers where needed
        FlatBatchUtil.markBarriersWhereNeeded(batchPlan);
        // prepare chain elements
        final List<BatchStepJob> batchChainElements = prepareBatchChain(batchPlan, input.getNode(), input.isExitOnFirstError());
        // execute plan with barriers and collect outputs chain correspondingly, collect results
        return executeBatchPlan(batchChainElements);
    }

    @VisibleForTesting
    Future<RpcResult<ProcessFlatBatchOutput>> executeBatchPlan(final List<BatchStepJob> batchJobsChain) {
        BatchStepJob batchJob;
        final List<ListenableFuture<RpcResult<ProcessFlatBatchOutput>>> firedJobs = new ArrayList<>();
        ListenableFuture<RpcResult<ProcessFlatBatchOutput>> chainSummaryResult =
                FlatBatchUtil.createEmptyRpcBatchResultFuture(true);

        for (int i = 0; i < batchJobsChain.size(); i++)  {
            batchJob = batchJobsChain.get(i);
            // wire actual job with chain
            firedJobs.add(Futures.transform(chainSummaryResult, batchJob.getStepFunction()));
            // if barrier after actual job is needed or it is the last job -> merge fired job results with chain result
            if ((batchJob.getPlanStep().isBarrierAfter()) || (i == batchJobsChain.size()-1)) {
                firedJobs.add(0, chainSummaryResult);
                chainSummaryResult = FlatBatchUtil.mergeJobsResultsFutures(firedJobs);
                firedJobs.clear();
            }
        }
        return chainSummaryResult;
    }

    @VisibleForTesting
    List<BatchStepJob> prepareBatchChain(final List<BatchPlanStep> batchPlan, final NodeRef node, final boolean exitOnFirstError) {
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
                                                                               final BatchPlanStep planStep,
                                                                               final int currentOffset) {
        final ListenableFuture<RpcResult<ProcessFlatBatchOutput>> chainOutput;

        switch (planStep.getStepType()) {
            case FLOW_ADD:
                final AddFlowsBatchInput addFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchAddFlow(planStep, node);
                final Future<RpcResult<AddFlowsBatchOutput>> resultAddFlowFuture = salFlowService.addFlowsBatch(addFlowsBatchInput);
                chainOutput = FlatBatchFlowAdapters.convertFlowBatchFutureForChain(resultAddFlowFuture, currentOffset);
                break;
            case FLOW_REMOVE:
                final RemoveFlowsBatchInput removeFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchRemoveFlow(planStep, node);
                final Future<RpcResult<RemoveFlowsBatchOutput>> resultRemoveFlowFuture = salFlowService.removeFlowsBatch(removeFlowsBatchInput);
                chainOutput = FlatBatchFlowAdapters.convertFlowBatchFutureForChain(resultRemoveFlowFuture, currentOffset);
                break;
            case FLOW_UPDATE:
                final UpdateFlowsBatchInput updateFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchUpdateFlow(planStep, node);
                final Future<RpcResult<UpdateFlowsBatchOutput>> resultUpdateFlowFuture = salFlowService.updateFlowsBatch(updateFlowsBatchInput);
                chainOutput = FlatBatchFlowAdapters.convertFlowBatchFutureForChain(resultUpdateFlowFuture, currentOffset);
                break;
            case GROUP_ADD:
                final AddGroupsBatchInput addGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchAddGroup(planStep, node);
                final Future<RpcResult<AddGroupsBatchOutput>> resultAddGroupFuture = salGroupService.addGroupsBatch(addGroupsBatchInput);
                chainOutput = FlatBatchGroupAdapters.convertGroupBatchFutureForChain(resultAddGroupFuture, currentOffset);
                break;
            case GROUP_REMOVE:
                final RemoveGroupsBatchInput removeGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchRemoveGroup(planStep, node);
                final Future<RpcResult<RemoveGroupsBatchOutput>> resultRemoveGroupFuture = salGroupService.removeGroupsBatch(removeGroupsBatchInput);
                chainOutput = FlatBatchGroupAdapters.convertGroupBatchFutureForChain(resultRemoveGroupFuture, currentOffset);
                break;
            case GROUP_UPDATE:
                final UpdateGroupsBatchInput updateGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchUpdateGroup(planStep, node);
                final Future<RpcResult<UpdateGroupsBatchOutput>> resultUpdateGroupFuture = salGroupService.updateGroupsBatch(updateGroupsBatchInput);
                chainOutput = FlatBatchGroupAdapters.convertGroupBatchFutureForChain(resultUpdateGroupFuture, currentOffset);
                break;
            case METER_ADD:
                final AddMetersBatchInput addMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchAddMeter(planStep, node);
                final Future<RpcResult<AddMetersBatchOutput>> resultAddMeterFuture = salMeterService.addMetersBatch(addMetersBatchInput);
                chainOutput = FlatBatchMeterAdapters.convertMeterBatchFutureForChain(resultAddMeterFuture, currentOffset);
                break;
            case METER_REMOVE:
                final RemoveMetersBatchInput removeMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchRemoveMeter(planStep, node);
                final Future<RpcResult<RemoveMetersBatchOutput>> resultRemoveMeterFuture = salMeterService.removeMetersBatch(removeMetersBatchInput);
                chainOutput = FlatBatchMeterAdapters.convertMeterBatchFutureForChain(resultRemoveMeterFuture, currentOffset);
                break;
            case METER_UPDATE:
                final UpdateMetersBatchInput updateMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchUpdateMeter(planStep, node);
                final Future<RpcResult<UpdateMetersBatchOutput>> resultUpdateMeterFuture = salMeterService.updateMetersBatch(updateMetersBatchInput);
                chainOutput = FlatBatchMeterAdapters.convertMeterBatchFutureForChain(resultUpdateMeterFuture, currentOffset);
                break;
            default:
                LOG.warn("Unsupported plan-step type occurred: {} -> OMITTING", planStep.getStepType());
                chainOutput = FlatBatchUtil.createEmptyRpcBatchResultFuture(true);
        }

        return chainOutput;
    }
}
