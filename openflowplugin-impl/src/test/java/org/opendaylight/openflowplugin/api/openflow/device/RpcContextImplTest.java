/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.impl.rpc.RpcContextImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 */
@RunWith(MockitoJUnitRunner.class)
public class RpcContextImplTest {

    @Mock
    private BindingAwareBroker.ProviderContext mockedRpcProviderRegistry;

    @Mock
    private DeviceContext deviceContext;

    private RpcContext rpcContext;

    private static final String QUEUE_IS_FULL = "Device's request queue is full.";

    @Before
    public void setup() {
        NodeId nodeId = new NodeId("openflow:1");
        KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));

        rpcContext = new RpcContextImpl(mockedRpcProviderRegistry, nodeInstanceIdentifier);
    }

    @Test
    public void invokeRpcTest() {

    }

    @Test
    public void testStoreOrFail() throws Exception {
        RequestContext requestContext = rpcContext.createRequestContext();
        rpcContext.setRequestContextQuota(100);
        Future<RpcResult<UpdateFlowOutput>> resultFuture = rpcContext.storeOrFail(requestContext);
        assertNotNull(resultFuture);
        assertFalse(resultFuture.isDone());
    }

    @Test
    public void testStoreOrFailThatFails() throws Exception {
        RequestContext requestContext = rpcContext.createRequestContext();
        rpcContext.setRequestContextQuota(0);
        Future<RpcResult<UpdateFlowOutput>> resultFuture = rpcContext.storeOrFail(requestContext);
        assertNotNull(resultFuture);
        assertTrue(resultFuture.isDone());
        RpcResult<UpdateFlowOutput> updateFlowOutputRpcResult = resultFuture.get();
        assertNotNull(updateFlowOutputRpcResult);
        assertEquals(1, updateFlowOutputRpcResult.getErrors().size());
        Iterator<RpcError> iterator = updateFlowOutputRpcResult.getErrors().iterator();
        assertEquals(QUEUE_IS_FULL, iterator.next().getMessage());
    }

}
