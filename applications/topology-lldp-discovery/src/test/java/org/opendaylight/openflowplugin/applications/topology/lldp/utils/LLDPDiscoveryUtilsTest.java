/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.topology.lldp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link LLDPDiscoveryUtils}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LLDPDiscoveryUtilsTest {

    private static final Logger LOG = LoggerFactory.getLogger(LLDPDiscoveryUtilsTest.class);

    @Test
    public void testLldpToNodeConnectorRefLLDP() throws Exception {
        byte[] packetLLDP = {
                0x01, 0x23, 0x00, 0x00, 0x00, 0x01, (byte) 0x8a, (byte) 0x8e,
                (byte) 0xcc, (byte) 0x85, (byte) 0xeb, 0x27,
                /* ethernet type LLDP 0x88cc */(byte) 0x88, (byte) 0xcc,
                0x02, 0x07, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x04,
                0x02, 0x07, 0x32, 0x06, 0x02, 0x13, 0x37, 0x0a, 0x0a,
                /* openflow:2 */0x6f, 0x70, 0x65, 0x6e, 0x66, 0x6c, 0x6f, 0x77, 0x3a, 0x32,
                (byte) 0xfe, 0x10, 0x00, 0x26, (byte) 0xe1, 0x00,
                /* openflow:2:2 */0x6f, 0x70, 0x65, 0x6e, 0x66, 0x6c, 0x6f, 0x77, 0x3a, 0x32, 0x3a, 0x32,
                (byte) 0xfe, 0x14, 0x00, 0x26, (byte) 0xe1, 0x01, 0x62, (byte) 0xc8, 0x2b, 0x67, (byte) 0xce,
                (byte) 0xbe, 0x7c, 0x2b, 0x47, (byte) 0xbe, 0x2b, (byte) 0xe7, (byte) 0xbc,
                (byte) 0xe9, 0x75, 0x3d, 0x00, 0x00
        };

        NodeConnectorRef nodeConnectorRef = LLDPDiscoveryUtils.lldpToNodeConnectorRef(packetLLDP, false);

        NodeKey nodeKey = nodeConnectorRef.getValue().firstKeyOf(Node.class, NodeKey.class);
        NodeConnectorKey nodeConnectorKey = nodeConnectorRef.getValue().firstKeyOf(NodeConnector.class,
                NodeConnectorKey.class);

        assertEquals(nodeKey.getId().getValue(), "openflow:2");
        assertEquals(nodeConnectorKey.getId().getValue(), "openflow:2:2");
    }

    @Test
    public void testLldpToNodeConnectorRefNotLLDP() throws Exception {
        byte[] packetNotLLDP = {
                0x01, 0x23, 0x00, 0x00, 0x00, 0x01, (byte) 0x8a, (byte) 0x8e,
                (byte) 0xcc, (byte) 0x85, (byte) 0xeb, 0x27,
                /* ethernet type IPv4 0x0800 */(byte) 0x08, (byte) 0x00,
                0x02, 0x07, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x04,
                0x02, 0x07, 0x32, 0x06, 0x02, 0x13, 0x37, 0x0a,
                0x0a, 0x6f, 0x70, 0x65, 0x6e, 0x66, 0x6c, 0x6f,
                0x77, 0x3a, 0x32, (byte) 0xfe, 0x10, 0x00, 0x26, (byte) 0xe1,
                0x00, 0x6f, 0x70, 0x65, 0x6e, 0x66, 0x6c, 0x6f,
                0x77, 0x3a, 0x32, 0x3a, 0x32, (byte) 0xfe, 0x14, 0x00,
                0x26, (byte) 0xe1, 0x01, 0x62, (byte) 0xc8, 0x2b, 0x67, (byte) 0xce,
                (byte) 0xbe, 0x7c, 0x2b, 0x47, (byte) 0xbe, 0x2b, (byte) 0xe7, (byte) 0xbc,
                (byte) 0xe9, 0x75, 0x3d, 0x00, 0x00
        };

        NodeConnectorRef nodeConnectorRef = LLDPDiscoveryUtils.lldpToNodeConnectorRef(packetNotLLDP, false);

        assertNull(nodeConnectorRef);
    }
}