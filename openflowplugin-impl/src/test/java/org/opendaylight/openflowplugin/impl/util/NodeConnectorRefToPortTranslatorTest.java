/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

@RunWith(MockitoJUnitRunner.class)
public class NodeConnectorRefToPortTranslatorTest extends TestCase {

    private static final String PACKET_DATA = "Test_Data";
    private static final Uint32 PORT_NO = Uint32.valueOf(5L);
    private static final Uint64 DATA_PATH_ID = Uint64.TEN;
    private static final short OF_VERSION = OFConstants.OFP_VERSION_1_3;
    private static final Uint32 TABLE_ID = Uint32.valueOf(42);

    private static PacketIn createPacketIn(final Uint32 portNo) {
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
                .setCookie(Uint64.ZERO)
                .setTableId(new TableId(TABLE_ID))
                .build();
    }

    @Test
    public void testGetPortNoFromPacketIn() {
        PacketIn packetIn = createPacketIn(PORT_NO);
        Uint32 portNo = NodeConnectorRefToPortTranslator.getPortNoFromPacketIn(packetIn);
        assertEquals(portNo, PORT_NO);
    }

    @Test
    public void testNodeConnectorConversion() {
        // Mock the packet in message
        PacketIn packetIn = createPacketIn(PORT_NO);

        // Convert PacketIn to NodeConnectorRef
        NodeConnectorRef ref = NodeConnectorRefToPortTranslator.toNodeConnectorRef(packetIn, DATA_PATH_ID);

        // Get port number from created NodeConnectorRef
        Uint32 refPort = NodeConnectorRefToPortTranslator.fromNodeConnectorRef(ref, OF_VERSION);

        // Check if we got the correct port number
        assertEquals(PORT_NO, refPort);

        // Check if 2 NodeConnectorRef created from same PacketIn have same value
        assertEquals(ref, NodeConnectorRefToPortTranslator.toNodeConnectorRef(packetIn, DATA_PATH_ID));

        // Check if 2 NodeConnectorRef created from same PacketIn but different datapaths do not have same value
        assertNotSame(ref, NodeConnectorRefToPortTranslator.toNodeConnectorRef(packetIn, Uint64.ONE));
    }
}
