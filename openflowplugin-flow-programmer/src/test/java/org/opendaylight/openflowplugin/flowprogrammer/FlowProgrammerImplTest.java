/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer;

import static org.junit.Assert.*;
import org.opendaylight.openflowplugin.flowprogrammer.FlowProgrammerImpl;

import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.flowprogrammer.AbstractDataStoreManager;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;

import com.google.common.base.Optional;

public class FlowProgrammerImplTest extends AbstractDataStoreManager {
    FlowProgrammerImpl flowProgrammerImpl;

    @Override
    protected void setupWithDataBroker(DataBroker dataBroker) {
        flowProgrammerImpl = new FlowProgrammerImpl();
        flowProgrammerImpl.setDataProvider( dataBroker );
    }

    @Test
    public void testFlowProgrammerImpl() throws Exception {
        // Create the NodeBuilder
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setId(new NodeId("openflow:1"));
        nodeBuilder.setKey(new NodeKey(nodeBuilder.getId()));

        InstanceIdentifier<Node> NODE_IID = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeBuilder.getKey()).build();

        DataBroker broker = getDataBroker();

        ReadOnlyTransaction rTx = broker.newReadOnlyTransaction();
        Optional<Node> optional = rTx.read( LogicalDatastoreType.OPERATIONAL, NODE_IID ).get();
        assertNotNull( optional );
        assertFalse( "Didn't expect operational data for node", optional.isPresent() );

        Optional<Node> rNode = rTx.read( LogicalDatastoreType.CONFIGURATION, NODE_IID ).get();
        assertFalse( "Didn't expect config data for node.",
                     rNode.isPresent() );
    }
}
