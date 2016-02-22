/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutputBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Created by kramesha on 9/1/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleContextImplTest {

    public static final int FUTURE_SAFETY_TIMEOUT = 5;
    @Mock
    private EntityOwnershipService entityOwnershipService;

    @Mock
    private DataBroker dataBroker;

    @Mock
    private RpcProviderRegistry rpcProviderRegistry;

    @Mock
    private DeviceContext deviceContext;

    @Mock
    private ConnectionContext connectionContext;

    @Mock
    private DeviceState deviceState;

    @Mock
    private SalRoleService salRoleService;

    @Mock
    private GetFeaturesOutput getFeaturesOutput;

    @Mock
    private FeaturesReply featuresReply;
    @Mock
    private MessageSpy mockedMessageSpy;

    private final NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
    private final Entity entity = new Entity(RoleManager.ENTITY_TYPE, nodeId.getValue());
    private final Entity txEntity = new Entity(RoleManager.TX_ENTITY_TYPE, nodeId.getValue());
    private RoleContextImpl roleContext;

    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceContext.getMessageSpy()).thenReturn(mockedMessageSpy);
        when(connectionContext.getNodeId()).thenReturn(nodeId);
        when(deviceState.getNodeInstanceIdentifier()).thenReturn(instanceIdentifier);
        when(deviceState.getNodeId()).thenReturn(nodeId);
        when(rpcProviderRegistry.getRpcService(SalRoleService.class)).thenReturn(salRoleService);
        when(deviceState.getFeatures()).thenReturn(getFeaturesOutput);
        when(getFeaturesOutput.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0);
        when(deviceContext.getPrimaryConnectionContext().getFeatures()).thenReturn(featuresReply);
        when(deviceContext.getPrimaryConnectionContext().getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        when(deviceContext.onClusterRoleChange(Matchers.<OfpRole>any(), Matchers.<OfpRole>any()))
                .thenReturn(Futures.immediateFuture((Void) null));

        roleContext = new RoleContextImpl(deviceContext, entityOwnershipService, entity, txEntity);
        roleContext.initialization();
    }

    @Test
    public void testOnRoleChangedStartingMaster() throws InterruptedException, ExecutionException, TimeoutException {
        final OfpRole oldRole = OfpRole.BECOMESLAVE;
        final OfpRole newRole = OfpRole.BECOMEMASTER;

        final SettableFuture<RpcResult<SetRoleOutput>> future = SettableFuture.create();
        future.set(RpcResultBuilder.<SetRoleOutput>success().build());
        when(salRoleService.setRole(Matchers.argThat(new SetRoleInputMatcher(newRole, instanceIdentifier))))
                .thenReturn(future);

        roleContext.setSalRoleService(salRoleService);

        final ListenableFuture<Void> onRoleChanged = roleContext.onRoleChanged(oldRole, newRole);
        onRoleChanged.get(FUTURE_SAFETY_TIMEOUT, TimeUnit.SECONDS);

        verify(deviceContext).onClusterRoleChange(oldRole, newRole);
    }

    @Test
    public void testOnRoleChangedStartingSlave() throws InterruptedException, ExecutionException, TimeoutException {
        final OfpRole oldRole = OfpRole.BECOMEMASTER;
        final OfpRole newRole = OfpRole.BECOMESLAVE;

        final SettableFuture<RpcResult<SetRoleOutput>> future = SettableFuture.create();
        future.set(RpcResultBuilder.<SetRoleOutput>success().build());
        when(salRoleService.setRole(Matchers.argThat(new SetRoleInputMatcher(newRole, instanceIdentifier))))
                .thenReturn(future);

        roleContext.setSalRoleService(salRoleService);

        final ListenableFuture<Void> onRoleChanged = roleContext.onRoleChanged(oldRole, newRole);
        onRoleChanged.get(5, TimeUnit.SECONDS);

        verify(deviceContext, Mockito.never()).onClusterRoleChange(oldRole, newRole);
    }

    @Test
    public void testOnRoleChangedWorkingMaster() throws InterruptedException, ExecutionException, TimeoutException {
        final OfpRole oldRole = OfpRole.BECOMESLAVE;
        final OfpRole newRole = OfpRole.BECOMEMASTER;

        final ListenableFuture<RpcResult<SetRoleOutput>> future =
                RpcResultBuilder.success(new SetRoleOutputBuilder().build()).buildFuture();
        when(salRoleService.setRole(Matchers.argThat(new SetRoleInputMatcher(newRole, instanceIdentifier))))
                .thenReturn(future);

        roleContext.setSalRoleService(salRoleService);

        final ListenableFuture<Void> onRoleChanged = roleContext.onRoleChanged(oldRole, newRole);
        onRoleChanged.get(5, TimeUnit.SECONDS);

        verify(deviceContext).onClusterRoleChange(oldRole, newRole);
    }

    @Test
    public void testOnRoleChangedWorkingSlave() throws InterruptedException, ExecutionException, TimeoutException {
        final OfpRole oldRole = OfpRole.BECOMEMASTER;
        final OfpRole newRole = OfpRole.BECOMESLAVE;

        final SettableFuture<RpcResult<SetRoleOutput>> future = SettableFuture.create();
        future.set(RpcResultBuilder.<SetRoleOutput>success().build());
        when(salRoleService.setRole(Matchers.argThat(new SetRoleInputMatcher(newRole, instanceIdentifier))))
                .thenReturn(future);

        roleContext.setSalRoleService(salRoleService);
        roleContext.promoteStateToWorking();

        final ListenableFuture<Void> onRoleChanged = roleContext.onRoleChanged(oldRole, newRole);
        onRoleChanged.get(5, TimeUnit.SECONDS);

        verify(deviceContext).onClusterRoleChange(oldRole, newRole);
    }

    private class SetRoleInputMatcher extends ArgumentMatcher<SetRoleInput> {

        private final OfpRole ofpRole;
        private final NodeRef nodeRef;

        public SetRoleInputMatcher(final OfpRole ofpRole, final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
            this.ofpRole = ofpRole;
            nodeRef = new NodeRef(instanceIdentifier);

        }

        @Override
        public boolean matches(final Object o) {
            final SetRoleInput input = (SetRoleInput) o;
            if (input.getControllerRole() == ofpRole &&
                    input.getNode().equals(nodeRef)) {
                return true;
            }
            return false;
        }
    }
}
