/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.buildGroupInstanceIdentifier;
import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.getActiveBundle;
import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.getFlowId;
import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.getNodeIdValueFromNodeIdentifier;
import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.isFlowDependentOnGroup;
import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.isGroupExistsOnDevice;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FlowForwarder It implements
 * {@link org.opendaylight.mdsal.binding.api.DataTreeChangeListener}
 * for WildCardedPath to {@link Flow} and ForwardingRulesCommiter interface for
 * methods: add, update and remove {@link Flow} processing for
 * {@link org.opendaylight.mdsal.binding.api.DataTreeModification}.
 */
public class FlowForwarder extends AbstractListeningCommiter<Flow> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowForwarder.class);

    private static final String GROUP_EXISTS_IN_DEVICE_ERROR = "GROUPEXISTS";

    private ListenerRegistration<FlowForwarder> listenerRegistration;

    private final BundleFlowForwarder bundleFlowForwarder;

    public FlowForwarder(final ForwardingRulesManager manager, final DataBroker db,
                         final ListenerRegistrationHelper registrationHelper) {
        super(manager, db, registrationHelper);
        bundleFlowForwarder = new BundleFlowForwarder(manager);
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
    public void remove(final InstanceIdentifier<Flow> identifier, final Flow removeDataObj,
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final TableKey tableKey = identifier.firstKeyOf(Table.class);
        if (tableIdValidationPrecondition(tableKey, removeDataObj)) {
            BundleId bundleId = getActiveBundle(nodeIdent, provider);
            if (bundleId != null) {
                provider.getBundleFlowListener().remove(identifier, removeDataObj, nodeIdent, bundleId);
            } else {
                final String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
                nodeConfigurator.enqueueJob(nodeId, () -> {
                    final RemoveFlowInputBuilder builder = new RemoveFlowInputBuilder(removeDataObj);
                    builder.setFlowRef(new FlowRef(identifier));
                    builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
                    builder.setFlowTable(new FlowTableRef(nodeIdent.child(Table.class, tableKey)));

                    // This method is called only when a given flow object has been
                    // removed from datastore. So FRM always needs to set strict flag
                    // into remove-flow input so that only a flow entry associated with
                    // a given flow object is removed.
                    builder.setTransactionUri(new Uri(provider.getNewTransactionId())).setStrict(Boolean.TRUE);
                    final ListenableFuture<RpcResult<RemoveFlowOutput>> resultFuture =
                            provider.getSalFlowService().removeFlow(builder.build());
                    LoggingFutures.addErrorLogging(resultFuture, LOG, "removeFlow");
                    return resultFuture;
                });
            }
        }
    }

    // TODO: Pull this into ForwardingRulesCommiter and override it here

    @Override
    public ListenableFuture<RpcResult<RemoveFlowOutput>> removeWithResult(final InstanceIdentifier<Flow> identifier,
            final Flow removeDataObj, final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        ListenableFuture<RpcResult<RemoveFlowOutput>> resultFuture = SettableFuture.create();
        final TableKey tableKey = identifier.firstKeyOf(Table.class);
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

        final TableKey tableKey = identifier.firstKeyOf(Table.class);
        if (tableIdValidationPrecondition(tableKey, update)) {
            BundleId bundleId = getActiveBundle(nodeIdent, provider);
            if (bundleId != null) {
                provider.getBundleFlowListener().update(identifier, original, update, nodeIdent, bundleId);
            } else {
                final String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
                nodeConfigurator.enqueueJob(nodeId, () -> {
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

                    Uint32 groupId = isFlowDependentOnGroup(update);
                    if (groupId != null) {
                        LOG.trace("The flow {} is dependent on group {}. Checking if the group is already present",
                                getFlowId(identifier), groupId);
                        if (isGroupExistsOnDevice(nodeIdent, groupId, provider)) {
                            LOG.trace("The dependent group {} is already programmed. Updating the flow {}", groupId,
                                    getFlowId(identifier));
                            return provider.getSalFlowService().updateFlow(builder.build());
                        } else {
                            LOG.trace("The dependent group {} isn't programmed yet. Pushing the group", groupId);
                            ListenableFuture<RpcResult<AddGroupOutput>> groupFuture = pushDependentGroup(nodeIdent,
                                    groupId);
                            SettableFuture<RpcResult<UpdateFlowOutput>> resultFuture = SettableFuture.create();
                            Futures.addCallback(groupFuture,
                                    new UpdateFlowCallBack(builder.build(), nodeId, resultFuture, groupId),
                                    MoreExecutors.directExecutor());
                            return resultFuture;
                        }
                    }

                    LOG.trace("The flow {} is not dependent on any group. Updating the flow",
                            getFlowId(identifier));
                    return provider.getSalFlowService().updateFlow(builder.build());
                });
            }
        }
    }

    @Override
    public Future<? extends RpcResult<?>> add(final InstanceIdentifier<Flow> identifier, final Flow addDataObj,
            final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final TableKey tableKey = identifier.firstKeyOf(Table.class);
        if (tableIdValidationPrecondition(tableKey, addDataObj)) {
            BundleId bundleId = getActiveBundle(nodeIdent, provider);
            if (bundleId != null) {
                return provider.getBundleFlowListener().add(identifier, addDataObj, nodeIdent, bundleId);
            } else {
                final String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
                nodeConfigurator.enqueueJob(nodeId, () -> {
                    final AddFlowInputBuilder builder = new AddFlowInputBuilder(addDataObj);

                    builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
                    builder.setFlowRef(new FlowRef(identifier));
                    builder.setFlowTable(new FlowTableRef(nodeIdent.child(Table.class, tableKey)));
                    builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
                    Uint32 groupId = isFlowDependentOnGroup(addDataObj);
                    if (groupId != null) {
                        LOG.trace("The flow {} is dependent on group {}. Checking if the group is already present",
                                getFlowId(new FlowRef(identifier)), groupId);
                        if (isGroupExistsOnDevice(nodeIdent, groupId, provider)) {
                            LOG.trace("The dependent group {} is already programmed. Adding the flow {}", groupId,
                                    getFlowId(new FlowRef(identifier)));
                            return provider.getSalFlowService().addFlow(builder.build());
                        } else {
                            LOG.trace("The dependent group {} isn't programmed yet. Pushing the group", groupId);
                            ListenableFuture<RpcResult<AddGroupOutput>> groupFuture = pushDependentGroup(nodeIdent,
                                    groupId);
                            SettableFuture<RpcResult<AddFlowOutput>> resultFuture = SettableFuture.create();
                            Futures.addCallback(groupFuture, new AddFlowCallBack(builder.build(), nodeId, groupId,
                                    resultFuture), MoreExecutors.directExecutor());
                            return resultFuture;
                        }
                    }

                    LOG.trace("The flow {} is not dependent on any group. Adding the flow",
                            getFlowId(new FlowRef(identifier)));
                    return provider.getSalFlowService().addFlow(builder.build());
                });
            }
        }
        return Futures.immediateFuture(null);
    }

    @Override
    public void createStaleMarkEntity(InstanceIdentifier<Flow> identifier, Flow del,
            InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Creating Stale-Mark entry for the switch {} for flow {} ", nodeIdent, del);
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

    private static StaleFlow makeStaleFlow(InstanceIdentifier<Flow> identifier, Flow del,
            InstanceIdentifier<FlowCapableNode> nodeIdent) {
        StaleFlowBuilder staleFlowBuilder = new StaleFlowBuilder(del);
        return staleFlowBuilder.setId(del.getId()).build();
    }

    private void persistStaleFlow(StaleFlow staleFlow, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, getStaleFlowInstanceIdentifier(staleFlow, nodeIdent),
                staleFlow);

        FluentFuture<?> submitFuture = writeTransaction.commit();
        handleStaleFlowResultFuture(submitFuture);
    }

    private static void handleStaleFlowResultFuture(FluentFuture<?> submitFuture) {
        submitFuture.addCallback(new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LOG.debug("Stale Flow creation success");
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Stale Flow creation failed", throwable);
            }
        }, MoreExecutors.directExecutor());

    }

    private static InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight
        .flow.inventory.rev130819.tables.table.StaleFlow> getStaleFlowInstanceIdentifier(
            StaleFlow staleFlow, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.child(Table.class, new TableKey(staleFlow.getTableId())).child(
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.StaleFlow.class,
                new StaleFlowKey(new FlowId(staleFlow.getId())));
    }

    private ListenableFuture<RpcResult<AddGroupOutput>> pushDependentGroup(
            final InstanceIdentifier<FlowCapableNode> nodeIdent, final Uint32 groupId) {

        //TODO This read to the DS might have a performance impact.
        //if the dependent group is not installed than we should just cache the parent group,
        //till we receive the dependent group DTCN and then push it.

        InstanceIdentifier<Group> groupIdent = buildGroupInstanceIdentifier(nodeIdent, groupId);
        ListenableFuture<RpcResult<AddGroupOutput>> resultFuture;
        LOG.info("Reading the group from config inventory: {}", groupId);
        try (ReadTransaction readTransaction = provider.getReadTransaction()) {
            Optional<Group> group = readTransaction.read(LogicalDatastoreType.CONFIGURATION, groupIdent).get();
            if (group.isPresent()) {
                final AddGroupInputBuilder builder = new AddGroupInputBuilder(group.get());
                builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
                builder.setGroupRef(new GroupRef(nodeIdent));
                builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
                AddGroupInput addGroupInput = builder.build();
                resultFuture = this.provider.getSalGroupService().addGroup(addGroupInput);
            } else {
                resultFuture = Futures.immediateFuture(RpcResultBuilder.<AddGroupOutput>failed()
                        .withError(RpcError.ErrorType.APPLICATION,
                                "Group " + groupId + " not present in the config inventory").build());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error while reading group from config datastore for the group ID {}", groupId, e);
            resultFuture = Futures.immediateFuture(RpcResultBuilder.<AddGroupOutput>failed()
                    .withError(RpcError.ErrorType.APPLICATION,
                            "Error while reading group " + groupId + " from inventory").build());
        }
        return resultFuture;
    }

    private final class AddFlowCallBack implements FutureCallback<RpcResult<AddGroupOutput>> {
        private final AddFlowInput addFlowInput;
        private final String nodeId;
        private final Uint32 groupId;
        private final SettableFuture<RpcResult<AddFlowOutput>> resultFuture;

        private AddFlowCallBack(final AddFlowInput addFlowInput, final String nodeId, Uint32 groupId,
                SettableFuture<RpcResult<AddFlowOutput>> resultFuture) {
            this.addFlowInput = addFlowInput;
            this.nodeId = nodeId;
            this.groupId = groupId;
            this.resultFuture = resultFuture;
        }

        @Override
        public void onSuccess(RpcResult<AddGroupOutput> rpcResult) {
            if (rpcResult.isSuccessful() || rpcResult.getErrors().size() == 1
                    && rpcResult.getErrors().iterator().next().getMessage().contains(GROUP_EXISTS_IN_DEVICE_ERROR)) {
                provider.getDevicesGroupRegistry().storeGroup(nodeId, groupId);
                Futures.addCallback(provider.getSalFlowService().addFlow(addFlowInput),
                    new FutureCallback<RpcResult<AddFlowOutput>>() {
                        @Override
                        public void onSuccess(RpcResult<AddFlowOutput> result) {
                            resultFuture.set(result);
                        }

                        @Override
                        public void onFailure(Throwable failure) {
                            resultFuture.setException(failure);
                        }
                    },  MoreExecutors.directExecutor());

                LOG.debug("Flow add with id {} finished without error for node {}",
                        getFlowId(addFlowInput.getFlowRef()), nodeId);
            } else {
                LOG.error("Flow add with id {} failed for node {} with error {}", getFlowId(addFlowInput.getFlowRef()),
                        nodeId, rpcResult.getErrors());
                resultFuture.set(RpcResultBuilder.<AddFlowOutput>failed()
                        .withRpcErrors(rpcResult.getErrors()).build());
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            LOG.error("Service call for adding flow with id {} failed for node {}",
                    getFlowId(addFlowInput.getFlowRef()), nodeId, throwable);
            resultFuture.setException(throwable);
        }
    }

    private final class UpdateFlowCallBack implements FutureCallback<RpcResult<AddGroupOutput>> {
        private final UpdateFlowInput updateFlowInput;
        private final String nodeId;
        private final Uint32 groupId;
        private final SettableFuture<RpcResult<UpdateFlowOutput>> resultFuture;

        private UpdateFlowCallBack(final UpdateFlowInput updateFlowInput, final String nodeId,
                SettableFuture<RpcResult<UpdateFlowOutput>> resultFuture, Uint32 groupId) {
            this.updateFlowInput = updateFlowInput;
            this.nodeId = nodeId;
            this.groupId = groupId;
            this.resultFuture = resultFuture;
        }

        @Override
        public void onSuccess(RpcResult<AddGroupOutput> rpcResult) {
            if (rpcResult.isSuccessful() || rpcResult.getErrors().size() == 1
                    && rpcResult.getErrors().iterator().next().getMessage().contains(GROUP_EXISTS_IN_DEVICE_ERROR)) {
                provider.getDevicesGroupRegistry().storeGroup(nodeId, groupId);
                Futures.addCallback(provider.getSalFlowService().updateFlow(updateFlowInput),
                    new FutureCallback<RpcResult<UpdateFlowOutput>>() {
                        @Override
                        public void onSuccess(RpcResult<UpdateFlowOutput> result) {
                            resultFuture.set(result);
                        }

                        @Override
                        public void onFailure(Throwable failure) {
                            resultFuture.setException(failure);
                        }
                    },  MoreExecutors.directExecutor());

                LOG.debug("Flow update with id {} finished without error for node {}",
                        getFlowId(updateFlowInput.getFlowRef()), nodeId);
            } else {
                LOG.error("Flow update with id {} failed for node {} with error {}",
                        getFlowId(updateFlowInput.getFlowRef()), nodeId, rpcResult.getErrors());
                resultFuture.set(RpcResultBuilder.<UpdateFlowOutput>failed()
                        .withRpcErrors(rpcResult.getErrors()).build());
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            LOG.error("Service call for updating flow with id {} failed for node {}",
                    getFlowId(updateFlowInput.getFlowRef()), nodeId, throwable);
            resultFuture.setException(throwable);
        }
    }
}