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
import org.opendaylight.openflowplugin.api.openflow.lifecycle.RoleChangeListener;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.role.RoleManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jozef Bacigal
 * Date: 4/19/16
 * Time: 12:56
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleContextImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(RoleContextImpl.class);

    @Mock
    private EntityOwnershipService entityOwnershipService;

    @Mock
    private EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration;

    private final NodeId nodeId = NodeId.getDefaultInstance("openflow:1");
    private final Entity entity = new Entity(RoleManager.ENTITY_TYPE, nodeId.getValue());
    private final Entity txEntity = new Entity(RoleManager.TX_ENTITY_TYPE, nodeId.getValue());
    private RoleContext roleContext;

    @Before
    public void setup() throws CandidateAlreadyRegisteredException {
        roleContext = new RoleContextBuilder(nodeId, entityOwnershipService, entity, txEntity).build();
        Mockito.when(entityOwnershipService.registerCandidate(entity)).thenReturn(entityOwnershipCandidateRegistration);
        Mockito.when(entityOwnershipService.registerCandidate(txEntity)).thenReturn(entityOwnershipCandidateRegistration);
    }

//  @Test
//  Run this test only if demanded because it takes 15s to run
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
        roleContext.termination();
        Assert.assertFalse(roleContext.isMainCandidateRegistered());
        Assert.assertFalse(roleContext.isTxCandidateRegistered());
    }

    @Test
    public void testCreateRequestContext() throws Exception {

    }

    @Test(expected = NullPointerException.class)
    public void testSetSalRoleService() throws Exception {
        roleContext.setSalRoleService(null);
    }

    @Test
    public void testAddListener() throws Exception {
        roleContext.addListener(new RoleChangeListener() {
            @Override
            public void roleInitializationDone(final NodeId nodeId, final boolean success) {
                Assert.assertTrue(nodeId.equals(roleContext.getNodeId()));
                Assert.assertTrue(success);
            }

            @Override
            public void roleCloseContext(final NodeId nodeId) {
                Assert.assertTrue(nodeId.equals(roleContext.getNodeId()));
            }

            @Override
            public void roleChangeOnDevice(final NodeId nodeId, final boolean success, final OfpRole newRole, final boolean initializationPhase) {
                Assert.assertTrue(nodeId.equals(roleContext.getNodeId()));
                Assert.assertTrue(success);
                Assert.assertFalse(initializationPhase);
                Assert.assertTrue(newRole.equals(OfpRole.BECOMEMASTER));
            }
        });
        roleContext.notifyListenersRoleInitializationDone(true);
        roleContext.notifyListenersRoleChangeOnDevice(true, OfpRole.BECOMEMASTER, false);
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
        Assert.assertTrue(roleContext.getNodeId().equals(nodeId));
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
