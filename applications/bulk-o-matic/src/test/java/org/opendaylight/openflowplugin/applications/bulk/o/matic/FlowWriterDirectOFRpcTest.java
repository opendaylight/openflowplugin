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

import java.util.HashMap;
import java.util.Map;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link FlowWriterDirectOFRpc}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowWriterDirectOFRpcTest {
    private static final int FLOWS_PER_DPN = 100;

    @Mock
    private DataBroker mockDataBroker;
    @Mock
    private ExecutorService mockFlowPusher;
    @Mock
    private ReadTransaction readOnlyTransaction;
    @Mock
    private Nodes mockNodes;
    @Mock
    private AddFlow addFlow;

    private FlowWriterDirectOFRpc flowWriterDirectOFRpc;

    @Before
    public void setUp() {
        doReturn(RpcResultBuilder.success().buildFuture()).when(addFlow).invoke(any());

        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        NodeBuilder nodeBuilder = new NodeBuilder()
                .setId(new NodeId("1"));

        final Map<NodeKey, Node> nodes = new HashMap<>();
        final Node node = nodeBuilder.build();
        nodes.put(node.key(), node);

        when(mockNodes.nonnullNode()).thenReturn(nodes);

        doReturn(FluentFutures.immediateFluentFuture(Optional.of(mockNodes))).when(readOnlyTransaction)
            .read(any(LogicalDatastoreType.class), any(DataObjectIdentifier.class));

        Mockito.doAnswer(invocation -> {
            ((Runnable)invocation.getArguments()[0]).run();
            return null;
        }).when(mockFlowPusher).execute(ArgumentMatchers.any());

        flowWriterDirectOFRpc = new FlowWriterDirectOFRpc(mockDataBroker, mockFlowPusher, addFlow);
    }

    @Test
    public void testRpcFlowAdd() {
        flowWriterDirectOFRpc.rpcFlowAdd("1", FLOWS_PER_DPN, 10);
        Mockito.verify(addFlow, Mockito.times(FLOWS_PER_DPN)).invoke(Mockito.any());
    }

    @Test
    public void testRpcFlowAddAll() {
        flowWriterDirectOFRpc.rpcFlowAddAll(FLOWS_PER_DPN, 10);
        Mockito.verify(addFlow, Mockito.times(FLOWS_PER_DPN)).invoke(Mockito.any());
    }
}
