/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import org.opendaylight.openflowplugin.applications.frsync.util.ItemSyncBox;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

/**
 * Provides create methods for data involved in {@link SynchronizationDiffInput}.
 */
public class DiffInputFactory {
    static ItemSyncBox<Group> createGroupSyncBox(final long... groupIDs) {
        final ItemSyncBox<Group> groupBox = new ItemSyncBox<>();

        for (long gid : groupIDs) {
            groupBox.getItemsToPush().add(createPlainGroup(gid));
        }
        return groupBox;
    }

    static ItemSyncBox<Group> createGroupSyncBoxWithUpdates(final long... groupIDs) {
        final ItemSyncBox<Group> groupBox = new ItemSyncBox<>();

        for (long gid : groupIDs) {
            groupBox.getItemsToPush().add(createPlainGroup(gid));
            groupBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(createPlainGroup(gid + 50),
                    createPlainGroup(gid + 100)));
        }
        return groupBox;
    }

    private static Group createPlainGroup(final long gid) {
        return new GroupBuilder().setGroupId(new GroupId(gid)).build();
    }

    static ItemSyncBox<Meter> createMeterSyncBox(final long... meterIDs) {
        final ItemSyncBox<Meter> groupBox = new ItemSyncBox<>();

        for (long gid : meterIDs) {
            groupBox.getItemsToPush().add(createPlainMeter(gid));
        }
        return groupBox;
    }

    static ItemSyncBox<Meter> createMeterSyncBoxWithUpdates(final long... meterIDs) {
        final ItemSyncBox<Meter> groupBox = new ItemSyncBox<>();

        for (long mid : meterIDs) {
            groupBox.getItemsToPush().add(createPlainMeter(mid));
            groupBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(createPlainMeter(mid + 50),
                    createPlainMeter(mid + 100)));
        }
        return groupBox;
    }

    private static Meter createPlainMeter(final long mid) {
        return new MeterBuilder().setMeterId(new MeterId(mid)).build();
    }

    static ItemSyncBox<Flow> createFlowSyncBox(final String... flowIDs) {
        final ItemSyncBox<Flow> flowBox = new ItemSyncBox<>();

        for (String fid : flowIDs) {
            flowBox.getItemsToPush().add(createPlainFlow(fid));
        }
        return flowBox;
    }

    static ItemSyncBox<Flow> createFlowSyncBoxWithUpdates(final String... flowIDs) {
        final ItemSyncBox<Flow> groupBox = new ItemSyncBox<>();

        for (String fid : flowIDs) {
            groupBox.getItemsToPush().add(createPlainFlow(fid));
            groupBox.getItemsToUpdate().add(new ItemSyncBox.ItemUpdateTuple<>(createPlainFlow(fid + "orig"),
                    createPlainFlow(fid + "upd")));
        }
        return groupBox;
    }

    private static Flow createPlainFlow(final String fid) {
        return new FlowBuilder().setId(new FlowId(fid)).build();
    }
}
