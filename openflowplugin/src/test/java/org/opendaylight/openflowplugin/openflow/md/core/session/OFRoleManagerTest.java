/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class OFRoleManagerTest {

    @Mock
    private SessionManager sessionManager;
    @Mock
    private SessionContext session;
    @Mock
    private GetFeaturesOutput features;
    @Mock
    private ConnectionConductor primaryConductor;
    @Mock
    private ConnectionAdapter connectionAdapter;

    private OFRoleManager manager;
    private RoleRequestOutput roleRequestOutput;
    private BarrierOutput barrierOutput;
    private BigInteger generationId = BigInteger.TEN;

    /**
     * prepare values
     */
    @Before
    public void setUp() {
        Mockito.when(session.getFeatures()).thenReturn(features);
        Mockito.when(features.getVersion()).thenReturn(Short.valueOf((short) 42));
        Mockito.when(session.getNextXid()).thenReturn(84L);
        Mockito.when(session.getPrimaryConductor()).thenReturn(primaryConductor);
        Mockito.when(primaryConductor.getConnectionAdapter()).thenReturn(connectionAdapter);
        roleRequestOutput = new RoleRequestOutputBuilder()
                .setGenerationId(generationId)
                .setRole(ControllerRole.OFPCRROLESLAVE)
                .setVersion((short) 42)
                .setXid(21L)
                .build();
        barrierOutput = new BarrierOutputBuilder()
                .setVersion((short) 42)
                .setXid(1L)
                .build();

        manager = new OFRoleManager(sessionManager);
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.session.OFRoleManager#manageRoleChange(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole)}.
     * <br/>
     * session lot is empty is invalid
     */
    @Test
    public void testManageRoleChangeFail1() {
        manager.manageRoleChange(OfpRole.BECOMESLAVE);
        Mockito.verify(connectionAdapter, Mockito.never()).roleRequest(Matchers.any(RoleRequestInput.class));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.session.OFRoleManager#manageRoleChange(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole)}.
     * <br/>
     * available session is invalid
     */
    @Test
    public void testManageRoleChangeFail2() {
        Mockito.when(sessionManager.getAllSessions()).thenReturn(Collections.singleton(session));
        manager.manageRoleChange(OfpRole.BECOMESLAVE);
        Mockito.verify(connectionAdapter, Mockito.never()).roleRequest(Matchers.any(RoleRequestInput.class));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.session.OFRoleManager#manageRoleChange(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole)}.
     * <br/>
     * device bound to valid session is not answering
     */
    @Test
    public void testManageRoleChangeFail3() {
        Mockito.when(session.isValid()).thenReturn(true);
        Mockito.when(sessionManager.getAllSessions()).thenReturn(Collections.singleton(session));
//        manager.manageRoleChange(OfpRole.BECOMESLAVE);
//        Mockito.verify(connectionAdapter, Mockito.times(1)).roleRequest(Matchers.any(RoleRequestInput.class));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.session.OFRoleManager#manageRoleChange(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole)}.
     */
    @Test
    public void testManageRoleChangeOK() {
        Mockito.when(session.isValid()).thenReturn(true);
        Mockito.when(sessionManager.getAllSessions()).thenReturn(Collections.singleton(session));
        Mockito.when(connectionAdapter.roleRequest(Matchers.any(RoleRequestInput.class)))
                .thenReturn(Futures.immediateFuture(RpcResultBuilder.success(roleRequestOutput).build()));
        Mockito.when(connectionAdapter.barrier(Matchers.any(BarrierInput.class)))
                .thenReturn(Futures.immediateFuture(RpcResultBuilder.success(barrierOutput).build()));

        //manager.manageRoleChange(OfpRole.BECOMESLAVE);

        ArgumentCaptor<RoleRequestInput> roleRequestCaptor = ArgumentCaptor.forClass(RoleRequestInput.class);
        //Mockito.verify(connectionAdapter, Mockito.times(2)).roleRequest(roleRequestCaptor.capture());

//        List<RoleRequestInput> values = roleRequestCaptor.getAllValues();
//        Assert.assertEquals(ControllerRole.OFPCRROLENOCHANGE, values.get(0).getRole());
//        Assert.assertEquals(0L, values.get(0).getGenerationId().longValue());
//        Assert.assertEquals(ControllerRole.OFPCRROLESLAVE, values.get(1).getRole());
//        Assert.assertEquals(11L, values.get(1).getGenerationId().longValue());
    }
}
