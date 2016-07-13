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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.util.ModificationUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEnd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to operational new nodes and delegates add/remove/update/barrier to {@link SyncReactor}.
 */
public class SimplifiedOperationalListener extends AbstractFrmSyncListener<Node> {
    private static final Logger LOG = LoggerFactory.getLogger(SimplifiedOperationalListener.class);

    private final SyncReactor reactor;
    private final FlowCapableNodeSnapshotDao operationalSnapshot;
    private final FlowCapableNodeDao configDao;
    private final DeviceManager deviceManager;

    public SimplifiedOperationalListener(final SyncReactor reactor,
                                         final FlowCapableNodeSnapshotDao operationalSnapshot,
                                         final FlowCapableNodeDao configDao,
                                         final DeviceManager deviceManager) {
        this.reactor = reactor;
        this.operationalSnapshot = operationalSnapshot;
        this.configDao = configDao;
        this.deviceManager = deviceManager;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Node>> modifications) {
        LOG.trace("Operational changes: {}", modifications.size());
        super.onDataTreeChanged(modifications);
    }

    /**
     * This method behaves like this:
     * <ul>
     * <li>If node is added to operational store then reconciliation.</li>
     * <li>Node is deleted from operational cache is removed.</li>
     * <li>Skip this event otherwise.</li>
     * </ul>
     *
     * @throws InterruptedException from syncup
     */
    protected Optional<ListenableFuture<Boolean>> processNodeModification(
            DataTreeModification<Node> modification) throws InterruptedException {
        final NodeId nodeId = ModificationUtil.nodeId(modification);
        updateCache(modification);

        if (!deviceManager.isDeviceMastered(nodeId)) {
            LOG.trace("Skip modification since not master for: {}", nodeId.getValue());
            return Optional.absent();
        }

        if (isAdd(modification) || isAddLogical(modification)) {
            deviceManager.onDeviceConnected(nodeId);
        }

        if (isRegisteredAndConsistentForReconcile(modification)) {
            return reconciliation(modification);
        }
        return skipModification(modification);
    }

    /**
     * Remove if delete. Update only if FlowCapableNode Augmentation modified.
     *
     * @param modification Datastore modification
     * @return true for cache update, false for cache remove
     */
    protected boolean updateCache(DataTreeModification<Node> modification) {
        NodeId nodeId = ModificationUtil.nodeId(modification);
        if (isDelete(modification) || isDeleteLogical(modification)) {
            operationalSnapshot.updateCache(nodeId, Optional.absent());
            deviceManager.onDeviceDisconnected(nodeId);
            return false;
        }
        operationalSnapshot.updateCache(nodeId, Optional.fromNullable(ModificationUtil.flowCapableNodeAfter(modification)));
        return true;
    }

    private Optional<ListenableFuture<Boolean>> skipModification(DataTreeModification<Node> modification) {
        LOG.trace("Skipping operational modification: {}, before {}, after {}",
                ModificationUtil.nodeIdValue(modification),
                modification.getRootNode().getDataBefore() == null ? "null" : "nonnull",
                modification.getRootNode().getDataAfter() == null ? "null" : "nonnull");
        return Optional.absent();
    }

    /**
     * ModificationType.DELETE.
     */
    private boolean isDelete(DataTreeModification<Node> modification) {
        if (ModificationType.DELETE == modification.getRootNode().getModificationType()) {
            LOG.trace("Delete {} (physical)", ModificationUtil.nodeIdValue(modification));
            return true;
        }

        return false;
    }

    /**
     * All connectors disappeared from operational store (logical delete).
     */
    private boolean isDeleteLogical(DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        if (!safeConnectorsEmpty(rootNode.getDataBefore()) && safeConnectorsEmpty(rootNode.getDataAfter())) {
            LOG.trace("Delete {} (logical)", ModificationUtil.nodeIdValue(modification));
            return true;
        }

        return false;
    }

    private boolean isAdd(DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        final Node dataAfter = rootNode.getDataAfter();
        final Node dataBefore = rootNode.getDataBefore();

        final boolean nodeAppearedInOperational = dataBefore == null && dataAfter != null;
        if (nodeAppearedInOperational) {
            LOG.trace("Add {} (physical)", ModificationUtil.nodeIdValue(modification));
        }
        return nodeAppearedInOperational;
    }

    /**
     * All connectors appeared in operational store (logical add).
     */
    private boolean isAddLogical(DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        if (safeConnectorsEmpty(rootNode.getDataBefore()) && !safeConnectorsEmpty(rootNode.getDataAfter())) {
            LOG.trace("Add {} (logical)", ModificationUtil.nodeIdValue(modification));
            return true;
        }

        return false;
    }

    private Optional<ListenableFuture<Boolean>> reconciliation(DataTreeModification<Node> modification) throws InterruptedException {
        final NodeId nodeId = ModificationUtil.nodeId(modification);
        final Optional<FlowCapableNode> nodeConfiguration = configDao.loadByNodeId(nodeId);

        if (nodeConfiguration.isPresent()) {
            LOG.debug("Reconciliation: {}", nodeId.getValue());
            final InstanceIdentifier<FlowCapableNode> nodePath = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, new NodeKey(ModificationUtil.nodeId(modification)))
                    .augmentation(FlowCapableNode.class);
            final FlowCapableNode fcNode = ModificationUtil.flowCapableNodeAfter(modification);
            return Optional.of(reactor.syncup(nodePath, nodeConfiguration.get(), fcNode, dsType()));
        } else {
            LOG.debug("Config not present for reconciliation: {}", nodeId.getValue());
            return skipModification(modification);
        }
    }

    private boolean isRegisteredAndConsistentForReconcile(DataTreeModification<Node> modification) {
        final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().getRootIdentifier());

        if (!deviceManager.getReconciliationRegistry().isRegistered(nodeId)) {
            return false;
        }

        final FlowCapableStatisticsGatheringStatus gatheringStatus = modification.getRootNode().getDataAfter()
                .getAugmentation(FlowCapableStatisticsGatheringStatus.class);

        if (gatheringStatus == null) {
            LOG.trace("Statistics gathering never started: {}", nodeId.getValue());
            return false;
        }

        final SnapshotGatheringStatusEnd gatheringStatusEnd = gatheringStatus.getSnapshotGatheringStatusEnd();

        if (gatheringStatusEnd == null) {
            LOG.trace("Statistics gathering is not over yet: {}", nodeId.getValue());
            return false;
        }

        if (!gatheringStatusEnd.isSucceeded()) {
            LOG.debug("Statistics gathering was not successful: {}", nodeId.getValue());
            return false;
        }

        try {
            Date timestampOfRegistration = deviceManager.getReconciliationRegistry().getRegistration(nodeId);
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ReconciliationRegistry.DATE_AND_TIME_FORMAT);
            Date timestampOfStatistics = simpleDateFormat.parse(gatheringStatusEnd.getEnd().getValue());
            if (timestampOfStatistics.after(timestampOfRegistration)) {
                LOG.debug("Fresh operational present: {}", nodeId.getValue());
                return true;
            }
        } catch (ParseException e) {
            LOG.error("Timestamp parsing error {}", e);
        }
        LOG.debug("Fresh operational not present: {}", nodeId.getValue());
        return false;
    }

    private static boolean safeConnectorsEmpty(Node node) {
        if (node == null) {
            return true;
        }

        final List<NodeConnector> nodeConnectors = node.getNodeConnector();

        return nodeConnectors == null || nodeConnectors.isEmpty();
    }

    @Override
    public LogicalDatastoreType dsType() {
        return LogicalDatastoreType.OPERATIONAL;
    }
}
