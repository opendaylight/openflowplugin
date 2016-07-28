/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator for running delegate syncup in Future.
 */
public class SyncReactorFutureDecorator implements SyncReactor {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureDecorator.class);
    public static final String FRM_RPC_CLIENT_PREFIX = "FRM-RPC-client-";
    private final SyncReactor delegate;
    private final ListeningExecutorService executorService;

    public SyncReactorFutureDecorator(SyncReactor delegate, ListeningExecutorService executorService) {
        this.delegate = delegate;
        this.executorService = executorService;
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                            final SyncupEntry syncupEntry) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup future decorator: {}", nodeId.getValue());

        return executorService.submit(() -> {
            final String oldThreadName = updateThreadName(nodeId);

            try {
                final Boolean ret = doSyncupInFuture(flowcapableNodePath, syncupEntry)
                        .get(10000, TimeUnit.MILLISECONDS);
                LOG.trace("syncup return in future decorator: {} [{}]", nodeId.getValue(), ret);
                return true;
            } catch (TimeoutException e) {
                LOG.error("doSyncupInFuture timeout occured {}", nodeId.getValue(), e);
                return false;
            } finally {
                updateThreadName(oldThreadName);
            }
        });
    }

    protected ListenableFuture<Boolean> doSyncupInFuture(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                                         final SyncupEntry syncupEntry) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("doSyncupInFuture future decorator: {}", nodeId.getValue());

        return delegate.syncup(flowcapableNodePath, syncupEntry);
    }

    private String updateThreadName(NodeId nodeId) {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        try {
            if (oldName.startsWith(SyncReactorFutureDecorator.FRM_RPC_CLIENT_PREFIX)) {
                currentThread.setName(oldName + "@" + nodeId.getValue());
            } else {
                LOG.warn("try to update foreign thread name {} {}", nodeId, oldName);
            }
        } catch (Exception e) {
            LOG.error("failed updating threadName {}", nodeId, e);
        }
        return oldName;
    }

    private void updateThreadName(String name) {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        try {
            if (oldName.startsWith(SyncReactorFutureDecorator.FRM_RPC_CLIENT_PREFIX)) {
                currentThread.setName(name);
            } else {
                LOG.warn("try to update foreign thread name {} {}", oldName, name);
            }
        } catch (Exception e) {
            LOG.error("failed updating threadName {}", name, e);
        }
    }
}
