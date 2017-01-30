/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer;

import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractFlowDirectStatisticsService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;

public class FlowDirectStatisticsService extends AbstractFlowDirectStatisticsService<MultipartReply> {

    public FlowDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext, ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor);
    }

    @Override
    protected GetFlowStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        return new GetFlowStatisticsOutputBuilder()
                .setFlowAndStatisticsMapList(input
                        .stream()
                        .flatMap(multipartReply -> MultipartReplyFlowStats.class
                                .cast(multipartReply.getMultipartReplyBody())
                                .getFlowAndStatisticsMapList()
                                .stream())
                        .map(flowAndStatisticsMapList -> {
                            final FlowId flowId = new FlowId(generateFlowId(flowAndStatisticsMapList));
                            return new FlowAndStatisticsMapListBuilder(flowAndStatisticsMapList)
                                    .setKey(new FlowAndStatisticsMapListKey(flowId))
                                    .setFlowId(flowId)
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
    }

}
