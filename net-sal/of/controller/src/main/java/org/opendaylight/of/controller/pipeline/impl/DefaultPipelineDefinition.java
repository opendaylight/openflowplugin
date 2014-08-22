/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.controller.pipeline.TableContext;
import org.opendaylight.of.lib.dt.TableId;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.INDENT;

/**
 * Default implementation for {@link PipelineDefinition} based on
 * <em>TableFeatures</em> OpenFlow message.
 * <p>
 * This class manages tables and corresponding table contexts of a device.
 * It also abstracts the table capabilities into a definition.
 *  
 * @author Pramod Shanbhag
 * @author Simon Hunt
 */
public class DefaultPipelineDefinition implements PipelineDefinition {

    protected ConcurrentHashMap <TableId, TableContext> tableIdToContextMap = 
            new ConcurrentHashMap <>();

    @Override
    public TableContext getTableContext(TableId tableId) {
        return tableIdToContextMap.get(tableId);
    }

    @Override
    public Set<TableId> getTableIds() {
        // pipeline ordered set.
        return new TreeSet<>(tableIdToContextMap.keySet());
    }

    @Override
    public boolean hasTables() {
        return !tableIdToContextMap.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TableContexts :");
        int i = 0;
        for (TableContext ctx : tableIdToContextMap.values()) { 
            sb.append(EOLI).append("[Context# ").append(i)
               .append(INDENT).append(EOLI).append(ctx).append("]");
            i++;
        }
        return sb.toString();
    }
}
