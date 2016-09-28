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
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to config changes and delegates sync entry to {@link SyncReactor}.
 */
public class SimplifiedConfigListener extends AbstractFrmSyncListener<FlowCapableNode> {

    private static final Logger LOG = LoggerFactory.getLogger(SimplifiedConfigListener.class);
    private final SyncReactor reactor;
    private final FlowCapableNodeSnapshotDao configSnapshot;
    private final FlowCapableNodeDao operationalDao;

    public SimplifiedConfigListener(final SyncReactor reactor,
                                    final FlowCapableNodeSnapshotDao configSnapshot,
                                    final FlowCapableNodeDao operationalDao) {
        this.reactor = reactor;
        this.configSnapshot = configSnapshot;
        this.operationalDao = operationalDao;
    }

    @Override
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<FlowCapableNode>> modifications) {
        super.onDataTreeChanged(modifications);
    }

    /**
     * Update cache. If operational data are present, choose appropriate data and start syncup.
     * Otherwise skip incoming change.
     */
    protected Optional<ListenableFuture<Boolean>> processNodeModification(
            final DataTreeModification<FlowCapableNode> modification) {
        final InstanceIdentifier<FlowCapableNode> nodePath = modification.getRootPath().getRootIdentifier();
        final NodeId nodeId = PathUtil.digNodeId(nodePath);

        configSnapshot.updateCache(nodeId, Optional.fromNullable(modification.getRootNode().getDataAfter()));

        final Optional<FlowCapableNode> operationalNode = operationalDao.loadByNodeId(nodeId);
        if (!operationalNode.isPresent()) {
            LOG.debug("Skip syncup, {} operational is not present", nodeId.getValue());
            return Optional.absent();
        }

        final DataObjectModification<FlowCapableNode> configModification = modification.getRootNode();
        final FlowCapableNode dataBefore = configModification.getDataBefore();
        final FlowCapableNode dataAfter = configModification.getDataAfter();
        final ListenableFuture<Boolean> endResult;
        if (dataBefore == null && dataAfter != null) {
            endResult = onNodeAdded(nodePath, dataAfter, operationalNode.get());
        } else if (dataBefore != null && dataAfter == null) {
            endResult = onNodeDeleted(nodePath, dataBefore);
        } else {
            endResult = onNodeUpdated(nodePath, dataBefore, dataAfter);
        }

        return Optional.of(endResult);
    }

    /**
     * If node was added to config DS and it is already present in operational DS (connected) diff between current
     * new configuration and actual configuration (seen in operational) should be calculated and sent to device.
     */
    private ListenableFuture<Boolean> onNodeAdded(final InstanceIdentifier<FlowCapableNode> nodePath,
                                                  final FlowCapableNode dataAfter,
                                                  final FlowCapableNode operationalNode) {
        LOG.debug("Reconciliation {}: {}", dsType(), PathUtil.digNodeId(nodePath).getValue());
        final SyncupEntry syncupEntry = new SyncupEntry(dataAfter, dsType(), operationalNode, LogicalDatastoreType.OPERATIONAL);
        return reactor.syncup(nodePath, syncupEntry);
    }

    /**
     * Apply minimal changes very fast. For better performance needed just compare config
     * after+before. Config listener should not be dependent on operational flows/groups/meters while
     * updating config because operational store is highly async and it depends on another module in
     * system which is updating operational store (that components is also trying to solve
     * scale/performance issues on several layers).
     */
    private ListenableFuture<Boolean> onNodeUpdated(final InstanceIdentifier<FlowCapableNode> nodePath,
                                                    final FlowCapableNode dataBefore,
                                                    final FlowCapableNode dataAfter) {
        final SyncupEntry syncupEntry = new SyncupEntry(dataAfter, dsType(), dataBefore, dsType());
        return reactor.syncup(nodePath, syncupEntry);
    }

    /**
     * Remove values that are being deleted in the config from the switch.
     * Note, this could be probably optimized using dedicated wipe-out RPC.
     */
    private ListenableFuture<Boolean> onNodeDeleted(final InstanceIdentifier<FlowCapableNode> nodePath,
                                                    final FlowCapableNode dataBefore) {
        final SyncupEntry syncupEntry = new SyncupEntry(null, dsType(), dataBefore, dsType());
        return reactor.syncup(nodePath, syncupEntry);
    }

    @Override
    public LogicalDatastoreType dsType() {
        return LogicalDatastoreType.CONFIGURATION;
    }
}
