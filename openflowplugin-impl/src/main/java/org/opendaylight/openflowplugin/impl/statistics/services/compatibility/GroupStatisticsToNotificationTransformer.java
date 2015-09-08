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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.GroupStatsResponseConvertor;
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

    private static GroupStatsResponseConvertor groupStatsConvertor = new GroupStatsResponseConvertor();

    /**
     * @param mpReplyList   raw multipart response from device
     * @param deviceContext device context
     * @param ofVersion     device version
     * @param emulatedTxId
     * @return notification containing flow stats
     */
    public static GroupStatisticsUpdated transformToNotification(final List<MultipartReply> mpReplyList,
                                                                 final DeviceContext deviceContext,
                                                                 final OpenflowVersion ofVersion,
                                                                 final TransactionId emulatedTxId) {

        GroupStatisticsUpdatedBuilder notification = new GroupStatisticsUpdatedBuilder();
        notification.setId(deviceContext.getDeviceState().getNodeId());
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        notification.setGroupStats(new ArrayList<GroupStats>());

        for (MultipartReply mpReply : mpReplyList) {
            MultipartReplyGroupCase caseBody = (MultipartReplyGroupCase) mpReply.getMultipartReplyBody();
            MultipartReplyGroup replyBody = caseBody.getMultipartReplyGroup();
            notification.getGroupStats().addAll(groupStatsConvertor.toSALGroupStatsList(replyBody.getGroupStats()));
        }
        return notification.build();
    }
}
