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
import org.opendaylight.openflowplugin.applications.frsync.ForwardingRulesUpdateCommitter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.OriginalTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link ForwardingRulesUpdateCommitter} methods for processing update of {@link TableFeatures}.
 */
public class TableForwarder implements ForwardingRulesUpdateCommitter<TableFeatures, UpdateTableOutput> {
    private static final Logger LOG = LoggerFactory.getLogger(TableForwarder.class);

    private final UpdateTable updateTable;

    public TableForwarder(final UpdateTable updateTable) {
        this.updateTable = requireNonNull(updateTable);
    }

    @Override
    public ListenableFuture<RpcResult<UpdateTableOutput>> update(final DataObjectIdentifier<TableFeatures> identifier,
            final TableFeatures original, final TableFeatures update,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Forwarding Table Update request [Tbl id, node Id {} {}", identifier, nodeIdent);

        LOG.debug("Invoking SalTableService {}", nodeIdent);
        return updateTable.invoke(new UpdateTableInputBuilder()
            .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
            .setTableRef(new TableRef(nodeIdent.toBuilder()
                .child(Table.class, new TableKey(identifier.getFirstKeyOf(TableFeatures.class).getTableId()))
                .build()))
            .setUpdatedTable(new UpdatedTableBuilder().setTableFeatures(BindingMap.of(update)).build())
            .setOriginalTable(new OriginalTableBuilder().setTableFeatures(BindingMap.of(original)).build())
            .build());
    }
}
