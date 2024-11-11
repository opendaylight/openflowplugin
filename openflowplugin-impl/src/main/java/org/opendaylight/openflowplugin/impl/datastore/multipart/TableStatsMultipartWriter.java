/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.datastore.multipart;

import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

public class TableStatsMultipartWriter extends AbstractMultipartWriter<FlowTableAndStatisticsMap> {
    public TableStatsMultipartWriter(final TxFacade txFacade, final WithKey<Node, NodeKey> instanceIdentifier) {
        super(txFacade, instanceIdentifier);
    }

    @Override
    protected Class<FlowTableAndStatisticsMap> getType() {
        return FlowTableAndStatisticsMap.class;
    }

    @Override
    public void storeStatistics(final FlowTableAndStatisticsMap statistics,
                                final boolean withParents) {
        statistics.nonnullFlowTableAndStatisticsMap().values()
            .forEach(stat -> writeToTransaction(
                getInstanceIdentifier().toBuilder()
                    .augmentation(FlowCapableNode.class)
                    .child(Table.class, new TableKey(stat.getTableId().getValue()))
                    .augmentation(FlowTableStatisticsData.class)
                    .child(FlowTableStatistics.class)
                    .build(),
                new FlowTableStatisticsBuilder(stat).build(),
                withParents));
    }
}
