/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValuesV10;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class InventoryDataServiceUtilTest {


    private static final long PORT_NO = PortNumberValuesV10.CONTROLLER.getIntValue();
    private static final BigInteger PATH_ID = BigInteger.TEN;

    @Mock
    DataBroker dataBroker;
    @Mock
    ReadOnlyTransaction readOnlyTransaction;
    @Mock
    WriteTransaction writeTransaction;
    @Mock
    Nodes nodes;
    @Mock
    Node node;


    @Before
    public void setupEnvironment() {
        when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        when(readOnlyTransaction.read(Mockito.any(LogicalDatastoreType.class), Mockito.<InstanceIdentifier<Nodes>>any())).thenReturn(Futures.immediateCheckedFuture(Optional.of(nodes)));

        OFSessionUtil.getSessionManager().setDataBroker(dataBroker);

    }

    @Test
    /**
     * Primitive test method for {@link InventoryDataServiceUtil#putNodeConnector(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId, org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector)} ()}.
     */
    public void testPutNodeConnector(){
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);

        NodeId nodeId = new NodeId("1");
        NodeConnectorBuilder nodeConnectorBuilder = new NodeConnectorBuilder();
        NodeConnectorId nodeConnectorId = new NodeConnectorId("1");
        nodeConnectorBuilder.setId(nodeConnectorId );
        nodeConnectorBuilder.setKey(new NodeConnectorKey(nodeConnectorId ));
        InventoryDataServiceUtil.putNodeConnector(nodeId, nodeConnectorBuilder.build());
    }

    /**
     * Primitive test method for {@link InventoryDataServiceUtil#readNode(org.opendaylight.yangtools.yang.binding.InstanceIdentifier)} ()}.
     */
    @Test
    public void testReadNode(){
        when(readOnlyTransaction.read(Mockito.any(LogicalDatastoreType.class), Mockito.<InstanceIdentifier<Node>>any())).thenReturn(Futures.immediateCheckedFuture(Optional.of(node)));
        InstanceIdentifier<Node> instanceId = InstanceIdentifier.create(Node.class);
        Node node = InventoryDataServiceUtil.readNode(instanceId);
        assertNotNull(node);
    }
    /**
     * Test method for {@link InventoryDataServiceUtil#checkForNodes()}.
     */
    @Test
    public void testCheckForNodes() {
        Nodes nodes = InventoryDataServiceUtil.checkForNodes();
        assertNotNull(nodes);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#readAllNodes()}.
     */
    @Test
    public void testReadAllNodes() {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node> nodes = InventoryDataServiceUtil.readAllNodes();
        assertNotNull(nodes);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeUpdatedBuilderFromDataPathId(BigInteger datapathId)}.
     */
    @Test
    public void testNodeUpdatedBuilderFromDataPathId() {
        NodeUpdatedBuilder nodeUpdatedBuilder = InventoryDataServiceUtil.nodeUpdatedBuilderFromDataPathId(PATH_ID);
        assertNotNull(nodeUpdatedBuilder);
    }

    /**
     * Test method for {@link InventoryDataServiceUtil#nodeConnectorBuilderFromDatapathIdPortNo(BigInteger datapathId,
     * Long portNo, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion ofVersion)}.
     */
    @Test
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
    @Test
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

    /**
     * Test method for {@link InventoryDataServiceUtil#portNoStringfromNodeConnectorID(String)}.
     */
    @Test
    public void testNodeConnectorIDToPortNoString() {
    	String[] nodeConnectorIDs = new String[]{
    			"openflow:2",
    			"openflow:2:3411",
    			"INPORT",
    			"openflow:628192264910264962"
    	};

    	String[] expectedPortNoStrings = new String[]{
    			"2",
    			"3411",
    			"INPORT",
    			"628192264910264962"
    	};

    	for (int i = 0; i < nodeConnectorIDs.length; i++) {
    		String portNoString = InventoryDataServiceUtil.portNoStringfromNodeConnectorID(nodeConnectorIDs[i]);
    		Assert.assertEquals(expectedPortNoStrings[i], portNoString);
    	}
    }
}
