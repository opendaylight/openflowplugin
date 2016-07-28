/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.applications.frsync.ForwardingRulesCommitter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link ForwardingRulesCommitter} methods for processing add, update and remove of {@link Group}.
 */
public class GroupForwarder implements ForwardingRulesCommitter<Group, AddGroupOutput, RemoveGroupOutput, UpdateGroupOutput> {

    private static final Logger LOG = LoggerFactory.getLogger(GroupForwarder.class);
    private final SalGroupService salGroupService;

    public GroupForwarder(SalGroupService salGroupService) {
        this.salGroupService = salGroupService;
    }

    @Override
    public Future<RpcResult<RemoveGroupOutput>> remove(final InstanceIdentifier<Group> identifier, final Group removeDataObj,
                                                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Forwarding Table REMOVE request [Tbl id, node Id {} {}",
                identifier, nodeIdent);

        final RemoveGroupInputBuilder builder = new RemoveGroupInputBuilder(removeDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setGroupRef(new GroupRef(identifier));
        // fix group removal - no buckets allowed
        builder.setBuckets(null);
        return salGroupService.removeGroup(builder.build());
    }

    @Override
    public Future<RpcResult<UpdateGroupOutput>> update(final InstanceIdentifier<Group> identifier,
                                                       final Group original, final Group update,
                                                       final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Forwarding Group UPDATE request [Tbl id, node Id {} {} {}",
                identifier, nodeIdent, update);

        final UpdateGroupInputBuilder builder = new UpdateGroupInputBuilder();

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setGroupRef(new GroupRef(identifier));
        builder.setUpdatedGroup((new UpdatedGroupBuilder(update)).build());
        builder.setOriginalGroup((new OriginalGroupBuilder(original)).build());

        return salGroupService.updateGroup(builder.build());
    }

    @Override
    public Future<RpcResult<AddGroupOutput>> add(final InstanceIdentifier<Group> identifier, final Group addDataObj,
                                                 final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        LOG.trace("Forwarding Group ADD request [Tbl id, node Id {} {} {}",
                identifier, nodeIdent, addDataObj);

        final AddGroupInputBuilder builder = new AddGroupInputBuilder(addDataObj);

        builder.setNode(new NodeRef(nodeIdent.firstIdentifierOf(Node.class)));
        builder.setGroupRef(new GroupRef(identifier));
        return salGroupService.addGroup(builder.build());
    }

}
