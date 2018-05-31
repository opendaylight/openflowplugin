/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.PacketOutConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.ConnectionCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by Jakub Toth jatoth@cisco.com on 9/23/14.
 */

public class PacketOutConvertorTest {

    private ConvertorManager convertorManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    /**
     * Test for {@link PacketOutConvertor} with null parameters.
     */
    @Test
    public void toPacketOutInputAllParmNullTest() {

        TransmitPacketInputBuilder transmitPacketInputBuilder = new TransmitPacketInputBuilder();

        Long bufferId = null;

        String nodeId = "0";
        String port = "0";

        NodeRef ref = createNodeRef(nodeId);
        NodeConnectorKey nodeConnKey = PacketOutConvertorTest.createNodeConnKey(nodeId, port);
        NodeConnectorRef egressConfRef = new NodeConnectorRef(
                createNodeConnRef(nodeId, nodeConnKey));

        transmitPacketInputBuilder.setBufferId(bufferId);
        transmitPacketInputBuilder.setConnectionCookie(null);
        transmitPacketInputBuilder.setAction(null);
        transmitPacketInputBuilder.setNode(ref);
        transmitPacketInputBuilder.setPayload(null);
        transmitPacketInputBuilder.setEgress(egressConfRef);
        transmitPacketInputBuilder.setIngress(null);
        TransmitPacketInput transmitPacketInput = transmitPacketInputBuilder
                .build();

        Short version = (short) 0x04;
        Long xid = null;
        PacketOutConvertorData data = new PacketOutConvertorData(version);
        PacketOutInput message = convert(transmitPacketInput, data);

        //FIXME : this has to be fixed along with actions changed in openflowjava

        Assert.assertEquals(buildActionForNullTransmitPacketInputAction(nodeConnKey, version), message.getAction());

        Assert.assertEquals(OFConstants.OFP_NO_BUFFER, message.getBufferId());
        Assert.assertEquals(new PortNumber(0xfffffffdL), message.getInPort());
        Assert.assertEquals(version, message.getVersion());
        Assert.assertEquals(xid, message.getXid());
        Assert.assertArrayEquals(transmitPacketInput.getPayload(), message.getData());
    }

    /**
     * Test for PacketOutConvertor.
     */
    @Test
    public void toPacketOutInputAllParmTest() {
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder ab =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(OFConstants.OFPCML_NO_BUFFER);
        Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(
                output.build()).build());
        ab.setOrder(0);
        ab.withKey(new ActionKey(0));

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionList =
                new ArrayList<>();
        actionList.add(ab.build());

        Long bufferId = 0xfL;

        Long valueForCookie = 0xeL;
        ConnectionCookie connCook = new ConnectionCookie(valueForCookie);

        String nodeId = "node:1";

        NodeRef ref = createNodeRef(nodeId);

        String portO = "0xfffffffd";
        NodeConnectorKey egrConKey = PacketOutConvertorTest.createNodeConnKey(nodeId, portO);
        NodeConnectorRef egressConfRef = new NodeConnectorRef(
                createNodeConnRef(nodeId, egrConKey));

        String inPort = "2";
        NodeConnectorKey ingrConKey = PacketOutConvertorTest.createNodeConnKey(nodeId, inPort);
        NodeConnectorRef ingressConRef = new NodeConnectorRef(
                createNodeConnRef(nodeId, ingrConKey));

        String string = new String("sendOutputMsg_TEST");
        byte[] msg = string.getBytes();

        byte[] payload = msg;

        TransmitPacketInputBuilder transmitPacketInputBuilder = new TransmitPacketInputBuilder();
        transmitPacketInputBuilder.setAction(actionList);
        transmitPacketInputBuilder.setBufferId(bufferId);
        transmitPacketInputBuilder.setConnectionCookie(connCook);
        transmitPacketInputBuilder.setEgress(egressConfRef);
        transmitPacketInputBuilder.setIngress(ingressConRef);
        transmitPacketInputBuilder.setNode(ref);
        transmitPacketInputBuilder.setPayload(payload);

        final TransmitPacketInput transmitPacketInput = transmitPacketInputBuilder.build();

        Short version = (short) 0x04;
        byte[] datapathIdByte = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        for (int i = 0; i < datapathIdByte.length; i++) {
            datapathIdByte[i] = 1;
        }
        BigInteger datapathId = new BigInteger(1, datapathIdByte);
        Long xid = 0xfffffL;

        PacketOutConvertorData data = new PacketOutConvertorData(version);
        data.setXid(xid);
        data.setDatapathId(datapathId);
        PacketOutInput message = convert(transmitPacketInput, data);

        Assert.assertEquals(transmitPacketInput.getBufferId(), message.getBufferId());
        Assert.assertEquals(Long.valueOf(inPort), message.getInPort().getValue());
        Assert.assertEquals(version, message.getVersion());
        Assert.assertEquals(xid, message.getXid());

        ActionConvertorData actionConvertorData = new ActionConvertorData(version);
        actionConvertorData.setDatapathId(datapathId);

        Optional<List<Action>> actionsOptional = convertorManager.convert(actionList, actionConvertorData);

        List<Action> actions = actionsOptional.orElse(Collections.emptyList());
        Assert.assertEquals(actions, message.getAction());
        Assert.assertArrayEquals(transmitPacketInput.getPayload(), message.getData());
    }

    private static List<Action> buildActionForNullTransmitPacketInputAction(
            final NodeConnectorKey nodeConKey, final short version) {

        PortNumber outPort = getPortNumber(nodeConKey, version);

        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice
            .OutputActionCaseBuilder outputActionCaseBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping
                    .action.choice.OutputActionCaseBuilder();

        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice
            .output.action._case.OutputActionBuilder outputActionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping
                    .action.choice.output.action._case.OutputActionBuilder();

        outputActionBuilder.setPort(outPort);
        outputActionBuilder.setMaxLength(0xffff);
        outputActionCaseBuilder.setOutputAction(outputActionBuilder.build());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(outputActionCaseBuilder.build());

        List<Action> actions = new ArrayList<>();
        actions.add(actionBuilder.build());
        return actions;
    }

    private static PortNumber getPortNumber(final NodeConnectorKey nodeConKey, final Short ofVersion) {
        Long port = InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.get(ofVersion),
                nodeConKey.getId());
        return new PortNumber(port);
    }

    private static NodeConnectorRef createNodeConnRef(final String nodeId, final NodeConnectorKey nodeConKey) {
        InstanceIdentifier<NodeConnector> path = InstanceIdentifier
                .<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class,
                        new NodeKey(new NodeId(nodeId)))
                .<NodeConnector, NodeConnectorKey>child(NodeConnector.class,
                        nodeConKey).build();

        return new NodeConnectorRef(path);
    }

    private static NodeConnectorKey createNodeConnKey(final String nodeId, final String port) {
        StringBuilder builder = new StringBuilder(nodeId).append(':').append(port);

        return new NodeConnectorKey(new NodeConnectorId(builder.toString()));
    }

    private static NodeRef createNodeRef(final String nodeId) {
        NodeKey key = new NodeKey(new NodeId(nodeId));
        InstanceIdentifier<Node> path = InstanceIdentifier
                .<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class, key).build();
        return new NodeRef(path);
    }

    private PacketOutInput convert(TransmitPacketInput transmitPacketInput, PacketOutConvertorData data) {
        Optional<PacketOutInput> messageOptional = convertorManager.convert(transmitPacketInput, data);
        return messageOptional.orElse(PacketOutConvertor.defaultResult(data.getVersion()));
    }
}
