/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
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
import org.opendaylight.openflowplugin.openflow.md.core.session.OFRoleManager;
import org.opendaylight.openflowplugin.openflow.md.core.session.RolePushException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.util.concurrent.Futures;

/**
 * testing {@link RoleUtil}
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleUtilTest {
    
    @Mock
    private SessionContext session;
    @Mock
    private GetFeaturesOutput features;
    @Mock
    private ConnectionConductor primaryConductor;
    @Mock
    private ConnectionAdapter connectionAdapter;
    
    private final BigInteger generationId = BigInteger.TEN;
    private RoleRequestOutput roleRequestOutput;

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
        Mockito.when(connectionAdapter.roleRequest(Matchers.any(RoleRequestInput.class)))
            .thenReturn(Futures.immediateFuture(RpcResultBuilder.success(roleRequestOutput).build()));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.util.RoleUtil#toOFJavaRole(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole)}.
     */
    @Test
    public void testToOFJavaRole() {
        Assert.assertEquals(ControllerRole.OFPCRROLEMASTER, RoleUtil.toOFJavaRole(OfpRole.BECOMEMASTER));
        Assert.assertEquals(ControllerRole.OFPCRROLESLAVE, RoleUtil.toOFJavaRole(OfpRole.BECOMESLAVE));
        Assert.assertEquals(ControllerRole.OFPCRROLENOCHANGE, RoleUtil.toOFJavaRole(OfpRole.NOCHANGE));
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.util.RoleUtil#toOFJavaRole(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole)}.
     */
    @Test(expected = NullPointerException.class)
    public void testToOFJavaRoleNull() {
        RoleUtil.toOFJavaRole(null);
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.util.RoleUtil#createRoleRequestInput(org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole, java.math.BigInteger)}.
     */
    @Test
    public void testCreateRuleRequestInput() {
        RoleRequestInput roleRequestInput = RoleUtil.createRoleRequestInput(session, OfpRole.BECOMEMASTER, generationId).build();
        Assert.assertEquals(generationId, roleRequestInput.getGenerationId());
        Assert.assertEquals(RoleRequestInput.class, roleRequestInput.getImplementedInterface());
        Assert.assertEquals(ControllerRole.OFPCRROLEMASTER, roleRequestInput.getRole());
        Assert.assertEquals(42, roleRequestInput.getVersion().intValue());
        Assert.assertEquals(84L, roleRequestInput.getXid().longValue());
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.util.RoleUtil#sendRoleChangeRequest(org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.OfpRole, java.math.BigInteger)}.
     * @throws Exception 
     */
    @Test
    public void testSendRoleChangeRequest() throws Exception {
        Future<RpcResult<RoleRequestOutput>> roleRequestOutputFx = RoleUtil.sendRoleChangeRequest(session, OfpRole.BECOMEMASTER, generationId);
        Assert.assertNotNull(roleRequestOutputFx);
        
        ArgumentCaptor<RoleRequestInput> roleRequestCaptor = ArgumentCaptor.forClass(RoleRequestInput.class);
        Mockito.verify(connectionAdapter).roleRequest(roleRequestCaptor.capture());
     
        RoleRequest roleRequestInput = roleRequestCaptor.getValue();
        Assert.assertEquals(generationId, roleRequestInput.getGenerationId());
        Assert.assertEquals(RoleRequestInput.class, roleRequestInput.getImplementedInterface());
        Assert.assertEquals(ControllerRole.OFPCRROLEMASTER, roleRequestInput.getRole());
        Assert.assertEquals(42, roleRequestInput.getVersion().intValue());
        Assert.assertEquals(84L, roleRequestInput.getXid().longValue());
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.util.RoleUtil#readGenerationIdFromDevice(org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext)}.
     * @throws Exception 
     */
    @Test
    public void testReadGenerationIdFromDevice() throws Exception {
        BigInteger generationIdFromDevice = RoleUtil.readGenerationIdFromDevice(session).get();
        Assert.assertEquals(generationId, generationIdFromDevice);
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.util.RoleUtil#getNextGenerationId(java.math.BigInteger)}.
     */
    @Test
    public void testGetNextGenerationId() {
        BigInteger[] src = new BigInteger[] {
                BigInteger.ZERO,
                BigInteger.ONE,
                OFRoleManager.MAX_GENERATION_ID.subtract(BigInteger.ONE),
                OFRoleManager.MAX_GENERATION_ID
        };
        
        BigInteger[] out = new BigInteger[] {
                BigInteger.ONE,
                BigInteger.valueOf(2L),
                OFRoleManager.MAX_GENERATION_ID,
                BigInteger.ZERO
        };
        
        for (int i = 0; i < src.length; i++) {
            BigInteger nextGenerationId = RoleUtil.getNextGenerationId(src[i]);
            Assert.assertEquals(out[i], nextGenerationId);
        }
    }
    
    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.util.RoleUtil#makeCheckedRuleRequestFxResult(com.google.common.util.concurrent.ListenableFuture)}.
     * @throws Exception 
     */
    @Test
    public void testMakeCheckedRuleRequestFxResult() throws Exception {
        String message = "me sooo naughty!";
        try {
            RoleUtil.makeCheckedRuleRequestFxResult(Futures.<Boolean>immediateFailedFuture(new Exception(message))).checkedGet();
        } catch (Exception e) {
            Assert.assertEquals(RolePushException.class, e.getClass());
            Assert.assertEquals(ExecutionException.class, e.getCause().getClass());
            Assert.assertEquals(Exception.class, e.getCause().getCause().getClass());
            Assert.assertNull(e.getCause().getCause().getCause());
            Assert.assertEquals(message, e.getCause().getCause().getMessage());
        }
        
        try {
            RoleUtil.makeCheckedRuleRequestFxResult(Futures.<Boolean>immediateFailedFuture(new RolePushException(message))).checkedGet();
        } catch (Exception e) {
            Assert.assertEquals(RolePushException.class, e.getClass());
            Assert.assertNull(e.getCause());
            Assert.assertEquals(message, e.getMessage());
        }
        
    }
}
