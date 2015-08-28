/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class AllFlowsInTableService extends AbstractMultipartService<GetAllFlowStatisticsFromFlowTableInput> {

    private Function<? super RpcResult<List<MultipartReply>>, ? extends RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> transformer;
    private final FlowStatsResponseConvertor flowStatsConvertor;

    public AllFlowsInTableService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
        // TODO: lookup by version
        flowStatsConvertor = new FlowStatsResponseConvertor();
        // TODO: use fixed version
        transformer = new Function<RpcResult<List<MultipartReply>>, RpcResult<GetAllFlowStatisticsFromFlowTableOutput>>() {
            @Nullable
            @Override
            public RpcResult<GetAllFlowStatisticsFromFlowTableOutput> apply(RpcResult<List<MultipartReply>> input) {
                GetAllFlowStatisticsFromFlowTableOutputBuilder outBld = new GetAllFlowStatisticsFromFlowTableOutputBuilder();
                List<FlowAndStatisticsMapList> statsListFinal = new ArrayList<>();
                outBld.setFlowAndStatisticsMapList(statsListFinal);

                List<FlowAndStatisticsMapList> statsList = new ArrayList<>();
                for (MultipartReply mpRawReply : input.getResult()) {
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
                return RpcResultBuilder.success(outBld.build()).build();
            }
        };
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllFlowStatisticsFromFlowTableInput input) {
        final MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
        mprFlowRequestBuilder.setTableId(input.getTableId().getValue());
        mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
        mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
        mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
        mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);

        final short version = getVersion();
        FlowCreatorUtil.setWildcardedFlowMatch(version, mprFlowRequestBuilder);

        final MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
        multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());

        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPFLOW, xid.getValue(), version);

        mprInput.setMultipartRequestBody(multipartRequestFlowCaseBuilder.build());

        return mprInput.build();
    }


    public Function<? super RpcResult<List<MultipartReply>>, ? extends RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> getTransformer() {
        return transformer;
    }
}
