/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.SemaphoreKeeperImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Listens to config changes and delegates add/remove/update/barrier to {@link SyncReactor}
 */
public class SimplifiedConfigListener extends AbstractFrmSyncListener {
    //private static final Logger LOG = LoggerFactory.getLogger(SimplifiedConfigListener.class);
    protected final SyncReactor reactor;

    public SimplifiedConfigListener(final SyncReactor reactor,
            final SemaphoreKeeperImpl<NodeId> semaphoreKeeper) {
        super(semaphoreKeeper);
        this.reactor = reactor;
    }

    /**
     * Compare config BEFORE and AFTER (performance reasons)
     */
    protected Optional<ListenableFuture<RpcResult<Void>>> processNodeModification(
            DataTreeModification<FlowCapableNode> modification) {
        final InstanceIdentifier<FlowCapableNode> nodePath = modification.getRootPath().getRootIdentifier();

        final DataObjectModification<FlowCapableNode> triggerModification = modification.getRootNode();
        //TODO handle if node is not connected (RPCs not registered?)
        final ListenableFuture<RpcResult<Void>> endResult =
                reactor.syncup(nodePath, triggerModification.getDataAfter(), triggerModification.getDataBefore());
        return Optional.of(endResult);
    }

    @Override
    public LogicalDatastoreType dsType() {
        return LogicalDatastoreType.CONFIGURATION;
    }
}
