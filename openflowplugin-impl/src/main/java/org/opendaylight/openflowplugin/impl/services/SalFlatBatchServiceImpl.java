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
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.impl.services.batch.BatchPlanStep;
import org.opendaylight.openflowplugin.impl.services.batch.FlatBatchFlowAdapters;
import org.opendaylight.openflowplugin.impl.services.batch.FlatBatchGroupAdapters;
import org.opendaylight.openflowplugin.impl.services.batch.FlatBatchMeterAdapters;
import org.opendaylight.openflowplugin.impl.util.FlatBatchUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
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
        final List<AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>>> batchChainElements =
                prepareBatchChain(batchPlan, input.getNode(), input.isExitOnFirstError());
        // execute plan with barriers and collect outputs chain correspondingly, collect results
        return executeBatchPlan(batchChainElements);
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
    List<AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>>> prepareBatchChain(
            final List<BatchPlanStep> batchPlan,
            final NodeRef node,
            final boolean exitOnFirstError) {

        // create batch API calls based on plan steps
        final List<AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>>> chainJobs = new ArrayList<>();
        int stepOffset = 0;
        for (final BatchPlanStep planStep : batchPlan) {
            final int currentOffset = stepOffset;
            chainJobs.add(new AsyncFunction<RpcResult<ProcessFlatBatchOutput>, RpcResult<ProcessFlatBatchOutput>>() {
                @Override
                public ListenableFuture<RpcResult<ProcessFlatBatchOutput>> apply(final RpcResult<ProcessFlatBatchOutput> chainInput) throws Exception {
                    if (exitOnFirstError && !chainInput.isSuccessful()) {
                        LOG.debug("error on flat batch chain occurred -> skipping step {}", planStep.getStepType());
                        return Futures.immediateFuture(chainInput);
                    }

                    LOG.trace("batch progressing on step type {}", planStep.getStepType());
                    LOG.trace("batch progressing previous step result: {}", chainInput.isSuccessful());

                    final ListenableFuture<RpcResult<ProcessFlatBatchOutput>> chainOutput;
                    switch (planStep.getStepType()) {
                        case FLOW_ADD:
                            final AddFlowsBatchInput addFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchAddFlow(planStep, node);
                            final Future<RpcResult<AddFlowsBatchOutput>> resultAddFlowFuture = salFlowService.addFlowsBatch(addFlowsBatchInput);
                            chainOutput = FlatBatchFlowAdapters.adaptFlowBatchFutureForChain(chainInput, resultAddFlowFuture, currentOffset);
                            break;
                        case FLOW_REMOVE:
                            final RemoveFlowsBatchInput removeFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchRemoveFlow(planStep, node);
                            final Future<RpcResult<RemoveFlowsBatchOutput>> resultRemoveFlowFuture = salFlowService.removeFlowsBatch(removeFlowsBatchInput);
                            chainOutput = FlatBatchFlowAdapters.adaptFlowBatchFutureForChain(chainInput, resultRemoveFlowFuture, currentOffset);
                            break;
                        case FLOW_UPDATE:
                            final UpdateFlowsBatchInput updateFlowsBatchInput = FlatBatchFlowAdapters.adaptFlatBatchUpdateFlow(planStep, node);
                            final Future<RpcResult<UpdateFlowsBatchOutput>> resultUpdateFlowFuture = salFlowService.updateFlowsBatch(updateFlowsBatchInput);
                            chainOutput = FlatBatchFlowAdapters.adaptFlowBatchFutureForChain(chainInput, resultUpdateFlowFuture, currentOffset);
                            break;
                        case GROUP_ADD:
                            final AddGroupsBatchInput addGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchAddGroup(planStep, node);
                            final Future<RpcResult<AddGroupsBatchOutput>> resultAddGroupFuture = salGroupService.addGroupsBatch(addGroupsBatchInput);
                            chainOutput = FlatBatchGroupAdapters.adaptGroupBatchFutureForChain(chainInput, resultAddGroupFuture, currentOffset);
                            break;
                        case GROUP_REMOVE:
                            final RemoveGroupsBatchInput removeGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchRemoveGroup(planStep, node);
                            final Future<RpcResult<RemoveGroupsBatchOutput>> resultRemoveGroupFuture = salGroupService.removeGroupsBatch(removeGroupsBatchInput);
                            chainOutput = FlatBatchGroupAdapters.adaptGroupBatchFutureForChain(chainInput, resultRemoveGroupFuture, currentOffset);
                            break;
                        case GROUP_UPDATE:
                            final UpdateGroupsBatchInput updateGroupsBatchInput = FlatBatchGroupAdapters.adaptFlatBatchUpdateGroup(planStep, node);
                            final Future<RpcResult<UpdateGroupsBatchOutput>> resultUpdateGroupFuture = salGroupService.updateGroupsBatch(updateGroupsBatchInput);
                            chainOutput = FlatBatchGroupAdapters.adaptGroupBatchFutureForChain(chainInput, resultUpdateGroupFuture, currentOffset);
                            break;
                        case METER_ADD:
                            final AddMetersBatchInput addMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchAddMeter(planStep, node);
                            final Future<RpcResult<AddMetersBatchOutput>> resultAddMeterFuture = salMeterService.addMetersBatch(addMetersBatchInput);
                            chainOutput = FlatBatchMeterAdapters.adaptMeterBatchFutureForChain(chainInput, resultAddMeterFuture, currentOffset);
                            break;
                        case METER_REMOVE:
                            final RemoveMetersBatchInput removeMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchRemoveMeter(planStep, node);
                            final Future<RpcResult<RemoveMetersBatchOutput>> resultRemoveMeterFuture = salMeterService.removeMetersBatch(removeMetersBatchInput);
                            chainOutput = FlatBatchMeterAdapters.adaptMeterBatchFutureForChain(chainInput, resultRemoveMeterFuture, currentOffset);
                            break;
                        case METER_UPDATE:
                            final UpdateMetersBatchInput updateMetersBatchInput = FlatBatchMeterAdapters.adaptFlatBatchUpdateMeter(planStep, node);
                            final Future<RpcResult<UpdateMetersBatchOutput>> resultUpdateMeterFuture = salMeterService.updateMetersBatch(updateMetersBatchInput);
                            chainOutput = FlatBatchMeterAdapters.adaptMeterBatchFutureForChain(chainInput, resultUpdateMeterFuture, currentOffset);
                            break;
                        default:
                            LOG.warn("Unsupported plan-step type occurred: {} -> OMITTING", planStep.getStepType());
                            chainOutput = Futures.immediateFuture(chainInput);
                    }
                    return chainOutput;
                }
            });
            stepOffset += planStep.getTaskBag().size();
        }

        return chainJobs;
    }

}
