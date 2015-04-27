package org.opendaylight.openflowplugin.impl.translator;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;

import java.math.BigInteger;

/**
 * Created by tkubas on 4/27/15.
 */
public class AggregatedFlowStatisticsTranslator implements MessageTranslator<MultipartReply, AggregatedFlowStatistics> {
    @Override
    public AggregatedFlowStatistics translate(MultipartReply input, DeviceContext deviceContext, Object connectionDistinguisher) {
        AggregatedFlowStatisticsBuilder aggregatedFlowStatisticsBuilder = new AggregatedFlowStatisticsBuilder();

        MultipartReplyAggregateCase caseBody = (MultipartReplyAggregateCase)input.getMultipartReplyBody();
        MultipartReplyAggregate replyBody = caseBody.getMultipartReplyAggregate();

        aggregatedFlowStatisticsBuilder.setByteCount(new Counter64(replyBody.getByteCount()));
        aggregatedFlowStatisticsBuilder.setFlowCount(new Counter32(replyBody.getFlowCount()));
        aggregatedFlowStatisticsBuilder.setPacketCount(new Counter64(replyBody.getPacketCount()));

        return aggregatedFlowStatisticsBuilder.build();
    }
}
