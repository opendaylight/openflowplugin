/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import java.util.List;
import java.util.Map;
import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Wraps all the required inputs (diffs) for synchronization strategy execution.
 */
public class SynchronizationDiffInput {

    private final InstanceIdentifier<FlowCapableNode> nodeIdent;
    private final List<ItemSyncBox<Group>> groupsToAddOrUpdate;
    private final ItemSyncBox<Meter> metersToAddOrUpdate;
    private final Map<TableKey, ItemSyncBox<Flow>> flowsToAddOrUpdate;
    private final Map<TableKey, ItemSyncBox<Flow>> flowsToRemove;
    private final ItemSyncBox<Meter> metersToRemove;
    private final List<ItemSyncBox<Group>> groupsToRemove;

    public SynchronizationDiffInput(final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                    final List<ItemSyncBox<Group>> groupsToAddOrUpdate,
                                    final ItemSyncBox<Meter> metersToAddOrUpdate,
                                    final Map<TableKey, ItemSyncBox<Flow>> flowsToAddOrUpdate,
                                    final Map<TableKey, ItemSyncBox<Flow>> flowsToRemove,
                                    final ItemSyncBox<Meter> metersToRemove,
                                    final List<ItemSyncBox<Group>> groupsToRemove) {
        this.nodeIdent = nodeIdent;
        this.groupsToAddOrUpdate = groupsToAddOrUpdate;
        this.metersToAddOrUpdate = metersToAddOrUpdate;
        this.flowsToAddOrUpdate = flowsToAddOrUpdate;
        this.flowsToRemove = flowsToRemove;
        this.metersToRemove = metersToRemove;
        this.groupsToRemove = groupsToRemove;
    }

    public InstanceIdentifier<FlowCapableNode> getNodeIdent() {
        return nodeIdent;
    }

    public List<ItemSyncBox<Group>> getGroupsToAddOrUpdate() {
        return groupsToAddOrUpdate;
    }

    public ItemSyncBox<Meter> getMetersToAddOrUpdate() {
        return metersToAddOrUpdate;
    }

    public Map<TableKey, ItemSyncBox<Flow>> getFlowsToAddOrUpdate() {
        return flowsToAddOrUpdate;
    }

    public Map<TableKey, ItemSyncBox<Flow>> getFlowsToRemove() {
        return flowsToRemove;
    }

    public ItemSyncBox<Meter> getMetersToRemove() {
        return metersToRemove;
    }

    public List<ItemSyncBox<Group>> getGroupsToRemove() {
        return groupsToRemove;
    }
}
