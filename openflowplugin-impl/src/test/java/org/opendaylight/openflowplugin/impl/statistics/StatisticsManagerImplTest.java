package org.opendaylight.openflowplugin.impl.statistics;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.registry.ItemLifeCycleRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsManagerControlService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsWorkMode;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(MockitoJUnitRunner.class)
public class StatisticsManagerImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImplTest.class);

    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("444");
    private static final Short DUMMY_VERSION = OFConstants.OFP_VERSION_1_3;

    @Mock
    RequestContextStack mockedRequestContextStack;
    @Mock
    ConnectionContext mockedPrimConnectionContext;
    @Mock
    FeaturesReply mockedFeatures;
    @Mock
    ConnectionAdapter mockedConnectionAdapter;
    @Mock
    MessageSpy mockedMessagSpy;
    @Mock
    DeviceContext mockedDeviceContext;
    @Mock
    DeviceState mockedDeviceState;
    @Mock
    DeviceInitializationPhaseHandler mockedDevicePhaseHandler;
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

    private RequestContext<List<MultipartReply>> currentRequestContext;
    private StatisticsManagerImpl statisticsManager;

    @Before
    public void initialization() {
        when(mockedFeatures.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeatures.getVersion()).thenReturn(DUMMY_VERSION);

        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedPrimConnectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        when(mockedPrimConnectionContext.getNodeId()).thenReturn(new NodeId("ut-node:123"));
        when(mockedPrimConnectionContext.getOutboundQueueProvider()).thenReturn(outboundQueue);

        when(mockedDeviceState.isFlowStatisticsAvailable()).thenReturn(true);
        when(mockedDeviceState.isGroupAvailable()).thenReturn(true);
        when(mockedDeviceState.isMetersAvailable()).thenReturn(true);
        when(mockedDeviceState.isPortStatisticsAvailable()).thenReturn(true);
        when(mockedDeviceState.isQueueStatisticsAvailable()).thenReturn(true);
        when(mockedDeviceState.isTableStatisticsAvailable()).thenReturn(true);

        when(mockedDeviceState.getNodeId()).thenReturn(new NodeId("ofp-unit-dummy-node-id"));

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl());
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getTimer()).thenReturn(hashedWheelTimer);
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

        statisticsManager = new StatisticsManagerImpl(rpcProviderRegistry);
    }

    @Test
    public void testOnDeviceContextLevelUp() throws Exception {
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final FutureCallback<OfHeader> callback = (FutureCallback<OfHeader>) invocation.getArguments()[2];
                LOG.debug("committing entry: {}", ((MultipartRequestInput) invocation.getArguments()[1]).getType());
                callback.onSuccess(null);
                currentRequestContext.setResult(RpcResultBuilder.<List<MultipartReply>>success().build());
                return null;
            }
        }).when(outboundQueue)
                .commitEntry(Matchers.anyLong(), Matchers.<OfHeader>any(), Matchers.<FutureCallback<OfHeader>>any());

        statisticsManager.setDeviceInitializationPhaseHandler(mockedDevicePhaseHandler);
        statisticsManager.onDeviceContextLevelUp(mockedDeviceContext);

        verify(mockedDeviceContext).addDeviceContextClosedHandler(statisticsManager);
        verify(mockedDeviceContext, Mockito.times(8)).getReservedXid();
        verify(mockedDeviceState).setDeviceSynchronized(true);
        verify(mockedDevicePhaseHandler).onDeviceContextLevelUp(mockedDeviceContext);
        verify(hashedWheelTimer).newTimeout(Matchers.<TimerTask>any(), Matchers.anyLong(), Matchers.<TimeUnit>any());
    }

    @Test
    public void testOnDeviceContextLevelUp1() throws Exception {
        statisticsManager = new StatisticsManagerImpl(rpcProviderRegistry, true);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final FutureCallback<OfHeader> callback = (FutureCallback<OfHeader>) invocation.getArguments()[2];
                LOG.debug("committing entry: {}", ((MultipartRequestInput) invocation.getArguments()[1]).getType());
                callback.onSuccess(null);
                currentRequestContext.setResult(RpcResultBuilder.<List<MultipartReply>>success().build());
                return null;
            }
        }).when(outboundQueue)
                .commitEntry(Matchers.anyLong(), Matchers.<OfHeader>any(), Matchers.<FutureCallback<OfHeader>>any());

        statisticsManager.setDeviceInitializationPhaseHandler(mockedDevicePhaseHandler);
        statisticsManager.onDeviceContextLevelUp(mockedDeviceContext);

        verify(mockedDeviceContext).addDeviceContextClosedHandler(statisticsManager);
        verify(mockedDeviceContext, Mockito.times(8)).getReservedXid();
        verify(mockedDeviceState).setDeviceSynchronized(true);
        verify(mockedDevicePhaseHandler).onDeviceContextLevelUp(mockedDeviceContext);
        verify(hashedWheelTimer, Mockito.never()).newTimeout(Matchers.<TimerTask>any(), Matchers.anyLong(), Matchers.<TimeUnit>any());
    }

    @Test
    public void testOnDeviceContextClosed() throws Exception {
        StatisticsContext statisticContext = Mockito.mock(StatisticsContext.class);
        final Map<DeviceContext, StatisticsContext> contextsMap = getContextsMap(statisticsManager);

        contextsMap.put(mockedDeviceContext, statisticContext);
        Assert.assertEquals(1, contextsMap.size());

        statisticsManager.onDeviceContextClosed(mockedDeviceContext);
        verify(statisticContext).close();
        Assert.assertEquals(0, contextsMap.size());
    }

    private static Map<DeviceContext, StatisticsContext> getContextsMap(StatisticsManagerImpl statisticsManager) throws NoSuchFieldException, IllegalAccessException {
        // HACK: contexts map for testing shall be accessed in some more civilized way
        final Field contextsField = StatisticsManagerImpl.class.getDeclaredField("contexts");
        Assert.assertNotNull(contextsField);
        contextsField.setAccessible(true);
        return (Map<DeviceContext, StatisticsContext>) contextsField.get(statisticsManager);
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
        StatisticsContext statisticContext = Mockito.mock(StatisticsContext.class);
        when(statisticContext.getPollTimeout()).thenReturn(
                Optional.<Timeout>absent());
        when(itemLifeCycleRegistry.getLifeCycleSources()).thenReturn(
                Collections.<ItemLifeCycleSource>emptyList());

        getContextsMap(statisticsManager).put(mockedDeviceContext, statisticContext);

        final ChangeStatisticsWorkModeInputBuilder changeStatisticsWorkModeInputBld =
                new ChangeStatisticsWorkModeInputBuilder()
                        .setMode(StatisticsWorkMode.FULLYDISABLED);

        Future<RpcResult<Void>> workMode = statisticsManager
                .changeStatisticsWorkMode(changeStatisticsWorkModeInputBld.build());

        checkWorkModeChangeOutcome(workMode);
        Mockito.verify(itemLifeCycleRegistry).getLifeCycleSources();
        Mockito.verify(statisticContext).getPollTimeout();
    }

    private static void checkWorkModeChangeOutcome(Future<RpcResult<Void>> workMode) throws InterruptedException, java.util.concurrent.ExecutionException {
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
        Timeout pollTimeout = Mockito.mock(Timeout.class);
        ItemLifeCycleSource itemLifecycleSource = Mockito.mock(ItemLifeCycleSource.class);
        StatisticsContext statisticContext = Mockito.mock(StatisticsContext.class);
        when(statisticContext.getPollTimeout()).thenReturn(
                Optional.of(pollTimeout));
        when(itemLifeCycleRegistry.getLifeCycleSources()).thenReturn(
                Collections.singletonList(itemLifecycleSource));

        getContextsMap(statisticsManager).put(mockedDeviceContext, statisticContext);

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
        Timeout pollTimeout = Mockito.mock(Timeout.class);
        ItemLifeCycleSource itemLifecycleSource = Mockito.mock(ItemLifeCycleSource.class);
        Mockito.doNothing().when(itemLifecycleSource)
                .setItemLifecycleListener(itemLifeCycleListenerCapt.capture());

        StatisticsContext statisticContext = Mockito.mock(StatisticsContext.class);
        when(statisticContext.getPollTimeout()).thenReturn(
                Optional.of(pollTimeout));
        when(statisticContext.getItemLifeCycleListener()).thenReturn(
                Mockito.mock(ItemLifecycleListener.class));
        when(itemLifeCycleRegistry.getLifeCycleSources()).thenReturn(
                Collections.singletonList(itemLifecycleSource));

        getContextsMap(statisticsManager).put(mockedDeviceContext, statisticContext);

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
        TimeCounter timeCounter = Mockito.mock(TimeCounter.class);
        when(timeCounter.getAverageTimeBetweenMarks()).thenReturn(2000L, 4000L);

        statisticsManager.calculateTimerDelay(timeCounter);
        Assert.assertEquals(3000L, StatisticsManagerImpl.getCurrentTimerDelay());
        statisticsManager.calculateTimerDelay(timeCounter);
        Assert.assertEquals(6000L, StatisticsManagerImpl.getCurrentTimerDelay());
    }
}