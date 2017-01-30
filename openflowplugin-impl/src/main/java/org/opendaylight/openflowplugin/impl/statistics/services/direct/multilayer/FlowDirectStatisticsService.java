/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractFlowDirectStatisticsService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.FlowStatsResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;

public class FlowDirectStatisticsService extends AbstractFlowDirectStatisticsService<MultipartReply> {

    private final FlowStatsResponseConvertorData data;

    public FlowDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext, ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor);
        data = new FlowStatsResponseConvertorData(getVersion());
        data.setDatapathId(getDatapathId());
        data.setMatchPath(MatchPath.RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH);
    }

    @Override
    protected GetFlowStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        final List<FlowAndStatisticsMapList> statsList = new ArrayList<>();

        if (success) {
            for (final MultipartReply mpReply : input) {
                final MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase) mpReply.getMultipartReplyBody();
                final MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();
                final Optional<List<FlowAndStatisticsMapList>> statsListPart = getConvertorExecutor().convert(
                        replyBody.getFlowStats(), data);

                if (statsListPart.isPresent()) {
                    for (final FlowAndStatisticsMapList part : statsListPart.get()) {
                        final FlowId flowId = new FlowId(generateFlowId(part).getValue());
                        statsList.add(new FlowAndStatisticsMapListBuilder(part)
                                .setKey(new FlowAndStatisticsMapListKey(flowId))
                                .setFlowId(flowId)
                                .build());
                    }
                }
            }
        }

        return new GetFlowStatisticsOutputBuilder()
                .setFlowAndStatisticsMapList(statsList)
                .build();
    }

}
