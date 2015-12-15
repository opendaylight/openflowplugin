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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterStatsResponseConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;

/**
 * pulled out meter stats to notification transformation
 */
public class MeterStatisticsToNotificationTransformer {

    private static MeterStatsResponseConvertor meterStatsConvertor = new MeterStatsResponseConvertor();

    /**
     * @param mpReplyList   raw multipart response from device
     * @param deviceContext device context
     * @param ofVersion     device version
     * @param emulatedTxId
     * @return notification containing flow stats
     */
    public static MeterStatisticsUpdated transformToNotification(final List<MultipartReply> mpReplyList,
                                                                 final DeviceContext deviceContext,
                                                                 final OpenflowVersion ofVersion,
                                                                 final TransactionId emulatedTxId) {

        MeterStatisticsUpdatedBuilder notification = new MeterStatisticsUpdatedBuilder();
        notification.setId(deviceContext.getDeviceState().getNodeId());
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        notification.setMeterStats(new ArrayList<MeterStats>());
        for (MultipartReply mpReply : mpReplyList) {
            MultipartReplyMeterCase caseBody = (MultipartReplyMeterCase) mpReply.getMultipartReplyBody();
            MultipartReplyMeter replyBody = caseBody.getMultipartReplyMeter();
            notification.getMeterStats().addAll(meterStatsConvertor.toSALMeterStatsList(replyBody.getMeterStats()));
        }

        return notification.build();
    }
}
