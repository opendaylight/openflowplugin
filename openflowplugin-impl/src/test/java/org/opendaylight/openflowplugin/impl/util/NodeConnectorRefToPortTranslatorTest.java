package org.opendaylight.openflowplugin.impl.util;

import com.google.common.collect.Lists;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
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
    PhyPort phyPort2;

    @Mock
    DeviceState secondDeviceState;
    @Mock
    GetFeaturesOutput secondGetFeaturesOutput;
    @Mock
    PhyPort secondPhyPort;

    static final String PACKET_DATA = "Test_Data";
    static final Long PORT_NO = 5l;
    static final Long SECOND_PORT_NO = 6l;
    static final String ID_VALUE = "openflow:10";

    private static PacketIn createPacketIn(long portNo) {
        InPortBuilder inPortBuilder = new InPortBuilder()
                .setPortNumber(new PortNumber(portNo));

        InPortCaseBuilder caseBuilder = new InPortCaseBuilder()
                .setInPort(inPortBuilder.build());

        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder()
                .setOxmClass(OpenflowBasicClass.class)
                .setOxmMatchField(InPort.class)
                .setHasMask(false)
                .setMatchEntryValue(caseBuilder.build());

        MatchBuilder matchBuilder = new MatchBuilder()
                .setMatchEntry(Lists.newArrayList(matchEntryBuilder.build()));

        return new PacketInMessageBuilder()
                .setVersion(OFConstants.OFP_VERSION_1_0)
                .setData(PACKET_DATA.getBytes())
                .setReason(PacketInReason.OFPRACTION)
                .setMatch(matchBuilder.build())
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .setCookie(BigInteger.ZERO)
                .setTableId(new TableId(42L))
                .build();
    }

    @Before
    public void setUp() throws Exception {
        // Create nodePath (we cannot mock it in regular way because KeyedInstanceIdentifier.getKey() is final)
        final KeyedInstanceIdentifier<Node, NodeKey> nodePath = KeyedInstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(ID_VALUE)));

        // Mock first device state
        final List<PhyPort> phyPorts = Arrays.asList(null, phyPort2, phyPort);
        when(deviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodePath);
        when(deviceState.getFeatures()).thenReturn(getFeaturesOutput);
        when(getFeaturesOutput.getDatapathId()).thenReturn(BigInteger.TEN);
        when(getFeaturesOutput.getPhyPort()).thenReturn(phyPorts);
        when(phyPort.getPortNo()).thenReturn(PORT_NO);
        when(phyPort2.getPortNo()).thenReturn(null);

        // Mock second device state
        final List<PhyPort> secondPhyPorts = Arrays.asList(secondPhyPort);
        when(secondDeviceState.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(secondDeviceState.getNodeInstanceIdentifier()).thenReturn(nodePath);
        when(secondDeviceState.getFeatures()).thenReturn(secondGetFeaturesOutput);
        when(secondGetFeaturesOutput.getDatapathId()).thenReturn(BigInteger.TEN);
        when(secondGetFeaturesOutput.getPhyPort()).thenReturn(secondPhyPorts);
        when(secondPhyPort.getPortNo()).thenReturn(SECOND_PORT_NO);

        // Initialize the OpenFlow version/port map
        OpenflowPortsUtil.init();
    }

    @Test(expected = NullPointerException.class)
    public void testForNotNullableDeviceStateInGetPortNo() throws Exception {
        NodeConnectorRefToPortTranslator.getPortNoFromDeviceState(null);
    }

    @Test(expected = NullPointerException.class)
    public void testForNotNullablePacketInInGetPortNo() throws Exception {
        NodeConnectorRefToPortTranslator.getPortNoFromPacketIn(null);
    }

    @Test(expected = NullPointerException.class)
    public void testForNotNullableDeviceStateInToNodeConnectorRef() throws Exception {
        NodeConnectorRefToPortTranslator.toNodeConnectorRef(null, null);
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
    public void testGetPortNoFromPacketIn() throws Exception {
        PacketIn packetIn = createPacketIn(PORT_NO);
        Long portNo = NodeConnectorRefToPortTranslator.getPortNoFromPacketIn(packetIn);
        assertEquals(portNo, PORT_NO);
    }

    @Test
    public void testNodeConnectorConversion() throws Exception {
        // Mock the packet in message
        PacketIn packetIn = createPacketIn(PORT_NO);

        // Convert PacketIn to NodeConnectorRef
        NodeConnectorRef ref = NodeConnectorRefToPortTranslator.toNodeConnectorRef(deviceState, packetIn);

        // Get port number from created NodeConnectorRef
        Long refPort = NodeConnectorRefToPortTranslator.fromNodeConnectorRef(deviceState, ref);

        // Check if we got the correct port number
        assertEquals(PORT_NO, refPort);

        // Check if 2 NodeConnectorRef created from same PacketIn and different DeviceState fallbacks have same value
        assertEquals(ref, NodeConnectorRefToPortTranslator.toNodeConnectorRef(secondDeviceState, packetIn));

        // Check if 2 port numbers got from same NodeConnectorRef and different DeviceState fallbacks have same value
        assertEquals(refPort, NodeConnectorRefToPortTranslator.fromNodeConnectorRef(secondDeviceState, ref));

        // Test fallback to device state if there is any problem with PacketIn
        assertEquals(ref, NodeConnectorRefToPortTranslator.toNodeConnectorRef(deviceState, null));

        // Test fallback to device state if there is any problem with NodeConnectorRef
        assertEquals(refPort, NodeConnectorRefToPortTranslator.fromNodeConnectorRef(deviceState, null));
    }
}