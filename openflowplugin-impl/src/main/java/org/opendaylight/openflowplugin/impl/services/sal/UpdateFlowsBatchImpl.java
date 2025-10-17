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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowInputUpdateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UpdateFlowsBatchImpl implements UpdateFlowsBatch {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateFlowsBatchImpl.class);

    private final UpdateFlow updateFlow;
    private final SendBarrier sendBarrier;

    public UpdateFlowsBatchImpl(final UpdateFlow updateFlow, final SendBarrier sendBarrier) {
        this.updateFlow = requireNonNull(updateFlow);
        this.sendBarrier = requireNonNull(sendBarrier);
    }

    @Override
    public ListenableFuture<RpcResult<UpdateFlowsBatchOutput>> invoke(final UpdateFlowsBatchInput input) {
        final var flows = input.nonnullBatchUpdateFlows().values();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Updating flows @ {} : {}", PathUtil.extractNodeId(input.getNode()), flows.size());
        }

        final var resultsLot = flows.stream()
            .map(batchFlow -> updateFlow.invoke(new UpdateFlowInputBuilder(input)
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

    private static FlowRef createFlowRef(final NodeRef nodeRef, final BatchFlowInputUpdateGrouping batchFlow) {
        return FlowUtil.buildFlowPath((DataObjectIdentifier<Node>) nodeRef.getValue(),
                batchFlow.getOriginalBatchedFlow().getTableId(), batchFlow.getFlowId());
    }
}
