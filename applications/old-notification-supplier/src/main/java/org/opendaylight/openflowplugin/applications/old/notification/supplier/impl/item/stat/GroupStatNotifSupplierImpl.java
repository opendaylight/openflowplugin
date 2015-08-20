/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.stat;

import com.google.common.base.Preconditions;
import java.util.Collections;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.AbstractNofitSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class GroupStatNotifSupplierImpl extends
        AbstractNofitSupplierDefinition<GroupStatistics, GroupStatisticsUpdated, GroupStatisticsUpdated, GroupStatisticsUpdated> {

    private final InstanceIdentifier<GroupStatistics> wildCardedInstanceIdent;

    /**
     * @param notifProviderService
     * @param db
     */
    public GroupStatNotifSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, GroupStatistics.class);
        wildCardedInstanceIdent = getNodeWildII().augmentation(FlowCapableNode.class).child(Group.class)
                .augmentation(NodeGroupStatistics.class).child(GroupStatistics.class);
    }

    @Override
    public InstanceIdentifier<GroupStatistics> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public GroupStatisticsUpdated createNotification(final GroupStatistics o,
            final InstanceIdentifier<GroupStatistics> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);

        final GroupStatisticsUpdatedBuilder builder = new GroupStatisticsUpdatedBuilder();
        builder.setId(path.firstKeyOf(Node.class, NodeKey.class).getId());
        builder.setMoreReplies(Boolean.FALSE);
        // TODO : fix if it needs, but we have to ask DataStore for the NodeConnector list
        builder.setNodeConnector(Collections.<NodeConnector> emptyList());
        builder.setGroupStats(Collections.singletonList(new GroupStatsBuilder(o).build()));
        return builder.build();
    }

    @Override
    public GroupStatisticsUpdated updateNotification(final GroupStatistics o,
            final InstanceIdentifier<GroupStatistics> path) {
        // NOOP
        return null;
    }

    @Override
    public GroupStatisticsUpdated deleteNotification(final InstanceIdentifier<GroupStatistics> path) {
        // NOOP
        return null;
    }

}

