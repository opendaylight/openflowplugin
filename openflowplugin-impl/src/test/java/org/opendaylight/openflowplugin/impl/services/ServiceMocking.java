package org.opendaylight.openflowplugin.impl.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import java.math.BigInteger;
import java.util.List;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.device.DeviceContextImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(MockitoJUnitRunner.class)
public abstract class ServiceMocking {
    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("444");
    private static final Short DUMMY_VERSION = OFConstants.OFP_VERSION_1_3;
    private static final Long DUMMY_XID_VALUE = 2121L;
    private static final Xid DUMMY_XID = new Xid(DUMMY_XID_VALUE);

    protected static final String DUMMY_NODE_ID = "dummyNodeID";
    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_II
            = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(DUMMY_NODE_ID)));


    @Mock
    protected RequestContextStack mockedRequestContextStack;
    @Mock
    protected ConnectionContext mockedPrimConnectionContext;
    @Mock
    protected FeaturesReply mockedFeatures;
    @Mock
    protected GetFeaturesOutput mockedFeaturesOutput;
    @Mock
    protected ConnectionAdapter mockedConnectionAdapter;
    @Mock
    protected MessageSpy mockedMessagSpy;
    @Mock
    protected DeviceContextImpl mockedDeviceContext;
    @Mock
    protected DeviceState mockedDeviceState;
    @Mock
    protected DeviceInitializationPhaseHandler mockedDevicePhaseHandler;
    @Mock
    protected RequestContext mockedRequestContext;
    @Mock
    protected OutboundQueue mockedOutboundQueue;
    @Mock
    protected MultiMsgCollector multiMessageCollector;

    @Before
    public void initialization() {
        when(mockedRequestContextStack.createRequestContext()).thenReturn(mockedRequestContext);
        when(mockedRequestContext.getXid()).thenReturn(DUMMY_XID);

        when(mockedFeatures.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeatures.getVersion()).thenReturn(DUMMY_VERSION);

        when(mockedFeaturesOutput.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedFeaturesOutput.getVersion()).thenReturn(DUMMY_VERSION);

        when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedPrimConnectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        when(mockedPrimConnectionContext.getOutboundQueueProvider()).thenReturn(mockedOutboundQueue);

        when(mockedDeviceState.getNodeInstanceIdentifier()).thenReturn(NODE_II);
        when(mockedDeviceState.getFeatures()).thenReturn(mockedFeaturesOutput);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        when(mockedDeviceContext.getDeviceFlowRegistry()).thenReturn(new DeviceFlowRegistryImpl());
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getMultiMsgCollector(Matchers.<RequestContext<List<MultipartReply>>>any())).thenReturn(multiMessageCollector);

        setup();
    }

    protected void setup() {
        //NOOP - to be overloaded
    }


    protected  <T> void mockSuccessfulFuture() {
        ListenableFuture<RpcResult<T>> dummySuccessfulFuture = Futures.immediateFuture(RpcResultBuilder.success((T) null).build());
        when(mockedRequestContext.getFuture()).thenReturn(dummySuccessfulFuture);
    }

}
