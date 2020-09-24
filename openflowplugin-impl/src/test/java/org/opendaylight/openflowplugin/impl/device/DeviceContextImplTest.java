/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.device.initialization.AbstractDeviceInitializer;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.ExperimenterMessageFromDev;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SalRoleService;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.util.concurrent.NotificationManager;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class DeviceContextImplTest {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceContextImplTest.class);
    private static final Uint8 DUMMY_AUXILIARY_ID = Uint8.valueOf(33);
    private static final Uint64 DUMMY_COOKIE = Uint64.valueOf("33");
    private static final Uint32 DUMMY_XID = Uint32.valueOf(544L);
    private static final Long DUMMY_PORT_NUMBER = 159L;
    private static final Uint64 DUMMY_DATAPATH_ID = Uint64.valueOf("55");
    private Xid xid;
    private Xid xidMulti;

    private DeviceContext deviceContext;
    @Mock
    private RequestContext<GetAsyncReply> requestContext;
    @Mock
    private RequestContext<MultipartReply> requestContextMultiReply;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private GetFeaturesOutput featuresOutput;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private ReadWriteTransaction writeTx;
    @Mock
    private ReadTransaction readTx;
    @Mock
    private TransactionChain txChainFactory;
    @Mock
    private HashedWheelTimer timer;
    @Mock
    private OutboundQueueProvider outboundQueueProvider;
    @Mock
    private ConnectionAdapter connectionAdapter;
    private final NodeId nodeId = new NodeId("h2g2:42");
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeKeyIdent =
            DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
    @Mock
    private TranslatorLibrary translatorLibrary;
    @Mock
    MessageTranslator messageTranslatorPacketReceived;
    @Mock
    private MessageTranslator messageTranslatorFlowCapableNodeConnector;
    @Mock
    private MessageTranslator<Object, Object> messageTranslatorFlowRemoved;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ConvertorExecutor convertorExecutor;
    @Mock
    private MessageSpy messageSpy;
    @Mock
    private DeviceInitializerProvider deviceInitializerProvider;
    @Mock
    private AbstractDeviceInitializer abstractDeviceInitializer;
    @Mock
    private SalRoleService salRoleService;
    @Mock
    private ContextChainHolder contextChainHolder;
    @Mock
    private ContextChain contextChain;
    @Mock
    private NotificationManager queuedNotificationManager;

    private final AtomicLong atomicLong = new AtomicLong(0);

    private DeviceContext deviceContextSpy;

    @Before
    public void setUp() {
        final FluentFuture<Optional<Node>> noExistNodeFuture =
                FluentFutures.immediateFluentFuture(Optional.<Node>empty());
        Mockito.lenient().when(readTx.read(LogicalDatastoreType.OPERATIONAL, nodeKeyIdent))
                .thenReturn(noExistNodeFuture);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(readTx);
        Mockito.when(dataBroker.createTransactionChain(any(TransactionChainManager.class)))
                .thenReturn(txChainFactory);
        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeKeyIdent);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
        Mockito.when(deviceInfo.getDatapathId()).thenReturn(Uint64.ONE);
        final SettableFuture<RpcResult<GetAsyncReply>> settableFuture = SettableFuture.create();
        final SettableFuture<RpcResult<MultipartReply>> settableFutureMultiReply = SettableFuture.create();
        Mockito.lenient().when(requestContext.getFuture()).thenReturn(settableFuture);
        Mockito.lenient().doAnswer(invocation -> {
            settableFuture.set((RpcResult<GetAsyncReply>) invocation.getArguments()[0]);
            return null;
        }).when(requestContext).setResult(Mockito.<RpcResult>any());

        Mockito.lenient().when(requestContextMultiReply.getFuture()).thenReturn(settableFutureMultiReply);
        Mockito.lenient().doAnswer(invocation -> {
            settableFutureMultiReply.set((RpcResult<MultipartReply>) invocation.getArguments()[0]);
            return null;
        }).when(requestContextMultiReply).setResult(any(RpcResult.class));
        Mockito.when(txChainFactory.newReadWriteTransaction()).thenReturn(writeTx);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(readTx);
        Mockito.lenient().when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        Mockito.when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionContext.getDeviceInfo()).thenReturn(deviceInfo);
        final FeaturesReply mockedFeaturesReply = mock(FeaturesReply.class);
        lenient().when(connectionContext.getFeatures()).thenReturn(mockedFeaturesReply);
        lenient().when(connectionContext.getFeatures().getCapabilities()).thenReturn(mock(Capabilities.class));

        Mockito.lenient().when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.lenient().when(featuresOutput.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        Mockito.lenient().when(featuresOutput.getVersion()).thenReturn(Uint8.valueOf(OFConstants.OFP_VERSION_1_3));
        Mockito.when(contextChainHolder.getContextChain(deviceInfo)).thenReturn(contextChain);
        Mockito.when(contextChain.isMastered(ContextChainMastershipState.CHECK, false)).thenReturn(true);

        final PacketReceived packetReceived = new PacketReceivedBuilder()
                .setMatch(new org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received
                        .MatchBuilder()
                        .setInPort(new NodeConnectorId("openflow:1:LOCAL"))
                        .build())
                .build();

        Mockito.when(messageTranslatorPacketReceived.translate(Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn(packetReceived);
        lenient().when(messageTranslatorFlowCapableNodeConnector.translate(Mockito.any(),
                Mockito.any(),
                Mockito.any())).thenReturn(mock(FlowCapableNodeConnector.class));
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3,
                PacketIn.class.getName())))).thenReturn(messageTranslatorPacketReceived);
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3,
                PortGrouping.class.getName())))).thenReturn(messageTranslatorFlowCapableNodeConnector);
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3,
                FlowRemoved.class.getName())))).thenReturn(messageTranslatorFlowRemoved);

        final java.util.Optional<AbstractDeviceInitializer> deviceInitializer = java.util.Optional
                .of(this.abstractDeviceInitializer);

        Mockito.lenient().when(deviceInitializerProvider.lookup(OFConstants.OFP_VERSION_1_3))
                .thenReturn(deviceInitializer);
        Mockito.lenient().when(salRoleService.setRole(any())).thenReturn(Futures.immediateFuture(null));

        deviceContext = new DeviceContextImpl(
                connectionContext,
                dataBroker,
                messageSpy,
                translatorLibrary,
                convertorExecutor,
                false, timer, false,
                deviceInitializerProvider,
                true, false,
                contextChainHolder,
                queuedNotificationManager,
                false);
        ((DeviceContextImpl) deviceContext).lazyTransactionManagerInitialization();
        deviceContextSpy = Mockito.spy(deviceContext);

        xid = new Xid(Uint32.valueOf(atomicLong.incrementAndGet()));
        xidMulti = new Xid(Uint32.valueOf(atomicLong.incrementAndGet()));
        lenient().doNothing().when(deviceContextSpy).writeToTransaction(any(), any(), any());
    }

    @Test
    public void testGetReadTransaction() {
        readTx = deviceContext.getReadTransaction();
        assertNotNull(readTx);
        assertEquals(this.readTx, readTx);
    }

    @Test
    public void testInitialSubmitTransaction() {
        Mockito.doReturn(CommitInfo.emptyFluentFuture()).when(writeTx).commit();
        final InstanceIdentifier<Nodes> dummyII = InstanceIdentifier.create(Nodes.class);
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().activateTransactionManager() ;
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().initialSubmitWriteTransaction();
        deviceContext.addDeleteToTxChain(LogicalDatastoreType.CONFIGURATION, dummyII);
        deviceContext.initialSubmitTransaction();
        verify(writeTx).commit();
    }

    private static ConnectionContext prepareConnectionContext() {
        final ConnectionContext mockedConnectionContext = mock(ConnectionContext.class);
        final FeaturesReply mockedFeaturesReply = mock(FeaturesReply.class);
        when(mockedFeaturesReply.getAuxiliaryId()).thenReturn(DUMMY_AUXILIARY_ID);
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeaturesReply);
        return mockedConnectionContext;
    }

    @Test
    public void testAddDeleteToTxChain() {
        final InstanceIdentifier<Nodes> dummyII = InstanceIdentifier.create(Nodes.class);
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().activateTransactionManager() ;
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().initialSubmitWriteTransaction();
        deviceContext.addDeleteToTxChain(LogicalDatastoreType.CONFIGURATION, dummyII);
        verify(writeTx).delete(eq(LogicalDatastoreType.CONFIGURATION), eq(dummyII));
    }

    @Test
    public void testSubmitTransaction() {
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().activateTransactionManager() ;
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().initialSubmitWriteTransaction();
        assertTrue(deviceContext.submitTransaction());
    }

    @Test
    public void testGetPrimaryConnectionContext() {
        final ConnectionContext primaryConnectionContext = deviceContext.getPrimaryConnectionContext();
        assertEquals(connectionContext, primaryConnectionContext);
    }

    @Test
    public void testGetDeviceFlowRegistry() {
        final DeviceFlowRegistry deviceFlowRegistry = deviceContext.getDeviceFlowRegistry();
        assertNotNull(deviceFlowRegistry);
    }

    @Test
    public void testGetDeviceGroupRegistry() {
        final DeviceGroupRegistry deviceGroupRegistry = deviceContext.getDeviceGroupRegistry();
        assertNotNull(deviceGroupRegistry);
    }

    @Test
    public void testGetDeviceMeterRegistry() {
        final DeviceMeterRegistry deviceMeterRegistry = deviceContext.getDeviceMeterRegistry();
        assertNotNull(deviceMeterRegistry);
    }

    @Test
    public void testProcessReply() {
        final Error mockedError = mock(Error.class);
        deviceContext.processReply(mockedError);
        verify(messageSpy).spyMessage(Mockito.<Class>any(),
                eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_FAILURE));
        final OfHeader mockedOfHeader = mock(OfHeader.class);
        deviceContext.processReply(mockedOfHeader);
        verify(messageSpy).spyMessage(Mockito.<Class>any(),
                eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessReply2() {
        final Xid dummyXid = new Xid(DUMMY_XID);

        final Error mockedError = mock(Error.class);
        deviceContext.processReply(dummyXid, Lists.newArrayList(mockedError));
        verify(messageSpy).spyMessage(Mockito.<Class>any(),
                eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_FAILURE));

        final MultipartReply mockedMultipartReply = mock(MultipartReply.class);
        deviceContext.processReply(dummyXid, Lists.newArrayList(mockedMultipartReply));
        verify(messageSpy).spyMessage(Mockito.<Class>any(),
                eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessPacketInMessageFutureSuccess() {
        final PacketInMessage mockedPacketInMessage = mock(PacketInMessage.class);
        final NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        final ListenableFuture stringListenableFuture = Futures.immediateFuture("dummy value");

        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class)))
                .thenReturn(stringListenableFuture);
        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        deviceContext.processPacketInMessage(mockedPacketInMessage);
        verify(messageSpy).spyMessage(Mockito.<Class>any(), eq(MessageSpy.StatisticsGroup.FROM_SWITCH));
        verify(messageSpy).spyMessage(Mockito.<Class>any(),
                eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessPacketInMessageFutureFailure() {
        final PacketInMessage mockedPacketInMessage = mock(PacketInMessage.class);
        final NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        final ListenableFuture dummyFuture = Futures.immediateFailedFuture(new IllegalStateException());

        when(mockedNotificationPublishService.offerNotification(Mockito.<PacketReceived>any())).thenReturn(dummyFuture);
        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        deviceContext.processPacketInMessage(mockedPacketInMessage);
        verify(messageSpy).spyMessage(Mockito.<Class>any(),
                eq(MessageSpy.StatisticsGroup.FROM_SWITCH_NOTIFICATION_REJECTED));
    }

    @Test
    public void testTranslatorLibrary() {
        final TranslatorLibrary pickedTranslatorLibrary = deviceContext.oook();
        assertEquals(translatorLibrary, pickedTranslatorLibrary);
    }

    @Test
    public void testGetMessageSpy() {
        final MessageSpy pickedMessageSpy = deviceContext.getMessageSpy();
        assertEquals(messageSpy, pickedMessageSpy);
    }

    @Test
    public void testOnPublished() {
        final ConnectionAdapter mockedConnectionAdapter = mock(ConnectionAdapter.class);
        when(connectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);

        deviceContext.onPublished();
        verify(mockedConnectionAdapter).setPacketInFiltering(eq(false));
    }

    @Test
    public void testPortStatusMessage() {
        final PortStatusMessage mockedPortStatusMessage = mock(PortStatusMessage.class);
        final Class dummyClass = Class.class;
        when(mockedPortStatusMessage.implementedInterface()).thenReturn(dummyClass);

        final GetFeaturesOutput mockedFeature = mock(GetFeaturesOutput.class);
        lenient().when(mockedFeature.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);

        lenient().when(mockedPortStatusMessage.getVersion()).thenReturn(Uint8.valueOf(OFConstants.OFP_VERSION_1_3));
        lenient().when(mockedPortStatusMessage.getReason()).thenReturn(PortReason.OFPPRADD);
        lenient().when(mockedPortStatusMessage.getPortNo()).thenReturn(Uint32.valueOf(42L));
        deviceContextSpy.processPortStatusMessage(mockedPortStatusMessage);
        verify(messageSpy).spyMessage(any(), any());
    }

    @Test
    public void testProcessFlowRemovedMessage() {
        // prepare translation result
        final FlowRemovedBuilder flowRemovedMdsalBld = new FlowRemovedBuilder()
                .setTableId(Uint8.ZERO)
                .setPriority(Uint16.valueOf(42))
                .setCookie(new FlowCookie(Uint64.ONE))
                .setMatch(new MatchBuilder().build());
        final NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);

        Mockito.lenient().when(messageTranslatorFlowRemoved
                .translate(any(Object.class), any(DeviceInfo.class), any(Object.class)))
                .thenReturn(flowRemovedMdsalBld.build());

        // insert flow+flowId into local registry
        final FlowRegistryKey flowRegKey =
                FlowRegistryKeyFactory.create(deviceInfo.getVersion(), flowRemovedMdsalBld.build());
        final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(Uint8.ZERO, new FlowId("ut-ofp:f456"));
        deviceContext.getDeviceFlowRegistry().storeDescriptor(flowRegKey, flowDescriptor);

        // prepare empty input message
        final FlowRemovedMessageBuilder flowRemovedBld = new FlowRemovedMessageBuilder();

        // prepare path to flow to be removed
        final KeyedInstanceIdentifier<Flow, FlowKey> flowToBeRemovedPath = nodeKeyIdent
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(Uint8.ZERO))
                .child(Flow.class, new FlowKey(new FlowId("ut-ofp:f456")));

        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        deviceContext.processFlowRemovedMessage(flowRemovedBld.build());
    }

    @Test
    public void testProcessExperimenterMessage() {
        final ConvertorMessageFromOFJava mockedMessageConverter = mock(ConvertorMessageFromOFJava.class);
        final ExtensionConverterProvider mockedExtensionConverterProvider = mock(ExtensionConverterProvider.class);
        when(mockedExtensionConverterProvider.getMessageConverter(any(MessageTypeKey.class)))
                .thenReturn(mockedMessageConverter);

        final ExperimenterDataOfChoice mockedExperimenterDataOfChoice = mock(ExperimenterDataOfChoice.class);
        final ExperimenterMessage experimenterMessage = new ExperimenterMessageBuilder()
                .setExperimenterDataOfChoice(mockedExperimenterDataOfChoice).build();

        final NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);

        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        ((DeviceContextImpl) deviceContext).setExtensionConverterProvider(mockedExtensionConverterProvider);
        deviceContext.processExperimenterMessage(experimenterMessage);

        verify(mockedNotificationPublishService).offerNotification(any(ExperimenterMessageFromDev.class));
    }

    @Test
    public void instantiateServiceInstance() {
        deviceContext.instantiateServiceInstance();
    }

    @Test
    public void close() {
        deviceContext.close();
    }

    @Test
    public void closeServiceInstance() {
        deviceContext.closeServiceInstance();
    }

}
