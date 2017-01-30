/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Test for {@link org.opendaylight.openflowplugin.impl.services.sal.PacketProcessingServiceImpl}.
 */
public class PacketProcessingServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_XID_VALUE = 100L;
    public static final String ULTIMATE_PAYLOAD = "What do you get when you multiply six by nine?";

    private PacketProcessingServiceImpl packetProcessingService;
    private KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> pathToNodeconnector;

    @Override
    protected void setup() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        packetProcessingService = new PacketProcessingServiceImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
        pathToNodeconnector = KeyedInstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("ofp-ut:123")))
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId("ofp-ut:123:1")));
    }

    @Test
    public void testTransmitPacket() throws Exception {
        TransmitPacketInput transmitPacketInput = buildTransmitPacketInput();
        packetProcessingService.transmitPacket(transmitPacketInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() throws Exception {
        TransmitPacketInput transmitPacketInput = buildTransmitPacketInput();

        final OfHeader request = packetProcessingService.buildRequest(new Xid(DUMMY_XID_VALUE), transmitPacketInput);
        assertEquals(DUMMY_XID_VALUE, request.getXid());
        assertTrue(request instanceof PacketOutInput);
        final PacketOutInput input = (PacketOutInput) request;
        assertEquals(OFConstants.OFP_NO_BUFFER, input.getBufferId());
        assertEquals(1, input.getAction().size());
        assertEquals(OutputActionCase.class, input.getAction().get(0).getActionChoice().getImplementedInterface());

        final OutputActionCase actionChoice = (OutputActionCase) input.getAction().get(0).getActionChoice();
        assertEquals(1, actionChoice.getOutputAction().getPort().getValue().intValue());
        assertEquals(ULTIMATE_PAYLOAD, new String(input.getData()));
    }

    private TransmitPacketInput buildTransmitPacketInput() {
        TransmitPacketInputBuilder transmitPacketInputBld = new TransmitPacketInputBuilder()
                .setBufferId(OFConstants.OFP_NO_BUFFER)
                .setNode(new NodeRef(mockedDeviceInfo.getNodeInstanceIdentifier()))
                .setPayload(ULTIMATE_PAYLOAD.getBytes())
                .setEgress(new NodeConnectorRef(pathToNodeconnector));
        return transmitPacketInputBld.build();
    }
}
