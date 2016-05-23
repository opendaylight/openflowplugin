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
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.OpendaylightDirectStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holder of registration request for fresh operational.
 */
public class SnapshotElicitRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(SnapshotElicitRegistry.class);
    private final OpendaylightDirectStatisticsService directStatisticsService;
    private Map<NodeId, Date> registration;
    public static String DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public SnapshotElicitRegistry(final OpendaylightDirectStatisticsService directStatisticsService) {
        this.registration = new HashMap<>();
        this.directStatisticsService = directStatisticsService;
    }

    public void registerForNextConsistentOperationalSnapshot(NodeId nodeId) {
        registration.put(nodeId, new Date());
        LOG.debug("Registered for next consistent operational for node {}", nodeId.getValue());
    }

    public void unregisterForNextConsistentOperationalSnapshot(NodeId nodeId) {
        registration.remove(nodeId);
        LOG.debug("Unregistered for next consistent operational for node {}", nodeId.getValue());
    }

    public boolean isConsistent(DataTreeModification<Node> modification) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
        final NodeId nodeId = PathUtil.digNodeId(modification.getRootPath().getRootIdentifier());
        FlowCapableStatisticsGatheringStatus status = modification.getRootNode().getDataAfter().getAugmentation(FlowCapableStatisticsGatheringStatus.class);

        if (!status.getSnapshotGatheringStatusEnd().isSucceeded()) {
            LOG.debug("Statistics gathering was not successful.");
            return false;
        }

        try {
            Date timestampOfRegistration = registration.get(nodeId);
            Date timestampOfStatistics = simpleDateFormat.parse(status.getSnapshotGatheringStatusEnd().getEnd().getValue());
            if (timestampOfStatistics.after(timestampOfRegistration)) {
                LOG.debug("Fresh operational present for {}", nodeId.getValue());
                return true;
            }
        } catch (ParseException e) {
            LOG.error("Timestamp parsing error {}", e);
        }
        LOG.debug("Fresh operational not present for {}", nodeId.getValue());
        return false;
    }

    public boolean isRegistered(NodeId nodeId) {
        return registration.get(nodeId) == null ? false : true;
    }

    public void triggerStatisticsGathering(NodeId nodeId) {
        NodeRef nodeRef = new NodeRef(InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId)));
        directStatisticsService.getFlowStatistics(new GetFlowStatisticsInputBuilder().setNode(nodeRef).build());
        directStatisticsService.getGroupStatistics(new GetGroupStatisticsInputBuilder().setNode(nodeRef).build());
        directStatisticsService.getMeterStatistics(new GetMeterStatisticsInputBuilder().setNode(nodeRef).build());
        directStatisticsService.getNodeConnectorStatistics(new GetNodeConnectorStatisticsInputBuilder().setNode(nodeRef).build());
        directStatisticsService.getQueueStatistics(new GetQueueStatisticsInputBuilder().setNode(nodeRef).build());
    }
}
