/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.collect.Lists;
import java.math.BigInteger;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
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

/**
 * Created by Tomas Slusny on 24.3.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeConnectorRefToPortTranslatorTest extends TestCase {

    static final String PACKET_DATA = "Test_Data";
    static final Long PORT_NO = 5l;
    static final Long SECOND_PORT_NO = 6l;
    static final BigInteger DATA_PATH_ID = BigInteger.TEN;
    static final short OF_VERSION = OFConstants.OFP_VERSION_1_3;
    static final String ID_VALUE = "openflow:" + DATA_PATH_ID;
    static final Long TABLE_ID = 42L;

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
                .setTableId(new TableId(TABLE_ID))
                .build();
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
        NodeConnectorRef ref = NodeConnectorRefToPortTranslator.toNodeConnectorRef(packetIn, DATA_PATH_ID);

        // Get port number from created NodeConnectorRef
        Long refPort = NodeConnectorRefToPortTranslator.fromNodeConnectorRef(ref, OF_VERSION);

        // Check if we got the correct port number
        assertEquals(PORT_NO, refPort);

        // Check if 2 NodeConnectorRef created from same PacketIn have same value
        assertEquals(ref, NodeConnectorRefToPortTranslator.toNodeConnectorRef(packetIn, DATA_PATH_ID));

        // Check if 2 NodeConnectorRef created from same PacketIn but different datapaths do not have same value
        assertNotSame(ref, NodeConnectorRefToPortTranslator.toNodeConnectorRef(packetIn, BigInteger.ONE));
    }
}
