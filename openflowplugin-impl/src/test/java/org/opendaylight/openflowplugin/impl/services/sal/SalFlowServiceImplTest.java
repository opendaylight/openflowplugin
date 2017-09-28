/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
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
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(MockitoJUnitRunner.class)
public class SalFlowServiceImplTest extends TestCase {

    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("444");
    private static final String DUMMY_NODE_ID = "dummyNodeID";
    private static final String DUMMY_FLOW_ID = "dummyFlowID";
    private static final Short DUMMY_TABLE_ID = (short) 0;

    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_II
            = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(DUMMY_NODE_ID)));

    private static final KeyedInstanceIdentifier<Table, TableKey> TABLE_II
            = NODE_II.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(DUMMY_TABLE_ID));

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
    private GetFeaturesOutput mockedFeaturesOutput;

    @Before
    public void initialization() {
        when(mockedFeatures.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeaturesOutput.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);

        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedPrimConnectionContext.getOutboundQueueProvider()).thenReturn(outboundQueue);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(deviceFlowRegistry);

        when(requestContext.getXid()).thenReturn(new Xid(84L));
        when(requestContext.getFuture()).thenReturn(RpcResultBuilder.success().buildFuture());
        when(mockedRequestContextStack.createRequestContext()).thenReturn(requestContext);

        when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(NODE_II);
        when(mockedDeviceInfo.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);

        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);
    }

    private SalFlowServiceImpl mockSalFlowService(final short version) {
        when(mockedFeatures.getVersion()).thenReturn(version);
        when(mockedFeaturesOutput.getVersion()).thenReturn(version);
        when(mockedDeviceInfo.getVersion()).thenReturn(version);

        if (OFConstants.OFP_VERSION_1_3 >= version) {
            when(mockedDeviceContext.canUseSingleLayerSerialization()).thenReturn(true);
        }

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        return new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
    }

    @Test
    public void testAddFlow() throws Exception {
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

    private void addFlowFailCallback(short version) throws InterruptedException, ExecutionException {
        AddFlowInput mockedAddFlowInput = new AddFlowInputBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();

        Mockito.doReturn(Futures.<RequestContext<Object>>immediateFailedFuture(new Exception("ut-failed-response")))
                .when(requestContext).getFuture();

        mockingFlowRegistryLookup();
        final Future<RpcResult<AddFlowOutput>> rpcResultFuture = mockSalFlowService(version).addFlow(mockedAddFlowInput);

        assertNotNull(rpcResultFuture);
        final RpcResult<?> addFlowOutputRpcResult = rpcResultFuture.get();
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

    private void removeFlowFailCallback(short version) throws InterruptedException, ExecutionException {
        RemoveFlowInput mockedRemoveFlowInput = new RemoveFlowInputBuilder()
                .setTableId((short)1)
                .setMatch(match)
                .build();

        Mockito.doReturn(Futures.<RequestContext<Object>>immediateFailedFuture(new Exception("ut-failed-response")))
                .when(requestContext).getFuture();

        final Future<RpcResult<RemoveFlowOutput>> rpcResultFuture = mockSalFlowService(version).removeFlow(mockedRemoveFlowInput);

        assertNotNull(rpcResultFuture);
        final RpcResult<?> removeFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(removeFlowOutputRpcResult);
        assertFalse(removeFlowOutputRpcResult.isSuccessful());
    }


    private void addFlow(short version) throws ExecutionException, InterruptedException {
        AddFlowInput mockedAddFlowInput = new AddFlowInputBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();
        SalFlowServiceImpl salFlowService = mockSalFlowService(version);

        mockingFlowRegistryLookup();
        verifyOutput(salFlowService.addFlow(mockedAddFlowInput));

    }

    @Test
    public void testRemoveFlow() throws Exception {
        removeFlow(OFConstants.OFP_VERSION_1_0);
        removeFlow(OFConstants.OFP_VERSION_1_3);
    }


    private void removeFlow(short version) throws Exception {
        RemoveFlowInput mockedRemoveFlowInput = new RemoveFlowInputBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();

        SalFlowServiceImpl salFlowService = mockSalFlowService(version);
        verifyOutput(salFlowService.removeFlow(mockedRemoveFlowInput));

    }

    @Test
    public void testUpdateFlow() throws Exception {
        updateFlow(OFConstants.OFP_VERSION_1_0);
        updateFlow(OFConstants.OFP_VERSION_1_3);
    }


    private void updateFlow(short version) throws Exception {
        UpdateFlowInput mockedUpdateFlowInput = mock(UpdateFlowInput.class);
        UpdateFlowInput mockedUpdateFlowInput1 = mock(UpdateFlowInput.class);

        UpdatedFlow mockedUpdateFlow = new UpdatedFlowBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();

        UpdatedFlow mockedUpdateFlow1 = new UpdatedFlowBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .setPriority(Integer.valueOf(1))
                .build();

        when(mockedUpdateFlowInput.getUpdatedFlow()).thenReturn(mockedUpdateFlow);
        when(mockedUpdateFlowInput1.getUpdatedFlow()).thenReturn(mockedUpdateFlow1);

        FlowRef mockedFlowRef = mock(FlowRef.class);
        Mockito.doReturn(TABLE_II.child(Flow.class, new FlowKey(new FlowId(DUMMY_FLOW_ID)))).when(mockedFlowRef).getValue();
        when(mockedUpdateFlowInput.getFlowRef()).thenReturn(mockedFlowRef);
        when(mockedUpdateFlowInput1.getFlowRef()).thenReturn(mockedFlowRef);

        OriginalFlow mockedOriginalFlow = new OriginalFlowBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();

        OriginalFlow mockedOriginalFlow1 = new OriginalFlowBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .setPriority(Integer.valueOf(2))
                .build();

        when(mockedUpdateFlowInput.getOriginalFlow()).thenReturn(mockedOriginalFlow);
        when(mockedUpdateFlowInput1.getOriginalFlow()).thenReturn(mockedOriginalFlow1);

        SalFlowServiceImpl salFlowService = mockSalFlowService(version);

        verifyOutput(salFlowService.updateFlow(mockedUpdateFlowInput));
        verifyOutput(salFlowService.updateFlow(mockedUpdateFlowInput1));
    }

    private void mockingFlowRegistryLookup() {
        FlowDescriptor mockedFlowDescriptor = mock(FlowDescriptor.class);
        FlowId flowId = new FlowId(DUMMY_FLOW_ID);
        when(mockedFlowDescriptor.getFlowId()).thenReturn(flowId);
        when(mockedFlowDescriptor.getTableKey()).thenReturn(new TableKey(DUMMY_TABLE_ID));

        when(deviceFlowRegistry.retrieveDescriptor(Matchers.any(FlowRegistryKey.class))).thenReturn(mockedFlowDescriptor);
    }

    private <T extends DataObject> void verifyOutput(Future<RpcResult<T>> rpcResultFuture) throws ExecutionException, InterruptedException {
        assertNotNull(rpcResultFuture);
        final RpcResult<?> addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertTrue(addFlowOutputRpcResult.isSuccessful());
    }
}
