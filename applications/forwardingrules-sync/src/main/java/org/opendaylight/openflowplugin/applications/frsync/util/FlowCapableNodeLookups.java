/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helpers for flow lookups in {@link FlowCapableNode}.
 */
public final class FlowCapableNodeLookups {
    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableNodeLookups.class);

    private FlowCapableNodeLookups() {
        // Hidden on purpose
    }

    public static @NonNull Map<Uint8, Table> wrapTablesToMap(final @Nullable Collection<Table> tables) {
        final Map<Uint8, Table> tableMap;

        if (tables == null) {
            tableMap = Collections.emptyMap();
        } else {
            LOG.trace("tables found: {}", tables.size());
            tableMap = new HashMap<>();
            for (Table table : tables) {
                tableMap.put(table.getId(), table);
            }
        }

        return tableMap;
    }

    public static @NonNull Map<FlowDescriptor, Flow> wrapFlowsToMap(final @Nullable Collection<Flow> flows) {
        final Map<FlowDescriptor, Flow> flowMap;

        if (flows == null) {
            flowMap = Collections.emptyMap();
        } else {
            LOG.trace("flows found: {}", flows.size());
            flowMap = new HashMap<>();
            for (Flow flow : flows) {
                flowMap.put(new FlowDescriptor(flow), flow);
            }
        }

        return flowMap;
    }

    public static Flow flowMapLookupExisting(Flow flow, Map<FlowDescriptor, Flow> flowConfigMap) {
        return flowConfigMap.get(new FlowDescriptor(flow));
    }

    public static @NonNull Map<MeterId, Meter> wrapMetersToMap(final @Nullable Collection<Meter> meters) {
        final Map<MeterId, Meter> meterMap;

        if (meters == null) {
            meterMap = Collections.emptyMap();
        } else {
            LOG.trace("meters found: {}", meters.size());
            meterMap = new HashMap<>();
            for (Meter meter : meters) {
                meterMap.put(meter.getMeterId(), meter);
            }
        }

        return meterMap;
    }

    public static @NonNull Map<Uint32, Group> wrapGroupsToMap(final @Nullable Collection<Group> groups) {
        final Map<Uint32, Group> groupMap;

        if (groups == null) {
            groupMap = Collections.emptyMap();
        } else {
            LOG.trace("groups found: {}", groups.size());
            groupMap = new HashMap<>();
            for (Group group : groups) {
                groupMap.put(group.getGroupId().getValue(), group);
            }
        }

        return groupMap;
    }
}
