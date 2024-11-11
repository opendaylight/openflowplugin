/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.datastore.multipart;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

public class FlowStatsMultipartWriter extends AbstractMultipartWriter<FlowAndStatisticsMapList> {
    private final DeviceRegistry registry;

    public FlowStatsMultipartWriter(final TxFacade txFacade,
                                    final WithKey<Node, NodeKey> instanceIdentifier,
                                    final DeviceRegistry registry) {
        super(txFacade, instanceIdentifier);
        this.registry = registry;
    }

    @Override
    protected Class<FlowAndStatisticsMapList> getType() {
        return FlowAndStatisticsMapList.class;
    }

    @Override
    public void storeStatistics(final FlowAndStatisticsMapList statistics, final boolean withParents) {
        statistics.nonnullFlowAndStatisticsMapList()
            .forEach(stat -> {
                final var flowBuilder = new FlowBuilder(stat)
                        .withKey(FlowRegistryKeyFactory.DUMMY_FLOW_KEY)
                        .addAugmentation(new FlowStatisticsDataBuilder()
                            .setFlowStatistics(new FlowStatisticsBuilder(stat).build())
                            .build());

                final var flowRegistry = registry.getDeviceFlowRegistry();
                final var flowRegistryKey = flowRegistry.createKey(flowBuilder.build());
                flowRegistry.store(flowRegistryKey);

                final var flowDescriptor = flowRegistry.retrieveDescriptor(flowRegistryKey);
                if (flowDescriptor != null) {
                    final FlowKey key = new FlowKey(flowDescriptor.getFlowId());

                    writeToTransaction(
                            getInstanceIdentifier().toBuilder()
                                    .augmentation(FlowCapableNode.class)
                                    .child(Table.class, new TableKey(stat.getTableId()))
                                    .child(Flow.class, key)
                                    .build(),
                            flowBuilder
                                    .setId(key.getId())
                                    .withKey(key)
                                    .build(),
                            withParents);
                }
            });
    }
}
