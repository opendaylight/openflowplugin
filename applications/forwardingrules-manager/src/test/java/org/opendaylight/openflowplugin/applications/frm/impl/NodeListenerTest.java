/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class NodeListenerTest extends AbstractFRMTest {
    private static final NodeKey NODE_KEY = new NodeKey(new NodeId("testnode:1"));

    @Before
    public void setUp() {
        setUpForwardingRulesManager();
    }

    @Test
    public void addRemoveNodeTest() throws Exception {
        addFlowCapableNode(NODE_KEY);

        var nodeII = DataObjectIdentifier.builder(Nodes.class)
                .child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class)
                .build();
        boolean nodeActive = getForwardingRulesManager().isNodeActive(nodeII);
        assertTrue(nodeActive);
        removeNode();
        nodeActive = getForwardingRulesManager().isNodeActive(nodeII);
        assertFalse(nodeActive);
    }

    @After
    public void tearDown() throws Exception {
        getForwardingRulesManager().close();
    }

    private void removeNode() throws ExecutionException, InterruptedException {
        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.OPERATIONAL,
                DataObjectIdentifier.builder(Nodes.class).child(Node.class, NODE_KEY).build());
        writeTx.commit().get();
    }
}
