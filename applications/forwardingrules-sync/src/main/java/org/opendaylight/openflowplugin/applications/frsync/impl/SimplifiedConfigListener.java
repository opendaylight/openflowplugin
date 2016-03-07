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
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
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
 * Listens to config changes and delegates add/remove/update/barrier to {@link SyncReactor}
 * 
 * @author joslezak
 */
public class SimplifiedConfigListener extends AbstractFrmSyncListener {
    private static final Logger LOG = LoggerFactory.getLogger(SimplifiedConfigListener.class);
    protected final SyncReactor reactor;
    private final FlowCapableNodeSnapshotDao configSnaphot;
    private final FlowCapableNodeDao operationalDao;

    public SimplifiedConfigListener(final SyncReactor reactor,
            final SemaphoreKeeperImpl<NodeId> semaphoreKeeper, FlowCapableNodeSnapshotDao configSnaphot,
            FlowCapableNodeDao operationalDao) {
        super(semaphoreKeeper);
        this.reactor = reactor;
        this.configSnaphot = configSnaphot;
        this.operationalDao = operationalDao;
    }

    /**
     * Compare cached operational with current config modification. If operational is not present
     * skip calling Inventory RPCs.
     */
    protected Optional<ListenableFuture<RpcResult<Void>>> processNodeModification(
            DataTreeModification<FlowCapableNode> modification) {
        configSnaphot.modification(modification);

        final InstanceIdentifier<FlowCapableNode> nodePath = modification.getRootPath().getRootIdentifier();
        final NodeId nodeId = PathUtil.digNodeId(nodePath);

        final Optional<FlowCapableNode> operationalNode = operationalDao.loadByNodeId(nodeId);
        if (!operationalNode.isPresent()) {
            LOG.info("Skip inventory RPC {} operational is not present", nodeId);
            return Optional.absent();
        }

        final DataObjectModification<FlowCapableNode> configModification = modification.getRootNode();
        final ListenableFuture<RpcResult<Void>> endResult =
                // better preformance needed just compare config after and before:
                reactor.syncup(nodePath, configModification.getDataAfter(),
                        configModification.getDataBefore());
        // reactor.syncup(nodePath, operationalNode.get(), configModification.getDataAfter());

        return Optional.of(endResult);
    }

    @Override
    public LogicalDatastoreType dsType() {
        return LogicalDatastoreType.CONFIGURATION;
    }
}
