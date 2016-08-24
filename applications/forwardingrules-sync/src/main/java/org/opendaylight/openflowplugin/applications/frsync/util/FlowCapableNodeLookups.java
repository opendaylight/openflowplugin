/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helpers for flow lookups in {@link FlowCapableNode}.
 */
public final class FlowCapableNodeLookups {

    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableNodeLookups.class);

    private FlowCapableNodeLookups() {
        throw new IllegalAccessError("non instantiable util class");
    }

    @Nonnull
    public static Map<Short, Table> wrapTablesToMap(@Nullable final List<Table> tables) {
        final Map<Short, Table> tableMap;

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

    @Nonnull
    public static Map<FlowDescriptor, Flow> wrapFlowsToMap(@Nullable final List<Flow> flows) {
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

    @Nonnull
    public static Map<MeterId, Meter> wrapMetersToMap(@Nullable final List<Meter> meters) {
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

    @Nonnull
    public static Map<Long, Group> wrapGroupsToMap(@Nullable final List<Group> groups) {
        final Map<Long, Group> groupMap;

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
