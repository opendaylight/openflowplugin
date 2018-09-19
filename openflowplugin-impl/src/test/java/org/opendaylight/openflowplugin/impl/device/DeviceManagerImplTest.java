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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
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
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class DeviceManagerImplTest {

    private static final long TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA = 2000L;
    private static final int BARRIER_COUNT_LIMIT = 25600;
    private static final long BARRIER_INTERVAL_NANOS = 500;
    private static final NodeId DUMMY_NODE_ID = new NodeId("dummyNodeId");
    private static final KeyedInstanceIdentifier<Node, NodeKey> DUMMY_IDENTIFIER  = DeviceStateUtil
            .createNodeInstanceIdentifier(DUMMY_NODE_ID);

    @Mock
    private CheckedFuture<Void, TransactionCommitFailedException> mockedFuture;
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
    private BindingTransactionChain transactionChain;
    @Mock
    private Capabilities capabilities;
    @Mock
    private CapabilitiesV10 capabilitiesV10;
    @Mock
    private NotificationPublishService notificationPublishService;
    @Mock
    private TranslatorLibrary translatorLibrary;

    private DeviceManagerImpl deviceManager;

    @Before
    public void setUp() throws Exception {
        when(mockConnectionContext.getFeatures()).thenReturn(mockFeatures);
        when(mockConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockConnectionContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(DUMMY_IDENTIFIER);
        when(deviceInfo.getNodeId()).thenReturn(DUMMY_NODE_ID);

        when(mockedFuture.isDone()).thenReturn(true);
        when(writeTransaction.submit()).thenReturn(mockedFuture);
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
                        .build(),
                dataBroker,
                messageIntelligenceAgency,
                notificationPublishService,
                new HashedWheelTimer(),
                convertorExecutor,
                DeviceInitializerProviderFactory.createDefaultProvider());

        deviceManager.setTranslatorLibrary(translatorLibrary);
        verify(dataBroker).newWriteOnlyTransaction();
        verify(writeTransaction).merge(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
        verify(writeTransaction).submit();
    }

    @Test
    public void createContext() throws Exception {
        final DeviceContext context = deviceManager.createContext(mockConnectionContext);
        assertEquals(deviceInfo, context.getDeviceInfo());

    }

    @Test
    public void removeDeviceFromOperationalDS() throws Exception {
        final ListenableFuture<Void> future = deviceManager
                .removeDeviceFromOperationalDS(DUMMY_IDENTIFIER);

        future.get();
        assertTrue(future.isDone());
        verify(writeTransaction).delete(LogicalDatastoreType.OPERATIONAL, DUMMY_IDENTIFIER);
    }

    @Test(expected = ExecutionException.class)
    public void removeDeviceFromOperationalDSException() throws Exception {
        final CheckedFuture<Void, TransactionCommitFailedException> failedFuture =
                Futures.immediateFailedCheckedFuture(
                        new TransactionCommitFailedException("Test failed transaction"));
        Mockito.when(writeTransaction.submit()).thenReturn(failedFuture);
        final ListenableFuture<Void> future = deviceManager
                .removeDeviceFromOperationalDS(DUMMY_IDENTIFIER);
        future.get();
        assertTrue(future.isDone());
        verify(writeTransaction).delete(LogicalDatastoreType.OPERATIONAL, DUMMY_IDENTIFIER);
    }

    @Test
    public void sendNodeAddedNotification() throws Exception {
        deviceManager.sendNodeAddedNotification(DUMMY_IDENTIFIER);
        deviceManager.sendNodeAddedNotification(DUMMY_IDENTIFIER);
        verify(notificationPublishService).offerNotification(new NodeUpdatedBuilder()
                .setId(DUMMY_NODE_ID)
                .setNodeRef(new NodeRef(DUMMY_IDENTIFIER))
                .build());
    }

    @Test
    public void sendNodeRemovedNotification() throws Exception {
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
