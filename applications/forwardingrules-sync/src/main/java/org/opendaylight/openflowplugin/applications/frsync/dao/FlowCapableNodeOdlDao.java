/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.dao;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of data access object for ODL {@link FlowCapableNode}.
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

    @Override
    public Optional<FlowCapableNode> loadByNodeId(@NonNull NodeId nodeId) {
        try (ReadTransaction roTx = dataBroker.newReadOnlyTransaction()) {
            final InstanceIdentifier<FlowCapableNode> path =
                    NODES_IID.child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class);
            return roTx.read(logicalDatastoreType, path).get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            LOG.error("error reading {}", nodeId.getValue(), e);
        }

        return Optional.empty();
    }

}
