package org.opendaylight.openflowplugin.impl.statistics.services.direct;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

import java.math.BigInteger;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDirectStatisticsServiceTest {
    protected static final Long PORT_NO = 1L;
    protected static final BigInteger DATAPATH_ID = BigInteger.TEN;
    protected static final short OF_VERSION = OFConstants.OFP_VERSION_1_3;
    protected static final String NODE_ID = "openflow:1";

    @Mock
    protected RequestContextStack requestContextStack;
    @Mock
    protected DeviceContext deviceContext;
    @Mock
    protected ConnectionContext connectionContext;
    @Mock
    protected FeaturesReply features;
    @Mock
    protected MessageSpy messageSpy;
    @Mock
    protected OutboundQueue outboundQueueProvider;
    @Mock
    protected MultiMsgCollector multiMsgCollector;
    @Mock
    protected TranslatorLibrary translatorLibrary;
    @Mock
    protected DeviceState deviceState;
    @Mock
    protected GetFeaturesOutput getFeaturesOutput;

    protected NodeConnectorId nodeConnectorId;
    protected KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;

    protected static NodeRef createNodeRef(String nodeIdValue) {
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(nodeIdValue)));

        return new NodeRef(nodePath);
    }

    @Before
    public void init() throws Exception {
        OpenflowPortsUtil.init();

        nodeConnectorId = InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(
                DATAPATH_ID, PORT_NO, OpenflowVersion.get(OF_VERSION));

        nodeInstanceIdentifier = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(NODE_ID)));

        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        Mockito.when(deviceContext.getMultiMsgCollector(Matchers.any(RequestContext.class))).thenReturn(multiMsgCollector);
        Mockito.when(deviceContext.oook()).thenReturn(translatorLibrary);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        Mockito.when(deviceState.getNodeId()).thenReturn(new NodeId(NODE_ID));
        Mockito.when(deviceState.getVersion()).thenReturn(OF_VERSION);
        Mockito.when(deviceState.getFeatures()).thenReturn(getFeaturesOutput);
        Mockito.when(getFeaturesOutput.getDatapathId()).thenReturn(DATAPATH_ID);
        Mockito.when(connectionContext.getFeatures()).thenReturn(features);
        Mockito.when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        Mockito.when(features.getVersion()).thenReturn(OF_VERSION);
        Mockito.when(features.getDatapathId()).thenReturn(DATAPATH_ID);
        setUp();
    }

    protected abstract void setUp() throws Exception;

    @Test
    public abstract void testBuildRequestBody() throws Exception;

    @Test
    public abstract void testBuildReply() throws Exception;

    @Test
    public abstract void testStoreStatistics() throws Exception;
}