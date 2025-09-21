/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.OriginalTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableForwarder extends AbstractListeningCommiter<TableFeatures> {
    private static final Logger LOG = LoggerFactory.getLogger(TableForwarder.class);

    public TableForwarder(final ForwardingRulesManager manager, final DataBroker dataBroker) {
        super(manager, dataBroker);
    }

    @Override
    protected DataObjectReference<TableFeatures> getWildCardPath() {
        return DataObjectReference.builder(Nodes.class)
            .child(Node.class)
            .augmentation(FlowCapableNode.class)
            .child(TableFeatures.class)
            .build();
    }

    @Override
    public void remove(final DataObjectIdentifier<TableFeatures> identifier, final TableFeatures removeDataObj,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        // DO Nothing
    }

    @Override
    public void update(final DataObjectIdentifier<TableFeatures> identifier, final TableFeatures original,
            final TableFeatures update, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Received the Table Update request [Tbl id, node Id, original, upd {} {} {} {}", identifier,
                nodeIdent, original, update);

        final TableFeatures originalTableFeatures = original;
        TableFeatures updatedTableFeatures;
        if (null == update) {
            updatedTableFeatures = original;
        } else {
            updatedTableFeatures = update;
        }
        final UpdateTableInputBuilder builder = new UpdateTableInputBuilder();

        builder.setNode(new NodeRef(nodeIdent.trimTo(Node.class)));

        // TODO: reconsider model - this particular field is not used in service
        // implementation
        builder.setTableRef(new TableRef(identifier));

        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));

        builder.setUpdatedTable(new UpdatedTableBuilder()
            .setTableFeatures(BindingMap.of(updatedTableFeatures))
            .build());

        builder.setOriginalTable(new OriginalTableBuilder()
            .setTableFeatures(BindingMap.of(originalTableFeatures))
            .build());

        final var updateTable = provider.updateTable();
        LOG.debug("Invoking SalTableService on {}", updateTable);
        LoggingFutures.addErrorLogging(provider.updateTable().invoke(builder.build()), LOG, "updateTable");
    }

    @Override
    public ListenableFuture<RpcResult<?>> add(final DataObjectIdentifier<TableFeatures> identifier,
            final TableFeatures addDataObj, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        return Futures.immediateFuture(null);
    }

    @Override
    public void createStaleMarkEntity(final DataObjectIdentifier<TableFeatures> identifier, final TableFeatures del,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("NO-OP");
    }

    @Override
    public ListenableFuture<RpcResult<?>> removeWithResult(final DataObjectIdentifier<TableFeatures> identifier,
            final TableFeatures del, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        return Futures.immediateFuture(null);
    }
}
