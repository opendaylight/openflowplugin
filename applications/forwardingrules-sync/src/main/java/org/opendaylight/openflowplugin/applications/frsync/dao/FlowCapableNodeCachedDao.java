package org.opendaylight.openflowplugin.applications.frsync.dao;

import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import com.google.common.base.Optional;

/**
 * @author joslezak
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
