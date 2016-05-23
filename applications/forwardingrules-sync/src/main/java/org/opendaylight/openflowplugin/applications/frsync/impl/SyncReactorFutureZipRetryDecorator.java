/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.ListeningExecutorService;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.RetryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modified {@link SyncReactorFutureZipDecorator} for usage as config reactor with retry engagement.
 */
public class SyncReactorFutureZipRetryDecorator extends SyncReactorFutureZipDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureZipRetryDecorator.class);
    private final RetryRegistry retryRegistry;

    public SyncReactorFutureZipRetryDecorator(SyncReactor delegate, ListeningExecutorService executorService,
                                              RetryRegistry retryRegistry) {
        super(delegate, executorService);
        this.retryRegistry = retryRegistry;
    }

    protected boolean updateCompressionState(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                             final FlowCapableNode configTree, final FlowCapableNode operationalTree,
                                             final LogicalDatastoreType dsType) {
        NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        final Pair<FlowCapableNode, FlowCapableNode> previous = compressionQueue.get(flowcapableNodePath);
        boolean isRetryRegistered = retryRegistry.isRegistered(nodeId);
        LOG.trace("updateCompressionState (config): {} previous: {} registered: {}", nodeId.getValue(), previous != null, isRetryRegistered);
        if (dsType == LogicalDatastoreType.CONFIGURATION) {
            if (isRetryRegistered) {
                compressionQueue.remove(flowcapableNodePath);
                return false;
            }
            if (previous != null ) {
                compressionQueue.put(flowcapableNodePath, Pair.of(configTree, previous.getRight()));
            } else {
                compressionQueue.put(flowcapableNodePath, Pair.of(configTree, operationalTree));
            }
        }
        else {
            compressionQueue.put(flowcapableNodePath, Pair.of(configTree, operationalTree));
        }
        return previous == null;
    }

}




