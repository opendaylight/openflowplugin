/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.role;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class RoleContextImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);

    @Mock
    private EntityOwnershipService entityOwnershipService;

    @Mock
    private EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration;

    @Mock
    private LifecycleConductor conductor;

    @Mock
    private DeviceInfo deviceInfo;

    private final NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private final Entity entity = new Entity(RoleManager.ENTITY_TYPE, nodeId.getValue());
    private final Entity txEntity = new Entity(RoleManager.TX_ENTITY_TYPE, nodeId.getValue());
    private RoleContext roleContext;

    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        roleContext = new RoleContextImpl(deviceInfo, entityOwnershipService, entity, txEntity, conductor);
        Mockito.when(entityOwnershipService.registerCandidate(entity)).thenReturn(entityOwnershipCandidateRegistration);
        Mockito.when(entityOwnershipService.registerCandidate(txEntity)).thenReturn(entityOwnershipCandidateRegistration);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
    }

    //@Test
    //Run this test only if demanded because it takes 15s to run
    public void testInitializationThreads() throws Exception {

        /*Setting answer which will hold the answer for 5s*/
        Mockito.when(entityOwnershipService.registerCandidate(entity)).thenAnswer(new Answer<EntityOwnershipService>() {
            @Override
            public EntityOwnershipService answer(final InvocationOnMock invocationOnMock) throws Throwable {
                LOG.info("Sleeping this thread for 14s");
                Thread.sleep(14000L);
                return null;
            }
        });

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Starting thread 1");
                Assert.assertTrue(roleContext.initialization());
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Starting thread 2");
                Assert.assertFalse(roleContext.initialization());
            }
        });

        t1.start();
        LOG.info("Sleeping main thread for 1s to prevent race condition.");
        Thread.sleep(1000L);
        t2.start();

        while (t2.isAlive()) {
            //Waiting
        }

    }

    @Test
    public void testTermination() throws Exception {
        roleContext.registerCandidate(entity);
        roleContext.registerCandidate(txEntity);
        Assert.assertTrue(roleContext.isMainCandidateRegistered());
        Assert.assertTrue(roleContext.isTxCandidateRegistered());
        roleContext.unregisterAllCandidates();
        Assert.assertFalse(roleContext.isMainCandidateRegistered());
    }

    @Test
    public void testCreateRequestContext() throws Exception {
        roleContext.createRequestContext();
        Mockito.verify(conductor).reserveXidForDeviceMessage(nodeId);
    }

    @Test(expected = NullPointerException.class)
    public void testSetSalRoleService() throws Exception {
        roleContext.setSalRoleService(null);
    }

    @Test
    public void testGetEntity() throws Exception {
        Assert.assertTrue(roleContext.getEntity().equals(entity));
    }

    @Test
    public void testGetTxEntity() throws Exception {
        Assert.assertTrue(roleContext.getTxEntity().equals(txEntity));
    }

    @Test
    public void testGetNodeId() throws Exception {
        Assert.assertTrue(roleContext.getDeviceInfo().getNodeId().equals(nodeId));
    }

    @Test
    public void testIsMaster() throws Exception {
        Assert.assertTrue(roleContext.initialization());
        Assert.assertFalse(roleContext.isMaster());
        Assert.assertTrue(roleContext.registerCandidate(txEntity));
        Assert.assertTrue(roleContext.isMaster());
        Assert.assertTrue(roleContext.unregisterCandidate(entity));
        Assert.assertFalse(roleContext.isMaster());
    }
}
