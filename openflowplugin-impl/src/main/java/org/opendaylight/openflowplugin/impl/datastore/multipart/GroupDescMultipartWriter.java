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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupDescStatsReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

public class GroupDescMultipartWriter extends AbstractMultipartWriter<GroupDescStatsReply> {
    private final DeviceRegistry registry;

    public GroupDescMultipartWriter(final TxFacade txFacade,
                                    final WithKey<Node, NodeKey> instanceIdentifier,
                                    final DeviceRegistry registry) {
        super(txFacade, instanceIdentifier);
        this.registry = registry;
    }

    @Override
    protected Class<GroupDescStatsReply> getType() {
        return GroupDescStatsReply.class;
    }

    @Override
    public void storeStatistics(final GroupDescStatsReply statistics, final boolean withParents) {
        statistics.nonnullGroupDescStats().values()
            .forEach(stat -> {
                writeToTransaction(
                    getInstanceIdentifier().toBuilder()
                        .augmentation(FlowCapableNode.class)
                        .child(Group.class, new GroupKey(stat.getGroupId()))
                        .build(),
                    new GroupBuilder(stat)
                        .withKey(new GroupKey(stat.getGroupId()))
                        .addAugmentation(new NodeGroupStatisticsBuilder().build())
                        .build(),
                    withParents);

                registry.getDeviceGroupRegistry().store(stat.getGroupId());
            });
    }
}
