/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * The Flow direct statistics service.
 */
public class FlowDirectStatisticsService extends AbstractDirectStatisticsService<GetFlowStatisticsInput, GetFlowStatisticsOutput> {
    private final FlowStatsResponseConvertor flowStatsConvertor = new FlowStatsResponseConvertor();

    /**
     * Instantiates a new Flow direct statistics service.
     *
     * @param requestContextStack the request context stack
     * @param deviceContext       the device context
     */
    public FlowDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(MultipartType.OFPMPFLOW, requestContextStack, deviceContext);
    }

    @Override
    protected MultipartRequestBody buildRequestBody(GetFlowStatisticsInput input) {
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

        MatchReactor.getInstance().convert(input.getMatch(), getVersion(), mprFlowRequestBuilder, getDatapathId());

        return new MultipartRequestFlowCaseBuilder()
                .setMultipartRequestFlow(mprFlowRequestBuilder.build())
                .build();
    }

    @Override
    protected GetFlowStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        final List<FlowAndStatisticsMapList> statsList = new ArrayList<>();

        if (success) {
            for (final MultipartReply mpReply : input) {
                final MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase) mpReply.getMultipartReplyBody();
                final MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();

                final List<FlowAndStatisticsMapList> statsListPart = flowStatsConvertor.toSALFlowStatsList(replyBody.getFlowStats(), getDatapathId(), getOfVersion());

                for (final FlowAndStatisticsMapList part : statsListPart) {
                    final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId flowId =
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId(generateFlowId(part).getValue());

                    statsList.add(new FlowAndStatisticsMapListBuilder(part)
                            .setKey(new FlowAndStatisticsMapListKey(flowId))
                            .setFlowId(flowId)
                            .build());
                }
            }
        }

        return new GetFlowStatisticsOutputBuilder()
                .setFlowAndStatisticsMapList(statsList)
                .build();
    }

    @Override
    protected void storeStatistics(GetFlowStatisticsOutput output) throws Exception {
        final InstanceIdentifier<FlowCapableNode> nodePath = getDeviceInfo().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);

        for (final FlowAndStatisticsMapList flowStatistics : output.getFlowAndStatisticsMapList()) {
            final FlowId flowId = generateFlowId(flowStatistics);
            final FlowKey flowKey = new FlowKey(flowId);

            final FlowStatisticsDataBuilder flowStatisticsDataBld = new FlowStatisticsDataBuilder()
                    .setFlowStatistics(new FlowStatisticsBuilder(flowStatistics).build());

            final FlowBuilder flowBuilder = new FlowBuilder(flowStatistics)
                    .addAugmentation(FlowStatisticsData.class, flowStatisticsDataBld.build())
                    .setKey(flowKey);

            final InstanceIdentifier<Flow> flowStatisticsPath = nodePath
                    .child(Table.class, new TableKey(flowStatistics.getTableId()))
                    .child(Flow.class, flowKey);

            getTxFacade().writeToTransactionWithParentsSlow(LogicalDatastoreType.OPERATIONAL, flowStatisticsPath, flowBuilder.build());
        }
    }

    private FlowId generateFlowId(FlowAndStatisticsMapList flowStatistics) {
        final FlowStatisticsDataBuilder flowStatisticsDataBld = new FlowStatisticsDataBuilder()
                .setFlowStatistics(new FlowStatisticsBuilder(flowStatistics).build());

        final FlowBuilder flowBuilder = new FlowBuilder(flowStatistics)
                .addAugmentation(FlowStatisticsData.class, flowStatisticsDataBld.build());

        final short tableId = flowStatistics.getTableId();
        final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(flowBuilder.build());
        return getDeviceRegistry().getDeviceFlowRegistry().storeIfNecessary(flowRegistryKey, tableId);
    }
}
