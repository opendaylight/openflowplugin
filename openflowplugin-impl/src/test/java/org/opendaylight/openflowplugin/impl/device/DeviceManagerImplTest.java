/*
 *
 *  * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *
 */

package org.opendaylight.openflowplugin.impl.device;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandler;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandTypeBitmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(MockitoJUnitRunner.class)
public class DeviceManagerImplTest {

    private static final boolean TEST_VALUE_SWITCH_FEATURE_MANDATORY = true;
    private static final long TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA = 2000l;
    private static final KeyedInstanceIdentifier<Node, NodeKey> DUMMY_NODE_II = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("dummyNodeId")));
    private static final Short DUMMY_TABLE_ID = 1;
    private static final Long DUMMY_MAX_METER = 544L;
    private static final String DUMMY_DATAPATH_ID = "44";
    private static final Long DUMMY_PORT_NUMBER = 21L;

    @Mock
    CheckedFuture<Void, TransactionCommitFailedException> mockedFuture;
    @Mock
    private FeaturesReply mockFeatures;
    @Mock
    private OutboundQueue outboundQueueProvider;
    @Mock
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    @Mock
    private TranslatorLibrary translatorLibrary;
    @Mock
    private ConnectionContext mockConnectionContext;
    @Mock
    private ConnectionAdapter mockedConnectionAdapter;
    @Mock
    private DeviceContextImpl mockedDeviceContext;

    @Before
    public void setUp() throws Exception {
        OpenflowPortsUtil.init();

        when(mockConnectionContext.getNodeId()).thenReturn(new NodeId("dummyNodeId"));
        when(mockConnectionContext.getFeatures()).thenReturn(mockFeatures);
        when(mockConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockConnectionContext);

        final Capabilities capabilitiesV13 = Mockito.mock(Capabilities.class);
        final CapabilitiesV10 capabilitiesV10 = Mockito.mock(CapabilitiesV10.class);
        when(mockFeatures.getCapabilities()).thenReturn(capabilitiesV13);
        when(mockFeatures.getCapabilitiesV10()).thenReturn(capabilitiesV10);
        when(mockFeatures.getDatapathId()).thenReturn(BigInteger.valueOf(21L));
    }

    @Test
    public void onDeviceContextLevelUpFailTest() {
        onDeviceContextLevelUp(true);
    }

    @Test
    public void onDeviceContextLevelUpSuccessTest() {
        onDeviceContextLevelUp(false);
    }

    private DeviceManagerImpl prepareDeviceManager() {
        return prepareDeviceManager(false);
    }

    private DeviceManagerImpl prepareDeviceManager(final boolean withException) {
        final DataBroker mockedDataBroker = mock(DataBroker.class);
        final WriteTransaction mockedWriteTransaction = mock(WriteTransaction.class);

        final BindingTransactionChain mockedTxChain = mock(BindingTransactionChain.class);
        final WriteTransaction mockedWTx = mock(WriteTransaction.class);
        when(mockedTxChain.newWriteOnlyTransaction()).thenReturn(mockedWTx);
        when(mockedDataBroker.createTransactionChain(any(TransactionChainListener.class))).thenReturn
                (mockedTxChain);
        when(mockedDataBroker.newWriteOnlyTransaction()).thenReturn(mockedWriteTransaction);

        when(mockedWriteTransaction.submit()).thenReturn(mockedFuture);

        final MessageIntelligenceAgency mockedMessageIntelligenceAgency = mock(MessageIntelligenceAgency.class);
        final DeviceManagerImpl deviceManager = new DeviceManagerImpl(mockedDataBroker,
                mockedMessageIntelligenceAgency, TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA);
        deviceManager.setDeviceInitializationPhaseHandler(deviceInitPhaseHandler);

        return deviceManager;
    }

    public void onDeviceContextLevelUp(final boolean withException) {
        final DeviceManagerImpl deviceManager = prepareDeviceManager(withException);
        final DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceState.getRole()).thenReturn(OfpRole.BECOMEMASTER);

        if (withException) {
            doThrow(new IllegalStateException("dummy")).when(mockedDeviceContext).initialSubmitTransaction();
        }

        deviceManager.onDeviceContextLevelUp(mockedDeviceContext);
        if (withException) {
            verify(mockedDeviceContext).close();
        } else {
            verify(mockedDeviceContext).initialSubmitTransaction();
            verify(mockedDeviceContext).onPublished();
        }
    }

    @Ignore
    @Test
    public void deviceConnectedTest() {
        final DeviceManagerImpl deviceManager = prepareDeviceManager();
        injectMockTranslatorLibrary(deviceManager);
        final ConnectionContext mockConnectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);

        deviceManager.deviceConnected(mockConnectionContext);

        final InOrder order = inOrder(mockConnectionContext);
        order.verify(mockConnectionContext).getFeatures();
        order.verify(mockConnectionContext).setOutboundQueueProvider(any(OutboundQueueProvider.class));
        order.verify(mockConnectionContext).setOutboundQueueHandleRegistration(
                Mockito.<OutboundQueueHandlerRegistration<OutboundQueueProvider>>any());
        order.verify(mockConnectionContext).getNodeId();
        order.verify(mockConnectionContext).setDeviceDisconnectedHandler(any(DeviceContext.class));

        Mockito.verify(deviceInitPhaseHandler).onDeviceContextLevelUp(Matchers.<DeviceContext>any());
    }

    @Ignore
    @Test
    public void deviceConnectedV10Test() {
        final DeviceManagerImpl deviceManager = prepareDeviceManager();
        injectMockTranslatorLibrary(deviceManager);
        final ConnectionContext mockConnectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_0);

        final PhyPortBuilder phyPort = new PhyPortBuilder()
                .setPortNo(41L);
        when(mockFeatures.getPhyPort()).thenReturn(Collections.singletonList(phyPort.build()));
        final MessageTranslator<Object, Object> mockedTranslator = Mockito.mock(MessageTranslator.class);
        when(mockedTranslator.translate(Matchers.<Object>any(), Matchers.<DeviceContext>any(), Matchers.any()))
                .thenReturn(null);
        when(translatorLibrary.lookupTranslator(Matchers.<TranslatorKey>any())).thenReturn(mockedTranslator);

        deviceManager.deviceConnected(mockConnectionContext);

        final InOrder order = inOrder(mockConnectionContext);
        order.verify(mockConnectionContext).getFeatures();
        order.verify(mockConnectionContext).setOutboundQueueProvider(any(OutboundQueueProvider.class));
        order.verify(mockConnectionContext).setOutboundQueueHandleRegistration(
                Mockito.<OutboundQueueHandlerRegistration<OutboundQueueProvider>>any());
        order.verify(mockConnectionContext).getNodeId();
        order.verify(mockConnectionContext).setDeviceDisconnectedHandler(any(DeviceContext.class));

        Mockito.verify(deviceInitPhaseHandler).onDeviceContextLevelUp(Matchers.<DeviceContext>any());
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
    public void chainTableTrunkWriteOF10Test() {
        final DeviceState mockedDeviceState = mock(DeviceState.class);

        final GetFeaturesOutput mockedFeatures = mock(GetFeaturesOutput.class);
        when(mockedFeatures.getTables()).thenReturn((short) 2);
        when(mockedDeviceState.getFeatures()).thenReturn(mockedFeatures);

        when(mockedDeviceState.getNodeInstanceIdentifier()).thenReturn(DUMMY_NODE_II);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        final RpcResult<List<MultipartReply>> mockedRpcResult = mock(RpcResult.class);
        when(mockedRpcResult.isSuccessful()).thenReturn(true);
        final List<RpcResult<List<MultipartReply>>> data = new ArrayList<RpcResult<List<MultipartReply>>>();
        data.add(mockedRpcResult);
        data.add(mockedRpcResult);

//        DeviceManagerImpl.chainTableTrunkWriteOF10(mockedDeviceContext, Futures.immediateFuture(data));
//        verify(mockedDeviceContext, times(3))
//                .writeToTransaction(any(LogicalDatastoreType.class), any(InstanceIdentifier.class), any(FlowCapableNode.class));
    }

    @Test
    public void testTranslateAndWriteReplyTypeDesc() {
        final ConnectionContext connectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);
        Mockito.when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        final DeviceState deviceState = Mockito.mock(DeviceState.class);
        Mockito.when(mockedDeviceContext.getDeviceState()).thenReturn(deviceState);

        final Collection<MultipartReply> multipartReplyMessages = prepareDataforTypeDesc(mockedDeviceContext);

//        DeviceManagerImpl.translateAndWriteReply(MultipartType.OFPMPDESC, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
//        verify(mockedDeviceContext)
//                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class)), any(FlowCapableNode.class));
    }

    private Collection<MultipartReply> prepareDataforTypeDesc(final DeviceContext mockedDeviceContext) {
        final MultipartReplyDesc multipartReplyDesc = new MultipartReplyDescBuilder().build();

        final MultipartReplyDescCaseBuilder multipartReplyDescCaseBuilder = new MultipartReplyDescCaseBuilder();
        multipartReplyDescCaseBuilder.setMultipartReplyDesc(multipartReplyDesc);

        final MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyDescCaseBuilder.build()).build();
        return Collections.<MultipartReply>singleton(multipartReplyMessage);

    }

    @Test
    public void translateAndWriteReplyTypeTableFeatures() {
        final TableFeaturesBuilder tableFeature = new TableFeaturesBuilder();
        tableFeature.setTableId(DUMMY_TABLE_ID);
        final List<TableFeatures> tableFeatures = new ArrayList<>();
        tableFeatures.add(tableFeature.build());

        final MultipartReplyTableFeatures multipartReplyTableFeatures = new MultipartReplyTableFeaturesBuilder().setTableFeatures(tableFeatures).build();
        final MultipartReplyTableFeaturesCaseBuilder multipartReplyTableFeaturesCaseBuilder = new MultipartReplyTableFeaturesCaseBuilder();
        multipartReplyTableFeaturesCaseBuilder.setMultipartReplyTableFeatures(multipartReplyTableFeatures);

        final MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyTableFeaturesCaseBuilder.build()).build();
        final Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);
//        DeviceManagerImpl.translateAndWriteReply(MultipartType.OFPMPTABLEFEATURES, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
//        verify(mockedDeviceContext)
//                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
//                        eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(DUMMY_TABLE_ID))), any(Table.class));

    }

    @Test
    public void translateAndWriteReplyTypeMeterFeatures() {
        final DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        final MultipartReplyMeterFeaturesBuilder multipartReplyMeterFeaturesBuilder = new MultipartReplyMeterFeaturesBuilder();
        multipartReplyMeterFeaturesBuilder.setBandTypes(new MeterBandTypeBitmap(true, true));
        multipartReplyMeterFeaturesBuilder.setCapabilities(new MeterFlags(true, true, true, true));
        multipartReplyMeterFeaturesBuilder.setMaxMeter(DUMMY_MAX_METER);

        final MultipartReplyMeterFeaturesCaseBuilder multipartReplyMeterFeaturesCaseBuilder = new MultipartReplyMeterFeaturesCaseBuilder();
        multipartReplyMeterFeaturesCaseBuilder.setMultipartReplyMeterFeatures(multipartReplyMeterFeaturesBuilder.build());

        final MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyMeterFeaturesCaseBuilder.build()).build();
        final Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);
//        DeviceManagerImpl.translateAndWriteReply(MultipartType.OFPMPMETERFEATURES, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
//        verify(mockedDeviceContext)
//                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(NodeMeterFeatures.class)), any(NodeMeterFeatures.class));
//        verify(mockedDeviceState).setMeterAvailable(eq(true));
    }

    @Test
    public void translateAndWriteReplyTypeGroupFeatures() {
        final MultipartReplyGroupFeaturesBuilder multipartReplyGroupFeaturesBuilder = new MultipartReplyGroupFeaturesBuilder();
        multipartReplyGroupFeaturesBuilder.setTypes(new GroupTypes(true, true, true, true));
        multipartReplyGroupFeaturesBuilder.setCapabilities(new GroupCapabilities(true, true, true, true));
        final ActionType actionType = new ActionType(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true);
        multipartReplyGroupFeaturesBuilder.setActionsBitmap(Lists.newArrayList(actionType));

        final MultipartReplyGroupFeatures multipartReplyGroupFeatures = multipartReplyGroupFeaturesBuilder.build();

        final MultipartReplyGroupFeaturesCaseBuilder multipartReplyGroupFeaturesCaseBuilder = new MultipartReplyGroupFeaturesCaseBuilder();
        multipartReplyGroupFeaturesCaseBuilder.setMultipartReplyGroupFeatures(multipartReplyGroupFeatures);

        final MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyGroupFeaturesCaseBuilder.build()).build();
        final Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);

//        DeviceManagerImpl.translateAndWriteReply(MultipartType.OFPMPGROUPFEATURES, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
//        verify(mockedDeviceContext)
//                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(NodeGroupFeatures.class)), any(NodeGroupFeatures.class));
    }


    @Test
    public void translateAndWriteReplyTypePortDesc() {
        final ConnectionContext mockedPrimaryConnectionContext = mock(ConnectionContext.class);
        final FeaturesReply mockedFeatures = mock(FeaturesReply.class);
        when(mockedFeatures.getDatapathId()).thenReturn(new BigInteger(DUMMY_DATAPATH_ID));
        when(mockedPrimaryConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimaryConnectionContext);
        final DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        final MessageTranslator mockedTranslator = mock(MessageTranslator.class);
        when(translatorLibrary.lookupTranslator(any(TranslatorKey.class))).thenReturn(mockedTranslator);
        when(mockedDeviceContext.oook()).thenReturn(translatorLibrary);

        final MultipartReplyPortDescBuilder multipartReplyPortDescBuilder = new MultipartReplyPortDescBuilder();

        final PortsBuilder portsBuilder = new PortsBuilder();
        portsBuilder.setPortNo(DUMMY_PORT_NUMBER);

        multipartReplyPortDescBuilder.setPorts(Lists.newArrayList(portsBuilder.build()));

        final MultipartReplyPortDescCaseBuilder multipartReplyPortDescCaseBuilder = new MultipartReplyPortDescCaseBuilder();
        multipartReplyPortDescCaseBuilder.setMultipartReplyPortDesc(multipartReplyPortDescBuilder.build());

        final MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyPortDescCaseBuilder.build()).build();
        final Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);

        OpenflowPortsUtil.init();
//        DeviceManagerImpl.translateAndWriteReply(MultipartType.OFPMPPORTDESC, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
//        verify(mockedDeviceContext)
//                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), any(InstanceIdentifier.class), any(NodeConnector.class));
    }

    @Test
    public void createSuccessProcessingCallbackTest() {
        final DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        final ConnectionContext connectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);

        final List<MultipartReply> multipartReplies = new ArrayList<>(prepareDataforTypeDesc(mockedDeviceContext));
        final RpcResult<List<MultipartReply>> result = RpcResultBuilder.<List<MultipartReply>>success(multipartReplies).build();
        final ListenableFuture<RpcResult<List<MultipartReply>>> mockedRequestContextFuture = Futures.immediateFuture(result);

//        DeviceManagerImpl.createSuccessProcessingCallback(MultipartType.OFPMPDESC, mockedDeviceContext, DUMMY_NODE_II, mockedRequestContextFuture);
//        verify(mockedDeviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class)), any(FlowCapableNode.class));
//
//        final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder.<List<MultipartReply>>failed().withError(RpcError.ErrorType.PROTOCOL, "dummy error").build();
//        mockedRequestContextFuture = Futures.immediateFuture(rpcResult);
//        DeviceManagerImpl.createSuccessProcessingCallback(MultipartType.OFPMPDESC, mockedDeviceContext, DUMMY_NODE_II, mockedRequestContextFuture);
//        verify(mockedDeviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class)), any(FlowCapableNode.class));
    }

    @Test
    public void testClose() throws Exception {
        final DeviceContext deviceContext = Mockito.mock(DeviceContext.class);
        final DeviceManagerImpl deviceManager = prepareDeviceManager();
        final Set<DeviceContext> deviceContexts = getContextsCollection(deviceManager);
        deviceContexts.add(deviceContext);
        Assert.assertEquals(1, deviceContexts.size());

        deviceManager.close();

        Mockito.verify(deviceContext).close();
    }

    private static Set<DeviceContext> getContextsCollection(final DeviceManagerImpl deviceManager) throws NoSuchFieldException, IllegalAccessException {
        // HACK: contexts collection for testing shall be accessed in some more civilized way
        final Field contextsField = DeviceManagerImpl.class.getDeclaredField("deviceContexts");
        Assert.assertNotNull(contextsField);
        contextsField.setAccessible(true);
        return (Set<DeviceContext>) contextsField.get(deviceManager);
    }

}
