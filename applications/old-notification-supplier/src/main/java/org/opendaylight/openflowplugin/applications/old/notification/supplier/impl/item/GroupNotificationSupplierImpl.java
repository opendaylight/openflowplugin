/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.AbstractNofitSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class GroupNotificationSupplierImpl extends
        AbstractNofitSupplierDefinition<Group, GroupUpdated, GroupUpdated, GroupRemoved> {

    private final InstanceIdentifier<Group> wildCardedInstanceIdent;

    /**
     * @param notifProviderService
     * @param db
     */
    public GroupNotificationSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, Group.class);
        wildCardedInstanceIdent = getNodeWildII().augmentation(FlowCapableNode.class).child(Group.class);
    }

    @Override
    public InstanceIdentifier<Group> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public GroupUpdated createNotification(final Group o, final InstanceIdentifier<Group> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);
        final GroupUpdatedBuilder builder = new GroupUpdatedBuilder(o);
        builder.setGroupRef(new GroupRef(path));
        builder.setNode(new NodeRef(path.firstIdentifierOf(Node.class)));
        return builder.build();
    }

    @Override
    public GroupUpdated updateNotification(final Group o, final InstanceIdentifier<Group> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);
        final GroupUpdatedBuilder builder = new GroupUpdatedBuilder(o);
        builder.setGroupRef(new GroupRef(path));
        builder.setNode(new NodeRef(path.firstIdentifierOf(Node.class)));
        return builder.build();
    }

    @Override
    public GroupRemoved deleteNotification(final InstanceIdentifier<Group> path) {
        Preconditions.checkArgument(path != null);
        final GroupRemovedBuilder builder = new GroupRemovedBuilder();
        builder.setGroupId(path.firstKeyOf(Group.class, GroupKey.class).getGroupId());
        builder.setGroupRef(new GroupRef(path));
        builder.setNode(new NodeRef(path.firstIdentifierOf(Node.class)));
        return builder.build();
    }
}

