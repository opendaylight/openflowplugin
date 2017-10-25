/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;

/**
 * Data entry of before and after data for syncup in {@link org.opendaylight.openflowplugin.applications.frsync.SyncReactor}.
 */
public class SyncupEntry {
    private final FlowCapableNode after;
    private final LogicalDatastoreType dsTypeAfter;
    private final FlowCapableNode before;
    private final LogicalDatastoreType dsTypeBefore;

    public SyncupEntry(final FlowCapableNode after, final LogicalDatastoreType dsTypeAfter,
                       final FlowCapableNode before, final LogicalDatastoreType dsTypeBefore) {
        this.after = after;
        this.dsTypeAfter = dsTypeAfter;
        this.before = before;
        this.dsTypeBefore = dsTypeBefore;
    }

    public FlowCapableNode getAfter() {
        return after;
    }

    public FlowCapableNode getBefore() {
        return before;
    }

    public LogicalDatastoreType getDsTypeAfter() {
        return dsTypeAfter;
    }

    public LogicalDatastoreType getDsTypeBefore() {
        return dsTypeBefore;
    }

    public boolean isOptimizedConfigDelta() {
        return dsTypeAfter == LogicalDatastoreType.CONFIGURATION && dsTypeBefore == LogicalDatastoreType.CONFIGURATION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SyncupEntry that = (SyncupEntry) o;

        if (after != null ? !after.equals(that.after) : that.after != null) {
            return false;
        }
        if (dsTypeAfter != that.dsTypeAfter) {
            return false;
        }
        if (before != null ? !before.equals(that.before) : that.before != null) {
            return false;
        }
        return dsTypeBefore == that.dsTypeBefore;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = after != null ? after.hashCode() : 0;
        result = prime * result + (dsTypeAfter != null ? dsTypeAfter.hashCode() : 0);
        result = prime * result + (before != null ? before.hashCode() : 0);
        result = prime * result + (dsTypeBefore != null ? dsTypeBefore.hashCode() : 0);
        return result;
    }

}
