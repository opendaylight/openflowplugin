/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.dt.TableId;

import java.util.Collections;
import java.util.Set;

/**
 * Represents an OpenFlow TABLE_MOD message; Since 1.1; Deprecated in 1.3.
 *
 * @author Simon Hunt
 */
public class OfmTableMod extends OpenflowMessage {
    TableId tableId;
    Set<TableConfig> config;

    /**
     * Constructs an OpenFlow TABLE_MOD message.
     *
     * @param header the message header
     */
    OfmTableMod(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",tabId=").append(tableId)
                .append(",cfg=").append(config)
                .append("}");
        return sb.toString();
    }

    /** Returns the table id; Since 1.1.
     * If this value is {@link TableId#ALL}, the configuration
     * applies to <em>all</em> tables.
     * 
     * @return table identifier 
     */
    public TableId getTableId() {
        return tableId;
    }

    /** Returns the table configuration flags; Since 1.1;
     * Removed (reserved) since 1.3.
     * This method returns {@code null} for version 1.3.
     *
     * @return the table configuration flags
     */
    public Set<TableConfig> getConfig() {
        return config == null ? null : Collections.unmodifiableSet(config);
    }

}
