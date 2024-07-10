/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.impl.util.BarrierUtil;
import org.opendaylight.openflowplugin.impl.util.GroupUtil;
import org.opendaylight.openflowplugin.impl.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AddGroupsBatchImpl implements AddGroupsBatch {
    private static final Logger LOG = LoggerFactory.getLogger(AddGroupsBatchImpl.class);

    private final AddGroup addGroup;
    private final SendBarrier sendBarrier;

    public AddGroupsBatchImpl(final AddGroup addGroup, final SendBarrier sendBarrier) {
        this.addGroup = requireNonNull(addGroup);
        this.sendBarrier = requireNonNull(sendBarrier);
    }

    @Override
    public ListenableFuture<RpcResult<AddGroupsBatchOutput>> invoke(final AddGroupsBatchInput input) {
        final var groups = input.nonnullBatchAddGroups().values();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Adding groups @ {} : {}", PathUtil.extractNodeId(input.getNode()), groups.size());
        }

        final var resultsLot = groups.stream()
            .map(group -> addGroup.invoke(new AddGroupInputBuilder(group)
                .setGroupRef(createGroupRef(input.getNode(), group))
                .setNode(input.getNode())
                .build()))
            .collect(Collectors.toList());

        final var commonResult = Futures.transform(Futures.allAsList(resultsLot),
            GroupUtil.createCumulatingFunction(groups), MoreExecutors.directExecutor());

        final var addGroupsBulkFuture = Futures.transform(commonResult, GroupUtil.GROUP_ADD_TRANSFORM,
            MoreExecutors.directExecutor());

        return input.getBarrierAfter()
            ? BarrierUtil.chainBarrier(addGroupsBulkFuture, input.getNode(), sendBarrier,
                GroupUtil.GROUP_ADD_COMPOSING_TRANSFORM)
            : addGroupsBulkFuture;
    }

    private static GroupRef createGroupRef(final NodeRef nodeRef, final Group batchGroup) {
        return GroupUtil.buildGroupPath(((DataObjectIdentifier<Node>) nodeRef.getValue()).toLegacy(),
            batchGroup.getGroupId());
    }
}
