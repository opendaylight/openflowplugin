/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to config changes and delegates add/remove/update/barrier to {@link SyncReactor}.
 */
public class SimplifiedConfigListener extends AbstractFrmSyncListener<FlowCapableNode> {
    private static final Logger LOG = LoggerFactory.getLogger(SimplifiedConfigListener.class);
    private final SyncReactor reactor;
    private final FlowCapableNodeSnapshotDao configSnapshot;
    private final FlowCapableNodeDao operationalDao;
    private final ReconciliationRegistry reconciliationRegistry;

    public SimplifiedConfigListener(final SyncReactor reactor,
                                    final FlowCapableNodeSnapshotDao configSnapshot,
                                    final FlowCapableNodeDao operationalDao,
                                    final ReconciliationRegistry reconciliationRegistry) {
        this.reactor = reactor;
        this.configSnapshot = configSnapshot;
        this.operationalDao = operationalDao;
        this.reconciliationRegistry = reconciliationRegistry;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<FlowCapableNode>> modifications) {
        LOG.trace("Config changes: {}", modifications.size());
        super.onDataTreeChanged(modifications);
    }

    /**
     * Compare cached operational with current config modification. If operational is not present
     * skip calling Inventory RPCs.
     * @throws InterruptedException from syncup
     */
    protected Optional<ListenableFuture<Boolean>> processNodeModification(
            DataTreeModification<FlowCapableNode> modification) throws InterruptedException {
        final InstanceIdentifier<FlowCapableNode> nodePath = modification.getRootPath().getRootIdentifier();
        final NodeId nodeId = PathUtil.digNodeId(nodePath);

        configSnapshot.updateCache(nodeId, Optional.fromNullable(modification.getRootNode().getDataAfter()));

        final Optional<FlowCapableNode> operationalNode = operationalDao.loadByNodeId(nodeId);

        final DataObjectModification<FlowCapableNode> configModification = modification.getRootNode();
        final FlowCapableNode dataBefore = configModification.getDataBefore();
        final FlowCapableNode dataAfter = configModification.getDataAfter();
        final Optional<ListenableFuture<Boolean>> endResult;
        if (dataBefore == null && dataAfter != null) {
            endResult = onNodeAdded(nodePath);
        } else if (dataBefore != null && dataAfter == null) {
            endResult = onNodeDeleted(nodePath, dataBefore);
        } else {
            endResult = onNodeUpdated(nodePath, dataBefore, dataAfter);
        }

        return endResult;
    }

    /**
     * If node was added in config DS, register it for reconciliation. Reconciliation should be done when
     * device connects and appears in operational DS. Add only what is missing in operational store.
     */
    private Optional<ListenableFuture<Boolean>> onNodeAdded(InstanceIdentifier<FlowCapableNode> nodePath) {
        NodeId nodeId = PathUtil.digNodeId(nodePath);
        LOG.trace("onNodeAdded {}", nodeId);
        reconciliationRegistry.register(nodeId);
        return Optional.absent();
    }

    /**
     * Apply minimal changes very fast. For better performance needed just compare config
     * after+before. Config listener should not be dependent on operational flows/groups while
     * updating config because operational store is highly async and it depends on another module in
     * system which is updating operational store (that components is also trying to solve
     * scale/performance issues on several layers).
     */
    private Optional<ListenableFuture<Boolean>> onNodeUpdated(InstanceIdentifier<FlowCapableNode> nodePath,
                          FlowCapableNode dataBefore, FlowCapableNode dataAfter) throws InterruptedException {
        NodeId nodeId = PathUtil.digNodeId(nodePath);
        LOG.trace("onNodeUpdated {}", nodeId);
        return Optional.of(reactor.syncup(nodePath, dataAfter, dataBefore, dsType()));
    }

    /**
     * Remove values that are being deleted in the config from the switch. Note, this could be
     * probably optimized using dedicated wipe-out RPC, but it has impact on switch if it is
     * programmed by two person/system
     */
    private Optional<ListenableFuture<Boolean>> onNodeDeleted(InstanceIdentifier<FlowCapableNode> nodePath,
                                                    FlowCapableNode dataBefore) throws InterruptedException {
        NodeId nodeId = PathUtil.digNodeId(nodePath);
        LOG.trace("onNodeDeleted {}", nodeId);
        return Optional.of(reactor.syncup(nodePath, null, dataBefore, dsType()));
    }

    @Override
    public LogicalDatastoreType dsType() {
        return LogicalDatastoreType.CONFIGURATION;
    }
}
