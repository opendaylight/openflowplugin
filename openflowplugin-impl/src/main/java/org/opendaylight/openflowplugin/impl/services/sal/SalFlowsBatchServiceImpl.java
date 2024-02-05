/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
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
    private final SendBarrier sendBarrier;

    public SalFlowsBatchServiceImpl(final SalFlowService salFlowService, final SendBarrier sendBarrier) {
        this.salFlowService = requireNonNull(salFlowService, "delegate flow service must not be null");
        this.sendBarrier = requireNonNull(sendBarrier, "delegate transaction service must not be null");
    }

    @Override
    public ListenableFuture<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBatch(final RemoveFlowsBatchInput input) {
        final var flows = input.nonnullBatchRemoveFlows().values();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Removing flows @ {} : {}", PathUtil.extractNodeId(input.getNode()), flows.size());
        }

        final var resultsLot = flows.stream()
            .map(batchFlow -> salFlowService.removeFlow(new RemoveFlowInputBuilder(batchFlow)
                .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                .setNode(input.getNode())
                .build()))
            .toList();

        final var commonResult = Futures.transform(Futures.successfulAsList(resultsLot),
            FlowUtil.createCumulatingFunction(flows), MoreExecutors.directExecutor());
        final var removeFlowsBulkFuture = Futures.transform(commonResult, FlowUtil.FLOW_REMOVE_TRANSFORM,
            MoreExecutors.directExecutor());
        return input.getBarrierAfter()
            ? BarrierUtil.chainBarrier(removeFlowsBulkFuture, input.getNode(), sendBarrier,
                FlowUtil.FLOW_REMOVE_COMPOSING_TRANSFORM)
            : removeFlowsBulkFuture;
    }

    @Override
    public ListenableFuture<RpcResult<AddFlowsBatchOutput>> addFlowsBatch(final AddFlowsBatchInput input) {
        final var flows = input.nonnullBatchAddFlows().values();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Adding flows @ {} : {}", PathUtil.extractNodeId(input.getNode()), flows.size());
        }

        final var resultsLot = flows.stream()
            .map(batchFlow -> salFlowService.addFlow(new AddFlowInputBuilder(batchFlow)
                .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                .setNode(input.getNode())
                .build()))
            .toList();

        final var commonResult = Futures.transform(Futures.successfulAsList(resultsLot),
            FlowUtil.createCumulatingFunction(flows), MoreExecutors.directExecutor());
        final var addFlowsBulkFuture = Futures.transform(commonResult, FlowUtil.FLOW_ADD_TRANSFORM,
            MoreExecutors.directExecutor());
        return input.getBarrierAfter()
            ? BarrierUtil.chainBarrier(addFlowsBulkFuture, input.getNode(), sendBarrier,
                FlowUtil.FLOW_ADD_COMPOSING_TRANSFORM)
            : addFlowsBulkFuture;
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
    public ListenableFuture<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBatch(final UpdateFlowsBatchInput input) {
        final var flows = input.nonnullBatchUpdateFlows().values();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Updating flows @ {} : {}", PathUtil.extractNodeId(input.getNode()), flows.size());
        }

        final var resultsLot = flows.stream()
            .map(batchFlow -> salFlowService.updateFlow(new UpdateFlowInputBuilder(input)
                .setOriginalFlow(new OriginalFlowBuilder(batchFlow.getOriginalBatchedFlow()).build())
                .setUpdatedFlow(new UpdatedFlowBuilder(batchFlow.getUpdatedBatchedFlow()).build())
                .setFlowRef(createFlowRef(input.getNode(), batchFlow))
                .setNode(input.getNode())
                .build()))
            .toList();

        final var commonResult = Futures.transform(Futures.successfulAsList(resultsLot),
            FlowUtil.createCumulatingFunction(flows), MoreExecutors.directExecutor());
        final var updateFlowsBulkFuture = Futures.transform(commonResult, FlowUtil.FLOW_UPDATE_TRANSFORM,
            MoreExecutors.directExecutor());
        return input.getBarrierAfter()
            ? BarrierUtil.chainBarrier(updateFlowsBulkFuture, input.getNode(), sendBarrier,
                FlowUtil.FLOW_UPDATE_COMPOSING_TRANSFORM)
            : updateFlowsBulkFuture;
    }
}
