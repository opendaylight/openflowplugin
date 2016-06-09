/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandler;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.impl.device.DeviceContextImpl;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandTypeBitmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInitializationUtilsTest {

    public static final String DUMMY_NODE_ID = "dummyNodeId";
    private static final KeyedInstanceIdentifier<Node, NodeKey> DUMMY_NODE_II = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(DUMMY_NODE_ID)));
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
    @Mock
    private DeviceInfo mockedDeviceInfo;
    @Mock
    private DeviceInitializationUtils deviceInitializationUtils;
    @Mock
    private DeviceInfo deviceInfo;

    @Before
    public void setUp() throws Exception {
        OpenflowPortsUtil.init();

        when(mockConnectionContext.getNodeId()).thenReturn(new NodeId(DUMMY_NODE_ID));
        when(mockConnectionContext.getFeatures()).thenReturn(mockFeatures);
        when(mockConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockConnectionContext);
        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);

        final Capabilities capabilitiesV13 = mock(Capabilities.class);
        final CapabilitiesV10 capabilitiesV10 = mock(CapabilitiesV10.class);
        when(mockFeatures.getCapabilities()).thenReturn(capabilitiesV13);
        when(mockFeatures.getCapabilitiesV10()).thenReturn(capabilitiesV10);
        when(mockFeatures.getDatapathId()).thenReturn(BigInteger.valueOf(21L));
    }

    @Test
    public void initializeNodeInformationTest() throws Exception {
        DeviceState mockedDeviceState = mock(DeviceState.class);
        MultiMsgCollector msgCollector = mock(MultiMsgCollector.class);
        TranslatorLibrary tLibrary = mock(TranslatorLibrary.class);

        GetFeaturesOutput mockedFeatures = mock(GetFeaturesOutput.class);
        when(mockedFeatures.getTables()).thenReturn((short) 2);
        when(mockedDeviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0);

        when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(DUMMY_NODE_II);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getMultiMsgCollector(Mockito.any(RequestContext.class))).thenReturn(msgCollector);
        when(mockedDeviceContext.oook()).thenReturn(tLibrary);

        final ConnectionContext connectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_0);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);

        DeviceInitializationUtils.initializeNodeInformation(mockedDeviceContext, true);

        verify(mockFeatures, atLeastOnce()).getPhyPort();
        verify(tLibrary, atLeastOnce()).lookupTranslator(any(TranslatorKey.class));
    }

    @Test
    public void chainTableTrunkWriteOF10Test() throws Exception {
        DeviceState mockedDeviceState = mock(DeviceState.class);

        final GetFeaturesOutput mockedFeatures = mock(GetFeaturesOutput.class);
        when(mockedFeatures.getTables()).thenReturn((short) 2);
        when(mockConnectionContext.getFeatures()).thenReturn(mockedFeatures);

        when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(DUMMY_NODE_II);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        final RpcResult<List<MultipartReply>> mockedRpcResult = mock(RpcResult.class);
        when(mockedRpcResult.isSuccessful()).thenReturn(true);
        final List<RpcResult<List<MultipartReply>>> data = new ArrayList<RpcResult<List<MultipartReply>>>();
        data.add(mockedRpcResult);
        data.add(mockedRpcResult);

        DeviceInitializationUtils.chainTableTrunkWriteOF10(mockedDeviceContext, Futures.immediateFuture(data));
        verify(mockedDeviceContext, times(3)).writeToTransaction(any(LogicalDatastoreType.class),
                Matchers.<InstanceIdentifier<FlowCapableNode>> any(), any(FlowCapableNode.class));
    }

    @Test
    public void testTranslateAndWriteReplyTypeDesc() throws Exception {
        final ConnectionContext connectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        final DeviceState deviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(deviceState);

        final Collection<MultipartReply> multipartReplyMessages = prepareDataforTypeDesc(mockedDeviceContext);

        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPDESC, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext)
                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class)), any(FlowCapableNode.class));
    }

    @Test
    public void translateAndWriteReplyTypeTableFeatures() throws Exception {
        TableFeaturesBuilder tableFeature = new TableFeaturesBuilder();
        tableFeature.setTableId(DUMMY_TABLE_ID);
        final List<TableFeatures> tableFeatures = new ArrayList<>();
        tableFeatures.add(tableFeature.build());

        final MultipartReplyTableFeatures multipartReplyTableFeatures = new MultipartReplyTableFeaturesBuilder().setTableFeatures(tableFeatures).build();
        final MultipartReplyTableFeaturesCaseBuilder multipartReplyTableFeaturesCaseBuilder = new MultipartReplyTableFeaturesCaseBuilder();
        multipartReplyTableFeaturesCaseBuilder.setMultipartReplyTableFeatures(multipartReplyTableFeatures);

        final MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyTableFeaturesCaseBuilder.build()).build();
        final Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);
        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPTABLEFEATURES, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext)
                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
                        eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(DUMMY_TABLE_ID))), any(Table.class));

    }

    @Test
    public void translateAndWriteReplyTypeMeterFeatures() throws Exception {
        DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        final MultipartReplyMeterFeaturesBuilder multipartReplyMeterFeaturesBuilder = new MultipartReplyMeterFeaturesBuilder();
        multipartReplyMeterFeaturesBuilder.setBandTypes(new MeterBandTypeBitmap(true, true));
        multipartReplyMeterFeaturesBuilder.setCapabilities(new MeterFlags(true, true, true, true));
        multipartReplyMeterFeaturesBuilder.setMaxMeter(DUMMY_MAX_METER);

        final MultipartReplyMeterFeaturesCaseBuilder multipartReplyMeterFeaturesCaseBuilder = new MultipartReplyMeterFeaturesCaseBuilder();
        multipartReplyMeterFeaturesCaseBuilder.setMultipartReplyMeterFeatures(multipartReplyMeterFeaturesBuilder.build());

        final MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyMeterFeaturesCaseBuilder.build()).build();
        final Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);
        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPMETERFEATURES, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext)
                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(NodeMeterFeatures.class)), any(NodeMeterFeatures.class));
        verify(mockedDeviceState).setMeterAvailable(eq(true));
    }

    @Test
    public void translateAndWriteReplyTypeGroupFeatures() throws Exception {
        MultipartReplyGroupFeaturesBuilder multipartReplyGroupFeaturesBuilder = new MultipartReplyGroupFeaturesBuilder();
        multipartReplyGroupFeaturesBuilder.setTypes(new GroupTypes(true, true, true, true));
        multipartReplyGroupFeaturesBuilder.setCapabilities(new GroupCapabilities(true, true, true, true));
        final ActionType actionType = new ActionType(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true);
        multipartReplyGroupFeaturesBuilder.setActionsBitmap(Lists.newArrayList(actionType));

        final MultipartReplyGroupFeatures multipartReplyGroupFeatures = multipartReplyGroupFeaturesBuilder.build();

        final MultipartReplyGroupFeaturesCaseBuilder multipartReplyGroupFeaturesCaseBuilder = new MultipartReplyGroupFeaturesCaseBuilder();
        multipartReplyGroupFeaturesCaseBuilder.setMultipartReplyGroupFeatures(multipartReplyGroupFeatures);

        final MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyGroupFeaturesCaseBuilder.build()).build();
        final Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);

        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPGROUPFEATURES, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext)
                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(NodeGroupFeatures.class)), any(NodeGroupFeatures.class));
    }


    @Test
    public void translateAndWriteReplyTypePortDesc() throws Exception {
        ConnectionContext mockedPrimaryConnectionContext = mock(ConnectionContext.class);
        FeaturesReply mockedFeatures = mock(FeaturesReply.class);
        when(mockedFeatures.getDatapathId()).thenReturn(new BigInteger(DUMMY_DATAPATH_ID));
        when(mockedPrimaryConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimaryConnectionContext);
        final DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0);
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
        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPPORTDESC, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.<InstanceIdentifier<NodeConnector>> any(), any(NodeConnector.class));
    }

    @Test
    public void createSuccessProcessingCallbackTest() throws Exception {
        DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        final ConnectionContext connectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);

        final List<MultipartReply> multipartReplies = new ArrayList<>(prepareDataforTypeDesc(mockedDeviceContext));
        final RpcResult<List<MultipartReply>> result = RpcResultBuilder.<List<MultipartReply>>success(multipartReplies).build();
        ListenableFuture<RpcResult<List<MultipartReply>>> mockedRequestContextFuture = Futures.immediateFuture(result);

        DeviceInitializationUtils.createSuccessProcessingCallback(MultipartType.OFPMPDESC, mockedDeviceContext, DUMMY_NODE_II, mockedRequestContextFuture);
        verify(mockedDeviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class)), any(FlowCapableNode.class));

        final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder.<List<MultipartReply>>failed().withError(RpcError.ErrorType.PROTOCOL, "dummy error").build();
        mockedRequestContextFuture = Futures.immediateFuture(rpcResult);
        DeviceInitializationUtils.createSuccessProcessingCallback(MultipartType.OFPMPDESC, mockedDeviceContext, DUMMY_NODE_II, mockedRequestContextFuture);
        verify(mockedDeviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class)), any(FlowCapableNode.class));
    }

    private Collection<MultipartReply> prepareDataforTypeDesc(final DeviceContext mockedDeviceContext) {
        final MultipartReplyDesc multipartReplyDesc = new MultipartReplyDescBuilder().build();

        final MultipartReplyDescCaseBuilder multipartReplyDescCaseBuilder = new MultipartReplyDescCaseBuilder();
        multipartReplyDescCaseBuilder.setMultipartReplyDesc(multipartReplyDesc);

        final MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyDescCaseBuilder.build()).build();
        return Collections.<MultipartReply>singleton(multipartReplyMessage);

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
}
