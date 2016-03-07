package org.opendaylight.openflowplugin.applications.frsync.dao;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Optional;

/**
 * @author joslezak
 */
public class FlowCapableNodeSnapshotDao implements FlowCapableNodeDao {

    private final ConcurrentHashMap<NodeId, FlowCapableNode> cache = new ConcurrentHashMap<>();

    public void modification(@Nonnull DataTreeModification<FlowCapableNode> modification) {
        final InstanceIdentifier<FlowCapableNode> nodePath = modification.getRootPath().getRootIdentifier();
        final NodeId nodeId = PathUtil.digNodeId(nodePath);

        final FlowCapableNode dataAfter = modification.getRootNode().getDataAfter();
        if (dataAfter == null) {
            cache.remove(nodeId);
        } else {
            cache.put(nodeId, dataAfter);
        }
    }

    public Optional<FlowCapableNode> loadByNodeId(@Nonnull NodeId nodeId) {
        final FlowCapableNode node = cache.get(nodeId);
        return Optional.fromNullable(node);
    }

}
