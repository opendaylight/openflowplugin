/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.impl.role.OpenflowOwnershipListener;
import org.opendaylight.openflowplugin.impl.role.RoleContextImpl;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
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
    private OpenflowOwnershipListener openflowOwnershipListener;

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

    private NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);

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

    }

    @Test
    public void testOnRoleChanged() {
        OfpRole newRole = OfpRole.BECOMEMASTER;

        SettableFuture<RpcResult<SetRoleOutput>> future = SettableFuture.create();
        future.set(RpcResultBuilder.<SetRoleOutput>success().build());
        when(salRoleService.setRole(Matchers.argThat(new SetRoleInputMatcher(newRole, instanceIdentifier))))
                .thenReturn(future);

        RoleContext roleContext = new RoleContextImpl(deviceContext, rpcProviderRegistry, entityOwnershipService, openflowOwnershipListener);

        roleContext.onRoleChanged(OfpRole.BECOMESLAVE, newRole);

        verify(deviceState).setRole(newRole);
    }


    private class SetRoleInputMatcher extends ArgumentMatcher<SetRoleInput> {

        private OfpRole ofpRole;
        private NodeRef nodeRef;
        public SetRoleInputMatcher(OfpRole ofpRole, KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
            this.ofpRole = ofpRole;
            nodeRef = new NodeRef(instanceIdentifier);

        }

        @Override
        public boolean matches(Object o) {
            SetRoleInput input = (SetRoleInput) o;
            if (input.getControllerRole() == ofpRole &&
                    input.getNode().equals(nodeRef)) {
                return true;
            }
            return false;
        }
    }
}
