/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.SemaphoreKeeperGuavaImpl;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

/**
 * Enriches {@link SyncReactorFutureDecorator} with state compression.
 */
public class SyncReactorFutureZipDecorator extends SyncReactorFutureDecorator {
    private final Map<DataObjectIdentifier<FlowCapableNode>, SyncupEntry> compressionQueue = new HashMap<>();
    private final SemaphoreKeeper<DataObjectIdentifier<FlowCapableNode>> semaphoreKeeper =
            new SemaphoreKeeperGuavaImpl<>(1, true);

    public SyncReactorFutureZipDecorator(final SyncReactor delegate, final Executor executor) {
        super(delegate, executor);
    }

    @Override
    public ListenableFuture<Boolean> syncup(final DataObjectIdentifier<FlowCapableNode> flowcapableNodePath,
            final SyncupEntry syncupEntry) {
        Semaphore guard = null;
        try {
            guard = semaphoreKeeper.summonGuardAndAcquire(flowcapableNodePath);
            if (guard == null) {
                return Futures.immediateFuture(Boolean.FALSE);
            }
            final boolean newTaskNecessary = updateCompressionState(flowcapableNodePath, syncupEntry);
            if (newTaskNecessary) {
                super.syncup(flowcapableNodePath, syncupEntry);
            }
            return Futures.immediateFuture(Boolean.TRUE);
        } finally {
            semaphoreKeeper.releaseGuard(guard);
        }
    }

    @Override
    protected ListenableFuture<Boolean> doSyncupInFuture(
            final DataObjectIdentifier<FlowCapableNode> flowcapableNodePath, final SyncupEntry syncupEntry) {
        final var lastCompressionState = removeLastCompressionState(flowcapableNodePath);
        if (lastCompressionState == null) {
            return Futures.immediateFuture(Boolean.TRUE);
        }
        return super.doSyncupInFuture(flowcapableNodePath, lastCompressionState);
    }

    /**
     * If a syncup entry for corresponding the device is present in compression queue and new configuration diff is
     * coming - update the entry in compression queue (zip). Create new (no entry in queue for device) or replace
     * entry (config vs. operational is coming) in queue otherwise.
     */
    private boolean updateCompressionState(final DataObjectIdentifier<FlowCapableNode> flowcapableNodePath,
            final SyncupEntry syncupEntry) {
        final var previousEntry = compressionQueue.get(flowcapableNodePath);
        if (previousEntry != null && syncupEntry.isOptimizedConfigDelta()) {
            updateOptimizedConfigDelta(flowcapableNodePath, syncupEntry, previousEntry);
        } else {
            compressionQueue.put(flowcapableNodePath, syncupEntry);
        }
        return previousEntry == null;
    }

    private void updateOptimizedConfigDelta(final DataObjectIdentifier<FlowCapableNode> flowcapableNodePath,
            final SyncupEntry actual, final SyncupEntry previous) {
        compressionQueue.put(flowcapableNodePath, new SyncupEntry(actual.getAfter(), actual.getDsTypeAfter(),
            previous.getBefore(), previous.getDsTypeBefore()));
    }

    private SyncupEntry removeLastCompressionState(final DataObjectIdentifier<FlowCapableNode> flowcapableNodePath) {
        Semaphore guard = null;
        try {
            guard = semaphoreKeeper.summonGuardAndAcquire(flowcapableNodePath);
            return guard == null ? null : compressionQueue.remove(flowcapableNodePath);
        } finally {
            semaphoreKeeper.releaseGuard(guard);
        }
    }
}