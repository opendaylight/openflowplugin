/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link PathUtil}.
 */
public class PathUtilTest {

    private static final NodeId NODE_ID = new NodeId("ut-dummy-node");
    private static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    private static final InstanceIdentifier<Node> NODE_II
            = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY);
    private static final NodeRef NODE_REF = new NodeRef(NODE_II.toIdentifier());

    @Test
    public void testExtractNodeId() {
        Assert.assertEquals(NODE_ID, PathUtil.extractNodeId(NODE_REF));
    }

    @Test
    public void testExtractNodeId2() {
        Assert.assertEquals(NODE_ID, PathUtil.extractNodeId(NODE_II));
    }
}