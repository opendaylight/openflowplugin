/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;

/**
 * pulled out queue stats to notification transformation
 */
public class QueueStatisticsToNotificationTransformer {

    private QueueStatisticsToNotificationTransformer() {
        // Hide implicit constructor
    }

    /**
     * @param mpReplyList   raw multipart response from device
     * @param deviceInfo   device state
     * @param ofVersion     device version
     * @param emulatedTxId
     * @return notification containing flow stats
     */
    public static QueueStatisticsUpdate transformToNotification(final List<MultipartReply> mpReplyList,
                                                                final DeviceInfo deviceInfo,
                                                                final OpenflowVersion ofVersion,
                                                                final TransactionId emulatedTxId) {

        QueueStatisticsUpdateBuilder notification = new QueueStatisticsUpdateBuilder();
        notification.setId(deviceInfo.getNodeId());
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        notification.setQueueIdAndStatisticsMap(new ArrayList<QueueIdAndStatisticsMap>());
        for (MultipartReply mpReply : mpReplyList) {

            MultipartReplyQueueCase caseBody = (MultipartReplyQueueCase) mpReply.getMultipartReplyBody();
            MultipartReplyQueue replyBody = caseBody.getMultipartReplyQueue();

            for (QueueStats queueStats : replyBody.getQueueStats()) {

                QueueIdAndStatisticsMapBuilder statsBuilder =
                        new QueueIdAndStatisticsMapBuilder();
                statsBuilder.setNodeConnectorId(
                        InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                                deviceInfo.getDatapathId(),
                                queueStats.getPortNo(), ofVersion));
                statsBuilder.setTransmissionErrors(new Counter64(queueStats.getTxErrors()));
                statsBuilder.setTransmittedBytes(new Counter64(queueStats.getTxBytes()));
                statsBuilder.setTransmittedPackets(new Counter64(queueStats.getTxPackets()));

                DurationBuilder durationBuilder = new DurationBuilder();
                durationBuilder.setSecond(new Counter32(queueStats.getDurationSec()));
                durationBuilder.setNanosecond(new Counter32(queueStats.getDurationNsec()));
                statsBuilder.setDuration(durationBuilder.build());

                statsBuilder.setQueueId(new QueueId(queueStats.getQueueId()));

                notification.getQueueIdAndStatisticsMap().add(statsBuilder.build());
            }
        }
        return notification.build();
    }


}
