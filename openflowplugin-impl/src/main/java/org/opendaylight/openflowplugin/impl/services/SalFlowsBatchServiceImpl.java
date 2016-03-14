/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowInputGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowInputUpdateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * default implementation of {@link SalFlowsBatchService} - delegates work to {@link SalFlowService}
 */
public class SalFlowsBatchServiceImpl implements SalFlowsBatchService {
    private static final Logger LOG = LoggerFactory.getLogger(SalFlowsBatchServiceImpl.class);

    private static final Function<Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>>, RpcResult<AddFlowsBatchOutput>> FLOW_ADD_COMPOSITE_TRANSFORM =
            new Function<Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>>, RpcResult<AddFlowsBatchOutput>>() {
                @Nullable
                @Override
                public RpcResult<AddFlowsBatchOutput> apply(@Nullable final Pair<RpcResult<AddFlowsBatchOutput>, RpcResult<Void>> input) {
                    final AddFlowsBatchOutput batchOutput =
                            new AddFlowsBatchOutputBuilder(input.getLeft().getResult())
                                    .setBarrierAfterPassed(input.getRight().isSuccessful())
                                    .build();
                    return RpcResultBuilder.success(batchOutput).build();
                }
            };

    private static final Function<Pair<RpcResult<RemoveFlowsBatchOutput>, RpcResult<Void>>, RpcResult<RemoveFlowsBatchOutput>> FLOW_REMOVE_COMPOSITE_TRANSFORM =
            new Function<Pair<RpcResult<RemoveFlowsBatchOutput>, RpcResult<Void>>, RpcResult<RemoveFlowsBatchOutput>>() {
                @Nullable
                @Override
                public RpcResult<RemoveFlowsBatchOutput> apply(@Nullable final Pair<RpcResult<RemoveFlowsBatchOutput>, RpcResult<Void>> input) {
                    final RemoveFlowsBatchOutput batchOutput =
                            new RemoveFlowsBatchOutputBuilder(input.getLeft().getResult())
                                    .setBarrierAfterPassed(input.getRight().isSuccessful())
                                    .build();
                    return RpcResultBuilder.success(batchOutput).build();
                }
            };

    private static final Function<Pair<RpcResult<UpdateFlowsBatchOutput>, RpcResult<Void>>, RpcResult<UpdateFlowsBatchOutput>> FLOW_UPDATE_COMPOSITE_TRANSFORM =
            new Function<Pair<RpcResult<UpdateFlowsBatchOutput>, RpcResult<Void>>, RpcResult<UpdateFlowsBatchOutput>>() {
                @Nullable
                @Override
                public RpcResult<UpdateFlowsBatchOutput> apply(@Nullable final Pair<RpcResult<UpdateFlowsBatchOutput>, RpcResult<Void>> input) {
                    final UpdateFlowsBatchOutput batchOutput =
                            new UpdateFlowsBatchOutputBuilder(input.getLeft().getResult())
                                    .setBarrierAfterPassed(input.getRight().isSuccessful())
                                    .build();
                    return RpcResultBuilder.success(batchOutput).build();
                }
            };

    private final SalFlowService salFlowService;
    private final FlowCapableTransactionService transactionService;

    private static final Function<ArrayList<BatchFlowsOutput>, RpcResult<AddFlowsBatchOutput>> FLOW_ADD_TRANSFORM =
            new Function<ArrayList<BatchFlowsOutput>, RpcResult<AddFlowsBatchOutput>>() {
                @Nullable
                @Override
                public RpcResult<AddFlowsBatchOutput> apply(@Nullable final ArrayList<BatchFlowsOutput> batchFlows) {
                    final AddFlowsBatchOutput batchOutput = new AddFlowsBatchOutputBuilder()
                            .setBatchFlowsOutput(batchFlows).build();
                    return RpcResultBuilder.success(batchOutput).build();
                }
            };

    private static final Function<ArrayList<BatchFlowsOutput>, RpcResult<RemoveFlowsBatchOutput>> FLOW_REMOVE_TRANSFORM =
            new Function<ArrayList<BatchFlowsOutput>, RpcResult<RemoveFlowsBatchOutput>>() {
                @Nullable
                @Override
                public RpcResult<RemoveFlowsBatchOutput> apply(@Nullable final ArrayList<BatchFlowsOutput> batchFlows) {
                    final RemoveFlowsBatchOutput batchOutput = new RemoveFlowsBatchOutputBuilder()
                            .setBatchFlowsOutput(batchFlows).build();
                    return RpcResultBuilder.success(batchOutput).build();
                }
            };

    private static final Function<ArrayList<BatchFlowsOutput>, RpcResult<UpdateFlowsBatchOutput>> FLOW_UPDATE_TRANSFORM =
            new Function<ArrayList<BatchFlowsOutput>, RpcResult<UpdateFlowsBatchOutput>>() {
                @Nullable
                @Override
                public RpcResult<UpdateFlowsBatchOutput> apply(@Nullable final ArrayList<BatchFlowsOutput> batchFlows) {
                    final UpdateFlowsBatchOutput batchOutput = new UpdateFlowsBatchOutputBuilder()
                            .setBatchFlowsOutput(batchFlows).build();
                    return RpcResultBuilder.success(batchOutput).build();
                }
            };

    public SalFlowsBatchServiceImpl(final SalFlowService salFlowService,
                                    final FlowCapableTransactionService transactionService) {
        this.salFlowService = Preconditions.checkNotNull(salFlowService);
        this.transactionService = Preconditions.checkNotNull(transactionService);
    }

    @Override
    public Future<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBatch(final RemoveFlowsBatchInput input) {
        LOG.trace("Removing flows @ {} : {}", extractNodeId(input.getNode()), input.getBatchRemoveFlows().size());
        final ArrayList<ListenableFuture<RpcResult<RemoveFlowOutput>>> resultsLot = new ArrayList<>();
        for (BatchFlowInputGrouping batchFlow : input.getBatchRemoveFlows()) {
            final RemoveFlowInput removeFlowInput = new RemoveFlowInputBuilder(batchFlow)
                    .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salFlowService.removeFlow(removeFlowInput)));
        }

        final ListenableFuture<ArrayList<BatchFlowsOutput>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot),
                        FlowUtil.<RemoveFlowOutput>createCumulativeFunction(input.getBatchRemoveFlows()));

        ListenableFuture<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBulkFuture = Futures.transform(commonResult, FLOW_REMOVE_TRANSFORM);

        if (input.isBarrierAfter()) {
            removeFlowsBulkFuture = BarrierUtil.chainBarrier(removeFlowsBulkFuture, input.getNode(),
                    transactionService, FLOW_REMOVE_COMPOSITE_TRANSFORM);
        }

        return removeFlowsBulkFuture;
    }

    @VisibleForTesting
    static NodeId extractNodeId(final NodeRef input) {
        return input.getValue().firstKeyOf(Node.class).getId();
    }

    @Override
    public Future<RpcResult<AddFlowsBatchOutput>> addFlowsBatch(final AddFlowsBatchInput input) {
        LOG.trace("Adding flows @ {} : {}", extractNodeId(input.getNode()), input.getBatchAddFlows().size());
        final ArrayList<ListenableFuture<RpcResult<AddFlowOutput>>> resultsLot = new ArrayList<>();
        for (BatchFlowInputGrouping batchFlow : input.getBatchAddFlows()) {
            final AddFlowInput addFlowInput = new AddFlowInputBuilder(batchFlow)
                    .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salFlowService.addFlow(addFlowInput)));
        }

        final ListenableFuture<ArrayList<BatchFlowsOutput>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot),
                        FlowUtil.<AddFlowOutput>createCumulativeFunction(input.getBatchAddFlows()));

        ListenableFuture<RpcResult<AddFlowsBatchOutput>> addFlowsBulkFuture =
                Futures.transform(commonResult, FLOW_ADD_TRANSFORM);

        if (input.isBarrierAfter()) {
            addFlowsBulkFuture = BarrierUtil.chainBarrier(addFlowsBulkFuture, input.getNode(),
                    transactionService, FLOW_ADD_COMPOSITE_TRANSFORM);
        }

        return addFlowsBulkFuture;
    }

    private static FlowRef createFlowRef(final NodeRef nodeRef, final BatchFlowInputGrouping batchFlow) {
        return FlowUtil.buildFlowPath((InstanceIdentifier<Node>) nodeRef.getValue(),
                batchFlow.getTableId(), batchFlow.getFlowId());
    }

    private static FlowRef createFlowRef(final NodeRef nodeRef, final BatchFlowInputUpdateGrouping batchFlow) {
        return FlowUtil.buildFlowPath((InstanceIdentifier<Node>) nodeRef.getValue(),
                batchFlow.getOriginalBatchedFlow().getTableId(), batchFlow.getFlowId());
    }

    @Override
    public Future<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBatch(final UpdateFlowsBatchInput input) {
        LOG.trace("Updating flows @ {} : {}", extractNodeId(input.getNode()), input.getBatchUpdateFlows().size());
        final ArrayList<ListenableFuture<RpcResult<UpdateFlowOutput>>> resultsLot = new ArrayList<>();
        for (BatchUpdateFlows batchFlow : input.getBatchUpdateFlows()) {
            final UpdateFlowInput updateFlowInput = new UpdateFlowInputBuilder(input)
                    .setOriginalFlow(new OriginalFlowBuilder(batchFlow.getOriginalBatchedFlow()).build())
                    .setUpdatedFlow(new UpdatedFlowBuilder(batchFlow.getUpdatedBatchedFlow()).build())
                    .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salFlowService.updateFlow(updateFlowInput)));
        }

        final ListenableFuture<ArrayList<BatchFlowsOutput>> commonResult =
                Futures.transform(Futures.allAsList(resultsLot), FlowUtil.<UpdateFlowOutput>createCumulativeFunction(input.getBatchUpdateFlows()));

        ListenableFuture<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBulkFuture = Futures.transform(commonResult, FLOW_UPDATE_TRANSFORM);

        if (input.isBarrierAfter()) {
            updateFlowsBulkFuture = BarrierUtil.chainBarrier(updateFlowsBulkFuture, input.getNode(),
                    transactionService, FLOW_UPDATE_COMPOSITE_TRANSFORM);
        }

        return updateFlowsBulkFuture;
    }

}
