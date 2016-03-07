/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.Collection;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeCachedDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeOdlDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SemaphoreKeeperImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Listens to operational new nodes and delegates add/remove/update/barrier to {@link SyncReactor}
 */
public class SimplifiedOperationalListener extends AbstractFrmSyncListener {
    private static final Logger LOG = LoggerFactory.getLogger(SimplifiedOperationalListener.class);

    protected final SyncReactor reactor;
    private final DataBroker dataBroker;

    private FlowCapableNodeSnapshotDao operationalSnaphot;

    private FlowCapableNodeDao configDao;

    public SimplifiedOperationalListener(SyncReactor reactor, DataBroker dataBroker,
            final SemaphoreKeeperImpl<NodeId> semaphoreKeeper, FlowCapableNodeSnapshotDao operationalSnaphot, FlowCapableNodeDao configDao) {
        super(semaphoreKeeper);
        this.reactor = reactor;
        this.dataBroker = dataBroker;
        this.operationalSnaphot = operationalSnaphot;
        this.configDao = configDao;
    }
    
    /**
     * If node is added to operational store than Inventory RPCs are called (reconciliation).
     */
    protected Optional<ListenableFuture<RpcResult<Void>>> processNodeModification(
            DataTreeModification<FlowCapableNode> modification) throws ReadFailedException {
        operationalSnaphot.modification(modification);
        
        final InstanceIdentifier<FlowCapableNode> nodePath = modification.getRootPath().getRootIdentifier();
        final NodeId nodeId = PathUtil.digNodeId(nodePath);

        final boolean nodeAppearedInOperational = modification.getRootNode().getDataBefore() == null
                && modification.getRootNode().getDataAfter() != null;
        if (!nodeAppearedInOperational) {
            LOG.trace("Skipping Inventory Operational modification {}", nodeId);
            return Optional.absent();// skip processing
        }

        final Optional<FlowCapableNode> nodeConfiguration = configDao.loadByNodeId(nodeId);
        final DataObjectModification<FlowCapableNode> operationalModification = modification.getRootNode();
        final ListenableFuture<RpcResult<Void>> rpcResult =
                reactor.syncup(nodePath, nodeConfiguration.orNull(), operationalModification.getDataAfter());
        return Optional.of(rpcResult);
    }

    @Override
    public LogicalDatastoreType dsType() {
        return LogicalDatastoreType.OPERATIONAL;
    }
}
