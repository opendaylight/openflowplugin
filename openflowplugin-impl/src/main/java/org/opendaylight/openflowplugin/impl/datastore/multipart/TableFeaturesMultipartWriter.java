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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

public class TableFeaturesMultipartWriter extends AbstractMultipartWriter<TableFeatures> {
    public TableFeaturesMultipartWriter(final TxFacade txFacade, final WithKey<Node, NodeKey> instanceIdentifier) {
        super(txFacade, instanceIdentifier);
    }

    @Override
    protected Class<TableFeatures> getType() {
        return TableFeatures.class;
    }

    @Override
    public void storeStatistics(final TableFeatures statistics, final boolean withParents) {
        statistics.nonnullTableFeatures().values()
            .forEach(stat -> {
                writeToTransaction(getInstanceIdentifier().toBuilder()
                        .augmentation(FlowCapableNode.class)
                        .child(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features
                                .TableFeatures.class, new TableFeaturesKey(stat.getTableId()))
                        .build(),
                    stat,
                    withParents);

                // Write parent for table statistics
                writeToTransaction(getInstanceIdentifier().toBuilder()
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class, new TableKey(stat.getTableId()))
                        .build(),
                    new TableBuilder()
                        .setId(stat.getTableId())
                        .addAugmentation(new FlowTableStatisticsDataBuilder().build())
                        .build(),
                    withParents);
            });
    }
}
