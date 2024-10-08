/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowIdGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class FlowUtil {

    private static final RpcResultBuilder<List<BatchFailedFlowsOutput>> SUCCESSFUL_FLOW_OUTPUT_RPC_RESULT =
            RpcResultBuilder.success(List.of());

    /**
     * Attach barrier response to given {@link RpcResult}&lt;RemoveFlowsBatchOutput&gt;.
     */
    public static final Function<Pair<RpcResult<RemoveFlowsBatchOutput>,
                                 RpcResult<SendBarrierOutput>>,
                                 RpcResult<RemoveFlowsBatchOutput>>
            FLOW_REMOVE_COMPOSING_TRANSFORM = createComposingFunction();

    /**
     * Attach barrier response to given {@link RpcResult}&lt;AddFlowsBatchOutput&gt;.
     */
    public static final Function<Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<SendBarrierOutput>>,
            RpcResult<AddFlowsBatchOutput>>
            FLOW_ADD_COMPOSING_TRANSFORM = createComposingFunction();

    /**
     * Attach barrier response to given {@link RpcResult}&lt;UpdateFlowsBatchOutput&gt;.
     */
    public static final Function<Pair<RpcResult<UpdateFlowsBatchOutput>,
                                 RpcResult<SendBarrierOutput>>,
                                 RpcResult<UpdateFlowsBatchOutput>>
            FLOW_UPDATE_COMPOSING_TRANSFORM = createComposingFunction();

    /**
     * Gather errors into collection and wrap it into {@link RpcResult} and propagate all {@link RpcError}.
     */
    public static final Function<RpcResult<List<BatchFailedFlowsOutput>>,
        RpcResult<RemoveFlowsBatchOutput>> FLOW_REMOVE_TRANSFORM =
            batchFlowsCumulativeResult -> {
                final RemoveFlowsBatchOutput batchOutput = new RemoveFlowsBatchOutputBuilder()
                        .setBatchFailedFlowsOutput(index(batchFlowsCumulativeResult.getResult())).build();

                final RpcResultBuilder<RemoveFlowsBatchOutput> resultBld =
                        createCumulativeRpcResult(batchFlowsCumulativeResult, batchOutput);
                return resultBld.build();
            };

    /**
     * Gather errors into collection and wrap it into {@link RpcResult} and propagate all {@link RpcError}.
     */
    public static final Function<RpcResult<List<BatchFailedFlowsOutput>>,
        RpcResult<AddFlowsBatchOutput>> FLOW_ADD_TRANSFORM =
            batchFlowsCumulativeResult -> {
                final AddFlowsBatchOutput batchOutput = new AddFlowsBatchOutputBuilder()
                        .setBatchFailedFlowsOutput(index(batchFlowsCumulativeResult.getResult())).build();

                final RpcResultBuilder<AddFlowsBatchOutput> resultBld =
                        createCumulativeRpcResult(batchFlowsCumulativeResult, batchOutput);
                return resultBld.build();
            };

    /**
     * Gather errors into collection and wrap it into {@link RpcResult} and propagate all {@link RpcError}.
     */
    public static final Function<RpcResult<List<BatchFailedFlowsOutput>>,
        RpcResult<UpdateFlowsBatchOutput>> FLOW_UPDATE_TRANSFORM =
            batchFlowsCumulativeResult -> {
                final UpdateFlowsBatchOutput batchOutput = new UpdateFlowsBatchOutputBuilder()
                        .setBatchFailedFlowsOutput(index(batchFlowsCumulativeResult.getResult())).build();

                final RpcResultBuilder<UpdateFlowsBatchOutput> resultBld =
                        createCumulativeRpcResult(batchFlowsCumulativeResult, batchOutput);
                return resultBld.build();
            };

    private FlowUtil() {
        // Hidden on purpose
    }

    static <K extends Key<V>, V extends EntryObject<V, K>> Map<K, V> index(final List<V> list) {
        return list == null ? null : BindingMap.ordered(list);
    }

    /**
     * Wrap given list of problematic flow-ids into {@link RpcResult} of given type.
     *
     * @param batchFlowsCumulativeResult list of ids failed flows
     * @param batchOutput flow operation type
     * @return batch flow operation output of given type containing list of flow-ids and corresponding success flag
     */
    private static <T extends BatchFlowOutputListGrouping> RpcResultBuilder<T> createCumulativeRpcResult(
            final RpcResult<List<BatchFailedFlowsOutput>> batchFlowsCumulativeResult,
            final T batchOutput) {
        final RpcResultBuilder<T> resultBld;
        if (batchFlowsCumulativeResult.isSuccessful()) {
            resultBld = RpcResultBuilder.success(batchOutput);
        } else {
            resultBld = RpcResultBuilder.failed();
            resultBld.withResult(batchOutput)
                    .withRpcErrors(batchFlowsCumulativeResult.getErrors());
        }
        return resultBld;
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
    static <T extends BatchFlowOutputListGrouping>
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
     * Build flow path flow ref.
     *
     * @param nodePath path to {@link Node}
     * @param tableId  path to {@link Table} under {@link Node}
     * @param flowId   path to {@link Flow} under {@link Table}
     * @return instance identifier assembled for given node, table and flow
     */
    public static FlowRef buildFlowPath(final InstanceIdentifier<Node> nodePath,
                                        final Uint8 tableId, final FlowId flowId) {
        return new FlowRef(nodePath
            .augmentation(FlowCapableNode.class)
            .child(Table.class, new TableKey(tableId))
            .child(Flow.class, new FlowKey(new FlowId(flowId)))
            .toIdentifier());
    }

    /**
     * Factory method: creates {@link Function} which keeps info of original inputs (passed to flow-rpc) and processes
     * list of all flow-rpc results.
     *
     * @param <O>             result container type
     * @param inputBatchFlows collection of problematic flow-ids wrapped in container of given type &lt;O&gt;
     * @return static reusable function
     */
    public static <O> Function<List<RpcResult<O>>, RpcResult<List<BatchFailedFlowsOutput>>> createCumulatingFunction(
            final Collection<? extends BatchFlowIdGrouping> inputBatchFlows) {
        return new CumulatingFunction<O>(inputBatchFlows).invoke();
    }

    private static class CumulatingFunction<O> {
        private final Collection<? extends BatchFlowIdGrouping> inputBatchFlows;

        CumulatingFunction(final Collection<? extends BatchFlowIdGrouping> inputBatchFlows) {
            this.inputBatchFlows = inputBatchFlows;
        }

        public Function<List<RpcResult<O>>, RpcResult<List<BatchFailedFlowsOutput>>> invoke() {
            return (final List<RpcResult<O>> innerInput) -> {
                final int sizeOfFutures = innerInput.size();
                final int sizeOfInputBatch = inputBatchFlows.size();
                Preconditions.checkArgument(sizeOfFutures == sizeOfInputBatch,
                        "wrong amount of returned futures: {} <> {}", sizeOfFutures, sizeOfInputBatch);

                final ArrayList<BatchFailedFlowsOutput> batchFlows = new ArrayList<>(sizeOfFutures);
                final Iterator<? extends BatchFlowIdGrouping> batchFlowIterator = inputBatchFlows.iterator();

                Collection<RpcError> flowErrors = new ArrayList<>(sizeOfFutures);

                int batchOrder = 0;
                for (RpcResult<O> flowModOutput : innerInput) {
                    final FlowId flowId = batchFlowIterator.next().getFlowId();

                    if (!flowModOutput.isSuccessful()) {
                        batchFlows.add(new BatchFailedFlowsOutputBuilder()
                                .setFlowId(flowId)
                                .setBatchOrder(Uint16.valueOf(batchOrder))
                                .build());
                        flowErrors.addAll(flowModOutput.getErrors());
                    }
                    batchOrder++;
                }

                final RpcResultBuilder<List<BatchFailedFlowsOutput>> resultBuilder;
                if (!flowErrors.isEmpty()) {
                    resultBuilder = RpcResultBuilder.<List<BatchFailedFlowsOutput>>failed()
                            .withRpcErrors(flowErrors).withResult(batchFlows);
                } else {
                    resultBuilder = SUCCESSFUL_FLOW_OUTPUT_RPC_RESULT;
                }
                return resultBuilder.build();
            };
        }
    }
}
