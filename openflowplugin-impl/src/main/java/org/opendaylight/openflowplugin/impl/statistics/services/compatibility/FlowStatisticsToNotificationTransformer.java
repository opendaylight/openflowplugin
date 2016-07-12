/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;

/**
 * pulled out flow stats to notification transformation
 */
public class FlowStatisticsToNotificationTransformer {
    /**
     * @param mpResult      raw multipart response from device
     * @param deviceInfo   device state
     * @param ofVersion     device version
     * @param emulatedTxId
     * @param convertorManager
     * @return notification containing flow stats
     */
    public static FlowsStatisticsUpdate transformToNotification(final List<MultipartReply> mpResult,
                                                                final DeviceInfo deviceInfo,
                                                                final OpenflowVersion ofVersion,
                                                                final TransactionId emulatedTxId,
                                                                final ConvertorManager convertorManager) {
        final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(ofVersion.getVersion());
        data.setDatapathId(deviceInfo.getDatapathId());
        final FlowsStatisticsUpdateBuilder notification = new FlowsStatisticsUpdateBuilder();
        final List<FlowAndStatisticsMapList> statsList = new ArrayList<>();
        notification.setId(deviceInfo.getNodeId());
        notification.setFlowAndStatisticsMapList(statsList);
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        for (MultipartReply mpRawReply : mpResult) {
            Preconditions.checkArgument(MultipartType.OFPMPFLOW.equals(mpRawReply.getType()));

            MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase) mpRawReply.getMultipartReplyBody();
            MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();
            final Optional<List<FlowAndStatisticsMapList>> outStatsItem =
                    convertorManager.convert(replyBody.getFlowStats(), data);


            if (outStatsItem.isPresent()) {
                statsList.addAll(outStatsItem.get());
            }
        }

        return notification.build();
    }
}
