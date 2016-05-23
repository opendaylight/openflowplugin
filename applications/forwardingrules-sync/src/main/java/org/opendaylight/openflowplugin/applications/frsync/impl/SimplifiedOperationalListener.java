/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeDao;
import org.opendaylight.openflowplugin.applications.frsync.dao.FlowCapableNodeSnapshotDao;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to operational new nodes and delegates add/remove/update/barrier to {@link SyncReactor}.
 */
public class SimplifiedOperationalListener extends AbstractFrmSyncListener<Node> {
    private static final Logger LOG = LoggerFactory.getLogger(SimplifiedOperationalListener.class);

    protected final SyncReactor reactor;
    protected final FlowCapableNodeSnapshotDao operationalSnapshot;
    protected final FlowCapableNodeDao configDao;

    public SimplifiedOperationalListener(SyncReactor reactor, FlowCapableNodeSnapshotDao operationalSnapshot,
                                         FlowCapableNodeDao configDao) {
        this.reactor = reactor;
        this.operationalSnapshot = operationalSnapshot;
        this.configDao = configDao;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Node>> modifications) {
        LOG.trace("Inventory Operational changes {}", modifications.size());
        super.onDataTreeChanged(modifications);
    }

    /**
     * This method behaves like this:
     * <ul>
     * <li>If node is added to operational store then reconciliation.</li>
     * <li>Node is deleted from operational cache is removed.</li>
     * <li>Skip this event otherwise.</li>
     * </ul>
     *
     * @throws InterruptedException from syncup
     */
    protected Optional<ListenableFuture<Boolean>> processNodeModification(
            DataTreeModification<Node> modification) throws ReadFailedException, InterruptedException {

        updateCache(modification);
        if (isAdd(modification) || isAddLogical(modification)) {
            return reconciliation(modification);
        }
        return skipModification(modification);
    }

    /**
     * Remove if delete. Update only if FlowCapableNode Augmentation modified.
     *
     * @param modification Datastore modification
     */
    protected void updateCache(DataTreeModification<Node> modification) {
        try {
            if (isDelete(modification) || isDeleteLogical(modification)) {
                operationalSnapshot.updateCache(nodeId(modification), Optional.<FlowCapableNode>absent());
                return;
            }
            operationalSnapshot.updateCache(nodeId(modification), Optional.fromNullable(flowCapableNodeAfter(modification)));
        } catch(Exception e) {
            LOG.error("update cache failed {}", nodeId(modification), e);
        }
    }

    protected Optional<ListenableFuture<Boolean>> skipModification(DataTreeModification<Node> modification) {
        LOG.trace("Skipping Inventory Operational modification {}, before {}, after {}", nodeIdValue(modification),
                modification.getRootNode().getDataBefore() == null ? "null" : "nonnull",
                modification.getRootNode().getDataAfter() == null ? "null" : "nonnull");
        return Optional.absent();
    }

    /**
     * ModificationType.DELETE
     */
    protected boolean isDelete(DataTreeModification<Node> modification) {
        if (ModificationType.DELETE == modification.getRootNode().getModificationType()) {
            LOG.trace("Delete {} (physical)", nodeIdValue(modification));
            return true;
        }

        return false;
    }

    /**
     * All connectors disappeared from operational store (logical delete).
     */
    protected boolean isDeleteLogical(DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        if (!safeConnectorsEmpty(rootNode.getDataBefore()) && safeConnectorsEmpty(rootNode.getDataAfter())) {
            LOG.trace("Delete {} (logical)", nodeIdValue(modification));
            return true;
        }

        return false;
    }

    protected boolean isAdd(DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        final Node dataAfter = rootNode.getDataAfter();
        final Node dataBefore = rootNode.getDataBefore();

        final boolean nodeAppearedInOperational = dataBefore == null && dataAfter != null;
        if (nodeAppearedInOperational) {
            LOG.trace("Add {} (physical)", nodeIdValue(modification));
        }
        return nodeAppearedInOperational;
    }

    /**
     * All connectors appeared in operational store (logical add).
     */
    protected boolean isAddLogical(DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        if (safeConnectorsEmpty(rootNode.getDataBefore()) && !safeConnectorsEmpty(rootNode.getDataAfter())) {
            LOG.trace("Add {} (logical)", nodeIdValue(modification));
            return true;
        }

        return false;
    }

    protected Optional<ListenableFuture<Boolean>> reconciliation(
            DataTreeModification<Node> modification) throws InterruptedException {
        final NodeId nodeId = nodeId(modification);
        final Optional<FlowCapableNode> nodeConfiguration = configDao.loadByNodeId(nodeId);

        if (nodeConfiguration.isPresent()) {
            LOG.debug("Reconciliation: {}", nodeId.getValue());
            final InstanceIdentifier<FlowCapableNode> nodePath = InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, new NodeKey(nodeId(modification))).augmentation(FlowCapableNode.class);
            return Optional.of(reactor.syncup(nodePath, nodeConfiguration.get(), flowCapableNodeAfter(modification), dsType()));
        }
        else {
            return skipModification(modification);
        }
    }

    static FlowCapableNode flowCapableNodeAfter(DataTreeModification<Node> modification) {
        final Node dataAfter = modification.getRootNode().getDataAfter();
        if (dataAfter == null) {
            return null;
        }
        return dataAfter.getAugmentation(FlowCapableNode.class);
    }

    static boolean safeConnectorsEmpty(Node node) {
        if (node == null) {
            return true;
        }

        final List<NodeConnector> nodeConnectors = node.getNodeConnector();

        return nodeConnectors == null || nodeConnectors.isEmpty();
    }

    static String nodeIdValue(DataTreeModification<Node> modification) {
        final NodeId nodeId = nodeId(modification);

        if (nodeId == null) {
            return null;
        }

        return nodeId.getValue();
    }

    static NodeId nodeId(DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        final Node dataAfter = rootNode.getDataAfter();

        if (dataAfter != null) {
            return dataAfter.getId();
        }

        final Node dataBefore = rootNode.getDataBefore();
        if (dataBefore != null) {
            return dataBefore.getId();
        }

        return null;
    }

    @Override
    public LogicalDatastoreType dsType() {
        return LogicalDatastoreType.OPERATIONAL;
    }
}
