/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator for running delegate syncup in Future.
 */
public class SyncReactorFutureDecorator implements SyncReactor {
    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureDecorator.class);

    private final SyncReactor delegate;
    private final Executor executor;

    public SyncReactorFutureDecorator(final SyncReactor delegate, final Executor executor) {
        this.delegate = requireNonNull(delegate);
        this.executor = requireNonNull(executor);
    }

    @Override
    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final SyncupEntry syncupEntry) {
        final var nodeId = PathUtil.digNodeId(flowcapableNodePath);
        return Futures.submit(() -> {
            try {
                return doSyncupInFuture(flowcapableNodePath, syncupEntry).get(10000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                LOG.warn("Syncup future timeout occured {}", nodeId.getValue());
                return Boolean.FALSE;
            }
        }, executor);
    }

    protected ListenableFuture<Boolean> doSyncupInFuture(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final SyncupEntry syncupEntry) {
        return delegate.syncup(flowcapableNodePath, syncupEntry);
    }
}
