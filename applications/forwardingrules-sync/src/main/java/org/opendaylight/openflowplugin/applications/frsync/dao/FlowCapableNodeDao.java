package org.opendaylight.openflowplugin.applications.frsync.dao;

import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import com.google.common.base.Optional;

/**
 * @author joslezak
 */
public interface FlowCapableNodeDao {
    public Optional<FlowCapableNode> loadByNodeId(@Nonnull NodeId nodeId);
}
