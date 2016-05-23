/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEnd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holder of registration request for fresh operational.
 */
public class SnapshotElicitRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(SnapshotElicitRegistry.class);
    private final Map<NodeId, Date> registration = new ConcurrentHashMap<>();
    public static final String DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public Date registerForNextConsistentOperationalSnapshot(NodeId nodeId) {
        Date timestamp = new Date();
        registration.put(nodeId, timestamp);
        LOG.debug("Registered for next consistent operational: {}", nodeId.getValue());
        return timestamp;
    }

    public Date unregisterForNextConsistentOperationalSnapshot(NodeId nodeId) {
        Date timestamp = registration.remove(nodeId);
        LOG.debug("Unregistered for next consistent operational: {}", nodeId.getValue());
        return timestamp;
    }

    public boolean isConsistent(DataTreeModification<Node> modification) {
        final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().getRootIdentifier());
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
            Date timestampOfRegistration = registration.get(nodeId);
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
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

    public boolean isRegistered(NodeId nodeId) {
        return registration.get(nodeId) != null;
    }

    public Map<NodeId, Date> getRegistration() {
        return this.registration;
    }

}
