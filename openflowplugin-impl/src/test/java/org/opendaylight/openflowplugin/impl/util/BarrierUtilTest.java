/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link BarrierUtil}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BarrierUtilTest {
    public static final NodeKey NODE_KEY = new NodeKey(new NodeId("ut-dummy-node"));
    private static final NodeRef NODE_REF = new NodeRef(DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, NODE_KEY)
            .build());

    @Mock
    private SendBarrier sendBarrier;
    @Mock
    private Function<Pair<RpcResult<String>, RpcResult<SendBarrierOutput>>, RpcResult<String>> compositeTransform;
    @Captor
    private ArgumentCaptor<Pair<RpcResult<String>, RpcResult<SendBarrierOutput>>> pairCpt;

    @Before
    public void setUp() {
        when(sendBarrier.invoke(any()))
                .thenReturn(RpcResultBuilder.<SendBarrierOutput>success().buildFuture());
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(sendBarrier, compositeTransform);
    }

    @Test
    public void testChainBarrier() {
        final String data = "ut-data1";
        final ListenableFuture<RpcResult<String>> input = RpcResultBuilder.success(data).buildFuture();
        final ListenableFuture<RpcResult<String>> chainResult =
                BarrierUtil.chainBarrier(input, NODE_REF, sendBarrier, compositeTransform);

        verify(sendBarrier).invoke(any());
        verify(compositeTransform).apply(pairCpt.capture());

        final Pair<RpcResult<String>, RpcResult<SendBarrierOutput>> value = pairCpt.getValue();
        assertTrue(value.getLeft().isSuccessful());
        assertEquals(data, value.getLeft().getResult());
        assertTrue(value.getRight().isSuccessful());
        assertNull(value.getRight().getResult());
    }

    @Test
    public void testCreateSendBarrierInput() {
        final SendBarrierInput barrierInput = BarrierUtil.createSendBarrierInput(NODE_REF);

        assertEquals(NODE_REF, barrierInput.getNode());
        assertEquals(SendBarrierInput.class, barrierInput.implementedInterface());
    }
}
