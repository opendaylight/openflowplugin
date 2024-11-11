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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.node.meter.statistics.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.node.meter.statistics.MeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStatisticsReply;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

public class MeterStatsMultipartWriter extends AbstractMultipartWriter<MeterStatisticsReply> {
    public MeterStatsMultipartWriter(final TxFacade txFacade, final WithKey<Node, NodeKey> instanceIdentifier) {
        super(txFacade, instanceIdentifier);
    }

    @Override
    protected Class<MeterStatisticsReply> getType() {
        return MeterStatisticsReply.class;
    }

    @Override
    public void storeStatistics(final MeterStatisticsReply statistics, final boolean withParents) {
        statistics.nonnullMeterStats().values()
            .forEach(stat -> writeToTransaction(
                getInstanceIdentifier().toBuilder()
                    .augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(stat.getMeterId()))
                    .augmentation(NodeMeterStatistics.class)
                    .child(MeterStatistics.class)
                    .build(),
                new MeterStatisticsBuilder(stat).build(),
                withParents));
    }
}
