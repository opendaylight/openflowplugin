package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.MeterStatsResponseConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class MeterDirectStatisticsService extends AbstractDirectStatisticsService<GetMeterStatisticsInput, GetMeterStatisticsOutput> {
    private final MeterStatsResponseConvertor meterStatsConvertor = new MeterStatsResponseConvertor();

    public MeterDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    protected OfHeader buildRequest(Xid xid, GetMeterStatisticsInput input) throws Exception {
        MultipartRequestMeterBuilder mprMeterBuild = new MultipartRequestMeterBuilder()
                .setMeterId(new MeterId(input.getMeterId().getValue()));

        MultipartRequestMeterCaseBuilder caseBuilder = new MultipartRequestMeterCaseBuilder()
                .setMultipartRequestMeter(mprMeterBuild.build());

        return RequestInputUtils.createMultipartHeader(MultipartType.OFPMPMETER, xid.getValue(), getVersion())
                .setMultipartRequestBody(caseBuilder.build())
                .build();
    }

    @Override
    protected GetMeterStatisticsOutput buildReply(List<MultipartReply> input) {
        final List<MeterStats> meterStats = new ArrayList<>();

        for (MultipartReply mpReply : input) {
            MultipartReplyMeterCase caseBody = (MultipartReplyMeterCase) mpReply.getMultipartReplyBody();
            MultipartReplyMeter replyBody = caseBody.getMultipartReplyMeter();
            meterStats.addAll(meterStatsConvertor.toSALMeterStatsList(replyBody.getMeterStats()));
        }

        return new GetMeterStatisticsOutputBuilder()
                .setMeterStats(meterStats)
                .build();
    }

    @Override
    protected void storeStatistics(GetMeterStatisticsOutput output) throws Exception {
        final InstanceIdentifier<FlowCapableNode> nodePath = getDeviceContext()
                .getDeviceState().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);

        //TODO: Remove dependency on deviceContext
        for (final MeterStats meterStatistics : output.getMeterStats()) {
            final InstanceIdentifier<MeterStatistics> meterPath = nodePath
                    .child(Meter.class, new MeterKey(meterStatistics.getMeterId()))
                    .augmentation(NodeMeterStatistics.class)
                    .child(MeterStatistics.class);

            final MeterStatistics stats = new MeterStatisticsBuilder(meterStatistics).build();
            getDeviceContext().writeToTransaction(LogicalDatastoreType.OPERATIONAL, meterPath, stats);
        }
    }
}
