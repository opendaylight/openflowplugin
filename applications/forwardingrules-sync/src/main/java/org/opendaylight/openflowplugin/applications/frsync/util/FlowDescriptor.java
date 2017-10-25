/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;

/**
 * Identifier of {@link Flow} in datastore using combination of flow-id and table-id from datastore datapath.
 */
public class FlowDescriptor {

    private final FlowId flowId;
    private final Short tableId;

    public FlowDescriptor(Flow flow) {
        this.flowId = flow.getId();
        this.tableId = flow.getTableId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FlowDescriptor that = (FlowDescriptor) o;
        if (flowId != null ? !flowId.equals(that.flowId) : that.flowId != null) {
            return false;
        }
        return tableId != null ? tableId.equals(that.tableId) : that.tableId == null;

    }

    @Override
    public int hashCode() {
        int result = flowId != null ? flowId.hashCode() : 0;
        result = 31 * result + (tableId != null ? tableId.hashCode() : 0);
        return result;
    }

}
