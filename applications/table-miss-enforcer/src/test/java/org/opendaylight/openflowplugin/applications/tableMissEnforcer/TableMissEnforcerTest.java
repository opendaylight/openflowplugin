/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.tableMissEnforcer;

import com.google.common.util.concurrent.Futures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link TableMissEnforcer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TableMissEnforcerTest {
    private TableMissEnforcer tableMissEnforcer;

    private static final InstanceIdentifier<FlowCapableNode> nodeIID = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("testnode:1")))
            .augmentation(FlowCapableNode.class);

    private static final InstanceIdentifier<FlowCapableNode> nodeII =
            InstanceIdentifier.create(Nodes.class)
                    .child(Node.class)
                    .augmentation(FlowCapableNode.class);
    @Mock
    private SalFlowService flowService;
    @Mock
    private DataTreeModification<FlowCapableNode> dataTreeModification;
    @Mock
    private DataObjectModification<FlowCapableNode> operationalModification;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonService;
    @Mock
    private ClusterSingletonServiceRegistration registration;

    @Before
    public void setUp() throws Exception {
        final DataTreeIdentifier<FlowCapableNode> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, nodeIID);
        Mockito.when(dataTreeModification.getRootPath()).thenReturn(identifier);
        Mockito.when(dataTreeModification.getRootNode()).thenReturn(operationalModification);

        Mockito.when(flowService.addFlow(Mockito.any())).thenReturn(Futures.immediateFuture(null));
        final NodeId nodeId = TableMissUtils.retreiveNodeId(dataTreeModification);

        Mockito.when(clusterSingletonService.registerClusterSingletonService(Mockito.any())).thenReturn(registration);
        tableMissEnforcer = new TableMissEnforcer(nodeId, dataTreeModification.getRootPath().getRootIdentifier(), clusterSingletonService, flowService);
    }

    @Test
    public void instantiateServiceInstance() throws Exception {
        tableMissEnforcer.instantiateServiceInstance();
        tableMissEnforcer.closeServiceInstance();
        tableMissEnforcer.close();

        Assert.assertNotNull(tableMissEnforcer.getIdentifier());
        Mockito.verify(flowService).addFlow(Mockito.any());
        Mockito.verify(registration).close();
    }

}