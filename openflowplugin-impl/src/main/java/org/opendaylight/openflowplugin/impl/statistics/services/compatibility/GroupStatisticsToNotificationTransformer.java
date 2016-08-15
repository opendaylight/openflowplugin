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
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;

/**
 * pulled out group stats to notification transformation
 */
public class GroupStatisticsToNotificationTransformer {
    private GroupStatisticsToNotificationTransformer() {
        // Hide implicit constructor
    }
    /**
     * @param mpReplyList   raw multipart response from device
     * @param deviceInfo   device state
     * @param emulatedTxId
     * @param convertorExecutor
     * @return notification containing flow stats
     */
    public static GroupStatisticsUpdated transformToNotification(final List<MultipartReply> mpReplyList,
                                                                 final DeviceInfo deviceInfo,
                                                                 final TransactionId emulatedTxId,
                                                                 final ConvertorExecutor convertorExecutor) {

        VersionConvertorData data = new VersionConvertorData(deviceInfo.getVersion());
        GroupStatisticsUpdatedBuilder notification = new GroupStatisticsUpdatedBuilder();
        notification.setId(deviceInfo.getNodeId());
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        notification.setGroupStats(new ArrayList<GroupStats>());

        for (MultipartReply mpReply : mpReplyList) {
            MultipartReplyGroupCase caseBody = (MultipartReplyGroupCase) mpReply.getMultipartReplyBody();
            MultipartReplyGroup replyBody = caseBody.getMultipartReplyGroup();
            final Optional<List<GroupStats>> groupStatsList = convertorExecutor.convert(
                    replyBody.getGroupStats(), data);

            if (groupStatsList.isPresent()) {
                notification.getGroupStats().addAll(groupStatsList.get());
            }
        }
        return notification.build();
    }
}
