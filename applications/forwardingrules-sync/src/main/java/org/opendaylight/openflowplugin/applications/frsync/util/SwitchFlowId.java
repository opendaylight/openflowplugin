/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;

/**
 * Identifier of {@link Flow} on device. Switch does not know about flow-id but,
 * it uses combination of these unique fields: table-id, priority, match.
 */
public class SwitchFlowId {

    private final Short tableId;

    private final Integer priority;

    private final Match match;

    public SwitchFlowId(Flow flow) {
        this.tableId = flow.getTableId();
        this.priority = flow.getPriority();
        this.match = flow.getMatch();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SwitchFlowId that = (SwitchFlowId) o;

        if (tableId != null ? !tableId.equals(that.tableId) : that.tableId != null) {
            return false;
        }
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) {
            return false;
        }
        return match != null ? match.equals(that.match) : that.match == null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((match == null) ? 0 : match.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
        return result;
    }

}
