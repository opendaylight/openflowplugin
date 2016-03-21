/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.batch;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.impl.util.FlatBatchUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchAddMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchRemoveMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.FlatBatchUpdateMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.meter._case.FlatBatchAddMeterContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.meter._case.FlatBatchRemoveMeterContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.meter._case.FlatBatchUpdateMeterContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureMeterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.BatchMeterOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMetersBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * transform between FlatBatch API and meter batch API
 */
public class FlatBatchMeterAdapters {

    private FlatBatchMeterAdapters() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    /**
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService#addMetersBatch(AddMetersBatchInput)}
     */
    public static AddMetersBatchInput adaptFlatBatchAddMeter(final BatchPlanStep<FlatBatchAddMeterCase> planStep, final NodeRef node) {
        final List<BatchAddMeters> batchMeters = new ArrayList<>();
        for (FlatBatchAddMeterCase choice : planStep.getTaskBag()) {
            final FlatBatchAddMeterContainer flatAddBatchMeterContainer = choice.getFlatBatchAddMeterContainer();
            final BatchAddMeters addMeters = new BatchAddMetersBuilder(flatAddBatchMeterContainer)
                    .setMeterId(flatAddBatchMeterContainer.getMeterId())
                    .build();
            batchMeters.add(addMeters);
        }

        return new AddMetersBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchAddMeters(batchMeters)
                .build();
    }

    /**
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService#removeMetersBatch(RemoveMetersBatchInput)}
     */
    public static RemoveMetersBatchInput adaptFlatBatchRemoveMeter(final BatchPlanStep<FlatBatchRemoveMeterCase> planStep, final NodeRef node) {
        final List<BatchRemoveMeters> batchMeters = new ArrayList<>();
        for (FlatBatchRemoveMeterCase choice : planStep.getTaskBag()) {
            final FlatBatchRemoveMeterContainer flatRemoveBatchMeterContainer = choice.getFlatBatchRemoveMeterContainer();
            final BatchRemoveMeters removeMeters = new BatchRemoveMetersBuilder(flatRemoveBatchMeterContainer)
                    .setMeterId(flatRemoveBatchMeterContainer.getMeterId())
                    .build();
            batchMeters.add(removeMeters);
        }

        return new RemoveMetersBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchRemoveMeters(batchMeters)
                .build();
    }

    /**
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService#updateMetersBatch(UpdateMetersBatchInput)}
     */
    public static UpdateMetersBatchInput adaptFlatBatchUpdateMeter(final BatchPlanStep<FlatBatchUpdateMeterCase> planStep, final NodeRef node) {
        final List<BatchUpdateMeters> batchMeters = new ArrayList<>();
        for (FlatBatchUpdateMeterCase choice : planStep.getTaskBag()) {
            final FlatBatchUpdateMeterContainer flatUpdateBatchMeterContainer = choice.getFlatBatchUpdateMeterContainer();
            final BatchUpdateMeters updateMeters = new BatchUpdateMetersBuilder(flatUpdateBatchMeterContainer)
                    .build();
            batchMeters.add(updateMeters);
        }

        return new UpdateMetersBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchUpdateMeters(batchMeters)
                .build();
    }

    /**
     * @param chainInput here all partial results are collected (values + errors)
     * @param stepOffset offset of current batch plan step
     * @return next chained result incorporating results of this step's batch
     */
    @VisibleForTesting
    static <T extends BatchMeterOutputListGrouping> Function<RpcResult<T>, RpcResult<ProcessFlatBatchOutput>>
    createBatchMeterChainingFunction(final RpcResult<ProcessFlatBatchOutput> chainInput,
                                     final int stepOffset) {
        return new Function<RpcResult<T>, RpcResult<ProcessFlatBatchOutput>>() {
            @Nullable
            @Override
            public RpcResult<ProcessFlatBatchOutput> apply(@Nullable final RpcResult<T> input) {
                // create rpcResult builder honoring both success/failure of current input and chained input + join errors
                final RpcResultBuilder<ProcessFlatBatchOutput> output = FlatBatchUtil.mergeRpcResults(chainInput, input);
                // convert values and add to chain values
                final ProcessFlatBatchOutputBuilder outputBuilder = new ProcessFlatBatchOutputBuilder(chainInput.getResult());
                final List<BatchFailure> batchFailures = wrapBatchMeterFailuresForFlat(input, stepOffset);
                // join values
                if (outputBuilder.getBatchFailure() == null) {
                    outputBuilder.setBatchFailure(new ArrayList<>(batchFailures.size()));
                }
                outputBuilder.getBatchFailure().addAll(batchFailures);

                return output.withResult(outputBuilder.build()).build();
            }
        };
    }

    private static <T extends BatchMeterOutputListGrouping> List<BatchFailure> wrapBatchMeterFailuresForFlat(
            final RpcResult<T> input, final int stepOffset) {
        final List<BatchFailure> batchFailures = new ArrayList<>();
        if (input.getResult().getBatchFailedMetersOutput() != null) {
            for (BatchFailedMetersOutput stepOutput : input.getResult().getBatchFailedMetersOutput()) {
                final BatchFailure batchFailure = new BatchFailureBuilder()
                        .setBatchOrder(stepOffset + stepOutput.getBatchOrder())
                        .setBatchItemIdChoice(new FlatBatchFailureMeterIdCaseBuilder()
                                .setMeterId(stepOutput.getMeterId())
                                .build())
                        .build();
                batchFailures.add(batchFailure);
            }
        }
        return batchFailures;
    }

    /**
     * shortcut for {@link #createBatchMeterChainingFunction(RpcResult, int)} with conversion {@link ListenableFuture}
     *
     * @param <T>                     exact type of batch flow output
     * @param chainInput              here all partial results are collected (values + errors)
     * @param resultUpdateMeterFuture batch group rpc-result (add/remove/update)
     * @param currentOffset           offset of current batch plan step with respect to entire chain of steps
     * @return
     */
    public static <T extends BatchMeterOutputListGrouping> ListenableFuture<RpcResult<ProcessFlatBatchOutput>>
    adaptMeterBatchFutureForChain(final RpcResult<ProcessFlatBatchOutput> chainInput,
                                  final Future<RpcResult<T>> resultUpdateMeterFuture,
                                  final int currentOffset) {
        return Futures.transform(JdkFutureAdapters.listenInPoolThread(resultUpdateMeterFuture),
                createBatchMeterChainingFunction(chainInput, currentOffset));
    }
}
