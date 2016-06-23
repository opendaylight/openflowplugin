/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.common
 *
 * Test class for testing {@link org.opendaylight.openflowplugin.impl.common.NodeConnectorTranslatorUtil}
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Mar 31, 2015
 */
public class NodeConnectorTranslatorUtilTest {

    private static final Logger LOG = LoggerFactory.getLogger(NodeConnectorTranslatorUtilTest.class);

    private static final String MAC_ADDRESS = "00:01:02:03:04:05";
    private static final String NAME = "PortTranslatorTest";
    private final Boolean[] pfBls = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private final boolean[] pfV10Bls = {false, false, false, false, false, false, false, false, false, false, false, false};
    private final boolean[] portCfgBools = {false, false, false, false};
    private final boolean[] portCfgV10bools = {false, false, false, false, false, false, false};
    private final boolean[] portStateBools = {false, false, false, false};
    private final Long currentSpeed = Long.decode("4294967295");
    private static final Long maxSpeed = Long.decode("4294967295");

    /**
     * Test method for {@link NodeConnectorTranslatorUtil#translateNodeConnectorFromFeaturesReply(FeaturesReply)}.
     */
    @Test
    public void testTranslateNodeConnectorFromFeaturesReply(){
        final FeaturesReply reply = mock(FeaturesReply.class);
        final BigInteger dataPathId = BigInteger.valueOf(25L);
        final List<PhyPort> listPorts = Arrays.asList(mockPhyPortPort());
        when(reply.getPhyPort()).thenReturn(listPorts);
        when(reply.getDatapathId()).thenReturn(dataPathId);
        when(reply.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        final List<NodeConnector> nodeConnector = NodeConnectorTranslatorUtil.translateNodeConnectorFromFeaturesReply(reply);
        Assert.assertNotNull(nodeConnector);
        Assert.assertEquals(1, nodeConnector.size());
    }

    /**
     * Test method for {@link NodeConnectorTranslatorUtil#translateNodeConnectorFromFeaturesReply(FeaturesReply)}.
     * {@link IllegalArgumentException}
     */
    @Test(expected=IllegalArgumentException.class)
    public void testTranslateNodeConnectorFromFeaturesReplyNullPorts(){
        final FeaturesReply reply = mock(FeaturesReply.class);
        when(reply.getPhyPort()).thenReturn(null);
        NodeConnectorTranslatorUtil.translateNodeConnectorFromFeaturesReply(reply);
    }

    /**
     * Test method for {@link NodeConnectorTranslatorUtil#translateNodeConnectorFromFeaturesReply(FeaturesReply)}.
     * {@link IllegalArgumentException}
     */
    @Test(expected=IllegalArgumentException.class)
    public void testTranslateNodeConnectorFromFeaturesReplyNullReplay(){
        NodeConnectorTranslatorUtil.translateNodeConnectorFromFeaturesReply(null);
    }

    /**
     * Test method for {@link NodeConnectorTranslatorUtil#translateFlowCapableNodeFromPhyPort(PhyPort, short)}.
     */
    @Test
    public void testTranslateFlowCapableNodeFromPhyPortOF10(){
        final PhyPort port = mockPhyPortPort();
        final FlowCapableNodeConnector flowCapableNodeConnector = NodeConnectorTranslatorUtil
                .translateFlowCapableNodeFromPhyPort(port, OFConstants.OFP_VERSION_1_0);
        Assert.assertNotNull(flowCapableNodeConnector);
        Assert.assertEquals(port.getName(), flowCapableNodeConnector.getName());
        Assert.assertEquals(port.getPortNo(), flowCapableNodeConnector.getPortNumber().getUint32());
        Assert.assertEquals(port.getHwAddr().getValue(), flowCapableNodeConnector.getHardwareAddress().getValue());
        Assert.assertEquals(port.getCurrSpeed(), flowCapableNodeConnector.getCurrentSpeed());
        Assert.assertEquals(port.getMaxSpeed(), flowCapableNodeConnector.getMaximumSpeed());
        assertEqualsStateV10(port.getStateV10(), flowCapableNodeConnector.getState());
        assertEqualsPortFeaturesV10(port.getAdvertisedFeaturesV10(), flowCapableNodeConnector.getAdvertisedFeatures());
        assertEqualsPortFeaturesV10(port.getCurrentFeaturesV10(), flowCapableNodeConnector.getCurrentFeature());
        assertEqualsPortFeaturesV10(port.getPeerFeaturesV10(), flowCapableNodeConnector.getPeerFeatures());
        assertEqualsPortFeaturesV10(port.getSupportedFeaturesV10(), flowCapableNodeConnector.getSupported());

    }

    /**
     * Test method for {@link NodeConnectorTranslatorUtil#translateFlowCapableNodeFromPhyPort(PhyPort, short)}.
     */
    @Test
    public void testTranslateFlowCapableNodeFromPhyPortOF13(){
        final PhyPort port = mockPhyPortPort();
        final FlowCapableNodeConnector flowCapableNodeConnector = NodeConnectorTranslatorUtil
                .translateFlowCapableNodeFromPhyPort(port, OFConstants.OFP_VERSION_1_3);
        Assert.assertNotNull(flowCapableNodeConnector);
        Assert.assertEquals(port.getName(), flowCapableNodeConnector.getName());
        Assert.assertEquals(port.getPortNo(), flowCapableNodeConnector.getPortNumber().getUint32());
        Assert.assertEquals(port.getHwAddr().getValue(), flowCapableNodeConnector.getHardwareAddress().getValue());
        Assert.assertEquals(port.getCurrSpeed(), flowCapableNodeConnector.getCurrentSpeed());
        Assert.assertEquals(port.getMaxSpeed(), flowCapableNodeConnector.getMaximumSpeed());
        assertEqualsState(port.getState(), flowCapableNodeConnector.getState());
        assertEqualsPortFeatures(port.getAdvertisedFeatures(), flowCapableNodeConnector.getAdvertisedFeatures());
        assertEqualsPortFeatures(port.getCurrentFeatures(), flowCapableNodeConnector.getCurrentFeature());
        assertEqualsPortFeatures(port.getPeerFeatures(), flowCapableNodeConnector.getPeerFeatures());
        assertEqualsPortFeatures(port.getSupportedFeatures(), flowCapableNodeConnector.getSupported());
    }

    /**
     * Here unsupported version is used
     * Test method for {@link NodeConnectorTranslatorUtil#translateFlowCapableNodeFromPhyPort(PhyPort, short)}.
     */
    @Test
    public void testTranslateFlowCapableNodeFromPhyPortOF12() {
        final PhyPort port = mockPhyPortPort();
        try {
            final FlowCapableNodeConnector flowCapableNodeConnector = NodeConnectorTranslatorUtil
                    .translateFlowCapableNodeFromPhyPort(port, (short) 0x03);
            Assert.fail("port of version 0x03 (OF-1.2) should not be translated");
        } catch (Exception e) {
            LOG.debug("expected exception: {}", e.getMessage());
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }

    }

    /**
     * Test method for {@link NodeConnectorTranslatorUtil#makeNodeConnectorId(BigInteger, String, long)}.
     */
    @Test
    public void testMakeNodeConnectorId(){
        final BigInteger dataPathId = BigInteger.valueOf(25L);
        final String logicalName = "testPort";
        final long portNo = 45L;
        final NodeConnectorId nodeConnectorId = NodeConnectorTranslatorUtil.makeNodeConnectorId(dataPathId, logicalName, portNo);
        Assert.assertNotNull(nodeConnectorId);
        Assert.assertNotNull(nodeConnectorId.getValue());
        Assert.assertTrue(nodeConnectorId.getValue().contains(logicalName));
        Assert.assertTrue(nodeConnectorId.getValue().contains(dataPathId.toString()));
        Assert.assertFalse(nodeConnectorId.getValue().contains(":" + portNo));
    }

    /**
     * Test method for {@link NodeConnectorTranslatorUtil#makeNodeConnectorId(BigInteger, String, long)}.
     */
    @Test
    public void testMakeNodeConnectorIdNullLogicalName(){
        final BigInteger dataPathId = BigInteger.valueOf(25L);
        final long portNo = 45L;
        final NodeConnectorId nodeConnectorId = NodeConnectorTranslatorUtil.makeNodeConnectorId(dataPathId, null, portNo);
        Assert.assertNotNull(nodeConnectorId);
        Assert.assertNotNull(nodeConnectorId.getValue());
        Assert.assertTrue(nodeConnectorId.getValue().contains(dataPathId.toString()));
        Assert.assertTrue(nodeConnectorId.getValue().contains(":" + portNo));
    }

    /**
     * Test method for {@link NodeConnectorTranslatorUtil#makeNodeConnectorId(BigInteger, String, long)}.
     * expect {@link IllegalArgumentException}
     */
    @Test(expected=IllegalArgumentException.class)
    public void testMakeNodeConnectorIdNullDataPath(){
        final long portNo = 45L;
        NodeConnectorTranslatorUtil.makeNodeConnectorId(null, null, portNo);
    }

    private PhyPort mockPhyPortPort() {
        final PhyPort phyport = mock(PhyPort.class);
        when(phyport.getAdvertisedFeatures()).thenReturn(getPortFeatures());
        when(phyport.getAdvertisedFeaturesV10()).thenReturn(getPortFeaturesV10());
        when(phyport.getConfig()).thenReturn(getPortConfig());
        when(phyport.getConfigV10()).thenReturn(getPortConfigV10());
        when(phyport.getCurrentFeatures()).thenReturn(getPortFeatures());
        when(phyport.getCurrentFeaturesV10()).thenReturn(getPortFeaturesV10());
        when(phyport.getCurrSpeed()).thenReturn(currentSpeed);
        when(phyport.getHwAddr()).thenReturn(getMacAddress());
        when(phyport.getName()).thenReturn(NAME);
        when(phyport.getMaxSpeed()).thenReturn(maxSpeed);
        when(phyport.getPeerFeatures()).thenReturn(getPortFeatures());
        when(phyport.getPeerFeaturesV10()).thenReturn(getPortFeaturesV10());
        when(phyport.getPortNo()).thenReturn(Long.MAX_VALUE);
        when(phyport.getState()).thenReturn(getPortState());
        when(phyport.getStateV10()).thenReturn(getPortStateV10());
        when(phyport.getSupportedFeatures()).thenReturn(getPortFeatures());
        when(phyport.getSupportedFeaturesV10()).thenReturn(getPortFeaturesV10());
        return phyport;
    }

    private static PortStateV10 getPortStateV10() {
        final PortStateV10 portState = new PortStateV10(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
        return portState;
    }

    private PortState getPortState() {
        final PortState portState = new PortState(portStateBools[0], portStateBools[1], portStateBools[2]);
        return portState;
    }

    private PortFeatures getPortFeatures() {
        return new PortFeatures(pfBls[0], pfBls[1], pfBls[2], pfBls[3], pfBls[4], pfBls[5], pfBls[6], pfBls[7], pfBls[8],
                pfBls[9], pfBls[10], pfBls[11], pfBls[12], pfBls[13], pfBls[14], pfBls[15]);
    }

    private PortFeaturesV10 getPortFeaturesV10() {
        return new PortFeaturesV10(pfV10Bls[0], pfV10Bls[1], pfV10Bls[2], pfV10Bls[3], pfV10Bls[4], pfV10Bls[5], pfV10Bls[6],
                pfV10Bls[7], pfV10Bls[8], pfV10Bls[9], pfV10Bls[10], pfV10Bls[11]);
    }

    private static MacAddress getMacAddress() {
        return new MacAddress(MAC_ADDRESS);
    }

    private PortConfigV10 getPortConfigV10() {
        return new PortConfigV10(portCfgV10bools[0], portCfgV10bools[1], portCfgV10bools[2], portCfgV10bools[3], portCfgV10bools[4], portCfgV10bools[5], portCfgV10bools[6]);
    }

    private PortConfig getPortConfig() {
        return new PortConfig(portCfgBools[0], portCfgBools[1], portCfgBools[2], portCfgBools[3]);
    }

    private static void assertEqualsStateV10(final PortStateV10 psV10, final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState state) {
        assertEquals(psV10.isBlocked(), state.isBlocked());
        assertEquals(psV10.isLinkDown(), state.isLinkDown());
        assertEquals(psV10.isLive(), state.isLive());
    }

    private static void assertEqualsState(final PortState ps, final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState state) {
        assertEquals(ps.isBlocked(), state.isBlocked());
        assertEquals(ps.isLinkDown(), state.isLinkDown());
        assertEquals(ps.isLive(), state.isLive());
    }

    private static void assertEqualsPortFeaturesV10(final PortFeaturesV10 apfV10, final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf) {
        assertEquals(apfV10.is_100mbFd(), npf.isHundredMbFd());
        assertEquals(apfV10.is_100mbHd(), npf.isHundredMbHd());

        assertEquals(apfV10.is_10gbFd(), npf.isTenGbFd());
        assertEquals(apfV10.is_10mbFd(), npf.isTenMbFd());
        assertEquals(apfV10.is_10mbHd(), npf.isTenMbHd());

        assertEquals(apfV10.is_1gbFd(), npf.isOneGbFd());
        assertEquals(apfV10.is_1gbHd(), npf.isOneGbHd());

        assertEquals(apfV10.isAutoneg(), npf.isAutoeng());
        assertEquals(apfV10.isCopper(), npf.isCopper());
        assertEquals(apfV10.isFiber(), npf.isFiber());
        assertEquals(apfV10.isPause(), npf.isPause());
        assertEquals(apfV10.isPauseAsym(), npf.isPauseAsym());
    }

    private static void assertEqualsPortFeatures(final PortFeatures apf, final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures npf) {
        assertEquals(apf.is_100gbFd(), npf.isHundredGbFd());
        assertEquals(apf.is_100mbFd(), npf.isHundredMbFd());
        assertEquals(apf.is_100mbHd(), npf.isHundredMbHd());

        assertEquals(apf.is_10gbFd(), npf.isTenGbFd());
        assertEquals(apf.is_10mbFd(), npf.isTenMbFd());
        assertEquals(apf.is_10mbHd(), npf.isTenMbHd());

        assertEquals(apf.is_1gbFd(), npf.isOneGbFd());
        assertEquals(apf.is_1gbHd(), npf.isOneGbHd());
        assertEquals(apf.is_1tbFd(), npf.isOneTbFd());

        assertEquals(apf.is_40gbFd(), npf.isFortyGbFd());

        assertEquals(apf.isAutoneg(), npf.isAutoeng());
        assertEquals(apf.isCopper(), npf.isCopper());
        assertEquals(apf.isFiber(), npf.isFiber());
        assertEquals(apf.isOther(), npf.isOther());
        assertEquals(apf.isPause(), npf.isPause());
        assertEquals(apf.isPauseAsym(), npf.isPauseAsym());
    }

    static InstanceIdentifier<NodeConnector> createNodeConnectorId(String nodeKey, String nodeConnectorKey) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(nodeKey)))
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId(nodeConnectorKey)))
                .build();
    }

    @Test
    public void testDummy() {
        InstanceIdentifier<NodeConnector> id = createNodeConnectorId("openflow:1", "openflow:1:1");
        InstanceIdentifier<Node> nodeId = id.firstIdentifierOf(Node.class);
        System.out.println(nodeId);
    }
}
