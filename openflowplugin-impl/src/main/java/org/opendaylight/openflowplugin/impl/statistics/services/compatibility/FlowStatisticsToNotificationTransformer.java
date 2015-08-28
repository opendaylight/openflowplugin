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
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowStatsResponseConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;

/**
 * pulled out flow to notification transformation
 */
public class FlowStatisticsToNotificationTransformer {

    private FlowStatsResponseConvertor flowStatsConvertor = new FlowStatsResponseConvertor();

    /**
     * @param mpResult      raw multipart response from device
     * @param deviceContext device context
     * @return notification containing flow stats
     */
    public FlowsStatisticsUpdate transformToNotification(final List<MultipartReply> mpResult, final DeviceContext deviceContext) {
        FlowsStatisticsUpdateBuilder outBld = new FlowsStatisticsUpdateBuilder();
        List<FlowAndStatisticsMapList> statsListFinal = new ArrayList<>();
        outBld.setFlowAndStatisticsMapList(statsListFinal);

        List<FlowAndStatisticsMapList> statsList = new ArrayList<>();
        for (MultipartReply mpRawReply : mpResult) {
            Preconditions.checkArgument(MultipartType.OFPMPFLOW.equals(mpRawReply.getType()));

            MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase) mpRawReply.getMultipartReplyBody();
            MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();
            List<FlowAndStatisticsMapList> outStatsItem = flowStatsConvertor.toSALFlowStatsList(replyBody.getFlowStats(),
                    deviceContext.getDeviceState().getFeatures().getDatapathId(),
                    OpenflowVersion.get(deviceContext.getDeviceState().getVersion()));
            statsList.addAll(outStatsItem);
        }

        // assign flowIds
        for (FlowAndStatisticsMapList flowStatsItem : statsList) {
            final FlowRegistryKey key = FlowRegistryKeyFactory.create(flowStatsItem);
            final FlowDescriptor flowDescriptor = deviceContext.getDeviceFlowRegistry().retrieveIdForFlow(key);
            if (flowDescriptor == null) {
                // unassigned flowId
                continue;
            }
            final FlowId flowIdStat = new FlowId(flowDescriptor.getFlowId());
            final FlowAndStatisticsMapList flowStatsFinal = new FlowAndStatisticsMapListBuilder(flowStatsItem)
                    .setFlowId(flowIdStat)
                    .setKey(new FlowAndStatisticsMapListKey(flowIdStat)).build();
            statsListFinal.add(flowStatsFinal);
        }
        return outBld.build();
    }
}
