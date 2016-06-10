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
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * The Queue direct statistics service.
 */
public class QueueDirectStatisticsService extends AbstractDirectStatisticsService<GetQueueStatisticsInput, GetQueueStatisticsOutput> {
    /**
     * Instantiates a new Queue direct statistics service.
     *
     * @param requestContextStack the request context stack
     * @param deviceContext       the device context
     */
    public QueueDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(MultipartType.OFPMPQUEUE, requestContextStack, deviceContext);
    }

    @Override
    protected MultipartRequestBody buildRequestBody(GetQueueStatisticsInput input) {
        final MultipartRequestQueueBuilder mprQueueBuilder = new MultipartRequestQueueBuilder();

        if (input.getQueueId() != null) {
            mprQueueBuilder.setQueueId(input.getQueueId().getValue());
        } else {
            mprQueueBuilder.setQueueId(OFConstants.OFPQ_ALL);
        }

        if (input.getNodeConnectorId() != null) {
            mprQueueBuilder.setPortNo(InventoryDataServiceUtil.portNumberfromNodeConnectorId(getOfVersion(), input.getNodeConnectorId()));
        } else {
            mprQueueBuilder.setPortNo(OFConstants.OFPP_ANY);
        }

        return new MultipartRequestQueueCaseBuilder()
                .setMultipartRequestQueue(mprQueueBuilder.build())
                .build();
    }

    @Override
    protected GetQueueStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        final List<QueueIdAndStatisticsMap> queueIdAndStatisticsMap = new ArrayList<>();

        if (success) {
            for (final MultipartReply mpReply : input) {
                final MultipartReplyQueueCase caseBody = (MultipartReplyQueueCase) mpReply.getMultipartReplyBody();
                final MultipartReplyQueue replyBody = caseBody.getMultipartReplyQueue();

                for (final QueueStats queueStats : replyBody.getQueueStats()) {
                    final DurationBuilder durationBuilder = new DurationBuilder()
                            .setSecond(new Counter32(queueStats.getDurationSec()))
                            .setNanosecond(new Counter32(queueStats.getDurationNsec()));

                    final QueueIdAndStatisticsMapBuilder statsBuilder = new QueueIdAndStatisticsMapBuilder()
                            .setNodeConnectorId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                                    getDatapathId(), queueStats.getPortNo(), getOfVersion()))
                            .setTransmissionErrors(new Counter64(queueStats.getTxErrors()))
                            .setTransmittedBytes(new Counter64(queueStats.getTxBytes()))
                            .setTransmittedPackets(new Counter64(queueStats.getTxPackets()))
                            .setQueueId(new QueueId(queueStats.getQueueId()))
                            .setDuration(durationBuilder.build());

                    queueIdAndStatisticsMap.add(statsBuilder.build());
                }
            }
        }

        return new GetQueueStatisticsOutputBuilder()
                .setQueueIdAndStatisticsMap(queueIdAndStatisticsMap)
                .build();
    }

    @Override
    protected void storeStatistics(GetQueueStatisticsOutput output) throws Exception {
        final InstanceIdentifier<Node> nodePath = getDeviceInfo().getNodeInstanceIdentifier();

        for (final QueueIdAndStatisticsMap queueStatistics : output.getQueueIdAndStatisticsMap()) {
            if (queueStatistics.getQueueId() != null) {
                final QueueKey qKey = new QueueKey(queueStatistics.getQueueId());

                final FlowCapableNodeConnectorQueueStatistics statChild =
                        new FlowCapableNodeConnectorQueueStatisticsBuilder(queueStatistics).build();

                final FlowCapableNodeConnectorQueueStatisticsDataBuilder statBuild =
                        new FlowCapableNodeConnectorQueueStatisticsDataBuilder()
                                .setFlowCapableNodeConnectorQueueStatistics(statChild);

                final InstanceIdentifier<Queue> queueStatisticsPath = nodePath
                        .child(NodeConnector.class, new NodeConnectorKey(queueStatistics.getNodeConnectorId()))
                        .augmentation(FlowCapableNodeConnector.class)
                        .child(Queue.class, qKey);

                final Queue stats = new QueueBuilder()
                        .setKey(qKey)
                        .setQueueId(queueStatistics.getQueueId())
                        .addAugmentation(FlowCapableNodeConnectorQueueStatisticsData.class, statBuild.build()).build();

                getTxFacade().writeToTransactionWithParentsSlow(LogicalDatastoreType.OPERATIONAL, queueStatisticsPath, stats);
            }
        }
    }
}