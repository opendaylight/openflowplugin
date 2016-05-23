/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.RetryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
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
     * This method behaves like this:
     * <ul>
     * <li>If node is added to operational store then reconciliation.</li>
     * <li>If node is in retry then reconciliation.</li>
     * <li>Node is deleted from operational, cache is removed.</li>
     * <li>Skip this event otherwise.</li>
     * </ul>
     *
     * @throws InterruptedException from syncup
     */
    protected Optional<ListenableFuture<Boolean>> processNodeModification(
            DataTreeModification<Node> modification) throws InterruptedException {

        updateCache(modification);
        if (isAdd(modification) || isAddLogical(modification) || inRetry(modification)) {
            return reconciliation(modification);
        }
        return skipModification(modification);
    }

    /**
     * Remove from cache if delete and unregister for retry.
     * Update only if FlowCapableNode Augmentation modified.
     *
     * @param modification Datastore modification
     */
    protected void updateCache(DataTreeModification<Node> modification) {
        try {
            if (isDelete(modification) || isDeleteLogical(modification)) {
                operationalSnapshot.updateCache(nodeId(modification), Optional.<FlowCapableNode>absent());
                retryRegistry.unregisterIfRegistered(nodeId(modification));
                return;
            }
            operationalSnapshot.updateCache(nodeId(modification), Optional.fromNullable(flowCapableNodeAfter(modification)));
        } catch(Exception e) {
            LOG.error("update cache failed {}", nodeId(modification), e);
        }
    }

    /**
     * Check if retry should be proceeded.
     */
    protected boolean inRetry(DataTreeModification<Node> modification) {
        final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().getRootIdentifier());
        if (isRegisteredAndConsistentForRetry(modification)) {
            retryRegistry.unregisterIfRegistered(nodeId);
            LOG.debug("Retry: {}", nodeId.getValue());
            return true;
        }
        return false;
    }

    /**
     * If device is registered for retry, check if modification is consistent.
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
                LOG.debug("Fresh operational present for: {}", nodeId.getValue());
                return true;
            }
        } catch (ParseException e) {
            LOG.error("Timestamp parsing error {}", e);
        }
        LOG.debug("Fresh operational not present for: {}", nodeId.getValue());
        return false;
    }
}
