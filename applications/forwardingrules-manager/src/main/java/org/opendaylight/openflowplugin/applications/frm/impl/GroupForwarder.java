/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.getActiveBundle;
import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.getNodeIdValueFromNodeIdentifier;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupForwarder It implements
 * {@link org.opendaylight.mdsal.binding.api.DataTreeChangeListener}
 * for WildCardedPath to {@link Group} and ForwardingRulesCommiter interface for
 * methods: add, update and remove {@link Group} processing for
 * {@link org.opendaylight.mdsal.binding.api.DataTreeModification}.
 */
public class GroupForwarder extends AbstractListeningCommiter<Group> {
    private static final Logger LOG = LoggerFactory.getLogger(GroupForwarder.class);

    public GroupForwarder(final ForwardingRulesManager manager, final DataBroker dataBroker) {
        super(manager, dataBroker);
    }

    @Override
    protected DataObjectReference<Group> getWildCardPath() {
        return DataObjectReference.builder(Nodes.class)
            .child(Node.class)
            .augmentation(FlowCapableNode.class)
            .child(Group.class)
            .build();
    }

    @Override
    public void remove(final DataObjectIdentifier<Group> identifier, final Group removeDataObj,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        BundleId bundleId = getActiveBundle(nodeIdent, provider);
        if (bundleId != null) {
            provider.getBundleGroupListener().remove(identifier, removeDataObj, nodeIdent, bundleId);
        } else {
            final String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
            nodeConfigurator.enqueueJob(nodeId, () -> {
                final var removeGroup = new RemoveGroupInputBuilder(removeDataObj)
                    .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
                    .setGroupRef(new GroupRef(identifier))
                    .setTransactionUri(new Uri(provider.getNewTransactionId()))
                    .build();

                final var resultFuture = provider.removeGroup().invoke(removeGroup);
                Futures.addCallback(resultFuture,
                    new RemoveGroupCallBack(removeDataObj.getGroupId().getValue(), nodeId),
                    MoreExecutors.directExecutor());
                return LoggingFutures.addErrorLogging(resultFuture, LOG, "removeGroup");
            });
        }
    }

    // TODO: Pull this into ForwardingRulesCommiter and override it here
    @Override
    public ListenableFuture<RpcResult<RemoveGroupOutput>> removeWithResult(final DataObjectIdentifier<Group> identifier,
            final Group removeDataObj, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        return provider.removeGroup().invoke(new RemoveGroupInputBuilder(removeDataObj)
            .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
            .setGroupRef(new GroupRef(identifier))
            .setTransactionUri(new Uri(provider.getNewTransactionId()))
            .build());
    }

    @Override
    public void update(final DataObjectIdentifier<Group> identifier, final Group original, final Group update,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        final var bundleId = getActiveBundle(nodeIdent, provider);
        if (bundleId != null) {
            provider.getBundleGroupListener().update(identifier, original, update, nodeIdent, bundleId);
            return;
        }

        final String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
        nodeConfigurator.enqueueJob(nodeId, () -> {
            final var updateGroupInput = new UpdateGroupInputBuilder()
                .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
                .setGroupRef(new GroupRef(identifier))
                .setTransactionUri(new Uri(provider.getNewTransactionId()))
                .setUpdatedGroup(new UpdatedGroupBuilder(update).build())
                .setOriginalGroup(new OriginalGroupBuilder(original).build())
                .build();
            final var resultFuture = LoggingFutures.addErrorLogging(provider.updateGroup().invoke(updateGroupInput),
                LOG, "updateGroup");
            Futures.addCallback(resultFuture,
                new UpdateGroupCallBack(updateGroupInput.getOriginalGroup().getGroupId().getValue(), nodeId),
                MoreExecutors.directExecutor());
            return resultFuture;
        });
    }

    @Override
    public ListenableFuture<? extends RpcResult<?>> add(final DataObjectIdentifier<Group> identifier,
            final Group addDataObj, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        final var bundleId = getActiveBundle(nodeIdent, provider);
        if (bundleId != null) {
            return provider.getBundleGroupListener().add(identifier, addDataObj, nodeIdent, bundleId);
        }

        final var nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
        return nodeConfigurator.enqueueJob(nodeId, () -> {
            final var addGroupInput = new AddGroupInputBuilder(addDataObj)
                .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
                .setGroupRef(new GroupRef(identifier))
                .setTransactionUri(new Uri(provider.getNewTransactionId()))
                .build();
            final var resultFuture = provider.addGroup().invoke(addGroupInput);
            Futures.addCallback(resultFuture, new AddGroupCallBack(addGroupInput.getGroupId().getValue(), nodeId),
                MoreExecutors.directExecutor());
            return resultFuture;
        });
    }

    @Override
    public void createStaleMarkEntity(final DataObjectIdentifier<Group> identifier, final Group del,
            final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Creating Stale-Mark entry for the switch {} for Group {} ", nodeIdent, del);
        final var staleGroup = new StaleGroupBuilder(del).setGroupId(del.getGroupId()).build();
        final var writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, nodeIdent.toBuilder()
            .child(StaleGroup.class, new StaleGroupKey(new GroupId(staleGroup.getGroupId())))
            .build(), staleGroup);
        writeTransaction.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo result) {
                LOG.debug("Stale Group creation success");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Stale Group creation failed", throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    private final class AddGroupCallBack implements FutureCallback<RpcResult<AddGroupOutput>> {
        private final Uint32 groupId;
        private final String nodeId;

        private AddGroupCallBack(final Uint32 groupId, final String nodeId) {
            this.groupId = groupId;
            this.nodeId = nodeId;
        }

        @Override
        public void onSuccess(final RpcResult<AddGroupOutput> result) {
            if (result.isSuccessful()) {
                provider.getDevicesGroupRegistry().storeGroup(nodeId, groupId);
                LOG.debug("Group add with id {} finished without error for node {}", groupId, nodeId);
            } else {
                LOG.debug("Group add with id {} failed for node {} with error {}", groupId, nodeId,
                        result.getErrors());
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.error("Service call for adding group {} failed for node with error {}", groupId, nodeId, throwable);
        }
    }

    private final class UpdateGroupCallBack implements FutureCallback<RpcResult<UpdateGroupOutput>> {
        private final Uint32 groupId;
        private final String nodeId;

        private UpdateGroupCallBack(final Uint32 groupId, final String nodeId) {
            this.groupId = groupId;
            this.nodeId = nodeId;
        }

        @Override
        public void onSuccess(final RpcResult<UpdateGroupOutput> result) {
            if (result.isSuccessful()) {
                provider.getDevicesGroupRegistry().storeGroup(nodeId, groupId);
                LOG.debug("Group update with id {} finished without error for node {}", groupId, nodeId);
            } else {
                LOG.debug("Group update with id {} failed for node {} with error {}", groupId, nodeId,
                        result.getErrors().toString());
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.error("Service call for updating group {} failed for node {} with", groupId, nodeId,
                    throwable);
        }
    }

    private final class RemoveGroupCallBack implements FutureCallback<RpcResult<RemoveGroupOutput>> {
        private final Uint32 groupId;
        private final String nodeId;

        private RemoveGroupCallBack(final Uint32 groupId, final String nodeId) {
            this.groupId = groupId;
            this.nodeId = nodeId;
        }

        @Override
        public void onSuccess(final RpcResult<RemoveGroupOutput> result) {
            if (result.isSuccessful()) {
                LOG.debug("Group remove with id {} finished without error for node {}", groupId, nodeId);
                provider.getDevicesGroupRegistry().removeGroup(nodeId, groupId);
            } else {
                LOG.debug("Group remove with id {} failed for node {} with error {}", groupId, nodeId,
                        result.getErrors().toString());
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            LOG.error("Service call for removing group {} failed for node with error {}", groupId, nodeId, throwable);
        }
    }
}
