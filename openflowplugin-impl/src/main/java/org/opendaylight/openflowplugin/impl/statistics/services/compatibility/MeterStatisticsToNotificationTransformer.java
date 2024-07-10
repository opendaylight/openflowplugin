/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yangtools.binding.util.BindingMap;

/**
 * Pulled out meter stats to notification transformation.
 */
public final class MeterStatisticsToNotificationTransformer {

    private MeterStatisticsToNotificationTransformer() {
        // Hide implicit constructor
    }

    /**
     * Transform statistics to notification.
     *
     * @param mpReplyList   raw multipart response from device
     * @param deviceInfo    device state
     * @param ofVersion     device version
     * @param emulatedTxId  emulated transaction Id
     * @param convertorExecutor convertor executor
     * @return notification containing flow stats
     */
    public static MeterStatisticsUpdated transformToNotification(final List<MultipartReply> mpReplyList,
                                                                 final DeviceInfo deviceInfo,
                                                                 final OpenflowVersion ofVersion,
                                                                 final TransactionId emulatedTxId,
                                                                 final ConvertorExecutor convertorExecutor) {

        VersionConvertorData data = new VersionConvertorData(deviceInfo.getVersion());
        final var stats = BindingMap.<MeterStatsKey, MeterStats>orderedBuilder();
        for (MultipartReply mpReply : mpReplyList) {
            MultipartReplyMeterCase caseBody = (MultipartReplyMeterCase) mpReply.getMultipartReplyBody();
            MultipartReplyMeter replyBody = caseBody.getMultipartReplyMeter();
            final Optional<List<MeterStats>> meterStatsList =
                    convertorExecutor.convert(replyBody.getMeterStats(), data);

            meterStatsList.ifPresent(stats::addAll);
        }

        return new MeterStatisticsUpdatedBuilder()
            .setId(deviceInfo.getNodeId())
            .setMoreReplies(Boolean.FALSE)
            .setTransactionId(emulatedTxId)
            .setMeterStats(stats.build())
            .build();
    }
}
