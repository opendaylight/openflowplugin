package org.opendaylight.openflowplugin.impl.util;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Tomas Slusny on 24.3.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeConnectorRefToPortTranslatorTest extends TestCase {

    @Mock
    DeviceState deviceState;
    @Mock
    KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    @Mock
    GetFeaturesOutput getFeaturesOutput;
    @Mock
    List<PhyPort> phyPorts;
    @Mock
    Iterator<PhyPort> phyPortsIterator;
    @Mock
    PhyPort phyPort;


    @Mock
    DeviceState secondDeviceState;
    @Mock
    GetFeaturesOutput secondGetFeaturesOutput;
    @Mock
    PhyPort secondPhyPort;

    static final Long PORT_NO = 5l;
    static final Long SECOND_PORT_NO = 6l;
    static final String ID_VALUE = "openflow:10";

    @Before
    public void setUp() throws Exception {
        // Create nodePath (we cannot mock it in regular way because KeyedInstanceIdentifier.getKey() is final)
        final KeyedInstanceIdentifier<Node, NodeKey> nodePath = KeyedInstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(ID_VALUE)));

        // Mock first device state
        final List<PhyPort> phyPorts = Arrays.asList(phyPort);
        when(deviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodePath);
        when(deviceState.getFeatures()).thenReturn(getFeaturesOutput);
        when(getFeaturesOutput.getDatapathId()).thenReturn(BigInteger.TEN);
        when(getFeaturesOutput.getPhyPort()).thenReturn(phyPorts);
        when(phyPort.getPortNo()).thenReturn(PORT_NO);

        // Mock second device state
        final List<PhyPort> secondPhyPorts = Arrays.asList(phyPort);
        when(secondDeviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(secondDeviceState.getNodeInstanceIdentifier()).thenReturn(nodePath);
        when(secondDeviceState.getFeatures()).thenReturn(secondGetFeaturesOutput);
        when(secondGetFeaturesOutput.getDatapathId()).thenReturn(BigInteger.TEN);
        when(secondGetFeaturesOutput.getPhyPort()).thenReturn(secondPhyPorts);
        when(secondPhyPort.getPortNo()).thenReturn(SECOND_PORT_NO);


        OpenflowPortsUtil.init();
    }

    @Test(expected = NullPointerException.class)
    public void testForNotNullableDeviceStateInGetPortNo() throws Exception {
        NodeConnectorRefToPortTranslator.getPortNoFromDeviceState(null);
    }

    @Test(expected = NullPointerException.class)
    public void testForNotNullableDeviceStateInToNodeConnectorRef() throws Exception {
        NodeConnectorRefToPortTranslator.toNodeConnectorRef(null);
    }

    @Test(expected = NullPointerException.class)
    public void testForNotNullableDeviceStateInFromNodeConnectorRef() throws Exception {
        NodeConnectorRefToPortTranslator.fromNodeConnectorRef(null, null);
    }

    @Test
    public void testGetPortNoFromDeviceState() throws Exception {
        Long portNo = NodeConnectorRefToPortTranslator.getPortNoFromDeviceState(deviceState);
        assertEquals(portNo, PORT_NO);
    }

    @Test
    public void testNodeConnectorConversion() throws Exception {
        // Convert DeviceState to NodeConnectorRef
        NodeConnectorRef ref = NodeConnectorRefToPortTranslator.toNodeConnectorRef(deviceState);

        // Test getting port from NodeConnectorRef
        Long refPort = NodeConnectorRefToPortTranslator.fromNodeConnectorRef(deviceState, ref);
        assertEquals(PORT_NO, refPort);

        // Test for getting same port, even when we used different DeviceState as fallback
        Long secondPort = NodeConnectorRefToPortTranslator.fromNodeConnectorRef(secondDeviceState, ref);
        assertEquals(refPort, secondPort);

        // Test fallback to device state if there is any problem with NodeConnectorRef
        refPort = NodeConnectorRefToPortTranslator.fromNodeConnectorRef(deviceState, null);
        assertEquals(PORT_NO, refPort);

        // Check if 2 NodeConnectorRef created from same DeviceState have same value
        assertEquals(ref, NodeConnectorRefToPortTranslator.toNodeConnectorRef(deviceState));
    }
}