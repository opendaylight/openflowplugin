/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.RetryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adding retry mechanism in case of unsuccessful syncup.
 */
public class SyncReactorRetryDecorator implements SyncReactor{

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorRetryDecorator.class);

    private final SyncReactor delegate;
    private final RetryRegistry retryRegistry;

    public SyncReactorRetryDecorator (final SyncReactor delegate, RetryRegistry retryRegistry) {
        this.delegate = delegate;
        this.retryRegistry = retryRegistry;
    }

    public ListenableFuture<Boolean> syncup (final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                             final FlowCapableNode configTree,
                                             final FlowCapableNode operationalTree) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup retry {}", nodeId.getValue());

        ListenableFuture<Boolean> syncupResult = delegate.syncup(flowcapableNodePath, configTree, operationalTree);

        return Futures.transform(syncupResult, new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean result) {
                LOG.trace("syncup ret in retry {}", result);
                if (result)
                    return true;
                else {
                    retryRegistry.register(nodeId);
                    // TODO  elicit statistics gathering if not running actually
                    // triggerStatisticsGathering(nodeId);
                    return false;
                }
            }
        });
    }
}
