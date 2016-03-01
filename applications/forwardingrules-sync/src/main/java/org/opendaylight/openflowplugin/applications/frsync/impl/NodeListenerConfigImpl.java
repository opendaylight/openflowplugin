/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SemaphoreKeeperImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this implementation listens to DS/config and reads DS/operational counterpart when notified
 */
public class NodeListenerConfigImpl extends AbstractNodeListener {

    private static final Logger LOG = LoggerFactory.getLogger(NodeListenerConfigImpl.class);

    public NodeListenerConfigImpl(SyncReactor reactor, DataBroker dataBroker, final SemaphoreKeeperImpl<NodeId> semaphoreKeeper) {
        super(reactor, dataBroker, semaphoreKeeper);
    }

    @Override
    LogicalDatastoreType getCounterpartDSLogicalType() {
        return LogicalDatastoreType.OPERATIONAL;
    }

    @Override
    AsyncFunction<Optional<FlowCapableNode>, RpcResult<Void>> createNextStepFunction(
            final InstanceIdentifier<FlowCapableNode> nodePath, final FlowCapableNode triggerModification) {
        return new AsyncFunction<Optional<FlowCapableNode>, RpcResult<Void>>() {
            @Override
            public ListenableFuture<RpcResult<Void>> apply(final Optional<FlowCapableNode> input) throws Exception {
                final ListenableFuture<RpcResult<Void>> nextResult;
                if (input.isPresent()) {
                    nextResult = reactor.syncup(nodePath, triggerModification, input.get());
                } else {
                    LOG.trace("no node present in DS/operational for nodeId={}", PathUtil.digNodeId(nodePath));
                    nextResult = Futures.immediateFuture(null);
                }

                return nextResult;
            }
        };
    }
}
