package org.opendaylight.openflowplugin.impl.services;


import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.Tables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import junit.framework.TestCase;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SalFlowServiceImplTest extends TestCase {

    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("444");
    private static final Short DUMMY_VERSION = OFConstants.OFP_VERSION_1_3;
    private static final String DUMMY_NODE_ID = "dummyNodeID";
    private static final String DUMMY_FLOW_ID = "dummyFlowID";
    private static final Short DUMMY_TABLE_ID = (short)0;

    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_II
            = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(DUMMY_NODE_ID)));

    private static final KeyedInstanceIdentifier<Table, TableKey> TABLE_II
            = NODE_II.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(DUMMY_TABLE_ID));

    @Mock
    RequestContextStack mockedRequestContextStack;
    @Mock
    DeviceContext mockedDeviceContext;
    @Mock
    ConnectionContext mockedPrimConnectionContext;
    @Mock
    FeaturesReply mockedFeatures;
    @Mock
    ConnectionAdapter mockedConnectionAdapter;
    @Mock
    MessageSpy mockedMessagSpy;
    @Mock
    DeviceState mockedDeviceState;

    @Before
    public void initialization() {
        when(mockedFeatures.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeatures.getVersion()).thenReturn(DUMMY_VERSION);

        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);

        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl());


        when(mockedDeviceState.getNodeInstanceIdentifier()).thenReturn(NODE_II);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
    }

    @Test
    public void testAddFlowWithItemLifecycle() throws Exception {
        addFlow(mock(ItemLifecycleListener.class));
    }

    @Test
    public void testAddFlow() throws Exception {
        addFlow(null);
    }

    private void addFlow(final ItemLifecycleListener itemLifecycleListener) throws ExecutionException, InterruptedException {
        final SalFlowServiceImpl salFlowService = new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext);

        AddFlowInput mockedAddFlowInput = mock(AddFlowInput.class);
        when(mockedAddFlowInput.getMatch()).thenReturn(mock(Match.class));


        if (itemLifecycleListener != null) {
            salFlowService.setItemLifecycleListener(itemLifecycleListener);
        }

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
        final SalFlowServiceImpl salFlowService = new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext);

        RemoveFlowInput mockedRemoveFlowInput = mock(RemoveFlowInput.class);
        when(mockedRemoveFlowInput.getMatch()).thenReturn(mock(Match.class));


        if (itemLifecycleListener != null) {
            salFlowService.setItemLifecycleListener(itemLifecycleListener);
            mockingForRemoveOperation();

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
        final SalFlowServiceImpl salFlowService = new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext);

        UpdateFlowInput mockedUpdateFlowInput = mock(UpdateFlowInput.class);


        UpdatedFlow mockedUpdateFlow = mock(UpdatedFlow.class);
        when(mockedUpdateFlow.getMatch()).thenReturn(mock(Match.class));
        when(mockedUpdateFlowInput.getUpdatedFlow()).thenReturn(mockedUpdateFlow);
        FlowRef mockedFlowRef = mock(FlowRef.class);
        when(mockedFlowRef.getValue()).thenReturn((InstanceIdentifier)TABLE_II.child(Flow.class, new FlowKey(new FlowId(DUMMY_FLOW_ID))));
        when(mockedUpdateFlowInput.getFlowRef()).thenReturn(mockedFlowRef);

        OriginalFlow mockedOriginalFlow = mock(OriginalFlow.class);
        when(mockedOriginalFlow.getMatch()).thenReturn(mock(Match.class));
        when(mockedUpdateFlowInput.getOriginalFlow()).thenReturn(mockedOriginalFlow);

        if (itemLifecycleListener != null) {
            salFlowService.setItemLifecycleListener(itemLifecycleListener);
            mockingForRemoveOperation();
        }

        verifyOutput(salFlowService.updateFlow(mockedUpdateFlowInput));

        if (itemLifecycleListener != null) {
            Mockito.verify(itemLifecycleListener).onAdded(Matchers.<KeyedInstanceIdentifier<Flow, FlowKey>>any(), Matchers.<Flow>any());
            Mockito.verify(itemLifecycleListener).onRemoved(Matchers.<KeyedInstanceIdentifier<Flow, FlowKey>>any());
        }

    }

    private void mockingForRemoveOperation() {
        DeviceFlowRegistry mockedFlowRegistry = mock(DeviceFlowRegistry.class);
        FlowDescriptor mockedFlowDescriptor = mock(FlowDescriptor.class);
        when(mockedFlowDescriptor.getFlowId()).thenReturn(new FlowId(DUMMY_FLOW_ID));
        when(mockedFlowDescriptor.getTableKey()).thenReturn(new TableKey(DUMMY_TABLE_ID));

        when(mockedFlowRegistry.retrieveIdForFlow(Matchers.any(FlowRegistryKey.class))).thenReturn(mockedFlowDescriptor);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(mockedFlowRegistry);
    }

    private <T extends DataObject> void verifyOutput(Future<RpcResult<T>> rpcResultFuture) throws ExecutionException, InterruptedException {
        assertNotNull(rpcResultFuture);
        final RpcResult<?> addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertTrue(addFlowOutputRpcResult.isSuccessful());
    }
}