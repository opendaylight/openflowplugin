/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * 
 */
public class InventoryDataServiceUtilTest {

    /**
     * Test method for {@link InventoryDataServiceUtil#dataPathIdFromNodeId(NodeId)}.
     */
    @Test
    public void testDataPathIdFromNodeId() {
        String string = "openflow:";
        NodeId[] nodeIds = new NodeId[] {
                // 0x00000000 000004d2
                new NodeId(string + "1234"),
                // 0x8db2089e 01391a86
                new NodeId(string + "10210232779920710278"),
                // 0xffffffff ffffffff
                new NodeId(string + "18446744073709551615"),
        };
        
        long[] expectedDPIDs = new long[] {
                1234L,
                -8236511293788841338L,
                -1L
        };
        
        for (int i = 0; i < nodeIds.length; i++) {
            BigInteger datapathId = InventoryDataServiceUtil.dataPathIdFromNodeId(nodeIds[i] );
            Assert.assertEquals(expectedDPIDs[i], datapathId.longValue());
        }
    }
    
    /**
     * Test method for {@link InventoryDataServiceUtil#bigIntegerToPaddedHex(BigInteger)}.
     */
    @Test
    public void testLongToPaddedHex() {
        BigInteger[] dpids = new BigInteger[] {
                // 0x00000000 000004d2
                new BigInteger("1234"),
                // 0x8db2089e 01391a86
                new BigInteger("10210232779920710278"),
                // 0xffffffff ffffffff
                new BigInteger("18446744073709551615"),
        };
        
        String[] expectedPaddedHexes = new String[] {
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
