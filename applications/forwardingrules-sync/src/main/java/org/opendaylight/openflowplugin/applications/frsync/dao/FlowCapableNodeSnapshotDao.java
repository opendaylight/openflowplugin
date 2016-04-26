package org.opendaylight.openflowplugin.applications.frsync.dao;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import com.google.common.base.Optional;

/**
 * @author joslezak
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