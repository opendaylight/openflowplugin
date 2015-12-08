/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl.util;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link ReconcileUtil}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReconcileUtilTest {

    private static final NodeId NODE_ID = new NodeId("unit-node-id");
    private InstanceIdentifier<FlowCapableNode> NODE_IDENT = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(NODE_ID)).augmentation(FlowCapableNode.class);
    @Mock
    private FlowCapableTransactionService flowCapableService;
    @Captor
    private ArgumentCaptor<SendBarrierInput> barrierInputCaptor;

    @Test
    public void testChainBarrierFlush() throws Exception {
        SettableFuture<RpcResult<Void>> testRabbit = SettableFuture.create();
        final ListenableFuture<RpcResult<Void>> vehicle =
                Futures.transform(testRabbit, ReconcileUtil.chainBarrierFlush(NODE_IDENT, flowCapableService));
        Mockito.when(flowCapableService.sendBarrier(barrierInputCaptor.capture()))
                .thenReturn(RpcResultBuilder.<Void>success().buildFuture());

        Mockito.verify(flowCapableService, Mockito.never()).sendBarrier(Matchers.<SendBarrierInput>any());
        Assert.assertFalse(vehicle.isDone());

        testRabbit.set(RpcResultBuilder.<Void>success().build());
        Mockito.verify(flowCapableService).sendBarrier(Matchers.<SendBarrierInput>any());
        Assert.assertTrue(vehicle.isDone());
        Assert.assertTrue(vehicle.get().isSuccessful());
    }
}