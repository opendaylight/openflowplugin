/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.dao;

import com.google.common.base.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Adding cache to data access object of {@link FlowCapableNode}.
 */
public class FlowCapableNodeSnapshotDao implements FlowCapableNodeDao {

    private final ConcurrentHashMap<String, FlowCapableNode> cache = new ConcurrentHashMap<>();

    public void updateCache(@Nonnull NodeId nodeId, Optional<FlowCapableNode> dataAfter) {
        if (dataAfter.isPresent()) {
            cache.put(nodeId.getValue(), dataAfter.get());
        } else {
            cache.remove(nodeId.getValue());
        }
    }

    public Optional<FlowCapableNode> loadByNodeId(@Nonnull NodeId nodeId) {
        final FlowCapableNode node = cache.get(nodeId.getValue());
        return Optional.fromNullable(node);
    }

}
