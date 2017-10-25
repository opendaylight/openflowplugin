/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link BarrierUtil}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BarrierUtilTest {

    public static final NodeKey NODE_KEY = new NodeKey(new NodeId("ut-dummy-node"));
    private static final NodeRef NODE_REF = new NodeRef(InstanceIdentifier.create(Nodes.class)
            .child(Node.class, NODE_KEY));

    @Mock
    private FlowCapableTransactionService transactionService;
    @Mock
    private Function<Pair<RpcResult<String>, RpcResult<Void>>, RpcResult<String>> compositeTransform;
    @Captor
    private ArgumentCaptor<Pair<RpcResult<String>, RpcResult<Void>>> pairCpt;

    @Before
    public void setUp() throws Exception {
        Mockito.when(transactionService.sendBarrier(Matchers.<SendBarrierInput>any()))
                .thenReturn(RpcResultBuilder.<Void>success().buildFuture());
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(transactionService, compositeTransform);
    }

    @Test
    public void testChainBarrier() throws Exception {
        final String data = "ut-data1";
        final ListenableFuture<RpcResult<String>> input = RpcResultBuilder.success(data).buildFuture();
        final ListenableFuture<RpcResult<String>> chainResult =
                BarrierUtil.chainBarrier(input, NODE_REF, transactionService, compositeTransform);

        Mockito.verify(transactionService).sendBarrier(Matchers.<SendBarrierInput>any());
        Mockito.verify(compositeTransform).apply(pairCpt.capture());

        final Pair<RpcResult<String>, RpcResult<Void>> value = pairCpt.getValue();
        Assert.assertTrue(value.getLeft().isSuccessful());
        Assert.assertEquals(data, value.getLeft().getResult());
        Assert.assertTrue(value.getRight().isSuccessful());
        Assert.assertNull(value.getRight().getResult());

    }

    @Test
    public void testCreateSendBarrierInput() throws Exception {
        final SendBarrierInput barrierInput = BarrierUtil.createSendBarrierInput(NODE_REF);

        Assert.assertEquals(NODE_REF, barrierInput.getNode());
        Assert.assertEquals(SendBarrierInput.class, barrierInput.getImplementedInterface());
    }
}