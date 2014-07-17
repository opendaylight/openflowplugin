/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import static org.junit.Assert.assertNotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValuesV10;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.math.BigInteger;
import java.util.List;

/**
 *
 */
public class InventoryDataServiceUtilTest {


    private static final long PORT_NO = (long) PortNumberValuesV10.CONTROLLER.getIntValue();
    private static final BigInteger PATH_ID = BigInteger.TEN;

    @Before
    public void setupEnvironment() {
        OpenflowPortsUtil.init();
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#checkForNodes()}.
     */
    public void testCheckForNodes() {
        Nodes nodes = InventoryDataServiceUtil.checkForNodes();
        assertNotNull(nodes);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#readAllNodes()}.
     */
    public void testReadAllNodes() {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node> nodes = InventoryDataServiceUtil.readAllNodes();
        assertNotNull(nodes);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeUpdatedBuilderFromDataPathId(BigInteger datapathId)}.
     */
    public void testNodeUpdatedBuilderFromDataPathId() {
        NodeUpdatedBuilder nodeUpdatedBuilder = InventoryDataServiceUtil.nodeUpdatedBuilderFromDataPathId(PATH_ID);
        assertNotNull(nodeUpdatedBuilder);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeConnectorBuilderFromDatapathIdPortNo(BigInteger datapathId,
     * Long portNo, OpenflowVersion ofVersion)}.
     */
    public void testNodeConnectorBuilderFromDatapathIdPortNo() {
        NodeConnectorBuilder nodeConnectorBuilder = InventoryDataServiceUtil.nodeConnectorBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.OF10);
        assertNotNull(nodeConnectorBuilder);

        nodeConnectorBuilder = InventoryDataServiceUtil.nodeConnectorBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.OF13);
        assertNotNull(nodeConnectorBuilder);

        nodeConnectorBuilder = InventoryDataServiceUtil.nodeConnectorBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.UNSUPPORTED);
        assertNotNull(nodeConnectorBuilder);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeConnectorUpdatedBuilderFromDatapathIdPortNo(BigInteger datapathId,
     * Long portNo, OpenflowVersion ofVersion)}
     */
    public void testNodeConnectorUpdatedBuilderFromDatapathIdPortNo() {
        NodeConnectorUpdatedBuilder nodeConnectorUpdatedBuilder = InventoryDataServiceUtil.nodeConnectorUpdatedBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.OF10);
        assertNotNull(nodeConnectorUpdatedBuilder);

        nodeConnectorUpdatedBuilder = InventoryDataServiceUtil.nodeConnectorUpdatedBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.OF13);
        assertNotNull(nodeConnectorUpdatedBuilder);

        nodeConnectorUpdatedBuilder = InventoryDataServiceUtil.nodeConnectorUpdatedBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.UNSUPPORTED);
        assertNotNull(nodeConnectorUpdatedBuilder);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeConnectorInstanceIdentifierFromDatapathIdPortno(BigInteger datapathId, Long portNo, OpenflowVersion ofVersion)}
     */
    @Test
    public void testNodeConnectorInstanceIdentifierFromDatapathIdPortno() {
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceIdentifier = InventoryDataServiceUtil.nodeConnectorInstanceIdentifierFromDatapathIdPortno(BigInteger.ONE,
                PORT_NO, OpenflowVersion.OF10);
        assertNotNull(nodeConnectorInstanceIdentifier);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeConnectorRefFromDatapathIdPortno(BigInteger datapathId, Long portNo, OpenflowVersion ofVersion)}
     */
    @Test
    public void testNodeConnectorRefFromDatapathIdPortno() {
        NodeConnectorRef nodeConnectorRef = InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(PATH_ID,
                PORT_NO,
                OpenflowVersion.OF10);
        assertNotNull(nodeConnectorRef);

        nodeConnectorRef = InventoryDataServiceUtil.nodeConnectorRefFromDatapathIdPortno(PATH_ID,
                PORT_NO,
                OpenflowVersion.OF13);
        assertNotNull(nodeConnectorRef);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#dataPathIdFromNodeId(NodeId)}.
     */
    @Test
    public void testDataPathIdFromNodeId() {
        String string = "openflow:";
        NodeId[] nodeIds = new NodeId[]{
                // 0x00000000 000004d2
                new NodeId(string + "1234"),
                // 0x8db2089e 01391a86
                new NodeId(string + "10210232779920710278"),
                // 0xffffffff ffffffff
                new NodeId(string + "18446744073709551615"),
        };

        long[] expectedDPIDs = new long[]{
                1234L,
                -8236511293788841338L,
                -1L
        };

        for (int i = 0; i < nodeIds.length; i++) {
            BigInteger datapathId = InventoryDataServiceUtil.dataPathIdFromNodeId(nodeIds[i]);
            Assert.assertEquals(expectedDPIDs[i], datapathId.longValue());
        }
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#bigIntegerToPaddedHex(BigInteger)}.
     */
    @Test
    public void testLongToPaddedHex() {
        BigInteger[] dpids = new BigInteger[]{
                // 0x00000000 000004d2
                new BigInteger("1234"),
                // 0x8db2089e 01391a86
                new BigInteger("10210232779920710278"),
                // 0xffffffff ffffffff
                new BigInteger("18446744073709551615"),
        };

        String[] expectedPaddedHexes = new String[]{
                "00000000000004d2",
                "8db2089e01391a86",
                "ffffffffffffffff"
        };

        for (int i = 0; i < dpids.length; i++) {
            String datapathIdHex = InventoryDataServiceUtil.bigIntegerToPaddedHex(dpids[i]);
            Assert.assertEquals(expectedPaddedHexes[i], datapathIdHex);
        }
    }

}
