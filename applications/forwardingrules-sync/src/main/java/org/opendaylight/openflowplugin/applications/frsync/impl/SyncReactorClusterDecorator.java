/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.impl.clustering.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator for cluster related issues.
 */
public class SyncReactorClusterDecorator implements SyncReactor {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorClusterDecorator.class);
    private final SyncReactor delegate;
    private final DeviceMastershipManager deviceMastershipManager;

    public SyncReactorClusterDecorator(final SyncReactor delegate,
                                       final DeviceMastershipManager deviceMastershipManager) {
        this.delegate = delegate;
        this.deviceMastershipManager = deviceMastershipManager;
    }

    @Override
    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                            final SyncupEntry syncupEntry) {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        if (!deviceMastershipManager.isDeviceMastered(nodeId)) {
            LOG.debug("Skip syncup since not master for: {}", nodeId.getValue());
            return Futures.immediateFuture(Boolean.TRUE);
        } else {
            return delegate.syncup(flowcapableNodePath, syncupEntry);
        }
    }
}
