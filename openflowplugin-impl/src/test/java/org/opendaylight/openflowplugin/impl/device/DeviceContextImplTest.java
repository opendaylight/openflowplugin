package org.opendaylight.openflowplugin.impl.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opendaylight.controller.md.sal.binding.api.NotificationPublishService.REJECTED;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.*;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.concepts.Registration;
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
    Xid xid;
    Xid xidMulti;
    DeviceContextImpl deviceContext;
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
    MessageTranslator messageTranslator;

    private final AtomicLong atomicLong = new AtomicLong(0);

    @Before
    public void setUp() {
        final CheckedFuture<Optional<Node>, ReadFailedException> noExistNodeFuture = Futures.immediateCheckedFuture(Optional.<Node>absent());
        Mockito.when(rTx.read(LogicalDatastoreType.OPERATIONAL, nodeKeyIdent)).thenReturn(noExistNodeFuture);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        Mockito.when(dataBroker.createTransactionChain(Mockito.any(TransactionChainManager.class))).thenReturn(txChainFactory);
        Mockito.when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodeKeyIdent);

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

        Mockito.when(deviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(messageTranslator.translate(any(Object.class), any(DeviceContext.class), any(Object.class))).thenReturn(mock(PacketReceived.class));
        Mockito.when(translatorLibrary.lookupTranslator(eq(new TranslatorKey(OFConstants.OFP_VERSION_1_3, PacketIn.class.getName())))).thenReturn(messageTranslator);


        deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker, timer, messageIntelligenceAgency, outboundQueueProvider, translatorLibrary, txChainManager);

        xid = new Xid(atomicLong.incrementAndGet());
        xidMulti = new Xid(atomicLong.incrementAndGet());
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullDataBroker() throws Exception {
        new DeviceContextImpl(connectionContext, deviceState, null, timer, messageIntelligenceAgency, outboundQueueProvider, translatorLibrary, txChainManager).close();
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullDeviceState() throws Exception {
        new DeviceContextImpl(connectionContext, null, dataBroker, timer, messageIntelligenceAgency, outboundQueueProvider, translatorLibrary, txChainManager).close();
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullTimer() throws Exception {
        new DeviceContextImpl(null, deviceState, dataBroker, null, messageIntelligenceAgency, outboundQueueProvider, translatorLibrary, txChainManager).close();
    }

    @Test
    public void testGetDeviceState() {
        final DeviceState deviceSt = deviceContext.getDeviceState();
        assertNotNull(deviceSt);
        Assert.assertEquals(deviceState, deviceSt);
    }

    @Test
    public void testGetReadTransaction() {
        final ReadTransaction readTx = deviceContext.getReadTransaction();
        assertNotNull(readTx);
        Assert.assertEquals(rTx, readTx);
    }

    @Test
    public void testInitialSubmitTransaction() {
        deviceContext.initialSubmitTransaction();
        verify(txChainManager).initialSubmitWriteTransaction();
    }

    @Test
    public void testGetReservedXid() {
        deviceContext.getReservedXid();
        verify(outboundQueueProvider).reserveEntry();
    }

    @Test
    public void testAuxiliaryConnectionContext() {
        ConnectionContext mockedConnectionContext = mock(ConnectionContext.class);
        FeaturesReply mockedFeaturesReply = mock(FeaturesReply.class);
        when(mockedFeaturesReply.getAuxiliaryId()).thenReturn(DUMMY_AUXILIARY_ID);
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeaturesReply);
        deviceContext.addAuxiliaryConenctionContext(mockedConnectionContext);
        final ConnectionContext pickedConnectiobContexts = deviceContext.getAuxiliaryConnectiobContexts(DUMMY_COOKIE);
        assertEquals(mockedConnectionContext, pickedConnectiobContexts);
    }

    @Test
    public void testAddDeleteToTxChain() {
        InstanceIdentifier<Nodes> dummyII = InstanceIdentifier.create(Nodes.class);
        deviceContext.addDeleteToTxChain(LogicalDatastoreType.CONFIGURATION, dummyII);
        verify(txChainManager).addDeleteOperationTotTxChain(eq(LogicalDatastoreType.CONFIGURATION), eq(dummyII));
    }

    @Test
    public void testSubmitTransaction() {
        deviceContext.submitTransaction();
        verify(txChainManager).submitWriteTransaction();
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
        Error mockedError = mock(Error.class);
        deviceContext.processReply(mockedError);
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE));
        OfHeader mockedOfHeader= mock(OfHeader.class);
        deviceContext.processReply(mockedOfHeader);
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS));
    }

    @Test
    public void testProcessReply2() {
        MultipartReply mockedMultipartReply = mock(MultipartReply.class);
        Xid dummyXid = new Xid(DUMMY_XID);
        deviceContext.processReply(dummyXid, Lists.newArrayList(mockedMultipartReply));
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE));
    }

    @Test
    public void testProcessPacketInMessage() {
        PacketInMessage mockedPacketInMessage = mock(PacketInMessage.class);
        NotificationPublishService mockedNotificationPublishService = mock(NotificationPublishService.class);
        ListenableFuture stringListenableFuture = Futures.immediateFuture(new String("dummy value"));

        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class))).thenReturn(stringListenableFuture);
        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
        deviceContext.processPacketInMessage(mockedPacketInMessage);
        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS));


//        final ListenableFuture dummyFuture = Futures.immediateFuture(new IllegalStateException());
//        when(mockedNotificationPublishService.offerNotification(any(PacketReceived.class))).thenReturn(dummyFuture);
//        deviceContext.setNotificationPublishService(mockedNotificationPublishService);
//        verify(messageIntelligenceAgency).spyMessage(any(Class.class), eq(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_NOTIFICATION_REJECTED));
    }
}
