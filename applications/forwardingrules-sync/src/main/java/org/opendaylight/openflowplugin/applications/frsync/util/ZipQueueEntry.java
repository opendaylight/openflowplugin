/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.frsync.impl.SyncReactorFutureZipDecorator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;

/**
 * Simple compression queue entry for {@link SyncReactorFutureZipDecorator}.
 */
public class ZipQueueEntry {
    private final FlowCapableNode after;
    private final FlowCapableNode before;
    private final LogicalDatastoreType dsTypeBefore;

    public ZipQueueEntry(final FlowCapableNode after, final FlowCapableNode before,
                         final LogicalDatastoreType dsTypeBefore) {
        this.after = after;
        this.before = before;
        this.dsTypeBefore = dsTypeBefore;
    }

    public FlowCapableNode getLeft() {
        return after;
    }

    public FlowCapableNode getRight() {
        return before;
    }

    public LogicalDatastoreType getDsType() {
        return dsTypeBefore;
    }

}
