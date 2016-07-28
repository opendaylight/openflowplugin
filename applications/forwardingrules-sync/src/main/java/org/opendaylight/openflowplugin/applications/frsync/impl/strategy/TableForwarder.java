/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import java.util.Collections;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.applications.frsync.ForwardingRulesUpdateCommitter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.OriginalTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link ForwardingRulesUpdateCommitter} methods for processing update of {@link TableFeatures}.
 */
public class TableForwarder implements ForwardingRulesUpdateCommitter<TableFeatures, UpdateTableOutput> {

    private static final Logger LOG = LoggerFactory.getLogger(TableForwarder.class);
    private final SalTableService salTableService;

    public TableForwarder(SalTableService salTableService) {
        this.salTableService = salTableService;
    }

    @Override
    public Future<RpcResult<UpdateTableOutput>> update(final InstanceIdentifier<TableFeatures> identifier,
                                                       final TableFeatures original, final TableFeatures update,
                                                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Forwarding Table Update request [Tbl id, node Id {} {}",
                identifier, nodeIdent);

        final UpdateTableInputBuilder builder = new UpdateTableInputBuilder();

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));

        final InstanceIdentifier<Table> iiToTable = nodeIdent.child(Table.class,
                new TableKey(identifier.firstKeyOf(TableFeatures.class).getTableId()));
        builder.setTableRef(new TableRef(iiToTable));

        builder.setUpdatedTable(new UpdatedTableBuilder().setTableFeatures(
                Collections.singletonList(update)).build());

        builder.setOriginalTable(new OriginalTableBuilder().setTableFeatures(
                Collections.singletonList(original)).build());
        LOG.debug("Invoking SalTableService {} ", nodeIdent);

        return salTableService.updateTable(builder.build());
    }

}
