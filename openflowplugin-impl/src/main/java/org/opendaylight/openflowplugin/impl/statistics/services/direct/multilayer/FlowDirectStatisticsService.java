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
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractFlowDirectStatisticsService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.FlowStatsResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

public class FlowDirectStatisticsService extends AbstractFlowDirectStatisticsService<MultipartReply> {

    private final FlowStatsResponseConvertorData data;

    public FlowDirectStatisticsService(final RequestContextStack requestContextStack,
                                       final DeviceContext deviceContext,
                                       final ConvertorExecutor convertorExecutor,
                                       final MultipartWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
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

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetFlowStatisticsInput input) {
        final MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();

        if (input.getTableId() != null) {
            mprFlowRequestBuilder.setTableId(input.getTableId());
        } else {
            mprFlowRequestBuilder.setTableId(OFConstants.OFPTT_ALL);
        }

        if (input.getOutPort() != null) {
            mprFlowRequestBuilder.setOutPort(input.getOutPort().longValue());
        } else {
            mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
        }

        if (input.getOutGroup() != null) {
            mprFlowRequestBuilder.setOutGroup(input.getOutGroup());
        } else {
            mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
        }

        if (input.getCookie() != null) {
            mprFlowRequestBuilder.setCookie(input.getCookie().getValue());
        } else {
            mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
        }

        if (input.getCookieMask() != null) {
            mprFlowRequestBuilder.setCookieMask(input.getCookieMask().getValue());
        } else {
            mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        }

        MatchReactor.getInstance().convert(input.getMatch(), getVersion(), mprFlowRequestBuilder, getConvertorExecutor());

        return RequestInputUtils.createMultipartHeader(getMultipartType(), xid.getValue(), getVersion())
            .setMultipartRequestBody(new MultipartRequestFlowCaseBuilder()
                .setMultipartRequestFlow(mprFlowRequestBuilder.build())
                .build())
            .build();
    }

}
