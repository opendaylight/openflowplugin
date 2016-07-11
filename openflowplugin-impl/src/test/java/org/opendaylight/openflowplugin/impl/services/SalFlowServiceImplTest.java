package org.opendaylight.openflowplugin.impl.services;


import static org.mockito.Mockito.mock;
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
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
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
    private static final Short DUMMY_VERSION = OFConstants.OFP_VERSION_1_3;
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
    private SalFlowServiceImpl salFlowService;

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
        when(mockedFeatures.getVersion()).thenReturn(DUMMY_VERSION);
        when(mockedFeaturesOutput.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeaturesOutput.getVersion()).thenReturn(DUMMY_VERSION);

        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedPrimConnectionContext.getOutboundQueueProvider()).thenReturn(outboundQueue);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);

        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(deviceFlowRegistry);
        when(mockedRequestContextStack.createRequestContext()).thenReturn(requestContext);

        when(requestContext.getXid()).thenReturn(new Xid(84L));
        when(requestContext.getFuture()).thenReturn(RpcResultBuilder.success().buildFuture());

        when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(NODE_II);
        when(mockedDeviceInfo.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedDeviceInfo.getVersion()).thenReturn(DUMMY_VERSION);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);

        salFlowService = new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void testAddFlow() throws Exception {
        addFlow(null);
    }

    @Test
    public void testAddFlowFailCallback() throws Exception {
        AddFlowInput mockedAddFlowInput = new AddFlowInputBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();

        Mockito.doReturn(Futures.<RequestContext<Object>>immediateFailedFuture(new Exception("ut-failed-response")))
                .when(requestContext).getFuture();

        final Future<RpcResult<AddFlowOutput>> rpcResultFuture = salFlowService.addFlow(mockedAddFlowInput);

        assertNotNull(rpcResultFuture);
        final RpcResult<?> addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertFalse(addFlowOutputRpcResult.isSuccessful());
    }

    @Test
    public void testRemoveFlowFailCallback() throws Exception {
        RemoveFlowInput mockedRemoveFlowInput = new RemoveFlowInputBuilder()
                .setMatch(match)
                .build();

        Mockito.doReturn(Futures.<RequestContext<Object>>immediateFailedFuture(new Exception("ut-failed-response")))
                .when(requestContext).getFuture();

        final Future<RpcResult<RemoveFlowOutput>> rpcResultFuture = salFlowService.removeFlow(mockedRemoveFlowInput);

        assertNotNull(rpcResultFuture);
        final RpcResult<?> removeFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(removeFlowOutputRpcResult);
        assertFalse(removeFlowOutputRpcResult.isSuccessful());
    }

    @Test
    public void testAddFlowWithItemLifecycle() throws Exception {
        addFlow(mock(ItemLifecycleListener.class));
    }

    private void addFlow(final ItemLifecycleListener itemLifecycleListener) throws ExecutionException, InterruptedException {
        AddFlowInput mockedAddFlowInput = new AddFlowInputBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();

        salFlowService.setItemLifecycleListener(itemLifecycleListener);

        verifyOutput(salFlowService.addFlow(mockedAddFlowInput));
        if (itemLifecycleListener != null) {
            Mockito.verify(itemLifecycleListener).onAdded(Matchers.<KeyedInstanceIdentifier<Flow, FlowKey>>any(), Matchers.<Flow>any());
        }
    }

    @Test
    public void testRemoveFlow() throws Exception {
        removeFlow(null);
    }

    @Test
    public void testRemoveFlowWithItemLifecycle() throws Exception {
        removeFlow(mock(ItemLifecycleListener.class));
    }

    private void removeFlow(final ItemLifecycleListener itemLifecycleListener) throws Exception {
        RemoveFlowInput mockedRemoveFlowInput = new RemoveFlowInputBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();

        if (itemLifecycleListener != null) {
            salFlowService.setItemLifecycleListener(itemLifecycleListener);
            mockingFlowRegistryLookup();

        }

        verifyOutput(salFlowService.removeFlow(mockedRemoveFlowInput));
        if (itemLifecycleListener != null) {
            Mockito.verify(itemLifecycleListener).onRemoved(Matchers.<KeyedInstanceIdentifier<Flow, FlowKey>>any());
        }

    }

    @Test
    public void testUpdateFlow() throws Exception {
        updateFlow(null);
    }

    @Test
    public void testUpdateFlowWithItemLifecycle() throws Exception {
        updateFlow(mock(ItemLifecycleListener.class));
    }

    private void updateFlow(final ItemLifecycleListener itemLifecycleListener) throws Exception {
        UpdateFlowInput mockedUpdateFlowInput = mock(UpdateFlowInput.class);

        UpdatedFlow mockedUpdateFlow = new UpdatedFlowBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();

        when(mockedUpdateFlowInput.getUpdatedFlow()).thenReturn(mockedUpdateFlow);

        FlowRef mockedFlowRef = mock(FlowRef.class);
        Mockito.doReturn(TABLE_II.child(Flow.class, new FlowKey(new FlowId(DUMMY_FLOW_ID)))).when(mockedFlowRef).getValue();
        when(mockedUpdateFlowInput.getFlowRef()).thenReturn(mockedFlowRef);

        OriginalFlow mockedOriginalFlow = new OriginalFlowBuilder()
                .setMatch(match)
                .setTableId((short)1)
                .build();

        when(mockedUpdateFlowInput.getOriginalFlow()).thenReturn(mockedOriginalFlow);

        if (itemLifecycleListener != null) {
            salFlowService.setItemLifecycleListener(itemLifecycleListener);
            mockingFlowRegistryLookup();
        }

        verifyOutput(salFlowService.updateFlow(mockedUpdateFlowInput));

        if (itemLifecycleListener != null) {
            Mockito.verify(itemLifecycleListener).onUpdated(Matchers.<KeyedInstanceIdentifier<Flow, FlowKey>>any(), Matchers.<Flow>any());
        }

    }

    private void mockingFlowRegistryLookup() {
        FlowDescriptor mockedFlowDescriptor = mock(FlowDescriptor.class);
        when(mockedFlowDescriptor.getFlowId()).thenReturn(new FlowId(DUMMY_FLOW_ID));
        when(mockedFlowDescriptor.getTableKey()).thenReturn(new TableKey(DUMMY_TABLE_ID));

        when(deviceFlowRegistry.retrieveIdForFlow(Matchers.any(FlowRegistryKey.class))).thenReturn(mockedFlowDescriptor);
    }

    private <T extends DataObject> void verifyOutput(Future<RpcResult<T>> rpcResultFuture) throws ExecutionException, InterruptedException {
        assertNotNull(rpcResultFuture);
        final RpcResult<?> addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertTrue(addFlowOutputRpcResult.isSuccessful());
    }
}