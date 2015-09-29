/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.math.BigInteger;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationEnqueuer;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContextOFImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.openflowplugin.openflow.md.core.role.OfEntityManager;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/26/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalRegistrationManagerTest {


    private static final BigInteger dataPathId = BigInteger.ONE;

    private SalRegistrationManager salRegistrationManager;

    private SessionContextOFImpl context;
    @Mock
    private ConnectionConductor conductor;
    @Mock
    private IMessageDispatchService messageDispatchService;
    @Mock
    private GetFeaturesOutput features;
    @Mock
    private BindingAwareBroker.ProviderContext providerContext;
    @Mock
    private NotificationEnqueuer notificationEnqueuer;
    @Mock
    private ListeningExecutorService rpcPool;
    @Mock
    private NotificationProviderService notificationProviderService;
    @Mock
    private RpcProviderRegistry rpcProviderRegistry;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private EntityOwnershipService entityOwnershipService;

    private ModelDrivenSwitch mdSwitchOF13;

    CompositeObjectRegistration<ModelDrivenSwitch> registration;


    @Before
    public void setUp() {
        OFSessionUtil.getSessionManager().setRpcPool(rpcPool);
        Mockito.when(conductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0)
                .thenReturn(OFConstants.OFP_VERSION_1_3);
        context = new SessionContextOFImpl();
        context.setPrimaryConductor(conductor);
        Mockito.when(features.getDatapathId()).thenReturn(BigInteger.valueOf(1));
        Mockito.when(features.getVersion()).thenReturn((short) 1);
        context.setFeatures(features);
        context.setNotificationEnqueuer(notificationEnqueuer);

	OfEntityManager entManager = new OfEntityManager(entityOwnershipService);
        mdSwitchOF13 = new ModelDrivenSwitchImpl(null, null, context);
        registration = new CompositeObjectRegistration<>(mdSwitchOF13, Collections.<Registration>emptyList());
        context.setProviderRegistration(registration);

        UpdateFlowOutputBuilder updateFlowOutput = new UpdateFlowOutputBuilder();
        RpcResult<UpdateFlowOutput> result = RpcResultBuilder.success(updateFlowOutput.build()).build();

        Mockito.when(
                messageDispatchService.flowMod(Matchers.any(FlowModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        salRegistrationManager = new SalRegistrationManager();
        salRegistrationManager.setPublishService(notificationProviderService);
        salRegistrationManager.setDataService(dataBroker);
        salRegistrationManager.setRpcProviderRegistry(rpcProviderRegistry);
        salRegistrationManager.setOfEntityManager(entManager);

        salRegistrationManager.init();

    }

    /**
     * free sesion manager
     */
    @After
    public void tearDown() {
        OFSessionUtil.releaseSessionManager();
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#identifierFromDatapathId(java.math.BigInteger)}
     */
    @Test
    public void testIdentifierFromDatapathId() {
        InstanceIdentifier<Node> node = SalRegistrationManager.identifierFromDatapathId(dataPathId);
        assertNotNull(node);
        assertEquals("NodeKey [_id=Uri [_value=openflow:1]]", ((KeyedInstanceIdentifier<?, ?>) node).getKey().toString());
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#nodeKeyFromDatapathId(java.math.BigInteger)}
     */
    @Test
    public void testNodeKeyFromDatapathId() {
        NodeKey nodeKey = SalRegistrationManager.nodeKeyFromDatapathId(dataPathId);
        assertNotNull(nodeKey);
        assertEquals("openflow:1", nodeKey.getId().getValue());
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#nodeIdFromDatapathId(java.math.BigInteger)}
     */
    @Test
    public void testNodeIdFromDatapathId() {
        NodeId nodeId = SalRegistrationManager.nodeIdFromDatapathId(dataPathId);
        assertNotNull(nodeId);
        assertEquals("openflow:1", nodeId.getValue());
    }

    /**
     * Test for {@link SalRegistrationManager#getSessionManager()}
     */
    @Test
    public void testGetSessionManager() {
        assertNotNull(salRegistrationManager.getPublishService());
    }


    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#onSessionRemoved(org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext)}
     */
    @Test
    public void testOnSessionRemoved() {
        assertNotNull(context.getProviderRegistration());
        salRegistrationManager.onSessionRemoved(context);
        assertNull(context.getProviderRegistration());
    }

    /**
     * Test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.SalRegistrationManager#onSessionAdded(org.opendaylight.openflowplugin.api.openflow.md.core.session.SwitchSessionKeyOF, org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext)}
     */
    public void testOnAdded() {
        SwitchSessionKeyOF switchSessionKeyOF = new SwitchSessionKeyOF();
        salRegistrationManager.onSessionAdded(switchSessionKeyOF, context);
    }
}

