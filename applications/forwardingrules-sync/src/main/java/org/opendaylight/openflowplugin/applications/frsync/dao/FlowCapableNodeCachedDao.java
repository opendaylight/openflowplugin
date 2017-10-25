/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.dao;

import com.google.common.base.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Implementation of data access object for {@link FlowCapableNode}.
 * Contains pair of snapshot and odl DAOs.
 */
public class FlowCapableNodeCachedDao implements FlowCapableNodeDao {

    private final FlowCapableNodeDao snapshotDao;
    private final FlowCapableNodeDao odlDao;

    public FlowCapableNodeCachedDao(FlowCapableNodeDao snapshotDao, FlowCapableNodeDao odlDao) {
        this.snapshotDao = snapshotDao;
        this.odlDao = odlDao;
    }

    public Optional<FlowCapableNode> loadByNodeId(@Nonnull NodeId nodeId) {
        final Optional<FlowCapableNode> node = snapshotDao.loadByNodeId(nodeId);

        if (node.isPresent()) {
            return node;
        }

        return odlDao.loadByNodeId(nodeId);
    }

}
