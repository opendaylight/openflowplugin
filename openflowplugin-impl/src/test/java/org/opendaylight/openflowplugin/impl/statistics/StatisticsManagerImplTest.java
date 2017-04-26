/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.registry.ItemLifeCycleRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsManagerControlService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsWorkMode;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(MockitoJUnitRunner.class)
public class StatisticsManagerImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImplTest.class);

    public static final NodeId NODE_ID = new NodeId("ofp-unit-dummy-node-id");

    @Mock
    RequestContextStack mockedRequestContextStack;
    @Mock
    ConnectionContext mockedPrimConnectionContext;
    @Mock
    FeaturesReply mockedFeatures;
    @Mock
    GetFeaturesOutput mockedFeaturesOutput;
    @Mock
    ConnectionAdapter mockedConnectionAdapter;
    @Mock
    MessageSpy mockedMessagSpy;
    @Mock
    DeviceContext mockedDeviceContext;
    @Mock
    DeviceState mockedDeviceState;
    @Mock
    DeviceInfo mockedDeviceInfo;
    @Mock
    DeviceInitializationPhaseHandler mockedDevicePhaseHandler;
    @Mock
    DeviceTerminationPhaseHandler mockedTerminationPhaseHandler;
    @Mock
    private RpcProviderRegistry rpcProviderRegistry;
    @Mock
    private HashedWheelTimer hashedWheelTimer;
    @Mock
    private OutboundQueue outboundQueue;
    @Mock
    private MultiMsgCollector multiMagCollector;
    @Mock
    private ItemLifeCycleRegistry itemLifeCycleRegistry;
    @Captor
    private ArgumentCaptor<ItemLifecycleListener> itemLifeCycleListenerCapt;
    @Mock
    private BindingAwareBroker.RpcRegistration<StatisticsManagerControlService> serviceControlRegistration;
    @Mock
    private DeviceManager deviceManager;
    @Mock
    private GetFeaturesOutput featuresOutput;
    @Mock
    private DeviceInitializationPhaseHandler deviceInitializationPhaseHandler;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private LifecycleService lifecycleService;

    private RequestContext<List<MultipartReply>> currentRequestContext;
    private StatisticsManagerImpl statisticsManager;


    @Before
    public void initialization() {
        final KeyedInstanceIdentifier<Node, NodeKey> nodePath = KeyedInstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("openflow:10")));

        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedPrimConnectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        when(mockedPrimConnectionContext.getNodeId()).thenReturn(NODE_ID);
        when(mockedPrimConnectionContext.getOutboundQueueProvider()).thenReturn(outboundQueue);

        when(mockedDeviceState.isFlowStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isGroupAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isMetersAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isPortStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isQueueStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isTableStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(nodePath);
        when(mockedDeviceInfo.getDatapathId()).thenReturn(BigInteger.TEN);
        when(mockedDeviceInfo.getNodeId()).thenReturn(NODE_ID);

        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl(OFConstants.OFP_VERSION_1_3, dataBroker, nodePath));
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getMultiMsgCollector(
                Matchers.<RequestContext<List<MultipartReply>>>any())).thenAnswer(
                new Answer<MultiMsgCollector>() {
                    @Override
                    public MultiMsgCollector answer(InvocationOnMock invocation) throws Throwable {
                        currentRequestContext = (RequestContext<List<MultipartReply>>) invocation.getArguments()[0];
                        return multiMagCollector;
                    }
                }
        );
        when(mockedDeviceContext.getItemLifeCycleSourceRegistry()).thenReturn(itemLifeCycleRegistry);
        when(rpcProviderRegistry.addRpcImplementation(
                Matchers.eq(StatisticsManagerControlService.class),
                Matchers.<StatisticsManagerControlService>any())).thenReturn(serviceControlRegistration);

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        final long basicTimerDelay = 3000L;
        final long maximumTimerDelay = 900000L;
        statisticsManager = new StatisticsManagerImpl(rpcProviderRegistry, new HashedWheelTimer(),
                convertorManager);
        statisticsManager.setBasicTimerDelay(basicTimerDelay);
        statisticsManager.setMaximumTimerDelay(maximumTimerDelay);
        statisticsManager.setIsStatisticsPollingOn(false);
    }

    private static Map<DeviceInfo, StatisticsContext> getContextsMap(final StatisticsManagerImpl statisticsManager)
            throws NoSuchFieldException, IllegalAccessException {
        // HACK: contexts map for testing shall be accessed in some more civilized way
        final Field contextsField = StatisticsManagerImpl.class.getDeclaredField("contexts");
        Assert.assertNotNull(contextsField);
        contextsField.setAccessible(true);
        return (Map<DeviceInfo, StatisticsContext>) contextsField.get(statisticsManager);
    }

    @Test
    public void testGetStatisticsWorkMode() throws Exception {
        final Future<RpcResult<GetStatisticsWorkModeOutput>> workMode = statisticsManager.getStatisticsWorkMode();
        Assert.assertTrue(workMode.isDone());
        Assert.assertTrue(workMode.get().isSuccessful());
        Assert.assertNotNull(workMode.get().getResult());
        Assert.assertEquals(StatisticsWorkMode.COLLECTALL, workMode.get().getResult().getMode());
    }

    /**
     * switching to {@link StatisticsWorkMode#FULLYDISABLED}; no pollTimeout and no lifecycleRegistry
     *
     * @throws Exception
     */
    @Test
    public void testChangeStatisticsWorkMode1() throws Exception {
        final StatisticsContext statisticContext = Mockito.mock(StatisticsContext.class);
        when(statisticContext.getPollTimeout()).thenReturn(
                Optional.<Timeout>empty());
        when(itemLifeCycleRegistry.getLifeCycleSources()).thenReturn(
                Collections.<ItemLifeCycleSource>emptyList());

        when(statisticContext.gainDeviceContext()).thenReturn(mockedDeviceContext);
        when(statisticContext.gainDeviceState()).thenReturn(mockedDeviceState);
        when(lifecycleService.getDeviceContext()).thenReturn(mockedDeviceContext);

        getContextsMap(statisticsManager).put(deviceInfo, statisticContext);

        final ChangeStatisticsWorkModeInputBuilder changeStatisticsWorkModeInputBld =
                new ChangeStatisticsWorkModeInputBuilder()
                        .setMode(StatisticsWorkMode.FULLYDISABLED);

        final Future<RpcResult<Void>> workMode = statisticsManager
                .changeStatisticsWorkMode(changeStatisticsWorkModeInputBld.build());

        checkWorkModeChangeOutcome(workMode);
        Mockito.verify(itemLifeCycleRegistry).getLifeCycleSources();
        Mockito.verify(statisticContext).getPollTimeout();
    }

    private static void checkWorkModeChangeOutcome(Future<RpcResult<Void>> workMode) throws InterruptedException, ExecutionException {
        Assert.assertTrue(workMode.isDone());
        Assert.assertTrue(workMode.get().isSuccessful());
    }


    /**
     * switching to {@link StatisticsWorkMode#FULLYDISABLED}; with pollTimeout and lifecycleRegistry
     *
     * @throws Exception
     */
    @Test
    public void testChangeStatisticsWorkMode2() throws Exception {
        final Timeout pollTimeout = Mockito.mock(Timeout.class);
        final ItemLifeCycleSource itemLifecycleSource = Mockito.mock(ItemLifeCycleSource.class);
        final StatisticsContext statisticContext = Mockito.mock(StatisticsContext.class);
        when(statisticContext.getPollTimeout()).thenReturn(
                Optional.of(pollTimeout));
        when(itemLifeCycleRegistry.getLifeCycleSources()).thenReturn(
                Collections.singletonList(itemLifecycleSource));

        getContextsMap(statisticsManager).put(deviceInfo, statisticContext);

        when(statisticContext.gainDeviceContext()).thenReturn(mockedDeviceContext);
        when(statisticContext.gainDeviceState()).thenReturn(mockedDeviceState);
        when(lifecycleService.getDeviceContext()).thenReturn(mockedDeviceContext);

        final ChangeStatisticsWorkModeInputBuilder changeStatisticsWorkModeInputBld =
                new ChangeStatisticsWorkModeInputBuilder()
                        .setMode(StatisticsWorkMode.FULLYDISABLED);

        Future<RpcResult<Void>> workMode = statisticsManager.changeStatisticsWorkMode(changeStatisticsWorkModeInputBld.build());
        checkWorkModeChangeOutcome(workMode);

        Mockito.verify(itemLifeCycleRegistry).getLifeCycleSources();
        Mockito.verify(statisticContext).getPollTimeout();
        Mockito.verify(pollTimeout).cancel();
        Mockito.verify(itemLifecycleSource).setItemLifecycleListener(Matchers.<ItemLifecycleListener>any());
    }

    /**
     * switching to {@link StatisticsWorkMode#FULLYDISABLED} and back
     * to {@link StatisticsWorkMode#COLLECTALL}; with lifecycleRegistry and pollTimeout
     *
     * @throws Exception
     */
    @Test
    public void testChangeStatisticsWorkMode3() throws Exception {
        final Timeout pollTimeout = Mockito.mock(Timeout.class);
        final ItemLifeCycleSource itemLifecycleSource = Mockito.mock(ItemLifeCycleSource.class);
        Mockito.doNothing().when(itemLifecycleSource)
                .setItemLifecycleListener(itemLifeCycleListenerCapt.capture());

        final StatisticsContext statisticContext = Mockito.mock(StatisticsContext.class);
        when(statisticContext.getPollTimeout()).thenReturn(
                Optional.of(pollTimeout));
        when(statisticContext.getItemLifeCycleListener()).thenReturn(
                Mockito.mock(ItemLifecycleListener.class));
        when(itemLifeCycleRegistry.getLifeCycleSources()).thenReturn(
                Collections.singletonList(itemLifecycleSource));

        getContextsMap(statisticsManager).put(deviceInfo, statisticContext);

        when(statisticContext.gainDeviceContext()).thenReturn(mockedDeviceContext);
        when(statisticContext.gainDeviceState()).thenReturn(mockedDeviceState);
        when(lifecycleService.getDeviceContext()).thenReturn(mockedDeviceContext);

        final ChangeStatisticsWorkModeInputBuilder changeStatisticsWorkModeInputBld =
                new ChangeStatisticsWorkModeInputBuilder()
                        .setMode(StatisticsWorkMode.FULLYDISABLED);

        Future<RpcResult<Void>> workMode;
        workMode = statisticsManager.changeStatisticsWorkMode(
                changeStatisticsWorkModeInputBld.build());
        checkWorkModeChangeOutcome(workMode);

        changeStatisticsWorkModeInputBld.setMode(StatisticsWorkMode.COLLECTALL);
        workMode = statisticsManager.changeStatisticsWorkMode(
                changeStatisticsWorkModeInputBld.build());
        checkWorkModeChangeOutcome(workMode);

        Mockito.verify(itemLifeCycleRegistry, Mockito.times(2)).getLifeCycleSources();
        Mockito.verify(statisticContext).getPollTimeout();
        Mockito.verify(pollTimeout).cancel();

        final List<ItemLifecycleListener> itemLifeCycleListenerValues = itemLifeCycleListenerCapt.getAllValues();
        Assert.assertEquals(2, itemLifeCycleListenerValues.size());
        Assert.assertNotNull(itemLifeCycleListenerValues.get(0));
        Assert.assertNull(itemLifeCycleListenerValues.get(1));
    }

    @Test
    public void testClose() throws Exception {
        statisticsManager.close();
        Mockito.verify(serviceControlRegistration).close();
    }

    @Test
    public void testCalculateTimerDelay() throws Exception {
        final TimeCounter timeCounter = Mockito.mock(TimeCounter.class);
        when(timeCounter.getAverageTimeBetweenMarks()).thenReturn(2000L, (Long)4000L);

        statisticsManager.calculateTimerDelay(timeCounter);
        Assert.assertEquals(3000L, statisticsManager.getCurrentTimerDelay());
        statisticsManager.calculateTimerDelay(timeCounter);
        Assert.assertEquals(6000L, statisticsManager.getCurrentTimerDelay());
    }

    @Test
    public void testPollStatistics() throws Exception {
        final StatisticsContext statisticsContext = Mockito.mock(StatisticsContext.class);
        final TimeCounter mockTimerCounter = Mockito.mock(TimeCounter.class);

        statisticsManager.pollStatistics(mockedDeviceContext.getDeviceState(), statisticsContext, mockTimerCounter, mockedDeviceInfo);
        verify(mockedDeviceContext).getDeviceState();

        statisticsManager.pollStatistics(mockedDeviceContext.getDeviceState(), statisticsContext, mockTimerCounter, mockedDeviceInfo);

        statisticsManager.pollStatistics(mockedDeviceContext.getDeviceState(), statisticsContext, mockTimerCounter, mockedDeviceInfo);

        when(statisticsContext.gatherDynamicData()).thenReturn(Futures.immediateCheckedFuture(Boolean.TRUE));
        when(statisticsContext.isSchedulingEnabled()).thenReturn(Boolean.TRUE);
        statisticsManager.pollStatistics(mockedDeviceContext.getDeviceState(), statisticsContext, mockTimerCounter, mockedDeviceInfo);
        Mockito.verify(mockTimerCounter).markStart();
        Mockito.verify(mockTimerCounter).addTimeMark();

        when(statisticsContext.gatherDynamicData()).thenReturn(Futures.immediateFailedFuture(new Throwable("error msg")));
        statisticsManager.pollStatistics(mockedDeviceContext.getDeviceState(), statisticsContext, mockTimerCounter, mockedDeviceInfo);
        Mockito.verify(mockTimerCounter,times(2)).addTimeMark();
    }
}
