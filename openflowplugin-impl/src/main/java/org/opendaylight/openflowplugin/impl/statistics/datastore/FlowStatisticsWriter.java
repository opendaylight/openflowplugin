/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.datastore;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FlowStatisticsWriter extends AbstractStatisticsWriter<FlowAndStatisticsMapList> {

    private final DeviceRegistry registry;

    public FlowStatisticsWriter(final TxFacade txFacade,
                                final InstanceIdentifier<Node> instanceIdentifier,
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
        statistics.getFlowAndStatisticsMapList()
            .forEach(stat -> {
                final FlowKey key = new FlowKey(registry
                    .getDeviceFlowRegistry()
                    .storeIfNecessary(FlowRegistryKeyFactory
                        .create(new FlowBuilder(stat)
                            .addAugmentation(
                                FlowStatisticsData.class,
                                new FlowStatisticsDataBuilder()
                                    .setFlowStatistics(new FlowStatisticsBuilder(stat).build())
                                    .build())
                            .build())));

                writeToTransaction(
                    getInstanceIdentifier()
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class, new TableKey(stat.getTableId()))
                        .child(Flow.class, key),
                    new FlowBuilder(stat)
                        .addAugmentation(
                            FlowStatisticsData.class,
                            new FlowStatisticsDataBuilder()
                                .setFlowStatistics(new FlowStatisticsBuilder(stat).build())
                                .build())
                        .setKey(key)
                        .build(),
                    withParents);
            });
    }

}
