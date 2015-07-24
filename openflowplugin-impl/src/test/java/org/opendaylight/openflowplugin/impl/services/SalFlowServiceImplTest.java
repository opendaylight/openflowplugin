package org.opendaylight.openflowplugin.impl.services;


import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import junit.framework.TestCase;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.binding.DataObject;
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


    @Before
    public void initialization() {
        when(mockedFeatures.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeatures.getVersion()).thenReturn(DUMMY_VERSION);

        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);

        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl());
    }

    @Test
    public void testAddFlow() throws Exception {
        final SalFlowServiceImpl salFlowService = new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext);

        AddFlowInput mockedAddFlowInput = mock(AddFlowInput.class);
        when(mockedAddFlowInput.getMatch()).thenReturn(mock(Match.class));

        verifyOutput(salFlowService.addFlow(mockedAddFlowInput));
    }

    @Test
    public void testRemoveFlow() throws Exception {
        final SalFlowServiceImpl salFlowService = new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext);

        RemoveFlowInput mockedRemoveFlowInput = mock(RemoveFlowInput.class);
        verifyOutput(salFlowService.removeFlow(mockedRemoveFlowInput));
    }

    @Test
    public void testUpdateFlow() throws Exception {
        final SalFlowServiceImpl salFlowService = new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext);

        UpdateFlowInput mockedUpdateFlowInput = mock(UpdateFlowInput.class);


        UpdatedFlow mockedUpdateFlow = mock(UpdatedFlow.class);
        when(mockedUpdateFlow.getMatch()).thenReturn(mock(Match.class));
        when(mockedUpdateFlowInput.getUpdatedFlow()).thenReturn(mockedUpdateFlow);


        OriginalFlow mockedOriginalFlow = mock(OriginalFlow.class);
        when(mockedOriginalFlow.getMatch()).thenReturn(mock(Match.class));
        when(mockedUpdateFlowInput.getOriginalFlow()).thenReturn(mockedOriginalFlow);

        verifyOutput(salFlowService.updateFlow(mockedUpdateFlowInput));
    }

    private <T extends DataObject> void verifyOutput(Future<RpcResult<T>> rpcResultFuture) throws ExecutionException, InterruptedException {
        assertNotNull(rpcResultFuture);
        final RpcResult<?> addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertTrue(addFlowOutputRpcResult.isSuccessful());
    }
}