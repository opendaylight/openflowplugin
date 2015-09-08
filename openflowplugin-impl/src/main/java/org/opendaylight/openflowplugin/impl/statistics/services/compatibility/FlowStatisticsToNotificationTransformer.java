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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowStatsResponseConvertor;
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

    private static FlowStatsResponseConvertor flowStatsConvertor = new FlowStatsResponseConvertor();

    /**
     * @param mpResult      raw multipart response from device
     * @param deviceContext device context
     * @param ofVersion device version
     * @param emulatedTxId
     * @return notification containing flow stats
     */
    public static FlowsStatisticsUpdate transformToNotification(final List<MultipartReply> mpResult,
                                                                final DeviceContext deviceContext,
                                                                final OpenflowVersion ofVersion,
                                                                final TransactionId emulatedTxId) {
        final FlowsStatisticsUpdateBuilder notification = new FlowsStatisticsUpdateBuilder();
        final List<FlowAndStatisticsMapList> statsList = new ArrayList<>();
        notification.setId(deviceContext.getDeviceState().getNodeId());
        notification.setFlowAndStatisticsMapList(statsList);
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        for (MultipartReply mpRawReply : mpResult) {
            Preconditions.checkArgument(MultipartType.OFPMPFLOW.equals(mpRawReply.getType()));

            MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase) mpRawReply.getMultipartReplyBody();
            MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();
            List<FlowAndStatisticsMapList> outStatsItem = flowStatsConvertor.toSALFlowStatsList(
                    replyBody.getFlowStats(),
                    deviceContext.getDeviceState().getFeatures().getDatapathId(),
                    ofVersion);
            statsList.addAll(outStatsItem);
        }

        return notification.build();
    }
}
