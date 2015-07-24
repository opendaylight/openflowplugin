package org.opendaylight.openflowplugin.impl.services;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.openflowplugin.impl.rpc.RpcContextImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SalTableServiceImplTest extends TestCase {

    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("444");
    private static final Short DUMMY_VERSION = OFConstants.OFP_VERSION_1_3;
    private static final int DUMMY_MAX_REQUEST = 88;

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
    RpcProviderRegistry mockedRpcProviderRegistry;
    @Mock
    OutboundQueue mockedOutboundQueue;

    @Before
    public void initialization() {
        when(mockedFeatures.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeatures.getVersion()).thenReturn(DUMMY_VERSION);

        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedPrimConnectionContext.getOutboundQueueProvider()).thenReturn(mockedOutboundQueue);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);

        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl());
    }

    @Test
    public void testUpdateTable() throws ExecutionException, InterruptedException {
        final RpcContextImpl rpcContext = new RpcContextImpl(mockedMessagSpy,
                mockedRpcProviderRegistry, mockedDeviceContext, DUMMY_MAX_REQUEST);
        final SalTableServiceImpl salTableService = new SalTableServiceImpl(rpcContext, mockedDeviceContext);
        final Future<RpcResult<UpdateTableOutput>> rpcResultFuture = salTableService.updateTable(prepareUpdateTable());
        assertNotNull(rpcResultFuture);
    }

    private UpdateTableInput prepareUpdateTable() {
        UpdateTableInputBuilder updateTableInputBuilder = new UpdateTableInputBuilder();
        UpdatedTableBuilder updatedTableBuilder = new UpdatedTableBuilder();
        updatedTableBuilder.setTableFeatures(Collections.<TableFeatures>emptyList());
        updateTableInputBuilder.setUpdatedTable(updatedTableBuilder.build());
        return updateTableInputBuilder.build();
    }

}