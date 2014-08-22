/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline;

import org.opendaylight.of.lib.dt.TableId;

import java.util.Set;

/**
 * Abstracts the table feature capabilities into a pipeline definition.
 * <p>
 * The definition manages {@link TableContext table-contexts} for a device.
 * Implementing classes will build the definition based on table feature 
 * capabilities information, such as OpenFlow <em>Table-Feature Reply</em> 
 * message.
 *
 * @author Pramod Shanbhag
 * @author Simon Hunt
 */
public interface PipelineDefinition {
    
    /**
     * Returns table context for the given table ID. Returns null for a
     * table ID that does not exists in the definition.
     * 
     * @param tableId the table ID
     * @return the table context
     */
    TableContext getTableContext(TableId tableId);

    /**
     * Returns all the table IDs contained in the definition.
     * 
     * @return the set of table IDs
     */
    Set<TableId> getTableIds();

    /**
     * Returns true if at least one table context/ID is present.
     *
     * @return true if tables are defined
     */
    boolean hasTables();
}
