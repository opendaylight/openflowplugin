/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProviderFactory;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint16Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint32Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfigBuilder;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class DeviceManagerImplTest {

    private static final Uint32 TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA = Uint32.valueOf(2000);
    private static final Uint16 BARRIER_COUNT_LIMIT = Uint16.valueOf(25600);
    private static final Uint32 BARRIER_INTERVAL_NANOS = Uint32.valueOf(500);
    private static final NodeId DUMMY_NODE_ID = new NodeId("dummyNodeId");
    private static final KeyedInstanceIdentifier<Node, NodeKey> DUMMY_IDENTIFIER  = DeviceStateUtil
            .createNodeInstanceIdentifier(DUMMY_NODE_ID);

    @Mock
    private FluentFuture<CommitInfo> mockedFuture;
    @Mock
    private FeaturesReply mockFeatures;
    @Mock
    private ConnectionContext mockConnectionContext;
    @Mock
    private ConnectionAdapter mockedConnectionAdapter;
    @Mock
    private DeviceContextImpl mockedDeviceContext;
    @Mock
    private MessageIntelligenceAgency messageIntelligenceAgency;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ConvertorExecutor convertorExecutor;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private WriteTransaction writeTransaction;
    @Mock
    private TransactionChain transactionChain;
    @Mock
    private Capabilities capabilities;
    @Mock
    private CapabilitiesV10 capabilitiesV10;
    @Mock
    private NotificationPublishService notificationPublishService;
    @Mock
    private TranslatorLibrary translatorLibrary;
    @Mock
    private ExecutorService executorService;

    private DeviceManagerImpl deviceManager;

    @Before
    public void setUp() {
        when(mockConnectionContext.getFeatures()).thenReturn(mockFeatures);
        when(mockConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockConnectionContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(DUMMY_IDENTIFIER);
        when(deviceInfo.getNodeId()).thenReturn(DUMMY_NODE_ID);

        when(mockedFuture.isDone()).thenReturn(true);
        doReturn(mockedFuture).when(writeTransaction).commit();
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);

        deviceManager = new DeviceManagerImpl(
                new OpenflowProviderConfigBuilder()
                        .setBarrierCountLimit(new NonZeroUint16Type(BARRIER_COUNT_LIMIT))
                        .setBarrierIntervalTimeoutLimit(new NonZeroUint32Type(BARRIER_INTERVAL_NANOS))
                        .setGlobalNotificationQuota(TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA)
                        .setSwitchFeaturesMandatory(false)
                        .setEnableFlowRemovedNotification(true)
                        .setSkipTableFeatures(false)
                        .setUseSingleLayerSerialization(true)
                        .setIsStatisticsPollingOn(false)
                        .build(),
                dataBroker,
                messageIntelligenceAgency,
                notificationPublishService,
                new HashedWheelTimer(),
                convertorExecutor,
                DeviceInitializerProviderFactory.createDefaultProvider(),
                executorService);

        deviceManager.setTranslatorLibrary(translatorLibrary);
        verify(dataBroker).newWriteOnlyTransaction();
        verify(writeTransaction).merge(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
        verify(writeTransaction).commit();
    }

    @Test
    public void createContext() {
        final DeviceContext context = deviceManager.createContext(mockConnectionContext);
        assertEquals(deviceInfo, context.getDeviceInfo());

    }

    @Test
    public void removeDeviceFromOperationalDS() throws Exception {
        final ListenableFuture<?> future = deviceManager
                .removeDeviceFromOperationalDS(DUMMY_IDENTIFIER);

        future.get();
        assertTrue(future.isDone());
        verify(writeTransaction).delete(LogicalDatastoreType.OPERATIONAL, DUMMY_IDENTIFIER);
    }

    @Test(expected = ExecutionException.class)
    public void removeDeviceFromOperationalDSException() throws Exception {
        final FluentFuture<?> failedFuture = FluentFutures.immediateFailedFluentFuture(
                        new ExecutionException(new Throwable("Test failed transaction")));
        Mockito.doReturn(failedFuture).when(writeTransaction).commit();
        final ListenableFuture<?> future = deviceManager.removeDeviceFromOperationalDS(DUMMY_IDENTIFIER);
        future.get();
        assertTrue(future.isDone());
        verify(writeTransaction).delete(LogicalDatastoreType.OPERATIONAL, DUMMY_IDENTIFIER);
    }

    @Test
    public void sendNodeAddedNotification() {
        deviceManager.sendNodeAddedNotification(DUMMY_IDENTIFIER);
        deviceManager.sendNodeAddedNotification(DUMMY_IDENTIFIER);
        verify(notificationPublishService).offerNotification(new NodeUpdatedBuilder()
                .setId(DUMMY_NODE_ID)
                .setNodeRef(new NodeRef(DUMMY_IDENTIFIER))
                .build());
    }

    @Test
    public void sendNodeRemovedNotification() {
        deviceManager.sendNodeAddedNotification(DUMMY_IDENTIFIER);
        deviceManager.sendNodeRemovedNotification(DUMMY_IDENTIFIER);
        deviceManager.sendNodeRemovedNotification(DUMMY_IDENTIFIER);
        verify(notificationPublishService).offerNotification(new NodeRemovedBuilder()
                .setNodeRef(new NodeRef(DUMMY_IDENTIFIER))
                .build());
    }

    @Test
    public void close() throws Exception {
        final DeviceContext deviceContext = mock(DeviceContext.class);
        final ConcurrentHashMap<DeviceInfo, DeviceContext> deviceContexts = getContextsCollection(deviceManager);
        deviceContexts.put(deviceInfo, deviceContext);
        Assert.assertEquals(1, deviceContexts.size());
        deviceManager.close();
        verify(deviceContext).close();
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentHashMap<DeviceInfo, DeviceContext> getContextsCollection(
            final DeviceManagerImpl deviceManager) throws NoSuchFieldException, IllegalAccessException {
        // HACK: contexts collection for testing shall be accessed in some more civilized way
        final Field contextsField = DeviceManagerImpl.class.getDeclaredField("deviceContexts");
        Assert.assertNotNull(contextsField);
        contextsField.setAccessible(true);
        return (ConcurrentHashMap<DeviceInfo, DeviceContext>) contextsField.get(deviceManager);
    }

}
