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
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.BatchMeterOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutputBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Provides meter util methods.
 */
public final class MeterUtil {

    private static final RpcResultBuilder<List<BatchFailedMetersOutput>> SUCCESSFUL_METER_OUTPUT_RPC_RESULT =
            RpcResultBuilder.success(Collections.emptyList());

    public static final Function<RpcResult<List<BatchFailedMetersOutput>>, RpcResult<AddMetersBatchOutput>>
        METER_ADD_TRANSFORM = batchMetersCumulatedResult -> {
            final AddMetersBatchOutput batchOutput = new AddMetersBatchOutputBuilder()
                    .setBatchFailedMetersOutput(FlowUtil.index(batchMetersCumulatedResult.getResult())).build();

            final RpcResultBuilder<AddMetersBatchOutput> resultBld =
                    createCumulativeRpcResult(batchMetersCumulatedResult, batchOutput);
            return resultBld.build();
        };
    public static final Function<Pair<RpcResult<AddMetersBatchOutput>,
                                      RpcResult<SendBarrierOutput>>,
                                      RpcResult<AddMetersBatchOutput>>
        METER_ADD_COMPOSING_TRANSFORM = createComposingFunction();

    public static final Function<RpcResult<List<BatchFailedMetersOutput>>, RpcResult<RemoveMetersBatchOutput>>
        METER_REMOVE_TRANSFORM =
            batchMetersCumulatedResult -> {
                final RemoveMetersBatchOutput batchOutput = new RemoveMetersBatchOutputBuilder()
                        .setBatchFailedMetersOutput(FlowUtil.index(batchMetersCumulatedResult.getResult())).build();

                final RpcResultBuilder<RemoveMetersBatchOutput> resultBld =
                        createCumulativeRpcResult(batchMetersCumulatedResult, batchOutput);
                return resultBld.build();
            };
    public static final Function<Pair<RpcResult<RemoveMetersBatchOutput>,
                                      RpcResult<SendBarrierOutput>>,
                                      RpcResult<RemoveMetersBatchOutput>>
        METER_REMOVE_COMPOSING_TRANSFORM = createComposingFunction();

    public static final Function<RpcResult<List<BatchFailedMetersOutput>>, RpcResult<UpdateMetersBatchOutput>>
        METER_UPDATE_TRANSFORM =
            batchMetersCumulatedResult -> {
                final UpdateMetersBatchOutput batchOutput = new UpdateMetersBatchOutputBuilder()
                        .setBatchFailedMetersOutput(FlowUtil.index(batchMetersCumulatedResult.getResult())).build();

                final RpcResultBuilder<UpdateMetersBatchOutput> resultBld =
                        createCumulativeRpcResult(batchMetersCumulatedResult, batchOutput);
                return resultBld.build();
            };
    public static final Function<Pair<RpcResult<UpdateMetersBatchOutput>,
                                      RpcResult<SendBarrierOutput>>,
                                      RpcResult<UpdateMetersBatchOutput>>
        METER_UPDATE_COMPOSING_TRANSFORM = createComposingFunction();

    private MeterUtil() {
        // Hidden on purpose
    }

    /**
     * Create meter path.
     * @param nodePath node path
     * @param meterId meter Id
     * @return instance identifier assembled for given node and meter
     */
    public static MeterRef buildMeterPath(final DataObjectIdentifier<Node> nodePath, final MeterId meterId) {
        return new MeterRef(nodePath.toBuilder()
            .augmentation(FlowCapableNode.class)
            .child(Meter.class, new MeterKey(meterId))
            .build());
    }

    public static <O> Function<List<RpcResult<O>>, RpcResult<List<BatchFailedMetersOutput>>> createCumulativeFunction(
            final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter>
                    inputBatchMeters) {
        return createCumulativeFunction(inputBatchMeters, Iterables.size(inputBatchMeters));
    }

    public static <O> Function<List<RpcResult<O>>, RpcResult<List<BatchFailedMetersOutput>>> createCumulativeFunction(
            final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter>
                    inputBatchMeters,
            final int sizeOfInputBatch) {
        return new CumulativeFunction<O>(inputBatchMeters, sizeOfInputBatch).invoke();
    }

    /**
     * Factory method: create {@link Function} which attaches barrier response to given {@link RpcResult}&lt;T&gt;
     * and changes success flag if needed.
     * <br>
     * Original rpcResult is the {@link Pair#getLeft()} and barrier result is the {@link Pair#getRight()}.
     *
     * @param <T> type of rpcResult value
     * @return reusable static function
     */
    @VisibleForTesting
    static <T extends BatchMeterOutputListGrouping>
        Function<Pair<RpcResult<T>, RpcResult<SendBarrierOutput>>, RpcResult<T>> createComposingFunction() {
        return input -> {
            final RpcResultBuilder<T> resultBld;
            if (input.getLeft().isSuccessful() && input.getRight().isSuccessful()) {
                resultBld = RpcResultBuilder.success();
            } else {
                resultBld = RpcResultBuilder.failed();
            }

            final ArrayList<RpcError> rpcErrors = new ArrayList<>(input.getLeft().getErrors());
            rpcErrors.addAll(input.getRight().getErrors());
            resultBld.withRpcErrors(rpcErrors);

            resultBld.withResult(input.getLeft().getResult());

            return resultBld.build();
        };
    }

    /**
     * Wrap given list of problematic group-ids into {@link RpcResult} of given type.
     *
     * @param batchMetersCumulativeResult list of ids failed groups
     * @param batchOutput group operation type
     * @return batch group operation output of given type containing list of group-ids and corresponding success flag
     */
    private static <T extends BatchMeterOutputListGrouping>
        RpcResultBuilder<T> createCumulativeRpcResult(
            final RpcResult<List<BatchFailedMetersOutput>> batchMetersCumulativeResult,
            final T batchOutput) {
        final RpcResultBuilder<T> resultBld;
        if (batchMetersCumulativeResult.isSuccessful()) {
            resultBld = RpcResultBuilder.success(batchOutput);
        } else {
            resultBld = RpcResultBuilder.failed();
            resultBld.withResult(batchOutput)
                    .withRpcErrors(batchMetersCumulativeResult.getErrors());
        }
        return resultBld;
    }

    private static class CumulativeFunction<O> {
        private final Iterable<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter>
                inputBatchMeters;
        private final int sizeOfInputBatch;

        CumulativeFunction(
                final Iterable<? extends org.opendaylight.yang.gen.v1.urn
                        .opendaylight.meter.types.rev130918.Meter> inputBatchMeters, final int sizeOfInputBatch) {
            this.inputBatchMeters = inputBatchMeters;
            this.sizeOfInputBatch = sizeOfInputBatch;
        }

        public Function<List<RpcResult<O>>, RpcResult<List<BatchFailedMetersOutput>>> invoke() {
            return innerInput -> {
                final int sizeOfFutures = innerInput.size();
                Preconditions.checkArgument(sizeOfFutures == sizeOfInputBatch,
                        "wrong amount of returned futures: {} <> {}", sizeOfFutures, sizeOfInputBatch);

                final List<BatchFailedMetersOutput> batchMeters = new ArrayList<>();
                final Iterator<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter>
                        batchMeterIterator = inputBatchMeters.iterator();

                Collection<RpcError> meterErrors = new ArrayList<>(sizeOfFutures);

                int batchOrder = 0;
                for (RpcResult<O> meterModOutput : innerInput) {
                    final MeterId meterId = batchMeterIterator.next().getMeterId();

                    if (!meterModOutput.isSuccessful()) {
                        batchMeters.add(new BatchFailedMetersOutputBuilder()
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .setMeterId(meterId)
                                .build());
                        meterErrors.addAll(meterModOutput.getErrors());
                    }
                    batchOrder++;
                }

                final RpcResultBuilder<List<BatchFailedMetersOutput>> resultBuilder;
                if (!meterErrors.isEmpty()) {
                    resultBuilder = RpcResultBuilder.<List<BatchFailedMetersOutput>>failed()
                            .withRpcErrors(meterErrors).withResult(batchMeters);
                } else {
                    resultBuilder = SUCCESSFUL_METER_OUTPUT_RPC_RESULT;
                }
                return resultBuilder.build();
            };
        }
    }
}
