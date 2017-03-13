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
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractFlowDirectStatisticsService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class FlowDirectStatisticsService extends AbstractFlowDirectStatisticsService<MultipartReply> {

    public FlowDirectStatisticsService(final RequestContextStack requestContextStack,
                                       final DeviceContext deviceContext,
                                       final ConvertorExecutor convertorExecutor,
                                       final MultipartWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);    }

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

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetFlowStatisticsInput input) throws ServiceException {
        return new MultipartRequestBuilder()
            .setXid(xid.getValue())
            .setVersion(getVersion())
            .setRequestMore(false)
            .setMultipartRequestBody(new MultipartRequestFlowStatsBuilder(input)
                .build())
            .build();
    }

}
