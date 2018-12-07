/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.Futures;
import java.util.Collections;
import java.util.concurrent.Future;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableForwarder extends AbstractListeningCommiter<TableFeatures> {

    private static final Logger LOG = LoggerFactory.getLogger(TableForwarder.class);

    public TableForwarder(final ForwardingRulesManager manager, final DataBroker db,
                          final ListenerRegistrationHelper registrationHelper) {
        super(manager, db, registrationHelper);
    }

    @Override
    public  void deregisterListener() {
        close();
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
    }

    @Override
    protected InstanceIdentifier<TableFeatures> getWildCardPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class)
                .child(TableFeatures.class);
    }

    @Override
    public void remove(final InstanceIdentifier<TableFeatures> identifier, final TableFeatures removeDataObj,
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        // DO Nothing
    }

    @Override
    public void update(final InstanceIdentifier<TableFeatures> identifier, final TableFeatures original,
            final TableFeatures update, final InstanceIdentifier<FlowCapableNode> nodeIdent) {
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

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));

        // TODO: reconsider model - this particular field is not used in service
        // implementation
        builder.setTableRef(new TableRef(identifier));

        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));

        builder.setUpdatedTable(
                new UpdatedTableBuilder().setTableFeatures(Collections.singletonList(updatedTableFeatures)).build());

        builder.setOriginalTable(
                new OriginalTableBuilder().setTableFeatures(Collections.singletonList(originalTableFeatures)).build());
        LOG.debug("Invoking SalTableService ");

        if (this.provider.getSalTableService() != null) {
            LOG.debug(" Handle to SalTableServices {}", this.provider.getSalTableService());
        }

        LoggingFutures.addErrorLogging(this.provider.getSalTableService().updateTable(builder.build()), LOG,
            "updateTable");
    }

    @Override
    public Future<? extends RpcResult<?>> add(final InstanceIdentifier<TableFeatures> identifier,
            final TableFeatures addDataObj, final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return Futures.immediateFuture(null);
    }

    @Override
    public void createStaleMarkEntity(InstanceIdentifier<TableFeatures> identifier, TableFeatures del,
            InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("NO-OP");
    }

    @Override
    public Future<? extends RpcResult<?>> removeWithResult(InstanceIdentifier<TableFeatures> identifier,
            TableFeatures del, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return null;
    }
}
