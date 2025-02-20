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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterConfigStatsReply;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

public class MeterConfigMultipartWriter extends AbstractMultipartWriter<MeterConfigStatsReply> {
    private final DeviceRegistry registry;

    public MeterConfigMultipartWriter(final TxFacade txFacade,
                                      final WithKey<Node, NodeKey> instanceIdentifier,
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
        statistics.nonnullMeterConfigStats().values()
            .forEach(stat -> {
                writeToTransaction(
                    getInstanceIdentifier().toBuilder()
                        .augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(stat.getMeterId()))
                        .build(),
                    new MeterBuilder(stat)
                        .withKey(new MeterKey(stat.getMeterId()))
                        .addAugmentation(new NodeMeterStatisticsBuilder().build())
                        .build(),
                    withParents);

                registry.getDeviceMeterRegistry().store(stat.getMeterId());
            });
    }
}
