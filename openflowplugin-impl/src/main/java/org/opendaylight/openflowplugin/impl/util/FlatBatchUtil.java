/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import org.opendaylight.openflowplugin.impl.services.batch.BatchPlanStep;
import org.opendaylight.openflowplugin.impl.services.batch.BatchStepType;
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
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides flat batch util methods
 */
public final class FlatBatchUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FlatBatchUtil.class);

    private FlatBatchUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static void markBarriersWhereNeeded(final List<BatchPlanStep<? extends BatchChoice>> batchPlan) {
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
        final boolean needBarrier;
        switch (type) {
            case FLOW_ADD:
            case FLOW_UPDATE:
                needBarrier = previousTypes.contains(BatchStepType.GROUP_ADD)
                        || previousTypes.contains(BatchStepType.METER_ADD);
                break;
            case GROUP_REMOVE:
                needBarrier = previousTypes.contains(BatchStepType.FLOW_REMOVE)
                        || previousTypes.contains(BatchStepType.FLOW_UPDATE);
                break;
            case METER_REMOVE:
                needBarrier = previousTypes.contains(BatchStepType.FLOW_REMOVE)
                        || previousTypes.contains(BatchStepType.FLOW_UPDATE);
                break;
            default:
                needBarrier = false;
        }
        return needBarrier;
    }

    public static List<BatchPlanStep<? extends BatchChoice>> assembleBatchPlan(List<Batch> batches) {
        final List<BatchPlanStep<? extends BatchChoice>> plan = new ArrayList<>();

        BatchPlanStep<? extends BatchChoice> planStep = null;
        for (Batch batch : batches) {
            final Class<? extends BatchChoice> nextImplementedInterface =
                    (Class<? extends BatchChoice>) batch.getBatchChoice().getImplementedInterface();
            final BatchStepType nextStepType = detectBatchStepType(batch.getBatchChoice());

            if (planStep == null) {
                planStep = new BatchPlanStep(nextImplementedInterface, nextStepType);
            }

            // finalize current step if starting new item type + action type combination
            if (!nextStepType.equals(planStep.getStepType())) {
                if (!planStep.isEmpty()) {
                    plan.add(planStep);
                }
                planStep = new BatchPlanStep(nextImplementedInterface, nextStepType);
            }

            planStep.add(batch.getBatchChoice());
        }

        // collect last step
        if (planStep != null) {
            Verify.verify(!planStep.isEmpty(), "last plan step must be not empty");
            plan.add(planStep);
        }

        return plan;
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

    /**
     * join errors of left and right rpc result into output
     *
     * @param output     target result
     * @param chainInput left part (chained rpc result)
     * @param input      right part (result of current operation)
     * @param <L>        chain type
     * @param <R>        current operation type
     */
    private static <L, R> void joinErrors(final RpcResultBuilder<L> output, final RpcResult<L> chainInput, final RpcResult<R> input) {
        final Collection<RpcError> rpcErrors = new ArrayList<>(chainInput.getErrors());
        rpcErrors.addAll(input.getErrors());
        if (!rpcErrors.isEmpty()) {
            output.withRpcErrors(rpcErrors);
        }
    }

    /**
     * create rpc result honoring success/fail outcomes of arguments
     *
     * @param chainInput left part (chained rpc result)
     * @param input      right part (results of current operation)
     * @param <L>        chain type
     * @param <R>        current operation type
     * @return rpc result with combined status
     */
    private static <L, R> RpcResultBuilder<L> createNextRpcResultBuilder(final RpcResult<L> chainInput, final RpcResult<R> input) {
        return RpcResultBuilder.<L>status(input.isSuccessful() && chainInput.isSuccessful());
    }

    /**
     * Create rpc result builder with combined status and sum of all errors.
     * <br></>
     * Shortcut for {@link #createNextRpcResultBuilder(RpcResult, RpcResult)} and
     * {@link #joinErrors(RpcResultBuilder, RpcResult, RpcResult)}.
     *
     * @param chainInput left part (chained rpc result)
     * @param input      right part (results of current operation)
     * @param <L>        chain type
     * @param <R>        current operation type
     * @return rpc result with combined status and all errors
     */
    public static <L, R> RpcResultBuilder<L> mergeRpcResults(final RpcResult<L> chainInput, final RpcResult<R> input) {
        // create rpcResult builder honoring both success/failure of current input and chained input
        final RpcResultBuilder<L> output = FlatBatchUtil.createNextRpcResultBuilder(chainInput, input);
        // join errors
        FlatBatchUtil.joinErrors(output, chainInput, input);
        return output;
    }
}
