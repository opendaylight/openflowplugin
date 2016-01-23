/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
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
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Created by kramesha on 9/1/15.
 */
public class RoleContextImplTest {

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

    RoleContextImpl roleContext;

    private final NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
    private final Entity entity = new Entity(RoleManager.ENTITY_TYPE, nodeId.getValue());

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(connectionContext.getNodeId()).thenReturn(nodeId);
        when(deviceState.getNodeInstanceIdentifier()).thenReturn(instanceIdentifier);
        when(rpcProviderRegistry.getRpcService(SalRoleService.class)).thenReturn(salRoleService);
        when(deviceState.getFeatures()).thenReturn(getFeaturesOutput);
        when(getFeaturesOutput.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceContext.getPrimaryConnectionContext().getFeatures()).thenReturn(featuresReply);
        when(deviceContext.getPrimaryConnectionContext().getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        roleContext = new RoleContextImpl(deviceContext, entityOwnershipService, entity);
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(entityOwnershipService);
    }


    @Test
    public void testOnRoleChanged() throws Exception {
        roleChange();
    }


    private void roleChange() throws Exception {
        final OfpRole newRole = OfpRole.BECOMEMASTER;
        final SettableFuture<RpcResult<SetRoleOutput>> future = SettableFuture.create();
        future.set(RpcResultBuilder.<SetRoleOutput>success().build());
        when(salRoleService.setRole(Matchers.argThat(new SetRoleInputMatcher(newRole, instanceIdentifier))))
                .thenReturn(future);
        roleContext.setSalRoleService(salRoleService);
        roleContext.onRoleChanged(OfpRole.BECOMESLAVE, newRole);
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

    @Test
    public void testInitialization() throws Exception {
        roleContext.initialization();
        verify(entityOwnershipService).registerCandidate(eq(entity));
    }

    @Test
    public void testOnRoleChangeDeviceStateRIP() throws Exception {
        when(deviceContext.getPrimaryConnectionContext().getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.RIP);
        roleChange();
    }

    @Test
    public void testClose() throws Exception {
        roleContext.close();
    }
}
