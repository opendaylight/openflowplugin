/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.frsync.ForwardingRulesCommitter;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowRpcs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link ForwardingRulesCommitter} methods for processing add, update and remove of {@link Flow}.
 */
public class FlowForwarder implements ForwardingRulesCommitter<Flow, AddFlowOutput, RemoveFlowOutput,
        UpdateFlowOutput> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowForwarder.class);
    private static final String TABLE_ID_MISMATCH = "tableId mismatch";
    private final SalFlowRpcs salFlowService;

    public FlowForwarder(final SalFlowRpcs salFlowService) {
        this.salFlowService = salFlowService;
    }

    @Override
    public ListenableFuture<RpcResult<RemoveFlowOutput>> remove(final InstanceIdentifier<Flow> identifier,
                                                      final Flow removeDataObj,
                                                      final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Forwarding Flow REMOVE request Tbl id, node Id {} {}",
                identifier, nodeIdent);

        final TableKey tableKey = identifier.firstKeyOf(Table.class);
        if (tableIdValidationPrecondition(tableKey, removeDataObj)) {
            final RemoveFlowInputBuilder builder = new RemoveFlowInputBuilder(removeDataObj);
            builder.setFlowRef(new FlowRef(identifier));
            builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
            builder.setFlowTable(new FlowTableRef(nodeIdent.child(Table.class, tableKey)));

            // always needs to set strict flag into remove-flow input so that
            // only a flow entry associated with a given flow object will be removed.
            builder.setStrict(Boolean.TRUE);
            return salFlowService.getRpcClassToInstanceMap().getInstance(RemoveFlow.class).invoke(builder.build());
        } else {
            return RpcResultBuilder.<RemoveFlowOutput>failed()
                    .withError(ErrorType.APPLICATION, TABLE_ID_MISMATCH).buildFuture();
        }
    }

    @Override
    public ListenableFuture<RpcResult<UpdateFlowOutput>> update(final InstanceIdentifier<Flow> identifier,
                                                      final Flow original, final Flow update,
                                                      final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Forwarding Flow UPDATE request [Tbl id, node Id {} {} {}",
                identifier, nodeIdent, update);

        final ListenableFuture<RpcResult<UpdateFlowOutput>> output;
        final TableKey tableKey = identifier.firstKeyOf(Table.class);
        if (tableIdValidationPrecondition(tableKey, update)) {
            final UpdateFlowInputBuilder builder = new UpdateFlowInputBuilder();

            builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
            builder.setFlowRef(new FlowRef(identifier));

            // always needs to set strict flag into update-flow input so that
            // only a flow entry associated with a given flow object is updated.
            builder.setUpdatedFlow(new UpdatedFlowBuilder(update).setStrict(Boolean.TRUE).build());
            builder.setOriginalFlow(new OriginalFlowBuilder(original).setStrict(Boolean.TRUE).build());

            output = salFlowService.getRpcClassToInstanceMap().getInstance(UpdateFlow.class).invoke(builder.build());
        } else {
            output = RpcResultBuilder.<UpdateFlowOutput>failed()
                    .withError(ErrorType.APPLICATION, TABLE_ID_MISMATCH).buildFuture();
        }

        return output;
    }

    @Override
    public ListenableFuture<RpcResult<AddFlowOutput>> add(final InstanceIdentifier<Flow> identifier,
                                                final Flow addDataObj,
                                                final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Forwarding the Flow ADD request [Tbl id, node Id {} {} {}",
                identifier, nodeIdent, addDataObj);

        final ListenableFuture<RpcResult<AddFlowOutput>> output;
        final TableKey tableKey = identifier.firstKeyOf(Table.class);
        if (tableIdValidationPrecondition(tableKey, addDataObj)) {
            final AddFlowInputBuilder builder = new AddFlowInputBuilder(addDataObj);

            builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
            builder.setFlowRef(new FlowRef(identifier));
            builder.setFlowTable(new FlowTableRef(nodeIdent.child(Table.class, tableKey)));
            output = salFlowService.getRpcClassToInstanceMap().getInstance(AddFlow.class)
                .invoke(builder.build());
        } else {
            output = RpcResultBuilder.<AddFlowOutput>failed().withError(ErrorType.APPLICATION, TABLE_ID_MISMATCH)
                .buildFuture();
        }
        return output;
    }

    private static boolean tableIdValidationPrecondition(final TableKey tableKey, final Flow flow) {
        requireNonNull(tableKey, "TableKey can not be null or empty!");
        requireNonNull(flow, "Flow can not be null or empty!");
        if (!tableKey.getId().equals(flow.getTableId())) {
            LOG.warn("TableID in URI tableId={} and in payload tableId={} is not same.",
                    flow.getTableId(), tableKey.getId());
            return false;
        }
        return true;
    }

}
