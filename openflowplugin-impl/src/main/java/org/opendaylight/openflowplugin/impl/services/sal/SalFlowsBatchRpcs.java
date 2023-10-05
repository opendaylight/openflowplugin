/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowInputGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowInputUpdateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation delegates work to {@link SalFlowRpcs}.
 */
public class SalFlowsBatchRpcs {
    private static final Logger LOG = LoggerFactory.getLogger(SalFlowsBatchRpcs.class);

    private final SalFlowRpcs salFlowRpcs;
    private final FlowCapableTransactionRpc transactionRpc;

    public SalFlowsBatchRpcs(final SalFlowRpcs salFlowRpcs,
                                    final FlowCapableTransactionRpc transactionRpc) {
        this.salFlowRpcs = requireNonNull(salFlowRpcs, "delegate flow rpcs must not be null");
        this.transactionRpc = requireNonNull(transactionRpc, "delegate transaction service must not be null");
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBatch(final RemoveFlowsBatchInput input) {
        LOG.trace("Removing flows @ {} : {}",
                  PathUtil.extractNodeId(input.getNode()),
                  input.getBatchRemoveFlows().size());
        final ArrayList<ListenableFuture<RpcResult<RemoveFlowOutput>>> resultsLot = new ArrayList<>();
        for (BatchFlowInputGrouping batchFlow : input.nonnullBatchRemoveFlows().values()) {
            final RemoveFlowInput removeFlowInput = new RemoveFlowInputBuilder(batchFlow)
                    .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(salFlowRpcs.getRpcClassToInstanceMap().getInstance(RemoveFlow.class)
                .invoke(removeFlowInput));
        }

        final ListenableFuture<RpcResult<List<BatchFailedFlowsOutput>>> commonResult =
                Futures.transform(Futures.successfulAsList(resultsLot),
                        FlowUtil.createCumulatingFunction(input.nonnullBatchRemoveFlows().values()),
                        MoreExecutors.directExecutor());

        ListenableFuture<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBulkFuture =
                Futures.transform(commonResult, FlowUtil.FLOW_REMOVE_TRANSFORM, MoreExecutors.directExecutor());

        if (input.getBarrierAfter()) {
            removeFlowsBulkFuture = BarrierUtil.chainBarrier(removeFlowsBulkFuture, input.getNode(),
                transactionRpc, FlowUtil.FLOW_REMOVE_COMPOSING_TRANSFORM);
        }

        return removeFlowsBulkFuture;
    }

    @VisibleForTesting
    ListenableFuture<RpcResult<AddFlowsBatchOutput>> addFlowsBatch(final AddFlowsBatchInput input) {
        LOG.trace("Adding flows @ {} : {}", PathUtil.extractNodeId(input.getNode()), input.getBatchAddFlows().size());
        final ArrayList<ListenableFuture<RpcResult<AddFlowOutput>>> resultsLot = new ArrayList<>();
        for (BatchFlowInputGrouping batchFlow : input.nonnullBatchAddFlows().values()) {
            final AddFlowInput addFlowInput = new AddFlowInputBuilder(batchFlow)
                    .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(salFlowRpcs.getRpcClassToInstanceMap().getInstance(AddFlow.class).invoke(addFlowInput));
        }

        final ListenableFuture<RpcResult<List<BatchFailedFlowsOutput>>> commonResult =
                Futures.transform(Futures.successfulAsList(resultsLot),
                        FlowUtil.createCumulatingFunction(input.nonnullBatchAddFlows().values()),
                        MoreExecutors.directExecutor());

        ListenableFuture<RpcResult<AddFlowsBatchOutput>> addFlowsBulkFuture =
                Futures.transform(commonResult, FlowUtil.FLOW_ADD_TRANSFORM, MoreExecutors.directExecutor());

        if (input.getBarrierAfter()) {
            addFlowsBulkFuture = BarrierUtil.chainBarrier(addFlowsBulkFuture, input.getNode(),
                transactionRpc, FlowUtil.FLOW_ADD_COMPOSING_TRANSFORM);
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

    @VisibleForTesting
    ListenableFuture<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBatch(final UpdateFlowsBatchInput input) {
        LOG.trace("Updating flows @ {} : {}",
                  PathUtil.extractNodeId(input.getNode()),
                  input.getBatchUpdateFlows().size());
        final ArrayList<ListenableFuture<RpcResult<UpdateFlowOutput>>> resultsLot = new ArrayList<>();
        for (BatchUpdateFlows batchFlow : input.nonnullBatchUpdateFlows().values()) {
            final UpdateFlowInput updateFlowInput = new UpdateFlowInputBuilder(input)
                    .setOriginalFlow(new OriginalFlowBuilder(batchFlow.getOriginalBatchedFlow()).build())
                    .setUpdatedFlow(new UpdatedFlowBuilder(batchFlow.getUpdatedBatchedFlow()).build())
                    .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                    .setNode(input.getNode())
                    .build();
            resultsLot.add(salFlowRpcs.getRpcClassToInstanceMap().getInstance(UpdateFlow.class)
                .invoke(updateFlowInput));
        }

        final ListenableFuture<RpcResult<List<BatchFailedFlowsOutput>>> commonResult =
                Futures.transform(Futures.successfulAsList(resultsLot),
                                  FlowUtil.createCumulatingFunction(input.nonnullBatchUpdateFlows().values()),
                        MoreExecutors.directExecutor());

        ListenableFuture<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBulkFuture =
                Futures.transform(commonResult, FlowUtil.FLOW_UPDATE_TRANSFORM, MoreExecutors.directExecutor());

        if (input.getBarrierAfter()) {
            updateFlowsBulkFuture = BarrierUtil.chainBarrier(updateFlowsBulkFuture, input.getNode(),
                transactionRpc, FlowUtil.FLOW_UPDATE_COMPOSING_TRANSFORM);
        }

        return updateFlowsBulkFuture;
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(RemoveFlowsBatch.class, this::removeFlowsBatch)
            .put(AddFlowsBatch.class, this::addFlowsBatch)
            .put(UpdateFlowsBatch.class, this::updateFlowsBatch)
            .build();
    }
}
