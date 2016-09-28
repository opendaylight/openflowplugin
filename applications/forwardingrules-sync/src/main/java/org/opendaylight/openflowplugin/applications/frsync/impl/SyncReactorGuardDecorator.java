/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SemaphoreKeeperGuavaImpl;
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
    private final SemaphoreKeeper<InstanceIdentifier<FlowCapableNode>> semaphoreKeeper =
            new SemaphoreKeeperGuavaImpl<>(1, true);

    public SyncReactorGuardDecorator(final SyncReactor delegate) {
        this.delegate = delegate;
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                            final SyncupEntry syncupEntry) {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        final long stampBeforeGuard = System.nanoTime();
        final Semaphore guard = semaphoreKeeper.summonGuardAndAcquire(flowcapableNodePath);
        if (Objects.isNull(guard)) {
            return Futures.immediateFuture(Boolean.FALSE);
        }
        final long stampAfterGuard = System.nanoTime();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Syncup guard acquired and running for {} ", nodeId.getValue());
        }
        final ListenableFuture<Boolean> endResult = delegate.syncup(flowcapableNodePath, syncupEntry);
        Futures.addCallback(endResult, createSyncupCallback(guard, stampBeforeGuard, stampAfterGuard, nodeId));
        return endResult;
    }

    private FutureCallback<Boolean> createSyncupCallback(final Semaphore guard,
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
                semaphoreKeeper.releaseGuard(guard);
            }
            @Override
            public void onFailure(final Throwable t) {
                final long stampFinished = System.nanoTime();
                LOG.warn("Syncup failed {} took:{} rpc:{} wait:{}", nodeId.getValue(),
                        formatNanos(stampFinished - stampBeforeGuard), formatNanos(stampFinished - stampAfterGuard),
                        formatNanos(stampAfterGuard - stampBeforeGuard));
                semaphoreKeeper.releaseGuard(guard);
            }};
    }

    private static String formatNanos(final long nanos) {
        return "'" + TimeUnit.NANOSECONDS.toMillis(nanos) + " ms'";
    }
}
