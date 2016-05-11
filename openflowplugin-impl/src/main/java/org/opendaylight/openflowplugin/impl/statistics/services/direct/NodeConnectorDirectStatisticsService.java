package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.List;

public class NodeConnectorDirectStatisticsService extends AbstractDirectStatisticsService<GetNodeConnectorStatisticsInput, GetNodeConnectorStatisticsOutput> {
    public NodeConnectorDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(MultipartType.OFPMPPORTSTATS, requestContextStack, deviceContext);
    }

    @Override
    protected MultipartRequestBody buildRequestBody(GetNodeConnectorStatisticsInput input) {
        MultipartRequestPortStatsBuilder mprPortStatsBuilder = new MultipartRequestPortStatsBuilder()
                .setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(getOfVersion(), input.getNodeConnectorId()));

        return new MultipartRequestPortStatsCaseBuilder()
                .setMultipartRequestPortStats(mprPortStatsBuilder.build())
                .build();
    }

    @Override
    protected GetNodeConnectorStatisticsOutput buildReply(List<MultipartReply> input) {
        final List<NodeConnectorStatisticsAndPortNumberMap> nodeConnectorStatisticsAndPortNumberMap = new ArrayList<>();

        for (MultipartReply mpReply : input) {
            MultipartReplyPortStatsCase caseBody = (MultipartReplyPortStatsCase) mpReply.getMultipartReplyBody();
            MultipartReplyPortStats replyBody = caseBody.getMultipartReplyPortStats();

            for (PortStats portStats : replyBody.getPortStats()) {
                final NodeConnectorId nodeConnectorId = InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                        getDatapathId(), portStats.getPortNo(), getOfVersion());

                BytesBuilder bytesBuilder = new BytesBuilder()
                        .setReceived(portStats.getRxBytes())
                        .setTransmitted(portStats.getTxBytes());

                PacketsBuilder packetsBuilder = new PacketsBuilder()
                        .setReceived(portStats.getRxPackets())
                        .setTransmitted(portStats.getTxPackets());

                DurationBuilder durationBuilder = new DurationBuilder();

                if (portStats.getDurationSec() != null) {
                    durationBuilder.setSecond(new Counter32(portStats.getDurationSec()));
                }

                if (portStats.getDurationNsec() != null) {
                    durationBuilder.setNanosecond(new Counter32(portStats.getDurationNsec()));
                }

                NodeConnectorStatisticsAndPortNumberMap stats = new NodeConnectorStatisticsAndPortNumberMapBuilder()
                        .setBytes(bytesBuilder.build())
                        .setPackets(packetsBuilder.build())
                        .setNodeConnectorId(nodeConnectorId)
                        .setDuration(durationBuilder.build())
                        .setCollisionCount(portStats.getCollisions())
                        .setKey(new NodeConnectorStatisticsAndPortNumberMapKey(nodeConnectorId))
                        .setReceiveCrcError(portStats.getRxCrcErr()) .setReceiveDrops(portStats.getRxDropped())
                        .setReceiveErrors(portStats.getRxErrors())
                        .setReceiveFrameError(portStats.getRxFrameErr())
                        .setReceiveOverRunError(portStats.getRxOverErr())
                        .setTransmitDrops(portStats.getTxDropped())
                        .setTransmitErrors(portStats.getTxErrors())
                        .build();

                nodeConnectorStatisticsAndPortNumberMap.add(stats);
            }
        }

        return new GetNodeConnectorStatisticsOutputBuilder()
                .setNodeConnectorStatisticsAndPortNumberMap(nodeConnectorStatisticsAndPortNumberMap)
                .build();
    }

    @Override
    protected void storeStatistics(GetNodeConnectorStatisticsOutput output) throws Exception {
        final InstanceIdentifier<Node> nodePath = getDeviceContext().getDeviceState().getNodeInstanceIdentifier();

        for (final NodeConnectorStatisticsAndPortNumberMap nodeConnectorStatistics : output.getNodeConnectorStatisticsAndPortNumberMap()) {
            final InstanceIdentifier<FlowCapableNodeConnectorStatistics> nodeConnectorPath = nodePath
                    .child(NodeConnector.class, new NodeConnectorKey(nodeConnectorStatistics.getNodeConnectorId()))
                    .augmentation(FlowCapableNodeConnectorStatisticsData.class)
                    .child(FlowCapableNodeConnectorStatistics.class);

            final FlowCapableNodeConnectorStatistics stats = new FlowCapableNodeConnectorStatisticsBuilder(nodeConnectorStatistics).build();
            getDeviceContext().writeToTransaction(LogicalDatastoreType.OPERATIONAL, nodeConnectorPath, stats);
        }
    }
}
