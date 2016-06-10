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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
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

    private final SyncReactor delegate;
    private final ListeningExecutorService executorService;

    public static final String FRM_RPC_CLIENT_PREFIX = "FRM-RPC-client-";

    public SyncReactorFutureDecorator(SyncReactor delegate, ListeningExecutorService executorService) {
        this.delegate = delegate;
        this.executorService = executorService;
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup future {}", nodeId.getValue());

        final ListenableFuture<Boolean> syncup = executorService.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                final String oldThreadName = updateThreadName(nodeId);

                try {
                    final Boolean ret = doSyncupInFuture(flowcapableNodePath, configTree, operationalTree)
                            .get(10000, TimeUnit.MILLISECONDS);
                    LOG.trace("ret {} {}", nodeId.getValue(), ret);
                    return true;
                } catch (TimeoutException e) {
                    LOG.error("doSyncupInFuture timeout occured {}", nodeId.getValue(), e);
                    return false;
                } finally {
                    updateThreadName(oldThreadName);
                }
            }
        });

        return syncup;
    }

    protected ListenableFuture<Boolean> doSyncupInFuture(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree)
                    throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("doSyncupInFuture future {}", nodeId.getValue());

        return delegate.syncup(flowcapableNodePath, configTree, operationalTree);
    }

    protected String updateThreadName(NodeId nodeId) {
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

    protected String updateThreadName(String name) {
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
        return oldName;
    }
}
