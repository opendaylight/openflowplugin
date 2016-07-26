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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
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
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
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
    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("55");
    Xid xid;
    Xid xidMulti;

    DeviceContext deviceContext;
    @Mock
    TransactionChainManager txChainManager;
    @Mock
    RequestContext<GetAsyncReply> requestContext;
    @Mock
    RequestContext<MultipartReply> requestContextMultiReply;
    @Mock
    ConnectionContext connectionContext;
    @Mock
    DeviceState deviceState;
    @Mock
    GetFeaturesOutput featuresOutput;
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
    private DeviceInfo deviceInfo;
    @Mock
    private DeviceManager deviceManager;
    @Mock
    private LifecycleService lifecycleService;
    @Mock
    private MessageSpy messageSpy;

    private InOrder inOrderDevState;

    private final AtomicLong atomicLong = new AtomicLong(0);

    private DeviceContext deviceContextSpy;

    @Before
    public void setUp() throws Exception{
        final CheckedFuture<Optional<Node>, ReadFailedException> noExistNodeFuture = Futures.immediateCheckedFuture(Optional.<Node>absent());
        Mockito.when(rTx.read(LogicalDatastoreType.OPERATIONAL, nodeKeyIdent)).thenReturn(noExistNodeFuture);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        Mockito.when(dataBroker.createTransactionChain(Mockito.any(TransactionChainManager.class))).thenReturn(txChainFactory);
        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeKeyIdent);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
        Mockito.when(deviceInfo.getDatapathId()).thenReturn(BigInteger.ONE);
        final SettableFuture<RpcResult<GetAsyncReply>> settableFuture = SettableFuture.create();
        final SettableFuture<RpcResult<MultipartReply>> settableFutureMultiReply = SettableFuture.create();
        Mockito.when(requestContext.getFuture()).thenReturn(settableFuture);
        Mockito.doAnswer(invocation -> {
            settableFuture.set((RpcResult<GetAsyncReply>) invocation.getArguments()[0]);
            return null;
        }).when(requestContext).setResult(any(RpcResult.class));

        Mockito.when(requestContextMultiReply.getFuture()).thenReturn(settableFutureMultiReply);
        Mockito.doAnswer(invocation -> {
            settableFutureMultiReply.set((RpcResult<MultipartReply>) invocation.getArguments()[0]);
            return null;
        }).when(requestContextMultiReply).setResult(any(RpcResult.class));
        Mockito.when(txChainFactory.newWriteOnlyTransaction()).thenReturn(wTx);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        Mockito.when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        Mockito.when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionContext.getDeviceInfo()).thenReturn(deviceInfo);
        final FeaturesReply mockedFeaturesReply = mock(FeaturesReply.class);
        when(connectionContext.getFeatures()).thenReturn(mockedFeaturesReply);
        when(connectionContext.getFeatures().getCapabilities()).thenReturn(mock(Capabilities.class));

        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(featuresOutput.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        Mockito.when(featuresOutput.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(messageTranslatorPacketReceived.translate(any(Object.class), any(DeviceInfo.class), any(Object.class))).thenReturn(mock(PacketReceived.class));
        Mockito.when(messageTranslatorFlowCapableNodeConnector.translate(any(Object.class), any(DeviceInfo.class), any(Object.class))).thenReturn(mock(FlowCapableNodeConnector.class));
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3, PacketIn.class.getName())))).thenReturn(messageTranslatorPacketReceived);
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3, PortGrouping.class.getName())))).thenReturn(messageTranslatorFlowCapableNodeConnector);
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3,
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved.class.getName()))))
                .thenReturn(messageTranslatorFlowRemoved);

        deviceContext = new DeviceContextImpl(
                connectionContext,
                dataBroker,
                messageSpy,
                translatorLibrary,
		        deviceManager);
        deviceContextSpy = Mockito.spy(deviceContext);

        xid = new Xid(atomicLong.incrementAndGet());
        xidMulti = new Xid(atomicLong.incrementAndGet());

        Mockito.doNothing().when(deviceContextSpy).writeToTransaction(Mockito.<LogicalDatastoreType>any(), Mockito.<InstanceIdentifier>any(), any());
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullDataBroker() throws Exception {
        new DeviceContextImpl(connectionContext, null, null, translatorLibrary, deviceManager).close();
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullTimer() throws Exception {
        new DeviceContextImpl(null, dataBroker, null, translatorLibrary, deviceManager).close();
    }

    @Test
    public void testGetReadTransaction() {
        final ReadTransaction readTx = deviceContext.getReadTransaction();
        assertNotNull(readTx);
        assertEquals(rTx, readTx);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testInitialSubmitTransaction() throws Exception {
        Mockito.when(wTx.submit()).thenReturn(Futures.immediateCheckedFuture(null));
        final InstanceIdentifier<Nodes> dummyII = InstanceIdentifier.create(Nodes.class);
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().activateTransactionManager() ;
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().enableSubmit();
        deviceContext.addDeleteToTxChain(LogicalDatastoreType.CONFIGURATION, dummyII);
        deviceContext.initialSubmitTransaction();
        verify(wTx).submit();
    }

    @Test
    public void testAuxiliaryConnectionContext() {
        final ConnectionContext mockedConnectionContext = addDummyAuxiliaryConnectionContext();
        final ConnectionContext pickedConnectiobContexts = deviceContext.getAuxiliaryConnectiobContexts(DUMMY_COOKIE);
        assertEquals(mockedConnectionContext, pickedConnectiobContexts);
    }
    @Test
    public void testRemoveAuxiliaryConnectionContext() {
        final ConnectionContext mockedConnectionContext = addDummyAuxiliaryConnectionContext();

        final ConnectionAdapter mockedAuxConnectionAdapter = mock(ConnectionAdapter.class);
        when(mockedConnectionContext.getConnectionAdapter()).thenReturn(mockedAuxConnectionAdapter);

        assertNotNull(deviceContext.getAuxiliaryConnectiobContexts(DUMMY_COOKIE));
        deviceContext.removeAuxiliaryConnectionContext(mockedConnectionContext);
        assertNull(deviceContext.getAuxiliaryConnectiobContexts(DUMMY_COOKIE));
    }

    private ConnectionContext addDummyAuxiliaryConnectionContext() {
        final ConnectionContext mockedConnectionContext = prepareConnectionContext();
        deviceContext.addAuxiliaryConnectionContext(mockedConnectionContext);
        return mockedConnectionContext;
    }

    private ConnectionContext prepareConnectionContext() {
        final ConnectionContext mockedConnectionContext = mock(ConnectionContext.class);
        final FeaturesReply mockedFeaturesReply = mock(FeaturesReply.class);
        when(mockedFeaturesReply.getAuxiliaryId()).thenReturn(DUMMY_AUXILIARY_ID);
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeaturesReply);
        return mockedConnectionContext;
    }

    /**
     * @throws Exception
     */
    @Test
    public void testAddDeleteToTxChain() throws Exception{
        final InstanceIdentifier<Nodes> dummyII = InstanceIdentifier.create(Nodes.class);
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().activateTransactionManager() ;
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().enableSubmit();
        deviceContext.addDeleteToTxChain(LogicalDatastoreType.CONFIGURATION, dummyII);
        verify(wTx).delete(eq(LogicalDatastoreType.CONFIGURATION), eq(dummyII));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSubmitTransaction() throws Exception {
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().activateTransactionManager() ;
        ((DeviceContextImpl) deviceContext).getTransactionChainManager().enableSubmit();
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
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE));
        final OfHeader mockedOfHeader = mock(OfHeader.class);
        deviceContext.processReply(mockedOfHeader);
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessReply2() {
        final MultipartReply mockedMultipartReply = mock(MultipartReply.class);
        final Xid dummyXid = new Xid(DUMMY_XID);
        deviceContext.processReply(dummyXid, Lists.newArrayList(mockedMultipartReply));
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE));
    }

    @Test
    public void testProcessPacketInMessageFutureSuccess() {
        final PacketInMessage mockedPacketInMessage = mock(PacketInMessage.class);
        final NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        final ListenableFuture stringListenableFuture = Futures.immediateFuture(new String("dummy value"));

        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class))).thenReturn(stringListenableFuture);
        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        deviceContext.processPacketInMessage(mockedPacketInMessage);
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessPacketInMessageFutureFailure() {
        final PacketInMessage mockedPacketInMessage = mock(PacketInMessage.class);
        final NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        final ListenableFuture dummyFuture = Futures.immediateFailedFuture(new IllegalStateException());

        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class))).thenReturn(dummyFuture);
        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        deviceContext.processPacketInMessage(mockedPacketInMessage);
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_NOTIFICATION_REJECTED));
    }

    @Test
    public void testTranslatorLibrary() {
        final TranslatorLibrary pickedTranslatorLibrary = deviceContext.oook();
        assertEquals(translatorLibrary, pickedTranslatorLibrary);
    }

    @Test
    public void testShutdownConnection() {
        final ConnectionAdapter mockedConnectionAdapter = mock(ConnectionAdapter.class);
        final InetSocketAddress mockRemoteAddress = InetSocketAddress.createUnresolved("odl-unit.example.org",999);
        when(mockedConnectionAdapter.getRemoteAddress()).thenReturn(mockRemoteAddress);
        when(connectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);

        final NodeId dummyNodeId = new NodeId("dummyNodeId");
        when(deviceInfo.getNodeId()).thenReturn(dummyNodeId);

        final ConnectionContext mockedAuxiliaryConnectionContext = prepareConnectionContext();
        deviceContext.addAuxiliaryConnectionContext(mockedAuxiliaryConnectionContext);
        deviceContext.shutdownConnection();
        verify(connectionContext).closeConnection(true);
    }

    @Test
    public void testBarrierFieldSetGet() {
        final Timeout mockedTimeout = mock(Timeout.class);
        deviceContext.setCurrentBarrierTimeout(mockedTimeout);
        final Timeout pickedBarrierTimeout = deviceContext.getBarrierTaskTimeout();
        assertEquals(mockedTimeout, pickedBarrierTimeout);
    }

    @Test
    public void testGetMessageSpy() {
        final MessageSpy pickedMessageSpy = deviceContext.getMessageSpy();
        assertEquals(messageSpy, pickedMessageSpy);
    }

    @Test
    public void testOnPublished() {
        final ConnectionContext auxiliaryConnectionContext = addDummyAuxiliaryConnectionContext();

        final ConnectionAdapter mockedAuxConnectionAdapter = mock(ConnectionAdapter.class);
        when(auxiliaryConnectionContext.getConnectionAdapter()).thenReturn(mockedAuxConnectionAdapter);

        final ConnectionAdapter mockedConnectionAdapter = mock(ConnectionAdapter.class);
        when(connectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);

        deviceContext.onPublished();
        verify(mockedAuxConnectionAdapter).setPacketInFiltering(eq(false));
        verify(mockedConnectionAdapter).setPacketInFiltering(eq(false));
    }

    @Test
    public void testPortStatusMessage() throws Exception{
        final PortStatusMessage mockedPortStatusMessage = mock(PortStatusMessage.class);
        final Class dummyClass = Class.class;
        when(mockedPortStatusMessage.getImplementedInterface()).thenReturn(dummyClass);


        final GetFeaturesOutput mockedFeature = mock(GetFeaturesOutput.class);
        when(mockedFeature.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);

        when(mockedPortStatusMessage.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(mockedPortStatusMessage.getReason()).thenReturn(PortReason.OFPPRADD);
        when(mockedPortStatusMessage.getPortNo()).thenReturn(42L);

        OpenflowPortsUtil.init();
        deviceContextSpy.processPortStatusMessage(mockedPortStatusMessage);
        verify(deviceContextSpy).writeToTransaction(Mockito.<LogicalDatastoreType>any(), Mockito.<InstanceIdentifier>any(), any());
        verify(deviceContextSpy).submitTransaction();
    }

    @Test
    public void testProcessFlowRemovedMessage() throws Exception {
        // prepare translation result
        final FlowRemovedBuilder flowRemovedMdsalBld = new FlowRemovedBuilder()
                .setTableId((short) 0)
                .setPriority(42)
                .setCookie(new FlowCookie(BigInteger.ONE))
                .setMatch(new MatchBuilder().build());
        final NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);

        Mockito.when(messageTranslatorFlowRemoved.translate(any(Object.class), any(DeviceInfo.class), any(Object.class)))
                .thenReturn(flowRemovedMdsalBld.build());

        // insert flow+flowId into local registry
        final FlowRegistryKey flowRegKey = FlowRegistryKeyFactory.create(flowRemovedMdsalBld.build());
        final FlowDescriptor flowDescriptor = FlowDescriptorFactory.create((short) 0, new FlowId("ut-ofp:f456"));
        deviceContext.getDeviceFlowRegistry().store(flowRegKey, flowDescriptor);

        // plug in lifecycleListener
        final ItemLifecycleListener itemLifecycleListener = Mockito.mock(ItemLifecycleListener.class);
        for (final ItemLifeCycleSource lifeCycleSource : deviceContext.getItemLifeCycleSourceRegistry().getLifeCycleSources()) {
            lifeCycleSource.setItemLifecycleListener(itemLifecycleListener);
        }

        // prepare empty input message
        final FlowRemovedMessageBuilder flowRemovedBld = new FlowRemovedMessageBuilder();

        // prepare path to flow to be removed
        final KeyedInstanceIdentifier<Flow, FlowKey> flowToBeRemovedPath = nodeKeyIdent
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey((short) 0))
                .child(Flow.class, new FlowKey(new FlowId("ut-ofp:f456")));

        Mockito.when(deviceManager.getIsNotificationFlowRemovedOff()).thenReturn(true);

        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        deviceContext.processFlowRemovedMessage(flowRemovedBld.build());

        Mockito.verify(itemLifecycleListener).onRemoved(flowToBeRemovedPath);
        Mockito.verify(mockedNotificationPublishService, Mockito.never()).offerNotification(Matchers.any(Notification.class));

        Mockito.when(deviceManager.getIsNotificationFlowRemovedOff()).thenReturn(false);
        deviceContext.processFlowRemovedMessage(flowRemovedBld.build());

        Mockito.verify(mockedNotificationPublishService).offerNotification(Matchers.any(Notification.class));
    }

    @Test
    public void testProcessExperimenterMessage() {
        final ConvertorMessageFromOFJava mockedMessageConverter = mock(ConvertorMessageFromOFJava.class);
        final ExtensionConverterProvider mockedExtensionConverterProvider = mock(ExtensionConverterProvider.class);
        when(mockedExtensionConverterProvider.getMessageConverter(any(MessageTypeKey.class))).thenReturn(mockedMessageConverter);

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
    public void testOnDeviceDisconnected() throws Exception {
        final DeviceTerminationPhaseHandler deviceContextClosedHandler = mock(DeviceTerminationPhaseHandler.class);

        assertEquals(0, deviceContext.getDeviceFlowRegistry().getAllFlowDescriptors().size());
        assertEquals(0, deviceContext.getDeviceGroupRegistry().getAllGroupIds().size());
        assertEquals(0, deviceContext.getDeviceMeterRegistry().getAllMeterIds().size());

    }
}
