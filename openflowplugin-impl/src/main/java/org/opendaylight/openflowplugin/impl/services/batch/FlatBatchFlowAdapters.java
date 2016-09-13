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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.add.flow._case.FlatBatchAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.remove.flow._case.FlatBatchRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.input.batch.batch.choice.flat.batch.update.flow._case.FlatBatchUpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailure;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.BatchFailureBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.process.flat.batch.output.batch.failure.batch.item.id.choice.FlatBatchFailureFlowIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Transform between FlatBatch API and flow batch API.
 */
public class FlatBatchFlowAdapters {

    private FlatBatchFlowAdapters() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    /**
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService#addFlowsBatch(AddFlowsBatchInput)}
     */
    public static AddFlowsBatchInput adaptFlatBatchAddFlow(final BatchPlanStep planStep, final NodeRef node) {
        final List<BatchAddFlows> batchFlows = new ArrayList<>();
        for (FlatBatchAddFlow batchAddFlows : planStep.<FlatBatchAddFlow>getTaskBag()) {
            final BatchAddFlows addFlows = new BatchAddFlowsBuilder((Flow) batchAddFlows)
                    .setFlowId(batchAddFlows.getFlowId())
                    .build();
            batchFlows.add(addFlows);
        }

        return new AddFlowsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchAddFlows(batchFlows)
                .build();
    }

    /**
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService#removeFlowsBatch(RemoveFlowsBatchInput)}
     */
    public static RemoveFlowsBatchInput adaptFlatBatchRemoveFlow(final BatchPlanStep planStep, final NodeRef node) {
        final List<BatchRemoveFlows> batchFlows = new ArrayList<>();
        for (FlatBatchRemoveFlow batchRemoveFlow : planStep.<FlatBatchRemoveFlow>getTaskBag()) {
            final BatchRemoveFlows removeFlows = new BatchRemoveFlowsBuilder((Flow) batchRemoveFlow)
                    .setFlowId(batchRemoveFlow.getFlowId())
                    .build();
            batchFlows.add(removeFlows);
        }

        return new RemoveFlowsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchRemoveFlows(batchFlows)
                .build();
    }

    /**
     * @param planStep batch step containing changes of the same type
     * @param node     pointer for RPC routing
     * @return input suitable for {@link org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService#updateFlowsBatch(UpdateFlowsBatchInput)}
     */
    public static UpdateFlowsBatchInput adaptFlatBatchUpdateFlow(final BatchPlanStep planStep, final NodeRef node) {
        final List<BatchUpdateFlows> batchFlows = new ArrayList<>();
        for (FlatBatchUpdateFlow batchUpdateFlow : planStep.<FlatBatchUpdateFlow>getTaskBag()) {
            final BatchUpdateFlows updateFlows = new BatchUpdateFlowsBuilder(batchUpdateFlow)
                    .build();
            batchFlows.add(updateFlows);
        }

        return new UpdateFlowsBatchInputBuilder()
                .setBarrierAfter(planStep.isBarrierAfter())
                .setNode(node)
                .setBatchUpdateFlows(batchFlows)
                .build();
    }

    /**
     * @param stepOffset offset of current batch plan step
     * @return converted {@link ProcessFlatBatchOutput} RPC result
     */
    @VisibleForTesting
    static <T extends BatchFlowOutputListGrouping> Function<RpcResult<T>, RpcResult<ProcessFlatBatchOutput>>
    convertBatchFlowResult(final int stepOffset) {
        return new Function<RpcResult<T>, RpcResult<ProcessFlatBatchOutput>>() {
            @Nullable
            @Override
            public RpcResult<ProcessFlatBatchOutput> apply(@Nullable final RpcResult<T> input) {
                List<BatchFailure> batchFailures = wrapBatchFlowFailuresForFlat(input, stepOffset);
                ProcessFlatBatchOutputBuilder outputBuilder = new ProcessFlatBatchOutputBuilder().setBatchFailure(batchFailures);
                return RpcResultBuilder.<ProcessFlatBatchOutput>status(input.isSuccessful())
                                       .withRpcErrors(input.getErrors())
                                       .withResult(outputBuilder.build())
                                       .build();
            }
        };
    }

    private static <T extends BatchFlowOutputListGrouping> List<BatchFailure> wrapBatchFlowFailuresForFlat(
            final RpcResult<T> input, final int stepOffset) {
        final List<BatchFailure> batchFailures = new ArrayList<>();
        if (input.getResult().getBatchFailedFlowsOutput() != null) {
            for (BatchFailedFlowsOutput stepOutput : input.getResult().getBatchFailedFlowsOutput()) {
                final BatchFailure batchFailure = new BatchFailureBuilder()
                        .setBatchOrder(stepOffset + stepOutput.getBatchOrder())
                        .setBatchItemIdChoice(new FlatBatchFailureFlowIdCaseBuilder()
                                .setFlowId(stepOutput.getFlowId())
                                .build())
                        .build();
                batchFailures.add(batchFailure);
            }
        }
        return batchFailures;
    }

    /**
     * shortcut for {@link #convertBatchFlowResult(int)} with conversion {@link ListenableFuture}
     *
     * @param <T>                    exact type of batch flow output
     * @param resultUpdateFlowFuture batch flow rpc-result (add/remove/update)
     * @param currentOffset          offset of current batch plan step with respect to entire chain of steps
     * @return ListenableFuture with converted result {@link ProcessFlatBatchOutput}
     */
    public static <T extends BatchFlowOutputListGrouping> ListenableFuture<RpcResult<ProcessFlatBatchOutput>>
    convertFlowBatchFutureForChain(final Future<RpcResult<T>> resultUpdateFlowFuture,
                                   final int currentOffset) {
        return Futures.transform(JdkFutureAdapters.listenInPoolThread(resultUpdateFlowFuture),
                FlatBatchFlowAdapters.<T>convertBatchFlowResult(currentOffset));
    }
}
