/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiAddFlow;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiRemoveFlow;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiUpdateFlow;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SalFlowServiceImplTest {
    private static final Uint64 DUMMY_DATAPATH_ID = Uint64.valueOf(444);
    private static final String DUMMY_NODE_ID = "dummyNodeID";
    private static final String DUMMY_FLOW_ID = "dummyFlowID";
    private static final Uint8 DUMMY_TABLE_ID = Uint8.ZERO;

    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_II =
        InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(DUMMY_NODE_ID)));

    private static final KeyedInstanceIdentifier<Table, TableKey> TABLE_II =
        NODE_II.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(DUMMY_TABLE_ID));

    private final NodeRef noderef = new NodeRef(NODE_II);

    @Mock
    private RequestContextStack mockedRequestContextStack;
    @Mock
    private DeviceContext mockedDeviceContext;
    @Mock
    private ConnectionContext mockedPrimConnectionContext;
    @Mock
    private FeaturesReply mockedFeatures;
    @Mock
    private ConnectionAdapter mockedConnectionAdapter;
    @Mock
    private MessageSpy mockedMessagSpy;
    @Mock
    private RequestContext<Object> requestContext;
    @Mock
    private OutboundQueue outboundQueue;
    @Mock
    private Match match;

    @Mock
    private DeviceState mockedDeviceState;
    @Mock
    private DeviceInfo mockedDeviceInfo;
    @Mock
    private DeviceFlowRegistry deviceFlowRegistry;
    @Mock
    private FlowRegistryKey flowKey;
    @Mock
    private GetFeaturesOutput mockedFeaturesOutput;

    @Before
    public void initialization() {

        when(mockedPrimConnectionContext.getOutboundQueueProvider()).thenReturn(outboundQueue);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(deviceFlowRegistry);

        when(requestContext.getXid()).thenReturn(new Xid(Uint32.valueOf(84L)));
        when(requestContext.getFuture()).thenReturn(RpcResultBuilder.success().buildFuture());
        when(mockedRequestContextStack.createRequestContext()).thenReturn(requestContext);

        when(mockedDeviceInfo.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);

        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);
    }

    private AddFlow mockAddFlow(final Uint8 version) {
        when(mockedFeatures.getVersion()).thenReturn(version);
        when(mockedFeaturesOutput.getVersion()).thenReturn(version);
        when(mockedDeviceInfo.getVersion()).thenReturn(version);

        return new MultiAddFlow(mockedRequestContextStack, mockedDeviceContext,
            ConvertorManagerFactory.createDefaultManager());
    }

    private RemoveFlow mockRemoveFlow(final Uint8 version) {
        when(mockedFeatures.getVersion()).thenReturn(version);
        when(mockedFeaturesOutput.getVersion()).thenReturn(version);
        when(mockedDeviceInfo.getVersion()).thenReturn(version);

        return new MultiRemoveFlow(mockedRequestContextStack, mockedDeviceContext,
            ConvertorManagerFactory.createDefaultManager());
    }

    private UpdateFlow mockUpdateFlow(final Uint8 version) {
        when(mockedFeatures.getVersion()).thenReturn(version);
        when(mockedFeaturesOutput.getVersion()).thenReturn(version);
        when(mockedDeviceInfo.getVersion()).thenReturn(version);

        return new MultiUpdateFlow(mockedRequestContextStack, mockedDeviceContext,
            ConvertorManagerFactory.createDefaultManager());
    }

    @Test
    public void testAddFlow() throws Exception {
        when(deviceFlowRegistry.createKey(any())).thenReturn(flowKey);
        addFlow(OFConstants.OFP_VERSION_1_0);
        addFlow(OFConstants.OFP_VERSION_1_3);
    }

    @Test
    public void testAddFlowFailCallback() throws Exception {
        addFlowFailCallback(OFConstants.OFP_VERSION_1_0);
    }

    @Test
    public void testAddFlowFailCallback1() throws Exception {
        addFlowFailCallback(OFConstants.OFP_VERSION_1_3);
    }

    private void addFlowFailCallback(final Uint8 version) throws Exception {
        AddFlowInput mockedAddFlowInput = new AddFlowInputBuilder()
                .setMatch(match)
                .setTableId(Uint8.ONE)
                .setNode(noderef)
                .build();

        doReturn(Futures.<RequestContext<Object>>immediateFailedFuture(new Exception("ut-failed-response")))
                .when(requestContext).getFuture();
        when(deviceFlowRegistry.createKey(any())).thenReturn(flowKey);

        final var rpcResultFuture = mockAddFlow(version).invoke(mockedAddFlowInput);

        assertNotNull(rpcResultFuture);
        final var addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertFalse(addFlowOutputRpcResult.isSuccessful());
    }

    @Test
    public void testRemoveFlowFailCallback() throws Exception {
        removeFlowFailCallback(OFConstants.OFP_VERSION_1_0);
    }

    @Test
    public void testRemoveFlowFailCallback1() throws Exception {
        removeFlowFailCallback(OFConstants.OFP_VERSION_1_3);
    }

    private void removeFlowFailCallback(final Uint8 version) throws Exception {
        RemoveFlowInput mockedRemoveFlowInput = new RemoveFlowInputBuilder()
                .setTableId(Uint8.ONE)
                .setMatch(match)
                .setNode(noderef)
                .build();

        doReturn(Futures.<RequestContext<Object>>immediateFailedFuture(new Exception("ut-failed-response")))
                .when(requestContext).getFuture();

        final var rpcResultFuture = mockRemoveFlow(version).invoke(mockedRemoveFlowInput);

        assertNotNull(rpcResultFuture);
        final var removeFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(removeFlowOutputRpcResult);
        assertFalse(removeFlowOutputRpcResult.isSuccessful());
    }

    @Test
    public void testAddFlowWithItemLifecycle() throws Exception {
        when(deviceFlowRegistry.createKey(any())).thenReturn(flowKey);
        addFlow(OFConstants.OFP_VERSION_1_0);
        addFlow(OFConstants.OFP_VERSION_1_3);
    }

    private void addFlow(final Uint8 version) throws Exception {

        AddFlowInput mockedAddFlowInput = new AddFlowInputBuilder()
                .setMatch(match)
                .setTableId(Uint8.ONE)
                .setNode(noderef)
                .build();
        var addFlow = mockAddFlow(version);

        verifyOutput(addFlow.invoke(mockedAddFlowInput));
    }

    @Test
    public void testRemoveFlow() throws Exception {
        removeFlow(OFConstants.OFP_VERSION_1_0);
        removeFlow(OFConstants.OFP_VERSION_1_3);
    }

    @Test
    public void testRemoveFlowWithItemLifecycle() throws Exception {
        removeFlow(OFConstants.OFP_VERSION_1_0);
        removeFlow(OFConstants.OFP_VERSION_1_3);
    }

    private void removeFlow(final Uint8 version) throws Exception {
        RemoveFlowInput mockedRemoveFlowInput = new RemoveFlowInputBuilder()
                .setMatch(match)
                .setTableId(Uint8.ONE)
                .setNode(noderef)
                .build();

        final var removeFlow = mockRemoveFlow(version);
        verifyOutput(removeFlow.invoke(mockedRemoveFlowInput));
    }

    @Test
    public void testUpdateFlow() throws Exception {
        updateFlow(OFConstants.OFP_VERSION_1_0);
        updateFlow(OFConstants.OFP_VERSION_1_3);
    }

    @Test
    public void testUpdateFlowWithItemLifecycle() throws Exception {
        updateFlow(OFConstants.OFP_VERSION_1_0);
        updateFlow(OFConstants.OFP_VERSION_1_3);
    }

    private void updateFlow(final Uint8 version) throws Exception {
        UpdateFlowInput mockedUpdateFlowInput = mock(UpdateFlowInput.class);
        UpdateFlowInput mockedUpdateFlowInput1 = mock(UpdateFlowInput.class);

        UpdatedFlow mockedUpdateFlow = new UpdatedFlowBuilder()
                .setMatch(match)
                .setTableId(Uint8.ONE)
                .build();

        UpdatedFlow mockedUpdateFlow1 = new UpdatedFlowBuilder()
                .setMatch(match)
                .setTableId(Uint8.ONE)
                .setPriority(Uint16.ONE)
                .build();

        when(mockedUpdateFlowInput.getUpdatedFlow()).thenReturn(mockedUpdateFlow);
        when(mockedUpdateFlowInput1.getUpdatedFlow()).thenReturn(mockedUpdateFlow1);

        FlowRef mockedFlowRef = mock(FlowRef.class);
        doReturn(TABLE_II.child(Flow.class,
                         new FlowKey(new FlowId(DUMMY_FLOW_ID)))).when(mockedFlowRef).getValue();
        when(mockedUpdateFlowInput.getFlowRef()).thenReturn(mockedFlowRef);
        when(mockedUpdateFlowInput1.getFlowRef()).thenReturn(mockedFlowRef);

        OriginalFlow mockedOriginalFlow = new OriginalFlowBuilder()
                .setMatch(match)
                .setTableId(Uint8.ONE)
                .build();

        OriginalFlow mockedOriginalFlow1 = new OriginalFlowBuilder()
                .setMatch(match)
                .setTableId(Uint8.ONE)
                .setPriority(Uint16.TWO)
                .build();

        when(mockedUpdateFlowInput.getOriginalFlow()).thenReturn(mockedOriginalFlow);
        when(mockedUpdateFlowInput1.getOriginalFlow()).thenReturn(mockedOriginalFlow1);

        final var updateFlow = mockUpdateFlow(version);
        verifyOutput(updateFlow.invoke(mockedUpdateFlowInput));
        verifyOutput(updateFlow.invoke(mockedUpdateFlowInput1));
    }

    private static <T extends DataObject> void verifyOutput(final ListenableFuture<RpcResult<T>> rpcResultFuture)
            throws Exception {
        assertNotNull(rpcResultFuture);
        final var addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertTrue(addFlowOutputRpcResult.isSuccessful());
    }
}
