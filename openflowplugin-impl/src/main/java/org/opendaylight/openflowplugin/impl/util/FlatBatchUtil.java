/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.opendaylight.openflowplugin.impl.services.batch.BatchPlanStep;
import org.opendaylight.openflowplugin.impl.services.batch.BatchStepType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.Batch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.BatchChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.service.batch.common.rev160322.BatchOrderGrouping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Provides flat batch util methods.
 */
public final class FlatBatchUtil {

    private FlatBatchUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static void markBarriersWhereNeeded(final List<BatchPlanStep> batchPlan) {
        final EnumSet<BatchStepType> previousTypes = EnumSet.noneOf(BatchStepType.class);

        BatchPlanStep previousPlanStep = null;
        for (BatchPlanStep planStep : batchPlan) {
            final BatchStepType type = planStep.getStepType();
            if (!previousTypes.isEmpty() && decideBarrier(previousTypes, type)) {
                previousPlanStep.setBarrierAfter(true);
                previousTypes.clear();
            }
            previousTypes.add(type);
            previousPlanStep = planStep;
        }
    }

    @VisibleForTesting
    static boolean decideBarrier(final EnumSet<BatchStepType> previousTypes, final BatchStepType type) {
        return isFlowBarrierNeeded(previousTypes, type)
                || isGroupBarrierNeeded(previousTypes, type)
                || isMeterBarrierNeeded(previousTypes, type);
    }

    private static boolean isFlowBarrierNeeded(final EnumSet<BatchStepType> previousTypes, final BatchStepType type) {
        return (type == BatchStepType.FLOW_ADD
                || type == BatchStepType.FLOW_UPDATE)
                && (previousTypes.contains(BatchStepType.GROUP_ADD)
                || previousTypes.contains(BatchStepType.METER_ADD));
    }

    private static boolean isGroupBarrierNeeded(final EnumSet<BatchStepType> previousTypes, final BatchStepType type) {
        return (type == BatchStepType.GROUP_ADD
                && (previousTypes.contains(BatchStepType.GROUP_ADD)
                || previousTypes.contains(BatchStepType.GROUP_UPDATE)))
                || (type == BatchStepType.GROUP_REMOVE
                && (previousTypes.contains(BatchStepType.FLOW_REMOVE)
                || previousTypes.contains(BatchStepType.FLOW_UPDATE)
                || previousTypes.contains(BatchStepType.GROUP_REMOVE)
                || previousTypes.contains(BatchStepType.GROUP_UPDATE)));
    }

    private static boolean isMeterBarrierNeeded(final EnumSet<BatchStepType> previousTypes, final BatchStepType type) {
        return type == BatchStepType.METER_REMOVE
                && (previousTypes.contains(BatchStepType.FLOW_REMOVE)
                || previousTypes.contains(BatchStepType.FLOW_UPDATE));
    }

    public static List<BatchPlanStep> assembleBatchPlan(List<Batch> batches) {
        final List<BatchPlanStep> plan = new ArrayList<>();

        BatchPlanStep planStep;
        for (Batch batch : batches) {
            final BatchStepType nextStepType = detectBatchStepType(batch.getBatchChoice());

            planStep = new BatchPlanStep(nextStepType);
            planStep.getTaskBag().addAll(extractBatchData(planStep.getStepType(), batch.getBatchChoice()));
            if (!planStep.isEmpty()) {
                plan.add(planStep);
            }
        }

        return plan;
    }

    private static List<? extends BatchOrderGrouping> extractBatchData(final BatchStepType batchStepType,
                                                                       final BatchChoice batchChoice) {
        final List<? extends BatchOrderGrouping> batchData;
        switch (batchStepType) {
            case FLOW_ADD:
                batchData = ((FlatBatchAddFlowCase) batchChoice).getFlatBatchAddFlow();
                break;
            case FLOW_REMOVE:
                batchData = ((FlatBatchRemoveFlowCase) batchChoice).getFlatBatchRemoveFlow();
                break;
            case FLOW_UPDATE:
                batchData = ((FlatBatchUpdateFlowCase) batchChoice).getFlatBatchUpdateFlow();
                break;
            case GROUP_ADD:
                batchData = ((FlatBatchAddGroupCase) batchChoice).getFlatBatchAddGroup();
                break;
            case GROUP_REMOVE:
                batchData = ((FlatBatchRemoveGroupCase) batchChoice).getFlatBatchRemoveGroup();
                break;
            case GROUP_UPDATE:
                batchData = ((FlatBatchUpdateGroupCase) batchChoice).getFlatBatchUpdateGroup();
                break;
            case METER_ADD:
                batchData = ((FlatBatchAddMeterCase) batchChoice).getFlatBatchAddMeter();
                break;
            case METER_REMOVE:
                batchData = ((FlatBatchRemoveMeterCase) batchChoice).getFlatBatchRemoveMeter();
                break;
            case METER_UPDATE:
                batchData = ((FlatBatchUpdateMeterCase) batchChoice).getFlatBatchUpdateMeter();
                break;
            default:
                throw new IllegalArgumentException("Unsupported batch step type obtained: " + batchStepType);
        }
        return batchData;
    }

    @VisibleForTesting
    static <T extends BatchChoice> BatchStepType detectBatchStepType(final T batchCase) {
        final BatchStepType type;
        final Class<? extends DataContainer> implementedInterface = batchCase.getImplementedInterface();

        if (FlatBatchAddFlowCase.class.equals(implementedInterface)) {
            type = BatchStepType.FLOW_ADD;
        } else if (FlatBatchRemoveFlowCase.class.equals(implementedInterface)) {
            type = BatchStepType.FLOW_REMOVE;
        } else if (FlatBatchUpdateFlowCase.class.equals(implementedInterface)) {
            type = BatchStepType.FLOW_UPDATE;
        } else if (FlatBatchAddGroupCase.class.equals(implementedInterface)) {
            type = BatchStepType.GROUP_ADD;
        } else if (FlatBatchRemoveGroupCase.class.equals(implementedInterface)) {
            type = BatchStepType.GROUP_REMOVE;
        } else if (FlatBatchUpdateGroupCase.class.equals(implementedInterface)) {
            type = BatchStepType.GROUP_UPDATE;
        } else if (FlatBatchAddMeterCase.class.equals(implementedInterface)) {
            type = BatchStepType.METER_ADD;
        } else if (FlatBatchRemoveMeterCase.class.equals(implementedInterface)) {
            type = BatchStepType.METER_REMOVE;
        } else if (FlatBatchUpdateMeterCase.class.equals(implementedInterface)) {
            type = BatchStepType.METER_UPDATE;
        } else {
            throw new IllegalArgumentException("Unsupported batch obtained: " + implementedInterface);
        }
        return type;
    }

    @VisibleForTesting
    static Function<List<RpcResult<ProcessFlatBatchOutput>>, RpcResult<ProcessFlatBatchOutput>> mergeRpcResults() {
        return jobsResults -> {
            boolean isSuccessful = true;
            List<RpcError> rpcErrors = new ArrayList<>();
            List<BatchFailure> batchFailures = new ArrayList<>();

            for (RpcResult<ProcessFlatBatchOutput> jobResult : jobsResults) {
                if (jobResult != null) {
                    isSuccessful = (isSuccessful && jobResult.isSuccessful());
                    rpcErrors.addAll(jobResult.getErrors());
                    batchFailures.addAll(jobResult.getResult().getBatchFailure());
                }
            }

            return RpcResultBuilder.<ProcessFlatBatchOutput>status(isSuccessful)
                    .withRpcErrors(rpcErrors)
                    .withResult(new ProcessFlatBatchOutputBuilder().setBatchFailure(batchFailures).build())
                    .build();
        };
    }

    /**
     * Merge list of Futures with partial results into one ListenableFuture with single result.
     * @param firedJobs list of ListenableFutures with RPC results {@link ProcessFlatBatchOutput}
     * @return ListenableFuture of RPC result with combined status and all errors + batch failures
     */
    public static ListenableFuture<RpcResult<ProcessFlatBatchOutput>> mergeJobsResultsFutures(
            final List<ListenableFuture<RpcResult<ProcessFlatBatchOutput>>> firedJobs) {
        return Futures.transform(Futures.successfulAsList(firedJobs), mergeRpcResults());
    }

    /**
     * Creates empty result future for flat batch service.
     * @param status RPC result status
     * @return ListenableFuture of RPC result with empty list of errors and batch failures
     */
    public static ListenableFuture<RpcResult<ProcessFlatBatchOutput>> createEmptyRpcBatchResultFuture(final boolean status) {
        return RpcResultBuilder.<ProcessFlatBatchOutput>status(status)
                               .withRpcErrors(new ArrayList<>())
                               .withResult(new ProcessFlatBatchOutputBuilder().setBatchFailure(new ArrayList<>()).build())
                               .buildFuture();
    }

}
