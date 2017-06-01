/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerFlowMultipartRequestOnTheFlyCallback;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(MockitoJUnitRunner.class)
public class MultipartRequestOnTheFlyCallbackTest {

    private static final String DUMMY_NODE_ID = "dummyNodeId";
    private static final String DUMMY_EVENT_NAME = "dummy event name 1";
    private static final String DUMMY_DEVICE_ID = "dummy device id 1";
    private static final Long DUMMY_XID = 55L;
    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_PATH = KeyedInstanceIdentifier
            .create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("uf-node:123")));

    @Mock
    private DeviceContext mockedDeviceContext;
    @Mock
    private RequestContext<List<MultipartReply>> mockedRequestContext;
    @Mock
    private ConnectionContext mockedPrimaryConnection;
    @Mock
    private NodeId mockedNodeId;
    @Mock
    private FeaturesReply mockedFeaturesReply;
    @Mock
    private DeviceState mockedDeviceState;
    @Mock
    private DeviceInfo mockedDeviceInfo;
    @Mock
    private GetFeaturesOutput mocketGetFeaturesOutput;
    @Mock
    private DeviceFlowRegistry mockedFlowRegistry;
    @Mock
    private FlowDescriptor mockedFlowDescriptor;
    @Mock
    private ReadOnlyTransaction mockedReadOnlyTx;

    private AbstractRequestContext<List<MultipartReply>> dummyRequestContext;
    private final EventIdentifier dummyEventIdentifier = new EventIdentifier(DUMMY_EVENT_NAME, DUMMY_DEVICE_ID);
    private AbstractMultipartRequestOnTheFlyCallback<MultipartReply> multipartRequestOnTheFlyCallback;
    private final short tableId = 0;

    @Before
    public void initialization() {
        when(mockedDeviceContext.getMessageSpy()).thenReturn(new MessageIntelligenceAgencyImpl());
        when(mockedNodeId.toString()).thenReturn(DUMMY_NODE_ID);
        when(mockedPrimaryConnection.getNodeId()).thenReturn(mockedNodeId);
        when(mockedPrimaryConnection.getFeatures()).thenReturn(mockedFeaturesReply);
        when(mockedFeaturesReply.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(mockedFeaturesReply.getDatapathId()).thenReturn(BigInteger.valueOf(123L));

        when(mocketGetFeaturesOutput.getTables()).thenReturn(tableId);
        when(mocketGetFeaturesOutput.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(mocketGetFeaturesOutput.getDatapathId()).thenReturn(BigInteger.valueOf(123L));

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimaryConnection);
        when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(NODE_PATH);
        when(mockedDeviceInfo.getNodeId()).thenReturn(mockedNodeId);
        when(mockedDeviceInfo.getDatapathId()).thenReturn(BigInteger.TEN);
        when(mockedDeviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);

        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(mockedFlowRegistry);
        when(mockedFlowRegistry.retrieveDescriptor(Matchers.any(FlowRegistryKey.class))).thenReturn(mockedFlowDescriptor);

        final InstanceIdentifier<FlowCapableNode> nodePath = mockedDeviceInfo.getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);
        final FlowCapableNodeBuilder flowNodeBuilder = new FlowCapableNodeBuilder();
        flowNodeBuilder.setTable(Collections.<Table> emptyList());
        final Optional<FlowCapableNode> flowNodeOpt = Optional.of(flowNodeBuilder.build());
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> flowNodeFuture = Futures.immediateCheckedFuture(flowNodeOpt);
        when(mockedReadOnlyTx.read(LogicalDatastoreType.OPERATIONAL, nodePath)).thenReturn(flowNodeFuture);
        when(mockedDeviceContext.getReadTransaction()).thenReturn(mockedReadOnlyTx);

        dummyRequestContext = new AbstractRequestContext<List<MultipartReply>>(DUMMY_XID) {

            @Override
            public void close() {
                //NOOP
            }
        };

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        multipartRequestOnTheFlyCallback = new MultiLayerFlowMultipartRequestOnTheFlyCallback<>(
            dummyRequestContext,
            String.class,
            mockedDeviceContext,
            dummyEventIdentifier,
            MultipartWriterProviderFactory.createDefaultProvider(mockedDeviceContext),
            convertorManager);
    }


    @Test
    public void testOnSuccessWithNull() throws Exception {
        multipartRequestOnTheFlyCallback.onSuccess(null);
        final RpcResult<List<MultipartReply>> expectedRpcResult = RpcResultBuilder.success(Collections.<MultipartReply>emptyList()).build();
        final RpcResult<List<MultipartReply>> actualResult = dummyRequestContext.getFuture().get();
        assertEquals(expectedRpcResult.getErrors(), actualResult.getErrors());
        assertEquals(expectedRpcResult.getResult(), actualResult.getResult());
        assertEquals(expectedRpcResult.isSuccessful(), actualResult.isSuccessful());
    }

    @Test
    public void testOnSuccessWithNotMultiNoMultipart() throws Exception {
        final HelloMessage mockedHelloMessage = mock(HelloMessage.class);
        multipartRequestOnTheFlyCallback.onSuccess(mockedHelloMessage);

        final RpcResult<List<MultipartReply>> expectedRpcResult =
                RpcResultBuilder.<List<MultipartReply>>failed().withError(RpcError.ErrorType.APPLICATION,
                        String.format("Unexpected response type received: %s.", mockedHelloMessage.getClass())).build();
        final RpcResult<List<MultipartReply>> actualResult = dummyRequestContext.getFuture().get();
        assertNotNull(actualResult.getErrors());
        assertEquals(1, actualResult.getErrors().size());

        final RpcError actualError = actualResult.getErrors().iterator().next();
        assertEquals(actualError.getMessage(), String.format("Unexpected response type received: %s.", mockedHelloMessage.getClass()));
        assertEquals(actualError.getErrorType(),RpcError.ErrorType.APPLICATION);
        assertEquals(expectedRpcResult.getResult(), actualResult.getResult());
        assertEquals(expectedRpcResult.isSuccessful(), actualResult.isSuccessful());

        Mockito.verify(mockedDeviceContext, Mockito.never()).writeToTransaction(Matchers.eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.<InstanceIdentifier>any(), Matchers.<DataObject>any());
        Mockito.verify(mockedDeviceContext).submitTransaction();
    }

    /**
     * not the last reply
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testOnSuccessWithValidMultipart1() throws Exception {
        final MatchBuilder matchBuilder = new MatchBuilder()
                .setMatchEntry(Collections.<MatchEntry>emptyList());
        final FlowStatsBuilder flowStatsBuilder = new FlowStatsBuilder()
                .setTableId(tableId)
                .setPriority(2)
                .setCookie(BigInteger.ZERO)
                .setByteCount(BigInteger.TEN)
                .setPacketCount(BigInteger.ONE)
                .setDurationSec(11L)
                .setDurationNsec(12L)
                .setMatch(matchBuilder.build())
                .setFlags(new FlowModFlags(true, false, false, false, false));
        final MultipartReplyFlowBuilder multipartReplyFlowBuilder = new MultipartReplyFlowBuilder()
                .setFlowStats(Collections.singletonList(flowStatsBuilder.build()));
        final MultipartReplyFlowCaseBuilder multipartReplyFlowCaseBuilder = new MultipartReplyFlowCaseBuilder()
                .setMultipartReplyFlow(multipartReplyFlowBuilder.build());
        final MultipartReplyMessageBuilder mpReplyMessage = new MultipartReplyMessageBuilder()
                .setType(MultipartType.OFPMPFLOW)
                .setFlags(new MultipartRequestFlags(true))
                .setMultipartReplyBody(multipartReplyFlowCaseBuilder.build())
                .setXid(21L);

        final InstanceIdentifier<FlowCapableNode> nodePath = mockedDeviceInfo.getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class);
        final FlowCapableNodeBuilder flowNodeBuilder = new FlowCapableNodeBuilder();
        final TableBuilder tableDataBld = new TableBuilder();
        tableDataBld.setId(tableId);
        flowNodeBuilder.setTable(Collections.singletonList(tableDataBld.build()));
        final Optional<FlowCapableNode> flowNodeOpt = Optional.of(flowNodeBuilder.build());
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> flowNodeFuture = Futures
                .immediateCheckedFuture(flowNodeOpt);
        when(mockedReadOnlyTx.read(LogicalDatastoreType.OPERATIONAL, nodePath)).thenReturn(flowNodeFuture);
        when(mockedDeviceContext.getReadTransaction()).thenReturn(mockedReadOnlyTx);

        multipartRequestOnTheFlyCallback.onSuccess(mpReplyMessage.build());

        verify(mockedReadOnlyTx, times(0)).read(LogicalDatastoreType.OPERATIONAL, nodePath);
        verify(mockedReadOnlyTx, times(0)).close();
        verify(mockedDeviceContext, times(1)).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.any(), Matchers.any());
    }

    /**
     * the last reply
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testOnSuccessWithValidMultipart2() throws Exception {
        final MultipartReplyMessageBuilder mpReplyMessage = new MultipartReplyMessageBuilder()
                .setType(MultipartType.OFPMPDESC)
                .setFlags(new MultipartRequestFlags(false));

        multipartRequestOnTheFlyCallback.onSuccess(mpReplyMessage.build());

        final RpcResult<List<MultipartReply>> actualResult = dummyRequestContext.getFuture().get();

        // Nothing else than flow is supported by on the fly callback
        assertNotNull(actualResult.getErrors());
        assertFalse(actualResult.getErrors().isEmpty());
        Mockito.verify(mockedFlowRegistry, Mockito.never()).store(Matchers.any());
        Mockito.verify(mockedDeviceContext, Mockito.never()).writeToTransaction(Matchers.eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.<InstanceIdentifier>any(), Matchers.<DataObject>any());
    }
}
