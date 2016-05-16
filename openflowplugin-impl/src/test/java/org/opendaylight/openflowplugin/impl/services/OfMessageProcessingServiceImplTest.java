/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.math.BigInteger;
import java.util.List;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.ConnectionCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.GetOfVersionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.TransmitOfMessageInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.of.message.service.rev160511.TransmitOfMessageInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Test for {@link OfMessageProcessingServiceImpl}.
 */
public class OfMessageProcessingServiceImplTest extends ServiceMocking {
    private OfMessageProcessingServiceImpl ofMessageProcessingService;
    private KeyedInstanceIdentifier<NodeConnector, NodeConnectorKey> pathToNodeconnector;

    // FlowModInput message
    String payload = "04 0e 00 80 01 02 03 04 ff 01 04 01 06 00 07 01 ff 05 00 00 09 30 00 "
            + "30 41 02 00 0c 00 00 00 7e 00 00 00 02 00 00 11 46 00 00 00 62 00 0b 00 00 00 01 "
            + "00 11 80 00 02 04 00 00 00 2a 80 00 12 01 04 00 00 00 00 00 00 00 00 01 00 08 2b 00 "
            + "00 00 00 02 00 18 00 00 00 00 ff 01 04 01 06 00 07 01 ff 05 00 00 09 30 00 30 00 04 00 "
            + "18 00 00 00 00 00 00 00 10 00 00 00 2a 00 34 00 00 00 00 00 00";

    private static final Long DUMMY_XID_VALUE = 16909060L;

    @Override
    protected void setup() {
        ofMessageProcessingService = new OfMessageProcessingServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        pathToNodeconnector = KeyedInstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("ofp-ut:123")))
                .child(NodeConnector.class, new NodeConnectorKey(new NodeConnectorId("ofp-ut:123:1")));
        OpenflowPortsUtil.init();
    }

    @Test
    public void testTransmitOfMessage() throws Exception {
        TransmitOfMessageInput transmitOfMessageInput = buildTransmitOfMessageInput();
        ofMessageProcessingService.transmitOfMessage(transmitOfMessageInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testGetOfVersion() throws Exception {
        GetOfVersionInputBuilder inputBld = new GetOfVersionInputBuilder();
        inputBld.setNode(new NodeRef(mockedDeviceState.getNodeInstanceIdentifier()));
        ofMessageProcessingService.getOfVersion(inputBld.build());
        verify(mockedDeviceState).getVersion();
    }

    @Test
    public void testBuildRequest() throws Exception {
        TransmitOfMessageInput transmitOfMessageInput = buildTransmitOfMessageInput();
        final OfHeader request = ofMessageProcessingService.buildRequest(new Xid(DUMMY_XID_VALUE), transmitOfMessageInput);
        assertEquals(DUMMY_XID_VALUE, request.getXid());
        assertTrue(request instanceof FlowModInput);
        final FlowModInput input = (FlowModInput) request;
        byte[] cookieMask = new byte[] { (byte) 0xFF, 0x05, 0x00, 0x00, 0x09, 0x30, 0x00, 0x30 };
        assertEquals("Wrong cookie mask", new BigInteger(1, cookieMask), input.getCookieMask());
        assertEquals("Wrong table id", new TableId(65L), input.getTableId());
        assertEquals("Wrong command", FlowModCommand.forValue(2), input.getCommand());
        assertEquals("Wrong idle timeout", 12, input.getIdleTimeout().intValue());
        assertEquals("Wrong hard timeout", 0, input.getHardTimeout().intValue());
        assertEquals("Wrong priority", 126, input.getPriority().intValue());
        assertEquals("Wrong buffer id ", 2L, input.getBufferId().longValue());
        assertEquals("Wrong out port", new PortNumber(4422L), input.getOutPort());
        assertEquals("Wrong out group", 98L, input.getOutGroup().longValue());
    }

    private TransmitOfMessageInput buildTransmitOfMessageInput() {
        TransmitOfMessageInputBuilder transmitOfMessageInputBld = new TransmitOfMessageInputBuilder()
                .setNode(new NodeRef(mockedDeviceState.getNodeInstanceIdentifier()))
                .setConnectionCookie(new ConnectionCookie(0L))
                .setIngress(new NodeRef(KeyedInstanceIdentifier.create(Nodes.class)))
                .setMessage(fromStringToByteArray(payload))
                .setEgress(new NodeConnectorRef(pathToNodeconnector));
        return transmitOfMessageInputBld.build();
    }

    private byte[] fromStringToByteArray(String input){
        final Splitter splitter =  Splitter.onPattern("\\s+").omitEmptyStrings();
        List<String> byteChips = Lists.newArrayList(splitter.split(input));
        byte[] result = new byte[byteChips.size()];
        int i = 0;
        for (String chip : byteChips) {
            result[i] = (byte) Short.parseShort(chip, 16);
            i++;
        }
        return result;
    }
}
