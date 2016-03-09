package org.opendaylight.openflowplugin.applications.frsync.dao;

import com.google.common.base.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author joslezak
 */
public class FlowCapableNodeOdlDao implements FlowCapableNodeDao {

    private static final Logger LOG = LoggerFactory.getLogger(FlowCapableNodeOdlDao.class);

    private static final InstanceIdentifier<Nodes> NODES_IID = InstanceIdentifier.create(Nodes.class);

    private final DataBroker dataBroker;
    private final LogicalDatastoreType logicalDatastoreType;

    public FlowCapableNodeOdlDao(DataBroker dataBroker, LogicalDatastoreType logicalDatastoreType) {
        this.dataBroker = dataBroker;
        this.logicalDatastoreType = logicalDatastoreType;
    }

    public Optional<FlowCapableNode> loadByNodeId(@Nonnull NodeId nodeId) {
        try (final ReadOnlyTransaction roTx = dataBroker.newReadOnlyTransaction()) {
            final InstanceIdentifier<FlowCapableNode> path =
                    NODES_IID.child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class);
            return roTx.read(logicalDatastoreType, path).checkedGet();
        } catch (ReadFailedException e) {
            LOG.error("error reading " + nodeId, e);
        }

        return Optional.absent();
    }
}
