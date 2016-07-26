/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandler;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;

@RunWith(MockitoJUnitRunner.class)
public class DeviceManagerImplTest {

    private static final long TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA = 2000l;
    private static final int barrierCountLimit = 25600;
    private static final int barrierIntervalNanos = 500;
    public static final NodeId DUMMY_NODE_ID = new NodeId("dummyNodeId");

    @Mock
    CheckedFuture<Void, TransactionCommitFailedException> mockedFuture;
    @Mock
    private FeaturesReply mockFeatures;
    @Mock
    private OutboundQueue outboundQueueProvider;
    @Mock
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    @Mock
    private DeviceTerminationPhaseHandler deviceTerminationPhaseHandler;
    @Mock
    private TranslatorLibrary translatorLibrary;
    @Mock
    private ConnectionContext mockConnectionContext;
    @Mock
    private ConnectionAdapter mockedConnectionAdapter;
    @Mock
    private DeviceContextImpl mockedDeviceContext;
    @Mock
    private NodeId mockedNodeId;
    @Mock
    private MessageIntelligenceAgency messageIntelligenceAgency;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private LifecycleService lifecycleService;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;

    @Before
    public void setUp() throws Exception {
        OpenflowPortsUtil.init();

        when(mockConnectionContext.getNodeId()).thenReturn(DUMMY_NODE_ID);
        when(mockConnectionContext.getFeatures()).thenReturn(mockFeatures);
        when(mockConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockConnectionContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockConnectionContext);
        when(deviceInfo.getNodeId()).thenReturn(DUMMY_NODE_ID);

        final Capabilities capabilitiesV13 = mock(Capabilities.class);
        final CapabilitiesV10 capabilitiesV10 = mock(CapabilitiesV10.class);
        when(mockFeatures.getCapabilities()).thenReturn(capabilitiesV13);
        when(mockFeatures.getCapabilitiesV10()).thenReturn(capabilitiesV10);
        when(mockFeatures.getDatapathId()).thenReturn(BigInteger.valueOf(21L));
    }

    @Test
    public void onDeviceContextLevelUpSuccessTest() throws Exception {
        onDeviceContextLevelUp(false);
    }

    private DeviceManagerImpl prepareDeviceManager() {
        final DataBroker mockedDataBroker = mock(DataBroker.class);
        final WriteTransaction mockedWriteTransaction = mock(WriteTransaction.class);

        final BindingTransactionChain mockedTxChain = mock(BindingTransactionChain.class);
        final WriteTransaction mockedWTx = mock(WriteTransaction.class);
        when(mockedTxChain.newWriteOnlyTransaction()).thenReturn(mockedWTx);
        when(mockedDataBroker.createTransactionChain(any(TransactionChainListener.class))).thenReturn
                (mockedTxChain);
        when(mockedDataBroker.newWriteOnlyTransaction()).thenReturn(mockedWriteTransaction);

        when(mockedWriteTransaction.submit()).thenReturn(mockedFuture);

        final DeviceManagerImpl deviceManager = new DeviceManagerImpl(
                mockedDataBroker,
                TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA,
                false,
                barrierIntervalNanos,
                barrierCountLimit,
                messageIntelligenceAgency,
                true,
                clusterSingletonServiceProvider,
                null,
                new HashedWheelTimer());

        deviceManager.setDeviceInitializationPhaseHandler(deviceInitPhaseHandler);
        deviceManager.setDeviceTerminationPhaseHandler(deviceTerminationPhaseHandler);

        return deviceManager;
    }

    public void onDeviceContextLevelUp(final boolean withException) throws Exception {
        final DeviceManagerImpl deviceManager = prepareDeviceManager();
        final DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        if (withException) {
            doThrow(new IllegalStateException("dummy")).when(mockedDeviceContext).initialSubmitTransaction();
        }
        deviceManager.addDeviceContextToMap(deviceInfo, mockedDeviceContext);
        deviceManager.onDeviceContextLevelUp(deviceInfo, lifecycleService);
        if (withException) {
            verify(mockedDeviceContext).close();
        } else {
            verify(mockedDeviceContext).onPublished();
        }
    }

    @Test
    public void deviceConnectedTest() throws Exception{
        final DeviceManagerImpl deviceManager = prepareDeviceManager();
        injectMockTranslatorLibrary(deviceManager);
        final ConnectionContext mockConnectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);

        deviceManager.deviceConnected(mockConnectionContext);

        final InOrder order = inOrder(mockConnectionContext);
        order.verify(mockConnectionContext).setOutboundQueueProvider(any(OutboundQueueProvider.class));
        order.verify(mockConnectionContext).setOutboundQueueHandleRegistration(
                Mockito.<OutboundQueueHandlerRegistration<OutboundQueueProvider>>any());
        verify(deviceInitPhaseHandler).onDeviceContextLevelUp(Matchers.<DeviceInfo>any(), Mockito.<LifecycleService>any());
    }

    @Test
    public void deviceConnectedV10Test() throws Exception{
        final DeviceManagerImpl deviceManager = prepareDeviceManager();
        injectMockTranslatorLibrary(deviceManager);
        final ConnectionContext mockConnectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_0);

        final PhyPortBuilder phyPort = new PhyPortBuilder()
                .setPortNo(41L);
        when(mockFeatures.getPhyPort()).thenReturn(Collections.singletonList(phyPort.build()));
        final MessageTranslator<Object, Object> mockedTranslator = mock(MessageTranslator.class);
        when(mockedTranslator.translate(Matchers.<Object>any(), Matchers.<DeviceInfo>any(), Matchers.any()))
                .thenReturn(null);
        when(translatorLibrary.lookupTranslator(Matchers.<TranslatorKey>any())).thenReturn(mockedTranslator);

        deviceManager.deviceConnected(mockConnectionContext);

        final InOrder order = inOrder(mockConnectionContext);
        order.verify(mockConnectionContext).setOutboundQueueProvider(any(OutboundQueueProvider.class));
        order.verify(mockConnectionContext).setOutboundQueueHandleRegistration(
                Mockito.<OutboundQueueHandlerRegistration<OutboundQueueProvider>>any());
        verify(deviceInitPhaseHandler).onDeviceContextLevelUp(Matchers.<DeviceInfo>any(), Mockito.<LifecycleService>any());
    }

    @Test
    public void deviceDisconnectedTest() throws Exception {
        final DeviceState deviceState = mock(DeviceState.class);

        final DeviceManagerImpl deviceManager = prepareDeviceManager();
        injectMockTranslatorLibrary(deviceManager);

        final ConnectionContext connectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);
        when(connectionContext.getNodeId()).thenReturn(mockedNodeId);

        final DeviceContext deviceContext = mock(DeviceContext.class);
        when(deviceContext.shuttingDownDataStoreTransactions()).thenReturn(Futures.immediateCheckedFuture(null));
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        when(deviceContext.getDeviceState()).thenReturn(deviceState);

        final ConcurrentHashMap<DeviceInfo, DeviceContext> deviceContexts = getContextsCollection(deviceManager);
        deviceContexts.put(deviceInfo, deviceContext);

        deviceManager.onDeviceDisconnected(connectionContext);
    }

    protected ConnectionContext buildMockConnectionContext(final short ofpVersion) {
        when(mockFeatures.getVersion()).thenReturn(ofpVersion);
        when(outboundQueueProvider.reserveEntry()).thenReturn(43L);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final FutureCallback<OfHeader> callBack = (FutureCallback<OfHeader>) invocation.getArguments()[2];
                callBack.onSuccess(null);
                return null;
            }
        })
                .when(outboundQueueProvider)
                .commitEntry(Matchers.anyLong(), Matchers.<MultipartRequestInput>any(), Matchers.<FutureCallback<OfHeader>>any());

        when(mockedConnectionAdapter.registerOutboundQueueHandler(Matchers.<OutboundQueueHandler>any(), Matchers.anyInt(), Matchers.anyLong()))
                .thenAnswer(new Answer<OutboundQueueHandlerRegistration<OutboundQueueHandler>>() {
                    @Override
                    public OutboundQueueHandlerRegistration<OutboundQueueHandler> answer(final InvocationOnMock invocation) throws Throwable {
                        final OutboundQueueHandler handler = (OutboundQueueHandler) invocation.getArguments()[0];
                        handler.onConnectionQueueChanged(outboundQueueProvider);
                        return null;
                    }
                });

        when(mockConnectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        return mockConnectionContext;
    }

    private void injectMockTranslatorLibrary(final DeviceManagerImpl deviceManager) {
        deviceManager.setTranslatorLibrary(translatorLibrary);
    }

    @Test
    public void testClose() throws Exception {
        final DeviceContext deviceContext = mock(DeviceContext.class);
        final DeviceManagerImpl deviceManager = prepareDeviceManager();
        final ConcurrentHashMap<DeviceInfo, DeviceContext> deviceContexts = getContextsCollection(deviceManager);
        deviceContexts.put(deviceInfo, deviceContext);
        Assert.assertEquals(1, deviceContexts.size());

        deviceManager.close();

        verify(deviceContext).shutdownConnection();
        verify(deviceContext, Mockito.never()).close();
    }

    private static ConcurrentHashMap<DeviceInfo, DeviceContext> getContextsCollection(final DeviceManagerImpl deviceManager) throws NoSuchFieldException, IllegalAccessException {
        // HACK: contexts collection for testing shall be accessed in some more civilized way
        final Field contextsField = DeviceManagerImpl.class.getDeclaredField("deviceContexts");
        Assert.assertNotNull(contextsField);
        contextsField.setAccessible(true);
        return (ConcurrentHashMap<DeviceInfo, DeviceContext>) contextsField.get(deviceManager);
    }

}
