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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
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
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupForwarder
 * It implements {@link org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener}
 * for WildCardedPath to {@link Group} and ForwardingRulesCommiter interface for methods:
 * add, update and remove {@link Group} processing for
 * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}.
 */
public class GroupForwarder extends AbstractListeningCommiter<Group> {

    private static final Logger LOG = LoggerFactory.getLogger(GroupForwarder.class);
    private final DataBroker dataBroker;
    private ListenerRegistration<GroupForwarder> listenerRegistration;

    public GroupForwarder (final ForwardingRulesManager manager, final DataBroker db) {
        super(manager, Group.class);
        dataBroker = Preconditions.checkNotNull(db, "DataBroker can not be null!");
        final DataTreeIdentifier<Group> treeId = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, getWildCardPath());

        try {
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
            listenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<GroupForwarder>>() {
                @Override
                public ListenerRegistration<GroupForwarder> call() throws Exception {
                    return db.registerDataTreeChangeListener(treeId, GroupForwarder.this);
                }
            });
        } catch (final Exception e) {
            LOG.warn("FRM Group DataTreeChange listener registration fail!");
            LOG.debug("FRM Group DataTreeChange listener registration fail ..", e);
            throw new IllegalStateException("GroupForwarder startup fail! System needs restart.", e);
        }
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            try {
                listenerRegistration.close();
            } catch (Exception e) {
                LOG.warn("Error by stop FRM GroupChangeListener: {}", e.getMessage());
                LOG.debug("Error by stop FRM GroupChangeListener..", e);
            }
            listenerRegistration = null;
        }
    }

    @Override
    protected InstanceIdentifier<Group> getWildCardPath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class).child(Group.class);
    }

    @Override
    public void remove(final InstanceIdentifier<Group> identifier, final Group removeDataObj,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final Group group = (removeDataObj);
        final RemoveGroupInputBuilder builder = new RemoveGroupInputBuilder(group);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setGroupRef(new GroupRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        this.provider.getSalGroupService().removeGroup(builder.build());
    }

    //TODO: Pull this into ForwardingRulesCommiter and override it here
    @Override
    public Future<RpcResult<RemoveGroupOutput>> removeWithResult(final InstanceIdentifier<Group> identifier, final Group removeDataObj,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final Group group = (removeDataObj);
        final RemoveGroupInputBuilder builder = new RemoveGroupInputBuilder(group);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setGroupRef(new GroupRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        return this.provider.getSalGroupService().removeGroup(builder.build());
    }

    @Override
    public void update(final InstanceIdentifier<Group> identifier,
                       final Group original, final Group update,
                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final Group originalGroup = (original);
        final Group updatedGroup = (update);
        final UpdateGroupInputBuilder builder = new UpdateGroupInputBuilder();

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setGroupRef(new GroupRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        builder.setUpdatedGroup((new UpdatedGroupBuilder(updatedGroup)).build());
        builder.setOriginalGroup((new OriginalGroupBuilder(originalGroup)).build());

        this.provider.getSalGroupService().updateGroup(builder.build());
    }

    @Override
    public Future<RpcResult<AddGroupOutput>> add(
        final InstanceIdentifier<Group> identifier, final Group addDataObj,
        final InstanceIdentifier<FlowCapableNode> nodeIdent) {

        final Group group = (addDataObj);
        final AddGroupInputBuilder builder = new AddGroupInputBuilder(group);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setGroupRef(new GroupRef(identifier));
        builder.setTransactionUri(new Uri(provider.getNewTransactionId()));
        return this.provider.getSalGroupService().addGroup(builder.build());
    }

    @Override
    public void createStaleMarkEntity(InstanceIdentifier<Group> identifier, Group del, InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.debug("Creating Stale-Mark entry for the switch {} for Group {} ", nodeIdent.toString(), del.toString());
        StaleGroup staleGroup = makeStaleGroup(identifier, del, nodeIdent);
        persistStaleGroup(staleGroup, nodeIdent);

    }


    private StaleGroup makeStaleGroup(InstanceIdentifier<Group> identifier, Group del, InstanceIdentifier<FlowCapableNode> nodeIdent){
        StaleGroupBuilder staleGroupBuilder = new StaleGroupBuilder(del);
        return staleGroupBuilder.setGroupId(del.getGroupId()).build();
    }

    private void persistStaleGroup(StaleGroup staleGroup, InstanceIdentifier<FlowCapableNode> nodeIdent){
        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, getStaleGroupInstanceIdentifier(staleGroup, nodeIdent), staleGroup, false);

        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTransaction.submit();
        handleStaleGroupResultFuture(submitFuture);
    }

    private void handleStaleGroupResultFuture(CheckedFuture<Void, TransactionCommitFailedException> submitFuture) {
        Futures.addCallback(submitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LOG.debug("Stale Group creation success");
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("Stale Group creation failed {}", t);
            }
        });

    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.StaleGroup> getStaleGroupInstanceIdentifier(StaleGroup staleGroup, InstanceIdentifier<FlowCapableNode> nodeIdent) {
            return nodeIdent
                .child(StaleGroup.class, new StaleGroupKey(new GroupId(staleGroup.getGroupId())));
    }

}

