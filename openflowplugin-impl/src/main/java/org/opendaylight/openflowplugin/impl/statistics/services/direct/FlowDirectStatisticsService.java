package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class FlowDirectStatisticsService extends AbstractDirectStatisticsService<GetFlowStatisticsInput, GetFlowStatisticsOutput> {
    private final FlowStatsResponseConvertor flowStatsConvertor = new FlowStatsResponseConvertor();

    public FlowDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    protected OfHeader buildRequest(Xid xid, GetFlowStatisticsInput input) throws Exception {
        final MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder()
                .setTableId(input.getTableId());

        final MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();

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
        multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());

        return RequestInputUtils.createMultipartHeader(MultipartType.OFPMPFLOW, xid.getValue(), getVersion())
                .setMultipartRequestBody(multipartRequestFlowCaseBuilder.build())
                .build();
    }

    @Override
    protected GetFlowStatisticsOutput buildReply(List<MultipartReply> input) {
        final List<FlowAndStatisticsMapList> statsList = new ArrayList<>();
        final OpenflowVersion ofVersion = OpenflowVersion.get(getVersion());

        for (MultipartReply mpReply : input) {
            MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase) mpReply.getMultipartReplyBody();
            MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();

            List<FlowAndStatisticsMapList> statsListPart = flowStatsConvertor.toSALFlowStatsList(replyBody.getFlowStats(), getDatapathId(), ofVersion);

            for (FlowAndStatisticsMapList part : statsListPart) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId flowId =
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId(generateFlowId(part).getValue());

                statsList.add(new FlowAndStatisticsMapListBuilder(part)
                        .setKey(new FlowAndStatisticsMapListKey(flowId))
                        .setFlowId(flowId)
                        .build());
            }
        }

        return new GetFlowStatisticsOutputBuilder()
                .setFlowAndStatisticsMapList(statsList)
                .build();
    }

    @Override
    protected void storeStatistics(GetFlowStatisticsOutput output) throws Exception {
        final InstanceIdentifier<FlowCapableNode> nodePath = getDeviceContext()
                .getDeviceState().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);

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

            getDeviceContext().writeToTransaction(LogicalDatastoreType.OPERATIONAL, flowStatisticsPath, flowBuilder.build());
        }
    }

    private FlowId generateFlowId(FlowAndStatisticsMapList flowStatistics) {
        final FlowStatisticsDataBuilder flowStatisticsDataBld = new FlowStatisticsDataBuilder()
                .setFlowStatistics(new FlowStatisticsBuilder(flowStatistics).build());

        final FlowBuilder flowBuilder = new FlowBuilder(flowStatistics)
                .addAugmentation(FlowStatisticsData.class, flowStatisticsDataBld.build());

        final short tableId = flowStatistics.getTableId();
        final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(flowBuilder.build());
        return getDeviceContext().getDeviceFlowRegistry().storeIfNecessary(flowRegistryKey, tableId);
    }
}
