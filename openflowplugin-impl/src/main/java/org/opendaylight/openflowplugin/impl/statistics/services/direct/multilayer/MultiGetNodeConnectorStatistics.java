/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer;

import java.util.List;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractGetNodeConnectorStatistics;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapKey;
import org.opendaylight.yangtools.binding.util.BindingMap;

public final class MultiGetNodeConnectorStatistics extends AbstractGetNodeConnectorStatistics<MultipartReply> {
    public MultiGetNodeConnectorStatistics(final RequestContextStack requestContextStack,
            final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor,
            final MultipartWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
    }

    @Override
    protected GetNodeConnectorStatisticsOutput buildReply(final List<MultipartReply> input, final boolean success) {
        if (!success) {
            return new GetNodeConnectorStatisticsOutputBuilder().build();
        }

        final var nodeConnectorStatisticsAndPortNumberMap = BindingMap.<NodeConnectorStatisticsAndPortNumberMapKey,
            NodeConnectorStatisticsAndPortNumberMap>orderedBuilder();
        for (var mpReply : input) {
            final var caseBody = (MultipartReplyPortStatsCase) mpReply.getMultipartReplyBody();
            final var replyBody = caseBody.getMultipartReplyPortStats();

            for (var portStats : replyBody.nonnullPortStats()) {
                final var durationBuilder = new DurationBuilder();
                if (portStats.getDurationSec() != null) {
                    durationBuilder.setSecond(new Counter32(portStats.getDurationSec()));
                }
                if (portStats.getDurationNsec() != null) {
                    durationBuilder.setNanosecond(new Counter32(portStats.getDurationNsec()));
                }

                nodeConnectorStatisticsAndPortNumberMap.add(new NodeConnectorStatisticsAndPortNumberMapBuilder()
                    .setBytes(new BytesBuilder()
                        .setReceived(portStats.getRxBytes())
                        .setTransmitted(portStats.getTxBytes())
                        .build())
                    .setPackets(new PacketsBuilder()
                        .setReceived(portStats.getRxPackets())
                        .setTransmitted(portStats.getTxPackets())
                        .build())
                    .setNodeConnectorId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(getDatapathId(),
                        portStats.getPortNo(), getOfVersion()))
                    .setDuration(durationBuilder.build())
                    .setCollisionCount(portStats.getCollisions())
                    .setReceiveCrcError(portStats.getRxCrcErr()).setReceiveDrops(portStats.getRxDropped())
                    .setReceiveErrors(portStats.getRxErrors())
                    .setReceiveFrameError(portStats.getRxFrameErr())
                    .setReceiveOverRunError(portStats.getRxOverErr())
                    .setTransmitDrops(portStats.getTxDropped())
                    .setTransmitErrors(portStats.getTxErrors())
                    .build());
            }
        }

        return new GetNodeConnectorStatisticsOutputBuilder()
            .setNodeConnectorStatisticsAndPortNumberMap(nodeConnectorStatisticsAndPortNumberMap.build())
            .build();
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetNodeConnectorStatisticsInput input) {
        final var mprPortStatsBuilder = new MultipartRequestPortStatsBuilder();

        if (input.getNodeConnectorId() != null) {
            mprPortStatsBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(getOfVersion(),
                    input.getNodeConnectorId()));
        } else {
            mprPortStatsBuilder.setPortNo(OFConstants.OFPP_ANY);
        }

        return RequestInputUtils.createMultipartHeader(getMultipartType(), xid.getValue(), getVersion())
            .setMultipartRequestBody(new MultipartRequestPortStatsCaseBuilder()
                .setMultipartRequestPortStats(mprPortStatsBuilder.build())
                .build())
            .build();
    }
}
