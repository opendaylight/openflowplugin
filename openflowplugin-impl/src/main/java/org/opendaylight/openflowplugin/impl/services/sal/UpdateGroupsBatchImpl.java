/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.OriginalGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.group.update.UpdatedGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.BatchGroupInputUpdateGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UpdateGroupsBatchImpl implements UpdateGroupsBatch {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateGroupsBatchImpl.class);

    private final UpdateGroup updateGroup;
    private final SendBarrier sendBarrier;

    public UpdateGroupsBatchImpl(final UpdateGroup updateGroup, final SendBarrier sendBarrier) {
        this.updateGroup = requireNonNull(updateGroup);
        this.sendBarrier = requireNonNull(sendBarrier);
    }

    @Override
    public ListenableFuture<RpcResult<UpdateGroupsBatchOutput>> invoke(final UpdateGroupsBatchInput input) {
        final var batchUpdateGroups = input.nonnullBatchUpdateGroups();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Updating groups @ {} : {}", PathUtil.extractNodeId(input.getNode()), batchUpdateGroups.size());
        }

        final var resultsLot = batchUpdateGroups.stream()
            .map(batchGroup -> updateGroup.invoke(new UpdateGroupInputBuilder(input)
                .setOriginalGroup(new OriginalGroupBuilder(batchGroup.getOriginalBatchedGroup()).build())
                .setUpdatedGroup(new UpdatedGroupBuilder(batchGroup.getUpdatedBatchedGroup()).build())
                .setGroupRef(createGroupRef(input.getNode(), batchGroup))
                .setNode(input.getNode())
                .build()))
            .collect(Collectors.toList());

        final var groups = batchUpdateGroups.stream()
                .map(BatchGroupInputUpdateGrouping::getUpdatedBatchedGroup)
                .collect(Collectors.toList());

        final var commonResult = Futures.transform(Futures.allAsList(resultsLot),
            GroupUtil.createCumulatingFunction(groups, batchUpdateGroups.size()),
            MoreExecutors.directExecutor());

        final var updateGroupsBulkFuture = Futures.transform(commonResult, GroupUtil.GROUP_UPDATE_TRANSFORM,
            MoreExecutors.directExecutor());

        return input.getBarrierAfter()
            ? BarrierUtil.chainBarrier(updateGroupsBulkFuture, input.getNode(), sendBarrier,
                GroupUtil.GROUP_UPDATE_COMPOSING_TRANSFORM)
            : updateGroupsBulkFuture;
    }

    private static GroupRef createGroupRef(final NodeRef nodeRef, final BatchUpdateGroups batchGroup) {
        return GroupUtil.buildGroupPath(((DataObjectIdentifier<Node>) nodeRef.getValue()).toLegacy(),
            batchGroup.getUpdatedBatchedGroup().getGroupId());
    }
}
