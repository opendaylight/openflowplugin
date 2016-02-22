/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * @author joe
 */
@RunWith(MockitoJUnitRunner.class)
public class RpcContextImplTest {

    @Mock
    private BindingAwareBroker.ProviderContext mockedRpcProviderRegistry;
    @Mock
    private DeviceState deviceState;
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private MessageSpy messageSpy;
    @Mock
    private NotificationPublishService notificationPublishService;

    private KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;

    @Before
    public void setup() {
        final NodeId nodeId = new NodeId("openflow:1");
        nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));

        when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
    }

    @Test
    public void invokeRpcTest() {

    }

    @Test
    public void testStoreOrFail() throws Exception {
        try (final RpcContext rpcContext = new RpcContextImpl(messageSpy, mockedRpcProviderRegistry, deviceContext, 100)) {
            final RequestContext<?> requestContext = rpcContext.createRequestContext();
            assertNotNull(requestContext);
        }
    }

    @Test
    public void testStoreOrFailThatFails() throws Exception {
        try (final RpcContext rpcContext = new RpcContextImpl(messageSpy, mockedRpcProviderRegistry, deviceContext, 0)) {
            final RequestContext<?> requestContext = rpcContext.createRequestContext();
            assertNull(requestContext);
        }
    }
}
