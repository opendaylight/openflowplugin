/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator for NodeId level syncup locking.
 */
public class SyncReactorGuardDecorator implements SyncReactor {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorGuardDecorator.class);
    private final SyncReactor delegate;
    private final SemaphoreKeeper<InstanceIdentifier<FlowCapableNode>> semaphoreKeeper;

    public SyncReactorGuardDecorator(SyncReactor delegate,
            SemaphoreKeeper<InstanceIdentifier<FlowCapableNode>> semaphoreKeeper) {
        this.delegate = delegate;
        this.semaphoreKeeper = semaphoreKeeper;
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                            final SyncupEntry syncupEntry) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        final long stampBeforeGuard = System.nanoTime();
        final Semaphore guard = summonGuardAndAcquire(flowcapableNodePath);
        if (guard == null) {
            return Futures.immediateFuture(Boolean.FALSE);
        }
        final long stampAfterGuard = System.nanoTime();

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Syncup guard acquired and running for {} ", nodeId.getValue());
            }
            final ListenableFuture<Boolean> endResult = delegate.syncup(flowcapableNodePath, syncupEntry);
            Futures.addCallback(endResult, createSyncupCallback(guard, stampBeforeGuard, stampAfterGuard, nodeId));
            return endResult;
        } catch (InterruptedException e) {
            releaseGuard(guard);
            throw e;
        }
    }

    private static FutureCallback<Boolean> createSyncupCallback(final Semaphore guard,
                                                                final long stampBeforeGuard,
                                                                final long stampAfterGuard,
                                                                final NodeId nodeId) {
        return new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable final Boolean result) {
                if (LOG.isDebugEnabled()) {
                    final long stampFinished = System.nanoTime();
                    LOG.debug("Syncup finished {} took:{} rpc:{} wait:{}", nodeId.getValue(),
                            formatNanos(stampFinished - stampBeforeGuard), formatNanos(stampFinished - stampAfterGuard),
                            formatNanos(stampAfterGuard - stampBeforeGuard));
                }
                releaseGuard(guard);
            }
            @Override
            public void onFailure(final Throwable t) {
                final long stampFinished = System.nanoTime();
                LOG.warn("Syncup failed {} took:{} rpc:{} wait:{}", nodeId.getValue(),
                        formatNanos(stampFinished - stampBeforeGuard), formatNanos(stampFinished - stampAfterGuard),
                        formatNanos(stampAfterGuard - stampBeforeGuard));
                releaseGuard(guard);
            }};
    }

    private static String formatNanos(final long nanos) {
        return "'" + TimeUnit.NANOSECONDS.toMillis(nanos) + " ms'";
    }

    /**
     * Get guard and lock for node.
     * @param flowcapableNodePath II of node for which guard should be acquired
     * @return semaphore guard
     */
    private Semaphore summonGuardAndAcquire(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath) {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        final Semaphore guard = Preconditions.checkNotNull(semaphoreKeeper.summonGuard(flowcapableNodePath),
                "No guard for " + nodeId.getValue());
        try {
            guard.acquire();
        } catch (InterruptedException e) {
            LOG.warn("Syncup summon {} failed {}", nodeId.getValue(), e);
            return null;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Syncup summon {} guard:{}", nodeId.getValue(), guard);
        }
        return guard;
    }

    /**
     * Unlock and release guard.
     * @param guard semaphore guard which should be unlocked
     */
    private static void releaseGuard(final Semaphore guard) {
        if (guard != null) {
            guard.release();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Syncup release guard:{} thread:{}", guard);
            }
        }
    }
}
