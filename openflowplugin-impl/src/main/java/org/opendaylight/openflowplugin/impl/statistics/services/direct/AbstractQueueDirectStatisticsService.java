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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * The Queue direct statistics service.
 */
public abstract class AbstractQueueDirectStatisticsService<T extends OfHeader>
        extends AbstractDirectStatisticsService<GetQueueStatisticsInput, GetQueueStatisticsOutput, T> {

    public AbstractQueueDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext, ConvertorExecutor convertorExecutor) {
        super(MultipartType.OFPMPQUEUE, requestContextStack, deviceContext, convertorExecutor);
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
    protected void storeStatistics(GetQueueStatisticsOutput output) {
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
