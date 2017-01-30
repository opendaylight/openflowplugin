/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowInputGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowInputUpdateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link SalFlowsBatchService} - delegates work to {@link SalFlowService}.
 */
public class SalFlowsBatchServiceImpl implements SalFlowsBatchService {
    private static final Logger LOG = LoggerFactory.getLogger(SalFlowsBatchServiceImpl.class);

    private final SalFlowService salFlowService;
    private final FlowCapableTransactionService transactionService;

    public SalFlowsBatchServiceImpl(final SalFlowService salFlowService,
                                    final FlowCapableTransactionService transactionService) {
        this.salFlowService = Preconditions.checkNotNull(salFlowService, "delegate flow service must not be null");
        this.transactionService = Preconditions.checkNotNull(transactionService, "delegate transaction service must not be null");
    }

    @Override
    public Future<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBatch(final RemoveFlowsBatchInput input) {
        LOG.trace("Removing flows @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchRemoveFlows().size());
        final ArrayList<ListenableFuture<RpcResult<RemoveFlowOutput>>> resultsLot = new ArrayList<>();
        for (BatchFlowInputGrouping batchFlow : input.getBatchRemoveFlows()) {
            final RemoveFlowInput removeFlowInput = new RemoveFlowInputBuilder(batchFlow)
                    .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salFlowService.removeFlow(removeFlowInput)));
        }

        final ListenableFuture<RpcResult<List<BatchFailedFlowsOutput>>> commonResult =
                Futures.transform(Futures.successfulAsList(resultsLot),
                        FlowUtil.<RemoveFlowOutput>createCumulatingFunction(input.getBatchRemoveFlows()));

        ListenableFuture<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBulkFuture = Futures.transform(commonResult, FlowUtil.FLOW_REMOVE_TRANSFORM);

        if (input.isBarrierAfter()) {
            removeFlowsBulkFuture = BarrierUtil.chainBarrier(removeFlowsBulkFuture, input.getNode(),
                    transactionService, FlowUtil.FLOW_REMOVE_COMPOSING_TRANSFORM);
        }

        return removeFlowsBulkFuture;
    }

    @Override
    public Future<RpcResult<AddFlowsBatchOutput>> addFlowsBatch(final AddFlowsBatchInput input) {
        LOG.trace("Adding flows @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchAddFlows().size());
        final ArrayList<ListenableFuture<RpcResult<AddFlowOutput>>> resultsLot = new ArrayList<>();
        for (BatchFlowInputGrouping batchFlow : input.getBatchAddFlows()) {
            final AddFlowInput addFlowInput = new AddFlowInputBuilder(batchFlow)
                    .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(JdkFutureAdapters.listenInPoolThread(salFlowService.addFlow(addFlowInput)));
        }

        final ListenableFuture<RpcResult<List<BatchFailedFlowsOutput>>> commonResult =
                Futures.transform(Futures.successfulAsList(resultsLot),
                        FlowUtil.<AddFlowOutput>createCumulatingFunction(input.getBatchAddFlows()));

        ListenableFuture<RpcResult<AddFlowsBatchOutput>> addFlowsBulkFuture =
                Futures.transform(commonResult, FlowUtil.FLOW_ADD_TRANSFORM);

        if (input.isBarrierAfter()) {
            addFlowsBulkFuture = BarrierUtil.chainBarrier(addFlowsBulkFuture, input.getNode(),
                    transactionService, FlowUtil.FLOW_ADD_COMPOSING_TRANSFORM);
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
        LOG.trace("Updating flows @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchUpdateFlows().size());
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

        final ListenableFuture<RpcResult<List<BatchFailedFlowsOutput>>> commonResult =
                Futures.transform(Futures.successfulAsList(resultsLot), FlowUtil.<UpdateFlowOutput>createCumulatingFunction(input.getBatchUpdateFlows()));

        ListenableFuture<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBulkFuture = Futures.transform(commonResult, FlowUtil.FLOW_UPDATE_TRANSFORM);

        if (input.isBarrierAfter()) {
            updateFlowsBulkFuture = BarrierUtil.chainBarrier(updateFlowsBulkFuture, input.getNode(),
                    transactionService, FlowUtil.FLOW_UPDATE_COMPOSING_TRANSFORM);
        }

        return updateFlowsBulkFuture;
    }

}
