/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.datastore;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStatisticsReply;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MeterStatisticsWriter extends AbstractStatisticsWriter<MeterStatisticsReply> {

    public MeterStatisticsWriter(final TxFacade txFacade, final InstanceIdentifier<Node> instanceIdentifier) {
        super(txFacade, instanceIdentifier);
    }

    @Override
    protected ListenableFuture<Boolean> storeStatistics(final List<MeterStatisticsReply> statistics, final boolean withParents) {
        statistics.stream()
            .flatMap(stats -> stats.getMeterStats().stream())
            .forEach(stat -> writeToTransaction(
                getFlowCapableNodeInstanceIdentifier()
                    .child(Meter.class, new MeterKey(stat.getMeterId()))
                    .augmentation(NodeMeterStatistics.class)
                    .child(MeterStatistics.class),
                new MeterStatisticsBuilder(stat).build(),
                withParents));

        return submitTransaction();
    }

}
