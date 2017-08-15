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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
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
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.device.initialization.AbstractDeviceInitializer;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.ExperimenterMessageFromDev;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

@RunWith(MockitoJUnitRunner.class)
public class DeviceContextImplTest {

    private static final short DUMMY_AUXILIARY_ID = 33;
    private static final Long DUMMY_XID = 544L;
    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("55");

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
    private WriteTransaction wTx;
    @Mock
    private ReadOnlyTransaction rTx;
    @Mock
    private BindingTransactionChain txChainFactory;
    @Mock
    private HashedWheelTimer timer;
    @Mock
    private OutboundQueueProvider outboundQueueProvider;
    @Mock
    private ConnectionAdapter connectionAdapter;
    @Mock
    private TranslatorLibrary translatorLibrary;
    @Mock
    private MessageTranslator<Object, Object> messageTranslatorPacketReceived;
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

    private NodeId nodeId = new NodeId("h2g2:42");
    private KeyedInstanceIdentifier<Node, NodeKey> nodeKeyIdent = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
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

        final PacketReceived packetReceived = new PacketReceivedBuilder()
                .setMatch(new org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.MatchBuilder()
                        .setInPort(new NodeConnectorId("openflow:1:LOCAL"))
                        .build())
                .build();

        Mockito.when(messageTranslatorPacketReceived.translate(any(Object.class), any(DeviceInfo.class), any(Object.class))).thenReturn(packetReceived);
        Mockito.when(messageTranslatorFlowCapableNodeConnector.translate(any(Object.class), any(DeviceInfo.class), any(Object.class))).thenReturn(mock(FlowCapableNodeConnector.class));
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3, PacketIn.class.getName())))).thenReturn(messageTranslatorPacketReceived);
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3, PortGrouping.class.getName())))).thenReturn(messageTranslatorFlowCapableNodeConnector);
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3,
                FlowRemoved.class.getName()))))
                .thenReturn(messageTranslatorFlowRemoved);

        Mockito.when(abstractDeviceInitializer.initialize(any(), anyBoolean(), anyBoolean(), any(), any()))
                .thenReturn(Futures.immediateFuture(null));

        final java.util.Optional<AbstractDeviceInitializer> deviceInitializer = java.util.Optional
                .of(this.abstractDeviceInitializer);

        Mockito.when(deviceInitializerProvider.lookup(OFConstants.OFP_VERSION_1_3)).thenReturn(deviceInitializer);
        Mockito.when(salRoleService.setRole(any())).thenReturn(Futures.immediateFuture(null));

        deviceContext = new DeviceContextImpl(
                connectionContext,
                dataBroker,
                messageSpy,
                translatorLibrary,
                convertorExecutor,
                false, timer, false,
                deviceInitializerProvider,
                true, false);

        deviceContext.setSalRoleService(salRoleService);
        ((DeviceContextImpl) deviceContext).lazyTransactionManagerInitialization();
        deviceContextSpy = Mockito.spy(deviceContext);

        Mockito.doNothing().when(deviceContextSpy).writeToTransaction(any(), any(), any());
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
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_FAILURE));
        final OfHeader mockedOfHeader = mock(OfHeader.class);
        deviceContext.processReply(mockedOfHeader);
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessReply2() {
        final Xid dummyXid = new Xid(DUMMY_XID);

        final Error mockedError = mock(Error.class);
        deviceContext.processReply(dummyXid, Lists.newArrayList(mockedError));
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_FAILURE));

        final MultipartReply mockedMultipartReply = mock(MultipartReply.class);
        deviceContext.processReply(dummyXid, Lists.newArrayList(mockedMultipartReply));
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessPacketInMessageFutureSuccess() {
        final PacketInMessage mockedPacketInMessage = mock(PacketInMessage.class);
        final NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        final ListenableFuture stringListenableFuture = Futures.immediateFuture(new String("dummy value"));

        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class))).thenReturn(stringListenableFuture);
        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        deviceContext.processPacketInMessage(mockedPacketInMessage);
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.StatisticsGroup.FROM_SWITCH));
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.StatisticsGroup.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessPacketInMessageFutureFailure() {
        final PacketInMessage mockedPacketInMessage = mock(PacketInMessage.class);
        final NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        final ListenableFuture dummyFuture = Futures.immediateFailedFuture(new IllegalStateException());

        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class))).thenReturn(dummyFuture);
        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        deviceContext.processPacketInMessage(mockedPacketInMessage);
        verify(messageSpy).spyMessage(any(Class.class), eq(MessageSpy.StatisticsGroup.FROM_SWITCH_NOTIFICATION_REJECTED));
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
    public void testPortStatusMessage() throws Exception{
        final PortStatusMessage mockedPortStatusMessage = mock(PortStatusMessage.class);
        final Class dummyClass = Class.class;
        when(mockedPortStatusMessage.getImplementedInterface()).thenReturn(dummyClass);


        final GetFeaturesOutput mockedFeature = mock(GetFeaturesOutput.class);
        when(mockedFeature.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);

        when(mockedPortStatusMessage.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(mockedPortStatusMessage.getReason()).thenReturn(PortReason.OFPPRADD);
        when(mockedPortStatusMessage.getPortNo()).thenReturn(42L);

        deviceContextSpy.processPortStatusMessage(mockedPortStatusMessage);
        verify(messageSpy).spyMessage(any(), any());
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
    public void instantiateServiceInstance() throws Exception {
        deviceContext.instantiateServiceInstance();
    }

    @Test
    public void close() throws Exception {
        deviceContext.close();
    }

    @Test
    public void closeServiceInstance() throws Exception {
        deviceContext.closeServiceInstance();
    }

}