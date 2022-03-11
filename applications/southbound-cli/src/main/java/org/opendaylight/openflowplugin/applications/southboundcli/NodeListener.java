/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeListener implements ClusteredDataTreeChangeListener<FlowCapableNode>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(NodeListener.class);
    public static final String DEFAULT_DPN_NAME = "UNKNOWN";
    public static final String SEPARATOR = ":";

    private final Map<Long, String> dpnIdToNameCache = new ConcurrentHashMap<>();
    private final DataBroker dataBroker;
    private ListenerRegistration<?> listenerReg;

    public NodeListener(final DataBroker broker) {
        dataBroker = broker;
    }

    public void start() {
        final InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class);
        final DataTreeIdentifier<FlowCapableNode> identifier =
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, path);
        listenerReg = dataBroker.registerDataTreeChangeListener(identifier, NodeListener.this);
    }

    @Override
    public void close() {
        if (listenerReg != null) {
            listenerReg.close();
        }
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<FlowCapableNode>> changes) {
        requireNonNull(changes, "Changes may not be null!");
        for (DataTreeModification<FlowCapableNode> change : changes) {
            final InstanceIdentifier<FlowCapableNode> key = change.getRootPath().getRootIdentifier();
            final DataObjectModification<FlowCapableNode> mod = change.getRootNode();
            final InstanceIdentifier<FlowCapableNode> nodeIdent = key.firstIdentifierOf(FlowCapableNode.class);
            switch (mod.getModificationType()) {
                case DELETE:
                    remove(nodeIdent, mod.getDataBefore());
                    break;
                case SUBTREE_MODIFIED:
                    update(nodeIdent, mod.getDataBefore(), mod.getDataAfter());
                    break;
                case WRITE:
                    if (mod.getDataBefore() == null) {
                        add(nodeIdent, mod.getDataAfter());
                    } else {
                        update(nodeIdent, mod.getDataBefore(), mod.getDataAfter());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
            }
        }
    }

    private void remove(final InstanceIdentifier<FlowCapableNode> instId, final FlowCapableNode delNode) {
        LOG.trace("Received remove notification for {}", delNode);
        String[] node = instId.firstKeyOf(Node.class).getId().getValue().split(SEPARATOR);
        if (node.length < 2) {
            LOG.error("Failed to remove Unexpected nodeId {}", instId.firstKeyOf(Node.class).getId()
                    .getValue());
            return;
        }
        long dpnId = Long.parseLong(node[1]);
        dpnIdToNameCache.remove(dpnId);
    }

    private void update(final InstanceIdentifier<FlowCapableNode> instId,
            final FlowCapableNode dataObjectModificationBefore, final FlowCapableNode dataObjectModificationAfter) {

        LOG.trace("Received update notification {}", instId);
        String[] node = instId.firstKeyOf(Node.class).getId().getValue().split(SEPARATOR);
        if (node.length < 2) {
            LOG.error("Failed to add Unexpected nodeId {}", instId.firstKeyOf(Node.class).getId().getValue());
            return;
        }
        long dpnId = Long.parseLong(node[1]);
        String nodeName = dataObjectModificationAfter == null ? null : dataObjectModificationAfter.getDescription();
        if (nodeName != null) {
            dpnIdToNameCache.put(dpnId, nodeName);
        } else {
            dpnIdToNameCache.put(dpnId, DEFAULT_DPN_NAME);
        }
    }

    private void add(final InstanceIdentifier<FlowCapableNode> instId, final FlowCapableNode addNode) {
        LOG.trace("Received ADD notification for {}", instId);
        String[] node = instId.firstKeyOf(Node.class).getId().getValue().split(SEPARATOR);
        if (node.length < 2) {
            LOG.error("Failed to add Unexpected nodeId {}", instId.firstKeyOf(Node.class).getId().getValue());
            return;
        }
        long dpnId = Long.parseLong(node[1]);
        String dpnName = addNode == null ? null : addNode.getDescription();
        LOG.trace("Adding DPNID {} to cache", dpnId);
        if (dpnName == null) {
            dpnName = DEFAULT_DPN_NAME;
        }
        dpnIdToNameCache.put(dpnId, dpnName);
    }

    public Map<Long, String> getDpnIdToNameCache() {
        return dpnIdToNameCache;
    }
}