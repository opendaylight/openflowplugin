/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock;

import org.junit.Test;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import test.mock.util.StatisticsManagerTest;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NodeRegistrationTest extends StatisticsManagerTest {

    @Test
    public void nodeRegistrationTest() throws ExecutionException, InterruptedException {
        StatisticsManager statisticsManager = setupStatisticsManager();

        addFlowCapableNode(s1Key);
        Thread.sleep(2000);
        final InstanceIdentifier<Node> nodeII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key);

        assertTrue(statisticsManager.isProvidedFlowNodeActive(nodeII));
    }

    @Test
    public void nodeUnregistrationTest() throws ExecutionException, InterruptedException {
        StatisticsManager statisticsManager = setupStatisticsManager();

        addFlowCapableNode(s1Key);
        Thread.sleep(2000);
        final InstanceIdentifier<Node> nodeII = InstanceIdentifier.create(Nodes.class).child(Node.class, s1Key);

        assertTrue(statisticsManager.isProvidedFlowNodeActive(nodeII));

        removeNode(s1Key);
        Thread.sleep(2000);
        assertFalse(statisticsManager.isProvidedFlowNodeActive(nodeII));
    }
}
