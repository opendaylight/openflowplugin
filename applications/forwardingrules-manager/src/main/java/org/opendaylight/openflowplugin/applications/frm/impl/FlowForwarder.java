/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FlowForwarder
 * It implements {@link org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener}
 * for WildCardedPath to {@link Flow} and ForwardingRulesCommiter interface for methods:
 * add, update and remove {@link Flow} processing for
 * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}.
 */
public class FlowForwarder extends AbstractListeningCommiter<Flow> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowForwarder.class);
    private final DataBroker dataBroker;
    private ListenerRegistration<FlowForwarder> listenerRegistration;

    public FlowForwarder (final ForwardingRulesManager manager, final DataBroker db) {
        super(manager, Flow.class);
        dataBroker = Preconditions.checkNotNull(db, "DataBroker can not be null!");
        registrationListener(db);
    }

    private void registrationListener(final DataBroker db) {
        final DataTreeIdentifier<Flow> treeId = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, getWildCardPath());
        try {
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
            listenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<FlowForwarder>>() {
                @Override
                public ListenerRegistration<FlowForwarder> call() throws Exception {
                    return db.registerDataTreeChangeListener(treeId, FlowForwarder.this);
                }
            });
        } catch (final Exception e) {
            LOG.warn("FRM Flow DataTreeChange listener registration fail!");
            LOG.debug("FRM Flow DataTreeChange listener registration fail ..", e);
            throw new IllegalStateException("FlowForwarder startup fail! System needs restart.", e);
        }
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            try {
                listenerRegistration.close();
            } catch (final Exception e) {
                LOG.warn("Error by stop FRM FlowChangeListener: {}", e.getMessage());
                LOG.debug("Error by stop FRM FlowChangeListener..", e);
            }
            listenerRegistration = null;
        }
    }

    @Override
    public void remove(final InstanceIdentifier<Flow> identifier,
                       final Flow removeDataObj,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final TableKey tableKey = identifier.firstKeyOf(Table.class, TableKey.class);
        if (tableIdValidationPrecondition(tableKey, removeDataObj)) {
            final RemoveFlowInputBuilder builder = new RemoveFlowInputBuilder(removeDataObj);
            builder.setFlowRef(new FlowRef(identifier));
            builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
            builder.setFlowTable(new FlowTableRef(nodeIdent.child(Table.class, tableKey)));

            // This method is called only when a given flow object has been
            // removed from datastore. So FRM always needs to set strict flag
            // into remove-flow input so that only a flow entry associated with
            // a given flow object is removed.
            builder.setTransactionUri(new Uri(provider.getNewTransactionId())).
                setStrict(Boolean.TRUE);
            provider.getSalFlowService().removeFlow(builder.build());
        }
    }




    //TODO: Pull this into ForwardingRulesCommiter and override it here

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeWithResult(final InstanceIdentifier<Flow> identifier,
                       final Flow removeDataObj,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        Future<RpcResult<RemoveFlowOutput>> resultFuture = SettableFuture.create();
        final TableKey tableKey = identifier.firstKeyOf(Table.class, TableKey.class);
        if (tableIdValidationPrecondition(tableKey, removeDataObj)) {
            final RemoveFlowInputBuilder builder = new RemoveFlowInputBuilder(removeDataObj);
            builder.setFlowRef(new FlowRef(identifier));
            builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
            builder.setFlowTable(new FlowTableRef(nodeIdent.child(Table.class, tableKey)));

            // This method is called only when a given flow object has been
            // removed from datastore. So FRM always needs to set strict flag
            // into remove-flow input so that only a flow entry associated with
            // a given flow object is removed.
            builder.setTransactionUri(new Uri(provider.getNewTransactionId())).
                    setStrict(Boolean.TRUE);
            resultFuture = provider.getSalFlowService().removeFlow(builder.build());
        }

        return resultFuture;
    }



    @Override
    public void update(final InstanceIdentifier<Flow> identifier,
                       final Flow original, final Flow update,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final TableKey tableKey = identifier.firstKeyOf(Table.class, TableKey.class);
        if (tableIdValidationPrecondition(tableKey, update)) {
            final UpdateFlowInputBuilder builder = new UpdateFlowInputBuilder();

            builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
            builder.setFlowRef(new FlowRef(identifier));
            builder.setTransactionUri(new Uri(provider.getNewTransactionId()));

            // This method is called only when a given flow object in datastore
            // has been updated. So FRM always needs to set strict flag into
            // update-flow input so that only a flow entry associated with
            // a given flow object is updated.
            builder.setUpdatedFlow((new UpdatedFlowBuilder(update)).setStrict(Boolean.TRUE).build());
            builder.setOriginalFlow((new OriginalFlowBuilder(original)).setStrict(Boolean.TRUE).build());

            provider.getSalFlowService().updateFlow(builder.build());
        }
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> add(
        final InstanceIdentifier<Flow> identifier, final Flow addDataObj,
        final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        Future<RpcResult<AddFlowOutput>> future;
        final TableKey tableKey = identifier.firstKeyOf(Table.class, TableKey.class);
        if (tableIdValidationPrecondition(tableKey, addDataObj)) {
            final AddFlowInputBuilder builder = new AddFlowInputBuilder(addDataObj);

            builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
            builder.setFlowRef(new FlowRef(identifier));
            builder.setFlowTable(new FlowTableRef(nodeIdent.child(Table.class, tableKey)));
            builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
            future = provider.getSalFlowService().addFlow(builder.build());
        } else {
            future = Futures.<RpcResult<AddFlowOutput>>immediateFuture(null);
        }

        return future;
    }

    @Override
    public void createStaleMarkEntity(InstanceIdentifier<Flow> identifier, Flow del, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Creating Stale-Mark entry for the switch {} for flow {} ", nodeIdent.toString(), del.toString());

        StaleFlow staleFlow = makeStaleFlow(identifier, del, nodeIdent);
        persistStaleFlow(staleFlow, nodeIdent);

    }



    @Override
    protected InstanceIdentifier<Flow> getWildCardPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class).child(Table.class).child(Flow.class);
    }

    private static boolean tableIdValidationPrecondition (final TableKey tableKey, final Flow flow) {
        Preconditions.checkNotNull(tableKey, "TableKey can not be null or empty!");
        Preconditions.checkNotNull(flow, "Flow can not be null or empty!");
        if (! tableKey.getId().equals(flow.getTableId())) {
            LOG.warn("TableID in URI tableId={} and in palyload tableId={} is not same.",
                    flow.getTableId(), tableKey.getId());
            return false;
        }
        return true;
    }

    private StaleFlow makeStaleFlow(InstanceIdentifier<Flow> identifier, Flow del, InstanceIdentifier<FlowCapableNode> nodeIdent){
         StaleFlowBuilder staleFlowBuilder = new StaleFlowBuilder(del);
        return staleFlowBuilder.setId(del.getId()).build();
    }

    private void persistStaleFlow(StaleFlow staleFlow, InstanceIdentifier<FlowCapableNode> nodeIdent){
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, getStaleFlowInstanceIdentifier(staleFlow, nodeIdent), staleFlow, false);

        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        handleStaleFlowResultFuture(submitFuture);
    }

    private void handleStaleFlowResultFuture(CheckedFuture<Void, TransactionCommitFailedException> submitFuture) {
        Futures.addCallback(submitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LOG.debug("Stale Flow creation success");
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("Stale Flow creation failed {}", t);
            }
        });

    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow> getStaleFlowInstanceIdentifier(StaleFlow staleFlow, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent
                .child(Table.class, new TableKey(staleFlow.getTableId()))
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow.class,
                        new StaleFlowKey(new FlowId(staleFlow.getId())));
    }
}

