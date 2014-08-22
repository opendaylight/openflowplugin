/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.dt.TableId;

import java.util.Collections;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.util.StringUtils.spaces;

/**
 * Represents a "Next-Table" table feature property. This implementation
 * provides the data as a set of table IDs for the tables that can be reached
 * directly from the present table.
 *
 * @author Simon Hunt
 */
public class TableFeaturePropNextTable extends TableFeatureProp {
    Set<TableId> nextTables;

    /** Constructs a table feature property.
     *
     * @param header the property header
     */
    TableFeaturePropNextTable(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        final int len = sb.length();
        sb.replace(len-1, len, ": nextTables=").append(nextTables).append("}");
        return sb.toString();
    }

    @Override
    public int getTotalLength() {
        return header.length;
    }

    @Override
    String toDebugString(int indent) {
        StringBuilder sb = new StringBuilder(super.toDebugString(indent));
        final String indStr = EOLI + spaces(indent + 2);
        sb.append(indStr).append("Next Tables: ").append(nextTables);
        return sb.toString();
    }

    /** Returns the set of IDs of tables that can be reached directly
     * from the current table.
     *
     * @return the set of next-table IDs
     */
    public Set<TableId> getNextTables() {
        return Collections.unmodifiableSet(nextTables);
    }
}
