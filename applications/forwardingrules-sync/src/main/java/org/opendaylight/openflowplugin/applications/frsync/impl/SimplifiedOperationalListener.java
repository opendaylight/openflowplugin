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
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
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
    public void onDataTreeChanged(@Nonnull final Collection<DataTreeModification<Node>> modifications) {
        super.onDataTreeChanged(modifications);
    }

    /**
     * Update cache, register for device mastership when device connected and start reconciliation if device
     * is registered and actual modification is consistent.Skip the event otherwise.
     */
    protected Optional<ListenableFuture<Boolean>> processNodeModification(
            final DataTreeModification<Node> modification) {
        Optional<ListenableFuture<Boolean>> result;
        final NodeId nodeId = ModificationUtil.nodeId(modification);
        final DataObjectModification<Node> nodeModification = modification.getRootNode();

        if (isDelete(nodeModification) || isDeleteLogical(nodeModification)) {
            operationalSnapshot.updateCache(nodeId, Optional.absent());
            deviceMastershipManager.onDeviceDisconnected(nodeId);
            result = skipModification(modification);
        } else {
            operationalSnapshot.updateCache(nodeId, Optional.fromNullable(ModificationUtil.flowCapableNodeAfter(modification)));

            final boolean isAdd = isAdd(nodeModification) || isAddLogical(nodeModification);

            if (isAdd) {
                deviceMastershipManager.onDeviceConnected(nodeId);
            }

            // if node is registered for reconcile we need consistent data from operational DS (skip partial collections)
            // but we can accept first modification since all statistics are intentionally collected in one step on startup
            if (reconciliationRegistry.isRegistered(nodeId) && (isAdd || isConsistentForReconcile(modification))) {
                result = reconciliation(modification);
            } else {
                result = skipModification(modification);
            }
        }
        return result;
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

    private boolean isDelete(final DataObjectModification<Node> nodeModification) {
        return Objects.nonNull(nodeModification.getDataBefore()) && Objects.isNull(nodeModification.getDataAfter());
    }

    /**
     * All connectors disappeared from operational store (logical delete).
     */
    private boolean isDeleteLogical(final DataObjectModification<Node> nodeModification) {
        return !safeConnectorsEmpty(nodeModification.getDataBefore()) && safeConnectorsEmpty(nodeModification.getDataAfter());

    }

    private boolean isAdd(final DataObjectModification<Node> nodeModification) {
        return Objects.isNull(nodeModification.getDataBefore()) && Objects.nonNull(nodeModification.getDataAfter());
    }

    /**
     * All connectors appeared in operational store (logical add).
     */
    private boolean isAddLogical(final DataObjectModification<Node> nodeModification) {
        return safeConnectorsEmpty(nodeModification.getDataBefore()) && !safeConnectorsEmpty(nodeModification.getDataAfter());
    }

    /**
     * If node is present in config DS diff between wanted configuration (in config DS) and actual device
     * configuration (coming from operational) should be calculated and sent to device.
     * @param modification from DS
     * @return optional syncup future
     */
    private Optional<ListenableFuture<Boolean>> reconciliation(final DataTreeModification<Node> modification) {
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

    /**
     * Check if modification is consistent for reconciliation. We need fresh data, which means that current statistics
     * were collected after registration for reconcile and whole bunch of statistics was collected successfully.
     * @param modification from DS
     * @return status of modification
     */
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
