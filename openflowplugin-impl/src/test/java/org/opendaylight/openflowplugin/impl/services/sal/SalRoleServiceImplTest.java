/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class SalRoleServiceImplTest {

    @Mock
    private RequestContextStack mockRequestContextStack;

    @Mock
    private DeviceContext mockDeviceContext;

    @Mock
    private FeaturesReply mockFeaturesReply;

    @Mock
    private ConnectionContext mockConnectionContext;

    @Mock
    private MessageSpy mockMessageSpy;

    @Mock
    private RequestContext<RoleRequestOutput> mockRequestContext;

    @Mock
    private DeviceState mockDeviceState;

    @Mock
    private DeviceInfo mockDeviceInfo;

    @Mock
    private GetFeaturesOutput mockFeaturesOutput;

    @Mock
    private OutboundQueue mockOutboundQueue;

    private final NodeId testNodeId = new NodeId(Uri.getDefaultInstance("openflow:1"));

    private static final Uint32 testXid = Uint32.valueOf(100);

    private static final String ROLEREQUESTFAILED = org.opendaylight.yang.gen.v1.urn
            .opendaylight.openflow.common.types.rev130731.ErrorType.ROLEREQUESTFAILED.name();

    private static final String ROLES_UNSUPPORTED = "Device reported error type " + ROLEREQUESTFAILED + " code UNSUP";

    private NodeRef nodeRef;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockDeviceInfo.getNodeId()).thenReturn(testNodeId);
        Mockito.when(mockDeviceInfo.getDatapathId()).thenReturn(Uint64.valueOf(10));
        Uint8 testVersion = Uint8.valueOf(4);
        Mockito.when(mockFeaturesOutput.getVersion()).thenReturn(testVersion);
        Mockito.when(mockDeviceContext.getDeviceState()).thenReturn(mockDeviceState);
        Mockito.when(mockDeviceContext.getDeviceInfo()).thenReturn(mockDeviceInfo);
        Mockito.when(mockDeviceContext.getPrimaryConnectionContext()).thenReturn(mockConnectionContext);
        Mockito.when(mockConnectionContext.getFeatures()).thenReturn(mockFeaturesReply);
        Mockito.when(mockConnectionContext.getNodeId()).thenReturn(testNodeId);
        Mockito.when(mockFeaturesReply.getVersion()).thenReturn(testVersion);
        Mockito.when(mockDeviceContext.getMessageSpy()).thenReturn(mockMessageSpy);
        Mockito.when(mockRequestContextStack.<RoleRequestOutput>createRequestContext()).thenReturn(mockRequestContext);
        Mockito.when(mockRequestContext.getXid()).thenReturn(new Xid(testXid));
        Mockito.when(mockConnectionContext.getOutboundQueueProvider()).thenReturn(mockOutboundQueue);
        Mockito.when(mockDeviceContext.getPrimaryConnectionContext().getConnectionState())
                .thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);

        NodeKey key = new NodeKey(testNodeId);
        InstanceIdentifier<Node> path = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, key)
                .build();
        nodeRef = new NodeRef(path);

    }

    @Test
    public void testSetRole() throws Exception {
        RoleRequestOutput roleRequestOutput = new RoleRequestOutputBuilder()
                .setXid(testXid).setGenerationId(Uint64.ONE).build();
        ListenableFuture<RpcResult<RoleRequestOutput>> futureOutput =
                RpcResultBuilder.<RoleRequestOutput>success().withResult(roleRequestOutput).buildFuture();

        Mockito.when(mockRequestContext.getFuture()).thenReturn(futureOutput);


        SalRoleService salRoleService = new SalRoleServiceImpl(mockRequestContextStack, mockDeviceContext);

        SetRoleInput setRoleInput = new SetRoleInputBuilder()
                .setControllerRole(OfpRole.BECOMESLAVE)
                .setNode(nodeRef)
                .build();

        Future<RpcResult<SetRoleOutput>> future = salRoleService.setRole(setRoleInput);

        RpcResult<SetRoleOutput> roleOutputRpcResult = future.get(5, TimeUnit.SECONDS);
        assertNotNull("RpcResult from future cannot be null.", roleOutputRpcResult);
        assertTrue("RpcResult from future is not successful.", roleOutputRpcResult.isSuccessful());

        SetRoleOutput setRoleOutput = roleOutputRpcResult.getResult();
        assertNotNull(setRoleOutput);
        assertEquals(Uint64.valueOf(testXid), setRoleOutput.getTransactionId().getValue());

    }

    @Test
    public void testDuplicateRoles() throws Exception {
        // set role to slave

        RoleRequestOutput roleRequestOutput = new RoleRequestOutputBuilder()
                .setXid(testXid).setGenerationId(Uint64.ONE).build();
        ListenableFuture<RpcResult<RoleRequestOutput>> futureOutput =
                RpcResultBuilder.<RoleRequestOutput>success().withResult(roleRequestOutput).buildFuture();

        Mockito.when(mockRequestContext.getFuture()).thenReturn(futureOutput);

        SalRoleService salRoleService = new SalRoleServiceImpl(mockRequestContextStack, mockDeviceContext);

        SetRoleInput setRoleInput = new SetRoleInputBuilder()
                .setControllerRole(OfpRole.BECOMESLAVE)
                .setNode(nodeRef)
                .build();

        Future<RpcResult<SetRoleOutput>> future = salRoleService.setRole(setRoleInput);

        RpcResult<SetRoleOutput> roleOutputRpcResult = future.get(5, TimeUnit.SECONDS);
        assertNotNull("RpcResult from future cannot be null.", roleOutputRpcResult);
        assertTrue("RpcResult from future is not successful.", roleOutputRpcResult.isSuccessful());

        SetRoleOutput setRoleOutput = roleOutputRpcResult.getResult();
        assertNotNull(setRoleOutput);
        assertEquals(Uint64.valueOf(testXid), setRoleOutput.getTransactionId().getValue());

        // make another role change with the same role - slave
        Future<RpcResult<SetRoleOutput>> future2 = salRoleService.setRole(setRoleInput);
        RpcResult<SetRoleOutput> roleOutputRpcResult2 = future2.get(5, TimeUnit.SECONDS);
        assertNotNull("RpcResult from future cannot be null.", roleOutputRpcResult2);
        assertTrue("RpcResult from future for duplicate role is not successful.", roleOutputRpcResult2.isSuccessful());

    }
}
