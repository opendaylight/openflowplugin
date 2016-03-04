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
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.applications.frsync.NodeListener;
import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
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
 * listens to changes of flow-capable-node in given logical type of DS the reads missing counterpart for
 * given node and delegates synchronization to {@link SyncReactor}
 */
public abstract class AbstractNodeListener implements NodeListener {
    private static final Logger LOG = LoggerFactory.getLogger(NodeListenerConfigImpl.class);
    protected final SyncReactor reactor;
    private final DataBroker dataBroker;
    private final SemaphoreKeeper<NodeId> semaphoreKeeper;

    public AbstractNodeListener(SyncReactor reactor, DataBroker dataBroker, final SemaphoreKeeperImpl<NodeId> semaphoreKeeper) {
        this.reactor = reactor;
        this.dataBroker = dataBroker;
        this.semaphoreKeeper = semaphoreKeeper;
    }

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<FlowCapableNode>> collection) {
        final ReadOnlyTransaction roTx = dataBroker.newReadOnlyTransaction();
        List<ListenableFuture<RpcResult<Void>>> processingResults = new ArrayList<>();

        for (DataTreeModification<FlowCapableNode> modification : collection) {
            final InstanceIdentifier<FlowCapableNode> nodePath = modification.getRootPath().getRootIdentifier();
            final NodeId nodeId = PathUtil.digNodeId(nodePath);
            final Semaphore guard = semaphoreKeeper.summonGuard(nodeId);
            try {
                // lock per node
                guard.acquire();
            } catch (InterruptedException e) {
                LOG.warn("permit for forwarding rules sync not acquired: {}", nodeId);
                continue;
            }
            final DataObjectModification<FlowCapableNode> triggerModification = modification.getRootNode();

            final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> read =
                    roTx.read(getCounterpartDSLogicalType(), nodePath);

            //TODO: sanitize mark&sweep in case config is empty
            final ListenableFuture<RpcResult<Void>> endResult = Futures.transform(read,
                    createNextStepFunction(nodePath, Optional.fromNullable(triggerModification.getDataAfter())));
            processingResults.add(endResult);
            // unlock per node
            hookGuardRelease(endResult, nodeId, guard);
        }

        final ListenableFuture<List<RpcResult<Void>>> finalResult = Futures.allAsList(processingResults);
        Futures.addCallback(finalResult, new FutureCallback<List<RpcResult<Void>>>() {
            @Override
            public void onSuccess(@Nullable final List<RpcResult<Void>> result) {
                LOG.debug("DS/config change successfully synced to device");
                roTx.close();
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("DS/config change failed to sync to device", t);
                roTx.close();
            }
        });
    }

    private void hookGuardRelease(final ListenableFuture<RpcResult<Void>> result, final NodeId nodeId, final Semaphore guard) {
        Futures.addCallback(result, new FutureCallback<RpcResult<Void>>() {
            @Override
            public void onSuccess(@Nullable final RpcResult<Void> result) {
                guard.release();
            }

            @Override
            public void onFailure(final Throwable t) {
                guard.release();
            }
        });
    }

    abstract LogicalDatastoreType getCounterpartDSLogicalType();

    abstract AsyncFunction<Optional<FlowCapableNode>, RpcResult<Void>> createNextStepFunction(
            final InstanceIdentifier<FlowCapableNode> nodePath, final Optional<FlowCapableNode> triggerModification);
}
