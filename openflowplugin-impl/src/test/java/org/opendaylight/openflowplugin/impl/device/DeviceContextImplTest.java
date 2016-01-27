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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextClosedHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class DeviceContextImplTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(DeviceContextImplTest.class);
    private static final short DUMMY_AUXILIARY_ID = 33;
    private static final BigInteger DUMMY_COOKIE = new BigInteger("33");
    private static final Long DUMMY_XID = 544L;
    private static final Long DUMMY_PORT_NUMBER = 159L;
    private static final Integer DUMMY_PORT = 753;
    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("55");
    Xid xid;
    Xid xidMulti;

    @Mock
    DeviceContextImpl mockDeviceContext;
    @Mock
    TransactionChainManager txChainManager;
    @Mock
    RequestContext<GetAsyncReply> requestContext;
    @Mock
    RequestContext<MultipartReply> requestContextMultiReply;
    @Mock
    ConnectionContext connectionContext;
    @Mock
    ConnectionContext anotherConnectionContext;
    @Mock
    DeviceState deviceState;
    @Mock
    DataBroker dataBroker;
    @Mock
    WriteTransaction wTx;
    @Mock
    ReadOnlyTransaction rTx;
    @Mock
    BindingTransactionChain txChainFactory;
    @Mock
    HashedWheelTimer timer;
    @Mock
    MessageIntelligenceAgency messageIntelligenceAgency;
    @Mock
    OutboundQueueProvider outboundQueueProvider;
    @Mock
    ConnectionAdapter connectionAdapter;
    NodeId nodeId = new NodeId("h2g2:42");
    KeyedInstanceIdentifier<Node, NodeKey> nodeKeyIdent = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
    @Mock
    TranslatorLibrary translatorLibrary;
    @Mock
    Registration registration;
    @Mock
    MessageTranslator messageTranslatorPacketReceived;
    @Mock
    MessageTranslator messageTranslatorFlowCapableNodeConnector;
    @Mock
    private MessageTranslator<Object, Object> messageTranslatorFlowRemoved;
    @Mock
    FeaturesReply featuresReply;
    @Mock
    ExtensionConverterProvider mockExtensionConverterProvider;
    @Mock
    RpcContext mockRpcContext;

    private final AtomicLong atomicLong = new AtomicLong(0);

    @Before
    public void setUp() {
        final CheckedFuture<Optional<Node>, ReadFailedException> noExistNodeFuture = Futures.immediateCheckedFuture(Optional.<Node>absent());
        Mockito.when(rTx.read(LogicalDatastoreType.OPERATIONAL, nodeKeyIdent)).thenReturn(noExistNodeFuture);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        Mockito.when(dataBroker.createTransactionChain(Mockito.any(TransactionChainManager.class))).thenReturn(txChainFactory);
        Mockito.when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodeKeyIdent);
        Mockito.when(deviceState.getNodeId()).thenReturn(nodeId);
        final SettableFuture<RpcResult<GetAsyncReply>> settableFuture = SettableFuture.create();
        final SettableFuture<RpcResult<MultipartReply>> settableFutureMultiReply = SettableFuture.create();
        Mockito.when(requestContext.getFuture()).thenReturn(settableFuture);
        Mockito.doAnswer(new Answer<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer(final InvocationOnMock invocation) {
                settableFuture.set((RpcResult<GetAsyncReply>) invocation.getArguments()[0]);
                return null;
            }
        }).when(requestContext).setResult(any(RpcResult.class));

        Mockito.when(requestContextMultiReply.getFuture()).thenReturn(settableFutureMultiReply);
        Mockito.doAnswer(new Answer<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer(final InvocationOnMock invocation) {
                settableFutureMultiReply.set((RpcResult<MultipartReply>) invocation.getArguments()[0]);
                return null;
            }
        }).when(requestContextMultiReply).setResult(any(RpcResult.class));
        Mockito.when(txChainFactory.newWriteOnlyTransaction()).thenReturn(wTx);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        Mockito.when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        Mockito.when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(anotherConnectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(anotherConnectionContext.getFeatures()).thenReturn(featuresReply);
        Mockito.when(featuresReply.getAuxiliaryId()).thenReturn(DUMMY_AUXILIARY_ID);
        Mockito.when(connectionAdapter.getRemoteAddress()).thenReturn(new InetSocketAddress(DUMMY_PORT));
        Mockito.when(deviceState.isValid()).thenReturn(true);
        Mockito.when(deviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(messageTranslatorPacketReceived.translate(any(Object.class), any(DeviceContext.class), any(Object.class))).thenReturn(mock(PacketReceived.class));
        Mockito.when(messageTranslatorFlowCapableNodeConnector.translate(any(Object.class), any(DeviceContext.class), any(Object.class))).thenReturn(mock(FlowCapableNodeConnector.class));
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3, PacketIn.class.getName())))).thenReturn(messageTranslatorPacketReceived);
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3, PortGrouping.class.getName())))).thenReturn(messageTranslatorFlowCapableNodeConnector);
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3,
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved.class.getName()))))
                .thenReturn(messageTranslatorFlowRemoved);
        mockDeviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker, timer, messageIntelligenceAgency, outboundQueueProvider, translatorLibrary);
        /** Setting null (mocked) txChainManager */
        mockDeviceContext.setTransactionChainManager(txChainManager);

        xid = new Xid(atomicLong.incrementAndGet());
        xidMulti = new Xid(atomicLong.incrementAndGet());
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullDataBroker() throws Exception {
        new DeviceContextImpl(connectionContext, deviceState, null, timer, messageIntelligenceAgency, outboundQueueProvider, translatorLibrary).close();
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullDeviceState() throws Exception {
        new DeviceContextImpl(connectionContext, null, dataBroker, timer, messageIntelligenceAgency, outboundQueueProvider, translatorLibrary).close();
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullTimer() throws Exception {
        new DeviceContextImpl(null, deviceState, dataBroker, null, messageIntelligenceAgency, outboundQueueProvider, translatorLibrary).close();
    }

    @Test
    public void testGetDeviceState() {
        final DeviceState deviceSt = mockDeviceContext.getDeviceState();
        assertNotNull(deviceSt);
        assertEquals(deviceState, deviceSt);
    }

    @Test
    public void testGetReadTransaction() {
        final ReadTransaction readTx = mockDeviceContext.getReadTransaction();
        assertNotNull(readTx);
        assertEquals(rTx, readTx);
    }

    @Test
    public void testGetSetExtensionConverterProvider() {
        mockDeviceContext.setExtensionConverterProvider(mockExtensionConverterProvider);
        final ExtensionConverterProvider extensionConverterProvider = mockDeviceContext.getExtensionConverterProvider();
        assertEquals(mockExtensionConverterProvider, extensionConverterProvider);
    }

    @Test
    public void testGetSetRpcContext() {
        mockDeviceContext.setRpcContext(mockRpcContext);
        final RpcContext rpcContext = mockDeviceContext.getRpcContext();
        assertEquals(mockRpcContext, rpcContext);
    }

    @Test
    public void testInitialSubmitTransaction() throws Exception {
        mockDeviceContext.initialSubmitTransaction();
        verify(txChainManager).initialSubmitWriteTransaction();
    }

    @Test
    public void testGetReservedXid() {
        mockDeviceContext.getReservedXid();
        verify(outboundQueueProvider).reserveEntry();
    }

    @Test
    public void testAuxiliaryConnectionContext() {
        ConnectionContext mockedConnectionContext = addDummyAuxiliaryConnectionContext();
        final ConnectionContext pickedConnectiobContexts = mockDeviceContext.getAuxiliaryConnectiobContexts(DUMMY_COOKIE);
        assertEquals(mockedConnectionContext, pickedConnectiobContexts);
    }

    private ConnectionContext addDummyAuxiliaryConnectionContext() {
        ConnectionContext mockedConnectionContext = prepareConnectionContext();
        mockDeviceContext.addAuxiliaryConenctionContext(mockedConnectionContext);
        return mockedConnectionContext;
    }

    private ConnectionContext prepareConnectionContext() {
        ConnectionContext mockedConnectionContext = mock(ConnectionContext.class);
        FeaturesReply mockedFeaturesReply = mock(FeaturesReply.class);
        when(mockedFeaturesReply.getAuxiliaryId()).thenReturn(DUMMY_AUXILIARY_ID);
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeaturesReply);
        return mockedConnectionContext;
    }

    @Test
    public void testAddDeleteToTxChain() throws Exception{
        InstanceIdentifier<Nodes> dummyII = InstanceIdentifier.create(Nodes.class);
        mockDeviceContext.addDeleteToTxChain(LogicalDatastoreType.CONFIGURATION, dummyII);
        verify(txChainManager).addDeleteOperationTotTxChain(eq(LogicalDatastoreType.CONFIGURATION), eq(dummyII));
    }

    @Test
    public void testSubmitTransaction() throws Exception {
        mockDeviceContext.submitTransaction();
        verify(txChainManager).submitWriteTransaction();
    }

    @Test
    public void testGetPrimaryConnectionContext() {
        final ConnectionContext primaryConnectionContext = mockDeviceContext.getPrimaryConnectionContext();
        assertEquals(connectionContext, primaryConnectionContext);
    }

    @Test
    public void testGetDeviceFlowRegistry() {
        final DeviceFlowRegistry deviceFlowRegistry = mockDeviceContext.getDeviceFlowRegistry();
        assertNotNull(deviceFlowRegistry);
    }

    @Test
    public void testGetDeviceGroupRegistry() {
        final DeviceGroupRegistry deviceGroupRegistry = mockDeviceContext.getDeviceGroupRegistry();
        assertNotNull(deviceGroupRegistry);
    }

    @Test
    public void testGetDeviceMeterRegistry() {
        final DeviceMeterRegistry deviceMeterRegistry = mockDeviceContext.getDeviceMeterRegistry();
        assertNotNull(deviceMeterRegistry);
    }

    @Test
    public void testProcessReply() {
        Error mockedError = mock(Error.class);
        mockDeviceContext.processReply(mockedError);
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE));
        OfHeader mockedOfHeader = mock(OfHeader.class);
        mockDeviceContext.processReply(mockedOfHeader);
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessReply2() {
        MultipartReply mockedMultipartReply = mock(MultipartReply.class);
        Xid dummyXid = new Xid(DUMMY_XID);
        mockDeviceContext.processReply(dummyXid, Lists.newArrayList(mockedMultipartReply));
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE));
    }

    @Test
    public void testProcessPacketInMessageFutureSuccess() {
        PacketInMessage mockedPacketInMessage = mock(PacketInMessage.class);
        NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        final ListenableFuture stringListenableFuture = Futures.immediateFuture(new String("dummy value"));

        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class))).thenReturn(stringListenableFuture);
        mockDeviceContext.setNotificationPublishService(mockedNotificationPublishService);
        mockDeviceContext.processPacketInMessage(mockedPacketInMessage);
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test(expected = NullPointerException.class)
    public void testProcessPacketInMessageNull() {
        NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        final ListenableFuture stringListenableFuture = Futures.immediateFuture(new String("dummy value"));

        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class))).thenReturn(stringListenableFuture);
        mockDeviceContext.setNotificationPublishService(mockedNotificationPublishService);
        mockDeviceContext.processPacketInMessage(null);
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE));
    }

    @Test
    public void testProcessPacketInMessageFutureFailure() {
        PacketInMessage mockedPacketInMessage = mock(PacketInMessage.class);
        NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        final ListenableFuture dummyFuture = Futures.immediateFailedFuture(new IllegalStateException());

        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class))).thenReturn(dummyFuture);
        mockDeviceContext.setNotificationPublishService(mockedNotificationPublishService);
        mockDeviceContext.processPacketInMessage(mockedPacketInMessage);
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_NOTIFICATION_REJECTED));
    }

    @Test
    public void testTranslatorLibrary() {
        final TranslatorLibrary pickedTranslatorLibrary = mockDeviceContext.oook();
        assertEquals(translatorLibrary, pickedTranslatorLibrary);
    }

    @Test
    public void testGetTimer() {
        final HashedWheelTimer pickedTimer = mockDeviceContext.getTimer();
        assertEquals(timer, pickedTimer);
    }

    @Test
    public void testClose() {
        ConnectionAdapter mockedConnectionAdapter = mock(ConnectionAdapter.class);
        InetSocketAddress mockRemoteAddress = InetSocketAddress.createUnresolved("odl-unit.example.org",999);
        when(mockedConnectionAdapter.getRemoteAddress()).thenReturn(mockRemoteAddress);
        when(connectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);

        NodeId dummyNodeId = new NodeId("dummyNodeId");
        when(deviceState.getNodeId()).thenReturn(dummyNodeId);

        ConnectionContext mockedAuxiliaryConnectionContext = prepareConnectionContext();
        mockDeviceContext.addAuxiliaryConenctionContext(mockedAuxiliaryConnectionContext);
        DeviceContextClosedHandler mockedDeviceContextClosedHandler = mock(DeviceContextClosedHandler.class);
        mockDeviceContext.addDeviceContextClosedHandler(mockedDeviceContextClosedHandler);
        mockDeviceContext.close();
        verify(connectionContext).closeConnection(eq(false));
        verify(deviceState).setValid(eq(false));
        verify(mockedAuxiliaryConnectionContext).closeConnection(eq(false));
    }

    @Test
    public void testBarrierFieldSetGet() {
        Timeout mockedTimeout = mock(Timeout.class);
        mockDeviceContext.setCurrentBarrierTimeout(mockedTimeout);
        final Timeout pickedBarrierTimeout = mockDeviceContext.getBarrierTaskTimeout();
        assertEquals(mockedTimeout, pickedBarrierTimeout);
    }

    @Test
    public void testGetMessageSpy() {
        final MessageSpy pickedMessageSpy = mockDeviceContext.getMessageSpy();
        assertEquals(messageIntelligenceAgency, pickedMessageSpy);
    }

    @Test
    public void testNodeConnector() {
        NodeConnectorRef mockedNodeConnectorRef = mock(NodeConnectorRef.class);
        mockDeviceContext.storeNodeConnectorRef(DUMMY_PORT_NUMBER, mockedNodeConnectorRef);
        final NodeConnectorRef nodeConnectorRef = mockDeviceContext.lookupNodeConnectorRef(DUMMY_PORT_NUMBER);
        assertEquals(mockedNodeConnectorRef, nodeConnectorRef);

    }

    @Test
    public void testOnPublished() {
        final ConnectionContext auxiliaryConnectionContext = addDummyAuxiliaryConnectionContext();

        ConnectionAdapter mockedAuxConnectionAdapter = mock(ConnectionAdapter.class);
        when(auxiliaryConnectionContext.getConnectionAdapter()).thenReturn(mockedAuxConnectionAdapter);

        ConnectionAdapter mockedConnectionAdapter = mock(ConnectionAdapter.class);
        when(connectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);

        mockDeviceContext.onPublished();
        verify(mockedAuxConnectionAdapter).setPacketInFiltering(eq(false));
        verify(mockedConnectionAdapter).setPacketInFiltering(eq(false));
    }

    @Test
    public void testPortStatusMessage() {
        PortStatusMessage mockedPortStatusMessage = mock(PortStatusMessage.class);
        Class dummyClass = Class.class;
        when(mockedPortStatusMessage.getImplementedInterface()).thenReturn(dummyClass);


        GetFeaturesOutput mockedFeature = mock(GetFeaturesOutput.class);
        when(mockedFeature.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(deviceState.getFeatures()).thenReturn(mockedFeature);

        when(mockedPortStatusMessage.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(mockedPortStatusMessage.getReason()).thenReturn(PortReason.OFPPRADD);

        OpenflowPortsUtil.init();
        mockDeviceContext.processPortStatusMessage(mockedPortStatusMessage);
        verify(txChainManager).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), any(InstanceIdentifier.class), any(DataObject.class));
    }

    @Test
    public void testProcessFlowRemovedMessage() throws Exception {
        // prepare translation result
        final FlowRemovedBuilder flowRemovedMdsalBld = new FlowRemovedBuilder()
                .setTableId((short) 0)
                .setPriority(42)
                .setCookie(new FlowCookie(BigInteger.ONE))
                .setMatch(new MatchBuilder().build());

        Mockito.when(messageTranslatorFlowRemoved.translate(any(Object.class), any(DeviceContext.class), any(Object.class)))
                .thenReturn(flowRemovedMdsalBld.build());

        // insert flow+flowId into local registry
        FlowRegistryKey flowRegKey = FlowRegistryKeyFactory.create(flowRemovedMdsalBld.build());
        FlowDescriptor flowDescriptor = FlowDescriptorFactory.create((short) 0, new FlowId("ut-ofp:f456"));
        mockDeviceContext.getDeviceFlowRegistry().store(flowRegKey, flowDescriptor);

        // plug in lifecycleListener
        final ItemLifecycleListener itemLifecycleListener = Mockito.mock(ItemLifecycleListener.class);
        for (ItemLifeCycleSource lifeCycleSource : mockDeviceContext.getItemLifeCycleSourceRegistry().getLifeCycleSources()) {
            lifeCycleSource.setItemLifecycleListener(itemLifecycleListener);
        }

        // prepare empty input message
        final FlowRemovedMessageBuilder flowRemovedBld = new FlowRemovedMessageBuilder();

        // prepare path to flow to be removed
        KeyedInstanceIdentifier<Flow, FlowKey> flowToBeRemovedPath = nodeKeyIdent
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey((short) 0))
                .child(Flow.class, new FlowKey(new FlowId("ut-ofp:f456")));

        mockDeviceContext.processFlowRemovedMessage(flowRemovedBld.build());
        verify(itemLifecycleListener).onRemoved(flowToBeRemovedPath);
    }

    @Test
    public void testOnDeviceDisconnected() throws Exception {
        DeviceContextClosedHandler deviceContextClosedHandler = mock(DeviceContextClosedHandler.class);
        mockDeviceContext.addDeviceContextClosedHandler(deviceContextClosedHandler);

        mockDeviceContext.onDeviceDisconnected(connectionContext);

        verify(deviceContextClosedHandler).onDeviceContextClosed(mockDeviceContext);
        assertEquals(0, mockDeviceContext.getDeviceFlowRegistry().getAllFlowDescriptors().size());
        assertEquals(0, mockDeviceContext.getDeviceGroupRegistry().getAllGroupIds().size());
        assertEquals(0, mockDeviceContext.getDeviceMeterRegistry().getAllMeterIds().size());

    }

    @Test(expected = NullPointerException.class)
    public void testOnDeviceDisconnectedNullContext() throws Exception {
        mockDeviceContext.onDeviceDisconnected(null);
    }

    @Test
    public void testOnDeviceDisconnectedBadContext() throws Exception {
        //TODO:What should happen on in this case ?
        mockDeviceContext.onDeviceDisconnected(anotherConnectionContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnClusterRoleChangeNull() throws Exception {
        mockDeviceContext.onClusterRoleChange(null);
    }

    @Test
    public void testOnClusterRoleChangeBecomeMaster() throws Exception {
        /** Setting not null (not mocked) transaction manager */
        mockDeviceContext.createAdnSetTransactionChainManager(dataBroker, deviceState);
        mockDeviceContext.onClusterRoleChange(OfpRole.BECOMEMASTER);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
    }

    @Test
    public void testOnClusterRoleChangeNoChangeFromMaster() throws Exception {
        /** Setting not null (not mocked) transaction manager */
        mockDeviceContext.createAdnSetTransactionChainManager(dataBroker, deviceState);
        mockDeviceContext.onClusterRoleChange(OfpRole.BECOMEMASTER);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
        mockDeviceContext.onClusterRoleChange(OfpRole.NOCHANGE);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
    }

    @Test
    public void testOnClusterRoleChangeNoChangeFromSlave() throws Exception {
        /** Setting not null (not mocked) transaction manager */
        mockDeviceContext.createAdnSetTransactionChainManager(dataBroker, deviceState);
        mockDeviceContext.onClusterRoleChange(OfpRole.BECOMESLAVE);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.SLEEPING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
        mockDeviceContext.onClusterRoleChange(OfpRole.NOCHANGE);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.SLEEPING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
    }

    @Test
    public void testOnClusterRoleChangeBecomeSlave() throws Exception {
        /** Setting not null (not mocked) transaction manager */
        mockDeviceContext.createAdnSetTransactionChainManager(dataBroker, deviceState);
        mockDeviceContext.onClusterRoleChange(OfpRole.BECOMESLAVE);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.SLEEPING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
    }

    @Test
    public void testOnClusterRoleChangeBecomeMasterAfterMaster() throws Exception {
        /** Setting not null (not mocked) transaction manager */
        mockDeviceContext.createAdnSetTransactionChainManager(dataBroker, deviceState);
        mockDeviceContext.onClusterRoleChange(OfpRole.BECOMEMASTER);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
        mockDeviceContext.onClusterRoleChange(OfpRole.BECOMEMASTER);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.WORKING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
    }

    @Test
    public void testOnClusterRoleChangeBecomeSlaveAfterSlave() throws Exception {
        /** Setting not null (not mocked) transaction manager */
        mockDeviceContext.createAdnSetTransactionChainManager(dataBroker, deviceState);
        mockDeviceContext.onClusterRoleChange(OfpRole.BECOMESLAVE);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.SLEEPING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
        mockDeviceContext.onClusterRoleChange(OfpRole.BECOMESLAVE);
        assertEquals(TransactionChainManager.TransactionChainManagerStatus.SLEEPING, mockDeviceContext.getTransactionChainManager().getTransactionChainManagerStatus());
    }
}
