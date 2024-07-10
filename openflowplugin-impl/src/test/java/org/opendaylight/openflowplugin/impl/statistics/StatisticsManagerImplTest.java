/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ReconciliationFrameworkRegistrar;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint32Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsWorkMode;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsManagerImplTest {
    public static final NodeId NODE_ID = new NodeId("ofp-unit-dummy-node-id");

    @Mock
    private ConnectionContext mockedPrimConnectionContext;
    @Mock
    private FeaturesReply mockedFeatures;
    @Mock
    private ConnectionAdapter mockedConnectionAdapter;
    @Mock
    private MessageSpy mockedMessagSpy;
    @Mock
    private DeviceContext mockedDeviceContext;
    @Mock
    private DeviceState mockedDeviceState;
    @Mock
    private DeviceInfo mockedDeviceInfo;
    @Mock
    private RpcProviderService rpcProviderRegistry;
    @Mock
    private OutboundQueue outboundQueue;
    @Mock
    private MultiMsgCollector multiMagCollector;
    @Mock
    private Registration serviceControlRegistration;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private ReconciliationFrameworkRegistrar reconciliationFrameworkRegistrar;
    @Mock
    private StatisticsContext statisticContext;

    private RequestContext<List<MultipartReply>> currentRequestContext;
    private StatisticsManagerImpl statisticsManager;

    @Before
    public void initialization() {
        final KeyedInstanceIdentifier<Node, NodeKey> nodePath = KeyedInstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("openflow:10")));

        when(rpcProviderRegistry.registerRpcImplementations(any(Rpc[].class))).thenReturn(serviceControlRegistration);

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();

        statisticsManager = new StatisticsManagerImpl(
                new OpenflowProviderConfigBuilder()
                        .setBasicTimerDelay(new NonZeroUint32Type(Uint32.valueOf(3000)))
                        .setMaximumTimerDelay(new NonZeroUint32Type(Uint32.valueOf(900000)))
                        .setIsStatisticsPollingOn(false)
                        .build(), rpcProviderRegistry,
                convertorManager,
                MoreExecutors.directExecutor());
    }

    @Test
    public void testGetStatisticsWorkMode() throws Exception {
        final var workMode = statisticsManager.getStatisticsWorkMode(null);
        assertTrue(workMode.isDone());
        assertTrue(workMode.get().isSuccessful());
        assertNotNull(workMode.get().getResult());
        assertEquals(StatisticsWorkMode.COLLECTALL, workMode.get().getResult().getMode());
    }

    /**
     * switching to {@link StatisticsWorkMode#FULLYDISABLED}; no pollTimeout and no lifecycleRegistry.
     *
     */
    @Test
    public void testChangeStatisticsWorkMode1() throws Exception {
        statisticsManager.contexts.put(deviceInfo, statisticContext);

        final ChangeStatisticsWorkModeInputBuilder changeStatisticsWorkModeInputBld =
                new ChangeStatisticsWorkModeInputBuilder()
                        .setMode(StatisticsWorkMode.FULLYDISABLED);

        final ListenableFuture<RpcResult<ChangeStatisticsWorkModeOutput>> workMode = statisticsManager
                .changeStatisticsWorkMode(changeStatisticsWorkModeInputBld.build());

        checkWorkModeChangeOutcome(workMode);
        verify(statisticContext).disableGathering();
    }

    private static void checkWorkModeChangeOutcome(
            final ListenableFuture<RpcResult<ChangeStatisticsWorkModeOutput>> workMode)
            throws InterruptedException, ExecutionException {
        assertTrue(workMode.isDone());
        assertTrue(workMode.get().isSuccessful());
    }


    /**
     * Switching to {@link StatisticsWorkMode#FULLYDISABLED}; with pollTimeout and lifecycleRegistry.
     *
     */
    @Test
    public void testChangeStatisticsWorkMode2() throws Exception {
        statisticsManager.contexts.put(deviceInfo, statisticContext);

        final ChangeStatisticsWorkModeInputBuilder changeStatisticsWorkModeInputBld =
                new ChangeStatisticsWorkModeInputBuilder()
                        .setMode(StatisticsWorkMode.FULLYDISABLED);

        ListenableFuture<RpcResult<ChangeStatisticsWorkModeOutput>> workMode = statisticsManager
            .changeStatisticsWorkMode(changeStatisticsWorkModeInputBld.build());
        checkWorkModeChangeOutcome(workMode);

        verify(statisticContext).disableGathering();
    }

    /**
     * Tests switching to {@link StatisticsWorkMode#FULLYDISABLED} and back
     * to {@link StatisticsWorkMode#COLLECTALL}; with lifecycleRegistry and pollTimeout.
     *
     */
    @Test
    public void testChangeStatisticsWorkMode3() throws Exception {
        statisticsManager.contexts.put(deviceInfo, statisticContext);

        final ChangeStatisticsWorkModeInputBuilder changeStatisticsWorkModeInputBld =
                new ChangeStatisticsWorkModeInputBuilder()
                        .setMode(StatisticsWorkMode.FULLYDISABLED);

        ListenableFuture<RpcResult<ChangeStatisticsWorkModeOutput>> workMode;
        workMode = statisticsManager.changeStatisticsWorkMode(
                changeStatisticsWorkModeInputBld.build());
        checkWorkModeChangeOutcome(workMode);

        verify(statisticContext).disableGathering();

        changeStatisticsWorkModeInputBld.setMode(StatisticsWorkMode.COLLECTALL);
        workMode = statisticsManager.changeStatisticsWorkMode(
                changeStatisticsWorkModeInputBld.build());
        checkWorkModeChangeOutcome(workMode);

        verify(statisticContext).enableGathering();
    }

    @Test
    public void testClose() {
        statisticsManager.close();
        verify(serviceControlRegistration).close();
    }
}
