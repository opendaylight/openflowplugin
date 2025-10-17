/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowInputGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoveFlowsBatchImpl implements RemoveFlowsBatch {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveFlowsBatchImpl.class);

    private final RemoveFlow removeFlow;
    private final SendBarrier sendBarrier;

    public RemoveFlowsBatchImpl(final RemoveFlow removeFlow, final SendBarrier sendBarrier) {
        this.removeFlow = requireNonNull(removeFlow);
        this.sendBarrier = requireNonNull(sendBarrier);
    }

    @Override
    public ListenableFuture<RpcResult<RemoveFlowsBatchOutput>> invoke(final RemoveFlowsBatchInput input) {
        final var flows = input.nonnullBatchRemoveFlows().values();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Removing flows @ {} : {}", PathUtil.extractNodeId(input.getNode()), flows.size());
        }

        final var resultsLot = flows.stream()
            .map(batchFlow -> removeFlow.invoke(new RemoveFlowInputBuilder(batchFlow)
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

    private static FlowRef createFlowRef(final NodeRef nodeRef, final BatchFlowInputGrouping batchFlow) {
        return FlowUtil.buildFlowPath((DataObjectIdentifier<Node>) nodeRef.getValue(),
                batchFlow.getTableId(), batchFlow.getFlowId());
    }
}
