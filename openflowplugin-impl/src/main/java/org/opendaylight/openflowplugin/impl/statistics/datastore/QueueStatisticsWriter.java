/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.datastore;

import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatisticsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class QueueStatisticsWriter extends AbstractStatisticsWriter<QueueIdAndStatisticsMap> {

    public QueueStatisticsWriter(final TxFacade txFacade) {
        super(txFacade);
    }

    @Override
    protected void storeStatistics(final QueueIdAndStatisticsMap statistics,
                                   final InstanceIdentifier<Node> instanceIdentifier,
                                   final boolean withParents) {
        statistics.getQueueIdAndStatisticsMap()
            .forEach(stat -> writeToTransaction(
                instanceIdentifier
                    .child(NodeConnector.class, new NodeConnectorKey(stat.getNodeConnectorId()))
                    .augmentation(FlowCapableNodeConnector.class)
                    .child(Queue.class, new QueueKey(stat.getQueueId())),
                new QueueBuilder()
                    .setKey(new QueueKey(stat.getQueueId()))
                    .setQueueId(stat.getQueueId())
                    .addAugmentation(
                        FlowCapableNodeConnectorQueueStatisticsData.class,
                        new FlowCapableNodeConnectorQueueStatisticsDataBuilder()
                            .setFlowCapableNodeConnectorQueueStatistics(
                                new FlowCapableNodeConnectorQueueStatisticsBuilder(stat).build())
                            .build())
                    .build(),
                withParents));
    }

}
