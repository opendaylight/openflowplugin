/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import com.google.common.annotations.VisibleForTesting;
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
import org.opendaylight.yangtools.binding.util.BindingMap;

/**
 * Pulled out port stats to notification transformation.
 */
public final class NodeConnectorStatisticsToNotificationTransformer {

    private NodeConnectorStatisticsToNotificationTransformer() {
        // Hide implicit constructor
    }

    /**
     * Transform statistics to notification.
     *
     * @param mpReplyList   raw multipart response from device
     * @param deviceInfo    device basic info
     * @param ofVersion     device version
     * @param emulatedTxId  emulated transaction Id
     * @return notification containing flow stats
     */
    public static NodeConnectorStatisticsUpdate transformToNotification(final List<MultipartReply> mpReplyList,
                                                                        final DeviceInfo deviceInfo,
                                                                        final OpenflowVersion ofVersion,
                                                                        final TransactionId emulatedTxId) {
        final var stats = BindingMap.<NodeConnectorStatisticsAndPortNumberMapKey,
            NodeConnectorStatisticsAndPortNumberMap>orderedBuilder();
        for (MultipartReply mpReply : mpReplyList) {
            MultipartReplyPortStatsCase caseBody = (MultipartReplyPortStatsCase) mpReply.getMultipartReplyBody();

            MultipartReplyPortStats replyBody = caseBody.getMultipartReplyPortStats();
            for (PortStats portStats : replyBody.getPortStats()) {
                stats.add(processSingleNodeConnectorStats(deviceInfo, ofVersion, portStats).build());
            }
        }
        return new NodeConnectorStatisticsUpdateBuilder()
            .setId(deviceInfo.getNodeId())
            .setMoreReplies(Boolean.FALSE)
            .setTransactionId(emulatedTxId)
            .setNodeConnectorStatisticsAndPortNumberMap(stats.build())
            .build();
    }

    @VisibleForTesting
    static NodeConnectorStatisticsAndPortNumberMapBuilder processSingleNodeConnectorStats(final DeviceInfo deviceInfo,
            final OpenflowVersion ofVersion, final PortStats portStats) {
        DurationBuilder durationBuilder = new DurationBuilder();
        if (portStats.getDurationSec() != null) {
            durationBuilder.setSecond(new Counter32(portStats.getDurationSec()));
        }
        if (portStats.getDurationNsec() != null) {
            durationBuilder.setNanosecond(new Counter32(portStats.getDurationNsec()));
        }

        return new NodeConnectorStatisticsAndPortNumberMapBuilder()
            .setNodeConnectorId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(deviceInfo.getDatapathId(),
                portStats.getPortNo(), ofVersion))
            .setBytes(new BytesBuilder()
                .setReceived(portStats.getRxBytes())
                .setTransmitted(portStats.getTxBytes())
                .build())
            .setPackets(new PacketsBuilder()
                .setReceived(portStats.getRxPackets())
                .setTransmitted(portStats.getTxPackets()).build())
            .setDuration(durationBuilder.build())
            .setCollisionCount(portStats.getCollisions())
            .setReceiveCrcError(portStats.getRxCrcErr())
            .setReceiveDrops(portStats.getRxDropped())
            .setReceiveErrors(portStats.getRxErrors())
            .setReceiveFrameError(portStats.getRxFrameErr())
            .setReceiveOverRunError(portStats.getRxOverErr())
            .setTransmitDrops(portStats.getTxDropped())
            .setTransmitErrors(portStats.getTxErrors());
    }
}
