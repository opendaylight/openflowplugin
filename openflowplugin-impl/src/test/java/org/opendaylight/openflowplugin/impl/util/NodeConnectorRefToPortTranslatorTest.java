package org.opendaylight.openflowplugin.impl.util;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
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
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by Tomas Slusny on 24.3.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeConnectorRefToPortTranslatorTest extends TestCase {
    @Mock DeviceState deviceState;
    @Mock KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier;
    @Mock GetFeaturesOutput getFeaturesOutput;
    @Mock List<PhyPort> phyPorts;
    @Mock PhyPort phyPort;

    final Long portNo = 5l;
    final String idValue = "openflow:10";

    @Before
    public void setUp() throws Exception {
        // Create nodePath (we cannot mock it in regular way because KeyedInstanceIdentifier.getKey() is final)
        final KeyedInstanceIdentifier<Node, NodeKey> nodePath = KeyedInstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(idValue)));

        when(deviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodePath);
        when(deviceState.getFeatures()).thenReturn(getFeaturesOutput);
        when(getFeaturesOutput.getDatapathId()).thenReturn(BigInteger.TEN);
        when(getFeaturesOutput.getPhyPort()).thenReturn(phyPorts);
        when(phyPorts.get(anyInt())).thenReturn(phyPort);
        when(phyPorts.size()).thenReturn(1);
        when(phyPort.getPortNo()).thenReturn(portNo);

        OpenflowPortsUtil.init();
    }

    @Test
    public void testNodeConnectorConversion() throws Exception {
        // Get port number (we will get it with regular way, even when we mocked it to point to 'this.portNo')
        Long portNo = deviceState.getFeatures().getPhyPort().get(0).getPortNo();

        // Convert DeviceState to NodeConnectorRef
        NodeConnectorRef ref = NodeConnectorRefToPortTranslator.toNodeConnectorRef(deviceState);

        // Test getting port from NodeConnectorRef
        Long refPort = NodeConnectorRefToPortTranslator.fromNodeConnectorRef(deviceState, ref);
        assertEquals(portNo, refPort);

        // Test fallback to device state if there is any problem with NodeConnectorRef
        refPort = NodeConnectorRefToPortTranslator.fromNodeConnectorRef(deviceState, null);
        assertEquals(portNo, refPort);

        // Check if 2 NodeConnectorRef created from same DeviceState have same value
        assertEquals(ref, NodeConnectorRefToPortTranslator.toNodeConnectorRef(deviceState));
    }

    @Test
    public void testGetPortNoFromDeviceState() throws Exception {
        Long portNo = NodeConnectorRefToPortTranslator.getPortNoFromDeviceState(deviceState);

        assertEquals(portNo, this.portNo);
    }
}