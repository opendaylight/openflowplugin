/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link FlowWriterDirectOFRpc}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowWriterDirectOFRpcTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowWriterDirectOFRpcTest.class);
    private static final int FLOWS_PER_DPN = 100;

    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private SalFlowService mockSalFlowService;
    @Mock
    private ExecutorService mockFlowPusher;
    @Mock
    private ReadTransaction readOnlyTransaction;
    @Mock
    private Nodes mockNodes;

    private FlowWriterDirectOFRpc flowWriterDirectOFRpc;

    @Before
    public void setUp() throws Exception {
        doReturn(RpcResultBuilder.success().buildFuture()).when(mockSalFlowService).addFlow(any());

        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        NodeBuilder nodeBuilder = new NodeBuilder()
                .setId(new NodeId("1"));

        final List<Node> nodes = new ArrayList<>();
        final Node node = nodeBuilder.build();
        nodes.add(node);

        when(mockNodes.getNode()).thenReturn(nodes);

        doReturn(FluentFutures.immediateFluentFuture(Optional.of(mockNodes))).when(readOnlyTransaction)
            .read(any(LogicalDatastoreType.class), any());

        Mockito.doAnswer(invocation -> {
            ((Runnable)invocation.getArguments()[0]).run();
            return null;
        }).when(mockFlowPusher).execute(ArgumentMatchers.any());

        flowWriterDirectOFRpc = new FlowWriterDirectOFRpc(mockDataBroker, mockSalFlowService, mockFlowPusher);
    }

    @Test
    public void testRpcFlowAdd() throws Exception {
        flowWriterDirectOFRpc.rpcFlowAdd("1", FLOWS_PER_DPN, 10);
        Mockito.verify(mockSalFlowService, Mockito.times(FLOWS_PER_DPN)).addFlow(Mockito.any());
    }

    @Test
    public void testRpcFlowAddAll() throws Exception {
        flowWriterDirectOFRpc.rpcFlowAddAll(FLOWS_PER_DPN, 10);
        Mockito.verify(mockSalFlowService, Mockito.times(FLOWS_PER_DPN)).addFlow(Mockito.any());
    }
}
