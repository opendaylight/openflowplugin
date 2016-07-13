/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
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

/**
 * The Node connector direct statistics service.
 */
public class NodeConnectorDirectStatisticsService extends AbstractDirectStatisticsService<GetNodeConnectorStatisticsInput, GetNodeConnectorStatisticsOutput> {
    /**
     * Instantiates a new Node connector direct statistics service.
     *
     * @param requestContextStack the request context stack
     * @param deviceContext       the device context
     */
    public NodeConnectorDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(MultipartType.OFPMPPORTSTATS, requestContextStack, deviceContext);
    }

    @Override
    protected MultipartRequestBody buildRequestBody(GetNodeConnectorStatisticsInput input) {
        final MultipartRequestPortStatsBuilder mprPortStatsBuilder = new MultipartRequestPortStatsBuilder();

        if (input.getNodeConnectorId() != null) {
            mprPortStatsBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(getOfVersion(), input.getNodeConnectorId()));
        } else {
            mprPortStatsBuilder.setPortNo(OFConstants.OFPP_ANY);
        }

        return new MultipartRequestPortStatsCaseBuilder()
                .setMultipartRequestPortStats(mprPortStatsBuilder.build())
                .build();
    }

    @Override
    protected GetNodeConnectorStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        final List<NodeConnectorStatisticsAndPortNumberMap> nodeConnectorStatisticsAndPortNumberMap = new ArrayList<>();

        if (success) {
            for (final MultipartReply mpReply : input) {
                final MultipartReplyPortStatsCase caseBody = (MultipartReplyPortStatsCase) mpReply.getMultipartReplyBody();
                final MultipartReplyPortStats replyBody = caseBody.getMultipartReplyPortStats();

                for (final PortStats portStats : replyBody.getPortStats()) {
                    final NodeConnectorId nodeConnectorId = InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                            getDatapathId(), portStats.getPortNo(), getOfVersion());

                    final BytesBuilder bytesBuilder = new BytesBuilder()
                            .setReceived(portStats.getRxBytes())
                            .setTransmitted(portStats.getTxBytes());

                    final PacketsBuilder packetsBuilder = new PacketsBuilder()
                            .setReceived(portStats.getRxPackets())
                            .setTransmitted(portStats.getTxPackets());

                    final DurationBuilder durationBuilder = new DurationBuilder();

                    if (portStats.getDurationSec() != null) {
                        durationBuilder.setSecond(new Counter32(portStats.getDurationSec()));
                    }

                    if (portStats.getDurationNsec() != null) {
                        durationBuilder.setNanosecond(new Counter32(portStats.getDurationNsec()));
                    }

                    final NodeConnectorStatisticsAndPortNumberMap stats = new NodeConnectorStatisticsAndPortNumberMapBuilder()
                            .setBytes(bytesBuilder.build())
                            .setPackets(packetsBuilder.build())
                            .setNodeConnectorId(nodeConnectorId)
                            .setDuration(durationBuilder.build())
                            .setCollisionCount(portStats.getCollisions())
                            .setKey(new NodeConnectorStatisticsAndPortNumberMapKey(nodeConnectorId))
                            .setReceiveCrcError(portStats.getRxCrcErr()).setReceiveDrops(portStats.getRxDropped())
                            .setReceiveErrors(portStats.getRxErrors())
                            .setReceiveFrameError(portStats.getRxFrameErr())
                            .setReceiveOverRunError(portStats.getRxOverErr())
                            .setTransmitDrops(portStats.getTxDropped())
                            .setTransmitErrors(portStats.getTxErrors())
                            .build();

                    nodeConnectorStatisticsAndPortNumberMap.add(stats);
                }
            }
        }

        return new GetNodeConnectorStatisticsOutputBuilder()
                .setNodeConnectorStatisticsAndPortNumberMap(nodeConnectorStatisticsAndPortNumberMap)
                .build();
    }

    @Override
    protected void storeStatistics(GetNodeConnectorStatisticsOutput output) throws Exception {
        final InstanceIdentifier<Node> nodePath = getDeviceInfo().getNodeInstanceIdentifier();

        for (final NodeConnectorStatisticsAndPortNumberMap nodeConnectorStatistics : output.getNodeConnectorStatisticsAndPortNumberMap()) {
            final InstanceIdentifier<FlowCapableNodeConnectorStatistics> nodeConnectorPath = nodePath
                    .child(NodeConnector.class, new NodeConnectorKey(nodeConnectorStatistics.getNodeConnectorId()))
                    .augmentation(FlowCapableNodeConnectorStatisticsData.class)
                    .child(FlowCapableNodeConnectorStatistics.class);

            final FlowCapableNodeConnectorStatistics stats = new FlowCapableNodeConnectorStatisticsBuilder(nodeConnectorStatistics).build();
            getTxFacade().writeToTransactionWithParentsSlow(LogicalDatastoreType.OPERATIONAL, nodeConnectorPath, stats);
        }
    }
}
