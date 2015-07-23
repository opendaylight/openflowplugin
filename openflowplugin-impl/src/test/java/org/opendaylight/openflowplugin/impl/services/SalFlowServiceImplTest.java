package org.opendaylight.openflowplugin.impl.services;


import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import junit.framework.TestCase;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.math.BigInteger;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SalFlowServiceImplTest extends TestCase {

    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("444");
    private static final Short DUMMY_VERSION = OFConstants.OFP_VERSION_1_3;

    @Test
    public void testAddFlow() throws Exception {
        RequestContextStack mockedRequestContextStack = mock(RequestContextStack.class);
        DeviceContext mockedDeviceContext = mock(DeviceContext.class);

        ConnectionContext mockedPrimConnectionContext = mock(ConnectionContext.class);
        FeaturesReply mockedFeatures = mock(FeaturesReply.class);
        when(mockedFeatures.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeatures.getVersion()).thenReturn(DUMMY_VERSION);
        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        ConnectionAdapter mockedConnectionAdapter = mock(ConnectionAdapter.class);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);
        MessageSpy mockedMessagSpy = mock(MessageSpy.class);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl());

        final SalFlowServiceImpl salFlowService = new SalFlowServiceImpl(mockedRequestContextStack, mockedDeviceContext);

        AddFlowInput mockedAddFlowInput = mock(AddFlowInput.class);
        when(mockedAddFlowInput.getMatch()).thenReturn(mock(Match.class));
        final Future<RpcResult<AddFlowOutput>> rpcResultFuture = salFlowService.addFlow(mockedAddFlowInput);
        assertNotNull(rpcResultFuture);
        final RpcResult<AddFlowOutput> addFlowOutputRpcResult = rpcResultFuture.get();
        assertNotNull(addFlowOutputRpcResult);
        assertTrue(addFlowOutputRpcResult.isSuccessful());
    }

    public void testRemoveFlow() throws Exception {

    }

    public void testUpdateFlow() throws Exception {

    }
}