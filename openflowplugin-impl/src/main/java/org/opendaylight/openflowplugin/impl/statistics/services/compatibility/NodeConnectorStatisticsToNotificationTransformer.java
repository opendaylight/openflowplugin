/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapKey;

/**
 * pulled out port stats to notification transformation
 */
public class NodeConnectorStatisticsToNotificationTransformer {

    private NodeConnectorStatisticsToNotificationTransformer() {
        // Hide implicit constructor
    }

    /**
     * @param mpReplyList   raw multipart response from device
     * @param deviceInfo    device basic info
     * @param ofVersion     device version
     * @param emulatedTxId
     * @return notification containing flow stats
     */
    public static NodeConnectorStatisticsUpdate transformToNotification(final List<MultipartReply> mpReplyList,
                                                                        final DeviceInfo deviceInfo,
                                                                        final OpenflowVersion ofVersion,
                                                                        final TransactionId emulatedTxId) {

        NodeConnectorStatisticsUpdateBuilder notification = new NodeConnectorStatisticsUpdateBuilder();
        notification.setId(deviceInfo.getNodeId());
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        notification.setNodeConnectorStatisticsAndPortNumberMap(new ArrayList<NodeConnectorStatisticsAndPortNumberMap>());
        for (MultipartReply mpReply : mpReplyList) {
            MultipartReplyPortStatsCase caseBody = (MultipartReplyPortStatsCase) mpReply.getMultipartReplyBody();

            MultipartReplyPortStats replyBody = caseBody.getMultipartReplyPortStats();
            for (PortStats portStats : replyBody.getPortStats()) {
                NodeConnectorStatisticsAndPortNumberMapBuilder statsBuilder =
                        processSingleNodeConnectorStats(deviceInfo, ofVersion, portStats);
                notification.getNodeConnectorStatisticsAndPortNumberMap().add(statsBuilder.build());
            }
        }
        return notification.build();
    }

    @VisibleForTesting
    static NodeConnectorStatisticsAndPortNumberMapBuilder processSingleNodeConnectorStats(DeviceInfo deviceInfo, OpenflowVersion ofVersion, PortStats portStats) {
        NodeConnectorStatisticsAndPortNumberMapBuilder statsBuilder =
                new NodeConnectorStatisticsAndPortNumberMapBuilder();
        statsBuilder.setNodeConnectorId(
                InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                        deviceInfo.getDatapathId(),
                        portStats.getPortNo(), ofVersion));

        BytesBuilder bytesBuilder = new BytesBuilder();
        bytesBuilder.setReceived(portStats.getRxBytes());
        bytesBuilder.setTransmitted(portStats.getTxBytes());
        statsBuilder.setBytes(bytesBuilder.build());

        PacketsBuilder packetsBuilder = new PacketsBuilder();
        packetsBuilder.setReceived(portStats.getRxPackets());
        packetsBuilder.setTransmitted(portStats.getTxPackets());
        statsBuilder.setPackets(packetsBuilder.build());

        DurationBuilder durationBuilder = new DurationBuilder();
        if (portStats.getDurationSec() != null) {
            durationBuilder.setSecond(new Counter32(portStats.getDurationSec()));
        }
        if (portStats.getDurationNsec() != null) {
            durationBuilder.setNanosecond(new Counter32(portStats.getDurationNsec()));
        }
        statsBuilder.setDuration(durationBuilder.build());
        statsBuilder.setCollisionCount(portStats.getCollisions());
        statsBuilder.setKey(new NodeConnectorStatisticsAndPortNumberMapKey(statsBuilder.getNodeConnectorId()));
        statsBuilder.setReceiveCrcError(portStats.getRxCrcErr());
        statsBuilder.setReceiveDrops(portStats.getRxDropped());
        statsBuilder.setReceiveErrors(portStats.getRxErrors());
        statsBuilder.setReceiveFrameError(portStats.getRxFrameErr());
        statsBuilder.setReceiveOverRunError(portStats.getRxOverErr());
        statsBuilder.setTransmitDrops(portStats.getTxDropped());
        statsBuilder.setTransmitErrors(portStats.getTxErrors());
        return statsBuilder;
    }
}
