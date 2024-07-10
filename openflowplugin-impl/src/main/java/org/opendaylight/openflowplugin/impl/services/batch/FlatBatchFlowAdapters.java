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
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.failure.ids.aug.FlatBatchFailureFlowIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.add.flow._case.FlatBatchAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.remove.flow._case.FlatBatchRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.flat.batch.flow.crud._case.aug.flat.batch.update.flow._case.FlatBatchUpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Transform between FlatBatch API and flow batch API.
 */
public final class FlatBatchFlowAdapters {

    private FlatBatchFlowAdapters() {
    }

    /**
     * Adapt flat batch add flow.
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn
     * .opendaylight.flows.service.rev160314.SalFlowsBatchService#addFlowsBatch(AddFlowsBatchInput)}
     */
    public static AddFlowsBatchInput adaptFlatBatchAddFlow(final BatchPlanStep planStep, final NodeRef node) {
        final var batchFlows = planStep.<FlatBatchAddFlow>getTaskBag().stream()
            .map(batchAddFlows -> new BatchAddFlowsBuilder((Flow) batchAddFlows)
                .setFlowId(batchAddFlows.getFlowId())
                .build())
            .collect(BindingMap.toOrderedMap());

        return new AddFlowsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchAddFlows(batchFlows)
                .build();
    }

    /**
     * Adapt flat batch remove flow.
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn
     * .opendaylight.flows.service.rev160314.SalFlowsBatchService#removeFlowsBatch(RemoveFlowsBatchInput)}
     */
    public static RemoveFlowsBatchInput adaptFlatBatchRemoveFlow(final BatchPlanStep planStep, final NodeRef node) {
        final var batchFlows = planStep.<FlatBatchRemoveFlow>getTaskBag().stream()
            .map(batchRemoveFlow -> new BatchRemoveFlowsBuilder((Flow) batchRemoveFlow)
                .setFlowId(batchRemoveFlow.getFlowId())
                .build())
            .collect(BindingMap.toOrderedMap());

        return new RemoveFlowsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchRemoveFlows(batchFlows)
                .build();
    }

    /**
     * Adapt flat batch update flow.
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn
     * .opendaylight.flows.service.rev160314.SalFlowsBatchService#updateFlowsBatch(UpdateFlowsBatchInput)}
     */
    public static UpdateFlowsBatchInput adaptFlatBatchUpdateFlow(final BatchPlanStep planStep, final NodeRef node) {
        final var batchFlows = planStep.<FlatBatchUpdateFlow>getTaskBag().stream()
            .map(batchUpdateFlow -> new BatchUpdateFlowsBuilder(batchUpdateFlow).build())
            .collect(BindingMap.toOrderedMap());

        return new UpdateFlowsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchUpdateFlows(batchFlows)
                .build();
    }

    /**
     * Convert batch result.
     * @param stepOffset offset of current batch plan step
     * @return converted {@link ProcessFlatBatchOutput} RPC result
     */
    @VisibleForTesting
    static <T extends BatchFlowOutputListGrouping> Function<RpcResult<T>, RpcResult<ProcessFlatBatchOutput>>
        convertBatchFlowResult(final int stepOffset) {
        return input -> RpcResultBuilder.<ProcessFlatBatchOutput>status(input.isSuccessful())
            .withRpcErrors(input.getErrors())
            .withResult(new ProcessFlatBatchOutputBuilder()
                .setBatchFailure(wrapBatchFlowFailuresForFlat(input, stepOffset))
                .build())
            .build();
    }

    private static <T extends BatchFlowOutputListGrouping>
            Map<BatchFailureKey, BatchFailure> wrapBatchFlowFailuresForFlat(final RpcResult<T> input,
                final int stepOffset) {
        return input.getResult().nonnullBatchFailedFlowsOutput().values().stream()
            .map(stepOutput -> new BatchFailureBuilder()
                .setBatchOrder(Uint16.valueOf(stepOffset + stepOutput.getBatchOrder().toJava()))
                .setBatchItemIdChoice(new FlatBatchFailureFlowIdCaseBuilder().setFlowId(stepOutput.getFlowId()).build())
                .build())
            .collect(BindingMap.toOrderedMap());
    }

    /**
     * Shortcut for {@link #convertBatchFlowResult(int)} with conversion {@link ListenableFuture}.
     *
     * @param <T>                    exact type of batch flow output
     * @param resultUpdateFlowFuture batch flow rpc-result (add/remove/update)
     * @param currentOffset          offset of current batch plan step with respect to entire chain of steps
     * @return ListenableFuture with converted result {@link ProcessFlatBatchOutput}
     */
    public static <T extends BatchFlowOutputListGrouping> ListenableFuture<RpcResult<ProcessFlatBatchOutput>>
        convertFlowBatchFutureForChain(final ListenableFuture<RpcResult<T>> resultUpdateFlowFuture,
                                   final int currentOffset) {
        return Futures.transform(resultUpdateFlowFuture, FlatBatchFlowAdapters.convertBatchFlowResult(currentOffset),
                MoreExecutors.directExecutor());
    }
}
