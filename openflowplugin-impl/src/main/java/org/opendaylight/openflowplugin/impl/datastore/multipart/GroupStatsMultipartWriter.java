/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.datastore.multipart;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupStatisticsReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GroupStatsMultipartWriter extends AbstractMultipartWriter<GroupStatisticsReply> {

    private final DeviceRegistry deviceRegistry;

    public GroupStatsMultipartWriter(DeviceContext deviceContext, final InstanceIdentifier<Node> instanceIdentifier) {
        super(deviceContext, instanceIdentifier);
        this.deviceRegistry = deviceContext;
    }

    @Override
    protected Class<GroupStatisticsReply> getType() {
        return GroupStatisticsReply.class;
    }

    @Override
    public void storeStatistics(final GroupStatisticsReply statistics, final boolean withParents) {
        statistics.getGroupStats()
            .forEach(stat -> { writeToTransaction(
                getInstanceIdentifier()
                    .augmentation(FlowCapableNode.class)
                    .child(Group.class, new GroupKey(stat.getGroupId()))
                    .augmentation(NodeGroupStatistics.class)
                    .child(GroupStatistics.class),
                new GroupStatisticsBuilder(stat).build(),
                withParents);
            deviceRegistry.getDeviceGroupRegistry().store(stat.getGroupId());
            });
    }

}
