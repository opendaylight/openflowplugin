package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yangtools.concepts.Builder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class QueueDirectStatisticsService extends AbstractDirectStatisticsService<GetQueueStatisticsInput, GetQueueStatisticsOutput> {
    public QueueDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    protected OfHeader buildRequest(Xid xid, GetQueueStatisticsInput input) throws Exception {
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();
        // Select specific queue
        mprQueueBuilder.setQueueId(input.getQueueId().getValue());
        // Select specific port
        final short version = getVersion();
        mprQueueBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                OpenflowVersion.get(version), input.getNodeConnectorId()));
        caseBuilder.setMultipartRequestQueue(mprQueueBuilder.build());

        // Set request body to main multipart request
        MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPQUEUE, xid.getValue(), version);
        mprInput.setMultipartRequestBody(caseBuilder.build());
        return mprInput.build();
    }

    @Override
    protected Builder<GetQueueStatisticsOutput> buildReply(@Nullable List<MultipartReply> input) {
        final GetQueueStatisticsOutputBuilder builder = new GetQueueStatisticsOutputBuilder();
        final OpenflowVersion ofVersion = OpenflowVersion.get(getVersion());
        final List<QueueIdAndStatisticsMap> queueIdAndStatisticsMap = new ArrayList<>();

        for (MultipartReply mpReply : input) {
            MultipartReplyQueueCase caseBody = (MultipartReplyQueueCase) mpReply.getMultipartReplyBody();
            MultipartReplyQueue replyBody = caseBody.getMultipartReplyQueue();

            for (QueueStats queueStats : replyBody.getQueueStats()) {
                QueueIdAndStatisticsMapBuilder statsBuilder = new QueueIdAndStatisticsMapBuilder();
                statsBuilder.setNodeConnectorId(
                        InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                                getDatapathId(),
                                queueStats.getPortNo(), ofVersion));
                statsBuilder.setTransmissionErrors(new Counter64(queueStats.getTxErrors()));
                statsBuilder.setTransmittedBytes(new Counter64(queueStats.getTxBytes()));
                statsBuilder.setTransmittedPackets(new Counter64(queueStats.getTxPackets()));

                DurationBuilder durationBuilder = new DurationBuilder();
                durationBuilder.setSecond(new Counter32(queueStats.getDurationSec()));
                durationBuilder.setNanosecond(new Counter32(queueStats.getDurationNsec()));
                statsBuilder.setDuration(durationBuilder.build());

                statsBuilder.setQueueId(new QueueId(queueStats.getQueueId()));

                queueIdAndStatisticsMap.add(statsBuilder.build());
            }
        }

        builder.setQueueIdAndStatisticsMap(queueIdAndStatisticsMap);

        return builder;
    }

    @Override
    protected void storeStatistics(@Nullable GetQueueStatisticsOutput input) {
        //TODO: Implement storing
    }
}