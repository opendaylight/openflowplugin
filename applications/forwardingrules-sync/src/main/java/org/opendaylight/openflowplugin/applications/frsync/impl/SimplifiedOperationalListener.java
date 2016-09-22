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
import org.opendaylight.openflowplugin.applications.frsync.impl.clustering.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frsync.util.ModificationUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
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
 * Listens to operational changes and starts reconciliation through {@link SyncReactor} when necessary.
 */
public class SimplifiedOperationalListener extends AbstractFrmSyncListener<Node> {
    private static final Logger LOG = LoggerFactory.getLogger(SimplifiedOperationalListener.class);
    public static final String DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private final SyncReactor reactor;
    private final FlowCapableNodeSnapshotDao operationalSnapshot;
    private final FlowCapableNodeDao configDao;
    private final ReconciliationRegistry reconciliationRegistry;
    private final DeviceMastershipManager deviceMastershipManager;

    public SimplifiedOperationalListener(final SyncReactor reactor,
                                         final FlowCapableNodeSnapshotDao operationalSnapshot,
                                         final FlowCapableNodeDao configDao,
                                         final ReconciliationRegistry reconciliationRegistry,
                                         final DeviceMastershipManager deviceMastershipManager) {
        this.reactor = reactor;
        this.operationalSnapshot = operationalSnapshot;
        this.configDao = configDao;
        this.reconciliationRegistry = reconciliationRegistry;
        this.deviceMastershipManager = deviceMastershipManager;
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<Node>> modifications) {
        super.onDataTreeChanged(modifications);
    }

    /**
     * Update cache, register for device masterhip when device connected and start reconciliation if device
     * is registered and actual modification is consistent.Skip the event otherwise.
     * @throws InterruptedException from syncup
     */
    protected Optional<ListenableFuture<Boolean>> processNodeModification(
            final DataTreeModification<Node> modification) throws InterruptedException {
        final NodeId nodeId = ModificationUtil.nodeId(modification);
        updateCache(modification);

        if (isAdd(modification) || isAddLogical(modification)) {
            deviceMastershipManager.onDeviceConnected(nodeId);
        }

        if (reconciliationRegistry.isRegistered(nodeId) && isConsistentForReconcile(modification)) {
            return reconciliation(modification);
        } else {
            return skipModification(modification);
        }
    }

    /**
     * Remove if delete. Update only if FlowCapableNode Augmentation modified.
     * Unregister for device mastership.
     * @param modification Datastore modification
     */
    private void updateCache(final DataTreeModification<Node> modification) {
        NodeId nodeId = ModificationUtil.nodeId(modification);
        if (isDelete(modification) || isDeleteLogical(modification)) {
            operationalSnapshot.updateCache(nodeId, Optional.absent());
            deviceMastershipManager.onDeviceDisconnected(nodeId);
            return;
        }
        operationalSnapshot.updateCache(nodeId, Optional.fromNullable(ModificationUtil.flowCapableNodeAfter(modification)));
    }

    private Optional<ListenableFuture<Boolean>> skipModification(final DataTreeModification<Node> modification) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Skipping operational modification: {}, before {}, after {}",
                    ModificationUtil.nodeIdValue(modification),
                    modification.getRootNode().getDataBefore() == null ? "null" : "nonnull",
                    modification.getRootNode().getDataAfter() == null ? "null" : "nonnull");
        }
        return Optional.absent();
    }

    /**
     * ModificationType.DELETE.
     */
    private boolean isDelete(final DataTreeModification<Node> modification) {
        return ModificationType.DELETE == modification.getRootNode().getModificationType();
    }

    /**
     * All connectors disappeared from operational store (logical delete).
     */
    private boolean isDeleteLogical(final DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        return !safeConnectorsEmpty(rootNode.getDataBefore()) && safeConnectorsEmpty(rootNode.getDataAfter());

    }

    private boolean isAdd(final DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        return rootNode.getDataBefore() == null && rootNode.getDataAfter() != null;
    }

    /**
     * All connectors appeared in operational store (logical add).
     */
    private boolean isAddLogical(final DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        return safeConnectorsEmpty(rootNode.getDataBefore()) && !safeConnectorsEmpty(rootNode.getDataAfter());
    }

    /**
     * If node is present in config DS diff between wanted configuration (in config DS) and actual device
     * configuration (coming from operational) should be calculated and sent to device.
     * @param modification from DS
     * @return optional syncup future
     * @throws InterruptedException from syncup
     */
    private Optional<ListenableFuture<Boolean>> reconciliation(final DataTreeModification<Node> modification)
            throws InterruptedException {
        final NodeId nodeId = ModificationUtil.nodeId(modification);
        final Optional<FlowCapableNode> nodeConfiguration = configDao.loadByNodeId(nodeId);

        if (nodeConfiguration.isPresent()) {
            LOG.debug("Reconciliation {}: {}", dsType(), nodeId.getValue());
            final InstanceIdentifier<FlowCapableNode> nodePath = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, new NodeKey(ModificationUtil.nodeId(modification)))
                    .augmentation(FlowCapableNode.class);
            final FlowCapableNode fcOperationalNode = ModificationUtil.flowCapableNodeAfter(modification);
            final SyncupEntry syncupEntry = new SyncupEntry(nodeConfiguration.get(), LogicalDatastoreType.CONFIGURATION,
                                                            fcOperationalNode, dsType());
            return Optional.of(reactor.syncup(nodePath, syncupEntry));
        } else {
            LOG.debug("Config not present for reconciliation: {}", nodeId.getValue());
            reconciliationRegistry.unregisterIfRegistered(nodeId);
            return skipModification(modification);
        }
    }

    private boolean isConsistentForReconcile(final DataTreeModification<Node> modification) {
        final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().getRootIdentifier());
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
            LOG.trace("Statistics gathering was not successful: {}", nodeId.getValue());
            return false;
        }

        try {
            Date timestampOfRegistration = reconciliationRegistry.getRegistrationTimestamp(nodeId);
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
            Date timestampOfStatistics = simpleDateFormat.parse(gatheringStatusEnd.getEnd().getValue());
            if (timestampOfStatistics.after(timestampOfRegistration)) {
                LOG.debug("Fresh operational present: {}", nodeId.getValue());
                return true;
            }
        } catch (ParseException e) {
            LOG.warn("Timestamp parsing error {}", e);
        }
        LOG.debug("Fresh operational not present: {}", nodeId.getValue());
        return false;
    }

    private static boolean safeConnectorsEmpty(final Node node) {
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
