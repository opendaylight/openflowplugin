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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterConfigStatsReply;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MeterConfigMultipartWriter extends AbstractMultipartWriter<MeterConfigStatsReply> {

    private final DeviceRegistry registry;

    public MeterConfigMultipartWriter(final TxFacade txFacade,
                                      final InstanceIdentifier<Node> instanceIdentifier,
                                      final DeviceRegistry registry) {
        super(txFacade, instanceIdentifier);
        this.registry = registry;
    }

    @Override
    protected Class<MeterConfigStatsReply> getType() {
        return MeterConfigStatsReply.class;
    }

    @Override
    public void storeStatistics(final MeterConfigStatsReply statistics, final boolean withParents) {
        statistics.getMeterConfigStats()
            .forEach(stat -> {
                writeToTransaction(
                    getInstanceIdentifier()
                        .augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(stat.getMeterId())),
                    new MeterBuilder(stat)
                        .setKey(new MeterKey(stat.getMeterId()))
                        .addAugmentation(NodeMeterStatistics.class, new NodeMeterStatisticsBuilder().build())
                        .build(),
                    withParents);

                registry.getDeviceMeterRegistry().store(stat.getMeterId());
            });
    }

}
