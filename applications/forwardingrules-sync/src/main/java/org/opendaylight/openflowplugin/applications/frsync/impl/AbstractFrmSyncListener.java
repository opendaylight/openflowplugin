/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
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

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Abstract Listener for node changes
 */
public abstract class AbstractFrmSyncListener implements NodeListener {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractFrmSyncListener.class);
    private final SemaphoreKeeper<NodeId> semaphoreKeeper;

    public AbstractFrmSyncListener(final SemaphoreKeeperImpl<NodeId> semaphoreKeeper) {
        this.semaphoreKeeper = semaphoreKeeper;
    }

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<FlowCapableNode>> collection) {
        for (DataTreeModification<FlowCapableNode> modification : collection) {
            final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().getRootIdentifier());

            final long stamp = System.currentTimeMillis();
            LOG.trace("Inventory {} modification {} {}", dsType(), nodeId, stamp);

            try {
                final Semaphore guard = lockAcquireForNodeId(nodeId);

                final Optional<ListenableFuture<RpcResult<Void>>> endResult = processNodeModification(modification);
                if (!endResult.isPresent()) {
                    continue;
                }

                final ListenableFuture<List<RpcResult<Void>>> finalResult = Futures.allAsList(endResult.get());
                Futures.addCallback(finalResult, new FutureCallback<List<RpcResult<Void>>>() {
                    @Override
                    public void onSuccess(@Nullable final List<RpcResult<Void>> result) {
                        LOG.debug("Successfull inventory {} modification {} {}", dsType(), nodeId, stamp);
                        lockReleaseForNodeId(nodeId, guard);
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        LOG.warn("Failed inventory " + dsType() + " modification " + nodeId + " " + stamp, t);
                        lockReleaseForNodeId(nodeId, guard);
                    }
                });
            } catch (Exception e) {
                LOG.error("error processing inventory node modification" + nodeId, e);
            }
        }
    }

    protected abstract Optional<ListenableFuture<RpcResult<Void>>> processNodeModification(
            DataTreeModification<FlowCapableNode> modification) throws ReadFailedException;

    /**
     * lock per node
     * 
     * @param nodeId
     * @param guard
     * @return
     */
    protected Semaphore lockAcquireForNodeId(final NodeId nodeId) {
        final Semaphore guard = semaphoreKeeper.summonGuard(nodeId);
        // try {
        // guard.acquire();
        // } catch (InterruptedException e) {
        // LOG.warn("permit for forwarding rules sync not acquired: {}", nodeId);
        // continue;
        // }
        return guard;
    }

    /**
     * unlock per node
     * 
     * @param nodeId
     * @param guard
     */
    protected void lockReleaseForNodeId(final NodeId nodeId,
            final Semaphore guard) {
        if (guard == null) {
            return;
        }
        guard.release();
    }

    public abstract LogicalDatastoreType dsType();
}
