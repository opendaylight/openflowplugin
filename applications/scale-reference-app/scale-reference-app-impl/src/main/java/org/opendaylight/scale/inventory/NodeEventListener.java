/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.scale.inventory;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.scale.reconciliation.FlowReconciliation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NodeEventListener listens for clustered DataTree node events and updates node in local map.
 * It gets notification whenever node is added/deleted in operational data store.
 */
public class NodeEventListener implements ClusteredDataTreeChangeListener<Node> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeEventListener.class);

    private Set<String> dpnSet = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());

    private final DataBroker dataBroker;


    public NodeEventListener(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        Preconditions.checkNotNull(dataBroker, "DataBroker Cannot be null!");
    }

    /**
     * register listener to get Node add/removed event.
     */
    public void register() {
        final DataTreeIdentifier<Node> treeId = new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, getWildCardNodePath());
        dataBroker.registerDataTreeChangeListener(treeId, NodeEventListener.this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Node>> changes) {
        Preconditions.checkNotNull(changes, "Data Tree Changes cannot be null");
        for (DataTreeModification<Node> change : changes) {
            DataObjectModification<Node> mod = change.getRootNode();
            switch (mod.getModificationType()) {
                case DELETE:
                    nodeDisconnected(mod.getDataBefore());
                    break;
                case SUBTREE_MODIFIED:
                    break;
                case WRITE:
                    if (mod.getDataBefore() == null) {
                        nodeConnected(mod.getDataAfter());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled node modification type " + mod.getModificationType());
            }
        }
    }

    private void nodeConnected(Node node) {
        LOG.info("Node {} is connected.", node.getId().getValue());

        dpnSet.add(node.getId().getValue());
        FlowReconciliation flowReconciliation = new FlowReconciliation();
        flowReconciliation.start(node);
    }

    private void nodeDisconnected(Node node) {
        LOG.info("Node {} is disconnected.", node.getId().getValue());
        dpnSet.remove(node.getId().getValue());
    }

    private InstanceIdentifier<Node> getWildCardNodePath() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class);
    }

    /**
     * return Set of dpn id
     *
     * @return dpnSet
     */
    public Set<String> getDpnSet() {
        return new HashSet<>(dpnSet);
    }
}