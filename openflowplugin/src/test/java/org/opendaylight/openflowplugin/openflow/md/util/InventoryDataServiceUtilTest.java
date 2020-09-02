/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValuesV10;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

@RunWith(MockitoJUnitRunner.class)
public class InventoryDataServiceUtilTest {
    private static final Uint32 PORT_NO = Uint32.valueOf(PortNumberValuesV10.CONTROLLER.getIntValue());
    private static final Uint64 PATH_ID = Uint64.valueOf(10);

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeUpdatedBuilderFromDataPathId(Uint64 datapathId)}.
     */
    @Test
    public void testNodeUpdatedBuilderFromDataPathId() {
        NodeUpdatedBuilder nodeUpdatedBuilder = InventoryDataServiceUtil.nodeUpdatedBuilderFromDataPathId(PATH_ID);
        assertNotNull(nodeUpdatedBuilder);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeConnectorBuilderFromDatapathIdPortNo(Uint64 datapathId,
     * Uint32 portNo, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion ofVersion)}.
     */
    @Test
    public void testNodeConnectorBuilderFromDatapathIdPortNo() {
        NodeConnectorBuilder nodeConnectorBuilder = InventoryDataServiceUtil
                .nodeConnectorBuilderFromDatapathIdPortNo(PATH_ID, PORT_NO, OpenflowVersion.OF10);
        assertNotNull(nodeConnectorBuilder);

        nodeConnectorBuilder = InventoryDataServiceUtil.nodeConnectorBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.OF13);
        assertNotNull(nodeConnectorBuilder);

        nodeConnectorBuilder = InventoryDataServiceUtil.nodeConnectorBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.UNSUPPORTED);
        assertNotNull(nodeConnectorBuilder);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeConnectorUpdatedBuilderFromDatapathIdPortNo(
     * Uint64 datapathId, Uint32 portNo, OpenflowVersion ofVersion)}.
     */
    @Test
    public void testNodeConnectorUpdatedBuilderFromDatapathIdPortNo() {
        NodeConnectorUpdatedBuilder nodeConnectorUpdatedBuilder = InventoryDataServiceUtil
                .nodeConnectorUpdatedBuilderFromDatapathIdPortNo(PATH_ID, PORT_NO, OpenflowVersion.OF10);
        assertNotNull(nodeConnectorUpdatedBuilder);

        nodeConnectorUpdatedBuilder = InventoryDataServiceUtil.nodeConnectorUpdatedBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.OF13);
        assertNotNull(nodeConnectorUpdatedBuilder);

        nodeConnectorUpdatedBuilder = InventoryDataServiceUtil.nodeConnectorUpdatedBuilderFromDatapathIdPortNo(PATH_ID,
                PORT_NO, OpenflowVersion.UNSUPPORTED);
        assertNotNull(nodeConnectorUpdatedBuilder);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeConnectorInstanceIdentifierFromDatapathIdPortno(
     * Uint64 datapathId, Uint32 portNo, OpenflowVersion ofVersion)}.
     */
    @Test
    public void testNodeConnectorInstanceIdentifierFromDatapathIdPortno() {
        InstanceIdentifier<NodeConnector> nodeConnectorInstanceIdentifier = InventoryDataServiceUtil
                .nodeConnectorInstanceIdentifierFromDatapathIdPortno(Uint64.ONE, PORT_NO, OpenflowVersion.OF10);
        assertNotNull(nodeConnectorInstanceIdentifier);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeConnectorRefFromDatapathIdPortno(
     * Uint64 datapathId, Uint32 portNo, OpenflowVersion ofVersion)}.
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

        long[] expectedDPIDs = new long[] { 1234L, -8236511293788841338L, -1L };

        for (int i = 0; i < nodeIds.length; i++) {
            Uint64 datapathId = InventoryDataServiceUtil.dataPathIdFromNodeId(nodeIds[i]);
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

        String[] expectedPaddedHexes = new String[] { "00000000000004d2", "8db2089e01391a86", "ffffffffffffffff" };

        for (int i = 0; i < dpids.length; i++) {
            String datapathIdHex = InventoryDataServiceUtil.bigIntegerToPaddedHex(dpids[i]);
            Assert.assertEquals(expectedPaddedHexes[i], datapathIdHex);
        }
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#portNoStringfromNodeConnectorID(String)}.
     */
    @Test
    public void testNodeConnectorIDToPortNoString() {
        String[] nodeConnectorIDs = new String[] { "openflow:2", "openflow:2:3411", "INPORT",
            "openflow:628192264910264962" };

        String[] expectedPortNoStrings = new String[] { "2", "3411", "INPORT", "628192264910264962" };

        for (int i = 0; i < nodeConnectorIDs.length; i++) {
            String portNoString = InventoryDataServiceUtil.portNoStringfromNodeConnectorID(nodeConnectorIDs[i]);
            Assert.assertEquals(expectedPortNoStrings[i], portNoString);
        }
    }
}
