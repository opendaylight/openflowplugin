/**
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.infrautils.utils.concurrent.JdkFutures;
import org.opendaylight.openflowplugin.applications.frm.ActionType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FlowForwarder It implements
 * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener}
 * for WildCardedPath to {@link Flow} and ForwardingRulesCommiter interface for
 * methods: add, update and remove {@link Flow} processing for
 * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}.
 */
public class FlowForwarder extends AbstractListeningCommiter<Flow> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowForwarder.class);

    private final DataBroker dataBroker;
    private ListenerRegistration<FlowForwarder> listenerRegistration;

    public FlowForwarder(final ForwardingRulesManager manager, final DataBroker db) {
        super(manager);
        dataBroker = Preconditions.checkNotNull(db, "DataBroker can not be null!");
        registrationListener(db);
    }

    @SuppressWarnings("IllegalCatch")
    private void registrationListener(final DataBroker db) {
        final DataTreeIdentifier<Flow> treeId = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION,
                getWildCardPath());
        try {
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
            listenerRegistration = looper
                    .loopUntilNoException(() -> db.registerDataTreeChangeListener(treeId, FlowForwarder.this));
        } catch (final Exception e) {
            LOG.warn("FRM Flow DataTreeChange listener registration fail!");
            LOG.debug("FRM Flow DataTreeChange listener registration fail ..", e);
            throw new IllegalStateException("FlowForwarder startup fail! System needs restart.", e);
        }
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
    }

    @Override
    public void remove(final InstanceIdentifier<Flow> identifier, final Flow removeDataObj,
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
            builder.setTransactionUri(new Uri(provider.getNewTransactionId())).setStrict(Boolean.TRUE);
            final Future<RpcResult<RemoveFlowOutput>> resultFuture =
                    provider.getSalFlowService().removeFlow(builder.build());
            JdkFutures.addErrorLogging(resultFuture, LOG, "removeFlow");
        }
    }

    // TODO: Pull this into ForwardingRulesCommiter and override it here

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeWithResult(final InstanceIdentifier<Flow> identifier,
            final Flow removeDataObj, final InstanceIdentifier<FlowCapableNode> nodeIdent) {

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
            builder.setTransactionUri(new Uri(provider.getNewTransactionId())).setStrict(Boolean.TRUE);
            resultFuture = provider.getSalFlowService().removeFlow(builder.build());
        }

        return resultFuture;
    }

    @Override
    public void update(final InstanceIdentifier<Flow> identifier, final Flow original, final Flow update,
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
            builder.setUpdatedFlow(new UpdatedFlowBuilder(update).setStrict(Boolean.TRUE).build());
            builder.setOriginalFlow(new OriginalFlowBuilder(original).setStrict(Boolean.TRUE).build());

            nodeConfigurator.enqueueJob(nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue(), () -> {
                Long groupId = isFlowDepenendsOnGroup(update);
                ListenableFuture<RpcResult<UpdateFlowOutput>> future = Futures.immediateFuture(null);
                if (groupId != null) {
                    if (isGroupExistsOnDevice(nodeIdent, groupId)) {
                        future = provider.getSalFlowService().updateFlow(builder.build());
                        JdkFutures.addErrorLogging(future, LOG, "updateFlow");
                    } else {
                        Future<? extends RpcResult<?>> groupFuture = pushDependentGroup(nodeIdent, groupId);
                        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(groupFuture),
                                new UpdateFlowCallBack(builder.build(), future),
                                MoreExecutors.directExecutor());
                    }
                } else {
                    future = provider.getSalFlowService().updateFlow(builder.build());
                    JdkFutures.addErrorLogging(future, LOG, "updateFlow");
                }
                return future;
            });
        }
    }

    @Override
    public Future<RpcResult<AddFlowOutput>> add(final InstanceIdentifier<Flow> identifier, final Flow addDataObj,
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final TableKey tableKey = identifier.firstKeyOf(Table.class, TableKey.class);
        if (tableIdValidationPrecondition(tableKey, addDataObj)) {
            final AddFlowInputBuilder builder = new AddFlowInputBuilder(addDataObj);

            builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
            builder.setFlowRef(new FlowRef(identifier));
            builder.setFlowTable(new FlowTableRef(nodeIdent.child(Table.class, tableKey)));
            builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
            nodeConfigurator.enqueueJob(nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId().getValue(), () -> {
                Long groupId = isFlowDepenendsOnGroup(addDataObj);
                ListenableFuture<RpcResult<AddFlowOutput>> future = SettableFuture.create();
                if (groupId != null) {
                    if (isGroupExistsOnDevice(nodeIdent, groupId)) {
                        future = provider.getSalFlowService().addFlow(builder.build());
                    } else {
                        Future<? extends RpcResult<?>> groupFuture = pushDependentGroup(nodeIdent, groupId);
                        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(groupFuture),
                                new AddFlowCallBack(builder.build(), future), MoreExecutors.directExecutor());
                    }
                } else {
                    future = provider.getSalFlowService().addFlow(builder.build());
                }
                return future;
            });

        }
        return Futures.immediateFuture(null);
    }

    @Override
    public void createStaleMarkEntity(InstanceIdentifier<Flow> identifier, Flow del,
            InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Creating Stale-Mark entry for the switch {} for flow {} ", nodeIdent.toString(), del.toString());

        StaleFlow staleFlow = makeStaleFlow(identifier, del, nodeIdent);
        persistStaleFlow(staleFlow, nodeIdent);

    }

    @Override
    protected InstanceIdentifier<Flow> getWildCardPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class)
                .child(Table.class).child(Flow.class);
    }

    private static boolean tableIdValidationPrecondition(final TableKey tableKey, final Flow flow) {
        Preconditions.checkNotNull(tableKey, "TableKey can not be null or empty!");
        Preconditions.checkNotNull(flow, "Flow can not be null or empty!");
        if (!tableKey.getId().equals(flow.getTableId())) {
            LOG.warn("TableID in URI tableId={} and in palyload tableId={} is not same.", flow.getTableId(),
                    tableKey.getId());
            return false;
        }
        return true;
    }

    private StaleFlow makeStaleFlow(InstanceIdentifier<Flow> identifier, Flow del,
            InstanceIdentifier<FlowCapableNode> nodeIdent) {
        StaleFlowBuilder staleFlowBuilder = new StaleFlowBuilder(del);
        return staleFlowBuilder.setId(del.getId()).build();
    }

    private void persistStaleFlow(StaleFlow staleFlow, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, getStaleFlowInstanceIdentifier(staleFlow, nodeIdent),
                staleFlow, false);

        ListenableFuture<Void> submitFuture = writeTransaction.submit();
        handleStaleFlowResultFuture(submitFuture);
    }

    private void handleStaleFlowResultFuture(ListenableFuture<Void> submitFuture) {
        Futures.addCallback(submitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LOG.debug("Stale Flow creation success");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Stale Flow creation failed {}", throwable);
            }
        }, MoreExecutors.directExecutor());

    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight
        .flow.inventory.rev130819.tables.table.StaleFlow> getStaleFlowInstanceIdentifier(
            StaleFlow staleFlow, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.child(Table.class, new TableKey(staleFlow.getTableId())).child(
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow.class,
                new StaleFlowKey(new FlowId(staleFlow.getId())));
    }

    private Future<? extends RpcResult<?>> pushDependentGroup(final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final Long groupId) {
        InstanceIdentifier<Group> groupIdent = buildGroupInstanceIdentifier(nodeIdent, groupId);
        Future<RpcResult<AddGroupOutput>> resultFuture = Futures.immediateFuture(null);
        LOG.info("Reading the group from config inventory: {}", groupId);
        try {
            Optional<Group> group;
            group = provider.getReadTranaction().read(LogicalDatastoreType.CONFIGURATION, groupIdent).get();
            if (group.isPresent()) {
                //  return provider.getGroupCommiter().add(groupIdent, group.get(), nodeIdent);
                final AddGroupInputBuilder builder = new AddGroupInputBuilder(group.get());
                builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
                builder.setGroupRef(new GroupRef(nodeIdent));
                builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
                AddGroupInput addGroupInput = builder.build();
                resultFuture = this.provider.getSalGroupService().addGroup(addGroupInput);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error while reading group from config datastore for the group ID {}", groupId, e);
        }
        return resultFuture;
    }

    private Long isFlowDepenendsOnGroup(final Flow flow) {
        LOG.info("The flow in isFlowDepenendsOnGroup is {}", flow);
        if (flow.getInstructions() != null) {
            List<Instruction> instructions = flow.getInstructions().getInstruction();
            for (Instruction instruction : instructions) {
                List<Action> actions = Collections.emptyList();
                if (instruction.getInstruction().getImplementedInterface().getClass()
                        .equals(ActionType.APPLY_ACTION.getActionType())) {
                    actions = ((ApplyActionsCase) (instruction.getInstruction())).getApplyActions().getAction();
                }
                for (Action action : actions) {
                    if (action.getAction().getImplementedInterface().getClass()
                            .equals(ActionType.GROUP_ACTION.getActionType())) {
                        return ((GroupActionCase) action.getAction()).getGroupAction().getGroupId();
                    }
                }
            }
        }
        return null;
    }

    private boolean isGroupExistsOnDevice(final InstanceIdentifier<FlowCapableNode> nodeIdent, final Long groupId) {
        NodeId nodeId = nodeIdent.firstIdentifierOf(Node.class).firstKeyOf(Node.class, NodeKey.class).getId();
        return provider.getDevicesGroupRegistry().isGroupExits(nodeId, groupId);
    }

    private InstanceIdentifier<Group> buildGroupInstanceIdentifier(final InstanceIdentifier<FlowCapableNode> nodeIdent,
            final Long groupId) {
        NodeId nodeId = nodeIdent.firstIdentifierOf(Node.class).firstKeyOf(Node.class, NodeKey.class).getId();
        InstanceIdentifier<Group> groupInstanceId = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(new GroupId(groupId))).build();
        return groupInstanceId;
    }

    private final class AddFlowCallBack implements FutureCallback<RpcResult<?>> {
        private final AddFlowInput addFlowInput;
        private Future<RpcResult<AddFlowOutput>> future;

        private AddFlowCallBack(final AddFlowInput addFlowInput, Future<RpcResult<AddFlowOutput>> future) {
            this.addFlowInput = addFlowInput;
            this.future = future;
        }

        @Override
        public void onSuccess(RpcResult<?> rpcResult) {
            future  = provider.getSalFlowService().addFlow(addFlowInput);
        }

        @Override
        public void onFailure(Throwable throwable) {

        }
    }

    private final class UpdateFlowCallBack implements FutureCallback<RpcResult<?>> {
        private final UpdateFlowInput updateFlowInput;
        private Future<RpcResult<UpdateFlowOutput>> future;

        private UpdateFlowCallBack(final UpdateFlowInput updateFlowInput, Future<RpcResult<UpdateFlowOutput>> future) {
            this.updateFlowInput = updateFlowInput;
            this.future = future;
        }

        @Override
        public void onSuccess(RpcResult<?> rpcResult) {
            future  = provider.getSalFlowService().updateFlow(updateFlowInput);
        }

        @Override
        public void onFailure(Throwable throwable) {

        }
    }
}
