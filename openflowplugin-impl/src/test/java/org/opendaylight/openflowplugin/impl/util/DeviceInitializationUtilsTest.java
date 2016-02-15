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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
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
    @Mock
    private DeviceInitializationUtils deviceInitializationUtils;

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
    public void chainTableTrunkWriteOF10Test() {
        DeviceState mockedDeviceState = mock(DeviceState.class);

        GetFeaturesOutput mockedFeatures = mock(GetFeaturesOutput.class);
        when(mockedFeatures.getTables()).thenReturn((short) 2);
        when(mockedDeviceState.getFeatures()).thenReturn(mockedFeatures);

        when(mockedDeviceState.getNodeInstanceIdentifier()).thenReturn(DUMMY_NODE_II);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        RpcResult<List<MultipartReply>> mockedRpcResult = mock(RpcResult.class);
        when(mockedRpcResult.isSuccessful()).thenReturn(true);
        List<RpcResult<List<MultipartReply>>> data = new ArrayList<RpcResult<List<MultipartReply>>>();
        data.add(mockedRpcResult);
        data.add(mockedRpcResult);

        DeviceInitializationUtils.chainTableTrunkWriteOF10(mockedDeviceContext, Futures.immediateFuture(data));
        verify(mockedDeviceContext, times(3))
                .writeToTransaction(any(LogicalDatastoreType.class), any(InstanceIdentifier.class), any(FlowCapableNode.class));
    }

    @Test
    public void testTranslateAndWriteReplyTypeDesc() {
        final ConnectionContext connectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);
        Mockito.when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        DeviceState deviceState = Mockito.mock(DeviceState.class);
        Mockito.when(mockedDeviceContext.getDeviceState()).thenReturn(deviceState);

        Collection<MultipartReply> multipartReplyMessages = prepareDataforTypeDesc(mockedDeviceContext);

        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPDESC, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext)
                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class)), any(FlowCapableNode.class));
    }

    @Test
    public void translateAndWriteReplyTypeTableFeatures() {
        TableFeaturesBuilder tableFeature = new TableFeaturesBuilder();
        tableFeature.setTableId(DUMMY_TABLE_ID);
        List<TableFeatures> tableFeatures = new ArrayList<>();
        tableFeatures.add(tableFeature.build());

        MultipartReplyTableFeatures multipartReplyTableFeatures = new MultipartReplyTableFeaturesBuilder().setTableFeatures(tableFeatures).build();
        MultipartReplyTableFeaturesCaseBuilder multipartReplyTableFeaturesCaseBuilder = new MultipartReplyTableFeaturesCaseBuilder();
        multipartReplyTableFeaturesCaseBuilder.setMultipartReplyTableFeatures(multipartReplyTableFeatures);

        MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyTableFeaturesCaseBuilder.build()).build();
        Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);
        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPTABLEFEATURES, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext)
                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
                        eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(DUMMY_TABLE_ID))), any(Table.class));

    }

    @Test
    public void translateAndWriteReplyTypeMeterFeatures() {
        DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        MultipartReplyMeterFeaturesBuilder multipartReplyMeterFeaturesBuilder = new MultipartReplyMeterFeaturesBuilder();
        multipartReplyMeterFeaturesBuilder.setBandTypes(new MeterBandTypeBitmap(true, true));
        multipartReplyMeterFeaturesBuilder.setCapabilities(new MeterFlags(true, true, true, true));
        multipartReplyMeterFeaturesBuilder.setMaxMeter(DUMMY_MAX_METER);

        MultipartReplyMeterFeaturesCaseBuilder multipartReplyMeterFeaturesCaseBuilder = new MultipartReplyMeterFeaturesCaseBuilder();
        multipartReplyMeterFeaturesCaseBuilder.setMultipartReplyMeterFeatures(multipartReplyMeterFeaturesBuilder.build());

        MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyMeterFeaturesCaseBuilder.build()).build();
        Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);
        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPMETERFEATURES, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext)
                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(NodeMeterFeatures.class)), any(NodeMeterFeatures.class));
        verify(mockedDeviceState).setMeterAvailable(eq(true));
    }

    @Test
    public void translateAndWriteReplyTypeGroupFeatures() {
        MultipartReplyGroupFeaturesBuilder multipartReplyGroupFeaturesBuilder = new MultipartReplyGroupFeaturesBuilder();
        multipartReplyGroupFeaturesBuilder.setTypes(new GroupTypes(true, true, true, true));
        multipartReplyGroupFeaturesBuilder.setCapabilities(new GroupCapabilities(true, true, true, true));
        ActionType actionType = new ActionType(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true);
        multipartReplyGroupFeaturesBuilder.setActionsBitmap(Lists.newArrayList(actionType));

        MultipartReplyGroupFeatures multipartReplyGroupFeatures = multipartReplyGroupFeaturesBuilder.build();

        MultipartReplyGroupFeaturesCaseBuilder multipartReplyGroupFeaturesCaseBuilder = new MultipartReplyGroupFeaturesCaseBuilder();
        multipartReplyGroupFeaturesCaseBuilder.setMultipartReplyGroupFeatures(multipartReplyGroupFeatures);

        MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyGroupFeaturesCaseBuilder.build()).build();
        Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);

        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPGROUPFEATURES, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext)
                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(NodeGroupFeatures.class)), any(NodeGroupFeatures.class));
    }


    @Test
    public void translateAndWriteReplyTypePortDesc() {
        ConnectionContext mockedPrimaryConnectionContext = mock(ConnectionContext.class);
        FeaturesReply mockedFeatures = mock(FeaturesReply.class);
        when(mockedFeatures.getDatapathId()).thenReturn(new BigInteger(DUMMY_DATAPATH_ID));
        when(mockedPrimaryConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimaryConnectionContext);
        DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        MessageTranslator mockedTranslator = mock(MessageTranslator.class);
        when(translatorLibrary.lookupTranslator(any(TranslatorKey.class))).thenReturn(mockedTranslator);
        when(mockedDeviceContext.oook()).thenReturn(translatorLibrary);

        MultipartReplyPortDescBuilder multipartReplyPortDescBuilder = new MultipartReplyPortDescBuilder();

        PortsBuilder portsBuilder = new PortsBuilder();
        portsBuilder.setPortNo(DUMMY_PORT_NUMBER);

        multipartReplyPortDescBuilder.setPorts(Lists.newArrayList(portsBuilder.build()));

        MultipartReplyPortDescCaseBuilder multipartReplyPortDescCaseBuilder = new MultipartReplyPortDescCaseBuilder();
        multipartReplyPortDescCaseBuilder.setMultipartReplyPortDesc(multipartReplyPortDescBuilder.build());

        MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyPortDescCaseBuilder.build()).build();
        Set<MultipartReply> multipartReplyMessages = Collections.<MultipartReply>singleton(multipartReplyMessage);

        OpenflowPortsUtil.init();
        DeviceInitializationUtils.translateAndWriteReply(MultipartType.OFPMPPORTDESC, mockedDeviceContext, DUMMY_NODE_II, multipartReplyMessages);
        verify(mockedDeviceContext)
                .writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), any(InstanceIdentifier.class), any(NodeConnector.class));
    }

    @Test
    public void createSuccessProcessingCallbackTest() {
        DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        final ConnectionContext connectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);

        List<MultipartReply> multipartReplies = new ArrayList<>(prepareDataforTypeDesc(mockedDeviceContext));
        RpcResult<List<MultipartReply>> result = RpcResultBuilder.<List<MultipartReply>>success(multipartReplies).build();
        ListenableFuture<RpcResult<List<MultipartReply>>> mockedRequestContextFuture = Futures.immediateFuture(result);

        DeviceInitializationUtils.createSuccessProcessingCallback(MultipartType.OFPMPDESC, mockedDeviceContext, DUMMY_NODE_II, mockedRequestContextFuture);
        verify(mockedDeviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class)), any(FlowCapableNode.class));

        RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder.<List<MultipartReply>>failed().withError(RpcError.ErrorType.PROTOCOL, "dummy error").build();
        mockedRequestContextFuture = Futures.immediateFuture(rpcResult);
        DeviceInitializationUtils.createSuccessProcessingCallback(MultipartType.OFPMPDESC, mockedDeviceContext, DUMMY_NODE_II, mockedRequestContextFuture);
        verify(mockedDeviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), eq(DUMMY_NODE_II.augmentation(FlowCapableNode.class)), any(FlowCapableNode.class));
    }

    private Collection<MultipartReply> prepareDataforTypeDesc(final DeviceContext mockedDeviceContext) {
        MultipartReplyDesc multipartReplyDesc = new MultipartReplyDescBuilder().build();

        MultipartReplyDescCaseBuilder multipartReplyDescCaseBuilder = new MultipartReplyDescCaseBuilder();
        multipartReplyDescCaseBuilder.setMultipartReplyDesc(multipartReplyDesc);

        MultipartReplyMessage multipartReplyMessage = new MultipartReplyMessageBuilder().setMultipartReplyBody(multipartReplyDescCaseBuilder.build()).build();
        return Collections.<MultipartReply>singleton(multipartReplyMessage);

    }

    protected ConnectionContext buildMockConnectionContext(short ofpVersion) {
        when(mockFeatures.getVersion()).thenReturn(ofpVersion);
        when(outboundQueueProvider.reserveEntry()).thenReturn(43L);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
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
                    public OutboundQueueHandlerRegistration<OutboundQueueHandler> answer(InvocationOnMock invocation) throws Throwable {
                        OutboundQueueHandler handler = (OutboundQueueHandler) invocation.getArguments()[0];
                        handler.onConnectionQueueChanged(outboundQueueProvider);
                        return null;
                    }
                });

        when(mockConnectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        return mockConnectionContext;
    }



}
