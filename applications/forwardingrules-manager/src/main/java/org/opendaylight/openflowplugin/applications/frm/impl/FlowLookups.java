/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * helpers for flow lookups in {@link FlowCapableNode}
 */
public final class FlowLookups {

    private static final Logger LOG = LoggerFactory.getLogger(FlowLookups.class);

    private FlowLookups() {
        throw new IllegalAccessError("non instantiable util class");
    }

    @Nullable
    public static Table findTableOnNode(@Nonnull final FlowCapableNode flowCapableNode, @Nonnull final TableKey tableKey) {
        Table foundTable = null;
        LOG.debug("searching for table {} in changed operational node", tableKey);
        final List<Table> tables = flowCapableNode.getTable();
        if (tables != null) {
            LOG.trace("tables found: {}", tables.size());
            for (Table tableOnNode : tables) {
                if (tableKey.equals(tableOnNode.getKey())) {
                    foundTable = tableOnNode;
                    break;
                }
            }
        }

        return foundTable;
    }

    @Nonnull
    public static Map<FlowId, Flow> wrapFlowsToMap(@Nonnull final Table table) {
        Map<FlowId, Flow> flowMap = Collections.emptyMap();
        LOG.debug("wrapping flows of table {} into map", table.getId());

        final List<Flow> flows = table.getFlow();
        if (flows != null) {
            LOG.trace("flows found: {}", flows.size());
            flowMap = new HashMap<>();
            for (Flow flow : flows) {
                flowMap.put(flow.getId(), flow);
            }
        }

        return flowMap;
    }
}
