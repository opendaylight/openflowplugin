/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCache;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupStatus;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.services.cache.FlowGroupCacheManagerImpl;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
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
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.class)
public class SalFlowServiceImplTest extends TestCase {

    private static final Uint64 DUMMY_DATAPATH_ID = Uint64.valueOf(444);
    private static final String DUMMY_NODE_ID = "dummyNodeID";
    private static final String DUMMY_FLOW_ID = "dummyFlowID";
    private static final Short DUMMY_TABLE_ID = (short) 0;

    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_II
            = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(DUMMY_NODE_ID)));

    private static final KeyedInstanceIdentifier<Table, TableKey> TABLE_II
            = NODE_II.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(DUMMY_TABLE_ID));

    private final NodeRef noderef = new NodeRef(NODE_II);
    private static final String KEY = "0";
    private static FlowGroupCache flowcache =
            new FlowGroupCache("0","mock class", FlowGroupStatus.ADDED, LocalDateTime.MAX);

    private static Queue<FlowGroupCache> caches() {
        Queue<FlowGroupCache> cache = new LinkedList<>();
        cache.add(flowcache);
        return cache;
    }

    private static final Queue<FlowGroupCache> CACHE = caches();

    private static Map<String, Queue<FlowGroupCache>> createMap() {
        Map<String,Queue<FlowGroupCache>> myMap = new HashMap<>();
        myMap.put(KEY, CACHE);
        return myMap;
    }

    private static final Map<String, Queue<FlowGroupCache>> MYMAP = createMap();

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
    @Mock
    private FlowGroupCacheManagerImpl flowGroupCacheManager;

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
        when(flowGroupCacheManager.getAllNodesFlowGroupCache()).thenReturn(MYMAP);
    }

    private SalFlowServiceImpl mockSalFlowService(final short version) {
        Uint8 ver = Uint8.valueOf(version);
        when(mockedFeatures.getVersion()).thenReturn(ver);
        when(mockedFeaturesOutput.getVersion()).thenReturn(ver);
        when(mockedDeviceInfo.getVersion()).thenReturn(version);

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        return new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager,
                flowGroupCacheManager);
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

    private void addFlowFailCallback(final short version) throws InterruptedException, ExecutionException {
        AddFlowInput mockedAddFlowInput = new AddFlowInputBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .setNode(noderef)
                .build();

        Mockito.doReturn(Futures.<RequestContext<Object>>immediateFailedFuture(new Exception("ut-failed-response")))
                .when(requestContext).getFuture();

        final Future<RpcResult<AddFlowOutput>> rpcResultFuture =
                mockSalFlowService(version).addFlow(mockedAddFlowInput);

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

    private void removeFlowFailCallback(final short version) throws InterruptedException, ExecutionException {
        RemoveFlowInput mockedRemoveFlowInput = new RemoveFlowInputBuilder()
                .setTableId((short)1)
                .setMatch(match)
                .setNode(noderef)
                .build();

        Mockito.doReturn(Futures.<RequestContext<Object>>immediateFailedFuture(new Exception("ut-failed-response")))
                .when(requestContext).getFuture();

        final Future<RpcResult<RemoveFlowOutput>> rpcResultFuture =
                mockSalFlowService(version).removeFlow(mockedRemoveFlowInput);

        assertNotNull(rpcResultFuture);
        final RpcResult<?> removeFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(removeFlowOutputRpcResult);
        assertFalse(removeFlowOutputRpcResult.isSuccessful());
    }

    @Test
    public void testAddFlowWithItemLifecycle() throws Exception {
        addFlow(OFConstants.OFP_VERSION_1_0);
        addFlow(OFConstants.OFP_VERSION_1_3);
    }

    private void addFlow(final short version) throws ExecutionException, InterruptedException {
        AddFlowInput mockedAddFlowInput = new AddFlowInputBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .setNode(noderef)
                .build();
        SalFlowServiceImpl salFlowService = mockSalFlowService(version);

        verifyOutput(salFlowService.addFlow(mockedAddFlowInput));
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

    private void removeFlow(final short version) throws Exception {
        RemoveFlowInput mockedRemoveFlowInput = new RemoveFlowInputBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .setNode(noderef)
                .build();

        SalFlowServiceImpl salFlowService = mockSalFlowService(version);
        verifyOutput(salFlowService.removeFlow(mockedRemoveFlowInput));
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

    private void updateFlow(final short version) throws Exception {
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
        Mockito.doReturn(TABLE_II.child(Flow.class,
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

        SalFlowServiceImpl salFlowService = mockSalFlowService(version);
        when(mockedUpdateFlowInput.getNode()).thenReturn(noderef);
        when(mockedUpdateFlowInput1.getNode()).thenReturn(noderef);
        verifyOutput(salFlowService.updateFlow(mockedUpdateFlowInput));
        verifyOutput(salFlowService.updateFlow(mockedUpdateFlowInput1));
    }

    private static <T extends DataObject> void verifyOutput(final Future<RpcResult<T>> rpcResultFuture)
            throws ExecutionException, InterruptedException {
        assertNotNull(rpcResultFuture);
        final RpcResult<?> addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertTrue(addFlowOutputRpcResult.isSuccessful());
    }
}
