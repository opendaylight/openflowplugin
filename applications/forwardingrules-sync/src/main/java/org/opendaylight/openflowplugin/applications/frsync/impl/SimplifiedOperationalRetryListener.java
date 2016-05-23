/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.annotations.VisibleForTesting;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.RetryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEnd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modified {@link SimplifiedOperationalListener} for usage of retry mechanism.
 */
public class SimplifiedOperationalRetryListener extends SimplifiedOperationalListener {

    private static final Logger LOG = LoggerFactory.getLogger(SimplifiedOperationalRetryListener.class);
    private final RetryRegistry retryRegistry;

    public SimplifiedOperationalRetryListener(SyncReactor reactor, FlowCapableNodeSnapshotDao operationalSnapshot,
                                              FlowCapableNodeDao configDao, RetryRegistry retryRegistry) {
        super(reactor, operationalSnapshot, configDao);
        this.retryRegistry = retryRegistry;
    }

    /**
     * Adding condition check for retry.
     *
     * @param modification operational datastore modification
     * @return true if reconciliation is needed, false otherwise
     */
    protected boolean isReconciliationNeeded(DataTreeModification<Node> modification) {
        return super.isReconciliationNeeded(modification) || isRegisteredAndConsistentForRetry(modification);
    }

    /**
     * If node is removed unregister for retry in addition.
     *
     * @param modification operational datastore modification
     * @return true for cache update, false for cache remove and retry unregister
     */
    protected boolean updateCache(DataTreeModification<Node> modification) {
        boolean nodeUpdated = super.updateCache(modification);
        if (!nodeUpdated) { // node removed if not updated
            retryRegistry.unregisterIfRegistered(nodeId(modification));
        }
        return nodeUpdated;
    }

    /**
     * Check if retry should be proceeded.
     *
     * @param modification operational modification
     * @return true if device is registered for retry and actual modification is consistent, false otherwise
     */
    @VisibleForTesting
    boolean isRegisteredAndConsistentForRetry(DataTreeModification<Node> modification) {
        final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().getRootIdentifier());

        if (!retryRegistry.isRegistered(nodeId)) {
            return false;
        }

        final FlowCapableStatisticsGatheringStatus gatheringStatus = modification.getRootNode().getDataAfter()
                .getAugmentation(FlowCapableStatisticsGatheringStatus.class);

        if (gatheringStatus == null) {
            LOG.trace("Statistics gathering never started for: {}", nodeId.getValue());
            return false;
        }

        final SnapshotGatheringStatusEnd gatheringStatusEnd = gatheringStatus.getSnapshotGatheringStatusEnd();

        if (gatheringStatusEnd == null) {
            LOG.trace("Statistics gathering is not over yet for: {}", nodeId.getValue());
            return false;
        }

        if (!gatheringStatusEnd.isSucceeded()) {
            LOG.debug("Statistics gathering was not successful for: {}", nodeId.getValue());
            return false;
        }

        try {
            Date timestampOfRegistration = retryRegistry.getRegistration(nodeId);;
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RetryRegistry.DATE_AND_TIME_FORMAT);
            Date timestampOfStatistics = simpleDateFormat.parse(gatheringStatusEnd.getEnd().getValue());
            if (timestampOfStatistics.after(timestampOfRegistration)) {
                LOG.debug("Fresh operational present for: {} -> going retry!", nodeId.getValue());
                return true;
            }
        } catch (ParseException e) {
            LOG.error("Timestamp parsing error {}", e);
        }
        LOG.debug("Fresh operational not present for: {}", nodeId.getValue());
        return false;
    }
}
