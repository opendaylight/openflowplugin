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
    private final FlowCapableNodeSnapshotDao configSnaphot;
    private final FlowCapableNodeDao operationalDao;

    public SimplifiedConfigListener(final SyncReactor reactor, FlowCapableNodeSnapshotDao configSnaphot,
            FlowCapableNodeDao operationalDao) {
        this.reactor = reactor;
        this.configSnaphot = configSnaphot;
        this.operationalDao = operationalDao;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<FlowCapableNode>> modifications) {
        LOG.trace("Inventory Config changes {}", modifications.size());
        super.onDataTreeChanged(modifications);
    }
    
    /**
     * Compare cached operational with current config modification. If operational is not present
     * skip calling Inventory RPCs.
     * 
     * @throws InterruptedException from syncup
     */
    protected Optional<ListenableFuture<Boolean>> processNodeModification(
            DataTreeModification<FlowCapableNode> modification) throws InterruptedException {
        final InstanceIdentifier<FlowCapableNode> nodePath = modification.getRootPath().getRootIdentifier();
        final NodeId nodeId = PathUtil.digNodeId(nodePath);

        configSnaphot.updateCache(nodeId, Optional.fromNullable(modification.getRootNode().getDataAfter()));


        final Optional<FlowCapableNode> operationalNode = operationalDao.loadByNodeId(nodeId);
        if (!operationalNode.isPresent()) {
            LOG.info("Skip syncup, {} operational is not present", nodeId.getValue());
            return Optional.absent();
        }

        final DataObjectModification<FlowCapableNode> configModification = modification.getRootNode();
        final FlowCapableNode dataBefore = configModification.getDataBefore();
        final FlowCapableNode dataAfter = configModification.getDataAfter();
        final ListenableFuture<Boolean> endResult;
        if (dataBefore == null && dataAfter != null) {
            endResult = onNodeAdded(nodePath, dataBefore, dataAfter, operationalNode.get());
        } else if (dataBefore != null && dataAfter == null) {
            endResult = onNodeDeleted(nodePath, dataBefore, operationalNode.get());
        } else {
            endResult = onNodeUpdated(nodePath, dataBefore, dataAfter, operationalNode.get());
        }

        return Optional.of(endResult);
    }

    /**
     * Add only what is missing in operational store. Config. node could be added in two situations:
     * <ul>
     * <li>Note very first time after restart was handled by operational listener. Syncup should
     * calculate no delta (we don want to reconfigure switch if not necessary).</li>
     * <li>But later the config. node could be deleted, after that config node added again. Syncup
     * should calculate that everything needs to be added. Operational store should be empty in
     * optimal case (but the switch could be reprogrammed by another person/system.</li>
     * </ul>
     */
    protected ListenableFuture<Boolean> onNodeAdded(InstanceIdentifier<FlowCapableNode> nodePath,
            FlowCapableNode dataBefore, FlowCapableNode dataAfter, FlowCapableNode operationalNode)
                    throws InterruptedException {
        LOG.trace("onNodeAdded {}", nodePath);
        
        final ListenableFuture<Boolean> endResult =
                reactor.syncup(nodePath, dataAfter, operationalNode, dsType());
        return endResult;
    }

    /**
     * Apply minimal changes very fast. For better performance needed just compare config
     * after+before. Config listener should not be dependent on operational flows/groups while
     * updating config because operational store is highly async and it depends on another module in
     * system which is updating operational store (that components is also trying to solve
     * scale/performance issues on several layers).
     */
    protected ListenableFuture<Boolean> onNodeUpdated(InstanceIdentifier<FlowCapableNode> nodePath,
            FlowCapableNode dataBefore, FlowCapableNode dataAfter, FlowCapableNode operationalNodeNode)
                    throws InterruptedException {
        LOG.trace("onNodeUpdated {}", nodePath);
        
        final ListenableFuture<Boolean> endResult =
                reactor.syncup(nodePath, dataAfter, dataBefore, dsType());
        return endResult;
    }

    /**
     * Remove values that are being deleted in the config from the switch. Note, this could be
     * probably optimized using dedicated wipe-out RPC, but it has impact on switch if it is
     * programmed by two person/system
     */
    protected ListenableFuture<Boolean> onNodeDeleted(InstanceIdentifier<FlowCapableNode> nodePath,
            FlowCapableNode dataBefore, FlowCapableNode operationalNode) throws InterruptedException {
        LOG.trace("onNodeDeleted {}", nodePath);
        
        final ListenableFuture<Boolean> endResult =
                reactor.syncup(nodePath, null, dataBefore, dsType());
        return endResult;
    }

    @Override
    public LogicalDatastoreType dsType() {
        return LogicalDatastoreType.CONFIGURATION;
    }
}
