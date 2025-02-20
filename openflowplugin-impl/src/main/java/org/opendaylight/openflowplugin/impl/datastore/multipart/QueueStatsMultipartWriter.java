/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.datastore.multipart;

import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatisticsBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.yang.common.Uint32;

public class QueueStatsMultipartWriter extends AbstractMultipartWriter<QueueIdAndStatisticsMap> {
    private final FeaturesReply features;

    public QueueStatsMultipartWriter(final TxFacade txFacade,
                                     final WithKey<Node, NodeKey> instanceIdentifier,
                                     final FeaturesReply features) {
        super(txFacade, instanceIdentifier);
        this.features = features;
    }

    @Override
    protected Class<QueueIdAndStatisticsMap> getType() {
        return QueueIdAndStatisticsMap.class;
    }

    @Override
    public void storeStatistics(final QueueIdAndStatisticsMap statistics, final boolean withParents) {
        final OpenflowVersion openflowVersion = OpenflowVersion.ofVersion(features.getVersion());

        statistics.nonnullQueueIdAndStatisticsMap().values()
            .forEach(stat -> {
                final Uint32 port = InventoryDataServiceUtil
                        .portNumberfromNodeConnectorId(openflowVersion, stat.getNodeConnectorId());
                final NodeConnectorId id = InventoryDataServiceUtil
                        .nodeConnectorIdfromDatapathPortNo(
                                features.getDatapathId(),
                                port,
                                OpenflowVersion.ofVersion(features.getVersion()));

                writeToTransaction(
                        getInstanceIdentifier().toBuilder()
                                .child(NodeConnector.class, new NodeConnectorKey(id))
                                .augmentation(FlowCapableNodeConnector.class)
                                .child(Queue.class, new QueueKey(stat.getQueueId()))
                                .build(),
                        new QueueBuilder()
                                .withKey(new QueueKey(stat.getQueueId()))
                                .setQueueId(stat.getQueueId())
                                .addAugmentation(new FlowCapableNodeConnectorQueueStatisticsDataBuilder()
                                                .setFlowCapableNodeConnectorQueueStatistics(
                                                        new FlowCapableNodeConnectorQueueStatisticsBuilder(stat)
                                                                .build())
                                                .build())
                                .build(),
                        withParents);
            });
    }
}
