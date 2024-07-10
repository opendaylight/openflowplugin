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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.failure.ids.aug.FlatBatchFailureMeterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.add.meter._case.FlatBatchAddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.remove.meter._case.FlatBatchRemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.meter.crud._case.aug.flat.batch.update.meter._case.FlatBatchUpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.BatchMeterOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMetersBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Transform between FlatBatch API and meter batch API.
 */
public final class FlatBatchMeterAdapters {

    private FlatBatchMeterAdapters() {
    }

    /**
     * Adapt flat batch add meter.
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn
     * .opendaylight.meters.service.rev160316.SalMetersBatchService#addMetersBatch(AddMetersBatchInput)}
     */
    public static AddMetersBatchInput adaptFlatBatchAddMeter(final BatchPlanStep planStep, final NodeRef node) {
        final var batchMeters = planStep.<FlatBatchAddMeter>getTaskBag().stream()
            .map(batchAddMeter -> new BatchAddMetersBuilder(batchAddMeter)
                .setMeterId(batchAddMeter.getMeterId())
                .build())
            .collect(BindingMap.toOrderedMap());

        return new AddMetersBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchAddMeters(batchMeters)
                .build();
    }

    /**
     * Adapt flat batch remove meter.
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn
     * .opendaylight.meters.service.rev160316.SalMetersBatchService#removeMetersBatch(RemoveMetersBatchInput)}
     */
    public static RemoveMetersBatchInput adaptFlatBatchRemoveMeter(final BatchPlanStep planStep, final NodeRef node) {
        final var batchMeters = planStep.<FlatBatchRemoveMeter>getTaskBag().stream()
            .map(batchRemoveMeter -> new BatchRemoveMetersBuilder(batchRemoveMeter)
                .setMeterId(batchRemoveMeter.getMeterId())
                .build())
            .collect(BindingMap.toOrderedMap());

        return new RemoveMetersBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchRemoveMeters(batchMeters)
                .build();
    }

    /**
     * Adapt flat batch update meter.
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn
     * .opendaylight.meters.service.rev160316.SalMetersBatchService#updateMetersBatch(UpdateMetersBatchInput)}
     */
    public static UpdateMetersBatchInput adaptFlatBatchUpdateMeter(final BatchPlanStep planStep, final NodeRef node) {
        final List<BatchUpdateMeters> batchMeters = new ArrayList<>();
        for (FlatBatchUpdateMeter batchUpdateMeter : planStep.<FlatBatchUpdateMeter>getTaskBag()) {
            final BatchUpdateMeters updateMeters = new BatchUpdateMetersBuilder(batchUpdateMeter)
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
     * Convert meter batch result.
     * @param stepOffset offset of current batch plan step
     * @return converted {@link ProcessFlatBatchOutput} RPC result
     */
    @VisibleForTesting
    static <T extends BatchMeterOutputListGrouping> Function<RpcResult<T>, RpcResult<ProcessFlatBatchOutput>>
        convertBatchMeterResult(final int stepOffset) {
        return input -> {
            ProcessFlatBatchOutputBuilder outputBuilder = new ProcessFlatBatchOutputBuilder()
                .setBatchFailure(wrapBatchMeterFailuresForFlat(input, stepOffset));
            return RpcResultBuilder.<ProcessFlatBatchOutput>status(input.isSuccessful())
                    .withRpcErrors(input.getErrors())
                    .withResult(outputBuilder.build())
                    .build();
        };
    }

    private static <T extends BatchMeterOutputListGrouping>
            Map<BatchFailureKey, BatchFailure> wrapBatchMeterFailuresForFlat(final RpcResult<T> input,
                final int stepOffset) {
        final BindingMap.Builder<BatchFailureKey, BatchFailure> batchFailures = BindingMap.orderedBuilder();
        if (input.getResult().getBatchFailedMetersOutput() != null) {
            for (BatchFailedMetersOutput stepOutput : input.getResult().nonnullBatchFailedMetersOutput().values()) {
                final BatchFailure batchFailure = new BatchFailureBuilder()
                        .setBatchOrder(Uint16.valueOf(stepOffset + stepOutput.getBatchOrder().toJava()))
                        .setBatchItemIdChoice(new FlatBatchFailureMeterIdCaseBuilder()
                                .setMeterId(stepOutput.getMeterId())
                                .build())
                        .build();
                batchFailures.add(batchFailure);
            }
        }
        return batchFailures.build();
    }

    /**
     * Shortcut for {@link #convertBatchMeterResult(int)} with conversion {@link ListenableFuture}.
     *
     * @param <T>                     exact type of batch flow output
     * @param resultUpdateMeterFuture batch group rpc-result (add/remove/update)
     * @param currentOffset           offset of current batch plan step with respect to entire chain of steps
     * @return ListenableFuture with converted result {@link ProcessFlatBatchOutput}
     */
    public static <T extends BatchMeterOutputListGrouping> ListenableFuture<RpcResult<ProcessFlatBatchOutput>>
        convertMeterBatchFutureForChain(final ListenableFuture<RpcResult<T>> resultUpdateMeterFuture,
                                    final int currentOffset) {
        return Futures.transform(resultUpdateMeterFuture,
                FlatBatchMeterAdapters.convertBatchMeterResult(currentOffset),
                MoreExecutors.directExecutor());
    }
}
