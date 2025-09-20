/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.openflowplugin.applications.frsync.ForwardingRulesCommitter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link ForwardingRulesCommitter} methods for processing add, update and remove of {@link Group}.
 */
public class GroupForwarder
        implements ForwardingRulesCommitter<Group, AddGroupOutput, RemoveGroupOutput, UpdateGroupOutput> {
    private static final Logger LOG = LoggerFactory.getLogger(GroupForwarder.class);

    private final AddGroup addGroupRpc;
    private final UpdateGroup updateGroupRpc;
    private final RemoveGroup removeGroupRpc;

    public GroupForwarder(final RpcService rpcConsumerRegistry) {
        addGroupRpc = rpcConsumerRegistry.getRpc(AddGroup.class);
        updateGroupRpc = rpcConsumerRegistry.getRpc(UpdateGroup.class);
        removeGroupRpc = rpcConsumerRegistry.getRpc(RemoveGroup.class);
    }

    @Override
    public ListenableFuture<RpcResult<RemoveGroupOutput>> remove(final DataObjectIdentifier<Group> identifier,
            final Group removeDataObj, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Forwarding Table REMOVE request [Tbl id, node Id {} {}", identifier, nodeIdent);
        return removeGroupRpc.invoke(new RemoveGroupInputBuilder(removeDataObj)
            .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
            .setGroupRef(new GroupRef(identifier))
            // fix group removal - no buckets allowed
            .setBuckets(null).build());
    }

    @Override
    public ListenableFuture<RpcResult<UpdateGroupOutput>> update(final DataObjectIdentifier<Group> identifier,
            final Group original, final Group update, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Forwarding Group UPDATE request [Tbl id, node Id {} {} {}", identifier, nodeIdent, update);
        return updateGroupRpc.invoke(new UpdateGroupInputBuilder()
            .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
            .setGroupRef(new GroupRef(identifier))
            .setUpdatedGroup(new UpdatedGroupBuilder(update).build())
            .setOriginalGroup(new OriginalGroupBuilder(original).build())
            .build());
    }

    @Override
    public ListenableFuture<RpcResult<AddGroupOutput>> add(final DataObjectIdentifier<Group> identifier,
            final Group addDataObj, final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Forwarding Group ADD request [Tbl id, node Id {} {} {}", identifier, nodeIdent, addDataObj);
        return addGroupRpc.invoke(new AddGroupInputBuilder(addDataObj)
            .setNode(new NodeRef(nodeIdent.trimTo(Node.class)))
            .setGroupRef(new GroupRef(identifier))
            .build());
    }
}
