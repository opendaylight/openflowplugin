package org.opendaylight.openflowplugin.impl.services;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(MockitoJUnitRunner.class)
public class SalFlowServiceImplTest extends TestCase {

    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("444");
    private static final Short DUMMY_VERSION = OFConstants.OFP_VERSION_1_3;

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


    @Before
    public void initialization() {
        when(mockedFeatures.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeatures.getVersion()).thenReturn(DUMMY_VERSION);

        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedPrimConnectionContext.getOutboundQueueProvider()).thenReturn(outboundQueue);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);

        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl());
        when(mockedRequestContextStack.createRequestContext()).thenReturn(requestContext);

        when(requestContext.getXid()).thenReturn(new Xid(84L));
        when(requestContext.getFuture()).thenReturn(RpcResultBuilder.success().buildFuture());

        salFlowService = new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void testAddFlow() throws Exception {
        final AddFlowInput mockedAddFlowInput = createFlowMock(AddFlowInput.class);

        verifyOutput(salFlowService.addFlow(mockedAddFlowInput));
    }

    @Test
    public void testRemoveFlow() throws Exception {
        final RemoveFlowInput mockedRemoveFlowInput = createFlowMock(RemoveFlowInput.class);

        verifyOutput(salFlowService.removeFlow(mockedRemoveFlowInput));
    }

    @Test
    public void testUpdateFlow() throws Exception {
        final UpdateFlowInput mockedUpdateFlowInput = mock(UpdateFlowInput.class);

        final UpdatedFlow mockedUpdateFlow = createFlowMock(UpdatedFlow.class);
        when(mockedUpdateFlowInput.getUpdatedFlow()).thenReturn(mockedUpdateFlow);

        final OriginalFlow mockedOriginalFlow = createFlowMock(OriginalFlow.class);
        when(mockedUpdateFlowInput.getOriginalFlow()).thenReturn(mockedOriginalFlow);

        verifyOutput(salFlowService.updateFlow(mockedUpdateFlowInput));
    }

    private <T extends DataObject> void verifyOutput(Future<RpcResult<T>> rpcResultFuture) throws ExecutionException, InterruptedException {
        assertNotNull(rpcResultFuture);
        final RpcResult<?> addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertTrue(addFlowOutputRpcResult.isSuccessful());
    }

    private <T extends Flow> T createFlowMock(Class<T> flowClazz) {
        T mockedFlow = mock(flowClazz);
        when(mockedFlow.getMatch()).thenReturn(match);
        return mockedFlow;
    }
}